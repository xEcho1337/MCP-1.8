package it.echo.kira.hud.impl;

import it.echo.kira.gui.Palette;
import it.echo.kira.gui.Render2D;
import it.echo.kira.gui.UiText;
import it.echo.kira.hud.HudElement;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class WatermarkElement extends HudElement {

    private static final int PADDING_X = 6;
    private static final int PADDING_Y = 4;
    private static final int BACKGROUND = new Color(35, 35, 35, 170).getRGB();

    public WatermarkElement(double x, double y) {
        super(x, y, 74.0D, 16.0D);
    }

    @Override
    protected void renderContent(Minecraft minecraft, FontRenderer font, int mouseX, int mouseY, float partialTicks) {
        Render2D.rect(x, y, width, height, BACKGROUND);
        Render2D.outline(x, y, width, height, Palette.HAIRLINE);
        UiText.draw(font, "Kira", (float) (x + PADDING_X), (float) (y + PADDING_Y), Palette.TEXT_SELECTED,
                shadow);
        UiText.draw(font, "1.8.9", (float) (x + PADDING_X + font.getStringWidth("Kira ")),
                (float) (y + PADDING_Y), Palette.TEXT_DIM, shadow);
    }
}
