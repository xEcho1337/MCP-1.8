package it.echo.kira;

import it.echo.kira.event.impl.ClientTickEvent;
import it.echo.kira.event.impl.KeyPressEvent;
import it.echo.kira.event.impl.KeyReleaseEvent;
import it.echo.kira.event.impl.MousePressEvent;
import it.echo.kira.event.impl.MouseReleaseEvent;
import it.echo.kira.event.impl.RenderHudEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

/**
 * Thin static hooks called from vanilla classes. Keeps vanilla diffs to one-liners.
 */
public final class KiraHooks {

    private KiraHooks() {
    }

    public static void init() {
        Kira.init();
    }

    public static void onKeyPressed(int keyCode, char keyChar, boolean repeat) {
        Kira.fire(new KeyPressEvent(keyCode, keyChar, repeat));
        if (!repeat) {
            Kira.modules().toggleBound(keyCode);
        }
    }

    public static void onKeyReleased(int keyCode, char keyChar) {
        Kira.fire(new KeyReleaseEvent(keyCode, keyChar));
    }

    public static void onMousePressed(int button, int mouseX, int mouseY) {
        Kira.fire(new MousePressEvent(button, mouseX, mouseY));
        // HUD drag gets first chance in edit mode
        Kira.hud().mousePressed(mouseX, mouseY, button);
    }

    public static void onMouseReleased(int button, int mouseX, int mouseY) {
        Kira.fire(new MouseReleaseEvent(button, mouseX, mouseY));
        Kira.hud().mouseReleased();
    }

    public static void onTick() {
        Kira.fire(ClientTickEvent.INSTANCE);
    }

    /**
     * True while HUD elements are being arranged.
     *
     * @return edit mode flag
     */
    public static boolean isHudEditing() {
        return Kira.hud().editMode();
    }

    /**
     * Draws draggable HUD and notifies listeners, called at the end of overlay render.
     *
     * @param minecraft mc instance
     * @param partialTicks partial ticks
     */
    public static void onHudRender(Minecraft minecraft, float partialTicks) {
        if (minecraft.theWorld == null || minecraft.fontRendererObj == null) {
            return;
        }
        ScaledResolution resolution = new ScaledResolution(minecraft);
        int mouseX = Mouse.getX() * resolution.getScaledWidth() / minecraft.displayWidth;
        int mouseY = resolution.getScaledHeight() - Mouse.getY() * resolution.getScaledHeight()
                / minecraft.displayHeight - 1;
        FontRenderer font = minecraft.fontRendererObj;
        Kira.fire(new RenderHudEvent(mouseX, mouseY, partialTicks));
        Kira.hud().mouseMoved(mouseX, mouseY, resolution.getScaledWidth(), resolution.getScaledHeight());
        Kira.hud().renderAll(minecraft, font, mouseX, mouseY, partialTicks);
    }
}
