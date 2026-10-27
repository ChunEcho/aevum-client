/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.EntityType
 *  net.minecraft.LightningBolt
 *  net.minecraft.Player
 *  net.minecraft.Level
 *  net.minecraft.SoundEvents
 *  net.minecraft.SoundSource
 */
package dev.aevum.mod.modules.impl.misc;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.DeathEvent;
import dev.aevum.api.utils.math.MathUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.Entity;
import net.minecraft.EntityType;
import net.minecraft.LightningBolt;
import net.minecraft.Player;
import net.minecraft.Level;
import net.minecraft.SoundEvents;
import net.minecraft.SoundSource;

public class KillEffect
extends Module {
    private final BooleanSetting lightning = this.add(new BooleanSetting("Lightning", true));
    private final BooleanSetting levelUp = this.add(new BooleanSetting("LevelUp", true).setParent());
    private final SliderSetting lMaxPitch = this.add(new SliderSetting("LMaxPitch", 1.0, 0.0, 2.0, 0.1, this.levelUp::isOpen));
    private final SliderSetting lMinPitch = this.add(new SliderSetting("LMinPitch", 1.0, 0.0, 2.0, 0.1, this.levelUp::isOpen));
    private final BooleanSetting trident = this.add(new BooleanSetting("Trident", false).setParent());
    private final SliderSetting tMaxPitch = this.add(new SliderSetting("TMaxPitch", 1.0, 0.0, 2.0, 0.1, this.trident::isOpen));
    private final SliderSetting tMinPitch = this.add(new SliderSetting("TMinPitch", 1.0, 0.0, 2.0, 0.1, this.trident::isOpen));
    private final SliderSetting factor = this.add(new SliderSetting("Factor", 1.0, 1.0, 10.0, 1.0));

    public KillEffect() {
        super("KillEffect", Module.Category.Misc);
        this.setChinese("\u51fb\u6740\u6548\u679c");
    }

    @EventListener
    public void onPlayerDeath(DeathEvent event) {
        if (KillEffect.nullCheck()) {
            return;
        }
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        int i = 0;
        while ((double)i < this.factor.getValue()) {
            this.doEffect(player);
            ++i;
        }
    }

    private void doEffect(Player player) {
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        if (this.lightning.getValue()) {
            LightningBolt lightningEntity = new LightningBolt(EntityType.LIGHTNING_BOLT, (Level)KillEffect.mc.level);
            lightningEntity.method_30634(x, y, z);
            lightningEntity.method_24203(x, y, z);
            KillEffect.mc.level.method_53875((Entity)lightningEntity);
        }
        if (this.levelUp.getValue()) {
            KillEffect.mc.level.method_43128((Player)KillEffect.mc.player, x, y, z, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 100.0f, MathUtil.random(this.lMinPitch.getValueFloat(), this.lMaxPitch.getValueFloat()));
        }
        if (this.trident.getValue()) {
            KillEffect.mc.level.method_60511((Player)KillEffect.mc.player, x, y, z, SoundEvents.TRIDENT_THUNDER, SoundSource.MASTER, 999.0f, MathUtil.random(this.tMinPitch.getValueFloat(), this.tMaxPitch.getValueFloat()));
        }
    }
}

