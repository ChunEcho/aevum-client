/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.SoundEvent
 *  net.minecraft.SoundEvents
 */
package dev.aevum.mod.modules.impl.misc;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.PlaySoundEvent;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import java.util.ArrayList;
import net.minecraft.SoundEvent;
import net.minecraft.SoundEvents;

public class NoSound
extends Module {
    static final ArrayList<SoundEvent> armor = new ArrayList();
    public static NoSound INSTANCE;
    private final BooleanSetting equip = this.add(new BooleanSetting("ArmorEquip", true));
    private final BooleanSetting explode = this.add(new BooleanSetting("Explode", true));
    private final BooleanSetting attack = this.add(new BooleanSetting("Attack", true));
    private final BooleanSetting teleport = this.add(new BooleanSetting("Teleport", true));
    private final BooleanSetting throwConfig = this.add(new BooleanSetting("Throw", true));
    private final BooleanSetting potion = this.add(new BooleanSetting("Potion", true));
    private final BooleanSetting elytra = this.add(new BooleanSetting("Elytra", true));

    public NoSound() {
        super("NoSound", Module.Category.Misc);
        this.setChinese("\u53bb\u9664\u58f0\u97f3");
        INSTANCE = this;
    }

    @EventListener
    public void onPlaySound(PlaySoundEvent event) {
        if (this.equip.getValue()) {
            for (SoundEvent se : armor) {
                if (event.sound.method_4775() != se.method_14833()) continue;
                event.cancel();
                return;
            }
        }
        if (this.explode.getValue() && (event.sound.method_4775() == ((SoundEvent)SoundEvents.GENERIC_EXPLODE.comp_349()).method_14833() || event.sound.method_4775() == SoundEvents.DRAGON_FIREBALL_EXPLODE.method_14833())) {
            event.cancel();
            return;
        }
        if (this.attack.getValue() && (event.sound.method_4775() == SoundEvents.PLAYER_ATTACK_WEAK.method_14833() || event.sound.method_4775() == SoundEvents.PLAYER_ATTACK_KNOCKBACK.method_14833() || event.sound.method_4775() == SoundEvents.PLAYER_ATTACK_STRONG.method_14833())) {
            event.cancel();
        }
        if (this.teleport.getValue() && event.sound.method_4775() == SoundEvents.PLAYER_TELEPORT.method_14833()) {
            event.cancel();
        }
        if (this.potion.getValue() && event.sound.method_4775() == SoundEvents.SPLASH_POTION_BREAK.method_14833()) {
            event.cancel();
        }
        if (this.elytra.getValue() && event.sound.method_4775() == SoundEvents.ELYTRA_FLYING.method_14833()) {
            event.cancel();
        }
        if (this.throwConfig.getValue() && (event.sound.method_4775() == SoundEvents.ENDER_PEARL_THROW.method_14833() || event.sound.method_4775() == SoundEvents.EGG_THROW.method_14833() || event.sound.method_4775() == SoundEvents.EXPERIENCE_BOTTLE_THROW.method_14833() || event.sound.method_4775() == SoundEvents.SNOWBALL_THROW.method_14833() || event.sound.method_4775() == SoundEvents.SPLASH_POTION_THROW.method_14833())) {
            event.cancel();
        }
    }

    static {
        armor.add((SoundEvent)SoundEvents.ARMOR_EQUIP_NETHERITE.comp_349());
        armor.add((SoundEvent)SoundEvents.ARMOR_EQUIP_TURTLE.comp_349());
        armor.add((SoundEvent)SoundEvents.ARMOR_EQUIP_CHAIN.comp_349());
        armor.add((SoundEvent)SoundEvents.ARMOR_EQUIP_ELYTRA.comp_349());
        armor.add((SoundEvent)SoundEvents.ARMOR_EQUIP_DIAMOND.comp_349());
        armor.add((SoundEvent)SoundEvents.ARMOR_EQUIP_GOLD.comp_349());
        armor.add((SoundEvent)SoundEvents.ARMOR_EQUIP_IRON.comp_349());
        armor.add((SoundEvent)SoundEvents.ARMOR_EQUIP_LEATHER.comp_349());
        armor.add((SoundEvent)SoundEvents.ARMOR_EQUIP_GENERIC.comp_349());
    }
}

