# Report 17: Profili Spring Attivi

**Valore**: ⭐⭐⭐ **Complessità**: 🟢 Semplice **Tempo**: 2-3 giorni
**Tags**: `#spring-profiles` `#environment-configuration` `#profile-activation`

## Descrizione

Analizza i profili Spring attivi nell'applicazione, identificando configurazioni profile-specific, @Profile annotations, e strategie di attivazione profili per diversi ambienti.

## Sezioni del Report

### 1. Profile Definition Analysis
- @Profile annotation usage on components and configurations
- Profile-specific configuration files (application-{profile}.properties)
- Profile activation conditions and logic
- Default profile configurations

### 2. Environment-Specific Configurations
- Profile-specific bean definitions
- Conditional component loading based on profiles
- Environment variable dependencies
- Profile hierarchy and inheritance

### 3. Profile Activation Strategies
- Programmatic profile activation
- Command-line and environment activation
- Profile groups and composite profiles
- Runtime profile switching capabilities

### 4. Profile Configuration Issues
- Missing profile configurations
- Profile conflicts and overlaps
- Unused or orphaned profiles
- Environment-specific deployment issues

## Implementazione Javassist Completa

```java
public class ActiveSpringProfilesAnalyzer {
    
    public SpringProfilesReport analyzeActiveSpringProfiles(CtClass[] classes) {
        SpringProfilesReport report = new SpringProfilesReport();
        
        // 1. Identifica usage @Profile
        analyzeProfileAnnotations(classes, report);
        
        // 2. Analizza configuration files per profili
        analyzeProfileConfigurationFiles(report);
        
        // 3. Analizza activation logic
        analyzeProfileActivationLogic(classes, report);
        
        // 4. Identifica problemi profili
        identifyProfileIssues(report);
        
        return report;
    }
    
    private void analyzeProfileAnnotations(CtClass[] classes, SpringProfilesReport report) {
        for (CtClass ctClass : classes) {
            try {
                analyzeClassProfileUsage(ctClass, report);
            } catch (Exception e) {
                report.addError("Errore nell'analisi profile annotations: " + e.getMessage());
            }
        }
    }
    
    private void analyzeClassProfileUsage(CtClass ctClass, SpringProfilesReport report) {
        try {
            if (ctClass.hasAnnotation("org.springframework.context.annotation.Profile")) {
                ProfiledComponent component = analyzeProfiledComponent(ctClass);
                report.addProfiledComponent(component);
            }
            
            // Analizza metodi con @Profile
            CtMethod[] methods = ctClass.getDeclaredMethods();
            for (CtMethod method : methods) {
                if (method.hasAnnotation("org.springframework.context.annotation.Profile")) {
                    ProfiledMethod profiledMethod = analyzeProfiledMethod(method, ctClass);
                    report.addProfiledMethod(profiledMethod);
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi classe profile: " + e.getMessage());
        }
    }
    
    private ProfiledComponent analyzeProfiledComponent(CtClass ctClass) {
        try {
            ProfiledComponent component = new ProfiledComponent();
            component.setClassName(ctClass.getName());
            component.setSimpleName(ctClass.getSimpleName());
            
            // Estrai profili dalla @Profile annotation
            Annotation profileAnnotation = ctClass.getAnnotation("org.springframework.context.annotation.Profile");
            String[] profiles = extractProfileValues(profileAnnotation);
            component.setProfiles(Arrays.asList(profiles));
            
            // Determina tipo componente
            ComponentType componentType = determineComponentType(ctClass);
            component.setComponentType(componentType);
            
            // Analizza conditional logic
            analyzeConditionalLogic(ctClass, component);
            
            return component;
            
        } catch (Exception e) {
            ProfiledComponent errorComponent = new ProfiledComponent();
            errorComponent.setClassName(ctClass.getName());
            errorComponent.addError("Errore nell'analisi profiled component: " + e.getMessage());
            return errorComponent;
        }
    }
    
    private ComponentType determineComponentType(CtClass ctClass) {
        try {
            if (ctClass.hasAnnotation("org.springframework.context.annotation.Configuration")) {
                return ComponentType.CONFIGURATION;
            } else if (ctClass.hasAnnotation("org.springframework.stereotype.Service")) {
                return ComponentType.SERVICE;
            } else if (ctClass.hasAnnotation("org.springframework.stereotype.Repository")) {
                return ComponentType.REPOSITORY;
            } else if (ctClass.hasAnnotation("org.springframework.stereotype.Controller") ||
                      ctClass.hasAnnotation("org.springframework.web.bind.annotation.RestController")) {
                return ComponentType.CONTROLLER;
            } else if (ctClass.hasAnnotation("org.springframework.stereotype.Component")) {
                return ComponentType.COMPONENT;
            } else {
                return ComponentType.OTHER;
            }
        } catch (Exception e) {
            return ComponentType.OTHER;
        }
    }
    
    private void analyzeConditionalLogic(CtClass ctClass, ProfiledComponent component) {
        try {
            List<String> conditionalAnnotations = new ArrayList<>();
            
            // @ConditionalOnProperty
            if (ctClass.hasAnnotation("org.springframework.boot.autoconfigure.condition.ConditionalOnProperty")) {
                Annotation annotation = ctClass.getAnnotation("org.springframework.boot.autoconfigure.condition.ConditionalOnProperty");
                String property = extractAnnotationValue(annotation, "name");
                conditionalAnnotations.add("ConditionalOnProperty: " + property);
            }
            
            // @ConditionalOnClass
            if (ctClass.hasAnnotation("org.springframework.boot.autoconfigure.condition.ConditionalOnClass")) {
                conditionalAnnotations.add("ConditionalOnClass");
            }
            
            // @ConditionalOnMissingBean
            if (ctClass.hasAnnotation("org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean")) {
                conditionalAnnotations.add("ConditionalOnMissingBean");
            }
            
            component.setConditionalAnnotations(conditionalAnnotations);
            
        } catch (Exception e) {
            component.addError("Errore nell'analisi conditional logic: " + e.getMessage());
        }
    }
    
    private void analyzeProfileConfigurationFiles(SpringProfilesReport report) {
        try {
            // Simula ricerca configuration files profile-specific
            List<ProfileConfigurationFile> configFiles = discoverProfileConfigurationFiles();
            
            for (ProfileConfigurationFile configFile : configFiles) {
                analyzeConfigurationFileContent(configFile, report);
            }
            
            report.setProfileConfigurationFiles(configFiles);
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi profile configuration files: " + e.getMessage());
        }
    }
    
    private List<ProfileConfigurationFile> discoverProfileConfigurationFiles() {
        List<ProfileConfigurationFile> configFiles = new ArrayList<>();
        
        // Pattern comuni per profile configuration files
        String[] commonPatterns = {
            "application-dev.properties", "application-prod.properties", 
            "application-test.properties", "application-local.properties",
            "application-dev.yml", "application-prod.yml", 
            "application-test.yml", "application-local.yml"
        };
        
        for (String pattern : commonPatterns) {
            // In implementazione reale, cercherebbe file esistenti
            ProfileConfigurationFile configFile = new ProfileConfigurationFile();
            configFile.setFileName(pattern);
            configFile.setProfile(extractProfileFromFileName(pattern));
            configFile.setFileType(pattern.endsWith(".yml") ? "YAML" : "PROPERTIES");
            
            configFiles.add(configFile);
        }
        
        return configFiles;
    }
    
    private String extractProfileFromFileName(String fileName) {
        // Estrae profile name da application-{profile}.properties/yml
        if (fileName.startsWith("application-")) {
            int start = "application-".length();
            int end = fileName.lastIndexOf('.');
            if (end > start) {
                return fileName.substring(start, end);
            }
        }
        return "unknown";
    }
    
    private void analyzeProfileActivationLogic(CtClass[] classes, SpringProfilesReport report) {
        try {
            for (CtClass ctClass : classes) {
                analyzeProfileActivationInClass(ctClass, report);
            }
        } catch (Exception e) {
            report.addError("Errore nell'analisi profile activation logic: " + e.getMessage());
        }
    }
    
    private void analyzeProfileActivationInClass(CtClass ctClass, SpringProfilesReport report) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                method.instrument(new ExprEditor() {
                    @Override
                    public void edit(MethodCall call) throws CannotCompileException {
                        String className = call.getClassName();
                        String methodName = call.getMethodName();
                        
                        // Rileva chiamate per activation profili
                        if (isProfileActivationCall(className, methodName)) {
                            ProfileActivation activation = new ProfileActivation();
                            activation.setClassName(ctClass.getName());
                            activation.setMethodName(method.getName());
                            activation.setActivationType(determineActivationType(className, methodName));
                            activation.setActivationCall(className + "." + methodName);
                            
                            report.addProfileActivation(activation);
                        }
                        
                        // Rileva environment access
                        if (isEnvironmentAccess(className, methodName)) {
                            EnvironmentAccess envAccess = new EnvironmentAccess();
                            envAccess.setClassName(ctClass.getName());
                            envAccess.setMethodName(method.getName());
                            envAccess.setAccessType(methodName);
                            
                            report.addEnvironmentAccess(envAccess);
                        }
                    }
                });
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi profile activation in class: " + e.getMessage());
        }
    }
    
    private boolean isProfileActivationCall(String className, String methodName) {
        return (className.contains("Environment") && methodName.equals("setActiveProfiles")) ||
               (className.contains("Environment") && methodName.equals("addActiveProfile")) ||
               (className.contains("ConfigurableEnvironment") && methodName.contains("Profile"));
    }
    
    private String determineActivationType(String className, String methodName) {
        if (methodName.equals("setActiveProfiles")) {
            return "SET_ACTIVE_PROFILES";
        } else if (methodName.equals("addActiveProfile")) {
            return "ADD_ACTIVE_PROFILE";
        } else if (methodName.contains("Default")) {
            return "SET_DEFAULT_PROFILES";
        } else {
            return "OTHER";
        }
    }
    
    private void identifyProfileIssues(SpringProfilesReport report) {
        try {
            // Identifica profili non utilizzati
            identifyUnusedProfiles(report);
            
            // Identifica conflitti profili
            identifyProfileConflicts(report);
            
            // Identifica configurazioni mancanti
            identifyMissingProfileConfigurations(report);
            
            // Identifica profile dependency issues
            identifyProfileDependencyIssues(report);
            
        } catch (Exception e) {
            report.addError("Errore nell'identificazione profile issues: " + e.getMessage());
        }
    }
    
    private void identifyUnusedProfiles(SpringProfilesReport report) {
        Set<String> definedProfiles = new HashSet<>();
        Set<String> usedProfiles = new HashSet<>();
        
        // Raccoglie profili definiti
        for (ProfileConfigurationFile configFile : report.getProfileConfigurationFiles()) {
            definedProfiles.add(configFile.getProfile());
        }
        
        // Raccoglie profili utilizzati
        for (ProfiledComponent component : report.getProfiledComponents()) {
            usedProfiles.addAll(component.getProfiles());
        }
        
        // Identifica profili definiti ma non utilizzati
        definedProfiles.removeAll(usedProfiles);
        
        for (String unusedProfile : definedProfiles) {
            ProfileIssue issue = new ProfileIssue();
            issue.setType(IssueType.UNUSED_PROFILE);
            issue.setProfileName(unusedProfile);
            issue.setSeverity(Severity.LOW);
            issue.setDescription("Profile '" + unusedProfile + "' has configuration file but no components use it");
            issue.setRecommendation("Remove unused profile configuration or add components that use it");
            
            report.addProfileIssue(issue);
        }
    }
    
    private void identifyProfileConflicts(SpringProfilesReport report) {
        Map<String, List<ProfiledComponent>> componentsByProfile = new HashMap<>();
        
        // Raggruppa componenti per profile
        for (ProfiledComponent component : report.getProfiledComponents()) {
            for (String profile : component.getProfiles()) {
                componentsByProfile.computeIfAbsent(profile, k -> new ArrayList<>()).add(component);
            }
        }
        
        // Cerca conflitti (multiple bean dello stesso tipo nello stesso profilo)
        for (Map.Entry<String, List<ProfiledComponent>> entry : componentsByProfile.entrySet()) {
            Map<ComponentType, Long> typeCount = entry.getValue().stream()
                .collect(Collectors.groupingBy(ProfiledComponent::getComponentType, Collectors.counting()));
            
            for (Map.Entry<ComponentType, Long> typeEntry : typeCount.entrySet()) {
                if (typeEntry.getValue() > 1 && typeEntry.getKey() != ComponentType.COMPONENT) {
                    ProfileIssue issue = new ProfileIssue();
                    issue.setType(IssueType.PROFILE_CONFLICT);
                    issue.setProfileName(entry.getKey());
                    issue.setSeverity(Severity.MEDIUM);
                    issue.setDescription("Multiple " + typeEntry.getKey() + " components in profile '" + entry.getKey() + "'");
                    issue.setRecommendation("Review component definitions to avoid conflicts");
                    
                    report.addProfileIssue(issue);
                }
            }
        }
    }
}

public class SpringProfilesReport {
    private List<ProfiledComponent> profiledComponents = new ArrayList<>();
    private List<ProfiledMethod> profiledMethods = new ArrayList<>();
    private List<ProfileConfigurationFile> profileConfigurationFiles = new ArrayList<>();
    private List<ProfileActivation> profileActivations = new ArrayList<>();
    private List<EnvironmentAccess> environmentAccesses = new ArrayList<>();
    private List<ProfileIssue> profileIssues = new ArrayList<>();
    private ProfileStatistics profileStatistics;
    private List<String> errors = new ArrayList<>();
    
    public static class ProfiledComponent {
        private String className;
        private String simpleName;
        private List<String> profiles = new ArrayList<>();
        private ComponentType componentType;
        private List<String> conditionalAnnotations = new ArrayList<>();
        private List<String> errors = new ArrayList<>();
    }
    
    public static class ProfileConfigurationFile {
        private String fileName;
        private String profile;
        private String fileType; // PROPERTIES, YAML
        private Map<String, String> properties = new HashMap<>();
        private boolean exists;
    }
    
    public static class ProfileActivation {
        private String className;
        private String methodName;
        private String activationType;
        private String activationCall;
    }
    
    public static class ProfileIssue {
        private IssueType type;
        private String profileName;
        private String className;
        private Severity severity;
        private String description;
        private String recommendation;
    }
    
    public enum ComponentType {
        CONFIGURATION, SERVICE, REPOSITORY, CONTROLLER, COMPONENT, OTHER
    }
    
    public enum IssueType {
        UNUSED_PROFILE,
        PROFILE_CONFLICT,
        MISSING_PROFILE_CONFIGURATION,
        PROFILE_DEPENDENCY_ISSUE,
        INVALID_PROFILE_ACTIVATION
    }
    
    public enum Severity {
        CRITICAL, HIGH, MEDIUM, LOW
    }
}
```

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculateSpringProfilesQualityScore(SpringProfilesReport result) {
    double score = 100.0;
    
    // Penalizzazioni per problemi gestione profili
    score -= result.getProfileConflicts() * 15;                   // -15 per conflitti tra profili
    score -= result.getMissingProfileConfigurations() * 12;       // -12 per configurazioni profili mancanti
    score -= result.getInvalidProfileActivations() * 10;          // -10 per activation profili non valide
    score -= result.getProfileDependencyIssues() * 8;             // -8 per problemi dipendenze profili
    score -= result.getUnusedProfiles() * 5;                      // -5 per profili inutilizzati
    score -= result.getMissingDefaultProfileHandling() * 6;       // -6 per mancanza gestione default profile
    
    // Bonus per buone pratiche gestione profili
    score += result.getWellDefinedProfiles() * 3;                 // +3 per profili ben definiti
    score += result.getConsistentProfileUsage() * 2;              // +2 per uso consistente profili
    score += result.getEnvironmentSpecificConfigurations() * 2;   // +2 per configurazioni environment-specific
    score += result.getConditionalComponentUsage() * 1;           // +1 per uso conditional components
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - Gestione profili problematica con conflitti significativi
- **41-60**: 🟡 SUFFICIENTE - Uso profili di base ma con lacune nella configurazione
- **61-80**: 🟢 BUONO - Buona gestione profili con alcuni miglioramenti possibili
- **81-100**: ⭐ ECCELLENTE - Gestione profili ottimale e environment-aware

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -12 to -15)
1. **Conflitti tra profili**
   - Descrizione: Multiple bean dello stesso tipo attivi nello stesso profilo
   - Rischio: Ambiguous bean resolution, unpredictable behavior
   - Soluzione: Rivedere definizioni componenti per evitare sovrapposizioni

