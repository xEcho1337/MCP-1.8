package it.echo.kira.gui.widget;

import it.echo.kira.gui.Palette;
import it.echo.kira.gui.Render2D;
import it.echo.kira.gui.UiText;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/**
 * Player row with avatar, name and badge.
 */
public class PlayerRow extends Widget {

    private static final int AVATAR_SIZE = 12;
    private static final int PADDING_X = 6;
    private static final int AVATAR_TOP = new Color(176, 58, 46).getRGB();
    private static final int AVATAR_EYES = new Color(255, 255, 255).getRGB();
    private static final int AVATAR_OUTLINE = new Color(0, 0, 0, 128).getRGB();

    private String name;
    private int avatarColor;
    private Runnable onClick;

    public PlayerRow(double x, double y, double width, double height, String name, int avatarColor) {
        super(x, y, width, height);
        this.name = name;
        this.avatarColor = avatarColor;
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
        UiText.draw(font, name, (float) (x + PADDING_X + AVATAR_SIZE + 4.0D), textY, Palette.TEXT_PRIMARY, shadow);
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
        Render2D.rect(avatarX, avatarY, AVATAR_SIZE, 3.0D, AVATAR_TOP);
        Render2D.rect(avatarX + 2.0D, avatarY + 5.0D, 2.0D, 2.0D, AVATAR_EYES);
        Render2D.rect(avatarX + 8.0D, avatarY + 5.0D, 2.0D, 2.0D, AVATAR_EYES);
        Render2D.outline(avatarX, avatarY, AVATAR_SIZE, AVATAR_SIZE, AVATAR_OUTLINE);
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
