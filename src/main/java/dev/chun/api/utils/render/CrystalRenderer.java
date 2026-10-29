package dev.chun.api.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.chun.api.events.impl.Render3DEvent;
import dev.chun.mod.modules.impl.render.Chams;
import java.awt.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

public class CrystalRenderer {
   private static final float N = -0.25F;
   private static final float P = 0.25F;
   private static final float[][] CUBE_VERTICES = new float[][]{
      {-0.25F, -0.25F, -0.25F},
      {0.25F, -0.25F, -0.25F},
      {0.25F, 0.25F, -0.25F},
      {-0.25F, 0.25F, -0.25F},
      {-0.25F, -0.25F, 0.25F},
      {0.25F, -0.25F, 0.25F},
      {0.25F, 0.25F, 0.25F},
      {-0.25F, 0.25F, 0.25F}
   };
   private static final int[][] CUBE_FACES = new int[][]{
      {0, 3, 2}, {0, 2, 1}, {4, 5, 6}, {4, 6, 7}, {0, 4, 7}, {0, 7, 3}, {1, 2, 6}, {1, 6, 5}, {3, 7, 6}, {3, 6, 2}, {0, 1, 5}, {0, 5, 4}
   };
   private static final int[][] CUBE_EDGES = new int[][]{{0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};

   public CrystalRenderer() {
   }

   public static void renderCrystal(Render3DEvent event, EndCrystalEntity entity, Chams module) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.gameRenderer != null) {
         MatrixStack matrices = event.matrixStack;
         float tickDelta = event.tickDelta;
         Camera camera = mc.gameRenderer.getCamera();
         Vec3d camPos = camera.getPos();
         float age = (float)entity.endCrystalAge + tickDelta;
         float g = MathHelper.sin(age * 0.2F) / 2.0F + 0.5F;
         g = (g * g + g) * 0.4F;
         float bounceOffset = g - 1.4F;
         float rotY = age * 3.0F;
         double renderX = MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX()) - camPos.x;
         double renderY = MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY()) - camPos.y;
         double renderZ = MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ()) - camPos.z;
         matrices.push();
         matrices.translate(renderX, renderY, renderZ);
         float scale = 2.0F * 1.0F;
         matrices.scale(scale, scale, scale);
         matrices.translate(0.0F, -0.5F, 0.0F);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotY));
         matrices.translate(0.0F, 1.5F + bounceOffset / 2.0F, 0.0F);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         RenderSystem.disableCull();
         float prevLineWidth = GL11.glGetFloat(2849);
         GL11.glLineWidth(1.5F);
         float sine45 = (float)Math.sin(Math.PI / 4);
         Quaternionf tilt = new Quaternionf().setAngleAxis((float) (Math.PI / 3), sine45, 0.0F, sine45);
         Color fill = module.fill.getValue();
         Color outline = module.line.getValue();
         matrices.multiply(tilt);
         renderCube(matrices.peek().getPositionMatrix(), fill, outline);
         matrices.scale(0.875F, 0.875F, 0.875F);
         matrices.multiply(tilt);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotY));
         renderCube(matrices.peek().getPositionMatrix(), fill, outline);
         matrices.scale(0.875F, 0.875F, 0.875F);
         matrices.multiply(tilt);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotY));
         renderCube(matrices.peek().getPositionMatrix(), fill, outline);
         GL11.glLineWidth(prevLineWidth);
         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
         matrices.pop();
      }
   }

   private static void renderCube(Matrix4f matrix, Color fill, Color outline) {
      drawCubeFill(matrix, fill);
      drawCubeOutline(matrix, outline);
   }

   private static void drawCubeFill(Matrix4f matrix, Color color) {
      float r = (float)color.getRed() / 255.0F;
      float g = (float)color.getGreen() / 255.0F;
      float b = (float)color.getBlue() / 255.0F;
      float a = (float)color.getAlpha() / 255.0F;
      Tessellator tess = Tessellator.getInstance();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      BufferBuilder buf = tess.begin(DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

      for (int[] face : CUBE_FACES) {
         float[] v0 = CUBE_VERTICES[face[0]];
         float[] v1 = CUBE_VERTICES[face[1]];
         float[] v2 = CUBE_VERTICES[face[2]];
         buf.vertex(matrix, v0[0], v0[1], v0[2]).color(r, g, b, a);
         buf.vertex(matrix, v1[0], v1[1], v1[2]).color(r, g, b, a);
         buf.vertex(matrix, v2[0], v2[1], v2[2]).color(r, g, b, a);
      }

      BufferRenderer.drawWithGlobalProgram(buf.end());
   }

   private static void drawCubeOutline(Matrix4f matrix, Color color) {
      float r = (float)color.getRed() / 255.0F;
      float g = (float)color.getGreen() / 255.0F;
      float b = (float)color.getBlue() / 255.0F;
      float a = (float)color.getAlpha() / 255.0F;
      Tessellator tess = Tessellator.getInstance();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      BufferBuilder buf = tess.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

      for (int[] edge : CUBE_EDGES) {
         float[] v1 = CUBE_VERTICES[edge[0]];
         float[] v2 = CUBE_VERTICES[edge[1]];
         buf.vertex(matrix, v1[0], v1[1], v1[2]).color(r, g, b, a);
         buf.vertex(matrix, v2[0], v2[1], v2[2]).color(r, g, b, a);
      }

      BufferRenderer.drawWithGlobalProgram(buf.end());
   }
}
