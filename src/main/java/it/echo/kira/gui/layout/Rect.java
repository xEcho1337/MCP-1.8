package it.echo.kira.gui.layout;

/**
 * Integer pixel area, the unit everything splits and renders into.
 *
 * @param x left
 * @param y top
 * @param width width in px
 * @param height height in px
 */
public record Rect(int x, int y, int width, int height) {

    public static Rect of(double x, double y, double width, double height) {
        return new Rect((int) Math.round(x), (int) Math.round(y), (int) Math.round(width),
                (int) Math.round(height));
    }

    /**
     * Shrinks the area by a uniform margin.
     *
     * @param margin px on every side
     * @return inner rect, never negative sized
     */
    public Rect inner(int margin) {
        return inner(margin, margin, margin, margin);
    }

    /**
     * Shrinks the area with per-side margins.
     *
     * @param left left inset
     * @param top top inset
     * @param right right inset
     * @param bottom bottom inset
     * @return inner rect
     */
    public Rect inner(int left, int top, int right, int bottom) {
        int nextX = x + left;
        int nextY = y + top;
        int nextWidth = Math.max(0, width - left - right);
        int nextHeight = Math.max(0, height - top - bottom);
        return new Rect(nextX, nextY, nextWidth, nextHeight);
    }

    /**
     * Checks cursor containment.
     *
     * @param mouseX cursor x
     * @param mouseY cursor y
     * @return true when inside
     */
    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    /**
     * Centers a box of the given size inside this rect.
     *
     * @param innerWidth box width
     * @param innerHeight box height
     * @return centered rect
     */
    public Rect centered(int innerWidth, int innerHeight) {
        return new Rect(x + (width - innerWidth) / 2, y + (height - innerHeight) / 2, innerWidth, innerHeight);
    }
}
