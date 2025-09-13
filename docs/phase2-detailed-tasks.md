# Fase 2: Entrypoint Analysis & Spring Boot Detection - Task Dettagliati

## T2.1.1: SpringBootDetector per Identificazione App Spring Boot

### Descrizione Dettagliata
Implementazione di un rilevatore intelligente che analizza il JAR per identificare se si tratta di un'applicazione Spring Boot mediante analisi di classi, annotazioni, dependencies e structure del JAR stesso.

### Scopo dell'Attività
- Distinguere tra JAR Spring Boot e JAR standard
- Abilitare analisi specifiche per Spring Boot
- Configurare strategy pattern basato sul tipo di applicazione

### Impatti su Altri Moduli
- **AnalyzerFactory**: Seleziona analyzer specifici per Spring Boot
- **ReportStrategy**: Abilita report specifici Spring Boot
- **UI Module**: Mostra informazioni specifiche del framework

### Componenti da Implementare

#### 1. Spring Boot Detection Engine
```java
public interface SpringBootDetector {
    SpringBootDetectionResult detectSpringBoot(JarContent jarContent);
}

public class JavassistSpringBootDetector implements SpringBootDetector {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistSpringBootDetector.class);
    
    private final Set<SpringBootIndicator> indicators;
    
    public JavassistSpringBootDetector() {
        this.indicators = Set.of(
            new SpringBootMainClassIndicator(),
            new SpringBootAnnotationIndicator(),
            new SpringBootDependencyIndicator(),
            new SpringBootJarStructureIndicator()
        );
    }
    
    @Override
    public SpringBootDetectionResult detectSpringBoot(JarContent jarContent) {
        LOGGER.startOperation("Spring Boot Detection");
        
        SpringBootDetectionResult.Builder resultBuilder = SpringBootDetectionResult.builder();
        
        for (SpringBootIndicator indicator : indicators) {
            IndicatorResult result = indicator.analyze(jarContent);
            resultBuilder.addIndicatorResult(indicator.getType(), result);
            
            LOGGER.debug("Indicator %s: confidence=%.2f, evidence=%s", 
                indicator.getType(), result.getConfidence(), result.getEvidence());
        }
        
        SpringBootDetectionResult finalResult = resultBuilder.build();
        LOGGER.info("Spring Boot detection completed: isSpringBoot=%s, confidence=%.2f", 
            finalResult.isSpringBootApplication(), finalResult.getOverallConfidence());
        
        return finalResult;
    }
}
```

#### 2. Spring Boot Indicators (Strategy Pattern)
```java
public interface SpringBootIndicator {
    IndicatorResult analyze(JarContent jarContent);
    SpringBootIndicatorType getType();
    double getWeight(); // Per weighted confidence calculation
}

public class SpringBootMainClassIndicator implements SpringBootIndicator {
    private static final String SPRING_APPLICATION_CLASS = "org.springframework.boot.SpringApplication";
    
    @Override
    public IndicatorResult analyze(JarContent jarContent) {
        try {
            Optional<ClassInfo> mainClass = findMainClass(jarContent);
            if (mainClass.isEmpty()) {
                return IndicatorResult.notFound("No main class found");
            }
            
            boolean callsSpringApplication = analyzesSpringApplicationUsage(mainClass.get());
            double confidence = callsSpringApplication ? 0.9 : 0.1;
            
            return IndicatorResult.builder()
                .confidence(confidence)
                .evidence(createEvidence(mainClass.get(), callsSpringApplication))
                .build();
                
        } catch (Exception e) {
            LOGGER.error("Error analyzing main class indicator", e);
            return IndicatorResult.error("Analysis failed: " + e.getMessage());
        }
    }
    
    private boolean analyzesSpringApplicationUsage(ClassInfo classInfo) {
        // Analizza bytecode per chiamate a SpringApplication.run()
        return classInfo.getMethods().stream()
            .filter(method -> "main".equals(method.getName()))
            .anyMatch(this::containsSpringApplicationCall);
    }
    
    private boolean containsSpringApplicationCall(MethodInfo method) {
        // Usa Javassist per analizzare il bytecode del metodo main
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.get(method.getDeclaringClass().getFullyQualifiedName());
            CtMethod ctMethod = ctClass.getDeclaredMethod("main");
            
            CodeAttribute codeAttribute = ctMethod.getMethodInfo().getCodeAttribute();
            CodeIterator iterator = codeAttribute.iterator();
            
            while (iterator.hasNext()) {
                int index = iterator.next();
                int opcode = iterator.byteAt(index);
                
                if (opcode == Opcode.INVOKESTATIC) {
                    int constIndex = iterator.u16bitAt(index + 1);
                    String methodRef = getMethodReference(ctClass, constIndex);
                    
                    if (methodRef.contains("SpringApplication.run")) {
                        return true;
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            LOGGER.error("Error analyzing method bytecode", e);
            return false;
        }
    }
}

public class SpringBootAnnotationIndicator implements SpringBootIndicator {
    private static final Set<String> SPRING_BOOT_ANNOTATIONS = Set.of(
        "org.springframework.boot.autoconfigure.SpringBootApplication",
        "org.springframework.boot.autoconfigure.EnableAutoConfiguration",
        "org.springframework.context.annotation.ComponentScan"
    );
    
    @Override
    public IndicatorResult analyze(JarContent jarContent) {
        Set<String> foundAnnotations = new HashSet<>();
        double confidence = 0.0;
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            for (AnnotationInfo annotation : classInfo.getAnnotations()) {
                String annotationType = annotation.getType();
                if (SPRING_BOOT_ANNOTATIONS.contains(annotationType)) {
                    foundAnnotations.add(annotationType);
                    confidence += getAnnotationWeight(annotationType);
                }
            }
        }
        
        confidence = Math.min(confidence, 1.0); // Cap at 1.0
        
        return IndicatorResult.builder()
            .confidence(confidence)
            .evidence(Map.of("foundAnnotations", foundAnnotations))
            .build();
    }
    
    private double getAnnotationWeight(String annotationType) {
        return switch (annotationType) {
            case "org.springframework.boot.autoconfigure.SpringBootApplication" -> 0.8;
            case "org.springframework.boot.autoconfigure.EnableAutoConfiguration" -> 0.6;
            case "org.springframework.context.annotation.ComponentScan" -> 0.3;
            default -> 0.1;
        };
    }
}
```

