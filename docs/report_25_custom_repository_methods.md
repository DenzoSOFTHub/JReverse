# Report 25: Analisi Metodi Custom nei Repository

**Valore**: ⭐⭐⭐ **Complessità**: 🟡 Media **Tempo**: 4-5 giorni
**Tags**: `#custom-repositories` `#spring-data` `#query-methods`

## Descrizione

Questo report analizza tutti i metodi custom implementati nei repository Spring Data, identificando pattern di implementazione, complessità dei metodi, conformità alle convenzioni Spring Data e potenziali problemi di performance o manutenibilità.

## Obiettivi dell'Analisi

1. **Identificazione Repository Custom**: Trovare tutte le interfacce repository che estendono funzionalità base
2. **Analisi Metodi Query**: Valutare metodi derived query e custom implementations
3. **Pattern Recognition**: Identificare pattern comuni e anti-pattern nei repository
4. **Performance Assessment**: Valutare efficienza dei metodi custom implementati
5. **Compliance Check**: Verificare conformità alle best practices Spring Data

## Implementazione con Javassist

```java
package com.jreverse.analyzer.repository;

import javassist.*;
import javassist.bytecode.*;
import javassist.expr.*;
import java.util.*;
import java.util.regex.*;
import java.io.IOException;

/**
 * Analyzer per metodi custom nei repository Spring Data
 */
public class CustomRepositoryMethodsAnalyzer {
    
    private ClassPool classPool;
    private Map<String, RepositoryInfo> customRepositories;
    private Map<String, List<CustomMethod>> customMethods;
    private Map<String, MethodComplexity> methodComplexities;
    private Set<String> derivedQueryMethods;
    private Set<String> customImplementations;
    private List<RepositoryIssue> repositoryIssues;
    
    // Pattern per identificare metodi derived query
    private static final Pattern DERIVED_QUERY_PATTERN = Pattern.compile(
        "^(find|read|get|query|stream|count|exists|delete|remove).*By.*$"
    );
    
    // Pattern per identificare naming conventions
    private static final Pattern NAMING_CONVENTION_PATTERN = Pattern.compile(
        "^(find|read|get|query|stream|count|exists|delete|remove)" +
        "(All|First|Top\\d*|Distinct)*" +
        "By" +
        "([A-Z][a-z]+)" +
        "(And|Or[A-Z][a-z]+)*" +
        "(OrderBy[A-Z][a-z]+(Asc|Desc))*$"
    );
    
    // Operatori supportati in derived queries
    private static final Set<String> SUPPORTED_OPERATORS = Set.of(
        "Equals", "Is", "Not", "IsNull", "IsNotNull", "True", "False",
        "LessThan", "LessThanEqual", "GreaterThan", "GreaterThanEqual",
        "Between", "DateBefore", "DateAfter", "Like", "NotLike",
        "StartingWith", "EndingWith", "Containing", "In", "NotIn",
        "IgnoreCase", "AllIgnoreCase", "OrderBy"
    );
    
    public CustomRepositoryMethodsAnalyzer() {
        this.classPool = ClassPool.getDefault();
        this.customRepositories = new HashMap<>();
        this.customMethods = new HashMap<>();
        this.methodComplexities = new HashMap<>();
        this.derivedQueryMethods = new HashSet<>();
        this.customImplementations = new HashSet<>();
        this.repositoryIssues = new ArrayList<>();
    }
    
    /**
     * Analizza tutti i repository custom nell'applicazione
     */
    public RepositoryAnalysisReport analyzeCustomRepositories(List<String> classNames) throws Exception {
        // Prima fase: identifica tutti i repository
        Set<String> repositoryInterfaces = identifyRepositoryInterfaces(classNames);
        
        // Seconda fase: analizza metodi custom
        for (String repoClass : repositoryInterfaces) {
            analyzeRepositoryMethods(repoClass);
        }
        
        // Terza fase: trova implementazioni custom
        for (String className : classNames) {
            if (className.endsWith("Impl") || className.contains("RepositoryImpl")) {
                analyzeCustomImplementation(className);
            }
        }
        
        return generateReport();
    }
    
    /**
     * Identifica tutte le interfacce repository
     */
    private Set<String> identifyRepositoryInterfaces(List<String> classNames) throws Exception {
        Set<String> repositories = new HashSet<>();
        
        for (String className : classNames) {
            try {
                CtClass ctClass = classPool.get(className);
                if (isRepositoryInterface(ctClass)) {
                    repositories.add(className);
                    analyzeRepositoryStructure(ctClass);
                }
            } catch (Exception e) {
                System.err.println("Errore nell'analisi della classe " + className + ": " + e.getMessage());
            }
        }
        
        return repositories;
    }
    
    /**
     * Verifica se una classe è un'interfaccia repository
     */
    private boolean isRepositoryInterface(CtClass ctClass) throws Exception {
        if (!ctClass.isInterface()) {
            return false;
        }
        
        // Verifica se estende Repository, CrudRepository, JpaRepository, etc.
        CtClass[] interfaces = ctClass.getInterfaces();
        for (CtClass iface : interfaces) {
            String ifaceName = iface.getName();
            if (ifaceName.contains("Repository") || ifaceName.contains("CrudRepository") ||
                ifaceName.contains("JpaRepository") || ifaceName.contains("PagingAndSortingRepository")) {
                return true;
            }
        }
        
        // Verifica annotation @Repository
        AnnotationsAttribute attr = (AnnotationsAttribute)
            ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        
        if (attr != null) {
            return attr.getAnnotation("org.springframework.stereotype.Repository") != null;
        }
        
        return false;
    }
    
    /**
     * Analizza la struttura di un repository
     */
    private void analyzeRepositoryStructure(CtClass repoClass) throws Exception {
        String repoName = repoClass.getName();
        RepositoryInfo info = new RepositoryInfo(repoName);
        
        // Identifica entity type
        info.entityType = extractEntityType(repoClass);
        
        // Analizza inheritance hierarchy
        info.baseRepositoryTypes = getBaseRepositoryTypes(repoClass);
        
        // Conta metodi custom vs derived
        for (CtMethod method : repoClass.getDeclaredMethods()) {
            if (isDerivedQueryMethod(method.getName())) {
                info.derivedQueryCount++;
                derivedQueryMethods.add(repoName + "." + method.getName());
                analyzeDerivedQueryMethod(repoName, method);
            } else {
                info.customMethodCount++;
                analyzeCustomMethod(repoName, method);
            }
        }
        
        customRepositories.put(repoName, info);
    }
    
    /**
     * Estrae il tipo di entity dal repository
     */
    private String extractEntityType(CtClass repoClass) throws Exception {
        // Analizza signature generica del repository
        SignatureAttribute sig = (SignatureAttribute) 
            repoClass.getClassFile().getAttribute(SignatureAttribute.tag);
        
        if (sig != null) {
            String signature = sig.getSignature();
            // Esempio: Ljava/lang/Object;Lcom/example/User;Ljava/lang/Long;
            Pattern entityPattern = Pattern.compile("L([^;]+);L([^;]+);");
            Matcher matcher = entityPattern.matcher(signature);
            if (matcher.find()) {
                return matcher.group(2).replace('/', '.');
            }
        }
        
        return "Unknown";
    }
    
    /**
     * Ottiene i tipi di repository base estesi
     */
    private List<String> getBaseRepositoryTypes(CtClass repoClass) throws Exception {
        List<String> baseTypes = new ArrayList<>();
        CtClass[] interfaces = repoClass.getInterfaces();
        
        for (CtClass iface : interfaces) {
            String name = iface.getName();
            if (name.endsWith("Repository")) {
                baseTypes.add(name);
            }
        }
        
        return baseTypes;
    }
    
    /**
     * Verifica se un metodo è una derived query
     */
    private boolean isDerivedQueryMethod(String methodName) {
        return DERIVED_QUERY_PATTERN.matcher(methodName).matches();
    }
    
    /**
     * Analizza un metodo derived query
     */
    private void analyzeDerivedQueryMethod(String repoName, CtMethod method) throws Exception {
        String methodName = method.getName();
        String methodId = repoName + "." + methodName;
        
        // Verifica naming convention
        if (!NAMING_CONVENTION_PATTERN.matcher(methodName).matches()) {
            repositoryIssues.add(new RepositoryIssue(
                IssueLevel.MEDIUM,
                "Naming convention non standard per derived query: " + methodName,
                methodId
            ));
        }
        
        // Analizza complessità del nome metodo
        int complexity = analyzeDerivedQueryComplexity(methodName);
        if (complexity > 10) {
            repositoryIssues.add(new RepositoryIssue(
                IssueLevel.HIGH,
                "Derived query troppo complessa, considera @Query: " + methodName,
                methodId
            ));
        }
        
        // Verifica parametri
        analyzeMethodParameters(repoName, method);
        
        // Verifica return type appropriato
        analyzeReturnType(repoName, method);
    }
    
    /**
     * Analizza complessità di una derived query
     */
    private int analyzeDerivedQueryComplexity(String methodName) {
        int complexity = 0;
        
        // Conta And/Or operators
        complexity += countOccurrences(methodName, "And") * 2;
        complexity += countOccurrences(methodName, "Or") * 3;
        
        // Conta operatori complessi
        for (String operator : SUPPORTED_OPERATORS) {
            if (methodName.contains(operator)) {
                complexity += getOperatorComplexity(operator);
            }
        }
        
        // Penalità per lunghezza nome
        complexity += methodName.length() / 10;
        
        return complexity;
    }
    
    /**
     * Ottiene complessità di un operatore
     */
    private int getOperatorComplexity(String operator) {
        switch (operator) {
            case "Between": return 3;
            case "Like": case "NotLike": return 2;
            case "In": case "NotIn": return 2;
            case "IgnoreCase": case "AllIgnoreCase": return 2;
            case "OrderBy": return 1;
            default: return 1;
        }
    }
    
    /**
     * Analizza un metodo custom
     */
    private void analyzeCustomMethod(String repoName, CtMethod method) throws Exception {
        String methodName = method.getName();
        String methodId = repoName + "." + methodName;
        
        CustomMethod customMethod = new CustomMethod(methodId, methodName, repoName);
        
        // Verifica se ha annotation @Query
        AnnotationsAttribute attr = (AnnotationsAttribute)
            method.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
        
        if (attr != null) {
            Annotation queryAnnotation = attr.getAnnotation("org.springframework.data.jpa.repository.Query");
            if (queryAnnotation != null) {
                customMethod.hasQueryAnnotation = true;
                customMethod.queryString = getAnnotationValue(queryAnnotation, "value");
                customMethod.isNativeQuery = getBooleanAnnotationValue(queryAnnotation, "nativeQuery");
            }
            
            // Verifica @Modifying
            if (attr.getAnnotation("org.springframework.data.jpa.repository.Modifying") != null) {
                customMethod.isModifying = true;
            }
        }
        
        // Analizza parametri
        customMethod.parameterCount = method.getParameterTypes().length;
        customMethod.parameterTypes = getParameterTypeNames(method);
        
        // Analizza return type
        customMethod.returnType = method.getReturnType().getName();
        analyzeReturnTypeComplexity(customMethod);
        
        // Calcola score di qualità
        customMethod.qualityScore = calculateMethodQualityScore(customMethod);
        
        customMethods.computeIfAbsent(repoName, k -> new ArrayList<>()).add(customMethod);
    }
    
    /**
     * Analizza implementazione custom
     */
    private void analyzeCustomImplementation(String implClassName) throws Exception {
        try {
            CtClass implClass = classPool.get(implClassName);
            customImplementations.add(implClassName);
            
            // Trova l'interfaccia corrispondente
            String interfaceName = implClassName.replace("Impl", "");
            
            for (CtMethod method : implClass.getDeclaredMethods()) {
                analyzeImplementationMethod(interfaceName, method);
            }
            
        } catch (Exception e) {
            System.err.println("Errore nell'analisi implementazione " + implClassName + ": " + e.getMessage());
        }
    }
    
    /**
     * Analizza metodo di implementazione
     */
    private void analyzeImplementationMethod(String interfaceName, CtMethod method) throws Exception {
        String methodName = method.getName();
        String methodId = interfaceName + "." + methodName;
        
        MethodComplexity complexity = new MethodComplexity(methodId);
        
        // Analizza complessità ciclomatica
        complexity.cyclomaticComplexity = calculateCyclomaticComplexity(method);
        
        // Analizza chiamate EntityManager/Session
        complexity.entityManagerCalls = countEntityManagerCalls(method);
        
        // Analizza uso di criteria API
        complexity.usesCriteriaAPI = usesCriteriaAPI(method);
        
        // Analizza transactionality
        complexity.isTransactional = isTransactional(method);
        
        // Calcola score complessivo
        complexity.overallScore = calculateComplexityScore(complexity);
        
        methodComplexities.put(methodId, complexity);
        
        // Verifica problemi comuni
        checkCommonImplementationIssues(methodId, method);
    }
    
    /**
     * Calcola complessità ciclomatica
     */
    private int calculateCyclomaticComplexity(CtMethod method) throws Exception {
        final int[] complexity = {1}; // Base complexity
        
        method.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                // Conta chiamate condizionali e cicli
                String methodName = m.getMethodName();
                if (methodName.equals("if") || methodName.equals("while") || 
                    methodName.equals("for") || methodName.equals("switch")) {
                    complexity[0]++;
                }
            }
            
            @Override
            public void edit(Handler handler) throws CannotCompileException {
                // Conta exception handlers
                complexity[0]++;
            }
        });
        
        return complexity[0];
    }
    
    /**
     * Conta chiamate EntityManager
     */
    private int countEntityManagerCalls(CtMethod method) throws Exception {
        final int[] count = {0};
        
        method.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                if (m.getClassName().contains("EntityManager") || 
                    m.getClassName().contains("Session")) {
                    count[0]++;
                }
            }
        });
        
        return count[0];
    }
    
    /**
     * Verifica uso Criteria API
     */
    private boolean usesCriteriaAPI(CtMethod method) throws Exception {
        final boolean[] uses = {false};
        
        method.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                if (m.getClassName().contains("CriteriaBuilder") ||
                    m.getClassName().contains("CriteriaQuery") ||
                    m.getClassName().contains("Root")) {
                    uses[0] = true;
                }
            }
        });
        
        return uses[0];
    }
    
    /**
     * Verifica se il metodo è transazionale
     */
    private boolean isTransactional(CtMethod method) throws Exception {
        AnnotationsAttribute attr = (AnnotationsAttribute)
            method.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
        
        if (attr != null) {
            return attr.getAnnotation("org.springframework.transaction.annotation.Transactional") != null;
        }
        
        return false;
    }
    
    /**
     * Calcola score di qualità per un metodo
     */
    private int calculateMethodQualityScore(CustomMethod method) {
        int score = 100;
        
        // Penalità per mancanza @Query su metodi custom
        if (!method.hasQueryAnnotation && !isDerivedQueryMethod(method.methodName)) {
            score -= 20;
        }
        
        // Penalità per troppi parametri
        if (method.parameterCount > 5) {
            score -= (method.parameterCount - 5) * 5;
        }
        
        // Penalità per return type complesso
        if (method.returnType.contains("Object") || method.returnType.contains("Map")) {
            score -= 15;
        }
        
        // Bonus per naming convention
        if (DERIVED_QUERY_PATTERN.matcher(method.methodName).matches()) {
            score += 10;
        }
        
        // Bonus per native query appropriata
        if (method.isNativeQuery && method.queryString != null && 
            method.queryString.toLowerCase().contains("performance")) {
            score += 5;
        }
        
        return Math.max(0, Math.min(100, score));
    }
    
    /**
     * Calcola score di complessità
     */
    private int calculateComplexityScore(MethodComplexity complexity) {
        int score = 100;
        
        // Penalità per alta complessità ciclomatica
        if (complexity.cyclomaticComplexity > 10) {
            score -= (complexity.cyclomaticComplexity - 10) * 5;
        }
        
        // Penalità per troppe chiamate EntityManager
        if (complexity.entityManagerCalls > 5) {
            score -= (complexity.entityManagerCalls - 5) * 3;
        }
        
        // Bonus per uso Criteria API
        if (complexity.usesCriteriaAPI) {
            score += 10;
        }
        
        // Bonus per transactionality
        if (complexity.isTransactional) {
            score += 5;
        }
        
        return Math.max(0, Math.min(100, score));
    }
    
    /**
     * Verifica problemi comuni nelle implementazioni
     */
    private void checkCommonImplementationIssues(String methodId, CtMethod method) throws Exception {
        // N+1 Query Problem
        if (hasN1QueryProblem(method)) {
            repositoryIssues.add(new RepositoryIssue(
                IssueLevel.HIGH,
                "Potenziale problema N+1 query detectato",
                methodId
            ));
        }
        
        // Lazy loading issues
        if (hasLazyLoadingIssues(method)) {
            repositoryIssues.add(new RepositoryIssue(
                IssueLevel.MEDIUM,
                "Potenziali problemi di lazy loading",
                methodId
            ));
        }
        
        // Missing pagination
        if (shouldHavePagination(method) && !hasPagination(method)) {
            repositoryIssues.add(new RepositoryIssue(
                IssueLevel.MEDIUM,
                "Metodo dovrebbe supportare paginazione",
                methodId
            ));
        }
    }
    
    // Metodi di utilità per pattern detection
    private boolean hasN1QueryProblem(CtMethod method) throws Exception {
        // Logica per rilevare N+1 query problems
        final boolean[] hasIssue = {false};
        
        method.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                // Rileva pattern di multiple query in loop
                if ((m.getMethodName().equals("find") || m.getMethodName().equals("get")) &&
                    isInLoop(m.getLineNumber())) {
                    hasIssue[0] = true;
                }
            }
        });
        
        return hasIssue[0];
    }
    
    private boolean hasLazyLoadingIssues(CtMethod method) throws Exception {
        // Implementazione per rilevare lazy loading issues
        return false; // Simplified
    }
    
    private boolean shouldHavePagination(CtMethod method) throws Exception {
        // Verifica se il metodo dovrebbe supportare paginazione
        return method.getName().startsWith("findAll") || 
               method.getName().contains("List") ||
               method.getName().contains("Search");
    }
    
    private boolean hasPagination(CtMethod method) throws Exception {
        CtClass[] paramTypes = method.getParameterTypes();
        for (CtClass param : paramTypes) {
            if (param.getName().contains("Pageable") || param.getName().contains("Page")) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isInLoop(int lineNumber) {
        // Logica semplificata per rilevare se siamo in un loop
        return false; // Simplified
    }
    
    // Metodi utility
    private int countOccurrences(String text, String pattern) {
        return text.split(pattern, -1).length - 1;
    }
    
    private String getAnnotationValue(Annotation annotation, String memberName) {
        try {
            return annotation.getMemberValue(memberName).toString().replaceAll("\"", "");
        } catch (Exception e) {
            return null;
        }
    }
    
    private boolean getBooleanAnnotationValue(Annotation annotation, String memberName) {
        try {
            return Boolean.parseBoolean(annotation.getMemberValue(memberName).toString());
        } catch (Exception e) {
            return false;
        }
    }
    
    private List<String> getParameterTypeNames(CtMethod method) throws Exception {
        List<String> types = new ArrayList<>();
        for (CtClass param : method.getParameterTypes()) {
            types.add(param.getName());
        }
        return types;
    }
    
    private void analyzeMethodParameters(String repoName, CtMethod method) throws Exception {
        // Implementazione analisi parametri
    }
    
    private void analyzeReturnType(String repoName, CtMethod method) throws Exception {
        // Implementazione analisi return type
    }
    
    private void analyzeReturnTypeComplexity(CustomMethod method) {
        // Implementazione analisi complessità return type
    }
    
    /**
     * Genera il report finale
     */
    private RepositoryAnalysisReport generateReport() {
        RepositoryAnalysisReport report = new RepositoryAnalysisReport();
        
        report.totalRepositories = customRepositories.size();
        report.totalCustomMethods = customMethods.values().stream()
            .mapToInt(List::size).sum();
        report.totalDerivedQueries = derivedQueryMethods.size();
        report.totalCustomImplementations = customImplementations.size();
        
        report.customRepositories = new HashMap<>(customRepositories);
        report.customMethods = new HashMap<>(customMethods);
        report.methodComplexities = new HashMap<>(methodComplexities);
        report.repositoryIssues = new ArrayList<>(repositoryIssues);
        
        // Calcolo metriche aggregate
        report.averageMethodQuality = calculateAverageMethodQuality();
        report.complexityDistribution = calculateComplexityDistribution();
        report.issuesByLevel = groupIssuesByLevel();
        
        return report;
    }
    
    private double calculateAverageMethodQuality() {
        return customMethods.values().stream()
            .flatMap(List::stream)
            .mapToInt(m -> m.qualityScore)
            .average()
            .orElse(0.0);
    }
    
    private Map<String, Integer> calculateComplexityDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("LOW", 0);
        distribution.put("MEDIUM", 0);
        distribution.put("HIGH", 0);
        distribution.put("VERY_HIGH", 0);
        
        for (MethodComplexity complexity : methodComplexities.values()) {
            if (complexity.cyclomaticComplexity <= 5) {
                distribution.merge("LOW", 1, Integer::sum);
            } else if (complexity.cyclomaticComplexity <= 10) {
                distribution.merge("MEDIUM", 1, Integer::sum);
            } else if (complexity.cyclomaticComplexity <= 15) {
                distribution.merge("HIGH", 1, Integer::sum);
            } else {
                distribution.merge("VERY_HIGH", 1, Integer::sum);
            }
        }
        
        return distribution;
    }
    
    private Map<IssueLevel, Integer> groupIssuesByLevel() {
        Map<IssueLevel, Integer> grouped = new HashMap<>();
        for (RepositoryIssue issue : repositoryIssues) {
            grouped.merge(issue.level, 1, Integer::sum);
        }
        return grouped;
    }
    
    // Classi di supporto
    public static class RepositoryInfo {
        public String repositoryName;
        public String entityType;
        public List<String> baseRepositoryTypes;
        public int customMethodCount;
        public int derivedQueryCount;
        
        public RepositoryInfo(String repositoryName) {
            this.repositoryName = repositoryName;
            this.baseRepositoryTypes = new ArrayList<>();
        }
    }
    
    public static class CustomMethod {
        public String methodId;
        public String methodName;
        public String repositoryName;
        public boolean hasQueryAnnotation;
        public String queryString;
        public boolean isNativeQuery;
        public boolean isModifying;
        public int parameterCount;
        public List<String> parameterTypes;
        public String returnType;
        public int qualityScore;
        
        public CustomMethod(String methodId, String methodName, String repositoryName) {
            this.methodId = methodId;
            this.methodName = methodName;
            this.repositoryName = repositoryName;
            this.parameterTypes = new ArrayList<>();
        }
    }
    
    public static class MethodComplexity {
        public String methodId;
        public int cyclomaticComplexity;
        public int entityManagerCalls;
        public boolean usesCriteriaAPI;
        public boolean isTransactional;
        public int overallScore;
        
        public MethodComplexity(String methodId) {
            this.methodId = methodId;
        }
    }
    
    public static class RepositoryIssue {
        public IssueLevel level;
        public String description;
        public String methodId;
        
        public RepositoryIssue(IssueLevel level, String description, String methodId) {
            this.level = level;
            this.description = description;
            this.methodId = methodId;
        }
    }
    
    public enum IssueLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public static class RepositoryAnalysisReport {
        public int totalRepositories;
        public int totalCustomMethods;
        public int totalDerivedQueries;
        public int totalCustomImplementations;
        public Map<String, RepositoryInfo> customRepositories;
        public Map<String, List<CustomMethod>> customMethods;
        public Map<String, MethodComplexity> methodComplexities;
        public List<RepositoryIssue> repositoryIssues;
        public double averageMethodQuality;
        public Map<String, Integer> complexityDistribution;
        public Map<IssueLevel, Integer> issuesByLevel;
    }
}
```

