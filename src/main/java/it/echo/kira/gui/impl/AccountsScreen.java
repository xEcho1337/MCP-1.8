package it.echo.kira.gui.impl;

import it.echo.kira.Kira;
import it.echo.kira.account.Account;
import it.echo.kira.account.AccountManager;
import it.echo.kira.account.MicrosoftAuth;
import it.echo.kira.gui.Palette;
import it.echo.kira.gui.Render2D;
import it.echo.kira.gui.UiText;
import it.echo.kira.gui.layout.Presets;
import it.echo.kira.gui.layout.Rect;
import it.echo.kira.gui.widget.IconButton;
import it.echo.kira.gui.widget.ModernButton;
import java.awt.Color;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Mouse;

public class AccountsScreen extends GuiScreen {

    private static final int WINDOW_WIDTH = 420;
    private static final int WINDOW_HEIGHT = 300;
    private static final int ROW_HEIGHT = 24;
    private static final int ROW_GAP = 4;

    private static final int BADGE_MS = new Color(68, 137, 197).getRGB();
    private static final int BADGE_OFFLINE = new Color(122, 122, 122).getRGB();
    private static final int CURRENT_DOT = new Color(76, 175, 80).getRGB();

    private enum Mode {
        MAIN,
        ADD_OFFLINE,
        MS_LOGIN
    }

    private Rect window;
    private Presets.AppWindow app;
    private IconButton closeButton;
    private Rect listArea;
    private int scroll;

    private Mode mode = Mode.MAIN;
    private GuiTextField nameField;
    private ModernButton addOfflineButton;
    private ModernButton msButton;
    private ModernButton removeButton;
    private ModernButton confirmAddButton;
    private ModernButton cancelButton;

    private Account selected;
    private volatile String status = "";
    private volatile int statusColor = Palette.TEXT_FAINT;

    // Microsoft device-flow state, touched from a worker thread
    private volatile MicrosoftAuth.DeviceCode device;
    private volatile boolean msCancelled;
    private volatile boolean msWorking;

    @Override
    public void initGui() {
        window = new Rect(0, 0, width, height).centered(WINDOW_WIDTH, WINDOW_HEIGHT);
        app = Presets.appWindow(window, 26, 0, 0);

        Rect title = app.title();
        closeButton = new IconButton(title.x() + title.width() - 18 - 6, title.y() + 4,
                IconButton.Icon.CLOSE);
        closeButton.onClick(() -> {
            msCancelled = true;
            mc.displayGuiScreen(null);
        });

        Rect content = app.content().inner(12);
        listArea = new Rect(content.x(), content.y() + 20, content.width(),
                content.height() - 20 - 24 - 22);
        scroll = 0;

        Rect buttonsPos = new Rect(content.x(), content.y() + content.height() - 18, content.width(), 18);
        Presets.TwoColumn columns = Presets.twoColumn(buttonsPos, 8);
        Rect left = columns.left();
        Rect right = columns.right();

        addOfflineButton = new ModernButton(left.x(), left.y(), left.width() / 2 - 4, left.height(),
                "Add Offline", ModernButton.Style.DARK);
        addOfflineButton.onClick(() -> {
            mode = Mode.ADD_OFFLINE;
            status = "";
            nameField.setText("");
        });
        msButton = new ModernButton(left.x() + left.width() / 2 + 4, left.y(), left.width() / 2 - 4,
                left.height(), "Microsoft", ModernButton.Style.BLUE);
        msButton.onClick(this::startMicrosoftLogin);
        removeButton = new ModernButton(right.x(), right.y(), right.width(), right.height(),
                "Remove", ModernButton.Style.DARK);
        removeButton.onClick(() -> {
            if (selected == null) {
                fail("Select an account first");
                return;
            }
            Kira.accounts().remove(selected);
            ok("Removed " + selected.name());
            selected = null;
        });

        Rect fieldPos = new Rect(content.x(), content.y() + content.height() - 18 - 24,
                content.width(), 18);
        nameField = new GuiTextField(0, fontRendererObj, (int) fieldPos.x(), (int) fieldPos.y(),
                (int) fieldPos.width(), 18);
        nameField.setMaxStringLength(16);

        confirmAddButton = new ModernButton(content.x(), content.y() + content.height() - 18,
                content.width() / 2 - 4, 18, "Add", ModernButton.Style.GREEN);
        confirmAddButton.onClick(this::confirmAddOffline);
        cancelButton = new ModernButton(content.x() + content.width() / 2 + 4,
                content.y() + content.height() - 18, content.width() / 2 - 4, 18,
                "Cancel", ModernButton.Style.DARK);
        cancelButton.onClick(() -> {
            msCancelled = true;
            mode = Mode.MAIN;
            status = "";
        });
    }

