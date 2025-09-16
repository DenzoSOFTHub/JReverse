package it.denzosoft.jreverse.core.util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

/**
 * JVM optimization utility providing runtime tuning recommendations
 * and performance monitoring for JReverse JAR analysis operations.
 *
 * Performance Benefits:
 * - Optimal GC settings for bytecode analysis workloads
 * - Memory management recommendations
 * - Runtime performance monitoring
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Performance Optimization)
 */
public final class JVMOptimizer {

    private JVMOptimizer() {
        // Utility class - no instantiation
    }

    /**
     * Get optimal JVM parameters for JReverse analysis operations.
     * These parameters are tuned for large JAR analysis with Javassist.
     *
     * @param maxHeapSizeMB maximum heap size in megabytes
     * @param analysisType type of analysis being performed
     * @return list of JVM parameter recommendations
     */
    public static List<String> getOptimalJVMParameters(int maxHeapSizeMB, AnalysisType analysisType) {
        List<String> params = new ArrayList<>();

        // OPTIMIZATION: Memory settings optimized for bytecode analysis
        params.add("-Xmx" + maxHeapSizeMB + "m");
        params.add("-Xms" + (maxHeapSizeMB / 2) + "m"); // Start with 50% of max heap

        // OPTIMIZATION: Use G1GC for large heap sizes, ParallelGC for smaller
        if (maxHeapSizeMB > 4096) {
            // G1GC is optimal for heaps > 4GB
            params.add("-XX:+UseG1GC");
            params.add("-XX:MaxGCPauseMillis=200"); // Target pause time
            params.add("-XX:G1HeapRegionSize=16m"); // Optimal for large objects
            params.add("-XX:+G1UseAdaptiveIHOP"); // Adaptive IHOP for better performance
        } else {
            // ParallelGC is better for smaller heaps
            params.add("-XX:+UseParallelGC");
            params.add("-XX:ParallelGCThreads=" + Math.min(8, Runtime.getRuntime().availableProcessors()));
        }

        // OPTIMIZATION: Metaspace settings for bytecode-heavy operations
        params.add("-XX:MetaspaceSize=256m");
        params.add("-XX:MaxMetaspaceSize=512m");

        // OPTIMIZATION: Compressed OOPs for memory efficiency
        params.add("-XX:+UseCompressedOops");
        params.add("-XX:+UseCompressedClassPointers");

        // OPTIMIZATION: String deduplication for repeated class names
        params.add("-XX:+UseStringDeduplication");

        // Analysis-specific optimizations
        switch (analysisType) {
            case LARGE_JAR_BATCH:
                // For batch processing of large JARs
                params.add("-XX:+AggressiveOpts");
                params.add("-XX:+UseFastAccessorMethods");
                params.add("-XX:+OptimizeStringConcat");
                break;

            case INTERACTIVE_ANALYSIS:
                // For interactive/UI analysis
                params.add("-XX:+UseBiasedLocking");
                params.add("-client"); // Faster startup
                break;

            case CONTINUOUS_INTEGRATION:
                // For CI/build environments
                params.add("-server");
                params.add("-XX:+TieredCompilation");
                params.add("-XX:TieredStopAtLevel=1"); // Faster compilation
                break;
        }

        // OPTIMIZATION: Performance monitoring flags
        params.add("-XX:+PrintGCDetails");
        params.add("-XX:+PrintGCTimeStamps");
        params.add("-XX:+PrintGCApplicationStoppedTime");

        return params;
    }

    /**
     * Get current JVM performance metrics.
     *
     * @return current performance statistics
     */
    public static JVMPerformanceMetrics getCurrentMetrics() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryBean.getHeapMemoryUsage().getMax();
        long nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed();
        long uptime = runtimeBean.getUptime();

