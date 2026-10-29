/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  by.radioegor146.nativeobfuscator.Native
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.sound.PositionedSoundInstance
 *  net.minecraft.client.sound.SoundInstance
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.sound.SoundEvents
 *  org.lwjgl.glfw.GLFW
 *  org.lwjgl.opengl.GL11
 */
package dev.chun.core.impl;

import dev.chun.Aevum;
import dev.chun.api.events.impl.Render2DEvent;
import dev.chun.api.events.impl.Render3DEvent;
import dev.chun.api.utils.Wrapper;
import dev.chun.api.utils.path.BaritoneUtil;
import dev.chun.api.utils.render.Render2DUtil;
import dev.chun.api.utils.render.TextUtil;
import dev.chun.mod.Mod;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.impl.client.AntiCheat;
import dev.chun.mod.modules.impl.client.BaritoneModule;
import dev.chun.mod.modules.impl.client.ClickGui;
import dev.chun.mod.modules.impl.client.ClientSetting;
import dev.chun.mod.modules.impl.client.Fonts;
import dev.chun.mod.modules.impl.client.HUD;
import dev.chun.mod.modules.impl.client.IRC;
import dev.chun.mod.modules.impl.client.ItemsCounter;
import dev.chun.mod.modules.impl.client.TextRadar;
import dev.chun.mod.modules.impl.combat.AntiCrawl;
import dev.chun.mod.modules.impl.combat.AntiPhase;
import dev.chun.mod.modules.impl.combat.AntiRegear;
import dev.chun.mod.modules.impl.combat.AntiWeak;
import dev.chun.mod.modules.impl.combat.Aura;
import dev.chun.mod.modules.impl.combat.AutoAnchor;
import dev.chun.mod.modules.impl.combat.AutoCrystal;
import dev.chun.mod.modules.impl.combat.AutoMend;
import dev.chun.mod.modules.impl.combat.AutoPush;
import dev.chun.mod.modules.impl.combat.AutoRegear;
import dev.chun.mod.modules.impl.combat.AutoTrap;
import dev.chun.mod.modules.impl.combat.AutoWeb;
import dev.chun.mod.modules.impl.combat.Blocker;
import dev.chun.mod.modules.impl.combat.Breaker;
import dev.chun.mod.modules.impl.combat.Burrow;
import dev.chun.mod.modules.impl.combat.CevBreaker;
import dev.chun.mod.modules.impl.combat.Criticals;
import dev.chun.mod.modules.impl.combat.HoleFiller;
import dev.chun.mod.modules.impl.combat.Offhand;
import dev.chun.mod.modules.impl.combat.Panic;
import dev.chun.mod.modules.impl.combat.PistonCrystal;
import dev.chun.mod.modules.impl.combat.Quiver;
import dev.chun.mod.modules.impl.combat.SelfTrap;
import dev.chun.mod.modules.impl.combat.Surround;
import dev.chun.mod.modules.impl.exploit.AntiHunger;
import dev.chun.mod.modules.impl.exploit.AntiPacket;
import dev.chun.mod.modules.impl.exploit.Blink;
import dev.chun.mod.modules.impl.exploit.BowBomb;
import dev.chun.mod.modules.impl.exploit.ChorusControl;
import dev.chun.mod.modules.impl.exploit.ChunkESP;
import dev.chun.mod.modules.impl.exploit.Clip;
import dev.chun.mod.modules.impl.exploit.InfiniteTrident;
import dev.chun.mod.modules.impl.exploit.MaceSpoof;
import dev.chun.mod.modules.impl.exploit.NoResourcePack;
import dev.chun.mod.modules.impl.exploit.PacketControl;
import dev.chun.mod.modules.impl.exploit.Phase;
import dev.chun.mod.modules.impl.exploit.PingSpoof;
import dev.chun.mod.modules.impl.exploit.RocketExtend;
import dev.chun.mod.modules.impl.exploit.ServerLagger;
import dev.chun.mod.modules.impl.exploit.SkinFlicker;
import dev.chun.mod.modules.impl.exploit.TeleportLogger;
import dev.chun.mod.modules.impl.exploit.XCarry;
import dev.chun.mod.modules.impl.misc.AutoEZ;
import dev.chun.mod.modules.impl.misc.AutoKit;
import dev.chun.mod.modules.impl.misc.AutoLog;
import dev.chun.mod.modules.impl.misc.AutoReconnect;
import dev.chun.mod.modules.impl.misc.BedCrafter;
// import dev.luminous.mod.modules.impl.misc.Bot;
import dev.chun.mod.modules.impl.misc.ChatAppend;
import dev.chun.mod.modules.impl.misc.ExtraTab;
import dev.chun.mod.modules.impl.misc.FakePlayer;
import dev.chun.mod.modules.impl.misc.Friend;
import dev.chun.mod.modules.impl.misc.KillEffect;
import dev.chun.mod.modules.impl.misc.LavaFiller;
import dev.chun.mod.modules.impl.misc.NoSound;
import dev.chun.mod.modules.impl.misc.NoTerrainScreen;
import dev.chun.mod.modules.impl.misc.Nuker;
import dev.chun.mod.modules.impl.misc.PacketLogger;
import dev.chun.mod.modules.impl.misc.Punctuation;
import dev.chun.mod.modules.impl.misc.ShulkerViewer;
import dev.chun.mod.modules.impl.misc.Spammer;
import dev.chun.mod.modules.impl.misc.Tips;
import dev.chun.mod.modules.impl.movement.AntiVoid;
import dev.chun.mod.modules.impl.movement.AutoWalk;
import dev.chun.mod.modules.impl.movement.BlockStrafe;
import dev.chun.mod.modules.impl.movement.ElytraFly;
import dev.chun.mod.modules.impl.movement.EntityControl;
import dev.chun.mod.modules.impl.movement.FastFall;
import dev.chun.mod.modules.impl.movement.FastSwim;
import dev.chun.mod.modules.impl.movement.FastWeb;
import dev.chun.mod.modules.impl.movement.Flatten;
import dev.chun.mod.modules.impl.movement.Fly;
import dev.chun.mod.modules.impl.movement.HoleSnap;
import dev.chun.mod.modules.impl.movement.MovementSync;
import dev.chun.mod.modules.impl.movement.NoJumpDelay;
import dev.chun.mod.modules.impl.movement.NoSlow;
import dev.chun.mod.modules.impl.movement.PacketFly;
import dev.chun.mod.modules.impl.movement.SafeWalk;
import dev.chun.mod.modules.impl.movement.Scaffold;
import dev.chun.mod.modules.impl.movement.Speed;
import dev.chun.mod.modules.impl.movement.Sprint;
import dev.chun.mod.modules.impl.movement.Step;
import dev.chun.mod.modules.impl.movement.Strafe;
import dev.chun.mod.modules.impl.movement.VClip;
import dev.chun.mod.modules.impl.movement.Velocity;
import dev.chun.mod.modules.impl.player.AirPlace;
import dev.chun.mod.modules.impl.player.AntiEffects;
import dev.chun.mod.modules.impl.player.AutoArmor;
import dev.chun.mod.modules.impl.player.AutoPearl;
import dev.chun.mod.modules.impl.player.AutoPot;
import dev.chun.mod.modules.impl.player.AutoTool;
import dev.chun.mod.modules.impl.player.FreeLook;
import dev.chun.mod.modules.impl.player.Freecam;
import dev.chun.mod.modules.impl.player.InteractTweaks;
import dev.chun.mod.modules.impl.player.NoFall;
import dev.chun.mod.modules.impl.player.PacketEat;
import dev.chun.mod.modules.impl.player.PacketMine;
import dev.chun.mod.modules.impl.player.Replenish;
import dev.chun.mod.modules.impl.player.Sorter;
import dev.chun.mod.modules.impl.player.TimerModule;
import dev.chun.mod.modules.impl.player.Yaw;
import dev.chun.mod.modules.impl.render.Ambience;
import dev.chun.mod.modules.impl.render.AspectRatio;
import dev.chun.mod.modules.impl.render.BreakESP;
import dev.chun.mod.modules.impl.render.CameraClip;
import dev.chun.mod.modules.impl.render.Chams;
import dev.chun.mod.modules.impl.render.Crosshair;
import dev.chun.mod.modules.impl.render.ESP;
import dev.chun.mod.modules.impl.render.Fov;
import dev.chun.mod.modules.impl.render.HighLight;
import dev.chun.mod.modules.impl.render.HoleESP;
import dev.chun.mod.modules.impl.render.LogoutSpots;
import dev.chun.mod.modules.impl.render.MotionCamera;
import dev.chun.mod.modules.impl.render.NameTags;
import dev.chun.mod.modules.impl.render.NoRender;
import dev.chun.mod.modules.impl.render.PhaseESP;
import dev.chun.mod.modules.impl.render.PlaceRender;
import dev.chun.mod.modules.impl.render.PopChams;
import dev.chun.mod.modules.impl.render.ShaderModule;
import dev.chun.mod.modules.impl.render.TotemParticle;
import dev.chun.mod.modules.impl.render.Tracers;
import dev.chun.mod.modules.impl.render.Trajectories;
import dev.chun.mod.modules.impl.render.ViewModel;
import dev.chun.mod.modules.impl.render.Xray;
import dev.chun.mod.modules.impl.render.Zoom;
import dev.chun.mod.modules.settings.Setting;
import dev.chun.mod.modules.settings.impl.BindSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.lwjgl.opengl.GL11;
import dev.chun.mod.modules.impl.client.HoverSound;
import dev.chun.mod.modules.impl.client.HudEditor;
import dev.chun.mod.modules.impl.combat.AntiCobweb;
import dev.chun.mod.modules.impl.combat.AntiPiston;
import dev.chun.mod.modules.impl.combat.AutoMace;
import dev.chun.mod.modules.impl.combat.BackTrack;
import dev.chun.mod.modules.impl.combat.FlatGroundMace;
import dev.chun.mod.modules.impl.combat.MaceAura;
import dev.chun.mod.modules.impl.combat.MaceGodPlus;
import dev.chun.mod.modules.impl.combat.MainHand;
import dev.chun.mod.modules.impl.combat.PlaceMace;
import dev.chun.mod.modules.impl.combat.SpearAura;
import dev.chun.mod.modules.impl.combat.TimerSpear;
import dev.chun.mod.modules.impl.exploit.AutoMacePlus;
import dev.chun.mod.modules.impl.exploit.WindChargeMace;
import dev.chun.mod.modules.impl.hud.ArmorHud;
import dev.chun.mod.modules.impl.hud.ArrayListHud;
import dev.chun.mod.modules.impl.hud.BackpackContents;
import dev.chun.mod.modules.impl.hud.ChatPosition;
import dev.chun.mod.modules.impl.hud.CircularStatusHud;
import dev.chun.mod.modules.impl.hud.CoordsHud;
import dev.chun.mod.modules.impl.hud.DFHealth;
import dev.chun.mod.modules.impl.hud.DFHungerBar;
import dev.chun.mod.modules.impl.hud.DFTitle;
import dev.chun.mod.modules.impl.hud.DFWeapon;
import dev.chun.mod.modules.impl.hud.DirectionHud;
import dev.chun.mod.modules.impl.hud.FPSHud;
import dev.chun.mod.modules.impl.hud.NotificationHud;
import dev.chun.mod.modules.impl.hud.PacketHud;
import dev.chun.mod.modules.impl.hud.PingHud;
import dev.chun.mod.modules.impl.hud.PotionHud;
import dev.chun.mod.modules.impl.hud.ServerInfoHud;
import dev.chun.mod.modules.impl.hud.SpeedHud;
import dev.chun.mod.modules.impl.hud.TPSHud;
import dev.chun.mod.modules.impl.hud.TimeHud;
import dev.chun.mod.modules.impl.hud.Totem;
import dev.chun.mod.modules.impl.hud.WaterMarkHud;
import dev.chun.mod.modules.impl.hud.WelcomerHud;
import dev.chun.mod.modules.impl.misc.AutoXYZ;
import dev.chun.mod.modules.impl.misc.FakeFPS;
import dev.chun.mod.modules.impl.misc.InventoryStyle;
import dev.chun.mod.modules.impl.misc.PopEz;
import dev.chun.mod.modules.impl.misc.Swing;
import dev.chun.mod.modules.impl.movement.AntiCobwebMine;
import dev.chun.mod.modules.impl.movement.AntiSlowFalling;
import dev.chun.mod.modules.impl.movement.LongJump;
import dev.chun.mod.modules.impl.movement.SpearLongJump;
import dev.chun.mod.modules.impl.movement.TargetFollow;
import dev.chun.mod.modules.impl.movement.WindLongJump;
import dev.chun.mod.modules.impl.player.AutoMine;
import dev.chun.mod.modules.impl.player.AutoRotate;
import dev.chun.mod.modules.impl.player.ItemAlert;
import dev.chun.mod.modules.impl.player.TargetLock;
import dev.chun.mod.modules.impl.render.Camera;
import dev.chun.mod.modules.impl.render.ChestPeek;
import dev.chun.mod.modules.impl.render.DamageNumber;
import dev.chun.mod.modules.impl.render.FrameInterp;
import dev.chun.mod.modules.impl.render.Keystrokes;
import dev.chun.mod.modules.impl.render.LowArmorWarning;
import dev.chun.mod.modules.impl.render.LowHealthWarning;
import dev.chun.mod.modules.impl.render.ModelModify;
import dev.chun.mod.modules.impl.render.PlayerModel;
import dev.chun.mod.modules.impl.render.PlayerTrail;
import dev.chun.mod.modules.impl.render.SpearFix;
import dev.chun.mod.modules.impl.render.TwoDESP;
import dev.chun.mod.modules.impl.render.skybox.Skybox;

