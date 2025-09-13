# Report 15: Bean Configuration Report

## Descrizione
Analisi completa delle configurazioni Spring Bean (@Configuration, @Bean) con mapping delle dipendenze, scope, e lifecycle management.

## Valore per l'Utente
**⭐⭐⭐⭐** - Alto Valore

## Complessità di Implementazione
**🟡 Media**

## Tempo di Realizzazione Stimato
**5-7 giorni**

## Sezioni del Report

### 1. Configuration Classes Analysis
- @Configuration annotated classes identification
- @Bean method definitions and their parameters
- Configuration class hierarchy and imports
- Conditional configuration analysis (@ConditionalOn*)

### 2. Bean Definitions and Scope Analysis
- Bean names, types, and scope configurations
- Singleton vs prototype vs session beans
- Lazy initialization patterns
- Primary and qualifier annotations

### 3. Dependency Injection Patterns
- Constructor vs field vs setter injection
- Circular dependency detection
- Optional vs required dependencies
- Dependency resolution order

### 4. Configuration Issues Detection
- Missing bean definitions
- Configuration conflicts and overrides
- Scope compatibility issues
- Circular configuration dependencies

## Implementazione Javassist Completa

```java
public class BeanConfigurationAnalyzer {
    
    public BeanConfigurationReport analyzeBeanConfiguration(CtClass[] classes) {
        BeanConfigurationReport report = new BeanConfigurationReport();
        
        // 1. Identifica configuration classes
        List<ConfigurationClass> configClasses = identifyConfigurationClasses(classes, report);
        
        // 2. Analizza bean definitions
        analyzeBeanDefinitions(configClasses, report);
        
        // 3. Analizza dependency injection patterns
        analyzeDependencyInjectionPatterns(classes, report);
        
        // 4. Identifica problemi di configurazione
        identifyConfigurationIssues(configClasses, report);
        
        return report;
    }
    
    private List<ConfigurationClass> identifyConfigurationClasses(CtClass[] classes, BeanConfigurationReport report) {
        List<ConfigurationClass> configClasses = new ArrayList<>();
        
        for (CtClass ctClass : classes) {
            try {
                if (isConfigurationClass(ctClass)) {
                    ConfigurationClass configClass = analyzeConfigurationClass(ctClass, report);
                    configClasses.add(configClass);
                }
            } catch (Exception e) {
                report.addError("Errore nell'analisi configuration class: " + e.getMessage());
            }
        }
        
        return configClasses;
    }
    
    private boolean isConfigurationClass(CtClass ctClass) {
        try {
            return ctClass.hasAnnotation("org.springframework.context.annotation.Configuration") ||
                   ctClass.hasAnnotation("org.springframework.boot.autoconfigure.SpringBootApplication") ||
                   hasComponentScanAnnotation(ctClass) ||
                   hasImportAnnotation(ctClass);
        } catch (Exception e) {
            return false;
        }
    }
    
    private ConfigurationClass analyzeConfigurationClass(CtClass ctClass, BeanConfigurationReport report) {
        try {
            ConfigurationClass configClass = new ConfigurationClass();
            configClass.setClassName(ctClass.getName());
            configClass.setSimpleName(ctClass.getSimpleName());
            
            // Analizza @Configuration annotation
            analyzeConfigurationAnnotation(ctClass, configClass);
            
            // Analizza @ComponentScan
            analyzeComponentScanAnnotation(ctClass, configClass);
            
            // Analizza @Import annotations
            analyzeImportAnnotations(ctClass, configClass);
            
            // Analizza @PropertySource
            analyzePropertySourceAnnotations(ctClass, configClass);
            
            // Analizza conditional annotations
            analyzeConditionalAnnotations(ctClass, configClass);
            
            // Analizza bean methods
            analyzeBeanMethods(ctClass, configClass, report);
            
            return configClass;
            
        } catch (Exception e) {
            ConfigurationClass errorConfig = new ConfigurationClass();
            errorConfig.setClassName(ctClass.getName());
            errorConfig.addError("Errore nell'analisi configuration class: " + e.getMessage());
            return errorConfig;
        }
    }
    
    private void analyzeBeanMethods(CtClass ctClass, ConfigurationClass configClass, BeanConfigurationReport report) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                if (method.hasAnnotation("org.springframework.context.annotation.Bean")) {
                    BeanDefinition beanDef = analyzeBeanMethod(method, configClass, report);
                    configClass.addBeanDefinition(beanDef);
                }
            }
            
        } catch (Exception e) {
            configClass.addError("Errore nell'analisi bean methods: " + e.getMessage());
        }
    }
    
    private BeanDefinition analyzeBeanMethod(CtMethod method, ConfigurationClass configClass, BeanConfigurationReport report) {
        try {
            BeanDefinition beanDef = new BeanDefinition();
            beanDef.setMethodName(method.getName());
            beanDef.setReturnType(method.getReturnType().getName());
            beanDef.setConfigurationClass(configClass.getClassName());
            
            // Analizza @Bean annotation
            Annotation beanAnnotation = method.getAnnotation("org.springframework.context.annotation.Bean");
            analyzeBeanAnnotation(beanAnnotation, beanDef);
            
            // Analizza @Scope
            if (method.hasAnnotation("org.springframework.context.annotation.Scope")) {
                Annotation scopeAnnotation = method.getAnnotation("org.springframework.context.annotation.Scope");
                String scopeValue = extractAnnotationValue(scopeAnnotation, "value");
                beanDef.setScope(scopeValue != null ? scopeValue : "singleton");
            } else {
                beanDef.setScope("singleton");
            }
            
            // Analizza @Lazy
            boolean isLazy = method.hasAnnotation("org.springframework.context.annotation.Lazy");
            beanDef.setLazy(isLazy);
            
            // Analizza @Primary
            boolean isPrimary = method.hasAnnotation("org.springframework.context.annotation.Primary");
            beanDef.setPrimary(isPrimary);
            
            // Analizza @Qualifier
            if (method.hasAnnotation("org.springframework.beans.factory.annotation.Qualifier")) {
                Annotation qualifierAnnotation = method.getAnnotation("org.springframework.beans.factory.annotation.Qualifier");
                String qualifierValue = extractAnnotationValue(qualifierAnnotation, "value");
                beanDef.setQualifier(qualifierValue);
            }
            
            // Analizza parametri del metodo (dependencies)
            analyzeBeanMethodParameters(method, beanDef, report);
            
            // Verifica potenziali problemi
            validateBeanDefinition(method, beanDef, report);
            
            return beanDef;
            
        } catch (Exception e) {
            BeanDefinition errorBean = new BeanDefinition();
            errorBean.setMethodName(method.getName());
            errorBean.addError("Errore nell'analisi bean method: " + e.getMessage());
            return errorBean;
        }
    }
    
    private void analyzeBeanMethodParameters(CtMethod method, BeanDefinition beanDef, BeanConfigurationReport report) {
        try {
            CtClass[] paramTypes = method.getParameterTypes();
            Object[][] paramAnnotations = method.getParameterAnnotations();
            
            List<BeanDependency> dependencies = new ArrayList<>();
            
            for (int i = 0; i < paramTypes.length; i++) {
                BeanDependency dependency = new BeanDependency();
                dependency.setParameterIndex(i);
                dependency.setParameterType(paramTypes[i].getName());
                
                // Analizza annotazioni parametro
                if (paramAnnotations[i].length > 0) {
                    for (Object annotation : paramAnnotations[i]) {
                        String annotationType = annotation.getClass().getName();
                        
                        if (annotationType.contains("Qualifier")) {
                            dependency.setQualifier(extractQualifierValue(annotation));
                        } else if (annotationType.contains("Value")) {
                            dependency.setPropertyValue(extractValueAnnotation(annotation));
                        } else if (annotationType.contains("Autowired")) {
                            boolean required = extractRequiredValue(annotation);
                            dependency.setRequired(required);
                        }
                    }
                }
                
                dependencies.add(dependency);
            }
            
            beanDef.setDependencies(dependencies);
            
        } catch (Exception e) {
            beanDef.addError("Errore nell'analisi parametri bean method: " + e.getMessage());
        }
    }
    
    private void analyzeDependencyInjectionPatterns(CtClass[] classes, BeanConfigurationReport report) {
        try {
            for (CtClass ctClass : classes) {
                if (isSpringManagedComponent(ctClass)) {
                    ComponentInjectionAnalysis injectionAnalysis = analyzeComponentInjection(ctClass, report);
                    report.addInjectionAnalysis(injectionAnalysis);
                }
            }
        } catch (Exception e) {
            report.addError("Errore nell'analisi dependency injection patterns: " + e.getMessage());
        }
    }
    
    private ComponentInjectionAnalysis analyzeComponentInjection(CtClass ctClass, BeanConfigurationReport report) {
        try {
            ComponentInjectionAnalysis analysis = new ComponentInjectionAnalysis();
            analysis.setClassName(ctClass.getName());
            
            // Analizza constructor injection
            analyzeConstructorInjection(ctClass, analysis);
            
            // Analizza field injection
            analyzeFieldInjection(ctClass, analysis, report);
            
            // Analizza setter injection
            analyzeSetterInjection(ctClass, analysis);
            
            // Determina injection pattern predominante
            determineInjectionPattern(analysis);
            
            return analysis;
            
        } catch (Exception e) {
            ComponentInjectionAnalysis errorAnalysis = new ComponentInjectionAnalysis();
            errorAnalysis.setClassName(ctClass.getName());
            errorAnalysis.addError("Errore nell'analisi component injection: " + e.getMessage());
            return errorAnalysis;
        }
    }
    
    private void analyzeFieldInjection(CtClass ctClass, ComponentInjectionAnalysis analysis, BeanConfigurationReport report) {
        try {
            CtField[] fields = ctClass.getDeclaredFields();
            
            for (CtField field : fields) {
                if (field.hasAnnotation("org.springframework.beans.factory.annotation.Autowired") ||
                    field.hasAnnotation("javax.inject.Inject") ||
                    field.hasAnnotation("org.springframework.beans.factory.annotation.Value")) {
                    
                    FieldInjection fieldInjection = new FieldInjection();
                    fieldInjection.setFieldName(field.getName());
                    fieldInjection.setFieldType(field.getType().getName());
                    
                    // Analizza annotazioni
                    analyzeInjectionAnnotations(field, fieldInjection);
                    
                    analysis.addFieldInjection(fieldInjection);
                    
                    // Field injection è considerato code smell
                    if (field.hasAnnotation("org.springframework.beans.factory.annotation.Autowired")) {
                        ConfigurationIssue issue = new ConfigurationIssue();
                        issue.setType(IssueType.FIELD_INJECTION_USAGE);
                        issue.setClassName(ctClass.getName());
                        issue.setFieldName(field.getName());
                        issue.setSeverity(Severity.LOW);
                        issue.setDescription("Field injection is discouraged - use constructor injection");
                        issue.setRecommendation("Refactor to use constructor injection for better testability");
                        
                        report.addConfigurationIssue(issue);
                    }
                }
            }
            
        } catch (Exception e) {
            analysis.addError("Errore nell'analisi field injection: " + e.getMessage());
        }
    }
    
    private void identifyConfigurationIssues(List<ConfigurationClass> configClasses, BeanConfigurationReport report) {
        try {
            // Verifica circular dependencies
            identifyCircularDependencies(configClasses, report);
            
            // Verifica bean name conflicts
            identifyBeanNameConflicts(configClasses, report);
            
            // Verifica scope incompatibilities
            identifyScopeIncompatibilities(configClasses, report);
            
            // Verifica missing dependencies
            identifyMissingDependencies(configClasses, report);
            
            // Verifica configuration overlaps
            identifyConfigurationOverlaps(configClasses, report);
            
        } catch (Exception e) {
            report.addError("Errore nell'identificazione configuration issues: " + e.getMessage());
        }
    }
    
    private void identifyBeanNameConflicts(List<ConfigurationClass> configClasses, BeanConfigurationReport report) {
        Map<String, List<BeanDefinition>> beansByName = new HashMap<>();
        
        // Raggruppa bean per nome
        for (ConfigurationClass configClass : configClasses) {
            for (BeanDefinition beanDef : configClass.getBeanDefinitions()) {
                String beanName = beanDef.getBeanName() != null ? beanDef.getBeanName() : beanDef.getMethodName();
                beansByName.computeIfAbsent(beanName, k -> new ArrayList<>()).add(beanDef);
            }
        }
        
        // Identifica conflitti
        for (Map.Entry<String, List<BeanDefinition>> entry : beansByName.entrySet()) {
            if (entry.getValue().size() > 1) {
                boolean hasPrimary = entry.getValue().stream().anyMatch(BeanDefinition::isPrimary);
                
                if (!hasPrimary) {
                    ConfigurationIssue issue = new ConfigurationIssue();
                    issue.setType(IssueType.BEAN_NAME_CONFLICT);
                    issue.setBeanName(entry.getKey());
                    issue.setSeverity(Severity.HIGH);
                    issue.setDescription("Multiple beans with name '" + entry.getKey() + "' without @Primary");
                    issue.setRecommendation("Add @Primary to one bean or use @Qualifier");
                    
                    report.addConfigurationIssue(issue);
                }
            }
        }
    }
    
    private void identifyCircularDependencies(List<ConfigurationClass> configClasses, BeanConfigurationReport report) {
        Map<String, Set<String>> dependencyGraph = buildDependencyGraph(configClasses);
        
        for (String beanName : dependencyGraph.keySet()) {
            Set<String> visited = new HashSet<>();
            Set<String> recursionStack = new HashSet<>();
            
            if (hasCircularDependency(beanName, dependencyGraph, visited, recursionStack)) {
                ConfigurationIssue issue = new ConfigurationIssue();
                issue.setType(IssueType.CIRCULAR_DEPENDENCY);
                issue.setBeanName(beanName);
                issue.setSeverity(Severity.HIGH);
                issue.setDescription("Circular dependency detected involving bean: " + beanName);
                issue.setRecommendation("Break circular dependency using @Lazy or redesign bean relationships");
                
                report.addConfigurationIssue(issue);
            }
        }
    }
}

public class BeanConfigurationReport {
    private List<ConfigurationClass> configurationClasses = new ArrayList<>();
    private List<BeanDefinition> allBeanDefinitions = new ArrayList<>();
    private List<ComponentInjectionAnalysis> injectionAnalyses = new ArrayList<>();
    private List<ConfigurationIssue> configurationIssues = new ArrayList<>();
    private BeanStatistics beanStatistics;
    private List<String> errors = new ArrayList<>();
    
    public static class ConfigurationClass {
        private String className;
        private String simpleName;
        private boolean proxyBeanMethods = true;
        private List<String> componentScanPackages = new ArrayList<>();
        private List<String> importedClasses = new ArrayList<>();
        private List<String> propertySourceFiles = new ArrayList<>();
        private List<String> conditionalAnnotations = new ArrayList<>();
        private List<BeanDefinition> beanDefinitions = new ArrayList<>();
        private List<String> errors = new ArrayList<>();
    }
    
    public static class BeanDefinition {
        private String methodName;
        private String beanName;
        private String returnType;
        private String scope = "singleton";
        private boolean lazy = false;
        private boolean primary = false;
        private String qualifier;
        private String configurationClass;
        private List<BeanDependency> dependencies = new ArrayList<>();
        private List<String> errors = new ArrayList<>();
    }
    
    public static class ComponentInjectionAnalysis {
        private String className;
        private List<ConstructorInjection> constructorInjections = new ArrayList<>();
        private List<FieldInjection> fieldInjections = new ArrayList<>();
        private List<SetterInjection> setterInjections = new ArrayList<>();
        private InjectionPattern predominantPattern;
        private List<String> errors = new ArrayList<>();
    }
    
    public static class ConfigurationIssue {
        private IssueType type;
        private String className;
        private String beanName;
        private String fieldName;
        private Severity severity;
        private String description;
        private String recommendation;
    }
    
    public enum IssueType {
        CIRCULAR_DEPENDENCY,
        BEAN_NAME_CONFLICT,
        SCOPE_INCOMPATIBILITY,
        MISSING_DEPENDENCY,
        FIELD_INJECTION_USAGE,
        CONFIGURATION_OVERLAP,
        UNUSED_BEAN_DEFINITION
    }
    
    public enum InjectionPattern {
        CONSTRUCTOR_INJECTION, FIELD_INJECTION, SETTER_INJECTION, MIXED
    }
    
    public enum Severity {
        CRITICAL, HIGH, MEDIUM, LOW
    }
}
```

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculateBeanConfigurationQualityScore(BeanConfigurationReport result) {
    double score = 100.0;
    
    // Penalizzazioni per problemi configurazione bean critici
    score -= result.getCircularDependencies() * 20;                // -20 per dipendenze circolari
    score -= result.getBeanNameConflicts() * 15;                   // -15 per conflitti nomi bean
    score -= result.getScopeIncompatibilities() * 12;              // -12 per incompatibilità scope
    score -= result.getMissingDependencies() * 18;                 // -18 per dipendenze mancanti
    score -= result.getFieldInjectionUsage() * 8;                  // -8 per uso field injection
    score -= result.getConfigurationOverlaps() * 10;               // -10 per sovrapposizione configurazioni
    score -= result.getUnusedBeanDefinitions() * 6;                // -6 per bean definition inutilizzati
    score -= result.getMissingPrimaryAnnotations() * 5;            // -5 per @Primary mancanti
    
    // Bonus per buone pratiche configurazione bean
    score += result.getConstructorInjectionUsage() * 3;            // +3 per uso constructor injection
    score += result.getProperBeanScoping() * 2;                    // +2 per scoping appropriato
    score += result.getWellDefinedQualifiers() * 2;                // +2 per qualifier ben definiti
    score += result.getConditionalConfigurationUsage() * 1;        // +1 per uso conditional configuration
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - Configurazione bean problematica con gravi conflitti
- **41-60**: 🟡 SUFFICIENTE - Configurazione funzionante ma con lacune architetturali
- **61-80**: 🟢 BUONO - Buona configurazione bean con alcuni miglioramenti
- **81-100**: ⭐ ECCELLENTE - Configurazione bean ottimale e best practices

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -18 to -20)
1. **Dipendenze circolari tra bean**
   - Descrizione: Bean che dipendono circolarmente l'uno dall'altro
   - Rischio: ApplicationContext startup failure, runtime instability
   - Soluzione: Usare @Lazy o ristrutturare dipendenze bean

