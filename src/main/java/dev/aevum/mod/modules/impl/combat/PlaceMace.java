/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.Items
 *  net.minecraft.ServerboundUseItemPacket
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.ClientTickEvent;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.InteractionHand;
import net.minecraft.Items;
import net.minecraft.ServerboundUseItemPacket;

public class PlaceMace
extends Module {
    public static PlaceMace INSTANCE;
    private final Timer timer1 = new Timer();
    private final SliderSetting Qztj = this.add(new SliderSetting("UpwardVelocity", 1.5, 0.1, 4.0, 0.1));
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 1.0, 0.1, 5.0, 0.1));

    public PlaceMace() {
        super("PlaceMace", Module.Category.Combat);
        this.setChinese("\u539f\u5730\u91cd\u9524");
        INSTANCE = this;
    }

    @EventListener
    public void onTick(ClientTickEvent clientTickEvent) {
        if (PlaceMace.nullCheck()) {
            return;
        }
        if (!this.getTimer12().passedS(this.getDelay().getValue())) {
            return;
        }
        int item = InventoryUtil.findItem(Items.WIND_CHARGE);
        if (item == -1) {
            return;
        }
        int selectedSlot = PlaceMace.mc.player.getInventory().selected;
        InventoryUtil.switchToSlot(item);
        PlaceMace.sendSequencedPacket(n -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, n, PlaceMace.mc.player.getYRot(), PlaceMace.mc.player.getXRot()));
        PlaceMace.mc.player.method_5762(0.0, this.QztjAccess().getValue(), 0.0);
        InventoryUtil.switchToSlot(selectedSlot);
        this.getTimer12().reset();
    }

    @Override
    public String getInfo() {
        if (PlaceMace.mc.player == null || PlaceMace.mc.level == null) {
            return "";
        }
        boolean b = false;
        boolean n = false;
        boolean n2 = false;
        int i = 0;
        for (int j = 0; j < 9; ++j) {
            if (PlaceMace.mc.player.getInventory().method_5438(j).method_7909() != Items.WIND_CHARGE) continue;
            i += PlaceMace.mc.player.getInventory().method_5438(j).getCount();
        }
        String value = i > 0 ? String.valueOf(i) : null;
        return value;
    }

    private Timer getTimer12() {
        return this.timer1;
    }

    private SliderSetting QztjAccess() {
        return this.Qztj;
    }

    private SliderSetting getDelay() {
        return this.delay;
    }
}

