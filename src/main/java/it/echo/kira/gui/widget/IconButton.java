package it.echo.kira.gui.widget;

import it.echo.kira.gui.Palette;
import it.echo.kira.gui.Render2D;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

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

    private static final int GLYPH = new Color(224, 224, 224).getRGB();
    private static final int GLYPH_HOVER = new Color(255, 255, 255).getRGB();
    private static final int PIXEL = 2;

    private void drawGlyph() {
        int color = hovered ? GLYPH_HOVER : GLYPH;
        switch (icon) {
            case CLOSE -> drawClose(color);
            case POPOUT -> drawPopout(color);
            case MINIMIZE -> drawMinimize(color);
            case PIN -> drawPin(color);
        }
    }

    private void drawClose(int color) {
        double originX = x + (width - 5 * PIXEL) / 2.0D;
        double originY = y + (height - 5 * PIXEL) / 2.0D;
        for (int i = 0; i < 5; i++) {
            Render2D.rect(originX + i * PIXEL, originY + i * PIXEL, PIXEL, PIXEL, color);
            Render2D.rect(originX + (4 - i) * PIXEL, originY + i * PIXEL, PIXEL, PIXEL, color);
        }
    }

    private void drawPopout(int color) {
        double left = x + 5.0D;
        double top = y + 7.0D;
        double right = x + width - 6.0D;
        double bottom = y + height - 5.0D;
        Render2D.rect(left, top, right - left, 1.0D, color);
        Render2D.rect(left, bottom - 1.0D, right - left, 1.0D, color);
        Render2D.rect(left, top, 1.0D, bottom - top, color);
        Render2D.rect(right - 1.0D, top, 1.0D, bottom - top, color);
        Render2D.rect(left + 3.0D, top - 3.0D, right - left - 3.0D, 2.0D, color);
        Render2D.rect(right - 2.0D, top - 3.0D, 2.0D, bottom - top - 1.0D, color);
    }

    private void drawMinimize(int color) {
        Render2D.rect(x + 4.0D, y + height - 6.0D, width - 8.0D, PIXEL, color);
    }

    private void drawPin(int color) {
        double centerX = x + width / 2.0D;
        Render2D.rect(centerX - 3.0D, y + 4.0D, 6.0D, 3.0D, color);
        Render2D.rect(centerX - 1.0D, y + 4.0D, 2.0D, height - 8.0D, color);
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