## Esempio di Output HTML

```html
<!DOCTYPE html>
<html>
<head>
    <title>Custom Repository Methods Analysis Report</title>
    <style>
        .repository-container { margin: 20px 0; padding: 15px; border-left: 4px solid #28a745; }
        .method-container { margin: 15px 0; padding: 10px; background: #f8f9fa; }
        .derived-query { border-left-color: #17a2b8; }
        .custom-method { border-left-color: #ffc107; }
        .implementation { border-left-color: #dc3545; }
        .quality-excellent { background-color: #d4edda; }
        .quality-good { background-color: #fff3cd; }
        .quality-poor { background-color: #f8d7da; }
        .complexity-score { font-weight: bold; padding: 5px 10px; border-radius: 3px; }
    </style>
</head>
<body>
    <h1>🏛️ Report: Analisi Metodi Custom nei Repository</h1>
    
    <div class="summary">
        <h2>📊 Riepilogo Generale</h2>
        <ul>
            <li><strong>Totale Repository:</strong> 12</li>
            <li><strong>Metodi Custom:</strong> 34</li>
            <li><strong>Derived Queries:</strong> 28</li>
            <li><strong>Implementazioni Custom:</strong> 6</li>
            <li><strong>Score Qualità Medio:</strong> 82/100</li>
        </ul>
    </div>
    
    <div class="repositories-analysis">
        <h2>📋 Analisi Repository</h2>
        
        <div class="repository-container">
            <h3>🟢 UserRepository</h3>
            <div class="repository-info">
                <p><strong>Entity:</strong> User</p>
                <p><strong>Base:</strong> JpaRepository, UserRepositoryCustom</p>
                <p><strong>Metodi Custom:</strong> 5 | <strong>Derived Queries:</strong> 8</p>
            </div>
            
            <div class="method-container derived-query quality-excellent">
                <h4>📘 findByUsernameAndActiveTrue</h4>
                <div class="method-details">
                    <span class="complexity-score" style="background: #28a745; color: white;">Qualità: 95/100</span>
                    <p><strong>Tipo:</strong> Derived Query</p>
                    <p><strong>Parametri:</strong> String username</p>
                    <p><strong>Return:</strong> Optional&lt;User&gt;</p>
                </div>
            </div>
            
            <div class="method-container custom-method quality-good">
                <h4>📙 findUsersWithComplexCriteria</h4>
                <div class="method-details">
                    <span class="complexity-score" style="background: #ffc107;">Qualità: 78/100</span>
                    <p><strong>Tipo:</strong> Custom Method with @Query</p>
                    <p><strong>Query:</strong> SELECT u FROM User u WHERE...</p>
                    <p><strong>Parametri:</strong> String name, Date from, Date to</p>
                </div>
            </div>
        </div>
        
        <div class="repository-container">
            <h3>🟡 ProductRepository</h3>
            <div class="implementation">
                <h4>🔧 ProductRepositoryImpl.findProductsWithDynamicFiltering</h4>
                <div class="complexity-analysis">
                    <span class="complexity-score" style="background: #dc3545; color: white;">Complessità: 12</span>
                    <ul>
                        <li>🔴 <strong>Alta Complessità Ciclomatica</strong>: 12 (soglia: 10)</li>
                        <li>🟠 <strong>Multiple EntityManager calls</strong>: 8 chiamate</li>
                        <li>🟢 <strong>Usa Criteria API</strong>: Implementazione type-safe</li>
                        <li>🟢 <strong>Transazionale</strong>: Annotation @Transactional presente</li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
    
    <div class="issues-analysis">
        <h2>⚠️ Problemi Identificati</h2>
        <div class="issues-summary">
            <h3>Distribuzione per Gravità</h3>
            <ul>
                <li>🔴 <strong>Critici:</strong> 1 issue</li>
                <li>🟠 <strong>Alti:</strong> 4 issues</li>
                <li>🟡 <strong>Medi:</strong> 7 issues</li>
                <li>🔵 <strong>Bassi:</strong> 2 issues</li>
            </ul>
        </div>
    </div>
    
    <div class="complexity-analysis">
        <h2>📊 Distribuzione Complessità</h2>
        <ul>
            <li>🟢 <strong>Bassa (1-5):</strong> 18 metodi</li>
            <li>🟡 <strong>Media (6-10):</strong> 12 metodi</li>
            <li>🟠 <strong>Alta (11-15):</strong> 3 metodi</li>
            <li>🔴 <strong>Molto Alta (&gt;15):</strong> 1 metodo</li>
        </ul>
    </div>
</body>
</html>
```

