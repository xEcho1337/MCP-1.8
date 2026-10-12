package it.echo.kira.gui;

import net.minecraft.client.gui.FontRenderer;

/**
 * Font helpers with flat rendering by default.
 */
public final class UiText {

    private UiText() {
    }

    public static void draw(FontRenderer font, String text, float x, float y, int color) {
        draw(font, text, x, y, color, false);
    }

    public static void drawShadow(FontRenderer font, String text, float x, float y, int color) {
        draw(font, text, x, y, color, true);
    }

    public static void draw(FontRenderer font, String text, float x, float y, int color, boolean shadow) {
        font.drawString(text, x, y, color, shadow);
    }

    public static void drawCentered(FontRenderer font, String text, float centerX, float y, int color) {
        drawCentered(font, text, centerX, y, color, false);
    }

    public static void drawCenteredShadow(FontRenderer font, String text, float centerX, float y, int color) {
        drawCentered(font, text, centerX, y, color, true);
    }

    public static void drawCentered(FontRenderer font, String text, float centerX, float y, int color,
            boolean shadow) {
        float x = centerX - font.getStringWidth(text) / 2.0F;
        font.drawString(text, x, y, color, shadow);
    }

    public static void drawRight(FontRenderer font, String text, float rightX, float y, int color) {
        drawRight(font, text, rightX, y, color, false);
    }

    public static void drawRightShadow(FontRenderer font, String text, float rightX, float y, int color) {
        drawRight(font, text, rightX, y, color, true);
    }

    public static void drawRight(FontRenderer font, String text, float rightX, float y, int color, boolean shadow) {
        font.drawString(text, rightX - font.getStringWidth(text), y, color, shadow);
    }

    public static String trimToWidth(FontRenderer font, String text, int maxWidth) {
        return font.trimStringToWidth(text, maxWidth);
    }

    public static String trimToWidthWithEllipsis(FontRenderer font, String text, int maxWidth) {
        if (font.getStringWidth(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int ellipsisWidth = font.getStringWidth(ellipsis);
        String trimmed = font.trimStringToWidth(text, maxWidth - ellipsisWidth);
        return trimmed + ellipsis;
    }

    public static float lineHeight(FontRenderer font) {
        return (float) font.FONT_HEIGHT;
    }
}
