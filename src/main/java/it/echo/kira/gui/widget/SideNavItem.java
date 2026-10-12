package it.echo.kira.gui.widget;

import it.echo.kira.gui.Palette;
import it.echo.kira.gui.Render2D;
import it.echo.kira.gui.UiText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/**
 * Selectable sidebar navigation entry.
 */
public class SideNavItem extends Widget {

    private final String label;
    private boolean selected;
    private Runnable onClick;

    public SideNavItem(double x, double y, double width, double height, String label) {
        super(x, y, width, height);
        this.label = label;
    }

    public SideNavItem onClick(Runnable onClick) {
        this.onClick = onClick;
        return this;
    }

    @Override
    public void draw(Minecraft minecraft, FontRenderer font, int mouseX, int mouseY, float partialTicks) {
        if (!visible) {
            return;
        }
        updateHovered(mouseX, mouseY);
        int color = Palette.TEXT_DIM;
        if (selected) {
            color = Palette.TEXT_SELECTED;
        } else if (hovered && enabled) {
            color = Palette.TEXT_PRIMARY;
        }
        UiText.draw(font, label, (float) x, (float) (y + (height - 8.0D) / 2.0D), color, shadow);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        boolean pressed = super.mouseClicked(mouseX, mouseY, button);
        if (pressed && onClick != null) {
            onClick.run();
        }
        return pressed;
    }

    public boolean selected() {
        return selected;
    }

    public void selected(boolean selected) {
        this.selected = selected;
    }

    public String label() {
        return label;
    }
}
