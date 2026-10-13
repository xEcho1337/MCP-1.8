package it.echo.kira.event.impl;

import it.echo.kira.event.Event;

/**
 * Fired after entities render, world-space overlays draw here.
 *
 * @param partialTicks render partial ticks
 */
public record RenderWorldEvent(float partialTicks) implements Event {
}
