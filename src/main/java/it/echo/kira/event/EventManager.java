package it.echo.kira.event;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Minimal high-performance event bus.
 *
 * <p>Listeners are registered manually per event class, no reflection, no
 * hierarchy scan on fire: dispatch is an exact-class map lookup plus an
 * indexed loop over a {@link CopyOnWriteArrayList} snapshot.</p>
 */
public final class EventManager {

    private final ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<EventListener<?>>> listeners =
            new ConcurrentHashMap<>();

    /**
     * Registers a callback for one event type.
     *
     * @param type event class, must not be null
     * @param listener callback, must not be null
     * @param <E> event type
     */
    public <E extends Event> void listen(Class<E> type, EventListener<E> listener) {
        listeners.computeIfAbsent(type, key -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /**
     * Removes a previously registered callback.
     *
     * @param type event class
     * @param listener callback to remove
     * @param <E> event type
     */
    public <E extends Event> void unlisten(Class<E> type, EventListener<E> listener) {
        List<EventListener<?>> list = listeners.get(type);
        if (list != null) {
            list.remove(listener);
        }
    }

    /**
     * Fires an event to its exact-class listeners only.
     *
     * @param event event instance
     * @param <E> event type
     */
    @SuppressWarnings("unchecked")
    public <E extends Event> void fire(E event) {
        List<EventListener<?>> list = listeners.get(event.getClass());

        if (list == null || list.isEmpty()) {
            return;
        }

        for (EventListener<?> eventListener : list) {
            ((EventListener<E>) eventListener).onEvent(event);
        }
    }

    /**
     * Returns how many callbacks are registered for a type.
     *
     * @param type event class
     * @return listener count
     */
    public int count(Class<? extends Event> type) {
        List<EventListener<?>> list = listeners.get(type);
        return list == null ? 0 : list.size();
    }

    /**
     * Removes every listener, mainly for tests.
     */
    public void clear() {
        listeners.clear();
    }
}
