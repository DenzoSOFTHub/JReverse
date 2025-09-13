# Report 24: Query Native e JPQL Usate

**Valore**: ⭐⭐⭐⭐ **Complessità**: 🟡 Media **Tempo**: 6-7 giorni
**Tags**: `#query-analysis` `#jpql` `#native-queries` `#performance`

## Descrizione

Questo report analizza tutte le query native SQL e JPQL presenti nell'applicazione Spring Boot, identificando potenziali problemi di performance, sicurezza e manutenibilità. L'analisi include la rilevazione di query complesse, l'uso di parameter binding, e la presenza di SQL injection vulnerabilities.

## Obiettivi dell'Analisi

1. **Identificazione Query**: Rilevare tutte le query native SQL e JPQL attraverso annotations @Query
2. **Analisi Performance**: Valutare complessità delle query e potenziali colli di bottiglia
3. **Sicurezza**: Identificare possibili vulnerabilità SQL injection e best practices
4. **Manutenibilità**: Valutare leggibilità e struttura delle query
5. **Conformità JPA**: Verificare l'uso corretto di JPQL e convenzioni Spring Data

## Implementazione con Javassist

```java
package com.jreverse.analyzer.query;

import javassist.*;
import javassist.bytecode.*;
import javassist.expr.*;
import java.util.*;
import java.util.regex.*;
import java.io.IOException;

/**
 * Analyzer per query native SQL e JPQL usate nell'applicazione
 */
public class NativeJpqlQueriesAnalyzer {
    
    private ClassPool classPool;
    private Map<String, QueryInfo> queriesFound;
    private Map<String, List<SecurityIssue>> securityIssues;
    private Map<String, PerformanceMetric> performanceMetrics;
    private List<String> complexQueries;
    private int totalQueries;
    private int nativeQueries;
    private int jpqlQueries;
    
    // Pattern per identificare SQL injection vulnerabilities
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(.*\\+\\s*[\"'][^\"']*[\"'].*)|" +  // String concatenation
        "(.*\\$\\{[^}]*\\}.*)|" +               // EL expression in query
        "(.*(concat|format)\\s*\\(.*)|" +       // Dynamic concatenation functions
        "(.*\\?\\d+.*\\+.*)"                   // Parameter + concatenation mix
    );
    
    // Pattern per query complesse
    private static final Pattern COMPLEX_QUERY_PATTERN = Pattern.compile(
        "(?i).*(select.*from.*join.*join.*)|" +     // Multiple joins
        ".*(union.*union.*)|" +                     // Multiple unions
        ".*(exists.*exists.*)|" +                   // Nested exists
        ".*(case.*when.*case.*when.*)|" +           // Nested case statements
        ".*\\b(\\w+)\\s+in\\s*\\(\\s*select.*"     // Subquery in IN clause
    );
    
    public NativeJpqlQueriesAnalyzer() {
        this.classPool = ClassPool.getDefault();
        this.queriesFound = new HashMap<>();
        this.securityIssues = new HashMap<>();
        this.performanceMetrics = new HashMap<>();
        this.complexQueries = new ArrayList<>();
        this.totalQueries = 0;
        this.nativeQueries = 0;
        this.jpqlQueries = 0;
    }
    
    /**
     * Analizza tutte le query native e JPQL nell'applicazione
     */
    public QueryAnalysisReport analyzeQueries(List<String> classNames) throws Exception {
        for (String className : classNames) {
            try {
                CtClass ctClass = classPool.get(className);
                analyzeClassQueries(ctClass);
            } catch (NotFoundException | IOException e) {
                System.err.println("Errore nell'analisi della classe " + className + ": " + e.getMessage());
            }
        }
        
        return generateReport();
    }
    
    /**
     * Analizza le query in una singola classe
     */
    private void analyzeClassQueries(CtClass ctClass) throws Exception {
        // Analizza annotations @Query sui metodi
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            analyzeMethodQueries(ctClass.getName(), method);
        }
        
        // Analizza @NamedQuery e @NamedNativeQuery sulla classe
        analyzeNamedQueries(ctClass);
        
        // Analizza query dinamiche nei metodi
        analyzeDynamicQueries(ctClass);
    }
    
    /**
     * Analizza annotations @Query sui metodi
     */
    private void analyzeMethodQueries(String className, CtMethod method) throws Exception {
        AnnotationsAttribute attr = (AnnotationsAttribute) 
            method.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
        
        if (attr != null) {
            Annotation queryAnnotation = attr.getAnnotation("org.springframework.data.jpa.repository.Query");
            if (queryAnnotation != null) {
                analyzeQueryAnnotation(className, method.getName(), queryAnnotation);
            }
            
            // Analizza anche @Modifying per update/delete
            Annotation modifyingAnnotation = attr.getAnnotation("org.springframework.data.jpa.repository.Modifying");
            if (modifyingAnnotation != null && queryAnnotation != null) {
                analyzeModifyingQuery(className, method.getName(), queryAnnotation);
            }
        }
    }
    
    /**
     * Analizza una singola annotation @Query
     */
    private void analyzeQueryAnnotation(String className, String methodName, Annotation queryAnnotation) {
        try {
            String queryString = getAnnotationValue(queryAnnotation, "value");
            boolean isNative = getBooleanAnnotationValue(queryAnnotation, "nativeQuery");
            
            if (queryString != null && !queryString.trim().isEmpty()) {
                totalQueries++;
                if (isNative) {
                    nativeQueries++;
                } else {
                    jpqlQueries++;
                }
                
                String queryId = className + "." + methodName;
                QueryInfo queryInfo = new QueryInfo(queryId, queryString, isNative, className, methodName);
                queriesFound.put(queryId, queryInfo);
                
                // Analisi sicurezza
                analyzeQuerySecurity(queryId, queryString);
                
                // Analisi performance
                analyzeQueryPerformance(queryId, queryString, isNative);
                
                // Verifica complessità
                analyzeQueryComplexity(queryId, queryString);
            }
        } catch (Exception e) {
            System.err.println("Errore nell'analisi query annotation: " + e.getMessage());
        }
    }
    
    /**
     * Analizza @NamedQuery e @NamedNativeQuery
     */
    private void analyzeNamedQueries(CtClass ctClass) throws Exception {
        AnnotationsAttribute attr = (AnnotationsAttribute) 
            ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        
        if (attr != null) {
            // @NamedQuery
            Annotation namedQuery = attr.getAnnotation("javax.persistence.NamedQuery");
            if (namedQuery != null) {
                analyzeNamedQuery(ctClass.getName(), namedQuery, false);
            }
            
            // @NamedNativeQuery
            Annotation namedNativeQuery = attr.getAnnotation("javax.persistence.NamedNativeQuery");
            if (namedNativeQuery != null) {
                analyzeNamedQuery(ctClass.getName(), namedNativeQuery, true);
            }
            
            // @NamedQueries e @NamedNativeQueries (arrays)
            analyzeNamedQueryArrays(ctClass.getName(), attr);
        }
    }
    
    /**
     * Analizza query dinamiche create nei metodi
     */
    private void analyzeDynamicQueries(CtClass ctClass) throws Exception {
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            method.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    try {
                        if (m.getClassName().equals("javax.persistence.EntityManager") ||
                            m.getClassName().equals("org.hibernate.Session")) {
                            
                            if (m.getMethodName().equals("createQuery") || 
                                m.getMethodName().equals("createNativeQuery")) {
                                
                                analyzeDynamicQueryCall(ctClass.getName(), method.getName(), 
                                                      m.getMethodName(), m.getLineNumber());
                            }
                        }
                    } catch (Exception e) {
                        // Log error but continue analysis
                    }
                }
            });
        }
    }
    
    /**
     * Analizza sicurezza di una query
     */
    private void analyzeQuerySecurity(String queryId, String queryString) {
        List<SecurityIssue> issues = new ArrayList<>();
        
        // Check per SQL injection tramite concatenazione
        if (SQL_INJECTION_PATTERN.matcher(queryString).find()) {
            issues.add(new SecurityIssue(SecurityLevel.CRITICAL, 
                "Potenziale SQL injection tramite concatenazione string", queryString));
        }
        
        // Check per parametri non bound
        if (queryString.contains("?") && !queryString.contains("?1")) {
            issues.add(new SecurityIssue(SecurityLevel.HIGH,
                "Query con parametri posizionali non numerati", queryString));
        }
        
        // Check per query con LIKE senza escape
        if (queryString.toLowerCase().contains("like") && 
            !queryString.toLowerCase().contains("escape")) {
            issues.add(new SecurityIssue(SecurityLevel.MEDIUM,
                "Query LIKE senza carattere di escape definito", queryString));
        }
        
        // Check per query con privilegi elevati
        if (queryString.toLowerCase().matches(".*\\b(grant|revoke|create|drop|alter|truncate)\\b.*")) {
            issues.add(new SecurityIssue(SecurityLevel.HIGH,
                "Query con operazioni DDL/DCL potenzialmente pericolose", queryString));
        }
        
        if (!issues.isEmpty()) {
            securityIssues.put(queryId, issues);
        }
    }
    
    /**
     * Analizza performance di una query
     */
    private void analyzeQueryPerformance(String queryId, String queryString, boolean isNative) {
        PerformanceMetric metric = new PerformanceMetric();
        
        String lowerQuery = queryString.toLowerCase();
        
        // Analisi join
        int joinCount = countOccurrences(lowerQuery, "join");
        metric.joinCount = joinCount;
        if (joinCount > 3) {
            metric.addIssue("Query con molti JOIN (" + joinCount + "), potenziale impatto performance");
        }
        
        // Analisi subquery
        int subqueryCount = countOccurrences(lowerQuery, "select") - 1; // -1 per la query principale
        metric.subqueryCount = Math.max(0, subqueryCount);
        if (subqueryCount > 2) {
            metric.addIssue("Query con multiple subquery (" + subqueryCount + "), considera ottimizzazioni");
        }
        
        // Analisi ORDER BY senza LIMIT
        if (lowerQuery.contains("order by") && !lowerQuery.contains("limit") && !lowerQuery.contains("top")) {
            metric.addIssue("ORDER BY senza LIMIT potrebbe causare problemi di memoria");
        }
        
        // Analisi SELECT *
        if (lowerQuery.contains("select *")) {
            metric.addIssue("Uso di SELECT * riduce performance e manutenibilità");
        }
        
        // Analisi funzioni aggregate senza GROUP BY appropriato
        if (containsAggregateFunction(lowerQuery) && !lowerQuery.contains("group by")) {
            metric.addIssue("Funzioni aggregate senza GROUP BY esplicito");
        }
        
        // Calcolo score complessivo performance (0-100)
        metric.performanceScore = calculatePerformanceScore(queryString, isNative, joinCount, subqueryCount);
        
        performanceMetrics.put(queryId, metric);
    }
    
    /**
     * Analizza complessità di una query
     */
    private void analyzeQueryComplexity(String queryId, String queryString) {
        if (COMPLEX_QUERY_PATTERN.matcher(queryString).find()) {
            complexQueries.add(queryId);
        }
        
        // Calcolo indice di complessità basato su vari fattori
        int complexityIndex = calculateComplexityIndex(queryString);
        if (complexityIndex > 15) {
            complexQueries.add(queryId + " (complexity: " + complexityIndex + ")");
        }
    }
    
    /**
     * Calcola score di performance (0-100, più alto = migliore)
     */
    private int calculatePerformanceScore(String query, boolean isNative, int joinCount, int subqueryCount) {
        int score = 100;
        
        // Penalità per complessità
        score -= joinCount * 5;
        score -= subqueryCount * 8;
        
        // Penalità per pattern problematici
        String lowerQuery = query.toLowerCase();
        if (lowerQuery.contains("select *")) score -= 15;
        if (lowerQuery.contains("order by") && !lowerQuery.contains("limit")) score -= 10;
        if (lowerQuery.contains("like '%") && lowerQuery.indexOf("like '%") == lowerQuery.lastIndexOf("like '%")) score -= 20;
        if (countOccurrences(lowerQuery, "or") > 3) score -= 10;
        
        // Bonus per best practices
        if (lowerQuery.contains("limit") || lowerQuery.contains("top")) score += 5;
        if (query.matches(".*\\?\\d+.*")) score += 5; // Parametri numerati
        if (isNative && lowerQuery.contains("index")) score += 3; // Hint per indici
        
        return Math.max(0, Math.min(100, score));
    }
    
    /**
     * Calcola indice di complessità query
     */
    private int calculateComplexityIndex(String query) {
        int complexity = 0;
        String lowerQuery = query.toLowerCase();
        
        complexity += countOccurrences(lowerQuery, "select");
        complexity += countOccurrences(lowerQuery, "join") * 2;
        complexity += countOccurrences(lowerQuery, "union") * 3;
        complexity += countOccurrences(lowerQuery, "exists") * 2;
        complexity += countOccurrences(lowerQuery, "case") * 2;
        complexity += countOccurrences(lowerQuery, "group by");
        complexity += countOccurrences(lowerQuery, "order by");
        complexity += countOccurrences(lowerQuery, "having");
        complexity += (query.length() / 100); // Lunghezza query
        
        return complexity;
    }
    
    // Metodi utility
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
    
    private int countOccurrences(String text, String pattern) {
        return text.split(pattern, -1).length - 1;
    }
    
    private boolean containsAggregateFunction(String query) {
        return query.matches("(?i).*(count|sum|avg|max|min|group_concat)\\s*\\(.*");
    }
    
    private void analyzeNamedQuery(String className, Annotation annotation, boolean isNative) {
        // Implementazione analisi named query
        String name = getAnnotationValue(annotation, "name");
        String query = getAnnotationValue(annotation, "query");
        
        if (name != null && query != null) {
            totalQueries++;
            if (isNative) nativeQueries++; else jpqlQueries++;
            
            String queryId = className + ".@" + name;
            QueryInfo queryInfo = new QueryInfo(queryId, query, isNative, className, name);
            queriesFound.put(queryId, queryInfo);
            
            analyzeQuerySecurity(queryId, query);
            analyzeQueryPerformance(queryId, query, isNative);
            analyzeQueryComplexity(queryId, query);
        }
    }
    
    private void analyzeNamedQueryArrays(String className, AnnotationsAttribute attr) {
        // Implementazione per arrays di named queries
    }
    
    private void analyzeModifyingQuery(String className, String methodName, Annotation queryAnnotation) {
        // Analisi specifica per query di modifica (UPDATE/DELETE)
        String queryString = getAnnotationValue(queryAnnotation, "value");
        if (queryString != null) {
            String lowerQuery = queryString.toLowerCase();
            if (lowerQuery.contains("delete") && !lowerQuery.contains("where")) {
                String queryId = className + "." + methodName;
                List<SecurityIssue> issues = securityIssues.computeIfAbsent(queryId, k -> new ArrayList<>());
                issues.add(new SecurityIssue(SecurityLevel.CRITICAL,
                    "DELETE query senza WHERE clause - rischio cancellazione dati", queryString));
            }
        }
    }
    
    private void analyzeDynamicQueryCall(String className, String methodName, String callMethod, int line) {
        // Analisi chiamate dinamiche createQuery/createNativeQuery
        totalQueries++;
        String queryId = className + "." + methodName + ":" + line;
        
        PerformanceMetric metric = new PerformanceMetric();
        metric.addIssue("Query dinamica a runtime - difficile da ottimizzare staticamente");
        metric.performanceScore = 60; // Score di default per query dinamiche
        
        performanceMetrics.put(queryId, metric);
    }
    
    /**
     * Genera il report finale dell'analisi
     */
    private QueryAnalysisReport generateReport() {
        QueryAnalysisReport report = new QueryAnalysisReport();
        
        report.totalQueries = totalQueries;
        report.nativeQueries = nativeQueries;
        report.jpqlQueries = jpqlQueries;
        report.queriesFound = new HashMap<>(queriesFound);
        report.securityIssues = new HashMap<>(securityIssues);
        report.performanceMetrics = new HashMap<>(performanceMetrics);
        report.complexQueries = new ArrayList<>(complexQueries);
        
        // Calcolo metriche aggregate
        report.averagePerformanceScore = calculateAveragePerformanceScore();
        report.securityRiskLevel = calculateOverallSecurityRisk();
        report.totalSecurityIssues = securityIssues.values().stream()
            .mapToInt(List::size).sum();
        
        return report;
    }
    
    private double calculateAveragePerformanceScore() {
        return performanceMetrics.values().stream()
            .mapToInt(m -> m.performanceScore)
            .average()
            .orElse(0.0);
    }
    
    private SecurityLevel calculateOverallSecurityRisk() {
        boolean hasCritical = securityIssues.values().stream()
            .flatMap(List::stream)
            .anyMatch(issue -> issue.level == SecurityLevel.CRITICAL);
        
        if (hasCritical) return SecurityLevel.CRITICAL;
        
        long highIssues = securityIssues.values().stream()
            .flatMap(List::stream)
            .filter(issue -> issue.level == SecurityLevel.HIGH)
            .count();
        
        if (highIssues > 0) return SecurityLevel.HIGH;
        
        return securityIssues.isEmpty() ? SecurityLevel.LOW : SecurityLevel.MEDIUM;
    }
    
    // Classi di supporto
    public static class QueryInfo {
        public String queryId;
        public String queryString;
        public boolean isNative;
        public String className;
        public String methodName;
        
        public QueryInfo(String queryId, String queryString, boolean isNative, 
                        String className, String methodName) {
            this.queryId = queryId;
            this.queryString = queryString;
            this.isNative = isNative;
            this.className = className;
            this.methodName = methodName;
        }
    }
    
    public static class SecurityIssue {
        public SecurityLevel level;
        public String description;
        public String queryFragment;
        
        public SecurityIssue(SecurityLevel level, String description, String queryFragment) {
            this.level = level;
            this.description = description;
            this.queryFragment = queryFragment;
        }
    }
    
    public static class PerformanceMetric {
        public int performanceScore = 100;
        public int joinCount = 0;
        public int subqueryCount = 0;
        public List<String> issues = new ArrayList<>();
        
        public void addIssue(String issue) {
            this.issues.add(issue);
        }
    }
    
    public enum SecurityLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public static class QueryAnalysisReport {
        public int totalQueries;
        public int nativeQueries;
        public int jpqlQueries;
        public Map<String, QueryInfo> queriesFound;
        public Map<String, List<SecurityIssue>> securityIssues;
        public Map<String, PerformanceMetric> performanceMetrics;
        public List<String> complexQueries;
        public double averagePerformanceScore;
        public SecurityLevel securityRiskLevel;
        public int totalSecurityIssues;
    }
}
```

