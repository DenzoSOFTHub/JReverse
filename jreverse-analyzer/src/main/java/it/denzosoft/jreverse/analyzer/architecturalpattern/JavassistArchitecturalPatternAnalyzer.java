package it.denzosoft.jreverse.analyzer.architecturalpattern;

import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.*;
import it.denzosoft.jreverse.core.port.ArchitecturalPatternAnalyzer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Javassist-based implementation of ArchitecturalPatternAnalyzer.
 * Analyzes architectural patterns, design patterns, and anti-patterns.
 */
public class JavassistArchitecturalPatternAnalyzer implements ArchitecturalPatternAnalyzer {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistArchitecturalPatternAnalyzer.class);
    
    private final Set<String> frameworkAnnotations;
    private final Set<String> designPatternIndicators;
    private final Set<String> antiPatternIndicators;
    
    public JavassistArchitecturalPatternAnalyzer() {
        this.frameworkAnnotations = Set.of(
            "Controller", "RestController", "Service", "Repository", "Component",
            "Configuration", "Bean", "Entity", "Transactional"
        );
        this.designPatternIndicators = Set.of(
            "Factory", "Builder", "Singleton", "Observer", "Strategy", "Command", "Adapter"
        );
        this.antiPatternIndicators = Set.of(
            "God", "Manager", "Handler", "Util", "Helper", "Common", "Base"
        );
        
        LOGGER.info("ArchitecturalPatternAnalyzer initialized with {} framework annotations, {} pattern indicators, {} anti-pattern indicators",
                   frameworkAnnotations.size(), designPatternIndicators.size(), antiPatternIndicators.size());
    }
    
    @Override
    public ArchitecturalPatternResult analyzePatterns(JarContent jarContent) {
        if (jarContent == null) {
            throw new IllegalArgumentException("JarContent cannot be null");
        }
        
        long startTime = System.currentTimeMillis();
        LOGGER.startOperation("Architectural pattern analysis");
        
        try {
            // Analyze architectural patterns
            List<DetectedArchitecturalPattern> architecturalPatterns = detectArchitecturalPatterns(jarContent);
            LOGGER.info("Found {} architectural patterns", architecturalPatterns.size());
            
            // Analyze design patterns
            List<DetectedDesignPattern> designPatterns = detectDesignPatterns(jarContent);
            LOGGER.info("Found {} design patterns", designPatterns.size());
            
            // Detect anti-patterns
            List<DetectedAntiPattern> antiPatterns = detectAntiPatterns(jarContent);
            LOGGER.info("Found {} anti-patterns", antiPatterns.size());
            
            // Calculate architectural metrics
            ArchitecturalPatternMetrics metrics = calculateArchitecturalMetrics(jarContent, architecturalPatterns, antiPatterns);
            
            // Generate recommendations
            List<ArchitecturalRecommendation> recommendations = generateRecommendations(antiPatterns, metrics);
            
            ArchitecturalPatternResult result = ArchitecturalPatternResult.builder()
                .architecturalPatterns(architecturalPatterns)
                .designPatterns(designPatterns)
                .antiPatterns(antiPatterns)
                .metrics(metrics)
                .recommendations(recommendations)
                .build();
            
            LOGGER.info("Architectural pattern analysis completed: {} arch patterns, {} design patterns, {} anti-patterns", 
                       architecturalPatterns.size(), designPatterns.size(), antiPatterns.size());
            
            return result;
            
        } catch (Exception e) {
            LOGGER.error("Architectural pattern analysis failed", e);
            
            // Return minimal result with error
            return ArchitecturalPatternResult.builder()
                .antiPatterns(List.of(DetectedAntiPattern.builder()
                    .antiPatternType(AntiPatternType.LAYERING_VIOLATION)
                    .severity(ViolationSeverity.HIGH)
                    .description("Architectural pattern analysis failed: " + e.getMessage())
                    .addAffectedClass("Analysis Engine")
                    .build()))
                .metrics(ArchitecturalPatternMetrics.builder()
                    .totalPatternsDetected(0)
                    .patternCoverage(0.0)
                    .totalAntiPatternsDetected(1)
                    .overallPatternQuality(0.0)
                    .build())
                .build();
                
        } finally {
            LOGGER.endOperation("Architectural pattern analysis", System.currentTimeMillis() - startTime);
        }
    }
    
    @Override
    public boolean canAnalyze(JarContent jarContent) {
        // Check for Java classes with reasonable complexity
        return jarContent.getClasses().size() > 0 &&
               jarContent.getClasses().stream().anyMatch(this::hasArchitecturalRelevance);
    }
    
    @Override
    public void shutdown() {
        LOGGER.info("ArchitecturalPatternAnalyzer shutting down");
    }
    
    private List<DetectedArchitecturalPattern> detectArchitecturalPatterns(JarContent jarContent) {
        List<DetectedArchitecturalPattern> patterns = new ArrayList<>();
        
        // Detect layered architecture
        DetectedArchitecturalPattern layeredPattern = detectLayeredArchitecture(jarContent);
        if (layeredPattern != null) {
            patterns.add(layeredPattern);
        }
        
        // Detect MVC pattern
        DetectedArchitecturalPattern mvcPattern = detectMVCPattern(jarContent);
        if (mvcPattern != null) {
            patterns.add(mvcPattern);
        }
        
        // Detect microservice architecture indicators
        DetectedArchitecturalPattern microservicePattern = detectMicroservicePattern(jarContent);
        if (microservicePattern != null) {
            patterns.add(microservicePattern);
        }
        
        // Detect repository pattern
        DetectedArchitecturalPattern repositoryPattern = detectRepositoryPattern(jarContent);
        if (repositoryPattern != null) {
            patterns.add(repositoryPattern);
        }
        
        return patterns;
    }
    
    private DetectedArchitecturalPattern detectLayeredArchitecture(JarContent jarContent) {
        Map<String, Set<ClassInfo>> layers = new HashMap<>();
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            String layer = identifyLayer(classInfo);
            layers.computeIfAbsent(layer, k -> new HashSet<>()).add(classInfo);
        }
        
        // Need at least 3 layers for layered architecture
        if (layers.size() >= 3) {
            return DetectedArchitecturalPattern.builder()
                .name("Layered Architecture")
                .patternType(ArchitecturalPatternType.LAYERED)
                .confidenceScore(calculateLayeredArchitectureConfidence(layers))
                .description("Layered architecture with " + layers.size() + " distinct layers")
                .involvedPackages(layers.keySet().stream().map(Object::toString).collect(java.util.stream.Collectors.toSet()))
                .build();
        }
        
        return null;
    }
    
    private DetectedArchitecturalPattern detectMVCPattern(JarContent jarContent) {
        boolean hasControllers = jarContent.getClasses().stream()
            .anyMatch(this::isController);
        
        boolean hasServices = jarContent.getClasses().stream()
            .anyMatch(this::isService);
        
        boolean hasModels = jarContent.getClasses().stream()
            .anyMatch(this::isModel);
        
        if (hasControllers && hasServices && hasModels) {
            Set<String> components = Set.of("Controller", "Service", "Model");
            
            return DetectedArchitecturalPattern.builder()
                .name("Model-View-Controller (MVC)")
                .patternType(ArchitecturalPatternType.MVC)
                .confidenceScore(0.8)
                .description("MVC pattern with clear separation of concerns")
                .involvedPackages(components)
                .build();
        }
        
        return null;
    }
    
    private DetectedArchitecturalPattern detectMicroservicePattern(JarContent jarContent) {
        boolean hasSpringBootApplication = jarContent.getClasses().stream()
            .anyMatch(this::isSpringBootApplication);
        
        boolean hasRestControllers = jarContent.getClasses().stream()
            .anyMatch(this::isRestController);
        
        boolean hasConfigurationClasses = jarContent.getClasses().stream()
            .anyMatch(this::isConfiguration);
        
        if (hasSpringBootApplication && hasRestControllers && hasConfigurationClasses) {
            return DetectedArchitecturalPattern.builder()
                .name("Microservice Architecture")
                .patternType(ArchitecturalPatternType.MICROSERVICE)
                .confidenceScore(0.9)
                .description("Microservice architecture with Spring Boot")
                .involvedPackages(Set.of("REST API", "Configuration", "Business Logic"))
                .build();
        }
        
        return null;
    }
    
    private DetectedArchitecturalPattern detectRepositoryPattern(JarContent jarContent) {
        boolean hasRepositories = jarContent.getClasses().stream()
            .anyMatch(this::isRepository);
        
        boolean hasEntities = jarContent.getClasses().stream()
            .anyMatch(this::isEntity);
        
        if (hasRepositories && hasEntities) {
            return DetectedArchitecturalPattern.builder()
                .name("Repository Pattern")
                .patternType(ArchitecturalPatternType.SERVICE_ORIENTED)
                .confidenceScore(0.85)
                .description("Repository pattern for data access abstraction")
                .involvedPackages(Set.of("Repository", "Entity"))
                .build();
        }
        
        return null;
    }
    
    private List<DetectedDesignPattern> detectDesignPatterns(JarContent jarContent) {
        List<DetectedDesignPattern> patterns = new ArrayList<>();
        
        // Detect Singleton pattern
        patterns.addAll(detectSingletonPattern(jarContent));
        
        // Detect Factory pattern
        patterns.addAll(detectFactoryPattern(jarContent));
        
        // Detect Builder pattern
        patterns.addAll(detectBuilderPattern(jarContent));
        
        // Detect Strategy pattern
        patterns.addAll(detectStrategyPattern(jarContent));
        
        // Detect Observer pattern
        patterns.addAll(detectObserverPattern(jarContent));
        
        return patterns;
    }
    
    private List<DetectedDesignPattern> detectSingletonPattern(JarContent jarContent) {
        List<DetectedDesignPattern> singletons = new ArrayList<>();
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            if (isSingletonCandidate(classInfo)) {
                singletons.add(DetectedDesignPattern.builder()
                    .patternType(DesignPatternType.SINGLETON)
                    .confidenceScore(0.8)
                    .description("Singleton pattern implementation")
                    .participatingClasses(Set.of(classInfo.getFullyQualifiedName()))
                    .build());
            }
        }
        
        return singletons;
    }
    
    private List<DetectedDesignPattern> detectFactoryPattern(JarContent jarContent) {
        List<DetectedDesignPattern> factories = new ArrayList<>();
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            if (isFactoryCandidate(classInfo)) {
                factories.add(DetectedDesignPattern.builder()
                    .patternType(DesignPatternType.FACTORY_METHOD)
                    .confidenceScore(0.7)
                    .description("Factory pattern for object creation")
                    .participatingClasses(Set.of(classInfo.getFullyQualifiedName()))
                    .build());
            }
        }
        
        return factories;
    }
    
    private List<DetectedDesignPattern> detectBuilderPattern(JarContent jarContent) {
        List<DetectedDesignPattern> builders = new ArrayList<>();
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            if (isBuilderCandidate(classInfo)) {
                builders.add(DetectedDesignPattern.builder()
                    .patternType(DesignPatternType.BUILDER)
                    .confidenceScore(0.9)
                    .description("Builder pattern for complex object construction")
                    .participatingClasses(Set.of(classInfo.getFullyQualifiedName()))
                    .build());
            }
        }
        
        return builders;
    }
    
    private List<DetectedDesignPattern> detectStrategyPattern(JarContent jarContent) {
        List<DetectedDesignPattern> strategies = new ArrayList<>();
        
        // Look for interfaces with multiple implementations
        Map<String, List<ClassInfo>> interfaceImplementations = new HashMap<>();
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            for (String interfaceName : classInfo.getInterfaceNames()) {
                interfaceImplementations.computeIfAbsent(interfaceName, k -> new ArrayList<>()).add(classInfo);
            }
        }
        
        for (Map.Entry<String, List<ClassInfo>> entry : interfaceImplementations.entrySet()) {
            if (entry.getValue().size() >= 2 && isStrategyInterface(entry.getKey())) {
                Set<String> participantNames = entry.getValue().stream()
                    .map(ClassInfo::getFullyQualifiedName)
                    .collect(java.util.stream.Collectors.toSet());
                
                strategies.add(DetectedDesignPattern.builder()
                    .patternType(DesignPatternType.STRATEGY)
                    .confidenceScore(0.75)
                    .description("Strategy pattern with " + entry.getValue().size() + " implementations")
                    .participatingClasses(participantNames)
                    .build());
            }
        }
        
        return strategies;
    }
    
    private List<DetectedDesignPattern> detectObserverPattern(JarContent jarContent) {
        List<DetectedDesignPattern> observers = new ArrayList<>();
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            if (isObserverCandidate(classInfo)) {
                observers.add(DetectedDesignPattern.builder()
                    .patternType(DesignPatternType.OBSERVER)
                    .confidenceScore(0.6)
                    .description("Observer pattern implementation")
                    .participatingClasses(Set.of(classInfo.getFullyQualifiedName()))
                    .build());
            }
        }
        
        return observers;
    }
    
    private List<DetectedAntiPattern> detectAntiPatterns(JarContent jarContent) {
        List<DetectedAntiPattern> antiPatterns = new ArrayList<>();
        
        // Detect God Class anti-pattern
        antiPatterns.addAll(detectGodClassAntiPattern(jarContent));
        
        // Detect Long Parameter List anti-pattern
        antiPatterns.addAll(detectLongParameterListAntiPattern(jarContent));
        
        // Detect Large Class anti-pattern
        antiPatterns.addAll(detectLargeClassAntiPattern(jarContent));
        
        // Detect Shotgun Surgery anti-pattern
        antiPatterns.addAll(detectShotgunSurgeryAntiPattern(jarContent));
        
        return antiPatterns;
    }
    
    private List<DetectedAntiPattern> detectGodClassAntiPattern(JarContent jarContent) {
        List<DetectedAntiPattern> godClasses = new ArrayList<>();
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            if (isGodClass(classInfo)) {
                godClasses.add(DetectedAntiPattern.builder()
                    .antiPatternType(AntiPatternType.GOD_PACKAGE)
                    .severity(ViolationSeverity.HIGH)
                    .description("God class with excessive responsibilities")
                    .addAffectedClass(classInfo.getFullyQualifiedName())
                    .addEvidence("methods", String.valueOf(classInfo.getMethods().size()))
                    .addEvidence("fields", String.valueOf(classInfo.getFields().size()))
                    .build());
            }
        }
        
        return godClasses;
    }
    
    private List<DetectedAntiPattern> detectLongParameterListAntiPattern(JarContent jarContent) {
        List<DetectedAntiPattern> longParameterLists = new ArrayList<>();
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            for (MethodInfo method : classInfo.getMethods()) {
                if (hasLongParameterList(method)) {
                    longParameterLists.add(DetectedAntiPattern.builder()
                        .antiPatternType(AntiPatternType.MIXED_CONCERNS)
                        .severity(ViolationSeverity.MEDIUM)
                        .description("Method with excessive parameters: " + method.getName())
                        .addAffectedClass(classInfo.getFullyQualifiedName() + "." + method.getName())
                        .addEvidence("parameters", String.valueOf(method.getParameters().size()))
                        .build());
                }
            }
        }
        
        return longParameterLists;
    }
    
    private List<DetectedAntiPattern> detectLargeClassAntiPattern(JarContent jarContent) {
        List<DetectedAntiPattern> largeClasses = new ArrayList<>();
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            if (isLargeClass(classInfo)) {
                largeClasses.add(DetectedAntiPattern.builder()
                    .antiPatternType(AntiPatternType.GOD_PACKAGE)
                    .severity(ViolationSeverity.MEDIUM)
                    .description("Large class with too many methods and fields")
                    .addAffectedClass(classInfo.getFullyQualifiedName())
                    .addEvidence("methods", String.valueOf(classInfo.getMethods().size()))
                    .addEvidence("fields", String.valueOf(classInfo.getFields().size()))
                    .addEvidence("linesOfCode", String.valueOf(estimateLinesOfCode(classInfo)))
                    .build());
            }
        }
        
        return largeClasses;
    }
    
    private List<DetectedAntiPattern> detectShotgunSurgeryAntiPattern(JarContent jarContent) {
        List<DetectedAntiPattern> shotgunSurgery = new ArrayList<>();
        
        // Detect classes with many small, scattered methods
        for (ClassInfo classInfo : jarContent.getClasses()) {
            if (isShotgunSurgeryCandidate(classInfo)) {
                shotgunSurgery.add(DetectedAntiPattern.builder()
                    .antiPatternType(AntiPatternType.SHOTGUN_SURGERY)
                    .severity(ViolationSeverity.MEDIUM)
                    .description("Class requires frequent small changes across many methods")
                    .addAffectedClass(classInfo.getFullyQualifiedName())
                    .addEvidence("smallMethods", String.valueOf(countSmallMethods(classInfo)))
                    .build());
            }
        }
        
        return shotgunSurgery;
    }
    
    // Helper methods for pattern detection
    
    private boolean hasArchitecturalRelevance(ClassInfo classInfo) {
        return classInfo.getAnnotations().stream()
            .anyMatch(annotation -> frameworkAnnotations.contains(getSimpleAnnotationName(annotation.getType()))) ||
               classInfo.getMethods().size() > 3 ||
               !classInfo.getInterfaceNames().isEmpty();
    }
    
    private String identifyLayer(ClassInfo classInfo) {
        if (isController(classInfo)) return "Controller";
        if (isService(classInfo)) return "Service";
        if (isRepository(classInfo)) return "Repository";
        if (isEntity(classInfo)) return "Entity";
        if (isConfiguration(classInfo)) return "Configuration";
        if (classInfo.getPackageName().contains("util")) return "Utility";
        return "Other";
    }
    
    private double calculateLayeredArchitectureConfidence(Map<String, Set<ClassInfo>> layers) {
        // Higher confidence if we have standard layers
        double confidence = 0.5;
        
        if (layers.containsKey("Controller")) confidence += 0.15;
        if (layers.containsKey("Service")) confidence += 0.15;
        if (layers.containsKey("Repository")) confidence += 0.15;
        if (layers.containsKey("Entity")) confidence += 0.05;
        
        return Math.min(1.0, confidence);
    }
    
    private boolean isController(ClassInfo classInfo) {
        return classInfo.getAnnotations().stream()
            .anyMatch(annotation -> annotation.getType().contains("Controller"));
    }
    
    private boolean isRestController(ClassInfo classInfo) {
        return classInfo.getAnnotations().stream()
            .anyMatch(annotation -> annotation.getType().contains("RestController"));
    }
    
    private boolean isService(ClassInfo classInfo) {
        return classInfo.getAnnotations().stream()
            .anyMatch(annotation -> annotation.getType().contains("Service"));
    }
    
    private boolean isRepository(ClassInfo classInfo) {
        return classInfo.getAnnotations().stream()
            .anyMatch(annotation -> annotation.getType().contains("Repository"));
    }
    
    private boolean isEntity(ClassInfo classInfo) {
        return classInfo.getAnnotations().stream()
            .anyMatch(annotation -> annotation.getType().contains("Entity"));
    }
    
    private boolean isConfiguration(ClassInfo classInfo) {
        return classInfo.getAnnotations().stream()
            .anyMatch(annotation -> annotation.getType().contains("Configuration"));
    }
    
    private boolean isSpringBootApplication(ClassInfo classInfo) {
        return classInfo.getAnnotations().stream()
            .anyMatch(annotation -> annotation.getType().contains("SpringBootApplication"));
    }
    
    private boolean isModel(ClassInfo classInfo) {
        return isEntity(classInfo) || 
               classInfo.getPackageName().contains("model") ||
               classInfo.getPackageName().contains("dto") ||
               classInfo.getPackageName().contains("domain");
    }
    
    private boolean isSingletonCandidate(ClassInfo classInfo) {
        // Look for private constructor and getInstance method
        boolean hasPrivateConstructor = classInfo.getMethods().stream()
            .anyMatch(method -> method.getName().equals("<init>") && method.isPrivate());
        
        boolean hasGetInstanceMethod = classInfo.getMethods().stream()
            .anyMatch(method -> method.getName().toLowerCase().contains("instance") && method.isStatic());
        
        return hasPrivateConstructor && hasGetInstanceMethod;
    }
    
    private boolean isFactoryCandidate(ClassInfo classInfo) {
        return classInfo.getSimpleName().toLowerCase().contains("factory") ||
               classInfo.getMethods().stream()
                   .anyMatch(method -> method.getName().toLowerCase().startsWith("create") && method.isStatic());
    }
    
    private boolean isBuilderCandidate(ClassInfo classInfo) {
        return classInfo.getSimpleName().toLowerCase().contains("builder") ||
               (classInfo.getMethods().stream().anyMatch(method -> method.getName().equals("build")) &&
                classInfo.getMethods().stream().anyMatch(method -> method.getReturnType().equals(classInfo.getFullyQualifiedName())));
    }
    
    private boolean isStrategyInterface(String interfaceName) {
        return interfaceName.toLowerCase().contains("strategy") ||
               interfaceName.toLowerCase().contains("handler") ||
               interfaceName.toLowerCase().contains("processor");
    }
    
    private boolean isObserverCandidate(ClassInfo classInfo) {
        return classInfo.getSimpleName().toLowerCase().contains("observer") ||
               classInfo.getSimpleName().toLowerCase().contains("listener") ||
               classInfo.getMethods().stream()
                   .anyMatch(method -> method.getName().toLowerCase().startsWith("notify") ||
                                      method.getName().toLowerCase().startsWith("update"));
    }
    
    private boolean isGodClass(ClassInfo classInfo) {
        return classInfo.getMethods().size() > 20 || 
               classInfo.getFields().size() > 15 ||
               classInfo.getSimpleName().toLowerCase().contains("manager") ||
               classInfo.getSimpleName().toLowerCase().contains("util");
    }
    
    private boolean hasLongParameterList(MethodInfo method) {
        return method.getParameters().size() > 6;
    }
    
    private boolean isLargeClass(ClassInfo classInfo) {
        int totalMembers = classInfo.getMethods().size() + classInfo.getFields().size();
        return totalMembers > 30 || estimateLinesOfCode(classInfo) > 500;
    }
    
    private boolean isShotgunSurgeryCandidate(ClassInfo classInfo) {
        int smallMethods = countSmallMethods(classInfo);
        return smallMethods > classInfo.getMethods().size() * 0.7; // More than 70% small methods
    }
    
    private int countSmallMethods(ClassInfo classInfo) {
        // Estimate small methods (methods with likely few lines of code)
        return (int) classInfo.getMethods().stream()
            .filter(method -> method.getParameters().size() <= 2 && 
                             !method.getName().startsWith("get") && 
                             !method.getName().startsWith("set"))
            .count();
    }
    
    private int estimateLinesOfCode(ClassInfo classInfo) {
        // Rough estimation based on methods and fields
        return classInfo.getMethods().size() * 5 + classInfo.getFields().size() * 2 + 10;
    }
    
    private String getSimpleAnnotationName(String fullName) {
        int lastDot = fullName.lastIndexOf('.');
        return lastDot >= 0 ? fullName.substring(lastDot + 1) : fullName;
    }
    
    private ArchitecturalPatternMetrics calculateArchitecturalMetrics(JarContent jarContent, 
                                                             List<DetectedArchitecturalPattern> patterns,
                                                             List<DetectedAntiPattern> antiPatterns) {
        double patternCoverage = calculatePatternCoverage(jarContent, patterns);
        double antiPatternDensity = calculateAntiPatternDensity(jarContent, antiPatterns);
        int architecturalComplexity = calculateArchitecturalComplexity(jarContent);
        
        return ArchitecturalPatternMetrics.builder()
            .totalPatternsDetected(patterns.size())
            .patternCoverage(patternCoverage)
            .totalAntiPatternsDetected(antiPatterns.size())
            .overallPatternQuality((patternCoverage * 100) - (antiPatternDensity * 50))
            .build();
    }
    
    private double calculatePatternCoverage(JarContent jarContent, List<DetectedArchitecturalPattern> patterns) {
        if (patterns.isEmpty()) return 0.0;
        
        return Math.min(1.0, patterns.size() / 5.0); // Max 5 patterns for 100% coverage
    }
    
    private double calculateAntiPatternDensity(JarContent jarContent, List<DetectedAntiPattern> antiPatterns) {
        if (jarContent.getClasses().isEmpty()) return 0.0;
        
        return Math.min(1.0, (double)antiPatterns.size() / jarContent.getClasses().size());
    }
    
    private int calculateArchitecturalComplexity(JarContent jarContent) {
        return jarContent.getClasses().size() / 10; // Simplified complexity calculation
    }
    
    private List<ArchitecturalRecommendation> generateRecommendations(List<DetectedAntiPattern> antiPatterns,
                                                                    ArchitecturalPatternMetrics metrics) {
        List<ArchitecturalRecommendation> recommendations = new ArrayList<>();
        
        for (DetectedAntiPattern antiPattern : antiPatterns) {
            switch (antiPattern.getAntiPatternType()) {
                case GOD_PACKAGE:
                    recommendations.add(ArchitecturalRecommendation.builder()
                        .type(ArchitecturalRecommendation.RecommendationType.ANTI_PATTERN_REMOVAL)
                        .priority(ArchitecturalRecommendation.Priority.HIGH)
                        .title("Refactor God Class")
                        .description("Break down God class into smaller, focused classes")
                        .addAffectedComponent(!antiPattern.getAffectedClasses().isEmpty() ? 
                                         antiPattern.getAffectedClasses().iterator().next() : "Unknown")
                        .build());
                    break;
                    
                case MIXED_CONCERNS:
                    recommendations.add(ArchitecturalRecommendation.builder()
                        .type(ArchitecturalRecommendation.RecommendationType.SEPARATION_OF_CONCERNS)
                        .priority(ArchitecturalRecommendation.Priority.MEDIUM)
                        .title("Separate Concerns")
                        .description("Consider using parameter objects or builder pattern")
                        .addAffectedComponent(!antiPattern.getAffectedClasses().isEmpty() ? 
                                         antiPattern.getAffectedClasses().iterator().next() : "Unknown")
                        .build());
                    break;
                    
                case SHOTGUN_SURGERY:
                    recommendations.add(ArchitecturalRecommendation.builder()
                        .type(ArchitecturalRecommendation.RecommendationType.ARCHITECTURE_REFACTORING)
                        .priority(ArchitecturalRecommendation.Priority.MEDIUM)
                        .title("Consolidate Changes")
                        .description("Consider splitting large class based on responsibilities")
                        .addAffectedComponent(!antiPattern.getAffectedClasses().isEmpty() ? 
                                         antiPattern.getAffectedClasses().iterator().next() : "Unknown")
                        .build());
                    break;
            }
        }
        
        return recommendations;
    }
    
    private ArchitecturalQuality calculateArchitecturalQuality(ArchitecturalPatternMetrics metrics, 
                                                             List<DetectedAntiPattern> antiPatterns) {
        double score = metrics.getOverallPatternQuality();
        
        if (score >= 80) return ArchitecturalQuality.EXCELLENT;
        if (score >= 60) return ArchitecturalQuality.GOOD;
        if (score >= 40) return ArchitecturalQuality.FAIR;
        return ArchitecturalQuality.POOR;
    }
    
    private double calculatePatternCompliance(List<DetectedArchitecturalPattern> architecturalPatterns,
                                            List<DetectedDesignPattern> designPatterns) {
        int totalPatterns = architecturalPatterns.size() + designPatterns.size();
        if (totalPatterns == 0) return 0.0;
        
        double avgConfidence = (architecturalPatterns.stream().mapToDouble(DetectedArchitecturalPattern::getConfidenceScore).average().orElse(0.0) +
                               designPatterns.stream().mapToDouble(DetectedDesignPattern::getConfidenceScore).average().orElse(0.0)) / 2.0;
        
        return avgConfidence;
    }
}