package it.echo.kira.gui.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Splits a rect into segments along one axis.
 */
public final class Layout {

    private final Direction direction;
    private final List<Constraint> constraints;
    private int margin;
    private int spacing;

    private Layout(Direction direction, List<Constraint> constraints) {
        this.direction = direction;
        this.constraints = new ArrayList<>(constraints);
    }

    /**
     * Vertical stack, top to bottom.
     *
     * @param constraints one per row
     * @return builder
     */
    public static Layout vertical(Constraint... constraints) {
        return new Layout(Direction.VERTICAL, Arrays.asList(constraints));
    }

    /**
     * Horizontal row, left to right.
     *
     * @param constraints one per column
     * @return builder
     */
    public static Layout horizontal(Constraint... constraints) {
        return new Layout(Direction.HORIZONTAL, Arrays.asList(constraints));
    }

    /**
     * Uniform outer margin kept untouched around the split area.
     *
     * @param margin px
     * @return this builder
     */
    public Layout margin(int margin) {
        this.margin = Math.max(0, margin);
        return this;
    }

    /**
     * Gap between segments.
     *
     * @param spacing px
     * @return this builder
     */
    public Layout spacing(int spacing) {
        this.spacing = Math.max(0, spacing);
        return this;
    }

    /**
     * Splits the area.
     *
     * @param area area to split
     * @return one rect per constraint, in order
     */
    public List<Rect> split(Rect area) {
        Rect outer = area.inner(margin);
        int count = constraints.size();
        if (count == 0) {
            return List.of(outer);
        }
        int total = direction == Direction.VERTICAL ? outer.height() : outer.width();
        int gaps = spacing * Math.max(0, count - 1);
        int available = Math.max(0, total - gaps);

        int[] sizes = new int[count];
        int fixed = 0;
        int flexWeight = 0;
        for (int i = 0; i < count; i++) {
            Constraint constraint = constraints.get(i);
            if (constraint instanceof Constraint.Length length) {
                sizes[i] = Math.min(length.pixels(), available);
                fixed += sizes[i];
            } else if (constraint instanceof Constraint.Percentage percentage) {
                sizes[i] = available * percentage.percent() / 100;
                fixed += sizes[i];
            } else if (constraint instanceof Constraint.Fill fill) {
                flexWeight += fill.weight();
            } else if (constraint instanceof Constraint.Min || constraint instanceof Constraint.Max) {
                flexWeight += 1;
            }
        }

        int remaining = Math.max(0, available - fixed);
        for (int i = 0; i < count; i++) {
            Constraint constraint = constraints.get(i);
            if (constraint instanceof Constraint.Fill fill) {
                sizes[i] = flexWeight == 0 ? 0 : remaining * fill.weight() / flexWeight;
            } else if (constraint instanceof Constraint.Min min) {
                int share = flexWeight == 0 ? 0 : remaining / flexWeight;
                sizes[i] = Math.max(min.pixels(), share);
            } else if (constraint instanceof Constraint.Max max) {
                int share = flexWeight == 0 ? 0 : remaining / flexWeight;
                sizes[i] = Math.min(max.pixels(), share);
            }
        }

        List<Rect> result = new ArrayList<>(count);
        int cursorX = outer.x();
        int cursorY = outer.y();
        for (int i = 0; i < count; i++) {
            int size = Math.max(0, sizes[i]);
            Rect chunk;
            if (direction == Direction.VERTICAL) {
                chunk = new Rect(outer.x(), cursorY, outer.width(), size);
                cursorY += size + spacing;
            } else {
                chunk = new Rect(cursorX, outer.y(), size, outer.height());
                cursorX += size + spacing;
            }
            result.add(chunk);
        }
        return result;
    }

    public Direction direction() {
        return direction;
    }

    public int margin() {
        return margin;
    }

    public int spacing() {
        return spacing;
    }
}
