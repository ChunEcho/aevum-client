package dev.chun.mod.modules.impl.render;

import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.PacketEvent;
import dev.chun.api.events.impl.UpdateEvent;
import dev.chun.api.utils.math.Timer;
import dev.chun.api.utils.render.ColorUtil;
import dev.chun.api.utils.render.Render3DUtil;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.impl.client.HUD;
import dev.chun.mod.modules.impl.combat.Criticals;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.ColorSetting;
import dev.chun.mod.modules.settings.impl.EnumSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.InteractType;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class DamageNumber extends Module {
   private final EnumSetting<DamageNumber.ColorMode> colorMode = this.add(new EnumSetting<>("ColorMode", DamageNumber.ColorMode.Sync));
   private final ColorSetting startColor = this.add(
      new ColorSetting("StartColor", new Color(189, 153, 255, 255), () -> this.colorMode.getValue() == DamageNumber.ColorMode.Custom)
   );
   private final ColorSetting endColor = this.add(
      new ColorSetting("EndColor", new Color(189, 153, 255, 40), () -> this.colorMode.getValue() == DamageNumber.ColorMode.Custom)
   );
   private final BooleanSetting onlyPlayers = this.add(new BooleanSetting("OnlyPlayers", true));
   private final SliderSetting lifeTime = this.add(new SliderSetting("LifeTime", 900.0, 200.0, 3000.0, 50.0));
   private final SliderSetting scale = this.add(new SliderSetting("Scale", 1.1, 0.4, 3.0, 0.05));
   private final SliderSetting rise = this.add(new SliderSetting("Rise", 0.9, 0.1, 2.5, 0.05));
   private final SliderSetting random = this.add(new SliderSetting("RandomOffset", 0.18, 0.0, 0.6, 0.01));
   private final SliderSetting range = this.add(new SliderSetting("TrackRange", 12.0, 2.0, 24.0, 0.5));
   private final Map<Integer, DamageNumber.TargetHealth> trackedTargets = new HashMap<>();
   private final List<DamageNumber.DamagePopup> popups = new ArrayList<>();
   private final DecimalFormat format = new DecimalFormat("0.0");

   public DamageNumber() {
      super("DamageNumber", Module.Category.Render);
      this.setChinese("\u4f24\u5bb3\u6570\u5b57");
   }

   @Override
   public void onEnable() {
      this.trackedTargets.clear();
      this.popups.clear();
   }

   @Override
   public void onDisable() {
      this.trackedTargets.clear();
      this.popups.clear();
   }

   @EventListener
   public void onPacketSend(PacketEvent.Send send) {
      Packet<?> packet;
      if (!Module.nullCheck() && (packet = send.getPacket()) instanceof PlayerInteractEntityC2SPacket) {
         PlayerInteractEntityC2SPacket PlayerInteractEntityC2SPacket = (PlayerInteractEntityC2SPacket)packet;
         if (Criticals.getInteractType(PlayerInteractEntityC2SPacket) == InteractType.ATTACK) {
            if (Criticals.getEntity(PlayerInteractEntityC2SPacket) instanceof LivingEntity class_2825) {
               if (!this.onlyPlayers.getValue() || class_2825 instanceof PlayerEntity) {
                  this.trackedTargets.put(class_2825.getId(), new DamageNumber.TargetHealth(class_2825, this.getHealth(class_2825), new Timer()));
               }
            }
         }
      }
   }

   @EventListener
   public void onUpdate(UpdateEvent updateEvent) {
      if (!nullCheck()) {
         this.popups.removeIf(damagePopup -> damagePopup.timer.passedMs((double)((long)this.lifeTime.getValue())));
         Iterator<Entry<Integer, DamageNumber.TargetHealth>> iterator = this.trackedTargets.entrySet().iterator();

         while (iterator.hasNext()) {
            DamageNumber.TargetHealth targetHealth = iterator.next().getValue();
            LivingEntity entity = targetHealth.entity;
            if (entity == null || !entity.isAlive() || entity.isRemoved() || (double)mc.player.distanceTo(entity) > this.range.getValue()) {
               iterator.remove();
            } else if (targetHealth.timer.passedMs(1400.0)) {
               iterator.remove();
            } else {
               float health = this.getHealth(entity);
               float damage = targetHealth.lastHealth - health;
               if (damage > 0.05F) {
                  this.popups.add(new DamageNumber.DamagePopup(this.getPopupPos(entity), damage, new Timer()));
                  targetHealth.lastHealth = health;
                  targetHealth.timer.reset();
               } else if (!(health <= targetHealth.lastHealth)) {
                  targetHealth.lastHealth = health;
                  targetHealth.timer.reset();
               }
            }
         }
      }
   }

   @Override
   public void onRender3D(MatrixStack MatrixStack) {
      if (!Module.nullCheck() && !this.popups.isEmpty()) {
         long n = (long)this.lifeTime.getValue();

         for (DamageNumber.DamagePopup damagePopup : this.popups) {
            float clamp = MathHelper.clamp((float)damagePopup.timer.getMs() / (float)n, 0.0F, 1.0F);
            Vec3d method_15364 = damagePopup.pos.add(0.0, this.rise.getValue() * (double)clamp, 0.0);
            Render3DUtil.drawText3D(
               Text.of(this.format.format((double)damagePopup.damage)),
               method_15364.x,
               method_15364.y,
               method_15364.z,
               0.0,
               0.0,
               this.scale.getValue(),
               this.getPopupColor(clamp).getRGB()
            );
         }
      }
   }

   private Vec3d getPopupPos(LivingEntity LivingEntity) {
      return new Vec3d(
         LivingEntity.getX() + (Math.random() - 0.5) * this.random.getValue(),
         LivingEntity.getY() + (double)LivingEntity.getHeight() + 0.3,
         LivingEntity.getZ() + (Math.random() - 0.5) * this.random.getValue()
      );
   }

   private float getHealth(LivingEntity LivingEntity) {
      return LivingEntity.getHealth() + LivingEntity.getAbsorptionAmount();
   }

   private Color getPopupColor(float n) {
      return ColorUtil.fadeColor(
         this.colorMode.getValue() == DamageNumber.ColorMode.Sync ? this.getHudColor(0) : this.startColor.getValue(),
         this.colorMode.getValue() == DamageNumber.ColorMode.Sync ? this.getHudColor(180) : this.endColor.getValue(),
         (double)n
      );
   }

   private Color getHudColor(int n) {
      return HUD.INSTANCE != null ? new Color(HUD.INSTANCE.getColor((double)n), true) : Color.WHITE;
   }

   private static enum ColorMode {
      Sync,
      Custom;

      private ColorMode() {
      }
   }

   static record DamagePopup(Vec3d pos, float damage, Timer timer) {
   }

   private static class TargetHealth {
      private final LivingEntity entity;
      private float lastHealth;
      private final Timer timer;

      private TargetHealth(LivingEntity entity, float lastHealth, Timer timer) {
         this.entity = entity;
         this.lastHealth = lastHealth;
         this.timer = timer;
      }
   }
}
