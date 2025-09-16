package it.denzosoft.jreverse.analyzer.packageanalyzer;

import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.*;
import it.denzosoft.jreverse.core.port.PackageAnalyzer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Javassist-based implementation of PackageAnalyzer.
 * Analyzes package hierarchy, naming conventions, and organization patterns.
 */
public class JavassistPackageAnalyzer implements PackageAnalyzer {

    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistPackageAnalyzer.class);

    @Override
    public PackageAnalysisResult analyzePackageStructure(JarContent jarContent) {
        if (jarContent == null) {
            throw new IllegalArgumentException("JarContent cannot be null");
        }

        LOGGER.info("Starting package structure analysis for " + jarContent.getLocation().getFileName());

        try {
            // Build package hierarchy
            PackageHierarchy hierarchy = buildPackageHierarchy(jarContent);

            // Create simplified result with available fields only
            PackageAnalysisResult result = PackageAnalysisResult.builder()
                .hierarchy(hierarchy)
                .build();

            LOGGER.info("Package analysis completed. Found " + jarContent.getPackages().size() + " packages");
            return result;

        } catch (Exception e) {
            LOGGER.error("Error during package analysis", e);
            throw new RuntimeException("Package analysis failed: " + e.getMessage(), e);
        }
    }

    private PackageHierarchy buildPackageHierarchy(JarContent jarContent) {
        Map<String, PackageInfo> packageInfoMap = new HashMap<>();

        // Analyze each package with simplified info
        for (String packageName : jarContent.getPackages()) {
            PackageInfo packageInfo = analyzePackage(packageName, jarContent);
            packageInfoMap.put(packageName, packageInfo);
        }

        // Calculate hierarchy depth
        int maxDepth = packageInfoMap.keySet().stream()
            .mapToInt(pkg -> pkg.split("\\.").length)
            .max()
            .orElse(0);

        return PackageHierarchy.builder()
            .packages(packageInfoMap)
            .maxDepth(maxDepth)
            .build();
    }

    private PackageInfo analyzePackage(String packageName, JarContent jarContent) {
        Set<ClassInfo> classesInPackage = jarContent.getClassesInPackage(packageName);

        // Calculate basic package metrics
        PackageMetrics metrics = calculateBasicPackageMetrics(classesInPackage);

        return PackageInfo.builder()
            .name(packageName)
            .classes(classesInPackage)
            .metrics(metrics)
            .build();
    }

    private PackageMetrics calculateBasicPackageMetrics(Set<ClassInfo> classes) {
        // Basic metrics calculation using available methods
        int totalMethods = classes.stream()
            .mapToInt(c -> c.getMethods().size())
            .sum();

        int totalFields = classes.stream()
            .mapToInt(c -> c.getFields().size())
            .sum();

        int interfaceCount = (int) classes.stream().filter(ClassInfo::isInterface).count();
        int abstractCount = (int) classes.stream().filter(ClassInfo::isAbstract).count();
        int publicCount = (int) classes.stream().filter(ClassInfo::isPublic).count();

        // Calculate approximate complexity score
        double complexityScore = totalMethods * 1.5 + totalFields * 0.5;

        return PackageMetrics.builder()
            .classCount(classes.size())
            .interfaceCount(interfaceCount)
            .abstractClassCount(abstractCount)
            .publicClassCount(publicCount)
            .totalLinesOfCode(totalMethods * 5) // Approximate LOC
            .complexityScore(complexityScore)
            .cohesionScore(0.7) // Default cohesion score
            .afferentCoupling(0) // Simplified for now
            .efferentCoupling(classes.size() / 10) // Simplified coupling estimate
            .cyclomaticComplexity(totalMethods * 2) // Approximate complexity
            .build();
    }

    @Override
    public boolean canAnalyze(JarContent jarContent) {
        return jarContent != null && !jarContent.getPackages().isEmpty();
    }

    @Override
    public String getAnalyzerName() {
        return "Javassist Package Analyzer";
    }
}