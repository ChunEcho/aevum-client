/*
 * Decompiled with CFR 0.152.
 */
package dev.aevum.mod.modules.impl.render;

import dev.aevum.mod.modules.Module;
import dev.aevum.optimizations.FrameInterpolation;

public class FrameInterp
extends Module {
    public static FrameInterp INSTANCE;

    public FrameInterp() {
        super("FrameInterp", Module.Category.Render);
        this.setChinese("\u63d2\u5e27");
        INSTANCE = this;
    }

    @Override
    public void onToggle() {
        if (!this.isOn()) {
            FrameInterpolation.cleanup();
        }
    }
}

