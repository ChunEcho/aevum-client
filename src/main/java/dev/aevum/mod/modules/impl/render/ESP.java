/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.ItemEntity
 *  net.minecraft.Player
 *  net.minecraft.ThrownEnderpearl
 *  net.minecraft.AABB
 *  net.minecraft.Vec3
 *  net.minecraft.BlockEntity
 *  net.minecraft.ChestBlockEntity
 *  net.minecraft.EnderChestBlockEntity
 *  net.minecraft.ShulkerBoxBlockEntity
 *  net.minecraft.TheEndPortalBlockEntity
 *  net.minecraft.PoseStack
 */
package dev.aevum.mod.modules.impl.render;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.EntitySpawnedEvent;
import dev.aevum.api.utils.math.MathUtil;
import dev.aevum.api.utils.render.Render3DUtil;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.asm.accessors.IEntity;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import java.awt.Color;
import java.util.Comparator;
import net.minecraft.Entity;
import net.minecraft.ItemEntity;
import net.minecraft.Player;
import net.minecraft.ThrownEnderpearl;
import net.minecraft.AABB;
import net.minecraft.Vec3;
import net.minecraft.BlockEntity;
import net.minecraft.ChestBlockEntity;
import net.minecraft.EnderChestBlockEntity;
import net.minecraft.ShulkerBoxBlockEntity;
import net.minecraft.TheEndPortalBlockEntity;
import net.minecraft.PoseStack;

