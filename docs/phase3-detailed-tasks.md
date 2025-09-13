# Fase 3: Architecture & Dependency Analysis - Task Dettagliati

## T3.1.1: PackageAnalyzer per Hierarchy e Organization

### Descrizione Dettagliata
Implementazione di un analyzer intelligente che analizza la struttura dei package dell'applicazione Java, mappando la gerarchia, identificando convenzioni di naming, e valutando l'organizzazione architetturale complessiva del progetto.

### Scopo dell'Attività
- Mappare la struttura gerarchica completa dei package
- Identificare pattern architetturali (layered, modular, hexagonal)
- Rilevare violazioni delle convenzioni di naming
- Analizzare la coesione e accoppiamento tra package

### Impatti su Altri Moduli
- **ClassRelationshipAnalyzer**: Fornisce contesto per relazioni inter-package
- **DependencyGraphBuilder**: Base per costruzione dependency graph
- **UMLGenerator**: Input per diagrammi di package
- **Report Generator**: Dati strutturali per report architetturali

### Componenti da Implementare

#### 1. Package Hierarchy Analyzer
```java
public interface PackageAnalyzer {
    PackageAnalysisResult analyzePackageStructure(JarContent jarContent);
}

public class JavassistPackageAnalyzer implements PackageAnalyzer {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistPackageAnalyzer.class);
    
    private final PackageNamingConventionAnalyzer namingAnalyzer;
    private final PackageOrganizationAnalyzer organizationAnalyzer;
    private final PackageCohesionAnalyzer cohesionAnalyzer;
    
    public JavassistPackageAnalyzer() {
        this.namingAnalyzer = new PackageNamingConventionAnalyzer();
        this.organizationAnalyzer = new PackageOrganizationAnalyzer();
        this.cohesionAnalyzer = new PackageCohesionAnalyzer();
    }
    
    @Override
    public PackageAnalysisResult analyzePackageStructure(JarContent jarContent) {
        LOGGER.startOperation("Package structure analysis");
        long startTime = System.currentTimeMillis();
        
        try {
            // Estrai struttura dei package
            PackageHierarchy hierarchy = extractPackageHierarchy(jarContent);
            
            // Analizza convenzioni di naming
            NamingConventionResult namingResult = namingAnalyzer.analyze(hierarchy);
            
            // Analizza organizzazione architetturale
            OrganizationAnalysisResult orgResult = organizationAnalyzer.analyze(hierarchy, jarContent);
            
            // Calcola metriche di coesione
            CohesionMetrics cohesionMetrics = cohesionAnalyzer.calculateCohesion(hierarchy, jarContent);
            
            // Rileva anti-pattern architetturali
            List<ArchitecturalAntiPattern> antiPatterns = detectAntiPatterns(hierarchy, jarContent);
            
            return PackageAnalysisResult.builder()
                .hierarchy(hierarchy)
                .namingConventionResult(namingResult)
                .organizationResult(orgResult)
                .cohesionMetrics(cohesionMetrics)
                .antiPatterns(antiPatterns)
                .analysisMetadata(AnalysisMetadata.successful(System.currentTimeMillis() - startTime))
                .build();
                
        } catch (Exception e) {
            LOGGER.error("Package analysis failed", e);
            return PackageAnalysisResult.failed("Analysis failed: " + e.getMessage());
        } finally {
            LOGGER.endOperation("Package structure analysis", System.currentTimeMillis() - startTime);
        }
    }
    
    private PackageHierarchy extractPackageHierarchy(JarContent jarContent) {
        Map<String, PackageInfo> packages = new HashMap<>();
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            String packageName = extractPackageName(classInfo.getFullyQualifiedName());
            
            PackageInfo packageInfo = packages.computeIfAbsent(packageName, 
                name -> PackageInfo.builder().name(name).build());
            
            packageInfo.addClass(classInfo);
        }
        
        return buildHierarchyTree(packages);
    }
    
    private PackageHierarchy buildHierarchyTree(Map<String, PackageInfo> packages) {
        PackageHierarchy.Builder builder = PackageHierarchy.builder();
        
        // Costruisci albero gerarchico
        for (PackageInfo packageInfo : packages.values()) {
            String parentPackage = getParentPackage(packageInfo.getName());
            if (parentPackage != null && packages.containsKey(parentPackage)) {
                packages.get(parentPackage).addSubpackage(packageInfo);
            } else {
                builder.addRootPackage(packageInfo);
            }
        }
        
        return builder.build();
    }
    
    private List<ArchitecturalAntiPattern> detectAntiPatterns(PackageHierarchy hierarchy, 
                                                              JarContent jarContent) {
        List<ArchitecturalAntiPattern> antiPatterns = new ArrayList<>();
        
        // Rileva "God Package" - package con troppe classi
        antiPatterns.addAll(detectGodPackages(hierarchy));
        
        // Rileva "Circular Package Dependencies"
        antiPatterns.addAll(detectCircularPackageDependencies(hierarchy, jarContent));
        
        // Rileva "Feature Envy" tra package
        antiPatterns.addAll(detectFeatureEnvyBetweenPackages(hierarchy, jarContent));
        
        // Rileva "Unstable Dependencies"
        antiPatterns.addAll(detectUnstableDependencies(hierarchy, jarContent));
        
        return antiPatterns;
    }
}
```

#### 2. Package Organization Analyzer
```java
public class PackageOrganizationAnalyzer {
    private static final Map<String, ArchitecturalPattern> PATTERN_INDICATORS = Map.of(
        "controller", ArchitecturalPattern.MVC_LAYERED,
        "service", ArchitecturalPattern.SERVICE_LAYER,
        "repository", ArchitecturalPattern.REPOSITORY_PATTERN,
        "domain", ArchitecturalPattern.DOMAIN_DRIVEN_DESIGN,
        "adapter", ArchitecturalPattern.HEXAGONAL,
        "port", ArchitecturalPattern.HEXAGONAL
    );
    
    public OrganizationAnalysisResult analyze(PackageHierarchy hierarchy, JarContent jarContent) {
        OrganizationAnalysisResult.Builder builder = OrganizationAnalysisResult.builder();
        
        // Identifica pattern architetturali
        Set<ArchitecturalPattern> detectedPatterns = detectArchitecturalPatterns(hierarchy);
        builder.detectedPatterns(detectedPatterns);
        
        // Analizza struttura layering
        LayeringAnalysis layeringAnalysis = analyzeLayering(hierarchy, jarContent);
        builder.layeringAnalysis(layeringAnalysis);
        
        // Calcola metriche di modularità
        ModularityMetrics modularity = calculateModularityMetrics(hierarchy, jarContent);
        builder.modularityMetrics(modularity);
        
        // Valuta conformità alle best practice
        ConformityAssessment conformity = assessConformityToBestPractices(hierarchy);
        builder.conformityAssessment(conformity);
        
        return builder.build();
    }
    
    private Set<ArchitecturalPattern> detectArchitecturalPatterns(PackageHierarchy hierarchy) {
        Set<ArchitecturalPattern> patterns = new HashSet<>();
        
        for (PackageInfo packageInfo : hierarchy.getAllPackages()) {
            String packageName = packageInfo.getName().toLowerCase();
            
            for (Map.Entry<String, ArchitecturalPattern> entry : PATTERN_INDICATORS.entrySet()) {
                if (packageName.contains(entry.getKey())) {
                    patterns.add(entry.getValue());
                }
            }
        }
        
        // Analizza combinazioni di pattern
        if (patterns.contains(ArchitecturalPattern.MVC_LAYERED) && 
            patterns.contains(ArchitecturalPattern.SERVICE_LAYER) &&
            patterns.contains(ArchitecturalPattern.REPOSITORY_PATTERN)) {
            patterns.add(ArchitecturalPattern.LAYERED_ARCHITECTURE);
        }
        
        return patterns;
    }
    
    private LayeringAnalysis analyzeLayering(PackageHierarchy hierarchy, JarContent jarContent) {
        LayeringAnalysis.Builder builder = LayeringAnalysis.builder();
        
        // Identifica layer logici
        Map<LayerType, Set<PackageInfo>> layers = identifyLayers(hierarchy);
        builder.layers(layers);
        
        // Verifica violazioni di layering
        List<LayerViolation> violations = detectLayerViolations(layers, jarContent);
        builder.violations(violations);
        
        // Calcola metriche di layer coupling
        Map<LayerType, Double> coupling = calculateLayerCoupling(layers, jarContent);
        builder.layerCoupling(coupling);
        
        return builder.build();
    }
    
    private Map<LayerType, Set<PackageInfo>> identifyLayers(PackageHierarchy hierarchy) {
        Map<LayerType, Set<PackageInfo>> layers = new EnumMap<>(LayerType.class);
        
        for (PackageInfo packageInfo : hierarchy.getAllPackages()) {
            LayerType layer = classifyPackageLayer(packageInfo);
            layers.computeIfAbsent(layer, k -> new HashSet<>()).add(packageInfo);
        }
        
        return layers;
    }
    
    private LayerType classifyPackageLayer(PackageInfo packageInfo) {
        String name = packageInfo.getName().toLowerCase();
        
        if (name.contains("controller") || name.contains("web") || name.contains("rest")) {
            return LayerType.PRESENTATION;
        } else if (name.contains("service") || name.contains("business") || name.contains("logic")) {
            return LayerType.BUSINESS;
        } else if (name.contains("repository") || name.contains("dao") || name.contains("data")) {
            return LayerType.DATA_ACCESS;
        } else if (name.contains("domain") || name.contains("model") || name.contains("entity")) {
            return LayerType.DOMAIN;
        } else if (name.contains("config") || name.contains("configuration")) {
            return LayerType.CONFIGURATION;
        } else {
            return LayerType.UTILITY;
        }
    }
}
```

#### 3. Package Cohesion Analyzer
```java
public class PackageCohesionAnalyzer {
    
    public CohesionMetrics calculateCohesion(PackageHierarchy hierarchy, JarContent jarContent) {
        CohesionMetrics.Builder builder = CohesionMetrics.builder();
        
        for (PackageInfo packageInfo : hierarchy.getAllPackages()) {
            PackageCohesionScore score = calculatePackageCohesion(packageInfo, jarContent);
            builder.addPackageScore(packageInfo.getName(), score);
        }
        
        // Calcola metriche aggregate
        double averageCohesion = calculateAverageCohesion(builder.getPackageScores());
        builder.averageCohesion(averageCohesion);
        
        // Identifica package con bassa coesione
        List<String> lowCohesionPackages = identifyLowCohesionPackages(builder.getPackageScores());
        builder.lowCohesionPackages(lowCohesionPackages);
        
        return builder.build();
    }
    
    private PackageCohesionScore calculatePackageCohesion(PackageInfo packageInfo, JarContent jarContent) {
        Set<ClassInfo> classes = packageInfo.getClasses();
        
        // LCOM (Lack of Cohesion of Methods) a livello di package
        double lcom = calculateLCOMForPackage(classes);
        
        // Relational Cohesion - quanto sono correlate le classi nel package
        double relationalCohesion = calculateRelationalCohesion(classes, jarContent);
        
        // Functional Cohesion - quanto le classi contribuiscono alla stessa funzionalità
        double functionalCohesion = calculateFunctionalCohesion(classes);
        
        // Compositional Cohesion - usage patterns tra classi
        double compositionalCohesion = calculateCompositionalCohesion(classes, jarContent);
        
        return PackageCohesionScore.builder()
            .packageName(packageInfo.getName())
            .lcom(lcom)
            .relationalCohesion(relationalCohesion)
            .functionalCohesion(functionalCohesion)
            .compositionalCohesion(compositionalCohesion)
            .overallCohesion((relationalCohesion + functionalCohesion + compositionalCohesion) / 3.0)
            .build();
    }
    
    private double calculateLCOMForPackage(Set<ClassInfo> classes) {
        if (classes.size() < 2) return 1.0; // Perfect cohesion for single class
        
        int totalPairs = 0;
        int sharingPairs = 0;
        
        List<ClassInfo> classList = new ArrayList<>(classes);
        for (int i = 0; i < classList.size(); i++) {
            for (int j = i + 1; j < classList.size(); j++) {
                totalPairs++;
                if (shareCommonFields(classList.get(i), classList.get(j))) {
                    sharingPairs++;
                }
            }
        }
        
        return totalPairs > 0 ? (double) sharingPairs / totalPairs : 0.0;
    }
    
    private double calculateRelationalCohesion(Set<ClassInfo> classes, JarContent jarContent) {
        // Analizza quante classi hanno relazioni dirette tra loro
        int totalPossibleRelations = classes.size() * (classes.size() - 1);
        int actualRelations = 0;
        
        for (ClassInfo classInfo : classes) {
            actualRelations += countRelationsToOtherPackageClasses(classInfo, classes, jarContent);
        }
        
        return totalPossibleRelations > 0 ? (double) actualRelations / totalPossibleRelations : 0.0;
    }
    
    private double calculateFunctionalCohesion(Set<ClassInfo> classes) {
        // Analizza pattern di naming per identificare funzionalità comuni
        Map<String, Integer> functionalGroups = new HashMap<>();
        
        for (ClassInfo classInfo : classes) {
            String functionalGroup = extractFunctionalGroup(classInfo.getFullyQualifiedName());
            functionalGroups.merge(functionalGroup, 1, Integer::sum);
        }
        
        // Calcola entropia per misurare dispersione funzionale
        return calculateEntropyBasedCohesion(functionalGroups, classes.size());
    }
    
    private String extractFunctionalGroup(String className) {
        // Estrai radice funzionale dal nome della classe (es: "UserService" -> "User")
        String simpleName = className.substring(className.lastIndexOf('.') + 1);
        
        // Rimuovi suffissi comuni
        String[] suffixes = {"Service", "Repository", "Controller", "DTO", "Entity", "Impl"};
        for (String suffix : suffixes) {
            if (simpleName.endsWith(suffix)) {
                return simpleName.substring(0, simpleName.length() - suffix.length());
            }
        }
        
        return simpleName;
    }
}
```

