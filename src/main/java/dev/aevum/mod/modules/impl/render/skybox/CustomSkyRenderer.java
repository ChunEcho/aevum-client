/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry$SkyRenderer
 *  net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
 *  net.minecraft.Level
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tesselator
 *  net.minecraft.DefaultVertexFormat
 *  net.minecraft.VertexBuffer
 *  net.minecraft.VertexBuffer$Usage
 *  net.minecraft.VertexFormat
 *  net.minecraft.VertexFormat$Mode
 *  net.minecraft.ResourceLocation
 *  net.minecraft.PoseStack
 *  net.minecraft.LegacyRandomSource
 *  net.minecraft.GameRenderer
 *  net.minecraft.FogRenderer
 *  net.minecraft.MeshData
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 */
package dev.aevum.mod.modules.impl.render.skybox;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.mod.modules.impl.render.skybox.BackgroundInfo;
import dev.aevum.mod.modules.impl.render.skybox.Skybox;
import dev.aevum.obf.zkm.NoFlowObfuscate;
import dev.aevum.obf.zkm.NoReferencesObfuscate;
import java.awt.Color;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.Level;
import net.minecraft.BufferBuilder;
import net.minecraft.Tesselator;
import net.minecraft.DefaultVertexFormat;
import net.minecraft.VertexBuffer;
import net.minecraft.VertexFormat;
import net.minecraft.ResourceLocation;
import net.minecraft.PoseStack;
import net.minecraft.LegacyRandomSource;
import net.minecraft.GameRenderer;
import net.minecraft.FogRenderer;
import net.minecraft.MeshData;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@NoFlowObfuscate
@NoReferencesObfuscate
public class CustomSkyRenderer
implements DimensionRenderingRegistry.SkyRenderer {
    private static final ResourceLocation NEBULA_1 = ResourceLocation.method_60655((String)"better_sky", (String)"textures/sky/nebula_2.png");
    private static final ResourceLocation NEBULA_2 = ResourceLocation.method_60655((String)"better_sky", (String)"textures/sky/nebula_3.png");
    private static final ResourceLocation HORIZON = ResourceLocation.method_60655((String)"better_sky", (String)"textures/sky/nebula_1.png");
    private static final ResourceLocation STARS = ResourceLocation.method_60655((String)"better_sky", (String)"textures/sky/stars.png");
    private static final ResourceLocation FOG = ResourceLocation.method_60655((String)"better_sky", (String)"textures/sky/fog.png");
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

    private void initialise() {
        if (!this.initialised) {
            this.initStars();
            LegacyRandomSource random = new LegacyRandomSource(131L);
            this.axis1 = new Vector3f(random.method_43057(), random.method_43057(), random.method_43057());
            this.axis2 = new Vector3f(random.method_43057(), random.method_43057(), random.method_43057());
            this.axis3 = new Vector3f(random.method_43057(), random.method_43057(), random.method_43057());
            this.axis4 = new Vector3f(random.method_43057(), random.method_43057(), random.method_43057());
            this.axis1.normalize();
            this.axis2.normalize();
            this.axis3.normalize();
            this.axis4.normalize();
            this.initialised = true;
        }
    }

    public void render(WorldRenderContext context) {
        if (context.world() == null) {
            return;
        }
        this.render((Level)context.world(), context.tickCounter().method_60637(false), context.matrixStack(), context.positionMatrix(), context.projectionMatrix());
    }

    public void render(Level world, float tickDelta, Matrix4f positionMatrix, Matrix4f projectionMatrix) {
        this.render(world, tickDelta, null, positionMatrix, projectionMatrix);
    }

    private void render(Level world, float tickDelta, PoseStack inputMatrices, Matrix4f positionMatrix, Matrix4f projectionMatrix) {
        float f;
        Skybox skybox = Skybox.INSTANCE;
        if (skybox == null || !skybox.isOn()) {
            return;
        }
        this.initialise();
        PoseStack matrices = inputMatrices;
        if (matrices == null) {
            matrices = new PoseStack();
            matrices.method_34425(positionMatrix);
        }
        float time = ((float)world.getDayTime() + tickDelta) % 360000.0f * 1.7453292E-5f;
        float time2 = time * 2.0f;
        float time3 = time * 3.0f;
        FogRenderer.levelFogColor();
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.blendFunc((GlStateManager.class_4535)GlStateManager.class_4535.SRC_ALPHA, (GlStateManager.class_4534)GlStateManager.class_4534.ONE_MINUS_SRC_ALPHA);
        float blindA = 1.0f - BackgroundInfo.blindness;
        float blind02 = blindA * 0.2f;
        float blind06 = blindA * 0.6f;
        Color horizonColor = skybox.horizonColor.getValue();
        Color nebula1Color = skybox.nebula1Color.getValue();
        Color nebula2Color = skybox.nebula2Color.getValue();
        Color uvStars1Color = skybox.uvStars1Color.getValue();
        Color uvStars2Color = skybox.uvStars2Color.getValue();
        Color stars1Color = skybox.stars1Color.getValue();
        Color stars2Color = skybox.stars2Color.getValue();
        if (blindA > 0.0f) {
            matrices.method_22903();
            matrices.method_22907(new Quaternionf().rotationXYZ(0.0f, time, 0.0f));
            RenderSystem.setShaderTexture((int)0, (ResourceLocation)HORIZON);
            this.renderBuffer(matrices, projectionMatrix, this.horizon, DefaultVertexFormat.POSITION_TEX, (float)horizonColor.getRed() / 255.0f, (float)horizonColor.getGreen() / 255.0f, (float)horizonColor.getBlue() / 255.0f, 0.7f * blindA);
            matrices.method_22909();
            matrices.method_22903();
            matrices.method_22907(new Quaternionf().rotationXYZ(0.0f, -time, 0.0f));
            RenderSystem.setShaderTexture((int)0, (ResourceLocation)NEBULA_1);
            this.renderBuffer(matrices, projectionMatrix, this.nebula1, DefaultVertexFormat.POSITION_TEX, (float)nebula1Color.getRed() / 255.0f, (float)nebula1Color.getGreen() / 255.0f, (float)nebula1Color.getBlue() / 255.0f, blind02);
            matrices.method_22909();
            matrices.method_22903();
            matrices.method_22907(new Quaternionf().rotationXYZ(0.0f, time2, 0.0f));
            RenderSystem.setShaderTexture((int)0, (ResourceLocation)NEBULA_2);
            this.renderBuffer(matrices, projectionMatrix, this.nebula2, DefaultVertexFormat.POSITION_TEX, (float)nebula2Color.getRed() / 255.0f, (float)nebula2Color.getGreen() / 255.0f, (float)nebula2Color.getBlue() / 255.0f, blind02);
            matrices.method_22909();
            if (skybox.stars.getValue()) {
                RenderSystem.setShaderTexture((int)0, (ResourceLocation)STARS);
                matrices.method_22903();
                matrices.method_22907(new Quaternionf().setAngleAxis(time, this.axis3.x, this.axis3.y, this.axis3.z));
                this.renderBuffer(matrices, projectionMatrix, this.stars3, DefaultVertexFormat.POSITION_TEX, (float)uvStars1Color.getRed() / 255.0f, (float)uvStars1Color.getGreen() / 255.0f, (float)uvStars1Color.getBlue() / 255.0f, blind06);
                matrices.method_22909();
                matrices.method_22903();
                matrices.method_22907(new Quaternionf().setAngleAxis(time2, this.axis4.x, this.axis4.y, this.axis4.z));
                this.renderBuffer(matrices, projectionMatrix, this.stars4, DefaultVertexFormat.POSITION_TEX, (float)uvStars2Color.getRed() / 255.0f, (float)uvStars2Color.getGreen() / 255.0f, (float)uvStars2Color.getBlue() / 255.0f, blind06);
                matrices.method_22909();
            }
        }
        float a = BackgroundInfo.fogDensity - 1.0f;
        if (f > 0.0f) {
            if (a > 1.0f) {
                a = 1.0f;
            }
            RenderSystem.setShaderTexture((int)0, (ResourceLocation)FOG);
            this.renderBuffer(matrices, projectionMatrix, this.fog, DefaultVertexFormat.POSITION_TEX, BackgroundInfo.fogColorRed, BackgroundInfo.fogColorGreen, BackgroundInfo.fogColorBlue, a);
        }
        if (blindA > 0.0f && skybox.stars.getValue()) {
            matrices.method_22903();
            matrices.method_22907(new Quaternionf().setAngleAxis(time3, this.axis1.x, this.axis1.y, this.axis1.z));
            this.renderBuffer(matrices, projectionMatrix, this.stars1, DefaultVertexFormat.POSITION, (float)stars1Color.getRed() / 255.0f, (float)stars1Color.getGreen() / 255.0f, (float)stars1Color.getBlue() / 255.0f, blind06);
            matrices.method_22909();
            matrices.method_22903();
            matrices.method_22907(new Quaternionf().setAngleAxis(time2, this.axis2.x, this.axis2.y, this.axis2.z));
            this.renderBuffer(matrices, projectionMatrix, this.stars2, DefaultVertexFormat.POSITION, (float)stars2Color.getRed() / 255.0f, (float)stars2Color.getGreen() / 255.0f, (float)stars2Color.getBlue() / 255.0f, blind06);
            matrices.method_22909();
        }
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    private void renderBuffer(PoseStack matrices, Matrix4f matrix4f, VertexBuffer buffer, VertexFormat format, float r, float g, float b, float a) {
        RenderSystem.setShaderColor((float)r, (float)g, (float)b, (float)a);
        buffer.method_1353();
        if (format == DefaultVertexFormat.POSITION) {
            buffer.method_34427(matrices.last().method_23761(), matrix4f, GameRenderer.method_34539());
        } else {
            buffer.method_34427(matrices.last().method_23761(), matrix4f, GameRenderer.method_34542());
        }
        VertexBuffer.method_1354();
    }

    private void initStars() {
        Tesselator tessellator = Tesselator.getInstance();
        this.stars1 = this.buildBuffer(tessellator, this.stars1, 0.1f, 0.3f, 3500, 41315L, this::makeStars);
        this.stars2 = this.buildBuffer(tessellator, this.stars2, 0.1f, 0.35f, 2000, 35151L, this::makeStars);
        this.stars3 = this.buildBuffer(tessellator, this.stars3, 0.4f, 1.2f, 1000, 61354L, this::makeUVStars);
        this.stars4 = this.buildBuffer(tessellator, this.stars4, 0.4f, 1.2f, 1000, 61355L, this::makeUVStars);
        this.nebula1 = this.buildBuffer(tessellator, this.nebula1, 40.0f, 60.0f, 30, 11515L, this::makeFarFog);
        this.nebula2 = this.buildBuffer(tessellator, this.nebula2, 40.0f, 60.0f, 10, 14151L, this::makeFarFog);
        this.horizon = this.buildBufferHorizon(tessellator, this.horizon);
        this.fog = this.buildBufferFog(tessellator, this.fog);
    }

    private VertexBuffer buildBuffer(Tesselator tessellator, VertexBuffer vertexBuffer, float minSize, float maxSize, int count, long seed, BufferFunction fkt) {
        if (vertexBuffer != null) {
            vertexBuffer.close();
        }
        vertexBuffer = new VertexBuffer(VertexBuffer.VertexBuffer$Usage.STATIC);
        BufferBuilder bufferBuilder = fkt.make(tessellator, minSize, maxSize, count, seed);
        MeshData builtBuffer = bufferBuilder.method_60800();
        vertexBuffer.method_1353();
        vertexBuffer.method_1352(builtBuffer);
        return vertexBuffer;
    }

    private VertexBuffer buildBufferHorizon(Tesselator tessellator, VertexBuffer buffer) {
        return this.buildBuffer(tessellator, buffer, 0.0f, 0.0f, 0, 0L, (_builder, _minSize, _maxSize, _count, _seed) -> this.makeCylinder(_builder, 16, 50.0f, 100.0f));
    }

    private VertexBuffer buildBufferFog(Tesselator tessellator, VertexBuffer buffer) {
        return this.buildBuffer(tessellator, buffer, 0.0f, 0.0f, 0, 0L, (_builder, _minSize, _maxSize, _count, _seed) -> this.makeCylinder(_builder, 16, 50.0f, 70.0f));
    }

    private BufferBuilder makeStars(Tesselator tessellator, float minSize, float maxSize, int count, long seed) {
        LegacyRandomSource random = new LegacyRandomSource(seed);
        RenderSystem.setShader(GameRenderer::method_34539);
        BufferBuilder buffer = tessellator.method_60827(VertexFormat.VertexFormat$Mode.QUADS, DefaultVertexFormat.POSITION);
        for (int i = 0; i < count; ++i) {
            float posX = random.method_43057() * 2.0f - 1.0f;
            float posY = random.method_43057() * 2.0f - 1.0f;
            float posZ = random.method_43057() * 2.0f - 1.0f;
            float size = this.randRange(minSize, maxSize, random);
            float length = posX * posX + posY * posY + posZ * posZ;
            if (!(length < 1.0f) || !(length > 0.001f)) continue;
            length = 1.0f / (float)Math.sqrt(length);
            float px = (posX *= length) * 100.0f;
            float py = (posY *= length) * 100.0f;
            float pz = (posZ *= length) * 100.0f;
            float angle = (float)Math.atan2(posX, posZ);
            float sin1 = (float)Math.sin(angle);
            float cos1 = (float)Math.cos(angle);
            angle = (float)Math.atan2((float)Math.sqrt(posX * posX + posZ * posZ), posY);
            float sin2 = (float)Math.sin(angle);
            float cos2 = (float)Math.cos(angle);
            angle = random.method_43057() * (float)Math.PI * 2.0f;
            float sin3 = (float)Math.sin(angle);
            float cos3 = (float)Math.cos(angle);
            for (int index = 0; index < 4; ++index) {
                float x = (float)((index & 2) - 1) * size;
                float y = (float)((index + 1 & 2) - 1) * size;
                float aa = x * cos3 - y * sin3;
                float ab = y * cos3 + x * sin3;
                float dy = aa * sin2 + 0.0f * cos2;
                float ae = 0.0f * sin2 - aa * cos2;
                float dx = ae * sin1 - ab * cos1;
                float dz = ab * sin1 + ae * cos1;
                buffer.method_22912(px + dx, py + dy, pz + dz);
            }
        }
        return buffer;
    }

    private BufferBuilder makeUVStars(Tesselator tessellator, float minSize, float maxSize, int count, long seed) {
        LegacyRandomSource random = new LegacyRandomSource(seed);
        RenderSystem.setShader(GameRenderer::method_34542);
        BufferBuilder buffer = tessellator.method_60827(VertexFormat.VertexFormat$Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        for (int i = 0; i < count; ++i) {
            float posX = random.method_43057() * 2.0f - 1.0f;
            float posY = random.method_43057() * 2.0f - 1.0f;
            float posZ = random.method_43057() * 2.0f - 1.0f;
            float size = this.randRange(minSize, maxSize, random);
            float length = posX * posX + posY * posY + posZ * posZ;
            if (!(length < 1.0f) || !(length > 0.001f)) continue;
            length = 1.0f / (float)Math.sqrt(length);
            float px = (posX *= length) * 100.0f;
            float py = (posY *= length) * 100.0f;
            float pz = (posZ *= length) * 100.0f;
            float angle = (float)Math.atan2(posX, posZ);
            float sin1 = (float)Math.sin(angle);
            float cos1 = (float)Math.cos(angle);
            angle = (float)Math.atan2((float)Math.sqrt(posX * posX + posZ * posZ), posY);
            float sin2 = (float)Math.sin(angle);
            float cos2 = (float)Math.cos(angle);
            angle = random.method_43057() * (float)Math.PI * 2.0f;
            float sin3 = (float)Math.sin(angle);
            float cos3 = (float)Math.cos(angle);
            float minV = (float)random.method_43048(4) / 4.0f;
            for (int index = 0; index < 4; ++index) {
                float x = (float)((index & 2) - 1) * size;
                float y = (float)((index + 1 & 2) - 1) * size;
                float aa = x * cos3 - y * sin3;
                float ab = y * cos3 + x * sin3;
                float dy = aa * sin2 + 0.0f * cos2;
                float ae = 0.0f * sin2 - aa * cos2;
                float dx = ae * sin1 - ab * cos1;
                float dz = ab * sin1 + ae * cos1;
                float texU = index >> 1 & 1;
                float texV = (float)(index + 1 >> 1 & 1) / 4.0f + minV;
                buffer.method_22912(px + dx, py + dy, pz + dz).method_22913(texU, texV);
            }
        }
        return buffer;
    }

    private BufferBuilder makeFarFog(Tesselator tessellator, float minSize, float maxSize, int count, long seed) {
        LegacyRandomSource random = new LegacyRandomSource(seed);
        RenderSystem.setShader(GameRenderer::method_34542);
        BufferBuilder buffer = tessellator.method_60827(VertexFormat.VertexFormat$Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        for (int i = 0; i < count; ++i) {
            float posX = random.method_43057() * 2.0f - 1.0f;
            float posY = random.method_43057() - 0.5f;
            float posZ = random.method_43057() * 2.0f - 1.0f;
            float size = this.randRange(minSize, maxSize, random);
            float length = posX * posX + posY * posY + posZ * posZ;
            float distance = 2.0f;
            if (!(length < 1.0f) || !(length > 0.001f)) continue;
            length = distance / (float)Math.sqrt(length);
            size *= distance;
            float px = (posX *= length) * 100.0f;
            float py = (posY *= length) * 100.0f;
            float pz = (posZ *= length) * 100.0f;
            float angle = (float)Math.atan2(posX, posZ);
            float sin1 = (float)Math.sin(angle);
            float cos1 = (float)Math.cos(angle);
            angle = (float)Math.atan2((float)Math.sqrt(posX * posX + posZ * posZ), posY);
            float sin2 = (float)Math.sin(angle);
            float cos2 = (float)Math.cos(angle);
            angle = random.method_43057() * (float)Math.PI * 2.0f;
            float sin3 = (float)Math.sin(angle);
            float cos3 = (float)Math.cos(angle);
            for (int index = 0; index < 4; ++index) {
                float x = (float)((index & 2) - 1) * size;
                float y = (float)((index + 1 & 2) - 1) * size;
                float aa = x * cos3 - y * sin3;
                float ab = y * cos3 + x * sin3;
                float dy = aa * sin2 + 0.0f * cos2;
                float ae = 0.0f * sin2 - aa * cos2;
                float dx = ae * sin1 - ab * cos1;
                float dz = ab * sin1 + ae * cos1;
                float texU = index >> 1 & 1;
                float texV = index + 1 >> 1 & 1;
                buffer.method_22912(px + dx, py + dy, pz + dz).method_22913(texU, texV);
            }
        }
        return buffer;
    }

    private BufferBuilder makeCylinder(Tesselator tessellator, int segments, float height, float radius) {
        RenderSystem.setShader(GameRenderer::method_34542);
        BufferBuilder buffer = tessellator.method_60827(VertexFormat.VertexFormat$Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        for (int i = 0; i < segments; ++i) {
            float a1 = (float)i * (float)Math.PI * 2.0f / (float)segments;
            float a2 = (float)(i + 1) * (float)Math.PI * 2.0f / (float)segments;
            float px1 = (float)Math.sin(a1) * radius;
            float pz1 = (float)Math.cos(a1) * radius;
            float px2 = (float)Math.sin(a2) * radius;
            float pz2 = (float)Math.cos(a2) * radius;
            float u0 = (float)i / (float)segments;
            float u1 = (float)(i + 1) / (float)segments;
            buffer.method_22912(px1, -height, pz1).method_22913(u0, 0.0f);
            buffer.method_22912(px1, height, pz1).method_22913(u0, 1.0f);
            buffer.method_22912(px2, height, pz2).method_22913(u1, 1.0f);
            buffer.method_22912(px2, -height, pz2).method_22913(u1, 0.0f);
        }
        return buffer;
    }

    private float randRange(float min, float max, LegacyRandomSource random) {
        return min + random.method_43057() * (max - min);
    }

    @FunctionalInterface
    static interface BufferFunction {
        public BufferBuilder make(Tesselator var1, float var2, float var3, int var4, long var5);
    }
}

