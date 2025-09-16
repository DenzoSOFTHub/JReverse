package it.denzosoft.jreverse.analyzer.configuration;

import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.AnnotationInfo;
import it.denzosoft.jreverse.core.model.ClassInfo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzes @Import chains and resource imports in configuration classes.
 */
public class ImportChainAnalyzer {

    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(ImportChainAnalyzer.class);

    // Import-related annotations
    private static final Set<String> IMPORT_ANNOTATIONS = Set.of(
        "org.springframework.context.annotation.Import",
        "org.springframework.context.annotation.ImportResource",
        "org.springframework.context.annotation.ImportAutoConfiguration"
    );

    /**
     * Analyzes import chains for all configuration classes.
     */
    public Map<String, List<String>> analyzeImportChains(List<ClassInfo> configurationClasses) {
        if (configurationClasses == null || configurationClasses.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, List<String>> importChains = new HashMap<>();

        for (ClassInfo configClass : configurationClasses) {
            List<String> imports = analyzeClassImports(configClass);
            if (!imports.isEmpty()) {
                importChains.put(configClass.getFullyQualifiedName(), imports);
                LOGGER.debug("Configuration %s imports: %s",
                    configClass.getFullyQualifiedName(), imports);
            }
        }

        LOGGER.info("Analyzed import chains for %d configuration classes", importChains.size());
        return importChains;
    }

    /**
     * Analyzes imports for a single configuration class.
     */
    private List<String> analyzeClassImports(ClassInfo configClass) {
        List<String> imports = new ArrayList<>();

        for (AnnotationInfo annotation : configClass.getAnnotations()) {
            if (IMPORT_ANNOTATIONS.contains(annotation.getType())) {
                List<String> annotationImports = extractImportsFromAnnotation(annotation);
                imports.addAll(annotationImports);
            }
        }

        return imports;
    }

    /**
     * Extracts imports from a single import annotation.
     */
    private List<String> extractImportsFromAnnotation(AnnotationInfo annotation) {
        List<String> imports = new ArrayList<>();

        try {
            switch (annotation.getType()) {
                case "org.springframework.context.annotation.Import":
                    imports.addAll(extractClassImports(annotation, "Configuration classes"));
                    break;

                case "org.springframework.context.annotation.ImportAutoConfiguration":
                    imports.addAll(extractClassImports(annotation, "Auto-configuration classes"));
                    break;

                case "org.springframework.context.annotation.ImportResource":
                    imports.addAll(extractResourceImports(annotation));
                    break;
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to extract imports from %s: %s", annotation.getType(), e.getMessage());
        }

        return imports;
    }

    /**
     * Extracts class imports from @Import or @ImportAutoConfiguration.
     */
    private List<String> extractClassImports(AnnotationInfo annotation, String importType) {
        List<String> imports = new ArrayList<>();

        // Try 'value' attribute first
        String[] classArray = annotation.getStringArrayAttribute("value");
        if (classArray != null && classArray.length > 0) {
            for (String className : classArray) {
                imports.add(String.format("%s: %s", importType, className));
            }
            return imports;
        }

        // Try 'classes' attribute
        classArray = annotation.getStringArrayAttribute("classes");
        if (classArray != null && classArray.length > 0) {
            for (String className : classArray) {
                imports.add(String.format("%s: %s", importType, className));
            }
        }

        return imports;
    }

    /**
     * Extracts resource imports from @ImportResource.
     */
    private List<String> extractResourceImports(AnnotationInfo annotation) {
        List<String> imports = new ArrayList<>();

        // Try 'value' attribute first
        String[] resourceArray = annotation.getStringArrayAttribute("value");
        if (resourceArray != null && resourceArray.length > 0) {
            for (String resource : resourceArray) {
                imports.add(String.format("XML Resource: %s", resource));
            }
            return imports;
        }

        // Try 'locations' attribute
        resourceArray = annotation.getStringArrayAttribute("locations");
        if (resourceArray != null && resourceArray.length > 0) {
            for (String resource : resourceArray) {
                imports.add(String.format("XML Resource: %s", resource));
            }
        }

        return imports;
    }

    /**
     * Builds a dependency graph of import relationships.
     */
    public ImportDependencyGraph buildImportDependencyGraph(List<ClassInfo> configurationClasses) {
        Map<String, Set<String>> directImports = new HashMap<>();
        Map<String, Set<String>> resourceImports = new HashMap<>();

        for (ClassInfo configClass : configurationClasses) {
            String className = configClass.getFullyQualifiedName();

            for (AnnotationInfo annotation : configClass.getAnnotations()) {
                if ("org.springframework.context.annotation.Import".equals(annotation.getType()) ||
                    "org.springframework.context.annotation.ImportAutoConfiguration".equals(annotation.getType())) {

                    Set<String> imports = extractDirectClassImports(annotation);
                    if (!imports.isEmpty()) {
                        directImports.put(className, imports);
                    }

                } else if ("org.springframework.context.annotation.ImportResource".equals(annotation.getType())) {

                    Set<String> resources = extractDirectResourceImports(annotation);
                    if (!resources.isEmpty()) {
                        resourceImports.put(className, resources);
                    }
                }
            }
        }

        return new ImportDependencyGraph(directImports, resourceImports);
    }

    /**
     * Extracts direct class imports without formatting.
     */
    private Set<String> extractDirectClassImports(AnnotationInfo annotation) {
        Set<String> imports = new HashSet<>();

        String[] classArray = annotation.getStringArrayAttribute("value");
        if (classArray != null) {
            imports.addAll(Arrays.asList(classArray));
        }

        classArray = annotation.getStringArrayAttribute("classes");
        if (classArray != null) {
            imports.addAll(Arrays.asList(classArray));
        }

        return imports;
    }

    /**
     * Extracts direct resource imports without formatting.
     */
    private Set<String> extractDirectResourceImports(AnnotationInfo annotation) {
        Set<String> resources = new HashSet<>();

        String[] resourceArray = annotation.getStringArrayAttribute("value");
        if (resourceArray != null) {
            resources.addAll(Arrays.asList(resourceArray));
        }

        resourceArray = annotation.getStringArrayAttribute("locations");
        if (resourceArray != null) {
            resources.addAll(Arrays.asList(resourceArray));
        }

        return resources;
    }

    /**
     * Represents the import dependency graph.
     */
    public static class ImportDependencyGraph {
        private final Map<String, Set<String>> classImports;
        private final Map<String, Set<String>> resourceImports;

        public ImportDependencyGraph(Map<String, Set<String>> classImports,
                                   Map<String, Set<String>> resourceImports) {
            this.classImports = Collections.unmodifiableMap(classImports);
            this.resourceImports = Collections.unmodifiableMap(resourceImports);
        }

        public Set<String> getClassImports(String configurationClass) {
            return classImports.getOrDefault(configurationClass, Collections.emptySet());
        }

        public Set<String> getResourceImports(String configurationClass) {
            return resourceImports.getOrDefault(configurationClass, Collections.emptySet());
        }

        public Set<String> getAllImportingClasses() {
            Set<String> allImporting = new HashSet<>();
            allImporting.addAll(classImports.keySet());
            allImporting.addAll(resourceImports.keySet());
            return allImporting;
        }

        public Set<String> getAllImportedClasses() {
            return classImports.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        }

        public Set<String> getAllImportedResources() {
            return resourceImports.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        }

        public boolean hasImports(String configurationClass) {
            return classImports.containsKey(configurationClass) ||
                   resourceImports.containsKey(configurationClass);
        }

        public int getTotalImportCount() {
            int classImportCount = classImports.values().stream()
                .mapToInt(Set::size)
                .sum();
            int resourceImportCount = resourceImports.values().stream()
                .mapToInt(Set::size)
                .sum();
            return classImportCount + resourceImportCount;
        }

        @Override
        public String toString() {
            return "ImportDependencyGraph{" +
                    "importingClasses=" + getAllImportingClasses().size() +
                    ", importedClasses=" + getAllImportedClasses().size() +
                    ", importedResources=" + getAllImportedResources().size() +
                    ", totalImports=" + getTotalImportCount() +
                    '}';
        }
    }
}