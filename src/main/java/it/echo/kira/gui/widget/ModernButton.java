package it.echo.kira.gui.widget;

import it.echo.kira.gui.Palette;
import it.echo.kira.gui.Render2D;
import it.echo.kira.gui.UiText;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/**
 * Flat bordered button.
 */
public class ModernButton extends Widget {

    public enum Style {
        GREEN(Palette.BUTTON_GREEN_BG, Palette.BUTTON_GREEN_BG_HOVER, Palette.BUTTON_GREEN_BORDER),
        BLUE(Palette.BUTTON_BLUE_BG, Palette.BUTTON_BLUE_BG_HOVER, Palette.BUTTON_BLUE_BORDER),
        DARK(Palette.BUTTON_DARK_BG, Palette.BUTTON_DARK_BG_HOVER, Palette.BUTTON_DARK_BORDER);

        private final int background;
        private final int backgroundHover;
        private final int border;

        Style(int background, int backgroundHover, int border) {
            this.background = background;
            this.backgroundHover = backgroundHover;
            this.border = border;
        }
    }

    private String label;
    private Style style;
    private Runnable onClick;

    public ModernButton(double x, double y, double width, double height, String label, Style style) {
        super(x, y, width, height);
        this.label = label;
        this.style = style;
    }

    public ModernButton onClick(Runnable onClick) {
        this.onClick = onClick;
        return this;
    }

    @Override
    public void draw(Minecraft minecraft, FontRenderer font, int mouseX, int mouseY, float partialTicks) {
        if (!visible) {
            return;
        }
        updateHovered(mouseX, mouseY);
        int background = hovered && enabled ? style.backgroundHover : style.background;
        if (!enabled) {
            background = Palette.BUTTON_DARK_BG;
        }
        Render2D.rect(x, y, width, height, background);
        Render2D.outline(x, y, width, height, enabled ? style.border : Palette.HAIRLINE);
        // Subtle top highlight for a less flat look
        Render2D.rect(x + 1.0D, y + 1.0D, width - 2.0D, 1.0D, new Color(255, 255, 255, 34).getRGB());
        int textColor = enabled ? Palette.TEXT_PRIMARY : Palette.TEXT_FAINT;
        UiText.drawCentered(font, label, (float) (x + width / 2.0D), (float) (y + (height - 8.0D) / 2.0D), textColor,
                shadow);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        boolean pressed = super.mouseClicked(mouseX, mouseY, button);
        if (pressed && onClick != null) {
            onClick.run();
        }
        return pressed;
    }

    public String getLabel() {
        return label;
    }

    public String label() {
        return label;
    }

    public void label(String label) {
        this.label = label;
    }

    public void setLabel(String label) {
        label(label);
    }

    public Style getStyle() {
        return style;
    }

    public Style style() {
        return style;
    }

    public void style(Style style) {
        this.style = style;
    }

    public void setStyle(Style style) {
        style(style);
    }
}
