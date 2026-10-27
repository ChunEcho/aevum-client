/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.Player
 *  net.minecraft.BowItem
 *  net.minecraft.Items
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.events.impl.UpdateRotateEvent;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BindSetting;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.InteractionHand;
import net.minecraft.Player;
import net.minecraft.BowItem;
import net.minecraft.Items;

public class Quiver
extends Module {
    private final BooleanSetting instant = this.add(new BooleanSetting("InstantRotate", true));
    private final SliderSetting time = this.add(new SliderSetting("Time", (double)0.11f, 0.0, 1.0, 0.01));
    private final BooleanSetting onlyPress = this.add(new BooleanSetting("OnlyPress", false));
    private final BindSetting key = this.add(new BindSetting("ActiveKey", -1));
    boolean bow = false;
    boolean pressed = false;
    boolean switching = false;
    int startSlot;

    public Quiver() {
        super("Quiver", Module.Category.Combat);
        this.setChinese("\u5934\u9876\u5c04\u7bad");
    }

    @Override
    public void onEnable() {
        this.bow = false;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        boolean bl;
        if (this.key.isPressed()) {
            int bow;
            if (!this.pressed && !this.switching && (bow = InventoryUtil.findItem(Items.BOW)) != -1) {
                this.startSlot = Quiver.mc.player.getInventory().selected;
                InventoryUtil.switchToSlot(bow);
                Quiver.mc.options.keyUse.method_23481(true);
                this.switching = true;
                this.pressed = true;
            }
        } else {
            this.pressed = false;
        }
        if (this.switching && (!Quiver.mc.options.keyUse.isDown() || Quiver.mc.player.isUsingItem() && Quiver.mc.player.getUseItem().method_7909() != Items.BOW)) {
            InventoryUtil.switchToSlot(this.startSlot);
            this.switching = false;
        }
        boolean bl2 = Quiver.mc.player.isUsingItem() && (Quiver.mc.player.getUsedItemHand() == InteractionHand.MAIN_HAND ? Quiver.mc.player.getMainHandItem() : Quiver.mc.player.getOffhandItem()).method_7909() instanceof BowItem ? true : (bl = (this.bow = false));
        if (this.bow && (!this.onlyPress.getValue() || this.switching) && (double)BowItem.method_7722((int)Quiver.mc.player.getTicksUsingItem()) >= this.time.getValue()) {
            if (this.instant.getValue()) {
                Aevum.ROTATION.snapAt(Aevum.ROTATION.rotationYaw, -90.0f);
            }
            Quiver.mc.options.keyUse.method_23481(false);
            Quiver.mc.gameMode.method_2897((Player)Quiver.mc.player);
            if (this.instant.getValue()) {
                Aevum.ROTATION.snapBack();
            }
        }
    }

    @EventListener
    public void onRotate(UpdateRotateEvent event) {
        if (this.bow && !this.instant.getValue()) {
            event.setPitch(-90.0f);
        }
    }
}

