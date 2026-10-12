package it.echo.kira.gui.widget;

import it.echo.kira.gui.Palette;
import it.echo.kira.gui.Render2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

/**
 * Small 18px titlebar button, icon is configurable by the caller.
 */
public class IconButton extends Widget {

    public enum Icon {
        CLOSE,
        POPOUT,
        MINIMIZE,
        PIN
    }

    private Icon icon;
    private Runnable onClick;

    public IconButton(double x, double y, Icon icon) {
        super(x, y, 18.0D, 18.0D);
        this.icon = icon;
    }

    public IconButton onClick(Runnable onClick) {
        this.onClick = onClick;
        return this;
    }

    @Override
    public void draw(Minecraft minecraft, FontRenderer font, int mouseX, int mouseY, float partialTicks) {
        if (!visible) {
            return;
        }
        updateHovered(mouseX, mouseY);
        int background = hovered ? Palette.ICON_BUTTON_BG_HOVER : Palette.ICON_BUTTON_BG;
        Render2D.rect(x, y, width, height, background);
        Render2D.outline(x, y, width, height, Palette.ICON_BUTTON_BORDER);
        drawGlyph();
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        boolean pressed = super.mouseClicked(mouseX, mouseY, button);
        if (pressed && onClick != null) {
            onClick.run();
        }
        return pressed;
    }

    private void drawGlyph() {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(0.88F, 0.88F, 0.88F, 1.0F);
        GL11.glLineWidth(1.5F);
        switch (icon) {
            case CLOSE -> drawClose();
            case POPOUT -> drawPopout();
            case MINIMIZE -> drawMinimize();
            case PIN -> drawPin();
        }
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private void drawClose() {
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2d(x + 5.0D, y + 5.0D);
        GL11.glVertex2d(x + width - 5.0D, y + height - 5.0D);
        GL11.glVertex2d(x + width - 5.0D, y + 5.0D);
        GL11.glVertex2d(x + 5.0D, y + height - 5.0D);
        GL11.glEnd();
    }

    private void drawPopout() {
        double left = x + 5.0D;
        double top = y + 6.0D;
        double right = x + width - 6.0D;
        double bottom = y + height - 5.0D;
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2d(left, top);
        GL11.glVertex2d(right, top);
        GL11.glVertex2d(right, bottom);
        GL11.glVertex2d(left, bottom);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2d(left + 2.0D, top);
        GL11.glVertex2d(left + 2.0D, top - 2.0D);
        GL11.glVertex2d(left + 2.0D, top - 2.0D);
        GL11.glVertex2d(right + 1.0D, top - 2.0D);
        GL11.glVertex2d(right + 1.0D, top - 2.0D);
        GL11.glVertex2d(right + 1.0D, bottom - 3.0D);
        GL11.glEnd();
    }

    private void drawMinimize() {
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2d(x + 5.0D, y + height - 6.0D);
        GL11.glVertex2d(x + width - 5.0D, y + height - 6.0D);
        GL11.glEnd();
    }

    private void drawPin() {
        double centerX = x + width / 2.0D;
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2d(centerX - 3.0D, y + 4.0D);
        GL11.glVertex2d(centerX + 3.0D, y + 4.0D);
        GL11.glVertex2d(centerX + 1.5D, y + 9.0D);
        GL11.glVertex2d(centerX + 1.5D, y + height - 4.0D);
        GL11.glVertex2d(centerX - 1.5D, y + height - 4.0D);
        GL11.glVertex2d(centerX - 1.5D, y + 9.0D);
        GL11.glEnd();
    }

    public Icon getIcon() {
        return icon;
    }

    public Icon icon() {
        return icon;
    }

    public void icon(Icon icon) {
        this.icon = icon;
    }

    public void setIcon(Icon icon) {
        icon(icon);
    }
}
