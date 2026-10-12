package it.echo.kira.module;

import it.echo.kira.Kira;
import it.echo.kira.event.Event;
import it.echo.kira.event.EventListener;

/**
 * Toggleable module with typed event callbacks.
 */
public abstract class Module {

    private final String name;
    private final ModuleCategory category;
    private int keyCode;
    private boolean enabled;

    protected Module(String name, ModuleCategory category, int keyCode) {
        this.name = name;
        this.category = category;
        this.keyCode = keyCode;
    }

    protected Module(String name, ModuleCategory category) {
        this(name, category, 0);
    }

    /**
     * Registers a typed callback on the shared event bus.
     *
     * @param type event class
     * @param listener callback
     * @param <E> event type
     */
    protected final <E extends Event> void listen(Class<E> type, EventListener<E> listener) {
        Kira.events().listen(type, listener);
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public final void toggle() {
        enabled(!enabled);
    }

    public String name() {
        return name;
    }

    public String getName() {
        return name;
    }

    public ModuleCategory category() {
        return category;
    }

    public ModuleCategory getCategory() {
        return category;
    }

    public int keyCode() {
        return keyCode;
    }

    public void keyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        keyCode(keyCode);
    }

    public boolean enabled() {
        return enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void setEnabled(boolean enabled) {
        enabled(enabled);
    }
}
