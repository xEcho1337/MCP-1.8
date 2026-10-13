package it.echo.kira.hud.impl;

import it.echo.kira.Kira;
import it.echo.kira.gui.Palette;
import it.echo.kira.gui.Render2D;
import it.echo.kira.gui.UiText;
import it.echo.kira.hud.HudElement;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

/**
 * Draggable panel listing active potion effects with icon, name and duration.
 */
public class PotionEffects extends HudElement {

    private static final ResourceLocation INVENTORY_TEXTURE =
            new ResourceLocation("textures/gui/container/inventory.png");
    private static final int ROW_HEIGHT = 22;
    private static final int ICON_SIZE = 18;
    private static final int PADDING = 5;
    private static final int TEXT_GAP = 4;
    private static final int BACKGROUND = new Color(35, 35, 35, 170).getRGB();
    private static final int MAX_LEVEL_KEY = 10;

    private final Gui iconRenderer = new Gui();

    public PotionEffects(double x, double y) {
        super(x, y, 110.0D, ROW_HEIGHT + PADDING * 2);
    }

    @Override
    protected void renderContent(Minecraft minecraft, FontRenderer font, int mouseX, int mouseY, float partialTicks) {
        List<Row> potions = activeRows(minecraft);

        if (potions.isEmpty()) {
            renderPlaceholder(font);
            return;
        }

        int textWidth = 0;

        for (Row row : potions) {
            textWidth = Math.max(textWidth, font.getStringWidth(row.name()));
            textWidth = Math.max(textWidth, font.getStringWidth(row.duration()));
        }

        width(PADDING * 2 + ICON_SIZE + TEXT_GAP + textWidth);
        height(PADDING * 2 + potions.size() * ROW_HEIGHT);

        Render2D.rect(x, y, width, height, BACKGROUND);
        Render2D.outline(x, y, width, height, Palette.HAIRLINE);

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();

        for (int i = 0; i < potions.size(); i++) {
            Row potionRow = potions.get(i);
            double rowY = y + PADDING + i * ROW_HEIGHT;
            int index = potionRow.iconIndex();

            // Rebind every row: text rendering swaps the bound texture to the font atlas
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            minecraft.getTextureManager().bindTexture(INVENTORY_TEXTURE);

            if (index >= 0) {
                int iconX = (int) (x + PADDING);
                int iconY = (int) (rowY + (ROW_HEIGHT - ICON_SIZE) / 2.0D);

                iconRenderer.drawTexturedModalRect(iconX, iconY, index % 8 * ICON_SIZE,
                        198 + index / 8 * ICON_SIZE, ICON_SIZE, ICON_SIZE);
            }

            float textX = (float) (x + PADDING + ICON_SIZE + TEXT_GAP);
            UiText.draw(font, potionRow.name(), textX, (float) (rowY + 2), Palette.TEXT_PRIMARY, shadow);
            UiText.draw(font, potionRow.duration(), textX, (float) (rowY + 12), Palette.TEXT_DIM, shadow);
        }
    }

    private List<Row> activeRows(Minecraft minecraft) {
        List<Row> rows = new ArrayList<>();

        if (minecraft.thePlayer == null || minecraft.theWorld == null) {
            return rows;
        }

        for (PotionEffect effect : minecraft.thePlayer.getActivePotionEffects()) {
            int id = effect.getPotionID();

            if (id < 0 || id >= Potion.potionTypes.length || Potion.potionTypes[id] == null) {
                continue;
            }

            Potion potion = Potion.potionTypes[id];
            int icon = potion.hasStatusIcon() ? potion.getStatusIconIndex() : -1;
            rows.add(new Row(displayName(effect, potion), Potion.getDurationString(effect), icon));
        }

        return rows;
    }

    private void renderPlaceholder(FontRenderer font) {
        if (!Kira.hud().editMode()) {
            return;
        }

        Render2D.rect(x, y, width, height, BACKGROUND);
        Render2D.outline(x, y, width, height, Palette.HAIRLINE);

        UiText.drawCentered(font, "Potions", (float) (x + width / 2.0D),
                (float) (y + height / 2.0D - font.FONT_HEIGHT / 2.0D), Palette.TEXT_FAINT, shadow);
    }

    private static String displayName(PotionEffect effect, Potion potion) {
        String name = I18n.format(potion.getName());

        if (effect.getAmplifier() <= 0) {
            return name;
        }

        int level = effect.getAmplifier() + 1;
        String suffix = level <= MAX_LEVEL_KEY
                ? I18n.format("enchantment.level." + level)
                : Integer.toString(level);

        return name + " " + suffix;
    }

    private record Row(String name, String duration, int iconIndex) {
    }
}
