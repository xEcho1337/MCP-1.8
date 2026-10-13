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
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;

/**
 * Draggable armor panel with a durability percent per piece.
 */
public class ArmorElement extends HudElement {

    private static final int ROW_HEIGHT = 20;
    private static final int ICON_SIZE = 16;
    private static final int PADDING = 4;
    private static final int TEXT_GAP = 4;
    private static final int BACKGROUND = new Color(35, 35, 35, 170).getRGB();
    private static final int HIGH_DURABILITY = new Color(85, 255, 85).getRGB();
    private static final int MID_DURABILITY = new Color(255, 255, 85).getRGB();
    private static final int LOW_DURABILITY = new Color(255, 85, 85).getRGB();
    private static final String UNKNOWN_DURABILITY = "--";

    public ArmorElement(double x, double y) {
        super(x, y, 64.0D, ROW_HEIGHT * 4 + PADDING * 2);
    }

    @Override
    protected void renderContent(Minecraft minecraft, FontRenderer font, int mouseX, int mouseY, float partialTicks) {
        List<ItemStack> worn = wornArmor(minecraft);

        if (worn.isEmpty()) {
            renderPlaceholder(font);
            return;
        }

        List<String> labels = new ArrayList<>(worn.size());
        int textWidth = 0;

        for (ItemStack stack : worn) {
            String label = durabilityLabel(stack);
            labels.add(label);
            textWidth = Math.max(textWidth, font.getStringWidth(label));
        }

        width(PADDING * 2 + ICON_SIZE + TEXT_GAP + textWidth);
        height(PADDING * 2 + worn.size() * ROW_HEIGHT);

        Render2D.rect(x, y, width, height, BACKGROUND);
        Render2D.outline(x, y, width, height, Palette.HAIRLINE);

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        RenderItem render = minecraft.getRenderItem();
        ScaledResolution resolution = new ScaledResolution(MC);

        for (int i = 0; i < worn.size(); i++) {
            ItemStack stack = worn.get(i);

            // true if the element is on the right side of the screen
            boolean textDirection = (x + width / 2.0) > resolution.getScaledWidth() / 2.0D;

            int iconX = (int) (x + PADDING);
            int iconY = (int) (y + PADDING + i * ROW_HEIGHT + (ROW_HEIGHT - ICON_SIZE) / 2.0D);

            float textX = iconX + ICON_SIZE + PADDING;
            float textY = (float) (y + PADDING + 2 + i * ROW_HEIGHT + (ROW_HEIGHT - font.FONT_HEIGHT) / 2.0D);

            int actualIconX = textDirection ? (int) (textX + PADDING / 2.0) : iconX;
            int actualTextX = (int) (textDirection ? iconX + PADDING / 2.0 : textX);

            render.renderItemAndEffectIntoGUI(stack, actualIconX, iconY);
            render.renderItemOverlays(font, stack, actualIconX, iconY);

            UiText.draw(font, labels.get(i), actualTextX, textY, durabilityColor(stack), shadow);
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
    }

    private List<ItemStack> wornArmor(Minecraft minecraft) {
        List<ItemStack> worn = new ArrayList<>(4);
        if (minecraft.thePlayer == null || minecraft.theWorld == null) {
            return worn;
        }
        ItemStack[] armor = minecraft.thePlayer.inventory.armorInventory;
        for (int slot = armor.length - 1; slot >= 0; slot--) {
            if (armor[slot] != null) {
                worn.add(armor[slot]);
            }
        }
        return worn;
    }

    private void renderPlaceholder(FontRenderer font) {
        if (!Kira.hud().editMode()) {
            return;
        }
        Render2D.rect(x, y, width, height, BACKGROUND);
        Render2D.outline(x, y, width, height, Palette.HAIRLINE);
        UiText.drawCentered(font, "Armor", (float) (x + width / 2.0D),
                (float) (y + height / 2.0D - font.FONT_HEIGHT / 2), Palette.TEXT_FAINT, shadow);
    }

    private static String durabilityLabel(ItemStack stack) {
        if (stack.getMaxDamage() <= 0) {
            return UNKNOWN_DURABILITY;
        }
        int percent = (stack.getMaxDamage() - stack.getItemDamage()) * 100 / stack.getMaxDamage();
        return percent + "%";
    }

    private static int durabilityColor(ItemStack stack) {
        if (stack.getMaxDamage() <= 0) {
            return Palette.TEXT_FAINT;
        }
        int percent = (stack.getMaxDamage() - stack.getItemDamage()) * 100 / stack.getMaxDamage();
        if (percent > 50) {
            return HIGH_DURABILITY;
        }
        if (percent > 25) {
            return MID_DURABILITY;
        }
        return LOW_DURABILITY;
    }
}
