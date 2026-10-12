package it.echo.kira.gui.widget;

import it.echo.kira.gui.Render2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/**
 * Minimal retained-mode widget base for custom screens.
 */
public abstract class Widget {

    protected double x;
    protected double y;
    protected double width;
    protected double height;
    protected boolean visible = true;
    protected boolean enabled = true;
    protected boolean hovered;
    protected boolean shadow = true;

    protected Widget(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void draw(Minecraft minecraft, FontRenderer font, int mouseX, int mouseY, float partialTicks);

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return enabled && visible && button == 0 && Render2D.inside(mouseX, mouseY, x, y, width, height);
    }

    protected void updateHovered(int mouseX, int mouseY) {
        hovered = Render2D.inside(mouseX, mouseY, x, y, width, height);
    }

    public boolean isHovered() {
        return hovered;
    }

    public boolean hovered() {
        return hovered;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double width() {
        return width;
    }

    public double height() {
        return height;
    }

    public boolean visible() {
        return visible;
    }

    public boolean enabled() {
        return enabled;
    }

    public boolean shadow() {
        return shadow;
    }

    public void x(double x) {
        this.x = x;
    }

    public void y(double y) {
        this.y = y;
    }

    public void width(double width) {
        this.width = width;
    }

    public void height(double height) {
        this.height = height;
    }

    public void visible(boolean visible) {
        this.visible = visible;
    }

    public void enabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void shadow(boolean shadow) {
        this.shadow = shadow;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        visible(visible);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        enabled(enabled);
    }
}
