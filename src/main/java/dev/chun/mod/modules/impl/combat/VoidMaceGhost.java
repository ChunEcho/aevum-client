package dev.chun.mod.modules.impl.combat;

import dev.chun.Aevum;
import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.ClientTickEvent;
import dev.chun.api.utils.math.MathUtil;
import dev.chun.api.utils.math.Timer;
import dev.chun.api.utils.player.EntityUtil;
import dev.chun.api.utils.player.InventoryUtil;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.enums.SwingSide;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.EnumSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class VoidMaceGhost extends Module {
    public static VoidMaceGhost INSTANCE;

    private final EnumSetting<Page> page = this.add(new EnumSetting<>("Page", Page.Target));

    private final SliderSetting targetRange = this.add(
        new SliderSetting("TargetRange", 8.0, 1.0, 30.0, 0.5, () -> this.page.is(Page.Target)).setSuffix("m")
    );
    private final SliderSetting attackRange = this.add(
        new SliderSetting("AttackRange", 6.0, 1.0, 10.0, 0.1, () -> this.page.is(Page.Target)).setSuffix("m")
    );
    private final BooleanSetting players = this.add(new BooleanSetting("Players", true, () -> this.page.is(Page.Target)));
    private final BooleanSetting mobs = this.add(new BooleanSetting("Mobs", false, () -> this.page.is(Page.Target)));
    private final BooleanSetting ignoreFriends = this.add(new BooleanSetting("IgnoreFriends", true, () -> this.page.is(Page.Target)));
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", false, () -> this.page.is(Page.Target)));

    private final SliderSetting diveHeight = this.add(
        new SliderSetting("DiveHeight", 26.0, 21.0, 60.0, 1.0, () -> this.page.is(Page.Path)).setSuffix("m")
    );
    private final BooleanSetting belowWorld = this.add(
        new BooleanSetting("BelowWorld", false, () -> this.page.is(Page.Path))
    );
    private final SliderSetting voidY = this.add(
        new SliderSetting("VoidY", -70.0, -200.0, 64.0, 1.0, () -> this.page.is(Page.Path) && this.belowWorld.getValue()).setSuffix("y")
    );

    private final BooleanSetting waterCancel = this.add(new BooleanSetting("WaterCancel", true, () -> this.page.is(Page.Safe)));
    private final BooleanSetting flyCancel = this.add(new BooleanSetting("FlyCancel", true, () -> this.page.is(Page.Safe)));
    private final BooleanSetting totemGuard = this.add(new BooleanSetting("TotemGuard", true, () -> this.page.is(Page.Safe)));
    private final SliderSetting totemMinHp = this.add(
        new SliderSetting("TotemMinHp", 4.0, 0.0, 20.0, 0.5, () -> this.page.is(Page.Safe) && this.totemGuard.getValue()).setSuffix("hp")
    );
    private final BooleanSetting groundOnly = this.add(new BooleanSetting("GroundOnly", false, () -> this.page.is(Page.Safe)));
    private final BooleanSetting spaceCheck = this.add(new BooleanSetting("SpaceCheck", true, () -> this.page.is(Page.Safe)));

    private final BooleanSetting ghostHand = this.add(new BooleanSetting("GhostHand", true, () -> this.page.is(Page.Misc)));
    private final BooleanSetting breachSwap = this.add(
        new BooleanSetting("BreachSwap", false, () -> this.page.is(Page.Misc) && this.ghostHand.getValue())
    );
    private final BooleanSetting ghostInventory = this.add(
        new BooleanSetting("GhostInventory", false, () -> this.page.is(Page.Misc) && this.ghostHand.getValue())
    );
    private final BooleanSetting autoMace = this.add(
        new BooleanSetting("AutoMace", true, () -> this.page.is(Page.Misc) && !this.ghostHand.getValue())
    );
    private final BooleanSetting syncBack = this.add(new BooleanSetting("SyncBack", true, () -> this.page.is(Page.Misc)));
    private final BooleanSetting chargeWait = this.add(new BooleanSetting("ChargeWait", true, () -> this.page.is(Page.Misc)));
    private final SliderSetting attackDelay = this.add(
        new SliderSetting("AttackDelay", 2000.0, 0.0, 5000.0, 50.0, () -> this.page.is(Page.Misc)).setSuffix("ms")
    );
    private final BooleanSetting swing = this.add(new BooleanSetting("Swing", true, () -> this.page.is(Page.Misc)));
    private final BooleanSetting debug = this.add(new BooleanSetting("Debug", false, () -> this.page.is(Page.Misc)));

    private final Timer attackTimer = new Timer();
    private double lastFall;
    private String lastReason = "";

    public VoidMaceGhost() {
        super("VoidMaceGhost", "fake dive for mace smash fall distance, restored without fall damage", Module.Category.Mace);
        this.setChinese("\u865a\u7a7a\u9524Ghost");
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        if (this.lastFall > 0.0) {
            return String.format("%.0f", this.lastFall);
        }
        return this.lastReason.isEmpty() ? "idle" : this.lastReason;
    }

    @Override
    public void onEnable() {
        this.lastFall = 0.0;
        this.lastReason = "";
        this.attackTimer.reset();
    }

    @EventListener
    public void onTick(ClientTickEvent event) {
        if (event.isPost() || nullCheck() || mc.getNetworkHandler() == null) {
            return;
        }
        if (!this.attackTimer.passedMs(this.attackDelay.getValue())) {
            return;
        }
        if (this.chargeWait.getValue() && mc.player.getAttackCooldownProgress(0.5f) < 0.99f) {
            this.reason("charge");
            return;
        }
        if (this.groundOnly.getValue() && !mc.player.isOnGround()) {
            this.reason("air");
            return;
        }
        if (this.totemGuard.getValue() && EntityUtil.getHealth(mc.player) <= this.totemMinHp.getValue()) {
            this.reason("lowhp");
            return;
        }
        if (this.flyCancel.getValue() && mc.player.isFallFlying()) {
            this.reason("elytra");
            return;
        }
        if (!this.hasMaceReady()) {
            this.reason("nomace");
            return;
        }
        Entity target = this.findTarget();
        if (target == null) {
            this.reason("notarget");
            return;
        }
        if (this.rotate.getValue()) {
            Aevum.ROTATION.lookAt(MathUtil.getClosestPointToBox(mc.player.getEyePos(), target.getBoundingBox()));
        }
        if (this.execute(target)) {
            this.attackTimer.reset();
            this.lastReason = "";
        }
    }

    private boolean execute(Entity target) {
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        double bottom = mc.world.getBottomY();
        double dest;
        if (this.belowWorld.getValue()) {
            dest = Math.min(this.voidY.getValue(), bottom - 4.0);
        } else {
            dest = y - this.diveHeight.getValue();
            if (dest < bottom + 1.0) {
                dest = bottom + 1.0;
            }
        }
        double fall = y - dest;
        if (fall < 20.0) {
            this.reason("lowspace");
            return false;
        }
        if (this.spaceCheck.getValue() && !this.airColumn(x, dest, y, z)) {
            this.reason("blocked");
            return false;
        }
        if (this.waterCancel.getValue() && this.fluidBetween(dest, y, x, z)) {
            this.reason("water");
            return false;
        }

        this.sendPos(x, dest, z, false);
        this.sendPos(x, y, z, false);

        if (this.ghostHand.getValue()) {
            this.ghostAttack(target);
        } else {
            this.normalAttack(target);
        }

        this.sendPos(x, y, z, false);

        this.lastFall = fall;
        this.note("smash fall=" + String.format("%.1f", fall) + " dest=" + String.format("%.1f", dest));
        return true;
    }

    private void normalAttack(Entity entity) {
        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
        mc.player.resetLastAttackedTicks();
        if (this.swing.getValue()) {
            EntityUtil.swingHand(Hand.MAIN_HAND, SwingSide.All);
        }
    }

    private void ghostAttack(Entity entity) {
        int slot = this.findMaceSlot();
        if (slot == -1) {
            this.normalAttack(entity);
            return;
        }
        boolean inventoryMode = this.ghostInventory.getValue() && slot >= 36;
        int selected = mc.player.getInventory().selectedSlot;
        this.swapTo(slot, inventoryMode);
        try {
            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
            mc.player.resetLastAttackedTicks();
            if (this.swing.getValue()) {
                EntityUtil.swingHand(Hand.MAIN_HAND, SwingSide.All);
            }
        } finally {
            this.swapBack(slot, selected, inventoryMode);
        }
    }

    private void swapTo(int slot, boolean inventoryMode) {
        if (inventoryMode) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private void swapBack(int slot, int selected, boolean inventoryMode) {
        if (inventoryMode) {
            InventoryUtil.inventorySwap(slot, selected);
            if (this.syncBack.getValue()) {
                EntityUtil.syncInventory();
            }
        } else if (selected >= 0 && selected != mc.player.getInventory().selectedSlot) {
            mc.player.getInventory().selectedSlot = selected;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(selected));
        }
    }

    private boolean hasMaceReady() {
        if (this.ghostHand.getValue()) {
            return this.findMaceSlot() != -1;
        }
        if (mc.player.getMainHandStack().getItem() == Items.MACE) {
            return true;
        }
        return this.autoMace.getValue() && this.findMaceSlot() != -1;
    }

    private int findMaceSlot() {
        boolean breach = this.breachSwap.getValue() && this.ghostHand.getValue();
        if (this.ghostHand.getValue() && this.ghostInventory.getValue()) {
            for (int i = 0; i < 36; i++) {
                if (this.isMace(mc.player.getInventory().getStack(i), breach)) {
                    return i < 9 ? i + 36 : i;
                }
            }
            return -1;
        }
        for (int i = 0; i < 9; i++) {
            if (this.isMace(mc.player.getInventory().getStack(i), breach)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isMace(ItemStack stack, boolean requireBreach) {
        if (stack.getItem() != Items.MACE) {
            return false;
        }
        if (!requireBreach) {
            return true;
        }
        return EnchantmentHelper.getEnchantments(stack)
            .getEnchantments()
            .contains(mc.world.getRegistryManager().get(Enchantments.BREACH.getRegistryRef()).getEntry(Enchantments.BREACH).get());
    }

    private void sendPos(double x, double y, double z, boolean onGround) {
        if (mc.getNetworkHandler() == null) {
            return;
        }
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, onGround));
    }

    private boolean airColumn(double x, double fromY, double toY, double z) {
        if (mc.world == null) {
            return false;
        }
        double lo = Math.min(fromY, toY);
        double hi = Math.max(fromY, toY);
        for (double cy = lo; cy <= hi; cy += 1.0) {
            BlockPos pos = BlockPos.ofFloored(x, cy, z);
            if (!mc.world.getBlockState(pos).getCollisionShape(mc.world, pos).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean fluidBetween(double fromY, double toY, double x, double z) {
        if (mc.world == null) {
            return true;
        }
        double lo = Math.min(fromY, toY);
        double hi = Math.max(fromY, toY);
        for (double cy = lo; cy <= hi; cy += 1.0) {
            if (!mc.world.getBlockState(BlockPos.ofFloored(x, cy, z)).getFluidState().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private Entity findTarget() {
        double range = this.targetRange.getValue();
        double rangeSq = range * range;
        double attack = this.attackRange.getValue();
        Entity best = null;
        double bestSq = Double.MAX_VALUE;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || !this.players.getValue()) {
                continue;
            }
            if (!this.acceptPlayer(player)) {
                continue;
            }
            double d = mc.player.squaredDistanceTo(player);
            if (d > rangeSq || d > attack * attack || d >= bestSq) {
                continue;
            }
            bestSq = d;
            best = player;
        }
        if (best != null) {
            return best;
        }
        if (!this.mobs.getValue()) {
            return null;
        }
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || entity instanceof EndCrystalEntity) {
                continue;
            }
            if (!(entity instanceof MobEntity) || entity instanceof AnimalEntity) {
                continue;
            }
            if (!entity.isAlive() || entity.isRemoved()) {
                continue;
            }
            double d = mc.player.squaredDistanceTo(entity);
            if (d > rangeSq || d > attack * attack || d >= bestSq) {
                continue;
            }
            bestSq = d;
            best = entity;
        }
        return best;
    }

    private boolean acceptPlayer(PlayerEntity player) {
        if (!player.isAlive() || player.isRemoved() || player.getHealth() <= 0.0f) {
            return false;
        }
        if (player.isSpectator()) {
            return false;
        }
        if (this.ignoreFriends.getValue()) {
            try {
                if (Aevum.FRIEND.isFriend(player)) {
                    return false;
                }
            } catch (Throwable ignored) {
            }
        }
        return Double.isFinite(player.getX()) && Double.isFinite(player.getZ());
    }

    private void reason(String why) {
        if (!why.equals(this.lastReason)) {
            this.lastReason = why;
            if (this.debug.getValue()) {
                this.sendMessage("\u00a77[VoidMaceGhost] \u5361\u4f4f: " + why);
            }
        }
    }

    private void note(String message) {
        if (this.debug.getValue()) {
            this.sendMessage("\u00a77[VoidMaceGhost] " + message);
        }
    }

    public enum Page {
        Target, Path, Safe, Misc
    }
}