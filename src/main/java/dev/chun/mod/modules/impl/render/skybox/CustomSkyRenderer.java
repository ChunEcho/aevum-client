package dev.chun.mod.modules.impl.render.skybox;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.chun.obf.zkm.NoFlowObfuscate;
import dev.chun.obf.zkm.NoReferencesObfuscate;
import java.awt.Color;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry.SkyRenderer;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.gl.VertexBuffer.Usage;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@NoFlowObfuscate
@NoReferencesObfuscate
public class CustomSkyRenderer implements SkyRenderer {
   private static final Identifier NEBULA_1 = Identifier.of("better_sky", "textures/sky/nebula_2.png");
   private static final Identifier NEBULA_2 = Identifier.of("better_sky", "textures/sky/nebula_3.png");
   private static final Identifier HORIZON = Identifier.of("better_sky", "textures/sky/nebula_1.png");
   private static final Identifier STARS = Identifier.of("better_sky", "textures/sky/stars.png");
   private static final Identifier FOG = Identifier.of("better_sky", "textures/sky/fog.png");
   private VertexBuffer nebula1;
   private VertexBuffer nebula2;
   private VertexBuffer horizon;
   private VertexBuffer stars1;
   private VertexBuffer stars2;
   private VertexBuffer stars3;
   private VertexBuffer stars4;
   private VertexBuffer fog;
   private Vector3f axis1;
   private Vector3f axis2;
   private Vector3f axis3;
   private Vector3f axis4;
   private boolean initialised;

   public CustomSkyRenderer() {
   }

   private void initialise() {
      if (!this.initialised) {
         this.initStars();
         CheckedRandom random = new CheckedRandom(131L);
         this.axis1 = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
         this.axis2 = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
         this.axis3 = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
         this.axis4 = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
         this.axis1.normalize();
         this.axis2.normalize();
         this.axis3.normalize();
         this.axis4.normalize();
         this.initialised = true;
      }
   }

   public void render(WorldRenderContext context) {
      if (context.world() != null) {
         this.render(context.world(), context.tickCounter().getTickDelta(false), context.matrixStack(), context.positionMatrix(), context.projectionMatrix());
      }
   }

   public void render(World world, float tickDelta, Matrix4f positionMatrix, Matrix4f projectionMatrix) {
      this.render(world, tickDelta, null, positionMatrix, projectionMatrix);
   }

