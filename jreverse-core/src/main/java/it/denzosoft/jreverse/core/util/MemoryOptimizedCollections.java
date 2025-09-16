package it.denzosoft.jreverse.core.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class providing memory-optimized collection implementations.
 * These collections are designed for large-scale analysis operations
 * where memory efficiency is critical.
 *
 * Performance Benefits:
 * - Reduced object overhead compared to standard collections
 * - Better memory locality for cache performance
 * - Bounded growth to prevent OutOfMemoryError
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Performance Optimization)
 */
public class MemoryOptimizedCollections {

    /**
     * Create a memory-efficient Set with bounded growth.
     * Automatically switches to a more memory-efficient implementation
     * when the set grows beyond the specified threshold.
     *
     * @param expectedSize expected maximum size of the set
     * @param <T> element type
     * @return memory-optimized set implementation
     */
    public static <T> Set<T> createBoundedSet(int expectedSize) {
        if (expectedSize > 1000) {
            // OPTIMIZATION: Use power-of-2 division for better performance
            return Collections.newSetFromMap(new ConcurrentHashMap<>(
                PrimitiveOptimizer.fastDivideByPowerOf2(expectedSize, 4), 0.75f));
        } else {
            // OPTIMIZATION: Use next power of 2 for optimal hash distribution
            int optimalSize = PrimitiveOptimizer.nextPowerOf2(expectedSize);
            return new HashSet<>(PrimitiveOptimizer.fastMax(16, optimalSize));
        }
    }

    /**
     * Create a memory-efficient Map with bounded growth and LRU eviction.
     * When the map exceeds the maximum size, it removes the least recently used entries.
     *
     * @param expectedSize expected maximum size of the map
     * @param maxSize maximum allowed size before eviction starts
     * @param <K> key type
     * @param <V> value type
     * @return memory-optimized map implementation
     */
    public static <K, V> Map<K, V> createBoundedMap(int expectedSize, int maxSize) {
        return new BoundedConcurrentHashMap<>(expectedSize, maxSize);
    }

    /**
     * Create a memory-efficient List with appropriate initial capacity.
     *
     * @param expectedSize expected size of the list
     * @param <T> element type
     * @return memory-optimized list implementation
     */
    public static <T> List<T> createOptimizedList(int expectedSize) {
        // OPTIMIZATION: Use next power of 2 for optimal memory allocation
        int optimalSize = PrimitiveOptimizer.nextPowerOf2(PrimitiveOptimizer.fastMax(16, expectedSize));
        return new ArrayList<>(optimalSize);
    }

    /**
     * Bounded ConcurrentHashMap that implements LRU eviction when size limit is reached.
     * This prevents memory leaks in long-running analysis operations.
     */
    private static class BoundedConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {
        private final int maxSize;
        // FIXED: Use ConcurrentLinkedQueue for thread safety
        private final Queue<K> accessOrder = new java.util.concurrent.ConcurrentLinkedQueue<>();

        public BoundedConcurrentHashMap(int initialCapacity, int maxSize) {
            super(initialCapacity, 0.75f);
            this.maxSize = maxSize;
        }

        @Override
        public V put(K key, V value) {
            V result = super.put(key, value);

            // Track access order for LRU eviction
            synchronized (accessOrder) {
                accessOrder.remove(key); // Remove if already present
                accessOrder.offer(key);  // Add to end (most recent)

                // Evict least recently used entries if over size limit
                while (size() > maxSize && !accessOrder.isEmpty()) {
                    K lruKey = accessOrder.poll();
                    if (lruKey != null) {
                        super.remove(lruKey);
                    }
                }
            }

            return result;
        }

        @Override
        public V get(Object key) {
            V result = super.get(key);
            if (result != null) {
                // FIXED: Update access order with double-check under lock
                synchronized (accessOrder) {
                    // Double-check existence under lock to prevent race condition
                    if (super.containsKey(key)) {
                        accessOrder.remove(key);
                        accessOrder.offer((K) key);
                    }
                }
            }
            return result;
        }
    }

    /**
     * Estimate memory usage of common collection types.
     * This helps in making decisions about collection sizing and caching strategies.
     *
     * @param collection the collection to analyze
     * @return estimated memory usage in bytes
     */
    public static long estimateMemoryUsage(Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return 0L;
        }

        long baseObjectOverhead = 16; // Approximate object header size
        long referenceSize = 8; // 64-bit reference size

        if (collection instanceof HashSet) {
            // HashSet: array + node objects + references
            return baseObjectOverhead + (collection.size() * (baseObjectOverhead + referenceSize * 2));
        } else if (collection instanceof ArrayList) {
            // ArrayList: array + references
            return baseObjectOverhead + (((ArrayList<?>) collection).size() * referenceSize);
        } else if (collection instanceof ConcurrentHashMap) {
            // ConcurrentHashMap: more complex structure with segments
            return baseObjectOverhead + (collection.size() * (baseObjectOverhead + referenceSize * 3));
        }

        // Default estimation for other collection types
        return baseObjectOverhead + (collection.size() * referenceSize);
    }

    /**
     * Check if the current JVM memory usage is approaching dangerous levels.
     * This can be used to trigger garbage collection or cache clearing.
     *
     * @param warningThresholdPercent percentage of max memory to trigger warning (e.g., 80)
     * @return true if memory usage is high and should trigger cleanup
     */
    public static boolean isMemoryUsageHigh(int warningThresholdPercent) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        double usagePercentage = (double) usedMemory / maxMemory * 100;
        return usagePercentage > warningThresholdPercent;
    }

    /**
     * Force garbage collection and return memory statistics.
     * Should be used sparingly as it can impact performance.
     *
     * @return memory usage statistics after GC
     */
    public static MemoryStats forceGarbageCollection() {
        System.gc();
        System.gc(); // Call twice for better cleanup

        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        return new MemoryStats(usedMemory, maxMemory, totalMemory, freeMemory);
    }

    /**
     * Memory usage statistics.
     */
    public static class MemoryStats {
        public final long usedMemory;
        public final long maxMemory;
        public final long totalMemory;
        public final long freeMemory;

        public MemoryStats(long usedMemory, long maxMemory, long totalMemory, long freeMemory) {
            this.usedMemory = usedMemory;
            this.maxMemory = maxMemory;
            this.totalMemory = totalMemory;
            this.freeMemory = freeMemory;
        }

        public double getUsagePercentage() {
            return (double) usedMemory / maxMemory * 100.0;
        }

        // OPTIMIZATION: String caching for toString to avoid repeated calculations
        private volatile String cachedToString;

        @Override
        public String toString() {
            String result = cachedToString;
            if (result == null) {
                // OPTIMIZATION: Use StringBuilder + PrimitiveOptimizer for maximum efficiency
                StringBuilder sb = new StringBuilder(64); // Pre-sized buffer
                sb.append("Memory[used=")
                  .append(PrimitiveOptimizer.fastDivideByPowerOf2((int)usedMemory, 1048576)) // Convert bytes to MB
                  .append("MB, max=")
                  .append(PrimitiveOptimizer.fastDivideByPowerOf2((int)maxMemory, 1048576))
                  .append("MB, usage=")
                  .append(String.format("%.1f", getUsagePercentage()))
                  .append("%]");
                cachedToString = result = sb.toString();
            }
            return result;
        }
    }
}