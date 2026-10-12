package it.echo.kira.event.impl;

import it.echo.kira.event.Event;

/**
 * Mouse button press in scaled coordinates.
 *
 * @param button button index
 * @param mouseX scaled x
 * @param mouseY scaled y
 */
public record MousePressEvent(int button, int mouseX, int mouseY) implements Event {
}
