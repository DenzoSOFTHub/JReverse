# Report 20: Analisi delle Dependency Injection Circolari

**Valore**: ⭐⭐⭐⭐⭐ **Complessità**: 🔴 Complessa **Tempo**: 8-10 giorni
**Tags**: `#circular-dependencies` `#di-analysis` `#architecture-issues`

## Descrizione

Analizza e identifica dipendenze circolari nel sistema di dependency injection Spring, mappando i cicli di dipendenze, valutando la gravità dei problemi e suggerendo strategie di risoluzione per migliorare l'architettura applicativa.

## Sezioni del Report

### 1. Circular Dependency Detection
- Identificazione cicli di dipendenze completi
- Analisi profondità e complessità dei cicli
- Mappatura relazioni circolari tra componenti
- Classificazione tipologie di dipendenze circolari

### 2. Dependency Chain Analysis
- Tracciamento catene di dipendenze complete
- Analisi lunghezza e profondità catene
- Identificazione punti critici nella catena
- Dependency graph visualization

### 3. Resolution Strategy Assessment
- Valutazione strategie risoluzione (@Lazy, interface segregation)
- Analisi impatto delle soluzioni proposte
- Refactoring opportunities identification
- Architecture improvement recommendations

### 4. Impact & Risk Analysis
- Valutazione rischi per stability applicazione
- Performance impact assessment
- Maintainability implications
- Testing difficulties identification

## Implementazione con Javassist