#### 4. Result Model Classes
```java
public class PackageAnalysisResult {
    private final PackageHierarchy hierarchy;
    private final NamingConventionResult namingConventionResult;
    private final OrganizationAnalysisResult organizationResult;
    private final CohesionMetrics cohesionMetrics;
    private final List<ArchitecturalAntiPattern> antiPatterns;
    private final AnalysisMetadata analysisMetadata;
    
    // Builder pattern implementation
    public static class Builder {
        private PackageHierarchy hierarchy;
        private NamingConventionResult namingConventionResult;
        private OrganizationAnalysisResult organizationResult;
        private CohesionMetrics cohesionMetrics;
        private List<ArchitecturalAntiPattern> antiPatterns = new ArrayList<>();
        private AnalysisMetadata analysisMetadata;
        
        public Builder hierarchy(PackageHierarchy hierarchy) {
            this.hierarchy = hierarchy;
            return this;
        }
        
        public PackageAnalysisResult build() {
            return new PackageAnalysisResult(
                hierarchy,
                namingConventionResult,
                organizationResult,
                cohesionMetrics,
                antiPatterns,
                analysisMetadata
            );
        }
    }
    
    // Convenience methods for report generation
    public Map<String, Object> toReportData() {
        Map<String, Object> reportData = new HashMap<>();
        
        reportData.put("totalPackages", hierarchy.getAllPackages().size());
        reportData.put("averageClassesPerPackage", calculateAverageClassesPerPackage());
        reportData.put("maxDepth", hierarchy.getMaxDepth());
        reportData.put("detectedPatterns", organizationResult.getDetectedPatterns());
        reportData.put("averageCohesion", cohesionMetrics.getAverageCohesion());
        reportData.put("antiPatternsCount", antiPatterns.size());
        
        return reportData;
    }
    
    private double calculateAverageClassesPerPackage() {
        return hierarchy.getAllPackages().stream()
            .mapToInt(pkg -> pkg.getClasses().size())
            .average()
            .orElse(0.0);
    }
}

public class PackageHierarchy {
    private final List<PackageInfo> rootPackages;
    private final Map<String, PackageInfo> packageLookup;
    private final int maxDepth;
    
    public static class Builder {
        private List<PackageInfo> rootPackages = new ArrayList<>();
        private Map<String, PackageInfo> packageLookup = new HashMap<>();
        
        public Builder addRootPackage(PackageInfo packageInfo) {
            this.rootPackages.add(packageInfo);
            addToLookup(packageInfo);
            return this;
        }
        
        private void addToLookup(PackageInfo packageInfo) {
            packageLookup.put(packageInfo.getName(), packageInfo);
            for (PackageInfo subpackage : packageInfo.getSubpackages()) {
                addToLookup(subpackage);
            }
        }
        
        public PackageHierarchy build() {
            int maxDepth = calculateMaxDepth(rootPackages);
            return new PackageHierarchy(rootPackages, packageLookup, maxDepth);
        }
        
        private int calculateMaxDepth(List<PackageInfo> packages) {
            return packages.stream()
                .mapToInt(this::calculatePackageDepth)
                .max()
                .orElse(0);
        }
        
        private int calculatePackageDepth(PackageInfo packageInfo) {
            int maxSubDepth = packageInfo.getSubpackages().stream()
                .mapToInt(this::calculatePackageDepth)
                .max()
                .orElse(0);
            return 1 + maxSubDepth;
        }
    }
}
```

### Principi SOLID Applicati
- **SRP**: Analyzer separati per naming, organization, cohesion
- **OCP**: Facilmente estendibile per nuovi pattern architetturali e metriche
- **LSP**: Implementazioni sostituibili via interfacce
- **ISP**: Interfacce specifiche per diversi aspetti dell'analisi
- **DIP**: Dipende da abstractions per hierarchy building

### Test Unitari da Implementare
```java
// PackageAnalyzerTest.java
@Test
void shouldExtractPackageHierarchy() {
    // Arrange
    JarContent jarContent = createJarWithPackageStructure(
        "com.example.controller.UserController",
        "com.example.service.UserService",
        "com.example.repository.UserRepository",
        "com.example.model.User"
    );
    PackageAnalyzer analyzer = new JavassistPackageAnalyzer();
    
    // Act
    PackageAnalysisResult result = analyzer.analyzePackageStructure(jarContent);
    
    // Assert
    PackageHierarchy hierarchy = result.getHierarchy();
    assertThat(hierarchy.getAllPackages()).hasSize(4);
    assertThat(hierarchy.getMaxDepth()).isEqualTo(3); // com.example.controller level
    
    PackageInfo rootPackage = hierarchy.findPackage("com.example");
    assertThat(rootPackage.getSubpackages()).hasSize(4);
}

@Test
void shouldDetectLayeredArchitecturePattern() {
    JarContent jarContent = createLayeredArchitectureJar();
    PackageAnalyzer analyzer = new JavassistPackageAnalyzer();
    
    PackageAnalysisResult result = analyzer.analyzePackageStructure(jarContent);
    
    OrganizationAnalysisResult orgResult = result.getOrganizationResult();
    assertThat(orgResult.getDetectedPatterns()).contains(ArchitecturalPattern.LAYERED_ARCHITECTURE);
    
    LayeringAnalysis layering = orgResult.getLayeringAnalysis();
    assertThat(layering.getLayers()).containsKeys(
        LayerType.PRESENTATION,
        LayerType.BUSINESS,
        LayerType.DATA_ACCESS
    );
}

@Test
void shouldCalculatePackageCohesion() {
    JarContent jarContent = createJarWithRelatedClasses();
    PackageAnalyzer analyzer = new JavassistPackageAnalyzer();
    
    PackageAnalysisResult result = analyzer.analyzePackageStructure(jarContent);
    
    CohesionMetrics cohesion = result.getCohesionMetrics();
    assertThat(cohesion.getAverageCohesion()).isBetween(0.0, 1.0);
    
    PackageCohesionScore userPackageScore = cohesion.getPackageScore("com.example.user");
    assertThat(userPackageScore.getOverallCohesion()).isGreaterThan(0.7); // High cohesion expected
}

@Test
void shouldDetectGodPackageAntiPattern() {
    JarContent jarContent = createJarWithGodPackage(50); // Package with 50 classes
    PackageAnalyzer analyzer = new JavassistPackageAnalyzer();
    
    PackageAnalysisResult result = analyzer.analyzePackageStructure(jarContent);
    
    List<ArchitecturalAntiPattern> antiPatterns = result.getAntiPatterns();
    assertThat(antiPatterns).anyMatch(
        pattern -> pattern.getType() == AntiPatternType.GOD_PACKAGE
    );
}

@Test
void shouldDetectCircularPackageDependencies() {
    JarContent jarContent = createJarWithCircularPackageDependencies();
    PackageAnalyzer analyzer = new JavassistPackageAnalyzer();
    
    PackageAnalysisResult result = analyzer.analyzePackageStructure(jarContent);
    
    List<ArchitecturalAntiPattern> antiPatterns = result.getAntiPatterns();
    assertThat(antiPatterns).anyMatch(
        pattern -> pattern.getType() == AntiPatternType.CIRCULAR_PACKAGE_DEPENDENCY
    );
    
    ArchitecturalAntiPattern circularDep = antiPatterns.stream()
        .filter(pattern -> pattern.getType() == AntiPatternType.CIRCULAR_PACKAGE_DEPENDENCY)
        .findFirst()
        .orElseThrow();
    
    assertThat(circularDep.getInvolvedElements()).hasSize(2);
}

// PackageNamingConventionAnalyzerTest.java
@Test
void shouldDetectNamingViolations() {
    PackageHierarchy hierarchy = createHierarchyWithNamingViolations();
    PackageNamingConventionAnalyzer analyzer = new PackageNamingConventionAnalyzer();
    
    NamingConventionResult result = analyzer.analyze(hierarchy);
    
    assertThat(result.getViolations()).isNotEmpty();
    assertThat(result.getViolations()).anyMatch(
        violation -> violation.getType() == NamingViolationType.INCONSISTENT_CASE
    );
}

// PackageCohesionAnalyzerTest.java  
@Test
void shouldCalculateLCOMForPackage() {
    Set<ClassInfo> relatedClasses = createRelatedClassSet();
    PackageCohesionAnalyzer analyzer = new PackageCohesionAnalyzer();
    
    PackageCohesionScore score = analyzer.calculatePackageCohesion(
        PackageInfo.builder().name("com.example.user").classes(relatedClasses).build(),
        createMockJarContent()
    );
    
    assertThat(score.getLcom()).isBetween(0.0, 1.0);
    assertThat(score.getOverallCohesion()).isBetween(0.0, 1.0);
}

// Integration Test
@Test
void shouldAnalyzeRealSpringBootPackageStructure() throws IOException {
    Path springBootJar = getTestResourcePath("sample-spring-boot.jar");
    JarContent jarContent = new JarLoader().loadJar(new JarLocation(springBootJar));
    PackageAnalyzer analyzer = new JavassistPackageAnalyzer();
    
    PackageAnalysisResult result = analyzer.analyzePackageStructure(jarContent);
    
    // Verifiche su applicazione reale
    assertThat(result.getHierarchy().getAllPackages()).hasSizeGreaterThan(10);
    assertThat(result.getOrganizationResult().getDetectedPatterns()).isNotEmpty();
    assertThat(result.getCohesionMetrics().getAverageCohesion()).isGreaterThan(0.0);
}
```

---

## T3.1.2: ClassRelationshipAnalyzer per Inheritance e Composition

### Descrizione Dettagliata
Analyzer sofisticato per mappare tutte le relazioni tra classi nell'applicazione, incluse inheritance hierarchies, composition patterns, aggregation, delegation, e usage dependencies. Fornisce una vista completa del design object-oriented.

### Scopo dell'Attività
- Mappare complete inheritance hierarchies (extends, implements)
- Identificare composition e aggregation patterns
- Rilevare delegation patterns e proxy usage
- Analizzare usage dependencies tra classi
- Calcolare metriche di accoppiamento object-oriented

### Impatti su Altri Moduli
- **UMLGenerator**: Input fondamentale per class diagrams
- **DependencyGraphBuilder**: Base per dependency relationships
- **CodeQualityAnalyzer**: Metriche di coupling e cohesion
- **ArchitectureAnalyzer**: Comprensione design patterns

### Componenti da Implementare

#### 1. Class Relationship Analyzer Core
```java
public interface ClassRelationshipAnalyzer {
    ClassRelationshipResult analyzeRelationships(JarContent jarContent);
}

public class JavassistClassRelationshipAnalyzer implements ClassRelationshipAnalyzer {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistClassRelationshipAnalyzer.class);
    
    private final InheritanceAnalyzer inheritanceAnalyzer;
    private final CompositionAnalyzer compositionAnalyzer;
    private final UsageDependencyAnalyzer usageAnalyzer;
    private final RelationshipMetricsCalculator metricsCalculator;
    
    public JavassistClassRelationshipAnalyzer() {
        this.inheritanceAnalyzer = new InheritanceAnalyzer();
        this.compositionAnalyzer = new CompositionAnalyzer();
        this.usageAnalyzer = new UsageDependencyAnalyzer();
        this.metricsCalculator = new RelationshipMetricsCalculator();
    }
    
    @Override
    public ClassRelationshipResult analyzeRelationships(JarContent jarContent) {
        LOGGER.startOperation("Class relationship analysis");
        long startTime = System.currentTimeMillis();
        
        try {
            ClassRelationshipResult.Builder resultBuilder = ClassRelationshipResult.builder();
            
            // Analizza inheritance relationships
            InheritanceHierarchy inheritanceHierarchy = inheritanceAnalyzer.buildHierarchy(jarContent);
            resultBuilder.inheritanceHierarchy(inheritanceHierarchy);
            
            // Analizza composition e aggregation
            CompositionGraph compositionGraph = compositionAnalyzer.analyzeComposition(jarContent);
            resultBuilder.compositionGraph(compositionGraph);
            
            // Analizza usage dependencies
            UsageDependencyGraph usageGraph = usageAnalyzer.analyzeUsageDependencies(jarContent);
            resultBuilder.usageDependencies(usageGraph);
            
            // Calcola metriche di relationship
            RelationshipMetrics metrics = metricsCalculator.calculateMetrics(
                inheritanceHierarchy, compositionGraph, usageGraph);
            resultBuilder.metrics(metrics);
            
            // Rileva design pattern nelle relazioni
            List<DetectedDesignPattern> patterns = detectDesignPatterns(
                inheritanceHierarchy, compositionGraph, usageGraph, jarContent);
            resultBuilder.designPatterns(patterns);
            
            // Rileva problemi nelle relazioni
            List<RelationshipIssue> issues = detectRelationshipIssues(
                inheritanceHierarchy, compositionGraph, usageGraph);
            resultBuilder.issues(issues);
            
            return resultBuilder
                .analysisMetadata(AnalysisMetadata.successful(System.currentTimeMillis() - startTime))
                .build();
                
        } catch (Exception e) {
            LOGGER.error("Class relationship analysis failed", e);
            return ClassRelationshipResult.failed("Analysis failed: " + e.getMessage());
        } finally {
            LOGGER.endOperation("Class relationship analysis", System.currentTimeMillis() - startTime);
        }
    }
    
    private List<DetectedDesignPattern> detectDesignPatterns(
            InheritanceHierarchy inheritance,
            CompositionGraph composition, 
            UsageDependencyGraph usage,
            JarContent jarContent) {
        
        List<DetectedDesignPattern> patterns = new ArrayList<>();
        
        // Strategy Pattern Detection
        patterns.addAll(detectStrategyPattern(inheritance, usage));
        
        // Observer Pattern Detection  
        patterns.addAll(detectObserverPattern(inheritance, composition));
        
        // Decorator Pattern Detection
        patterns.addAll(detectDecoratorPattern(inheritance, composition));
        
        // Factory Pattern Detection
        patterns.addAll(detectFactoryPattern(usage, jarContent));
        
        // Builder Pattern Detection
        patterns.addAll(detectBuilderPattern(composition, jarContent));
        
        // Proxy Pattern Detection
        patterns.addAll(detectProxyPattern(inheritance, composition));
        
        return patterns;
    }
}
```

