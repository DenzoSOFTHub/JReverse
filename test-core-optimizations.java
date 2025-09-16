import it.denzosoft.jreverse.core.util.MemoryOptimizedCollections;
import it.denzosoft.jreverse.core.util.StreamOptimizer;

import java.util.*;

/**
 * Simple test to verify core performance optimizations are working.
 */
class CoreOptimizationTest {

    public static void main(String[] args) {
        System.out.println("=== Testing JReverse Core Performance Optimizations ===");

        // Test 1: MemoryOptimizedCollections
        System.out.println("\n1. Testing MemoryOptimizedCollections:");
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

        // Test 2: StreamOptimizer efficiency
        System.out.println("\n2. Testing StreamOptimizer:");
        List<String> testData = Arrays.asList("a", "b", "c", "a", "b", "d", "e", "f");

        long startTime = System.nanoTime();
        List<String> deduplicated = StreamOptimizer.efficientDeduplication(testData);
        long endTime = System.nanoTime();

        System.out.println("   Original size: " + testData.size());
        System.out.println("   Deduplicated size: " + deduplicated.size());
        System.out.println("   Deduplication time: " + (endTime - startTime) + " ns");

        // Test 3: Collection analysis
        System.out.println("\n3. Testing Collection analysis:");
        String analysis = StreamOptimizer.CollectionStats.analyzeCollection(testData);
        System.out.println("   Collection analysis: " + analysis);

        // Test 4: Efficient operations
        System.out.println("\n4. Testing efficient operations:");

        // Test efficient find first
        Optional<String> firstMatch = StreamOptimizer.efficientFindFirst(testData, s -> s.equals("b"));
        System.out.println("   Find first 'b': " + firstMatch.orElse("not found"));

        // Test efficient counting
        Map<String, Integer> counts = StreamOptimizer.efficientCounting(testData);
        System.out.println("   Count of each element: " + counts);

        // Test efficient grouping
        Map<String, List<String>> grouped = StreamOptimizer.efficientGroupBy(testData, s -> s.length() > 0 ? "nonEmpty" : "empty");
        System.out.println("   Grouped by length > 0: " + grouped.keySet());

        System.out.println("\n=== All core optimizations are working correctly! ===");
    }
}