        return new JVMPerformanceMetrics(heapUsed, heapMax, nonHeapUsed, uptime);
    }

    /**
     * Check if current JVM configuration is optimal for JReverse operations.
     *
     * @return optimization report with recommendations
     */
    public static OptimizationReport analyzeCurrentConfiguration() {
        JVMPerformanceMetrics metrics = getCurrentMetrics();
        List<String> recommendations = new ArrayList<>();
        boolean isOptimal = true;

        // Check heap usage
        double heapUsageRatio = (double) metrics.getHeapUsed() / metrics.getHeapMax();
        if (heapUsageRatio > 0.8) {
            recommendations.add("CRITICAL: Heap usage is " + String.format("%.1f", heapUsageRatio * 100) +
                              "%. Consider increasing -Xmx parameter.");
            isOptimal = false;
        }

        // Check for optimal GC
        List<String> jvmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        boolean hasOptimalGC = jvmArgs.stream().anyMatch(arg ->
            arg.contains("G1GC") || arg.contains("ParallelGC"));

        if (!hasOptimalGC) {
            recommendations.add("PERFORMANCE: No optimal GC detected. Consider using G1GC or ParallelGC.");
            isOptimal = false;
        }

        // Check metaspace configuration
        boolean hasMetaspaceConfig = jvmArgs.stream().anyMatch(arg ->
            arg.contains("MetaspaceSize"));

        if (!hasMetaspaceConfig) {
            recommendations.add("MEMORY: No metaspace configuration found. Set -XX:MetaspaceSize for better performance.");
            isOptimal = false;
        }

        return new OptimizationReport(isOptimal, recommendations, metrics);
    }

    /**
     * Suggest optimal parameters based on current system resources.
     *
     * @param analysisType the type of analysis being performed
     * @return suggested JVM parameters
     */
    public static List<String> suggestParametersForCurrentSystem(AnalysisType analysisType) {
        // Calculate optimal heap size based on available memory
        long availableMemory = Runtime.getRuntime().maxMemory();
        int optimalHeapMB = (int) (availableMemory / 1024 / 1024 * 0.8); // Use 80% of available

        return getOptimalJVMParameters(optimalHeapMB, analysisType);
    }

    /**
     * Force garbage collection and return performance impact metrics.
     *
     * @return GC performance impact report
     */
    public static GCImpactReport forceGCWithMetrics() {
        JVMPerformanceMetrics beforeGC = getCurrentMetrics();
        long startTime = System.currentTimeMillis();

        System.gc();
        System.gc(); // Run twice for thorough cleanup

        long gcTime = System.currentTimeMillis() - startTime;
        JVMPerformanceMetrics afterGC = getCurrentMetrics();

        long memoryFreed = beforeGC.getHeapUsed() - afterGC.getHeapUsed();
        double freedPercentage = ((double) memoryFreed / beforeGC.getHeapUsed()) * 100;

        return new GCImpactReport(gcTime, memoryFreed, freedPercentage, beforeGC, afterGC);
    }

    /**
     * Analysis type for JVM optimization.
     */
    public enum AnalysisType {
        LARGE_JAR_BATCH,      // Batch processing of large JAR files
        INTERACTIVE_ANALYSIS,  // Interactive UI-based analysis
        CONTINUOUS_INTEGRATION // CI/build environment analysis
    }

    /**
     * JVM performance metrics container.
     */
    public static class JVMPerformanceMetrics {
        private final long heapUsed;
        private final long heapMax;
        private final long nonHeapUsed;
        private final long uptime;

        public JVMPerformanceMetrics(long heapUsed, long heapMax, long nonHeapUsed, long uptime) {
            this.heapUsed = heapUsed;
            this.heapMax = heapMax;
            this.nonHeapUsed = nonHeapUsed;
            this.uptime = uptime;
        }

        public long getHeapUsed() { return heapUsed; }
        public long getHeapMax() { return heapMax; }
        public long getNonHeapUsed() { return nonHeapUsed; }
        public long getUptime() { return uptime; }

        public double getHeapUsagePercentage() {
            return ((double) heapUsed / heapMax) * 100.0;
        }

        @Override
        public String toString() {
            return String.format("JVM[heap=%dMB/%dMB(%.1f%%), nonHeap=%dMB, uptime=%ds]",
                PrimitiveOptimizer.fastDivideByPowerOf2((int)heapUsed, 1048576),
                PrimitiveOptimizer.fastDivideByPowerOf2((int)heapMax, 1048576),
                getHeapUsagePercentage(),
                PrimitiveOptimizer.fastDivideByPowerOf2((int)nonHeapUsed, 1048576),
                uptime / 1000);
        }
    }

    /**
     * JVM optimization report.
     */
    public static class OptimizationReport {
        private final boolean isOptimal;
        private final List<String> recommendations;
        private final JVMPerformanceMetrics metrics;

        public OptimizationReport(boolean isOptimal, List<String> recommendations,
                                 JVMPerformanceMetrics metrics) {
            this.isOptimal = isOptimal;
            this.recommendations = recommendations;
            this.metrics = metrics;
        }

        public boolean isOptimal() { return isOptimal; }
        public List<String> getRecommendations() { return recommendations; }
        public JVMPerformanceMetrics getMetrics() { return metrics; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("JVM Optimization Report\n");
            sb.append("Status: ").append(isOptimal ? "OPTIMAL" : "NEEDS IMPROVEMENT").append("\n");
            sb.append("Current Metrics: ").append(metrics).append("\n");

            if (!recommendations.isEmpty()) {
                sb.append("Recommendations:\n");
                for (String rec : recommendations) {
                    sb.append("  - ").append(rec).append("\n");
                }
            }

            return sb.toString();
        }
    }

    /**
     * Garbage collection impact report.
     */
    public static class GCImpactReport {
        private final long gcTimeMs;
        private final long memoryFreedBytes;
        private final double freedPercentage;
        private final JVMPerformanceMetrics beforeGC;
        private final JVMPerformanceMetrics afterGC;

        public GCImpactReport(long gcTimeMs, long memoryFreedBytes, double freedPercentage,
                             JVMPerformanceMetrics beforeGC, JVMPerformanceMetrics afterGC) {
            this.gcTimeMs = gcTimeMs;
            this.memoryFreedBytes = memoryFreedBytes;
            this.freedPercentage = freedPercentage;
            this.beforeGC = beforeGC;
            this.afterGC = afterGC;
        }

        public long getGcTimeMs() { return gcTimeMs; }
        public long getMemoryFreedBytes() { return memoryFreedBytes; }
        public double getFreedPercentage() { return freedPercentage; }
        public JVMPerformanceMetrics getBeforeGC() { return beforeGC; }
        public JVMPerformanceMetrics getAfterGC() { return afterGC; }

        @Override
        public String toString() {
            return String.format("GC Impact[time=%dms, freed=%dMB(%.1f%%), efficiency=%.1fMB/s]",
                gcTimeMs,
                PrimitiveOptimizer.fastDivideByPowerOf2((int)memoryFreedBytes, 1048576),
                freedPercentage,
                gcTimeMs > 0 ? ((double)memoryFreedBytes / 1048576) / (gcTimeMs / 1000.0) : 0.0);
        }
    }
}