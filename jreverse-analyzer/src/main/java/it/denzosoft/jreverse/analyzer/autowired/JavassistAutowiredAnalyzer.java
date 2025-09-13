package it.denzosoft.jreverse.analyzer.autowired;

import it.denzosoft.jreverse.analyzer.beancreation.BeanDependency;
import it.denzosoft.jreverse.analyzer.beancreation.BeanDependencyAnalyzer;
import it.denzosoft.jreverse.analyzer.beancreation.DependencyInjectionType;
import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.AutowiredAnalysisResult;
import it.denzosoft.jreverse.core.model.AutowiredDependency;
import it.denzosoft.jreverse.core.model.AutowiredSummary;
import it.denzosoft.jreverse.core.model.AutowiringIssue;
import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.port.AutowiredAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Javassist-based implementation of AutowiredAnalyzer that delegates to existing
 * BeanDependencyAnalyzer and transforms results to focused autowiring analysis.
 */
public class JavassistAutowiredAnalyzer implements AutowiredAnalyzer {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistAutowiredAnalyzer.class);
    
    private final BeanDependencyAnalyzer beanDependencyAnalyzer;
    
    public JavassistAutowiredAnalyzer() {
        this.beanDependencyAnalyzer = new BeanDependencyAnalyzer();
    }
    
    @Override
    public AutowiredAnalysisResult analyzeAutowiring(JarContent jarContent) {
        LOGGER.info("Starting autowiring analysis for JAR: %s", jarContent.getLocation().getFileName());
        
        try {
            List<AutowiredDependency> allDependencies = new ArrayList<>();
            Map<String, List<AutowiredDependency>> dependenciesByClass = new HashMap<>();
            List<AutowiringIssue> issues = new ArrayList<>();
            
            // Analyze each class for autowired dependencies
            for (ClassInfo classInfo : jarContent.getClasses()) {
                if (isRelevantForAutowiring(classInfo)) {
                    List<BeanDependency> classDependencies = beanDependencyAnalyzer.analyzeDependencies(classInfo);
                    
                    if (!classDependencies.isEmpty()) {
                        List<AutowiredDependency> autowiredDeps = transformToAutowiredDependencies(classInfo, classDependencies);
                        allDependencies.addAll(autowiredDeps);
                        dependenciesByClass.put(classInfo.getFullyQualifiedName(), autowiredDeps);
                        
                        // Analyze for issues
                        issues.addAll(analyzeAutowiringIssues(classInfo, classDependencies));
                    }
                }
            }
            
            // Calculate statistics
            Map<String, Integer> injectionTypeStats = calculateInjectionTypeStatistics(allDependencies);
            AutowiredSummary summary = buildSummary(jarContent, allDependencies, dependenciesByClass, issues, injectionTypeStats);
            
            LOGGER.info("Autowiring analysis completed. Found %d dependencies in %d classes with %d issues",
                allDependencies.size(), dependenciesByClass.size(), issues.size());
            
            return AutowiredAnalysisResult.builder()
                .dependencies(allDependencies)
                .dependenciesByClass(dependenciesByClass)
                .injectionTypeStatistics(injectionTypeStats)
                .issues(issues)
                .summary(summary)
                .build();
                
        } catch (Exception e) {
            LOGGER.error("Failed to analyze autowiring", e);
            throw new RuntimeException("Autowiring analysis failed", e);
        }
    }
    
    @Override
    public boolean canAnalyze(JarContent jarContent) {
        return jarContent != null && jarContent.getClasses() != null && !jarContent.getClasses().isEmpty();
    }
    
    /**
     * Checks if a class is relevant for autowiring analysis.
     */
    private boolean isRelevantForAutowiring(ClassInfo classInfo) {
        // Skip interfaces, enums, and annotations
        if (classInfo.isInterface() || classInfo.isEnum() || classInfo.isAnnotation()) {
            return false;
        }
        
        // Skip test classes
        String className = classInfo.getFullyQualifiedName();
        if (className.contains(".test.") || className.endsWith("Test") || className.endsWith("Tests")) {
            return false;
        }
        
        // Skip generated classes
        if (className.contains("$") || className.contains("CGLIB") || className.contains("Proxy")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Transforms BeanDependency objects to AutowiredDependency objects.
     */
    private List<AutowiredDependency> transformToAutowiredDependencies(ClassInfo classInfo, List<BeanDependency> beanDependencies) {
        return beanDependencies.stream()
            .map(beanDep -> AutowiredDependency.builder()
                .ownerClass(classInfo.getFullyQualifiedName())
                .dependencyType(beanDep.getType())
                .dependencyName(beanDep.getName())
                .injectionType(beanDep.getInjectionType().getDescription())
                .injectionPoint(beanDep.getInjectionPoint())
                .isRequired(beanDep.isRequired())
                .qualifier(beanDep.getQualifier())
                .isCollection(beanDep.isCollection())
                .build())
            .collect(Collectors.toList());
    }
    
    /**
     * Analyzes potential autowiring issues for a class.
     */
    private List<AutowiringIssue> analyzeAutowiringIssues(ClassInfo classInfo, List<BeanDependency> dependencies) {
        List<AutowiringIssue> issues = new ArrayList<>();
        String className = classInfo.getFullyQualifiedName();
        
        for (BeanDependency dependency : dependencies) {
            // Issue: Field injection discouraged
            if (dependency.getInjectionType() == DependencyInjectionType.FIELD) {
                issues.add(AutowiringIssue.builder()
                    .type(AutowiringIssue.IssueType.FIELD_INJECTION)
                    .severity(AutowiringIssue.Severity.WARNING)
                    .className(className)
                    .location(dependency.getInjectionPoint())
                    .message("Field injection is discouraged. Consider using constructor injection instead.")
                    .recommendation("Replace @Autowired field with constructor parameter for better testability and immutability.")
                    .build());
            }
            
            // Issue: Optional dependency pattern
            if (!dependency.isRequired()) {
                issues.add(AutowiringIssue.builder()
                    .type(AutowiringIssue.IssueType.OPTIONAL_DEPENDENCY)
                    .severity(AutowiringIssue.Severity.INFO)
                    .className(className)
                    .location(dependency.getInjectionPoint())
                    .message("Optional dependency detected with required=false.")
                    .recommendation("Ensure proper null-checking for optional dependencies.")
                    .build());
            }
            
            // Issue: Collection injection pattern
            if (dependency.isCollection()) {
                issues.add(AutowiringIssue.builder()
                    .type(AutowiringIssue.IssueType.COLLECTION_INJECTION)
                    .severity(AutowiringIssue.Severity.INFO)
                    .className(className)
                    .location(dependency.getInjectionPoint())
                    .message("Collection injection detected. All matching beans will be injected.")
                    .recommendation("Consider using @Qualifier if specific beans are needed, or @Order for ordering.")
                    .build());
            }
            
            // Issue: Missing qualifier for interface injection
            if (!dependency.hasQualifier() && isLikelyInterface(dependency.getType())) {
                issues.add(AutowiringIssue.builder()
                    .type(AutowiringIssue.IssueType.MISSING_QUALIFIER)
                    .severity(AutowiringIssue.Severity.WARNING)
                    .className(className)
                    .location(dependency.getInjectionPoint())
                    .message("Interface injection without qualifier may cause ambiguity if multiple implementations exist.")
                    .recommendation("Consider adding @Qualifier annotation if multiple implementations are available.")
                    .build());
            }
        }
        
        return issues;
    }
    
    /**
     * Checks if a type is likely an interface based on naming conventions.
     */
    private boolean isLikelyInterface(String type) {
        if (type == null) return false;
        
        // Common interface patterns
        String simpleName = type.substring(type.lastIndexOf('.') + 1);
        return simpleName.endsWith("Service") || 
               simpleName.endsWith("Repository") || 
               simpleName.endsWith("Component") ||
               simpleName.endsWith("Manager") ||
               simpleName.endsWith("Handler") ||
               simpleName.startsWith("I") && Character.isUpperCase(simpleName.charAt(1));
    }
    
    /**
     * Calculates statistics for injection types.
     */
    private Map<String, Integer> calculateInjectionTypeStatistics(List<AutowiredDependency> dependencies) {
        Map<String, Integer> stats = new HashMap<>();
        
        for (AutowiredDependency dependency : dependencies) {
            String injectionType = dependency.getInjectionType();
            stats.merge(injectionType, 1, Integer::sum);
        }
        
        return stats;
    }
    
    /**
     * Builds the summary for the autowiring analysis.
     */
    private AutowiredSummary buildSummary(JarContent jarContent, List<AutowiredDependency> allDependencies,
                                        Map<String, List<AutowiredDependency>> dependenciesByClass,
                                        List<AutowiringIssue> issues, Map<String, Integer> injectionTypeStats) {
        
        int totalClasses = jarContent.getClasses().size();
        int classesWithDependencies = dependenciesByClass.size();
        int totalDependencies = allDependencies.size();
        
        // Count injection types
        int constructorInjections = injectionTypeStats.getOrDefault("Constructor injection", 0);
        int fieldInjections = injectionTypeStats.getOrDefault("Field injection", 0);
        int setterInjections = injectionTypeStats.getOrDefault("Setter injection", 0);
        int methodInjections = injectionTypeStats.getOrDefault("Method injection", 0);
        int resourceInjections = injectionTypeStats.getOrDefault("Resource injection", 0);
        int injectAnnotations = injectionTypeStats.getOrDefault("@Inject injection", 0);
        
        // Count special dependency types
        int qualifiedDependencies = (int) allDependencies.stream().filter(AutowiredDependency::hasQualifier).count();
        int optionalDependencies = (int) allDependencies.stream().filter(dep -> !dep.isRequired()).count();
        int collectionDependencies = (int) allDependencies.stream().filter(AutowiredDependency::isCollection).count();
        
        // Count issues by severity
        int warningIssues = (int) issues.stream().filter(issue -> issue.getSeverity() == AutowiringIssue.Severity.WARNING).count();
        int errorIssues = (int) issues.stream().filter(issue -> issue.getSeverity() == AutowiringIssue.Severity.ERROR).count();
        
        return AutowiredSummary.builder()
            .totalClasses(totalClasses)
            .classesWithDependencies(classesWithDependencies)
            .totalDependencies(totalDependencies)
            .constructorInjections(constructorInjections)
            .fieldInjections(fieldInjections)
            .setterInjections(setterInjections)
            .methodInjections(methodInjections)
            .resourceInjections(resourceInjections)
            .injectAnnotations(injectAnnotations)
            .qualifiedDependencies(qualifiedDependencies)
            .optionalDependencies(optionalDependencies)
            .collectionDependencies(collectionDependencies)
            .totalIssues(issues.size())
            .warningIssues(warningIssues)
            .errorIssues(errorIssues)
            .build();
    }
}