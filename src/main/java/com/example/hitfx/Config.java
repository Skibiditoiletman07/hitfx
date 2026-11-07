package com.example.hitfx;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Properties;

public final class Config {
    public enum Feature { TARGET_HUD, SHIELD_BREAKER, RIPPLE, BACKTRACK, AFTERGLOW, HIT_SPLASH, CRIT_STARS }

    private static Theme theme = Theme.VIOLET;
    private static final EnumSet<Feature> ENABLED = EnumSet.allOf(Feature.class);
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("hitfx.properties");

    public static Theme theme() { return theme; }
    public static boolean on(Feature f) { return ENABLED.contains(f); }

    public static void setTheme(Theme t) { theme = t; save(); }
    public static void set(Feature f, boolean value) {
        if (value) ENABLED.add(f); else ENABLED.remove(f);
        save();
    }

    public static void load() {
        if (!Files.exists(FILE)) return;
        try (Reader r = Files.newBufferedReader(FILE)) {
            Properties p = new Properties();
            p.load(r);
            theme = Theme.valueOf(p.getProperty("theme", "VIOLET"));
            ENABLED.clear();
            for (Feature f : Feature.values())
                if (Boolean.parseBoolean(p.getProperty(f.name().toLowerCase(), "true"))) ENABLED.add(f);
        } catch (IOException | IllegalArgumentException ignored) { }
    }

    private static void save() {
        Properties p = new Properties();
        p.setProperty("theme", theme.name());
        for (Feature f : Feature.values()) p.setProperty(f.name().toLowerCase(), String.valueOf(ENABLED.contains(f)));
        try (Writer w = Files.newBufferedWriter(FILE)) { p.store(w, "HitFX"); } catch (IOException ignored) { }
    }
}
