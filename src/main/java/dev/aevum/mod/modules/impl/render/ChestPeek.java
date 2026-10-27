/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.Container
 *  net.minecraft.ItemStack
 *  net.minecraft.Block
 *  net.minecraft.EnderChestBlock
 *  net.minecraft.BlockPos
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3
 *  net.minecraft.ShulkerBoxBlock
 *  net.minecraft.BlockEntity
 *  net.minecraft.Lighting
 *  net.minecraft.GuiGraphics
 *  net.minecraft.BlockHitResult
 *  net.minecraft.PoseStack
 *  org.lwjgl.opengl.GL11
 */
package dev.aevum.mod.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.Aevum;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.render.Render2DUtil;
import dev.aevum.api.utils.render.TextUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.Locale;
import net.minecraft.Container;
import net.minecraft.ItemStack;
import net.minecraft.Block;
import net.minecraft.EnderChestBlock;
import net.minecraft.BlockPos;
import net.minecraft.HitResult;
import net.minecraft.Vec3;
import net.minecraft.ShulkerBoxBlock;
import net.minecraft.BlockEntity;
import net.minecraft.Lighting;
import net.minecraft.GuiGraphics;
import net.minecraft.BlockHitResult;
import net.minecraft.PoseStack;
import org.lwjgl.opengl.GL11;

