/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.Player
 *  net.minecraft.Minecraft
 */
package dev.aevum.mod.modules.impl.misc;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.TotemEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import dev.aevum.mod.modules.settings.impl.StringSetting;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.Entity;
import net.minecraft.Player;
import net.minecraft.Minecraft;

public class PopEz
extends Module {
    public static PopEz INSTANCE;
    private final SliderSetting randomChars;
    public final BooleanSetting slowSend;
    private final StringSetting messageFormat = this.add(new StringSetting("Message", "%player% has popped %count% totems."));
    Random random;
    Timer timer;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private Map<Integer, Integer> popQueue;

    public PopEz() {
        super("PopEz", Module.Category.Misc);
        this.randomChars = this.add(new SliderSetting("RandomChars", 3.0, 0.0, 20.0, 1.0));
        this.slowSend = this.add(new BooleanSetting("SlowSend", false));
        this.random = new Random();
        this.timer = new Timer();
        this.popQueue = new HashMap<Integer, Integer>();
        this.setChinese("POP\u5632\u8bbd");
        INSTANCE = this;
    }

    @EventListener
    public void onTotem(TotemEvent totemEvent) {
        Player player = totemEvent.getPlayer();
        if (player != null && !player.equals((Object)this.getMinecraft().player) && !Aevum.FRIEND.isFriend(player)) {
            int intValue = 1;
            if (Aevum.POP.popContainer.containsKey(player.method_5477().getString())) {
                intValue = Aevum.POP.popContainer.get(player.method_5477().getString());
            }
            if (this.slowSend.getValue()) {
                this.popQueue.put(player.method_5628(), intValue);
                return;
            }
            this.sendMessage(this.buildMessage(player.method_5477().getString(), intValue), player.method_5628());
        }
    }

    @EventListener
    public void onUpdate(UpdateEvent updateEvent) {
        if (this.slowSend.getValue() && !this.popQueue.isEmpty() && this.timer.passedS(3.2)) {
            this.timer.reset();
            Map.Entry<Integer, Integer> entry = this.popQueue.entrySet().iterator().next();
            int intValue = entry.getKey();
            int intValue2 = entry.getValue();
            Player PlayerEntity = null;
            for (Entity Player$BedSleepingProblem : this.getMinecraft().level.method_18112()) {
                Player ChatVisibility;
                if (!(Player$BedSleepingProblem instanceof Player) || (ChatVisibility = (Player)Player$BedSleepingProblem).method_5628() != intValue) continue;
                PlayerEntity = ChatVisibility;
                break;
            }
            if (PlayerEntity != null) {
                this.sendMessage(this.buildMessage(PlayerEntity.method_5477().getString(), intValue2), intValue);
                this.popQueue.remove(intValue);
                return;
            }
            this.popQueue.remove(intValue);
        }
    }

    public void sendMessage(String s, int n) {
        Minecraft minecraft = this.getMinecraft();
        if (minecraft.player != null && minecraft.player.connection != null) {
            String generateRandomString = this.generateRandomString(this.randomChars.getValueInt());
            if (!generateRandomString.isEmpty()) {
                s = (String)s + " " + generateRandomString;
            }
            minecraft.player.connection.method_45729((String)s);
        }
    }

    private String generateRandomString(int capacity) {
        StringBuilder sb = new StringBuilder(capacity);
        for (int i = 0; i < capacity; ++i) {
            sb.append(CHARACTERS.charAt(this.random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    private Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    private String buildMessage(String replacement, int i) {
        return this.messageFormat.getValue().replace("%player%", replacement).replace("%count%", String.valueOf(i));
    }
}

