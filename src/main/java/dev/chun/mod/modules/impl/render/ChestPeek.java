package dev.chun.mod.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.chun.Aevum;
import dev.chun.api.utils.Wrapper;
import dev.chun.api.utils.render.Render2DUtil;
import dev.chun.api.utils.render.TextUtil;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.Locale;
import net.minecraft.block.Block;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class ChestPeek extends Module implements Wrapper {
   public final BooleanSetting showDistance = this.add(new BooleanSetting("Distance", true));
   public final BooleanSetting showCount = this.add(new BooleanSetting("ItemCount", true));
   public final SliderSetting offsetX = this.add(new SliderSetting("OffsetX", 24, -400, 400));
   public final SliderSetting offsetY = this.add(new SliderSetting("OffsetY", 0, -300, 300));
   public final SliderSetting backgroundAlpha = this.add(new SliderSetting("BackgroundAlpha", 190, 0, 255));
   public final SliderSetting size = this.add(new SliderSetting("Size", 1.0, 0.5, 2.0));
   private static final float GRID = 18.0F;
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
   public void onRender2D(DrawContext DrawContext, float n) {
      if (!Module.nullCheck() && this.isOn()) {
         long currentTimeMillis = System.currentTimeMillis();
         BlockPos getBlockPos = null;
         if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == Type.BLOCK) {
            getBlockPos = ((BlockHitResult)mc.crosshairTarget).getBlockPos();
         }

         ItemStack[] array = new ItemStack[27];
         int n2 = 0;
         int snapCount = 0;
         boolean b = false;
         String string = "";
         if (getBlockPos != null) {
            string = mc.world.getBlockState(getBlockPos).toString();
            if (string.contains("minecraft:ender_chest")) {
               b = true;
               if (Aevum.PLAYER != null && Aevum.PLAYER.ENDERCHEST_ITEM != null) {
                  n2 = Math.min(Aevum.PLAYER.ENDERCHEST_ITEM.size(), 27);

                  for (int i = 0; i < n2; i++) {
                     ItemStack NarratorManager = (ItemStack)Aevum.PLAYER.ENDERCHEST_ITEM.get(i);
                     if ((array[i] = NarratorManager) != null && !NarratorManager.isEmpty()) {
                        snapCount++;
                     }
                  }
               }
            } else if (mc.world.getBlockEntity(getBlockPos) instanceof Inventory class_334) {
               n2 = Math.min(class_334.size(), 27);

               for (int j = 0; j < n2; j++) {
                  ItemStack withSide = class_334.getStack(j);
                  if ((array[j] = withSide) != null && !withSide.isEmpty()) {
                     snapCount++;
                  }
               }
            }
         }

         if (snapCount <= 0) {
            if (this.hadTarget) {
               this.hadTarget = false;
               this.disappearing = true;
               this.disappearMs = currentTimeMillis;
            }
         } else {
            if (this.targetPos == null || !this.targetPos.equals(getBlockPos)) {
               this.targetPos = getBlockPos;
               this.appearMs = currentTimeMillis;
            }

            this.hadTarget = true;
            this.disappearing = false;
            Block getSide = mc.world.getBlockState(getBlockPos).getBlock();
            String format;
            if (b) {
               format = "\u672b\u5f71\u7bb1";
            } else if (getSide instanceof ShulkerBoxBlock) {
               format = "\u6f5c\u5f71\u76d2";
            } else if (getSide instanceof EnderChestBlock) {
               format = "\u7bb1\u5b50";
            } else if (string.contains("minecraft:barrel")) {
               format = "\u6728\u6876";
            } else {
               format = "\u5bb9\u5668";
            }

            if (this.showDistance.getValue()) {
               format = String.format(Locale.ROOT, "%s [%.1fm]", format, mc.player.getPos().distanceTo(getBlockPos.toCenterPos()));
            }

            int min = Math.min(3, (n2 + 9 - 1) / 9);
            Vec3d worldSpaceToScreenSpace = TextUtil.worldSpaceToScreenSpace(getBlockPos.toCenterPos());
            if (worldSpaceToScreenSpace.z <= 0.0
               || worldSpaceToScreenSpace.z >= 1.0
               || Double.isNaN(worldSpaceToScreenSpace.x)
               || Double.isNaN(worldSpaceToScreenSpace.y)) {
               return;
            }

            float n3 = 26.0F + (float)min * 18.0F + 6.0F;
            float snapX = (float)worldSpaceToScreenSpace.x + (float)this.offsetX.getValueInt();
            float n4 = (float)worldSpaceToScreenSpace.y - n3 / 2.0F + (float)this.offsetY.getValueInt();
            float min2 = Math.min(1.0F, (float)(currentTimeMillis - this.appearMs) / 170.0F);
            float snapY = n4 + (1.0F - min2) * 8.0F;

            for (int k = 0; k < 27; k++) {
               this.snapItems[k] = array[k];
            }

            this.snapTitle = format;
            this.snapRows = min;
            this.snapCount = snapCount;
            this.drawCard(DrawContext, this.snapX = snapX, this.snapY = snapY, this.size.getValueFloat(), format, array, min, snapCount, min2);
         }

         if (this.disappearing) {
            long n5 = currentTimeMillis - this.disappearMs;
            if (n5 >= 170L) {
               this.disappearing = false;
               this.targetPos = null;
            } else {
               float n6 = 1.0F - (float)n5 / 170.0F;
               this.drawCard(
                  DrawContext,
                  this.snapX,
                  this.snapY + (1.0F - n6) * -6.0F,
                  this.size.getValueFloat(),
                  this.snapTitle,
                  this.snapItems,
                  this.snapRows,
                  this.snapCount,
                  n6
               );
            }
         }
      }
   }

   private void drawCard(DrawContext DrawContext, float n, float n2, float b, String s, ItemStack[] array, int n3, int n4, float n5) {
      float n6 = 176.0F;
      float n7 = 26.0F + (float)n3 * 18.0F + 6.0F;
      int clamp = clamp(Math.round((float)this.backgroundAlpha.getValueInt() * n5), 0, 255);
      int clamp2 = clamp(Math.round(26.0F * n5), 0, 255);
      int clamp3 = clamp(Math.round(255.0F * n5), 0, 255);
      int clamp4 = clamp(Math.round(225.0F * n5), 0, 255);
      if (clamp > 0) {
         float max = Math.max(0.1F, b);
         MatrixStack getMatrices = DrawContext.getMatrices();
         getMatrices.push();
         getMatrices.translate(n, n2, 0.0F);
         getMatrices.scale(max, max, 1.0F);
         Render2DUtil.drawRoundedRect(getMatrices, 0.0F, 0.0F, 176.0F, n7, 7.0F, new Color(8, 8, 10, clamp));
         Render2DUtil.drawRoundedStroke(getMatrices, 0.0F, 0.0F, 176.0F, n7, 7.0F, new Color(255, 255, 255, clamp2), 10);
         DrawContext.drawText(mc.textRenderer, s, 7, 6, new Color(248, 248, 248, clamp3).getRGB(), true);
         if (this.showCount.getValue()) {
            DrawContext.drawText(mc.textRenderer, n4 + " items", 7, 16, new Color(160, 200, 230, clamp4).getRGB(), true);
         }

         RenderSystem.disableDepthTest();
         GL11.glClear(256);
         DiffuseLighting.enableGuiDepthLighting();

         for (int i = 0; i < n3; i++) {
            for (int j = 0; j < 9; j++) {
               int n8 = i * 9 + j;
               if (n8 < array.length) {
                  ItemStack NarratorManager = array[n8];
                  if (NarratorManager != null && !NarratorManager.isEmpty()) {
                     int n9 = 7 + (int)((float)j * 18.0F);
                     int n10 = 26 + i * 18;
                     DrawContext.drawItem(NarratorManager, n9, n10);
                     DrawContext.drawItemInSlot(mc.textRenderer, NarratorManager, n9, n10);
                  }
               }
            }
         }

         DiffuseLighting.disableGuiDepthLighting();
         RenderSystem.enableDepthTest();
         getMatrices.pop();
      }
   }

   private static int clamp(int a, int n, int b) {
      return a < n ? n : Math.min(a, b);
   }
}
