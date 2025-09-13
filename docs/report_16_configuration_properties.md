# Report 16: Proprietà di Configurazione Caricate

**Valore**: ⭐⭐⭐ **Complessità**: 🟢 Semplice **Tempo**: 3-4 giorni
**Tags**: `#configuration-properties` `#application-properties` `#environment-config`

## Descrizione

Analizza tutte le proprietà di configurazione caricate dall'applicazione Spring Boot, inclusi file properties, YAML, variabili d'ambiente e configuration classes, mappando il loro utilizzo e validazione.

## Sezioni del Report

### 1. Configuration Sources Overview
- File di proprietà utilizzati (application.properties, application.yml)
- Profile-specific configurations
- External property sources (environment variables, command line)
- PropertySource priority e override behavior

### 2. Property Analysis
- @ConfigurationProperties classes
- @Value injections nelle classi
- Property keys utilizzati vs non utilizzati
- Default values e validation constraints

### 3. Environment Configuration
- Active profiles detection
- Environment-specific overrides
- Sensitive properties identification
- Configuration validation

### 4. Best Practices Assessment
- Property organization e naming conventions
- Externalization appropriateness
- Security di configurazioni sensitive
- Documentation delle proprietà

## Implementazione con Javassist

```java
public class ConfigurationPropertiesAnalyzer {
    
    public ConfigurationPropertiesReport analyzeConfigurationProperties(CtClass[] classes) {
        ConfigurationPropertiesReport report = new ConfigurationPropertiesReport();
        
        for (CtClass ctClass : classes) {
            analyzeConfigurationPropertiesClasses(ctClass, report);
            analyzeValueInjections(ctClass, report);
        }
        
        analyzePropertyFiles(report);
        validatePropertyUsage(report);
        
        return report;
    }
    
    private void analyzeConfigurationPropertiesClasses(CtClass ctClass, ConfigurationPropertiesReport report) {
        try {
            if (ctClass.hasAnnotation("org.springframework.boot.context.properties.ConfigurationProperties")) {
                ConfigurationPropertiesClass configClass = new ConfigurationPropertiesClass();
                configClass.setClassName(ctClass.getName());
                
                Annotation annotation = ctClass.getAnnotation("ConfigurationProperties");
                MemberValue prefixValue = annotation.getMemberValue("prefix");
                if (prefixValue != null) {
                    configClass.setPrefix(prefixValue.toString().replace("\"", ""));
                }
                
                // Analizza campi della classe
                CtField[] fields = ctClass.getDeclaredFields();
                for (CtField field : fields) {
                    ConfigurationProperty property = analyzeConfigurationField(field, configClass.getPrefix());
                    configClass.addProperty(property);
                }
                
                // Analizza nested configuration objects
                analyzeNestedConfigurations(ctClass, configClass);
                
                report.addConfigurationPropertiesClass(configClass);
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi @ConfigurationProperties: " + e.getMessage());
        }
    }
    
    private ConfigurationProperty analyzeConfigurationField(CtField field, String prefix) {
        ConfigurationProperty property = new ConfigurationProperty();
        property.setFieldName(field.getName());
        property.setFieldType(field.getType().getName());
        property.setPropertyKey(buildPropertyKey(prefix, field.getName()));
        
        try {
            // Analizza annotazioni di validazione
            analyzeValidationAnnotations(field, property);
            
            // Determina se ha valore di default
            if (hasDefaultValue(field)) {
                property.setHasDefaultValue(true);
                property.setDefaultValue(extractDefaultValue(field));
            }
            
            // Verifica se proprietà è required/optional
            property.setRequired(isRequiredProperty(field));
            
        } catch (Exception e) {
            property.addError("Error analyzing field: " + e.getMessage());
        }
        
        return property;
    }
    
    private void analyzeValueInjections(CtClass ctClass, ConfigurationPropertiesReport report) {
        try {
            CtField[] fields = ctClass.getDeclaredFields();
            
            for (CtField field : fields) {
                if (field.hasAnnotation("org.springframework.beans.factory.annotation.Value")) {
                    ValueInjection valueInjection = new ValueInjection();
                    valueInjection.setClassName(ctClass.getName());
                    valueInjection.setFieldName(field.getName());
                    valueInjection.setFieldType(field.getType().getName());
                    
                    Annotation valueAnnotation = field.getAnnotation("Value");
                    MemberValue value = valueAnnotation.getMemberValue("value");
                    if (value != null) {
                        String propertyExpression = value.toString().replace("\"", "");
                        valueInjection.setPropertyExpression(propertyExpression);
                        
                        // Estrai property key e default value
                        parsePropertyExpression(propertyExpression, valueInjection);
                    }
                    
                    report.addValueInjection(valueInjection);
                }
            }
            
            // Analizza anche metodi con @Value
            CtMethod[] methods = ctClass.getDeclaredMethods();
            for (CtMethod method : methods) {
                if (method.hasAnnotation("org.springframework.beans.factory.annotation.Value")) {
                    analyzeMethodValueInjection(method, report);
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi @Value injections: " + e.getMessage());
        }
    }
    
    private void parsePropertyExpression(String expression, ValueInjection injection) {
        // Pattern: ${property.key:defaultValue}
        if (expression.startsWith("${") && expression.endsWith("}")) {
            String content = expression.substring(2, expression.length() - 1);
            
            if (content.contains(":")) {
                String[] parts = content.split(":", 2);
                injection.setPropertyKey(parts[0].trim());
                injection.setDefaultValue(parts[1].trim());
                injection.setHasDefaultValue(true);
            } else {
                injection.setPropertyKey(content.trim());
                injection.setHasDefaultValue(false);
            }
        } else if (expression.startsWith("#{")) {
            // SpEL expression
            injection.setSpelExpression(true);
            injection.setPropertyExpression(expression);
        } else {
            // Literal value
            injection.setLiteralValue(expression);
        }
    }
    
    private void analyzePropertyFiles(ConfigurationPropertiesReport report) {
        // Simula analisi di file properties comuni
        List<String> propertyFiles = Arrays.asList(
            "application.properties",
            "application.yml", 
            "application-dev.properties",
            "application-prod.properties",
            "application-test.properties"
        );
        
        for (String fileName : propertyFiles) {
            PropertyFileInfo fileInfo = analyzePropertyFile(fileName);
            if (fileInfo != null) {
                report.addPropertyFile(fileInfo);
            }
        }
    }
    
    private PropertyFileInfo analyzePropertyFile(String fileName) {
        PropertyFileInfo fileInfo = new PropertyFileInfo();
        fileInfo.setFileName(fileName);
        fileInfo.setType(fileName.endsWith(".yml") || fileName.endsWith(".yaml") ? 
                          PropertyFileType.YAML : PropertyFileType.PROPERTIES);
        
        // In una implementazione reale, qui si parserebbe il file
        // Per questa analisi statica, generiamo info simulate
        fileInfo.setExists(true);
        fileInfo.setProfileSpecific(fileName.contains("-"));
        
        if (fileName.contains("-")) {
            String profile = extractProfileFromFileName(fileName);
            fileInfo.setProfile(profile);
        }
        
        return fileInfo;
    }
    
    private void validatePropertyUsage(ConfigurationPropertiesReport report) {
        PropertyUsageValidation validation = new PropertyUsageValidation();
        
        // Raccoglie tutte le property keys utilizzate
        Set<String> usedProperties = new HashSet<>();
        
        // Da @ConfigurationProperties classes
        for (ConfigurationPropertiesClass configClass : report.getConfigurationPropertiesClasses()) {
            for (ConfigurationProperty property : configClass.getProperties()) {
                usedProperties.add(property.getPropertyKey());
            }
        }
        
        // Da @Value injections
        for (ValueInjection injection : report.getValueInjections()) {
            if (injection.getPropertyKey() != null) {
                usedProperties.add(injection.getPropertyKey());
            }
        }
        
        validation.setUsedPropertyKeys(usedProperties);
        
        // Identifica proprietà comuni che potrebbero essere configurate
        identifyMissingStandardProperties(validation);
        
        // Identifica possibili proprietà duplicate o inconsistenti
        identifyPropertyInconsistencies(report, validation);
        
        report.setPropertyUsageValidation(validation);
    }
    
    private void identifyMissingStandardProperties(PropertyUsageValidation validation) {
        Set<String> standardProperties = Set.of(
            "server.port",
            "spring.application.name",
            "spring.profiles.active",
            "logging.level.root",
            "spring.datasource.url",
            "spring.jpa.hibernate.ddl-auto"
        );
        
        Set<String> missingStandard = new HashSet<>();
        for (String standardProp : standardProperties) {
            if (!validation.getUsedPropertyKeys().contains(standardProp)) {
                missingStandard.add(standardProp);
            }
        }
        
        validation.setMissingStandardProperties(missingStandard);
    }
    
    private void identifyPropertyInconsistencies(ConfigurationPropertiesReport report, PropertyUsageValidation validation) {
        List<PropertyInconsistency> inconsistencies = new ArrayList<>();
        
        // Verifica naming conventions
        for (String propertyKey : validation.getUsedPropertyKeys()) {
            if (!followsNamingConventions(propertyKey)) {
                PropertyInconsistency inconsistency = new PropertyInconsistency();
                inconsistency.setType(InconsistencyType.NAMING_CONVENTION);
                inconsistency.setPropertyKey(propertyKey);
                inconsistency.setDescription("Property key doesn't follow kebab-case convention");
                inconsistency.setRecommendation("Use kebab-case: " + toKebabCase(propertyKey));
                
                inconsistencies.add(inconsistency);
            }
        }
        
        validation.setPropertyInconsistencies(inconsistencies);
    }
    
    private boolean followsNamingConventions(String propertyKey) {
        // Spring Boot recommends kebab-case
        return propertyKey.equals(propertyKey.toLowerCase()) && 
               !propertyKey.contains("_") && 
               !propertyKey.matches(".*[A-Z].*");
    }
}

public class ConfigurationPropertiesReport {
    private List<ConfigurationPropertiesClass> configurationPropertiesClasses = new ArrayList<>();
    private List<ValueInjection> valueInjections = new ArrayList<>();
    private List<PropertyFileInfo> propertyFiles = new ArrayList<>();
    private PropertyUsageValidation propertyUsageValidation;
    private List<String> errors = new ArrayList<>();
    
    public static class ConfigurationPropertiesClass {
        private String className;
        private String prefix;
        private List<ConfigurationProperty> properties = new ArrayList<>();
        private boolean validated;
    }
    
    public static class ConfigurationProperty {
        private String fieldName;
        private String fieldType;
        private String propertyKey;
        private boolean required;
        private boolean hasDefaultValue;
        private String defaultValue;
        private List<ValidationConstraint> validationConstraints = new ArrayList<>();
    }
    
    public static class ValueInjection {
        private String className;
        private String fieldName;
        private String fieldType;
        private String propertyExpression;
        private String propertyKey;
        private String defaultValue;
        private boolean hasDefaultValue;
        private boolean spelExpression;
        private String literalValue;
    }
    
    public static class PropertyFileInfo {
        private String fileName;
        private PropertyFileType type;
        private boolean exists;
        private boolean profileSpecific;
        private String profile;
        private int propertyCount;
    }
    
    public static class PropertyUsageValidation {
        private Set<String> usedPropertyKeys = new HashSet<>();
        private Set<String> missingStandardProperties = new HashSet<>();
        private List<PropertyInconsistency> propertyInconsistencies = new ArrayList<>();
    }
}
```