#### 3. Detection Result Model
```java
public class SpringBootDetectionResult {
    private final boolean isSpringBootApplication;
    private final double overallConfidence;
    private final SpringBootVersion detectedVersion;
    private final Map<SpringBootIndicatorType, IndicatorResult> indicatorResults;
    private final Set<String> detectedFeatures;
    
    // Builder pattern implementation
    public static class Builder {
        private Map<SpringBootIndicatorType, IndicatorResult> indicatorResults = new HashMap<>();
        
        public Builder addIndicatorResult(SpringBootIndicatorType type, IndicatorResult result) {
            this.indicatorResults.put(type, result);
            return this;
        }
        
        public SpringBootDetectionResult build() {
            double weightedConfidence = calculateWeightedConfidence();
            boolean isSpringBoot = weightedConfidence > 0.6; // Threshold
            
            return new SpringBootDetectionResult(
                isSpringBoot,
                weightedConfidence,
                detectVersion(),
                indicatorResults,
                extractFeatures()
            );
        }
        
        private double calculateWeightedConfidence() {
            double totalWeight = 0.0;
            double weightedSum = 0.0;
            
            for (Map.Entry<SpringBootIndicatorType, IndicatorResult> entry : indicatorResults.entrySet()) {
                double weight = entry.getKey().getWeight();
                double confidence = entry.getValue().getConfidence();
                
                totalWeight += weight;
                weightedSum += weight * confidence;
            }
            
            return totalWeight > 0 ? weightedSum / totalWeight : 0.0;
        }
    }
}
```

### Principi SOLID Applicati
- **SRP**: Ogni indicator ha responsabilità specifica di detection
- **OCP**: Facilmente estendibile con nuovi indicator
- **ISP**: Interfacce small e focused
- **DIP**: Detector dipende da abstractions, non da implementations