#### 2. Inheritance Analyzer
```java
public class InheritanceAnalyzer {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(InheritanceAnalyzer.class);
    
    public InheritanceHierarchy buildHierarchy(JarContent jarContent) {
        LOGGER.info("Building inheritance hierarchy for {} classes", jarContent.getClasses().size());
        
        InheritanceHierarchy.Builder builder = InheritanceHierarchy.builder();
        
        // Prima passata: identifica tutte le relazioni dirette
        Map<String, ClassInheritanceInfo> classInfoMap = new HashMap<>();
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            ClassInheritanceInfo inheritanceInfo = extractInheritanceInfo(classInfo);
            classInfoMap.put(classInfo.getFullyQualifiedName(), inheritanceInfo);
        }
        
        // Seconda passata: costruisci albero gerarchico
        for (ClassInheritanceInfo classInfo : classInfoMap.values()) {
            buildClassHierarchy(classInfo, classInfoMap, builder);
        }
        
        // Calcola metriche di inheritance
        InheritanceMetrics metrics = calculateInheritanceMetrics(classInfoMap.values());
        builder.metrics(metrics);
        
        return builder.build();
    }
    
    private ClassInheritanceInfo extractInheritanceInfo(ClassInfo classInfo) {
        ClassInheritanceInfo.Builder builder = ClassInheritanceInfo.builder()
            .className(classInfo.getFullyQualifiedName())
            .isInterface(classInfo.isInterface())
            .isAbstract(classInfo.isAbstract())
            .accessModifier(classInfo.getAccessModifier());
        
        // Superclass
        if (classInfo.getSuperclass() != null) {
            builder.superclass(classInfo.getSuperclass().getFullyQualifiedName());
        }
        
        // Implemented interfaces
        for (ClassInfo interfaceInfo : classInfo.getImplementedInterfaces()) {
            builder.addImplementedInterface(interfaceInfo.getFullyQualifiedName());
        }
        
        // Analizza overridden methods
        Set<String> overriddenMethods = findOverriddenMethods(classInfo);
        builder.overriddenMethods(overriddenMethods);
        
        return builder.build();
    }
    
    private void buildClassHierarchy(ClassInheritanceInfo classInfo, 
                                   Map<String, ClassInheritanceInfo> allClasses,
                                   InheritanceHierarchy.Builder hierarchyBuilder) {
        
        // Costruisci inheritance tree
        if (classInfo.getSuperclass() != null) {
            hierarchyBuilder.addInheritanceRelation(
                classInfo.getSuperclass(),
                classInfo.getClassName(),
                RelationType.EXTENDS
            );
        }
        
        // Aggiungi interface implementations
        for (String interfaceName : classInfo.getImplementedInterfaces()) {
            hierarchyBuilder.addInheritanceRelation(
                interfaceName,
                classInfo.getClassName(),
                RelationType.IMPLEMENTS
            );
        }
    }
    
    private Set<String> findOverriddenMethods(ClassInfo classInfo) {
        Set<String> overriddenMethods = new HashSet<>();
        
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.get(classInfo.getFullyQualifiedName());
            
            for (CtMethod method : ctClass.getDeclaredMethods()) {
                if (isMethodOverridden(method, ctClass)) {
                    overriddenMethods.add(method.getName() + method.getSignature());
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("Error analyzing overridden methods for {}", classInfo.getFullyQualifiedName(), e);
        }
        
        return overriddenMethods;
    }
    
    private boolean isMethodOverridden(CtMethod method, CtClass ctClass) throws NotFoundException {
        // Controlla se il metodo è presente nella superclass
        CtClass superClass = ctClass.getSuperclass();
        if (superClass == null) return false;
        
        try {
            CtMethod superMethod = superClass.getMethod(method.getName(), method.getSignature());
            return superMethod != null && !Modifier.isFinal(superMethod.getModifiers());
        } catch (NotFoundException e) {
            // Metodo non trovato nella superclass, non è override
            return false;
        }
    }
    
    private InheritanceMetrics calculateInheritanceMetrics(Collection<ClassInheritanceInfo> classes) {
        InheritanceMetrics.Builder builder = InheritanceMetrics.builder();
        
        // Depth of Inheritance Tree (DIT)
        Map<String, Integer> ditMetrics = calculateDIT(classes);
        builder.depthOfInheritanceTree(ditMetrics);
        
        // Number of Children (NOC)
        Map<String, Integer> nocMetrics = calculateNOC(classes);
        builder.numberOfChildren(nocMetrics);
        
        // Response for Class (RFC) - considerando inheritance
        Map<String, Integer> rfcMetrics = calculateRFC(classes);
        builder.responseForClass(rfcMetrics);
        
        return builder.build();
    }
}
```

#### 3. Composition Analyzer
```java
public class CompositionAnalyzer {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(CompositionAnalyzer.class);
    
    public CompositionGraph analyzeComposition(JarContent jarContent) {
        LOGGER.info("Analyzing composition relationships");
        
        CompositionGraph.Builder graphBuilder = CompositionGraph.builder();
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            List<CompositionRelationship> relationships = extractCompositionRelationships(classInfo);
            
            for (CompositionRelationship relationship : relationships) {
                graphBuilder.addRelationship(relationship);
            }
        }
        
        // Analizza pattern di composition
        List<CompositionPattern> patterns = detectCompositionPatterns(graphBuilder.getRelationships());
        graphBuilder.detectedPatterns(patterns);
        
        return graphBuilder.build();
    }
    
    private List<CompositionRelationship> extractCompositionRelationships(ClassInfo classInfo) {
        List<CompositionRelationship> relationships = new ArrayList<>();
        
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.get(classInfo.getFullyQualifiedName());
            
            // Analizza field per composition/aggregation
            for (CtField field : ctClass.getDeclaredFields()) {
                CompositionType type = determineCompositionType(field, ctClass);
                if (type != CompositionType.NONE) {
                    CompositionRelationship relationship = CompositionRelationship.builder()
                        .source(classInfo.getFullyQualifiedName())
                        .target(field.getType().getName())
                        .fieldName(field.getName())
                        .compositionType(type)
                        .multiplicity(determineMultiplicity(field))
                        .accessModifier(AccessModifier.fromModifier(field.getModifiers()))
                        .isFinal(Modifier.isFinal(field.getModifiers()))
                        .build();
                    
                    relationships.add(relationship);
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("Error analyzing composition for {}", classInfo.getFullyQualifiedName(), e);
        }
        
        return relationships;
    }
    
    private CompositionType determineCompositionType(CtField field, CtClass ownerClass) 
            throws NotFoundException {
        
        String fieldTypeName = field.getType().getName();
        
        // Skip primitive types and common utility classes
        if (isPrimitiveOrUtility(fieldTypeName)) {
            return CompositionType.NONE;
        }
        
        // Analizza se è composition o aggregation basato su lifecycle management
        if (isComposition(field, ownerClass)) {
            return CompositionType.COMPOSITION;
        } else if (isAggregation(field, ownerClass)) {
            return CompositionType.AGGREGATION;
        } else {
            return CompositionType.ASSOCIATION;
        }
    }
    
    private boolean isComposition(CtField field, CtClass ownerClass) throws NotFoundException {
        // Cerca nel constructor per vedere se il field è inizializzato direttamente
        for (CtConstructor constructor : ownerClass.getConstructors()) {
            if (constructorInitializesField(constructor, field)) {
                return true;
            }
        }
        
        // Controlla se c'è dependency injection - potrebbe essere aggregation
        return !hasInjectionAnnotation(field);
    }
    
    private boolean isAggregation(CtField field, CtClass ownerClass) throws NotFoundException {
        // Aggregation tipicamente via dependency injection o setter
        return hasInjectionAnnotation(field) || hasSetterForField(field, ownerClass);
    }
    
    private boolean hasInjectionAnnotation(CtField field) {
        try {
            return field.hasAnnotation("org.springframework.beans.factory.annotation.Autowired") ||
                   field.hasAnnotation("javax.inject.Inject") ||
                   field.hasAnnotation("org.springframework.beans.factory.annotation.Value");
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    private Multiplicity determineMultiplicity(CtField field) throws NotFoundException {
        String typeName = field.getType().getName();
        
        if (typeName.startsWith("[") || typeName.endsWith("[]")) {
            return Multiplicity.ONE_TO_MANY; // Array
        } else if (isCollectionType(typeName)) {
            return Multiplicity.ONE_TO_MANY; // Collection
        } else {
            return Multiplicity.ONE_TO_ONE; // Single object
        }
    }
    
    private boolean isCollectionType(String typeName) {
        return typeName.equals("java.util.List") ||
               typeName.equals("java.util.Set") ||
               typeName.equals("java.util.Collection") ||
               typeName.equals("java.util.Map") ||
               typeName.startsWith("java.util.List<") ||
               typeName.startsWith("java.util.Set<") ||
               typeName.startsWith("java.util.Collection<") ||
               typeName.startsWith("java.util.Map<");
    }
    
    private List<CompositionPattern> detectCompositionPatterns(List<CompositionRelationship> relationships) {
        List<CompositionPattern> patterns = new ArrayList<>();
        
        // Composite Pattern Detection
        patterns.addAll(detectCompositePattern(relationships));
        
        // Aggregate Root Pattern (DDD)
        patterns.addAll(detectAggregateRootPattern(relationships));
        
        // Delegation Pattern
        patterns.addAll(detectDelegationPattern(relationships));
        
        return patterns;
    }
    
    private List<CompositionPattern> detectCompositePattern(List<CompositionRelationship> relationships) {
        // Cerca strutture ricorsive dove una classe compone se stessa (attraverso collection)
        return relationships.stream()
            .filter(rel -> rel.getSource().equals(rel.getTarget()) && 
                          rel.getMultiplicity() == Multiplicity.ONE_TO_MANY)
            .map(rel -> CompositionPattern.composite(rel.getSource()))
            .collect(Collectors.toList());
    }
}
```

#### 4. Usage Dependency Analyzer
```java
public class UsageDependencyAnalyzer {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(UsageDependencyAnalyzer.class);
    
    public UsageDependencyGraph analyzeUsageDependencies(JarContent jarContent) {
        LOGGER.info("Analyzing usage dependencies");
        
        UsageDependencyGraph.Builder graphBuilder = UsageDependencyGraph.builder();
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            List<UsageDependency> dependencies = extractUsageDependencies(classInfo, jarContent);
            
            for (UsageDependency dependency : dependencies) {
                graphBuilder.addDependency(dependency);
            }
        }
        
        // Calcola metriche di coupling
        CouplingMetrics couplingMetrics = calculateCouplingMetrics(graphBuilder.getDependencies());
        graphBuilder.couplingMetrics(couplingMetrics);
        
        // Rileva circular dependencies
        List<CircularDependency> circularDependencies = detectCircularDependencies(graphBuilder.getDependencies());
        graphBuilder.circularDependencies(circularDependencies);
        
        return graphBuilder.build();
    }
    
    private List<UsageDependency> extractUsageDependencies(ClassInfo classInfo, JarContent jarContent) {
        List<UsageDependency> dependencies = new ArrayList<>();
        
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.get(classInfo.getFullyQualifiedName());
            
            // Analizza method bodies per usage
            for (CtMethod method : ctClass.getDeclaredMethods()) {
                dependencies.addAll(extractMethodDependencies(method, classInfo.getFullyQualifiedName()));
            }
            
        } catch (Exception e) {
            LOGGER.error("Error analyzing usage dependencies for {}", classInfo.getFullyQualifiedName(), e);
        }
        
        return dependencies;
    }
    
    private List<UsageDependency> extractMethodDependencies(CtMethod method, String sourceClass) {
        List<UsageDependency> dependencies = new ArrayList<>();
        
        try {
            CodeAttribute codeAttr = method.getMethodInfo().getCodeAttribute();
            if (codeAttr == null) return dependencies; // Abstract method
            
            CodeIterator iterator = codeAttr.iterator();
            
            while (iterator.hasNext()) {
                int index = iterator.next();
                int opcode = iterator.byteAt(index);
                
                switch (opcode) {
                    case Opcode.INVOKEVIRTUAL, Opcode.INVOKESPECIAL, 
                         Opcode.INVOKESTATIC, Opcode.INVOKEINTERFACE -> {
                        String targetClass = getInvokedMethodClass(method.getDeclaringClass(), iterator, index);
                        if (targetClass != null && !targetClass.equals(sourceClass)) {
                            UsageDependency dependency = UsageDependency.builder()
                                .source(sourceClass)
                                .target(targetClass)
                                .dependencyType(UsageDependencyType.METHOD_CALL)
                                .context(method.getName())
                                .build();
                            dependencies.add(dependency);
                        }
                    }
                    
                    case Opcode.NEW -> {
                        String instantiatedClass = getInstantiatedClass(method.getDeclaringClass(), iterator, index);
                        if (instantiatedClass != null && !instantiatedClass.equals(sourceClass)) {
                            UsageDependency dependency = UsageDependency.builder()
                                .source(sourceClass)
                                .target(instantiatedClass)
                                .dependencyType(UsageDependencyType.INSTANTIATION)
                                .context(method.getName())
                                .build();
                            dependencies.add(dependency);
                        }
                    }
                    
                    case Opcode.CHECKCAST, Opcode.INSTANCEOF -> {
                        String castClass = getCastClass(method.getDeclaringClass(), iterator, index);
                        if (castClass != null && !castClass.equals(sourceClass)) {
                            UsageDependency dependency = UsageDependency.builder()
                                .source(sourceClass)
                                .target(castClass)
                                .dependencyType(UsageDependencyType.TYPE_CHECK)
                                .context(method.getName())
                                .build();
                            dependencies.add(dependency);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("Error analyzing method dependencies for {}", method.getLongName(), e);
        }
        
        return dependencies;
    }
    
    private CouplingMetrics calculateCouplingMetrics(List<UsageDependency> dependencies) {
        CouplingMetrics.Builder builder = CouplingMetrics.builder();
        
        // Afferent Coupling (Ca) - quante classi dipendono da questa classe
        Map<String, Integer> afferentCoupling = new HashMap<>();
        
        // Efferent Coupling (Ce) - da quante classi dipende questa classe
        Map<String, Integer> efferentCoupling = new HashMap<>();
        
        for (UsageDependency dependency : dependencies) {
            afferentCoupling.merge(dependency.getTarget(), 1, Integer::sum);
            efferentCoupling.merge(dependency.getSource(), 1, Integer::sum);
        }
        
        builder.afferentCoupling(afferentCoupling);
        builder.efferentCoupling(efferentCoupling);
        
        // Instability (I = Ce / (Ca + Ce))
        Map<String, Double> instability = new HashMap<>();
        Set<String> allClasses = new HashSet<>();
        allClasses.addAll(afferentCoupling.keySet());
        allClasses.addAll(efferentCoupling.keySet());
        
        for (String className : allClasses) {
            int ca = afferentCoupling.getOrDefault(className, 0);
            int ce = efferentCoupling.getOrDefault(className, 0);
            double instabilityValue = (ca + ce) > 0 ? (double) ce / (ca + ce) : 0.0;
            instability.put(className, instabilityValue);
        }
        builder.instability(instability);
        
        return builder.build();
    }
}
```

### Principi SOLID Applicati
- **SRP**: Analyzer separati per inheritance, composition, usage
- **OCP**: Estendibile per nuovi tipi di relazioni e pattern
- **LSP**: Implementazioni intercambiabili via interfacce
- **ISP**: Interfacce specifiche per diversi tipi di analisi
- **DIP**: Dipende da abstractions per bytecode analysis

