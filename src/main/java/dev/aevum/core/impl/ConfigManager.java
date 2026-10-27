/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Splitter
 *  org.apache.commons.io.IOUtils
 */
package dev.aevum.core.impl;

import com.google.common.base.Splitter;
import dev.aevum.Aevum;
import dev.aevum.core.Manager;
import dev.aevum.mod.modules.HudElement;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.client.HUD;
import dev.aevum.mod.modules.settings.Setting;
import dev.aevum.mod.modules.settings.impl.BindSetting;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import dev.aevum.mod.modules.settings.impl.StringSetting;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;

public class ConfigManager
extends Manager {
    public static File options = ConfigManager.getFile("options.txt");
    private final Hashtable<String, String> settings = new Hashtable();

    public void load() {
        this.settings.clear();
        Splitter COLON_SPLITTER = Splitter.on((char)':');
        try {
            if (options.exists()) {
                List list = IOUtils.readLines((InputStream)new FileInputStream(options), (Charset)StandardCharsets.UTF_8);
                for (String s : list) {
                    try {
                        Iterator iterator = COLON_SPLITTER.limit(2).split((CharSequence)s).iterator();
                        this.settings.put((String)iterator.next(), (String)iterator.next());
                    }
                    catch (Exception var10) {
                        System.out.println("Skipping bad option: " + s);
                    }
                }
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
            System.out.println("[Aevum Client] Failed to load settings");
        }
        for (Module module : Aevum.MODULE.getModules()) {
            for (Setting setting : module.getSettings()) {
                Setting s;
                String line = module.getName() + "_" + setting.getName();
                Objects.requireNonNull(setting);
                if (setting instanceof BooleanSetting) {
                    s = (BooleanSetting)setting;
                    ((BooleanSetting)s).setValueWithoutTask(Aevum.CONFIG.getBoolean(line, ((BooleanSetting)s).getDefaultValue()));
                    continue;
                }
                if (setting instanceof SliderSetting) {
                    s = (SliderSetting)setting;
                    ((SliderSetting)s).setValue(Aevum.CONFIG.getFloat(line, (float)((SliderSetting)s).getDefaultValue()));
                    continue;
                }
                if (setting instanceof BindSetting) {
                    s = (BindSetting)setting;
                    ((BindSetting)s).setValue(Aevum.CONFIG.getInt(line, ((BindSetting)s).getDefaultValue()));
                    ((BindSetting)s).setHoldEnable(Aevum.CONFIG.getBoolean(line + "_hold"));
                    continue;
                }
                if (setting instanceof EnumSetting) {
                    s = (EnumSetting)setting;
                    ((EnumSetting)s).loadSetting(Aevum.CONFIG.getString(line));
                    continue;
                }
                if (setting instanceof ColorSetting) {
                    s = (ColorSetting)setting;
                    ((ColorSetting)s).setValue(new Color(Aevum.CONFIG.getInt(line, ((ColorSetting)s).getDefaultValue().getRGB()), true));
                    boolean rainbow = Aevum.CONFIG.getString(line + "Rainbow") != null ? Aevum.CONFIG.getBoolean(line + "Rainbow", ((ColorSetting)s).getDefaultRainbow()) : Aevum.CONFIG.getBoolean(line + "Sync", ((ColorSetting)s).getDefaultRainbow());
                    ((ColorSetting)s).setRainbow(rainbow);
                    if (!((ColorSetting)s).injectBoolean) continue;
                    ((ColorSetting)s).booleanValue = Aevum.CONFIG.getBoolean(line + "Boolean", ((ColorSetting)s).getDefaultBooleanValue());
                    continue;
                }
                if (!(setting instanceof StringSetting)) continue;
                s = (StringSetting)setting;
                ((StringSetting)s).setValue(Aevum.CONFIG.getString(line, ((StringSetting)s).getDefaultValue()));
            }
            module.setState(Aevum.CONFIG.getBoolean(module.getName() + "_state", module instanceof HUD));
            this.safeLoadHud(module);
        }
    }

    public void read() {
        this.load();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void save() {
        PrintWriter printwriter = null;
        try {
            printwriter = new PrintWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(options), StandardCharsets.UTF_8));
            for (Module module : Aevum.MODULE.getModules()) {
                for (Setting setting : module.getSettings()) {
                    Setting s;
                    String line = module.getName() + "_" + setting.getName();
                    Objects.requireNonNull(setting);
                    if (setting instanceof BooleanSetting) {
                        s = (BooleanSetting)setting;
                        printwriter.println(line + ":" + ((BooleanSetting)s).getValue());
                        continue;
                    }
                    if (setting instanceof SliderSetting) {
                        s = (SliderSetting)setting;
                        printwriter.println(line + ":" + ((SliderSetting)s).getValue());
                        continue;
                    }
                    if (setting instanceof BindSetting) {
                        s = (BindSetting)setting;
                        printwriter.println(line + ":" + ((BindSetting)s).getValue());
                        printwriter.println(line + "_hold:" + ((BindSetting)s).isHoldEnable());
                        continue;
                    }
                    if (setting instanceof EnumSetting) {
                        s = (EnumSetting)setting;
                        printwriter.println(line + ":" + ((Enum)((EnumSetting)s).getValue()).name());
                        continue;
                    }
                    if (setting instanceof ColorSetting) {
                        s = (ColorSetting)setting;
                        printwriter.println(line + ":" + ((ColorSetting)s).getValue().getRGB());
                        printwriter.println(line + "Rainbow:" + ((ColorSetting)s).rainbow);
                        if (!((ColorSetting)s).injectBoolean) continue;
                        printwriter.println(line + "Boolean:" + ((ColorSetting)s).booleanValue);
                        continue;
                    }
                    if (!(setting instanceof StringSetting)) continue;
                    s = (StringSetting)setting;
                    printwriter.println(line + ":" + ((StringSetting)s).getValue());
                }
                printwriter.println(module.getName() + "_state:" + module.isOn());
                if (!(module instanceof HudElement)) continue;
                HudElement hud = (HudElement)((Object)module);
                printwriter.println(module.getName() + "_hudAnchor:" + this.getInternalAnchorX(hud));
                printwriter.println(module.getName() + "_hudY:" + hud.getElementY());
                printwriter.println(module.getName() + "_hudAlign:" + hud.getAlignMode().name());
            }
            IOUtils.closeQuietly((Writer)printwriter);
            IOUtils.closeQuietly((Writer)printwriter);
        }
        catch (Exception exception) {
            exception.printStackTrace();
            System.out.println("[Aevum Client] Failed to save settings");
        }
        finally {
            IOUtils.closeQuietly(printwriter);
        }
    }

    private void forceSetAlignMode(HudElement hud, HudElement.AlignMode mode) {
        hud.setAlignMode(mode);
    }

    private void forceSetAnchorX(HudElement hud, int anchorValue) {
        try {
            if (hud.getAlignMode() == HudElement.AlignMode.RIGHT) {
                hud.setRightAnchorX(anchorValue);
            } else {
                hud.setElementX(anchorValue);
            }
        }
        catch (Exception e) {
            hud.setElementX(anchorValue);
        }
    }

    private int getInternalAnchorX(HudElement hud) {
        try {
            return hud.getAlignMode() == HudElement.AlignMode.RIGHT ? hud.getRightAnchorX() : hud.getElementX();
        }
        catch (Exception e) {
            return hud.getElementX();
        }
    }

    private void safeLoadHud(Module module) {
        if (!(module instanceof HudElement)) {
            return;
        }
        HudElement hud = (HudElement)((Object)module);
        try {
            String keyAlign = module.getName() + "_hudAlign";
            String keyAnchor = module.getName() + "_hudAnchor";
            String keyY = module.getName() + "_hudY";
            String alignStr = this.getString(keyAlign);
            if (alignStr != null) {
                try {
                    this.forceSetAlignMode(hud, HudElement.AlignMode.valueOf(alignStr));
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    // empty catch block
                }
            }
            if (this.getString(keyAnchor) != null) {
                this.forceSetAnchorX(hud, this.getInt(keyAnchor, 0));
            }
            if (this.getString(keyY) != null) {
                hud.setElementY(this.getInt(keyY, 0));
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public void set(String key, Object value) {
        this.settings.put(key, String.valueOf(value));
    }

    public int getInt(String setting, int defaultValue) {
        String s = this.settings.get(setting);
        if (s == null || !this.isInteger(s)) {
            return defaultValue;
        }
        return Integer.parseInt(s);
    }

    public float getFloat(String setting, float defaultValue) {
        String s = this.settings.get(setting);
        if (s == null || !this.isFloat(s)) {
            return defaultValue;
        }
        return Float.parseFloat(s);
    }

    public boolean getBoolean(String setting) {
        String s = this.settings.get(setting);
        return Boolean.parseBoolean(s);
    }

    public boolean getBoolean(String setting, boolean defaultValue) {
        if (this.settings.get(setting) != null) {
            String s = this.settings.get(setting);
            return Boolean.parseBoolean(s);
        }
        return defaultValue;
    }

    public String getString(String setting) {
        return this.settings.get(setting);
    }

    public String getString(String setting, String defaultValue) {
        if (this.settings.get(setting) == null) {
            return defaultValue;
        }
        return this.settings.get(setting);
    }

    public boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    public boolean isFloat(String str) {
        String pattern = "^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$";
        return str.matches(pattern);
    }
}

