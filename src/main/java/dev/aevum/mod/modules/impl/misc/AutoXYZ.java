/*
 * Decompiled with CFR 0.152.
 */
package dev.aevum.mod.modules.impl.misc;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.client.IRC;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import dev.aevum.mod.modules.settings.impl.StringSetting;

public class AutoXYZ
extends Module {
    public static AutoXYZ INSTANCE;
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 20.0, 0.0, 600.0, 1.0).setSuffix("s"));
    private final StringSetting message = this.add(new StringSetting("Message", "I am in %x% %y% %z%"));
    private final BooleanSetting onEnable = this.add(new BooleanSetting("SendOnEnable", true));
    private final BooleanSetting ircMark = this.add(new BooleanSetting("IrcMark", false));
    private final Timer timer = new Timer();

    public AutoXYZ() {
        super("AutoXYZ", Module.Category.Misc);
        this.setChinese("\u81ea\u52a8\u62a5\u5750\u6807");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        this.timer.reset();
        if (this.onEnable.getValue()) {
            this.send();
        }
    }

    @EventListener
    public void onUpdate(UpdateEvent updateEvent) {
        if (Module.nullCheck() || this.delay.getValue() <= 0.0) {
            return;
        }
        if (!this.timer.passed((long)(this.delay.getValue() * 1000.0))) {
            return;
        }
        this.timer.reset();
        this.send();
    }

    private void send() {
        if (AutoXYZ.nullCheck()) {
            return;
        }
        int i = (int)Math.floor(AutoXYZ.mc.player.getX());
        int j = (int)Math.floor(AutoXYZ.mc.player.getY());
        int k = (int)Math.floor(AutoXYZ.mc.player.getZ());
        mc.getConnection().method_45729(this.message.getValue().replace("%x%", Integer.toString(i)).replace("%y%", Integer.toString(j)).replace("%z%", Integer.toString(k)));
        if (this.ircMark.getValue() && IRC.INSTANCE != null && IRC.INSTANCE.isConnected()) {
            IRC.sendMark(i, j, k, "xyz");
        }
    }
}

