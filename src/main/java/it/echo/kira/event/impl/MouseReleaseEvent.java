package it.echo.kira.event.impl;

import it.echo.kira.event.Event;

/**
 * Mouse button release in scaled GUI coordinates.
 *
 * @param button button index
 * @param mouseX scaled x
 * @param mouseY scaled y
 */
public record MouseReleaseEvent(int button, int mouseX, int mouseY) implements Event {
}
