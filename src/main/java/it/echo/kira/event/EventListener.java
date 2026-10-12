package it.echo.kira.event;

/**
 * Typed event callback.
 *
 * @param <E> event type
 */
@FunctionalInterface
public interface EventListener<E extends Event> {

    void onEvent(E event);
}
