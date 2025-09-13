# Report 28: Analisi dei Datasource Configurati

**Valore**: ⭐⭐⭐ **Complessità**: 🟢 Semplice **Tempo**: 2-3 giorni
**Tags**: `#datasource-configuration` `#database-connections`

## Descrizione

Questo report analizza la configurazione dei datasource nell'applicazione Spring Boot, identificando configurazioni multiple, pool di connessioni, parametri di ottimizzazione e potenziali problemi di sicurezza o performance nelle configurazioni database.

## Obiettivi dell'Analisi

1. **Identificazione Datasource**: Rilevare tutti i datasource configurati nell'applicazione
2. **Analisi Configurazioni**: Valutare parametri di connection pooling e ottimizzazione
3. **Security Assessment**: Identificare configurazioni di sicurezza e credenziali
4. **Performance Tuning**: Analizzare impostazioni per performance database
5. **Multiple Datasource**: Gestione di configurazioni multi-database

## Implementazione con Javassist

```java
package com.jreverse.analyzer.datasource;

import javassist.*;
import javassist.bytecode.*;
import javassist.expr.*;
import java.util.*;
import java.util.regex.*;
import java.io.IOException;

/**
 * Analyzer per configurazioni datasource nell'applicazione
 */
public class DatasourceConfigurationAnalyzer {
    
    private ClassPool classPool;
    private Map<String, DatasourceConfig> datasources;
    private Map<String, ConnectionPoolConfig> poolConfigurations;
    private List<SecurityIssue> securityIssues;
    private List<PerformanceIssue> performanceIssues;
    private Map<String, ConfigurationProperty> configProperties;
    
    // Pattern per identificare configurazioni datasource
    private static final Pattern DATASOURCE_PATTERN = Pattern.compile(
        "(?i)(spring\\.datasource|jdbc|database|connection)"
    );
    
    private static final Pattern DB_URL_PATTERN = Pattern.compile(
        "jdbc:([^:]+)://([^:/]+)(?::(\\d+))?/([^?]+)"
    );
    
    // Proprietà critiche per sicurezza
    private static final Set<String> SECURITY_SENSITIVE_PROPS = Set.of(
        "password", "username", "user", "secret", "key", "token", "credential"
    );
    
    public DatasourceConfigurationAnalyzer() {
        this.classPool = ClassPool.getDefault();
        this.datasources = new HashMap<>();
        this.poolConfigurations = new HashMap<>();
        this.securityIssues = new ArrayList<>();
        this.performanceIssues = new ArrayList<>();
        this.configProperties = new HashMap<>();
    }
    
    /**
     * Analizza tutte le configurazioni datasource
     */
    public DatasourceAnalysisReport analyzeDatasourceConfigurations(List<String> classNames) throws Exception {
        // Fase 1: Analizza classi @Configuration
        for (String className : classNames) {
            try {
                CtClass ctClass = classPool.get(className);
                if (isConfigurationClass(ctClass)) {
                    analyzeDatasourceConfiguration(ctClass);
                }
            } catch (Exception e) {
                System.err.println("Errore nell'analisi della classe " + className + ": " + e.getMessage());
            }
        }
        
        // Fase 2: Analizza properties file (simulato)
        analyzeApplicationProperties();
        
        // Fase 3: Analizza configurazioni programmatiche
        analyzeDataSourceBeans(classNames);
        
        // Fase 4: Valuta configurazioni trovate
        validateConfigurations();
        
        return generateReport();
    }
    
    /**
     * Verifica se una classe è una configuration class
     */
    private boolean isConfigurationClass(CtClass ctClass) throws Exception {
        AnnotationsAttribute attr = (AnnotationsAttribute)
            ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        
        if (attr != null) {
            return attr.getAnnotation("org.springframework.context.annotation.Configuration") != null ||
                   attr.getAnnotation("org.springframework.boot.autoconfigure.SpringBootApplication") != null;
        }
        
        return false;
    }
    
    /**
     * Analizza configurazioni datasource in una classe
     */
    private void analyzeDatasourceConfiguration(CtClass configClass) throws Exception {
        String className = configClass.getName();
        
        // Analizza metodi @Bean che returnano DataSource
        for (CtMethod method : configClass.getDeclaredMethods()) {
            if (isDataSourceBeanMethod(method)) {
                analyzeDatasourceBeanMethod(className, method);
            }
        }
        
        // Analizza field injection di DataSource
        for (CtField field : configClass.getDeclaredFields()) {
            if (isDatasourceField(field)) {
                analyzeDatasourceField(className, field);
            }
        }
        
        // Analizza @ConfigurationProperties
        analyzeConfigurationProperties(configClass);
    }
    
    /**
     * Verifica se un metodo è un bean DataSource
     */
    private boolean isDataSourceBeanMethod(CtMethod method) throws Exception {
        // Verifica annotation @Bean
        AnnotationsAttribute attr = (AnnotationsAttribute)
            method.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
        
        if (attr != null && attr.getAnnotation("org.springframework.context.annotation.Bean") != null) {
            // Verifica return type DataSource
            CtClass returnType = method.getReturnType();
            return isDataSourceType(returnType.getName());
        }
        
        return false;
    }
    
    /**
     * Verifica se il tipo è un DataSource
     */
    private boolean isDataSourceType(String typeName) {
        return typeName.contains("DataSource") || 
               typeName.contains("ConnectionPool") ||
               typeName.equals("javax.sql.DataSource");
    }
    
    /**
     * Analizza metodo bean datasource
     */
    private void analyzeDatasourceBeanMethod(String className, CtMethod method) throws Exception {
        String methodName = method.getName();
        String datasourceId = className + "." + methodName;
        
        DatasourceConfig config = new DatasourceConfig(datasourceId);
        config.beanMethod = methodName;
        config.configurationClass = className;
        
        // Analizza annotations specifiche
        analyzeDataSourceAnnotations(method, config);
        
        // Analizza corpo del metodo per configurazioni
        analyzeDatasourceMethodBody(method, config);
        
        // Verifica qualificatori (@Primary, @Qualifier)
        analyzeDataSourceQualifiers(method, config);
        
        datasources.put(datasourceId, config);
    }
    
    /**
     * Analizza annotations su metodo DataSource
     */
    private void analyzeDataSourceAnnotations(CtMethod method, DatasourceConfig config) throws Exception {
        AnnotationsAttribute attr = (AnnotationsAttribute)
            method.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
        
        if (attr != null) {
            // @Primary
            if (attr.getAnnotation("org.springframework.context.annotation.Primary") != null) {
                config.isPrimary = true;
            }
            
            // @Qualifier
            Annotation qualifier = attr.getAnnotation("org.springframework.beans.factory.annotation.Qualifier");
            if (qualifier != null) {
                config.qualifier = getAnnotationValue(qualifier, "value");
            }
            
            // @ConfigurationProperties
            Annotation configProps = attr.getAnnotation("org.springframework.boot.context.properties.ConfigurationProperties");
            if (configProps != null) {
                config.configurationPrefix = getAnnotationValue(configProps, "prefix");
            }
        }
    }
    
    /**
     * Analizza corpo metodo per configurazioni datasource
     */
    private void analyzeDatasourceMethodBody(CtMethod method, DatasourceConfig config) throws Exception {
        method.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                try {
                    String methodName = m.getMethodName();
                    String className = m.getClassName();
                    
                    // Intercetta chiamate di configurazione
                    if (isDatasourceConfigurationCall(className, methodName)) {
                        analyzeConfigurationCall(config, className, methodName, m.getLineNumber());
                    }
                    
                    // Intercetta creazione DataSource
                    if (isDatasourceCreation(className, methodName)) {
                        config.datasourceType = extractDatasourceType(className);
                        config.creationMethod = methodName;
                    }
                    
                } catch (Exception e) {
                    // Log error but continue
                }
            }
            
            @Override
            public void edit(FieldAccess f) throws CannotCompileException {
                try {
                    // Intercetta accessi a proprietà di configurazione
                    if (f.isReader() && isConfigurationProperty(f.getFieldName())) {
                        config.usedProperties.add(f.getFieldName());
                    }
                } catch (Exception e) {
                    // Log error but continue
                }
            }
        });
    }
    
    /**
     * Verifica se è una chiamata di configurazione datasource
     */
    private boolean isDatasourceConfigurationCall(String className, String methodName) {
        return (className.contains("DataSource") && 
                (methodName.startsWith("set") || methodName.equals("configure"))) ||
               (className.contains("Builder") && methodName.equals("build"));
    }
    
    /**
     * Analizza chiamata di configurazione
     */
    private void analyzeConfigurationCall(DatasourceConfig config, String className, 
                                         String methodName, int lineNumber) {
        ConfigurationCall call = new ConfigurationCall();
        call.className = className;
        call.methodName = methodName;
        call.lineNumber = lineNumber;
        call.configType = determineConfigurationType(methodName);
        
        config.configurationCalls.add(call);
        
        // Analisi specifica per tipo di configurazione
        if (methodName.contains("Url") || methodName.contains("url")) {
            config.hasUrlConfiguration = true;
        } else if (methodName.contains("Username") || methodName.contains("username")) {
            config.hasUsernameConfiguration = true;
        } else if (methodName.contains("Password") || methodName.contains("password")) {
            config.hasPasswordConfiguration = true;
            // Security issue: hardcoded password
            securityIssues.add(new SecurityIssue(
                SecurityLevel.HIGH,
                "Password potentially hardcoded in datasource configuration",
                config.datasourceId,
                lineNumber
            ));
        }
    }
    
    /**
     * Determina tipo di configurazione
     */
    private ConfigurationType determineConfigurationType(String methodName) {
        String lower = methodName.toLowerCase();
        if (lower.contains("pool")) return ConfigurationType.POOL;
        if (lower.contains("connection")) return ConfigurationType.CONNECTION;
        if (lower.contains("security") || lower.contains("ssl")) return ConfigurationType.SECURITY;
        if (lower.contains("timeout")) return ConfigurationType.TIMEOUT;
        return ConfigurationType.GENERAL;
    }
    
    /**
     * Estrae tipo di datasource dalla classe
     */
    private String extractDatasourceType(String className) {
        if (className.contains("HikariDataSource")) return "HikariCP";
        if (className.contains("TomcatDataSource")) return "Tomcat JDBC";
        if (className.contains("BasicDataSource")) return "Apache DBCP";
        if (className.contains("C3P0")) return "C3P0";
        return "Generic";
    }
    
    /**
     * Analizza field datasource
     */
    private void analyzeDatasourceField(String className, CtField field) throws Exception {
        String fieldName = field.getName();
        String fieldType = field.getType().getName();
        
        // Verifica annotations
        AnnotationsAttribute attr = (AnnotationsAttribute)
            field.getFieldInfo().getAttribute(AnnotationsAttribute.visibleTag);
        
        if (attr != null) {
            // @Autowired DataSource
            if (attr.getAnnotation("org.springframework.beans.factory.annotation.Autowired") != null) {
                DatasourceConfig config = new DatasourceConfig(className + "." + fieldName);
                config.isInjected = true;
                config.fieldName = fieldName;
                config.datasourceType = fieldType;
                
                // Verifica @Qualifier
                Annotation qualifier = attr.getAnnotation("org.springframework.beans.factory.annotation.Qualifier");
                if (qualifier != null) {
                    config.qualifier = getAnnotationValue(qualifier, "value");
                }
                
                datasources.put(config.datasourceId, config);
            }
        }
    }
    
    /**
     * Verifica se è un field datasource
     */
    private boolean isDatasourceField(CtField field) throws Exception {
        return isDataSourceType(field.getType().getName());
    }
    
    /**
     * Analizza @ConfigurationProperties
     */
    private void analyzeConfigurationProperties(CtClass configClass) throws Exception {
        AnnotationsAttribute attr = (AnnotationsAttribute)
            configClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        
        if (attr != null) {
            Annotation configProps = attr.getAnnotation("org.springframework.boot.context.properties.ConfigurationProperties");
            if (configProps != null) {
                String prefix = getAnnotationValue(configProps, "prefix");
                if (prefix != null && DATASOURCE_PATTERN.matcher(prefix).find()) {
                    analyzeConfigurationPropertiesClass(configClass, prefix);
                }
            }
        }
    }
    
    /**
     * Analizza classe @ConfigurationProperties
     */
    private void analyzeConfigurationPropertiesClass(CtClass configClass, String prefix) throws Exception {
        String className = configClass.getName();
        
        for (CtField field : configClass.getDeclaredFields()) {
            String propertyName = prefix + "." + field.getName();
            String propertyType = field.getType().getName();
            
            ConfigurationProperty property = new ConfigurationProperty();
            property.name = propertyName;
            property.type = propertyType;
            property.configurationClass = className;
            
            // Verifica se è security sensitive
            if (SECURITY_SENSITIVE_PROPS.stream().anyMatch(prop -> 
                field.getName().toLowerCase().contains(prop))) {
                property.isSecuritySensitive = true;
            }
            
            configProperties.put(propertyName, property);
        }
    }
    
    /**
     * Analizza properties dell'applicazione (simulato)
     */
    private void analyzeApplicationProperties() {
        // Simula lettura application.properties/yml
        Map<String, String> properties = loadApplicationProperties();
        
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            if (DATASOURCE_PATTERN.matcher(key).find()) {
                analyzePropertyConfiguration(key, value);
            }
        }
    }
    
    /**
     * Carica properties dell'applicazione (simulato)
     */
    private Map<String, String> loadApplicationProperties() {
        // In un'implementazione reale, questo leggerebbe da application.properties
        Map<String, String> props = new HashMap<>();
        // Esempi di proprietà tipiche
        props.put("spring.datasource.url", "jdbc:postgresql://localhost:5432/mydb");
        props.put("spring.datasource.username", "user");
        props.put("spring.datasource.password", "password");
        props.put("spring.datasource.hikari.maximum-pool-size", "20");
        return props;
    }
    
    /**
     * Analizza configurazione property
     */
    private void analyzePropertyConfiguration(String key, String value) {
        ConfigurationProperty property = new ConfigurationProperty();
        property.name = key;
        property.value = value;
        property.source = "application.properties";
        
        // Analisi specifica per tipo di proprietà
        if (key.contains("url")) {
            analyzeUrlProperty(property, value);
        } else if (key.contains("username")) {
            property.isSecuritySensitive = true;
        } else if (key.contains("password")) {
            property.isSecuritySensitive = true;
            // Security check per password in plaintext
            if (!value.startsWith("${") && !value.equals("")) {
                securityIssues.add(new SecurityIssue(
                    SecurityLevel.CRITICAL,
                    "Plaintext password in configuration: " + key,
                    key,
                    0
                ));
            }
        } else if (key.contains("pool") || key.contains("connection")) {
            analyzePoolProperty(property, value);
        }
        
        configProperties.put(key, property);
    }
    
    /**
     * Analizza proprietà URL
     */
    private void analyzeUrlProperty(ConfigurationProperty property, String url) {
        Matcher matcher = DB_URL_PATTERN.matcher(url);
        if (matcher.find()) {
            property.databaseType = matcher.group(1);
            property.host = matcher.group(2);
            property.port = matcher.group(3);
            property.databaseName = matcher.group(4);
            
            // Security check per host localhost in production
            if ("localhost".equals(property.host) || "127.0.0.1".equals(property.host)) {
                securityIssues.add(new SecurityIssue(
                    SecurityLevel.MEDIUM,
                    "Localhost database configuration may not be suitable for production",
                    property.name,
                    0
                ));
            }
        }
    }
    
    /**
     * Analizza proprietà pool
     */
    private void analyzePoolProperty(ConfigurationProperty property, String value) {
        try {
            int numericValue = Integer.parseInt(value);
            
            if (property.name.contains("maximum-pool-size") || property.name.contains("max-pool-size")) {
                if (numericValue > 50) {
                    performanceIssues.add(new PerformanceIssue(
                        PerformanceLevel.MEDIUM,
                        "Very large connection pool size: " + numericValue,
                        property.name
                    ));
                } else if (numericValue < 5) {
                    performanceIssues.add(new PerformanceIssue(
                        PerformanceLevel.LOW,
                        "Very small connection pool size: " + numericValue,
                        property.name
                    ));
                }
            }
            
            if (property.name.contains("timeout") && numericValue < 1000) {
                performanceIssues.add(new PerformanceIssue(
                    PerformanceLevel.MEDIUM,
                    "Very short timeout configured: " + numericValue + "ms",
                    property.name
                ));
            }
            
        } catch (NumberFormatException e) {
            // Non è un valore numerico
        }
    }
    
    /**
     * Analizza bean DataSource
     */
    private void analyzeDataSourceBeans(List<String> classNames) throws Exception {
        for (String className : classNames) {
            try {
                CtClass ctClass = classPool.get(className);
                
                // Cerca costruttori che creano DataSource
                for (CtConstructor constructor : ctClass.getConstructors()) {
                    analyzeDataSourceConstructor(className, constructor);
                }
                
            } catch (Exception e) {
                System.err.println("Errore nell'analisi bean DataSource " + className + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Analizza costruttore DataSource
     */
    private void analyzeDataSourceConstructor(String className, CtConstructor constructor) throws Exception {
        constructor.instrument(new ExprEditor() {
            @Override
            public void edit(NewExpr e) throws CannotCompileException {
                try {
                    String constructorClass = e.getClassName();
                    if (isDataSourceType(constructorClass)) {
                        // Trovato costruttore DataSource
                        DatasourceConfig config = datasources.computeIfAbsent(
                            className + ".constructor", 
                            k -> new DatasourceConfig(k)
                        );
                        config.datasourceType = extractDatasourceType(constructorClass);
                        config.isConstructorCreated = true;
                    }
                } catch (Exception ex) {
                    // Log but continue
                }
            }
        });
    }
    
    /**
     * Valida tutte le configurazioni trovate
     */
    private void validateConfigurations() {
        for (DatasourceConfig config : datasources.values()) {
            validateDatasourceConfiguration(config);
        }
        
        // Verifica configurazioni duplicate
        checkForDuplicateConfigurations();
        
        // Verifica missing configurations
        checkForMissingConfigurations();
    }
    
    /**
     * Valida singola configurazione datasource
     */
    private void validateDatasourceConfiguration(DatasourceConfig config) {
        // Verifica configurazione completa
        if (!config.hasUrlConfiguration && config.configurationPrefix == null) {
            securityIssues.add(new SecurityIssue(
                SecurityLevel.HIGH,
                "DataSource senza URL configuration: " + config.datasourceId,
                config.datasourceId,
                0
            ));
        }
        
        // Verifica sicurezza
        if (config.hasPasswordConfiguration && !config.hasUsernameConfiguration) {
            securityIssues.add(new SecurityIssue(
                SecurityLevel.MEDIUM,
                "Password configuration senza username: " + config.datasourceId,
                config.datasourceId,
                0
            ));
        }
        
        // Verifica performance
        if (config.configurationCalls.size() > 15) {
            performanceIssues.add(new PerformanceIssue(
                PerformanceLevel.LOW,
                "Molte chiamate di configurazione (" + config.configurationCalls.size() + ")",
                config.datasourceId
            ));
        }
    }
    
    /**
     * Verifica configurazioni duplicate
     */
    private void checkForDuplicateConfigurations() {
        Map<String, List<DatasourceConfig>> grouped = new HashMap<>();
        
        for (DatasourceConfig config : datasources.values()) {
            String key = config.qualifier != null ? config.qualifier : "default";
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(config);
        }
        
        for (Map.Entry<String, List<DatasourceConfig>> entry : grouped.entrySet()) {
            if (entry.getValue().size() > 1 && !"default".equals(entry.getKey())) {
                securityIssues.add(new SecurityIssue(
                    SecurityLevel.MEDIUM,
                    "Multiple DataSource con stesso qualifier: " + entry.getKey(),
                    entry.getKey(),
                    0
                ));
            }
        }
    }
    
    /**
     * Verifica configurazioni mancanti
     */
    private void checkForMissingConfigurations() {
        boolean hasPrimary = datasources.values().stream().anyMatch(c -> c.isPrimary);
        
        if (datasources.size() > 1 && !hasPrimary) {
            securityIssues.add(new SecurityIssue(
                SecurityLevel.MEDIUM,
                "Multiple DataSource senza @Primary configuration",
                "global",
                0
            ));
        }
    }
    
    // Metodi utility
    private String getAnnotationValue(Annotation annotation, String memberName) {
        try {
            return annotation.getMemberValue(memberName).toString().replaceAll("\"", "");
        } catch (Exception e) {
            return null;
        }
    }
    
    private boolean isConfigurationProperty(String fieldName) {
        return DATASOURCE_PATTERN.matcher(fieldName).find();
    }
    
    private void analyzeDataSourceQualifiers(CtMethod method, DatasourceConfig config) {
        // Implementazione analisi qualificatori
    }
    
    /**
     * Genera report finale
     */
    private DatasourceAnalysisReport generateReport() {
        DatasourceAnalysisReport report = new DatasourceAnalysisReport();
        
        report.totalDatasources = datasources.size();
        report.datasources = new HashMap<>(datasources);
        report.configurationProperties = new HashMap<>(configProperties);
        report.securityIssues = new ArrayList<>(securityIssues);
        report.performanceIssues = new ArrayList<>(performanceIssues);
        
        // Metriche aggregate
        report.securityScore = calculateSecurityScore();
        report.configurationComplexity = calculateConfigurationComplexity();
        report.datasourceTypes = getDatasourceTypeDistribution();
        report.primaryDatasources = countPrimaryDatasources();
        
        return report;
    }
    
    private int calculateSecurityScore() {
        int baseScore = 100;
        
        baseScore -= securityIssues.stream()
            .mapToInt(issue -> issue.level == SecurityLevel.CRITICAL ? 25 : 
                              issue.level == SecurityLevel.HIGH ? 15 : 
                              issue.level == SecurityLevel.MEDIUM ? 8 : 3)
            .sum();
        
        return Math.max(0, baseScore);
    }
    
    private int calculateConfigurationComplexity() {
        return datasources.values().stream()
            .mapToInt(config -> config.configurationCalls.size())
            .sum();
    }
    
    private Map<String, Integer> getDatasourceTypeDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        for (DatasourceConfig config : datasources.values()) {
            distribution.merge(config.datasourceType, 1, Integer::sum);
        }
        return distribution;
    }
    
    private int countPrimaryDatasources() {
        return (int) datasources.values().stream().filter(c -> c.isPrimary).count();
    }
    
    // Classi di supporto
    public static class DatasourceConfig {
        public String datasourceId;
        public String beanMethod;
        public String configurationClass;
        public String fieldName;
        public String datasourceType;
        public String qualifier;
        public String configurationPrefix;
        public boolean isPrimary;
        public boolean isInjected;
        public boolean isConstructorCreated;
        public boolean hasUrlConfiguration;
        public boolean hasUsernameConfiguration;
        public boolean hasPasswordConfiguration;
        public String creationMethod;
        public List<ConfigurationCall> configurationCalls;
        public Set<String> usedProperties;
        
        public DatasourceConfig(String datasourceId) {
            this.datasourceId = datasourceId;
            this.configurationCalls = new ArrayList<>();
            this.usedProperties = new HashSet<>();
        }
    }
    
    public static class ConfigurationCall {
        public String className;
        public String methodName;
        public int lineNumber;
        public ConfigurationType configType;
    }
    
    public static class ConfigurationProperty {
        public String name;
        public String value;
        public String type;
        public String source;
        public String configurationClass;
        public boolean isSecuritySensitive;
        public String databaseType;
        public String host;
        public String port;
        public String databaseName;
    }
    
    public static class SecurityIssue {
        public SecurityLevel level;
        public String description;
        public String datasourceId;
        public int lineNumber;
        
        public SecurityIssue(SecurityLevel level, String description, String datasourceId, int lineNumber) {
            this.level = level;
            this.description = description;
            this.datasourceId = datasourceId;
            this.lineNumber = lineNumber;
        }
    }
    
    public static class PerformanceIssue {
        public PerformanceLevel level;
        public String description;
        public String datasourceId;
        
        public PerformanceIssue(PerformanceLevel level, String description, String datasourceId) {
            this.level = level;
            this.description = description;
            this.datasourceId = datasourceId;
        }
    }
    
    public enum ConfigurationType {
        POOL, CONNECTION, SECURITY, TIMEOUT, GENERAL
    }
    
    public enum SecurityLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public enum PerformanceLevel {
        LOW, MEDIUM, HIGH
    }
    
    public static class DatasourceAnalysisReport {
        public int totalDatasources;
        public Map<String, DatasourceConfig> datasources;
        public Map<String, ConfigurationProperty> configurationProperties;
        public List<SecurityIssue> securityIssues;
        public List<PerformanceIssue> performanceIssues;
        public int securityScore;
        public int configurationComplexity;
        public Map<String, Integer> datasourceTypes;
        public int primaryDatasources;
    }
}
```

