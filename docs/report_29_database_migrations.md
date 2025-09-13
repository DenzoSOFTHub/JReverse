# Report 29: Mappa delle Migrazioni DB

**Valore**: ⭐⭐⭐ **Complessità**: 🟡 Media **Tempo**: 4-5 giorni
**Tags**: `#database-migrations` `#liquibase` `#flyway` `#schema-evolution`

## Descrizione

Questo report analizza il sistema di migrazione database dell'applicazione, identificando script Flyway/Liquibase, sequenze di migrazione, potenziali conflitti e best practices per l'evoluzione controllata dello schema database.

## Obiettivi dell'Analisi

1. **Rilevamento Migration Tools**: Identificare Flyway, Liquibase o sistemi custom
2. **Analisi Sequenze**: Verificare ordine e dipendenze tra migrazioni
3. **Conflict Detection**: Identificare potenziali conflitti di migrazione
4. **Rollback Analysis**: Valutare strategie di rollback disponibili
5. **Performance Impact**: Analizzare impatto performance delle migrazioni

## Implementazione con Javassist

```java
package com.jreverse.analyzer.migrations;

import javassist.*;
import javassist.bytecode.*;
import javassist.expr.*;
import java.util.*;
import java.util.regex.*;
import java.io.IOException;

/**
 * Analyzer per migrazioni database nell'applicazione
 */
public class DatabaseMigrationsAnalyzer {
    
    private ClassPool classPool;
    private Map<String, MigrationScript> migrationScripts;
    private Map<String, MigrationConfig> migrationConfigurations;
    private List<MigrationIssue> migrationIssues;
    private List<ConflictAnalysis> conflicts;
    private MigrationTool detectedTool;
    
    // Pattern per identificare file di migrazione
    private static final Pattern FLYWAY_PATTERN = Pattern.compile(
        "^V(\\d+(?:\\.\\d+)*)__(\\w+.*)\\.sql$"
    );
    
    private static final Pattern LIQUIBASE_PATTERN = Pattern.compile(
        ".*(changelog|migration).*\\.(xml|yml|yaml|json)$"
    );
    
    // Pattern per comandi SQL pericolosi
    private static final Pattern DANGEROUS_SQL_PATTERN = Pattern.compile(
        "(?i)\\b(DROP\\s+TABLE|TRUNCATE|DELETE\\s+FROM(?!.*WHERE)|ALTER\\s+TABLE.*DROP)\\b"
    );
    
    // Pattern per performance impact
    private static final Pattern PERFORMANCE_PATTERN = Pattern.compile(
        "(?i)\\b(CREATE\\s+INDEX|DROP\\s+INDEX|ALTER\\s+TABLE.*ADD\\s+INDEX|REINDEX)\\b"
    );
    
    public DatabaseMigrationsAnalyzer() {
        this.classPool = ClassPool.getDefault();
        this.migrationScripts = new HashMap<>();
        this.migrationConfigurations = new HashMap<>();
        this.migrationIssues = new ArrayList<>();
        this.conflicts = new ArrayList<>();
        this.detectedTool = MigrationTool.NONE;
    }
    
    /**
     * Analizza tutte le migrazioni database
     */
    public MigrationAnalysisReport analyzeMigrations(List<String> classNames, 
                                                   List<String> migrationFiles) throws Exception {
        // Fase 1: Identifica migration tool
        detectMigrationTool(classNames, migrationFiles);
        
        // Fase 2: Analizza configurazioni
        analyzeMigrationConfigurations(classNames);
        
        // Fase 3: Analizza script di migrazione
        analyzeMigrationFiles(migrationFiles);
        
        // Fase 4: Verifica sequenze e dipendenze
        analyzeMigrationSequences();
        
        // Fase 5: Conflict detection
        detectMigrationConflicts();
        
        // Fase 6: Performance analysis
        analyzePerformanceImpact();
        
        return generateReport();
    }
    
    /**
     * Rileva il tool di migrazione utilizzato
     */
    private void detectMigrationTool(List<String> classNames, List<String> migrationFiles) throws Exception {
        // Verifica Flyway
        for (String className : classNames) {
            if (className.contains("flyway") || className.contains("Flyway")) {
                try {
                    CtClass ctClass = classPool.get(className);
                    if (isFlywayConfiguration(ctClass)) {
                        detectedTool = MigrationTool.FLYWAY;
                        analyzeFlywayConfiguration(ctClass);
                        break;
                    }
                } catch (Exception e) {
                    // Continue checking
                }
            }
        }
        
        // Verifica Liquibase
        if (detectedTool == MigrationTool.NONE) {
            for (String className : classNames) {
                if (className.contains("liquibase") || className.contains("Liquibase")) {
                    try {
                        CtClass ctClass = classPool.get(className);
                        if (isLiquibaseConfiguration(ctClass)) {
                            detectedTool = MigrationTool.LIQUIBASE;
                            analyzeLiquibaseConfiguration(ctClass);
                            break;
                        }
                    } catch (Exception e) {
                        // Continue checking
                    }
                }
            }
        }
        
        // Verifica dai file di migrazione
        if (detectedTool == MigrationTool.NONE) {
            detectToolFromFiles(migrationFiles);
        }
    }
    
    /**
     * Verifica se è una configurazione Flyway
     */
    private boolean isFlywayConfiguration(CtClass ctClass) throws Exception {
        // Verifica import Flyway
        for (CtField field : ctClass.getDeclaredFields()) {
            String fieldType = field.getType().getName();
            if (fieldType.contains("flyway") || fieldType.contains("Flyway")) {
                return true;
            }
        }
        
        // Verifica annotations
        AnnotationsAttribute attr = (AnnotationsAttribute)
            ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        
        if (attr != null) {
            for (Annotation annotation : attr.getAnnotations()) {
                if (annotation.getTypeName().contains("Flyway")) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Verifica se è una configurazione Liquibase
     */
    private boolean isLiquibaseConfiguration(CtClass ctClass) throws Exception {
        // Verifica import Liquibase
        for (CtField field : ctClass.getDeclaredFields()) {
            String fieldType = field.getType().getName();
            if (fieldType.contains("liquibase") || fieldType.contains("Liquibase")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Rileva tool dai file di migrazione
     */
    private void detectToolFromFiles(List<String> migrationFiles) {
        for (String filename : migrationFiles) {
            if (FLYWAY_PATTERN.matcher(filename).matches()) {
                detectedTool = MigrationTool.FLYWAY;
                return;
            } else if (LIQUIBASE_PATTERN.matcher(filename).matches()) {
                detectedTool = MigrationTool.LIQUIBASE;
                return;
            }
        }
        
        // Verifica custom tool
        if (!migrationFiles.isEmpty()) {
            detectedTool = MigrationTool.CUSTOM;
        }
    }
    
    /**
     * Analizza configurazione Flyway
     */
    private void analyzeFlywayConfiguration(CtClass configClass) throws Exception {
        String className = configClass.getName();
        MigrationConfig config = new MigrationConfig(className, MigrationTool.FLYWAY);
        
        // Analizza metodi di configurazione
        for (CtMethod method : configClass.getDeclaredMethods()) {
            if (isFlywayConfigurationMethod(method)) {
                analyzeFlywayConfigurationMethod(method, config);
            }
        }
        
        // Analizza properties
        analyzeFlywayProperties(config);
        
        migrationConfigurations.put(className, config);
    }
    
    /**
     * Verifica se è un metodo di configurazione Flyway
     */
    private boolean isFlywayConfigurationMethod(CtMethod method) throws Exception {
        String methodName = method.getName();
        CtClass returnType = method.getReturnType();
        
        return (methodName.startsWith("flyway") || 
                returnType.getName().contains("Flyway")) &&
               hasConfigurationAnnotation(method);
    }
    
    /**
     * Verifica se ha annotation di configurazione
     */
    private boolean hasConfigurationAnnotation(CtMethod method) throws Exception {
        AnnotationsAttribute attr = (AnnotationsAttribute)
            method.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
        
        return attr != null && 
               attr.getAnnotation("org.springframework.context.annotation.Bean") != null;
    }
    
    /**
     * Analizza metodo configurazione Flyway
     */
    private void analyzeFlywayConfigurationMethod(CtMethod method, MigrationConfig config) throws Exception {
        method.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                try {
                    String methodName = m.getMethodName();
                    String className = m.getClassName();
                    
                    if (className.contains("Flyway") && isFlywayConfigMethod(methodName)) {
                        FlywayConfigOption option = new FlywayConfigOption();
                        option.methodName = methodName;
                        option.lineNumber = m.getLineNumber();
                        option.configType = determineFlywayConfigType(methodName);
                        config.configOptions.add(option);
                    }
                    
                } catch (Exception e) {
                    // Log but continue
                }
            }
        });
    }
    
    /**
     * Verifica se è un metodo di configurazione Flyway
     */
    private boolean isFlywayConfigMethod(String methodName) {
        return methodName.startsWith("locations") ||
               methodName.startsWith("baselineOnMigrate") ||
               methodName.startsWith("validateOnMigrate") ||
               methodName.startsWith("cleanDisabled") ||
               methodName.startsWith("outOfOrder") ||
               methodName.startsWith("target");
    }
    
    /**
     * Determina tipo di configurazione Flyway
     */
    private FlywayConfigType determineFlywayConfigType(String methodName) {
        if (methodName.contains("location")) return FlywayConfigType.LOCATION;
        if (methodName.contains("baseline")) return FlywayConfigType.BASELINE;
        if (methodName.contains("validate")) return FlywayConfigType.VALIDATION;
        if (methodName.contains("clean")) return FlywayConfigType.CLEAN;
        if (methodName.contains("target")) return FlywayConfigType.TARGET;
        return FlywayConfigType.GENERAL;
    }
    
    /**
     * Analizza proprietà Flyway
     */
    private void analyzeFlywayProperties(MigrationConfig config) {
        // Simula lettura flyway properties
        Map<String, String> properties = loadFlywayProperties();
        
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            ConfigProperty property = new ConfigProperty();
            property.name = key;
            property.value = value;
            property.source = "application.properties";
            
            // Analisi sicurezza
            if (key.contains("clean-disabled") && "false".equals(value)) {
                migrationIssues.add(new MigrationIssue(
                    IssueLevel.CRITICAL,
                    "Flyway clean abilitato in production - rischio perdita dati",
                    key
                ));
            }
            
            // Analisi best practices
            if (key.contains("validate-on-migrate") && "false".equals(value)) {
                migrationIssues.add(new MigrationIssue(
                    IssueLevel.MEDIUM,
                    "Validazione migrazioni disabilitata",
                    key
                ));
            }
            
            config.properties.put(key, property);
        }
    }
    
    /**
     * Carica proprietà Flyway (simulato)
     */
    private Map<String, String> loadFlywayProperties() {
        Map<String, String> props = new HashMap<>();
        props.put("spring.flyway.locations", "classpath:db/migration");
        props.put("spring.flyway.clean-disabled", "true");
        props.put("spring.flyway.validate-on-migrate", "true");
        props.put("spring.flyway.baseline-on-migrate", "true");
        return props;
    }
    
    /**
     * Analizza configurazione Liquibase
     */
    private void analyzeLiquibaseConfiguration(CtClass configClass) throws Exception {
        String className = configClass.getName();
        MigrationConfig config = new MigrationConfig(className, MigrationTool.LIQUIBASE);
        
        // Analizza changelog locations
        for (CtMethod method : configClass.getDeclaredMethods()) {
            if (isLiquibaseConfigurationMethod(method)) {
                analyzeLiquibaseConfigurationMethod(method, config);
            }
        }
        
        migrationConfigurations.put(className, config);
    }
    
    /**
     * Verifica se è un metodo configurazione Liquibase
     */
    private boolean isLiquibaseConfigurationMethod(CtMethod method) throws Exception {
        String methodName = method.getName();
        CtClass returnType = method.getReturnType();
        
        return methodName.contains("liquibase") || 
               returnType.getName().contains("Liquibase");
    }
    
    /**
     * Analizza metodo configurazione Liquibase
     */
    private void analyzeLiquibaseConfigurationMethod(CtMethod method, MigrationConfig config) throws Exception {
        // Implementazione analisi Liquibase
        config.configOptions.add(new FlywayConfigOption()); // Placeholder
    }
    
    /**
     * Analizza configurazioni di migrazione
     */
    private void analyzeMigrationConfigurations(List<String> classNames) throws Exception {
        for (String className : classNames) {
            try {
                CtClass ctClass = classPool.get(className);
                
                // Verifica configurazioni custom
                if (hasCustomMigrationConfiguration(ctClass)) {
                    analyzeCustomMigrationConfiguration(ctClass);
                }
                
            } catch (Exception e) {
                System.err.println("Errore nell'analisi configurazione: " + e.getMessage());
            }
        }
    }
    
    /**
     * Verifica se ha configurazione custom
     */
    private boolean hasCustomMigrationConfiguration(CtClass ctClass) throws Exception {
        // Cerca annotazioni custom o implementazioni specifiche
        return false; // Implementazione semplificata
    }
    
    /**
     * Analizza configurazione custom
     */
    private void analyzeCustomMigrationConfiguration(CtClass ctClass) {
        // Implementazione per configurazioni custom
    }
    
    /**
     * Analizza file di migrazione
     */
    private void analyzeMigrationFiles(List<String> migrationFiles) {
        for (String filename : migrationFiles) {
            MigrationScript script = new MigrationScript(filename);
            
            if (detectedTool == MigrationTool.FLYWAY) {
                analyzeFlywayScript(script);
            } else if (detectedTool == MigrationTool.LIQUIBASE) {
                analyzeLiquibaseScript(script);
            } else {
                analyzeCustomScript(script);
            }
            
            migrationScripts.put(filename, script);
        }
    }
    
    /**
     * Analizza script Flyway
     */
    private void analyzeFlywayScript(MigrationScript script) {
        Matcher matcher = FLYWAY_PATTERN.matcher(script.filename);
        if (matcher.matches()) {
            script.version = matcher.group(1);
            script.description = matcher.group(2);
            script.tool = MigrationTool.FLYWAY;
            
            // Simula lettura contenuto file SQL
            String content = loadFileContent(script.filename);
            analyzeScriptContent(script, content);
        } else {
            migrationIssues.add(new MigrationIssue(
                IssueLevel.HIGH,
                "Nome file Flyway non conforme: " + script.filename,
                script.filename
            ));
        }
    }
    
    /**
     * Analizza script Liquibase
     */
    private void analyzeLiquibaseScript(MigrationScript script) {
        script.tool = MigrationTool.LIQUIBASE;
        
        // Determina formato (XML, YAML, JSON)
        if (script.filename.endsWith(".xml")) {
            script.format = ScriptFormat.XML;
        } else if (script.filename.endsWith(".yml") || script.filename.endsWith(".yaml")) {
            script.format = ScriptFormat.YAML;
        } else if (script.filename.endsWith(".json")) {
            script.format = ScriptFormat.JSON;
        }
        
        // Simula parsing contenuto Liquibase
        String content = loadFileContent(script.filename);
        analyzeLiquibaseContent(script, content);
    }
    
    /**
     * Analizza script custom
     */
    private void analyzeCustomScript(MigrationScript script) {
        script.tool = MigrationTool.CUSTOM;
        
        // Tenta di estrarre versione da naming convention
        extractVersionFromCustomScript(script);
        
        String content = loadFileContent(script.filename);
        analyzeScriptContent(script, content);
    }
    
    /**
     * Carica contenuto file (simulato)
     */
    private String loadFileContent(String filename) {
        // In implementazione reale, leggerebbe il file
        return "-- Example migration content\n" +
               "CREATE TABLE users (\n" +
               "    id SERIAL PRIMARY KEY,\n" +
               "    username VARCHAR(50) NOT NULL\n" +
               ");";
    }
    
    /**
     * Analizza contenuto script SQL
     */
    private void analyzeScriptContent(MigrationScript script, String content) {
        script.content = content;
        script.lineCount = content.split("\n").length;
        
        // Analisi comandi SQL
        analyzeStatements(script, content);
        
        // Analisi sicurezza
        analyzeSecurityRisks(script, content);
        
        // Analisi performance
        analyzePerformanceRisks(script, content);
        
        // Analisi rollback
        analyzeRollbackCapability(script, content);
    }
    
    /**
     * Analizza statement SQL
     */
    private void analyzeStatements(MigrationScript script, String content) {
        String upperContent = content.toUpperCase();
        
        // Conta tipi di statement
        script.createStatements = countOccurrences(upperContent, "CREATE");
        script.alterStatements = countOccurrences(upperContent, "ALTER");
        script.dropStatements = countOccurrences(upperContent, "DROP");
        script.insertStatements = countOccurrences(upperContent, "INSERT");
        script.updateStatements = countOccurrences(upperContent, "UPDATE");
        script.deleteStatements = countOccurrences(upperContent, "DELETE");
        
        // Calcola complexity score
        script.complexityScore = calculateComplexityScore(script);
    }
    
    /**
     * Analizza rischi di sicurezza
     */
    private void analyzeSecurityRisks(MigrationScript script, String content) {
        Matcher dangerousMatcher = DANGEROUS_SQL_PATTERN.matcher(content);
        while (dangerousMatcher.find()) {
            SecurityRisk risk = new SecurityRisk();
            risk.riskType = SecurityRiskType.DANGEROUS_OPERATION;
            risk.description = "Operazione pericolosa: " + dangerousMatcher.group();
            risk.lineNumber = getLineNumber(content, dangerousMatcher.start());
            script.securityRisks.add(risk);
            
            migrationIssues.add(new MigrationIssue(
                IssueLevel.HIGH,
                "Operazione SQL pericolosa in " + script.filename + ": " + dangerousMatcher.group(),
                script.filename
            ));
        }
        
        // Verifica operazioni senza WHERE clause
        if (content.toUpperCase().contains("DELETE FROM") && 
            !content.toUpperCase().contains("WHERE")) {
            SecurityRisk risk = new SecurityRisk();
            risk.riskType = SecurityRiskType.MISSING_WHERE_CLAUSE;
            risk.description = "DELETE senza WHERE clause";
            script.securityRisks.add(risk);
        }
    }
    
    /**
     * Analizza rischi di performance
     */
    private void analyzePerformanceRisks(MigrationScript script, String content) {
        Matcher perfMatcher = PERFORMANCE_PATTERN.matcher(content);
        while (perfMatcher.find()) {
            PerformanceImpact impact = new PerformanceImpact();
            impact.impactType = PerformanceImpactType.INDEX_OPERATION;
            impact.description = "Operazione su indice: " + perfMatcher.group();
            impact.estimatedDuration = estimateOperationDuration(perfMatcher.group());
            script.performanceImpacts.add(impact);
        }
        
        // Verifica operazioni massive
        if (script.insertStatements > 1000) {
            PerformanceImpact impact = new PerformanceImpact();
            impact.impactType = PerformanceImpactType.BULK_OPERATION;
            impact.description = "Molti INSERT statements: " + script.insertStatements;
            impact.estimatedDuration = "LONG";
            script.performanceImpacts.add(impact);
        }
    }
    
    /**
     * Analizza capacità di rollback
     */
    private void analyzeRollbackCapability(MigrationScript script, String content) {
        // DDL operations are generally not rollback-able
        if (script.createStatements > 0 || script.alterStatements > 0 || script.dropStatements > 0) {
            script.isRollbackable = false;
            script.rollbackRisk = RollbackRisk.HIGH;
        } else {
            script.isRollbackable = true;
            script.rollbackRisk = RollbackRisk.LOW;
        }
        
        // Check for transaction control
        if (content.toUpperCase().contains("BEGIN") && content.toUpperCase().contains("COMMIT")) {
            script.hasTransactionControl = true;
        }
    }
    
    /**
     * Analizza contenuto Liquibase
     */
    private void analyzeLiquibaseContent(MigrationScript script, String content) {
        // Analisi specifica per Liquibase
        script.content = content;
        
        if (content.contains("changeSet")) {
            script.hasChangeSets = true;
            script.changeSetCount = countOccurrences(content, "changeSet");
        }
        
        if (content.contains("rollback")) {
            script.hasRollbackInfo = true;
            script.isRollbackable = true;
        }
    }
    
    /**
     * Estrae versione da script custom
     */
    private void extractVersionFromCustomScript(MigrationScript script) {
        // Pattern comuni per script custom
        Pattern customVersionPattern = Pattern.compile("(\\d+)");
        Matcher matcher = customVersionPattern.matcher(script.filename);
        if (matcher.find()) {
            script.version = matcher.group(1);
        }
    }
    
    /**
     * Analizza sequenze di migrazione
     */
    private void analyzeMigrationSequences() {
        List<MigrationScript> sortedScripts = new ArrayList<>(migrationScripts.values());
        sortedScripts.sort((a, b) -> compareVersions(a.version, b.version));
        
        for (int i = 0; i < sortedScripts.size() - 1; i++) {
            MigrationScript current = sortedScripts.get(i);
            MigrationScript next = sortedScripts.get(i + 1);
            
            // Verifica gap nelle versioni
            if (hasVersionGap(current.version, next.version)) {
                migrationIssues.add(new MigrationIssue(
                    IssueLevel.MEDIUM,
                    "Gap nella sequenza versioni: " + current.version + " -> " + next.version,
                    current.filename + " | " + next.filename
                ));
            }
        }
    }
    
    /**
     * Rileva conflitti di migrazione
     */
    private void detectMigrationConflicts() {
        Map<String, List<MigrationScript>> versionGroups = groupByVersion();
        
        for (Map.Entry<String, List<MigrationScript>> entry : versionGroups.entrySet()) {
            if (entry.getValue().size() > 1) {
                ConflictAnalysis conflict = new ConflictAnalysis();
                conflict.version = entry.getKey();
                conflict.conflictingScripts = entry.getValue();
                conflict.conflictType = ConflictType.DUPLICATE_VERSION;
                conflict.severity = ConflictSeverity.HIGH;
                conflicts.add(conflict);
                
                migrationIssues.add(new MigrationIssue(
                    IssueLevel.CRITICAL,
                    "Versione duplicata: " + entry.getKey(),
                    entry.getValue().stream().map(s -> s.filename).reduce((a, b) -> a + ", " + b).orElse("")
                ));
            }
        }
        
        // Analizza conflitti di schema
        detectSchemaConflicts();
    }
    
    /**
     * Rileva conflitti di schema
     */
    private void detectSchemaConflicts() {
        Set<String> createdTables = new HashSet<>();
        Set<String> droppedTables = new HashSet<>();
        
        for (MigrationScript script : migrationScripts.values()) {
            // Estrae nomi tabelle create/droppate (semplificato)
            String content = script.content.toUpperCase();
            
            // CREATE TABLE
            Pattern createTablePattern = Pattern.compile("CREATE\\s+TABLE\\s+(\\w+)");
            Matcher createMatcher = createTablePattern.matcher(content);
            while (createMatcher.find()) {
                String tableName = createMatcher.group(1);
                if (droppedTables.contains(tableName)) {
                    ConflictAnalysis conflict = new ConflictAnalysis();
                    conflict.conflictType = ConflictType.TABLE_RECREATE;
                    conflict.severity = ConflictSeverity.MEDIUM;
                    conflict.description = "Tabella ricreata: " + tableName;
                    conflicts.add(conflict);
                }
                createdTables.add(tableName);
            }
            
            // DROP TABLE
            Pattern dropTablePattern = Pattern.compile("DROP\\s+TABLE\\s+(\\w+)");
            Matcher dropMatcher = dropTablePattern.matcher(content);
            while (dropMatcher.find()) {
                String tableName = dropMatcher.group(1);
                droppedTables.add(tableName);
            }
        }
    }
    
    /**
     * Analizza impatto performance
     */
    private void analyzePerformanceImpact() {
        for (MigrationScript script : migrationScripts.values()) {
            if (!script.performanceImpacts.isEmpty()) {
                // Calcola impatto totale
                int totalImpact = script.performanceImpacts.size();
                
                if (totalImpact > 5) {
                    migrationIssues.add(new MigrationIssue(
                        IssueLevel.MEDIUM,
                        "Alto impatto performance: " + totalImpact + " operazioni critiche",
                        script.filename
                    ));
                }
            }
        }
    }
    
    // Metodi utility
    private int countOccurrences(String text, String pattern) {
        return text.split(pattern, -1).length - 1;
    }
    
    private int calculateComplexityScore(MigrationScript script) {
        int score = 0;
        score += script.createStatements * 2;
        score += script.alterStatements * 3;
        score += script.dropStatements * 4;
        score += script.insertStatements;
        score += script.updateStatements * 2;
        score += script.deleteStatements * 3;
        return score;
    }
    
    private int getLineNumber(String content, int position) {
        return content.substring(0, position).split("\n").length;
    }
    
    private String estimateOperationDuration(String operation) {
        if (operation.contains("CREATE INDEX")) return "MEDIUM";
        if (operation.contains("DROP INDEX")) return "SHORT";
        if (operation.contains("REINDEX")) return "LONG";
        return "SHORT";
    }
    
    private int compareVersions(String v1, String v2) {
        if (v1 == null) v1 = "0";
        if (v2 == null) v2 = "0";
        
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        
        int maxLength = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < maxLength; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            
            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        return 0;
    }
    
    private boolean hasVersionGap(String v1, String v2) {
        // Implementazione semplificata
        try {
            double version1 = Double.parseDouble(v1);
            double version2 = Double.parseDouble(v2);
            return (version2 - version1) > 1.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private Map<String, List<MigrationScript>> groupByVersion() {
        Map<String, List<MigrationScript>> groups = new HashMap<>();
        for (MigrationScript script : migrationScripts.values()) {
            groups.computeIfAbsent(script.version, k -> new ArrayList<>()).add(script);
        }
        return groups;
    }
    
    /**
     * Genera report finale
     */
    private MigrationAnalysisReport generateReport() {
        MigrationAnalysisReport report = new MigrationAnalysisReport();
        
        report.detectedTool = detectedTool;
        report.totalScripts = migrationScripts.size();
        report.migrationScripts = new HashMap<>(migrationScripts);
        report.migrationConfigurations = new HashMap<>(migrationConfigurations);
        report.migrationIssues = new ArrayList<>(migrationIssues);
        report.conflicts = new ArrayList<>(conflicts);
        
        // Metriche aggregate
        report.totalComplexity = calculateTotalComplexity();
        report.averageComplexity = calculateAverageComplexity();
        report.rollbackableScripts = countRollbackableScripts();
        report.securityRiskCount = countSecurityRisks();
        report.performanceImpactCount = countPerformanceImpacts();
        
        return report;
    }
    
    private int calculateTotalComplexity() {
        return migrationScripts.values().stream()
            .mapToInt(s -> s.complexityScore)
            .sum();
    }
    
    private double calculateAverageComplexity() {
        return migrationScripts.isEmpty() ? 0.0 :
            calculateTotalComplexity() / (double) migrationScripts.size();
    }
    
    private int countRollbackableScripts() {
        return (int) migrationScripts.values().stream()
            .filter(s -> s.isRollbackable)
            .count();
    }
    
    private int countSecurityRisks() {
        return migrationScripts.values().stream()
            .mapToInt(s -> s.securityRisks.size())
            .sum();
    }
    
    private int countPerformanceImpacts() {
        return migrationScripts.values().stream()
            .mapToInt(s -> s.performanceImpacts.size())
            .sum();
    }
    
    // Classi di supporto
    public static class MigrationScript {
        public String filename;
        public String version;
        public String description;
        public String content;
        public MigrationTool tool;
        public ScriptFormat format;
        public int lineCount;
        public int complexityScore;
        
        // Statement counts
        public int createStatements;
        public int alterStatements;
        public int dropStatements;
        public int insertStatements;
        public int updateStatements;
        public int deleteStatements;
        
        // Rollback info
        public boolean isRollbackable;
        public boolean hasTransactionControl;
        public boolean hasRollbackInfo;
        public RollbackRisk rollbackRisk;
        
        // Liquibase specific
        public boolean hasChangeSets;
        public int changeSetCount;
        
        public List<SecurityRisk> securityRisks;
        public List<PerformanceImpact> performanceImpacts;
        
        public MigrationScript(String filename) {
            this.filename = filename;
            this.securityRisks = new ArrayList<>();
            this.performanceImpacts = new ArrayList<>();
            this.format = ScriptFormat.SQL;
        }
    }
    
    public static class MigrationConfig {
        public String configurationClass;
        public MigrationTool tool;
        public List<FlywayConfigOption> configOptions;
        public Map<String, ConfigProperty> properties;
        
        public MigrationConfig(String configurationClass, MigrationTool tool) {
            this.configurationClass = configurationClass;
            this.tool = tool;
            this.configOptions = new ArrayList<>();
            this.properties = new HashMap<>();
        }
    }
    
    public static class FlywayConfigOption {
        public String methodName;
        public int lineNumber;
        public FlywayConfigType configType;
    }
    
    public static class ConfigProperty {
        public String name;
        public String value;
        public String source;
    }
    
    public static class SecurityRisk {
        public SecurityRiskType riskType;
        public String description;
        public int lineNumber;
    }
    
    public static class PerformanceImpact {
        public PerformanceImpactType impactType;
        public String description;
        public String estimatedDuration;
    }
    
    public static class ConflictAnalysis {
        public String version;
        public List<MigrationScript> conflictingScripts;
        public ConflictType conflictType;
        public ConflictSeverity severity;
        public String description;
    }
    
    public static class MigrationIssue {
        public IssueLevel level;
        public String description;
        public String source;
        
        public MigrationIssue(IssueLevel level, String description, String source) {
            this.level = level;
            this.description = description;
            this.source = source;
        }
    }
    
    public enum MigrationTool {
        FLYWAY, LIQUIBASE, CUSTOM, NONE
    }
    
    public enum ScriptFormat {
        SQL, XML, YAML, JSON
    }
    
    public enum FlywayConfigType {
        LOCATION, BASELINE, VALIDATION, CLEAN, TARGET, GENERAL
    }
    
    public enum SecurityRiskType {
        DANGEROUS_OPERATION, MISSING_WHERE_CLAUSE, PRIVILEGE_ESCALATION
    }
    
    public enum PerformanceImpactType {
        INDEX_OPERATION, BULK_OPERATION, TABLE_LOCK, FULL_SCAN
    }
    
    public enum ConflictType {
        DUPLICATE_VERSION, TABLE_RECREATE, SCHEMA_MISMATCH
    }
    
    public enum ConflictSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public enum RollbackRisk {
        LOW, MEDIUM, HIGH
    }
    
    public enum IssueLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public static class MigrationAnalysisReport {
        public MigrationTool detectedTool;
        public int totalScripts;
        public Map<String, MigrationScript> migrationScripts;
        public Map<String, MigrationConfig> migrationConfigurations;
        public List<MigrationIssue> migrationIssues;
        public List<ConflictAnalysis> conflicts;
        public int totalComplexity;
        public double averageComplexity;
        public int rollbackableScripts;
        public int securityRiskCount;
        public int performanceImpactCount;
    }
}
```