### Test Unitari da Implementare
```java
// ClassRelationshipAnalyzerTest.java
@Test
void shouldAnalyzeInheritanceHierarchy() {
    // Arrange
    JarContent jarContent = createJarWithInheritanceHierarchy();
    ClassRelationshipAnalyzer analyzer = new JavassistClassRelationshipAnalyzer();
    
    // Act
    ClassRelationshipResult result = analyzer.analyzeRelationships(jarContent);
    
    // Assert
    InheritanceHierarchy hierarchy = result.getInheritanceHierarchy();
    assertThat(hierarchy.getRootClasses()).isNotEmpty();
    
    ClassInheritanceInfo animalClass = hierarchy.findClass("com.example.Animal");
    assertThat(animalClass.getSubclasses()).contains("com.example.Dog", "com.example.Cat");
}

@Test
void shouldDetectCompositionRelationships() {
    JarContent jarContent = createJarWithComposition();
    ClassRelationshipAnalyzer analyzer = new JavassistClassRelationshipAnalyzer();
    
    ClassRelationshipResult result = analyzer.analyzeRelationships(jarContent);
    
    CompositionGraph compositionGraph = result.getCompositionGraph();
    List<CompositionRelationship> relationships = compositionGraph.getRelationshipsFor("com.example.Car");
    
    assertThat(relationships).hasSize(2); // Engine and Wheels
    assertThat(relationships).anyMatch(
        rel -> rel.getTarget().equals("com.example.Engine") && 
               rel.getCompositionType() == CompositionType.COMPOSITION
    );
}

@Test
void shouldCalculateCouplingMetrics() {
    JarContent jarContent = createJarWithDependencies();
    ClassRelationshipAnalyzer analyzer = new JavassistClassRelationshipAnalyzer();
    
    ClassRelationshipResult result = analyzer.analyzeRelationships(jarContent);
    
    RelationshipMetrics metrics = result.getMetrics();
    assertThat(metrics.getCouplingMetrics()).isNotNull();
    
    CouplingMetrics coupling = metrics.getCouplingMetrics();
    assertThat(coupling.getAfferentCoupling()).isNotEmpty();
    assertThat(coupling.getEfferentCoupling()).isNotEmpty();
    assertThat(coupling.getInstability()).isNotEmpty();
}

@Test
void shouldDetectStrategyPattern() {
    JarContent jarContent = createJarWithStrategyPattern();
    ClassRelationshipAnalyzer analyzer = new JavassistClassRelationshipAnalyzer();
    
    ClassRelationshipResult result = analyzer.analyzeRelationships(jarContent);
    
    List<DetectedDesignPattern> patterns = result.getDesignPatterns();
    assertThat(patterns).anyMatch(
        pattern -> pattern.getPatternType() == DesignPatternType.STRATEGY
    );
    
    DetectedDesignPattern strategyPattern = patterns.stream()
        .filter(pattern -> pattern.getPatternType() == DesignPatternType.STRATEGY)
        .findFirst()
        .orElseThrow();
    
    assertThat(strategyPattern.getParticipants()).hasSize(3); // Strategy, ConcreteStrategies, Context
}

@Test
void shouldDetectCircularDependencies() {
    JarContent jarContent = createJarWithCircularDependencies();
    ClassRelationshipAnalyzer analyzer = new JavassistClassRelationshipAnalyzer();
    
    ClassRelationshipResult result = analyzer.analyzeRelationships(jarContent);
    
    UsageDependencyGraph usageGraph = result.getUsageDependencies();
    assertThat(usageGraph.getCircularDependencies()).isNotEmpty();
    
    CircularDependency circular = usageGraph.getCircularDependencies().get(0);
    assertThat(circular.getCycle()).hasSize(2);
    assertThat(circular.getCycle()).containsExactlyInAnyOrder("com.example.A", "com.example.B");
}

// InheritanceAnalyzerTest.java
@Test
void shouldCalculateDepthOfInheritanceTree() {
    JarContent jarContent = createDeepInheritanceHierarchy(); // Object -> A -> B -> C -> D
    InheritanceAnalyzer analyzer = new InheritanceAnalyzer();
    
    InheritanceHierarchy hierarchy = analyzer.buildHierarchy(jarContent);
    
    InheritanceMetrics metrics = hierarchy.getMetrics();
    assertThat(metrics.getDepthOfInheritanceTree()).containsEntry("com.example.D", 4);
}

@Test
void shouldIdentifyOverriddenMethods() {
    JarContent jarContent = createJarWithMethodOverrides();
    InheritanceAnalyzer analyzer = new InheritanceAnalyzer();
    
    InheritanceHierarchy hierarchy = analyzer.buildHierarchy(jarContent);
    
    ClassInheritanceInfo childClass = hierarchy.findClass("com.example.Child");
    assertThat(childClass.getOverriddenMethods()).contains("toString()Ljava/lang/String;");
}

// CompositionAnalyzerTest.java
@Test
void shouldDetectCompositePattern() {
    JarContent jarContent = createJarWithCompositePattern();
    CompositionAnalyzer analyzer = new CompositionAnalyzer();
    
    CompositionGraph graph = analyzer.analyzeComposition(jarContent);
    
    List<CompositionPattern> patterns = graph.getDetectedPatterns();
    assertThat(patterns).anyMatch(
        pattern -> pattern.getPatternType() == CompositionPatternType.COMPOSITE
    );
}

// Integration Test
@Test
void shouldAnalyzeComplexRealWorldRelationships() throws IOException {
    Path springBootJar = getTestResourcePath("complex-spring-boot.jar");
    JarContent jarContent = new JarLoader().loadJar(new JarLocation(springBootJar));
    ClassRelationshipAnalyzer analyzer = new JavassistClassRelationshipAnalyzer();
    
    ClassRelationshipResult result = analyzer.analyzeRelationships(jarContent);
    
    // Verifiche su applicazione complessa
    assertThat(result.getInheritanceHierarchy().getAllClasses()).hasSizeGreaterThan(50);
    assertThat(result.getCompositionGraph().getAllRelationships()).hasSizeGreaterThan(100);
    assertThat(result.getDesignPatterns()).isNotEmpty();
    assertThat(result.getMetrics()).isNotNull();
}
```

---

## T3.1.3: UMLGenerator per Class Diagrams (PlantUML syntax)

### Descrizione Dettagliata
Generator avanzato che produce diagrammi UML delle classi in formato PlantUML, utilizzando i dati delle analisi precedenti per creare rappresentazioni visuali accurate delle relazioni, inheritance hierarchies, e design patterns identificati.

### Scopo dell'Attività
- Generare class diagrams completi in sintassi PlantUML
- Visualizzare inheritance hierarchies e relationships
- Rappresentare design patterns identificati
- Fornire diverse viste (package, subsystem, pattern-focused)
- Supportare customizzazione di styling e layout

### Impatti su Altri Moduli
- **HTMLReportGenerator**: Integra diagrammi nei report finali
- **PackageAnalyzer**: Utilizza struttura package per organizzazione
- **ClassRelationshipAnalyzer**: Input fondamentale per relationships
- **Report System**: Componente chiave per documentazione visuale

### Componenti da Implementare

#### 1. UML Generator Core
```java
public interface UMLGenerator {
    UMLGenerationResult generateClassDiagram(UMLGenerationRequest request);
    UMLGenerationResult generatePackageDiagram(PackageAnalysisResult packageAnalysis);
    UMLGenerationResult generatePatternDiagram(List<DetectedDesignPattern> patterns, 
                                             ClassRelationshipResult relationships);
}

public class PlantUMLGenerator implements UMLGenerator {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(PlantUMLGenerator.class);
    
    private final PlantUMLClassRenderer classRenderer;
    private final PlantUMLRelationshipRenderer relationshipRenderer;
    private final PlantUMLPackageRenderer packageRenderer;
    private final PlantUMLStyleManager styleManager;
    
    public PlantUMLGenerator() {
        this.classRenderer = new PlantUMLClassRenderer();
        this.relationshipRenderer = new PlantUMLRelationshipRenderer();
        this.packageRenderer = new PlantUMLPackageRenderer();
        this.styleManager = new PlantUMLStyleManager();
    }
    
    @Override
    public UMLGenerationResult generateClassDiagram(UMLGenerationRequest request) {
        LOGGER.startOperation("UML class diagram generation");
        long startTime = System.currentTimeMillis();
        
        try {
            PlantUMLDocument.Builder documentBuilder = PlantUMLDocument.builder()
                .title(request.getTitle())
                .addHeader("@startuml")
                .addFooter("@enduml");
            
            // Applica stili custom
            if (request.getStyleOptions() != null) {
                String styleDirectives = styleManager.generateStyleDirectives(request.getStyleOptions());
                documentBuilder.addSection(styleDirectives);
            }
            
            // Genera package structure se richiesto
            if (request.isIncludePackages()) {
                String packageStructure = packageRenderer.renderPackageStructure(
                    request.getPackageAnalysis());
                documentBuilder.addSection(packageStructure);
            }
            
            // Genera classi
            for (ClassInfo classInfo : request.getClassesToInclude()) {
                String classDefinition = classRenderer.renderClass(classInfo, request.getDetailLevel());
                documentBuilder.addClassDefinition(classDefinition);
            }
            
            // Genera relationships
            if (request.getClassRelationships() != null) {
                String relationships = relationshipRenderer.renderRelationships(
                    request.getClassRelationships(), 
                    request.getClassesToInclude()
                );
                documentBuilder.addSection(relationships);
            }
            
            // Evidenzia design patterns se richiesto
            if (request.isHighlightPatterns() && request.getDetectedPatterns() != null) {
                String patternHighlights = renderPatternHighlights(
                    request.getDetectedPatterns(), 
                    request.getClassesToInclude()
                );
                documentBuilder.addSection(patternHighlights);
            }
            
            PlantUMLDocument document = documentBuilder.build();
            
            return UMLGenerationResult.success(
                document.getContent(),
                document.getMetadata(),
                System.currentTimeMillis() - startTime
            );
            
        } catch (Exception e) {
            LOGGER.error("UML generation failed", e);
            return UMLGenerationResult.failed("Generation failed: " + e.getMessage());
        } finally {
            LOGGER.endOperation("UML class diagram generation", System.currentTimeMillis() - startTime);
        }
    }
    
    @Override
    public UMLGenerationResult generatePackageDiagram(PackageAnalysisResult packageAnalysis) {
        LOGGER.info("Generating package diagram for {} packages", 
            packageAnalysis.getHierarchy().getAllPackages().size());
        
        try {
            PlantUMLDocument.Builder documentBuilder = PlantUMLDocument.builder()
                .title("Package Structure Diagram")
                .addHeader("@startuml");
            
            // Genera struttura package
            String packageStructure = packageRenderer.renderFullPackageHierarchy(
                packageAnalysis.getHierarchy()
            );
            documentBuilder.addSection(packageStructure);
            
            // Genera dependencies tra package
            String packageDependencies = packageRenderer.renderPackageDependencies(
                packageAnalysis
            );
            documentBuilder.addSection(packageDependencies);
            
            // Evidenzia anti-patterns se presenti
            if (!packageAnalysis.getAntiPatterns().isEmpty()) {
                String antiPatternHighlights = renderAntiPatternHighlights(
                    packageAnalysis.getAntiPatterns()
                );
                documentBuilder.addSection(antiPatternHighlights);
            }
            
            documentBuilder.addFooter("@enduml");
            PlantUMLDocument document = documentBuilder.build();
            
            return UMLGenerationResult.success(document.getContent(), document.getMetadata(), 0);
            
        } catch (Exception e) {
            LOGGER.error("Package diagram generation failed", e);
            return UMLGenerationResult.failed("Package diagram generation failed: " + e.getMessage());
        }
    }
    
    @Override
    public UMLGenerationResult generatePatternDiagram(List<DetectedDesignPattern> patterns,
                                                     ClassRelationshipResult relationships) {
        LOGGER.info("Generating pattern diagram for {} detected patterns", patterns.size());
        
        try {
            List<UMLGenerationResult> patternDiagrams = new ArrayList<>();
            
            for (DetectedDesignPattern pattern : patterns) {
                UMLGenerationResult patternDiagram = generateSinglePatternDiagram(pattern, relationships);
                patternDiagrams.add(patternDiagram);
            }
            
            // Combina tutti i diagrammi pattern in un unico documento
            String combinedContent = combinePatternDiagrams(patternDiagrams);
            
            return UMLGenerationResult.success(combinedContent, 
                UMLMetadata.forPatternDiagram(patterns.size()), 0);
            
        } catch (Exception e) {
            LOGGER.error("Pattern diagram generation failed", e);
            return UMLGenerationResult.failed("Pattern diagram generation failed: " + e.getMessage());
        }
    }
    
    private UMLGenerationResult generateSinglePatternDiagram(DetectedDesignPattern pattern,
                                                            ClassRelationshipResult relationships) {
        
        PlantUMLDocument.Builder documentBuilder = PlantUMLDocument.builder()
            .title(pattern.getPatternType().getDisplayName() + " Pattern")
            .addHeader("@startuml");
        
        // Applica stile specifico per il pattern
        String patternStyle = styleManager.getPatternSpecificStyle(pattern.getPatternType());
        documentBuilder.addSection(patternStyle);
        
        // Renderizza solo le classi coinvolte nel pattern
        Set<String> patternClasses = pattern.getParticipants();
        for (String className : patternClasses) {
            ClassInfo classInfo = findClassInfo(className, relationships);
            if (classInfo != null) {
                String classDefinition = classRenderer.renderClass(classInfo, DetailLevel.PATTERN_FOCUSED);
                documentBuilder.addClassDefinition(classDefinition);
            }
        }
        
        // Renderizza solo le relazioni del pattern
        String patternRelationships = relationshipRenderer.renderPatternSpecificRelationships(
            pattern, relationships
        );
        documentBuilder.addSection(patternRelationships);
        
        // Aggiungi annotazioni del pattern
        String patternAnnotations = generatePatternAnnotations(pattern);
        documentBuilder.addSection(patternAnnotations);
        
        documentBuilder.addFooter("@enduml");
        PlantUMLDocument document = documentBuilder.build();
        
        return UMLGenerationResult.success(document.getContent(), 
            UMLMetadata.forSinglePattern(pattern), 0);
    }
}
```