## Esempio di Output HTML

```html
<!DOCTYPE html>
<html>
<head>
    <title>Datasource Configuration Analysis Report</title>
    <style>
        .datasource-container { margin: 20px 0; padding: 15px; border-left: 4px solid #007bff; }
        .primary-datasource { border-left-color: #28a745; }
        .secondary-datasource { border-left-color: #ffc107; }
        .security-critical { background-color: #ffe6e6; }
        .security-good { background-color: #d4edda; }
        .config-property { margin: 10px 0; padding: 8px; background: #f8f9fa; }
        .sensitive-property { background-color: #fff3cd; }
    </style>
</head>
<body>
    <h1>🗃️ Report: Analisi dei Datasource Configurati</h1>
    
    <div class="summary">
        <h2>📊 Riepilogo Generale</h2>
        <ul>
            <li><strong>Totale Datasource:</strong> 3</li>
            <li><strong>Datasource Primari:</strong> 1</li>
            <li><strong>Score Sicurezza:</strong> 75/100</li>
            <li><strong>Problemi Sicurezza:</strong> 4</li>
            <li><strong>Issues Performance:</strong> 2</li>
        </ul>
    </div>
    
    <div class="datasources-analysis">
        <h2>🏗️ Configurazioni Datasource</h2>
        
        <div class="datasource-container primary-datasource">
            <h3>🟢 Primary DataSource (HikariCP)</h3>
            <div class="datasource-info">
                <p><strong>Bean Method:</strong> primaryDataSource()</p>
                <p><strong>Configuration Class:</strong> DatabaseConfig</p>
                <p><strong>Type:</strong> HikariCP</p>
                <p><strong>Qualifier:</strong> @Primary</p>
            </div>
            
            <div class="config-properties">
                <h4>⚙️ Proprietà Configurazione</h4>
                <div class="config-property">
                    <strong>spring.datasource.url:</strong> jdbc:postgresql://prod-db:5432/app_db
                </div>
                <div class="config-property sensitive-property">
                    <strong>spring.datasource.username:</strong> app_user ⚠️ Sensibile
                </div>
                <div class="config-property sensitive-property">
                    <strong>spring.datasource.password:</strong> ****** 🔐 Protetto
                </div>
                <div class="config-property">
                    <strong>spring.datasource.hikari.maximum-pool-size:</strong> 20
                </div>
            </div>
        </div>
        
        <div class="datasource-container secondary-datasource security-critical">
            <h3>🔴 Secondary DataSource (Tomcat JDBC)</h3>
            <div class="datasource-info">
                <p><strong>Bean Method:</strong> secondaryDataSource()</p>
                <p><strong>Qualifier:</strong> @Qualifier("secondary")</p>
                <p><strong>Type:</strong> Tomcat JDBC</p>
            </div>
            
            <div class="security-issues">
                <h4>🚨 Problemi Sicurezza</h4>
                <ul>
                    <li>🔴 <strong>Password in plaintext</strong>: Configurazione password non protetta</li>
                    <li>🟠 <strong>Localhost configuration</strong>: URL punta a localhost</li>
                </ul>
            </div>
        </div>
    </div>
    
    <div class="security-analysis">
        <h2>🔒 Analisi Sicurezza</h2>
        <div class="security-summary">
            <h3>Distribuzione Problemi</h3>
            <ul>
                <li>🔴 <strong>Critici:</strong> 1 issue</li>
                <li>🟠 <strong>Alti:</strong> 1 issue</li>
                <li>🟡 <strong>Medi:</strong> 2 issues</li>
            </ul>
        </div>
    </div>
    
    <div class="performance-analysis">
        <h2>⚡ Ottimizzazioni Performance</h2>
        <ul>
            <li>🟢 <strong>Pool Size Ottimale:</strong> Configurazione HikariCP appropriata</li>
            <li>🟡 <strong>Timeout Configurati:</strong> Verificare timeout connection</li>
            <li>🔴 <strong>Pool Eccessivo:</strong> Secondary datasource con pool size 50</li>
        </ul>
    </div>
</body>
</html>
```

