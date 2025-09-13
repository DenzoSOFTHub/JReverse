# Report 47: Esternalizzazione della Configurazione

**Valore**: ⭐⭐⭐⭐ **Complessità**: 🟡 Media **Tempo**: 4-5 giorni
**Tags**: `#configuration` `#externalization` `#properties` `#environment-specific`

## Descrizione

Analizza come l'applicazione gestisce la configurazione esternalizzata, identificando proprietà hardcoded, configurazioni environment-specific e best practices per la gestione della configurazione.

## Sezioni del Report

### 1. Configuration Sources
- File di proprietà (application.properties, application.yml)
- Variabili d'ambiente
- Argomenti della JVM
- Configuration servers esterni

### 2. Configuration Usage Analysis
- Proprietà utilizzate nel codice
- Valori hardcoded vs esternalizzati
- Profile-specific configurations
- Default values e fallback

### 3. Environment-Specific Settings
- Configurazioni per ambiente (dev, test, prod)
- Sensitive data management
- Configuration overrides
- Dynamic configuration loading

### 4. Best Practices Compliance
- Separation of concerns
- Security di configurazioni sensibili
- Configurazione immutable
- Validation delle configurazioni

## Implementazione con Javassist

```java
public class ConfigurationExternalizationAnalyzer {
    
    private static final Set<String> SENSITIVE_PROPERTY_PATTERNS = Set.of(
        "password", "pwd", "secret", "key", "token", "credential", 
        "auth", "api-key", "private", "cert", "keystore", "truststore"
    );
    
    private static final Set<String> CONFIGURATION_ANNOTATIONS = Set.of(
        "org.springframework.boot.context.properties.ConfigurationProperties",
        "org.springframework.context.annotation.Configuration",
        "org.springframework.beans.factory.annotation.Value",
        "org.springframework.boot.context.properties.EnableConfigurationProperties"
    );
    
    private static final Map<String, Integer> CONFIG_SEVERITY_PENALTIES = Map.of(
        "HARDCODED_SENSITIVE_DATA", -40,    // Dati sensibili hardcoded
        "HARDCODED_CONFIGURATION", -25,     // Configurazioni hardcoded
        "MISSING_EXTERNALIZATION", -30,     // Configurazioni non esternalizzate
        "SENSITIVE_DATA_EXPOSURE", -35,     // Esposizione dati sensibili in plain text
        "PROFILE_SPECIFIC_HARDCODING", -20, // Hardcoding profile-specific
        "MISSING_DEFAULT_VALUES", -15,      // Mancanza valori di default
        "CONFIGURATION_DUPLICATION", -18,   // Duplicazione configurazioni
        "INVALID_PROPERTY_NAMING", -10,     // Naming convention proprietà non valide
        "MISSING_VALIDATION", -20,          // Mancanza validazione configurazioni
        "CIRCULAR_DEPENDENCIES", -25,       // Dipendenze circolari configurazioni
        "ENVIRONMENT_COUPLING", -22         // Forte accoppiamento con ambiente
    );
    
    private static final Map<String, Integer> CONFIG_QUALITY_BONUSES = Map.of(
        "EXTERNALIZED_CONFIGURATION", 20,   // Configurazione completamente esternalizzata
        "PROFILE_BASED_CONFIG", 15,         // Configurazioni basate su profili
        "ENCRYPTED_SENSITIVE_DATA", 25,     // Dati sensibili crittografati
        "CONFIGURATION_VALIDATION", 15,     // Validazione configurazioni implementata
        "DEFAULT_VALUES_PROVIDED", 10,      // Valori di default forniti
        "PROPERTY_PLACEHOLDER_USAGE", 12,   // Uso appropriato di property placeholder
        "ENVIRONMENT_ABSTRACTION", 18,      // Astrazione dall'ambiente
        "CONFIGURATION_DOCUMENTATION", 8,   // Configurazioni documentate
        "DYNAMIC_CONFIGURATION", 12,        // Configurazione dinamica supportata
        "CONFIGURATION_SECURITY", 15,       // Configurazioni sicure implementate
        "CENTRALIZED_CONFIG_MANAGEMENT", 10 // Gestione centralizzata configurazioni
    );
    
    public ConfigurationExternalizationReport analyzeConfiguration(CtClass[] classes) {
        ConfigurationExternalizationReport report = new ConfigurationExternalizationReport();
        
        for (CtClass ctClass : classes) {
            analyzeConfigurationProperties(ctClass, report);
            analyzeValueAnnotations(ctClass, report);
            analyzeConfigurationClasses(ctClass, report);
            analyzeHardcodedValues(ctClass, report);
            analyzeSensitiveDataExposure(ctClass, report);
            analyzeProfileSpecificConfig(ctClass, report);
        }
        
        evaluateConfigurationPractices(report);
        calculateConfigurationQualityScore(report);
        return report;
    }
    
    private void analyzeConfigurationProperties(CtClass ctClass, ConfigurationExternalizationReport report) {
        try {
            AnnotationsAttribute attr = (AnnotationsAttribute) 
                ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
            
            if (attr != null) {
                for (Annotation annotation : attr.getAnnotations()) {
                    if (annotation.getTypeName().equals("org.springframework.boot.context.properties.ConfigurationProperties")) {
                        ConfigurationPropertiesClass configClass = new ConfigurationPropertiesClass();
                        configClass.setClassName(ctClass.getName());
                        
                        MemberValue prefixValue = annotation.getMemberValue("prefix");
                        if (prefixValue != null) {
                            configClass.setPrefix(prefixValue.toString());
                        }
                        
                        analyzeConfigurationFields(ctClass, configClass);
                        report.addConfigurationPropertiesClass(configClass);
                    }
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi configuration properties: " + e.getMessage());
        }
    }
    
    private void analyzeValueAnnotations(CtClass ctClass, ConfigurationExternalizationReport report) {
        try {
            CtField[] fields = ctClass.getDeclaredFields();
            
            for (CtField field : fields) {
                AnnotationsAttribute attr = (AnnotationsAttribute) 
                    field.getFieldInfo().getAttribute(AnnotationsAttribute.visibleTag);
                
                if (attr != null) {
                    for (Annotation annotation : attr.getAnnotations()) {
                        if (annotation.getTypeName().equals("org.springframework.beans.factory.annotation.Value")) {
                            ValueAnnotationUsage valueUsage = new ValueAnnotationUsage();
                            valueUsage.setClassName(ctClass.getName());
                            valueUsage.setFieldName(field.getName());
                            valueUsage.setFieldType(field.getType().getName());
                            
                            MemberValue value = annotation.getMemberValue("value");
                            if (value != null) {
                                String propertyExpression = value.toString();
                                valueUsage.setPropertyExpression(propertyExpression);
                                valueUsage.setPropertyKey(extractPropertyKey(propertyExpression));
                                valueUsage.setDefaultValue(extractDefaultValue(propertyExpression));
                            }
                            
                            report.addValueAnnotationUsage(valueUsage);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi @Value annotations: " + e.getMessage());
        }
    }
    
    private void analyzeConfigurationClasses(CtClass ctClass, ConfigurationExternalizationReport report) {
        try {
            AnnotationsAttribute attr = (AnnotationsAttribute) 
                ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
            
            if (attr != null) {
                for (Annotation annotation : attr.getAnnotations()) {
                    if (annotation.getTypeName().equals("org.springframework.context.annotation.Configuration")) {
                        ConfigurationClass configClass = new ConfigurationClass();
                        configClass.setClassName(ctClass.getName());
                        
                        analyzeBeanDefinitions(ctClass, configClass);
                        analyzeProfileAnnotations(ctClass, configClass);
                        
                        report.addConfigurationClass(configClass);
                    }
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi configuration classes: " + e.getMessage());
        }
    }
    
    private void analyzeHardcodedValues(CtClass ctClass, ConfigurationExternalizationReport report) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                String methodBody = getMethodBody(method);
                
                List<HardcodedValue> hardcodedValues = findHardcodedValues(methodBody);
                for (HardcodedValue hardcoded : hardcodedValues) {
                    hardcoded.setClassName(ctClass.getName());
                    hardcoded.setMethodName(method.getName());
                    report.addHardcodedValue(hardcoded);
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi hardcoded values: " + e.getMessage());
        }
    }
    
    private void analyzePropertyFiles(ConfigurationExternalizationReport report) {
        // Analizza i file di proprietà presenti nel classpath
        List<String> propertyFiles = findPropertyFiles();
        
        for (String propertyFile : propertyFiles) {
            PropertyFileAnalysis analysis = analyzePropertyFile(propertyFile);
            report.addPropertyFileAnalysis(analysis);
        }
    }
    
    private List<HardcodedValue> findHardcodedValues(String methodBody) {
        List<HardcodedValue> hardcodedValues = new ArrayList<>();
        
        // Pattern per identificare valori hardcoded sospetti
        Pattern[] patterns = {
            Pattern.compile("\"jdbc:.*?\""),           // Connection strings
            Pattern.compile("\"http[s]?://.*?\""),     // URLs
            Pattern.compile("\".*?password.*?\"", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\".*?secret.*?\"", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\".*?key.*?\"", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\".*?token.*?\"", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\"\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\""), // IP addresses
            Pattern.compile("\".*?:\\d+\"")            // Host:port combinations
        };
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(methodBody);
            while (matcher.find()) {
                HardcodedValue hardcoded = new HardcodedValue();
                hardcoded.setValue(matcher.group());
                hardcoded.setType(determineHardcodedType(matcher.group()));
                hardcoded.setSeverity(determineSeverity(hardcoded.getType()));
                hardcodedValues.add(hardcoded);
            }
        }
        
        return hardcodedValues;
    }
    
    private void evaluateConfigurationPractices(ConfigurationExternalizationReport report) {
        // Valuta le best practices
        BestPracticesEvaluation evaluation = new BestPracticesEvaluation();
        
        // Verifica esternalizzazione
        int totalConfigs = report.getValueAnnotationUsages().size() + 
                          report.getHardcodedValues().size();
        int externalizedConfigs = report.getValueAnnotationUsages().size();
        
        if (totalConfigs > 0) {
            double externalizationRate = (double) externalizedConfigs / totalConfigs;
            evaluation.setExternalizationRate(externalizationRate);
        }
        
        // Verifica sicurezza
        long sensitiveHardcoded = report.getHardcodedValues().stream()
            .filter(hv -> "HIGH".equals(hv.getSeverity()))
            .count();
        evaluation.setSensitiveHardcodedCount(sensitiveHardcoded);
        
        report.setBestPracticesEvaluation(evaluation);
    }
    
    private void analyzeSensitiveDataExposure(CtClass ctClass, ConfigurationExternalizationReport report) {
        try {
            CtField[] fields = ctClass.getDeclaredFields();
            
            for (CtField field : fields) {
                String fieldValue = getFieldInitializationValue(field);
                
                if (fieldValue != null && isSensitiveData(fieldValue)) {
                    SensitiveDataExposure exposure = new SensitiveDataExposure();
                    exposure.setClassName(ctClass.getName());
                    exposure.setFieldName(field.getName());
                    exposure.setExposureType(determineSensitiveDataType(fieldValue));
                    exposure.setSeverity("CRITICA");
                    exposure.setRecommendation("Esternalizzare in variabile d'ambiente o vault");
                    
                    report.addSensitiveDataExposure(exposure);
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi sensitive data: " + e.getMessage());
        }
    }
    
    private void analyzeProfileSpecificConfig(CtClass ctClass, ConfigurationExternalizationReport report) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                AnnotationsAttribute attr = (AnnotationsAttribute) 
                    method.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
                
                if (attr != null) {
                    for (Annotation annotation : attr.getAnnotations()) {
                        if (annotation.getTypeName().contains("Profile")) {
                            ProfileSpecificConfig profileConfig = new ProfileSpecificConfig();
                            profileConfig.setClassName(ctClass.getName());
                            profileConfig.setMethodName(method.getName());
                            profileConfig.setProfileName(extractProfileName(annotation));
                            
                            // Verifica se ha configurazioni hardcoded
                            String methodBody = getMethodBody(method);
                            if (hasHardcodedConfiguration(methodBody)) {
                                profileConfig.setHasHardcodedConfig(true);
                                profileConfig.addQualityIssue("PROFILE_SPECIFIC_HARDCODING",
                                    "Configurazioni hardcoded per profilo specifico");
                            }
                            
                            report.addProfileSpecificConfig(profileConfig);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi profile config: " + e.getMessage());
        }
    }
    
    private void evaluateConfigurationPractices(ConfigurationExternalizationReport report) {
        ConfigurationBestPracticesEvaluation evaluation = new ConfigurationBestPracticesEvaluation();
        
        // Calcola percentuale esternalizzazione
        int totalConfigurationItems = report.getValueAnnotationUsages().size() + 
                                     report.getHardcodedValues().size();
        int externalizedItems = report.getValueAnnotationUsages().size();
        
        if (totalConfigurationItems > 0) {
            double externalizationRate = (double) externalizedItems / totalConfigurationItems;
            evaluation.setExternalizationRate(externalizationRate);
        }
        
        // Conta configurazioni sensibili hardcoded
        long sensitiveHardcoded = report.getSensitiveDataExposures().size();
        evaluation.setSensitiveHardcodedCount(sensitiveHardcoded);
        
        // Valuta uso profili
        boolean hasProfileSupport = !report.getProfileSpecificConfigs().isEmpty();
        evaluation.setHasProfileSupport(hasProfileSupport);
        
        // Valuta validazione configurazioni
        boolean hasConfigValidation = report.getConfigurationPropertiesClasses().stream()
            .anyMatch(config -> config.isHasValidation());
        evaluation.setHasConfigurationValidation(hasConfigValidation);
        
        // Valuta default values
        long configsWithDefaults = report.getValueAnnotationUsages().stream()
            .filter(usage -> usage.getDefaultValue() != null)
            .count();
        
        if (report.getValueAnnotationUsages().size() > 0) {
            double defaultValuesCoverage = (double) configsWithDefaults / 
                                         report.getValueAnnotationUsages().size();
            evaluation.setDefaultValuesCoverage(defaultValuesCoverage);
        }
        
        report.setBestPracticesEvaluation(evaluation);
    }
    
    private void calculateConfigurationQualityScore(ConfigurationExternalizationReport report) {
        int baseScore = 100;
        int totalPenalties = 0;
        int totalBonuses = 0;
        
        ConfigurationBestPracticesEvaluation evaluation = report.getBestPracticesEvaluation();
        List<QualityIssue> qualityIssues = new ArrayList<>();
        
        // Penalità per dati sensibili hardcoded
        if (evaluation.getSensitiveHardcodedCount() > 0) {
            int penalty = Math.min(40, (int) evaluation.getSensitiveHardcodedCount() * 20);
            totalPenalties += penalty;
            qualityIssues.add(new QualityIssue(
                "HARDCODED_SENSITIVE_DATA",
                "CRITICA", 
                String.format("%d configurazioni sensibili hardcoded", evaluation.getSensitiveHardcodedCount()),
                "Esternalizzare tutti i dati sensibili in variabili d'ambiente o configuration vault"
            ));
        }
        
        // Penalità per bassa esternalizzazione
        double externalizationRate = evaluation.getExternalizationRate();
        if (externalizationRate < 0.6) {
            totalPenalties += 30;
            qualityIssues.add(new QualityIssue(
                "MISSING_EXTERNALIZATION",
                "ALTA",
                String.format("Bassa esternalizzazione configurazioni: %.1f%%", externalizationRate * 100),
                "Esternalizzare tutte le configurazioni environment-specific"
            ));
        } else if (externalizationRate < 0.8) {
            totalPenalties += 15;
            qualityIssues.add(new QualityIssue(
                "PARTIAL_EXTERNALIZATION", 
                "MEDIA",
                String.format("Esternalizzazione migliorabile: %.1f%%", externalizationRate * 100),
                "Completare l'esternalizzazione delle configurazioni rimanenti"
            ));
        }
        
        // Penalità per mancanza validazione
        if (!evaluation.isHasConfigurationValidation()) {
            totalPenalties += 20;
            qualityIssues.add(new QualityIssue(
                "MISSING_VALIDATION",
                "ALTA",
                "Mancanza di validazione delle configurazioni",
                "Implementare validation annotations per le configuration properties"
            ));
        }
        
        // Bonus per alta esternalizzazione
        if (externalizationRate > 0.9) {
            totalBonuses += 20;
        }
        
        // Bonus per supporto profili
        if (evaluation.isHasProfileSupport()) {
            totalBonuses += 15;
        }
        
        // Bonus per default values
        double defaultValuesCoverage = evaluation.getDefaultValuesCoverage();
        if (defaultValuesCoverage > 0.7) {
            totalBonuses += 10;
        }
        
        // Bonus per configurazione sicura
        if (evaluation.getSensitiveHardcodedCount() == 0) {
            totalBonuses += 25;
        }
        
        // Calcola score finale
        int finalScore = Math.max(0, Math.min(100, baseScore - totalPenalties + totalBonuses));
        
        ConfigurationQualityScore qualityScore = new ConfigurationQualityScore();
        qualityScore.setOverallScore(finalScore);
        qualityScore.setExternalizationScore((int) (externalizationRate * 100));
        qualityScore.setSecurityScore(calculateConfigSecurityScore(evaluation));
        qualityScore.setProfileSupportScore(evaluation.isHasProfileSupport() ? 100 : 0);
        qualityScore.setValidationScore(evaluation.isHasConfigurationValidation() ? 100 : 60);
        qualityScore.setQualityLevel(determineQualityLevel(finalScore));
        qualityScore.setQualityIssues(qualityIssues);
        qualityScore.setTotalPenalties(totalPenalties);
        qualityScore.setTotalBonuses(totalBonuses);
        
        report.setQualityScore(qualityScore);
    }
    
    // Helper methods
    private boolean isSensitiveData(String value) {
        String lowerValue = value.toLowerCase();
        return SENSITIVE_PROPERTY_PATTERNS.stream()
            .anyMatch(pattern -> lowerValue.contains(pattern));
    }
    
    private String determineSensitiveDataType(String value) {
        String lowerValue = value.toLowerCase();
        if (lowerValue.contains("password") || lowerValue.contains("pwd")) return "PASSWORD";
        if (lowerValue.contains("secret") || lowerValue.contains("key")) return "SECRET";
        if (lowerValue.contains("token")) return "TOKEN";
        if (lowerValue.contains("credential")) return "CREDENTIAL";
        return "SENSITIVE";
    }
    
    private int calculateConfigSecurityScore(ConfigurationBestPracticesEvaluation evaluation) {
        int score = 100;
        
        // Penalità per dati sensibili hardcoded
        score -= evaluation.getSensitiveHardcodedCount() * 30;
        
        return Math.max(0, score);
    }
    
    private String determineQualityLevel(int score) {
        if (score >= 90) return "ECCELLENTE";
        if (score >= 80) return "BUONO"; 
        if (score >= 70) return "DISCRETO";
        if (score >= 60) return "SUFFICIENTE";
        return "INSUFFICIENTE";
    }
}

public class ConfigurationExternalizationReport {
    private List<ConfigurationPropertiesClass> configurationPropertiesClasses = new ArrayList<>();
    private List<ValueAnnotationUsage> valueAnnotationUsages = new ArrayList<>();
    private List<ConfigurationClass> configurationClasses = new ArrayList<>();
    private List<HardcodedValue> hardcodedValues = new ArrayList<>();
    private List<PropertyFileAnalysis> propertyFileAnalyses = new ArrayList<>();
    private List<SensitiveDataExposure> sensitiveDataExposures = new ArrayList<>();
    private List<ProfileSpecificConfig> profileSpecificConfigs = new ArrayList<>();
    private BestPracticesEvaluation bestPracticesEvaluation;
    private ConfigurationQualityScore qualityScore;
    private List<String> errors = new ArrayList<>();
    
    public static class ConfigurationPropertiesClass {
        private String className;
        private String prefix;
        private List<ConfigurationField> fields = new ArrayList<>();
    }
    
    public static class ValueAnnotationUsage {
        private String className;
        private String fieldName;
        private String fieldType;
        private String propertyExpression;
        private String propertyKey;
        private String defaultValue;
    }
    
    public static class HardcodedValue {
        private String className;
        private String methodName;
        private String value;
        private String type;
        private String severity;
    }
    
    public static class SensitiveDataExposure {
        private String className;
        private String fieldName;
        private String exposureType;
        private String severity;
        private String recommendation;
        
        // Getters and setters...
    }
    
    public static class ProfileSpecificConfig {
        private String className;
        private String methodName;
        private String profileName;
        private boolean hasHardcodedConfig;
        private List<String> qualityIssues = new ArrayList<>();
        
        public void addQualityIssue(String issueType, String description) {
            this.qualityIssues.add(issueType + ": " + description);
        }
        
        // Getters and setters...
    }
    
    public static class BestPracticesEvaluation {
        private double externalizationRate;
        private long sensitiveHardcodedCount;
        private boolean hasProfileSupport;
        private boolean hasConfigurationValidation;
        private double defaultValuesCoverage;
        private List<String> recommendations = new ArrayList<>();
        
        // Getters and setters...
    }
    
    public static class ConfigurationQualityScore {
        private int overallScore;
        private int externalizationScore;
        private int securityScore;
        private int profileSupportScore;
        private int validationScore;
        private String qualityLevel;
        private List<QualityIssue> qualityIssues = new ArrayList<>();
        private int totalPenalties;
        private int totalBonuses;
        
        // Getters and setters...
    }
    
    public static class QualityIssue {
        private String issueType;
        private String severity;
        private String description;
        private String recommendation;
        
        public QualityIssue(String issueType, String severity, String description, String recommendation) {
            this.issueType = issueType;
            this.severity = severity;
            this.description = description;
            this.recommendation = recommendation;
        }
        
        // Getters and setters...
    }
    
    // Getter and setter methods
    public void addSensitiveDataExposure(SensitiveDataExposure exposure) {
        this.sensitiveDataExposures.add(exposure);
    }
    
    public void addProfileSpecificConfig(ProfileSpecificConfig config) {
        this.profileSpecificConfigs.add(config);
    }
}
```

