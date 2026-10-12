package it.echo.kira.module.impl;

import it.echo.kira.Kira;
import it.echo.kira.event.impl.KeyPressEvent;
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
        if (event.repeat() || event.keyCode() != Keyboard.KEY_H) {
            return;
        }
        toggle();
    }

    @Override
    protected void onEnable() {
        Kira.hud().editMode(true);
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft != null) {
            minecraft.setIngameNotInFocus();
        }
    }

    @Override
    protected void onDisable() {
        Kira.hud().editMode(false);
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft != null && minecraft.currentScreen == null) {
            minecraft.setIngameFocus();
        }
    }
}