## Esempio di Output HTML

```html
<!DOCTYPE html>
<html>
<head>
    <title>Database Migrations Analysis Report</title>
    <style>
        .migration-container { margin: 20px 0; padding: 15px; border-left: 4px solid #6c757d; }
        .flyway-migration { border-left-color: #007bff; }
        .liquibase-migration { border-left-color: #28a745; }
        .security-critical { background-color: #ffe6e6; }
        .performance-warning { background-color: #fff3cd; }
        .rollback-info { background-color: #e2e3e5; }
        .version-timeline { display: flex; align-items: center; margin: 10px 0; }
    </style>
</head>
<body>
    <h1>🔄 Report: Mappa delle Migrazioni DB</h1>
    
    <div class="summary">
        <h2>📊 Riepilogo Generale</h2>
        <ul>
            <li><strong>Tool Rilevato:</strong> Flyway</li>
            <li><strong>Totale Script:</strong> 15</li>
            <li><strong>Complessità Media:</strong> 12.3</li>
            <li><strong>Script Rollback-able:</strong> 8/15</li>
            <li><strong>Rischi Sicurezza:</strong> 3</li>
            <li><strong>Impatti Performance:</strong> 5</li>
        </ul>
    </div>
    
    <div class="migrations-timeline">
        <h2>📅 Timeline Migrazioni</h2>
        
        <div class="migration-container flyway-migration">
            <h3>🟢 V1.0__create_users_table.sql</h3>
            <div class="version-timeline">
                <span class="badge badge-primary">v1.0</span>
                <span class="mx-2">→</span>
                <span class="badge badge-success">CREATE TABLE</span>
            </div>
            <div class="migration-details">
                <p><strong>Complessità:</strong> 8 | <strong>Linee:</strong> 12</p>
                <p><strong>Operazioni:</strong> 1 CREATE, 0 ALTER, 0 DROP</p>
                <p><strong>Rollback:</strong> 🟢 Possibile</p>
            </div>
        </div>
        
        <div class="migration-container flyway-migration security-critical">
            <h3>🔴 V2.1__cleanup_old_data.sql</h3>
            <div class="version-timeline">
                <span class="badge badge-primary">v2.1</span>
                <span class="mx-2">→</span>
                <span class="badge badge-danger">DELETE + DROP</span>
            </div>
            <div class="migration-details">
                <p><strong>Complessità:</strong> 25 | <strong>Linee:</strong> 45</p>
                <p><strong>Operazioni:</strong> 0 CREATE, 2 ALTER, 1 DROP</p>
                <p><strong>Rollback:</strong> 🔴 Impossibile</p>
            </div>
            <div class="security-issues">
                <h4>⚠️ Rischi Sicurezza</h4>
                <ul>
                    <li>🔴 <strong>DROP TABLE senza backup</strong></li>
                    <li>🟠 <strong>DELETE senza WHERE clause</strong></li>
                </ul>
            </div>
        </div>
        
        <div class="migration-container flyway-migration performance-warning">
            <h3>🟡 V3.0__add_indexes.sql</h3>
            <div class="version-timeline">
                <span class="badge badge-primary">v3.0</span>
                <span class="mx-2">→</span>
                <span class="badge badge-warning">INDEX OPS</span>
            </div>
            <div class="performance-impacts">
                <h4>⚡ Impatti Performance</h4>
                <ul>
                    <li>🟡 <strong>CREATE INDEX</strong>: Durata stimata MEDIUM</li>
                    <li>🟡 <strong>REINDEX</strong>: Durata stimata LONG</li>
                </ul>
            </div>
        </div>
    </div>
    
    <div class="conflicts-analysis">
        <h2>⚠️ Conflitti Rilevati</h2>
        <div class="conflict-item">
            <h3>🔴 Versione Duplicata: v2.5</h3>
            <p><strong>Script Conflittuali:</strong></p>
            <ul>
                <li>V2.5__add_email_column.sql</li>
                <li>V2.5__update_user_schema.sql</li>
            </ul>
        </div>
    </div>
    
    <div class="rollback-analysis">
        <h2>🔄 Analisi Rollback</h2>
        <div class="rollback-info">
            <h3>Capacità di Rollback per Version</h3>
            <ul>
                <li>🟢 <strong>v1.0 - v1.5:</strong> Rollback completo supportato</li>
                <li>🟡 <strong>v2.0 - v2.0:</strong> Rollback parziale (DDL irreversibili)</li>
                <li>🔴 <strong>v2.1+:</strong> Rollback ad alto rischio</li>
            </ul>
        </div>
    </div>
</body>
</html>
```