## Raccolta Dati

### 1. Configuration Properties
- Classi annotate con `@ConfigurationProperties`
- Prefissi delle proprietà
- Campi di configurazione e loro tipi

### 2. Value Annotations
- Uso di `@Value` per injection di proprietà
- Espressioni SpEL utilizzate
- Valori di default specificati

### 3. Hardcoded Values
- Stringhe hardcoded sospette nel codice
- Connection strings, URLs, password
- Configurazioni che dovrebbero essere esternalizzate

### 4. Property Files
- File application.properties/yml
- Profile-specific property files
- Proprietà definite e loro utilizzo

## Metriche di Qualità del Codice

Il sistema di scoring valuta la qualità dell'esternalizzazione delle configurazioni con focus su security, maintainability e best practices di deployment.

### Algoritmo di Scoring (0-100)

```java
baseScore = 100

// Penalità principali (-10 a -40 punti)
HARDCODED_SENSITIVE_DATA: -40     // Dati sensibili hardcoded
SENSITIVE_DATA_EXPOSURE: -35      // Esposizione dati sensibili in plain text
MISSING_EXTERNALIZATION: -30     // Configurazioni non esternalizzate
CIRCULAR_DEPENDENCIES: -25       // Dipendenze circolari configurazioni
ENVIRONMENT_COUPLING: -22        // Forte accoppiamento con ambiente
MISSING_VALIDATION: -20          // Mancanza validazione configurazioni
PROFILE_SPECIFIC_HARDCODING: -20 // Hardcoding profile-specific
CONFIGURATION_DUPLICATION: -18   // Duplicazione configurazioni

// Bonus principali (+8 a +25 punti)
ENCRYPTED_SENSITIVE_DATA: +25     // Dati sensibili crittografati
EXTERNALIZED_CONFIGURATION: +20  // Configurazione completamente esternalizzata
ENVIRONMENT_ABSTRACTION: +18     // Astrazione dall'ambiente
PROFILE_BASED_CONFIG: +15        // Configurazioni basate su profili
CONFIGURATION_VALIDATION: +15    // Validazione configurazioni implementata
PROPERTY_PLACEHOLDER_USAGE: +12  // Uso appropriato di property placeholder
DYNAMIC_CONFIGURATION: +12       // Configurazione dinamica supportata

// Penalità per coverage
if (externalizationRate < 60%) penaltyPoints += 30
if (externalizationRate < 80%) penaltyPoints += 15
if (sensitiveHardcodedCount > 0) penaltyPoints += 20 per item

// Bonus per best practices
if (externalizationRate > 90%) bonusPoints += 20
if (hasProfileSupport) bonusPoints += 15
if (sensitiveHardcodedCount == 0) bonusPoints += 25

finalScore = max(0, min(100, baseScore - totalPenalties + totalBonuses))
```

