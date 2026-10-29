package dev.chun.mod.modules.impl.combat;

import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.PacketEvent;
import dev.chun.api.events.impl.UpdateEvent;
import dev.chun.api.utils.Wrapper;
import dev.chun.api.utils.render.Render3DUtil;
import dev.chun.asm.accessors.IEntity;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.ColorSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class BackTrack extends Module {
   private final CopyOnWriteArrayList<BackTrack.QueuedPacket> packets = new CopyOnWriteArrayList<>();
   private final SliderSetting delay = this.add(new SliderSetting("Delay", 100.0, 0.0, 1000.0, 1.0).setSuffix("ms"));
   private final SliderSetting maxQueued = this.add(new SliderSetting("MaxQueued", 200.0, 0.0, 1000.0, 1.0));
   private final BooleanSetting render = this.add(new BooleanSetting("Render", true));
   private final ColorSetting fill = this.add(new ColorSetting("Fill", new Color(189, 153, 255, 30), () -> this.render.getValue()).injectBoolean(true));
   private final ColorSetting outline = this.add(new ColorSetting("Outline", new Color(189, 153, 255, 120), () -> this.render.getValue()).injectBoolean(true));
   private Vec3d serverPos;

   public BackTrack() {
      super("BackTrack", Module.Category.Exploit);
      this.setChinese("\u6253\u6ed1\u56de\u6eaf");
   }

   @Override
   public String getInfo() {
      return this.delay.getValueInt() + "ms";
   }

   @Override
   public void onDisable() {
      this.flush();
   }

   @Override
   public void onLogout() {
      this.packets.clear();
      this.serverPos = null;
   }

   @EventListener
   public void onPacketReceive(PacketEvent.Receive event) {
      if (!nullCheck()) {
         Packet packet = event.getPacket();
         if (packet instanceof PlayerPositionLookS2CPacket) {
            this.flush();
         } else {
            Entity target = this.getTarget();
            if (target != null) {
               if (!(packet instanceof EntityS2CPacket entityPacket2)) {
                  if (packet instanceof EntityPositionS2CPacket entityPacket) {
                     if (entityPacket.getEntityId() != target.getId()) {
                        return;
                     }

                     this.updateServerPos(entityPacket);
                     this.queue(packet);
                     event.cancel();
                  }
               } else {
                  Entity entity = entityPacket2.getEntity(mc.world);
                  if (entity != null && entity == target) {
                     this.updateServerPos(entityPacket2, target);
                     this.queue(packet);
                     event.cancel();
                  }
               }
            }
         }
      }
   }

   @EventListener
   public void onUpdate(UpdateEvent event) {
      this.update();
   }

   @Override
   public void onRender3D(MatrixStack matrixStack) {
      this.update();
      if (this.render.getValue() && !Module.nullCheck()) {
         Entity target = this.getTarget();
         if (target != null && this.serverPos != null) {
            Box box = ((IEntity)target).getDimensions().getBoxAt(this.serverPos).expand(0.0, 0.1, 0.0);
            Render3DUtil.draw3DBox(matrixStack, box, this.fill.getValue(), this.outline.getValue(), this.outline.booleanValue, this.fill.booleanValue);
         }
      }
   }

   private void update() {
      if (nullCheck()) {
         this.packets.clear();
         this.serverPos = null;
      } else {
         long now = System.currentTimeMillis();
         this.packets.removeIf(p -> p.trySend(now));
      }
   }

   private void flush() {
      if (nullCheck()) {
         this.packets.clear();
         this.serverPos = null;
      } else {
         for (BackTrack.QueuedPacket p : this.packets) {
            p.apply();
         }

         this.packets.clear();
         this.serverPos = null;
      }
   }

   private void queue(Packet packet) {
      int max = this.maxQueued.getValueInt();
      if (max > 0) {
         this.packets.add(new BackTrack.QueuedPacket(packet, System.currentTimeMillis() + (long)this.delay.getValueInt()));

         while (this.packets.size() > max) {
            BackTrack.QueuedPacket overflow = this.packets.remove(0);
            overflow.apply();
         }
      }
   }

   private Entity getTarget() {
      if (Aura.target != null) {
         return Aura.target;
      } else {
         return mc.crosshairTarget instanceof EntityHitResult EntityHitResult ? EntityHitResult.getEntity() : null;
      }
   }

   private void updateServerPos(EntityPositionS2CPacket packet) {
      this.serverPos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
   }

   private void updateServerPos(EntityS2CPacket packet, Entity entity) {
      if (this.serverPos == null) {
         this.serverPos = entity.getPos();
      }

      double dx = (double)packet.getDeltaX() / 4096.0;
      double dy = (double)packet.getDeltaY() / 4096.0;
      double dz = (double)packet.getDeltaZ() / 4096.0;
      this.serverPos = this.serverPos.add(dx, dy, dz);
   }

   private static class QueuedPacket {
      private final Packet packet;
      private final long sendAt;

      public QueuedPacket(Packet packet, long sendAt) {
         this.packet = packet;
         this.sendAt = sendAt;
      }

      public boolean trySend(long now) {
         if (now >= this.sendAt) {
            this.apply();
            return true;
         } else {
            return false;
         }
      }

      public void apply() {
         if (this.packet != null) {
            this.packet.apply(Wrapper.mc.getNetworkHandler());
         }
      }
   }
}
