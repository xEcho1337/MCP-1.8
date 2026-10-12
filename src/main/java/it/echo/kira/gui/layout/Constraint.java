package it.echo.kira.gui.layout;

/**
 * Size rule for one layout segment.
 */
public sealed interface Constraint permits Constraint.Length, Constraint.Percentage, Constraint.Fill, Constraint.Min,
        Constraint.Max {

    /**
     * Fixed pixel size.
     *
     * @param pixels size in px
     * @return constraint
     */
    static Constraint length(int pixels) {
        return new Length(Math.max(0, pixels));
    }

    /**
     * Share of the available space.
     *
     * @param percent 0-100
     * @return constraint
     */
    static Constraint percentage(int percent) {
        return new Percentage(Math.max(0, Math.min(100, percent)));
    }

    /**
     * Flexible share of whatever is left, weighted.
     *
     * @param weight relative weight, higher takes more
     * @return constraint
     */
    static Constraint fill(int weight) {
        return new Fill(Math.max(1, weight));
    }

    /**
     * Flexible with a guaranteed minimum.
     *
     * @param pixels minimum px
     * @return constraint
     */
    static Constraint min(int pixels) {
        return new Min(Math.max(0, pixels));
    }

    /**
     * Flexible capped at a maximum.
     *
     * @param pixels maximum px
     * @return constraint
     */
    static Constraint max(int pixels) {
        return new Max(Math.max(0, pixels));
    }

    /**
     * Fixed size.
     *
     * @param pixels px
     */
    record Length(int pixels) implements Constraint {
    }

    /**
     * Percentage share.
     *
     * @param percent 0-100
     */
    record Percentage(int percent) implements Constraint {
    }

    /**
     * Weighted flexible share.
     *
     * @param weight weight
     */
    record Fill(int weight) implements Constraint {
    }

    /**
     * Flexible with minimum.
     *
     * @param pixels minimum px
     */
    record Min(int pixels) implements Constraint {
    }

    /**
     * Flexible with maximum.
     *
     * @param pixels maximum px
     */
    record Max(int pixels) implements Constraint {
    }
}
