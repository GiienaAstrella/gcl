package me.giiena.config.api;

import me.giiena.config.impl.ConfigRegistryImpl;

import java.util.EnumMap;
import java.util.Optional;

public final class ConfigRegistry {
    /**
     * Registers a config for {@code modID}.
     * The registered config will be stored in the config directory at
     * {@code CONFIGDIR/modID - type.toml}.
     * If {@code modID} will only ever have one config file, consider registering with
     * {@link #registerSingle(String, Config.Type)}}.
     */
    @SuppressWarnings("unused")
    public static Config register(String modID, Config.Type type) {
        return ConfigRegistryImpl.register(modID, type);
    }

    /**
     * Registers a config for {@code modID}.
     * The registered config will be stored in the config directory at
     * {@code CONFIGFIR/modID.toml}.
     * If {@code modID} needs more than one config file, consider registering with
     * {@link #register(String, Config.Type)}.
     */
    public static Config registerSingle(String modID, Config.Type type) {
        return ConfigRegistryImpl.registerSingle(modID, type);
    }

    /**
     * Gets the config for {@code modID}.
     */
    public static Optional<Config> get(String modID, Config.Type type) {
        return ConfigRegistryImpl.get(modID, type);
    }

    /**
     * Returns all configs for {@code modID}.
     */
    public static EnumMap<Config.Type, Config> getAll(String modID) {
        return ConfigRegistryImpl.getAll(modID);
    }

    /**
     * Reloads all configs for {@code modID}.
     */
    @SuppressWarnings("unused")
    public static void reloadAll(String modID) {
        ConfigRegistryImpl.reloadAll(modID);
    }
}