```java
public class CircularDependencyAnalyzer {
    
    private final ClassPool classPool;
    private final DependencyGraph dependencyGraph = new DependencyGraph();
    private final Map<String, ComponentInfo> components = new HashMap<>();
    
    public CircularDependencyReport analyzeCircularDependencies(CtClass[] classes) {
        CircularDependencyReport report = new CircularDependencyReport();
        
        // 1. Costruisci dependency graph completo
        buildCompleteDependencyGraph(classes);
        
        // 2. Rileva tutti i cicli di dipendenze
        List<CircularDependencyChain> cycles = detectAllCircularDependencies();
        
        // 3. Analizza ogni ciclo per gravità e tipo
        for (CircularDependencyChain cycle : cycles) {
            analyzeCircularDependencyChain(cycle);
            report.addCircularDependency(cycle);
        }
        
        // 4. Suggerisci strategie di risoluzione
        generateResolutionStrategies(report);
        
        // 5. Calcola metriche di impatto
        calculateImpactMetrics(report);
        
        return report;
    }
    
    private void buildCompleteDependencyGraph(CtClass[] classes) {
        // Prima passata: identifica tutti i componenti
        for (CtClass ctClass : classes) {
            if (isSpringComponent(ctClass)) {
                ComponentInfo component = analyzeComponent(ctClass);
                components.put(ctClass.getName(), component);
                dependencyGraph.addNode(new DependencyNode(component));
            }
        }
        
        // Seconda passata: costruisci tutte le relazioni
        for (ComponentInfo component : components.values()) {
            buildComponentDependencies(component);
        }
    }
    
    private boolean isSpringComponent(CtClass ctClass) {
        return ctClass.hasAnnotation("org.springframework.stereotype.Component") ||
               ctClass.hasAnnotation("org.springframework.stereotype.Service") ||
               ctClass.hasAnnotation("org.springframework.stereotype.Repository") ||
               ctClass.hasAnnotation("org.springframework.stereotype.Controller") ||
               ctClass.hasAnnotation("org.springframework.web.bind.annotation.RestController") ||
               ctClass.hasAnnotation("org.springframework.context.annotation.Configuration");
    }
    
    private ComponentInfo analyzeComponent(CtClass ctClass) {
        ComponentInfo component = new ComponentInfo();
        component.setClassName(ctClass.getName());
        component.setComponentType(determineComponentType(ctClass));
        
        try {
            // Analizza constructor dependencies
            analyzeConstructorDependencies(ctClass, component);
            
            // Analizza field dependencies
            analyzeFieldDependencies(ctClass, component);
            
            // Analizza method dependencies
            analyzeMethodDependencies(ctClass, component);
            
        } catch (Exception e) {
            component.addError("Error analyzing component: " + e.getMessage());
        }
        
        return component;
    }
    
    private void analyzeConstructorDependencies(CtClass ctClass, ComponentInfo component) {
        try {
            CtConstructor[] constructors = ctClass.getConstructors();
            
            for (CtConstructor constructor : constructors) {
                // Constructor con @Autowired esplicito o single constructor
                if (constructor.hasAnnotation("org.springframework.beans.factory.annotation.Autowired") ||
                    constructors.length == 1) {
                    
                    CtClass[] paramTypes = constructor.getParameterTypes();
                    Annotation[][] paramAnnotations = constructor.getParameterAnnotations();
                    
                    for (int i = 0; i < paramTypes.length; i++) {
                        DependencyInfo dependency = new DependencyInfo();
                        dependency.setTargetClass(paramTypes[i].getName());
                        dependency.setInjectionType(InjectionType.CONSTRUCTOR);
                        dependency.setParameterIndex(i);
                        
                        // Check for @Lazy annotation
                        for (Annotation annotation : paramAnnotations[i]) {
                            if ("org.springframework.context.annotation.Lazy".equals(annotation.getTypeName())) {
                                dependency.setLazy(true);
                                break;
                            }
                        }
                        
                        component.addDependency(dependency);
                    }
                }
            }
            
        } catch (NotFoundException e) {
            component.addError("Error analyzing constructor dependencies: " + e.getMessage());
        }
    }
    
    private void analyzeFieldDependencies(CtClass ctClass, ComponentInfo component) {
        try {
            CtField[] fields = ctClass.getDeclaredFields();
            
            for (CtField field : fields) {
                if (field.hasAnnotation("org.springframework.beans.factory.annotation.Autowired")) {
                    DependencyInfo dependency = new DependencyInfo();
                    dependency.setTargetClass(field.getType().getName());
                    dependency.setInjectionType(InjectionType.FIELD);
                    dependency.setFieldName(field.getName());
                    
                    // Check for @Lazy annotation
                    if (field.hasAnnotation("org.springframework.context.annotation.Lazy")) {
                        dependency.setLazy(true);
                    }
                    
                    // Field injection is generally problematic for circular dependencies
                    dependency.setCircularDependencyRisk(CircularDependencyRisk.HIGH);
                    
                    component.addDependency(dependency);
                }
            }
            
        } catch (NotFoundException e) {
            component.addError("Error analyzing field dependencies: " + e.getMessage());
        }
    }
    
    private void analyzeMethodDependencies(CtClass ctClass, ComponentInfo component) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                if (method.hasAnnotation("org.springframework.beans.factory.annotation.Autowired")) {
                    CtClass[] paramTypes = method.getParameterTypes();
                    
                    for (int i = 0; i < paramTypes.length; i++) {
                        DependencyInfo dependency = new DependencyInfo();
                        dependency.setTargetClass(paramTypes[i].getName());
                        dependency.setInjectionType(InjectionType.METHOD);
                        dependency.setMethodName(method.getName());
                        dependency.setParameterIndex(i);
                        
                        component.addDependency(dependency);
                    }
                }
            }
            
        } catch (NotFoundException e) {
            component.addError("Error analyzing method dependencies: " + e.getMessage());
        }
    }
    
    private List<CircularDependencyChain> detectAllCircularDependencies() {
        List<CircularDependencyChain> cycles = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Map<String, Integer> recursionStack = new HashMap<>();
        
        for (DependencyNode node : dependencyGraph.getNodes()) {
            if (!visited.contains(node.getClassName())) {
                List<String> currentPath = new ArrayList<>();
                detectCyclesDFS(node, visited, recursionStack, currentPath, cycles, 0);
            }
        }
        
        return cycles;
    }
    
    private void detectCyclesDFS(DependencyNode node, Set<String> visited, 
                                Map<String, Integer> recursionStack, List<String> currentPath,
                                List<CircularDependencyChain> cycles, int depth) {
        
        String className = node.getClassName();
        visited.add(className);
        recursionStack.put(className, depth);
        currentPath.add(className);
        
        for (DependencyEdge edge : node.getOutgoingEdges()) {
            String targetClass = edge.getTarget().getClassName();
            
            if (!visited.contains(targetClass)) {
                detectCyclesDFS(edge.getTarget(), visited, recursionStack, 
                              currentPath, cycles, depth + 1);
            } else if (recursionStack.containsKey(targetClass)) {
                // Ciclo trovato
                int cycleStartIndex = recursionStack.get(targetClass);
                List<String> cycle = new ArrayList<>(currentPath.subList(cycleStartIndex, currentPath.size()));
                cycle.add(targetClass); // Chiudi il ciclo
                
                CircularDependencyChain circularChain = new CircularDependencyChain(cycle);
                circularChain.setDepth(depth - cycleStartIndex);
                circularChain.setTriggerEdge(edge);
                
                cycles.add(circularChain);
            }
        }
        
        recursionStack.remove(className);
        currentPath.remove(currentPath.size() - 1);
    }
    
    private void analyzeCircularDependencyChain(CircularDependencyChain chain) {
        // Analizza il tipo di dipendenze nel ciclo
        List<DependencyInfo> chainDependencies = new ArrayList<>();
        List<String> cycle = chain.getCycle();
        
        for (int i = 0; i < cycle.size() - 1; i++) {
            String sourceClass = cycle.get(i);
            String targetClass = cycle.get(i + 1);
            
            ComponentInfo sourceComponent = components.get(sourceClass);
            if (sourceComponent != null) {
                DependencyInfo dependency = sourceComponent.getDependencies().stream()
                    .filter(dep -> dep.getTargetClass().equals(targetClass))
                    .findFirst()
                    .orElse(null);
                
                if (dependency != null) {
                    chainDependencies.add(dependency);
                }
            }
        }
        
        chain.setChainDependencies(chainDependencies);
        
        // Determina la gravità del ciclo
        CircularDependencySeverity severity = determineCircularDependencySeverity(chainDependencies);
        chain.setSeverity(severity);
        
        // Determina il tipo di ciclo
        CircularDependencyType type = determineCircularDependencyType(chainDependencies);
        chain.setType(type);
        
        // Identifica possibili risoluzioni
        identifyResolutionOptions(chain);
    }
    
    private CircularDependencySeverity determineCircularDependencySeverity(List<DependencyInfo> dependencies) {
        boolean hasFieldInjection = dependencies.stream()
            .anyMatch(dep -> dep.getInjectionType() == InjectionType.FIELD);
        
        boolean hasLazyDependency = dependencies.stream()
            .anyMatch(DependencyInfo::isLazy);
        
        int cycleLength = dependencies.size();
        
        if (hasFieldInjection && !hasLazyDependency) {
            return CircularDependencySeverity.CRITICAL;
        } else if (cycleLength > 4 && !hasLazyDependency) {
            return CircularDependencySeverity.HIGH;
        } else if (hasLazyDependency) {
            return CircularDependencySeverity.LOW;
        } else {
            return CircularDependencySeverity.MEDIUM;
        }
    }
    
    private CircularDependencyType determineCircularDependencyType(List<DependencyInfo> dependencies) {
        boolean allConstructor = dependencies.stream()
            .allMatch(dep -> dep.getInjectionType() == InjectionType.CONSTRUCTOR);
        
        boolean hasField = dependencies.stream()
            .anyMatch(dep -> dep.getInjectionType() == InjectionType.FIELD);
        
        boolean hasMethod = dependencies.stream()
            .anyMatch(dep -> dep.getInjectionType() == InjectionType.METHOD);
        
        if (allConstructor) {
            return CircularDependencyType.CONSTRUCTOR_CYCLE;
        } else if (hasField) {
            return CircularDependencyType.FIELD_INJECTION_CYCLE;
        } else if (hasMethod) {
            return CircularDependencyType.METHOD_INJECTION_CYCLE;
        } else {
            return CircularDependencyType.MIXED_INJECTION_CYCLE;
        }
    }
    
    private void identifyResolutionOptions(CircularDependencyChain chain) {
        List<ResolutionOption> options = new ArrayList<>();
        
        // Opzione 1: @Lazy annotation
        if (chain.getType() == CircularDependencyType.CONSTRUCTOR_CYCLE) {
            ResolutionOption lazyOption = new ResolutionOption();
            lazyOption.setStrategy(ResolutionStrategy.LAZY_INITIALIZATION);
            lazyOption.setDescription("Add @Lazy annotation to break the cycle");
            lazyOption.setComplexity(ResolutionComplexity.LOW);
            lazyOption.setRecommendedTarget(findBestLazyTarget(chain));
            options.add(lazyOption);
        }
        
        // Opzione 2: Interface segregation
        ResolutionOption interfaceOption = new ResolutionOption();
        interfaceOption.setStrategy(ResolutionStrategy.INTERFACE_SEGREGATION);
        interfaceOption.setDescription("Introduce interfaces to break direct dependencies");
        interfaceOption.setComplexity(ResolutionComplexity.MEDIUM);
        options.add(interfaceOption);
        
        // Opzione 3: Architectural refactoring
        if (chain.getDepth() > 3) {
            ResolutionOption refactorOption = new ResolutionOption();
            refactorOption.setStrategy(ResolutionStrategy.ARCHITECTURAL_REFACTORING);
            refactorOption.setDescription("Refactor architecture to eliminate circular dependencies");
            refactorOption.setComplexity(ResolutionComplexity.HIGH);
            options.add(refactorOption);
        }
        
        // Opzione 4: Event-driven communication
        ResolutionOption eventOption = new ResolutionOption();
        eventOption.setStrategy(ResolutionStrategy.EVENT_DRIVEN);
        eventOption.setDescription("Use ApplicationEventPublisher for loose coupling");
        eventOption.setComplexity(ResolutionComplexity.MEDIUM);
        options.add(eventOption);
        
        chain.setResolutionOptions(options);
    }
    
    private String findBestLazyTarget(CircularDependencyChain chain) {
        // Trova il miglior target per @Lazy annotation basato su:
        // 1. Tipo di componente (Service > Repository > Controller)
        // 2. Posizione nel ciclo
        // 3. Complessità delle dipendenze
        
        List<String> cycle = chain.getCycle();
        Map<String, Integer> componentPriority = new HashMap<>();
        
        for (String className : cycle) {
            ComponentInfo component = components.get(className);
            if (component != null) {
                int priority = calculateLazyTargetPriority(component);
                componentPriority.put(className, priority);
            }
        }
        
        return componentPriority.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(cycle.get(0));
    }
    
    private int calculateLazyTargetPriority(ComponentInfo component) {
        int priority = 0;
        
        // Priorità basata su tipo componente
        switch (component.getComponentType()) {
            case SERVICE:
                priority += 10;
                break;
            case REPOSITORY:
                priority += 8;
                break;
            case COMPONENT:
                priority += 6;
                break;
            case CONTROLLER:
                priority += 4;
                break;
        }
        
        // Penalizzazione per molte dipendenze
        priority -= component.getDependencies().size();
        
        return priority;
    }
    
    private void generateResolutionStrategies(CircularDependencyReport report) {
        ResolutionStrategyAnalysis analysis = new ResolutionStrategyAnalysis();
        
        Map<ResolutionStrategy, Integer> strategyCount = new HashMap<>();
        Map<CircularDependencySeverity, Integer> severityCount = new HashMap<>();
        
        for (CircularDependencyChain chain : report.getCircularDependencies()) {
            severityCount.merge(chain.getSeverity(), 1, Integer::sum);
            
            for (ResolutionOption option : chain.getResolutionOptions()) {
                strategyCount.merge(option.getStrategy(), 1, Integer::sum);
            }
        }
        
        analysis.setStrategyDistribution(strategyCount);
        analysis.setSeverityDistribution(severityCount);
        
        // Genera raccomandazioni generali
        List<GeneralRecommendation> recommendations = generateGeneralRecommendations(report);
        analysis.setGeneralRecommendations(recommendations);
        
        report.setResolutionStrategyAnalysis(analysis);
    }
    
    private void calculateImpactMetrics(CircularDependencyReport report) {
        ImpactMetrics metrics = new ImpactMetrics();
        
        int totalComponents = components.size();
        int affectedComponents = report.getCircularDependencies().stream()
            .flatMap(chain -> chain.getCycle().stream())
            .collect(Collectors.toSet())
            .size();
        
        metrics.setTotalComponents(totalComponents);
        metrics.setAffectedComponents(affectedComponents);
        metrics.setCircularDependencyRatio((double) affectedComponents / totalComponents);
        
        // Calcola complexity score
        int totalCycles = report.getCircularDependencies().size();
        int avgCycleDepth = report.getCircularDependencies().stream()
            .mapToInt(CircularDependencyChain::getDepth)
            .sum() / Math.max(totalCycles, 1);
        
        metrics.setTotalCircularDependencies(totalCycles);
        metrics.setAverageCycleDepth(avgCycleDepth);
        metrics.setComplexityScore(calculateComplexityScore(totalCycles, avgCycleDepth, affectedComponents));
        
        report.setImpactMetrics(metrics);
    }
}

public class CircularDependencyReport {
    private List<CircularDependencyChain> circularDependencies = new ArrayList<>();
    private ResolutionStrategyAnalysis resolutionStrategyAnalysis;
    private ImpactMetrics impactMetrics;
    private List<String> errors = new ArrayList<>();
    
    public static class CircularDependencyChain {
        private List<String> cycle;
        private int depth;
        private CircularDependencySeverity severity;
        private CircularDependencyType type;
        private List<DependencyInfo> chainDependencies = new ArrayList<>();
        private List<ResolutionOption> resolutionOptions = new ArrayList<>();
        private DependencyEdge triggerEdge;
    }
    
    public static class ResolutionOption {
        private ResolutionStrategy strategy;
        private String description;
        private ResolutionComplexity complexity;
        private String recommendedTarget;
        private String implementationNotes;
    }
    
    public enum CircularDependencySeverity {
        CRITICAL, HIGH, MEDIUM, LOW
    }
    
    public enum CircularDependencyType {
        CONSTRUCTOR_CYCLE, FIELD_INJECTION_CYCLE, METHOD_INJECTION_CYCLE, MIXED_INJECTION_CYCLE
    }
    
    public enum ResolutionStrategy {
        LAZY_INITIALIZATION, INTERFACE_SEGREGATION, ARCHITECTURAL_REFACTORING, EVENT_DRIVEN
    }
    
    public enum ResolutionComplexity {
        LOW, MEDIUM, HIGH
    }
}
```

