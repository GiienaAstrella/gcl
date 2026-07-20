package me.giiena.config.platform;

import me.giiena.config.ConfigConstants;
import me.giiena.config.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

public class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz, Services.class.getClassLoader())
                .findFirst()
                .orElseThrow(() ->
                        new NullPointerException("Failed to load service for " + clazz.getName()));
        ConfigConstants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
