/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BlockGetter
 *  net.minecraft.Blocks
 *  net.minecraft.Block
 *  net.minecraft.BlockPos
 *  net.minecraft.AABB
 *  net.minecraft.BlockState
 *  net.minecraft.PoseStack
 */
package dev.aevum.mod.modules.impl.render;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.render.Render3DUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.BlockGetter;
import net.minecraft.Blocks;
import net.minecraft.Block;
import net.minecraft.BlockPos;
import net.minecraft.AABB;
import net.minecraft.BlockState;
import net.minecraft.PoseStack;

public class PhaseESP
extends Module {
    public static PhaseESP INSTANCE;
    private final SliderSetting distance = this.add(new SliderSetting("Distance", 0.1, 0.0, 1.0, 0.1));
    private final SliderSetting bevelDistance = this.add(new SliderSetting("BevelDistance", 0.2, 0.0, 1.0, 0.1));
    private final ColorSetting safeFill = this.add(new ColorSetting("SafeFill", new Color(0, 255, 0, 50)).injectBoolean(true));
    private final ColorSetting safeBox = this.add(new ColorSetting("SafeBox", new Color(0, 255, 0, 100)).injectBoolean(true));
    private final ColorSetting semiSafeFill = this.add(new ColorSetting("SemiSafeFill", new Color(244, 255, 0, 50)).injectBoolean(true));
    private final ColorSetting semiSafeBox = this.add(new ColorSetting("SemiSafeBox", new Color(244, 255, 0, 100)).injectBoolean(true));
    private final ColorSetting unsafeFill = this.add(new ColorSetting("UnsafeFill", new Color(148, 0, 0, 50)).injectBoolean(true));
    private final ColorSetting unsafeBox = this.add(new ColorSetting("UnsafeBox", new Color(148, 0, 0, 100)).injectBoolean(true));
    List<BlockPos> safe = new ArrayList<BlockPos>();
    List<BlockPos> semiSafe = new ArrayList<BlockPos>();
    List<BlockPos> unsafe = new ArrayList<BlockPos>();
    int[] offsets = new int[]{1, 0, -1};

    public PhaseESP() {
        super("PhaseESP", Module.Category.Render);
        this.setChinese("\u7a7f\u5899\u663e\u793a");
        INSTANCE = this;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        this.safe.clear();
        this.semiSafe.clear();
        this.unsafe.clear();
        for (int x : this.offsets) {
            for (int z : this.offsets) {
                Block downBlock;
                double d2;
                BlockPos pos = PhaseESP.mc.player.method_24515().method_10069(x, 0, z);
                double d = PhaseESP.mc.player.position().method_1022(pos.method_61082());
                double d3 = d2 = x != 0 && z != 0 ? this.bevelDistance.getValue() + 1.0 : this.distance.getValue() + 0.8;
                if (!(d <= d2)) continue;
                BlockState blockState = PhaseESP.mc.level.method_8320(pos);
                BlockPos downPos = pos.method_10074();
                if (blockState.method_26204() == Blocks.BEDROCK) {
                    downBlock = PhaseESP.mc.level.method_8320(downPos).method_26204();
                    if (downBlock == Blocks.BEDROCK) {
                        this.safe.add(pos);
                        continue;
                    }
                    this.unsafe.add(pos);
                    continue;
                }
                if (!blockState.method_26234((BlockGetter)PhaseESP.mc.level, pos)) continue;
                downBlock = PhaseESP.mc.level.method_8320(downPos).method_26204();
                if (downBlock == Blocks.BEDROCK) {
                    this.semiSafe.add(pos);
                    continue;
                }
                this.unsafe.add(pos);
            }
        }
    }

    @Override
    public void onRender3D(PoseStack matrixStack) {
        this.draw(matrixStack, this.safe, this.safeFill, this.safeBox);
        this.draw(matrixStack, this.unsafe, this.unsafeFill, this.unsafeBox);
        this.draw(matrixStack, this.semiSafe, this.semiSafeFill, this.semiSafeBox);
    }

    private void draw(PoseStack matrixStack, List<BlockPos> list, ColorSetting fill, ColorSetting box) {
        for (BlockPos pos : list) {
            AABB espBox = new AABB((double)pos.method_10263(), (double)pos.method_10264(), (double)pos.getZ(), (double)(pos.method_10263() + 1), (double)pos.method_10264(), (double)(pos.getZ() + 1));
            if (fill.booleanValue) {
                Render3DUtil.drawFill(matrixStack, espBox, fill.getValue());
            }
            if (!box.booleanValue) continue;
            Render3DUtil.drawBox(matrixStack, espBox, box.getValue());
        }
    }

    public static enum Type {
        None,
        Air,
        Normal,
        Bedrock;

    }
}

