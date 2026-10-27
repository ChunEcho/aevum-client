/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.Window
 *  net.minecraft.I18n
 *  net.minecraft.IntegratedServer
 *  net.minecraft.InteractionHand
 *  net.minecraft.SharedConstants
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.HitResult
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Minecraft
 *  net.minecraft.Gui
 *  net.minecraft.BlockHitResult
 *  net.minecraft.ReentrantBlockableEventLoop
 *  net.minecraft.ConnectScreen
 *  net.minecraft.ReceivingLevelScreen
 *  net.minecraft.ProgressScreen
 *  net.minecraft.Screen
 *  net.minecraft.TitleScreen
 *  net.minecraft.PoseStack
 *  net.minecraft.GameConfig
 *  net.minecraft.ClientPacketListener
 *  net.minecraft.MultiPlayerGameMode
 *  net.minecraft.ClientLevel
 *  net.minecraft.ServerData
 *  net.minecraft.ParticleEngine
 *  net.minecraft.LocalPlayer
 *  org.ladysnake.satin.api.managed.ManagedShaderEffect
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.aevum.asm.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.Aevum;
import dev.aevum.api.events.Event;
import dev.aevum.api.events.impl.ClientTickEvent;
import dev.aevum.api.events.impl.DoAttackEvent;
import dev.aevum.api.events.impl.GameLeftEvent;
import dev.aevum.api.events.impl.OpenScreenEvent;
import dev.aevum.api.events.impl.ResizeEvent;
import dev.aevum.api.utils.render.Render2DUtil;
import dev.aevum.core.impl.CommandManager;
import dev.aevum.core.impl.FontManager;
import dev.aevum.core.impl.ShaderManager;
import dev.aevum.mod.modules.impl.client.ClientSetting;
import dev.aevum.mod.modules.impl.player.InteractTweaks;
import java.awt.Color;
import net.minecraft.Window;
import net.minecraft.I18n;
import net.minecraft.IntegratedServer;
import net.minecraft.InteractionHand;
import net.minecraft.SharedConstants;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.HitResult;
import net.minecraft.Minecraft;
import net.minecraft.Gui;
import net.minecraft.BlockHitResult;
import net.minecraft.ReentrantBlockableEventLoop;
import net.minecraft.ConnectScreen;
import net.minecraft.ReceivingLevelScreen;
import net.minecraft.ProgressScreen;
import net.minecraft.Screen;
import net.minecraft.TitleScreen;
import net.minecraft.PoseStack;
import net.minecraft.GameConfig;
import net.minecraft.ClientPacketListener;
import net.minecraft.MultiPlayerGameMode;
import net.minecraft.ClientLevel;
import net.minecraft.ServerData;
import net.minecraft.ParticleEngine;
import net.minecraft.LocalPlayer;
import org.ladysnake.satin.api.managed.ManagedShaderEffect;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Minecraft.class})
public abstract class MixinMinecraftClient
extends ReentrantBlockableEventLoop<Runnable> {
    @Shadow
    @Final
    public Gui gui;
    @Shadow
    public int missTime;
    @Shadow
    public LocalPlayer player;
    @Shadow
    public HitResult hitResult;
    @Shadow
    public MultiPlayerGameMode gameMode;
    @Final
    @Shadow
    public ParticleEngine particleEngine;
    @Shadow
    public ClientLevel level;
    @Shadow
    private IntegratedServer singleplayerServer;
    @Shadow
    public Screen screen;
    @Shadow
    @Final
    private Window window;
    @Unique
    private static long AevumStartTs = 0L;

    public MixinMinecraftClient(String string) {
        super(string);
    }

    @Inject(method={"onResolutionChanged"}, at={@At(value="TAIL")})
    private void captureResize(CallbackInfo ci) {
        Aevum.EVENT_BUS.post(new ResizeEvent(this.window));
    }

    @Redirect(method={"render"}, at=@At(value="INVOKE", target="Lcom/mojang/blaze3d/systems/RenderSystem;limitDisplayFPS(I)V"), require=0)
    public void fpsHook(int fps) {
        if (!ClientSetting.INSTANCE.fuckFPSLimit.getValue()) {
            RenderSystem.limitDisplayFPS((int)fps);
        }
    }

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    void postWindowInit(GameConfig args, CallbackInfo ci) {
        FontManager.init();
        AevumStartTs = System.currentTimeMillis();
    }

    @Inject(method={"setScreen"}, at={@At(value="HEAD")}, cancellable=true)
    private void onSetScreen(Screen screen, CallbackInfo info) {
        if (Aevum.EVENT_BUS != null) {
            OpenScreenEvent event = OpenScreenEvent.get(screen);
            Aevum.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                info.cancel();
            }
        }
    }

    @Inject(method={"render"}, at={@At(value="TAIL")})
    private void aevumMagentaOverlay(CallbackInfo ci) {
        float ny;
        float nx;
        float x0;
        float i;
        Screen screen = this.screen;
        if (screen == null) {
            return;
        }
        boolean isMainMenu = screen instanceof TitleScreen;
        boolean isLoading = screen instanceof ProgressScreen || screen instanceof ReceivingLevelScreen || screen instanceof ConnectScreen;
        boolean bl = isLoading;
        if (!isMainMenu && !isLoading) {
            return;
        }
        if (Aevum.SHADER.fullNullCheck()) {
            return;
        }
        float w = this.window.getGuiScaledWidth();
        float h = this.window.getGuiScaledHeight();
        float t = (float)(System.currentTimeMillis() % 100000L) / 1000.0f;
        ManagedShaderEffect gradient = Aevum.SHADER.getShader(ShaderManager.Shader.Gradient);
        gradient.setUniformValue("alpha2", 0.85f);
        gradient.setUniformValue("rgb", 0.78f, 0.05f, 0.59f);
        gradient.setUniformValue("rgb1", 0.56f, 0.06f, 0.68f);
        gradient.setUniformValue("rgb2", 0.93f, 0.12f, 0.63f);
        gradient.setUniformValue("rgb3", 0.64f, 0.0f, 0.64f);
        gradient.setUniformValue("step", 180.0f);
        gradient.setUniformValue("radius", 2.0f);
        gradient.setUniformValue("quality", 1.0f);
        gradient.setUniformValue("divider", 150.0f);
        gradient.setUniformValue("maxSample", 10.0f);
        gradient.setUniformValue("resolution", w, h);
        gradient.setUniformValue("time", t * 300.0f);
        gradient.render(((Minecraft)this).getTimer().method_60637(true));
        ManagedShaderEffect pulse = Aevum.SHADER.getShader(ShaderManager.Shader.Pulse);
        pulse.setUniformValue("mixFactor", 0.65f);
        pulse.setUniformValue("minAlpha", 0.35f);
        pulse.setUniformValue("radius", 2.0f);
        pulse.setUniformValue("quality", 1.0f);
        pulse.setUniformValue("divider", 150.0f);
        pulse.setUniformValue("maxSample", 10.0f);
        pulse.setUniformValue("color", 0.85f, 0.05f, 0.66f);
        pulse.setUniformValue("color2", 0.56f, 0.06f, 0.68f);
        pulse.setUniformValue("time", t);
        pulse.setUniformValue("size", 12.0f);
        pulse.setUniformValue("resolution", w, h);
        pulse.render(((Minecraft)this).getTimer().method_60637(true));
        PoseStack m = new PoseStack();
        w = this.window.getGuiScaledWidth();
        h = this.window.getGuiScaledHeight();
        long now = System.currentTimeMillis();
        long elapsed = now - AevumStartTs;
        if (elapsed < 2400L) {
            float p = Math.max(0.0f, Math.min(1.0f, (float)elapsed / 2400.0f));
            int aTop = (int)(180.0 * Math.sin((double)p * Math.PI));
            Color c1 = new Color(28, 60, 110, aTop);
            Color c2 = new Color(190, 50, 160, aTop);
            Render2DUtil.verticalGradient(m, 0.0f, 0.0f, w, h, c1, c2);
            float r = Math.min(w, h) * (0.12f + 0.18f * p);
            Render2DUtil.drawCircle(m, w / 2.0f, h / 2.0f, r, new Color(255, 255, 255, (int)(70.0f * p)), 80);
            Render2DUtil.drawCircle(m, w / 2.0f, h / 2.0f, r * 1.2f, new Color(120, 220, 255, (int)(50.0f * p)), 80);
            return;
        }
        float phase = (float)(now % 6000L) / 6000.0f;
        float angle = 0.523599f;
        float dx = (float)Math.tan(angle) * h;
        float base = -h;
        float spacing = 28.0f;
        float shift = phase * spacing * 5.2f;
        int cA1 = new Color(230, 60, 170, 64).getRGB();
        int cA2 = new Color(160, 40, 130, 48).getRGB();
        int nodeC = new Color(255, 255, 255, 42).getRGB();
        for (i = base; i < w; i += spacing) {
            x0 = i + shift;
            Render2DUtil.drawLine(m, x0, 0.0f, x0 + dx, h, cA1);
            nx = x0 + dx * 0.25f;
            ny = h * 0.25f;
            Render2DUtil.drawCircle(m, nx, ny, 2.5f, new Color(nodeC, true), 32);
        }
        for (i = base + spacing / 2.0f; i < w; i += spacing) {
            x0 = i + shift * 0.85f;
            Render2DUtil.drawLine(m, x0, 0.0f, x0 + dx, h, cA2);
            nx = x0 + dx * 0.65f;
            ny = h * 0.6f;
            Render2DUtil.drawCircle(m, nx, ny, 2.0f, new Color(nodeC, true), 28);
        }
    }

    @Inject(method={"doAttack"}, at={@At(value="INVOKE", target="Lnet/minecraft/util/hit/HitResult;getType()Lnet/minecraft/util/hit/HitResult$Type;", shift=At.Shift.BEFORE)})
    public void onAttack(CallbackInfoReturnable<Boolean> cir) {
        Aevum.EVENT_BUS.post(DoAttackEvent.getPre());
    }

    @Inject(method={"doAttack"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;swingHand(Lnet/minecraft/util/Hand;)V", shift=At.Shift.AFTER)})
    public void onAttackPost(CallbackInfoReturnable<Boolean> cir) {
        if (Aevum.EVENT_BUS != null) {
            Aevum.EVENT_BUS.post(DoAttackEvent.getPost());
        }
    }

    @Inject(method={"doAttack"}, at={@At(value="HEAD")}, cancellable=true)
    public void doAttackHook(CallbackInfoReturnable<Boolean> cir) {
        if (Aevum.EVENT_BUS != null) {
            DoAttackEvent event = new DoAttackEvent();
            Aevum.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                cir.setReturnValue((Object)false);
            }
        }
    }

    @Inject(method={"disconnect(Lnet/minecraft/client/gui/screen/Screen;)V"}, at={@At(value="HEAD")})
    private void onDisconnect(Screen screen, CallbackInfo info) {
        if (Aevum.EVENT_BUS != null) {
            Aevum.EVENT_BUS.post(GameLeftEvent.INSTANCE);
        }
        if (Aevum.MODULE != null) {
            Aevum.MODULE.onLogout();
        }
    }

    @Inject(method={"disconnect(Lnet/minecraft/client/gui/screen/Screen;)V"}, at={@At(value="HEAD")})
    private void clearTitleMixin(Screen screen, CallbackInfo info) {
        if (ClientSetting.INSTANCE.titleFix.getValue()) {
            this.gui.method_34003();
            this.gui.method_1742();
        }
    }

    @Inject(method={"handleBlockBreaking"}, at={@At(value="HEAD")}, cancellable=true)
    private void handleBlockBreaking(boolean breaking, CallbackInfo ci) {
        if (this.missTime <= 0 && this.player.isUsingItem() && InteractTweaks.INSTANCE.multiTask()) {
            if (breaking && this.hitResult != null && this.hitResult.method_17783() == HitResult.HitResult$Type.BLOCK) {
                Direction direction;
                BlockHitResult blockHitResult = (BlockHitResult)this.hitResult;
                BlockPos blockPos = blockHitResult.method_17777();
                if (!this.level.method_8320(blockPos).method_26215() && this.gameMode.method_2902(blockPos, direction = blockHitResult.getDirection())) {
                    this.particleEngine.method_3054(blockPos, direction);
                    this.player.method_6104(InteractionHand.MAIN_HAND);
                }
            } else {
                this.gameMode.method_2925();
            }
            ci.cancel();
        }
    }

    @Inject(at={@At(value="HEAD")}, method={"tick()V"})
    public void tickHead(CallbackInfo info) {
        block3: {
            try {
                if (Aevum.EVENT_BUS != null) {
                    Aevum.EVENT_BUS.post(ClientTickEvent.get(Event.Stage.Pre));
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                if (!ClientSetting.INSTANCE.debug.getValue()) break block3;
                CommandManager.sendMessage("\u00a74An error has occurred (MinecraftClient.tick() [HEAD]) Message: [" + e.getMessage() + "]");
            }
        }
    }

    @Inject(at={@At(value="TAIL")}, method={"tick()V"})
    public void tickTail(CallbackInfo info) {
        block3: {
            try {
                if (Aevum.EVENT_BUS != null) {
                    Aevum.EVENT_BUS.post(ClientTickEvent.get(Event.Stage.Post));
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                if (!ClientSetting.INSTANCE.debug.getValue()) break block3;
                CommandManager.sendMessage("\u00a74An error has occurred (MinecraftClient.tick() [TAIL]) Message: [" + e.getMessage() + "]");
            }
        }
    }

    @Overwrite
    private String createTitle() {
        if (ClientSetting.INSTANCE == null) {
            return "Aevum Client: Loading..";
        }
        if (ClientSetting.INSTANCE.titleOverride.getValue()) {
            return ClientSetting.INSTANCE.windowTitle.getValue();
        }
        StringBuilder stringBuilder = new StringBuilder("Aevum Client");
        stringBuilder.append(" ");
        stringBuilder.append(SharedConstants.getCurrentVersion().method_48019());
        ClientPacketListener clientPlayNetworkHandler = this.getConnection();
        if (clientPlayNetworkHandler != null && clientPlayNetworkHandler.getConnection().isConnected()) {
            stringBuilder.append(" - ");
            ServerData serverInfo = this.getCurrentServer();
            if (this.singleplayerServer != null && !this.singleplayerServer.method_3860()) {
                stringBuilder.append(I18n.method_4662((String)"title.singleplayer", (Object[])new Object[0]));
            } else if (serverInfo != null && serverInfo.isRealm()) {
                stringBuilder.append(I18n.method_4662((String)"title.multiplayer.realms", (Object[])new Object[0]));
            } else if (!(this.singleplayerServer != null || serverInfo != null && serverInfo.isLan())) {
                stringBuilder.append(I18n.method_4662((String)"title.multiplayer.other", (Object[])new Object[0]));
            } else {
                stringBuilder.append(I18n.method_4662((String)"title.multiplayer.lan", (Object[])new Object[0]));
            }
        }
        return stringBuilder.toString();
    }

    @Shadow
    public ClientPacketListener getConnection() {
        return null;
    }

    @Shadow
    public ServerData getCurrentServer() {
        return null;
    }
}