public class ESP
extends Module {
    public static ESP INSTANCE;
    public final BooleanSetting box = this.add(new BooleanSetting("BoxESP", true).setParent());
    private final ColorSetting endPortalFill = this.add(new ColorSetting("EndPortalFill", new Color(255, 243, 129, 100), this.box::isOpen).injectBoolean(false));
    private final ColorSetting endPortalOutline = this.add(new ColorSetting("EndPortalOutline", new Color(255, 243, 129, 100), this.box::isOpen).injectBoolean(false));
    private final ColorSetting itemFill = this.add(new ColorSetting("ItemFill", new Color(255, 255, 255, 100), this.box::isOpen).injectBoolean(true));
    private final ColorSetting itemOutline = this.add(new ColorSetting("ItemOutline", new Color(255, 255, 255, 100), this.box::isOpen).injectBoolean(true));
    private final ColorSetting playerFill = this.add(new ColorSetting("PlayerFill", new Color(255, 255, 255, 100), this.box::isOpen).injectBoolean(true));
    private final ColorSetting playerOutline = this.add(new ColorSetting("PlayerOutline", new Color(255, 255, 255, 100), this.box::isOpen).injectBoolean(true));
    private final ColorSetting chestFill = this.add(new ColorSetting("ChestFill", new Color(255, 198, 123, 100), this.box::isOpen).injectBoolean(false));
    private final ColorSetting chestOutline = this.add(new ColorSetting("ChestOutline", new Color(255, 198, 123, 100), this.box::isOpen).injectBoolean(false));
    private final ColorSetting enderChestFill = this.add(new ColorSetting("EnderChestFill", new Color(255, 100, 255, 100), this.box::isOpen).injectBoolean(false));
    private final ColorSetting enderChestOutline = this.add(new ColorSetting("EnderChestOutline", new Color(255, 100, 255, 100), this.box::isOpen).injectBoolean(false));
    private final ColorSetting shulkerBoxFill = this.add(new ColorSetting("ShulkerBoxFill", new Color(15, 255, 255, 100), this.box::isOpen).injectBoolean(false));
    private final ColorSetting shulkerBoxOutline = this.add(new ColorSetting("ShulkerBoxOutline", new Color(15, 255, 255, 100), this.box::isOpen).injectBoolean(false));
    public final BooleanSetting item = this.add(new BooleanSetting("ItemName", false).setParent());
    public final BooleanSetting customName = this.add(new BooleanSetting("CustomName", false, this.item::isOpen));
    public final BooleanSetting count = this.add(new BooleanSetting("Count", true, this.item::isOpen));
    private final ColorSetting text = this.add(new ColorSetting("Text", new Color(255, 255, 255, 255), this.item::isOpen));
    public final BooleanSetting pearl = this.add(new BooleanSetting("PearlOwner", true));

    public ESP() {
        super("ESP", Module.Category.Render);
        this.setChinese("\u900f\u89c6");
        INSTANCE = this;
    }

    @Override
    public void onRender3D(PoseStack matrixStack) {
        if (this.item.getValue()) {
            for (Entity entity : Aevum.THREAD.getEntities()) {
                if (!(entity instanceof ItemEntity)) continue;
                ItemEntity itemEntity = (ItemEntity)entity;
                int itemCount = itemEntity.getItem().getCount();
                String s = this.count.getValue() && itemCount > 1 ? " x" + itemCount : "";
                String name = (this.customName.getValue() ? itemEntity.getItem().getHoverName() : itemEntity.getItem().method_7909().getDescription()).getString();
                Render3DUtil.drawText3D(name + s, ((IEntity)itemEntity).getDimensions().method_30757(new Vec3(MathUtil.interpolate(itemEntity.xOld, itemEntity.getX(), (double)mc.getTimer().method_60637(true)), MathUtil.interpolate(itemEntity.yOld, itemEntity.getY(), (double)mc.getTimer().method_60637(true)), MathUtil.interpolate(itemEntity.zOld, itemEntity.getZ(), (double)mc.getTimer().method_60637(true)))).method_1009(0.0, 0.1, 0.0).method_1005().method_1031(0.0, 0.5, 0.0), this.text.getValue());
            }
        }
        if (this.box.getValue()) {
            if (this.itemFill.booleanValue || this.playerFill.booleanValue) {
                for (Entity entityx : Aevum.THREAD.getEntities()) {
                    Color color;
                    if (!(entityx instanceof ItemEntity) || !this.itemFill.booleanValue && !this.itemOutline.booleanValue) {
                        if (!(entityx instanceof Player) || !this.playerFill.booleanValue && !this.playerOutline.booleanValue) continue;
                        color = this.playerFill.getValue();
                        Render3DUtil.draw3DBox(matrixStack, ((IEntity)entityx).getDimensions().method_30757(new Vec3(MathUtil.interpolate(entityx.xOld, entityx.getX(), (double)mc.getTimer().method_60637(true)), MathUtil.interpolate(entityx.yOld, entityx.getY(), (double)mc.getTimer().method_60637(true)), MathUtil.interpolate(entityx.zOld, entityx.getZ(), (double)mc.getTimer().method_60637(true)))).method_1009(0.0, 0.1, 0.0), color, this.playerOutline.getValue(), this.playerOutline.booleanValue, this.playerFill.booleanValue);
                        continue;
                    }
                    color = this.itemFill.getValue();
                    Render3DUtil.draw3DBox(matrixStack, ((IEntity)entityx).getDimensions().method_30757(new Vec3(MathUtil.interpolate(entityx.xOld, entityx.getX(), (double)mc.getTimer().method_60637(true)), MathUtil.interpolate(entityx.yOld, entityx.getY(), (double)mc.getTimer().method_60637(true)), MathUtil.interpolate(entityx.zOld, entityx.getZ(), (double)mc.getTimer().method_60637(true)))), color, this.itemOutline.getValue(), this.itemOutline.booleanValue, this.itemFill.booleanValue);
                }
            }
            for (BlockEntity blockEntity : BlockUtil.getTileEntities()) {
                AABB box;
                if (!(blockEntity instanceof ChestBlockEntity) || !this.chestFill.booleanValue && !this.chestOutline.booleanValue) {
                    if (!(blockEntity instanceof EnderChestBlockEntity) || !this.enderChestFill.booleanValue && !this.enderChestOutline.booleanValue) {
                        if (!(blockEntity instanceof ShulkerBoxBlockEntity) || !this.shulkerBoxFill.booleanValue && !this.shulkerBoxOutline.booleanValue) {
                            if (!(blockEntity instanceof TheEndPortalBlockEntity) || !this.endPortalFill.booleanValue && !this.endPortalOutline.booleanValue) continue;
                            box = new AABB(blockEntity.getBlockPos());
                            Render3DUtil.draw3DBox(matrixStack, box, this.endPortalFill.getValue(), this.endPortalOutline.getValue(), this.endPortalOutline.booleanValue, this.endPortalFill.booleanValue);
                            continue;
                        }
                        box = new AABB(blockEntity.getBlockPos());
                        Render3DUtil.draw3DBox(matrixStack, box, this.shulkerBoxFill.getValue(), this.shulkerBoxOutline.getValue(), this.shulkerBoxOutline.booleanValue, this.shulkerBoxFill.booleanValue);
                        continue;
                    }
                    box = new AABB(blockEntity.getBlockPos());
                    Render3DUtil.draw3DBox(matrixStack, box, this.enderChestFill.getValue(), this.enderChestOutline.getValue(), this.enderChestOutline.booleanValue, this.enderChestFill.booleanValue);
                    continue;
                }
                box = new AABB(blockEntity.getBlockPos());
                Render3DUtil.draw3DBox(matrixStack, box, this.chestFill.getValue(), this.chestOutline.getValue(), this.chestOutline.booleanValue, this.chestFill.booleanValue);
            }
        }
    }

    @EventListener
    public void onReceivePacket(EntitySpawnedEvent event) {
        Entity class_12972;
        if (!ESP.nullCheck() && this.pearl.getValue() && (class_12972 = event.getEntity()) instanceof ThrownEnderpearl) {
            ThrownEnderpearl pearlEntity = (ThrownEnderpearl)class_12972;
            if (pearlEntity.method_24921() != null) {
                pearlEntity.method_5665(pearlEntity.method_24921().method_5477());
                pearlEntity.method_5880(true);
            } else {
                ESP.mc.level.method_18456().stream().min(Comparator.comparingDouble(p -> p.position().method_1022(new Vec3(pearlEntity.getX(), pearlEntity.getY(), pearlEntity.getZ())))).ifPresent(player -> {
                    pearlEntity.method_5665(player.method_5477());
                    pearlEntity.method_5880(true);
                });
            }
        }
    }
}

