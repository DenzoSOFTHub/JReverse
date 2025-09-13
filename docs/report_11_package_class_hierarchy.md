# Report 11: Mappa dei Package e delle Classi

## Descrizione
Analisi completa della struttura gerarchica dei package con catalogazione di tutte le classi, interfacce, enum, e annotation, inclusa l'organizzazione architetturale del codice.

## Valore per l'Utente
**⭐⭐⭐⭐** - Alto Valore
- Comprensione dell'organizzazione del codice
- Identificazione di problemi architetturali
- Base per refactoring strutturale
- Analisi della modularità
- Documentazione automatica dell'architettura

## Complessità di Implementazione
**🟡 Media** - Analisi di struttura package e classificazione classi

## Tempo di Realizzazione Stimato
**5-6 giorni** di sviluppo

## Implementazione Javassist

```java
public class PackageHierarchyAnalyzer {
    
    private final ClassPool classPool;
    private final PackageTree packageTree = new PackageTree();
    
    public PackageAnalysis analyzePackageHierarchy() {
        // 1. Scopri tutti i package
        Map<String, PackageInfo> packages = discoverPackages();
        
        // 2. Classifica tutte le classi
        for (PackageInfo pkg : packages.values()) {
            classifyPackageClasses(pkg);
        }
        
        // 3. Analizza dipendenze tra package
        analyzePackageDependencies(packages);
        
        // 4. Calcola metriche architetturali
        ArchitecturalMetrics metrics = calculateArchitecturalMetrics(packages);
        
        return new PackageAnalysis(packageTree, packages, metrics);
    }
    
    private Map<String, PackageInfo> discoverPackages() {
        Map<String, PackageInfo> packages = new HashMap<>();
        
        for (CtClass clazz : classPool.getAllClasses()) {
            String packageName = clazz.getPackageName();
            
            PackageInfo pkgInfo = packages.computeIfAbsent(packageName, this::createPackageInfo);
            
            ClassInfo classInfo = analyzeClass(clazz);
            pkgInfo.addClass(classInfo);
        }
        
        return packages;
    }
    
    private ClassInfo analyzeClass(CtClass clazz) {
        ClassInfo info = new ClassInfo();
        info.setClassName(clazz.getName());
        info.setSimpleName(clazz.getSimpleName());
        info.setClassType(determineClassType(clazz));
        info.setModifiers(clazz.getModifiers());
        info.setLineCount(estimateLineCount(clazz));
        
        // Analizza annotazioni principali
        analyzeClassAnnotations(clazz, info);
        
        // Analizza inheritance
        analyzeInheritanceHierarchy(clazz, info);
        
        // Analizza inner classes
        analyzeInnerClasses(clazz, info);
        
        return info;
    }
    
    private ClassType determineClassType(CtClass clazz) {
        if (clazz.isInterface()) {
            return ClassType.INTERFACE;
        } else if (clazz.isEnum()) {
            return ClassType.ENUM;
        } else if (clazz.isAnnotation()) {
            return ClassType.ANNOTATION;
        } else if (Modifier.isAbstract(clazz.getModifiers())) {
            return ClassType.ABSTRACT_CLASS;
        } else {
            return ClassType.CONCRETE_CLASS;
        }
    }
}
```

## Sezioni del Report

### 1. Package Structure Analysis
- Hierarchical package organization
- Package naming convention compliance  
- Package depth analysis
- Root package identification

### 2. Class Classification
- Concrete classes, abstract classes, interfaces
- Enums and annotation types
- Inner classes and nested types
- Utility and constant classes

### 3. Architectural Patterns Detection
- Layer separation (controller, service, repository)
- Domain-driven design patterns
- Package coupling analysis
- Circular dependency detection

### 4. Code Organization Quality
- Package size distribution
- Class count per package
- Naming consistency analysis
- Architectural violation detection

## Implementazione Javassist Completa