## Raccolta Dati

### 1. Dependency Graph Construction
- Identificazione componenti Spring (Service, Repository, Controller)
- Analisi injection points (constructor, field, method)
- Mappatura relazioni dirette e indirette
- Graph traversal per dependency chains

### 2. Circular Dependency Detection
- DFS algorithm per cycle detection
- Analisi profondità e complessità cicli
- Identificazione multiple circular paths
- Classification per tipologia injection

### 3. Impact Assessment
- Componenti coinvolti nei cicli
- Severità basata su injection type
- Performance implications analysis
- Maintainability impact evaluation

### 4. Resolution Strategy Analysis
- @Lazy annotation opportunities
- Interface segregation possibilities
- Architectural refactoring options
- Event-driven alternatives

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculateCircularDependencyQualityScore(CircularDependencyReport result) {
    double score = 100.0;
    
    // Penalizzazioni per dipendenze circolari critiche
    score -= result.getCriticalCircularDependencies() * 35;   // -35 per ogni ciclo critico
    score -= result.getHighSeverityCircularDependencies() * 25; // -25 per ogni ciclo high severity
    score -= result.getMediumSeverityCircularDependencies() * 15; // -15 per ogni ciclo medium severity
    score -= result.getLowSeverityCircularDependencies() * 8; // -8 per ogni ciclo low severity
    score -= result.getFieldInjectionCycles() * 20;          // -20 per cicli con field injection
    score -= result.getDeepCircularChains() * 12;            // -12 per catene circolari profonde (>4)
    score -= result.getUnresolvedCircularDependencies() * 10; // -10 per cicli senza strategie risoluzione
    
    // Bonus per buone pratiche e risoluzioni
    score += result.getLazyResolvedCycles() * 8;              // +8 per cicli risolti con @Lazy
    score += result.getInterfaceSegregationResolutions() * 6; // +6 per risoluzioni con interface segregation
    score += result.getEventDrivenResolutions() * 4;         // +4 per risoluzioni event-driven
    score += result.getArchitecturalRefactorings() * 3;      // +3 per refactoring architetturali
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - Gravi problemi dipendenze circolari che compromettono architettura
- **41-60**: 🟡 SUFFICIENTE - Alcune dipendenze circolari presenti ma gestibili
- **61-80**: 🟢 BUONO - Architettura ben strutturata con minor circular dependencies
- **81-100**: ⭐ ECCELLENTE - Architettura ottimale senza dipendenze circolari significative

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -25 to -35)
1. **Dipendenze circolari con field injection**
   - Descrizione: Cicli di dipendenze che utilizzano @Autowired su campi
   - Rischio: BeanCurrentlyInCreationException, impossibilità inizializzazione beans
   - Soluzione: Convertire a constructor injection e aggiungere @Lazy