## Metriche di Qualità del Codice

### Algoritmo di Scoring (0-100)

```java
public class RepositoryQualityScorer {
    
    public int calculateQualityScore(RepositoryAnalysisReport report) {
        int baseScore = 100;
        
        // Penalità per metodi di bassa qualità
        double avgQuality = report.averageMethodQuality;
        if (avgQuality < 60) baseScore -= 30;
        else if (avgQuality < 75) baseScore -= 15;
        else if (avgQuality < 85) baseScore -= 5;
        
        // Penalità per alta complessità
        int highComplexityMethods = report.complexityDistribution.getOrDefault("HIGH", 0) +
                                   report.complexityDistribution.getOrDefault("VERY_HIGH", 0);
        int totalMethods = report.totalCustomMethods;
        if (totalMethods > 0) {
            int complexityRatio = (highComplexityMethods * 100) / totalMethods;
            if (complexityRatio > 20) baseScore -= 20;
            else if (complexityRatio > 10) baseScore -= 10;
        }
        
        // Penalità per issues critici e alti
        Map<IssueLevel, Integer> issues = report.issuesByLevel;
        baseScore -= issues.getOrDefault(IssueLevel.CRITICAL, 0) * 15;
        baseScore -= issues.getOrDefault(IssueLevel.HIGH, 0) * 8;
        baseScore -= issues.getOrDefault(IssueLevel.MEDIUM, 0) * 3;
        
        // Penalità per troppi repository senza custom implementation
        int reposWithoutImpl = report.totalRepositories - report.totalCustomImplementations;
        if (reposWithoutImpl > report.totalRepositories * 0.3) baseScore -= 10;
        
        // Bonus per best practices
        if (avgQuality > 90) baseScore += 10;
        if (report.totalDerivedQueries > report.totalCustomMethods) baseScore += 5; // Preferisce derived queries
        
        return Math.max(0, Math.min(100, baseScore));
    }
    
    public String getQualityLevel(int score) {
        if (score >= 90) return "🟢 ECCELLENTE";
        if (score >= 75) return "🟡 BUONO";
        if (score >= 60) return "🟠 SUFFICIENTE";
        return "🔴 CRITICO";
    }
}
```