### Soglie di Valutazione

| Punteggio | Livello | Descrizione |
|-----------|---------|-------------|
| 90-100 | 🟢 **ECCELLENTE** | Esternalizzazione completa con tutte le best practices |
| 80-89  | 🔵 **BUONO** | Configurazione ben esternalizzata con protezioni adeguate |
| 70-79  | 🟡 **DISCRETO** | Esternalizzazione parziale con alcune lacune |
| 60-69  | 🟠 **SUFFICIENTE** | Configurazione base con miglioramenti necessari |
| 0-59   | 🔴 **INSUFFICIENTE** | Configurazione inadeguata con rischi significativi |

### Categorie di Problemi per Gravità

#### 🔴 CRITICA (35+ punti penalità)
- **Hardcoded Sensitive Data** (-40): Password, chiavi API, token hardcoded nel codice
- **Sensitive Data Exposure** (-35): Dati sensibili in plain text nei file di configurazione
- **Missing Externalization** (-30): Configurazioni environment-specific non esternalizzate

#### 🟠 ALTA (20-34 punti penalità)
- **Circular Dependencies** (-25): Dipendenze circolari nelle configurazioni
- **Environment Coupling** (-22): Forte accoppiamento con ambiente specifico
- **Missing Validation** (-20): Mancanza validazione configurazioni critiche
- **Profile Hardcoding** (-20): Configurazioni profile-specific hardcoded

