package it.echo.kira.module.impl;

import it.echo.kira.module.Module;
import it.echo.kira.module.ModuleCategory;

public class Animations extends Module {

    /** Enables the legacy 1.7 first-person item transforms. */
    public Animations() {
        this("Animations", ModuleCategory.RENDER, 0);
    }

    public Animations(String name, ModuleCategory category, int keyCode) {
        super(name, category, keyCode);
    }
}
