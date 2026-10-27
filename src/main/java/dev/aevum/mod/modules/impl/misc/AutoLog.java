/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Component
 *  net.minecraft.Packet
 *  net.minecraft.ClientboundDisconnectPacket
 *  net.minecraft.ServerboundSetCarriedItemPacket
 */
package dev.aevum.mod.modules.impl.misc;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.TotemEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.core.impl.CommandManager;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.Component;
import net.minecraft.Packet;
import net.minecraft.ClientboundDisconnectPacket;
import net.minecraft.ServerboundSetCarriedItemPacket;

public class AutoLog
extends Module {
    public static boolean loggedOut = false;
    private final BooleanSetting logOnEnable = this.add(new BooleanSetting("LogOnEnable", false));
    private final BooleanSetting onPop = this.add(new BooleanSetting("OnPop", true));
    private final BooleanSetting lowArmor = this.add(new BooleanSetting("LowArmor", true));
    private final BooleanSetting totemLess = this.add(new BooleanSetting("TotemLess", true).setParent());
    private final SliderSetting totems = this.add(new SliderSetting("Totems", 2.0, 0.0, 20.0, 1.0, this.totemLess::isOpen));
    private final BooleanSetting autoDisable = this.add(new BooleanSetting("AutoDisable", true));
    private final BooleanSetting showReason = this.add(new BooleanSetting("ShowReason", false));

    public AutoLog() {
        super("AutoLog", Module.Category.Misc);
        this.setChinese("\u81ea\u52a8\u4e0b\u7ebf");
    }

    @Override
    public void onEnable() {
        if (this.logOnEnable.getValue()) {
            this.disconnect("Enabled");
        }
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.totemLess.getValue()) {
            int n;
            int totem = InventoryUtil.getItemCount(Items.TOTEM_OF_UNDYING);
            if ((double)n <= this.totems.getValue()) {
                this.disconnect("You have too few totems (" + totem + ").");
                return;
            }
        }
        if (this.lowArmor.getValue()) {
            for (ItemStack armor : AutoLog.mc.player.getInventory().armor) {
                int damage;
                if (armor.method_7960() || (damage = EntityUtil.getDamagePercent(armor)) >= 5) continue;
                this.disconnect("Your armor has keyCodec durability of less than 5%.");
                return;
            }
        }
    }

    @EventListener
    public void onPop(TotemEvent event) {
        if (this.onPop.getValue() && event.getPlayer() == AutoLog.mc.player) {
            this.disconnect("You poped 1 totem!");
        }
    }

    @Override
    public void onLogout() {
        if (this.autoDisable.getValue()) {
            this.disable();
        }
    }

    private void disconnect(String reason) {
        loggedOut = true;
        CommandManager.sendMessage("\u00a74[AutoLog] " + reason);
        mc.getConnection().method_52787((Packet)new ServerboundSetCarriedItemPacket(114514));
        if (this.showReason.getValue()) {
            AutoLog.mc.player.connection.method_52781(new ClientboundDisconnectPacket((Component)Component.method_43470((String)("[AutoLog]" + reason))));
        }
    }
}

