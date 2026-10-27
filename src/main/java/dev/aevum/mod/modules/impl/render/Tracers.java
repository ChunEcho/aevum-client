/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.Donkey
 *  net.minecraft.Mule
 *  net.minecraft.ItemEntity
 *  net.minecraft.Player
 *  net.minecraft.Blocks
 *  net.minecraft.BlockPos
 *  net.minecraft.Vec3
 *  net.minecraft.BlockEntity
 *  net.minecraft.ChestBlockEntity
 *  net.minecraft.EnderChestBlockEntity
 *  net.minecraft.ShulkerBoxBlockEntity
 *  net.minecraft.PoseStack
 */
package dev.aevum.mod.modules.impl.render;

import dev.aevum.Aevum;
import dev.aevum.api.utils.render.Render3DUtil;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.Entity;
import net.minecraft.Donkey;
import net.minecraft.Mule;
import net.minecraft.ItemEntity;
import net.minecraft.Player;
import net.minecraft.Blocks;
import net.minecraft.BlockPos;
import net.minecraft.Vec3;
import net.minecraft.BlockEntity;
import net.minecraft.ChestBlockEntity;
import net.minecraft.EnderChestBlockEntity;
import net.minecraft.ShulkerBoxBlockEntity;
import net.minecraft.PoseStack;

public class Tracers
extends Module {
    private final ColorSetting item = this.add(new ColorSetting("Item", new Color(255, 255, 255, 100)).injectBoolean(true));
    private final ColorSetting player = this.add(new ColorSetting("Player", new Color(255, 255, 255, 100)).injectBoolean(true));
    private final ColorSetting chest = this.add(new ColorSetting("Chest", new Color(255, 255, 255, 100)).injectBoolean(false));
    private final ColorSetting enderChest = this.add(new ColorSetting("EnderChest", new Color(255, 100, 255, 100)).injectBoolean(false));
    private final ColorSetting shulkerBox = this.add(new ColorSetting("ShulkerBox", new Color(15, 255, 255, 100)).injectBoolean(false));
    private final ColorSetting donkey = this.add(new ColorSetting("Donkey", new Color(139, 90, 43, 100)).injectBoolean(false));
    private final ColorSetting netherPortal = this.add(new ColorSetting("NetherPortal", new Color(150, 50, 250, 100)).injectBoolean(false));
    private final Set<BlockPos> portals = new HashSet<BlockPos>();
    private int lastScanAge = -1;

    public Tracers() {
        super("Tracers", Module.Category.Render);
        this.setChinese("\u8ffd\u8e2a\u8005");
    }

    @Override
    public void onRender3D(PoseStack matrixStack) {
        Tracers.mc.options.bobView().method_41748((Object)false);
        if (this.item.booleanValue || this.player.booleanValue || this.donkey.booleanValue) {
            for (Entity entity : Aevum.THREAD.getEntities()) {
                if (entity instanceof ItemEntity && this.item.booleanValue) {
                    this.drawLine(entity.position(), this.item.getValue());
                    continue;
                }
                if (entity instanceof Player && this.player.booleanValue && entity != Tracers.mc.player) {
                    this.drawLine(entity.position(), this.player.getValue());
                    continue;
                }
                if (!(entity instanceof Donkey) && !(entity instanceof Mule) || !this.donkey.booleanValue) continue;
                this.drawLine(entity.position(), this.donkey.getValue());
            }
        }
        for (BlockEntity blockEntity : BlockUtil.getTileEntities()) {
            if (blockEntity instanceof ChestBlockEntity && this.chest.booleanValue) {
                this.drawLine(blockEntity.getBlockPos().method_46558(), this.chest.getValue());
                continue;
            }
            if (blockEntity instanceof EnderChestBlockEntity && this.enderChest.booleanValue) {
                this.drawLine(blockEntity.getBlockPos().method_46558(), this.enderChest.getValue());
                continue;
            }
            if (!(blockEntity instanceof ShulkerBoxBlockEntity) || !this.shulkerBox.booleanValue) continue;
            this.drawLine(blockEntity.getBlockPos().method_46558(), this.shulkerBox.getValue());
        }
        if (this.netherPortal.booleanValue) {
            if (Tracers.mc.player.tickCount % 40 == 0 && Tracers.mc.player.tickCount != this.lastScanAge) {
                this.lastScanAge = Tracers.mc.player.tickCount;
                this.portals.clear();
                BlockPos playerPos = Tracers.mc.player.method_24515();
                int r = 32;
                for (int x = -r; x <= r; ++x) {
                    for (int y = -r; y <= r; ++y) {
                        for (int z = -r; z <= r; ++z) {
                            BlockPos pos = playerPos.method_10069(x, y, z);
                            if (!Tracers.mc.level.method_8320(pos).method_27852(Blocks.NETHER_PORTAL)) continue;
                            this.portals.add(pos);
                        }
                    }
                }
            }
            for (BlockPos pos : this.portals) {
                this.drawLine(pos.method_46558(), this.netherPortal.getValue());
            }
        }
    }

    private void drawLine(Vec3 pos, Color color) {
        Render3DUtil.drawLine(pos, Tracers.mc.gameRenderer.method_19418().getPosition().method_1019(Vec3.method_1030((float)Tracers.mc.player.method_5695(mc.getTimer().method_60637(true)), (float)Tracers.mc.player.method_5705(mc.getTimer().method_60637(true))).method_1021(0.2)), color);
    }
}