## Metriche di Qualità del Codice

### Algoritmo di Scoring (0-100)

```java
public class DatasourceQualityScorer {
    
    public int calculateQualityScore(DatasourceAnalysisReport report) {
        int baseScore = 100;
        
        // Penalità per problemi di sicurezza
        for (SecurityIssue issue : report.securityIssues) {
            switch (issue.level) {
                case CRITICAL: baseScore -= 20; break;
                case HIGH: baseScore -= 12; break;
                case MEDIUM: baseScore -= 6; break;
                case LOW: baseScore -= 2; break;
            }
        }
        
        // Penalità per problemi di performance
        baseScore -= report.performanceIssues.size() * 5;
        
        // Penalità per complessità eccessiva
        if (report.configurationComplexity > 30) baseScore -= 15;
        else if (report.configurationComplexity > 20) baseScore -= 8;
        
        // Penalità per multiple datasource senza @Primary
        if (report.totalDatasources > 1 && report.primaryDatasources == 0) {
            baseScore -= 10;
        }
        
        // Bonus per best practices
        if (report.securityScore > 90) baseScore += 10;
        if (report.datasourceTypes.containsKey("HikariCP")) baseScore += 5; // Preferred pool
        if (report.primaryDatasources == 1) baseScore += 3;
        
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
| **Score Sicurezza** | >90 | 80-90 | 65-79 | <65 |
| **Problemi Critici** | 0 | 0 | 1 | >1 |
| **Complessità Config** | <15 | 15-25 | 26-35 | >35 |
| **Primary Datasource** | 1 | 1 | 0-1 | >1 |

### Segnalazioni per Gravità

#### 🔴 CRITICA
- **Password in Plaintext**: Credenziali database non protette
- **SQL Injection Risk**: Configurazioni URL vulnerabili
- **Missing SSL Configuration**: Connessioni non crittografate
- **Hardcoded Credentials**: Username/password nel codice sorgente

#### 🟠 ALTA  
- **Datasource senza URL**: Configurazioni incomplete
- **Localhost in Production**: Configurazioni di sviluppo in produzione
- **Missing Connection Validation**: Pool senza validazione connessioni
- **Excessive Pool Size**: Pool di connessioni sovradimensionati

#### 🟡 MEDIA
- **Multiple DataSource senza @Primary**: Ambiguità nelle configurazioni
- **Password senza Username**: Configurazioni incomplete
- **Timeout Molto Bassi**: Configurazioni timeout inadeguate  
- **Pool Size Ridotto**: Possibili colli di bottiglia

#### 🔵 BASSA
- **Missing JMX Monitoring**: Mancanza monitoring pool connessioni
- **Configurazioni Duplicate**: Proprietà ridondanti
- **Naming Convention**: Convenzioni nomi bean non standard
- **Documentation Missing**: Configurazioni complesse senza documentazione

### Valore di Business

- **Sicurezza Dati**: Prevenzione accessi non autorizzati database
- **Performance Applicazione**: Ottimizzazione pool connessioni per scalabilità
- **Reliability Sistema**: Configurazioni robuste per alta disponibilità  
- **Operational Excellence**: Monitoring e gestione connessioni database
- **Compliance**: Conformità standard sicurezza per dati sensibili