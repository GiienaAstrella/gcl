package me.giiena.config.impl;

import me.giiena.config.api.Config;
import me.giiena.config.impl.platform.Services;
import net.minecraft.locale.Language;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class TranslationChecker {
    private static final Map<String, Map<Config.Type, Set<String>>> UNTRANSLATED =
            new LinkedHashMap<>();

    /**
     * Returns true if translation exists for {@code key}.
     * Untranslatable {@code key}s are tracked and grouped by
     * {@link Config#getModID()} and {@link Config.Type}.
     */
    public static boolean has(final Config config, final String key) {
        if (Language.getInstance().has(key)) return true;
        Map<Config.Type, Set<String>> mod = UNTRANSLATED.computeIfAbsent(config.getModID(),
                _ -> new LinkedHashMap<>());
        Set<String> untranslated = mod.computeIfAbsent(config.getType(),
                _ -> new LinkedHashSet<>());
        untranslated.add(key);
        return false;
    }

    /**
     * Logs untranslatable keys if in development environment.
     * This is no-op in production.
     */
    public static void done() {
        if (!Services.PLATFORM.isDevelopmentEnvironment()) return;

        for (Map.Entry<String, Map<Config.Type, Set<String>>> mod : UNTRANSLATED.entrySet()) {
            for (Map.Entry<Config.Type, Set<String>> config : mod.getValue().entrySet()) {
                ConfigConstants.LOG.warn("{} missing translation keys for {}:\n  {}",
                        mod.getKey(),
                        config.getKey().suffix(),
                        String.join("\n  ", config.getValue()));
            }
        }
    }
}
