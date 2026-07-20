package me.giiena.config;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ConfigCommon {
    /**
     * Returns {@link Identifier} in the GCL namespace.
     */
    public static Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath(ConfigConstants.MOD_ID, path);
    }

    public static String langKey(String path, String prefix) {
        return langKey(ConfigConstants.MOD_ID, path, prefix, null);
    }

    public static String langKey(String path, String prefix, @Nullable String suffix) {
        return langKey(ConfigConstants.MOD_ID, path, prefix, suffix);
    }

    public static String langKey(String namespace, String path, String prefix, @Nullable String suffix) {
        List<String> key = new ArrayList<>();
        key.add(namespace);
        key.add(path);
        key.addFirst(prefix);
        if (suffix != null) key.addLast(suffix);
        return String.join(".", key);
    }
}
