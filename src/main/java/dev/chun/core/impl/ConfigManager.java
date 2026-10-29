/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  by.radioegor146.nativeobfuscator.Native
 *  com.google.common.base.Splitter
 *  org.apache.commons.io.IOUtils
 */
package dev.chun.core.impl;

import com.google.common.base.Splitter;
import dev.chun.Aevum;
import dev.chun.core.Manager;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.impl.client.HUD;
import dev.chun.mod.modules.settings.Setting;
import dev.chun.mod.modules.settings.impl.BindSetting;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.ColorSetting;
import dev.chun.mod.modules.settings.impl.EnumSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import dev.chun.mod.modules.settings.impl.StringSetting;
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

    public ConfigManager() {
    }

    public void load() {
        this.settings.clear();
        Splitter COLON_SPLITTER = Splitter.on((char)':');
        try {
            if (options.exists()) {
                List<String> list = IOUtils.readLines((InputStream)new FileInputStream(options), (Charset)StandardCharsets.UTF_8);
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
                String line = module.getName() + "_" + setting.getName();
                Objects.requireNonNull(setting);
                if (setting instanceof BooleanSetting) {
                    BooleanSetting s = (BooleanSetting)setting;
                    s.setValueWithoutTask(Aevum.CONFIG.getBoolean(line, s.getDefaultValue()));
                } else if (setting instanceof SliderSetting) {
                    SliderSetting s = (SliderSetting)setting;
                    s.setValue(Aevum.CONFIG.getFloat(line, (float)s.getDefaultValue()));
                } else if (setting instanceof BindSetting) {
                    BindSetting s = (BindSetting)setting;
                    s.setValue(Aevum.CONFIG.getInt(line, s.getDefaultValue()));
                    s.setHoldEnable(Aevum.CONFIG.getBoolean(line + "_hold"));
                } else if (setting instanceof EnumSetting) {
                    EnumSetting s = (EnumSetting)setting;
                    s.loadSetting(Aevum.CONFIG.getString(line));
                } else if (setting instanceof ColorSetting) {
                    ColorSetting s = (ColorSetting)setting;
                    s.setValue(new Color(Aevum.CONFIG.getInt(line, s.getDefaultValue().getRGB()), true));
                    boolean rainbow;
                    if (Aevum.CONFIG.getString(line + "Rainbow") != null) {
                        rainbow = Aevum.CONFIG.getBoolean(line + "Rainbow", s.getDefaultRainbow());
                    } else {
                        rainbow = Aevum.CONFIG.getBoolean(line + "Sync", s.getDefaultRainbow());
                    }
                    s.setRainbow(rainbow);
                    if (s.injectBoolean) {
                        s.booleanValue = Aevum.CONFIG.getBoolean(line + "Boolean", s.getDefaultBooleanValue());
                    }
                } else if (setting instanceof StringSetting) {
                    StringSetting s = (StringSetting)setting;
                    s.setValue(Aevum.CONFIG.getString(line, s.getDefaultValue()));
                }
            }
            module.setState(Aevum.CONFIG.getBoolean(module.getName() + "_state", module instanceof HUD));
        }
    }

    public void read() {
        this.load();
    }

    public void save() {
        PrintWriter printwriter = null;
        try {
            printwriter = new PrintWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(options), StandardCharsets.UTF_8));
            for (Module module : Aevum.MODULE.getModules()) {
                for (Setting setting : module.getSettings()) {
                    String line = module.getName() + "_" + setting.getName();
                    Objects.requireNonNull(setting);
                    if (setting instanceof BooleanSetting) {
                        BooleanSetting s = (BooleanSetting)setting;
                        printwriter.println(line + ":" + s.getValue());
                    } else if (setting instanceof SliderSetting) {
                        SliderSetting s = (SliderSetting)setting;
                        printwriter.println(line + ":" + s.getValue());
                    } else if (setting instanceof BindSetting) {
                        BindSetting s = (BindSetting)setting;
                        printwriter.println(line + ":" + s.getValue());
                        printwriter.println(line + "_hold:" + s.isHoldEnable());
                    } else if (setting instanceof EnumSetting) {
                        EnumSetting s = (EnumSetting)setting;
                        printwriter.println(line + ":" + ((Enum)s.getValue()).name());
                    } else if (setting instanceof ColorSetting) {
                        ColorSetting s = (ColorSetting)setting;
                        printwriter.println(line + ":" + s.getValue().getRGB());
                        printwriter.println(line + "Rainbow:" + s.rainbow);
                        if (s.injectBoolean) {
                            printwriter.println(line + "Boolean:" + s.booleanValue);
                        }
                    } else if (setting instanceof StringSetting) {
                        StringSetting s = (StringSetting)setting;
                        printwriter.println(line + ":" + s.getValue());
                    }
                }
                printwriter.println(module.getName() + "_state:" + module.isOn());
            }
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

    public void set(String key, Object value) {
        if (value instanceof Integer || value instanceof Boolean || value instanceof String) {
            this.set(key, String.valueOf(value));
        } else if (value instanceof Double || value instanceof Float) {
            this.set(key, String.valueOf(value));
        } else {
            this.set(key, String.valueOf(value));
        }
    }

    public void set(String key, int value) {
        this.set(key, String.valueOf(value));
    }
}
