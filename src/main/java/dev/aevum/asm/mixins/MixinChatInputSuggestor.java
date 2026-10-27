/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  net.minecraft.Component
 *  net.minecraft.GuiGraphics
 *  net.minecraft.EditBox
 *  net.minecraft.CommandSuggestions
 *  net.minecraft.FormattedCharSequence
 *  org.apache.commons.lang3.StringUtils
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.aevum.Aevum;
import dev.aevum.api.utils.render.Render2DUtil;
import dev.aevum.mod.commands.Command;
import dev.aevum.mod.modules.impl.client.ClientSetting;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.Component;
import net.minecraft.GuiGraphics;
import net.minecraft.EditBox;
import net.minecraft.CommandSuggestions;
import net.minecraft.FormattedCharSequence;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={CommandSuggestions.class})
public abstract class MixinChatInputSuggestor {
    @Final
    @Shadow
    EditBox input;
    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;
    @Final
    @Shadow
    private List<FormattedCharSequence> commandUsage;
    @Unique
    private boolean showOutline = false;

    @Shadow
    public abstract void method_23920(boolean var1);

    @Inject(at={@At(value="HEAD")}, method={"render"})
    private void onRender(GuiGraphics context, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.showOutline) {
            int x = this.input.method_46426() - 3;
            int y = this.input.method_46427() - 3;
            Render2DUtil.drawRect(context.method_51448(), (float)x, (float)y, (float)(this.input.method_25368() + 1), 1.0f, ClientSetting.INSTANCE.color.getValue().getRGB());
            Render2DUtil.drawRect(context.method_51448(), (float)x, (float)(y + this.input.method_25364() + 1), (float)(this.input.method_25368() + 1), 1.0f, ClientSetting.INSTANCE.color.getValue().getRGB());
            Render2DUtil.drawRect(context.method_51448(), (float)x, (float)y, 1.0f, (float)(this.input.method_25364() + 1), ClientSetting.INSTANCE.color.getValue().getRGB());
            Render2DUtil.drawRect(context.method_51448(), (float)(x + this.input.method_25368() + 1), (float)y, 1.0f, (float)(this.input.method_25364() + 2), ClientSetting.INSTANCE.color.getValue().getRGB());
        }
    }

    @Inject(at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/widget/TextFieldWidget;getCursor()I", ordinal=0)}, method={"refresh()V"})
    private void onRefresh(CallbackInfo ci) {
        String string2;
        String prefix = Aevum.getPrefix();
        String string = this.input.method_1882();
        this.showOutline = string.startsWith(prefix);
        if (string.isEmpty() || !prefix.startsWith(string2 = string.substring(0, this.input.method_1881())) && !string2.startsWith(prefix)) {
            return;
        }
        int j = 0;
        Matcher matcher = Pattern.compile("(\\s+)").matcher(string2);
        while (matcher.find()) {
            j = matcher.end();
        }
        SuggestionsBuilder builder = new SuggestionsBuilder(string2, j);
        if (string2.length() < prefix.length()) {
            if (!prefix.startsWith(string2)) {
                return;
            }
            builder.suggest(prefix);
        } else {
            if (!string2.startsWith(prefix)) {
                return;
            }
            int count = StringUtils.countMatches((CharSequence)string2, (CharSequence)" ");
            List<String> seperated = Arrays.asList(string2.split(" "));
            if (count == 0) {
                for (Object strObj : Aevum.COMMAND.getCommands().keySet().toArray()) {
                    String str = (String)strObj;
                    builder.suggest(Aevum.getPrefix() + str + " ");
                }
            } else {
                if (seperated.isEmpty()) {
                    return;
                }
                Command c = Aevum.COMMAND.getCommandBySyntax(seperated.getFirst().substring(prefix.length()));
                if (c == null) {
                    this.commandUsage.add(Component.method_30163((String)(" \u00a74no commands found: \u00a7f" + seperated.getFirst().substring(prefix.length()))).method_30937());
                    return;
                }
                String[] suggestions = c.getAutocorrect(count, seperated);
                if (suggestions == null || suggestions.length == 0) {
                    return;
                }
                for (String str : suggestions) {
                    builder.suggest(str + " ");
                }
            }
        }
        this.pendingSuggestions = builder.buildFuture();
        this.method_23920(false);
    }
}