#### 2. PlantUML Class Renderer
```java
public class PlantUMLClassRenderer {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(PlantUMLClassRenderer.class);
    
    public String renderClass(ClassInfo classInfo, DetailLevel detailLevel) {
        StringBuilder classBuilder = new StringBuilder();
        
        // Determina il tipo di elemento UML
        String umlElementType = determineUMLElementType(classInfo);
        
        classBuilder.append(umlElementType).append(" ");
        classBuilder.append(sanitizeClassName(classInfo.getFullyQualifiedName()));
        
        // Aggiungi stereotypes se applicabili
        String stereotypes = generateStereotypes(classInfo);
        if (!stereotypes.isEmpty()) {
            classBuilder.append(" ").append(stereotypes);
        }
        
        classBuilder.append(" {\n");
        
        // Aggiungi contenuto basato su detail level
        switch (detailLevel) {
            case MINIMAL -> {
                // Solo nome classe e stereotypes
            }
            case SUMMARY -> {
                classBuilder.append(renderFieldsSummary(classInfo));
                classBuilder.append(renderMethodsSummary(classInfo));
            }
            case DETAILED -> {
                classBuilder.append(renderFieldsDetailed(classInfo));
                classBuilder.append(renderMethodsDetailed(classInfo));
            }
            case PATTERN_FOCUSED -> {
                classBuilder.append(renderPatternRelevantMembers(classInfo));
            }
        }
        
        classBuilder.append("}\n");
        
        return classBuilder.toString();
    }
    
    private String determineUMLElementType(ClassInfo classInfo) {
        if (classInfo.isInterface()) {
            return "interface";
        } else if (classInfo.isAbstract()) {
            return "abstract class";
        } else if (classInfo.isEnum()) {
            return "enum";
        } else {
            return "class";
        }
    }
    
    private String generateStereotypes(ClassInfo classInfo) {
        List<String> stereotypes = new ArrayList<>();
        
        // Spring stereotypes
        if (hasAnnotation(classInfo, "org.springframework.stereotype.Component")) {
            stereotypes.add("<<Component>>");
        }
        if (hasAnnotation(classInfo, "org.springframework.stereotype.Service")) {
            stereotypes.add("<<Service>>");
        }
        if (hasAnnotation(classInfo, "org.springframework.stereotype.Repository")) {
            stereotypes.add("<<Repository>>");
        }
        if (hasAnnotation(classInfo, "org.springframework.stereotype.Controller")) {
            stereotypes.add("<<Controller>>");
        }
        if (hasAnnotation(classInfo, "org.springframework.web.bind.annotation.RestController")) {
            stereotypes.add("<<RestController>>");
        }
        
        // JPA stereotypes
        if (hasAnnotation(classInfo, "javax.persistence.Entity")) {
            stereotypes.add("<<Entity>>");
        }
        if (hasAnnotation(classInfo, "javax.persistence.Embeddable")) {
            stereotypes.add("<<Embeddable>>");
        }
        
        // Design pattern stereotypes (da analisi precedente)
        // Questi potrebbero essere aggiunti dal pattern detector
        
        return String.join(" ", stereotypes);
    }
    
    private String renderFieldsDetailed(ClassInfo classInfo) {
        StringBuilder fieldsBuilder = new StringBuilder();
        
        for (FieldInfo field : classInfo.getFields()) {
            fieldsBuilder.append("  ");
            fieldsBuilder.append(renderVisibility(field.getAccessModifier()));
            fieldsBuilder.append(field.getName());
            fieldsBuilder.append(" : ");
            fieldsBuilder.append(simplifyTypeName(field.getType()));
            
            // Aggiungi modificatori
            if (field.isStatic()) {
                fieldsBuilder.append(" {static}");
            }
            if (field.isFinal()) {
                fieldsBuilder.append(" {readOnly}");
            }
            
            fieldsBuilder.append("\n");
        }
        
        return fieldsBuilder.toString();
    }
    
    private String renderMethodsDetailed(ClassInfo classInfo) {
        StringBuilder methodsBuilder = new StringBuilder();
        
        for (MethodInfo method : classInfo.getMethods()) {
            // Filtra metodi getter/setter se richiesto
            if (isGetterSetter(method) && shouldHideGettersSetters()) {
                continue;
            }
            
            methodsBuilder.append("  ");
            methodsBuilder.append(renderVisibility(method.getAccessModifier()));
            methodsBuilder.append(method.getName());
            methodsBuilder.append("(");
            
            // Parametri
            String parameters = method.getParameters().stream()
                .map(param -> param.getName() + " : " + simplifyTypeName(param.getType()))
                .collect(Collectors.joining(", "));
            methodsBuilder.append(parameters);
            
            methodsBuilder.append(") : ");
            methodsBuilder.append(simplifyTypeName(method.getReturnType()));
            
            // Modificatori
            if (method.isStatic()) {
                methodsBuilder.append(" {static}");
            }
            if (method.isAbstract()) {
                methodsBuilder.append(" {abstract}");
            }
            
            methodsBuilder.append("\n");
        }
        
        return methodsBuilder.toString();
    }
    
    private String renderVisibility(AccessModifier modifier) {
        return switch (modifier) {
            case PUBLIC -> "+";
            case PROTECTED -> "#";
            case PRIVATE -> "-";
            case PACKAGE_PRIVATE -> "~";
        };
    }
    
    private String simplifyTypeName(String fullTypeName) {
        // Rimuovi package names per leggibilità
        if (fullTypeName.lastIndexOf('.') > 0) {
            return fullTypeName.substring(fullTypeName.lastIndexOf('.') + 1);
        }
        return fullTypeName;
    }
    
    private String sanitizeClassName(String className) {
        // Sostituisci caratteri non validi per PlantUML
        return className.replace("$", "_");
    }
}
```

#### 3. PlantUML Relationship Renderer
```java
public class PlantUMLRelationshipRenderer {
    
    public String renderRelationships(ClassRelationshipResult relationships, 
                                    Set<ClassInfo> includedClasses) {
        StringBuilder relationshipBuilder = new StringBuilder();
        relationshipBuilder.append("\n' Relationships\n");
        
        // Render inheritance relationships
        relationshipBuilder.append(renderInheritanceRelationships(
            relationships.getInheritanceHierarchy(), 
            includedClasses
        ));
        
        // Render composition/aggregation relationships  
        relationshipBuilder.append(renderCompositionRelationships(
            relationships.getCompositionGraph(),
            includedClasses
        ));
        
        // Render usage dependencies (selettivamente)
        relationshipBuilder.append(renderUsageDependencies(
            relationships.getUsageDependencies(),
            includedClasses
        ));
        
        return relationshipBuilder.toString();
    }
    
    private String renderInheritanceRelationships(InheritanceHierarchy hierarchy, 
                                                Set<ClassInfo> includedClasses) {
        StringBuilder inheritanceBuilder = new StringBuilder();
        
        for (InheritanceRelation relation : hierarchy.getAllRelations()) {
            if (isIncluded(relation.getParent(), includedClasses) && 
                isIncluded(relation.getChild(), includedClasses)) {
                
                String plantUMLRelation = convertToPlantUMLInheritance(relation);
                inheritanceBuilder.append(plantUMLRelation).append("\n");
            }
        }
        
        return inheritanceBuilder.toString();
    }
    
    private String convertToPlantUMLInheritance(InheritanceRelation relation) {
        String parent = sanitizeClassName(relation.getParent());
        String child = sanitizeClassName(relation.getChild());
        
        return switch (relation.getType()) {
            case EXTENDS -> parent + " <|-- " + child;
            case IMPLEMENTS -> parent + " <|.. " + child;
        };
    }
    
    private String renderCompositionRelationships(CompositionGraph compositionGraph,
                                                Set<ClassInfo> includedClasses) {
        StringBuilder compositionBuilder = new StringBuilder();
        
        for (CompositionRelationship relation : compositionGraph.getAllRelationships()) {
            if (isIncluded(relation.getSource(), includedClasses) &&
                isIncluded(relation.getTarget(), includedClasses)) {
                
                String plantUMLRelation = convertToPlantUMLComposition(relation);
                compositionBuilder.append(plantUMLRelation).append("\n");
            }
        }
        
        return compositionBuilder.toString();
    }
    
    private String convertToPlantUMLComposition(CompositionRelationship relation) {
        String source = sanitizeClassName(relation.getSource());
        String target = sanitizeClassName(relation.getTarget());
        String multiplicity = convertMultiplicity(relation.getMultiplicity());
        
        String relationSymbol = switch (relation.getCompositionType()) {
            case COMPOSITION -> " *-- ";
            case AGGREGATION -> " o-- ";
            case ASSOCIATION -> " --> ";
            default -> " -- ";
        };
        
        String relationLine = source + relationSymbol + multiplicity + " " + target;
        
        // Aggiungi label se disponibile
        if (relation.getFieldName() != null) {
            relationLine += " : " + relation.getFieldName();
        }
        
        return relationLine;
    }
    
    private String convertMultiplicity(Multiplicity multiplicity) {
        return switch (multiplicity) {
            case ONE_TO_ONE -> "\"1\"";
            case ONE_TO_MANY -> "\"1..*\"";
            case ZERO_TO_ONE -> "\"0..1\"";
            case ZERO_TO_MANY -> "\"*\"";
        };
    }
    
    public String renderPatternSpecificRelationships(DetectedDesignPattern pattern,
                                                   ClassRelationshipResult relationships) {
        StringBuilder patternBuilder = new StringBuilder();
        
        switch (pattern.getPatternType()) {
            case STRATEGY -> {
                patternBuilder.append(renderStrategyPatternRelationships(pattern, relationships));
            }
            case OBSERVER -> {
                patternBuilder.append(renderObserverPatternRelationships(pattern, relationships));
            }
            case DECORATOR -> {
                patternBuilder.append(renderDecoratorPatternRelationships(pattern, relationships));
            }
            case FACTORY -> {
                patternBuilder.append(renderFactoryPatternRelationships(pattern, relationships));
            }
            // Add other patterns as needed
        }
        
        return patternBuilder.toString();
    }
    
    private String renderStrategyPatternRelationships(DetectedDesignPattern pattern,
                                                    ClassRelationshipResult relationships) {
        StringBuilder strategyBuilder = new StringBuilder();
        
        // In uno Strategy pattern tipico:
        // Context --> Strategy (association)
        // Strategy <|-- ConcreteStrategy (implementation)
        
        Map<String, String> participants = pattern.getParticipantRoles();
        String context = participants.get("Context");
        String strategy = participants.get("Strategy");
        List<String> concreteStrategies = pattern.getParticipantsWithRole("ConcreteStrategy");
        
        if (context != null && strategy != null) {
            // Context uses Strategy
            strategyBuilder.append(sanitizeClassName(context))
                          .append(" --> ")
                          .append(sanitizeClassName(strategy))
                          .append(" : uses\n");
        }
        
        // ConcreteStrategies implement Strategy
        for (String concreteStrategy : concreteStrategies) {
            strategyBuilder.append(sanitizeClassName(strategy))
                          .append(" <|.. ")
                          .append(sanitizeClassName(concreteStrategy))
                          .append("\n");
        }
        
        return strategyBuilder.toString();
    }
}
```

#### 4. Style Manager and Configuration
```java
public class PlantUMLStyleManager {
    
    public String generateStyleDirectives(UMLStyleOptions options) {
        StringBuilder styleBuilder = new StringBuilder();
        
        styleBuilder.append("\n' Style Configuration\n");
        
        // Skin parameters
        if (options.getSkinParam() != null) {
            styleBuilder.append("!theme ").append(options.getSkinParam()).append("\n");
        }
        
        // Colors
        if (options.getClassColor() != null) {
            styleBuilder.append("skinparam class {\n");
            styleBuilder.append("  BackgroundColor ").append(options.getClassColor()).append("\n");
            styleBuilder.append("}\n");
        }
        
        // Arrow styles
        if (options.getArrowStyle() != null) {
            styleBuilder.append("skinparam linetype ").append(options.getArrowStyle()).append("\n");
        }
        
        // Layout direction
        if (options.getLayoutDirection() != null) {
            styleBuilder.append("!define DIRECTION ").append(options.getLayoutDirection()).append("\n");
            styleBuilder.append("left to right direction\n");
        }
        
        return styleBuilder.toString();
    }
    
    public String getPatternSpecificStyle(DesignPatternType patternType) {
        return switch (patternType) {
            case STRATEGY -> """
                skinparam class {
                  BackgroundColor<<Strategy>> lightblue
                  BackgroundColor<<ConcreteStrategy>> lightgreen
                  BackgroundColor<<Context>> lightyellow
                }
                """;
            case OBSERVER -> """
                skinparam class {
                  BackgroundColor<<Subject>> lightcoral
                  BackgroundColor<<Observer>> lightblue
                  BackgroundColor<<ConcreteObserver>> lightgreen
                }
                """;
            case DECORATOR -> """
                skinparam class {
                  BackgroundColor<<Component>> lightblue
                  BackgroundColor<<ConcreteComponent>> lightgreen
                  BackgroundColor<<Decorator>> lightyellow
                  BackgroundColor<<ConcreteDecorator>> lightcoral
                }
                """;
            default -> "";
        };
    }
}

public class UMLGenerationRequest {
    private final String title;
    private final Set<ClassInfo> classesToInclude;
    private final DetailLevel detailLevel;
    private final boolean includePackages;
    private final boolean highlightPatterns;
    private final PackageAnalysisResult packageAnalysis;
    private final ClassRelationshipResult classRelationships;
    private final List<DetectedDesignPattern> detectedPatterns;
    private final UMLStyleOptions styleOptions;
    
    public static class Builder {
        private String title = "Class Diagram";
        private Set<ClassInfo> classesToInclude = new HashSet<>();
        private DetailLevel detailLevel = DetailLevel.SUMMARY;
        private boolean includePackages = false;
        private boolean highlightPatterns = false;
        private PackageAnalysisResult packageAnalysis;
        private ClassRelationshipResult classRelationships;
        private List<DetectedDesignPattern> detectedPatterns = new ArrayList<>();
        private UMLStyleOptions styleOptions;
        
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder includeClass(ClassInfo classInfo) {
            this.classesToInclude.add(classInfo);
            return this;
        }
        
        public Builder includeClasses(Collection<ClassInfo> classes) {
            this.classesToInclude.addAll(classes);
            return this;
        }
        
        public Builder detailLevel(DetailLevel level) {
            this.detailLevel = level;
            return this;
        }
        
        public Builder includePackages(PackageAnalysisResult packageAnalysis) {
            this.includePackages = true;
            this.packageAnalysis = packageAnalysis;
            return this;
        }
        
        public Builder highlightPatterns(List<DetectedDesignPattern> patterns) {
            this.highlightPatterns = true;
            this.detectedPatterns = patterns;
            return this;
        }
        
        public Builder relationshipData(ClassRelationshipResult relationships) {
            this.classRelationships = relationships;
            return this;
        }
        
        public Builder styleOptions(UMLStyleOptions options) {
            this.styleOptions = options;
            return this;
        }
        
        public UMLGenerationRequest build() {
            return new UMLGenerationRequest(
                title, classesToInclude, detailLevel, includePackages,
                highlightPatterns, packageAnalysis, classRelationships,
                detectedPatterns, styleOptions
            );
        }
    }
}

public enum DetailLevel {
    MINIMAL,        // Solo nomi classi
    SUMMARY,        // Campi e metodi principali
    DETAILED,       // Tutti i campi e metodi
    PATTERN_FOCUSED // Solo elementi rilevanti per pattern
}
```

