package me.giiena.config.impl;

import me.giiena.config.ConfigConstants;
import me.giiena.config.api.Config;
import me.giiena.config.platform.Services;
import com.google.common.base.Preconditions;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ConfigRegistryImpl {
    private static final Map<String, Map<Config.Type, Config>> REGISTRY = new LinkedHashMap<>();

    /**
     * Registers a config for {@code modID}.
     * The registered config will be stored in the config directory at
     * {@code CONFIGDIR/modID - type.toml}.
     * If {@code modID} will only ever have one config file, consider registering with
     * {@link #registerSingle(String, Config.Type)}}.
     */
    public static Config register(String modID, Config.Type type) {
        return register(modID, type, resolvePath(modID, type));
    }

    /**
     * Registers a config for {@code modID}.
     * The registered config will be stored in the config directory at
     * {@code CONFIGFIR/modID.toml}.
     * If {@code modID} needs more than one config file, consider registering with
     * {@link #register(String, Config.Type)}.
     */
    public static Config registerSingle(String modID, Config.Type type) {
        return register(modID, type, Services.PLATFORM.getConfigDir().resolve(modID + ".toml"));
    }

    /**
     * Gets the config for {@code modID}.
     */
    public static Optional<Config> get(String modID, Config.Type type) {
        return Optional.ofNullable(REGISTRY.getOrDefault(modID, Collections.emptyMap()).get(type));
    }

    /**
     * Returns all configs for {@code modID}.
     */
    public static EnumMap<Config.Type, Config> getAll(String modID) {
        EnumMap<Config.Type, Config> map = new EnumMap<>(Config.Type.class);
        for (Config.Type type : Config.Type.values()) {
            get(modID, type).ifPresent(c -> map.put(type, c));
        }
        return map;
    }

    /**
     * Reloads all configs for {@code modID}.
     */
    public static void reloadAll(String modID) {
        ConfigConstants.LOG.info("Reloading all configs for {}", modID);
        Map<Config.Type, Config> mod = REGISTRY.getOrDefault(modID, Collections.emptyMap());
        mod.values().forEach(Config::load);
    }

    /**
     * Returns all {@link Config.Type#COMMON} configuration.
     */
    public static Collection<Config> getAllCommons() {
        List<Config> result = new ArrayList<>();
        for (Map<Config.Type, Config> map : REGISTRY.values()) {
            Config c = map.get(Config.Type.COMMON);
            if (c != null) result.add(c);
        }
        return result;
    }

    /**
     * Registers a config for {@code modID} to be stored at {@code filePath}.
     * The registered config will be stored in the config directory at
     * {@code CONFIGDIR/modID - type.toml}.
     * If {@code modID} will only ever have one config file, consider registering with
     * {@link #registerSingle(String, Config.Type)}}.
     */
    private static Config register(String modID, Config.Type type, Path filePath) {
        Map<Config.Type, Config> mod = REGISTRY.computeIfAbsent(modID, _ -> new LinkedHashMap<>());
        Preconditions.checkState(!mod.containsKey(type), "%s Config already registered for %s",
                type, modID);
        Config config = new Config(modID, type, filePath);
        mod.put(type, config);
        return config;
    }

    /**
     * Resolves config file path.
     */
    private static Path resolvePath(String modID, Config.Type type) {
        return Services.PLATFORM.getConfigDir().resolve(modID + "-" + type.suffix() + ".toml");
    }
}
