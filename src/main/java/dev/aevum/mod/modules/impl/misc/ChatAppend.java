/*
 * Decompiled with CFR 0.152.
 */
package dev.aevum.mod.modules.impl.misc;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.SendMessageEvent;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.misc.AutoReconnect;
import dev.aevum.mod.modules.settings.impl.StringSetting;

public class ChatAppend
extends Module {
    public static ChatAppend INSTANCE;
    private final StringSetting message = this.add(new StringSetting("Text", "Aevum"));

    public ChatAppend() {
        super("ChatAppend", Module.Category.Misc);
        this.setChinese("\u6d88\u606f\u540e\u7f00");
        INSTANCE = this;
    }

    @EventListener
    public void onSendMessage(SendMessageEvent event) {
        if (ChatAppend.nullCheck() || event.isCancelled() || AutoReconnect.inQueueServer) {
            return;
        }
        String message = event.message;
        if (message.startsWith("/") || message.startsWith("!") || message.startsWith("$") || message.startsWith("#") || message.endsWith(this.message.getValue())) {
            return;
        }
        String suffix = this.message.getValue();
        event.message = message + " " + suffix;
    }
}

