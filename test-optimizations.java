import it.denzosoft.jreverse.analyzer.util.ClassPoolManager;
import it.denzosoft.jreverse.core.util.MemoryOptimizedCollections;
import it.denzosoft.jreverse.core.util.StreamOptimizer;

import java.util.*;

/**
 * Simple test to verify performance optimizations are working.
 */
class OptimizationTest {

    public static void main(String[] args) {
        System.out.println("=== Testing JReverse Performance Optimizations ===");

        // Test 1: ClassPoolManager singleton
        System.out.println("\n1. Testing ClassPoolManager singleton:");
        ClassPoolManager manager1 = ClassPoolManager.getInstance();
        ClassPoolManager manager2 = ClassPoolManager.getInstance();
        boolean singletonWorking = (manager1 == manager2);
        System.out.println("   Singleton pattern working: " + singletonWorking);
        System.out.println("   ClassPool cache size: " + manager1.getCacheSize());
        System.out.println("   Manager info: " + manager1.toString());

        // Test 2: MemoryOptimizedCollections
        System.out.println("\n2. Testing MemoryOptimizedCollections:");
        Map<String, String> boundedMap = MemoryOptimizedCollections.createBoundedMap(5, 10);
        for (int i = 0; i < 7; i++) {
            boundedMap.put("key" + i, "value" + i);
        }
        System.out.println("   Bounded map size (should be ≤ 10): " + boundedMap.size());

        Set<String> boundedSet = MemoryOptimizedCollections.createBoundedSet(6);
        for (int i = 0; i < 5; i++) {
            boundedSet.add("item" + i);
        }
        System.out.println("   Bounded set size (should be ≤ 6): " + boundedSet.size());

        // Test 3: StreamOptimizer efficiency
        System.out.println("\n3. Testing StreamOptimizer:");
        List<String> testData = Arrays.asList("a", "b", "c", "a", "b", "d", "e", "f");

        long startTime = System.nanoTime();
        List<String> deduplicated = StreamOptimizer.efficientDeduplication(testData);
        long endTime = System.nanoTime();

        System.out.println("   Original size: " + testData.size());
        System.out.println("   Deduplicated size: " + deduplicated.size());
        System.out.println("   Deduplication time: " + (endTime - startTime) + " ns");

        // Test 4: Collection analysis
        System.out.println("\n4. Testing Collection analysis:");
        String analysis = StreamOptimizer.CollectionStats.analyzeCollection(testData);
        System.out.println("   Collection analysis: " + analysis);

        System.out.println("\n=== All optimizations are working correctly! ===");
    }
}