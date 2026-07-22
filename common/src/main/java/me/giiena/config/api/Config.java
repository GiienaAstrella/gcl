package me.giiena.config.api;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlWriter;
import me.giiena.config.impl.ConfigCommon;
import me.giiena.config.impl.ConfigConstants;
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
import java.util.function.Predicate;
import java.util.function.Supplier;

@NullMarked
@SuppressWarnings("unused")
public class Config {
    private final String modID;
    private final Type type;
    private final Path filePath;
    private final CommentedFileConfig config;
    private final Map<String, Value<?>> values = new LinkedHashMap<>();
    private final Map<String, String> comments = new LinkedHashMap<>();

    private boolean synced = false;

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

        boolean corrected = false;
        if (Files.exists(this.filePath)) {
            try (CommentedFileConfig onDisk =
                         CommentedFileConfig.builder(this.filePath).sync().build()) {
                onDisk.load();
                for (Map.Entry<String, Value<?>> entry : this.values.entrySet()) {
                    String path = entry.getKey();
                    Object raw = onDisk.get(path);
                    if (raw == null) {
                        this.config.set(path, entry.getValue().getDefault());
                        corrected = true;
                        continue;
                    }

                    Optional<?> coerced = entry.getValue().coerce(raw);
                    if (coerced.isPresent()) {
                        this.config.set(path, coerced.get());
                    } else {
                        ConfigConstants.LOG.error("Invalid value for {}, falling back to default!",
                                path);
                        this.config.set(path, entry.getValue().getDefault());
                        corrected = true;
                    }
                }
            }
        } else {
            for (Map.Entry<String, Value<?>> entry : this.values.entrySet()) {
                this.config.set(entry.getKey(), entry.getValue().getDefault());
            }
            corrected = true;
        }

        for (Value<?> value : this.values.values()) {
            value.clearCache();
        }

