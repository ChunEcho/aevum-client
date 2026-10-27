/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Screen
 */
package dev.aevum.mod.modules.impl.client;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.TickEvent;
import dev.aevum.mod.gui.ClickGuiScreen;
import dev.aevum.mod.modules.Module;
import net.minecraft.Screen;

public class HudEditor
extends Module {
    public static HudEditor INSTANCE;

    public HudEditor() {
        super("HudEditor", "HudEditor", Module.Category.Client);
        this.setChinese("\u754c\u9762\u7f16\u8f91\u5668");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (HudEditor.mc.player != null) {
            mc.execute(() -> mc.method_1507((Screen)ClickGuiScreen.getInstance()));
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (HudEditor.mc.screen instanceof ClickGuiScreen) {
            mc.execute(() -> mc.method_1507(null));
        }
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (!(HudEditor.mc.screen instanceof ClickGuiScreen)) {
            this.disable();
        }
    }
}

