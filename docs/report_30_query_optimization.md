# Report 30: Ottimizzazione Query

**Valore**: ⭐⭐⭐⭐⭐ **Complessità**: 🔴 Molto Complessa **Tempo**: 15-18 giorni
**Tags**: `#query-optimization` `#performance-analysis` `#sql-tuning`

## Descrizione

Questo report analizza l'ottimizzazione delle query nell'applicazione, identificando query inefficienti, problemi di performance, suggerimenti per indici e strategie di ottimizzazione basate sull'analisi statica del codice e pattern anti-performance.

## Obiettivi dell'Analisi

1. **Query Performance Analysis**: Identificare query lente e inefficienti
2. **Index Optimization**: Suggerire indici mancanti e ottimizzazioni
3. **N+1 Problem Detection**: Rilevare pattern N+1 query
4. **Join Optimization**: Analizzare efficienza dei JOIN
5. **Caching Opportunities**: Identificare query cacheable

## Implementazione con Javassist

```java
package com.jreverse.analyzer.queryopt;

import javassist.*;
import javassist.bytecode.*;
import javassist.expr.*;
import java.util.*;
import java.util.regex.*;
import java.io.IOException;

/**
 * Analyzer per ottimizzazione query nell'applicazione
 */
public class QueryOptimizationAnalyzer {
    
    private ClassPool classPool;
    private Map<String, QueryAnalysis> analyzedQueries;
    private List<OptimizationSuggestion> optimizationSuggestions;
    private List<PerformanceIssue> performanceIssues;
    private Map<String, IndexRecommendation> indexRecommendations;
    private Set<String> n1Problems;
    
    // Pattern per query inefficienti
    private static final Pattern INEFFICIENT_QUERY_PATTERN = Pattern.compile(
        "(?i)(SELECT\\s+\\*\\s+FROM)|(LIKE\\s+'%[^%]*%')|(OR\\s+.*OR\\s+.*OR)"
    );
    
    // Pattern per N+1 problems
    private static final Pattern N1_PATTERN = Pattern.compile(
        "(?i)(findBy\\w+\\(.*\\).*forEach)|(stream\\(\\).*map.*findBy)"
    );
    
    // Pattern per query senza indici
    private static final Pattern MISSING_INDEX_PATTERN = Pattern.compile(
        "(?i)(WHERE\\s+\\w+\\s*=\\s*\\?)|(ORDER\\s+BY\\s+\\w+)|(GROUP\\s+BY\\s+\\w+)"
    );
    
    // Pattern per operazioni costose
    private static final Pattern EXPENSIVE_OPERATIONS_PATTERN = Pattern.compile(
        "(?i)(DISTINCT\\s+)|(UNION\\s+)|(EXISTS\\s*\\()|(NOT\\s+EXISTS)"
    );
    
    public QueryOptimizationAnalyzer() {
        this.classPool = ClassPool.getDefault();
        this.analyzedQueries = new HashMap<>();
        this.optimizationSuggestions = new ArrayList<>();
        this.performanceIssues = new ArrayList<>();
        this.indexRecommendations = new HashMap<>();
        this.n1Problems = new HashSet<>();
    }
    
    /**
     * Analizza ottimizzazioni query nell'applicazione
     */
    public QueryOptimizationReport analyzeQueryOptimization(List<String> classNames) throws Exception {
        // Fase 1: Analizza repository methods
        analyzeRepositoryQueries(classNames);
        
        // Fase 2: Analizza custom queries
        analyzeCustomQueries(classNames);
        
        // Fase 3: Rileva N+1 problems
        detectN1Problems(classNames);
        
        // Fase 4: Analizza join patterns
        analyzeJoinPatterns();
        
        // Fase 5: Genera raccomandazioni indici
        generateIndexRecommendations();
        
        // Fase 6: Suggerisci ottimizzazioni
        generateOptimizationSuggestions();
        
        return generateReport();
    }
    
    /**
     * Analizza query nei repository
     */
    private void analyzeRepositoryQueries(List<String> classNames) throws Exception {
        for (String className : classNames) {
            if (className.contains("Repository")) {
                try {
                    CtClass ctClass = classPool.get(className);
                    analyzeRepositoryClass(ctClass);
                } catch (Exception e) {
                    System.err.println("Errore nell'analisi repository: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Analizza classe repository
     */
    private void analyzeRepositoryClass(CtClass repoClass) throws Exception {
        String className = repoClass.getName();
        
        for (CtMethod method : repoClass.getDeclaredMethods()) {
            if (hasQueryAnnotation(method)) {
                analyzeQueryMethod(className, method);
            } else if (isDerivedQueryMethod(method)) {
                analyzeDerivedQueryMethod(className, method);
            }
        }
    }
    
    /**
     * Verifica se ha annotation @Query
     */
    private boolean hasQueryAnnotation(CtMethod method) throws Exception {
        AnnotationsAttribute attr = (AnnotationsAttribute)
            method.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
        
        return attr != null && 
               attr.getAnnotation("org.springframework.data.jpa.repository.Query") != null;
    }
    
    /**
     * Verifica se è derived query method
     */
    private boolean isDerivedQueryMethod(CtMethod method) {
        String methodName = method.getName();
        return methodName.startsWith("findBy") || 
               methodName.startsWith("getBy") || 
               methodName.startsWith("queryBy") ||
               methodName.startsWith("countBy");
    }
    
    /**
     * Analizza metodo con @Query
     */
    private void analyzeQueryMethod(String className, CtMethod method) throws Exception {
        String methodName = method.getName();
        String queryId = className + "." + methodName;
        
        AnnotationsAttribute attr = (AnnotationsAttribute)
            method.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
        
        Annotation queryAnnotation = attr.getAnnotation("org.springframework.data.jpa.repository.Query");
        if (queryAnnotation != null) {
            String queryString = getAnnotationValue(queryAnnotation, "value");
            boolean isNative = getBooleanAnnotationValue(queryAnnotation, "nativeQuery");
            
            QueryAnalysis analysis = new QueryAnalysis(queryId, queryString, isNative);
            
            // Analisi performance
            analyzeQueryPerformance(analysis);
            
            // Analisi struttura
            analyzeQueryStructure(analysis);
            
            // Analisi caching opportunities
            analyzeCachingOpportunities(analysis, method);
            
            analyzedQueries.put(queryId, analysis);
        }
    }
    
    /**
     * Analizza derived query method
     */
    private void analyzeDerivedQueryMethod(String className, CtMethod method) throws Exception {
        String methodName = method.getName();
        String queryId = className + "." + methodName;
        
        // Deriva query JPQL dal nome metodo
        String derivedQuery = derivePlausibleQuery(methodName);
        
        QueryAnalysis analysis = new QueryAnalysis(queryId, derivedQuery, false);
        analysis.isDerived = true;
        analysis.methodName = methodName;
        
        // Analisi complessità derived query
        analyzeDerivedQueryComplexity(analysis);
        
        // Verifica se potrebbe beneficiare di @Query
        checkIfNeedsQueryAnnotation(analysis);
        
        analyzedQueries.put(queryId, analysis);
    }
    
    /**
     * Deriva query plausibile dal nome metodo
     */
    private String derivePlausibleQuery(String methodName) {
        // Implementazione semplificata
        String entityName = extractEntityName(methodName);
        String conditions = extractConditions(methodName);
        
        return "SELECT e FROM " + entityName + " e WHERE " + conditions;
    }
    
    /**
     * Estrae nome entity dal metodo
     */
    private String extractEntityName(String methodName) {
        // Logica per estrarre entity name (semplificata)
        return "Entity";
    }
    
    /**
     * Estrae condizioni dal nome metodo
     */
    private String extractConditions(String methodName) {
        // Converte findByNameAndAge in name = ? AND age = ?
        String conditions = methodName.replaceFirst("findBy|getBy|queryBy|countBy", "");
        conditions = conditions.replaceAll("And", " AND ");
        conditions = conditions.replaceAll("Or", " OR ");
        // Semplificazione: assume parametri singoli
        return conditions.toLowerCase() + " = ?";
    }
    
    /**
     * Analizza performance di una query
     */
    private void analyzeQueryPerformance(QueryAnalysis analysis) {
        String query = analysis.queryString.toUpperCase();
        
        // Analisi SELECT *
        if (query.contains("SELECT *")) {
            PerformanceIssue issue = new PerformanceIssue(
                PerformanceIssueType.SELECT_ALL,
                "Uso di SELECT * riduce performance",
                analysis.queryId,
                PerformanceImpact.MEDIUM
            );
            performanceIssues.add(issue);
            analysis.performanceScore -= 15;
        }
        
        // Analisi LIKE con wildcard iniziale
        if (query.matches(".*LIKE\\s+'%[^%]*'.*")) {
            PerformanceIssue issue = new PerformanceIssue(
                PerformanceIssueType.WILDCARD_LIKE,
                "LIKE con wildcard iniziale impedisce uso indici",
                analysis.queryId,
                PerformanceImpact.HIGH
            );
            performanceIssues.add(issue);
            analysis.performanceScore -= 25;
        }
        
        // Analisi multiple OR conditions
        int orCount = countOccurrences(query, " OR ");
        if (orCount > 3) {
            PerformanceIssue issue = new PerformanceIssue(
                PerformanceIssueType.MULTIPLE_OR,
                "Molte condizioni OR (" + orCount + ") possono impedire uso indici",
                analysis.queryId,
                PerformanceImpact.MEDIUM
            );
            performanceIssues.add(issue);
            analysis.performanceScore -= (orCount * 5);
        }
        
        // Analisi subquery
        int subqueryCount = countOccurrences(query, "SELECT") - 1;
        if (subqueryCount > 0) {
            analysis.hasSubqueries = true;
            analysis.subqueryCount = subqueryCount;
            
            if (subqueryCount > 2) {
                PerformanceIssue issue = new PerformanceIssue(
                    PerformanceIssueType.COMPLEX_SUBQUERY,
                    "Multiple subquery (" + subqueryCount + ") possono causare performance issues",
                    analysis.queryId,
                    PerformanceImpact.HIGH
                );
                performanceIssues.add(issue);
                analysis.performanceScore -= (subqueryCount * 10);
            }
        }
        
        // Analisi JOIN
        int joinCount = countOccurrences(query, "JOIN");
        analysis.joinCount = joinCount;
        if (joinCount > 4) {
            PerformanceIssue issue = new PerformanceIssue(
                PerformanceIssueType.EXCESSIVE_JOINS,
                "Molti JOIN (" + joinCount + ") possono causare performance issues",
                analysis.queryId,
                PerformanceImpact.MEDIUM
            );
            performanceIssues.add(issue);
            analysis.performanceScore -= (joinCount * 3);
        }
        
        // Analisi ORDER BY senza LIMIT
        if (query.contains("ORDER BY") && !query.contains("LIMIT") && !query.contains("TOP")) {
            PerformanceIssue issue = new PerformanceIssue(
                PerformanceIssueType.ORDER_WITHOUT_LIMIT,
                "ORDER BY senza LIMIT può causare problemi memory",
                analysis.queryId,
                PerformanceImpact.MEDIUM
            );
            performanceIssues.add(issue);
            analysis.performanceScore -= 10;
        }
    }
    
    /**
     * Analizza struttura query
     */
    private void analyzeQueryStructure(QueryAnalysis analysis) {
        String query = analysis.queryString.toUpperCase();
        
        // Calcola complessità
        analysis.complexityScore = calculateQueryComplexity(query);
        
        // Identifica tabelle coinvolte
        analysis.involvedTables = extractTableNames(query);
        
        // Identifica colonne in WHERE clause
        analysis.whereColumns = extractWhereColumns(query);
        
        // Identifica colonne in ORDER BY
        analysis.orderByColumns = extractOrderByColumns(query);
    }
    
    /**
     * Calcola complessità query
     */
    private int calculateQueryComplexity(String query) {
        int complexity = 0;
        
        // Base complexity
        complexity += countOccurrences(query, "SELECT");
        complexity += countOccurrences(query, "JOIN") * 2;
        complexity += countOccurrences(query, "UNION") * 3;
        complexity += countOccurrences(query, "EXISTS") * 2;
        complexity += countOccurrences(query, "CASE") * 2;
        complexity += countOccurrences(query, "GROUP BY");
        complexity += countOccurrences(query, "HAVING") * 2;
        complexity += countOccurrences(query, "ORDER BY");
        
        // Functional complexity
        complexity += countOccurrences(query, "DISTINCT");
        complexity += countOccurrences(query, "LIKE") * 2;
        
        return complexity;
    }
    
    /**
     * Estrae nomi tabelle dalla query
     */
    private Set<String> extractTableNames(String query) {
        Set<String> tables = new HashSet<>();
        
        // Pattern semplificati per FROM e JOIN
        Pattern fromPattern = Pattern.compile("FROM\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher fromMatcher = fromPattern.matcher(query);
        while (fromMatcher.find()) {
            tables.add(fromMatcher.group(1).toUpperCase());
        }
        
        Pattern joinPattern = Pattern.compile("JOIN\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher joinMatcher = joinPattern.matcher(query);
        while (joinMatcher.find()) {
            tables.add(joinMatcher.group(1).toUpperCase());
        }
        
        return tables;
    }
    
    /**
     * Estrae colonne WHERE clause
     */
    private Set<String> extractWhereColumns(String query) {
        Set<String> columns = new HashSet<>();
        
        Pattern wherePattern = Pattern.compile(
            "WHERE\\s+.*?(\\w+)\\s*[=<>!]", 
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        
        Matcher whereMatcher = wherePattern.matcher(query);
        while (whereMatcher.find()) {
            String column = whereMatcher.group(1);
            if (!isReservedWord(column)) {
                columns.add(column.toUpperCase());
            }
        }
        
        return columns;
    }
    
    /**
     * Estrae colonne ORDER BY
     */
    private Set<String> extractOrderByColumns(String query) {
        Set<String> columns = new HashSet<>();
        
        Pattern orderPattern = Pattern.compile(
            "ORDER\\s+BY\\s+(\\w+)", 
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher orderMatcher = orderPattern.matcher(query);
        while (orderMatcher.find()) {
            columns.add(orderMatcher.group(1).toUpperCase());
        }
        
        return columns;
    }
    
    /**
     * Verifica se è parola riservata SQL
     */
    private boolean isReservedWord(String word) {
        Set<String> reserved = Set.of("AND", "OR", "NOT", "NULL", "TRUE", "FALSE", "SELECT", "FROM", "WHERE");
        return reserved.contains(word.toUpperCase());
    }
    
    /**
     * Analizza opportunità di caching
     */
    private void analyzeCachingOpportunities(QueryAnalysis analysis, CtMethod method) throws Exception {
        // Verifica se query è candidata per caching
        if (isCacheable(analysis)) {
            // Verifica se già presente @Cacheable
            if (!hasCacheableAnnotation(method)) {
                OptimizationSuggestion suggestion = new OptimizationSuggestion(
                    OptimizationType.CACHING,
                    "Considera aggiungere @Cacheable per query readonly frequente",
                    analysis.queryId,
                    OptimizationPriority.MEDIUM
                );
                optimizationSuggestions.add(suggestion);
                analysis.isCacheCandidate = true;
            } else {
                analysis.isCached = true;
            }
        }
    }
    
    /**
     * Verifica se query è cacheable
     */
    private boolean isCacheable(QueryAnalysis analysis) {
        String query = analysis.queryString.toUpperCase();
        
        // Solo query SELECT
        if (!query.startsWith("SELECT")) {
            return false;
        }
        
        // Non deve avere date/time functions
        if (query.matches(".*\\b(NOW|CURRENT_DATE|CURRENT_TIME|SYSDATE)\\b.*")) {
            return false;
        }
        
        // Query semplici sono più cacheable
        return analysis.complexityScore < 10 && analysis.joinCount <= 2;
    }
    
    /**
     * Verifica presenza annotation @Cacheable
     */
    private boolean hasCacheableAnnotation(CtMethod method) throws Exception {
        AnnotationsAttribute attr = (AnnotationsAttribute)
            method.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
        
        return attr != null && 
               attr.getAnnotation("org.springframework.cache.annotation.Cacheable") != null;
    }
    
    /**
     * Analizza complessità derived query
     */
    private void analyzeDerivedQueryComplexity(QueryAnalysis analysis) {
        String methodName = analysis.methodName;
        
        // Conta And/Or operators
        int andCount = countOccurrences(methodName, "And");
        int orCount = countOccurrences(methodName, "Or");
        
        analysis.derivedQueryComplexity = andCount * 2 + orCount * 3;
        
        if (analysis.derivedQueryComplexity > 10) {
            OptimizationSuggestion suggestion = new OptimizationSuggestion(
                OptimizationType.QUERY_REFACTORING,
                "Derived query molto complessa, considera @Query personalizzata",
                analysis.queryId,
                OptimizationPriority.HIGH
            );
            optimizationSuggestions.add(suggestion);
        }
    }
    
    /**
     * Verifica se serve @Query annotation
     */
    private void checkIfNeedsQueryAnnotation(QueryAnalysis analysis) {
        // Se derived query è complessa, suggerisci @Query
        if (analysis.derivedQueryComplexity > 8) {
            OptimizationSuggestion suggestion = new OptimizationSuggestion(
                OptimizationType.QUERY_ANNOTATION,
                "Sostituisci derived query complessa con @Query ottimizzata",
                analysis.queryId,
                OptimizationPriority.MEDIUM
            );
            optimizationSuggestions.add(suggestion);
        }
    }
    
    /**
     * Analizza query custom
     */
    private void analyzeCustomQueries(List<String> classNames) throws Exception {
        for (String className : classNames) {
            if (className.contains("Service") || className.contains("Controller")) {
                try {
                    CtClass ctClass = classPool.get(className);
                    analyzeServiceQueries(ctClass);
                } catch (Exception e) {
                    // Continue with other classes
                }
            }
        }
    }
    
    /**
     * Analizza query nei service
     */
    private void analyzeServiceQueries(CtClass serviceClass) throws Exception {
        for (CtMethod method : serviceClass.getDeclaredMethods()) {
            analyzeMethodForQueries(serviceClass.getName(), method);
        }
    }
    
    /**
     * Analizza metodo per pattern query
     */
    private void analyzeMethodForQueries(String className, CtMethod method) throws Exception {
        method.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                try {
                    String methodName = m.getMethodName();
                    String calledClass = m.getClassName();
                    
                    // Rileva chiamate repository
                    if (calledClass.contains("Repository") && 
                        (methodName.startsWith("find") || methodName.startsWith("get"))) {
                        
                        analyzeRepositoryCall(className, method.getName(), 
                                            calledClass, methodName, m.getLineNumber());
                    }
                    
                } catch (Exception e) {
                    // Continue analysis
                }
            }
        });
    }
    
    /**
     * Analizza chiamata repository
     */
    private void analyzeRepositoryCall(String serviceClass, String serviceMethod, 
                                     String repoClass, String repoMethod, int line) {
        String callId = serviceClass + "." + serviceMethod + ":" + line;
        
        // Verifica pattern N+1 context
        if (isInLoopContext(serviceMethod, line)) {
            n1Problems.add(callId);
            
            PerformanceIssue issue = new PerformanceIssue(
                PerformanceIssueType.N_PLUS_1,
                "Potenziale N+1 problem: chiamata repository in loop",
                callId,
                PerformanceImpact.CRITICAL
            );
            performanceIssues.add(issue);
            
            OptimizationSuggestion suggestion = new OptimizationSuggestion(
                OptimizationType.BATCH_LOADING,
                "Usa batch loading o fetch join per evitare N+1 problem",
                callId,
                OptimizationPriority.CRITICAL
            );
            optimizationSuggestions.add(suggestion);
        }
    }
    
    /**
     * Rileva N+1 problems
     */
    private void detectN1Problems(List<String> classNames) throws Exception {
        for (String className : classNames) {
            try {
                CtClass ctClass = classPool.get(className);
                detectN1InClass(ctClass);
            } catch (Exception e) {
                // Continue with other classes
            }
        }
    }
    
    /**
     * Rileva N+1 in una classe
     */
    private void detectN1InClass(CtClass ctClass) throws Exception {
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            String methodBody = getMethodBody(method);
            
            if (N1_PATTERN.matcher(methodBody).find()) {
                String problemId = ctClass.getName() + "." + method.getName();
                n1Problems.add(problemId);
                
                PerformanceIssue issue = new PerformanceIssue(
                    PerformanceIssueType.N_PLUS_1,
                    "N+1 problem rilevato tramite pattern analysis",
                    problemId,
                    PerformanceImpact.CRITICAL
                );
                performanceIssues.add(issue);
            }
        }
    }
    
    /**
     * Verifica se la chiamata è in contesto loop
     */
    private boolean isInLoopContext(String methodName, int line) {
        // Implementazione semplificata
        return methodName.contains("forEach") || methodName.contains("stream");
    }
    
    /**
     * Ottiene corpo metodo (semplificato)
     */
    private String getMethodBody(CtMethod method) {
        // In una implementazione reale, si analizza il bytecode
        return method.getName(); // Placeholder
    }
    
    /**
     * Analizza pattern JOIN
     */
    private void analyzeJoinPatterns() {
        for (QueryAnalysis analysis : analyzedQueries.values()) {
            if (analysis.joinCount > 0) {
                analyzeJoinOptimization(analysis);
            }
        }
    }
    
    /**
     * Analizza ottimizzazione JOIN
     */
    private void analyzeJoinOptimization(QueryAnalysis analysis) {
        String query = analysis.queryString.toUpperCase();
        
        // Verifica INNER vs LEFT JOIN appropriato
        if (query.contains("LEFT JOIN") && !query.contains("IS NULL")) {
            OptimizationSuggestion suggestion = new OptimizationSuggestion(
                OptimizationType.JOIN_OPTIMIZATION,
                "Considera INNER JOIN se non servono row null",
                analysis.queryId,
                OptimizationPriority.LOW
            );
            optimizationSuggestions.add(suggestion);
        }
        
        // Suggerisci fetch join per evitare lazy loading
        if (analysis.joinCount > 0 && analysis.isNative == false) {
            OptimizationSuggestion suggestion = new OptimizationSuggestion(
                OptimizationType.FETCH_JOIN,
                "Considera FETCH JOIN per evitare lazy loading issues",
                analysis.queryId,
                OptimizationPriority.MEDIUM
            );
            optimizationSuggestions.add(suggestion);
        }
    }
    
    /**
     * Genera raccomandazioni indici
     */
    private void generateIndexRecommendations() {
        Map<String, Set<String>> tableColumns = new HashMap<>();
        
        // Aggrega colonne per tabella
        for (QueryAnalysis analysis : analyzedQueries.values()) {
            for (String table : analysis.involvedTables) {
                Set<String> columns = tableColumns.computeIfAbsent(table, k -> new HashSet<>());
                columns.addAll(analysis.whereColumns);
                columns.addAll(analysis.orderByColumns);
            }
        }
        
        // Genera raccomandazioni per ogni tabella
        for (Map.Entry<String, Set<String>> entry : tableColumns.entrySet()) {
            String table = entry.getKey();
            Set<String> columns = entry.getValue();
            
            if (!columns.isEmpty()) {
                IndexRecommendation recommendation = new IndexRecommendation(
                    table,
                    new ArrayList<>(columns),
                    calculateIndexPriority(table, columns)
                );
                
                indexRecommendations.put(table, recommendation);
            }
        }
    }
    
    /**
     * Calcola priorità indice
     */
    private IndexPriority calculateIndexPriority(String table, Set<String> columns) {
        // Query frequency simulation
        long queryCount = analyzedQueries.values().stream()
            .filter(q -> q.involvedTables.contains(table))
            .count();
        
        if (queryCount > 10) return IndexPriority.HIGH;
        if (queryCount > 5) return IndexPriority.MEDIUM;
        return IndexPriority.LOW;
    }
    
    /**
     * Genera suggerimenti ottimizzazione
     */
    private void generateOptimizationSuggestions() {
        // Già generati durante l'analisi, qui aggiungiamo suggerimenti generali
        
        // Suggerisci pagination per query senza LIMIT
        for (QueryAnalysis analysis : analyzedQueries.values()) {
            if (!analysis.queryString.toUpperCase().contains("LIMIT") && 
                !analysis.queryString.toUpperCase().contains("TOP")) {
                
                OptimizationSuggestion suggestion = new OptimizationSuggestion(
                    OptimizationType.PAGINATION,
                    "Considera aggiungere paginazione per evitare large result sets",
                    analysis.queryId,
                    OptimizationPriority.MEDIUM
                );
                optimizationSuggestions.add(suggestion);
            }
        }
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
    
    /**
     * Genera report finale
     */
    private QueryOptimizationReport generateReport() {
        QueryOptimizationReport report = new QueryOptimizationReport();
        
        report.totalQueries = analyzedQueries.size();
        report.analyzedQueries = new HashMap<>(analyzedQueries);
        report.performanceIssues = new ArrayList<>(performanceIssues);
        report.optimizationSuggestions = new ArrayList<>(optimizationSuggestions);
        report.indexRecommendations = new HashMap<>(indexRecommendations);
        report.n1Problems = new HashSet<>(n1Problems);
        
        // Metriche aggregate
        report.averagePerformanceScore = calculateAveragePerformanceScore();
        report.averageComplexityScore = calculateAverageComplexityScore();
        report.criticalIssuesCount = countCriticalIssues();
        report.highPriorityOptimizations = countHighPriorityOptimizations();
        
        return report;
    }
    
    private double calculateAveragePerformanceScore() {
        return analyzedQueries.values().stream()
            .mapToInt(q -> q.performanceScore)
            .average()
            .orElse(100.0);
    }
    
    private double calculateAverageComplexityScore() {
        return analyzedQueries.values().stream()
            .mapToInt(q -> q.complexityScore)
            .average()
            .orElse(0.0);
    }
    
    private int countCriticalIssues() {
        return (int) performanceIssues.stream()
            .filter(issue -> issue.impact == PerformanceImpact.CRITICAL)
            .count();
    }
    
    private int countHighPriorityOptimizations() {
        return (int) optimizationSuggestions.stream()
            .filter(opt -> opt.priority == OptimizationPriority.CRITICAL || 
                          opt.priority == OptimizationPriority.HIGH)
            .count();
    }
    
    // Classi di supporto
    public static class QueryAnalysis {
        public String queryId;
        public String queryString;
        public boolean isNative;
        public boolean isDerived;
        public String methodName;
        public int performanceScore = 100;
        public int complexityScore;
        public int derivedQueryComplexity;
        public int joinCount;
        public int subqueryCount;
        public boolean hasSubqueries;
        public boolean isCacheCandidate;
        public boolean isCached;
        
        public Set<String> involvedTables = new HashSet<>();
        public Set<String> whereColumns = new HashSet<>();
        public Set<String> orderByColumns = new HashSet<>();
        
        public QueryAnalysis(String queryId, String queryString, boolean isNative) {
            this.queryId = queryId;
            this.queryString = queryString;
            this.isNative = isNative;
        }
    }
    
    public static class PerformanceIssue {
        public PerformanceIssueType type;
        public String description;
        public String queryId;
        public PerformanceImpact impact;
        
        public PerformanceIssue(PerformanceIssueType type, String description, 
                              String queryId, PerformanceImpact impact) {
            this.type = type;
            this.description = description;
            this.queryId = queryId;
            this.impact = impact;
        }
    }
    
    public static class OptimizationSuggestion {
        public OptimizationType type;
        public String description;
        public String queryId;
        public OptimizationPriority priority;
        
        public OptimizationSuggestion(OptimizationType type, String description, 
                                    String queryId, OptimizationPriority priority) {
            this.type = type;
            this.description = description;
            this.queryId = queryId;
            this.priority = priority;
        }
    }
    
    public static class IndexRecommendation {
        public String tableName;
        public List<String> columns;
        public IndexPriority priority;
        public String suggestedIndexName;
        
        public IndexRecommendation(String tableName, List<String> columns, IndexPriority priority) {
            this.tableName = tableName;
            this.columns = columns;
            this.priority = priority;
            this.suggestedIndexName = "idx_" + tableName.toLowerCase() + "_" + 
                                     String.join("_", columns).toLowerCase();
        }
    }
    
    public enum PerformanceIssueType {
        SELECT_ALL, WILDCARD_LIKE, MULTIPLE_OR, COMPLEX_SUBQUERY, 
        EXCESSIVE_JOINS, ORDER_WITHOUT_LIMIT, N_PLUS_1
    }
    
    public enum PerformanceImpact {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public enum OptimizationType {
        CACHING, QUERY_REFACTORING, QUERY_ANNOTATION, BATCH_LOADING,
        JOIN_OPTIMIZATION, FETCH_JOIN, PAGINATION, INDEX_CREATION
    }
    
    public enum OptimizationPriority {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public enum IndexPriority {
        LOW, MEDIUM, HIGH
    }
    
    public static class QueryOptimizationReport {
        public int totalQueries;
        public Map<String, QueryAnalysis> analyzedQueries;
        public List<PerformanceIssue> performanceIssues;
        public List<OptimizationSuggestion> optimizationSuggestions;
        public Map<String, IndexRecommendation> indexRecommendations;
        public Set<String> n1Problems;
        public double averagePerformanceScore;
        public double averageComplexityScore;
        public int criticalIssuesCount;
        public int highPriorityOptimizations;
    }
}
```