### Test Unitari da Implementare
```java
// SpringBootDetectorTest.java
@Test
void shouldDetectSpringBootApplication() {
    // Arrange
    JarContent springBootJar = createMockSpringBootJar();
    SpringBootDetector detector = new JavassistSpringBootDetector();
    
    // Act
    SpringBootDetectionResult result = detector.detectSpringBoot(springBootJar);
    
    // Assert
    assertThat(result.isSpringBootApplication()).isTrue();
    assertThat(result.getOverallConfidence()).isGreaterThan(0.6);
    assertThat(result.getDetectedVersion()).isNotNull();
}

@Test
void shouldNotDetectRegularJarAsSpringBoot() {
    JarContent regularJar = createMockRegularJar();
    SpringBootDetector detector = new JavassistSpringBootDetector();
    
    SpringBootDetectionResult result = detector.detectSpringBoot(regularJar);
    
    assertThat(result.isSpringBootApplication()).isFalse();
    assertThat(result.getOverallConfidence()).isLessThan(0.5);
}

// SpringBootMainClassIndicatorTest.java
@Test
void shouldDetectSpringApplicationRunCall() {
    // Create mock ClassInfo with main method containing SpringApplication.run()
    ClassInfo mockClass = createMockMainClassWithSpringApplicationRun();
    JarContent jarContent = JarContent.builder().addClass(mockClass).build();
    
    SpringBootMainClassIndicator indicator = new SpringBootMainClassIndicator();
    IndicatorResult result = indicator.analyze(jarContent);
    
    assertThat(result.getConfidence()).isEqualTo(0.9);
    assertThat(result.getEvidence()).containsKey("mainClass");
}

@Test
void shouldHandleMissingMainClass() {
    JarContent emptyJar = JarContent.builder().build();
    SpringBootMainClassIndicator indicator = new SpringBootMainClassIndicator();
    
    IndicatorResult result = indicator.analyze(emptyJar);
    
    assertThat(result.getConfidence()).isEqualTo(0.0);
    assertThat(result.getEvidence()).containsEntry("reason", "No main class found");
}

// SpringBootAnnotationIndicatorTest.java
@Test
void shouldDetectSpringBootApplicationAnnotation() {
    ClassInfo mockClass = createClassWithAnnotation("org.springframework.boot.autoconfigure.SpringBootApplication");
    JarContent jarContent = JarContent.builder().addClass(mockClass).build();
    
    SpringBootAnnotationIndicator indicator = new SpringBootAnnotationIndicator();
    IndicatorResult result = indicator.analyze(jarContent);
    
    assertThat(result.getConfidence()).isEqualTo(0.8);
    Set<String> foundAnnotations = (Set<String>) result.getEvidence().get("foundAnnotations");
    assertThat(foundAnnotations).contains("org.springframework.boot.autoconfigure.SpringBootApplication");
}
```

---

## T2.1.2: MainMethodAnalyzer per Finding SpringApplication.run()

### Descrizione Dettagliata
Analyzer specializzato nell'identificazione e analisi del metodo main nelle applicazioni Spring Boot, con particolare focus sulla chiamata SpringApplication.run() e sui parametri passati.

### Scopo dell'Attività
- Identificare entry point delle applicazioni Spring Boot
- Analizzare parametri di configurazione passati a SpringApplication
- Estrarre informazioni di bootstrap dell'applicazione

### Impatti su Altri Moduli
- **Bootstrap Analyzer**: Fornisce entry point per analisi del ciclo di vita
- **Configuration Analyzer**: Identifica classi di configurazione principali
- **Report Generator**: Dati per report di entry point

### Componenti da Implementare

#### 1. Main Method Analyzer
```java
public interface MainMethodAnalyzer {
    MainMethodAnalysisResult analyzeMainMethod(JarContent jarContent);
}

public class JavassistMainMethodAnalyzer implements MainMethodAnalyzer {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistMainMethodAnalyzer.class);
    
    @Override
    public MainMethodAnalysisResult analyzeMainMethod(JarContent jarContent) {
        LOGGER.startOperation("Main method analysis");
        
        try {
            List<MainMethodInfo> mainMethods = findAllMainMethods(jarContent);
            Optional<MainMethodInfo> springBootMain = identifySpringBootMain(mainMethods);
            
            if (springBootMain.isPresent()) {
                SpringApplicationCallInfo callInfo = analyzeSpringApplicationCall(springBootMain.get());
                return MainMethodAnalysisResult.springBootMain(springBootMain.get(), callInfo);
            } else if (!mainMethods.isEmpty()) {
                return MainMethodAnalysisResult.regularMain(mainMethods.get(0));
            } else {
                return MainMethodAnalysisResult.noMainFound();
            }
            
        } finally {
            LOGGER.endOperation("Main method analysis", System.currentTimeMillis());
        }
    }
    
    private List<MainMethodInfo> findAllMainMethods(JarContent jarContent) {
        return jarContent.getClasses().stream()
            .flatMap(classInfo -> classInfo.getMethods().stream())
            .filter(this::isMainMethod)
            .map(this::createMainMethodInfo)
            .collect(Collectors.toList());
    }
    
    private boolean isMainMethod(MethodInfo method) {
        return "main".equals(method.getName()) &&
               method.isStatic() &&
               method.isPublic() &&
               method.getParameters().size() == 1 &&
               "java.lang.String[]".equals(method.getParameters().get(0).getType());
    }
    
    private Optional<MainMethodInfo> identifySpringBootMain(List<MainMethodInfo> mainMethods) {
        return mainMethods.stream()
            .filter(this::containsSpringApplicationCall)
            .findFirst();
    }
    
    private boolean containsSpringApplicationCall(MainMethodInfo mainMethod) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.get(mainMethod.getDeclaringClass().getFullyQualifiedName());
            CtMethod ctMethod = ctClass.getDeclaredMethod("main", new CtClass[]{pool.get("java.lang.String[]")});
            
            return analyzeMethodBytecode(ctMethod);
            
        } catch (Exception e) {
            LOGGER.error("Error analyzing main method bytecode", e);
            return false;
        }
    }
    
    private boolean analyzeMethodBytecode(CtMethod method) throws BadBytecode {
        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        if (codeAttribute == null) return false;
        
        CodeIterator iterator = codeAttribute.iterator();
        
        while (iterator.hasNext()) {
            int index = iterator.next();
            int opcode = iterator.byteAt(index);
            
            if (opcode == Opcode.INVOKESTATIC) {
                String methodRef = getInvokedMethod(method.getDeclaringClass(), iterator, index);
                if (isSpringApplicationRunCall(methodRef)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean isSpringApplicationRunCall(String methodRef) {
        return methodRef.contains("org.springframework.boot.SpringApplication") &&
               methodRef.contains("run");
    }
}
```

