/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.Blocks
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.AABB
 *  net.minecraft.PoseStack
 */
package dev.aevum.mod.modules.impl.render;

import dev.aevum.Aevum;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.render.ColorUtil;
import dev.aevum.api.utils.render.Render3DUtil;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.Entity;
import net.minecraft.Blocks;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.AABB;
import net.minecraft.PoseStack;

public class HoleESP
extends Module {
    public static HoleESP INSTANCE;
    public final SliderSetting startFade = this.add(new SliderSetting("StartFade", 5.0, 1.0, 20.0));
    public final SliderSetting distance = this.add(new SliderSetting("Distance", 6.0, 1.0, 20.0));
    public final SliderSetting airHeight = this.add(new SliderSetting("AirHeight", 1.0, -3.0, 3.0, 0.01));
    public final BooleanSetting airYCheck = this.add(new BooleanSetting("AirYCheck", true));
    public final SliderSetting height = this.add(new SliderSetting("Height", 1.0, -3.0, 3.0, 0.1));
    public final SliderSetting wallHeight = this.add(new SliderSetting("WallHeight", 3.0, -3.0, 3.0, 0.1));
    public final BooleanSetting sideCheck = this.add(new BooleanSetting("SideCheck", true));
    private final ColorSetting airFill = this.add(new ColorSetting("AirFill", new Color(148, 0, 0, 100)).injectBoolean(true));
    private final ColorSetting airBox = this.add(new ColorSetting("AirBox", new Color(148, 0, 0, 100)).injectBoolean(true));
    private final ColorSetting airFade = this.add(new ColorSetting("AirFade", new Color(148, 0, 0, 0)).injectBoolean(true));
    private final ColorSetting normalFill = this.add(new ColorSetting("UnsafeFill", new Color(255, 0, 0, 50)).injectBoolean(true));
    private final ColorSetting normalBox = this.add(new ColorSetting("UnsafeBox", new Color(255, 0, 0, 100)).injectBoolean(true));
    private final ColorSetting normalFade = this.add(new ColorSetting("UnsafeFade", new Color(255, 0, 0, 0)).injectBoolean(true));
    private final ColorSetting bedrockFill = this.add(new ColorSetting("SafeFill", new Color(8, 255, 79, 50)).injectBoolean(true));
    private final ColorSetting bedrockBox = this.add(new ColorSetting("SafeBox", new Color(8, 255, 79, 100)).injectBoolean(true));
    private final ColorSetting bedrockFade = this.add(new ColorSetting("SafeFade", new Color(8, 255, 79, 100)).injectBoolean(true));
    private final ColorSetting wallFill = this.add(new ColorSetting("WallFill", new Color(0, 255, 255, 128)).injectBoolean(true));
    private final ColorSetting wallBox = this.add(new ColorSetting("WallBox", new Color(0, 225, 255, 255)).injectBoolean(true));
    private final ColorSetting wallFade = this.add(new ColorSetting("WallFade", new Color(0, 255, 255, 64)).injectBoolean(true));
    private final ColorSetting wallSideFill = this.add(new ColorSetting("WallSideFill", new Color(0, 255, 255, 128)).injectBoolean(true));
    private final ColorSetting wallSideBox = this.add(new ColorSetting("WallSideBox", new Color(0, 225, 255, 255)).injectBoolean(true));
    private final ColorSetting wallSideFade = this.add(new ColorSetting("WallSideFade", new Color(0, 255, 255, 64)).injectBoolean(true));
    private final SliderSetting updateDelay = this.add(new SliderSetting("UpdateDelay", 50, 0, 1000));
    private final List<BlockPos> tempNormalList = new ArrayList<BlockPos>();
    private final List<BlockPos> tempBedrockList = new ArrayList<BlockPos>();
    private final List<BlockPos> tempAirList = new ArrayList<BlockPos>();
    private final List<BlockPos> tempWallList = new ArrayList<BlockPos>();
    private final List<BlockPos> tempWallSideList = new ArrayList<BlockPos>();
    private final Timer timer = new Timer();
    boolean drawing = false;
    private List<BlockPos> normalList = new ArrayList<BlockPos>();
    private List<BlockPos> bedrockList = new ArrayList<BlockPos>();
    private List<BlockPos> airList = new ArrayList<BlockPos>();
    private List<BlockPos> wallList = new ArrayList<BlockPos>();
    private List<BlockPos> wallSideList = new ArrayList<BlockPos>();

    public HoleESP() {
        super("HoleESP", Module.Category.Render);
        this.setChinese("\u5751\u900f\u89c6");
        INSTANCE = this;
    }

    public void onThread() {
        if (!HoleESP.nullCheck() && !this.isOff() && !this.drawing && this.timer.passedMs(this.updateDelay.getValue())) {
            this.normalList = new ArrayList<BlockPos>(this.tempNormalList);
            this.bedrockList = new ArrayList<BlockPos>(this.tempBedrockList);
            this.airList = new ArrayList<BlockPos>(this.tempAirList);
            this.wallList = new ArrayList<BlockPos>(this.tempWallList);
            this.wallSideList = new ArrayList<BlockPos>(this.tempWallSideList);
            this.timer.reset();
            this.tempBedrockList.clear();
            this.tempNormalList.clear();
            this.tempAirList.clear();
            this.tempWallList.clear();
            this.tempWallSideList.clear();
            for (BlockPos pos : BlockUtil.getSphere(this.distance.getValueFloat(), HoleESP.mc.player.position())) {
                Type type;
                if (this.isBedrock(pos) && this.isBedrock(pos.method_10086(2)) && this.isBedrock(pos.method_10074())) {
                    Direction side = this.getWallSide(pos);
                    if (side != null || !this.sideCheck.getValue()) {
                        this.tempWallList.add(pos);
                    }
                    if (side != null) {
                        this.tempWallSideList.add(pos.method_10093(side));
                    }
                }
                if ((type = this.isHole(pos)) == Type.Bedrock) {
                    this.tempBedrockList.add(pos);
                    continue;
                }
                if (type == Type.Normal) {
                    this.tempNormalList.add(pos);
                    continue;
                }
                if (type != Type.Air) continue;
                this.tempAirList.add(pos);
            }
        }
    }

    private Direction getWallSide(BlockPos pos) {
        double distance = Double.MAX_VALUE;
        Direction side = null;
        for (Direction direction : Direction.values()) {
            BlockPos offsetPos;
            if (direction == Direction.UP || direction == Direction.DOWN || !BlockUtil.canCollide(new AABB((offsetPos = pos.method_10093(direction)).method_10074())) || BlockUtil.canCollide(new AABB(offsetPos)) || BlockUtil.canCollide(new AABB(offsetPos.method_10084()))) continue;
            if (side == null) {
                side = direction;
                distance = HoleESP.mc.player.getEyePosition().method_1022(offsetPos.method_46558());
                continue;
            }
            if (!(HoleESP.mc.player.getEyePosition().method_1022(offsetPos.method_46558()) < distance)) continue;
            side = direction;
            distance = HoleESP.mc.player.getEyePosition().method_1022(offsetPos.method_46558());
        }
        return side;
    }

    private boolean isBedrock(BlockPos pos) {
        return HoleESP.mc.level.method_8320(pos).method_26204() == Blocks.BEDROCK;
    }

    Type isHole(BlockPos pos) {
        if (HoleESP.mc.level.isEmptyBlock(pos) && (!this.airYCheck.getValue() || pos.method_10264() == HoleESP.mc.player.getBlockY() - 1 || pos.method_10264() == HoleESP.mc.player.getBlockY()) && Aevum.HOLE.isHard(pos.method_10084())) {
            return Type.Air;
        }
        int blockProgress = 0;
        boolean bedRock = true;
        for (Direction i : Direction.values()) {
            if (i == Direction.UP || i == Direction.DOWN || !Aevum.HOLE.isHard(pos.method_10093(i))) continue;
            if (HoleESP.mc.level.method_8320(pos.method_10093(i)).method_26204() != Blocks.BEDROCK) {
                bedRock = false;
            }
            ++blockProgress;
        }
        if (HoleESP.mc.level.isEmptyBlock(pos) && HoleESP.mc.level.isEmptyBlock(pos.method_10084()) && HoleESP.mc.level.isEmptyBlock(pos.method_10086(2)) && blockProgress > 3 && BlockUtil.canCollide((Entity)HoleESP.mc.player, new AABB(pos.method_10074()))) {
            return bedRock ? Type.Bedrock : Type.Normal;
        }
        return Aevum.HOLE.isDoubleHole(pos) ? Type.Normal : Type.None;
    }

    @Override
    public void onRender3D(PoseStack matrixStack) {
        this.drawing = true;
        this.draw(matrixStack, this.bedrockList, this.bedrockFill, this.bedrockFade, this.bedrockBox, this.height.getValue());
        this.draw(matrixStack, this.airList, this.airFill, this.airFade, this.airBox, this.airHeight.getValue());
        this.draw(matrixStack, this.normalList, this.normalFill, this.normalFade, this.normalBox, this.height.getValue());
        this.draw(matrixStack, this.wallList, this.wallFill, this.wallFade, this.wallBox, this.wallHeight.getValue());
        this.draw(matrixStack, this.wallSideList, this.wallSideFill, this.wallSideFade, this.wallSideBox, this.height.getValue());
        this.drawing = false;
    }

    private void draw(PoseStack matrixStack, List<BlockPos> list, ColorSetting fill, ColorSetting fade, ColorSetting box, double height) {
        for (BlockPos pos : list) {
            double distance = HoleESP.mc.player.position().method_1022(pos.method_46558());
            double alpha = distance > this.startFade.getValue() ? Math.max(Math.min(1.0, 1.0 - (distance - this.startFade.getValue()) / (this.distance.getValue() - this.startFade.getValue())), 0.0) : 1.0;
            AABB espBox = new AABB((double)pos.method_10263(), (double)pos.method_10264(), (double)pos.getZ(), (double)(pos.method_10263() + 1), (double)pos.method_10264() + height, (double)(pos.getZ() + 1));
            if (fill.booleanValue) {
                if (fade.booleanValue) {
                    Render3DUtil.drawFadeFill(matrixStack, espBox, ColorUtil.injectAlpha(fill.getValue(), (int)((double)fill.getValue().getAlpha() * alpha)), ColorUtil.injectAlpha(fade.getValue(), (int)((double)fade.getValue().getAlpha() * alpha)));
                } else {
                    Render3DUtil.drawFill(matrixStack, espBox, ColorUtil.injectAlpha(fill.getValue(), (int)((double)fill.getValue().getAlpha() * alpha)));
                }
            }
            if (!box.booleanValue) continue;
            Render3DUtil.drawBox(matrixStack, espBox, ColorUtil.injectAlpha(box.getValue(), (int)((double)box.getValue().getAlpha() * alpha)));
        }
    }

    public static enum Type {
        None,
        Air,
        Normal,
        Bedrock;

    }
}