### Principi SOLID Applicati
- **SRP**: Renderer separati per classi, relazioni, package, stili
- **OCP**: Facilmente estendibile per nuovi tipi di diagrammi e stili
- **LSP**: Implementazioni intercambiabili per diversi formati UML
- **ISP**: Interfacce specifiche per diversi aspetti della generazione
- **DIP**: Dipende da abstractions per data input

### Test Unitari da Implementare
```java
// PlantUMLGeneratorTest.java
@Test
void shouldGenerateBasicClassDiagram() {
    // Arrange
    Set<ClassInfo> classes = createSampleClasses();
    UMLGenerationRequest request = UMLGenerationRequest.builder()
        .title("Sample Class Diagram")
        .includeClasses(classes)
        .detailLevel(DetailLevel.SUMMARY)
        .build();
    
    UMLGenerator generator = new PlantUMLGenerator();
    
    // Act
    UMLGenerationResult result = generator.generateClassDiagram(request);
    
    // Assert
    assertThat(result.isSuccessful()).isTrue();
    String plantUML = result.getContent();
    
    assertThat(plantUML).startsWith("@startuml");
    assertThat(plantUML).endsWith("@enduml");
    assertThat(plantUML).contains("class com.example.User");
    assertThat(plantUML).contains("class com.example.Order");
}

@Test
void shouldGenerateInheritanceRelationships() {
    JarContent jarContent = createJarWithInheritance();
    ClassRelationshipResult relationships = new JavassistClassRelationshipAnalyzer()
        .analyzeRelationships(jarContent);
    
    UMLGenerationRequest request = UMLGenerationRequest.builder()
        .includeClasses(jarContent.getClasses())
        .relationshipData(relationships)
        .build();
    
    UMLGenerationResult result = new PlantUMLGenerator().generateClassDiagram(request);
    
    String plantUML = result.getContent();
    assertThat(plantUML).contains("Animal <|-- Dog");
    assertThat(plantUML).contains("Animal <|-- Cat");
}

@Test
void shouldGenerateCompositionRelationships() {
    JarContent jarContent = createJarWithComposition();
    ClassRelationshipResult relationships = new JavassistClassRelationshipAnalyzer()
        .analyzeRelationships(jarContent);
    
    UMLGenerationRequest request = UMLGenerationRequest.builder()
        .includeClasses(jarContent.getClasses())
        .relationshipData(relationships)
        .build();
    
    UMLGenerationResult result = new PlantUMLGenerator().generateClassDiagram(request);
    
    String plantUML = result.getContent();
    assertThat(plantUML).contains("Car *-- \"1\" Engine");
    assertThat(plantUML).contains("Car o-- \"4\" Wheel");
}

@Test
void shouldApplySpringStereotypes() {
    ClassInfo serviceClass = createClassWithAnnotation(
        "com.example.UserService",
        "org.springframework.stereotype.Service"
    );
    
    UMLGenerationRequest request = UMLGenerationRequest.builder()
        .includeClass(serviceClass)
        .detailLevel(DetailLevel.SUMMARY)
        .build();
    
    UMLGenerationResult result = new PlantUMLGenerator().generateClassDiagram(request);
    
    String plantUML = result.getContent();
    assertThat(plantUML).contains("class com.example.UserService <<Service>>");
}

@Test
void shouldGeneratePackageDiagram() {
    PackageAnalysisResult packageAnalysis = createPackageAnalysisResult();
    
    UMLGenerationResult result = new PlantUMLGenerator().generatePackageDiagram(packageAnalysis);
    
    assertThat(result.isSuccessful()).isTrue();
    String plantUML = result.getContent();
    
    assertThat(plantUML).contains("package com.example.controller");
    assertThat(plantUML).contains("package com.example.service");
    assertThat(plantUML).contains("package com.example.repository");
}

@Test
void shouldHighlightDesignPatterns() {
    DetectedDesignPattern strategyPattern = createStrategyPattern();
    ClassRelationshipResult relationships = createRelationshipsForStrategy();
    
    UMLGenerationRequest request = UMLGenerationRequest.builder()
        .includeClasses(getPatternClasses(strategyPattern))
        .relationshipData(relationships)
        .highlightPatterns(List.of(strategyPattern))
        .build();
    
    UMLGenerationResult result = new PlantUMLGenerator().generateClassDiagram(request);
    
    String plantUML = result.getContent();
    assertThat(plantUML).contains("<<Strategy>>");
    assertThat(plantUML).contains("<<ConcreteStrategy>>");
    assertThat(plantUML).contains("<<Context>>");
}

// PlantUMLClassRendererTest.java
@Test
void shouldRenderClassWithFields() {
    ClassInfo classInfo = createClassWithFields();
    PlantUMLClassRenderer renderer = new PlantUMLClassRenderer();
    
    String rendered = renderer.renderClass(classInfo, DetailLevel.DETAILED);
    
    assertThat(rendered).contains("class com.example.User {");
    assertThat(rendered).contains("+name : String");
    assertThat(rendered).contains("-id : Long");
    assertThat(rendered).contains("}");
}

@Test
void shouldRenderInterface() {
    ClassInfo interfaceInfo = createInterface();
    PlantUMLClassRenderer renderer = new PlantUMLClassRenderer();
    
    String rendered = renderer.renderClass(interfaceInfo, DetailLevel.SUMMARY);
    
    assertThat(rendered).startsWith("interface com.example.UserRepository");
}

@Test
void shouldApplyDetailLevels() {
    ClassInfo classInfo = createComplexClass();
    PlantUMLClassRenderer renderer = new PlantUMLClassRenderer();
    
    String minimal = renderer.renderClass(classInfo, DetailLevel.MINIMAL);
    String detailed = renderer.renderClass(classInfo, DetailLevel.DETAILED);
    
    assertThat(minimal.length()).isLessThan(detailed.length());
    assertThat(detailed).contains("private");
    assertThat(detailed).contains("public");
}

// Integration Test
@Test
void shouldGenerateCompleteApplicationDiagram() throws IOException {
    // Load real Spring Boot application
    Path springBootJar = getTestResourcePath("sample-spring-boot.jar");
    JarContent jarContent = new JarLoader().loadJar(new JarLocation(springBootJar));
    
    // Analyze all aspects
    PackageAnalysisResult packageAnalysis = new JavassistPackageAnalyzer()
        .analyzePackageStructure(jarContent);
    ClassRelationshipResult relationships = new JavassistClassRelationshipAnalyzer()
        .analyzeRelationships(jarContent);
    
    // Generate comprehensive diagram
    UMLGenerationRequest request = UMLGenerationRequest.builder()
        .title("Complete Application Architecture")
        .includeClasses(jarContent.getClasses())
        .includePackages(packageAnalysis)
        .relationshipData(relationships)
        .highlightPatterns(relationships.getDesignPatterns())
        .detailLevel(DetailLevel.SUMMARY)
        .build();
    
    UMLGenerationResult result = new PlantUMLGenerator().generateClassDiagram(request);
    
    assertThat(result.isSuccessful()).isTrue();
    assertThat(result.getContent()).contains("@startuml");
    assertThat(result.getContent()).contains("@enduml");
    
    // Verify the generated PlantUML is valid (could use PlantUML parser)
    assertValidPlantUML(result.getContent());
}

private void assertValidPlantUML(String plantUMLContent) {
    // Basic syntax validation
    assertThat(plantUMLContent).startsWith("@startuml");
    assertThat(plantUMLContent).endsWith("@enduml");
    
    // Count brackets
    long openBraces = plantUMLContent.chars().filter(ch -> ch == '{').count();
    long closeBraces = plantUMLContent.chars().filter(ch -> ch == '}').count();
    assertThat(openBraces).isEqualTo(closeBraces);
}
```

---

## T3.1.4: DependencyGraphBuilder per Inter-Package Dependencies

### Descrizione Dettagliata
Builder sofisticato per creare grafi completi delle dipendenze inter-package, analizzando le relazioni tra package diversi e identificando pattern di dipendenza, accoppiamento architetturale e possibili violazioni dei principi di design.

### Scopo dell'Attività
- Mappare tutte le dipendenze tra package differenti
- Calcolare metriche di accoppiamento inter-package
- Identificare cicli di dipendenze tra package
- Rilevare violazioni dei principi architetturali
- Supportare analisi di impatto per modifiche

### Impatti su Altri Moduli
- **ArchitectureAnalyzer**: Input per valutazione architetturale
- **CircularDependencyDetector**: Base per detection algoritmi
- **UMLGenerator**: Dati per package dependency diagrams  
- **RefactoringAnalyzer**: Identificazione candidati per refactoring

### Componenti da Implementare

#### 1. Dependency Graph Builder Core
```java
public interface DependencyGraphBuilder {
    PackageDependencyGraph buildDependencyGraph(JarContent jarContent, 
                                               PackageAnalysisResult packageAnalysis);
}

public class JavassistDependencyGraphBuilder implements DependencyGraphBuilder {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistDependencyGraphBuilder.class);
    
    private final PackageDependencyExtractor dependencyExtractor;
    private final DependencyMetricsCalculator metricsCalculator;
    private final CircularDependencyDetector circularDetector;
    private final ArchitecturalViolationDetector violationDetector;
    
    public JavassistDependencyGraphBuilder() {
        this.dependencyExtractor = new PackageDependencyExtractor();
        this.metricsCalculator = new DependencyMetricsCalculator();
        this.circularDetector = new CircularDependencyDetector();
        this.violationDetector = new ArchitecturalViolationDetector();
    }
    
    @Override
    public PackageDependencyGraph buildDependencyGraph(JarContent jarContent,
                                                      PackageAnalysisResult packageAnalysis) {
        LOGGER.startOperation("Building package dependency graph");
        long startTime = System.currentTimeMillis();
        
        try {
            PackageDependencyGraph.Builder graphBuilder = PackageDependencyGraph.builder();
            
            // Estrai dipendenze raw tra package
            List<PackageDependency> rawDependencies = dependencyExtractor.extractDependencies(
                jarContent, packageAnalysis.getHierarchy()
            );
            
            // Filtra e classifica dipendenze
            List<PackageDependency> filteredDependencies = filterAndClassifyDependencies(rawDependencies);
            graphBuilder.dependencies(filteredDependencies);
            
            // Costruisci grafo navigabile
            DependencyGraphStructure graphStructure = buildGraphStructure(filteredDependencies);
            graphBuilder.graphStructure(graphStructure);
            
            // Calcola metriche di dipendenza
            PackageDependencyMetrics metrics = metricsCalculator.calculateMetrics(
                filteredDependencies, packageAnalysis.getHierarchy()
            );
            graphBuilder.metrics(metrics);
            
            // Rileva cicli di dipendenze
            List<PackageCircularDependency> circularDependencies = circularDetector.detectCircularDependencies(
                filteredDependencies, packageAnalysis.getHierarchy()
            );
            graphBuilder.circularDependencies(circularDependencies);
            
            // Rileva violazioni architetturali
            List<ArchitecturalViolation> violations = violationDetector.detectViolations(
                filteredDependencies, packageAnalysis
            );
            graphBuilder.architecturalViolations(violations);
            
            // Analizza stabilità dei package
            Map<String, PackageStabilityMetrics> stabilityMetrics = calculatePackageStability(
                filteredDependencies, packageAnalysis.getHierarchy()
            );
            graphBuilder.stabilityMetrics(stabilityMetrics);
            
            return graphBuilder
                .analysisMetadata(AnalysisMetadata.successful(System.currentTimeMillis() - startTime))
                .build();
                
        } catch (Exception e) {
            LOGGER.error("Dependency graph building failed", e);
            return PackageDependencyGraph.failed("Graph building failed: " + e.getMessage());
        } finally {
            LOGGER.endOperation("Building package dependency graph", System.currentTimeMillis() - startTime);
        }
    }
    
    private List<PackageDependency> filterAndClassifyDependencies(List<PackageDependency> rawDependencies) {
        return rawDependencies.stream()
            .filter(this::isRelevantDependency)
            .map(this::classifyDependency)
            .collect(Collectors.toList());
    }
    
    private boolean isRelevantDependency(PackageDependency dependency) {
        // Filtra dipendenze verso JDK e librerie standard
        String targetPackage = dependency.getTargetPackage();
        
        return !targetPackage.startsWith("java.") &&
               !targetPackage.startsWith("javax.") &&
               !targetPackage.startsWith("sun.") &&
               !dependency.getSourcePackage().equals(dependency.getTargetPackage());
    }
    
    private PackageDependency classifyDependency(PackageDependency dependency) {
        DependencyType type = determineDependencyType(dependency);
        DependencyStrength strength = calculateDependencyStrength(dependency);
        
        return dependency.withClassification(type, strength);
    }
    
    private DependencyType determineDependencyType(PackageDependency dependency) {
        // Analizza il tipo di dipendenza basato sui usage patterns
        if (dependency.getUsageTypes().contains(UsageType.INHERITANCE)) {
            return DependencyType.INHERITANCE;
        } else if (dependency.getUsageTypes().contains(UsageType.COMPOSITION)) {
            return DependencyType.COMPOSITION;
        } else if (dependency.getUsageTypes().contains(UsageType.METHOD_CALL)) {
            return DependencyType.USAGE;
        } else {
            return DependencyType.REFERENCE;
        }
    }
    
    private DependencyStrength calculateDependencyStrength(PackageDependency dependency) {
        int usageCount = dependency.getUsageCount();
        int uniqueClasses = dependency.getUniqueSourceClasses().size();
        
        // Formula per calcolare la forza della dipendenza
        double strength = (usageCount * 0.7) + (uniqueClasses * 0.3);
        
        if (strength > 50) return DependencyStrength.STRONG;
        else if (strength > 20) return DependencyStrength.MODERATE;
        else return DependencyStrength.WEAK;
    }
}
```

