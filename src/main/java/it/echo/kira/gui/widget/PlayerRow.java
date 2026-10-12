package it.echo.kira.gui.widget;

import it.echo.kira.gui.Palette;
import it.echo.kira.gui.Render2D;
import it.echo.kira.gui.UiText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/**
 * Player row with avatar, name and badge.
 */
public class PlayerRow extends Widget {

    private static final int AVATAR_SIZE = 12;
    private static final int PADDING_X = 6;

    private String name;
    private int avatarColor;
    private boolean crown;
    private Runnable onClick;

    public PlayerRow(double x, double y, double width, double height, String name, int avatarColor, boolean crown) {
        super(x, y, width, height);
        this.name = name;
        this.avatarColor = avatarColor;
        this.crown = crown;
    }

    public PlayerRow onClick(Runnable onClick) {
        this.onClick = onClick;
        return this;
    }

    @Override
    public void draw(Minecraft minecraft, FontRenderer font, int mouseX, int mouseY, float partialTicks) {
        if (!visible) {
            return;
        }
        updateHovered(mouseX, mouseY);
        Render2D.rect(x, y, width, height, hovered ? Palette.ROW_HOVER : Palette.ROW_BG);
        double avatarY = y + (height - AVATAR_SIZE) / 2.0D;
        drawAvatar(x + PADDING_X, avatarY);
        float textY = (float) (y + (height - 8.0D) / 2.0D);
        UiText.draw(font, name, (float) (x + PADDING_X + AVATAR_SIZE + 4.0D), textY, Palette.TEXT_PRIMARY);
        if (crown) {
            float crownX = (float) (x + PADDING_X + AVATAR_SIZE + 4.0D + font.getStringWidth(name) + 4.0D);
            // Vanilla font has no crown glyph, use a gold star-like marker
            UiText.draw(font, "\u264B", crownX, textY, 0xFFFFC040);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        boolean pressed = super.mouseClicked(mouseX, mouseY, button);
        if (pressed && onClick != null) {
            onClick.run();
        }
        return pressed;
    }

    private void drawAvatar(double avatarX, double avatarY) {
        Render2D.rect(avatarX, avatarY, AVATAR_SIZE, AVATAR_SIZE, avatarColor);
        Render2D.rect(avatarX, avatarY, AVATAR_SIZE, 3.0D, 0xFFB03A2E);
        Render2D.rect(avatarX + 2.0D, avatarY + 5.0D, 2.0D, 2.0D, 0xFFFFFFFF);
        Render2D.rect(avatarX + 8.0D, avatarY + 5.0D, 2.0D, 2.0D, 0xFFFFFFFF);
        Render2D.outline(avatarX, avatarY, AVATAR_SIZE, AVATAR_SIZE, 0x80000000);
    }

    public String getName() {
        return name;
    }

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }

    public void setName(String name) {
        name(name);
    }
}