#### 2. Spring Application Call Analyzer
```java
public class SpringApplicationCallAnalyzer {
    
    public SpringApplicationCallInfo analyzeCall(MainMethodInfo mainMethod) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.get(mainMethod.getDeclaringClass().getFullyQualifiedName());
            CtMethod ctMethod = ctClass.getDeclaredMethod("main", new CtClass[]{pool.get("java.lang.String[]")});
            
            return extractCallInformation(ctMethod);
            
        } catch (Exception e) {
            LOGGER.error("Error analyzing SpringApplication call", e);
            return SpringApplicationCallInfo.unknown();
        }
    }
    
    private SpringApplicationCallInfo extractCallInformation(CtMethod method) throws BadBytecode {
        SpringApplicationCallInfo.Builder builder = SpringApplicationCallInfo.builder();
        
        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        CodeIterator iterator = codeAttribute.iterator();
        
        while (iterator.hasNext()) {
            int index = iterator.next();
            int opcode = iterator.byteAt(index);
            
            switch (opcode) {
                case Opcode.LDC -> {
                    String constant = getStringConstant(method.getDeclaringClass(), iterator, index);
                    if (isConfigurationClass(constant)) {
                        builder.addSourceClass(constant);
                    }
                }
                case Opcode.INVOKESTATIC -> {
                    String methodRef = getInvokedMethod(method.getDeclaringClass(), iterator, index);
                    if (isSpringApplicationRunCall(methodRef)) {
                        builder.callType(extractCallType(methodRef));
                        builder.argumentCount(extractArgumentCount(methodRef));
                    }
                }
                case Opcode.ANEWARRAY -> {
                    // Array creation for multiple source classes
                    builder.hasMultipleSourceClasses(true);
                }
            }
        }
        
        return builder.build();
    }
    
    private boolean isConfigurationClass(String className) {
        // Euristica per identificare classi di configurazione
        return className != null && 
               (className.endsWith("Application") || 
                className.endsWith("Config") ||
                className.contains("Configuration"));
    }
}
```

#### 3. Result Models
```java
public class MainMethodAnalysisResult {
    private final MainMethodType type;
    private final Optional<MainMethodInfo> mainMethod;
    private final Optional<SpringApplicationCallInfo> springApplicationCall;
    private final AnalysisMetadata metadata;
    
    public static MainMethodAnalysisResult springBootMain(MainMethodInfo mainMethod, 
                                                         SpringApplicationCallInfo callInfo) {
        return new MainMethodAnalysisResult(
            MainMethodType.SPRING_BOOT,
            Optional.of(mainMethod),
            Optional.of(callInfo),
            AnalysisMetadata.successful()
        );
    }
    
    public static MainMethodAnalysisResult regularMain(MainMethodInfo mainMethod) {
        return new MainMethodAnalysisResult(
            MainMethodType.REGULAR,
            Optional.of(mainMethod),
            Optional.empty(),
            AnalysisMetadata.successful()
        );
    }
    
    public static MainMethodAnalysisResult noMainFound() {
        return new MainMethodAnalysisResult(
            MainMethodType.NONE,
            Optional.empty(),
            Optional.empty(),
            AnalysisMetadata.warning("No main method found")
        );
    }
}

public class SpringApplicationCallInfo {
    private final SpringApplicationCallType callType;
    private final List<String> sourceClasses;
    private final int argumentCount;
    private final Map<String, Object> additionalProperties;
    private final boolean hasCustomConfiguration;
    
    public static class Builder {
        private SpringApplicationCallType callType = SpringApplicationCallType.STANDARD;
        private Set<String> sourceClasses = new HashSet<>();
        private int argumentCount = 0;
        private Map<String, Object> additionalProperties = new HashMap<>();
        private boolean hasMultipleSourceClasses = false;
        
        public Builder callType(SpringApplicationCallType callType) {
            this.callType = callType;
            return this;
        }
        
        public Builder addSourceClass(String className) {
            this.sourceClasses.add(className);
            return this;
        }
        
        public SpringApplicationCallInfo build() {
            return new SpringApplicationCallInfo(
                callType,
                new ArrayList<>(sourceClasses),
                argumentCount,
                Map.copyOf(additionalProperties),
                hasMultipleSourceClasses || sourceClasses.size() > 1
            );
        }
    }
}
```

