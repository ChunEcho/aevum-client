/*
 * Decompiled with CFR 0.152.
 */
package dev.aevum.mod.modules.impl.misc;

import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;

public class Swing
extends Module {
    public static Swing INSTANCE;
    public final EnumSetting<HandMode> hand = this.add(new EnumSetting<HandMode>("Hand", HandMode.Mainhand));
    public final EnumSetting<AnimMode> type = this.add(new EnumSetting<AnimMode>("Type", AnimMode.Vanilla));
    public final SliderSetting speed = this.add(new SliderSetting("Speed", 1.0, 0.5, 2.0, 0.1));

    public Swing() {
        super("Swing", "Changes your swinging hand.", Module.Category.Misc);
        this.setChinese("\u6325\u624b");
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        return this.hand.getValue().name();
    }

    public static enum HandMode {
        Mainhand,
        Offhand,
        Packet,
        Vanilla;

    }

    public static enum AnimMode {
        Vanilla,
        ONE_EIGHT,
        ONE_TWELVE;

    }
}

