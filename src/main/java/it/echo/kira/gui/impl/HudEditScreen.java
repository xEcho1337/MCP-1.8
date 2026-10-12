package it.echo.kira.gui.impl;

import it.echo.kira.Kira;
import it.echo.kira.gui.layout.Rect;
import it.echo.kira.gui.widget.ModernButton;
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
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        modsButton.draw(mc, fontRendererObj, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed() {
        Kira.hud().editMode(false);
        mc.setIngameFocus();
    }
}
