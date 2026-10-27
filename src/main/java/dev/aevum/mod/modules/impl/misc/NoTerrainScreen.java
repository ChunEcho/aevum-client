/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ReceivingLevelScreen
 *  net.minecraft.ProgressScreen
 */
package dev.aevum.mod.modules.impl.misc;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.ClientTickEvent;
import dev.aevum.mod.modules.Module;
import net.minecraft.ReceivingLevelScreen;
import net.minecraft.ProgressScreen;

public class NoTerrainScreen
extends Module {
    public NoTerrainScreen() {
        super("NoTerrainScreen", Module.Category.Misc);
        this.setChinese("\u6ca1\u6709\u52a0\u8f7d\u754c\u9762");
    }

    @EventListener
    public void onEvent(ClientTickEvent event) {
        if (NoTerrainScreen.nullCheck()) {
            return;
        }
        if (NoTerrainScreen.mc.screen instanceof ReceivingLevelScreen || NoTerrainScreen.mc.screen instanceof ProgressScreen) {
            NoTerrainScreen.mc.screen = null;
        }
    }
}

