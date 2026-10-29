package dev.chun.mod.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.UpdateEvent;
import dev.chun.api.utils.Wrapper;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.ColorSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class PlayerTrail extends Module {
   private final ColorSetting trailColor = this.add(new ColorSetting("Color", new Color(255, 100, 200, 180)));
   private final SliderSetting trailLength = this.add(new SliderSetting("Length", 30, 5, 100));
   private final SliderSetting lineWidth = this.add(new SliderSetting("LineWidth", 0.35F, 0.05F, 2.0));
   private final SliderSetting lineHeight = this.add(new SliderSetting("LineHeight", 0.9F, 0.05F, 2.0));
   private final SliderSetting yOffset = this.add(new SliderSetting("YOffset", 0.6F, 0.0, 2.0, 0.1F));
   private final BooleanSetting onlySelf = this.add(new BooleanSetting("OnlySelf", true));
   private final SliderSetting updateInterval = this.add(new SliderSetting("UpdateInterval", 1, 1, 10));
   private final SliderSetting minStep = this.add(new SliderSetting("MinStep", 0.08F, 0.01F, 0.5));
   private final BooleanSetting cuteMode = this.add(new BooleanSetting("CuteMode", false));
   private final SliderSetting cuteStep = this.add(new SliderSetting("CuteStep", 0.35F, 0.1F, 1.5, 0.05F));
   private final SliderSetting rainbowSpeed = this.add(new SliderSetting("RainbowSpeed", 1.0, 0.1F, 5.0, 0.1F));
   private final List<PlayerTrail.PlayerTrailData> trailDataList = new CopyOnWriteArrayList<>();
   private int updateCounter = 0;
   private float rainbowHue = 0.0F;

   public PlayerTrail() {
      super("PlayerTrail", "Displays continuous trail line behind players", Module.Category.Render);
      this.setChinese("\u73a9\u5bb6\u8f68\u8ff9\u7ebf");
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.trailDataList.clear();
      this.updateCounter = 0;
      this.rainbowHue = 0.0F;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.trailDataList.clear();
   }

   @EventListener
   public void onUpdate(UpdateEvent event) {
      if (this.isOn()) {
         if (Wrapper.mc.world != null && Wrapper.mc.player != null) {
            if (this.cuteMode.getValue()) {
               this.rainbowHue = this.rainbowHue + this.rainbowSpeed.getValueFloat() * 0.5F;
               if (this.rainbowHue > 360.0F) {
                  this.rainbowHue -= 360.0F;
               }
            }

            this.updateCounter++;
            if (!((double)this.updateCounter < this.updateInterval.getValue())) {
               this.updateCounter = 0;

               for (PlayerEntity player : Wrapper.mc.world.getPlayers()) {
                  if ((!this.onlySelf.getValue() || player == Wrapper.mc.player) && !player.isSpectator()) {
                     PlayerTrail.PlayerTrailData trailData = this.getOrCreateTrailData(player);
                     float heightOffset = this.yOffset.getValueFloat();
                     Vec3d currentPos = new Vec3d(player.getX(), player.getY() + (double)(player.getEyeHeight(player.getPose()) * heightOffset), player.getZ());
                     if (trailData.points.isEmpty()
                        || trailData.points.get(trailData.points.size() - 1).position.distanceTo(currentPos) >= (double)this.minStep.getValueFloat()) {
                        Color pointColor = this.getTrailColor();
                        trailData.points.add(new PlayerTrail.TrailPoint(currentPos, pointColor, (int)this.trailLength.getValue()));

                        while ((double)trailData.points.size() > this.trailLength.getValue()) {
                           trailData.points.remove(0);
                        }
                     }
                  }
               }

               this.trailDataList.forEach(data -> data.points.removeIf(PlayerTrail.TrailPoint::shouldRemove));
            }
         }
      }
   }

   @Override
   public void onRender3D(MatrixStack matrixStack) {
      if (this.isOn()) {
         if (Wrapper.mc.world != null && Wrapper.mc.player != null) {
            if (!this.trailDataList.isEmpty()) {
               net.minecraft.client.render.Camera camera = Wrapper.mc.gameRenderer.getCamera();
               Vec3d camPos = camera.getPos();
               if (this.cuteMode.getValue()) {
                  this.renderCuteMode(matrixStack, camPos);
               } else {
                  this.renderNormalMode(matrixStack, camPos);
               }
            }
         }
      }
   }

   private Color getTrailColor() {
      if (this.cuteMode.getValue()) {
         float hue = this.rainbowHue / 360.0F;
         return Color.getHSBColor(hue, 0.8F, 1.0F);
      } else {
         return this.trailColor.getValue();
      }
   }

   private Color applyAlpha(Color c, float alpha) {
      int a = MathHelper.clamp((int)((float)c.getAlpha() * alpha), 0, 255);
      return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
   }

   private void renderNormalMode(MatrixStack matrixStack, Vec3d camPos) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
      Matrix4f matrix = matrixStack.peek().getPositionMatrix();
      float width = this.lineWidth.getValueFloat();
      float height = this.lineHeight.getValueFloat();

      for (PlayerTrail.PlayerTrailData trailData : this.trailDataList) {
         if ((!this.onlySelf.getValue() || trailData.player == Wrapper.mc.player) && trailData.points.size() >= 2) {
            for (PlayerTrail.TrailPoint point : trailData.points) {
               Vec3d relativePos = point.position.subtract(camPos);
               float alpha = point.getAlpha();
               Color c = this.applyAlpha(point.color, alpha);
               buffer.vertex(matrix, (float)relativePos.x, (float)relativePos.y, (float)relativePos.z)
                  .color((float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getBlue() / 255.0F, (float)c.getAlpha() / 255.0F);
               buffer.vertex(matrix, (float)relativePos.x, (float)(relativePos.y + (double)height), (float)relativePos.z)
                  .color((float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getBlue() / 255.0F, (float)c.getAlpha() / 255.0F);
            }
         }
      }

      BufferRenderer.drawWithGlobalProgram(buffer.end());
      RenderSystem.enableDepthTest();
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }

   private void renderCuteMode(MatrixStack matrixStack, Vec3d camPos) {
      Color[] colors = new Color[]{
         new Color(69, 221, 255, 180),
         new Color(248, 139, 160, 180),
         new Color(255, 255, 255, 180),
         new Color(248, 139, 160, 180),
         new Color(69, 221, 255, 180)
      };
      float step = this.cuteStep.getValueFloat();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);

      for (int layer = 0; layer < 5; layer++) {
         this.renderCuteLayer(matrixStack, camPos, colors[layer], (float)layer * step, step);
      }

      RenderSystem.enableDepthTest();
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }

   private void renderCuteLayer(MatrixStack matrixStack, Vec3d camPos, Color color, float layerYOffset, float layerHeight) {
      boolean hasData = false;

      for (PlayerTrail.PlayerTrailData trailData : this.trailDataList) {
         if ((!this.onlySelf.getValue() || trailData.player == Wrapper.mc.player) && trailData.points.size() >= 2) {
            hasData = true;
            break;
         }
      }

      if (hasData) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         RenderSystem.disableDepthTest();
         RenderSystem.setShader(GameRenderer::getPositionColorProgram);
         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
         Matrix4f matrix = matrixStack.peek().getPositionMatrix();
         boolean vertexCountBefore = false;

         for (PlayerTrail.PlayerTrailData trailDatax : this.trailDataList) {
            if ((!this.onlySelf.getValue() || trailDatax.player == Wrapper.mc.player) && trailDatax.points.size() >= 2) {
               for (int i = 0; i < trailDatax.points.size(); i++) {
                  PlayerTrail.TrailPoint point = trailDatax.points.get(i);
                  Vec3d relativePos = point.position.subtract(camPos);
                  float alpha = point.getAlpha();
                  Color c = this.applyAlpha(color, alpha * 0.8F);
                  buffer.vertex(matrix, (float)relativePos.x, (float)(relativePos.y + (double)layerYOffset), (float)relativePos.z)
                     .color((float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getBlue() / 255.0F, (float)c.getAlpha() / 255.0F);
                  buffer.vertex(matrix, (float)relativePos.x, (float)(relativePos.y + (double)layerYOffset + (double)layerHeight), (float)relativePos.z)
                     .color((float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getBlue() / 255.0F, (float)c.getAlpha() / 255.0F);
               }
            }
         }

         try {
            BufferRenderer.drawWithGlobalProgram(buffer.end());
         } catch (IllegalStateException var17) {
            if (var17.getMessage() == null || !var17.getMessage().contains("BufferBuilder was empty")) {
               throw var17;
            }
         }
      }
   }

   private PlayerTrail.PlayerTrailData getOrCreateTrailData(PlayerEntity player) {
      for (PlayerTrail.PlayerTrailData data : this.trailDataList) {
         if (data.player == player) {
            return data;
         }
      }

      PlayerTrail.PlayerTrailData newData = new PlayerTrail.PlayerTrailData(player);
      this.trailDataList.add(newData);
      return newData;
   }

   private static class PlayerTrailData {
      public final PlayerEntity player;
      public final List<PlayerTrail.TrailPoint> points;

      public PlayerTrailData(PlayerEntity player) {
         this.player = player;
         this.points = new ArrayList<>();
      }
   }

   private static class TrailPoint {
      public final Vec3d position;
      public final Color color;
      public int life;
      public int prevLife;
      public final int maxLife;

      public TrailPoint(Vec3d position, Color color, int maxLife) {
         this.position = position;
         this.color = color;
         this.life = maxLife;
         this.prevLife = maxLife;
         this.maxLife = maxLife;
      }

      public boolean shouldRemove() {
         this.prevLife = this.life--;
         return this.life <= 0;
      }

      public float getAlpha() {
         float factor = (float)this.life / (float)this.maxLife;
         return factor * factor;
      }
   }
}
