package me.giiena.config;

import me.giiena.config.api.ConfigRegistry;
import me.giiena.config.client.screen.ConfigScreenFactory;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(value = Dist.CLIENT)
public class ConfigNeoForgeClient {
    private static final Set<String> LOGGED = new HashSet<>();

    @SubscribeEvent
    private static void setup(FMLClientSetupEvent event) {
        ModList.get().forEachModContainer((modID, modContainer) -> {
            if (ConfigRegistry.getAll(modID).isEmpty()) return;
            if (!LOGGED.contains(modID)) {
                ConfigConstants.LOG.info("Registering config screens for {}", modID);
                LOGGED.add(modID);
            }

            modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                    new IConfigScreenFactory() {
                        @Override
                        @NonNull
                        public Screen createScreen(
                                @NonNull ModContainer container,
                                @NonNull Screen modListScreen) {
                            return ConfigScreenFactory.createScreen(
                                    container.getModInfo().getDisplayName(),
                                    modListScreen,
                                    ConfigRegistry.getAll(container.getModId()));
                        }
                    });
        });
    }
}
