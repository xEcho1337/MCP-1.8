package it.echo.kira.gui.impl;

import it.echo.kira.Kira;
import it.echo.kira.gui.Palette;
import it.echo.kira.gui.Render2D;
import it.echo.kira.gui.UiText;
import it.echo.kira.gui.layout.Layout;
import it.echo.kira.gui.layout.Constraint;
import it.echo.kira.gui.layout.Presets;
import it.echo.kira.gui.layout.Rect;
import it.echo.kira.gui.widget.IconButton;
import it.echo.kira.gui.widget.SideNavItem;
import it.echo.kira.module.Module;
import it.echo.kira.module.ModuleCategory;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Mouse;

public class ModsScreen extends GuiScreen {

    private static final int WINDOW_WIDTH = 420;
    private static final int WINDOW_HEIGHT = 300;
    private static final int ROW_HEIGHT = 22;
    private static final int ROW_GAP = 4;
    private static final int TOGGLE_WIDTH = 26;
    private static final int TOGGLE_HEIGHT = 12;

    private static final int TOGGLE_ON = new Color(76, 175, 80).getRGB();
    private static final int TOGGLE_OFF = new Color(90, 90, 90).getRGB();
    private static final int TOGGLE_KNOB = new Color(235, 235, 235).getRGB();

    private Rect window;
    private Presets.AppWindow app;
    private IconButton closeButton;
    private GuiTextField search;
    private final List<SideNavItem> nav = new ArrayList<>();
    private ModuleCategory filter;
    private Rect listArea;
    private int scroll;

    @Override
    public void initGui() {
        nav.clear();
        window = new Rect(0, 0, width, height).centered(WINDOW_WIDTH, WINDOW_HEIGHT);
        app = Presets.appWindow(window, 26, 110, 0);

        Rect title = app.title();
        closeButton = new IconButton(title.x() + title.width() - 18 - 6, title.y() + 4, IconButton.Icon.CLOSE);
        closeButton.onClick(() -> mc.displayGuiScreen(null));

        Rect sidebar = app.sidebar().inner(12);
        List<Rect> rows = Layout.vertical(
                Constraint.length(16),
                Constraint.length(16),
                Constraint.length(16),
                Constraint.length(16),
                Constraint.length(16),
                Constraint.length(16),
                Constraint.fill(1)
        ).spacing(2).split(sidebar);

        String[] labels = {"All", "Combat", "Movement", "Player", "Render", "Misc"};
        ModuleCategory[] values = {null, ModuleCategory.COMBAT, ModuleCategory.MOVEMENT,
                ModuleCategory.PLAYER, ModuleCategory.RENDER, ModuleCategory.MISC};

        for (int i = 0; i < labels.length; i++) {
            Rect row = rows.get(i);
            SideNavItem item = new SideNavItem(row.x(), row.y(), row.width(), 16.0D, labels[i]);

            item.selected(values[i] == filter);

            ModuleCategory value = values[i];
            item.onClick(() -> {
                filter = value;
                scroll = 0;
                for (SideNavItem other : nav) {
                    other.selected(other == item);
                }
            });

            nav.add(item);
        }

        Rect content = app.content().inner(12);
        search = new GuiTextField(0, fontRendererObj, content.x(), content.y(),
            content.width(), 18);
        search.setMaxStringLength(32);
        listArea = new Rect(content.x(), content.y() + 24, content.width(), content.height() - 24 - 16);
        scroll = 0;
    }

    private List<Module> visible() {
        String query = search.getText().toLowerCase(Locale.ROOT).trim();
        List<Module> result = new ArrayList<>();
        for (Module module : Kira.modules().all()) {
            if (filter != null && module.category() != filter) {
                continue;
            }
            if (!query.isEmpty() && !module.name().toLowerCase(Locale.ROOT).contains(query)) {
                continue;
            }
            result.add(module);
        }
        return result;
    }

