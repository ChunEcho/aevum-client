/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.Component
 *  net.minecraft.GuiMessage
 *  net.minecraft.GuiMessage$Line
 *  net.minecraft.Minecraft
 *  net.minecraft.Font
 *  net.minecraft.GuiGraphics
 *  net.minecraft.ChatComponent
 *  net.minecraft.FormattedCharSequence
 *  net.minecraft.MessageSignature
 *  net.minecraft.GuiMessageTag
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import dev.aevum.api.interfaces.IChatHudHook;
import dev.aevum.api.interfaces.IChatHudLineHook;
import dev.aevum.api.utils.math.FadeUtils;
import dev.aevum.api.utils.render.ColorUtil;
import dev.aevum.asm.accessors.IChatHud;
import dev.aevum.mod.modules.impl.client.ClientSetting;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.Component;
import net.minecraft.GuiMessage;
import net.minecraft.Minecraft;
import net.minecraft.Font;
import net.minecraft.GuiGraphics;
import net.minecraft.ChatComponent;
import net.minecraft.FormattedCharSequence;
import net.minecraft.MessageSignature;
import net.minecraft.GuiMessageTag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ChatComponent.class})
public abstract class MixinChatHud
implements IChatHudHook {
    @Unique
    private int nextMessageId = 0;
    @Unique
    private boolean nextSync;
    @Unique
    private int chatLineIndex;
    @Final
    @Shadow
    private List<GuiMessage.GuiMessage$Line> trimmedMessages;
    @Shadow
    @Final
    private List<GuiMessage> allMessages;

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    public void onInit(Minecraft client, CallbackInfo ci) {
        ((IChatHud)((Object)this)).setMessages(new CopyOnWriteArrayList<GuiMessage>());
        ((IChatHud)((Object)this)).setVisibleMessages(new CopyOnWriteArrayList<GuiMessage.GuiMessage$Line>());
    }

    @Override
    public void aevumClient$addMessage(Component message, int id) {
        this.nextMessageId = id;
        this.nextSync = true;
        this.method_1812(message);
        this.nextSync = false;
        this.nextMessageId = 0;
    }

    @Override
    public void aevumClient$addMessage(Component message) {
        this.nextSync = true;
        this.method_1812(message);
        this.nextSync = false;
    }

    @Override
    public void aevumClient$addMessageOutSync(Component message, int id) {
        this.nextMessageId = id;
        this.method_1812(message);
        this.nextMessageId = 0;
    }

    @Inject(method={"addVisibleMessage"}, at={@At(value="INVOKE", target="Ljava/util/List;add(ILjava/lang/Object;)V", ordinal=0, shift=At.Shift.AFTER)})
    private void onAddMessageAfterNewChatHudLineVisible(GuiMessage message, CallbackInfo ci) {
        IChatHudLineHook line = (IChatHudLineHook)this.trimmedMessages.getFirst();
        if (line != null) {
            line.aevumClient$setMessageId(this.nextMessageId);
            line.aevumClient$setSync(this.nextSync);
            line.aevumClient$setFade(new FadeUtils(ClientSetting.INSTANCE.animationTime.getValueInt()));
        }
    }

    @Inject(method={"addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V"}, at={@At(value="INVOKE", target="Ljava/util/List;add(ILjava/lang/Object;)V", ordinal=0, shift=At.Shift.AFTER)})
    private void onAddMessageAfterNewChatHudLine(GuiMessage message, CallbackInfo ci) {
        IChatHudLineHook line = (IChatHudLineHook)this.allMessages.getFirst();
        if (line != null) {
            line.aevumClient$setMessageId(this.nextMessageId);
            line.aevumClient$setSync(this.nextSync);
            line.aevumClient$setFade(new FadeUtils(ClientSetting.INSTANCE.animationTime.getValueInt()));
        }
    }

    @Inject(at={@At(value="HEAD")}, method={"addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V"})
    private void onAddMessage(Component message, MessageSignature signatureData, GuiMessageTag indicator, CallbackInfo ci) {
        if (this.nextMessageId != 0) {
            this.trimmedMessages.removeIf(msg -> ((IChatHudLineHook)msg).aevumClient$getMessageId() == this.nextMessageId);
            this.allMessages.removeIf(msg -> ((IChatHudLineHook)msg).aevumClient$getMessageId() == this.nextMessageId);
        }
    }

    @Redirect(method={"addVisibleMessage"}, at=@At(value="INVOKE", target="Ljava/util/List;size()I", ordinal=2, remap=false), require=0)
    public int chatLinesSize(List<GuiMessage.GuiMessage$Line> list) {
        return ClientSetting.INSTANCE.isOn() && ClientSetting.INSTANCE.infiniteChat.getValue() ? -2147483647 : list.size();
    }

    @Redirect(method={"render"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"), require=0)
    private int drawStringWithShadow(GuiGraphics drawContext, Font textRenderer, FormattedCharSequence text, int x, int y, int color) {
        IChatHudLineHook line = (IChatHudLineHook)this.trimmedMessages.get(this.chatLineIndex);
        if (line != null) {
            FadeUtils fadeUtils = line.aevumClient$getFade();
            double ease = fadeUtils == null ? 0.0 : fadeUtils.ease(ClientSetting.INSTANCE.ease.getValue());
            double fade = 1.0 - ease;
            x += (int)(fade * ClientSetting.INSTANCE.animateOffset.getValue());
            double c = Math.max(10.0, (double)(color >> 24 & 0xFF) * ease);
            if (line.aevumClient$getSync()) {
                return drawContext.method_35720(textRenderer, text, x, y, ColorUtil.injectAlpha(ClientSetting.INSTANCE.color.getValue(), ClientSetting.INSTANCE.fade.getValue() ? (int)c : color >> 24 & 0xFF).getRGB());
            }
            return drawContext.method_35720(textRenderer, text, x, y, ColorUtil.injectAlpha(color, ClientSetting.INSTANCE.fade.getValue() ? (int)c : color >> 24 & 0xFF));
        }
        return drawContext.method_35720(textRenderer, text, x, y, color);
    }

    @Inject(method={"render"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;addedTime()I")})
    public void getChatLineIndex(CallbackInfo ci, @Local(ordinal=13) int chatLineIndex) {
        this.chatLineIndex = chatLineIndex;
    }

    @ModifyVariable(method={"render"}, at=@At(value="STORE"))
    private GuiMessageTag removeMessageIndicator(GuiMessageTag messageIndicator) {
        if (ClientSetting.INSTANCE.hideIndicator.getValue()) {
            return null;
        }
        return messageIndicator;
    }

    @Shadow
    public abstract void method_1812(Component var1);

    @Shadow
    public int getWidth() {
        return 0;
    }
}