## Raccolta Dati

### 1. Configuration Properties Classes
- Classi annotate con @ConfigurationProperties
- Mapping prefix -> properties
- Validation constraints sui campi
- Nested configuration objects

### 2. Value Injections
- @Value annotations su campi e metodi
- Property keys referenziati
- Default values specificati
- SpEL expressions utilizzate

### 3. Property Files Analysis
- File properties/yaml disponibili
- Profile-specific configurations
- Property keys definiti nei file
- Override hierarchy

### 4. Usage Validation
- Proprietà utilizzate vs definite nei file
- Missing standard properties
- Naming convention compliance
- Sensitive data identification

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculateConfigurationPropertiesQualityScore(ConfigurationPropertiesReport result) {
    double score = 100.0;
    
    // Penalizzazioni per problemi di configurazione
    score -= result.getHardcodedValues() * 15;                // -15 per valori hardcoded
    score -= result.getMissingValidation() * 10;              // -10 per proprietà senza validazione
    score -= result.getNamingInconsistencies() * 8;           // -8 per naming inconsistente
    score -= result.getMissingDocumentation() * 6;            // -6 per proprietà non documentate
    score -= result.getSensitiveDataExposed() * 20;           // -20 per dati sensibili esposti
    score -= result.getUnusedProperties() * 3;                // -3 per proprietà non utilizzate
    score -= result.getMissingDefaults() * 4;                 // -4 per mancanza default values
    
    // Bonus per buone pratiche
    score += result.getProperlyExternalizedConfigs() * 2;     // +2 per configurazioni esternalizzate
    score += result.getWellDocumentedProperties() * 1;        // +1 per documentazione completa
    score += result.getConsistentNaming() * 1;                // +1 per naming consistente
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - Configuration management inadeguato e rischi di sicurezza
- **41-60**: 🟡 SUFFICIENTE - Configurazione funzionale ma non ottimizzata
- **61-80**: 🟢 BUONO - Gestione configurazione ben strutturata  
- **81-100**: ⭐ ECCELLENTE - Configuration management ottimale e sicuro

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -15 to -20)
1. **Dati sensibili esposti in properties**
   - Descrizione: Password, API keys, secrets in plain text in property files
   - Rischio: Security breach, credential exposure in version control
   - Soluzione: Utilizzare environment variables, encryption, o secret management