public class ChestPeek
extends Module
implements Wrapper {
    public final BooleanSetting showDistance = this.add(new BooleanSetting("Distance", true));
    public final BooleanSetting showCount = this.add(new BooleanSetting("ItemCount", true));
    public final SliderSetting offsetX = this.add(new SliderSetting("OffsetX", 24, -400, 400));
    public final SliderSetting offsetY = this.add(new SliderSetting("OffsetY", 0, -300, 300));
    public final SliderSetting backgroundAlpha = this.add(new SliderSetting("BackgroundAlpha", 190, 0, 255));
    public final SliderSetting size = this.add(new SliderSetting("Size", 1.0, 0.5, 2.0));
    private static final float GRID = 18.0f;
    private static final int COLS = 9;
    private static final int MAX_SLOTS = 27;
    private static final long FADE_MS = 170L;
    private static final int PAD = 7;
    private BlockPos targetPos;
    private long appearMs;
    private boolean hadTarget;
    private boolean disappearing;
    private long disappearMs;
    private final ItemStack[] snapItems = new ItemStack[27];
    private String snapTitle = "";
    private int snapRows;
    private int snapCount;
    private float snapX;
    private float snapY;

    public ChestPeek() {
        super("ChestPeek", Module.Category.Render);
        this.setChinese("\u7bb1\u5b50\u900f\u89c6");
    }

    @Override
    public void onRender2D(GuiGraphics DrawContext, float n) {
        if (Module.nullCheck() || !this.isOn()) {
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();
        BlockPos getBlockPos = null;
        if (ChestPeek.mc.hitResult != null && ChestPeek.mc.hitResult.method_17783() == HitResult.HitResult$Type.BLOCK) {
            getBlockPos = ((BlockHitResult)ChestPeek.mc.hitResult).method_17777();
        }
        ItemStack[] array = new ItemStack[27];
        int n2 = 0;
        int snapCount = 0;
        boolean b = false;
        String string = "";
        if (getBlockPos != null) {
            string = ChestPeek.mc.level.method_8320(getBlockPos).toString();
            if (string.contains("minecraft:ender_chest")) {
                b = true;
                if (Aevum.PLAYER != null && Aevum.PLAYER.ENDERCHEST_ITEM != null) {
                    n2 = Math.min(Aevum.PLAYER.ENDERCHEST_ITEM.size(), 27);
                    for (int i = 0; i < n2; ++i) {
                        ItemStack NarratorManager = (ItemStack)Aevum.PLAYER.ENDERCHEST_ITEM.get(i);
                        array[i] = NarratorManager;
                        if (array[i] == null || NarratorManager.method_7960()) continue;
                        ++snapCount;
                    }
                }
            } else {
                BlockEntity createMissed = ChestPeek.mc.level.method_8321(getBlockPos);
                if (createMissed instanceof Container) {
                    Container class_334 = (Container)createMissed;
                    n2 = Math.min(class_334.method_5439(), 27);
                    for (int j = 0; j < n2; ++j) {
                        ItemStack withSide = class_334.method_5438(j);
                        array[j] = withSide;
                        if (array[j] == null || withSide.method_7960()) continue;
                        ++snapCount;
                    }
                }
            }
        }
        if (snapCount > 0) {
            if (this.targetPos == null || !this.targetPos.equals((Object)getBlockPos)) {
                this.targetPos = getBlockPos;
                this.appearMs = currentTimeMillis;
            }
            this.hadTarget = true;
            this.disappearing = false;
            Block getSide = ChestPeek.mc.level.method_8320(getBlockPos).method_26204();
            String format = b ? "\u672b\u5f71\u7bb1" : (getSide instanceof ShulkerBoxBlock ? "\u6f5c\u5f71\u76d2" : (getSide instanceof EnderChestBlock ? "\u7bb1\u5b50" : (string.contains("minecraft:barrel") ? "\u6728\u6876" : "\u5bb9\u5668")));
            if (this.showDistance.getValue()) {
                format = String.format(Locale.ROOT, "%s [%.1fm]", format, ChestPeek.mc.player.position().method_1022(getBlockPos.method_46558()));
            }
            int min = Math.min(3, (n2 + 9 - 1) / 9);
            Vec3 worldSpaceToScreenSpace = TextUtil.worldSpaceToScreenSpace(getBlockPos.method_46558());
            if (worldSpaceToScreenSpace.z <= 0.0 || worldSpaceToScreenSpace.z >= 1.0 || Double.isNaN(worldSpaceToScreenSpace.x) || Double.isNaN(worldSpaceToScreenSpace.y)) {
                return;
            }
            float n3 = 26.0f + (float)min * 18.0f + 6.0f;
            float snapX = (float)worldSpaceToScreenSpace.x + (float)this.offsetX.getValueInt();
            float n4 = (float)worldSpaceToScreenSpace.y - n3 / 2.0f + (float)this.offsetY.getValueInt();
            float min2 = Math.min(1.0f, (float)(currentTimeMillis - this.appearMs) / 170.0f);
            float snapY = n4 + (1.0f - min2) * 8.0f;
            for (int k = 0; k < 27; ++k) {
                this.snapItems[k] = array[k];
            }
            this.snapTitle = format;
            this.snapRows = min;
            this.snapCount = snapCount;
            this.snapX = snapX;
            this.snapY = snapY;
            this.drawCard(DrawContext, this.snapX, this.snapY, this.size.getValueFloat(), format, array, min, snapCount, min2);
        } else if (this.hadTarget) {
            this.hadTarget = false;
            this.disappearing = true;
            this.disappearMs = currentTimeMillis;
        }
        if (this.disappearing) {
            long n5 = currentTimeMillis - this.disappearMs;
            if (n5 >= 170L) {
                this.disappearing = false;
                this.targetPos = null;
            } else {
                float n6 = 1.0f - (float)n5 / 170.0f;
                this.drawCard(DrawContext, this.snapX, this.snapY + (1.0f - n6) * -6.0f, this.size.getValueFloat(), this.snapTitle, this.snapItems, this.snapRows, this.snapCount, n6);
            }
        }
    }

    private void drawCard(GuiGraphics DrawContext, float n, float n2, float b, String s, ItemStack[] array, int n3, int n4, float n5) {
        float n6 = 176.0f;
        float n7 = 26.0f + (float)n3 * 18.0f + 6.0f;
        int clamp = ChestPeek.clamp(Math.round((float)this.backgroundAlpha.getValueInt() * n5), 0, 255);
        int clamp2 = ChestPeek.clamp(Math.round(26.0f * n5), 0, 255);
        int clamp3 = ChestPeek.clamp(Math.round(255.0f * n5), 0, 255);
        int clamp4 = ChestPeek.clamp(Math.round(225.0f * n5), 0, 255);
        if (clamp <= 0) {
            return;
        }
        float max = Math.max(0.1f, b);
        PoseStack getMatrices = DrawContext.method_51448();
        getMatrices.method_22903();
        getMatrices.method_46416(n, n2, 0.0f);
        getMatrices.method_22905(max, max, 1.0f);
        Render2DUtil.drawRoundedRect(getMatrices, 0.0f, 0.0f, 176.0f, n7, 7.0f, new Color(8, 8, 10, clamp));
        Render2DUtil.drawRoundedStroke(getMatrices, 0.0f, 0.0f, 176.0f, n7, 7.0f, new Color(255, 255, 255, clamp2), 10);
        DrawContext.method_51433(ChestPeek.mc.font, s, 7, 6, new Color(248, 248, 248, clamp3).getRGB(), true);
        if (this.showCount.getValue()) {
            DrawContext.method_51433(ChestPeek.mc.font, n4 + " items", 7, 16, new Color(160, 200, 230, clamp4).getRGB(), true);
        }
        RenderSystem.disableDepthTest();
        GL11.glClear((int)256);
        Lighting.setupFor3DItems();
        for (int i = 0; i < n3; ++i) {
            for (int j = 0; j < 9; ++j) {
                ItemStack NarratorManager;
                int n8 = i * 9 + j;
                if (n8 >= array.length || (NarratorManager = array[n8]) == null || NarratorManager.method_7960()) continue;
                int n9 = 7 + (int)((float)j * 18.0f);
                int n10 = 26 + i * 18;
                DrawContext.method_51427(NarratorManager, n9, n10);
                DrawContext.method_51431(ChestPeek.mc.font, NarratorManager, n9, n10);
            }
        }
        Lighting.setupForFlatItems();
        RenderSystem.enableDepthTest();
        getMatrices.method_22909();
    }

    private static int clamp(int a, int n, int b) {
        return a < n ? n : Math.min(a, b);
    }
}