public class ModuleManager
implements Wrapper {
    private final ArrayList<Module> modules = new ArrayList();
    private final ArrayList<ToggleBanner> toggleBanners = new ArrayList();
    private boolean notificationMouseWasDown = false;
    private boolean interactiveToggleInBanner = false;

    public ArrayList<Module> getModules() {
        return this.modules;
    }

    public ModuleManager() {
        this.init();
    }

    public void init() {
        if (BaritoneUtil.loaded) {
            this.addModule(new BaritoneModule());
        }
        this.addModule(new Panic());
        this.addModule(new AutoKit());
        this.addModule(new Fonts());
        this.addModule(new NoTerrainScreen());
        this.addModule(new AutoCrystal());
        this.addModule(new Ambience());
        this.addModule(new AntiHunger());
        this.addModule(new AntiVoid());
        this.addModule(new AutoWalk());
        this.addModule(new VClip());
        this.addModule(new ExtraTab());
        this.addModule(new AntiWeak());
        this.addModule(new BedCrafter());
        this.addModule(new Friend());
        this.addModule(new AspectRatio());
        this.addModule(new ChunkESP());
        this.addModule(new Aura());
        this.addModule(new PistonCrystal());
        this.addModule(new AutoAnchor());
        this.addModule(new PhaseESP());
        this.addModule(new AutoArmor());
        this.addModule(new Breaker());
        this.addModule(new AutoLog());
        this.addModule(new AutoEZ());
        this.addModule(new SelfTrap());
        this.addModule(new Sorter());
        this.addModule(new AutoMend());
        this.addModule(new AutoPot());
        this.addModule(new AutoPush());
        this.addModule(new Offhand());
        this.addModule(new Nuker());
        this.addModule(new AutoTrap());
        this.addModule(new AutoWeb());
        this.addModule(new Blink());
        this.addModule(new ChorusControl());
        this.addModule(new BlockStrafe());
        this.addModule(new FastSwim());
        this.addModule(new Blocker());
        this.addModule(new Quiver());
        this.addModule(new BowBomb());
        this.addModule(new BreakESP());
        this.addModule(new Burrow());
        this.addModule(new Punctuation());
        this.addModule(new MaceSpoof());
        this.addModule(new CameraClip());
        this.addModule(new ChatAppend());
        this.addModule(new ClickGui());
        this.addModule(new InfiniteTrident());
        this.addModule(new AutoRegear());
        this.addModule(new LavaFiller());
        this.addModule(new AntiPhase());
        this.addModule(new Clip());
        this.addModule(new AntiCheat());
        this.addModule(new IRC());
        this.addModule(new ItemsCounter());
        this.addModule(new Fov());
        this.addModule(new Criticals());
        this.addModule(new CevBreaker());
        this.addModule(new Crosshair());
        this.addModule(new Chams());
        this.addModule(new AntiPacket());
        this.addModule(new AutoReconnect());
        this.addModule(new ESP());
        this.addModule(new HoleESP());
        this.addModule(new Tracers());
        this.addModule(new MovementSync());
        this.addModule(new ElytraFly());
        this.addModule(new PacketLogger());
        this.addModule(new TeleportLogger());
        this.addModule(new SkinFlicker());
        this.addModule(new EntityControl());
        this.addModule(new NameTags());
        this.addModule(new ShulkerViewer());
        this.addModule(new PingSpoof());
        this.addModule(new FakePlayer());
        this.addModule(new Spammer());
        this.addModule(new MotionCamera());
        this.addModule(new HighLight());
        this.addModule(new FastFall());
        this.addModule(new FastWeb());
        this.addModule(new Flatten());
        this.addModule(new Fly());
        this.addModule(new Yaw());
        this.addModule(new Freecam());
        this.addModule(new FreeLook());
        this.addModule(new TimerModule());
        this.addModule(new Tips());
        this.addModule(new ClientSetting());
        this.addModule(new TextRadar());
        this.addModule(new HUD());
        this.addModule(new NoResourcePack());
        this.addModule(new RocketExtend());
        this.addModule(new HoleFiller());
        this.addModule(new HoleSnap());
        this.addModule(new LogoutSpots());
        this.addModule(new AutoTool());
        this.addModule(new Trajectories());
        this.addModule(new KillEffect());
        this.addModule(new AutoPearl());
        this.addModule(new AntiEffects());
        this.addModule(new NoFall());
        this.addModule(new NoRender());
        this.addModule(new NoSlow());
        this.addModule(new NoSound());
        this.addModule(new AirPlace());
        this.addModule(new Xray());
        this.addModule(new PacketEat());
        this.addModule(new PacketFly());
        this.addModule(new PacketMine());
        this.addModule(new PacketControl());
        this.addModule(new Phase());
        this.addModule(new PlaceRender());
        this.addModule(new InteractTweaks());
        this.addModule(new PopChams());
        this.addModule(new Replenish());
        this.addModule(new ServerLagger());
        this.addModule(new Scaffold());
        this.addModule(new ShaderModule());
        this.addModule(new AntiCrawl());
        this.addModule(new AntiRegear());
        this.addModule(new SafeWalk());
        this.addModule(new NoJumpDelay());
        this.addModule(new Speed());
        this.addModule(new Sprint());
        this.addModule(new Strafe());
        this.addModule(new Step());
        this.addModule(new Surround());
        this.addModule(new TotemParticle());
        this.addModule(new Velocity());
        this.addModule(new ViewModel());
        this.addModule(new XCarry());
        this.addModule(new Zoom());
            this.addModule(new HoverSound());
            this.addModule(new HudEditor());
            this.addModule(new AntiCobweb());
            this.addModule(new AntiPiston());
            this.addModule(new AutoMace());
            this.addModule(new BackTrack());
            this.addModule(new FlatGroundMace());
            this.addModule(new MaceAura());
            this.addModule(new MaceGodPlus());
            this.addModule(new MainHand());
            this.addModule(new PlaceMace());
            this.addModule(new SpearAura());
            this.addModule(new TimerSpear());
            this.addModule(new AutoMacePlus());
            this.addModule(new WindChargeMace());
            this.addModule(new ArmorHud());
            this.addModule(new ArrayListHud());
            this.addModule(new BackpackContents());
            this.addModule(new ChatPosition());
            this.addModule(new CircularStatusHud());
            this.addModule(new CoordsHud());
            this.addModule(new DFHealth());
            this.addModule(new DFHungerBar());
            this.addModule(new DFTitle());
            this.addModule(new DFWeapon());
            this.addModule(new DirectionHud());
            this.addModule(new FPSHud());
            this.addModule(new NotificationHud());
            this.addModule(new PacketHud());
            this.addModule(new PingHud());
            this.addModule(new PotionHud());
            this.addModule(new ServerInfoHud());
            this.addModule(new SpeedHud());
            this.addModule(new TPSHud());
            this.addModule(new TimeHud());
            this.addModule(new Totem());
            this.addModule(new WaterMarkHud());
            this.addModule(new WelcomerHud());
            this.addModule(new AutoXYZ());
            this.addModule(new FakeFPS());
            this.addModule(new InventoryStyle());
            this.addModule(new PopEz());
            this.addModule(new Swing());
            this.addModule(new AntiCobwebMine());
            this.addModule(new AntiSlowFalling());
            this.addModule(new LongJump());
            this.addModule(new SpearLongJump());
            this.addModule(new TargetFollow());
            this.addModule(new WindLongJump());
            this.addModule(new AutoMine());
            this.addModule(new AutoRotate());
            this.addModule(new ItemAlert());
            this.addModule(new TargetLock());
            this.addModule(new Camera());
            this.addModule(new ChestPeek());
            this.addModule(new DamageNumber());
            this.addModule(new FrameInterp());
            this.addModule(new Keystrokes());
            this.addModule(new LowArmorWarning());
            this.addModule(new LowHealthWarning());
            this.addModule(new ModelModify());
            this.addModule(new PlayerModel());
            this.addModule(new PlayerTrail());
            this.addModule(new SpearFix());
            this.addModule(new TwoDESP());
            this.addModule(new Skybox());
        this.modules.sort(Comparator.comparing(Mod::getName));
    }

    public void onKeyReleased(int eventKey) {
        if (eventKey == -1 || eventKey == 0) {
            return;
        }
        this.handleKeyEvent(eventKey, false);
    }

    public void onKeyPressed(int eventKey) {
        if (eventKey == -1 || eventKey == 0) {
            return;
        }
        this.handleKeyEvent(eventKey, true);
    }

    private void handleKeyEvent(int key, boolean isPressed) {
        for (Module module : this.modules) {
            BindSetting bindSetting = module.getBindSetting();
            if (bindSetting.getValue() == key) {
                if (isPressed && ModuleManager.mc.currentScreen == null) {
                    module.toggle();
                    bindSetting.holding = true;
                } else if (!isPressed && bindSetting.isHoldEnable() && bindSetting.holding) {
                    module.toggle();
                    bindSetting.holding = false;
                }
            }
            for (Setting setting : module.getSettings()) {
                BindSetting bind;
                if (!(setting instanceof BindSetting) || (bind = (BindSetting)setting).getValue() != key) continue;
                bind.setPressed(isPressed);
            }
        }
    }

    public void onLogin() {
        for (Module module : this.modules) {
            if (!module.isOn()) continue;
            module.onLogin();
        }
    }

    public void onLogout() {
        for (Module module : this.modules) {
            if (!module.isOn()) continue;
            module.onLogout();
        }
    }

    public void onRender2D(DrawContext drawContext) {
        block5: {
            for (Module module : this.modules) {
                if (!module.isOn()) continue;
                try {
                    module.onRender2D(drawContext, mc.getRenderTickCounter().getTickDelta(true));
                }
                catch (Exception e) {
                    e.printStackTrace();
                    if (!ClientSetting.INSTANCE.debug.getValue()) continue;
                    CommandManager.sendMessage("\u00a74An error has occurred (" + module.getName() + " [onRender2D]) Message: [" + e.getMessage() + "]");
                }
            }
            try {
                Aevum.EVENT_BUS.post(Render2DEvent.get(drawContext, mc.getRenderTickCounter().getTickDelta(true)));
            }
            catch (Exception e) {
                e.printStackTrace();
                if (!ClientSetting.INSTANCE.debug.getValue()) break block5;
                CommandManager.sendMessage("\u00a74An error has occurred (Render3DEvent) Message: [" + e.getMessage() + "]");
            }
        }
        this.renderToggleBanners(drawContext);
    }

    public void render3D(MatrixStack matrices) {
        block5: {
            GL11.glEnable((int)2848);
            for (Module module : this.modules) {
                if (!module.isOn()) continue;
                try {
                    module.onRender3D(matrices);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    if (!ClientSetting.INSTANCE.debug.getValue()) continue;
                    CommandManager.sendMessage("\u00a74An error has occurred (" + module.getName() + " [onRender3D]) Message: [" + e.getMessage() + "]");
                }
            }
            try {
                Aevum.EVENT_BUS.post(Render3DEvent.get(matrices, mc.getRenderTickCounter().getTickDelta(true)));
            }
            catch (Exception e) {
                e.printStackTrace();
                if (!ClientSetting.INSTANCE.debug.getValue()) break block5;
                CommandManager.sendMessage("\u00a74An error has occurred (Render3DEvent) Message: [" + e.getMessage() + "]");
            }
        }
        GL11.glDisable((int)2848);
    }

    public void showToggleBanner(Module module, boolean enabled) {
        return;
    }

    private void renderToggleBanners(DrawContext ctx) {
        this.toggleBanners.clear();
    }

    private void renderPotionListLegacy(DrawContext ctx) {
        if (ModuleManager.mc.player == null) {
            return;
        }
        int margin = 14;
        int startX = margin + 2;
        int startY = margin + 92;
        int pillH = 14;
        int pillPad = 6;
        int idx = 0;
        for (StatusEffectInstance se : ModuleManager.mc.player.getStatusEffects()) {
            String name = ((StatusEffect)se.getEffectType().value()).getName().getString();
            int ticks = se.getDuration();
            int totalSec = Math.max(0, ticks / 20);
            int mm = totalSec / 60;
            int ss = totalSec % 60;
            String time = String.format("%d:%02d", mm, ss);
            String text = name + " " + time;
            boolean customFont = FontManager.isCustomFontEnabled();
            int tw = customFont ? (int)FontManager.ui.getWidth(text) : (int)TextUtil.getWidth(text);
            int pillW = tw + pillPad * 2;
            int x = startX;
            int y = startY + idx * (pillH + 4);
            int keyCodec = 180;
            Render2DUtil.drawRoundedRect(ctx.getMatrices(), x, y, pillW, pillH, 4.0f, new Color(255, 255, 255, keyCodec));
            Render2DUtil.drawRoundedStroke(ctx.getMatrices(), x, y, pillW, pillH, 4.0f, new Color(220, 224, 230, 160), 48);
            int tx = x + pillPad;
            double ty = (double)y + (double)((float)pillH - (customFont ? FontManager.ui.getFontHeight() : TextUtil.getHeight())) / 2.0;
            TextUtil.drawString(ctx, text, tx, ty, new Color(30, 30, 30).getRGB(), customFont);
            if (++idx < 5) continue;
            break;
        }
    }

    private void renderPotionBanners(DrawContext ctx) {
    }

    private String formatDuration(int ticks) {
        int totalSec = Math.max(0, ticks / 20);
        int mm = totalSec / 60;
        int ss = totalSec % 60;
        return String.format("%d:%02d", mm, ss);
    }

    private Module getModuleByDisplayName(String name) {
        for (Module m : this.modules) {
            if (!m.getDisplayName().equalsIgnoreCase(name) && !m.getName().equalsIgnoreCase(name)) continue;
            return m;
        }
        return null;
    }

    public void addModule(Module module) {
        this.modules.add(module);
    }

    public Module getModuleByName(String string) {
        for (Module module : this.modules) {
            if (!module.getName().equalsIgnoreCase(string)) continue;
            return module;
        }
        return null;
    }

    private static class ToggleBanner {
        public final String name;
        public final boolean enabled;
        public final long start;

        public ToggleBanner(String name, boolean enabled, long start) {
            this.name = name;
            this.enabled = enabled;
            this.start = start;
        }
    }
}