### Principi SOLID Applicati
- **SRP**: Separate analyzer per main method e SpringApplication call
- **OCP**: Estendibile per nuovi tipi di main method patterns
- **LSP**: Implementazioni intercambiabili via interfaces
- **DIP**: Dipende da abstractions del bytecode analysis

### Test Unitari da Implementare
```java
// MainMethodAnalyzerTest.java
@Test
void shouldFindSpringBootMainMethod() {
    // Arrange
    ClassInfo mainClass = createClassWithSpringBootMain();
    JarContent jarContent = JarContent.builder().addClass(mainClass).build();
    MainMethodAnalyzer analyzer = new JavassistMainMethodAnalyzer();
    
    // Act
    MainMethodAnalysisResult result = analyzer.analyzeMainMethod(jarContent);
    
    // Assert
    assertThat(result.getType()).isEqualTo(MainMethodType.SPRING_BOOT);
    assertThat(result.getMainMethod()).isPresent();
    assertThat(result.getSpringApplicationCall()).isPresent();
}

@Test
void shouldIdentifyRegularMainMethod() {
    ClassInfo mainClass = createClassWithRegularMain();
    JarContent jarContent = JarContent.builder().addClass(mainClass).build();
    MainMethodAnalyzer analyzer = new JavassistMainMethodAnalyzer();
    
    MainMethodAnalysisResult result = analyzer.analyzeMainMethod(jarContent);
    
    assertThat(result.getType()).isEqualTo(MainMethodType.REGULAR);
    assertThat(result.getMainMethod()).isPresent();
    assertThat(result.getSpringApplicationCall()).isEmpty();
}

@Test
void shouldHandleJarWithoutMainMethod() {
    JarContent jarContent = createJarWithoutMainMethod();
    MainMethodAnalyzer analyzer = new JavassistMainMethodAnalyzer();
    
    MainMethodAnalysisResult result = analyzer.analyzeMainMethod(jarContent);
    
    assertThat(result.getType()).isEqualTo(MainMethodType.NONE);
    assertThat(result.getMainMethod()).isEmpty();
}

// SpringApplicationCallAnalyzerTest.java
@Test
void shouldExtractSourceClasses() {
    MainMethodInfo mainMethod = createMainMethodWithSourceClasses("com.example.Application", "com.example.Config");
    SpringApplicationCallAnalyzer analyzer = new SpringApplicationCallAnalyzer();
    
    SpringApplicationCallInfo callInfo = analyzer.analyzeCall(mainMethod);
    
    assertThat(callInfo.getSourceClasses()).containsExactlyInAnyOrder(
        "com.example.Application", 
        "com.example.Config"
    );
    assertThat(callInfo.hasCustomConfiguration()).isTrue();
}

@Test
void shouldIdentifyStandardSpringApplicationRun() {
    MainMethodInfo mainMethod = createStandardSpringBootMain();
    SpringApplicationCallAnalyzer analyzer = new SpringApplicationCallAnalyzer();
    
    SpringApplicationCallInfo callInfo = analyzer.analyzeCall(mainMethod);
    
    assertThat(callInfo.getCallType()).isEqualTo(SpringApplicationCallType.STANDARD);
    assertThat(callInfo.getArgumentCount()).isEqualTo(2); // Class and args
}

// Integration Test
@Test
void shouldAnalyzeRealSpringBootJar() throws IOException {
    Path springBootJar = getTestResourcePath("sample-spring-boot.jar");
    JarContent jarContent = new JarLoader().loadJar(new JarLocation(springBootJar));
    MainMethodAnalyzer analyzer = new JavassistMainMethodAnalyzer();
    
    MainMethodAnalysisResult result = analyzer.analyzeMainMethod(jarContent);
    
    assertThat(result.getType()).isEqualTo(MainMethodType.SPRING_BOOT);
    SpringApplicationCallInfo callInfo = result.getSpringApplicationCall().orElseThrow();
    assertThat(callInfo.getSourceClasses()).isNotEmpty();
}
```

---

## T2.1.3: ComponentScanAnalyzer per @ComponentScan

### Descrizione Dettagliata
Analyzer per identificare e analizzare le configurazioni di component scanning in applicazioni Spring Boot, inclusa l'identificazione dei package base, filtri di inclusione/esclusione e strategie di scanning.

### Scopo dell'Attività
- Mappare la strategia di component scanning dell'applicazione
- Identificare package e classi automaticamente scansionati
- Rilevare configurazioni custom di component scanning

### Impatti su Altri Moduli
- **Bean Creation Analyzer**: Identifica scope di scanning per bean discovery
- **Dependency Injection Analyzer**: Comprende quali classi sono candidate per injection
- **Package Analyzer**: Mappa relazioni tra package configuration

