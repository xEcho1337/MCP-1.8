package it.echo.kira;

import it.echo.kira.account.AccountManager;
import it.echo.kira.event.Event;
import it.echo.kira.event.EventManager;
import it.echo.kira.hud.HudManager;
import it.echo.kira.hud.impl.WatermarkElement;
import it.echo.kira.module.ModuleManager;
import it.echo.kira.module.impl.DemoGuiModule;
import it.echo.kira.module.impl.HudEditModule;
import net.minecraft.client.Minecraft;

/**
 * Client entry point, holds shared singletons.
 */
public final class Kira {

    private static final EventManager EVENTS = new EventManager();
    private static final ModuleManager MODULES = new ModuleManager();
    private static final HudManager HUD = new HudManager();
    private static AccountManager ACCOUNTS;
    private static boolean initialized;

    private Kira() {
    }

    /**
     * Bootstraps modules and HUD once, safe to call repeatedly.
     */
    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        MODULES.register(new DemoGuiModule());
        MODULES.register(new HudEditModule());
        HUD.add(new WatermarkElement(6.0D, 6.0D));
        ACCOUNTS = new AccountManager(Minecraft.getMinecraft().mcDataDir);
        ACCOUNTS.load();
        ACCOUNTS.autoLogin();
        for (var module : MODULES.all()) {
            if (module.enabled()) {
                module.enabled(true);
            }
        }
    }

    /**
     * Fires an event on the shared bus.
     *
     * @param event event instance
     * @param <E> event type
     */
    public static <E extends Event> void fire(E event) {
        EVENTS.fire(event);
    }

    public static EventManager events() {
        return EVENTS;
    }

    public static ModuleManager modules() {
        return MODULES;
    }

    public static AccountManager accounts() {
        return ACCOUNTS;
    }

    public static HudManager hud() {
        return HUD;
    }
}
