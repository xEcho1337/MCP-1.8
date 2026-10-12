package it.echo.kira.event.impl;

import it.echo.kira.event.Event;

/**
 * Keyboard release.
 *
 * @param keyCode key code
 * @param keyChar typed char
 */
public record KeyReleaseEvent(int keyCode, char keyChar) implements Event {
}