## Esempio di Output HTML

```html
<!DOCTYPE html>
<html>
<head>
    <title>Query Native e JPQL Analysis Report</title>
    <style>
        .query-container { margin: 20px 0; padding: 15px; border-left: 4px solid #007acc; }
        .native-query { border-left-color: #ff6b35; }
        .jpql-query { border-left-color: #4ecdc4; }
        .security-critical { background-color: #ffe6e6; }
        .security-high { background-color: #fff3cd; }
        .performance-poor { background-color: #f8d7da; }
        .performance-good { background-color: #d4edda; }
        .query-text { font-family: monospace; background: #f8f9fa; padding: 10px; }
    </style>
</head>
<body>
    <h1>🔍 Report: Query Native e JPQL Usate</h1>
    
    <div class="summary">
        <h2>📊 Riepilogo Generale</h2>
        <ul>
            <li><strong>Totale Query:</strong> 45</li>
            <li><strong>Query Native SQL:</strong> 18 (40%)</li>
            <li><strong>Query JPQL:</strong> 27 (60%)</li>
            <li><strong>Score Performance Medio:</strong> 78/100</li>
            <li><strong>Livello Rischio Sicurezza:</strong> 🟡 MEDIO</li>
            <li><strong>Query Complesse:</strong> 8</li>
        </ul>
    </div>
    
    <div class="queries-analysis">
        <h2>📋 Analisi Dettagliata Query</h2>
        
        <div class="query-container native-query security-critical">
            <h3>🔴 UserRepository.findUsersByCustomCriteria</h3>
            <div class="query-text">
                SELECT * FROM users u WHERE u.name = ? + ' AND u.role = ' + ?2
            </div>
            <div class="metrics">
                <span class="performance-score">Performance: 25/100</span>
                <span class="security-level">Sicurezza: 🔴 CRITICA</span>
            </div>
            <div class="issues">
                <h4>⚠️ Problemi Identificati:</h4>
                <ul>
                    <li>🔴 <strong>SQL Injection</strong>: Concatenazione di stringhe nella query</li>
                    <li>🟠 <strong>Performance</strong>: Uso di SELECT * senza necessità</li>
                    <li>🟡 <strong>Manutenibilità</strong>: Query complessa senza documentazione</li>
                </ul>
            </div>
        </div>
        
        <div class="query-container jpql-query performance-good">
            <h3>🟢 ProductRepository.findActiveProducts</h3>
            <div class="query-text">
                SELECT p FROM Product p WHERE p.active = true ORDER BY p.createdDate DESC
            </div>
            <div class="metrics">
                <span class="performance-score">Performance: 95/100</span>
                <span class="security-level">Sicurezza: 🟢 BASSA</span>
            </div>
            <div class="best-practices">
                <h4>✅ Best Practices Applicate:</h4>
                <ul>
                    <li>Query JPQL ottimizzata</li>
                    <li>Uso di parametri type-safe</li>
                    <li>Logica di business chiara</li>
                </ul>
            </div>
        </div>
    </div>
    
    <div class="security-analysis">
        <h2>🔒 Analisi Sicurezza</h2>
        <div class="security-summary">
            <h3>Distribuzione Problemi Sicurezza</h3>
            <ul>
                <li>🔴 <strong>Critici:</strong> 2 issues</li>
                <li>🟠 <strong>Alti:</strong> 5 issues</li>
                <li>🟡 <strong>Medi:</strong> 8 issues</li>
                <li>🔵 <strong>Bassi:</strong> 3 issues</li>
            </ul>
        </div>
    </div>
    
    <div class="performance-analysis">
        <h2>⚡ Analisi Performance</h2>
        <div class="performance-charts">
            <h3>Distribuzione Score Performance</h3>
            <ul>
                <li>🟢 <strong>Eccellenti (90-100):</strong> 12 query</li>
                <li>🟡 <strong>Buone (70-89):</strong> 20 query</li>
                <li>🟠 <strong>Mediocri (50-69):</strong> 10 query</li>
                <li>🔴 <strong>Problematiche (&lt;50):</strong> 3 query</li>
            </ul>
        </div>
    </div>
</body>
</html>
```

