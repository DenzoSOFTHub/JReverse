# Report 08: Mappa Repository/Database

## Descrizione
Catalogazione completa dei repository (@Repository, JpaRepository) con analisi delle query personalizzate, metodi derivati, e mappatura delle operazioni di accesso ai dati.

## Valore per l'Utente
**⭐⭐⭐⭐⭐** - Valore Massimo
- Comprensione dell'architettura di persistenza
- Identificazione di query inefficienti
- Analisi delle performance del database
- Documentazione automatica delle operazioni CRUD
- Ottimizzazione dell'accesso ai dati

## Complessità di Implementazione
**🟡 Media** - Analisi di Spring Data JPA patterns

## Tempo di Realizzazione Stimato
**7-9 giorni** di sviluppo

## Implementazione Javassist

```java
public class RepositoryAnalyzer {
    
    public RepositoryAnalysis analyzeRepositoryLayer() {
        List<RepositoryInfo> repositories = findRepositoryClasses();
        
        for (RepositoryInfo repo : repositories) {
            analyzeRepositoryMethods(repo);
            analyzeCustomQueries(repo);
            analyzeEntityRelationships(repo);
        }
        
        return new RepositoryAnalysis(repositories);
    }
    
    private List<RepositoryInfo> findRepositoryClasses() {
        return classPool.getAllClasses().stream()
            .filter(this::isRepositoryClass)
            .map(this::createRepositoryInfo)
            .collect(Collectors.toList());
    }
    
    private boolean isRepositoryClass(CtClass clazz) {
        return clazz.hasAnnotation("org.springframework.stereotype.Repository") ||
               isJpaRepository(clazz);
    }
    
    private void analyzeRepositoryMethods(RepositoryInfo repoInfo) {
        try {
            CtClass repoClass = classPool.get(repoInfo.getClassName());
            
            for (CtMethod method : repoClass.getDeclaredMethods()) {
                QueryMethodInfo methodInfo = analyzeQueryMethod(method);
                repoInfo.addQueryMethod(methodInfo);
            }
        } catch (NotFoundException e) {
            logger.error("Repository class not found: {}", repoInfo.getClassName());
        }
    }
    
    private QueryMethodInfo analyzeQueryMethod(CtMethod method) {
        QueryMethodInfo info = new QueryMethodInfo();
        info.setMethodName(method.getName());
        
        // Analizza @Query annotation
        if (method.hasAnnotation("org.springframework.data.jpa.repository.Query")) {
            analyzeCustomQuery(method, info);
        } else {
            // Derived query method
            analyzeDerivedQuery(method, info);
        }
        
        // Analizza @Modifying
        if (method.hasAnnotation("org.springframework.data.jpa.repository.Modifying")) {
            info.setModifying(true);
        }
        
        return info;
    }
}
```

## Implementazione Javassist Completa