## Esempio di Output HTML

```html
<!DOCTYPE html>
<html>
<head>
    <title>Query Optimization Analysis Report</title>
    <style>
        .query-container { margin: 20px 0; padding: 15px; border-left: 4px solid #28a745; }
        .performance-critical { border-left-color: #dc3545; background-color: #ffe6e6; }
        .performance-warning { border-left-color: #ffc107; background-color: #fff3cd; }
        .optimization-suggestion { margin: 10px 0; padding: 10px; background: #f8f9fa; }
        .priority-critical { border-left: 3px solid #dc3545; }
        .priority-high { border-left: 3px solid #fd7e14; }
        .index-recommendation { background: #e7f3ff; padding: 10px; margin: 10px 0; }
    </style>
</head>
<body>
    <h1>⚡ Report: Ottimizzazione Query</h1>
    
    <div class="summary">
        <h2>📊 Riepilogo Performance</h2>
        <ul>
            <li><strong>Totale Query Analizzate:</strong> 45</li>
            <li><strong>Score Performance Medio:</strong> 72/100</li>
            <li><strong>Issues Critici:</strong> 5</li>
            <li><strong>N+1 Problems:</strong> 3</li>
            <li><strong>Raccomandazioni Indici:</strong> 12</li>
        </ul>
    </div>
    
    <div class="critical-issues">
        <h2>🔴 Issues Critici</h2>
        
        <div class="query-container performance-critical">
            <h3>UserService.loadUserWithOrders</h3>
            <div class="query-details">
                <p><strong>Issue:</strong> N+1 Problem rilevato</p>
                <p><strong>Impatto:</strong> CRITICO - Caricamento 1000+ query per 100 utenti</p>
                <div class="code-snippet">
                    <pre>users.forEach(user -> user.getOrders().size())</pre>
                </div>
            </div>
            <div class="optimization-suggestion priority-critical">
                <h4>🚀 Suggerimento Ottimizzazione</h4>
                <p><strong>Tipo:</strong> Batch Loading</p>
                <p><strong>Soluzione:</strong> Usa @EntityGraph o fetch join:</p>
                <pre>@Query("SELECT u FROM User u LEFT JOIN FETCH u.orders WHERE u.active = true")</pre>
            </div>
        </div>
    </div>
    
    <div class="performance-issues">
        <h2>⚠️ Issues Performance</h2>
        
        <div class="query-container performance-warning">
            <h3>ProductRepository.searchProducts</h3>
            <div class="query-details">
                <p><strong>Query:</strong> SELECT * FROM products WHERE name LIKE '%search%'</p>
                <p><strong>Score Performance:</strong> 45/100</p>
                <p><strong>Problemi:</strong></p>
                <ul>
                    <li>SELECT * riduce performance</li>
                    <li>LIKE con wildcard iniziale impedisce uso indici</li>
                    <li>Mancanza paginazione</li>
                </ul>
            </div>
            <div class="optimization-suggestion priority-high">
                <h4>🔧 Ottimizzazioni Suggerite</h4>
                <ul>
                    <li>Seleziona solo colonne necessarie</li>
                    <li>Usa full-text search per ricerche testuali</li>
                    <li>Aggiungi paginazione con Pageable</li>
                </ul>
            </div>
        </div>
    </div>
    
    <div class="index-recommendations">
        <h2>📊 Raccomandazioni Indici</h2>
        
        <div class="index-recommendation">
            <h3>Tabella: USERS</h3>
            <p><strong>Colonne Suggerite:</strong> email, active_status, created_date</p>
            <p><strong>Priorità:</strong> ALTA</p>
            <p><strong>SQL Suggerito:</strong></p>
            <pre>CREATE INDEX idx_users_email_active_created ON users(email, active_status, created_date);</pre>
            <p><strong>Benefici:</strong> Ottimizza 15 query frequenti</p>
        </div>
        
        <div class="index-recommendation">
            <h3>Tabella: ORDERS</h3>
            <p><strong>Colonne Suggerite:</strong> user_id, order_status, order_date</p>
            <p><strong>Priorità:</strong> MEDIA</p>
            <p><strong>SQL Suggerito:</strong></p>
            <pre>CREATE INDEX idx_orders_user_status_date ON orders(user_id, order_status, order_date);</pre>
        </div>
    </div>
    
    <div class="caching-opportunities">
        <h2>💾 Opportunità Caching</h2>
        <div class="cache-suggestion">
            <h3>CategoryRepository.findAllActive</h3>
            <p><strong>Motivo:</strong> Query readonly frequente con bassa variabilità</p>
            <p><strong>Suggerimento:</strong> Aggiungi @Cacheable("categories")</p>
            <p><strong>Benefici Stimati:</strong> -80% query database</p>
        </div>
    </div>
    
    <div class="complexity-analysis">
        <h2>📈 Analisi Complessità</h2>
        <ul>
            <li>🟢 <strong>Query Semplici (0-5):</strong> 28 query</li>
            <li>🟡 <strong>Query Medie (6-10):</strong> 12 query</li>
            <li>🟠 <strong>Query Complesse (11-15):</strong> 4 query</li>
            <li>🔴 <strong>Query Molto Complesse (>15):</strong> 1 query</li>
        </ul>
    </div>
</body>
</html>
```

