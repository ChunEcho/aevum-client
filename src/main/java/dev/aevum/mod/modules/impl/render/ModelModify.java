/*
 * Decompiled with CFR 0.152.
 */
package dev.aevum.mod.modules.impl.render;

import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.EnumSetting;

public class ModelModify
extends Module {
    public static ModelModify INSTANCE;
    public final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.Sneak));

    public ModelModify() {
        super("ModelModify", "Modify player model pose", Module.Category.Render);
        this.setChinese("\u6a21\u578b\u4fee\u6539");
        INSTANCE = this;
    }

    public boolean isSneak() {
        return this.isOn() && this.mode.is(Mode.Sneak);
    }

    public boolean isSit() {
        return this.isOn() && this.mode.is(Mode.Sit);
    }

    public boolean isSwim() {
        return this.isOn() && this.mode.is(Mode.Swim);
    }

    public boolean isProne() {
        return this.isOn() && this.mode.is(Mode.Prone);
    }

    public static enum Mode {
        Sneak,
        Sit,
        Swim,
        Prone;

    }
}

