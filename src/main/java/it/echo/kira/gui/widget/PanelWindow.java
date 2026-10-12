package it.echo.kira.gui.widget;

import it.echo.kira.gui.Palette;
import it.echo.kira.gui.Render2D;
import it.echo.kira.gui.UiText;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/**
 * Window frame with titlebar, sidebar and content areas.
 */
public class PanelWindow {

    public static final int TITLEBAR_HEIGHT = 26;
    public static final int SIDEBAR_WIDTH = 132;
    public static final int PADDING = 12;
    private static final double BUTTON_SIZE = 18.0D;
    private static final double BUTTON_GAP = 4.0D;
    private static final double BUTTON_MARGIN = 6.0D;
    private static final double BUTTON_Y_OFFSET = 4.0D;

    private final double width;
    private final double height;
    private final List<IconButton> titlebarButtons = new ArrayList<>();
    private final IconButton closeButton = new IconButton(0.0D, 0.0D, IconButton.Icon.CLOSE);

    private String title;
    private double x;
    private double y;

    public PanelWindow(double x, double y, double width, double height, String title) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.title = title;
        layoutButtons();
    }

    public static PanelWindow centered(int screenWidth, int screenHeight, int windowWidth, int windowHeight,
            String title) {
        double x = (screenWidth - windowWidth) / 2.0D;
        double y = (screenHeight - windowHeight) / 2.0D;
        return new PanelWindow(x, y, windowWidth, windowHeight, title);
    }

    public void draw(Minecraft minecraft, FontRenderer font, int mouseX, int mouseY, float partialTicks) {
        Render2D.dropShadow(x, y, width, height, 2.0D, 3);
        Render2D.rect(x, y, width, height, Palette.WINDOW_BG);
        Render2D.outline(x, y, width, height, Palette.BORDER);

        // Titlebar
        Render2D.rect(x + 1.0D, y + 1.0D, width - 2.0D, TITLEBAR_HEIGHT, Palette.TITLEBAR_BG);
        Render2D.rect(x + 1.0D, y + 1.0D + TITLEBAR_HEIGHT, width - 2.0D, 1.0D, Palette.SIDEBAR_DIVIDER);
        UiText.draw(font, title, (float) (x + PADDING), (float) (y + (TITLEBAR_HEIGHT - 8.0D) / 2.0D),
                Palette.TEXT_PRIMARY);

        for (IconButton button : titlebarButtons) {
            button.draw(minecraft, font, mouseX, mouseY, partialTicks);
        }
        closeButton.draw(minecraft, font, mouseX, mouseY, partialTicks);

        // Sidebar vs content split
        double bodyY = y + TITLEBAR_HEIGHT + 1.0D;
        double bodyHeight = height - TITLEBAR_HEIGHT - 1.0D;
        Render2D.rect(x + 1.0D, bodyY, SIDEBAR_WIDTH, bodyHeight - 1.0D, Palette.SIDEBAR_BG);
        Render2D.verticalLine(x + 1.0D + SIDEBAR_WIDTH, bodyY, bodyY + bodyHeight - 1.0D, Palette.SIDEBAR_DIVIDER);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        for (IconButton titlebarButton : titlebarButtons) {
            if (titlebarButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return closeButton.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Adds a configurable titlebar button, placed left of the close button.
     *
     * @param icon icon glyph
     * @param onClick click callback, may be null
     * @return the created button for further setup
     */
    public IconButton addTitlebarButton(IconButton.Icon icon, Runnable onClick) {
        IconButton button = new IconButton(0.0D, 0.0D, icon);
        if (onClick != null) {
            button.onClick(onClick);
        }
        titlebarButtons.add(button);
        layoutButtons();
        return button;
    }

    /**
     * Adds an already built button to the titlebar.
     *
     * @param button button to adopt
     * @return the same button
     */
    public IconButton addTitlebarButton(IconButton button) {
        titlebarButtons.add(button);
        layoutButtons();
        return button;
    }

    /**
     * Removes custom buttons, the close button always stays.
     */
    public void clearTitlebarButtons() {
        titlebarButtons.clear();
        layoutButtons();
    }

    public double getX() {
        return x;
    }

    public double x() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double y() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double width() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double height() {
        return height;
    }

    public double sidebarX() {
        return x + PADDING;
    }

    public double sidebarY() {
        return y + TITLEBAR_HEIGHT + PADDING;
    }

    public double sidebarWidth() {
        return SIDEBAR_WIDTH - PADDING * 2.0D;
    }

    public double contentX() {
        return x + SIDEBAR_WIDTH + PADDING;
    }

    public double contentY() {
        return y + TITLEBAR_HEIGHT + PADDING;
    }

    public double contentWidth() {
        return width - SIDEBAR_WIDTH - PADDING * 2.0D;
    }

    public double contentHeight() {
        return height - TITLEBAR_HEIGHT - PADDING * 2.0D;
    }

    public List<IconButton> titlebarButtons() {
        return titlebarButtons;
    }

    public List<IconButton> getTitlebarButtons() {
        return titlebarButtons;
    }

    public IconButton getCloseButton() {
        return closeButton;
    }

    public IconButton closeButton() {
        return closeButton;
    }

    public String getTitle() {
        return title;
    }

    public String title() {
        return title;
    }

    public void title(String title) {
        this.title = title;
    }

    public void setTitle(String title) {
        title(title);
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
        layoutButtons();
    }

    private void layoutButtons() {
        double buttonY = y + BUTTON_Y_OFFSET;
        double cursorX = x + width - BUTTON_MARGIN - BUTTON_SIZE;
        closeButton.setPosition(cursorX, buttonY);
        cursorX -= BUTTON_GAP + BUTTON_SIZE;
        for (int i = titlebarButtons.size() - 1; i >= 0; i--) {
            titlebarButtons.get(i).setPosition(cursorX, buttonY);
            cursorX -= BUTTON_GAP + BUTTON_SIZE;
        }
    }
}
