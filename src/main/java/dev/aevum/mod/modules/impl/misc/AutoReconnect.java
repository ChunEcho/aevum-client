/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.Pair
 *  it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair
 *  net.minecraft.InteractionHand
 *  net.minecraft.Player
 *  net.minecraft.ChestMenu
 *  net.minecraft.ClickType
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundUseItemPacket
 *  net.minecraft.ContainerScreen
 *  net.minecraft.ServerAddress
 *  net.minecraft.ServerData
 *  net.minecraft.ClientboundSystemChatPacket
 */
package dev.aevum.mod.modules.impl.misc;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.ServerConnectBeginEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.misc.AutoLog;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import dev.aevum.mod.modules.settings.impl.StringSetting;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import java.util.HashMap;
import net.minecraft.InteractionHand;
import net.minecraft.Player;
import net.minecraft.ChestMenu;
import net.minecraft.ClickType;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.Packet;
import net.minecraft.ServerboundUseItemPacket;
import net.minecraft.ContainerScreen;
import net.minecraft.ServerAddress;
import net.minecraft.ServerData;
import net.minecraft.ClientboundSystemChatPacket;

public class AutoReconnect
extends Module {
    public static AutoReconnect INSTANCE;
    public final BooleanSetting rejoin = this.add(new BooleanSetting("Rejoin", true));
    public final SliderSetting delay = this.add(new SliderSetting("Delay", 5.0, 0.0, 20.0, 0.1).setSuffix("s"));
    public final BooleanSetting autoLogin = this.add(new BooleanSetting("AutoAuth", true));
    public final SliderSetting afterLoginTime = this.add(new SliderSetting("AfterLoginTime", 3.0, 0.0, 10.0, 0.1).setSuffix("s"));
    public final BooleanSetting autoQueue = this.add(new BooleanSetting("AutoQueue", true));
    public final SliderSetting joinQueueDelay = this.add(new SliderSetting("JoinQueueDelay", 3.0, 0.0, 10.0, 0.1).setSuffix("s"));
    final StringSetting password = this.add(new StringSetting("password", "123456"));
    public final BooleanSetting autoAnswer = this.add(new BooleanSetting("AutoAnswer", true));
    public static boolean inQueueServer;
    private boolean queueingByMessage;
    private final Timer queueTimer = new Timer();
    private final Timer queueMenuTimer = new Timer();
    private final Timer timer = new Timer();
    public Pair<ServerAddress, ServerData> lastServerConnection;
    private boolean login = false;
    final String[] abc = new String[]{"A", "B", "C"};
    public static final HashMap<String, String> asks;

    public AutoReconnect() {
        super("AutoReconnect", Module.Category.Misc);
        this.setChinese("\u81ea\u52a8\u91cd\u8fde");
        INSTANCE = this;
        Aevum.EVENT_BUS.subscribe(new StaticListener());
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        ChestMenu container;
        if (this.login && this.timer.passedS(this.afterLoginTime.getValue())) {
            mc.getConnection().method_45730("login " + this.password.getValue());
            this.login = false;
        }
        if (this.autoQueue.getValue() && !this.queueingByMessage && InventoryUtil.findItem(Items.COMPASS) != -1 && this.queueTimer.passedS(this.joinQueueDelay.getValue())) {
            InventoryUtil.switchToSlot(InventoryUtil.findItem(Items.COMPASS));
            AutoReconnect.sendSequencedPacket(id -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, id, Aevum.ROTATION.getLastYaw(), Aevum.ROTATION.getLastPitch()));
            this.queueTimer.reset();
        }
        if (AutoReconnect.nullCheck()) {
            inQueueServer = false;
            this.queueingByMessage = false;
            return;
        }
        if (this.autoQueue.getValue() && !this.queueingByMessage && AutoReconnect.mc.screen instanceof ContainerScreen && this.queueMenuTimer.passedS(this.joinQueueDelay.getValue()) && (container = (ChestMenu)((ContainerScreen)AutoReconnect.mc.screen).method_17577()) != null && AutoReconnect.mc.player.containerMenu.getCarried().method_7960()) {
            ItemStack stack;
            int targetSlot;
            int size = container.getContainer().method_5439();
            int n = size == 9 ? 4 : (targetSlot = size == 5 ? 2 : -1);
            if (targetSlot != -1 && (stack = container.getContainer().method_5438(targetSlot)) != null && stack.method_7909() == Items.COMPASS) {
                AutoReconnect.mc.gameMode.method_2906(container.containerId, targetSlot, 0, ClickType.PICKUP, (Player)AutoReconnect.mc.player);
                this.queueMenuTimer.reset();
            }
        }
        inQueueServer = InventoryUtil.findItem(Items.COMPASS) != -1;
    }

    @Override
    public void onLogin() {
        if (this.autoLogin.getValue()) {
            this.login = true;
            this.timer.reset();
        }
    }

    public boolean rejoin() {
        return this.isOn() && this.rejoin.getValue() && !AutoLog.loggedOut;
    }

    @Override
    public void onLogout() {
        inQueueServer = false;
        this.queueingByMessage = false;
    }

    @Override
    public void onDisable() {
        inQueueServer = false;
        this.queueingByMessage = false;
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive e) {
        ClientboundSystemChatPacket packet2;
        if (AutoReconnect.nullCheck()) {
            return;
        }
        Packet<?> packet = e.getPacket();
        String content = null;
        if (packet instanceof ClientboundSystemChatPacket && (content = (packet2 = (ClientboundSystemChatPacket)packet).comp_763().getString()).toLowerCase().contains("position in queue")) {
            this.queueingByMessage = true;
        }
        if (!this.autoAnswer.getValue()) {
            return;
        }
        if (!inQueueServer) {
            return;
        }
        if (content != null) {
            for (String key : asks.keySet()) {
                if (!content.contains(key)) continue;
                for (String s : this.abc) {
                    if (!content.contains(s + "." + asks.get(key))) continue;
                    mc.getConnection().method_45729(s.toLowerCase());
                    return;
                }
            }
        }
    }

    static {
        asks = new HashMap<String, String>(){
            {
                this.put("\u7ea2\u77f3\u706b\u628a", "15");
                this.put("\u732a\u88ab\u95ea\u7535", "\u50f5\u5c38\u732a\u4eba");
                this.put("\u5c0f\u7bb1\u5b50\u80fd", "27");
                this.put("\u5f00\u670d\u5e74\u4efd", "2020");
                this.put("\u5b9a\u4f4d\u672b\u5730\u9057\u8ff9", "0");
                this.put("\u722c\u884c\u8005\u88ab\u95ea\u7535", "\u9ad8\u538b\u722c\u884c\u8005");
                this.put("\u5927\u7bb1\u5b50\u80fd", "54");
                this.put("\u7f8a\u9a7c\u4f1a\u4e3b\u52a8", "\u4e0d\u4f1a");
                this.put("\u65e0\u9650\u6c34", "3");
                this.put("\u6316\u6398\u901f\u5ea6\u6700\u5feb", "\u91d1\u9550");
                this.put("\u51cb\u7075\u6b7b\u540e", "\u4e0b\u754c\u4e4b\u661f");
                this.put("\u82e6\u529b\u6015\u7684\u5b98\u65b9", "\u722c\u884c\u8005");
                this.put("\u5357\u74dc\u7684\u751f\u957f", "\u4e0d\u9700\u8981");
                this.put("\u5b9a\u4f4d\u672b\u5730", "0");
            }
        };
    }

    private class StaticListener {
        private StaticListener() {
        }

        @EventListener
        private void onGameJoined(ServerConnectBeginEvent event) {
            AutoReconnect.this.lastServerConnection = new ObjectObjectImmutablePair((Object)event.address, (Object)event.info);
        }
    }
}

