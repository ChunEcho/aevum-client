/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.Vec2
 *  net.minecraft.Vec3
 *  net.minecraft.ClientboundPlayerPositionPacket
 *  net.minecraft.BufferUploader
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tesselator
 *  net.minecraft.DefaultVertexFormat
 *  net.minecraft.VertexFormat$Mode
 *  net.minecraft.PoseStack
 *  net.minecraft.GameRenderer
 *  net.minecraft.MeshData
 *  org.joml.Matrix4f
 *  org.lwjgl.opengl.GL11
 */
package dev.aevum.mod.modules.impl.movement;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.KeyboardInputEvent;
import dev.aevum.api.events.impl.MoveEvent;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.RotationEvent;
import dev.aevum.api.events.impl.TimerEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.player.MovementUtil;
import dev.aevum.api.utils.render.ColorUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.client.AntiCheat;
import dev.aevum.mod.modules.impl.player.Freecam;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.Vec2;
import net.minecraft.Vec3;
import net.minecraft.ClientboundPlayerPositionPacket;
import net.minecraft.BufferUploader;
import net.minecraft.BufferBuilder;
import net.minecraft.Tesselator;
import net.minecraft.DefaultVertexFormat;
import net.minecraft.VertexFormat;
import net.minecraft.PoseStack;
import net.minecraft.GameRenderer;
import net.minecraft.MeshData;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class HoleSnap
extends Module {
    public static HoleSnap INSTANCE;
    public final BooleanSetting any = this.add(new BooleanSetting("AnyHole", true));
    public final SliderSetting timer = this.add(new SliderSetting("Timer", 1.0, 0.1, 8.0, 0.1));
    public final BooleanSetting up = this.add(new BooleanSetting("Up", true));
    public final BooleanSetting grim = this.add(new BooleanSetting("Grim", false));
    public final ColorSetting color = this.add(new ColorSetting("Color", new Color(255, 255, 255, 100)));
    public final SliderSetting circleSize = this.add(new SliderSetting("CircleSize", 1.0, 0.1f, 2.5));
    public final BooleanSetting fade = this.add(new BooleanSetting("Fade", true));
    public final SliderSetting segments = this.add(new SliderSetting("Segments", 180, 0, 360));
    private final SliderSetting range = this.add(new SliderSetting("Range", 5, 1, 50));
    private final SliderSetting timeoutTicks = this.add(new SliderSetting("TimeOut", 40, 0, 100));
    private final SliderSetting steps = this.add(new SliderSetting("Steps", 0.8, 0.0, 1.0, 0.01, this.grim::getValue));
    private final SliderSetting priority = this.add(new SliderSetting("Priority", 10, 0, 100, this.grim::getValue));
    boolean resetMove = false;
    boolean applyTimer = false;
    Vec3 targetPos;
    private BlockPos holePos;
    private int stuckTicks;
    private int enabledTicks;

    public HoleSnap() {
        super("HoleSnap", "HoleSnap", Module.Category.Movement);
        this.setChinese("\u62c9\u5751");
        INSTANCE = this;
    }

    public static Vec2 getRotationTo(Vec3 posFrom, Vec3 posTo) {
        Vec3 vec3d = posTo.method_1020(posFrom);
        return HoleSnap.getRotationFromVec(vec3d);
    }

    public static void drawCircle(PoseStack matrixStack, Color color, double circleSize, Vec3 pos, int segments) {
        Vec3 camPos = HoleSnap.mc.getBlockEntityRenderDispatcher().camera.getPosition();
        RenderSystem.disableDepthTest();
        Matrix4f matrix = matrixStack.last().method_23761();
        RenderSystem.setShader(GameRenderer::method_34540);
        float keyCodec = (float)color.getAlpha() / 255.0f;
        float r = (float)color.getRed() / 255.0f;
        float g = (float)color.getGreen() / 255.0f;
        float elementCodec = (float)color.getBlue() / 255.0f;
        BufferBuilder bufferBuilder = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        for (double i = 0.0; i < 360.0; i += 360.0 / (double)segments) {
            double x = Math.sin(Math.toRadians(i)) * circleSize;
            double z = Math.cos(Math.toRadians(i)) * circleSize;
            Vec3 tempPos = new Vec3(pos.x + x, pos.y, pos.z + z).method_1031(-camPos.x, -camPos.y, -camPos.z);
            bufferBuilder.method_22918(matrix, (float)tempPos.x, (float)tempPos.y, (float)tempPos.z).method_22915(r, g, elementCodec, keyCodec);
        }
        BufferUploader.method_43433((MeshData)bufferBuilder.method_60800());
        RenderSystem.enableDepthTest();
    }

    private static Vec2 getRotationFromVec(Vec3 vec) {
        double d = vec.x;
        double d2 = vec.z;
        double xz = Math.hypot(d, d2);
        d2 = vec.z;
        double d3 = vec.x;
        double yaw = HoleSnap.normalizeAngle(Math.toDegrees(Math.atan2(d2, d3)) - 90.0);
        double pitch = HoleSnap.normalizeAngle(Math.toDegrees(-Math.atan2(vec.y, xz)));
        return new Vec2((float)yaw, (float)pitch);
    }

    private static double normalizeAngle(double angleIn) {
        double angle = angleIn;
        if ((angle %= 360.0) >= 180.0) {
            angle -= 360.0;
        }
        if (angle < -180.0) {
            angle += 360.0;
        }
        return angle;
    }

    @EventListener(priority=-99)
    public void onTimer(TimerEvent event) {
        if (this.applyTimer) {
            event.set(this.timer.getValueFloat());
        }
    }

    @Override
    public void onEnable() {
        this.applyTimer = false;
        if (HoleSnap.nullCheck()) {
            this.disable();
            return;
        }
        this.resetMove = false;
        this.holePos = Aevum.HOLE.getHole((float)this.range.getValue(), true, this.any.getValue(), this.up.getValue());
    }

    @Override
    public void onDisable() {
        this.holePos = null;
        this.stuckTicks = 0;
        this.enabledTicks = 0;
        if (HoleSnap.nullCheck()) {
            return;
        }
        if (this.resetMove && !this.grim.getValue()) {
            MovementUtil.setMotionX(0.0);
            MovementUtil.setMotionZ(0.0);
        }
    }

    @EventListener
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
            this.disable();
        }
    }

    @EventListener(priority=-999)
    public void onKeyInput(KeyboardInputEvent e) {
        if (!this.grim.getValue()) {
            return;
        }
        if (!AntiCheat.INSTANCE.movementSync()) {
            this.sendMessage("\u00a74HoleSnap require MovementSync.");
            this.disable();
            return;
        }
        if (HoleSnap.mc.player.isHandsBusy() || Freecam.INSTANCE.isOn()) {
            return;
        }
        HoleSnap.mc.player.input.leftImpulse = 0.0f;
        HoleSnap.mc.player.input.forwardImpulse = 1.0f;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        Direction facing;
        this.holePos = Aevum.HOLE.getHole((float)this.range.getValue(), true, this.any.getValue(), this.up.getValue());
        if (this.holePos == null) {
            this.disable();
            return;
        }
        ++this.enabledTicks;
        if ((double)this.enabledTicks > this.timeoutTicks.getValue() - 1.0) {
            this.disable();
            return;
        }
        this.applyTimer = true;
        if (!this.grim.getValue()) {
            return;
        }
        if (!HoleSnap.mc.player.isAlive() || HoleSnap.mc.player.isFallFlying()) {
            this.disable();
            return;
        }
        if (this.stuckTicks > 8) {
            this.disable();
            return;
        }
        if (this.holePos == null) {
            this.disable();
            return;
        }
        Vec3 playerPos = HoleSnap.mc.player.position();
        this.targetPos = new Vec3((double)this.holePos.method_10263() + 0.5, HoleSnap.mc.player.getY(), (double)this.holePos.getZ() + 0.5);
        if (Aevum.HOLE.isDoubleHole(this.holePos) && (facing = Aevum.HOLE.is3Block(this.holePos)) != null) {
            this.targetPos = this.targetPos.method_1019(new Vec3((double)facing.getNormal().method_10263() * 0.5, (double)facing.getNormal().method_10264() * 0.5, (double)facing.getNormal().getZ() * 0.5));
        }
        this.applyTimer = true;
        this.resetMove = true;
        float rotation = HoleSnap.getRotationTo((Vec3)playerPos, (Vec3)this.targetPos).x;
        float yawRad = rotation / 180.0f * (float)Math.PI;
        double dist = playerPos.method_1022(this.targetPos);
        double cappedSpeed = Math.min(0.2873, dist);
        double x = (double)(-((float)Math.sin(yawRad))) * cappedSpeed;
        double z = (double)((float)Math.cos(yawRad)) * cappedSpeed;
        if (Math.abs(x) < 0.25 && Math.abs(z) < 0.25 && playerPos.y <= (double)this.holePos.method_10264() + 0.8) {
            this.disable();
            return;
        }
        this.stuckTicks = HoleSnap.mc.player.horizontalCollision ? (this.stuckTicks = this.stuckTicks + 1) : 0;
    }

    @EventListener
    public void onRotate(RotationEvent event) {
        if (this.grim.getValue() && this.holePos != null) {
            Direction facing;
            this.targetPos = new Vec3((double)this.holePos.method_10263() + 0.5, HoleSnap.mc.player.getY(), (double)this.holePos.getZ() + 0.5);
            if (Aevum.HOLE.isDoubleHole(this.holePos) && (facing = Aevum.HOLE.is3Block(this.holePos)) != null) {
                this.targetPos = this.targetPos.method_1019(new Vec3((double)facing.getNormal().method_10263() * 0.5, (double)facing.getNormal().method_10264() * 0.5, (double)facing.getNormal().getZ() * 0.5));
            }
            event.setTarget(this.targetPos, this.steps.getValueFloat(), this.priority.getValueFloat());
        }
    }

    @EventListener
    public void onMove(MoveEvent event) {
        Direction facing;
        if (this.grim.getValue()) {
            return;
        }
        if (!HoleSnap.mc.player.isAlive() || HoleSnap.mc.player.isFallFlying()) {
            this.disable();
            return;
        }
        if (this.stuckTicks > 8) {
            this.disable();
            return;
        }
        if (this.holePos == null) {
            this.disable();
            return;
        }
        Vec3 playerPos = HoleSnap.mc.player.position();
        this.targetPos = new Vec3((double)this.holePos.method_10263() + 0.5, HoleSnap.mc.player.getY(), (double)this.holePos.getZ() + 0.5);
        if (Aevum.HOLE.isDoubleHole(this.holePos) && (facing = Aevum.HOLE.is3Block(this.holePos)) != null) {
            this.targetPos = this.targetPos.method_1019(new Vec3((double)facing.getNormal().method_10263() * 0.5, (double)facing.getNormal().method_10264() * 0.5, (double)facing.getNormal().getZ() * 0.5));
        }
        this.applyTimer = true;
        this.resetMove = true;
        float rotation = HoleSnap.getRotationTo((Vec3)playerPos, (Vec3)this.targetPos).x;
        float yawRad = rotation / 180.0f * (float)Math.PI;
        double dist = playerPos.method_1022(this.targetPos);
        double cappedSpeed = Math.min(0.2873, dist);
        double x = (double)(-((float)Math.sin(yawRad))) * cappedSpeed;
        double z = (double)((float)Math.cos(yawRad)) * cappedSpeed;
        event.setX(x);
        event.setZ(z);
        if (Math.abs(x) < 0.1 && Math.abs(z) < 0.1 && playerPos.y <= (double)this.holePos.method_10264() + 0.5) {
            this.disable();
        }
        this.stuckTicks = HoleSnap.mc.player.horizontalCollision ? (this.stuckTicks = this.stuckTicks + 1) : 0;
    }

    @Override
    public void onRender3D(PoseStack matrixStack) {
        if (this.targetPos == null || this.holePos == null) {
            return;
        }
        GL11.glEnable((int)3042);
        Color color = this.color.getValue();
        Vec3 pos = new Vec3(this.targetPos.x, (double)this.holePos.method_10264(), this.targetPos.method_10215());
        if (this.fade.getValue()) {
            double temp = 0.01;
            for (double i = 0.0; i < this.circleSize.getValue(); i += temp) {
                HoleSnap.drawCircle(matrixStack, ColorUtil.injectAlpha(color, (int)Math.min((double)(color.getAlpha() * 2) / (this.circleSize.getValue() / temp), 255.0)), i, pos, this.segments.getValueInt());
            }
        } else {
            HoleSnap.drawCircle(matrixStack, color, this.circleSize.getValue(), pos, this.segments.getValueInt());
        }
        GL11.glDisable((int)3042);
    }
}