```java
public class PackageClassHierarchyAnalyzer {
    
    public PackageHierarchyReport analyzePackageClassHierarchy(CtClass[] classes) {
        PackageHierarchyReport report = new PackageHierarchyReport();
        
        Map<String, PackageInfo> packageMap = new HashMap<>();
        
        for (CtClass ctClass : classes) {
            analyzeClassAndPackage(ctClass, packageMap, report);
        }
        
        analyzePackageStructure(packageMap, report);
        identifyArchitecturalIssues(packageMap, report);
        
        return report;
    }
    
    private void analyzeClassAndPackage(CtClass ctClass, Map<String, PackageInfo> packageMap, PackageHierarchyReport report) {
        try {
            String packageName = ctClass.getPackageName();
            
            // Crea o recupera package info
            PackageInfo packageInfo = packageMap.computeIfAbsent(packageName, this::createPackageInfo);
            
            // Analizza la classe
            ClassInfo classInfo = analyzeClass(ctClass, report);
            packageInfo.addClass(classInfo);
            
            // Aggiorna statistiche package
            updatePackageStatistics(packageInfo, classInfo);
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi classe/package: " + e.getMessage());
        }
    }
    
    private ClassInfo analyzeClass(CtClass ctClass, PackageHierarchyReport report) {
        try {
            ClassInfo classInfo = new ClassInfo();
            classInfo.setClassName(ctClass.getName());
            classInfo.setSimpleName(ctClass.getSimpleName());
            classInfo.setPackageName(ctClass.getPackageName());
            
            // Determina tipo di classe
            ClassType classType = determineClassType(ctClass);
            classInfo.setClassType(classType);
            
            // Analizza modifiers
            int modifiers = ctClass.getModifiers();
            classInfo.setPublic(Modifier.isPublic(modifiers));
            classInfo.setAbstract(Modifier.isAbstract(modifiers));
            classInfo.setFinal(Modifier.isFinal(modifiers));
            
            // Analizza annotazioni Spring
            analyzeSpringAnnotations(ctClass, classInfo, report);
            
            // Analizza inheritance hierarchy
            analyzeInheritanceHierarchy(ctClass, classInfo);
            
            // Analizza inner classes
            analyzeInnerClasses(ctClass, classInfo);
            
            // Conta metodi e campi
            countMembersAndComplexity(ctClass, classInfo);
            
            // Verifica naming conventions
            validateNamingConventions(ctClass, classInfo, report);
            
            return classInfo;
            
        } catch (Exception e) {
            ClassInfo errorInfo = new ClassInfo();
            errorInfo.setClassName(ctClass.getName());
            errorInfo.addError("Errore nell'analisi classe: " + e.getMessage());
            return errorInfo;
        }
    }
    
    private ClassType determineClassType(CtClass ctClass) {
        try {
            if (ctClass.isInterface()) {
                return ClassType.INTERFACE;
            } else if (ctClass.isEnum()) {
                return ClassType.ENUM;
            } else if (ctClass.isAnnotation()) {
                return ClassType.ANNOTATION;
            } else if (Modifier.isAbstract(ctClass.getModifiers())) {
                return ClassType.ABSTRACT_CLASS;
            } else {
                return ClassType.CONCRETE_CLASS;
            }
        } catch (Exception e) {
            return ClassType.UNKNOWN;
        }
    }
    
    private void analyzeSpringAnnotations(CtClass ctClass, ClassInfo classInfo, PackageHierarchyReport report) {
        try {
            List<String> springAnnotations = new ArrayList<>();
            
            // Check common Spring annotations
            String[] commonAnnotations = {
                "org.springframework.stereotype.Component",
                "org.springframework.stereotype.Service", 
                "org.springframework.stereotype.Repository",
                "org.springframework.stereotype.Controller",
                "org.springframework.web.bind.annotation.RestController",
                "org.springframework.boot.autoconfigure.SpringBootApplication",
                "org.springframework.context.annotation.Configuration"
            };
            
            for (String annotation : commonAnnotations) {
                if (ctClass.hasAnnotation(annotation)) {
                    springAnnotations.add(annotation.substring(annotation.lastIndexOf('.') + 1));
                }
            }
            
            classInfo.setSpringAnnotations(springAnnotations);
            
            // Determina architectural layer basato su annotazioni
            String architecturalLayer = determineArchitecturalLayer(springAnnotations, ctClass.getName());
            classInfo.setArchitecturalLayer(architecturalLayer);
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi Spring annotations: " + e.getMessage());
        }
    }
    
    private String determineArchitecturalLayer(List<String> springAnnotations, String className) {
        for (String annotation : springAnnotations) {
            switch (annotation) {
                case "Controller":
                case "RestController":
                    return "PRESENTATION";
                case "Service":
                    return "SERVICE";
                case "Repository":
                    return "DATA";
                case "Component":
                    return "COMPONENT";
                case "Configuration":
                    return "CONFIGURATION";
            }
        }
        
        // Fallback basato su naming convention
        if (className.contains("Controller")) return "PRESENTATION";
        if (className.contains("Service")) return "SERVICE";
        if (className.contains("Repository") || className.contains("Dao")) return "DATA";
        if (className.contains("Config")) return "CONFIGURATION";
        if (className.contains("Dto") || className.contains("Entity")) return "MODEL";
        
        return "UNCLASSIFIED";
    }
    
    private void analyzeInheritanceHierarchy(CtClass ctClass, ClassInfo classInfo) {
        try {
            // Superclass analysis
            CtClass superClass = ctClass.getSuperclass();
            if (superClass != null && !"java.lang.Object".equals(superClass.getName())) {
                classInfo.setSuperClassName(superClass.getName());
            }
            
            // Interfaces analysis
            CtClass[] interfaces = ctClass.getInterfaces();
            List<String> interfaceNames = new ArrayList<>();
            for (CtClass interfaceClass : interfaces) {
                interfaceNames.add(interfaceClass.getName());
            }
            classInfo.setImplementedInterfaces(interfaceNames);
            
            // Calculate inheritance depth
            int depth = calculateInheritanceDepth(ctClass);
            classInfo.setInheritanceDepth(depth);
            
        } catch (Exception e) {
            classInfo.addError("Errore nell'analisi inheritance: " + e.getMessage());
        }
    }
    
    private int calculateInheritanceDepth(CtClass ctClass) {
        int depth = 0;
        try {
            CtClass current = ctClass.getSuperclass();
            while (current != null && !"java.lang.Object".equals(current.getName())) {
                depth++;
                current = current.getSuperclass();
            }
        } catch (Exception e) {
            // Ignore errors in depth calculation
        }
        return depth;
    }
    
    private void analyzeInnerClasses(CtClass ctClass, ClassInfo classInfo) {
        try {
            CtClass[] nestedClasses = ctClass.getNestedClasses();
            List<String> innerClassNames = new ArrayList<>();
            
            for (CtClass nested : nestedClasses) {
                innerClassNames.add(nested.getSimpleName());
            }
            
            classInfo.setInnerClasses(innerClassNames);
            classInfo.setHasInnerClasses(!innerClassNames.isEmpty());
            
        } catch (Exception e) {
            classInfo.addError("Errore nell'analisi inner classes: " + e.getMessage());
        }
    }
    
    private void countMembersAndComplexity(CtClass ctClass, ClassInfo classInfo) {
        try {
            // Count methods
            CtMethod[] methods = ctClass.getDeclaredMethods();
            classInfo.setMethodCount(methods.length);
            
            // Count fields
            CtField[] fields = ctClass.getDeclaredFields();
            classInfo.setFieldCount(fields.length);
            
            // Count constructors
            CtConstructor[] constructors = ctClass.getDeclaredConstructors();
            classInfo.setConstructorCount(constructors.length);
            
            // Estimate complexity (simple heuristic)
            int complexity = methods.length + fields.length * 2 + constructors.length;
            classInfo.setComplexityScore(complexity);
            
            // Estimate lines of code (rough approximation)
            int estimatedLoc = methods.length * 10 + fields.length * 2 + constructors.length * 5;
            classInfo.setEstimatedLinesOfCode(estimatedLoc);
            
        } catch (Exception e) {
            classInfo.addError("Errore nel conteggio membri: " + e.getMessage());
        }
    }
    
    private void validateNamingConventions(CtClass ctClass, ClassInfo classInfo, PackageHierarchyReport report) {
        try {
            String className = ctClass.getSimpleName();
            String packageName = ctClass.getPackageName();
            
            List<NamingIssue> namingIssues = new ArrayList<>();
            
            // Check class naming conventions
            if (!Character.isUpperCase(className.charAt(0))) {
                namingIssues.add(new NamingIssue(
                    "CLASS_NAME_NOT_CAPITALIZED",
                    "Class name should start with uppercase letter",
                    className
                ));
            }
            
            // Check package naming conventions
            if (!packageName.toLowerCase().equals(packageName)) {
                namingIssues.add(new NamingIssue(
                    "PACKAGE_NAME_NOT_LOWERCASE",
                    "Package name should be lowercase", 
                    packageName
                ));
            }
            
            // Check for abbreviations and acronyms
            if (containsInappropriateAbbreviations(className)) {
                namingIssues.add(new NamingIssue(
                    "INAPPROPRIATE_ABBREVIATIONS",
                    "Class name contains inappropriate abbreviations",
                    className
                ));
            }
            
            // Check architectural consistency
            validateArchitecturalNaming(classInfo, namingIssues);
            
            classInfo.setNamingIssues(namingIssues);
            
        } catch (Exception e) {
            report.addError("Errore nella validazione naming: " + e.getMessage());
        }
    }
    
    private void validateArchitecturalNaming(ClassInfo classInfo, List<NamingIssue> namingIssues) {
        String className = classInfo.getSimpleName();
        String layer = classInfo.getArchitecturalLayer();
        
        // Check consistency between layer and naming
        if ("PRESENTATION".equals(layer) && !className.contains("Controller")) {
            namingIssues.add(new NamingIssue(
                "INCONSISTENT_CONTROLLER_NAMING",
                "Presentation layer class should contain 'Controller' in name",
                className
            ));
        }
        
        if ("SERVICE".equals(layer) && !className.contains("Service")) {
            namingIssues.add(new NamingIssue(
                "INCONSISTENT_SERVICE_NAMING", 
                "Service layer class should contain 'Service' in name",
                className
            ));
        }
        
        if ("DATA".equals(layer) && !className.contains("Repository") && !className.contains("Dao")) {
            namingIssues.add(new NamingIssue(
                "INCONSISTENT_REPOSITORY_NAMING",
                "Data layer class should contain 'Repository' or 'Dao' in name", 
                className
            ));
        }
    }
    
    private void analyzePackageStructure(Map<String, PackageInfo> packageMap, PackageHierarchyReport report) {
        try {
            for (PackageInfo packageInfo : packageMap.values()) {
                // Calculate package metrics
                calculatePackageMetrics(packageInfo);
                
                // Analyze package organization
                analyzePackageOrganization(packageInfo, report);
                
                // Check package size
                validatePackageSize(packageInfo, report);
            }
            
            // Analyze package hierarchy depth
            analyzePackageDepth(packageMap, report);
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi struttura package: " + e.getMessage());
        }
    }
    
    private void calculatePackageMetrics(PackageInfo packageInfo) {
        List<ClassInfo> classes = packageInfo.getClasses();
        
        int totalMethods = classes.stream().mapToInt(ClassInfo::getMethodCount).sum();
        int totalFields = classes.stream().mapToInt(ClassInfo::getFieldCount).sum();
        int totalLoc = classes.stream().mapToInt(ClassInfo::getEstimatedLinesOfCode).sum();
        
        packageInfo.setTotalMethods(totalMethods);
        packageInfo.setTotalFields(totalFields);
        packageInfo.setTotalLinesOfCode(totalLoc);
        
        // Calculate average complexity
        double avgComplexity = classes.stream()
            .mapToInt(ClassInfo::getComplexityScore)
            .average()
            .orElse(0.0);
        packageInfo.setAverageComplexity(avgComplexity);
    }
    
    private void identifyArchitecturalIssues(Map<String, PackageInfo> packageMap, PackageHierarchyReport report) {
        try {
            // Check for layer violations
            checkLayerViolations(packageMap, report);
            
            // Check for circular dependencies (basic check)
            checkCircularDependencies(packageMap, report);
            
            // Check for oversized packages
            checkOversizedPackages(packageMap, report);
            
            // Check for naming inconsistencies
            checkNamingInconsistencies(packageMap, report);
            
        } catch (Exception e) {
            report.addError("Errore nell'identificazione problemi architetturali: " + e.getMessage());
        }
    }
    
    private void checkLayerViolations(Map<String, PackageInfo> packageMap, PackageHierarchyReport report) {
        for (PackageInfo packageInfo : packageMap.values()) {
            Set<String> layersInPackage = new HashSet<>();
            
            for (ClassInfo classInfo : packageInfo.getClasses()) {
                String layer = classInfo.getArchitecturalLayer();
                if (!"UNCLASSIFIED".equals(layer)) {
                    layersInPackage.add(layer);
                }
            }
            
            if (layersInPackage.size() > 1) {
                ArchitecturalIssue issue = new ArchitecturalIssue();
                issue.setType(IssueType.MIXED_LAYERS_IN_PACKAGE);
                issue.setPackageName(packageInfo.getPackageName());
                issue.setSeverity(Severity.MEDIUM);
                issue.setDescription("Package contains classes from multiple architectural layers: " + layersInPackage);
                issue.setRecommendation("Separate classes into layer-specific packages");
                
                report.addArchitecturalIssue(issue);
            }
        }
    }
}

public class PackageHierarchyReport {
    private Map<String, PackageInfo> packages = new HashMap<>();
    private List<ArchitecturalIssue> architecturalIssues = new ArrayList<>();
    private PackageStatistics packageStatistics;
    private List<String> errors = new ArrayList<>();
    
    public static class PackageInfo {
        private String packageName;
        private List<ClassInfo> classes = new ArrayList<>();
        private int totalMethods;
        private int totalFields;
        private int totalLinesOfCode;
        private double averageComplexity;
        private Set<String> architecturalLayers = new HashSet<>();
    }
    
    public static class ClassInfo {
        private String className;
        private String simpleName;
        private String packageName;
        private ClassType classType;
        private String architecturalLayer;
        private boolean isPublic;
        private boolean isAbstract;
        private boolean isFinal;
        private String superClassName;
        private List<String> implementedInterfaces = new ArrayList<>();
        private List<String> springAnnotations = new ArrayList<>();
        private List<String> innerClasses = new ArrayList<>();
        private List<NamingIssue> namingIssues = new ArrayList<>();
        private int methodCount;
        private int fieldCount;
        private int constructorCount;
        private int complexityScore;
        private int estimatedLinesOfCode;
        private int inheritanceDepth;
        private boolean hasInnerClasses;
        private List<String> errors = new ArrayList<>();
    }
    
    public static class ArchitecturalIssue {
        private IssueType type;
        private String packageName;
        private String className;
        private Severity severity;
        private String description;
        private String recommendation;
    }
    
    public enum ClassType {
        CONCRETE_CLASS, ABSTRACT_CLASS, INTERFACE, ENUM, ANNOTATION, UNKNOWN
    }
    
    public enum IssueType {
        MIXED_LAYERS_IN_PACKAGE,
        OVERSIZED_PACKAGE,
        NAMING_INCONSISTENCY,
        EXCESSIVE_PACKAGE_DEPTH,
        CIRCULAR_PACKAGE_DEPENDENCY,
        INAPPROPRIATE_CLASS_PLACEMENT
    }
    
    public enum Severity {
        CRITICAL, HIGH, MEDIUM, LOW
    }
}
```

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculatePackageHierarchyQualityScore(PackageHierarchyReport result) {
    double score = 100.0;
    
    // Penalizzazioni per problemi architetturali critici
    score -= result.getMixedLayersInPackages() * 15;              // -15 per package con layer mescolati
    score -= result.getOversizedPackages() * 12;                 // -12 per package troppo grandi
    score -= result.getExcessivePackageDepth() * 10;             // -10 per profondità package eccessiva
    score -= result.getCircularPackageDependencies() * 18;       // -18 per dipendenze circolari
    score -= result.getNamingInconsistencies() * 8;              // -8 per inconsistenze naming
    score -= result.getInappropriateClassPlacements() * 6;       // -6 per classi in package inappropriati
    score -= result.getClassesWithoutLayer() * 5;                // -5 per classi senza layer definito
    
    // Bonus per buone pratiche architetturali
    score += result.getWellOrganizedPackages() * 3;              // +3 per package ben organizzati
    score += result.getConsistentNamingConventions() * 2;        // +2 per naming conventions consistenti
    score += result.getProperLayerSeparation() * 3;              // +3 per separazione layer appropriata
    score += result.getOptimalPackageSizes() * 1;                // +1 per dimensioni package ottimali
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - Architettura package problematica con gravi violazioni
- **41-60**: 🟡 SUFFICIENTE - Organizzazione di base ma con lacune architetturali
- **61-80**: 🟢 BUONO - Struttura package ben organizzata con alcuni miglioramenti
- **81-100**: ⭐ ECCELLENTE - Architettura package ottimale e ben strutturata

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -15 to -18)
1. **Dipendenze circolari tra package**
   - Descrizione: Package che si referenziano reciprocamente creando cicli
   - Rischio: Accoppiamento forte, difficile refactoring, build issues
   - Soluzione: Ristrutturare architettura per eliminare dipendenze circolari

