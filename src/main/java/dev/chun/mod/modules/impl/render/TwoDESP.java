package dev.chun.mod.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.chun.Aevum;
import dev.chun.api.utils.math.MathUtil;
import dev.chun.api.utils.render.TextUtil;
import dev.chun.core.impl.FontManager;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.ColorSetting;
import dev.chun.mod.modules.settings.impl.EnumSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4d;

public class TwoDESP extends Module {
   public static TwoDESP INSTANCE;
   private final EnumSetting<TwoDESP.TwoDESPMode> page = this.add(new EnumSetting<>("Settings", TwoDESP.TwoDESPMode.Target));
   public final ColorSetting armorDuraColor = this.add(
      new ColorSetting("Armor Dura Color", new Color(12425727), () -> this.page.getValue() == TwoDESP.TwoDESPMode.Color)
   );
   public final ColorSetting hHealth = this.add(
      new ColorSetting("High Health Color", new Color(189, 153, 255, 255), () -> this.page.getValue() == TwoDESP.TwoDESPMode.Color)
   );
   public final ColorSetting mHealth = this.add(
      new ColorSetting("Mid Health Color", new Color(189, 153, 255, 255), () -> this.page.getValue() == TwoDESP.TwoDESPMode.Color)
   );
   public final ColorSetting lHealth = this.add(
      new ColorSetting("Low Health Color", new Color(189, 153, 255, 255), () -> this.page.getValue() == TwoDESP.TwoDESPMode.Color)
   );
   private final BooleanSetting outline = this.add(new BooleanSetting("Outline", true, () -> this.page.getValue() == TwoDESP.TwoDESPMode.Setting));
   private final BooleanSetting renderHealth = this.add(new BooleanSetting("renderHealth", true, () -> this.page.getValue() == TwoDESP.TwoDESPMode.Setting));
   private final BooleanSetting renderArmor = this.add(new BooleanSetting("Armor Dura", true, () -> this.page.getValue() == TwoDESP.TwoDESPMode.Setting));
   private final BooleanSetting renderEffects = this.add(new BooleanSetting("Effects", true, () -> this.page.getValue() == TwoDESP.TwoDESPMode.Setting));
   private final SliderSetting durascale = this.add(new SliderSetting("DuraScale", 1.0, 0.0, 2.0, 0.1, () -> this.renderArmor.getValue()));
   private final BooleanSetting drawItem = this.add(new BooleanSetting("draw Item Name", true, () -> this.page.getValue() == TwoDESP.TwoDESPMode.Setting));
   private final BooleanSetting drawItemC = this.add(
      new BooleanSetting("draw Item Count", true, () -> this.page.getValue() == TwoDESP.TwoDESPMode.Setting && this.drawItem.getValue())
   );
   public final ColorSetting countColor = this.add(
      new ColorSetting("Item Count Color", new Color(189, 153, 255, 255), () -> this.page.getValue() == TwoDESP.TwoDESPMode.Color && this.drawItemC.getValue())
   );
   public final ColorSetting textcolor = this.add(
      new ColorSetting("Item Name Color", new Color(189, 153, 255, 255), () -> this.page.getValue() == TwoDESP.TwoDESPMode.Color && this.drawItem.getValue())
   );
   private final BooleanSetting font = this.add(new BooleanSetting("CustomFont", true, () -> this.page.getValue() == TwoDESP.TwoDESPMode.Setting));
   private final BooleanSetting players = this.add(new BooleanSetting("Players", true, () -> this.page.getValue() == TwoDESP.TwoDESPMode.Target));
   private final BooleanSetting friends = this.add(new BooleanSetting("Friends", true, () -> this.page.getValue() == TwoDESP.TwoDESPMode.Target));
   private final BooleanSetting crystals = this.add(new BooleanSetting("Crystals", true, () -> this.page.getValue() == TwoDESP.TwoDESPMode.Target));
   private final BooleanSetting creatures = this.add(new BooleanSetting("Creatures", false, () -> this.page.getValue() == TwoDESP.TwoDESPMode.Target));
   private final BooleanSetting monsters = this.add(new BooleanSetting("Monsters", false, () -> this.page.getValue() == TwoDESP.TwoDESPMode.Target));
   private final BooleanSetting ambients = this.add(new BooleanSetting("Ambients", false, () -> this.page.getValue() == TwoDESP.TwoDESPMode.Target));
   private final BooleanSetting others = this.add(new BooleanSetting("Others", false, () -> this.page.getValue() == TwoDESP.TwoDESPMode.Target));
   private final ColorSetting playersC = this.add(new ColorSetting("PlayersBox", new Color(12425727), () -> this.page.getValue() == TwoDESP.TwoDESPMode.Color));
   private final ColorSetting friendsC = this.add(new ColorSetting("FriendsBox", new Color(12425727), () -> this.page.getValue() == TwoDESP.TwoDESPMode.Color));
   private final ColorSetting crystalsC = this.add(
      new ColorSetting("CrystalsBox", new Color(12425727), () -> this.page.getValue() == TwoDESP.TwoDESPMode.Color)
   );
   private final ColorSetting creaturesC = this.add(
      new ColorSetting("CreaturesBox", new Color(12425727), () -> this.page.getValue() == TwoDESP.TwoDESPMode.Color)
   );
   private final ColorSetting monstersC = this.add(
      new ColorSetting("MonstersBox", new Color(12425727), () -> this.page.getValue() == TwoDESP.TwoDESPMode.Color)
   );
   private final ColorSetting ambientsC = this.add(
      new ColorSetting("AmbientsBox", new Color(12425727), () -> this.page.getValue() == TwoDESP.TwoDESPMode.Color)
   );
   private final ColorSetting othersC = this.add(new ColorSetting("OthersBox", new Color(12425727), () -> this.page.getValue() == TwoDESP.TwoDESPMode.Color));

