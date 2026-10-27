/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Player
 *  net.minecraft.BlockPos
 *  net.minecraft.AABB
 *  net.minecraft.HitResult
 *  net.minecraft.Vec3
 *  net.minecraft.Packet
 *  net.minecraft.Font
 *  net.minecraft.GuiGraphics
 *  net.minecraft.SoundEvents
 *  net.minecraft.SoundSource
 *  net.minecraft.BlockHitResult
 *  net.minecraft.PoseStack
 *  net.minecraft.ClientboundPlayerChatPacket
 *  net.minecraft.ClientboundSystemChatPacket
 */
package dev.aevum.mod.modules.impl.misc;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.math.MathUtil;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.render.Render3DUtil;
import dev.aevum.api.utils.render.TextUtil;
import dev.aevum.api.utils.world.BlockPosX;
import dev.aevum.core.impl.CommandManager;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BindSetting;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import dev.aevum.mod.modules.settings.impl.StringSetting;
import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.minecraft.Player;
import net.minecraft.BlockPos;
import net.minecraft.AABB;
import net.minecraft.HitResult;
import net.minecraft.Vec3;
import net.minecraft.Packet;
import net.minecraft.Font;
import net.minecraft.GuiGraphics;
import net.minecraft.SoundEvents;
import net.minecraft.SoundSource;
import net.minecraft.BlockHitResult;
import net.minecraft.PoseStack;
import net.minecraft.ClientboundPlayerChatPacket;
import net.minecraft.ClientboundSystemChatPacket;