#### 2. Package Dependency Extractor  
```java
public class PackageDependencyExtractor {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(PackageDependencyExtractor.class);
    
    public List<PackageDependency> extractDependencies(JarContent jarContent, 
                                                      PackageHierarchy packageHierarchy) {
        LOGGER.info("Extracting package dependencies from {} classes", jarContent.getClasses().size());
        
        Map<PackagePair, PackageDependency.Builder> dependencyBuilders = new HashMap<>();
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            extractDependenciesFromClass(classInfo, dependencyBuilders);
        }
        
        return dependencyBuilders.values().stream()
            .map(PackageDependency.Builder::build)
            .collect(Collectors.toList());
    }
    
    private void extractDependenciesFromClass(ClassInfo classInfo, 
                                            Map<PackagePair, PackageDependency.Builder> dependencyBuilders) {
        String sourcePackage = extractPackageName(classInfo.getFullyQualifiedName());
        
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.get(classInfo.getFullyQualifiedName());
            
            // Analizza imports e references
            extractFromImports(ctClass, sourcePackage, dependencyBuilders);
            
            // Analizza field types
            extractFromFields(ctClass, sourcePackage, dependencyBuilders);
            
            // Analizza method signatures
            extractFromMethods(ctClass, sourcePackage, dependencyBuilders);
            
            // Analizza method bodies
            extractFromMethodBodies(ctClass, sourcePackage, dependencyBuilders);
            
        } catch (Exception e) {
            LOGGER.error("Error extracting dependencies from {}", classInfo.getFullyQualifiedName(), e);
        }
    }
    
    private void extractFromFields(CtClass ctClass, String sourcePackage,
                                 Map<PackagePair, PackageDependency.Builder> dependencyBuilders) 
            throws NotFoundException {
        
        for (CtField field : ctClass.getDeclaredFields()) {
            String fieldTypeName = field.getType().getName();
            String targetPackage = extractPackageName(fieldTypeName);
            
            if (targetPackage != null && !targetPackage.equals(sourcePackage)) {
                PackagePair key = new PackagePair(sourcePackage, targetPackage);
                PackageDependency.Builder builder = dependencyBuilders.computeIfAbsent(
                    key, k -> PackageDependency.builder()
                        .sourcePackage(sourcePackage)
                        .targetPackage(targetPackage)
                );
                
                builder.addUsage(UsageType.FIELD_TYPE, ctClass.getName(), field.getName());
                builder.addSourceClass(ctClass.getName());
            }
        }
    }
    
    private void extractFromMethods(CtClass ctClass, String sourcePackage,
                                  Map<PackagePair, PackageDependency.Builder> dependencyBuilders) 
            throws NotFoundException {
        
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            // Return type
            String returnTypeName = method.getReturnType().getName();
            String returnTypePackage = extractPackageName(returnTypeName);
            
            if (returnTypePackage != null && !returnTypePackage.equals(sourcePackage)) {
                addDependency(sourcePackage, returnTypePackage, UsageType.RETURN_TYPE, 
                            ctClass.getName(), method.getName(), dependencyBuilders);
            }
            
            // Parameter types
            try {
                CtClass[] parameterTypes = method.getParameterTypes();
                for (CtClass paramType : parameterTypes) {
                    String paramTypeName = paramType.getName();
                    String paramTypePackage = extractPackageName(paramTypeName);
                    
                    if (paramTypePackage != null && !paramTypePackage.equals(sourcePackage)) {
                        addDependency(sourcePackage, paramTypePackage, UsageType.PARAMETER_TYPE,
                                    ctClass.getName(), method.getName(), dependencyBuilders);
                    }
                }
            } catch (NotFoundException e) {
                LOGGER.debug("Could not analyze parameter types for {}", method.getLongName());
            }
        }
    }
    
    private void extractFromMethodBodies(CtClass ctClass, String sourcePackage,
                                       Map<PackagePair, PackageDependency.Builder> dependencyBuilders) {
        
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            try {
                CodeAttribute codeAttr = method.getMethodInfo().getCodeAttribute();
                if (codeAttr == null) continue; // Abstract method
                
                CodeIterator iterator = codeAttr.iterator();
                
                while (iterator.hasNext()) {
                    int index = iterator.next();
                    int opcode = iterator.byteAt(index);
                    
                    switch (opcode) {
                        case Opcode.INVOKEVIRTUAL, Opcode.INVOKESPECIAL, 
                             Opcode.INVOKESTATIC, Opcode.INVOKEINTERFACE -> {
                            String targetClass = getInvokedMethodClass(ctClass, iterator, index);
                            if (targetClass != null) {
                                String targetPackage = extractPackageName(targetClass);
                                if (targetPackage != null && !targetPackage.equals(sourcePackage)) {
                                    addDependency(sourcePackage, targetPackage, UsageType.METHOD_CALL,
                                                ctClass.getName(), method.getName(), dependencyBuilders);
                                }
                            }
                        }
                        
                        case Opcode.NEW -> {
                            String instantiatedClass = getInstantiatedClass(ctClass, iterator, index);
                            if (instantiatedClass != null) {
                                String targetPackage = extractPackageName(instantiatedClass);
                                if (targetPackage != null && !targetPackage.equals(sourcePackage)) {
                                    addDependency(sourcePackage, targetPackage, UsageType.INSTANTIATION,
                                                ctClass.getName(), method.getName(), dependencyBuilders);
                                }
                            }
                        }
                        
                        case Opcode.CHECKCAST, Opcode.INSTANCEOF -> {
                            String castClass = getCastClass(ctClass, iterator, index);
                            if (castClass != null) {
                                String targetPackage = extractPackageName(castClass);
                                if (targetPackage != null && !targetPackage.equals(sourcePackage)) {
                                    addDependency(sourcePackage, targetPackage, UsageType.TYPE_CHECK,
                                                ctClass.getName(), method.getName(), dependencyBuilders);
                                }
                            }
                        }
                    }
                }
                
            } catch (Exception e) {
                LOGGER.debug("Error analyzing method body: {}", method.getLongName(), e);
            }
        }
    }
    
    private void addDependency(String sourcePackage, String targetPackage, UsageType usageType,
                             String sourceClass, String context,
                             Map<PackagePair, PackageDependency.Builder> dependencyBuilders) {
        
        PackagePair key = new PackagePair(sourcePackage, targetPackage);
        PackageDependency.Builder builder = dependencyBuilders.computeIfAbsent(
            key, k -> PackageDependency.builder()
                .sourcePackage(sourcePackage)
                .targetPackage(targetPackage)
        );
        
        builder.addUsage(usageType, sourceClass, context);
        builder.addSourceClass(sourceClass);
    }
}
```

#### 3. Circular Dependency Detector
```java
public class CircularDependencyDetector {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(CircularDependencyDetector.class);
    
    public List<PackageCircularDependency> detectCircularDependencies(
            List<PackageDependency> dependencies, 
            PackageHierarchy packageHierarchy) {
        
        LOGGER.info("Detecting circular dependencies among {} package dependencies", dependencies.size());
        
        // Costruisci grafo diretto
        DirectedGraph<String> dependencyGraph = buildDirectedGraph(dependencies);
        
        // Usa algoritmo di Tarjan per trovare Strongly Connected Components
        TarjanAlgorithm tarjan = new TarjanAlgorithm();
        List<List<String>> stronglyConnectedComponents = tarjan.findStronglyConnectedComponents(dependencyGraph);
        
        // Filtra SCC con più di un nodo (cicli reali)
        List<PackageCircularDependency> circularDependencies = new ArrayList<>();
        
        for (List<String> component : stronglyConnectedComponents) {
            if (component.size() > 1) {
                PackageCircularDependency circular = buildCircularDependency(component, dependencies);
                circularDependencies.add(circular);
            }
        }
        
        // Classifica per severità
        circularDependencies.sort(Comparator.comparing(PackageCircularDependency::getSeverity).reversed());
        
        LOGGER.info("Found {} circular dependencies", circularDependencies.size());
        return circularDependencies;
    }
    
    private DirectedGraph<String> buildDirectedGraph(List<PackageDependency> dependencies) {
        DirectedGraph<String> graph = new DirectedGraph<>();
        
        for (PackageDependency dependency : dependencies) {
            graph.addEdge(dependency.getSourcePackage(), dependency.getTargetPackage());
        }
        
        return graph;
    }
    
    private PackageCircularDependency buildCircularDependency(List<String> cycle, 
                                                             List<PackageDependency> allDependencies) {
        
        PackageCircularDependency.Builder builder = PackageCircularDependency.builder()
            .involvedPackages(cycle);
        
        // Trova le dipendenze specifiche che creano il ciclo
        for (int i = 0; i < cycle.size(); i++) {
            String current = cycle.get(i);
            String next = cycle.get((i + 1) % cycle.size());
            
            Optional<PackageDependency> cyclicDependency = allDependencies.stream()
                .filter(dep -> dep.getSourcePackage().equals(current) && 
                              dep.getTargetPackage().equals(next))
                .findFirst();
                
            cyclicDependency.ifPresent(builder::addCyclicDependency);
        }
        
        // Calcola severità del ciclo
        CircularDependencySeverity severity = calculateCycleSeverity(cycle, allDependencies);
        builder.severity(severity);
        
        // Suggerisci possibili soluzioni
        List<CircularDependencySolution> solutions = suggestSolutions(cycle, allDependencies);
        builder.suggestedSolutions(solutions);
        
        return builder.build();
    }
    
    private CircularDependencySeverity calculateCycleSeverity(List<String> cycle, 
                                                             List<PackageDependency> allDependencies) {
        
        int totalUsages = 0;
        int strongDependencies = 0;
        
        for (String packageName : cycle) {
            List<PackageDependency> packageDeps = allDependencies.stream()
                .filter(dep -> cycle.contains(dep.getSourcePackage()) && 
                              cycle.contains(dep.getTargetPackage()))
                .collect(Collectors.toList());
                
            totalUsages += packageDeps.stream().mapToInt(PackageDependency::getUsageCount).sum();
            strongDependencies += (int) packageDeps.stream()
                .filter(dep -> dep.getStrength() == DependencyStrength.STRONG)
                .count();
        }
        
        if (strongDependencies > 3 || totalUsages > 100) {
            return CircularDependencySeverity.HIGH;
        } else if (strongDependencies > 1 || totalUsages > 50) {
            return CircularDependencySeverity.MEDIUM;
        } else {
            return CircularDependencySeverity.LOW;
        }
    }
    
    private List<CircularDependencySolution> suggestSolutions(List<String> cycle, 
                                                             List<PackageDependency> allDependencies) {
        List<CircularDependencySolution> solutions = new ArrayList<>();
        
        // Soluzione 1: Dependency Inversion
        solutions.add(CircularDependencySolution.builder()
            .type(SolutionType.DEPENDENCY_INVERSION)
            .description("Introduce interfaces to break direct dependencies")
            .effort(RefactoringEffort.MEDIUM)
            .build());
            
        // Soluzione 2: Extract Common Package
        solutions.add(CircularDependencySolution.builder()
            .type(SolutionType.EXTRACT_COMMON_PACKAGE)
            .description("Move shared components to a new common package")
            .effort(RefactoringEffort.HIGH)
            .build());
            
        // Soluzione 3: Merge Packages (se il ciclo è piccolo)
        if (cycle.size() == 2) {
            solutions.add(CircularDependencySolution.builder()
                .type(SolutionType.MERGE_PACKAGES)
                .description("Consider merging tightly coupled packages")
                .effort(RefactoringEffort.LOW)
                .build());
        }
        
        return solutions;
    }
}
```

#### 4. Architectural Violation Detector
```java
public class ArchitecturalViolationDetector {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(ArchitecturalViolationDetector.class);
    
    private final List<ArchitecturalRule> standardRules;
    
    public ArchitecturalViolationDetector() {
        this.standardRules = initializeStandardRules();
    }
    
    public List<ArchitecturalViolation> detectViolations(List<PackageDependency> dependencies,
                                                        PackageAnalysisResult packageAnalysis) {
        
        LOGGER.info("Detecting architectural violations");
        List<ArchitecturalViolation> violations = new ArrayList<>();
        
        for (ArchitecturalRule rule : standardRules) {
            violations.addAll(rule.checkViolations(dependencies, packageAnalysis));
        }
        
        // Classifica violazioni per gravità
        violations.sort(Comparator.comparing(ArchitecturalViolation::getSeverity).reversed());
        
        LOGGER.info("Found {} architectural violations", violations.size());
        return violations;
    }
    
    private List<ArchitecturalRule> initializeStandardRules() {
        List<ArchitecturalRule> rules = new ArrayList<>();
        
        // Layered Architecture Rules
        rules.add(new LayerAccessRule());
        rules.add(new NoCircularLayerDependencyRule());
        
        // Package Organization Rules  
        rules.add(new NoUpwardDependencyRule());
        rules.add(new UtilityPackageRule());
        
        // Spring-specific Rules
        rules.add(new ControllerServiceRepositoryRule());
        rules.add(new NoControllerToRepositoryRule());
        
        // General Design Rules
        rules.add(new StableDependencyRule());
        rules.add(new AcyclicDependencyRule());
        
        return rules;
    }
    
    // Example rule implementation
    public static class LayerAccessRule implements ArchitecturalRule {
        private static final Map<LayerType, Set<LayerType>> ALLOWED_DEPENDENCIES = Map.of(
            LayerType.PRESENTATION, Set.of(LayerType.BUSINESS, LayerType.DOMAIN),
            LayerType.BUSINESS, Set.of(LayerType.DATA_ACCESS, LayerType.DOMAIN),
            LayerType.DATA_ACCESS, Set.of(LayerType.DOMAIN),
            LayerType.DOMAIN, Set.of() // Domain should not depend on other layers
        );
        
        @Override
        public List<ArchitecturalViolation> checkViolations(List<PackageDependency> dependencies,
                                                           PackageAnalysisResult packageAnalysis) {
            
            List<ArchitecturalViolation> violations = new ArrayList<>();
            
            Map<LayerType, Set<PackageInfo>> layers = packageAnalysis
                .getOrganizationResult()
                .getLayeringAnalysis()
                .getLayers();
                
            for (PackageDependency dependency : dependencies) {
                LayerType sourceLayer = findPackageLayer(dependency.getSourcePackage(), layers);
                LayerType targetLayer = findPackageLayer(dependency.getTargetPackage(), layers);
                
                if (sourceLayer != null && targetLayer != null) {
                    Set<LayerType> allowedTargets = ALLOWED_DEPENDENCIES.get(sourceLayer);
                    
                    if (allowedTargets != null && !allowedTargets.contains(targetLayer)) {
                        violations.add(ArchitecturalViolation.builder()
                            .ruleType(RuleType.LAYER_ACCESS_VIOLATION)
                            .severity(ViolationSeverity.HIGH)
                            .description(String.format(
                                "Layer %s should not depend on layer %s", 
                                sourceLayer, targetLayer))
                            .sourcePackage(dependency.getSourcePackage())
                            .targetPackage(dependency.getTargetPackage())
                            .dependency(dependency)
                            .build());
                    }
                }
            }
            
            return violations;
        }
        
        private LayerType findPackageLayer(String packageName, Map<LayerType, Set<PackageInfo>> layers) {
            for (Map.Entry<LayerType, Set<PackageInfo>> entry : layers.entrySet()) {
                if (entry.getValue().stream()
                    .anyMatch(pkg -> pkg.getName().equals(packageName))) {
                    return entry.getKey();
                }
            }
            return null;
        }
    }
}
```