   public TwoDESP() {
      super("TwoDESP", Module.Category.Render);
      this.setChinese("2D\u900f\u89c6");
      INSTANCE = this;
   }

   @Override
   public void onRender2D(DrawContext context, float tickDelta) {
      if (!nullCheck()) {
         Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
         RenderSystem.enableBlend();
         RenderSystem.setShader(GameRenderer::getPositionColorProgram);
         BufferBuilder bufferBuilder = null;
         boolean hasVertices = false;

         for (Entity ent : Aevum.THREAD.getEntities()) {
            if (this.shouldRender(ent)) {
               if (bufferBuilder == null) {
                  bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
               }

               if (this.drawBox(bufferBuilder, ent, matrix, context)) {
                  hasVertices = true;
               }
            }
         }

         if (hasVertices && bufferBuilder != null) {
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
         }

         RenderSystem.disableBlend();

         for (Entity entx : Aevum.THREAD.getEntities()) {
            if (this.shouldRender(entx)) {
               this.drawText(entx, context);
            }
         }
      }
   }

   public boolean shouldRender(Entity entity) {
      if (entity == null) {
         return false;
      } else if (mc.player == null) {
         return false;
      } else if (entity instanceof PlayerEntity) {
         if (entity == mc.player && mc.options.getPerspective().isFirstPerson()) {
            return false;
         } else {
            return Aevum.FRIEND.isFriend((PlayerEntity)entity) ? this.friends.getValue() : this.players.getValue();
         }
      } else if (entity instanceof EndCrystalEntity) {
         return this.crystals.getValue();
      } else {
         return switch (entity.getType().getSpawnGroup()) {
            case CREATURE, WATER_CREATURE -> this.creatures.getValue();
            case MONSTER -> this.monsters.getValue();
            case AMBIENT, WATER_AMBIENT -> this.ambients.getValue();
            default -> this.others.getValue();
         };
      }
   }

   public Color getEntityColor(Entity entity) {
      if (entity == null) {
         return new Color(31743);
      } else if (entity instanceof PlayerEntity PlayerEntity) {
         return Aevum.FRIEND.isFriend(PlayerEntity) ? this.friendsC.getValue() : this.playersC.getValue();
      } else if (entity instanceof EndCrystalEntity) {
         return this.crystalsC.getValue();
      } else {
         return switch (entity.getType().getSpawnGroup()) {
            case CREATURE, WATER_CREATURE -> this.creaturesC.getValue();
            case MONSTER -> this.monstersC.getValue();
            case AMBIENT, WATER_AMBIENT -> this.ambientsC.getValue();
            default -> this.othersC.getValue();
         };
      }
   }

