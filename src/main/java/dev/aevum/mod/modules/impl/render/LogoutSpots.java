/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  net.minecraft.Player
 *  net.minecraft.AABB
 *  net.minecraft.Vec3
 *  net.minecraft.Packet
 *  net.minecraft.ClientboundPlayerInfoUpdatePacket
 *  net.minecraft.ClientboundPlayerInfoUpdatePacket$Entry
 *  net.minecraft.ClientboundPlayerInfoUpdatePacket$Action
 *  net.minecraft.PoseStack
 *  net.minecraft.AbstractClientPlayer
 *  net.minecraft.ClientboundPlayerInfoRemovePacket
 */
package dev.aevum.mod.modules.impl.render;

import com.google.common.collect.Maps;
import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.render.ModelPlayer;
import dev.aevum.api.utils.render.Render3DUtil;
import dev.aevum.asm.accessors.IEntity;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.Player;
import net.minecraft.AABB;
import net.minecraft.Vec3;
import net.minecraft.Packet;
import net.minecraft.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.PoseStack;
import net.minecraft.AbstractClientPlayer;
import net.minecraft.ClientboundPlayerInfoRemovePacket;

public class LogoutSpots
extends Module {
    public static LogoutSpots INSTANCE;
    private final ColorSetting box = this.add(new ColorSetting("Box", new Color(255, 255, 255, 100)).injectBoolean(true));
    private final ColorSetting fill = this.add(new ColorSetting("Fill", new Color(255, 255, 255, 100)).injectBoolean(true));
    private final ColorSetting text = this.add(new ColorSetting("Text", new Color(255, 255, 255, 255)).injectBoolean(true));
    private final ColorSetting chamsFill = this.add(new ColorSetting("ChamsFill", new Color(255, 255, 255, 100)).injectBoolean(true));
    private final ColorSetting chamsLine = this.add(new ColorSetting("ChamsLine", new Color(255, 255, 255, 100)).injectBoolean(true));
    final Map<UUID, Player> playerCache = Maps.newConcurrentMap();
    final Map<UUID, ModelPlayer> logoutCache = Maps.newConcurrentMap();
    private final BooleanSetting health = this.add(new BooleanSetting("Health", true));
    private final BooleanSetting totem = this.add(new BooleanSetting("Totem", true));
    private final BooleanSetting message = this.add(new BooleanSetting("Message", true));

    public LogoutSpots() {
        super("LogoutSpots", Module.Category.Render);
        INSTANCE = this;
        this.setChinese("\u9000\u51fa\u8bb0\u5f55");
    }

    public Collection<ModelPlayer> getLogoutSpots() {
        return this.logoutCache.values();
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        block7: {
            Packet<?> object;
            block8: {
                if (LogoutSpots.nullCheck()) break block7;
                object = event.getPacket();
                if (!(object instanceof ClientboundPlayerInfoUpdatePacket)) break block8;
                ClientboundPlayerInfoUpdatePacket packet = (ClientboundPlayerInfoUpdatePacket)object;
                if (!packet.actions().contains(ClientboundPlayerInfoUpdatePacket.ClientboundPlayerInfoUpdatePacket$Action.ADD_PLAYER)) break block7;
                for (ClientboundPlayerInfoUpdatePacket.ClientboundPlayerInfoUpdatePacket$Entry addedPlayer : packet.newEntries()) {
                    for (UUID uuid : this.logoutCache.keySet()) {
                        if (!uuid.equals(addedPlayer.comp_1107().getId())) continue;
                        Player player = this.logoutCache.get((Object)uuid).player;
                        if (this.message.getValue()) {
                            mc.execute(() -> this.sendMessage("\u00a7f" + player.method_5477().getString() + " \u00a7rLogged back at \u00a7f" + player.getBlockX() + ", " + player.getBlockY() + ", " + player.getBlockZ()));
                        }
                        this.logoutCache.remove(uuid);
                    }
                }
                break block7;
            }
            object = event.getPacket();
            if (object instanceof ClientboundPlayerInfoRemovePacket) {
                ClientboundPlayerInfoRemovePacket class_78282 = (ClientboundPlayerInfoRemovePacket)object;
                List profileIds = class_78282.comp_1105();
                for (UUID uuid2 : profileIds) {
                    for (UUID uuidx : this.playerCache.keySet()) {
                        if (!uuidx.equals(uuid2)) continue;
                        Player player = this.playerCache.get(uuidx);
                        if (this.logoutCache.containsKey(uuidx) || player == null) continue;
                        ModelPlayer modelPlayer = new ModelPlayer(player);
                        if (this.message.getValue()) {
                            mc.execute(() -> this.sendMessage("\u00a7f" + player.method_5477().getString() + " \u00a7rLogged out at \u00a7f" + player.getBlockX() + ", " + player.getBlockY() + ", " + player.getBlockZ()));
                        }
                        this.logoutCache.put(uuidx, modelPlayer);
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        this.playerCache.clear();
        this.logoutCache.clear();
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        this.playerCache.clear();
        for (AbstractClientPlayer player : Aevum.THREAD.getPlayers()) {
            if (player == null || player.equals((Object)LogoutSpots.mc.player)) continue;
            this.playerCache.put(player.getGameProfile().getId(), (Player)player);
        }
    }

    @Override
    public void onRender3D(PoseStack matrixStack) {
        for (ModelPlayer data : this.logoutCache.values()) {
            Player player = data.player;
            AABB box = ((IEntity)player).getDimensions().method_30757(player.position());
            if (this.box.booleanValue) {
                Render3DUtil.drawBox(matrixStack, box, this.box.getValue());
            }
            if (this.fill.booleanValue) {
                Render3DUtil.drawFill(matrixStack, box, this.fill.getValue());
            }
            if (this.chamsFill.booleanValue || this.chamsLine.booleanValue) {
                data.render(matrixStack, this.chamsFill, this.chamsLine);
            }
            if (!this.text.booleanValue) continue;
            Render3DUtil.drawText3D(player.method_5477().getString() + (this.health.getValue() ? String.valueOf(LogoutSpots.round2(player.getHealth() + player.getAbsorptionAmount())) : "") + (this.totem.getValue() && Aevum.POP.getPop(player) > 0 ? String.valueOf(Aevum.POP.getPop(player)) : ""), new Vec3(player.getX(), ((IEntity)player).getDimensions().method_30757((Vec3)player.position()).maxY + 0.5, player.getZ()), this.text.getValue());
        }
    }

    public static float round2(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        return bd.floatValue();
    }
}

