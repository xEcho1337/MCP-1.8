package it.echo.kira.gui.impl;

import it.echo.kira.Kira;
import it.echo.kira.gui.Palette;
import it.echo.kira.gui.Render2D;
import it.echo.kira.gui.UiText;
import it.echo.kira.gui.layout.Rect;
import it.echo.kira.gui.widget.ModernButton;
import java.io.IOException;
import net.minecraft.client.gui.GuiScreen;

public class HudEditScreen extends GuiScreen {

    private ModernButton modsButton;

    @Override
    public void initGui() {
        Kira.hud().editMode(true);
        mc.setIngameNotInFocus();

        Rect size = Rect.of(0, 0, 70, 25);
        Rect center = Rect.of(0, 0, width, height).centered(size.width(), size.height());

        modsButton = new ModernButton(center.x(), center.y(), size.width(), size.height(), "Mods", ModernButton.Style.BLUE);
        modsButton.onClick(() -> mc.displayGuiScreen(new ModsScreen()));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Render2D.dimBackground(width, height);
        Kira.hud().mouseMoved(mouseX, mouseY, width, height);
        Kira.hud().renderAll(mc, fontRendererObj, mouseX, mouseY, partialTicks);
        UiText.drawCenteredShadow(fontRendererObj, "Drag elements to move - RSHIFT to close",
                width / 2.0F, height - 20.0F, Palette.TEXT_DIM);
        modsButton.draw(mc, fontRendererObj, mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        modsButton.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onGuiClosed() {
        Kira.hud().mouseReleased();
        Kira.hud().editMode(false);
        if (Kira.modules().byName("HudEdit") != null) {
            Kira.modules().byName("HudEdit").enabled(false);
        }
    }
}
