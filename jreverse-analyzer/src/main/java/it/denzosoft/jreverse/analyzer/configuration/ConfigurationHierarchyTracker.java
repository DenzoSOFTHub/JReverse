package it.denzosoft.jreverse.analyzer.configuration;

import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.AnnotationInfo;
import it.denzosoft.jreverse.core.model.ClassInfo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tracks configuration class hierarchies including inheritance and @Import relationships.
 */
public class ConfigurationHierarchyTracker {

    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(ConfigurationHierarchyTracker.class);

    // Spring configuration hierarchy annotations
    private static final Set<String> HIERARCHY_ANNOTATIONS = Set.of(
        "org.springframework.context.annotation.Import",
        "org.springframework.context.annotation.ImportResource",
        "org.springframework.context.annotation.ImportAutoConfiguration"
    );

    /**
     * Builds a configuration hierarchy from a list of configuration classes.
     */
    public ConfigurationHierarchy buildHierarchy(List<ClassInfo> configurationClasses) {
        if (configurationClasses == null || configurationClasses.isEmpty()) {
            return new ConfigurationHierarchy(Collections.emptyMap(), Collections.emptyMap());
        }

        Map<String, Set<String>> parentToChildren = new HashMap<>();
        Map<String, Set<String>> childToParents = new HashMap<>();

        // Build inheritance hierarchy
        buildInheritanceHierarchy(configurationClasses, parentToChildren, childToParents);

        // Build import hierarchy
        buildImportHierarchy(configurationClasses, parentToChildren, childToParents);

        LOGGER.info("Built configuration hierarchy with %d parent-child relationships",
            parentToChildren.values().stream().mapToInt(Set::size).sum());

        return new ConfigurationHierarchy(parentToChildren, childToParents);
    }

    /**
     * Builds inheritance-based hierarchy (extends relationships).
     */
    private void buildInheritanceHierarchy(List<ClassInfo> configurationClasses,
                                         Map<String, Set<String>> parentToChildren,
                                         Map<String, Set<String>> childToParents) {
        Map<String, ClassInfo> classMap = configurationClasses.stream()
            .collect(Collectors.toMap(ClassInfo::getFullyQualifiedName, classInfo -> classInfo));

        for (ClassInfo classInfo : configurationClasses) {
            String superClass = classInfo.getSuperClassName();
            if (superClass != null && classMap.containsKey(superClass)) {
                // This is a configuration class extending another configuration class
                addHierarchyRelationship(superClass, classInfo.getFullyQualifiedName(),
                    parentToChildren, childToParents);

                LOGGER.debug("Found inheritance: %s extends %s",
                    classInfo.getFullyQualifiedName(), superClass);
            }
        }
    }

    /**
     * Builds import-based hierarchy (@Import relationships).
     */
    private void buildImportHierarchy(List<ClassInfo> configurationClasses,
                                    Map<String, Set<String>> parentToChildren,
                                    Map<String, Set<String>> childToParents) {
        Map<String, ClassInfo> classMap = configurationClasses.stream()
            .collect(Collectors.toMap(ClassInfo::getFullyQualifiedName, classInfo -> classInfo));

        for (ClassInfo classInfo : configurationClasses) {
            for (AnnotationInfo annotation : classInfo.getAnnotations()) {
                if (HIERARCHY_ANNOTATIONS.contains(annotation.getType())) {
                    Set<String> importedClasses = extractImportedClasses(annotation);

                    for (String importedClass : importedClasses) {
                        if (classMap.containsKey(importedClass)) {
                            // This configuration imports another configuration
                            addHierarchyRelationship(classInfo.getFullyQualifiedName(), importedClass,
                                parentToChildren, childToParents);

                            LOGGER.debug("Found import: %s imports %s via %s",
                                classInfo.getFullyQualifiedName(), importedClass,
                                annotation.getSimpleType());
                        }
                    }
                }
            }
        }
    }