   public boolean drawBox(BufferBuilder bufferBuilder, Entity ent, Matrix4f matrix, DrawContext context) {
      double x = MathUtil.interpolate(ent.lastRenderX, ent.getX(), (double)mc.getRenderTickCounter().getTickDelta(true));
      double y = MathUtil.interpolate(ent.lastRenderY, ent.getY(), (double)mc.getRenderTickCounter().getTickDelta(true));
      double z = MathUtil.interpolate(ent.lastRenderZ, ent.getZ(), (double)mc.getRenderTickCounter().getTickDelta(true));
      Box axisAlignedBB2 = ent.getBoundingBox();
      Box axisAlignedBB3 = new Box(
         axisAlignedBB2.minX - ent.getX() + x - 0.05,
         axisAlignedBB2.minY - ent.getY() + y,
         axisAlignedBB2.minZ - ent.getZ() + z - 0.05,
         axisAlignedBB2.maxX - ent.getX() + x + 0.05,
         axisAlignedBB2.maxY - ent.getY() + y + 0.15,
         axisAlignedBB2.maxZ - ent.getZ() + z + 0.05
      );
      Vec3d[] vectors = new Vec3d[]{
         new Vec3d(axisAlignedBB3.minX, axisAlignedBB3.minY, axisAlignedBB3.minZ),
         new Vec3d(axisAlignedBB3.minX, axisAlignedBB3.maxY, axisAlignedBB3.minZ),
         new Vec3d(axisAlignedBB3.maxX, axisAlignedBB3.minY, axisAlignedBB3.minZ),
         new Vec3d(axisAlignedBB3.maxX, axisAlignedBB3.maxY, axisAlignedBB3.minZ),
         new Vec3d(axisAlignedBB3.minX, axisAlignedBB3.minY, axisAlignedBB3.maxZ),
         new Vec3d(axisAlignedBB3.minX, axisAlignedBB3.maxY, axisAlignedBB3.maxZ),
         new Vec3d(axisAlignedBB3.maxX, axisAlignedBB3.minY, axisAlignedBB3.maxZ),
         new Vec3d(axisAlignedBB3.maxX, axisAlignedBB3.maxY, axisAlignedBB3.maxZ)
      };
      Color col = this.getEntityColor(ent);
      Vector4d position = null;

      for (Vec3d vector : vectors) {
         vector = TextUtil.worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
         if (vector.z > 0.0 && vector.z < 1.0) {
            if (position == null) {
               position = new Vector4d(vector.x, vector.y, vector.z, 0.0);
            }

            position.x = Math.min(vector.x, position.x);
            position.y = Math.min(vector.y, position.y);
            position.z = Math.max(vector.x, position.z);
            position.w = Math.max(vector.y, position.w);
         }
      }

      if (position == null) {
         return false;
      } else {
         double posX = position.x;
         double posY = position.y;
         double endPosX = position.z;
         double endPosY = position.w;
         if (this.outline.getValue()) {
            this.drawRectToBuffer(bufferBuilder, matrix, (float)(posX - 1.0), (float)posY, 1.5F, (float)(endPosY - posY + 0.5), Color.BLACK);
            this.drawRectToBuffer(bufferBuilder, matrix, (float)(posX - 1.0), (float)(posY - 0.5), (float)(endPosX - posX + 1.5), 1.5F, Color.BLACK);
            this.drawRectToBuffer(bufferBuilder, matrix, (float)(endPosX - 1.0), (float)posY, 1.5F, (float)(endPosY - posY + 0.5), Color.BLACK);
            this.drawRectToBuffer(bufferBuilder, matrix, (float)(posX - 1.0), (float)(endPosY - 1.0), (float)(endPosX - posX + 1.5), 1.5F, Color.BLACK);
            this.drawRectToBuffer(bufferBuilder, matrix, (float)(posX - 0.5), (float)posY, 0.5F, (float)(endPosY - posY), col);
            this.drawRectToBuffer(bufferBuilder, matrix, (float)posX, (float)(endPosY - 0.5), (float)(endPosX - posX), 0.5F, col);
            this.drawRectToBuffer(bufferBuilder, matrix, (float)(posX - 0.5), (float)posY, (float)(endPosX - posX + 0.5), 0.5F, col);
            this.drawRectToBuffer(bufferBuilder, matrix, (float)(endPosX - 0.5), (float)posY, 0.5F, (float)(endPosY - posY), col);
         }

         if (ent instanceof LivingEntity lent && lent.getHealth() != 0.0F && this.renderHealth.getValue()) {
            this.drawRectToBuffer(bufferBuilder, matrix, (float)(posX - 4.0), (float)posY, 1.0F, (float)(endPosY - posY), Color.BLACK);
            Color color = this.getcolor(lent.getHealth());
            float healthHeight = (float)(endPosY - (endPosY + (posY - endPosY) * (double)lent.getHealth() / (double)lent.getMaxHealth()));
            this.drawRectToBuffer(bufferBuilder, matrix, (float)(posX - 4.0), (float)(endPosY - (double)healthHeight), 1.0F, healthHeight, color);
         }

         if (ent instanceof PlayerEntity player && this.renderArmor.getValue()) {
            double height = (endPosY - posY) / 4.0;
            ArrayList<ItemStack> stacks = new ArrayList<>();
            stacks.add((ItemStack)player.getInventory().armor.get(3));
            stacks.add((ItemStack)player.getInventory().armor.get(2));
            stacks.add((ItemStack)player.getInventory().armor.get(1));
            stacks.add((ItemStack)player.getInventory().armor.get(0));
            int i = -1;

            for (ItemStack armor : stacks) {
               i++;
               if (!armor.isEmpty()) {
                  float durability = (float)(armor.getMaxDamage() - armor.getDamage());
                  int percent = (int)(durability / (float)armor.getMaxDamage() * 100.0F);
                  double finalH = height * (double)(percent / 100) * (double)this.durascale.getValueFloat();
                  finalH = Math.min(height, finalH);
                  this.drawRectToBuffer(
                     bufferBuilder,
                     matrix,
                     (float)(endPosX + 1.5),
                     (float)((double)((float)posY) + height * (double)i + 1.2 * (double)(i + 1)),
                     1.5F,
                     (float)finalH,
                     this.armorDuraColor.getValue()
                  );
               }
            }
         }

         return true;
      }
   }

