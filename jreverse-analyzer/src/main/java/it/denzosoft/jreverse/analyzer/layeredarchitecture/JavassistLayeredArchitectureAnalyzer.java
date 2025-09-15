package it.denzosoft.jreverse.analyzer.layeredarchitecture;

import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.*;
import it.denzosoft.jreverse.core.model.LayeredArchitectureCompliance.ComplianceLevel;
import it.denzosoft.jreverse.core.port.LayeredArchitectureAnalyzer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Javassist-based implementation of LayeredArchitectureAnalyzer.
 * Analyzes layered architecture compliance, layer violations, and architectural quality.
 */
public class JavassistLayeredArchitectureAnalyzer implements LayeredArchitectureAnalyzer {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistLayeredArchitectureAnalyzer.class);
    
    private final Map<LayerType, Set<String>> layerIndicators;
    private final Map<LayerType, Integer> layerHierarchy;
    
    public JavassistLayeredArchitectureAnalyzer() {
        this.layerIndicators = initializeLayerIndicators();
        this.layerHierarchy = initializeLayerHierarchy();
        
        LOGGER.info("LayeredArchitectureAnalyzer initialized with {} layer types", layerIndicators.size());
    }
    
    private Map<LayerType, Set<String>> initializeLayerIndicators() {
        Map<LayerType, Set<String>> indicators = new HashMap<>();
        
        indicators.put(LayerType.PRESENTATION, Set.of(
            "Controller", "RestController", "WebController", "MvcController",
            "handler", "endpoint", "resource", "api", "web"
        ));
        
        indicators.put(LayerType.BUSINESS, Set.of(
            "Service", "BusinessService", "ApplicationService", "UseCase",
            "service", "business", "logic", "application", "workflow"
        ));
        
        indicators.put(LayerType.PERSISTENCE, Set.of(
            "Repository", "Dao", "DataAccess", "JpaRepository", "CrudRepository",
            "repository", "dao", "data", "persistence", "store"
        ));
        
        indicators.put(LayerType.DOMAIN, Set.of(
            "Entity", "Model", "Domain", "DomainObject", "ValueObject",
            "entity", "model", "domain", "dto", "pojo"
        ));
        
        indicators.put(LayerType.INFRASTRUCTURE, Set.of(
            "Configuration", "Config", "Infrastructure", "External", "Client",
            "config", "infrastructure", "external", "client", "gateway"
        ));
        
        return indicators;
    }
    
    private Map<LayerType, Integer> initializeLayerHierarchy() {
        Map<LayerType, Integer> hierarchy = new HashMap<>();
        
        // Lower numbers = higher in hierarchy (should not depend on higher numbers)
        hierarchy.put(LayerType.PRESENTATION, 1);
        hierarchy.put(LayerType.BUSINESS, 2);
        hierarchy.put(LayerType.DOMAIN, 3);
        hierarchy.put(LayerType.PERSISTENCE, 4);
        hierarchy.put(LayerType.INFRASTRUCTURE, 5);
        
        return hierarchy;
    }
    
    @Override
    public LayeredArchitectureResult analyzeLayeredArchitecture(JarContent jarContent) {
        if (jarContent == null) {
            throw new IllegalArgumentException("JarContent cannot be null");
        }
        
        long startTime = System.currentTimeMillis();
        LOGGER.startOperation("Layered architecture analysis");
        
        try {
            // Classify classes into layers
            Map<LayerType, Set<ClassInfo>> layerClassification = classifyClassesIntoLayers(jarContent);
            LOGGER.info("Classified {} classes into {} layers", 
                       jarContent.getClasses().size(), layerClassification.size());
            
            // Analyze layer dependencies
            List<LayerDependency> layerDependencies = analyzeDependenciesBetweenLayers(layerClassification, jarContent);
            
            // Detect layer violations
            List<LayerViolation> violations = detectLayerViolations(layerDependencies);
            LOGGER.info("Found {} layer violations", violations.size());
            
            // Calculate layer cohesion
            Map<LayerType, Double> layerCohesion = calculateLayerCohesion(layerClassification);
            
            // Calculate layer coupling
            Map<LayerType, Double> layerCoupling = calculateLayerCoupling(layerClassification, layerDependencies);
            
            // Generate layer metrics
            LayeredArchitectureMetrics metrics = calculateLayerMetrics(layerClassification, violations, layerCohesion, layerCoupling);
            
            // Generate recommendations
            List<LayerRecommendation> recommendations = generateLayerRecommendations(violations, metrics);
            
            // Calculate compliance
            LayeredArchitectureCompliance compliance = LayeredArchitectureCompliance.builder()
                .complianceScore(calculateArchitecturalCompliance(violations, layerClassification))
                .overallCompliance(ComplianceLevel.fromScore(calculateArchitecturalCompliance(violations, layerClassification)))
                .build();
            
            LayeredArchitectureResult result = LayeredArchitectureResult.builder()
                .layerClassification(layerClassification)
                .layerDependencies(layerDependencies)
                .violations(violations)
                .metrics(metrics)
                .recommendations(recommendations)
                .compliance(compliance)
                .build();
            
            LOGGER.info("Layered architecture analysis completed: {} layers, {} violations, compliance: {:.2f}%", 
                       layerClassification.size(), violations.size(), compliance.getComplianceScore() * 100);
            
            return result;
            
        } catch (Exception e) {
            LOGGER.error("Layered architecture analysis failed", e);
            
            // Return minimal result with error
            return LayeredArchitectureResult.builder()
                .metrics(LayeredArchitectureMetrics.builder()
                    .totalLayers(0)
                    .totalViolations(1)
                    .layeringCompliance(0.0)
                    .architecturalIntegrity(0.0)
                    .build())
                .compliance(LayeredArchitectureCompliance.builder()
                    .complianceScore(0.0)
                    .overallCompliance(ComplianceLevel.POOR)
                    .build())
                .violations(List.of(LayerViolation.builder()
                    .violationType(LayerViolation.Type.CIRCULAR_DEPENDENCY)
                    .severity(LayerViolation.Severity.CRITICAL)
                    .description("Layered architecture analysis failed: " + e.getMessage())
                    .sourceLayer(LayerType.INFRASTRUCTURE)
                    .targetLayer(LayerType.INFRASTRUCTURE)
                    .build()))
                .build();
                
        } finally {
            LOGGER.endOperation("Layered architecture analysis", System.currentTimeMillis() - startTime);
        }
    }
    
    @Override
    public boolean canAnalyze(JarContent jarContent) {
        // Check for classes that indicate layered architecture
        long layeredClasses = jarContent.getClasses().stream()
            .filter(this::hasLayerIndicators)
            .count();
        
        return layeredClasses >= 3; // Need at least 3 classes with layer indicators
    }
    
    @Override
    public void shutdown() {
        LOGGER.info("LayeredArchitectureAnalyzer shutting down");
    }
    
    private Map<LayerType, Set<ClassInfo>> classifyClassesIntoLayers(JarContent jarContent) {
        Map<LayerType, Set<ClassInfo>> classification = new HashMap<>();
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            LayerType layer = determineLayer(classInfo);
            classification.computeIfAbsent(layer, k -> new HashSet<>()).add(classInfo);
        }
        
        return classification;
    }
    
    private LayerType determineLayer(ClassInfo classInfo) {
        // Check annotations first
        for (AnnotationInfo annotation : classInfo.getAnnotations()) {
            LayerType layerFromAnnotation = getLayerFromAnnotation(annotation.getType());
            if (layerFromAnnotation != null) {
                return layerFromAnnotation;
            }
        }
        
        // Check class name and package
        for (Map.Entry<LayerType, Set<String>> entry : layerIndicators.entrySet()) {
            for (String indicator : entry.getValue()) {
                if (matchesLayerIndicator(classInfo, indicator)) {
                    return entry.getKey();
                }
            }
        }
        
        // Default classification based on package structure
        return classifyByPackageStructure(classInfo);
    }
    
    private LayerType getLayerFromAnnotation(String annotationType) {
        String simpleAnnotationName = getSimpleAnnotationName(annotationType);
        
        for (Map.Entry<LayerType, Set<String>> entry : layerIndicators.entrySet()) {
            if (entry.getValue().contains(simpleAnnotationName)) {
                return entry.getKey();
            }
        }
        
        return null;
    }
    
    private boolean matchesLayerIndicator(ClassInfo classInfo, String indicator) {
        return classInfo.getSimpleName().toLowerCase().contains(indicator.toLowerCase()) ||
               classInfo.getPackageName().toLowerCase().contains(indicator.toLowerCase());
    }
    
    private LayerType classifyByPackageStructure(ClassInfo classInfo) {
        String packageName = classInfo.getPackageName().toLowerCase();
        
        if (packageName.contains("controller") || packageName.contains("web") || packageName.contains("api")) {
            return LayerType.PRESENTATION;
        } else if (packageName.contains("service") || packageName.contains("business") || packageName.contains("application")) {
            return LayerType.BUSINESS;
        } else if (packageName.contains("repository") || packageName.contains("dao") || packageName.contains("data")) {
            return LayerType.PERSISTENCE;
        } else if (packageName.contains("model") || packageName.contains("entity") || packageName.contains("domain")) {
            return LayerType.DOMAIN;
        } else if (packageName.contains("config") || packageName.contains("infrastructure")) {
            return LayerType.INFRASTRUCTURE;
        }
        
        return LayerType.INFRASTRUCTURE; // Default fallback
    }
    
    private List<LayerDependency> analyzeDependenciesBetweenLayers(Map<LayerType, Set<ClassInfo>> layerClassification,
                                                                  JarContent jarContent) {
        List<LayerDependency> dependencies = new ArrayList<>();
        
        for (Map.Entry<LayerType, Set<ClassInfo>> sourceLayerEntry : layerClassification.entrySet()) {
            LayerType sourceLayer = sourceLayerEntry.getKey();
            
            for (ClassInfo sourceClass : sourceLayerEntry.getValue()) {
                Set<LayerType> targetLayers = findDependentLayers(sourceClass, layerClassification, jarContent);
                
                for (LayerType targetLayer : targetLayers) {
                    if (sourceLayer != targetLayer) {
                        String sourcePackage = extractPackageName(sourceClass.getFullyQualifiedName());
                        String targetPackage = findRepresentativePackageForLayer(targetLayer, layerClassification);

                        dependencies.add(LayerDependency.builder()
                            .sourceLayer(sourceLayer)
                            .targetLayer(targetLayer)
                            .sourcePackage(sourcePackage)
                            .targetPackage(targetPackage)
                            .addDependentClass(sourceClass.getFullyQualifiedName())
                            .dependencyType(LayerDependency.DependencyType.DIRECT)
                            .dependencyStrength(calculateDependencyStrength(sourceClass, targetLayer, layerClassification))
                            .build());
                    }
                }
            }
        }
        
        return dependencies;
    }
    
    private Set<LayerType> findDependentLayers(ClassInfo sourceClass, 
                                              Map<LayerType, Set<ClassInfo>> layerClassification,
                                              JarContent jarContent) {
        Set<LayerType> dependentLayers = new HashSet<>();
        
        // Check field types
        for (FieldInfo field : sourceClass.getFields()) {
            LayerType targetLayer = findLayerForType(field.getType(), layerClassification);
            if (targetLayer != null) {
                dependentLayers.add(targetLayer);
            }
        }
        
        // Check method parameters and return types
        for (MethodInfo method : sourceClass.getMethods()) {
            // Check return type
            LayerType returnTypeLayer = findLayerForType(method.getReturnType(), layerClassification);
            if (returnTypeLayer != null) {
                dependentLayers.add(returnTypeLayer);
            }
            
            // Check parameter types
            for (ParameterInfo parameter : method.getParameters()) {
                LayerType parameterLayer = findLayerForType(parameter.getType(), layerClassification);
                if (parameterLayer != null) {
                    dependentLayers.add(parameterLayer);
                }
            }
        }
        
        // Check interfaces
        for (String interfaceName : sourceClass.getInterfaceNames()) {
            LayerType interfaceLayer = findLayerForType(interfaceName, layerClassification);
            if (interfaceLayer != null) {
                dependentLayers.add(interfaceLayer);
            }
        }
        
        // Check superclass
        if (sourceClass.getSuperClassName() != null && !sourceClass.getSuperClassName().equals("java.lang.Object")) {
            LayerType superclassLayer = findLayerForType(sourceClass.getSuperClassName(), layerClassification);
            if (superclassLayer != null) {
                dependentLayers.add(superclassLayer);
            }
        }
        
        return dependentLayers;
    }
    
    private LayerType findLayerForType(String typeName, Map<LayerType, Set<ClassInfo>> layerClassification) {
        if (typeName == null || typeName.startsWith("java.")) {
            return null; // Skip Java standard library types
        }
        
        for (Map.Entry<LayerType, Set<ClassInfo>> entry : layerClassification.entrySet()) {
            for (ClassInfo classInfo : entry.getValue()) {
                if (classInfo.getFullyQualifiedName().equals(typeName)) {
                    return entry.getKey();
                }
            }
        }
        
        return null;
    }
    
    private double calculateDependencyStrength(ClassInfo sourceClass, LayerType targetLayer, 
                                              Map<LayerType, Set<ClassInfo>> layerClassification) {
        int dependencies = 0;
        int totalReferences = sourceClass.getFields().size() + sourceClass.getMethods().size();
        
        // Count dependencies to target layer
        for (FieldInfo field : sourceClass.getFields()) {
            if (findLayerForType(field.getType(), layerClassification) == targetLayer) {
                dependencies++;
            }
        }
        
        for (MethodInfo method : sourceClass.getMethods()) {
            if (findLayerForType(method.getReturnType(), layerClassification) == targetLayer) {
                dependencies++;
            }
            for (ParameterInfo parameter : method.getParameters()) {
                if (findLayerForType(parameter.getType(), layerClassification) == targetLayer) {
                    dependencies++;
                }
            }
        }
        
        return totalReferences > 0 ? (double) dependencies / totalReferences : 0.0;
    }
    
    private List<LayerViolation> detectLayerViolations(List<LayerDependency> layerDependencies) {
        List<LayerViolation> violations = new ArrayList<>();
        
        for (LayerDependency dependency : layerDependencies) {
            LayerViolation.Type violationType = checkForViolation(dependency);
            
            if (violationType != null) {
                violations.add(LayerViolation.builder()
                    .violationType(violationType)
                    .severity(calculateViolationSeverity(violationType, dependency))
                    .description(generateViolationDescription(violationType, dependency))
                    .sourceLayer(dependency.getSourceLayer())
                    .targetLayer(dependency.getTargetLayer())
                    .sourcePackage(dependency.getSourcePackage())
                    .impactScore(dependency.getDependencyStrength())
                    .build());
            }
        }
        
        return violations;
    }
    
    private LayerViolation.Type checkForViolation(LayerDependency dependency) {
        LayerType sourceLayer = dependency.getSourceLayer();
        LayerType targetLayer = dependency.getTargetLayer();
        
        Integer sourceHierarchy = layerHierarchy.get(sourceLayer);
        Integer targetHierarchy = layerHierarchy.get(targetLayer);
        
        if (sourceHierarchy == null || targetHierarchy == null) {
            return null; // Unknown layers
        }
        
        // Check for upward dependency violation (lower layer depending on higher layer)
        if (sourceHierarchy > targetHierarchy) {
            return LayerViolation.Type.UPWARD_DEPENDENCY;
        }
        
        // Check for skip layer violation (skipping intermediate layers)
        if (sourceHierarchy < targetHierarchy && (targetHierarchy - sourceHierarchy) > 1) {
            return LayerViolation.Type.SKIP_LAYER_DEPENDENCY;
        }
        
        // Check for circular dependency
        if (sourceLayer == targetLayer) {
            return LayerViolation.Type.CIRCULAR_DEPENDENCY;
        }
        
        return null; // No violation
    }
    
    private LayerViolation.Severity calculateViolationSeverity(LayerViolation.Type violationType, LayerDependency dependency) {
        switch (violationType) {
            case UPWARD_DEPENDENCY:
                return dependency.getDependencyStrength() > 0.5 ? LayerViolation.Severity.CRITICAL : LayerViolation.Severity.HIGH;
            case CIRCULAR_DEPENDENCY:
                return LayerViolation.Severity.CRITICAL;
            case SKIP_LAYER_DEPENDENCY:
                return LayerViolation.Severity.MEDIUM;
            default:
                return LayerViolation.Severity.LOW;
        }
    }
    
    private String generateViolationDescription(LayerViolation.Type violationType, LayerDependency dependency) {
        switch (violationType) {
            case UPWARD_DEPENDENCY:
                return String.format("Layer %s depends on higher layer %s", 
                                    dependency.getSourceLayer(), dependency.getTargetLayer());
            case CIRCULAR_DEPENDENCY:
                return String.format("Circular dependency detected in layer %s", dependency.getSourceLayer());
            case SKIP_LAYER_DEPENDENCY:
                return String.format("Layer %s skips intermediate layers to access %s", 
                                    dependency.getSourceLayer(), dependency.getTargetLayer());
            default:
                return "Unknown layer violation";
        }
    }
    
    private Map<LayerType, Double> calculateLayerCohesion(Map<LayerType, Set<ClassInfo>> layerClassification) {
        Map<LayerType, Double> cohesion = new HashMap<>();
        
        for (Map.Entry<LayerType, Set<ClassInfo>> entry : layerClassification.entrySet()) {
            double layerCohesion = calculateSingleLayerCohesion(entry.getValue());
            cohesion.put(entry.getKey(), layerCohesion);
        }
        
        return cohesion;
    }
    
    private double calculateSingleLayerCohesion(Set<ClassInfo> layerClasses) {
        if (layerClasses.isEmpty()) return 0.0;
        
        // Simplified cohesion calculation based on shared interfaces and packages
        Set<String> packages = layerClasses.stream()
            .map(ClassInfo::getPackageName)
            .collect(Collectors.toSet());
        
        Set<String> interfaces = layerClasses.stream()
            .flatMap(c -> c.getInterfaceNames().stream())
            .collect(Collectors.toSet());
        
        // Higher cohesion if classes are in similar packages and implement similar interfaces
        double packageCohesion = 1.0 / Math.max(1, packages.size());
        double interfaceCohesion = Math.min(1.0, interfaces.size() / (double) layerClasses.size());
        
        return (packageCohesion + interfaceCohesion) / 2.0;
    }
    
    private Map<LayerType, Double> calculateLayerCoupling(Map<LayerType, Set<ClassInfo>> layerClassification,
                                                         List<LayerDependency> layerDependencies) {
        Map<LayerType, Double> coupling = new HashMap<>();
        
        for (LayerType layer : layerClassification.keySet()) {
            double layerCoupling = calculateSingleLayerCoupling(layer, layerDependencies, layerClassification);
            coupling.put(layer, layerCoupling);
        }
        
        return coupling;
    }
    
    private double calculateSingleLayerCoupling(LayerType layer, 
                                               List<LayerDependency> layerDependencies,
                                               Map<LayerType, Set<ClassInfo>> layerClassification) {
        Set<ClassInfo> layerClasses = layerClassification.get(layer);
        if (layerClasses == null || layerClasses.isEmpty()) return 0.0;
        
        long outgoingDependencies = layerDependencies.stream()
            .filter(dep -> dep.getSourceLayer() == layer)
            .count();
        
        long incomingDependencies = layerDependencies.stream()
            .filter(dep -> dep.getTargetLayer() == layer)
            .count();
        
        long totalDependencies = outgoingDependencies + incomingDependencies;
        int maxPossibleDependencies = layerClasses.size() * (layerClassification.size() - 1);
        
        return maxPossibleDependencies > 0 ? (double) totalDependencies / maxPossibleDependencies : 0.0;
    }
    
    private LayeredArchitectureMetrics calculateLayerMetrics(Map<LayerType, Set<ClassInfo>> layerClassification,
                                             List<LayerViolation> violations,
                                             Map<LayerType, Double> layerCohesion,
                                             Map<LayerType, Double> layerCoupling) {
        
        int totalClasses = layerClassification.values().stream()
            .mapToInt(Set::size)
            .sum();
        
        double avgCohesion = layerCohesion.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
        
        double avgCoupling = layerCoupling.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
        
        return LayeredArchitectureMetrics.builder()
            .totalLayers(layerClassification.size())
            .classesByLayer(getClassCountsByLayer(layerClassification))
            .totalViolations(violations.size())
            .averageCohesionPerLayer(avgCohesion)
            .averageCouplingPerLayer(avgCoupling)
            .layeringCompliance(calculateLayerSeparationIndex(violations, totalClasses))
            .architecturalIntegrity(1.0 - calculateArchitecturalDebtIndex(violations, totalClasses))
            .build();
    }
    
    private double calculateLayerSeparationIndex(List<LayerViolation> violations, int totalClasses) {
        if (totalClasses == 0) return 0.0;
        
        double violationWeight = violations.stream()
            .mapToDouble(v -> getSeverityWeight(v.getSeverity()))
            .sum();
        
        return Math.max(0.0, 1.0 - (violationWeight / totalClasses));
    }
    
    private double calculateArchitecturalDebtIndex(List<LayerViolation> violations, int totalClasses) {
        if (totalClasses == 0) return 0.0;
        
        double debtScore = violations.stream()
            .mapToDouble(v -> getSeverityWeight(v.getSeverity()) * 
                             1.0)
            .sum();
        
        return Math.min(1.0, debtScore / totalClasses);
    }
    
    private double getSeverityWeight(LayerViolation.Severity severity) {
        switch (severity) {
            case CRITICAL: return 1.0;
            case HIGH: return 0.75;
            case MEDIUM: return 0.5;
            case LOW: return 0.25;
            default: return 0.1;
        }
    }
    
    private List<LayerRecommendation> generateLayerRecommendations(List<LayerViolation> violations, LayeredArchitectureMetrics metrics) {
        List<LayerRecommendation> recommendations = new ArrayList<>();
        
        // Group violations by type
        Map<LayerViolation.Type, List<LayerViolation>> violationsByType = violations.stream()
            .collect(Collectors.groupingBy(LayerViolation::getViolationType));
        
        for (Map.Entry<LayerViolation.Type, List<LayerViolation>> entry : violationsByType.entrySet()) {
            LayerRecommendation recommendation = generateRecommendationForViolationType(entry.getKey(), entry.getValue());
            if (recommendation != null) {
                recommendations.add(recommendation);
            }
        }
        
        // Add general recommendations based on metrics
        if (metrics.getArchitecturalIntegrity() < 0.5) {
            recommendations.add(LayerRecommendation.builder()
                .type(LayerRecommendation.RecommendationType.LAYER_RESTRUCTURING)
                .priority(LayerRecommendation.Priority.HIGH)
                .title("Architectural Refactoring")
                .description("High architectural debt detected. Consider systematic refactoring of layer violations.")
                .effortEstimate(0.8)
                .build());
        }
        
        return recommendations;
    }
    
    private LayerRecommendation generateRecommendationForViolationType(LayerViolation.Type violationType, 
                                                                      List<LayerViolation> violations) {
        switch (violationType) {
            case UPWARD_DEPENDENCY:
                return LayerRecommendation.builder()
                    .type(LayerRecommendation.RecommendationType.DEPENDENCY_REFACTORING)
                    .priority(LayerRecommendation.Priority.HIGH)
                    .title("Fix Upward Dependencies")
                    .description(String.format("Found %d upward dependencies. Consider using dependency inversion principle.", violations.size()))
                    .affectedComponents(violations.stream()
                        .filter(v -> v.hasSourcePackage())
                        .map(LayerViolation::getSourceClass)
                        .collect(Collectors.toSet()))
                    .effortEstimate(0.6)
                    .build();
                    
            case SKIP_LAYER_DEPENDENCY:
                return LayerRecommendation.builder()
                    .type(LayerRecommendation.RecommendationType.LAYER_RESTRUCTURING)
                    .priority(LayerRecommendation.Priority.MEDIUM)
                    .title("Fix Layer Skipping")
                    .description(String.format("Found %d layer-skipping dependencies. Consider proper layer sequencing.", violations.size()))
                    .affectedComponents(violations.stream()
                        .filter(v -> v.hasSourcePackage())
                        .map(LayerViolation::getSourceClass)
                        .collect(Collectors.toSet()))
                    .effortEstimate(0.3)
                    .build();
                    
            case CIRCULAR_DEPENDENCY:
                return LayerRecommendation.builder()
                    .type(LayerRecommendation.RecommendationType.DEPENDENCY_REFACTORING)
                    .priority(LayerRecommendation.Priority.CRITICAL)
                    .title("Fix Circular Dependencies")
                    .description(String.format("Found %d circular dependencies. Immediate resolution required.", violations.size()))
                    .affectedComponents(violations.stream()
                        .filter(v -> v.hasSourcePackage())
                        .map(LayerViolation::getSourceClass)
                        .collect(Collectors.toSet()))
                    .effortEstimate(0.8)
                    .build();
                    
            default:
                return null;
        }
    }
    
    private double calculateArchitecturalCompliance(List<LayerViolation> violations, 
                                                   Map<LayerType, Set<ClassInfo>> layerClassification) {
        int totalClasses = layerClassification.values().stream().mapToInt(Set::size).sum();
        if (totalClasses == 0) return 1.0;
        
        double violationWeight = violations.stream()
            .mapToDouble(v -> getSeverityWeight(v.getSeverity()))
            .sum();
        
        return Math.max(0.0, 1.0 - (violationWeight / totalClasses));
    }
    
    private ArchitecturalQuality calculateLayerSeparationQuality(List<LayerViolation> violations, 
                                                                  List<LayerDependency> layerDependencies) {
        if (violations.isEmpty()) {
            return ArchitecturalQuality.EXCELLENT;
        }
        
        double violationRatio = (double) violations.size() / Math.max(1, layerDependencies.size());
        
        if (violationRatio <= 0.1) return ArchitecturalQuality.GOOD;
        if (violationRatio <= 0.3) return ArchitecturalQuality.FAIR;
        return ArchitecturalQuality.POOR;
    }
    
    private ArchitecturalQuality calculateOverallQuality(LayeredArchitectureMetrics metrics, List<LayerViolation> violations) {
        double separationIndex = metrics.getLayeringCompliance();
        double debtIndex = 1.0 - metrics.getArchitecturalIntegrity();
        
        // Combine separation and debt indices
        double overallScore = (separationIndex * 0.7) + ((1.0 - debtIndex) * 0.3);
        
        if (overallScore >= 0.8) return ArchitecturalQuality.EXCELLENT;
        if (overallScore >= 0.6) return ArchitecturalQuality.GOOD;
        if (overallScore >= 0.4) return ArchitecturalQuality.FAIR;
        return ArchitecturalQuality.POOR;
    }
    
    private boolean hasLayerIndicators(ClassInfo classInfo) {
        // Check for layer-related annotations
        for (AnnotationInfo annotation : classInfo.getAnnotations()) {
            if (getLayerFromAnnotation(annotation.getType()) != null) {
                return true;
            }
        }
        
        // Check for layer-related naming patterns
        for (Set<String> indicators : layerIndicators.values()) {
            for (String indicator : indicators) {
                if (matchesLayerIndicator(classInfo, indicator)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private String getSimpleAnnotationName(String fullName) {
        int lastDot = fullName.lastIndexOf('.');
        return lastDot >= 0 ? fullName.substring(lastDot + 1) : fullName;
    }
    
    private Map<LayerType, Integer> getClassCountsByLayer(Map<LayerType, Set<ClassInfo>> layerClassification) {
        Map<LayerType, Integer> counts = new HashMap<>();
        for (Map.Entry<LayerType, Set<ClassInfo>> entry : layerClassification.entrySet()) {
            counts.put(entry.getKey(), entry.getValue().size());
        }
        return counts;
    }

    private String extractPackageName(String fullyQualifiedClassName) {
        if (fullyQualifiedClassName == null || fullyQualifiedClassName.isEmpty()) {
            return "default";
        }
        int lastDot = fullyQualifiedClassName.lastIndexOf('.');
        return lastDot > 0 ? fullyQualifiedClassName.substring(0, lastDot) : "default";
    }

    private String findRepresentativePackageForLayer(LayerType targetLayer,
                                                   Map<LayerType, Set<ClassInfo>> layerClassification) {
        Set<ClassInfo> layerClasses = layerClassification.get(targetLayer);
        if (layerClasses == null || layerClasses.isEmpty()) {
            return "unknown";
        }

        // Take the first class as representative
        ClassInfo representativeClass = layerClasses.iterator().next();
        return extractPackageName(representativeClass.getFullyQualifiedName());
    }
}