   private void render(World world, float tickDelta, MatrixStack inputMatrices, Matrix4f positionMatrix, Matrix4f projectionMatrix) {
      Skybox skybox = Skybox.INSTANCE;
      if (skybox != null && skybox.isOn()) {
         this.initialise();
         MatrixStack matrices = inputMatrices;
         if (inputMatrices == null) {
            matrices = new MatrixStack();
            matrices.multiplyPositionMatrix(positionMatrix);
         }

         float time = ((float)world.getTimeOfDay() + tickDelta) % 360000.0F * 1.7453292E-5F;
         float time2 = time * 2.0F;
         float time3 = time * 3.0F;
         BackgroundRenderer.applyFogColor();
         RenderSystem.enableBlend();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
         float blindA = 1.0F - BackgroundInfo.blindness;
         float blind02 = blindA * 0.2F;
         float blind06 = blindA * 0.6F;
         Color horizonColor = skybox.horizonColor.getValue();
         Color nebula1Color = skybox.nebula1Color.getValue();
         Color nebula2Color = skybox.nebula2Color.getValue();
         Color uvStars1Color = skybox.uvStars1Color.getValue();
         Color uvStars2Color = skybox.uvStars2Color.getValue();
         Color stars1Color = skybox.stars1Color.getValue();
         Color stars2Color = skybox.stars2Color.getValue();
         if (blindA > 0.0F) {
            matrices.push();
            matrices.multiply(new Quaternionf().rotationXYZ(0.0F, time, 0.0F));
            RenderSystem.setShaderTexture(0, HORIZON);
            this.renderBuffer(
               matrices,
               projectionMatrix,
               this.horizon,
               VertexFormats.POSITION_TEXTURE,
               (float)horizonColor.getRed() / 255.0F,
               (float)horizonColor.getGreen() / 255.0F,
               (float)horizonColor.getBlue() / 255.0F,
               0.7F * blindA
            );
            matrices.pop();
            matrices.push();
            matrices.multiply(new Quaternionf().rotationXYZ(0.0F, -time, 0.0F));
            RenderSystem.setShaderTexture(0, NEBULA_1);
            this.renderBuffer(
               matrices,
               projectionMatrix,
               this.nebula1,
               VertexFormats.POSITION_TEXTURE,
               (float)nebula1Color.getRed() / 255.0F,
               (float)nebula1Color.getGreen() / 255.0F,
               (float)nebula1Color.getBlue() / 255.0F,
               blind02
            );
            matrices.pop();
            matrices.push();
            matrices.multiply(new Quaternionf().rotationXYZ(0.0F, time2, 0.0F));
            RenderSystem.setShaderTexture(0, NEBULA_2);
            this.renderBuffer(
               matrices,
               projectionMatrix,
               this.nebula2,
               VertexFormats.POSITION_TEXTURE,
               (float)nebula2Color.getRed() / 255.0F,
               (float)nebula2Color.getGreen() / 255.0F,
               (float)nebula2Color.getBlue() / 255.0F,
               blind02
            );
            matrices.pop();
            if (skybox.stars.getValue()) {
               RenderSystem.setShaderTexture(0, STARS);
               matrices.push();
               matrices.multiply(new Quaternionf().setAngleAxis(time, this.axis3.x, this.axis3.y, this.axis3.z));
               this.renderBuffer(
                  matrices,
                  projectionMatrix,
                  this.stars3,
                  VertexFormats.POSITION_TEXTURE,
                  (float)uvStars1Color.getRed() / 255.0F,
                  (float)uvStars1Color.getGreen() / 255.0F,
                  (float)uvStars1Color.getBlue() / 255.0F,
                  blind06
               );
               matrices.pop();
               matrices.push();
               matrices.multiply(new Quaternionf().setAngleAxis(time2, this.axis4.x, this.axis4.y, this.axis4.z));
               this.renderBuffer(
                  matrices,
                  projectionMatrix,
                  this.stars4,
                  VertexFormats.POSITION_TEXTURE,
                  (float)uvStars2Color.getRed() / 255.0F,
                  (float)uvStars2Color.getGreen() / 255.0F,
                  (float)uvStars2Color.getBlue() / 255.0F,
                  blind06
               );
               matrices.pop();
            }
         }

         float a;
         if ((a = BackgroundInfo.fogDensity - 1.0F) > 0.0F) {
            if (a > 1.0F) {
               a = 1.0F;
            }

            RenderSystem.setShaderTexture(0, FOG);
            this.renderBuffer(
               matrices,
               projectionMatrix,
               this.fog,
               VertexFormats.POSITION_TEXTURE,
               BackgroundInfo.fogColorRed,
               BackgroundInfo.fogColorGreen,
               BackgroundInfo.fogColorBlue,
               a
            );
         }

         if (blindA > 0.0F && skybox.stars.getValue()) {
            matrices.push();
            matrices.multiply(new Quaternionf().setAngleAxis(time3, this.axis1.x, this.axis1.y, this.axis1.z));
            this.renderBuffer(
               matrices,
               projectionMatrix,
               this.stars1,
               VertexFormats.POSITION,
               (float)stars1Color.getRed() / 255.0F,
               (float)stars1Color.getGreen() / 255.0F,
               (float)stars1Color.getBlue() / 255.0F,
               blind06
            );
            matrices.pop();
            matrices.push();
            matrices.multiply(new Quaternionf().setAngleAxis(time2, this.axis2.x, this.axis2.y, this.axis2.z));
            this.renderBuffer(
               matrices,
               projectionMatrix,
               this.stars2,
               VertexFormats.POSITION,
               (float)stars2Color.getRed() / 255.0F,
               (float)stars2Color.getGreen() / 255.0F,
               (float)stars2Color.getBlue() / 255.0F,
               blind06
            );
            matrices.pop();
         }

         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   private void renderBuffer(MatrixStack matrices, Matrix4f matrix4f, VertexBuffer buffer, VertexFormat format, float r, float g, float b, float a) {
      RenderSystem.setShaderColor(r, g, b, a);
      buffer.bind();
      if (format == VertexFormats.POSITION) {
         buffer.draw(matrices.peek().getPositionMatrix(), matrix4f, GameRenderer.getPositionProgram());
      } else {
         buffer.draw(matrices.peek().getPositionMatrix(), matrix4f, GameRenderer.getPositionTexProgram());
      }

      VertexBuffer.unbind();
   }

   private void initStars() {
      Tessellator tessellator = Tessellator.getInstance();
      this.stars1 = this.buildBuffer(tessellator, this.stars1, 0.1F, 0.3F, 3500, 41315L, this::makeStars);
      this.stars2 = this.buildBuffer(tessellator, this.stars2, 0.1F, 0.35F, 2000, 35151L, this::makeStars);
      this.stars3 = this.buildBuffer(tessellator, this.stars3, 0.4F, 1.2F, 1000, 61354L, this::makeUVStars);
      this.stars4 = this.buildBuffer(tessellator, this.stars4, 0.4F, 1.2F, 1000, 61355L, this::makeUVStars);
      this.nebula1 = this.buildBuffer(tessellator, this.nebula1, 40.0F, 60.0F, 30, 11515L, this::makeFarFog);
      this.nebula2 = this.buildBuffer(tessellator, this.nebula2, 40.0F, 60.0F, 10, 14151L, this::makeFarFog);
      this.horizon = this.buildBufferHorizon(tessellator, this.horizon);
      this.fog = this.buildBufferFog(tessellator, this.fog);
   }

   private VertexBuffer buildBuffer(
      Tessellator tessellator, VertexBuffer vertexBuffer, float minSize, float maxSize, int count, long seed, CustomSkyRenderer.BufferFunction fkt
   ) {
      if (vertexBuffer != null) {
         vertexBuffer.close();
      }

      vertexBuffer = new VertexBuffer(Usage.STATIC);
      BufferBuilder bufferBuilder = fkt.make(tessellator, minSize, maxSize, count, seed);
      BuiltBuffer builtBuffer = bufferBuilder.end();
      vertexBuffer.bind();
      vertexBuffer.upload(builtBuffer);
      return vertexBuffer;
   }

   private VertexBuffer buildBufferHorizon(Tessellator tessellator, VertexBuffer buffer) {
      return this.buildBuffer(
         tessellator, buffer, 0.0F, 0.0F, 0, 0L, (_builder, _minSize, _maxSize, _count, _seed) -> this.makeCylinder(_builder, 16, 50.0F, 100.0F)
      );
   }

   private VertexBuffer buildBufferFog(Tessellator tessellator, VertexBuffer buffer) {
      return this.buildBuffer(
         tessellator, buffer, 0.0F, 0.0F, 0, 0L, (_builder, _minSize, _maxSize, _count, _seed) -> this.makeCylinder(_builder, 16, 50.0F, 70.0F)
      );
   }

   private BufferBuilder makeStars(Tessellator tessellator, float minSize, float maxSize, int count, long seed) {
      CheckedRandom random = new CheckedRandom(seed);
      RenderSystem.setShader(GameRenderer::getPositionProgram);
      BufferBuilder buffer = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION);

      for (int i = 0; i < count; i++) {
         float posX = random.nextFloat() * 2.0F - 1.0F;
         float posY = random.nextFloat() * 2.0F - 1.0F;
         float posZ = random.nextFloat() * 2.0F - 1.0F;
         float size = this.randRange(minSize, maxSize, random);
         float length = posX * posX + posY * posY + posZ * posZ;
         if (length < 1.0F && length > 0.001F) {
            length = 1.0F / (float)Math.sqrt((double)length);
            float var34;
            float px = (var34 = posX * length) * 100.0F;
            float var35;
            float py = (var35 = posY * length) * 100.0F;
            float var36;
            float pz = (var36 = posZ * length) * 100.0F;
            float angle = (float)Math.atan2((double)var34, (double)var36);
            float sin1 = (float)Math.sin((double)angle);
            float cos1 = (float)Math.cos((double)angle);
            angle = (float)Math.atan2((double)((float)Math.sqrt((double)(var34 * var34 + var36 * var36))), (double)var35);
            float sin2 = (float)Math.sin((double)angle);
            float cos2 = (float)Math.cos((double)angle);
            angle = random.nextFloat() * (float) Math.PI * 2.0F;
            float sin3 = (float)Math.sin((double)angle);
            float cos3 = (float)Math.cos((double)angle);

            for (int index = 0; index < 4; index++) {
               float x = (float)((index & 2) - 1) * size;
               float y = (float)((index + 1 & 2) - 1) * size;
               float aa = x * cos3 - y * sin3;
               float ab = y * cos3 + x * sin3;
               float dy = aa * sin2 + 0.0F * cos2;
               float ae = 0.0F * sin2 - aa * cos2;
               float dx = ae * sin1 - ab * cos1;
               float dz = ab * sin1 + ae * cos1;
               buffer.vertex(px + dx, py + dy, pz + dz);
            }
         }
      }

      return buffer;
   }

   private BufferBuilder makeUVStars(Tessellator tessellator, float minSize, float maxSize, int count, long seed) {
      CheckedRandom random = new CheckedRandom(seed);
      RenderSystem.setShader(GameRenderer::getPositionTexProgram);
      BufferBuilder buffer = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

      for (int i = 0; i < count; i++) {
         float posX = random.nextFloat() * 2.0F - 1.0F;
         float posY = random.nextFloat() * 2.0F - 1.0F;
         float posZ = random.nextFloat() * 2.0F - 1.0F;
         float size = this.randRange(minSize, maxSize, random);
         float length = posX * posX + posY * posY + posZ * posZ;
         if (length < 1.0F && length > 0.001F) {
            length = 1.0F / (float)Math.sqrt((double)length);
            float var37;
            float px = (var37 = posX * length) * 100.0F;
            float var38;
            float py = (var38 = posY * length) * 100.0F;
            float var39;
            float pz = (var39 = posZ * length) * 100.0F;
            float angle = (float)Math.atan2((double)var37, (double)var39);
            float sin1 = (float)Math.sin((double)angle);
            float cos1 = (float)Math.cos((double)angle);
            angle = (float)Math.atan2((double)((float)Math.sqrt((double)(var37 * var37 + var39 * var39))), (double)var38);
            float sin2 = (float)Math.sin((double)angle);
            float cos2 = (float)Math.cos((double)angle);
            angle = random.nextFloat() * (float) Math.PI * 2.0F;
            float sin3 = (float)Math.sin((double)angle);
            float cos3 = (float)Math.cos((double)angle);
            float minV = (float)random.nextInt(4) / 4.0F;

            for (int index = 0; index < 4; index++) {
               float x = (float)((index & 2) - 1) * size;
               float y = (float)((index + 1 & 2) - 1) * size;
               float aa = x * cos3 - y * sin3;
               float ab = y * cos3 + x * sin3;
               float dy = aa * sin2 + 0.0F * cos2;
               float ae = 0.0F * sin2 - aa * cos2;
               float dx = ae * sin1 - ab * cos1;
               float dz = ab * sin1 + ae * cos1;
               float texU = (float)(index >> 1 & 1);
               float texV = (float)(index + 1 >> 1 & 1) / 4.0F + minV;
               buffer.vertex(px + dx, py + dy, pz + dz).texture(texU, texV);
            }
         }
      }

      return buffer;
   }

   private BufferBuilder makeFarFog(Tessellator tessellator, float minSize, float maxSize, int count, long seed) {
      CheckedRandom random = new CheckedRandom(seed);
      RenderSystem.setShader(GameRenderer::getPositionTexProgram);
      BufferBuilder buffer = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

      for (int i = 0; i < count; i++) {
         float posX = random.nextFloat() * 2.0F - 1.0F;
         float posY = random.nextFloat() - 0.5F;
         float posZ = random.nextFloat() * 2.0F - 1.0F;
         float size = this.randRange(minSize, maxSize, random);
         float length = posX * posX + posY * posY + posZ * posZ;
         float distance = 2.0F;
         if (length < 1.0F && length > 0.001F) {
            length = distance / (float)Math.sqrt((double)length);
            size *= distance;
            float var37;
            float px = (var37 = posX * length) * 100.0F;
            float var38;
            float py = (var38 = posY * length) * 100.0F;
            float var39;
            float pz = (var39 = posZ * length) * 100.0F;
            float angle = (float)Math.atan2((double)var37, (double)var39);
            float sin1 = (float)Math.sin((double)angle);
            float cos1 = (float)Math.cos((double)angle);
            angle = (float)Math.atan2((double)((float)Math.sqrt((double)(var37 * var37 + var39 * var39))), (double)var38);
            float sin2 = (float)Math.sin((double)angle);
            float cos2 = (float)Math.cos((double)angle);
            angle = random.nextFloat() * (float) Math.PI * 2.0F;
            float sin3 = (float)Math.sin((double)angle);
            float cos3 = (float)Math.cos((double)angle);

            for (int index = 0; index < 4; index++) {
               float x = (float)((index & 2) - 1) * size;
               float y = (float)((index + 1 & 2) - 1) * size;
               float aa = x * cos3 - y * sin3;
               float ab = y * cos3 + x * sin3;
               float dy = aa * sin2 + 0.0F * cos2;
               float ae = 0.0F * sin2 - aa * cos2;
               float dx = ae * sin1 - ab * cos1;
               float dz = ab * sin1 + ae * cos1;
               float texU = (float)(index >> 1 & 1);
               float texV = (float)(index + 1 >> 1 & 1);
               buffer.vertex(px + dx, py + dy, pz + dz).texture(texU, texV);
            }
         }
      }

      return buffer;
   }

   private BufferBuilder makeCylinder(Tessellator tessellator, int segments, float height, float radius) {
      RenderSystem.setShader(GameRenderer::getPositionTexProgram);
      BufferBuilder buffer = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

      for (int i = 0; i < segments; i++) {
         float a1 = (float)i * (float) Math.PI * 2.0F / (float)segments;
         float a2 = (float)(i + 1) * (float) Math.PI * 2.0F / (float)segments;
         float px1 = (float)Math.sin((double)a1) * radius;
         float pz1 = (float)Math.cos((double)a1) * radius;
         float px2 = (float)Math.sin((double)a2) * radius;
         float pz2 = (float)Math.cos((double)a2) * radius;
         float u0 = (float)i / (float)segments;
         float u1 = (float)(i + 1) / (float)segments;
         buffer.vertex(px1, -height, pz1).texture(u0, 0.0F);
         buffer.vertex(px1, height, pz1).texture(u0, 1.0F);
         buffer.vertex(px2, height, pz2).texture(u1, 1.0F);
         buffer.vertex(px2, -height, pz2).texture(u1, 0.0F);
      }

      return buffer;
   }

   private float randRange(float min, float max, CheckedRandom random) {
      return min + random.nextFloat() * (max - min);
   }

   @FunctionalInterface
   interface BufferFunction {
      BufferBuilder make(Tessellator var1, float var2, float var3, int var4, long var5);
   }
}
