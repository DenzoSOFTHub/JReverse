package it.denzosoft.jreverse.analyzer.external;

import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.*;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Javassist-based analyzer for external library dependencies and usage patterns.
 * Analyzes JAR dependencies, version conflicts, and library usage.
 */
public class JavassistExternalLibraryAnalyzer {

    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistExternalLibraryAnalyzer.class);

    // Common external library package patterns
    private static final Set<String> EXTERNAL_LIBRARY_PATTERNS = Set.of(
        "org.springframework",
        "org.apache",
        "com.fasterxml.jackson",
        "com.fasterxml",  // Added for Jackson detection at root level
        "org.slf4j",
        "ch.qos.logback",
        "org.hibernate",
        "com.google",
        "org.junit",
        "org.mockito",
        "com.h2database",
        "mysql",
        "postgresql",
        "oracle",
        "com.zaxxer.hikari"
    );

    private static final Pattern VERSION_PATTERN = Pattern.compile("\\d+\\.\\d+(\\.\\d+)?");

    public JavassistExternalLibraryAnalyzer() {
        LOGGER.info("JavassistExternalLibraryAnalyzer initialized");
    }

    /**
     * Analyzes JAR content for external library usage and dependencies.
     */
    public ExternalLibraryResult analyze(JarContent jarContent) {
        LOGGER.info("Starting external library analysis for JAR: {}", jarContent.getLocation().getFileName());

        try {
            // Identify external libraries
            Set<ExternalLibraryInfo> externalLibraries = identifyExternalLibraries(jarContent);

            // Analyze library usage patterns
            Map<String, LibraryUsageInfo> usagePatterns = analyzeLibraryUsage(jarContent, externalLibraries);

            // Detect version conflicts
            List<VersionConflict> versionConflicts = detectVersionConflicts(externalLibraries);

            // Analyze dependency relationships
            Map<String, Set<String>> dependencyGraph = buildDependencyGraph(externalLibraries);

            // Calculate metrics
            ExternalLibraryMetrics metrics = calculateMetrics(externalLibraries, usagePatterns);

            LOGGER.info("External library analysis completed. Found {} external libraries",
                       externalLibraries.size());

            return ExternalLibraryResult.builder()
                .externalLibraries(externalLibraries)
                .usagePatterns(usagePatterns)
                .versionConflicts(versionConflicts)
                .dependencyGraph(dependencyGraph)
                .metrics(metrics)
                .totalLibraries(externalLibraries.size())
                .build();

        } catch (Exception e) {
            LOGGER.error("Error during external library analysis", e);
            return ExternalLibraryResult.error("External library analysis failed: " + e.getMessage());
        }
    }

    private Set<ExternalLibraryInfo> identifyExternalLibraries(JarContent jarContent) {
        Set<ExternalLibraryInfo> libraries = new HashSet<>();

        // Analyze packages to identify external libraries
        Map<String, List<ClassInfo>> packageGroups = jarContent.getClasses().stream()
            .collect(Collectors.groupingBy(this::extractRootPackage));

        for (Map.Entry<String, List<ClassInfo>> entry : packageGroups.entrySet()) {
            String packageName = entry.getKey();
            List<ClassInfo> classes = entry.getValue();

            if (isExternalLibraryPackage(packageName)) {
                ExternalLibraryInfo library = createLibraryInfo(packageName, classes, jarContent);
                libraries.add(library);
            }
        }

        return libraries;
    }

    private String extractRootPackage(ClassInfo classInfo) {
        String fullyQualifiedName = classInfo.getFullyQualifiedName();
        String[] parts = fullyQualifiedName.split("\\.");

        if (parts.length >= 2) {
            return parts[0] + "." + parts[1];
        }
        return parts[0];
    }

    private boolean isExternalLibraryPackage(String packageName) {
        return EXTERNAL_LIBRARY_PATTERNS.stream()
            .anyMatch(pattern -> packageName.startsWith(pattern));
    }

    private ExternalLibraryInfo createLibraryInfo(String packageName, List<ClassInfo> classes, JarContent jarContent) {
        String libraryName = inferLibraryName(packageName);
        String version = extractVersionFromManifest(jarContent, libraryName);

        return ExternalLibraryInfo.builder()
            .name(libraryName)
            .packageName(packageName)
            .version(version)
            .classCount(classes.size())
            .licenses(extractLicenseInfo(jarContent))
            .description(inferLibraryDescription(libraryName))
            .build();
    }

    private String inferLibraryName(String packageName) {
        if (packageName.startsWith("org.springframework")) return "Spring Framework";
        if (packageName.startsWith("org.apache")) return "Apache Commons";
        if (packageName.startsWith("com.fasterxml.jackson") || packageName.startsWith("com.fasterxml")) return "Jackson JSON";
        if (packageName.startsWith("org.slf4j")) return "SLF4J";
        if (packageName.startsWith("ch.qos.logback")) return "Logback";
        if (packageName.startsWith("org.hibernate")) return "Hibernate";
        if (packageName.startsWith("com.google")) return "Google Libraries";
        if (packageName.startsWith("org.junit")) return "JUnit";
        if (packageName.startsWith("org.mockito")) return "Mockito";
        return "Unknown Library";
    }

    private String extractVersionFromManifest(JarContent jarContent, String libraryName) {
        JarManifestInfo manifest = jarContent.getManifest();
        if (manifest != null) {
            String implementationVersion = manifest.getImplementationVersion();
            if (implementationVersion != null && VERSION_PATTERN.matcher(implementationVersion).find()) {
                return implementationVersion;
            }
        }
        return "Unknown";
    }

    private Set<String> extractLicenseInfo(JarContent jarContent) {
        // This would typically read from META-INF/LICENSE files
        return Set.of("Unknown License");
    }

    private String inferLibraryDescription(String libraryName) {
        switch (libraryName) {
            case "Spring Framework": return "Java application framework";
            case "Jackson JSON": return "JSON processing library";
            case "SLF4J": return "Logging facade";
            case "Hibernate": return "Object-relational mapping framework";
            default: return "External library";
        }
    }

    private Map<String, LibraryUsageInfo> analyzeLibraryUsage(JarContent jarContent, Set<ExternalLibraryInfo> libraries) {
        Map<String, LibraryUsageInfo> usagePatterns = new HashMap<>();

        for (ExternalLibraryInfo library : libraries) {
            int usageCount = countLibraryUsage(jarContent, library);

            LibraryUsageInfo usage = LibraryUsageInfo.builder()
                .libraryName(library.getName())
                .usageCount(usageCount)
                .usageIntensity(calculateUsageIntensity(usageCount, jarContent.getClasses().size()))
                .mainUsagePatterns(identifyUsagePatterns(jarContent, library))
                .build();

            usagePatterns.put(library.getName(), usage);
        }

        return usagePatterns;
    }

    private int countLibraryUsage(JarContent jarContent, ExternalLibraryInfo library) {
        // Count classes that belong to the library package
        return (int) jarContent.getClasses().stream()
            .filter(classInfo -> classInfo.getFullyQualifiedName().startsWith(library.getPackageName()))
            .count();
    }

    private double calculateUsageIntensity(int usageCount, int totalClasses) {
        return totalClasses > 0 ? (double) usageCount / totalClasses : 0.0;
    }

    private Set<String> identifyUsagePatterns(JarContent jarContent, ExternalLibraryInfo library) {
        Set<String> patterns = new HashSet<>();

        if (library.getName().equals("Spring Framework")) {
            patterns.add("Dependency Injection");
            patterns.add("Configuration");
        }
        if (library.getName().equals("Jackson JSON")) {
            patterns.add("JSON Serialization");
        }

        return patterns;
    }

    private List<VersionConflict> detectVersionConflicts(Set<ExternalLibraryInfo> libraries) {
        List<VersionConflict> conflicts = new ArrayList<>();

        // Group libraries by name to detect version conflicts
        Map<String, List<ExternalLibraryInfo>> libraryGroups = libraries.stream()
            .collect(Collectors.groupingBy(ExternalLibraryInfo::getName));

        for (Map.Entry<String, List<ExternalLibraryInfo>> entry : libraryGroups.entrySet()) {
            List<ExternalLibraryInfo> versions = entry.getValue();
            if (versions.size() > 1) {
                VersionConflict conflict = VersionConflict.builder()
                    .libraryName(entry.getKey())
                    .conflictingVersions(versions.stream()
                        .map(ExternalLibraryInfo::getVersion)
                        .collect(Collectors.toSet()))
                    .severity(calculateConflictSeverity(versions))
                    .build();
                conflicts.add(conflict);
            }
        }

        return conflicts;
    }

    private String calculateConflictSeverity(List<ExternalLibraryInfo> versions) {
        // Simple heuristic based on version differences
        return versions.size() > 2 ? "HIGH" : "MEDIUM";
    }

    private Map<String, Set<String>> buildDependencyGraph(Set<ExternalLibraryInfo> libraries) {
        Map<String, Set<String>> graph = new HashMap<>();

        for (ExternalLibraryInfo library : libraries) {
            // This would require more sophisticated analysis of actual dependencies
            graph.put(library.getName(), new HashSet<>());
        }

        return graph;
    }

    private ExternalLibraryMetrics calculateMetrics(Set<ExternalLibraryInfo> libraries,
                                                   Map<String, LibraryUsageInfo> usagePatterns) {
        int totalLibraries = libraries.size();
        int totalClasses = libraries.stream()
            .mapToInt(ExternalLibraryInfo::getClassCount)
            .sum();

        double averageUsageIntensity = usagePatterns.values().stream()
            .mapToDouble(LibraryUsageInfo::getUsageIntensity)
            .average()
            .orElse(0.0);

        return ExternalLibraryMetrics.builder()
            .totalLibraries(totalLibraries)
            .totalExternalClasses(totalClasses)
            .averageUsageIntensity(averageUsageIntensity)
            .mostUsedLibrary(findMostUsedLibrary(usagePatterns))
            .build();
    }

    private String findMostUsedLibrary(Map<String, LibraryUsageInfo> usagePatterns) {
        return usagePatterns.entrySet().stream()
            .max(Map.Entry.comparingByValue(
                (u1, u2) -> Integer.compare(u1.getUsageCount(), u2.getUsageCount())))
            .map(Map.Entry::getKey)
            .orElse("None");
    }

    /**
     * Alias method for analyze() for backward compatibility with tests.
     */
    public ExternalLibraryResult analyzeExternalLibraries(JarContent jarContent) {
        return analyze(jarContent);
    }
}