2. **Layer mescolati nello stesso package**
   - Descrizione: Controller, Service e Repository nello stesso package
   - Rischio: Violazione separation of concerns, difficile manutenzione
   - Soluzione: Separare classi in package specifici per layer

### 🟠 GRAVITÀ ALTA (Score Impact: -10 to -12)  
3. **Package troppo grandi**
   - Descrizione: Package con oltre 50 classi o 10,000 LOC
   - Rischio: Difficult navigation, poor cohesion, refactoring complexity
   - Soluzione: Scomporre package grandi in sub-package logici

4. **Profondità package eccessiva**
   - Descrizione: Gerarchia package con oltre 6-7 livelli di nesting
   - Rischio: Navigation complessa, path lunghi, confusion
   - Soluzione: Semplificare struttura gerarchica package

### 🟡 GRAVITÀ MEDIA (Score Impact: -6 to -8)
5. **Inconsistenze nelle naming conventions**
   - Descrizione: Package/classi che non seguono convenzioni standard
   - Rischio: Reduced readability, team confusion, maintenance issues
   - Soluzione: Standardizzare naming conventions across codebase

6. **Classi in package inappropriati**
   - Descrizione: Classi posizionate in package non coerenti con la funzione
   - Rischio: Logical confusion, difficult code discovery
   - Soluzione: Riposizionare classi in package appropriati

### 🔵 GRAVITÀ BASSA (Score Impact: -5)
7. **Classi senza layer architetturale definito**
   - Descrizione: Classi che non appartengono chiaramente a un layer
   - Rischio: Architectural ambiguity, unclear responsibilities
   - Soluzione: Definire layer appropriato o ristrutturare responsabilità

## Metriche di Valore

- **Code Organization**: Migliora navigabilità e comprensibilità del codice
- **Architectural Clarity**: Fornisce visione chiara della struttura applicativa
- **Maintenance Efficiency**: Facilita modifiche e refactoring futuri
- **Team Collaboration**: Standardizza organizzazione per team development

## Tags per Classificazione
`#package-structure` `#class-hierarchy` `#architecture` `#code-organization` `#medium-complexity` `#high-value`