### Principi SOLID Applicati
- **SRP**: Builder, extractor, detector separati per responsabilità specifiche
- **OCP**: Facilmente estendibile per nuovi tipi di rules architetturali
- **LSP**: Implementazioni intercambiabili per diversi algoritmi di detection
- **ISP**: Interfacce specifiche per building, extraction, detection
- **DIP**: Dipende da abstractions per graph algorithms e rule systems

### Test Unitari da Implementare
```java
// DependencyGraphBuilderTest.java
@Test
void shouldBuildPackageDependencyGraph() {
    // Arrange
    JarContent jarContent = createJarWithPackageDependencies();
    PackageAnalysisResult packageAnalysis = createPackageAnalysisResult(jarContent);
    DependencyGraphBuilder builder = new JavassistDependencyGraphBuilder();
    
    // Act
    PackageDependencyGraph graph = builder.buildDependencyGraph(jarContent, packageAnalysis);
    
    // Assert
    assertThat(graph.isSuccessful()).isTrue();
    assertThat(graph.getDependencies()).isNotEmpty();
    
    // Verifica dipendenza specifica
    Optional<PackageDependency> controllerToService = graph.findDependency(
        "com.example.controller", "com.example.service"
    );
    assertThat(controllerToService).isPresent();
    assertThat(controllerToService.get().getUsageTypes()).contains(UsageType.METHOD_CALL);
}

@Test
void shouldCalculateDependencyMetrics() {
    JarContent jarContent = createComplexPackageStructure();
    PackageAnalysisResult packageAnalysis = createPackageAnalysisResult(jarContent);
    DependencyGraphBuilder builder = new JavassistDependencyGraphBuilder();
    
    PackageDependencyGraph graph = builder.buildDependencyGraph(jarContent, packageAnalysis);
    
    PackageDependencyMetrics metrics = graph.getMetrics();
    assertThat(metrics.getTotalDependencies()).isGreaterThan(0);
    assertThat(metrics.getAverageAfferentCoupling()).isBetween(0.0, Double.MAX_VALUE);
    assertThat(metrics.getAverageEfferentCoupling()).isBetween(0.0, Double.MAX_VALUE);
}

@Test
void shouldFilterIrrelevantDependencies() {
    JarContent jarContent = createJarWithJdkDependencies();
    PackageAnalysisResult packageAnalysis = createPackageAnalysisResult(jarContent);
    DependencyGraphBuilder builder = new JavassistDependencyGraphBuilder();
    
    PackageDependencyGraph graph = builder.buildDependencyGraph(jarContent, packageAnalysis);
    
    // Verifica che le dipendenze JDK siano filtrate
    boolean hasJdkDependency = graph.getDependencies().stream()
        .anyMatch(dep -> dep.getTargetPackage().startsWith("java."));
    assertThat(hasJdkDependency).isFalse();
}

// CircularDependencyDetectorTest.java
@Test
void shouldDetectSimpleCircularDependency() {
    List<PackageDependency> dependencies = List.of(
        createDependency("com.example.a", "com.example.b"),
        createDependency("com.example.b", "com.example.a")
    );
    
    CircularDependencyDetector detector = new CircularDependencyDetector();
    List<PackageCircularDependency> circular = detector.detectCircularDependencies(
        dependencies, createMockPackageHierarchy()
    );
    
    assertThat(circular).hasSize(1);
    PackageCircularDependency cycle = circular.get(0);
    assertThat(cycle.getInvolvedPackages()).containsExactlyInAnyOrder(
        "com.example.a", "com.example.b"
    );
}

@Test
void shouldDetectComplexCircularDependency() {
    // A -> B -> C -> A (ciclo di 3)
    List<PackageDependency> dependencies = List.of(
        createDependency("com.example.a", "com.example.b"),
        createDependency("com.example.b", "com.example.c"),
        createDependency("com.example.c", "com.example.a")
    );
    
    CircularDependencyDetector detector = new CircularDependencyDetector();
    List<PackageCircularDependency> circular = detector.detectCircularDependencies(
        dependencies, createMockPackageHierarchy()
    );
    
    assertThat(circular).hasSize(1);
    assertThat(circular.get(0).getInvolvedPackages()).hasSize(3);
}

@Test
void shouldCalculateCycleSeverity() {
    List<PackageDependency> strongDependencies = List.of(
        createStrongDependency("com.example.a", "com.example.b", 100),
        createStrongDependency("com.example.b", "com.example.a", 80)
    );
    
    CircularDependencyDetector detector = new CircularDependencyDetector();
    List<PackageCircularDependency> circular = detector.detectCircularDependencies(
        strongDependencies, createMockPackageHierarchy()
    );
    
    assertThat(circular.get(0).getSeverity()).isEqualTo(CircularDependencySeverity.HIGH);
}

// ArchitecturalViolationDetectorTest.java
@Test
void shouldDetectLayerViolations() {
    List<PackageDependency> dependencies = List.of(
        // Violazione: Repository che dipende da Controller
        createDependency("com.example.repository", "com.example.controller")
    );
    
    PackageAnalysisResult packageAnalysis = createLayeredPackageAnalysis();
    ArchitecturalViolationDetector detector = new ArchitecturalViolationDetector();
    
    List<ArchitecturalViolation> violations = detector.detectViolations(dependencies, packageAnalysis);
    
    assertThat(violations).hasSize(1);
    ArchitecturalViolation violation = violations.get(0);
    assertThat(violation.getRuleType()).isEqualTo(RuleType.LAYER_ACCESS_VIOLATION);
    assertThat(violation.getSeverity()).isEqualTo(ViolationSeverity.HIGH);
}

@Test
void shouldDetectSpringArchitecturalViolations() {
    // Controller che accede direttamente al Repository (should go through Service)
    List<PackageDependency> dependencies = List.of(
        createDependency("com.example.controller", "com.example.repository")
    );
    
    PackageAnalysisResult springAnalysis = createSpringPackageAnalysis();
    ArchitecturalViolationDetector detector = new ArchitecturalViolationDetector();
    
    List<ArchitecturalViolation> violations = detector.detectViolations(dependencies, springAnalysis);
    
    assertThat(violations).anyMatch(
        v -> v.getRuleType() == RuleType.NO_CONTROLLER_TO_REPOSITORY
    );
}

// PackageDependencyExtractorTest.java
@Test
void shouldExtractFieldTypeDependencies() {
    ClassInfo classWithFields = createClassWithFieldDependencies();
    JarContent jarContent = JarContent.builder().addClass(classWithFields).build();
    PackageHierarchy hierarchy = createMockPackageHierarchy();
    
    PackageDependencyExtractor extractor = new PackageDependencyExtractor();
    List<PackageDependency> dependencies = extractor.extractDependencies(jarContent, hierarchy);
    
    assertThat(dependencies).anyMatch(
        dep -> dep.getUsageTypes().contains(UsageType.FIELD_TYPE)
    );
}

@Test
void shouldExtractMethodCallDependencies() {
    ClassInfo classWithMethodCalls = createClassWithMethodCallDependencies();
    JarContent jarContent = JarContent.builder().addClass(classWithMethodCalls).build();
    PackageHierarchy hierarchy = createMockPackageHierarchy();
    
    PackageDependencyExtractor extractor = new PackageDependencyExtractor();
    List<PackageDependency> dependencies = extractor.extractDependencies(jarContent, hierarchy);
    
    assertThat(dependencies).anyMatch(
        dep -> dep.getUsageTypes().contains(UsageType.METHOD_CALL)
    );
}

// Integration Test
@Test
void shouldAnalyzeRealWorldPackageDependencies() throws IOException {
    Path springBootJar = getTestResourcePath("real-spring-boot.jar");
    JarContent jarContent = new JarLoader().loadJar(new JarLocation(springBootJar));
    
    PackageAnalysisResult packageAnalysis = new JavassistPackageAnalyzer()
        .analyzePackageStructure(jarContent);
    
    DependencyGraphBuilder builder = new JavassistDependencyGraphBuilder();
    PackageDependencyGraph graph = builder.buildDependencyGraph(jarContent, packageAnalysis);
    
    // Verifiche su applicazione reale
    assertThat(graph.getDependencies()).hasSizeGreaterThan(20);
    assertThat(graph.getMetrics().getTotalDependencies()).isGreaterThan(20);
    
    // Verifica che ci siano dipendenze da controller a service
    boolean hasControllerToService = graph.getDependencies().stream()
        .anyMatch(dep -> dep.getSourcePackage().contains("controller") && 
                        dep.getTargetPackage().contains("service"));
    assertThat(hasControllerToService).isTrue();
}
```

---

## T3.1.5: ConfigurationAnalyzer per @Configuration Classes

### Descrizione Dettagliata
Analyzer specializzato nell'identificazione e analisi delle classi di configurazione Spring, con particolare focus su classi annotate con @Configuration, analisi dei bean definiti, delle proprietà configurate e delle dipendenze di configurazione.

### Scopo dell'Attività
- Identificare tutte le classi @Configuration nell'applicazione
- Analizzare bean definitions e loro scope
- Mappare configuration properties e loro sources
- Rilevare configuration conflicts e duplicazioni
- Tracciare configuration inheritance e composition

### Impatti su Altri Moduli
- **BeanDefinitionAnalyzer**: Input per bean lifecycle analysis
- **PropertyAnalyzer**: Collegamento con application properties
- **DependencyGraphBuilder**: Configuration dependency mapping
- **SpringSecurityAnalyzer**: Security configuration analysis

### Componenti da Implementare

#### 1. Configuration Analyzer Core
```java
public interface ConfigurationAnalyzer {
    ConfigurationAnalysisResult analyzeConfigurations(JarContent jarContent);
}

public class JavassistConfigurationAnalyzer implements ConfigurationAnalyzer {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistConfigurationAnalyzer.class);
    
    private static final Set<String> CONFIGURATION_ANNOTATIONS = Set.of(
        "org.springframework.context.annotation.Configuration",
        "org.springframework.boot.autoconfigure.SpringBootApplication",
        "org.springframework.context.annotation.EnableAutoConfiguration"
    );
    
    private final ConfigurationClassExtractor configExtractor;
    private final BeanDefinitionExtractor beanExtractor;
    private final ConfigurationPropertyExtractor propertyExtractor;
    private final ConfigurationConflictDetector conflictDetector;
    
    public JavassistConfigurationAnalyzer() {
        this.configExtractor = new ConfigurationClassExtractor();
        this.beanExtractor = new BeanDefinitionExtractor();
        this.propertyExtractor = new ConfigurationPropertyExtractor();
        this.conflictDetector = new ConfigurationConflictDetector();
    }
    
    @Override
    public ConfigurationAnalysisResult analyzeConfigurations(JarContent jarContent) {
        LOGGER.startOperation("Configuration analysis");
        long startTime = System.currentTimeMillis();
        
        try {
            ConfigurationAnalysisResult.Builder resultBuilder = ConfigurationAnalysisResult.builder();
            
            // Trova tutte le classi di configurazione
            List<ConfigurationClass> configClasses = configExtractor.extractConfigurationClasses(jarContent);
            resultBuilder.configurationClasses(configClasses);
            
            LOGGER.info("Found {} configuration classes", configClasses.size());
            
            // Estrai bean definitions da ogni configuration class
            List<ConfigurationBeanDefinition> beanDefinitions = new ArrayList<>();
            for (ConfigurationClass configClass : configClasses) {
                List<ConfigurationBeanDefinition> classBeans = beanExtractor.extractBeanDefinitions(
                    configClass, jarContent);
                beanDefinitions.addAll(classBeans);
            }
            resultBuilder.beanDefinitions(beanDefinitions);
            
            LOGGER.info("Extracted {} bean definitions", beanDefinitions.size());
            
            // Analizza configuration properties
            ConfigurationPropertyMapping propertyMapping = propertyExtractor.extractProperties(
                configClasses, jarContent);
            resultBuilder.propertyMapping(propertyMapping);
            
            // Costruisci configuration dependency graph
            ConfigurationDependencyGraph dependencyGraph = buildConfigurationDependencyGraph(
                configClasses, beanDefinitions);
            resultBuilder.dependencyGraph(dependencyGraph);
            
            // Rileva conflicts e problemi
            List<ConfigurationConflict> conflicts = conflictDetector.detectConflicts(
                configClasses, beanDefinitions, propertyMapping);
            resultBuilder.conflicts(conflicts);
            
            // Calcola metriche di configurazione
            ConfigurationMetrics metrics = calculateConfigurationMetrics(
                configClasses, beanDefinitions, propertyMapping);
            resultBuilder.metrics(metrics);
            
            return resultBuilder
                .analysisMetadata(AnalysisMetadata.successful(System.currentTimeMillis() - startTime))
                .build();
                
        } catch (Exception e) {
            LOGGER.error("Configuration analysis failed", e);
            return ConfigurationAnalysisResult.failed("Analysis failed: " + e.getMessage());
        } finally {
            LOGGER.endOperation("Configuration analysis", System.currentTimeMillis() - startTime);
        }
    }
    
    private ConfigurationDependencyGraph buildConfigurationDependencyGraph(
            List<ConfigurationClass> configClasses,
            List<ConfigurationBeanDefinition> beanDefinitions) {
        
        ConfigurationDependencyGraph.Builder graphBuilder = ConfigurationDependencyGraph.builder();
        
        // Analizza import dependencies
        for (ConfigurationClass configClass : configClasses) {
            for (String importedClass : configClass.getImportedClasses()) {
                if (isConfigurationClass(importedClass, configClasses)) {
                    graphBuilder.addConfigurationDependency(
                        configClass.getClassName(), importedClass, DependencyType.IMPORT
                    );
                }
            }
        }
        
        // Analizza bean dependencies
        for (ConfigurationBeanDefinition beanDef : beanDefinitions) {
            for (String dependency : beanDef.getDependencies()) {
                // Trova la configuration class che definisce la dependency
                Optional<ConfigurationClass> dependencySource = findBeanSourceConfiguration(
                    dependency, beanDefinitions, configClasses);
                
                if (dependencySource.isPresent() && 
                    !dependencySource.get().getClassName().equals(beanDef.getSourceConfiguration())) {
                    
                    graphBuilder.addConfigurationDependency(
                        beanDef.getSourceConfiguration(),
                        dependencySource.get().getClassName(),
                        DependencyType.BEAN_DEPENDENCY
                    );
                }
            }
        }
        
        return graphBuilder.build();
    }
}
```

Continuo con il resto dei task per la Fase 3 negli prossimi file...
```