## Metriche di Qualità del Codice

### Algoritmo di Scoring (0-100)

```java
public class QueryOptimizationQualityScorer {
    
    public int calculateQualityScore(QueryOptimizationReport report) {
        int baseScore = 100;
        
        // Penalità per issues critici
        baseScore -= report.criticalIssuesCount * 25;
        
        // Penalità per performance scadente
        if (report.averagePerformanceScore < 50) baseScore -= 30;
        else if (report.averagePerformanceScore < 70) baseScore -= 15;
        else if (report.averagePerformanceScore < 85) baseScore -= 5;
        
        // Penalità per N+1 problems
        baseScore -= report.n1Problems.size() * 20;
        
        // Penalità per alta complessità
        if (report.averageComplexityScore > 15) baseScore -= 20;
        else if (report.averageComplexityScore > 10) baseScore -= 10;
        
        // Penalità per issues performance
        for (PerformanceIssue issue : report.performanceIssues) {
            switch (issue.impact) {
                case CRITICAL: baseScore -= 15; break;
                case HIGH: baseScore -= 10; break;
                case MEDIUM: baseScore -= 5; break;
                case LOW: baseScore -= 2; break;
            }
        }
        
        // Bonus per ottimizzazioni implementate
        long cachingOptimizations = report.optimizationSuggestions.stream()
            .filter(opt -> opt.type == OptimizationType.CACHING)
            .count();
        baseScore += Math.min(cachingOptimizations * 3, 10);
        
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
| **Performance Media** | >85 | 70-85 | 50-69 | <50 |
| **Issues Critici** | 0 | 0-1 | 2-3 | >3 |
| **N+1 Problems** | 0 | 0 | 1-2 | >2 |
| **Complessità Media** | <8 | 8-12 | 12-15 | >15 |

### Segnalazioni per Gravità

#### 🔴 CRITICA
- **N+1 Query Problems**: Pattern che causano explosion queries
- **SELECT * su Large Tables**: Performance degradation critica
- **Subquery Non Ottimizzate**: Query con >3 livelli nesting
- **Missing Pagination**: Query senza LIMIT su dataset grandi

#### 🟠 ALTA
- **LIKE con Wildcard Iniziale**: Pattern che impediscono uso indici
- **Multiple OR Conditions**: >5 condizioni OR che bypassano indici
- **Excessive JOINs**: >4 JOIN in singola query
- **Missing Query Indexes**: Tabelle senza indici su colonne WHERE/ORDER BY

#### 🟡 MEDIA
- **ORDER BY senza LIMIT**: Potenziali problemi memory
- **Complex Derived Queries**: Nomi metodi con >8 condizioni
- **Missing Caching**: Query readonly frequenti non cachate
- **Subquery Inefficienti**: Pattern che potrebbero essere JOIN

#### 🔵 BASSA
- **Query Complexity Alta**: Score complessità >12 ma funzionali
- **Missing Fetch Joins**: Lazy loading che potrebbe essere eager
- **Naming Convention**: Metodi repository non standard
- **Documentation Missing**: Query complesse senza commenti

### Valore di Business

- **Performance Applicazione**: Query ottimizzate migliorano response time
- **Scalabilità Sistema**: Riduzione carico database per crescita utenti
- **Costi Infrastruttura**: Query efficienti riducono risorse database necessarie
- **User Experience**: Tempi risposta rapidi aumentano satisfaction
- **Resource Optimization**: Uso ottimale CPU/memoria database server