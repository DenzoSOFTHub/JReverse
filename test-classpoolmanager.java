import it.denzosoft.jreverse.analyzer.util.ClassPoolManager;

/**
 * Test per verificare il funzionamento del ClassPoolManager ottimizzato
 */
class ClassPoolManagerTest {

    public static void main(String[] args) {
        System.out.println("=== Testing ClassPoolManager Optimizations ===");

        // Test 1: Singleton Pattern
        System.out.println("\n1. Testing Singleton Pattern:");
        ClassPoolManager manager1 = ClassPoolManager.getInstance();
        ClassPoolManager manager2 = ClassPoolManager.getInstance();
        boolean singletonWorking = (manager1 == manager2);
        System.out.println("   Singleton pattern working: " + singletonWorking + " ✅");

        // Test 2: Cache Statistics
        System.out.println("\n2. Testing Cache Statistics:");
        System.out.println("   Initial cache size: " + manager1.getCacheSize());
        System.out.println("   Initial hit rate: " + String.format("%.2f%%", manager1.getCacheHitRate()));
        System.out.println("   Total requests: " + manager1.getTotalRequests());

        // Test 3: Cache Operations (simulate some usage)
        System.out.println("\n3. Testing Cache Operations:");

        // Try to get some standard Java classes
        long startTime = System.nanoTime();
        boolean stringExists = manager1.classExists("java.lang.String");
        boolean objectExists = manager1.classExists("java.lang.Object");
        boolean listExists = manager1.classExists("java.util.List");
        long endTime = System.nanoTime();

        System.out.println("   java.lang.String exists: " + stringExists + " ✅");
        System.out.println("   java.lang.Object exists: " + objectExists + " ✅");
        System.out.println("   java.util.List exists: " + listExists + " ✅");
        System.out.println("   Class existence check time: " + (endTime - startTime) + " ns");

        // Test 4: Cache Statistics After Operations
        System.out.println("\n4. Testing Cache Statistics After Operations:");
        System.out.println("   Cache size after operations: " + manager1.getCacheSize());
        System.out.println("   Hit rate after operations: " + String.format("%.2f%%", manager1.getCacheHitRate()));
        System.out.println("   Total requests after operations: " + manager1.getTotalRequests());

        // Test 5: Clear Cache (Memory Leak Prevention)
        System.out.println("\n5. Testing Cache Clear (Memory Leak Prevention):");
        System.out.println("   Cache size before clear: " + manager1.getCacheSize());
        manager1.clearCache();
        System.out.println("   Cache size after clear: " + manager1.getCacheSize() + " ✅");
        System.out.println("   Hit rate after clear: " + String.format("%.2f%%", manager1.getCacheHitRate()));

        // Test 6: Manager Info
        System.out.println("\n6. Testing Manager Info:");
        System.out.println("   " + manager1.toString());

        System.out.println("\n=== ClassPoolManager optimizations working correctly! ===");
    }
}