#### 🟡 MEDIA (15-19 punti penalità)
- **Configuration Duplication** (-18): Duplicazione configurazioni cross-environment
- **Missing Default Values** (-15): Mancanza valori di default per configurazioni

#### 🔵 BASSA (< 15 punti penalità)
- **Invalid Property Naming** (-10): Naming convention proprietà non conforme
- **Suboptimal Placeholder Usage** (-8): Uso subottimale property placeholders

### Esempio Output HTML

```html
<div class="configuration-externalization-quality-score">
    <h3>⚙️ Configuration Externalization Score: 76/100 (DISCRETO)</h3>
    
    <div class="score-breakdown">
        <div class="metric">
            <span class="label">Externalization Rate:</span>
            <div class="bar"><div class="fill" style="width: 82%"></div></div>
            <span class="value">82%</span>
        </div>
        
        <div class="metric">
            <span class="label">Security Score:</span>
            <div class="bar security-low"><div class="fill" style="width: 65%"></div></div>
            <span class="value">65%</span>
        </div>
        
        <div class="metric">
            <span class="label">Profile Support:</span>
            <div class="bar"><div class="fill" style="width: 100%"></div></div>
            <span class="value">100%</span>
        </div>
        
        <div class="metric">
            <span class="label">Validation Score:</span>
            <div class="bar"><div class="fill" style="width: 60%"></div></div>
            <span class="value">60%</span>
        </div>
    </div>

    <div class="critical-issues">
        <h4>🚨 Problemi Critici</h4>
        <div class="issue critical">
            🔴 <strong>HARDCODED_SENSITIVE_DATA</strong>: 2 configurazioni sensibili hardcoded
            <br>→ <em>Spostare password e API keys in variabili d'ambiente</em>
        </div>
        
        <div class="issue high">
            🟠 <strong>MISSING_VALIDATION</strong>: Configurazioni senza validazione
            <br>→ <em>Implementare @Validated e constraint annotations</em>
        </div>
    </div>
    
    <div class="externalization-status">
        <h4>📊 Externalization Status</h4>
        <div class="status-item">
            <span class="label">Total Configuration Items:</span>
            <span class="value">47</span>
        </div>
        
        <div class="status-item">
            <span class="label">Externalized:</span>
            <span class="value">39 (82%)</span>
        </div>
        
        <div class="status-item">
            <span class="label">Hardcoded:</span>
            <span class="value">8 (18%)</span>
        </div>
        
        <div class="status-item critical">
            <span class="label">Sensitive Hardcoded:</span>
            <span class="value">2 (4%)</span>
        </div>
    </div>
    
    <div class="best-practices">
        <h4>✅ Best Practices Implemented</h4>
        <ul>
            <li>✅ Profile-based configuration (dev/test/prod)</li>
            <li>✅ @ConfigurationProperties usage</li>
            <li>✅ Property placeholder resolution</li>
            <li>⚠️ Default values: partial coverage</li>
            <li>❌ Configuration validation: not implemented</li>
            <li>❌ Sensitive data encryption: missing</li>
        </ul>
    </div>
</div>
```