    private void ok(String message) {
        status = message;
        statusColor = CURRENT_DOT;
    }

    private void fail(String message) {
        status = message;
        statusColor = Palette.STATUS_OFFLINE;
    }

    private void confirmAddOffline() {
        try {
            Account account = Kira.accounts().addOffline(nameField.getText().trim());
            Kira.accounts().login(account);
            selected = account;
            mode = Mode.MAIN;
            ok("Logged in as " + account.name());
        } catch (IllegalArgumentException | IllegalStateException e) {
            fail(e.getMessage());
        }
    }

    private void startMicrosoftLogin() {
        if (msWorking) {
            return;
        }
        mode = Mode.MS_LOGIN;
        msCancelled = false;
        msWorking = true;
        status = "Contacting Microsoft...";
        statusColor = Palette.TEXT_DIM;
        Thread thread = new Thread(() -> {
            try {
                MicrosoftAuth.DeviceCode flow = MicrosoftAuth.beginDeviceFlow();
                device = flow;
                MicrosoftAuth.Tokens tokens = MicrosoftAuth.pollForTokens(flow, () -> msCancelled);
                if (tokens == null || tokens.accessToken() == null) {
                    fail("Cancelled");
                    return;
                }
                MicrosoftAuth.LoginResult result =
                        MicrosoftAuth.loginWithMicrosoftToken(tokens.accessToken());
                Account account = Kira.accounts().addMicrosoft(
                        new MicrosoftAuth.LoginResult(result.name(), result.uuid(),
                                result.mcToken(), tokens.refreshToken()),
                        tokens.refreshToken());
                AccountManager.applySession(result.name(), result.uuid(), result.mcToken());
                selected = account;
                device = null;
                mode = Mode.MAIN;
                ok("Logged in as " + result.name());
            } catch (InterruptedException e) {
                fail("Cancelled");
            } catch (Exception e) {
                fail(shortError(e));
            } finally {
                msWorking = false;
            }
        }, "Kira-MS-Login");
        thread.setDaemon(true);
        thread.start();
    }

