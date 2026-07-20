package me.giiena.config.client.screen;

import me.giiena.config.api.Config;
import me.giiena.config.platform.Services;
import net.minecraft.client.gui.screens.Screen;

import java.util.EnumMap;

public class ConfigScreenFactory {
    public static Screen createScreen(
            String modName,
            Screen previous,
            final EnumMap<Config.Type, Config> configs) {
        if (configs.size() == 1 && !Services.PLATFORM.isDevelopmentEnvironment()) {
            return new ConfigEditScreen(modName, previous, configs.values().iterator().next());
        } else {
            return new ConfigListScreen(modName, previous, configs);
        }
    }
}