```java
public class RepositoryDatabaseAnalyzer {
    
    public RepositoryDatabaseReport analyzeRepositoryDatabaseMapping(CtClass[] classes) {
        RepositoryDatabaseReport report = new RepositoryDatabaseReport();
        
        for (CtClass ctClass : classes) {
            analyzeRepositoryClass(ctClass, report);
        }
        
        analyzeQueryPerformance(report);
        identifyRepositoryIssues(report);
        
        return report;
    }
    
    private void analyzeRepositoryClass(CtClass ctClass, RepositoryDatabaseReport report) {
        try {
            if (!isRepositoryClass(ctClass)) return;
            
            RepositoryInfo repoInfo = new RepositoryInfo();
            repoInfo.setClassName(ctClass.getName());
            
            // Analizza tipo repository (JpaRepository, CrudRepository, etc.)
            String repositoryType = extractRepositoryType(ctClass);
            repoInfo.setRepositoryType(repositoryType);
            
            // Estrai entity type dal generic
            String entityType = extractEntityType(ctClass);
            repoInfo.setEntityType(entityType);
            
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                analyzeRepositoryMethod(method, repoInfo, report);
            }
            
            report.addRepositoryInfo(repoInfo);
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi repository: " + e.getMessage());
        }
    }
    
    private void analyzeRepositoryMethod(CtMethod method, RepositoryInfo repoInfo, RepositoryDatabaseReport report) {
        try {
            QueryMethodInfo methodInfo = new QueryMethodInfo();
            methodInfo.setMethodName(method.getName());
            methodInfo.setMethodSignature(method.getSignature());
            
            // Analizza @Query personalizzate
            if (method.hasAnnotation("org.springframework.data.jpa.repository.Query")) {
                analyzeCustomQuery(method, methodInfo, report);
            } else {
                // Query derivate (findByName, etc.)
                analyzeDerivedQuery(method, methodInfo, report);
            }
            
            // Analizza @Modifying per operazioni di modifica
            if (method.hasAnnotation("org.springframework.data.jpa.repository.Modifying")) {
                methodInfo.setModifying(true);
                
                // Verifica se manca @Transactional
                if (!hasTransactionalAnnotation(method)) {
                    RepositoryIssue issue = new RepositoryIssue();
                    issue.setType(IssueType.MODIFYING_WITHOUT_TRANSACTIONAL);
                    issue.setClassName(method.getDeclaringClass().getName());
                    issue.setMethodName(method.getName());
                    issue.setSeverity(Severity.HIGH);
                    issue.setDescription("@Modifying query without @Transactional");
                    issue.setRecommendation("Add @Transactional annotation to method or class");
                    
                    report.addRepositoryIssue(issue);
                }
            }
            
            // Analizza parametri del metodo
            analyzeMethodParameters(method, methodInfo, report);
            
            repoInfo.addQueryMethod(methodInfo);
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi repository method: " + e.getMessage());
        }
    }
    
    private void analyzeCustomQuery(CtMethod method, QueryMethodInfo methodInfo, RepositoryDatabaseReport report) {
        try {
            Annotation queryAnnotation = method.getAnnotation("org.springframework.data.jpa.repository.Query");
            
            String queryValue = extractAnnotationValue(queryAnnotation, "value");
            methodInfo.setCustomQuery(queryValue);
            methodInfo.setQueryType("CUSTOM");
            
            // Analizza complessità query
            int queryComplexity = calculateQueryComplexity(queryValue);
            methodInfo.setComplexity(queryComplexity);
            
            // Identifica query potenzialmente lente
            if (isSlowQuery(queryValue)) {
                RepositoryIssue issue = new RepositoryIssue();
                issue.setType(IssueType.POTENTIALLY_SLOW_QUERY);
                issue.setClassName(method.getDeclaringClass().getName());
                issue.setMethodName(method.getName());
                issue.setSeverity(Severity.MEDIUM);
                issue.setDescription("Query potentially slow: " + queryValue);
                issue.setRecommendation("Consider adding indexes or optimizing query");
                
                report.addRepositoryIssue(issue);
            }
            
            // Verifica uso di native queries
            Boolean nativeQuery = extractBooleanAnnotationValue(queryAnnotation, "nativeQuery");
            if (Boolean.TRUE.equals(nativeQuery)) {
                methodInfo.setNativeQuery(true);
                
                RepositoryIssue issue = new RepositoryIssue();
                issue.setType(IssueType.NATIVE_QUERY_USAGE);
                issue.setClassName(method.getDeclaringClass().getName());
                issue.setMethodName(method.getName());
                issue.setSeverity(Severity.LOW);
                issue.setDescription("Usage of native SQL query reduces portability");
                issue.setRecommendation("Consider using JPQL if possible");
                
                report.addRepositoryIssue(issue);
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi custom query: " + e.getMessage());
        }
    }
    
    private void analyzeDerivedQuery(CtMethod method, QueryMethodInfo methodInfo, RepositoryDatabaseReport report) {
        try {
            String methodName = method.getName();
            methodInfo.setQueryType("DERIVED");
            
            // Analizza pattern Spring Data JPA (findBy, countBy, deleteBy, etc.)
            if (methodName.startsWith("findBy")) {
                methodInfo.setOperationType("SELECT");
                String condition = methodName.substring(6);
                methodInfo.setDerivedCondition(condition);
                
                // Verifica complessità condizioni (And, Or, etc.)
                int conditionComplexity = countLogicalOperators(condition);
                if (conditionComplexity > 3) {
                    RepositoryIssue issue = new RepositoryIssue();
                    issue.setType(IssueType.COMPLEX_DERIVED_QUERY);
                    issue.setClassName(method.getDeclaringClass().getName());
                    issue.setMethodName(method.getName());
                    issue.setSeverity(Severity.MEDIUM);
                    issue.setDescription("Complex derived query with many conditions");
                    issue.setRecommendation("Consider using @Query for better readability");
                    
                    report.addRepositoryIssue(issue);
                }
            } else if (methodName.startsWith("countBy")) {
                methodInfo.setOperationType("COUNT");
            } else if (methodName.startsWith("deleteBy") || methodName.startsWith("removeBy")) {
                methodInfo.setOperationType("DELETE");
                
                // Delete operations dovrebbero essere transazionali
                if (!hasTransactionalAnnotation(method)) {
                    RepositoryIssue issue = new RepositoryIssue();
                    issue.setType(IssueType.DELETE_WITHOUT_TRANSACTIONAL);
                    issue.setClassName(method.getDeclaringClass().getName());
                    issue.setMethodName(method.getName());
                    issue.setSeverity(Severity.HIGH);
                    issue.setDescription("Delete operation without @Transactional");
                    issue.setRecommendation("Add @Transactional annotation");
                    
                    report.addRepositoryIssue(issue);
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi derived query: " + e.getMessage());
        }
    }
    
    private void analyzeMethodParameters(CtMethod method, QueryMethodInfo methodInfo, RepositoryDatabaseReport report) {
        try {
            CtClass[] paramTypes = method.getParameterTypes();
            
            for (int i = 0; i < paramTypes.length; i++) {
                ParameterInfo paramInfo = new ParameterInfo();
                paramInfo.setType(paramTypes[i].getName());
                paramInfo.setPosition(i);
                
                // Verifica se usa @Param annotation
                Object[][] paramAnnotations = method.getParameterAnnotations();
                if (paramAnnotations[i].length > 0) {
                    for (Object annotation : paramAnnotations[i]) {
                        if (annotation.toString().contains("Param")) {
                            paramInfo.setHasParamAnnotation(true);
                        }
                    }
                }
                
                methodInfo.addParameter(paramInfo);
            }
            
            // Verifica uso inconsistente di @Param
            if (hasInconsistentParamUsage(methodInfo)) {
                RepositoryIssue issue = new RepositoryIssue();
                issue.setType(IssueType.INCONSISTENT_PARAM_ANNOTATION);
                issue.setClassName(method.getDeclaringClass().getName());
                issue.setMethodName(method.getName());
                issue.setSeverity(Severity.LOW);
                issue.setDescription("Inconsistent usage of @Param annotation");
                issue.setRecommendation("Use @Param consistently on all parameters");
                
                report.addRepositoryIssue(issue);
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi method parameters: " + e.getMessage());
        }
    }
}

public class RepositoryDatabaseReport {
    private List<RepositoryInfo> repositories = new ArrayList<>();
    private List<RepositoryIssue> repositoryIssues = new ArrayList<>();
    private RepositoryStatistics statistics;
    private List<String> errors = new ArrayList<>();
    
    public static class RepositoryInfo {
        private String className;
        private String repositoryType;
        private String entityType;
        private List<QueryMethodInfo> queryMethods = new ArrayList<>();
        private boolean hasCustomQueries;
        private boolean hasModifyingOperations;
    }
    
    public static class QueryMethodInfo {
        private String methodName;
        private String methodSignature;
        private String queryType; // CUSTOM, DERIVED
        private String operationType; // SELECT, INSERT, UPDATE, DELETE, COUNT
        private String customQuery;
        private String derivedCondition;
        private boolean isModifying;
        private boolean isNativeQuery;
        private int complexity;
        private List<ParameterInfo> parameters = new ArrayList<>();
    }
    
    public static class RepositoryIssue {
        private IssueType type;
        private String className;
        private String methodName;
        private Severity severity;
        private String description;
        private String recommendation;
    }
    
    public enum IssueType {
        MODIFYING_WITHOUT_TRANSACTIONAL,
        DELETE_WITHOUT_TRANSACTIONAL,
        POTENTIALLY_SLOW_QUERY,
        COMPLEX_DERIVED_QUERY,
        NATIVE_QUERY_USAGE,
        INCONSISTENT_PARAM_ANNOTATION,
        MISSING_REPOSITORY_ANNOTATION,
        EXCESSIVE_QUERY_COMPLEXITY
    }
    
    public enum Severity {
        CRITICAL, HIGH, MEDIUM, LOW
    }
}
```

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculateRepositoryDatabaseQualityScore(RepositoryDatabaseReport result) {
    double score = 100.0;
    
    // Penalizzazioni per problemi repository/database critici
    score -= result.getModifyingWithoutTransactional() * 25;      // -25 per @Modifying senza @Transactional
    score -= result.getDeleteWithoutTransactional() * 20;        // -20 per delete senza @Transactional
    score -= result.getPotentiallySlowQueries() * 15;            // -15 per query potenzialmente lente
    score -= result.getComplexDerivedQueries() * 10;             // -10 per derived query complesse
    score -= result.getMissingRepositoryAnnotations() * 8;       // -8 per repository senza annotazione
    score -= result.getExcessiveQueryComplexity() * 12;          // -12 per query eccessivamente complesse
    score -= result.getNativeQueryUsage() * 3;                   // -3 per uso native query
    score -= result.getInconsistentParamAnnotations() * 2;       // -2 per uso inconsistente @Param
    
    // Bonus per buone pratiche repository
    score += result.getProperlyAnnotatedRepositories() * 2;      // +2 per repository ben annotati
    score += result.getOptimizedQueries() * 3;                   // +3 per query ottimizzate
    score += result.getConsistentNamingConventions() * 1;        // +1 per naming consistente
    score += result.getProperTransactionBoundaries() * 2;        // +2 per transaction boundary corretti
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - Gravi problemi nell'accesso ai dati e gestione repository
- **41-60**: 🟡 SUFFICIENTE - Repository funzionali ma con lacune nell'ottimizzazione
- **61-80**: 🟢 BUONO - Accesso dati ben strutturato con alcuni miglioramenti
- **81-100**: ⭐ ECCELLENTE - Repository ottimizzati e best practices implementate

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -20 to -25)
1. **@Modifying senza @Transactional**
   - Descrizione: Operazioni di modifica senza contesto transazionale
   - Rischio: Inconsistenza dati, rollback impossibile
   - Soluzione: Aggiungere @Transactional a metodi o classi con @Modifying