## Metriche di Qualità del Codice

### Algoritmo di Scoring (0-100)

```java
public class QueryQualityScorer {
    
    public int calculateQualityScore(QueryAnalysisReport report) {
        int baseScore = 100;
        
        // Penalità per problemi di sicurezza
        baseScore -= report.totalSecurityIssues * 8;
        
        // Penalità per performance scadente
        double avgPerf = report.averagePerformanceScore;
        if (avgPerf < 50) baseScore -= 25;
        else if (avgPerf < 70) baseScore -= 15;
        else if (avgPerf < 85) baseScore -= 5;
        
        // Penalità per query complesse
        int complexityRatio = (report.complexQueries.size() * 100) / report.totalQueries;
        baseScore -= complexityRatio / 2;
        
        // Penalità per alto rapporto native queries
        int nativeRatio = (report.nativeQueries * 100) / report.totalQueries;
        if (nativeRatio > 70) baseScore -= 15;
        else if (nativeRatio > 50) baseScore -= 8;
        
        // Bonus per best practices
        if (report.securityRiskLevel == SecurityLevel.LOW) baseScore += 10;
        if (avgPerf > 85) baseScore += 5;
        
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
| **Problemi Sicurezza** | 0-2 | 3-5 | 6-10 | >10 |
| **Query Complesse (%)** | <10% | 10-20% | 20-30% | >30% |
| **Native Queries (%)** | <30% | 30-50% | 50-70% | >70% |

### Segnalazioni per Gravità

#### 🔴 CRITICA
- **SQL Injection Vulnerabilities**: Query con concatenazione di stringhe non sicura
- **DELETE/UPDATE senza WHERE**: Operazioni di massa non controllate  
- **Privilegi DDL Eccessivi**: Query con CREATE, DROP, ALTER inappropriati
- **Performance Score <30**: Query estremamente inefficienti

#### 🟠 ALTA  
- **Query N+1 Pattern**: Potenziali problemi di performance in cicli
- **Subquery Non Ottimizzate**: Troppi livelli di nesting
- **Parametri Non Bound**: Uso di parametri posizionali incorretti
- **SELECT * Eccessivo**: Selezione dati non necessari

#### 🟡 MEDIA
- **Query Complesse**: Logica difficile da manutenere
- **ORDER BY senza LIMIT**: Potenziali problemi di memoria  
- **LIKE senza Escape**: Pattern matching non sicuro
- **Mancanza Indici Hint**: Performance non ottimizzata

#### 🔵 BASSA
- **Convenzioni Nomenclatura**: Naming inconsistente per named queries
- **Documentazione Mancante**: Query complesse senza commenti
- **Partizionamento Suggerito**: Ottimizzazioni per large dataset
- **Cache Query Consigliate**: Query frequenti non cachate

### Valore di Business

- **Identificazione Rischi Sicurezza**: Prevenzione SQL injection e vulnerabilità
- **Ottimizzazione Performance**: Identificazione colli di bottiglia query
- **Riduzione Costi Infrastruttura**: Query più efficienti = minor carico DB
- **Manutenibilità Codice**: Query ben strutturate e documentate
- **Compliance Normativo**: Audit trail per operazioni sui dati sensibili