2. **Dipendenze mancanti**
   - Descrizione: Bean che referenziano dipendenze non definite
   - Rischio: NoSuchBeanDefinitionException, application startup failure
   - Soluzione: Definire bean mancanti o rimuovere riferimenti

### 🟠 GRAVITÀ ALTA (Score Impact: -12 to -15)  
3. **Conflitti nomi bean**
   - Descrizione: Multiple bean definitions con stesso nome senza @Primary
   - Rischio: Ambiguous bean resolution, unpredictable behavior
   - Soluzione: Aggiungere @Primary o usare @Qualifier

4. **Incompatibilità scope**
   - Descrizione: Bean singleton che dipendono da bean prototype
   - Rischio: Behavior inaspettato, memory leaks, incorrect state
   - Soluzione: Usare @Lookup methods o Provider<T>

### 🟡 GRAVITÀ MEDIA (Score Impact: -8 to -10)
5. **Sovrapposizione configurazioni**
   - Descrizione: Multiple @Configuration classi che definiscono bean simili
   - Rischio: Configuration confusion, maintenance overhead
   - Soluzione: Consolidare o chiarire responsabilità configuration

6. **Uso field injection**
   - Descrizione: @Autowired su field invece di constructor injection
   - Rischio: Difficult testing, hidden dependencies, coupling
   - Soluzione: Refactoring a constructor injection

### 🔵 GRAVITÀ BASSA (Score Impact: -5 to -6)
7. **Bean definitions inutilizzati**
   - Descrizione: Bean definiti ma mai referenziati nell'applicazione
   - Rischio: Code bloat, confusion architetturale, wasted resources
   - Soluzione: Rimuovere bean definition non utilizzati

8. **@Primary mancanti**
   - Descrizione: Multiple bean dello stesso tipo senza @Primary
   - Rischio: Ambiguous resolution in alcuni contesti
   - Soluzione: Aggiungere @Primary al bean preferito

## Metriche di Valore

- **Application Stability**: Previene conflitti e errori startup attraverso configurazione corretta
- **Maintainability**: Facilita maintenance attraverso dependency injection patterns chiari
- **Testability**: Migliora testability attraverso constructor injection
- **Architecture Clarity**: Fornisce visione chiara delle dipendenze e configurazioni

## Tags
`#spring-configuration` `#bean-definitions` `#configuration-analysis` `#dependency-injection` `#high-value` `#medium-complexity`