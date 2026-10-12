package it.echo.kira.event.impl;

import it.echo.kira.event.Event;

/**
 * Fired once per client tick. Singleton instance reused to avoid allocation.
 */
public record ClientTickEvent() implements Event {

    public static final ClientTickEvent INSTANCE = new ClientTickEvent();
}