2. **Valori hardcoded nel codice**
   - Descrizione: Configuration values embedded nel codice invece che esternalizzati
   - Rischio: Difficoltà deployment multi-ambiente, violazione 12-factor app
   - Soluzione: Esternalizzare in application.properties con @ConfigurationProperties

### 🟠 GRAVITÀ ALTA (Score Impact: -8 to -10)
3. **Proprietà senza validazione**
   - Descrizione: @ConfigurationProperties classes senza validation constraints
   - Rischio: Runtime errors con configurazioni invalid, difficult debugging
   - Soluzione: Aggiungere @Valid e JSR-303 constraints (@NotNull, @Min, etc.)

4. **Naming conventions inconsistenti**
   - Descrizione: Mix di camelCase, snake_case, kebab-case nelle property keys
   - Rischio: Confusione per sviluppatori, difficoltà maintenance
   - Soluzione: Standardizzare su kebab-case come raccomandato da Spring Boot

### 🟡 GRAVITÀ MEDIA (Score Impact: -4 to -6)
5. **Proprietà non documentate**
   - Descrizione: Configuration properties senza Javadoc o commenti
   - Rischio: Utilizzo scorretto, difficulty onboarding nuovi sviluppatori
   - Soluzione: Documentare purpose e valori accettabili per ogni proprietà

6. **Default values mancanti**
   - Descrizione: Required properties senza fallback values appropriati
   - Rischio: Application fails to start con configurazioni incomplete
   - Soluzione: Fornire default values ragionevoli dove possibile

### 🔵 GRAVITÀ BASSA (Score Impact: -1 to -3)
7. **Proprietà definite ma non utilizzate**
   - Descrizione: Property keys nei file ma non referenziati nel codice
   - Rischio: Configurazione cluttered, maintenance overhead minimo
   - Soluzione: Cleanup proprietà inutilizzate periodicamente

## Metriche di Valore

- **Environment Portability**: Facilita deployment cross-environment
- **Configuration Management**: Centralizza gestione configurazioni applicative
- **Security Posture**: Riduce rischi esposizione credential e sensitive data
- **Developer Experience**: Semplifica configurazione e debugging

## Classificazione

**Categoria**: Architecture & Dependencies
**Priorità**: Alta - Configuration management è cruciale per operazioni
**Stakeholder**: Development team, DevOps, Operations team