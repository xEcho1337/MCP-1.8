package it.echo.kira.gui.layout;

import it.echo.kira.gui.Render2D;
import it.echo.kira.gui.widget.Widget;
import java.util.List;

/**
 * Ready-made rect splits.
 */
public final class Presets {

    private Presets() {
    }

    /**
     * Title strip on top, body below.
     *
     * @param window full window
     * @param titleHeight title strip px
     * @return layout
     */
    public static TitleBody titleBody(Rect window, int titleHeight) {
        List<Rect> rows = Layout.vertical(Constraint.length(titleHeight), Constraint.fill(1)).split(window);
        return new TitleBody(rows.get(0), rows.get(1));
    }

    /**
     * Sidebar on the left, content on the right.
     *
     * @param body body area
     * @param sidebarWidth sidebar px
     * @param gap gap between columns
     * @return layout
     */
    public static SidebarBody sidebar(Rect body, int sidebarWidth, int gap) {
        List<Rect> columns = Layout.horizontal(Constraint.length(sidebarWidth), Constraint.fill(1))
                .spacing(gap)
                .split(body);
        return new SidebarBody(columns.get(0), columns.get(1));
    }

    /**
     * Full app window: titlebar, then sidebar + content.
     *
     * @param window full window
     * @param titleHeight title strip px
     * @param sidebarWidth sidebar px
     * @param gap gap between sidebar and content
     * @return layout
     */
    public static AppWindow appWindow(Rect window, int titleHeight, int sidebarWidth, int gap) {
        TitleBody titleBody = titleBody(window, titleHeight);
        SidebarBody sidebarBody = sidebar(titleBody.body(), sidebarWidth, gap);
        return new AppWindow(titleBody.title(), sidebarBody.sidebar(), sidebarBody.content());
    }

    /**
     * Two equal columns with a gap.
     *
     * @param body body area
     * @param gap gap between columns
     * @return layout
     */
    public static TwoColumn twoColumn(Rect body, int gap) {
        List<Rect> columns = Layout.horizontal(Constraint.fill(1), Constraint.fill(1))
                .spacing(gap)
                .split(body);
        return new TwoColumn(columns.get(0), columns.get(1));
    }

    /**
     * Header, body and footer rows.
     *
     * @param window full window
     * @param headerHeight header px
     * @param footerHeight footer px
     * @param gap vertical gaps
     * @return layout
     */
    public static HeaderBodyFooter headerBodyFooter(Rect window, int headerHeight, int footerHeight, int gap) {
        List<Rect> rows = Layout.vertical(Constraint.length(headerHeight), Constraint.fill(1),
                Constraint.length(footerHeight)).spacing(gap).split(window);
        return new HeaderBodyFooter(rows.get(0), rows.get(1), rows.get(2));
    }

    /**
     * Fills the app body with two tones plus divider.
     *
     * @param app split areas
     * @param sidebarBg sidebar fill
     * @param contentBg content fill
     * @param divider divider line color
     */
    public static void paintAppWindow(AppWindow app, int sidebarBg, int contentBg, int divider) {
        Rect sidebar = app.sidebar();
        Rect content = app.content();
        Render2D.rect(sidebar.x(), sidebar.y(), sidebar.width(), sidebar.height(), sidebarBg);
        Render2D.rect(content.x(), content.y(), content.width(), content.height(), contentBg);
        Render2D.verticalLine(content.x(), sidebar.y(), sidebar.y() + sidebar.height(), divider);
    }

    /**
     * Moves a widget into a rect.
     *
     * @param widget widget to move
     * @param area target area
     */
    public static void place(Widget widget, Rect area) {
        widget.setPosition(area.x(), area.y());
        widget.setSize(area.width(), area.height());
    }

    /**
     * Title + body areas.
     *
     * @param title title strip
     * @param body remaining body
     */
    public record TitleBody(Rect title, Rect body) {
    }

    /**
     * Sidebar + content areas.
     *
     * @param sidebar left column
     * @param content right column
     */
    public record SidebarBody(Rect sidebar, Rect content) {
    }

    /**
     * Complete window split.
     *
     * @param title title strip
     * @param sidebar left column
     * @param content right column
     */
    public record AppWindow(Rect title, Rect sidebar, Rect content) {
    }

    /**
     * Two side-by-side areas.
     *
     * @param left left column
     * @param right right column
     */
    public record TwoColumn(Rect left, Rect right) {
    }

    /**
     * Three stacked areas.
     *
     * @param header top strip
     * @param body middle area
     * @param footer bottom strip
     */
    public record HeaderBodyFooter(Rect header, Rect body, Rect footer) {
    }
}