### Metriche Business Value

#### 🔧 Maintainability Impact
- **Multi-Environment Support**: Deployment semplificato su diversi ambienti
- **Configuration Management**: Gestione centralizzata delle configurazioni
- **Deployment Automation**: Automazione CI/CD attraverso configurazione esterna
- **Environment Parity**: Riduzione discrepanze tra ambienti

#### 🔒 Security Benefits  
- **Sensitive Data Protection**: Protezione credenziali e dati sensibili
- **Secret Management**: Integrazione con vault e secret management systems
- **Configuration Security**: Prevenzione di data leak attraverso configurazioni
- **Audit Trail**: Tracciabilità modifiche configurazione

#### 💰 Operational Efficiency
- **Deployment Speed**: Deploy più veloci senza rebuild per configuration changes
- **Environment Consistency**: Riduzione errori da configurazioni inconsistenti  
- **Rollback Capability**: Possibilità di rollback configurazioni indipendentemente dal codice
- **Scalability Support**: Supporto per scaling orizzontale e configurazioni dinamiche

#### 👥 Development Productivity
- **Developer Experience**: Semplificazione setup ambiente sviluppo
- **Testing Facilitation**: Configurazioni dedicate per testing automatizzato
- **Configuration Documentation**: Centralizzione e documentazione configurazioni
- **Debug Support**: Easier troubleshooting attraverso configurazioni esterne

### Raccomandazioni Prioritarie

1. **Eliminate Hardcoded Sensitive Data**: Spostare tutte le credenziali in variabili d'ambiente
2. **Implement Configuration Validation**: Aggiungere validation annotations alle configuration properties
3. **Complete Externalization**: Esternalizzare tutte le configurazioni environment-specific
4. **Enable Configuration Encryption**: Implementare encryption per dati sensibili
5. **Centralize Configuration Management**: Utilizzare configuration server o vault per gestione centralizzata

## Metriche di Valore

- **Maintainability**: Facilita la gestione di configurazioni multi-ambiente
- **Security**: Identifica dati sensibili hardcoded
- **Flexibility**: Valuta l'adattabilità dell'applicazione a diversi ambienti
- **Best Practices**: Misura l'aderenza agli standard di configurazione

## Classificazione

**Categoria**: Architecture & Dependencies
**Priorità**: Alta - La gestione corretta della configurazione è cruciale per applicazioni enterprise
**Stakeholder**: Development team, DevOps, Operations team