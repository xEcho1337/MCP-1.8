package it.echo.kira.module.impl;

import it.echo.kira.event.impl.KeyPressEvent;
import it.echo.kira.gui.demo.HostWorldDemoScreen;
import it.echo.kira.gui.demo.LayoutDemoScreen;
import it.echo.kira.module.Module;
import it.echo.kira.module.ModuleCategory;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

/**
 * Opens the demo window on right-shift.
 */
public class DemoGuiModule extends Module {

    public DemoGuiModule() {
        super("DemoGui", ModuleCategory.MISC);
        listen(KeyPressEvent.class, this::onKeyPress);
    }

    private void onKeyPress(KeyPressEvent event) {
        if (event.repeat() || event.keyCode() != Keyboard.KEY_RSHIFT) {
            return;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null) {
            return;
        }
        if (minecraft.currentScreen instanceof HostWorldDemoScreen) {
            minecraft.displayGuiScreen(null);
            return;
        }
        if (minecraft.currentScreen == null) {
            minecraft.displayGuiScreen(new LayoutDemoScreen());
        }
    }
}