    /**
     * Extracts imported class names from @Import annotations.
     */
    private Set<String> extractImportedClasses(AnnotationInfo annotation) {
        Set<String> importedClasses = new HashSet<>();

        try {
            switch (annotation.getType()) {
                case "org.springframework.context.annotation.Import":
                case "org.springframework.context.annotation.ImportAutoConfiguration":
                    // These annotations have 'value' attribute with class array
                    String[] classArray = annotation.getStringArrayAttribute("value");
                    if (classArray != null) {
                        importedClasses.addAll(Arrays.asList(classArray));
                    }
                    break;

                case "org.springframework.context.annotation.ImportResource":
                    // This annotation imports XML resources, not classes
                    // We can track it for completeness but it doesn't create class hierarchy
                    String[] resources = annotation.getStringArrayAttribute("value");
                    if (resources != null) {
                        LOGGER.debug("Found resource imports: %s", Arrays.toString(resources));
                    }
                    break;
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to extract imported classes from %s: %s",
                annotation.getType(), e.getMessage());
        }

        return importedClasses;
    }

    /**
     * Adds a parent-child relationship to the hierarchy maps.
     */
    private void addHierarchyRelationship(String parent, String child,
                                        Map<String, Set<String>> parentToChildren,
                                        Map<String, Set<String>> childToParents) {
        parentToChildren.computeIfAbsent(parent, k -> new HashSet<>()).add(child);
        childToParents.computeIfAbsent(child, k -> new HashSet<>()).add(parent);
    }

    /**
     * Represents the configuration class hierarchy.
     */
    public static class ConfigurationHierarchy {
        private final Map<String, Set<String>> parentToChildren;
        private final Map<String, Set<String>> childToParents;

        public ConfigurationHierarchy(Map<String, Set<String>> parentToChildren,
                                    Map<String, Set<String>> childToParents) {
            this.parentToChildren = Collections.unmodifiableMap(parentToChildren);
            this.childToParents = Collections.unmodifiableMap(childToParents);
        }

        public Set<String> getChildren(String parentClass) {
            return parentToChildren.getOrDefault(parentClass, Collections.emptySet());
        }

        public Set<String> getParents(String childClass) {
            return childToParents.getOrDefault(childClass, Collections.emptySet());
        }

        public boolean hasChildren(String className) {
            return parentToChildren.containsKey(className) && !parentToChildren.get(className).isEmpty();
        }

        public boolean hasParents(String className) {
            return childToParents.containsKey(className) && !childToParents.get(className).isEmpty();
        }

        public Set<String> getAllConfigurationClasses() {
            Set<String> allClasses = new HashSet<>();
            allClasses.addAll(parentToChildren.keySet());
            allClasses.addAll(childToParents.keySet());
            return allClasses;
        }

        public int getHierarchyDepth(String className) {
            return calculateDepth(className, new HashSet<>());
        }

        private int calculateDepth(String className, Set<String> visited) {
            if (visited.contains(className)) {
                return 0; // Circular reference detected
            }

            visited.add(className);
            Set<String> parents = getParents(className);

            if (parents.isEmpty()) {
                return 0; // Root class
            }

            int maxParentDepth = parents.stream()
                .mapToInt(parent -> calculateDepth(parent, new HashSet<>(visited)))
                .max()
                .orElse(0);

            return maxParentDepth + 1;
        }

        public List<String> getRootConfigurations() {
            return getAllConfigurationClasses().stream()
                .filter(className -> !hasParents(className))
                .collect(Collectors.toList());
        }

        public List<String> getLeafConfigurations() {
            return getAllConfigurationClasses().stream()
                .filter(className -> !hasChildren(className))
                .collect(Collectors.toList());
        }

        @Override
        public String toString() {
            return "ConfigurationHierarchy{" +
                    "totalClasses=" + getAllConfigurationClasses().size() +
                    ", rootConfigurations=" + getRootConfigurations().size() +
                    ", leafConfigurations=" + getLeafConfigurations().size() +
                    '}';
        }
    }
}