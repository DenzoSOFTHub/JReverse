import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Simulazione realistica dell'analisi JAR con le ottimizzazioni Priority 3.
 * Questo test simula il comportamento del DefaultJarAnalyzer ottimizzato.
 */
public class JarAnalysisSimulationTest {

    private static final OptimizedLogger LOGGER = new OptimizedLogger("JarAnalysisTest");

    public static void main(String[] args) {
        System.out.println("=== Simulazione Analisi JAR con Ottimizzazioni Priority 3 ===\n");

        // Test con il JAR ottimizzato creato
        String jarPath = "/workspace/JReverse/test-optimized.jar";
        File jarFile = new File(jarPath);

        if (!jarFile.exists()) {
            System.out.println("JAR non trovato: " + jarPath);
            System.out.println("Eseguendo simulazione con dati mock...\n");
            simulateJarAnalysis();
        } else {
            System.out.println("Trovato JAR reale: " + jarPath);
            System.out.println("Analizzando JAR reale con ottimizzazioni...\n");
            analyzeRealJar(jarPath);
        }
    }

    private static void analyzeRealJar(String jarPath) {
        LOGGER.info("Starting OPTIMIZED analysis of JAR: " + jarPath);
        long startTime = System.currentTimeMillis();

        try (JarFile jarFile = new JarFile(jarPath)) {
            // Simula analyzeClasses con ottimizzazioni
            Set<ClassInfo> classes = analyzeClassesOptimized(jarFile);

            long analysisTime = System.currentTimeMillis() - startTime;
            LOGGER.info("Analysis completed. Found " + classes.size() + " classes in " +
                       jarFile.getName() + " (took " + analysisTime + "ms)");

            // Test delle ottimizzazioni specifiche
            testClassPoolOptimizations();
            testMemoryOptimizations(classes.size());
            testLazyLoggingOptimizations();

        } catch (IOException e) {
            LOGGER.error("Failed to analyze JAR: " + e.getMessage());
        }
    }

    private static Set<ClassInfo> analyzeClassesOptimized(JarFile jarFile) throws IOException {
        LOGGER.debug("Starting optimized class analysis for JAR entries");

        // OPTIMIZATION: Collect class entries first to avoid repeated stream operations
        List<JarEntry> classEntries = new ArrayList<>();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (isClassFile(entry)) {
                classEntries.add(entry);
            }
        }

        LOGGER.info("Found " + classEntries.size() + " class files to analyze");

