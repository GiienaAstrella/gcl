package me.giiena.config.api;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlWriter;
import me.giiena.config.ConfigConstants;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@NullMarked
public class Config {
    private final String modID;
    private final Type type;
    private final Path filePath;
    private final CommentedFileConfig config;
    private final Map<String, Value<?>> values = new LinkedHashMap<>();
    private final Map<String, Value<?>> synced = new LinkedHashMap<>();
    private final Map<String, String> comments = new LinkedHashMap<>();

    public Config(String modID, Type type, Path filePath) {
        this.modID = modID;
        this.type = type;
        this.filePath = filePath;

        this.config = CommentedFileConfig.builder(filePath)
                .sync()
                .build();
    }

    public String getModID() {
        return this.modID;
    }

    public Type getType() {
        return this.type;
    }

    public Path getFilePath() {
        return this.filePath;
    }

    public void load() {
        this.ensureParentDirExists();
        ConfigConstants.LOG.info("Loading {} for {}", this.getFilePath(), this.getModID());

        if (Files.exists(this.filePath)) {
            try (CommentedFileConfig onDisk =
                         CommentedFileConfig.builder(this.filePath).sync().build()) {
                onDisk.load();
                for (String path : this.values.keySet()) {
                    Object raw = onDisk.get(path);
                    if (raw != null) this.config.set(path, raw);
                }
            }
        }

        for (Value<?> value : this.values.values()) {
            value.load();
        }
    }

    public void save() {
        ConfigConstants.LOG.info("Saving {} for {}", this.getFilePath(), this.getModID());
        this.config.save();
    }

    /**
     * Returns the value for {@code path}.
     * If {@code isSynced()} returns {@code true}, the returned value will be that received from
     * the server, not the one locally configured.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T get(String path) {
        Value<T> val;
        if (this.isSynced()) {
            val = (Value<T>) this.synced.get(path);
        } else {
            val = (Value<T>) this.values.get(path);
        }
        return (val != null) ? val.get() : null;
    }

    /**
     * Returns the value for {@code path}, if it exists.
     * Otherwise, returns {@code defaultValue}.
     */
    public <T> T getOrDefault(String path, T defaultValue) {
        T value = this.get(path);
        return (value != null) ? value : defaultValue;
    }

    /**
     * Clears all synced values.
     */
    @SuppressWarnings("unused")
    public void clearSyncedValues() {
        this.synced.clear();
    }

    /**
     * Returns {@code true} if this config is synced with the server.
     */
    @SuppressWarnings("unused")
    public boolean isSynced() {
        return !this.synced.isEmpty();
    }

    /**
     * Accepts raw config data from syncing source.
     */
    public void acceptSyncedConfig(byte[] data) {
        this.clearSyncedValues();
        CommentedConfig raw = TomlFormat.instance().createConfig();
        TomlFormat.instance().createParser().parse(
                new ByteArrayInputStream(data),
                raw,
                ParsingMode.REPLACE);
        for (Map.Entry<String, Value<?>> entry : this.values.entrySet()) {
            Value<?> syncedVal = entry.getValue().copy(raw);
            syncedVal.load();
            this.synced.put(entry.getKey(), syncedVal);
        }
    }