2. **Cicli di dipendenze critici senza risoluzione**
   - Descrizione: Dipendenze circolari senza strategie @Lazy o alternative
   - Rischio: Application startup failure, runtime exceptions
   - Soluzione: Implementare @Lazy, interface segregation, o architectural refactoring

### 🟠 GRAVITÀ ALTA (Score Impact: -15 to -25)
3. **Catene circolari profonde (>4 componenti)**
   - Descrizione: Cicli di dipendenze che coinvolgono più di 4 componenti
   - Rischio: Complessità architetturale eccessiva, difficile debugging e maintenance
   - Soluzione: Refactoring architetturale per ridurre coupling

4. **Constructor injection circular dependencies**
   - Descrizione: Cicli creati da constructor injection senza @Lazy
   - Rischio: Bean creation failure, circular reference exceptions
   - Soluzione: Aggiungere @Lazy su almeno una dipendenza nel ciclo

### 🟡 GRAVITÀ MEDIA (Score Impact: -8 to -15)
5. **Medium severity circular dependencies**
   - Descrizione: Cicli gestibili ma che indicano design issues
   - Rischio: Code complexity, potential maintenance issues
   - Soluzione: Consider interface segregation o event-driven patterns

6. **Mixed injection type cycles**
   - Descrizione: Cicli che utilizzano mix di constructor, field, method injection
   - Rischio: Inconsistent dependency management, confusion
   - Soluzione: Standardizzare su constructor injection + @Lazy dove necessario

### 🔵 GRAVITÀ BASSA (Score Impact: -1 to -8)
7. **Low severity circular dependencies (con @Lazy)**
   - Descrizione: Cicli già risolti con @Lazy ma ancora presenti structurally
   - Rischio: Minimal impact, ma indicate architectural improvements possibili
   - Soluzione: Consider long-term architectural refactoring

## Metriche di Valore

- **Architecture Quality**: Migliora qualità complessiva architettura eliminando coupling problematico
- **Application Stability**: Previene runtime exceptions e startup failures
- **Code Maintainability**: Facilita maintenance e evolution riducendo coupling
- **Testing Capability**: Migliora testability riducendo dependencies complesse

## Classificazione

**Categoria**: Architecture & Dependencies
**Priorità**: Critica - Dipendenze circolari possono impedire startup applicazione
**Stakeholder**: Development team, Solution architects, Technical leads

## Tags per Classificazione

`#circular-dependencies` `#di-analysis` `#architecture-issues` `#spring-framework` `#dependency-injection` `#code-quality` `#architectural-refactoring` `#bean-lifecycle`