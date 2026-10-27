/*
 * Decompiled with CFR 0.152.
 */
package dev.aevum.mod.modules.impl.misc;

import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.SliderSetting;

public class InventoryStyle
extends Module {
    public static InventoryStyle INSTANCE;
    private final SliderSetting opacity = this.add(new SliderSetting("Opacity", 50.0, 10.0, 100.0, 5.0).setSuffix("%"));

    public InventoryStyle() {
        super("InventoryStyle", "Adjustable container background opacity.", Module.Category.Misc);
        this.setChinese("\u80cc\u5305\u900f\u660e");
        INSTANCE = this;
    }

    public static float getOpacityF() {
        InventoryStyle instance = INSTANCE;
        if (instance == null || !instance.isOn()) {
            return 1.0f;
        }
        return (float)(instance.opacity.getValue() / 100.0);
    }
}

