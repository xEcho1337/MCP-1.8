package it.echo.kira.account;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

/**
 * Stores accounts on disk, swaps the live session, refreshes Microsoft logins.
 */
public final class AccountManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "kira_accounts.json";

    private final File file;
    private final List<Account> accounts = new ArrayList<>();
    private String selectedName;

    public AccountManager(File mcDataDir) {
        this.file = new File(mcDataDir, FILE_NAME);
    }

    /** Loads saved accounts and re-applies the selected one without blocking. */
    public synchronized void load() {
        accounts.clear();
        selectedName = null;
        if (!file.isFile()) {
            return;
        }
        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (root.has("selected") && !root.get("selected").isJsonNull()) {
                selectedName = root.get("selected").getAsString();
            }
            if (root.has("accounts") && root.get("accounts").isJsonArray()) {
                JsonArray array = root.getAsJsonArray("accounts");
                for (JsonElement element : array) {
                    JsonObject obj = element.getAsJsonObject();
                    Account.Type type = Account.Type.valueOf(obj.get("type").getAsString());
                    String name = obj.get("name").getAsString();
                    String uuid = obj.get("uuid").getAsString();
                    String refresh = obj.has("refreshToken") && !obj.get("refreshToken").isJsonNull()
                            ? obj.get("refreshToken").getAsString()
                            : null;
                    accounts.add(new Account(type, name, uuid, refresh));
                }
            }
        } catch (Exception e) {
            System.err.println("[Kira] Failed to load accounts: " + e);
        }
    }

    public synchronized void save() {
        JsonObject root = new JsonObject();
        root.addProperty("selected", selectedName);
        JsonArray array = new JsonArray();
        for (Account account : accounts) {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", account.type().name());
            obj.addProperty("name", account.name());
            obj.addProperty("uuid", account.uuid());
            if (account.refreshToken() != null) {
                obj.addProperty("refreshToken", account.refreshToken());
            }
            array.add(obj);
        }
        root.add("accounts", array);
        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        } catch (Exception e) {
            System.err.println("[Kira] Failed to save accounts: " + e);
        }
    }

    /** Re-logs the previously selected account in the background (network may block). */
    public void autoLogin() {
        Account selected;
        synchronized (this) {
            selected = byName(selectedName);
        }
        if (selected == null) {
            return;
        }
        if (selected.type() == Account.Type.OFFLINE) {
            try {
                login(selected);
            } catch (Exception e) {
                System.err.println("[Kira] Auto-login failed: " + e);
            }
            return;
        }
        Thread thread = new Thread(() -> {
            try {
                refreshAndLogin(selected);
            } catch (Exception e) {
                System.err.println("[Kira] Auto-login failed for " + selected.name() + ": " + e.getMessage());
            }
        }, "Kira-Account-Refresh");
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized List<Account> all() {
        return new ArrayList<>(accounts);
    }

    public synchronized Account byName(String name) {
        if (name == null) {
            return null;
        }
        for (Account account : accounts) {
            if (account.name().equalsIgnoreCase(name)) {
                return account;
            }
        }
        return null;
    }

    public synchronized boolean isSelected(Account account) {
        return selectedName != null && selectedName.equalsIgnoreCase(account.name());
    }

    public synchronized Account addOffline(String name) {
        validateOfflineName(name);
        if (byName(name) != null) {
            throw new IllegalArgumentException("Account already exists");
        }
        String uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8)).toString();
        Account account = new Account(Account.Type.OFFLINE, name, uuid, null);
        accounts.add(account);
        save();
        return account;
    }

    public synchronized Account addMicrosoft(MicrosoftAuth.LoginResult result, String refreshToken) {
        Account existing = byName(result.name());
        if (existing != null && existing.type() == Account.Type.MICROSOFT) {
            existing.uuid(result.uuid());
            existing.refreshToken(refreshToken);
            save();
            return existing;
        }
        Account account = new Account(Account.Type.MICROSOFT, result.name(), result.uuid(), refreshToken);
        accounts.add(account);
        save();
        return account;
    }

    public synchronized boolean remove(Account account) {
        boolean removed = accounts.remove(account);
        if (removed) {
            if (isSelected(account)) {
                selectedName = null;
            }
            save();
        }
        return removed;
    }

    /** Applies an offline account immediately. */
    public void login(Account account) {
        if (account.type() != Account.Type.OFFLINE) {
            throw new IllegalArgumentException("Use refreshAndLogin for Microsoft accounts");
        }
        applySession(account.name(), account.uuid(), "0");
        synchronized (this) {
            selectedName = account.name();
            save();
        }
    }

    /** Refreshes the Microsoft token chain (network) and applies the new session. */
    public Account refreshAndLogin(Account account) throws Exception {
        if (account.type() != Account.Type.MICROSOFT) {
            throw new IllegalArgumentException("Not a Microsoft account");
        }
        if (account.refreshToken() == null) {
            throw new IllegalStateException("No refresh token, log in again");
        }
        MicrosoftAuth.LoginResult result = MicrosoftAuth.loginWithRefreshToken(account.refreshToken());
        String newRefresh = result.refreshToken() != null ? result.refreshToken() : account.refreshToken();
        synchronized (this) {
            account.name(result.name());
            account.uuid(result.uuid());
            account.refreshToken(newRefresh);
            selectedName = account.name();
            save();
        }
        applySession(result.name(), result.uuid(), result.mcToken());
        return account;
    }

    public static void validateOfflineName(String name) {
        if (name == null || name.length() < 3 || name.length() > 16
                || !name.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("Name must be 3-16 chars (A-Z, 0-9, _)");
        }
    }

    /** Replaces the live session so servers see the new account. */
    public static void applySession(String name, String uuid, String token) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null) {
            throw new IllegalStateException("Minecraft not ready");
        }
        String dashed = uuid.toLowerCase(Locale.ROOT);
        Session session = new Session(name, dashed, token, "mojang");
        try {
            Field field = Minecraft.class.getDeclaredField("session");
            field.setAccessible(true);
            field.set(minecraft, session);
        } catch (Exception e) {
            throw new IllegalStateException("Could not swap session", e);
        }
    }

    public static String currentName() {
        Minecraft minecraft = Minecraft.getMinecraft();
        return minecraft == null ? "-" : minecraft.getSession().getUsername();
    }
}
