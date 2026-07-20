package me.giiena.config.integration;

import me.giiena.config.ConfigConstants;
import me.giiena.config.api.ConfigRegistry;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModMenuPlugin implements ModMenuApi {
    private static final Set<String> LOGGED = new HashSet<>();

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        Map<String, ConfigScreenFactory<?>> map = new HashMap<>();

        FabricLoader.getInstance().getAllMods().forEach(mod -> {
            String modID = mod.getMetadata().getId();
            if (ConfigRegistry.getAll(modID).isEmpty()) return;
            if (!LOGGED.contains(modID)) {
                ConfigConstants.LOG.info("Registering config screens for {}", modID);
                LOGGED.add(modID);
            }

            map.put(modID, parent -> me.giiena.config.client.screen.ConfigScreenFactory
                    .createScreen(
                            mod.getMetadata().getName(),
                            parent,
                            ConfigRegistry.getAll(modID)));
        });
        return map;
    }
}