### Componenti da Implementare

#### 1. Component Scan Analyzer
```java
public interface ComponentScanAnalyzer {
    ComponentScanAnalysisResult analyzeComponentScan(JarContent jarContent);
}

public class JavassistComponentScanAnalyzer implements ComponentScanAnalyzer {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistComponentScanAnalyzer.class);
    
    private static final Set<String> COMPONENT_SCAN_ANNOTATIONS = Set.of(
        "org.springframework.context.annotation.ComponentScan",
        "org.springframework.context.annotation.ComponentScans",
        "org.springframework.boot.autoconfigure.SpringBootApplication"
    );
    
    @Override
    public ComponentScanAnalysisResult analyzeComponentScan(JarContent jarContent) {
        LOGGER.startOperation("Component scan analysis");
        
        ComponentScanAnalysisResult.Builder resultBuilder = ComponentScanAnalysisResult.builder();
        
        try {
            // Trova tutte le classi con annotazioni di component scan
            List<ComponentScanConfiguration> configurations = findComponentScanConfigurations(jarContent);
            
            // Analizza ogni configurazione
            for (ComponentScanConfiguration config : configurations) {
                ComponentScanDetails details = analyzeConfiguration(config);
                resultBuilder.addConfiguration(details);
            }
            
            // Calcola package scanning effettivo
            Set<String> effectivePackages = calculateEffectivePackages(configurations, jarContent);
            resultBuilder.effectivePackages(effectivePackages);
            
            // Identifica conflitti o sovrapposizioni
            List<ComponentScanConflict> conflicts = detectConflicts(configurations);
            resultBuilder.conflicts(conflicts);
            
            return resultBuilder.build();
            
        } finally {
            LOGGER.endOperation("Component scan analysis", System.currentTimeMillis());
        }
    }
    
    private List<ComponentScanConfiguration> findComponentScanConfigurations(JarContent jarContent) {
        return jarContent.getClasses().stream()
            .filter(this::hasComponentScanAnnotation)
            .map(this::extractConfiguration)
            .collect(Collectors.toList());
    }
    
    private boolean hasComponentScanAnnotation(ClassInfo classInfo) {
        return classInfo.getAnnotations().stream()
            .anyMatch(annotation -> COMPONENT_SCAN_ANNOTATIONS.contains(annotation.getType()));
    }
    
    private ComponentScanConfiguration extractConfiguration(ClassInfo classInfo) {
        ComponentScanConfiguration.Builder builder = ComponentScanConfiguration.builder()
            .sourceClass(classInfo.getFullyQualifiedName());
        
        for (AnnotationInfo annotation : classInfo.getAnnotations()) {
            if (COMPONENT_SCAN_ANNOTATIONS.contains(annotation.getType())) {
                extractAnnotationDetails(annotation, builder);
            }
        }
        
        return builder.build();
    }
    
    private void extractAnnotationDetails(AnnotationInfo annotation, ComponentScanConfiguration.Builder builder) {
        Map<String, Object> attributes = annotation.getAttributes();
        
        // Base packages
        Object basePackages = attributes.get("basePackages");
        if (basePackages instanceof String[]) {
            builder.basePackages(Arrays.asList((String[]) basePackages));
        }
        
        // Base package classes
        Object basePackageClasses = attributes.get("basePackageClasses");
        if (basePackageClasses instanceof Class[]) {
            Class<?>[] classes = (Class<?>[]) basePackageClasses;
            List<String> packageNames = Arrays.stream(classes)
                .map(clazz -> clazz.getPackage().getName())
                .collect(Collectors.toList());
            builder.basePackageClasses(packageNames);
        }
        
        // Include filters
        Object includeFilters = attributes.get("includeFilters");
        if (includeFilters != null) {
            builder.includeFilters(extractFilters(includeFilters));
        }
        
        // Exclude filters
        Object excludeFilters = attributes.get("excludeFilters");
        if (excludeFilters != null) {
            builder.excludeFilters(extractFilters(excludeFilters));
        }
        
        // Lazy init
        Boolean lazyInit = (Boolean) attributes.get("lazyInit");
        if (lazyInit != null) {
            builder.lazyInit(lazyInit);
        }
    }
}
```

