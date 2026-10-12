package it.echo.kira.gui.widget;

import it.echo.kira.gui.Palette;
import it.echo.kira.gui.Render2D;
import it.echo.kira.gui.UiText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/**
 * Section title with trailing hairline.
 */
public class SectionHeader extends Widget {

    private String title;
    private int gap = 4;

    public SectionHeader(double x, double y, double width, String title) {
        super(x, y, width, 10.0D);
        this.title = title;
    }

    @Override
    public void draw(Minecraft minecraft, FontRenderer font, int mouseX, int mouseY, float partialTicks) {
        if (!visible) {
            return;
        }
        UiText.draw(font, title, (float) x, (float) y, Palette.TEXT_DIM, shadow);
        int titleWidth = font.getStringWidth(title);
        double lineX = x + titleWidth + gap;
        double lineWidth = width - titleWidth - gap;
        if (lineWidth > 2.0D) {
            Render2D.horizontalLine(lineX, lineX + lineWidth, y + 4.0D, Palette.HAIRLINE);
        }
    }

    public int gap() {
        return gap;
    }

    public void gap(int gap) {
        this.gap = gap;
    }

    public String title() {
        return title;
    }

    public void title(String title) {
        this.title = title;
    }
}
