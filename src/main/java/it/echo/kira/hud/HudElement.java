package it.echo.kira.hud;

import it.echo.kira.MinecraftAccess;
import it.echo.kira.gui.Render2D;
import it.echo.kira.gui.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/**
 * Widget that can be moved with the mouse while HUD edit mode is on.
 */
public abstract class HudElement extends Widget implements MinecraftAccess {

    private boolean dragging;
    private double dragOffsetX;
    private double dragOffsetY;

    protected HudElement(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    /**
     * Starts a drag when inside bounds.
     *
     * @param mouseX scaled x
     * @param mouseY scaled y
     * @param button mouse button
     * @return true when drag started
     */
    public boolean handlePress(int mouseX, int mouseY, int button) {
        if (button != 0 || !visible || !Render2D.inside(mouseX, mouseY, x, y, width, height)) {
            return false;
        }
        dragging = true;
        dragOffsetX = mouseX - x;
        dragOffsetY = mouseY - y;
        return true;
    }

    /**
     * Moves the element when dragging.
     *
     * @param mouseX scaled x
     * @param mouseY scaled y
     * @param screenWidth scaled screen width, used for clamping
     * @param screenHeight scaled screen height
     */
    public void handleMove(int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (!dragging) {
            return;
        }
        double nextX = mouseX - dragOffsetX;
        double nextY = mouseY - dragOffsetY;
        nextX = Math.max(0.0D, Math.min(nextX, screenWidth - width));
        nextY = Math.max(0.0D, Math.min(nextY, screenHeight - height));
        x = nextX;
        y = nextY;
    }

    /**
     * Stops any active drag.
     */
    public void handleRelease() {
        dragging = false;
    }

    public boolean dragging() {
        return dragging;
    }

    @Override
    public final void draw(Minecraft minecraft, FontRenderer font, int mouseX, int mouseY, float partialTicks) {
        if (!visible) {
            return;
        }
        updateHovered(mouseX, mouseY);
        renderContent(minecraft, font, mouseX, mouseY, partialTicks);
    }

    /**
     * Draws the element body.
     *
     * @param minecraft mc instance
     * @param font font renderer
     * @param mouseX scaled mouse x
     * @param mouseY scaled mouse y
     * @param partialTicks partial ticks
     */
    protected abstract void renderContent(Minecraft minecraft, FontRenderer font, int mouseX, int mouseY,
            float partialTicks);
}