    private static String shortError(Exception e) {
        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            return "Login failed: " + e.getClass().getSimpleName();
        }
        return message.length() > 90 ? message.substring(0, 90) : message;
    }

    private int maxScroll(int count) {
        int total = count * (ROW_HEIGHT + ROW_GAP) - ROW_GAP;
        return Math.max(0, total - (int) listArea.height());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        Rect title = app.title();

        Render2D.dimBackground(width, height);
        Render2D.rect(title.x(), title.y(), title.width(), title.height(), Palette.TITLEBAR_BG);

        UiText.drawShadow(fontRendererObj, "Kira Accounts", (float) (title.x() + 12),
                (float) (title.y() + 9), Palette.TEXT_PRIMARY);
        closeButton.draw(mc, fontRendererObj, mouseX, mouseY, partialTicks);

        Presets.paintAppWindow(app, Palette.SIDEBAR_BG, Palette.CONTENT_BG, Palette.SIDEBAR_DIVIDER);

        if (mode == Mode.MS_LOGIN) {
            drawMicrosoftPanel(mouseX, mouseY, partialTicks);
        } else {
            drawMain(mouseX, mouseY, partialTicks);
        }
        if (!status.isEmpty()) {
            Rect content = app.content().inner(12);
            UiText.draw(fontRendererObj, status, (float) content.x(),
                    (float) (content.y() + content.height() - 36), statusColor, false);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawMain(int mouseX, int mouseY, float partialTicks) {
        List<Account> accounts = Kira.accounts().all();
        scroll = Math.min(scroll, maxScroll(accounts.size()));

        UiText.draw(fontRendererObj, "Playing as: " + AccountManager.currentName(),
                (float) listArea.x(), (float) (listArea.y() - 16), Palette.TEXT_DIM, false);
        Render2D.horizontalLine(listArea.x(), listArea.x() + listArea.width(),
                listArea.y() - 5, Palette.HAIRLINE);

        Render2D.enableScissor(listArea.x(), listArea.y(), listArea.width(), listArea.height());
        double y = listArea.y() - scroll;
        for (Account account : accounts) {
            if (y + ROW_HEIGHT >= listArea.y() && y <= listArea.y() + listArea.height()) {
                drawRow(account, listArea.x(), y, listArea.width(), mouseX, mouseY);
            }
            y += ROW_HEIGHT + ROW_GAP;
        }
        if (accounts.isEmpty()) {
            UiText.draw(fontRendererObj, "No accounts yet - add one below",
                    (float) (listArea.x() + 8.0D), (float) (listArea.y() + 8.0D),
                    Palette.TEXT_FAINT, false);
        }
        Render2D.disableScissor();

        if (mode == Mode.ADD_OFFLINE) {
            nameField.drawTextBox();
            confirmAddButton.draw(mc, fontRendererObj, mouseX, mouseY, partialTicks);
            cancelButton.draw(mc, fontRendererObj, mouseX, mouseY, partialTicks);
        } else {
            addOfflineButton.draw(mc, fontRendererObj, mouseX, mouseY, partialTicks);
            msButton.draw(mc, fontRendererObj, mouseX, mouseY, partialTicks);
            removeButton.draw(mc, fontRendererObj, mouseX, mouseY, partialTicks);
        }
    }

    private void drawMicrosoftPanel(int mouseX, int mouseY, float partialTicks) {
        MicrosoftAuth.DeviceCode flow = device;
        double centerX = listArea.x() + listArea.width() / 2.0D;
        if (flow == null) {
            UiText.drawCentered(fontRendererObj, "Contacting Microsoft...",
                    (float) centerX, (float) (listArea.y() + 40), Palette.TEXT_DIM, false);
        } else {
            UiText.drawCentered(fontRendererObj, "Go to:",
                    (float) centerX, (float) (listArea.y() + 30), Palette.TEXT_DIM, false);
            UiText.drawCentered(fontRendererObj, flow.verificationUri(),
                    (float) centerX, (float) (listArea.y() + 44), Palette.TEXT_SELECTED, false);
            UiText.drawCentered(fontRendererObj, "Enter code:",
                    (float) centerX, (float) (listArea.y() + 64), Palette.TEXT_DIM, false);
            UiText.drawCentered(fontRendererObj, flow.userCode(),
                    (float) centerX, (float) (listArea.y() + 78), Palette.TEXT_PRIMARY, false);
            UiText.drawCentered(fontRendererObj, "Waiting for approval...",
                    (float) centerX, (float) (listArea.y() + 100), Palette.TEXT_FAINT, false);
        }
        cancelButton.draw(mc, fontRendererObj, mouseX, mouseY, partialTicks);
    }

    private void drawRow(Account account, double x, double y, double w, int mouseX, int mouseY) {
        boolean hovered = Render2D.inside(mouseX, mouseY, x, y, w, ROW_HEIGHT)
                && Render2D.inside(mouseX, mouseY, listArea.x(), listArea.y(),
                        listArea.width(), listArea.height());
        Render2D.rect(x, y, w, ROW_HEIGHT, hovered ? Palette.ROW_HOVER : Palette.ROW_BG);
        if (account == selected) {
            Render2D.outline(x, y, w, ROW_HEIGHT, Palette.TEXT_SELECTED);
        } else {
            Render2D.outline(x, y, w, ROW_HEIGHT, Palette.HAIRLINE);
        }

        double dotX = x + 8.0D;
        double dotY = y + (ROW_HEIGHT - 6.0D) / 2.0D;
        if (Kira.accounts().isSelected(account)) {
            Render2D.circle(dotX + 3.0D, dotY + 3.0D, 3.0D, CURRENT_DOT);
        }

        float textY = (float) (y + (ROW_HEIGHT - 8.0D) / 2.0D);
        UiText.draw(fontRendererObj, account.name(), (float) (dotX + 12.0D), textY,
                Palette.TEXT_PRIMARY, false);

        String badge = account.type() == Account.Type.MICROSOFT ? "MS" : "OFF";
        int badgeColor = account.type() == Account.Type.MICROSOFT ? BADGE_MS : BADGE_OFFLINE;
        int badgeWidth = fontRendererObj.getStringWidth(badge) + 8;
        double badgeX = x + w - badgeWidth - 8.0D;
        double badgeY = y + (ROW_HEIGHT - 12.0D) / 2.0D;
        Render2D.rect(badgeX, badgeY, badgeWidth, 12.0D, badgeColor);
        UiText.drawCentered(fontRendererObj, badge, (float) (badgeX + badgeWidth / 2.0D),
                (float) (badgeY + 2.0D), Palette.TEXT_PRIMARY, false);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        closeButton.mouseClicked(mouseX, mouseY, mouseButton);
        if (mode == Mode.MS_LOGIN) {
            cancelButton.mouseClicked(mouseX, mouseY, mouseButton);
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }
        if (mode == Mode.ADD_OFFLINE) {
            nameField.mouseClicked(mouseX, mouseY, mouseButton);
            confirmAddButton.mouseClicked(mouseX, mouseY, mouseButton);
            cancelButton.mouseClicked(mouseX, mouseY, mouseButton);
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }
        addOfflineButton.mouseClicked(mouseX, mouseY, mouseButton);
        msButton.mouseClicked(mouseX, mouseY, mouseButton);
        removeButton.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0 && Render2D.inside(mouseX, mouseY, listArea.x(), listArea.y(),
                listArea.width(), listArea.height())) {
            List<Account> accounts = Kira.accounts().all();
            double y = listArea.y() - scroll;
            for (Account account : accounts) {
                if (Render2D.inside(mouseX, mouseY, listArea.x(), y, listArea.width(), ROW_HEIGHT)) {
                    selectAndLogin(account);
                    break;
                }
                y += ROW_HEIGHT + ROW_GAP;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void selectAndLogin(Account account) {
        selected = account;
        if (account.type() == Account.Type.OFFLINE) {
            try {
                Kira.accounts().login(account);
                ok("Logged in as " + account.name());
            } catch (IllegalStateException e) {
                fail(e.getMessage());
            }
            return;
        }
        status = "Refreshing " + account.name() + "...";
        statusColor = Palette.TEXT_DIM;
        Thread thread = new Thread(() -> {
            try {
                Kira.accounts().refreshAndLogin(account);
                ok("Logged in as " + account.name());
            } catch (Exception e) {
                fail(shortError(e));
            }
        }, "Kira-Account-Login");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (mode == Mode.ADD_OFFLINE) {
            if (keyCode == 28) {
                confirmAddOffline();
                return;
            }
            if (nameField.textboxKeyTyped(typedChar, keyCode)) {
                return;
            }
        }
        if (keyCode == 1) {
            msCancelled = true;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0 || mode != Mode.MAIN) {
            return;
        }
        List<Account> accounts = Kira.accounts().all();
        scroll = Math.max(0, Math.min(maxScroll(accounts.size()),
                scroll - Integer.signum(wheel) * (ROW_HEIGHT + ROW_GAP) * 2));
    }

    @Override
    public void onGuiClosed() {
        msCancelled = true;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
