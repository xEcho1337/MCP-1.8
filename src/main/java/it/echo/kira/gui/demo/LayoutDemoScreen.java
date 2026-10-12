package it.echo.kira.gui.demo;

import it.echo.kira.gui.Palette;
import it.echo.kira.gui.Render2D;
import it.echo.kira.gui.UiText;
import it.echo.kira.gui.layout.Block;
import it.echo.kira.gui.layout.Constraint;
import it.echo.kira.gui.layout.Layout;
import it.echo.kira.gui.layout.Presets;
import it.echo.kira.gui.layout.Rect;
import it.echo.kira.gui.widget.IconButton;
import it.echo.kira.gui.widget.ModernButton;
import it.echo.kira.gui.widget.PlayerRow;
import it.echo.kira.gui.widget.SectionHeader;
import it.echo.kira.gui.widget.SideNavItem;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;

public class LayoutDemoScreen extends GuiScreen {

    private static final int WINDOW_WIDTH = 300;
    private static final int WINDOW_HEIGHT = 275;

    private record StatusLine(String label, String value, int valueColor) {
    }

    private final List<SideNavItem> nav = new ArrayList<>();
    private final List<StatusLine> status = new ArrayList<>();
    private final Block windowFrame = Block.bordered()
            .border(Palette.TITLEBAR_BG)
            .build();

    private Rect window;
    private Presets.AppWindow app;
    private SectionHeader header;
    private PlayerRow playerRow;
    private ModernButton hostButton;
    private ModernButton addButton;
    private IconButton closeButton;

    @Override
    public void initGui() {
        nav.clear();
        status.clear();

        window = new Rect(0, 0, width, height).centered(WINDOW_WIDTH, WINDOW_HEIGHT);
        app = Presets.appWindow(window, 26, 100, 0);

        Rect title = app.title();
        closeButton = new IconButton(title.x() + title.width() - 18 - 6, title.y() + 4,
                IconButton.Icon.CLOSE);
        closeButton.onClick(() -> mc.displayGuiScreen(null));

        List<Rect> navRows = Layout.vertical(
                Constraint.length(16),
                Constraint.length(16),
                Constraint.length(16),
                Constraint.length(16),
                Constraint.fill(1)
        ).spacing(2).split(app.sidebar().inner(12));
        String[] labels = {"Members", "General", "Game Rules", "Packs"};

        for (int i = 0; i < labels.length; i++) {
            Rect row = navRows.get(i);
            SideNavItem item = new SideNavItem(row.x(), row.y(), row.width(), 16.0D, labels[i]);
            item.selected(i == 0);
            nav.add(item);
        }

        status.add(new StatusLine("Status", "Offline", Palette.STATUS_OFFLINE));
        status.add(new StatusLine("Host", "n/a", Palette.TEXT_DIM));
        status.add(new StatusLine("Version", "26.3", Palette.TEXT_DIM));

        Rect content = app.content().inner(12);
        header = new SectionHeader(content.x(), content.y(), content.width(), "Playing [1/8]");
        playerRow = new PlayerRow(content.x(), content.y() + 18, content.width(), 22.0D, "NotEchq",
                new Color(122, 92, 78).getRGB());

        Rect buttonsPos = new Rect(content.x(), content.y() + content.height() - 16, content.width(), 18);
        Presets.TwoColumn buttons = Presets.twoColumn(buttonsPos, 8);

        Rect left = buttons.left();
        Rect right = buttons.right();

        hostButton = new ModernButton(left.x(), left.y(), left.width(), left.height(), "Start Hosting", ModernButton.Style.GREEN);
        addButton = new ModernButton(right.x(), right.y(), right.width(), right.height(), "Add Members", ModernButton.Style.BLUE);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        Rect title = app.title();

        Render2D.dimBackground(width, height);
        Render2D.rect(title.x(), title.y(), title.width(), title.height(), Palette.TITLEBAR_BG);

        UiText.drawShadow(fontRendererObj, "New World", (float) (title.x() + 12),
                (float) (title.y() + 9), Palette.TEXT_PRIMARY);
        closeButton.draw(mc, fontRendererObj, mouseX, mouseY, partialTicks);

        Presets.paintAppWindow(app, Palette.SIDEBAR_BG, Palette.CONTENT_BG, Palette.SIDEBAR_DIVIDER);

        for (SideNavItem item : nav) {
            item.draw(mc, fontRendererObj, mouseX, mouseY, partialTicks);
        }

        double statusY = app.sidebar().y() + app.sidebar().height() - 12 - 78;
        for (StatusLine line : status) {
            UiText.drawShadow(fontRendererObj, line.label(), (float) (app.sidebar().x() + 12), (float) statusY,
                    Palette.TEXT_FAINT);
            statusY += 10.0D;
            UiText.drawShadow(fontRendererObj, line.value(), (float) (app.sidebar().x() + 12), (float) statusY,
                    line.valueColor());
            statusY += 16.0D;
        }

        header.draw(mc, fontRendererObj, mouseX, mouseY, partialTicks);
        playerRow.draw(mc, fontRendererObj, mouseX, mouseY, partialTicks);
        hostButton.draw(mc, fontRendererObj, mouseX, mouseY, partialTicks);
        addButton.draw(mc, fontRendererObj, mouseX, mouseY, partialTicks);
        windowFrame.render(fontRendererObj, window);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        closeButton.mouseClicked(mouseX, mouseY, mouseButton);
        for (SideNavItem item : nav) {
            if (item.mouseClicked(mouseX, mouseY, mouseButton)) {
                for (SideNavItem other : nav) {
                    other.selected(other == item);
                }
            }
        }

        playerRow.mouseClicked(mouseX, mouseY, mouseButton);
        hostButton.mouseClicked(mouseX, mouseY, mouseButton);
        addButton.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
