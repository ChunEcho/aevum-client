/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BlockItem
 *  net.minecraft.Blocks
 *  net.minecraft.AABB
 *  net.minecraft.HitResult
 *  net.minecraft.BlockHitResult
 *  net.minecraft.PoseStack
 */
package dev.aevum.mod.modules.impl.player;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.render.Render3DUtil;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.BlockItem;
import net.minecraft.Blocks;
import net.minecraft.AABB;
import net.minecraft.HitResult;
import net.minecraft.BlockHitResult;
import net.minecraft.PoseStack;

public class AirPlace
extends Module {
    public static AirPlace INSTANCE;
    public final BooleanSetting module = this.add(new BooleanSetting("Module", true));
    public final BooleanSetting grimBypass = this.add(new BooleanSetting("GrimBypass", false));
    public final BooleanSetting crossHair = this.add(new BooleanSetting("Crosshair", true).setParent());
    private final SliderSetting range = this.add(new SliderSetting("Range", 5.0, 0.0, 6.0, this.crossHair::isOpen));
    private final ColorSetting fill = this.add(new ColorSetting("Fill", new Color(255, 0, 0, 50), this.crossHair::isOpen).injectBoolean(true));
    private final ColorSetting box = this.add(new ColorSetting("Box", new Color(255, 0, 0, 100), this.crossHair::isOpen).injectBoolean(true));
    private BlockHitResult hit;
    private int cooldown;

    public AirPlace() {
        super("AirPlace", Module.Category.Player);
        this.setChinese("\u7a7a\u6c14\u653e\u7f6e");
        INSTANCE = this;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.crossHair.getValue()) {
            BlockHitResult bhr;
            HitResult hitResult;
            if (this.cooldown > 0) {
                --this.cooldown;
            }
            BlockHitResult class_39652 = this.hit = (hitResult = mc.getCameraEntity().method_5745(this.range.getValue(), 0.0f, false)) instanceof BlockHitResult ? (bhr = (BlockHitResult)hitResult) : null;
            if (this.hit == null || !AirPlace.mc.level.method_8320(this.hit.method_17777()).method_26204().equals(Blocks.AIR) || !(AirPlace.mc.player.getMainHandItem().method_7909() instanceof BlockItem)) {
                return;
            }
            boolean main = AirPlace.mc.player.getMainHandItem().method_7909() instanceof BlockItem;
            if (AirPlace.mc.options.keyUse.isDown() && main && this.cooldown <= 0) {
                BlockUtil.airPlace(this.hit.method_17777(), false);
                this.cooldown = 2;
            }
        }
    }

    @Override
    public void onRender3D(PoseStack stack) {
        if (this.crossHair.getValue()) {
            if (this.hit == null || !AirPlace.mc.level.method_8320(this.hit.method_17777()).method_26204().equals(Blocks.AIR) || !(AirPlace.mc.player.getMainHandItem().method_7909() instanceof BlockItem)) {
                return;
            }
            Render3DUtil.draw3DBox(stack, new AABB(this.hit.method_17777()), this.fill.getValue(), this.box.getValue(), this.box.booleanValue, this.fill.booleanValue);
        }
    }
}

