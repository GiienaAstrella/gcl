package me.giiena.config.api;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A wrapper for nested {@link Map}s.
 * @param <K1> outer keys
 * @param <K2> inner keys
 * @param <V> values
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
@NullMarked
public class NestedMap<K1, K2, V> {
    private final Map<K1, Map<K2, V>> map;
    private final Supplier<Map<K2, V>> innerFactory;

    /**
     * Creates a new nested map using the default implementations.
     * Default implementations are {@link IdentityHashMap} for the outer map and
     * {@link HashMap} for the inner maps.
     */
    public NestedMap() {
        this(IdentityHashMap::new, HashMap::new);
    }

    /**
     * Creates a new map using the specified factories.
     */
    public NestedMap(final Supplier<Map<K1, Map<K2, V>>> outerFactory,
                     final Supplier<Map<K2, V>> innerFactory) {
        this.map = outerFactory.get();
        this.innerFactory = innerFactory;
    }

    /**
     * Returns the number of key-key-value mappings in this map.
     */
    public int size() {
        int sum = 0;
        for (Map<K2, V> inner : this.map.values()) {
            sum += inner.size();
        }
        return sum;
    }

    /**
     * Returns {@code true} if this map contains no key-key-value mappings.
     */
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    /**
     * Returns the value to which the specified keys are mapped, or {@code null} if this map
     * contains no mapping for the keys.
     */
    @Nullable
    public V get(K1 k1, K2 k2) {
        Map<K2, V> inner = this.map.get(k1);
        if (inner == null) return null;
        return inner.get(k2);
    }

    /**
     * Associates the specified value with the specified keys.
     * If the map previously contained a mapping for the keys, the old value is replaced by the
     * specified value.
     * The previous value is returned.
     */
    @Nullable
    public V put(K1 k1, K2 k2, V v) {
        Map<K2, V> inner = this.map.computeIfAbsent(k1, _ -> this.innerFactory.get());
        V prev = inner.get(k2);
        inner.put(k2, v);
        return prev;
    }

    /**
     * Removes the mapping for the keys from this map if it is present.
     * The previously contained value is returned.
     */
    @Nullable
    public V remove(K1 k1, K2 k2) {
        Map<K2, V> inner = this.map.get(k1);
        if (inner == null) return null;
        V removed = inner.remove(k2);
        if (inner.isEmpty()) this.map.remove(k1);
        return removed;
    }

    /**
     * Removes all the mappings from this map.
     */
    public void clear() {
        this.map.clear();
    }
}