   private void drawRectToBuffer(BufferBuilder bufferBuilder, Matrix4f matrix, float x, float y, float width, float height, Color color) {
      float a = (float)color.getAlpha() / 255.0F;
      float r = (float)color.getRed() / 255.0F;
      float g = (float)color.getGreen() / 255.0F;
      float b = (float)color.getBlue() / 255.0F;
      bufferBuilder.vertex(matrix, x, y, 0.0F).color(r, g, b, a);
      bufferBuilder.vertex(matrix, x, y + height, 0.0F).color(r, g, b, a);
      bufferBuilder.vertex(matrix, x + width, y + height, 0.0F).color(r, g, b, a);
      bufferBuilder.vertex(matrix, x + width, y, 0.0F).color(r, g, b, a);
   }

   public void drawText(Entity ent, DrawContext context) {
      double x = MathUtil.interpolate(ent.lastRenderX, ent.getX(), (double)mc.getRenderTickCounter().getTickDelta(true));
      double y = MathUtil.interpolate(ent.lastRenderY, ent.getY(), (double)mc.getRenderTickCounter().getTickDelta(true));
      double z = MathUtil.interpolate(ent.lastRenderZ, ent.getZ(), (double)mc.getRenderTickCounter().getTickDelta(true));
      Box axisAlignedBB2 = ent.getBoundingBox();
      Box axisAlignedBB3 = new Box(
         axisAlignedBB2.minX - ent.getX() + x - 0.05,
         axisAlignedBB2.minY - ent.getY() + y,
         axisAlignedBB2.minZ - ent.getZ() + z - 0.05,
         axisAlignedBB2.maxX - ent.getX() + x + 0.05,
         axisAlignedBB2.maxY - ent.getY() + y + 0.15,
         axisAlignedBB2.maxZ - ent.getZ() + z + 0.05
      );
      Vec3d[] vectors = new Vec3d[]{
         new Vec3d(axisAlignedBB3.minX, axisAlignedBB3.minY, axisAlignedBB3.minZ),
         new Vec3d(axisAlignedBB3.minX, axisAlignedBB3.maxY, axisAlignedBB3.minZ),
         new Vec3d(axisAlignedBB3.maxX, axisAlignedBB3.minY, axisAlignedBB3.minZ),
         new Vec3d(axisAlignedBB3.maxX, axisAlignedBB3.maxY, axisAlignedBB3.minZ),
         new Vec3d(axisAlignedBB3.minX, axisAlignedBB3.minY, axisAlignedBB3.maxZ),
         new Vec3d(axisAlignedBB3.minX, axisAlignedBB3.maxY, axisAlignedBB3.maxZ),
         new Vec3d(axisAlignedBB3.maxX, axisAlignedBB3.minY, axisAlignedBB3.maxZ),
         new Vec3d(axisAlignedBB3.maxX, axisAlignedBB3.maxY, axisAlignedBB3.maxZ)
      };
      Vector4d position = null;

      for (Vec3d vector : vectors) {
         vector = TextUtil.worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
         if (vector.z > 0.0 && vector.z < 1.0) {
            if (position == null) {
               position = new Vector4d(vector.x, vector.y, vector.z, 0.0);
            }

            position.x = Math.min(vector.x, position.x);
            position.y = Math.min(vector.y, position.y);
            position.z = Math.max(vector.x, position.z);
            position.w = Math.max(vector.y, position.w);
         }
      }

      if (position != null) {
         double posX = position.x;
         double posY = position.y;
         double endPosX = position.z;
         double endPosY = position.w;
         if (ent instanceof ItemEntity entity && this.drawItem.getValue()) {
            float diff = (float)((endPosX - posX) / 2.0);
            float textWidth = FontManager.ui.getWidth(entity.getDisplayName().getString());
            float tagX = (float)(posX + (double)diff - (double)(textWidth / 2.0F));
            int count = entity.getStack().getCount();
            context.drawText(mc.textRenderer, entity.getDisplayName().getString(), (int)tagX, (int)(posY - 10.0), this.textcolor.getValue().getRGB(), false);
            if (this.drawItemC.getValue()) {
               context.drawText(
                  mc.textRenderer,
                  " x" + count,
                  (int)(tagX + (float)mc.textRenderer.getWidth(entity.getDisplayName().getString())),
                  (int)posY - 10,
                  this.countColor.getValue().getRGB(),
                  false
               );
            }
         }

         if (ent instanceof PlayerEntity player) {
            if (this.renderArmor.getValue()) {
               double height = (endPosY - posY) / 4.0;
               ArrayList<ItemStack> stacks = new ArrayList<>();
               stacks.add((ItemStack)player.getInventory().armor.get(3));
               stacks.add((ItemStack)player.getInventory().armor.get(2));
               stacks.add((ItemStack)player.getInventory().armor.get(1));
               stacks.add((ItemStack)player.getInventory().armor.get(0));
               int i = -1;

               for (ItemStack armor : stacks) {
                  i++;
                  if (!armor.isEmpty()) {
                     float durability = (float)(armor.getMaxDamage() - armor.getDamage());
                     int percent = (int)(durability / (float)armor.getMaxDamage() * 100.0F);
                     double finalH = height * ((double)percent / 100.0) * (double)this.durascale.getValueFloat();
                     finalH = Math.min(height, finalH);
                     context.drawItem(armor, (int)(endPosX + 4.0), (int)(posY + height * (double)i + 1.2 * (double)(i + 1) + finalH / 2.0));
                  }
               }
            }

            if (this.renderEffects.getValue()) {
               float currentY = (float)posY - 20.0F;

               for (StatusEffectInstance effect : player.getStatusEffects()) {
                  StatusEffect statusEffect = (StatusEffect)effect.getEffectType().value();
                  String effectName = statusEffect.getName().getString();
                  int duration = effect.getDuration();
                  String timeText;
                  if (duration >= 72000) {
                     int hours = duration / 72000;
                     int minutes = duration % 72000 / 1200;
                     timeText = String.format("%dh%dm", hours, minutes);
                  } else if (duration >= 1200) {
                     int minutes2 = duration / 1200;
                     int seconds = duration % 1200 / 20;
                     timeText = String.format("%dm%ds", minutes2, seconds);
                  } else {
                     int seconds2 = duration / 20;
                     timeText = String.format("%ds", seconds2);
                  }

                  String text = effectName + " " + timeText;
                  int color = statusEffect.getColor();
                  float textWidth2 = this.font.getValue() ? FontManager.ui.getWidth(text) : TextUtil.getWidth(text);
                  float effectX = (float)(posX + (endPosX - posX) / 2.0 - (double)textWidth2 / 2.0);
                  TextUtil.drawString(context, text, (double)effectX, (double)currentY, color, this.font.getValue(), false);
                  currentY -= 12.0F;
               }
            }
         }
      }
   }

   public Color getcolor(float health) {
      if (health >= 20.0F) {
         return this.hHealth.getValue();
      } else {
         return 20.0F > health && health > 10.0F ? this.mHealth.getValue() : this.lHealth.getValue();
      }
   }

   public static enum TwoDESPMode {
      Setting,
      Target,
      Color;

      private TwoDESPMode() {
      }
   }
}
