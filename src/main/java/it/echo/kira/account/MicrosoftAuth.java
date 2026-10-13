package it.echo.kira.account;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Microsoft OAuth device-code flow plus Xbox Live / Minecraft Services calls.
 * No authlib support for Microsoft exists in 1.8, so this is plain HTTPS + Gson.
 */
public final class MicrosoftAuth {

    /** Public Azure app id used by open-source launchers (PrismLauncher). */
    public static final String CLIENT_ID = "c36a9fb6-4f2a-41ff-90bd-ae7cc92031eb";
    private static final String SCOPE = "XboxLive.SignIn XboxLive.offline_access";

    private static final String DEVICE_CODE_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode";
    private static final String TOKEN_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
    private static final String XBL_URL = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String XSTS_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String MC_LOGIN_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String MC_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";

    private MicrosoftAuth() {
    }

    public record DeviceCode(String userCode, String verificationUri, String deviceCode, int intervalSeconds) {
    }

    public record LoginResult(String name, String uuid, String mcToken, String refreshToken) {
    }

    public interface Cancel {
        boolean cancelled();
    }

    public static DeviceCode beginDeviceFlow() throws IOException {
        Map<String, String> form = new HashMap<>();
        form.put("client_id", CLIENT_ID);
        form.put("scope", SCOPE);
        JsonObject json = postForm(DEVICE_CODE_URL, form);
        String verificationUri = firstPresent(json, "verification_uri", "verification_url");
        requirePresent(json, "user_code");
        requirePresent(json, "device_code");
        if (verificationUri == null) {
            throw new IOException("Microsoft returned a device flow response without verification URI");
        }
        return new DeviceCode(
                json.get("user_code").getAsString(),
                verificationUri,
                json.get("device_code").getAsString(),
                json.has("interval") ? json.get("interval").getAsInt() : 5);
    }

    /**
     * Polls until the user approves, polling fails, or cancelled.
     *
     * @return tokens, or null when cancelled
     */
    public static Tokens pollForTokens(DeviceCode device, Cancel cancel) throws IOException, InterruptedException {
        while (true) {
            if (cancel.cancelled()) {
                return null;
            }
            Thread.sleep(device.intervalSeconds() * 1000L);
            if (cancel.cancelled()) {
                return null;
            }
            Map<String, String> form = new HashMap<>();
            form.put("grant_type", "urn:ietf:params:oauth:grant-type:device_code");
            form.put("client_id", CLIENT_ID);
            form.put("device_code", device.deviceCode());
            JsonObject json = postFormLenient(TOKEN_URL, form);
            if (json.has("access_token")) {
                return new Tokens(json.get("access_token").getAsString(),
                        json.has("refresh_token") ? json.get("refresh_token").getAsString() : null);
            }
            String error = json.has("error") ? json.get("error").getAsString() : "";
            if (!error.equals("authorization_pending") && !error.equals("slow_down")) {
                throw new IOException("Microsoft login failed: " + describeError(json));
            }
        }
    }

    public static Tokens refresh(String refreshToken) throws IOException {
        Map<String, String> form = new HashMap<>();
        form.put("grant_type", "refresh_token");
        form.put("client_id", CLIENT_ID);
        form.put("refresh_token", refreshToken);
        form.put("scope", SCOPE);
        JsonObject json = postForm(TOKEN_URL, form);
        return new Tokens(json.get("access_token").getAsString(),
                json.has("refresh_token") ? json.get("refresh_token").getAsString() : refreshToken);
    }

    public record Tokens(String accessToken, String refreshToken) {
    }

    public static LoginResult loginWithMicrosoftToken(String msToken) throws IOException {
        // 1. Xbox Live
        JsonObject xblProps = new JsonObject();
        xblProps.addProperty("AuthMethod", "RPS");
        xblProps.addProperty("SiteName", "user.auth.xboxlive.com");
        xblProps.addProperty("RpsTicket", "d=" + msToken);
        JsonObject xblBody = new JsonObject();
        xblBody.addProperty("RelyingParty", "http://auth.xboxlive.com");
        xblBody.addProperty("TokenType", "JWT");
        xblBody.add("Properties", xblProps);
        JsonObject xbl = postJson(XBL_URL, xblBody);
        String xblToken = xbl.get("Token").getAsString();

        // 2. XSTS
        JsonObject xstsProps = new JsonObject();
        xstsProps.addProperty("SandboxId", "RETAIL");
        var userTokens = new com.google.gson.JsonArray();
        userTokens.add(xblToken);
        xstsProps.add("UserTokens", userTokens);
        JsonObject xstsBody = new JsonObject();
        xstsBody.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
        xstsBody.addProperty("TokenType", "JWT");
        xstsBody.add("Properties", xstsProps);
        JsonObject xsts = postJson(XSTS_URL, xstsBody);
        String xstsToken = xsts.get("Token").getAsString();
        String userHash = xsts.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0)
                .getAsJsonObject().get("uhs").getAsString();

