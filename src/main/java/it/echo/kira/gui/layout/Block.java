package it.echo.kira.gui.layout;

import it.echo.kira.gui.Palette;
import it.echo.kira.gui.Render2D;
import it.echo.kira.gui.UiText;
import net.minecraft.client.gui.FontRenderer;

/**
 * Bordered box with optional title.
 */
public final class Block {

    private final String title;
    private final int background;
    private final int border;
    private final boolean bordered;
    private final int padding;
    private final boolean shadow;

    private Block(Builder builder) {
        this.title = builder.title;
        this.background = builder.background;
        this.border = builder.border;
        this.bordered = builder.bordered;
        this.padding = builder.padding;
        this.shadow = builder.shadow;
    }

    /**
     * Starts a bordered box builder.
     *
     * @return builder
     */
    public static Builder bordered() {
        return new Builder(true);
    }

    /**
     * Starts a plain box builder without border.
     *
     * @return builder
     */
    public static Builder plain() {
        return new Builder(false);
    }

    /**
     * Draws the frame over the area.
     *
     * @param font font for the title, may be null when no title is set
     * @param area area to frame
     */
    public void render(FontRenderer font, Rect area) {
        if (background != 0) {
            Render2D.rect(area.x(), area.y(), area.width(), area.height(), background);
        }
        if (bordered) {
            Render2D.outline(area.x(), area.y(), area.width(), area.height(), border);
        }
        if (title != null && !title.isEmpty() && font != null) {
            int textY = area.y() + 4;
            UiText.draw(font, title, (float) (area.x() + 6), (float) textY, Palette.TEXT_DIM, shadow);
        }
    }

    /**
     * Returns the content area inside border + padding + title strip.
     *
     * @param area outer area
     * @return inner area for widgets
     */
    public Rect inner(Rect area) {
        int frame = bordered ? 1 : 0;
        int topExtra = (title != null && !title.isEmpty()) ? 14 : 0;
        return area.inner(frame + padding, frame + padding + topExtra, frame + padding, frame + padding);
    }

    public String title() {
        return title;
    }

    public boolean shadow() {
        return shadow;
    }

    /**
     * Fluent builder.
     */
    public static final class Builder {

        private final boolean bordered;
        private String title = "";
        private int background;
        private int border = Palette.BORDER;
        private int padding;
        private boolean shadow = true;

        private Builder(boolean bordered) {
            this.bordered = bordered;
        }

        /**
         * Sets the title drawn at the top-left.
         *
         * @param title title text
         * @return this builder
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * Sets the fill color, 0 means transparent.
         *
         * @param background packed ARGB or 0
         * @return this builder
         */
        public Builder background(int background) {
            this.background = background;
            return this;
        }

        /**
         * Sets the border color.
         *
         * @param border packed ARGB
         * @return this builder
         */
        public Builder border(int border) {
            this.border = border;
            return this;
        }

        /**
         * Sets inner padding applied by {@link Block#inner}.
         *
         * @param padding px
         * @return this builder
         */
        public Builder padding(int padding) {
            this.padding = Math.max(0, padding);
            return this;
        }

        /**
         * Toggles title shadow.
         *
         * @param shadow true for shadowed text
         * @return this builder
         */
        public Builder shadow(boolean shadow) {
            this.shadow = shadow;
            return this;
        }

        /**
         * Builds the block.
         *
         * @return block
         */
        public Block build() {
            return new Block(this);
        }
    }
}