## Metriche di Qualità del Codice

### Algoritmo di Scoring (0-100)

```java
public class MigrationQualityScorer {
    
    public int calculateQualityScore(MigrationAnalysisReport report) {
        int baseScore = 100;
        
        // Penalità per issues critici
        for (MigrationIssue issue : report.migrationIssues) {
            switch (issue.level) {
                case CRITICAL: baseScore -= 20; break;
                case HIGH: baseScore -= 12; break;
                case MEDIUM: baseScore -= 6; break;
                case LOW: baseScore -= 2; break;
            }
        }
        
        // Penalità per conflitti
        for (ConflictAnalysis conflict : report.conflicts) {
            switch (conflict.severity) {
                case CRITICAL: baseScore -= 25; break;
                case HIGH: baseScore -= 15; break;
                case MEDIUM: baseScore -= 8; break;
                case LOW: baseScore -= 3; break;
            }
        }
        
        // Penalità per rischi sicurezza
        baseScore -= report.securityRiskCount * 5;
        
        // Penalità per alta complessità
        if (report.averageComplexity > 20) baseScore -= 15;
        else if (report.averageComplexity > 15) baseScore -= 8;
        
        // Penalità per bassa rollback capability
        int rollbackRatio = (report.rollbackableScripts * 100) / report.totalScripts;
        if (rollbackRatio < 50) baseScore -= 20;
        else if (rollbackRatio < 70) baseScore -= 10;
        
        // Bonus per best practices
        if (report.detectedTool != MigrationTool.NONE) baseScore += 10;
        if (rollbackRatio > 80) baseScore += 5;
        if (report.securityRiskCount == 0) baseScore += 8;
        
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
| **Issues Critici** | 0 | 0 | 1 | >1 |
| **Conflitti** | 0 | 0-1 | 2-3 | >3 |
| **Rollback Capability (%)** | >80% | 70-80% | 50-69% | <50% |
| **Complessità Media** | <10 | 10-15 | 15-20 | >20 |

### Segnalazioni per Gravità

#### 🔴 CRITICA
- **Versioni Duplicate**: Script con stessa versione causano conflitti deployment
- **DROP senza Rollback**: Perdita dati irreversibile
- **Clean Abilitato**: Flyway clean abilitato in production
- **DELETE senza WHERE**: Operazioni di massa non controllate

#### 🟠 ALTA
- **Operazioni DDL Pericolose**: ALTER/DROP senza backup strategy
- **Gap Versioni**: Salti nella sequenza numerica versioni
- **Missing Transaction Control**: Script senza gestione transazionale
- **Performance Impact Alto**: Operazioni che bloccano database

#### 🟡 MEDIA
- **Naming Convention Violata**: File non conformi a standard tool
- **Validazione Disabilitata**: Controlli qualità disattivati
- **Complessità Alta**: Script troppo complessi da manutenere
- **Missing Rollback Info**: Mancanza informazioni per rollback

#### 🔵 BASSA
- **Documentation Mancante**: Script senza commenti esplicativi
- **Baseline Mancante**: Missing baseline per nuovi environment
- **Location Non Standard**: Path non convenzionali per script
- **Checksum Warnings**: Modifiche non tracciate a script esistenti

### Valore di Business

- **Zero-Downtime Deployments**: Migrazioni sicure senza interruzioni servizio
- **Data Integrity**: Protezione dati durante evoluzione schema
- **Rollback Strategy**: Capacità recupero rapido in caso problemi
- **Team Collaboration**: Gestione conflitti in sviluppo parallelo
- **Compliance Auditing**: Tracciabilità completa modifiche schema database