package it.echo.kira.gui.impl;

import it.echo.kira.gui.Palette;
import it.echo.kira.gui.Render2D;
import it.echo.kira.gui.UiText;
import it.echo.kira.gui.layout.Presets;
import it.echo.kira.gui.layout.Rect;
import it.echo.kira.gui.widget.IconButton;
import net.minecraft.client.gui.GuiScreen;

public class ModsScreen extends GuiScreen {

    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 300;

    private Rect window;
    private Presets.AppWindow app;
    private IconButton closeButton;

    @Override
    public void initGui() {
        window = new Rect(0, 0, width, height).centered(WINDOW_WIDTH, WINDOW_HEIGHT);
        app = Presets.appWindow(window, 26, 100, 0);

        Rect title = app.title();
        closeButton = new IconButton(title.x() + title.width() - 18 - 6, title.y() + 4,
                IconButton.Icon.CLOSE);
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
    }
}
