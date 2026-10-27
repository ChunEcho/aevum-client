/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.Player
 *  net.minecraft.Vec3
 *  net.minecraft.BufferUploader
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tesselator
 *  net.minecraft.DefaultVertexFormat
 *  net.minecraft.VertexFormat$Mode
 *  net.minecraft.Mth
 *  net.minecraft.Camera
 *  net.minecraft.PoseStack
 *  net.minecraft.GameRenderer
 *  net.minecraft.MeshData
 *  org.joml.Matrix4f
 */
package dev.aevum.mod.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.Player;
import net.minecraft.Vec3;
import net.minecraft.BufferUploader;
import net.minecraft.BufferBuilder;
import net.minecraft.Tesselator;
import net.minecraft.DefaultVertexFormat;
import net.minecraft.VertexFormat;
import net.minecraft.Mth;
import net.minecraft.Camera;
import net.minecraft.PoseStack;
import net.minecraft.GameRenderer;
import net.minecraft.MeshData;
import org.joml.Matrix4f;

public class PlayerTrail
extends Module {
    private final ColorSetting trailColor = this.add(new ColorSetting("Color", new Color(255, 100, 200, 180)));
    private final SliderSetting trailLength = this.add(new SliderSetting("Length", 30, 5, 100));
    private final SliderSetting lineWidth = this.add(new SliderSetting("LineWidth", 0.35f, 0.05f, 2.0));
    private final SliderSetting lineHeight = this.add(new SliderSetting("LineHeight", 0.9f, 0.05f, 2.0));
    private final SliderSetting yOffset = this.add(new SliderSetting("YOffset", (double)0.6f, 0.0, 2.0, 0.1f));
    private final BooleanSetting onlySelf = this.add(new BooleanSetting("OnlySelf", true));
    private final SliderSetting updateInterval = this.add(new SliderSetting("UpdateInterval", 1, 1, 10));
    private final SliderSetting minStep = this.add(new SliderSetting("MinStep", 0.08f, 0.01f, 0.5));
    private final BooleanSetting cuteMode = this.add(new BooleanSetting("CuteMode", false));
    private final SliderSetting cuteStep = this.add(new SliderSetting("CuteStep", (double)0.35f, (double)0.1f, 1.5, 0.05f));
    private final SliderSetting rainbowSpeed = this.add(new SliderSetting("RainbowSpeed", 1.0, (double)0.1f, 5.0, 0.1f));
    private final List<PlayerTrailData> trailDataList = new CopyOnWriteArrayList<PlayerTrailData>();
    private int updateCounter = 0;
    private float rainbowHue = 0.0f;

    public PlayerTrail() {
        super("PlayerTrail", "Displays continuous trail line behind players", Module.Category.Render);
        this.setChinese("\u73a9\u5bb6\u8f68\u8ff9\u7ebf");
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.trailDataList.clear();
        this.updateCounter = 0;
        this.rainbowHue = 0.0f;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.trailDataList.clear();
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (!this.isOn()) {
            return;
        }
        if (Wrapper.mc.level == null || Wrapper.mc.player == null) {
            return;
        }
        if (this.cuteMode.getValue()) {
            this.rainbowHue += this.rainbowSpeed.getValueFloat() * 0.5f;
            if (this.rainbowHue > 360.0f) {
                this.rainbowHue -= 360.0f;
            }
        }
        ++this.updateCounter;
        if ((double)this.updateCounter < this.updateInterval.getValue()) {
            return;
        }
        this.updateCounter = 0;
        for (Player player : Wrapper.mc.level.method_18456()) {
            if (this.onlySelf.getValue() && player != Wrapper.mc.player || player.isSpectator()) continue;
            PlayerTrailData trailData = this.getOrCreateTrailData(player);
            float heightOffset = this.yOffset.getValueFloat();
            Vec3 currentPos = new Vec3(player.getX(), player.getY() + (double)(player.method_18381(player.getPose()) * heightOffset), player.getZ());
            if (!trailData.points.isEmpty() && !(trailData.points.get((int)(trailData.points.size() - 1)).position.method_1022(currentPos) >= (double)this.minStep.getValueFloat())) continue;
            Color pointColor = this.getTrailColor();
            trailData.points.add(new TrailPoint(currentPos, pointColor, (int)this.trailLength.getValue()));
            while ((double)trailData.points.size() > this.trailLength.getValue()) {
                trailData.points.remove(0);
            }
        }
        this.trailDataList.forEach(data -> data.points.removeIf(TrailPoint::shouldRemove));
    }

    @Override
    public void onRender3D(PoseStack matrixStack) {
        if (!this.isOn()) {
            return;
        }
        if (Wrapper.mc.level == null || Wrapper.mc.player == null) {
            return;
        }
        if (this.trailDataList.isEmpty()) {
            return;
        }
        Camera camera = Wrapper.mc.gameRenderer.method_19418();
        Vec3 camPos = camera.getPosition();
        if (this.cuteMode.getValue()) {
            this.renderCuteMode(matrixStack, camPos);
        } else {
            this.renderNormalMode(matrixStack, camPos);
        }
    }

    private Color getTrailColor() {
        if (this.cuteMode.getValue()) {
            float hue = this.rainbowHue / 360.0f;
            return Color.getHSBColor(hue, 0.8f, 1.0f);
        }
        return this.trailColor.getValue();
    }

    private Color applyAlpha(Color c, float alpha) {
        int a = Mth.method_15340((int)((int)((float)c.getAlpha() * alpha)), (int)0, (int)255);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    private void renderNormalMode(PoseStack matrixStack, Vec3 camPos) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::method_34540);
        BufferBuilder buffer = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = matrixStack.last().method_23761();
        float width = this.lineWidth.getValueFloat();
        float height = this.lineHeight.getValueFloat();
        for (PlayerTrailData trailData : this.trailDataList) {
            if (this.onlySelf.getValue() && trailData.player != Wrapper.mc.player || trailData.points.size() < 2) continue;
            for (TrailPoint point : trailData.points) {
                Vec3 relativePos = point.position.method_1020(camPos);
                float alpha = point.getAlpha();
                Color c = this.applyAlpha(point.color, alpha);
                buffer.method_22918(matrix, (float)relativePos.x, (float)relativePos.y, (float)relativePos.z).method_22915((float)c.getRed() / 255.0f, (float)c.getGreen() / 255.0f, (float)c.getBlue() / 255.0f, (float)c.getAlpha() / 255.0f);
                buffer.method_22918(matrix, (float)relativePos.x, (float)(relativePos.y + (double)height), (float)relativePos.z).method_22915((float)c.getRed() / 255.0f, (float)c.getGreen() / 255.0f, (float)c.getBlue() / 255.0f, (float)c.getAlpha() / 255.0f);
            }
        }
        BufferUploader.method_43433((MeshData)buffer.method_60800());
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void renderCuteMode(PoseStack matrixStack, Vec3 camPos) {
        Color[] colors = new Color[]{new Color(69, 221, 255, 180), new Color(248, 139, 160, 180), new Color(255, 255, 255, 180), new Color(248, 139, 160, 180), new Color(69, 221, 255, 180)};
        float step = this.cuteStep.getValueFloat();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::method_34540);
        for (int layer = 0; layer < 5; ++layer) {
            this.renderCuteLayer(matrixStack, camPos, colors[layer], (float)layer * step, step);
        }
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void renderCuteLayer(PoseStack matrixStack, Vec3 camPos, Color color, float layerYOffset, float layerHeight) {
        block6: {
            boolean hasData = false;
            for (PlayerTrailData trailData : this.trailDataList) {
                if (this.onlySelf.getValue() && trailData.player != Wrapper.mc.player || trailData.points.size() < 2) continue;
                hasData = true;
                break;
            }
            if (!hasData) {
                return;
            }
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.setShader(GameRenderer::method_34540);
            BufferBuilder buffer = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            Matrix4f matrix = matrixStack.last().method_23761();
            boolean vertexCountBefore = false;
            for (PlayerTrailData trailData : this.trailDataList) {
                if (this.onlySelf.getValue() && trailData.player != Wrapper.mc.player || trailData.points.size() < 2) continue;
                for (int i = 0; i < trailData.points.size(); ++i) {
                    TrailPoint point = trailData.points.get(i);
                    Vec3 relativePos = point.position.method_1020(camPos);
                    float alpha = point.getAlpha();
                    Color c = this.applyAlpha(color, alpha * 0.8f);
                    buffer.method_22918(matrix, (float)relativePos.x, (float)(relativePos.y + (double)layerYOffset), (float)relativePos.z).method_22915((float)c.getRed() / 255.0f, (float)c.getGreen() / 255.0f, (float)c.getBlue() / 255.0f, (float)c.getAlpha() / 255.0f);
                    buffer.method_22918(matrix, (float)relativePos.x, (float)(relativePos.y + (double)layerYOffset + (double)layerHeight), (float)relativePos.z).method_22915((float)c.getRed() / 255.0f, (float)c.getGreen() / 255.0f, (float)c.getBlue() / 255.0f, (float)c.getAlpha() / 255.0f);
                }
            }
            try {
                BufferUploader.method_43433((MeshData)buffer.method_60800());
            }
            catch (IllegalStateException e) {
                if (e.getMessage() != null && e.getMessage().contains("BufferBuilder was empty")) break block6;
                throw e;
            }
        }
    }

    private PlayerTrailData getOrCreateTrailData(Player player) {
        for (PlayerTrailData data : this.trailDataList) {
            if (data.player != player) continue;
            return data;
        }
        PlayerTrailData newData = new PlayerTrailData(player);
        this.trailDataList.add(newData);
        return newData;
    }

    private static class PlayerTrailData {
        public final Player player;
        public final List<TrailPoint> points;

        public PlayerTrailData(Player player) {
            this.player = player;
            this.points = new ArrayList<TrailPoint>();
        }
    }

    private static class TrailPoint {
        public final Vec3 position;
        public final Color color;
        public int life;
        public int prevLife;
        public final int maxLife;

        public TrailPoint(Vec3 position, Color color, int maxLife) {
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

