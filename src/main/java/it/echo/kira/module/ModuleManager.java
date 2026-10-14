package it.echo.kira.module;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores modules by their concrete class.
 */
public final class ModuleManager {

    private final Map<Class<? extends Module>, Module> modules = new LinkedHashMap<>();

    /**
     * Registers a module instance.
     *
     * @param module module to store
     */
    public void register(Module module) {
        modules.put(module.getClass(), module);
    }

    /**
     * Finds a module by its concrete class.
     *
     * @param type module class
     * @return module or null
     */
    public <T extends Module> T get(Class<T> type) {
        return type.cast(modules.get(type));
    }

    /**
     * Returns every registered module in registration order.
     *
     * @return module list copy
     */
    public List<Module> all() {
        return new ArrayList<>(modules.values());
    }

    /**
     * Returns enabled modules only.
     *
     * @return enabled list
     */
    public List<Module> enabled() {
        List<Module> result = new ArrayList<>();
        for (Module module : modules.values()) {
            if (module.enabled()) {
                result.add(module);
            }
        }
        return result;
    }

    /**
     * Toggles modules bound to a key code.
     *
     * @param keyCode LWJGL key code
     */
    public void toggleBound(int keyCode) {
        if (keyCode == 0) {
            return;
        }
        for (Module module : modules.values()) {
            if (module.keyCode() == keyCode) {
                module.toggle();
            }
        }
    }

    /**
     * Returns registration count.
     *
     * @return size
     */
    public int size() {
        return modules.size();
    }
}