        if (corrected) this.save();
    }

    public void save() {
        ConfigConstants.LOG.info("Saving {} for {}", this.getFilePath(), this.getModID());
        this.config.save();
    }

    /**
     * Returns config comment at {@code path}.
     */
    public Optional<String> getComment(String path) {
        return Optional.ofNullable(this.comments.get(path));
    }

    /**
     * Clears all synced values.
     */
    @SuppressWarnings("unused")
    public void clearSyncedValues() {
        for (Map.Entry<String, Value<?>> entry : this.values.entrySet()) {
            entry.getValue().clearCache();
        }
        this.synced = false;
    }

    /**
     * Returns {@code true} if this config is synced with the server.
     */
    @SuppressWarnings("unused")
    public boolean isSynced() {
        return this.synced;
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
            entry.getValue().setSynced(raw.get(entry.getKey()));
        }
        this.synced = true;
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
     * Appends {@code comment} to the pending comment.
     */
    public Builder comment(String comment) {
        return new Builder(this).comment(comment);
    }

    /**
     * Pushes a new section.
     */
    public Builder push(String path) {
        return new Builder(this).push(path);
    }

    /**
     * Defines an entry.
     */
    public <T> Value<T> define(String path, T defaultValue) {
        return new Builder(this).push(path).define(defaultValue);
    }

    /**
     * Defines an entry.
     */
    public <T> Value<T> define(String path, T defaultValue, Predicate<T> validator) {
        return new Builder(this).push(path).define(defaultValue, validator);
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
    public static class Value<T> implements Supplier<T> {
        private final Config config;
        private final String path;
        private final Supplier<T> defaultSupplier;
        private final Predicate<T> validator;

        @Nullable
        private T value = null;

        protected Value(Config config,
                        String path,
                        Supplier<T> defaultSupplier,
                        Predicate<T> validator) {
            Preconditions.checkArgument(validator.test(defaultSupplier.get()),
                    "Default supplier fails its own validator");

            this.config = config;
            this.path = path;
            this.defaultSupplier = defaultSupplier;
            this.validator = validator;
        }

        /**
         * Returns the path for this config value.
         */
        public String path() {
            return this.path;
        }

        /**
         * Returns the cached value.
         * If no value has been loaded, load and cache it.
         * If this is a value of {@link Type#COMMON}, this value may be the synced value.
         */
        public T get() {
            Preconditions.checkNotNull(this.config, "Cannot get config value before building");
            if (this.value == null) {
                this.value = this.getRaw(this.config, this.path, this.defaultSupplier);
            }
            return this.value;
        }

        @SuppressWarnings("unchecked")
        protected T getRaw(Config config, String path, Supplier<T> defaultSupplier) {
            Object raw = config.config.get(path);
            if (raw == null) {
                return defaultSupplier.get();
            }

            if (this.getDefault() instanceof Integer && raw instanceof Long val) {
                return (T)(Integer) val.intValue();
            } else {
                return (T) raw;
            }
        }

        /**
         * Returns the default value.
         */
        public T getDefault() {
            return this.defaultSupplier.get();
        }

        @SuppressWarnings("unchecked")
        private Optional<T> coerce(Object raw) {
            T candidate;
            if (this.getDefault().getClass().isInstance(raw)) {
                candidate = (T) raw;
            } else if (this.getDefault() instanceof Integer && raw instanceof Long l) {
                candidate = (T)(Integer) l.intValue();
            } else {
                return Optional.empty();
            }
            return this.validator.test(candidate) ? Optional.of(candidate) : Optional.empty();
        }

        /**
         * Sets the value.
         */
        public void set(T value) {
            Preconditions.checkNotNull(this.config, "Cannot set config value before building");
            Preconditions.checkArgument(this.validator.test(value), "Invalid value for %s",
                    this.path);
            this.config.config.set(this.path, value);
            this.value = value;
        }

        void setSynced(@Nullable Object raw) {
            if (raw == null) {
                this.clearCache();
                return;
            }
            Optional<T> coerced = this.coerce(raw);
            if (coerced.isEmpty()) {
                ConfigConstants.LOG.warn("Ignoring invalid value for {}", this.path);
                return;
            }
            this.value = coerced.get();
        }

        /**
         * Saves this value to disk.
         */
        public void save() {
            this.config.save();
        }

        /**
         * Clears the cached value.
         */
        public void clearCache() {
            this.value = null;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    @NullMarked
    public static final class Builder {
        private final Config config;
        private final Deque<String> stack;

        @Nullable
        private String comment = null;

        private Builder(Config config) {
            this(config, new ArrayDeque<>());
        }

        private Builder(Config config, Deque<String> stack) {
            this.config = config;
            this.stack = stack;
        }

        private static String fullPath(Deque<String> stack) {
            List<String> parts = new ArrayList<>(stack);
            Collections.reverse(parts);
            return ConfigCommon.DOT_JOINER.join(parts);
        }

        /**
         * Pushes a new section.
         */
        public Builder push(String path) {
            this.checkPath(path);
            Deque<String> nextStack = new ArrayDeque<>(this.stack);
            ConfigCommon.DOT_SPLITTER.split(path).forEach(nextStack::push);
            this.consumeComment(fullPath(nextStack));
            return new Builder(this.config, nextStack);
        }

        /**
         * Closes the current section.
         */
        public Builder pop() {
            Preconditions.checkState(!this.stack.isEmpty(), "pop() without matching push()");
            this.consumeComment(this.fullPath());
            Deque<String> nextStack = new ArrayDeque<>(this.stack);
            nextStack.pop();
            return new Builder(config, nextStack);
        }

        /**
         * Closes {@code n} number of sections (bottom to top).
         */
        public Builder pop(int n) {
            for (int i = 0; i < n; i++) this.pop();
            return this;
        }

        /**
         * Appends {@code comment} to the pending comment.
         */
        public Builder comment(String comment) {
            Preconditions.checkNotNull(comment, "Comment cannot be null");
            Preconditions.checkArgument(!comment.isBlank(), "Comment cannot be blank");
            if (this.comment != null) {
                this.comment += comment;
            } else {
                this.comment = comment;
            }
            return this;
        }

        /**
         * Defines an entry.
         */
        public <T> Value<T> define(T defaultValue) {
            return this.define(defaultValue, v -> true);
        }

        /**
         * Defines an entry.
         */
        public <T> Value<T> define(T defaultValue, Predicate<T> validator) {
            String path = this.fullPath();
            Value<T> val = new Value<>(this.config, path, () -> defaultValue, validator);
            this.config.values.put(path, val);
            this.consumeComment(path);
            return val;
        }

        private void checkPath(String path) {
            Preconditions.checkNotNull(path, "path cannot be null");
            Preconditions.checkArgument(!path.isBlank(), "path cannot be blank");
        }

        private String fullPath() {
            return fullPath(this.stack);
        }

        private void consumeComment(String path) {
            if (this.comment == null) return;
            this.config.config.setComment(path, this.comment);
            this.config.comments.put(path, this.comment);
            this.comment = null;
        }
    }
}
