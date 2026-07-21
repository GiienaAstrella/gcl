package me.giiena.config.impl.platform;

import me.giiena.config.impl.ConfigConstants;
import me.giiena.config.impl.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

public class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    @SuppressWarnings("SameParameterValue")
    private static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz, Services.class.getClassLoader())
                .findFirst()
                .orElseThrow(() ->
                        new NullPointerException("Failed to load service for " + clazz.getName()));
        ConfigConstants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