2. **Operazioni delete senza @Transactional**
   - Descrizione: Delete operations senza gestione transazionale
   - Rischio: Perdita dati, inconsistency nel database
   - Soluzione: Implementare transaction boundary appropriati

### 🟠 GRAVITÀ ALTA (Score Impact: -12 to -15)  
3. **Query potenzialmente lente**
   - Descrizione: Query complesse senza ottimizzazioni apparenti
   - Rischio: Performance degradation, timeout database
   - Soluzione: Ottimizzare query, aggiungere indici, considerare pagination

4. **Complessità query eccessiva**
   - Descrizione: Query con troppi join o condizioni complesse
   - Rischio: Difficile manutenzione, performance issues
   - Soluzione: Semplificare query o usare approcci alternativi (projection)

### 🟡 GRAVITÀ MEDIA (Score Impact: -8 to -10)
5. **Derived query complesse**
   - Descrizione: Metodi findBy con troppe condizioni (And, Or)
   - Rischio: Leggibilità ridotta, errori nei nomi metodi
   - Soluzione: Sostituire con @Query esplicite per maggiore chiarezza

6. **Repository senza @Repository annotation**
   - Descrizione: Classi repository senza annotazione appropriata
   - Rischio: Problemi con component scanning, transaction management
   - Soluzione: Aggiungere @Repository annotation

### 🔵 GRAVITÀ BASSA (Score Impact: -1 to -3)
7. **Uso inconsistente @Param**
   - Descrizione: Alcuni parametri con @Param, altri senza
   - Rischio: Confusione nel mapping parametri, errori runtime
   - Soluzione: Standardizzare uso @Param su tutti i parametri named

8. **Utilizzo native queries**
   - Descrizione: Preference per SQL nativo invece di JPQL
   - Rischio: Ridotta portabilità tra database
   - Soluzione: Migrare a JPQL quando possibile

## Metriche di Valore

- **Data Integrity**: Garantisce operazioni CRUD sicure e transazionali
- **Performance**: Ottimizza accesso database e query execution
- **Maintainability**: Struttura repository chiara e ben documentata
- **Scalability**: Pattern di accesso dati efficienti per high-load scenarios

## Tags per Classificazione
`#repository` `#database` `#jpa` `#queries` `#data-access` `#performance` `#high-value` `#medium-complexity`