    private int maxScroll(int count) {
        int total = count * (ROW_HEIGHT + ROW_GAP) - ROW_GAP;
        return Math.max(0, total - listArea.height());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        Rect title = app.title();

        Render2D.dimBackground(width, height);
        Render2D.rect(title.x(), title.y(), title.width(), title.height(), Palette.TITLEBAR_BG);

        UiText.drawShadow(fontRendererObj, "Kira Mod List", (float) (title.x() + 12),
                (float) (title.y() + 9), Palette.TEXT_PRIMARY);
        closeButton.draw(mc, fontRendererObj, mouseX, mouseY, partialTicks);

        Presets.paintAppWindow(app, Palette.SIDEBAR_BG, Palette.CONTENT_BG, Palette.SIDEBAR_DIVIDER);

        for (SideNavItem item : nav) {
            item.draw(mc, fontRendererObj, mouseX, mouseY, partialTicks);
        }

        search.drawTextBox();

        List<Module> modules = visible();
        scroll = Math.min(scroll, maxScroll(modules.size()));

        Render2D.enableScissor(listArea.x(), listArea.y(), listArea.width(), listArea.height());
        double y = listArea.y() - scroll;
        for (Module module : modules) {
            if (y + ROW_HEIGHT >= listArea.y() && y <= listArea.y() + listArea.height()) {
                drawRow(module, listArea.x(), y, listArea.width(), mouseX, mouseY);
            }
            y += ROW_HEIGHT + ROW_GAP;
        }
        Render2D.disableScissor();

        long enabled = Kira.modules().enabled().size();
        UiText.draw(fontRendererObj, modules.size() + " shown - " + enabled + " enabled",
                (float) listArea.x(), (float) (listArea.y() + listArea.height() + 4),
                Palette.TEXT_FAINT, false);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawRow(Module module, double x, double y, double w, int mouseX, int mouseY) {
        boolean hovered = Render2D.inside(mouseX, mouseY, x, y, w, ROW_HEIGHT)
                && Render2D.inside(mouseX, mouseY, listArea.x(), listArea.y(),
                        listArea.width(), listArea.height());
        Render2D.rect(x, y, w, ROW_HEIGHT, hovered ? Palette.ROW_HOVER : Palette.ROW_BG);
        Render2D.outline(x, y, w, ROW_HEIGHT, Palette.HAIRLINE);

        float textY = (float) (y + (ROW_HEIGHT - 8.0D) / 2.0D);
        UiText.draw(fontRendererObj, module.name(), (float) (x + 8.0D), textY,
                module.enabled() ? Palette.TEXT_PRIMARY : Palette.TEXT_DIM, false);
        String category = module.category().name().charAt(0)
                + module.category().name().substring(1).toLowerCase(Locale.ROOT);
        UiText.draw(fontRendererObj, category, (float) (x + 8.0D + fontRendererObj.getStringWidth(module.name()) + 6.0D),
                textY, Palette.TEXT_FAINT, false);

        double toggleX = x + w - TOGGLE_WIDTH - 8.0D;
        double toggleY = y + (ROW_HEIGHT - TOGGLE_HEIGHT) / 2.0D;
        Render2D.roundedRect(toggleX, toggleY, TOGGLE_WIDTH, TOGGLE_HEIGHT, 6.0D,
                module.enabled() ? TOGGLE_ON : TOGGLE_OFF);
        double knobX = module.enabled() ? toggleX + TOGGLE_WIDTH - 10.0D : toggleX + 2.0D;
        Render2D.circle(knobX + 4.0D, toggleY + TOGGLE_HEIGHT / 2.0D, 4.0D, TOGGLE_KNOB);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        closeButton.mouseClicked(mouseX, mouseY, mouseButton);
        for (SideNavItem item : nav) {
            item.mouseClicked(mouseX, mouseY, mouseButton);
        }
        search.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0 && Render2D.inside(mouseX, mouseY, listArea.x(), listArea.y(),
                listArea.width(), listArea.height())) {
            List<Module> modules = visible();
            double y = listArea.y() - scroll;
            for (Module module : modules) {
                if (Render2D.inside(mouseX, mouseY, listArea.x(), y, listArea.width(), ROW_HEIGHT)) {
                    module.toggle();
                    break;
                }
                y += ROW_HEIGHT + ROW_GAP;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (search.textboxKeyTyped(typedChar, keyCode)) {
            scroll = 0;
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) {
            return;
        }
        List<Module> modules = visible();
        scroll = Math.max(0, Math.min(maxScroll(modules.size()),
                scroll - Integer.signum(wheel) * (ROW_HEIGHT + ROW_GAP) * 2));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