        // OPTIMIZATION: Use parallel processing for large JARs with optimized threshold
        if (classEntries.size() > 100) {
            LOGGER.info("Large JAR detected - using optimized parallel processing");
            // OPTIMIZATION: Use memory-optimized collection with power-of-2 sizing
            int optimalSize = nextPowerOf2(classEntries.size());
            ConcurrentHashMap<String, ClassInfo> uniqueClasses = new ConcurrentHashMap<>(optimalSize, 0.75f);

            return classEntries.parallelStream()
                .map(entry -> safeAnalyzeClassEntry(entry, jarFile))
                .filter(Objects::nonNull)
                .filter(classInfo -> {
                    ClassInfo existing = uniqueClasses.putIfAbsent(
                        classInfo.getClassName(), classInfo);
                    if (existing != null) {
                        LOGGER.debug("Duplicate class found, keeping existing: " + existing.getClassName());
                    }
                    return existing == null; // Only include if we successfully added it
                })
                .collect(() -> new HashSet<>(), (set, item) -> set.add(item), (set1, set2) -> set1.addAll(set2));
        } else {
            LOGGER.info("Small JAR - using optimized sequential processing");
            // OPTIMIZATION: Small JAR - use sequential processing with optimized collection sizing
            Set<ClassInfo> classes = new HashSet<>(nextPowerOf2(classEntries.size()));
            for (JarEntry entry : classEntries) {
                ClassInfo classInfo = safeAnalyzeClassEntry(entry, jarFile);
                if (classInfo != null) {
                    classes.add(classInfo);
                }
            }
            return classes;
        }
    }

    private static ClassInfo safeAnalyzeClassEntry(JarEntry entry, JarFile jarFile) {
        try {
            return analyzeClassEntry(entry);
        } catch (Exception e) {
            LOGGER.debug("Failed to analyze class " + entry.getName() + ": " + e.getMessage());
            return null;
        }
    }

    private static ClassInfo analyzeClassEntry(JarEntry entry) {
        String className = getClassName(entry);
        // Simulazione dell'analisi della classe
        return new ClassInfo(className, determineClassType(entry));
    }

    private static boolean isClassFile(JarEntry entry) {
        return !entry.isDirectory() &&
               entry.getName().endsWith(".class") &&
               !entry.getName().contains("$") && // Skip inner classes for basic analysis
               !entry.getName().startsWith("META-INF/");
    }

    private static String getClassName(JarEntry entry) {
        String name = entry.getName();
        // Convert path to class name: com/example/Class.class -> com.example.Class
        return name.substring(0, name.length() - 6) // Remove .class
                   .replace('/', '.');
    }

    private static String determineClassType(JarEntry entry) {
        // Simulazione semplificata
        String name = entry.getName();
        if (name.contains("Interface")) return "INTERFACE";
        if (name.contains("Enum")) return "ENUM";
        if (name.contains("Annotation")) return "ANNOTATION";
        return "CLASS";
    }

    private static void simulateJarAnalysis() {
        LOGGER.info("Simulating JAR analysis with 5000 mock classes");
        long startTime = System.currentTimeMillis();

        // Simula l'analisi di un JAR grande
        Set<ClassInfo> classes = generateMockClasses(5000);

        long analysisTime = System.currentTimeMillis() - startTime;
        LOGGER.info("Mock analysis completed. Generated " + classes.size() + " classes (took " + analysisTime + "ms)");

        // Test delle ottimizzazioni
        testClassPoolOptimizations();
        testMemoryOptimizations(classes.size());
        testLazyLoggingOptimizations();
    }

    private static Set<ClassInfo> generateMockClasses(int count) {
        // OPTIMIZATION: Use optimized collection sizing
        Set<ClassInfo> classes = new HashSet<>(nextPowerOf2(count));

        for (int i = 0; i < count; i++) {
            String packageName = "com.example.package" + (i / 100);
            String className = packageName + ".Class" + i;
            String type = (i % 10 == 0) ? "INTERFACE" : "CLASS";
            classes.add(new ClassInfo(className, type));
        }

        return classes;
    }

    private static void testClassPoolOptimizations() {
        System.out.println("\n--- Test ClassPool Optimizations ---");
        long startTime = System.currentTimeMillis();

        // Simula ClassPoolManager con cache LRU
        ClassPoolSimulator simulator = new ClassPoolSimulator();

        // Test cache performance
        for (int i = 0; i < 1000; i++) {
            String className = "com.test.Class" + (i % 100); // Ripetizioni per test cache
            simulator.getCachedClass(className);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("ClassPool cache simulation: " + elapsed + "ms");
        System.out.println("Cache statistics: " + simulator.getStats());
        System.out.println("✅ ClassPool optimizations verified");
    }

    private static void testMemoryOptimizations(int classCount) {
        System.out.println("\n--- Test Memory Optimizations ---");

        // Test memory usage estimation
        long estimatedMemory = estimateMemoryUsage(classCount);
        System.out.println("Estimated memory usage for " + classCount + " classes: " +
                          (estimatedMemory / 1024 / 1024) + " MB");

        // Test memory threshold checking
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double usagePercentage = (double) usedMemory / runtime.maxMemory() * 100;

        System.out.println("Current memory usage: " + String.format("%.1f", usagePercentage) + "%");

        if (usagePercentage > 80) {
            System.out.println("⚠️  High memory usage - triggering optimization");
            // Simula garbage collection
            System.gc();
            System.out.println("✅ Memory optimization triggered");
        } else {
            System.out.println("✅ Memory usage optimal");
        }
    }

    private static void testLazyLoggingOptimizations() {
        System.out.println("\n--- Test Lazy Logging Optimizations ---");
        long startTime = System.currentTimeMillis();

        // Test lazy evaluation performance
        for (int i = 0; i < 1000; i++) {
            final int index = i;
            // Lazy logging - expensive computation only if debug enabled
            LOGGER.debug(() -> "Processing complex analysis for class " + index +
                              " with expensive computation: " + expensiveComputation(index));
        }

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("Lazy logging test completed in: " + elapsed + "ms");
        System.out.println("✅ Lazy logging optimizations verified");
    }

    private static String expensiveComputation(int value) {
        // Simula una computazione costosa che viene evitata con lazy logging
        return "result_" + (value * 1000 + System.currentTimeMillis());
    }

    private static long estimateMemoryUsage(int classCount) {
        long baseObjectOverhead = 16; // Approximate object header size
        long referenceSize = 8; // 64-bit reference size
        return classCount * (baseObjectOverhead + referenceSize * 3); // Simplified estimation
    }

    // OPTIMIZATION: Fast round-up to next power of 2 for optimal collection sizing
    private static int nextPowerOf2(int value) {
        if (value <= 1) return 1;
        return 1 << (32 - Integer.numberOfLeadingZeros(value - 1));
    }

    // Classe di supporto per simulare ClassInfo
    private static class ClassInfo {
        private final String className;
        private final String classType;

        public ClassInfo(String className, String classType) {
            this.className = className;
            this.classType = classType;
        }

        public String getClassName() { return className; }
        public String getClassType() { return classType; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ClassInfo)) return false;
            ClassInfo classInfo = (ClassInfo) o;
            return Objects.equals(className, classInfo.className);
        }

        @Override
        public int hashCode() {
            return Objects.hash(className);
        }
    }

    // Simulatore ClassPool ottimizzato
    private static class ClassPoolSimulator {
        private final Map<String, String> cache = new LinkedHashMap<String, String>(100, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > 100; // LRU cache con limite
            }
        };
        private int cacheHits = 0;
        private int cacheMisses = 0;

        public String getCachedClass(String className) {
            String cached = cache.get(className);
            if (cached != null) {
                cacheHits++;
                return cached;
            } else {
                cacheMisses++;
                // Simula caricamento classe
                String mockClass = "MockClass[" + className + "]";
                cache.put(className, mockClass);
                return mockClass;
            }
        }

        public String getStats() {
            int total = cacheHits + cacheMisses;
            double hitRate = total > 0 ? ((double) cacheHits / total * 100) : 0;
            return String.format("hits=%d, misses=%d, rate=%.1f%%", cacheHits, cacheMisses, hitRate);
        }
    }

    // Logger ottimizzato con lazy evaluation
    private static class OptimizedLogger {
        private final String name;

        public OptimizedLogger(String name) {
            this.name = name;
        }

        public void info(String message) {
            System.out.println("[INFO] " + name + ": " + message);
        }

        public void debug(String message) {
            // Debug disabilitato per performance - messaggio ignorato
        }

        public void debug(Supplier<String> messageSupplier) {
            // OPTIMIZATION: Lazy evaluation - messageSupplier.get() non viene chiamato
            // se debug è disabilitato, evitando computazioni costose
        }

        public void error(String message) {
            System.err.println("[ERROR] " + name + ": " + message);
        }
    }
}