#### 2. Component Scan Details Analyzer
```java
public class ComponentScanDetailsAnalyzer {
    
    public ComponentScanDetails analyzeConfiguration(ComponentScanConfiguration config) {
        ComponentScanDetails.Builder builder = ComponentScanDetails.builder()
            .configuration(config);
        
        // Calcola package effettivi da scansionare
        Set<String> effectivePackages = calculateEffectivePackages(config);
        builder.effectivePackages(effectivePackages);
        
        // Analizza filtri
        FilterAnalysisResult filterResult = analyzeFilters(config);
        builder.filterAnalysis(filterResult);
        
        // Stima numero di classi che verranno scansionate
        ComponentScanImpact impact = estimateImpact(effectivePackages, config);
        builder.impact(impact);
        
        return builder.build();
    }
    
    private Set<String> calculateEffectivePackages(ComponentScanConfiguration config) {
        Set<String> packages = new HashSet<>();
        
        // Base packages specificati esplicitamente
        packages.addAll(config.getBasePackages());
        
        // Package derivati da base package classes
        packages.addAll(config.getBasePackageClasses());
        
        // Se nessun package specificato, usa il package della classe di configurazione
        if (packages.isEmpty()) {
            String sourcePackage = extractPackageName(config.getSourceClass());
            packages.add(sourcePackage);
        }
        
        return packages;
    }
    
    private FilterAnalysisResult analyzeFilters(ComponentScanConfiguration config) {
        FilterAnalysisResult.Builder builder = FilterAnalysisResult.builder();
        
        // Analizza include filters
        for (ComponentScanFilter filter : config.getIncludeFilters()) {
            FilterImpact impact = analyzeFilter(filter, true);
            builder.addIncludeImpact(impact);
        }
        
        // Analizza exclude filters
        for (ComponentScanFilter filter : config.getExcludeFilters()) {
            FilterImpact impact = analyzeFilter(filter, false);
            builder.addExcludeImpact(impact);
        }
        
        return builder.build();
    }
    
    private FilterImpact analyzeFilter(ComponentScanFilter filter, boolean isInclude) {
        return switch (filter.getType()) {
            case ANNOTATION -> analyzeAnnotationFilter(filter, isInclude);
            case ASSIGNABLE_TYPE -> analyzeAssignableTypeFilter(filter, isInclude);
            case ASPECTJ -> analyzeAspectJFilter(filter, isInclude);
            case REGEX -> analyzeRegexFilter(filter, isInclude);
            case CUSTOM -> analyzeCustomFilter(filter, isInclude);
        };
    }
    
    private ComponentScanImpact estimateImpact(Set<String> packages, ComponentScanConfiguration config) {
        ComponentScanImpact.Builder builder = ComponentScanImpact.builder();
        
        // Stima basata sui package
        int estimatedClasses = packages.size() * 10; // Euristica: 10 classi per package
        builder.estimatedClassesScanned(estimatedClasses);
        
        // Considera i filtri
        if (!config.getExcludeFilters().isEmpty()) {
            estimatedClasses = (int) (estimatedClasses * 0.8); // Riduzione per exclude filters
        }
        if (!config.getIncludeFilters().isEmpty()) {
            estimatedClasses = (int) (estimatedClasses * 0.6); // Riduzione per include filters specifici
        }
        
        builder.estimatedBeansCreated(estimatedClasses / 2); // Euristica: 50% delle classi diventano bean
        
        // Performance impact
        PerformanceImpact perfImpact = estimatePerformanceImpact(packages.size(), config);
        builder.performanceImpact(perfImpact);
        
        return builder.build();
    }
}
```

#### 3. Conflict Detection
```java
public class ComponentScanConflictDetector {
    
    public List<ComponentScanConflict> detectConflicts(List<ComponentScanConfiguration> configurations) {
        List<ComponentScanConflict> conflicts = new ArrayList<>();
        
        // Rileva sovrapposizioni di package
        conflicts.addAll(detectPackageOverlaps(configurations));
        
        // Rileva filtri contrastanti
        conflicts.addAll(detectFilterConflicts(configurations));
        
        // Rileva configurazioni ridondanti
        conflicts.addAll(detectRedundantConfigurations(configurations));
        
        return conflicts;
    }
    
    private List<ComponentScanConflict> detectPackageOverlaps(List<ComponentScanConfiguration> configurations) {
        List<ComponentScanConflict> conflicts = new ArrayList<>();
        
        for (int i = 0; i < configurations.size(); i++) {
            for (int j = i + 1; j < configurations.size(); j++) {
                ComponentScanConfiguration config1 = configurations.get(i);
                ComponentScanConfiguration config2 = configurations.get(j);
                
                Set<String> overlap = findPackageOverlap(config1, config2);
                if (!overlap.isEmpty()) {
                    conflicts.add(ComponentScanConflict.packageOverlap(config1, config2, overlap));
                }
            }
        }
        
        return conflicts;
    }
    
    private Set<String> findPackageOverlap(ComponentScanConfiguration config1, ComponentScanConfiguration config2) {
        Set<String> packages1 = getAllEffectivePackages(config1);
        Set<String> packages2 = getAllEffectivePackages(config2);
        
        return packages1.stream()
            .filter(pkg1 -> packages2.stream().anyMatch(pkg2 -> isPackageOverlap(pkg1, pkg2)))
            .collect(Collectors.toSet());
    }
    
    private boolean isPackageOverlap(String package1, String package2) {
        return package1.startsWith(package2) || package2.startsWith(package1);
    }
}
```