    /**
     * Returns the raw config data for syncing.
     */
    public byte[] toml() {
        TomlWriter writer = new TomlWriter();
        StringWriter sw = new StringWriter();
        writer.write(this.config, sw);
        return sw.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Returns config values.
     */
    public List<Value<?>> values() {
        return List.copyOf(this.values.values());
    }

    /**
     * Returns config comment at {@code path}.
     */
    public Optional<String> comment(String path) {
        return Optional.ofNullable(this.comments.get(path));
    }

    private void ensureParentDirExists() {
        Path parent = this.filePath.getParent();
        if (parent == null) return;
        try {
            Files.createDirectories(parent);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed creating config directory", e);
        }
    }

    /**
     * Opens a new section.
     */
    public Builder section(String path) {
        return new Builder(this)
                .section(path);
    }

    /**
     * Opens a new section and sets a comment for that section.
     */
    @SuppressWarnings({"UnusedReturnValue", "unused"})
    public Builder comment(String path, String comment) {
        return new Builder(this)
                .section(path)
                .comment(comment);
    }

    /**
     * Opens a new section and sets the value for that section, then closes the section.
     */
    @SuppressWarnings("unchecked")
    public <T> void set(String path, T value) {
        this.config.set(path, value);
        Value<T> val = (Value<T>) this.values.get(path);
        if (val != null) {
            val.set(value);
        } else {
            val = new Value<>(this.config, path, value);
            this.values.put(path, val);
        }
    }

    public enum Type {
        COMMON,
        SERVER,
        CLIENT;

        public String suffix() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    @NullMarked
    public static final class Value<T> implements Supplier<T> {
        private final com.electronwill.nightconfig.core.Config config;
        private final String path;
        private final T defaultValue;
        @Nullable
        private T value;

        Value(com.electronwill.nightconfig.core.Config config, String path, T defaultValue) {
            this.config = config;
            this.path = path;
            this.defaultValue = defaultValue;
        }

        public String path() {
            return this.path;
        }

        @Override
        @Nullable
        public T get() {
            return this.value;
        }

        public T getDefault() {
            return this.defaultValue;
        }

        public void set(T value) {
            this.value = value;
        }

        Value<T> copy(com.electronwill.nightconfig.core.Config config) {
            return new Value<>(config, this.path, this.defaultValue);
        }

        @SuppressWarnings("unchecked")
        void load() {
            Object raw = this.config.get(this.path);
            if (raw == null) {
                this.value = this.defaultValue;
                return;
            }

            if (this.defaultValue instanceof Integer && raw instanceof Long val) {
                this.value = (T)(Integer) val.intValue();
            } else {
                this.value = (T) raw;
            }
        }
    }

    @NullMarked
    public static final class Builder {
        private final Config config;
        private final Deque<String> stack = new ArrayDeque<>();

        @Nullable
        private String comment = null;

        private Builder(Config config) {
            this.config = config;
        }

        /**
         * Opens a new section.
         */
        public Builder section(String path) {
            Preconditions.checkNotNull(path, "path cannot be null");
            Preconditions.checkArgument(!path.isBlank(), "path cannot be blank");

            if (!this.stack.isEmpty()) {
                this.consumeComment(this.config.config::setComment, this.config.comments::put);
            }

            if (path.contains(".")) {
                for (String part : path.split("\\.")) {
                    this.stack.push(part);
                }
            } else {
                this.stack.push(path);
            }
            return this;
        }

        /**
         * Closes the current section.
         */
        @SuppressWarnings("UnusedReturnValue")
        public Builder close() {
            Preconditions.checkState(!this.stack.isEmpty(), "close() without matching section()");
            this.consumeComment(this.config.config::setComment, this.config.comments::put);
            this.stack.pop();
            return this;
        }

        /**
         * Closes {@code n} number of sections (bottom to top).
         */
        @SuppressWarnings("UnusedReturnValue")
        public Builder close(int n) {
            for (int i = 0; i < n; i++) this.close();
            return this;
        }

        /**
         * Closes the section identified by {@code path}.
         */
        @SuppressWarnings("unused")
        public Builder close(String path) {
            Preconditions.checkNotNull(path, "path cannot be null");
            Preconditions.checkArgument(!path.isBlank(), "path cannot be blank");
            if (path.contains(".")) {
                this.close(path.split("\\.").length);
            } else {
                this.close();
            }
            return this;
        }

        /**
         * Sets a comment.
         */
        public Builder comment(@Nullable String comment) {
            if (comment == null || comment.isBlank()) return this;
            this.comment = comment;
            return this;
        }

        /**
         * Sets value for the section, applies any comment perviously set, then closes the section.
         */
        public <T> Builder set(T value) {
            String path = this.fullPath();
            this.config.config.set(path, value);
            this.config.values.put(path, new Value<>(this.config.config, path, value));
            this.consumeComment(this.config.config::setComment, this.config.comments::put);
            ConfigConstants.LOG.info("{}={}", path, value);
            this.stack.pop();
            return this;
        }

        /**
         * Returns the full dot-separated path for the current active section.
         */
        private String fullPath() {
            List<String> parts = new ArrayList<>(this.stack);
            Collections.reverse(parts);
            return String.join(".", parts);
        }

        @SafeVarargs
        private void consumeComment(BiConsumer<String, String>... consumers) {
            if (this.comment == null) return;
            String path = this.fullPath();
            for (BiConsumer<String, String> consumer : consumers) {
                consumer.accept(path, this.comment);
            }
            this.comment = null;
        }
    }
}