### Soglie di Valutazione

| Metrica | 🟢 Eccellente | 🟡 Buono | 🟠 Sufficiente | 🔴 Critico |
|---------|---------------|----------|----------------|-------------|
| **Score Complessivo** | 90-100 | 75-89 | 60-74 | 0-59 |
| **Qualità Media Metodi** | >90 | 80-90 | 65-79 | <65 |
| **Metodi Alta Complessità (%)** | <5% | 5-10% | 10-20% | >20% |
| **Issues Critici + Alti** | 0-1 | 2-3 | 4-6 | >6 |
| **Ratio Derived/Custom** | >2:1 | 1.5:1 | 1:1 | <1:1 |

### Segnalazioni per Gravità

#### 🔴 CRITICA
- **Metodi senza Paginazione**: Query di massa senza controllo volume dati
- **N+1 Query Problems**: Pattern che causano performance critiche
- **Complessità Ciclomatica >20**: Metodi impossibili da manutenere
- **SQL Injection in Custom**: Vulnerabilità sicurezza in implementazioni

#### 🟠 ALTA
- **Derived Query Troppo Complesse**: Nomi metodi >150 caratteri o >10 operatori
- **Missing @Transactional**: Operazioni di modifica senza controllo transazionale
- **Return Type Object/Map**: Tipi di ritorno non type-safe
- **Troppe Chiamate EntityManager**: >10 chiamate in un singolo metodo

#### 🟡 MEDIA
- **Naming Convention Violata**: Metodi derived query non conformi
- **Parametri Eccessivi**: Metodi con >5 parametri
- **Mancanza Custom Implementation**: Repository che dovrebbero avere implementazioni custom
- **Lazy Loading Issues**: Potenziali problemi di caricamento lazy

#### 🔵 BASSA
- **Documentation Mancante**: Metodi custom senza Javadoc
- **Return Type Subottimale**: Uso di List invece di Set appropriato
- **Ordinamento Hard-coded**: OrderBy fisso invece di parametrico
- **Cache Suggestions**: Metodi che beneficerebbero di cache

### Valore di Business

- **Manutenibilità Codice**: Repository ben strutturati riducono costi manutenzione
- **Performance Database**: Identificazione query inefficienti e N+1 problems
- **Type Safety**: Uso di derived queries riduce errori runtime
- **Scalabilità Applicazione**: Pattern repository appropriati per crescita
- **Developer Productivity**: Best practices facilitano sviluppo team