### Principi SOLID Applicati
- **SRP**: Analyzer separati per configuration extraction, conflict detection, impact analysis
- **OCP**: Facilmente estendibile per nuovi tipi di filtri
- **ISP**: Interfacce specifiche per diversi aspetti dell'analisi
- **DIP**: Dipende da abstractions per filter analysis

### Test Unitari da Implementare
```java
// ComponentScanAnalyzerTest.java
@Test
void shouldDetectComponentScanAnnotation() {
    ClassInfo configClass = createClassWithComponentScan(
        basePackages = {"com.example.service", "com.example.repository"}
    );
    JarContent jarContent = JarContent.builder().addClass(configClass).build();
    ComponentScanAnalyzer analyzer = new JavassistComponentScanAnalyzer();
    
    ComponentScanAnalysisResult result = analyzer.analyzeComponentScan(jarContent);
    
    assertThat(result.getConfigurations()).hasSize(1);
    ComponentScanDetails details = result.getConfigurations().get(0);
    assertThat(details.getEffectivePackages()).containsExactlyInAnyOrder(
        "com.example.service", "com.example.repository"
    );
}

@Test
void shouldDetectSpringBootApplicationScan() {
    ClassInfo mainClass = createClassWithSpringBootApplication();
    JarContent jarContent = JarContent.builder().addClass(mainClass).build();
    ComponentScanAnalyzer analyzer = new JavassistComponentScanAnalyzer();
    
    ComponentScanAnalysisResult result = analyzer.analyzeComponentScan(jarContent);
    
    assertThat(result.getConfigurations()).hasSize(1);
    // @SpringBootApplication includes implicit @ComponentScan
    ComponentScanDetails details = result.getConfigurations().get(0);
    assertThat(details.getEffectivePackages()).contains("com.example"); // Package of main class
}

@Test
void shouldAnalyzeIncludeFilters() {
    ComponentScanFilter annotationFilter = ComponentScanFilter.annotation("org.springframework.stereotype.Service");
    ClassInfo configClass = createClassWithComponentScan(
        includeFilters = {annotationFilter}
    );
    JarContent jarContent = JarContent.builder().addClass(configClass).build();
    
    ComponentScanAnalysisResult result = new JavassistComponentScanAnalyzer().analyzeComponentScan(jarContent);
    
    ComponentScanDetails details = result.getConfigurations().get(0);
    FilterAnalysisResult filterResult = details.getFilterAnalysis();
    assertThat(filterResult.getIncludeImpacts()).hasSize(1);
    assertThat(filterResult.getIncludeImpacts().get(0).getFilterType()).isEqualTo(FilterType.ANNOTATION);
}

@Test
void shouldDetectPackageOverlapConflicts() {
    ClassInfo config1 = createClassWithComponentScan(basePackages = {"com.example"});
    ClassInfo config2 = createClassWithComponentScan(basePackages = {"com.example.service"});
    JarContent jarContent = JarContent.builder()
        .addClass(config1)
        .addClass(config2)
        .build();
    
    ComponentScanAnalysisResult result = new JavassistComponentScanAnalyzer().analyzeComponentScan(jarContent);
    
    assertThat(result.getConflicts()).hasSize(1);
    ComponentScanConflict conflict = result.getConflicts().get(0);
    assertThat(conflict.getType()).isEqualTo(ConflictType.PACKAGE_OVERLAP);
    assertThat(conflict.getOverlappingPackages()).contains("com.example.service");
}

// Integration Test
@Test
void shouldAnalyzeComplexComponentScanConfiguration() {
    ClassInfo complexConfig = createComplexComponentScanClass();
    JarContent jarContent = JarContent.builder().addClass(complexConfig).build();
    
    ComponentScanAnalysisResult result = new JavassistComponentScanAnalyzer().analyzeComponentScan(jarContent);
    
    ComponentScanDetails details = result.getConfigurations().get(0);
    assertThat(details.getImpact().getEstimatedClassesScanned()).isGreaterThan(0);
    assertThat(details.getImpact().getPerformanceImpact()).isNotNull();
}

// ComponentScanConflictDetectorTest.java
@Test
void shouldDetectRedundantConfigurations() {
    ComponentScanConfiguration config1 = createConfig("com.example", "com.example.service");
    ComponentScanConfiguration config2 = createConfig("com.example.service");
    
    List<ComponentScanConflict> conflicts = new ComponentScanConflictDetector()
        .detectConflicts(Arrays.asList(config1, config2));
    
    assertThat(conflicts).hasSize(1);
    assertThat(conflicts.get(0).getType()).isEqualTo(ConflictType.REDUNDANT_CONFIGURATION);
}
```

Continuo con i restanti task della Fase 2...