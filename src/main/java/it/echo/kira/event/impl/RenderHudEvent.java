package it.echo.kira.event.impl;

import it.echo.kira.event.Event;

/**
 * Fired at the end of {@code GuiIngame.renderGameOverlay}, HUD should draw here.
 *
 * @param mouseX current scaled mouse x, used for drag handling
 * @param mouseY current scaled mouse y
 * @param partialTicks render partial ticks
 */
public record RenderHudEvent(int mouseX, int mouseY, float partialTicks) implements Event {
}
