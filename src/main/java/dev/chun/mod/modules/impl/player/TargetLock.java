package dev.chun.mod.modules.impl.player;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.chun.Aevum;
import dev.chun.api.utils.math.MathUtil;
import dev.chun.api.utils.render.TextUtil;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class TargetLock extends Module {
   public static TargetLock INSTANCE;
   private final SliderSetting lineWidth = this.add(new SliderSetting("LineWidth", 3.0, 0.5, 10.0, 0.5));
   private final SliderSetting scale = this.add(new SliderSetting("Scale", 0.45, 0.2, 1.5, 0.05));
   private final SliderSetting heightPos = this.add(new SliderSetting("HeightPos", 0.63, 0.0, 1.0, 0.01));
   private final SliderSetting gap = this.add(new SliderSetting("Gap", 0.28, 0.0, 0.45, 0.01));
   private final SliderSetting rotationSpeed = this.add(new SliderSetting("RotationSpeed", 0.0, -180.0, 180.0, 1.0));
   private final SliderSetting rainbowSpeed = this.add(new SliderSetting("RainbowSpeed", 1.0, 0.1, 3.0, 0.1));
   private final BooleanSetting players = this.add(new BooleanSetting("Players", true));
   private final BooleanSetting friends = this.add(new BooleanSetting("Friends", false));
   private final BooleanSetting self = this.add(new BooleanSetting("Self", false));

   public TargetLock() {
      super("TargetLock", Module.Category.Player);
      this.setChinese("\u76ee\u6807\u9501\u5b9a");
      INSTANCE = this;
   }

   @Override
   public void onRender2D(DrawContext DrawContext, float n) {
      if (!Module.nullCheck()) {
         MatrixStack getMatrices = DrawContext.getMatrices();

         for (Entity NarratorManager : Aevum.THREAD.getEntities()) {
            if (NarratorManager instanceof PlayerEntity
               && (NarratorManager != mc.player || this.self.getValue())
               && (this.friends.getValue() || !Aevum.FRIEND.isFriend(((PlayerEntity)NarratorManager).getName().getString()))
               && this.players.getValue()) {
               this.drawTargetLock(getMatrices, NarratorManager, n);
            }
         }
      }
   }

   private void drawTargetLock(MatrixStack MatrixStack, Entity VertexConsumer, float n) {
      double interpolate = MathUtil.interpolate(VertexConsumer.lastRenderX, VertexConsumer.getX(), (double)n);
      double interpolate2 = MathUtil.interpolate(VertexConsumer.lastRenderY, VertexConsumer.getY(), (double)n);
      double interpolate3 = MathUtil.interpolate(VertexConsumer.lastRenderZ, VertexConsumer.getZ(), (double)n);
      Box getBoundingBox = VertexConsumer.getBoundingBox();
      Box class_4589 = new Box(
         getBoundingBox.minX - VertexConsumer.getX() + interpolate,
         getBoundingBox.minY - VertexConsumer.getY() + interpolate2,
         getBoundingBox.minZ - VertexConsumer.getZ() + interpolate3,
         getBoundingBox.maxX - VertexConsumer.getX() + interpolate,
         getBoundingBox.maxY - VertexConsumer.getY() + interpolate2,
         getBoundingBox.maxZ - VertexConsumer.getZ() + interpolate3
      );
      Vec3d[] array = new Vec3d[]{
         new Vec3d(class_4589.minX, class_4589.minY, class_4589.minZ),
         new Vec3d(class_4589.minX, class_4589.maxY, class_4589.minZ),
         new Vec3d(class_4589.maxX, class_4589.minY, class_4589.minZ),
         new Vec3d(class_4589.maxX, class_4589.maxY, class_4589.minZ),
         new Vec3d(class_4589.minX, class_4589.minY, class_4589.maxZ),
         new Vec3d(class_4589.minX, class_4589.maxY, class_4589.maxZ),
         new Vec3d(class_4589.maxX, class_4589.minY, class_4589.maxZ),
         new Vec3d(class_4589.maxX, class_4589.maxY, class_4589.maxZ)
      };
      double min = Double.POSITIVE_INFINITY;
      double min2 = Double.POSITIVE_INFINITY;
      double max = Double.NEGATIVE_INFINITY;
      double max2 = Double.NEGATIVE_INFINITY;
      boolean b = false;
      Vec3d[] array2 = array;
      int length = array.length;

      for (int i = 0; i < length; i++) {
         Vec3d worldSpaceToScreenSpace = TextUtil.worldSpaceToScreenSpace(array2[i]);
         if (worldSpaceToScreenSpace.z > 0.0 && worldSpaceToScreenSpace.z < 1.0) {
            b = true;
            min = Math.min(worldSpaceToScreenSpace.x, min);
            min2 = Math.min(worldSpaceToScreenSpace.y, min2);
            max = Math.max(worldSpaceToScreenSpace.x, max);
            max2 = Math.max(worldSpaceToScreenSpace.y, max2);
         }
      }

      if (b && max - min > 0.0 && max2 - min2 > 0.0) {
         float n2 = (float)(max2 - min2);
         float n3 = (float)(min + max) / 2.0F;
         float n4 = (float)max2 - n2 * this.heightPos.getValueFloat();
         float n5 = n2 * 0.5F * this.scale.getValueFloat();
         float n6 = 0.5F - Math.max(0.0F, Math.min(0.5F, this.gap.getValueFloat())) * 0.5F;
         long currentTimeMillis = System.currentTimeMillis();
         double n7 = (double)(currentTimeMillis % 360000L) / 1000.0 * Math.toRadians(this.rotationSpeed.getValue());
         float n8 = (float)Math.cos(n7);
         float n9 = (float)Math.sin(n7);
         float n10 = (float)(currentTimeMillis % 10000L) * this.rainbowSpeed.getValueFloat() / 2000.0F;
         float valueFloat = this.lineWidth.getValueFloat();
         float[][] array3 = new float[][]{{-n5, n5}, {n5, n5}, {n5, -n5}, {-n5, -n5}};
         float[][] array4 = new float[4][2];

         for (int j = 0; j < 4; j++) {
            array4[j][0] = n3 + array3[j][0] * n8 - array3[j][1] * n9;
            array4[j][1] = n4 + array3[j][0] * n9 + array3[j][1] * n8;
         }

         for (int k = 0; k < 4; k++) {
            float[] array5 = array4[k];
            float[] array6 = array4[(k + 1) % 4];
            this.drawSegment(MatrixStack, array5, array6, 0.0F, n6, valueFloat, n10, k);
            this.drawSegment(MatrixStack, array5, array6, 1.0F - n6, 1.0F, valueFloat, n10, k);
         }
      }
   }

   private void drawSegment(MatrixStack MatrixStack, float[] array, float[] array2, float n, float n2, float n3, float n4, int n5) {
      float n6 = array[0] + (array2[0] - array[0]) * n;
      float n7 = array[1] + (array2[1] - array[1]) * n;
      float n8 = array[0] + (array2[0] - array[0]) * n2;
      float n9 = array[1] + (array2[1] - array[1]) * n2;
      int n10 = Color.getHSBColor((n4 + (float)n5 / 4.0F + (n + n2) / 8.0F) % 1.0F, 1.0F, 1.0F).getRGB() & 16777215;
      this.drawThickLine(MatrixStack, n6, n7, n8, n9, n3 * 3.0F, n10 | 1275068416);
      this.drawThickLine(MatrixStack, n6, n7, n8, n9, n3, n10 | 0xFF000000);
   }

   private void drawThickLine(MatrixStack MatrixStack, float n, float n2, float n3, float n4, float n5, int n6) {
      float n7 = n3 - n;
      float n8 = n4 - n2;
      float n9 = (float)Math.sqrt((double)(n7 * n7 + n8 * n8));
      if (n9 > 0.0F) {
         float n10 = -n8 / n9 * n5 / 2.0F;
         float n11 = n7 / n9 * n5 / 2.0F;
         Matrix4f getPositionMatrix = MatrixStack.peek().getPositionMatrix();
         RenderSystem.enableBlend();
         RenderSystem.setShader(GameRenderer::getPositionColorProgram);
         BufferBuilder getNormalMatrix = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
         getNormalMatrix.vertex(getPositionMatrix, n + n10, n2 + n11, 0.0F).color(n6);
         getNormalMatrix.vertex(getPositionMatrix, n3 + n10, n4 + n11, 0.0F).color(n6);
         getNormalMatrix.vertex(getPositionMatrix, n3 - n10, n4 - n11, 0.0F).color(n6);
         getNormalMatrix.vertex(getPositionMatrix, n - n10, n2 - n11, 0.0F).color(n6);
         BufferRenderer.drawWithGlobalProgram(getNormalMatrix.end());
         RenderSystem.disableBlend();
      }
   }
}
