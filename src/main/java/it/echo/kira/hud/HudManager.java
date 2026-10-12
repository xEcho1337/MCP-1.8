package it.echo.kira.hud;

import it.echo.kira.gui.Palette;
import it.echo.kira.gui.Render2D;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/**
 * Stores draggable HUD elements and routes mouse input while edit mode is on.
 */
public final class HudManager {

    private final List<HudElement> elements = new ArrayList<>();
    private boolean editMode;

    /**
     * Adds an element.
     *
     * @param element element to store
     */
    public void add(HudElement element) {
        elements.add(element);
    }

    /**
     * Returns elements in z-order, last entry is on top.
     *
     * @return live list, do not replace
     */
    public List<HudElement> all() {
        return elements;
    }

    public boolean editMode() {
        return editMode;
    }

    public void editMode(boolean editMode) {
        if (!editMode) {
            for (HudElement element : elements) {
                element.handleRelease();
            }
        }
        this.editMode = editMode;
    }

    /**
     * Draws every element, plus a move outline when edit mode is on.
     *
     * @param minecraft mc instance
     * @param font font renderer
     * @param mouseX scaled mouse x
     * @param mouseY scaled mouse y
     * @param partialTicks partial ticks
     */
    public void renderAll(Minecraft minecraft, FontRenderer font, int mouseX, int mouseY, float partialTicks) {
        for (HudElement element : elements) {
            element.draw(minecraft, font, mouseX, mouseY, partialTicks);
            if (editMode) {
                Render2D.outline(element.x(), element.y(), element.width(), element.height(),
                        Palette.TEXT_SELECTED);
            }
        }
    }

    /**
     * Starts dragging the top-most element under the cursor.
     *
     * @param mouseX scaled x
     * @param mouseY scaled y
     * @param button mouse button
     * @return true when an element grabbed the click
     */
    public boolean mousePressed(int mouseX, int mouseY, int button) {
        if (!editMode) {
            return false;
        }
        for (int i = elements.size() - 1; i >= 0; i--) {
            if (elements.get(i).handlePress(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates active drags.
     *
     * @param mouseX scaled x
     * @param mouseY scaled y
     * @param screenWidth scaled width
     * @param screenHeight scaled height
     */
    public void mouseMoved(int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (!editMode) {
            return;
        }
        for (HudElement element : elements) {
            element.handleMove(mouseX, mouseY, screenWidth, screenHeight);
        }
    }

    /**
     * Stops every drag.
     */
    public void mouseReleased() {
        for (HudElement element : elements) {
            element.handleRelease();
        }
    }
}
