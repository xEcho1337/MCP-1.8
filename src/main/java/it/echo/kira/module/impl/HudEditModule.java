package it.echo.kira.module.impl;

import it.echo.kira.Kira;
import it.echo.kira.event.impl.KeyPressEvent;
import it.echo.kira.gui.impl.HudEditScreen;
import it.echo.kira.module.Module;
import it.echo.kira.module.ModuleCategory;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

/**
 * Toggles HUD edit mode so {@code HudElement} instances can be dragged.
 */
public class HudEditModule extends Module {

    public HudEditModule() {
        super("HudEdit", ModuleCategory.RENDER);
        listen(KeyPressEvent.class, this::onKeyPress);
    }

    private void onKeyPress(KeyPressEvent event) {
        if (event.repeat() || event.keyCode() != Keyboard.KEY_RSHIFT) {
            return;
        }
        toggle();
    }

    @Override
    protected void onEnable() {
        MC.displayGuiScreen(new HudEditScreen());
    }

    @Override
    protected void onDisable() {
        Kira.hud().editMode(false);
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft != null && minecraft.currentScreen instanceof HudEditScreen) {
            minecraft.displayGuiScreen(null);
        } else if (minecraft != null && minecraft.currentScreen == null) {
            minecraft.setIngameFocus();
        }
    }
}
