package it.echo.kira.gui;

import java.awt.Color;

/**
 * Packed ARGB color constants.
 */
public final class Palette {

    public static final int WINDOW_BG = new Color(35, 35, 35).getRGB();
    public static final int TITLEBAR_BG = new Color(51, 51, 51).getRGB();
    public static final int SIDEBAR_BG = new Color(43, 43, 43).getRGB();
    public static final int CONTENT_BG = new Color(35, 35, 35).getRGB();
    public static final int ROW_BG = new Color(48, 48, 48).getRGB();
    public static final int ROW_HOVER = new Color(56, 56, 56).getRGB();

    public static final int BORDER = new Color(16, 16, 16).getRGB();
    public static final int HAIRLINE = new Color(74, 74, 74).getRGB();
    public static final int SIDEBAR_DIVIDER = new Color(27, 27, 27).getRGB();

    public static final int TEXT_PRIMARY = new Color(226, 226, 226).getRGB();
    public static final int TEXT_DIM = new Color(168, 168, 168).getRGB();
    public static final int TEXT_FAINT = new Color(122, 122, 122).getRGB();
    public static final int TEXT_SELECTED = new Color(90, 169, 230).getRGB();

    public static final int STATUS_OFFLINE = new Color(217, 64, 64).getRGB();

    public static final int BUTTON_GREEN_BG = new Color(47, 93, 51).getRGB();
    public static final int BUTTON_GREEN_BG_HOVER = new Color(58, 115, 64).getRGB();
    public static final int BUTTON_GREEN_BORDER = new Color(76, 175, 80).getRGB();

    public static final int BUTTON_BLUE_BG = new Color(46, 74, 107).getRGB();
    public static final int BUTTON_BLUE_BG_HOVER = new Color(58, 93, 134).getRGB();
    public static final int BUTTON_BLUE_BORDER = new Color(91, 155, 213).getRGB();

    public static final int BUTTON_DARK_BG = new Color(58, 58, 58).getRGB();
    public static final int BUTTON_DARK_BG_HOVER = new Color(69, 69, 69).getRGB();
    public static final int BUTTON_DARK_BORDER = new Color(90, 90, 90).getRGB();

    public static final int ICON_BUTTON_BG = new Color(61, 61, 61).getRGB();
    public static final int ICON_BUTTON_BG_HOVER = new Color(77, 77, 77).getRGB();
    public static final int ICON_BUTTON_BORDER = new Color(92, 92, 92).getRGB();

    public static final int DIM = new Color(0, 0, 0, 153).getRGB();
    public static final int SHADOW = new Color(0, 0, 0, 128).getRGB();

    private Palette() {
    }
}