        // 3. Minecraft Services login
        JsonObject mcBody = new JsonObject();
        mcBody.addProperty("identityToken", "XBL3.0 x=" + userHash + ";" + xstsToken);
        JsonObject mcLogin = postJson(MC_LOGIN_URL, mcBody);
        String mcToken = mcLogin.get("access_token").getAsString();

        // 4. Profile
        JsonObject profile = getJson(MC_PROFILE_URL, mcToken);
        String name = profile.get("name").getAsString();
        String uuid = dashed(profile.get("id").getAsString());

        // refresh token is only returned by the token endpoint, not here; caller fills it in
        return new LoginResult(name, uuid, mcToken, null);
    }

    public static LoginResult loginWithRefreshToken(String refreshToken) throws IOException {
        Tokens tokens = refresh(refreshToken);
        LoginResult result = loginWithMicrosoftToken(tokens.accessToken());
        return new LoginResult(result.name(), result.uuid(), result.mcToken(), tokens.refreshToken());
    }

    private static String dashed(String undashed) {
        if (undashed == null || undashed.length() != 32) {
            return undashed;
        }
        return undashed.substring(0, 8) + "-" + undashed.substring(8, 12) + "-"
                + undashed.substring(12, 16) + "-" + undashed.substring(16, 20) + "-"
                + undashed.substring(20);
    }

    private static JsonObject postForm(String url, Map<String, String> form) throws IOException {
        JsonObject json = postFormLenient(url, form);
        if (json.has("error")) {
            throw new IOException("Request failed: " + describeError(json));
        }
        return json;
    }

    private static JsonObject postFormLenient(String url, Map<String, String> form) throws IOException {
        StringBuilder body = new StringBuilder();
        for (Map.Entry<String, String> entry : form.entrySet()) {
            if (body.length() > 0) {
                body.append('&');
            }
            body.append(java.net.URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            body.append('=');
            body.append(java.net.URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Length", Integer.toString(bytes.length));
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        connection.getOutputStream().write(bytes);
        return readJson(connection);
    }

    private static JsonObject postJson(String url, JsonObject body) throws IOException {
        byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        connection.getOutputStream().write(bytes);
        int code = connection.getResponseCode();
        if (code < 200 || code >= 300) {
            String detail = readError(connection);
            throw new IOException("Request to " + url + " failed (" + code + "): " + detail);
        }
        try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static JsonObject getJson(String url, String bearer) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + bearer);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        int code = connection.getResponseCode();
        if (code == 404) {
            throw new IOException("No Minecraft profile on this Microsoft account (game not owned?)");
        }
        if (code < 200 || code >= 300) {
            throw new IOException("Profile request failed (" + code + "): " + readError(connection));
        }
        try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static JsonObject readJson(HttpURLConnection connection) throws IOException {
        InputStream stream = connection.getResponseCode() >= 400
                ? connection.getErrorStream()
                : connection.getInputStream();
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static String readError(HttpURLConnection connection) {
        try (InputStream stream = connection.getErrorStream()) {
            if (stream == null) {
                return "";
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    private static String describeError(JsonObject json) {
        String error = json.has("error") ? json.get("error").getAsString() : "unknown_error";
        if (json.has("error_description")) {
            return error + ": " + json.get("error_description").getAsString();
        }
        return error;
    }

    private static String firstPresent(JsonObject json, String... keys) {
        for (String key : keys) {
            if (json.has(key) && !json.get(key).isJsonNull()) {
                return json.get(key).getAsString();
            }
        }
        return null;
    }

    private static void requirePresent(JsonObject json, String key) throws IOException {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            throw new IOException("Microsoft returned an unexpected response (missing " + key + ")");
        }
    }

}
