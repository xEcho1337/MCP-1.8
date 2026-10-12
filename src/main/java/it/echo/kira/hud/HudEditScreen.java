package it.echo.kira.hud;

import it.echo.kira.Kira;
import it.echo.kira.gui.Palette;
import it.echo.kira.gui.Render2D;
import it.echo.kira.gui.UiText;
import java.io.IOException;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

public class HudEditScreen extends GuiScreen {

    private boolean prevEditMode;

    @Override
    public void initGui() {
        prevEditMode = Kira.hud().editMode();
        Kira.hud().editMode(true);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Render2D.dimBackground(width, height);
        Kira.hud().mouseMoved(mouseX, mouseY, width, height);
        Kira.hud().renderAll(mc, fontRendererObj, mouseX, mouseY, partialTicks);
        UiText.drawCenteredShadow(fontRendererObj, "Drag elements to move - RSHIFT to close",
                width / 2.0F, height - 20.0F, Palette.TEXT_DIM);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_RSHIFT) {
            mc.displayGuiScreen(null);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        Kira.hud().mouseReleased();
        Kira.hud().editMode(prevEditMode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
