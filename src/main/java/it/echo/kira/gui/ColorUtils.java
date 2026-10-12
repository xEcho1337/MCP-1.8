package it.echo.kira.gui;

/**
 * Small packed-ARGB helpers, no dependencies on Minecraft classes.
 */
public final class ColorUtils {

    private ColorUtils() {
    }

    public static int argb(int alpha, int red, int green, int blue) {
        return ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
    }

    public static int rgb(int red, int green, int blue) {
        return argb(0xFF, red, green, blue);
    }

    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    public static int alphaOf(int color) {
        return (color >> 24) & 0xFF;
    }

    public static int lighten(int color, float amount) {
        int alpha = alphaOf(color);
        int red = clamp(Math.round(((color >> 16) & 0xFF) + 255.0F * amount));
        int green = clamp(Math.round(((color >> 8) & 0xFF) + 255.0F * amount));
        int blue = clamp(Math.round((color & 0xFF) + 255.0F * amount));
        return argb(alpha, red, green, blue);
    }

    public static int darken(int color, float amount) {
        return lighten(color, -amount);
    }

    public static int lerp(int first, int second, float factor) {
        float inverse = 1.0F - factor;
        int alpha = Math.round((((first >> 24) & 0xFF) * inverse) + (((second >> 24) & 0xFF) * factor));
        int red = Math.round((((first >> 16) & 0xFF) * inverse) + (((second >> 16) & 0xFF) * factor));
        int green = Math.round((((first >> 8) & 0xFF) * inverse) + (((second >> 8) & 0xFF) * factor));
        int blue = Math.round(((first & 0xFF) * inverse) + ((second & 0xFF) * factor));
        return argb(alpha, red, green, blue);
    }

    private static int clamp(int value) {
        if (value < 0) {
            return 0;
        }
        return Math.min(value, 255);
    }
}