2. **Configurazioni profili mancanti**
   - Descrizione: Profili referenziati in @Profile senza configuration files
   - Rischio: Missing configuration, application startup failures
   - Soluzione: Creare file configurazione per tutti i profili utilizzati

### 🟠 GRAVITÀ ALTA (Score Impact: -8 to -10)  
3. **Activation profili non valide**
   - Descrizione: Logica programmatic activation non corretta
   - Rischio: Wrong profile activation, incorrect environment behavior
   - Soluzione: Correggere logica activation o usare activation standard

4. **Problemi dipendenze profili**
   - Descrizione: Bean profile-specific che dipendono da bean non disponibili
   - Rischio: Missing dependencies in specific environments
   - Soluzione: Assicurare dependencies disponibili in tutti profili necessari

### 🟡 GRAVITÀ MEDIA (Score Impact: -6)
5. **Mancanza gestione default profile**
   - Descrizione: Nessuna configurazione per comportamento senza profili attivi
   - Rischio: Unpredictable behavior quando nessun profilo è specificato
   - Soluzione: Definire default profile behavior appropriato

### 🔵 GRAVITÀ BASSA (Score Impact: -5)
6. **Profili inutilizzati**
   - Descrizione: Configuration files per profili mai utilizzati
   - Rischio: Configuration drift, maintenance overhead
   - Soluzione: Rimuovere configuration profiles non utilizzati

## Metriche di Valore

- **Environment Management**: Facilita gestione configurazioni multi-environment
- **Deployment Flexibility**: Permette deployment flessibile across diversi ambienti
- **Configuration Clarity**: Migliora chiarezza separazione configurazioni per ambiente
- **Runtime Adaptability**: Abilita comportamento runtime adattivo basato su environment

**Tags**: `#spring-profiles` `#environment-configuration` `#profile-activation` `#medium-value` `#simple-complexity`