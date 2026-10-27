/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.MobEffect
 *  net.minecraft.MobEffects
 *  net.minecraft.Player
 *  net.minecraft.ClientboundPlayerPositionPacket
 *  net.minecraft.Font
 *  net.minecraft.GuiGraphics
 *  net.minecraft.SoundEvents
 *  net.minecraft.SoundSource
 */
package dev.aevum.mod.modules.impl.misc;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.DeathEvent;
import dev.aevum.api.events.impl.EntitySpawnEvent;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.RemoveEntityEvent;
import dev.aevum.api.events.impl.TotemEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.core.impl.CommandManager;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.client.ClientSetting;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.MobEffect;
import net.minecraft.MobEffects;
import net.minecraft.Player;
import net.minecraft.ClientboundPlayerPositionPacket;
import net.minecraft.Font;
import net.minecraft.GuiGraphics;
import net.minecraft.SoundEvents;
import net.minecraft.SoundSource;

public class Tips
extends Module {
    public static Tips INSTANCE;
    public final BooleanSetting visualRange = this.add(new BooleanSetting("VisualRange", false).setParent());
    public final BooleanSetting friends = this.add(new BooleanSetting("Friends", false, this.visualRange::isOpen));
    public final BooleanSetting popCounter = this.add(new BooleanSetting("PopCounter", true));
    public final BooleanSetting deathCoords = this.add(new BooleanSetting("DeathCoords", true));
    public final BooleanSetting serverLag = this.add(new BooleanSetting("ServerLag", true));
    public final BooleanSetting lagBack = this.add(new BooleanSetting("LagBack", true));
    public final BooleanSetting potion = this.add(new BooleanSetting("Potion", true).setParent());
    public final BooleanSetting resistanceLevelCheck = this.add(new BooleanSetting("ResistanceLevelCheck", true, this.potion::isOpen));
    private final SliderSetting yOffset = this.add(new SliderSetting("YOffset", 0, -200, 200, this.potion::isOpen));
    final DecimalFormat df = new DecimalFormat("0.0");
    final int color = new Color(190, 0, 0).getRGB();
    private final Timer lagTimer = new Timer();
    private final Timer lagBackTimer = new Timer();
    int turtles = 0;

    public Tips() {
        super("Tips", Module.Category.Misc);
        this.setChinese("\u63d0\u793a");
        INSTANCE = this;
    }

    @EventListener
    public void onAddEntity(EntitySpawnEvent event) {
        if (!this.visualRange.getValue() || !(event.getEntity() instanceof Player) || event.getEntity().getDisplayName() == null) {
            return;
        }
        String playerName = event.getEntity().getDisplayName().getString();
        boolean isFriend = Aevum.FRIEND.isFriend(playerName);
        if (isFriend && !this.friends.getValue() || event.getEntity() == Tips.mc.player) {
            return;
        }
        CommandManager.sendMessageId((isFriend ? String.valueOf(ChatFormatting.AQUA) + playerName : String.valueOf(ChatFormatting.WHITE) + playerName) + "\u00a7f entered your visual range.", event.getEntity().method_5628() + 777);
        Tips.mc.level.method_8396((Player)Tips.mc.player, Tips.mc.player.method_24515(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 100.0f, 1.9f);
    }

    @EventListener
    public void onRemoveEntity(RemoveEntityEvent event) {
        if (!this.visualRange.getValue() || !(event.getEntity() instanceof Player) || event.getEntity().getDisplayName() == null) {
            return;
        }
        String playerName = event.getEntity().getDisplayName().getString();
        boolean isFriend = Aevum.FRIEND.isFriend(playerName);
        if (isFriend && !this.friends.getValue() || event.getEntity() == Tips.mc.player) {
            return;
        }
        CommandManager.sendMessageId((isFriend ? String.valueOf(ChatFormatting.AQUA) + playerName : String.valueOf(ChatFormatting.WHITE) + playerName) + "\u00a7f left your visual range.", event.getEntity().method_5628() + 777);
        Tips.mc.level.method_8396((Player)Tips.mc.player, Tips.mc.player.method_24515(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 100.0f, 1.9f);
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.potion.getValue()) {
            this.turtles = InventoryUtil.getPotionCount((MobEffect)MobEffects.DAMAGE_RESISTANCE.comp_349());
        }
    }

    @EventListener
    public void onPacketEvent(PacketEvent.Receive event) {
        this.lagTimer.reset();
        if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
            this.lagBackTimer.reset();
        }
    }

    @Override
    public void onRender2D(GuiGraphics drawContext, float tickDelta) {
        int n;
        Font textRenderer;
        String text;
        if (this.serverLag.getValue() && this.lagTimer.passedS(1.4)) {
            text = "Server not responding (" + this.df.format((double)this.lagTimer.getMs() / 1000.0) + "s)";
            textRenderer = Tips.mc.font;
            n = mc.getWindow().getGuiScaledWidth() / 2 - Tips.mc.font.method_1727(text) / 2;
            Objects.requireNonNull(Tips.mc.font);
            drawContext.method_51433(textRenderer, text, n, 19, this.color, true);
        }
        if (this.lagBack.getValue() && !this.lagBackTimer.passedS(1.5)) {
            text = "Lagback (" + this.df.format((double)(1500L - this.lagBackTimer.getMs()) / 1000.0) + "s)";
            textRenderer = Tips.mc.font;
            n = mc.getWindow().getGuiScaledWidth() / 2 - Tips.mc.font.method_1727(text) / 2;
            Objects.requireNonNull(Tips.mc.font);
            drawContext.method_51433(textRenderer, text, n, 28, this.color, true);
        }
        if (this.potion.getValue()) {
            StringBuilder stringBuilder = new StringBuilder();
            if (this.turtles > 0) {
                stringBuilder.append("\u00a7e").append(this.turtles);
            }
            if (Tips.mc.player.method_6059(MobEffects.DAMAGE_RESISTANCE) && (!this.resistanceLevelCheck.getValue() || Tips.mc.player.method_6112(MobEffects.DAMAGE_RESISTANCE).method_5578() > 0)) {
                if (!stringBuilder.isEmpty()) {
                    stringBuilder.append(" ");
                }
                stringBuilder.append("\u00a79").append(Tips.mc.player.method_6112(MobEffects.DAMAGE_RESISTANCE).method_5584() / 20 + 1);
            }
            if (Tips.mc.player.method_6059(MobEffects.DAMAGE_BOOST)) {
                if (!stringBuilder.isEmpty()) {
                    stringBuilder.append(" ");
                }
                stringBuilder.append("\u00a74").append(Tips.mc.player.method_6112(MobEffects.DAMAGE_BOOST).method_5584() / 20 + 1);
            }
            if (Tips.mc.player.method_6059(MobEffects.MOVEMENT_SPEED)) {
                if (!stringBuilder.isEmpty()) {
                    stringBuilder.append(" ");
                }
                stringBuilder.append("\u00a7b").append(Tips.mc.player.method_6112(MobEffects.MOVEMENT_SPEED).method_5584() / 20 + 1);
            }
            Font textRenderer2 = Tips.mc.font;
            String string = stringBuilder.toString();
            int n2 = mc.getWindow().getGuiScaledWidth() / 2 - Tips.mc.font.method_1727(stringBuilder.toString()) / 2;
            int n22 = mc.getWindow().getGuiScaledHeight() / 2;
            Objects.requireNonNull(Tips.mc.font);
            drawContext.method_51433(textRenderer2, string, n2, n22 + 9 - this.yOffset.getValueInt(), -1, true);
        }
    }

    @EventListener
    public void onPlayerDeath(DeathEvent event) {
        Player player = event.getPlayer();
        if (this.popCounter.getValue()) {
            if (Aevum.POP.popContainer.containsKey(player.method_5477().getString())) {
                int l_Count = Aevum.POP.popContainer.get(player.method_5477().getString());
                if (l_Count == 1) {
                    if (player.equals((Object)Tips.mc.player)) {
                        this.sendMessage("\u00a7fYou\u00a7r died after popping \u00a7f" + l_Count + "\u00a7r totem.", player.method_5628());
                    } else {
                        this.sendMessage("\u00a7f" + player.method_5477().getString() + "\u00a7r died after popping \u00a7f" + l_Count + "\u00a7r totem.", player.method_5628());
                    }
                } else if (player.equals((Object)Tips.mc.player)) {
                    this.sendMessage("\u00a7fYou\u00a7r died after popping \u00a7f" + l_Count + "\u00a7r totems.", player.method_5628());
                } else {
                    this.sendMessage("\u00a7f" + player.method_5477().getString() + "\u00a7r died after popping \u00a7f" + l_Count + "\u00a7r totems.", player.method_5628());
                }
            } else if (player.equals((Object)Tips.mc.player)) {
                this.sendMessage("\u00a7fYou\u00a7r died.", player.method_5628());
            } else {
                this.sendMessage("\u00a7f" + player.method_5477().getString() + "\u00a7r died.", player.method_5628());
            }
        }
        if (this.deathCoords.getValue() && player == Tips.mc.player) {
            this.sendMessage("\u00a74You died at " + player.getBlockX() + ", " + player.getBlockY() + ", " + player.getBlockZ());
        }
    }

    @EventListener
    public void onTotem(TotemEvent event) {
        if (this.popCounter.getValue()) {
            Player player = event.getPlayer();
            int l_Count = 1;
            if (Aevum.POP.popContainer.containsKey(player.method_5477().getString())) {
                l_Count = Aevum.POP.popContainer.get(player.method_5477().getString());
            }
            if (l_Count == 1) {
                if (player.equals((Object)Tips.mc.player)) {
                    this.sendMessage("\u00a7fYou\u00a7r popped \u00a7f" + l_Count + "\u00a7r totem.", player.method_5628());
                } else {
                    this.sendMessage("\u00a7f" + player.method_5477().getString() + " \u00a7rpopped \u00a7f" + l_Count + "\u00a7r totems.", player.method_5628());
                }
            } else if (player.equals((Object)Tips.mc.player)) {
                this.sendMessage("\u00a7fYou\u00a7r popped \u00a7f" + l_Count + "\u00a7r totem.", player.method_5628());
            } else {
                this.sendMessage("\u00a7f" + player.method_5477().getString() + " \u00a7rhas popped \u00a7f" + l_Count + "\u00a7r totems.", player.method_5628());
            }
        }
    }

    public void sendMessage(String message, int id) {
        if (!Tips.nullCheck()) {
            if (ClientSetting.INSTANCE.messageStyle.getValue() == ClientSetting.Style.Moon) {
                CommandManager.sendMessageId("\u00a7f[\u00a73" + this.getName() + "\u00a7f] " + message, id);
                return;
            }
            CommandManager.sendMessageId(message, id);
        }
    }
}