public class Punctuation
extends Module {
    private final BooleanSetting sound = this.add(new BooleanSetting("Sound", true));
    private final SliderSetting clearTime = this.add(new SliderSetting("ClearTime", 10.0, 0.0, 100.0, 0.1).setSuffix("s"));
    private final ColorSetting color = this.add(new ColorSetting("Color", new Color(255, 255, 255, 100)));
    private final BindSetting enemySpot = this.add(new BindSetting("EnemySpot", -1));
    private final StringSetting key = this.add(new StringSetting("EncryptKey", "IDKWTFTHIS"));
    private final ConcurrentHashMap<String, Spot> waypoint = new ConcurrentHashMap();
    private boolean pressed = false;

    public Punctuation() {
        super("Punctuation", Module.Category.Misc);
        this.setChinese("\u6807\u70b9");
    }

    public static SecretKeySpec getKey(String myKey) {
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            return new SecretKeySpec(key, "AES");
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onDisable() {
        this.waypoint.clear();
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        this.waypoint.values().removeIf(t -> t.timer.passedS(this.clearTime.getValue()));
        if (this.enemySpot.isPressed() && Punctuation.mc.screen == null) {
            HitResult hitResult;
            if (!this.pressed && (hitResult = mc.getCameraEntity().method_5745(256.0, 0.0f, false)) instanceof BlockHitResult) {
                BlockHitResult blockHitResult = (BlockHitResult)hitResult;
                BlockPos pos = blockHitResult.method_17777();
                this.waypoint.put("Local", new Spot("Local", new BlockPosX(pos.method_10263(), pos.method_10264(), pos.getZ()), this.color.getValue(), new Timer()));
                CommandManager.sendMessage("Marked locally at \u00a7r(" + pos.method_10263() + ", " + pos.method_10264() + ", " + pos.getZ() + ")");
            }
            this.pressed = true;
        } else {
            this.pressed = false;
        }
    }

    @Override
    public void onRender2D(GuiGraphics context, float tickDelta) {
        for (Spot spot : this.waypoint.values()) {
            Vec3 vector = TextUtil.worldSpaceToScreenSpace(spot.pos.method_46558().method_1031(0.0, 1.0, 0.0));
            String text = "\u00a7a" + spot.name + " \u00a7f(" + spot.pos.method_10263() + ", " + spot.pos.method_10264() + ", " + spot.pos.getZ() + ")";
            if (!(vector.z > 0.0) || !(vector.z < 1.0)) continue;
            double posX = vector.x;
            double posY = vector.y;
            double endPosX = Math.max(vector.x, vector.z);
            float diff = (float)(endPosX - posX) / 2.0f;
            float textWidth = Punctuation.mc.font.method_1727(text);
            float tagX = (float)((posX + (double)diff - (double)(textWidth / 4.0f)) * 1.0);
            context.method_51448().method_22903();
            context.method_51448().method_22905(0.5f, 0.5f, 1.0f);
            Font textRenderer = Punctuation.mc.font;
            int n = (int)tagX * 2;
            Objects.requireNonNull(Punctuation.mc.font);
            context.method_51433(textRenderer, text, n, (int)(posY - 11.0 + 10.799999999999999) * 2, -1, true);
            context.method_51448().method_22909();
        }
    }

    @Override
    public void onRender3D(PoseStack matrixStack) {
        for (Spot spot : this.waypoint.values()) {
            Render3DUtil.drawFill(matrixStack, new AABB((double)spot.pos.method_10263() + 0.25, -60.0, (double)spot.pos.getZ() + 0.25, (double)spot.pos.method_10263() + 0.75, 320.0, (double)spot.pos.getZ() + 0.75), spot.color);
        }
    }

    @EventListener
    private void PacketReceive(PacketEvent.Receive event) {
        ClientboundSystemChatPacket packet;
        if (Punctuation.nullCheck()) {
            return;
        }
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof ClientboundSystemChatPacket && (packet = (ClientboundSystemChatPacket)packet2).comp_763() != null) {
            mc.execute(() -> this.receive(packet.comp_763().getString()));
        }
        if ((packet2 = event.getPacket()) instanceof ClientboundPlayerChatPacket) {
            ClientboundPlayerChatPacket chatPacket = (ClientboundPlayerChatPacket)packet2;
            if (chatPacket.comp_1103() != null) {
                mc.execute(() -> this.receive(chatPacket.comp_1103().getString()));
            } else {
                mc.execute(() -> this.receive(chatPacket.comp_1102().comp_1090()));
            }
        }
    }

    private void receive(String s) {
        try {
            Pattern pattern;
            Matcher matcher;
            if (s == null) {
                return;
            }
            String decrypt = this.Decrypt(s.replaceAll("\u00a7[0-9a-fk-or]", "").replaceAll("<[^>]*> ", ""));
            if (decrypt == null) {
                return;
            }
            if (decrypt.contains("EnemyHere") && (matcher = (pattern = Pattern.compile("\\{(.*?)}")).matcher(decrypt)).find()) {
                String pos = matcher.group(1);
                String[] posSplit = pos.split(",");
                if (posSplit.length == 3) {
                    if (this.sound.getValue()) {
                        Punctuation.mc.level.method_8396((Player)Punctuation.mc.player, Punctuation.mc.player.method_24515(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 100.0f, 1.9f);
                    }
                    String xString = posSplit[0];
                    String yString = posSplit[1];
                    String zString = posSplit[2];
                    pattern = Pattern.compile("<(.*?)>");
                    matcher = pattern.matcher(s);
                    if (!this.isNumeric(xString)) {
                        return;
                    }
                    double x = Double.parseDouble(xString);
                    if (!this.isNumeric(yString)) {
                        return;
                    }
                    double y = Double.parseDouble(yString);
                    if (!this.isNumeric(zString)) {
                        return;
                    }
                    double z = Double.parseDouble(zString);
                    if (matcher.find()) {
                        String sender = matcher.group(1);
                        this.waypoint.put(sender, new Spot(sender, new BlockPosX(x, y, z), this.color.getValue(), new Timer()));
                        CommandManager.sendMessage(sender + " marked at \u00a7r(" + xString + ", " + yString + ", " + zString + ")");
                    } else {
                        this.waypoint.put("" + MathUtil.random(0.0f, 1.0E9f), new Spot("Unknown", new BlockPosX(x, y, z), this.color.getValue(), new Timer()));
                        CommandManager.sendMessage("Unknown marked at \u00a7r(" + xString + ", " + yString + ", " + zString + ")");
                    }
                } else if (posSplit.length == 4) {
                    if (this.sound.getValue()) {
                        Punctuation.mc.level.method_8396((Player)Punctuation.mc.player, Punctuation.mc.player.method_24515(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 100.0f, 1.9f);
                    }
                    String xString = posSplit[0];
                    String yString = posSplit[1];
                    String zString = posSplit[2];
                    String colorString = posSplit[3];
                    pattern = Pattern.compile("<(.*?)>");
                    matcher = pattern.matcher(s);
                    if (!this.isNumeric(xString)) {
                        return;
                    }
                    double x = Double.parseDouble(xString);
                    if (!this.isNumeric(yString)) {
                        return;
                    }
                    double y = Double.parseDouble(yString);
                    if (!this.isNumeric(zString)) {
                        return;
                    }
                    double z = Double.parseDouble(zString);
                    if (!this.isNumeric(colorString)) {
                        return;
                    }
                    double color = Double.parseDouble(colorString);
                    if (matcher.find()) {
                        String sender = matcher.group(1);
                        this.waypoint.put(sender, new Spot(sender, new BlockPosX(x, y, z), new Color((int)color, true), new Timer()));
                        CommandManager.sendMessage(sender + " marked at \u00a7r(" + xString + ", " + yString + ", " + zString + ")");
                    } else {
                        this.waypoint.put("" + MathUtil.random(0.0f, 1.0E9f), new Spot("Unknown", new BlockPosX(x, y, z), new Color((int)color, true), new Timer()));
                        CommandManager.sendMessage("Unknown marked at \u00a7r(" + xString + ", " + yString + ", " + zString + ")");
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    public String Decrypt(String strToDecrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKey = Punctuation.getKey(this.key.getValue());
            byte[] iv = new byte[16];
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(2, (Key)secretKey, ivParams);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(strToDecrypt));
            return new String(original, StandardCharsets.UTF_8);
        }
        catch (Exception e) {
            return null;
        }
    }

    public String Encrypt(String strToEncrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKey = Punctuation.getKey(this.key.getValue());
            byte[] iv = new byte[16];
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(1, (Key)secretKey, ivParams);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        }
        catch (Exception e) {
            return null;
        }
    }

    private record Spot(String name, BlockPos pos, Color color, Timer timer) {
    }
}

