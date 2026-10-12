package it.echo.kira.gui.demo;

import it.echo.kira.gui.Palette;
import it.echo.kira.gui.Render2D;
import it.echo.kira.gui.UiText;
import it.echo.kira.gui.layout.Block;
import it.echo.kira.gui.layout.Constraint;
import it.echo.kira.gui.layout.Layout;
import it.echo.kira.gui.layout.Presets;
import it.echo.kira.gui.layout.Rect;
import it.echo.kira.gui.widget.ModernButton;
import it.echo.kira.gui.widget.SectionHeader;
import it.echo.kira.gui.widget.SideNavItem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;

public class LayoutDemoScreen extends GuiScreen {

    private static final int WINDOW_WIDTH = 350;
    private static final int WINDOW_HEIGHT = 300;

    private final List<SideNavItem> nav = new ArrayList<>();
    private final Block windowBlock = Block.bordered().background(Palette.WINDOW_BG).build();

    private Rect window;
    private Presets.AppWindow app;
    private SectionHeader header;
    private ModernButton hostButton;
    private ModernButton addButton;

    @Override
    public void initGui() {
        nav.clear();

        window = new Rect(0, 0, width, height).centered(WINDOW_WIDTH, WINDOW_HEIGHT);
        app = Presets.appWindow(window, 26, 132, 0);

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

        Rect content = app.content().inner(12);
        header = new SectionHeader(content.x(), content.y(), content.width(), "Playing [1/8]");

        Presets.TwoColumn buttons = Presets.twoColumn(
                new Rect(content.x(), content.y() + content.height() - 22, content.width(), 22), 8);

        Rect left = buttons.left();
        Rect right = buttons.right();

        hostButton = new ModernButton(left.x(), left.y(), left.width(), left.height(), "Start Hosting", ModernButton.Style.GREEN);
        addButton = new ModernButton(right.x(), right.y(), right.width(), right.height(), "Add Members", ModernButton.Style.BLUE);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        Render2D.dimBackground(width, height);
        windowBlock.render(fontRendererObj, window);

        Render2D.rect(app.title().x(), app.title().y(), app.title().width(), app.title().height(), Palette.TITLEBAR_BG);

        UiText.draw(fontRendererObj, "New World (layout engine)", (float) (app.title().x() + 12),
                (float) (app.title().y() + 9), Palette.TEXT_PRIMARY);

        for (SideNavItem item : nav) {
            item.draw(mc, fontRendererObj, mouseX, mouseY, partialTicks);
        }

        header.draw(mc, fontRendererObj, mouseX, mouseY, partialTicks);
        hostButton.draw(mc, fontRendererObj, mouseX, mouseY, partialTicks);
        addButton.draw(mc, fontRendererObj, mouseX, mouseY, partialTicks);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (SideNavItem item : nav) {
            if (item.mouseClicked(mouseX, mouseY, mouseButton)) {
                for (SideNavItem other : nav) {
                    other.selected(other == item);
                }
            }
        }

        hostButton.mouseClicked(mouseX, mouseY, mouseButton);
        addButton.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
