package it.echo.kira.event.impl;

import it.echo.kira.event.Event;

/**
 * Keyboard press.
 *
 * @param keyCode key code
 * @param keyChar typed char, may be {@code 0} for special keys
 * @param repeat true on held-key auto-repeat
 */
public record KeyPressEvent(int keyCode, char keyChar, boolean repeat) implements Event {
}
