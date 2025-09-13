# Report 18: Report delle Annotazioni Custom

**Valore**: ⭐⭐⭐ **Complessità**: 🟡 Media **Tempo**: 4-5 giorni
**Tags**: `#custom-annotations` `#metadata-analysis` `#annotation-processing`

## Descrizione

Analizza le annotazioni custom definite nell'applicazione, il loro utilizzo, patterns di implementazione, e integrazione con framework Spring e altri sistemi di processing.

## Sezioni del Report

### 1. Custom Annotation Definitions
- @interface custom annotation declarations
- Annotation attributes and default values
- Target and retention policy analysis
- Meta-annotation usage (@Target, @Retention, @Documented)

### 2. Annotation Usage Patterns  
- Classes, methods, and fields using custom annotations
- Annotation attribute value analysis
- Composition patterns with other annotations
- Conditional usage and environment-specific annotations

### 3. Annotation Processing Integration
- Spring framework integration patterns
- Custom annotation processors and handlers
- AOP integration with custom annotations
- Validation and security annotation patterns

### 4. Annotation Quality Assessment
- Missing documentation and usage guidelines
- Inconsistent annotation usage patterns
- Potential conflicts with standard annotations
- Maintenance and evolution considerations

## Implementazione Javassist Completa

```java
public class CustomAnnotationsAnalyzer {
    
    public CustomAnnotationsReport analyzeCustomAnnotations(CtClass[] classes) {
        CustomAnnotationsReport report = new CustomAnnotationsReport();
        
        // 1. Identifica custom annotations
        List<CustomAnnotationDefinition> customAnnotations = identifyCustomAnnotations(classes, report);
        
        // 2. Analizza usage patterns
        analyzeAnnotationUsage(classes, customAnnotations, report);
        
        // 3. Analizza integration patterns
        analyzeIntegrationPatterns(classes, customAnnotations, report);
        
        // 4. Identifica problemi qualità
        identifyAnnotationQualityIssues(customAnnotations, report);
        
        return report;
    }
    
    private List<CustomAnnotationDefinition> identifyCustomAnnotations(CtClass[] classes, CustomAnnotationsReport report) {
        List<CustomAnnotationDefinition> customAnnotations = new ArrayList<>();
        
        for (CtClass ctClass : classes) {
            try {
                if (ctClass.isAnnotation() && isCustomAnnotation(ctClass)) {
                    CustomAnnotationDefinition annotationDef = analyzeCustomAnnotation(ctClass, report);
                    customAnnotations.add(annotationDef);
                }
            } catch (Exception e) {
                report.addError("Errore nell'analisi custom annotation: " + e.getMessage());
            }
        }
        
        return customAnnotations;
    }
    
    private boolean isCustomAnnotation(CtClass ctClass) {
        try {
            String packageName = ctClass.getPackageName();
            
            // Skip standard Java/Spring annotations
            if (packageName.startsWith("java.") || 
                packageName.startsWith("javax.") ||
                packageName.startsWith("jakarta.") ||
                packageName.startsWith("org.springframework.") ||
                packageName.startsWith("org.junit.") ||
                packageName.startsWith("org.mockito.")) {
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    private CustomAnnotationDefinition analyzeCustomAnnotation(CtClass ctClass, CustomAnnotationsReport report) {
        try {
            CustomAnnotationDefinition annotationDef = new CustomAnnotationDefinition();
            annotationDef.setAnnotationName(ctClass.getName());
            annotationDef.setSimpleName(ctClass.getSimpleName());
            annotationDef.setPackageName(ctClass.getPackageName());
            
            // Analizza meta-annotations
            analyzeMetaAnnotations(ctClass, annotationDef);
            
            // Analizza attributes
            analyzeAnnotationAttributes(ctClass, annotationDef);
            
            // Analizza documentation
            analyzeAnnotationDocumentation(ctClass, annotationDef);
            
            // Determina purpose e category
            determineAnnotationPurpose(ctClass, annotationDef);
            
            return annotationDef;
            
        } catch (Exception e) {
            CustomAnnotationDefinition errorAnnotation = new CustomAnnotationDefinition();
            errorAnnotation.setAnnotationName(ctClass.getName());
            errorAnnotation.addError("Errore nell'analisi custom annotation definition: " + e.getMessage());
            return errorAnnotation;
        }
    }
    
    private void analyzeMetaAnnotations(CtClass ctClass, CustomAnnotationDefinition annotationDef) {
        try {
            List<String> metaAnnotations = new ArrayList<>();
            
            // @Target analysis
            if (ctClass.hasAnnotation("java.lang.annotation.Target")) {
                Annotation targetAnnotation = ctClass.getAnnotation("java.lang.annotation.Target");
                String[] targets = extractTargetValues(targetAnnotation);
                annotationDef.setTargets(Arrays.asList(targets));
                metaAnnotations.add("@Target");
            }
            
            // @Retention analysis
            if (ctClass.hasAnnotation("java.lang.annotation.Retention")) {
                Annotation retentionAnnotation = ctClass.getAnnotation("java.lang.annotation.Retention");
                String retention = extractRetentionValue(retentionAnnotation);
                annotationDef.setRetention(retention);
                metaAnnotations.add("@Retention(" + retention + ")");
            }
            
            // @Documented
            if (ctClass.hasAnnotation("java.lang.annotation.Documented")) {
                annotationDef.setDocumented(true);
                metaAnnotations.add("@Documented");
            }
            
            // @Inherited
            if (ctClass.hasAnnotation("java.lang.annotation.Inherited")) {
                annotationDef.setInherited(true);
                metaAnnotations.add("@Inherited");
            }
            
            // Spring meta-annotations
            if (ctClass.hasAnnotation("org.springframework.stereotype.Component")) {
                metaAnnotations.add("@Component");
                annotationDef.setSpringStereotype(true);
            }
            
            annotationDef.setMetaAnnotations(metaAnnotations);
            
        } catch (Exception e) {
            annotationDef.addError("Errore nell'analisi meta-annotations: " + e.getMessage());
        }
    }
    
    private void analyzeAnnotationAttributes(CtClass ctClass, CustomAnnotationDefinition annotationDef) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            List<AnnotationAttribute> attributes = new ArrayList<>();
            
            for (CtMethod method : methods) {
                AnnotationAttribute attribute = new AnnotationAttribute();
                attribute.setAttributeName(method.getName());
                attribute.setAttributeType(method.getReturnType().getName());
                
                // Check for default value
                try {
                    Object defaultValue = method.getAnnotation("java.lang.annotation.Default");
                    if (defaultValue != null) {
                        attribute.setHasDefaultValue(true);
                        attribute.setDefaultValue(defaultValue.toString());
                    }
                } catch (Exception e) {
                    // No default value
                    attribute.setHasDefaultValue(false);
                }
                
                // Analyze attribute constraints
                analyzeAttributeConstraints(method, attribute);
                
                attributes.add(attribute);
            }
            
            annotationDef.setAttributes(attributes);
            
        } catch (Exception e) {
            annotationDef.addError("Errore nell'analisi annotation attributes: " + e.getMessage());
        }
    }
    
    private void determineAnnotationPurpose(CtClass ctClass, CustomAnnotationDefinition annotationDef) {
        try {
            String annotationName = ctClass.getSimpleName().toLowerCase();
            
            // Security-related
            if (annotationName.contains("secure") || annotationName.contains("auth") || 
                annotationName.contains("role") || annotationName.contains("permission")) {
                annotationDef.setPurpose(AnnotationPurpose.SECURITY);
            }
            // Validation-related
            else if (annotationName.contains("valid") || annotationName.contains("check") ||
                    annotationName.contains("constraint")) {
                annotationDef.setPurpose(AnnotationPurpose.VALIDATION);
            }
            // Configuration-related
            else if (annotationName.contains("config") || annotationName.contains("property") ||
                    annotationName.contains("enable")) {
                annotationDef.setPurpose(AnnotationPurpose.CONFIGURATION);
            }
            // AOP/Cross-cutting
            else if (annotationName.contains("log") || annotationName.contains("audit") ||
                    annotationName.contains("monitor") || annotationName.contains("trace")) {
                annotationDef.setPurpose(AnnotationPurpose.AOP_CROSSCUTTING);
            }
            // Business Logic
            else if (annotationName.contains("business") || annotationName.contains("service") ||
                    annotationName.contains("transactional")) {
                annotationDef.setPurpose(AnnotationPurpose.BUSINESS_LOGIC);
            }
            // Testing
            else if (annotationName.contains("test") || annotationName.contains("mock")) {
                annotationDef.setPurpose(AnnotationPurpose.TESTING);
            }
            else {
                annotationDef.setPurpose(AnnotationPurpose.OTHER);
            }
            
        } catch (Exception e) {
            annotationDef.setPurpose(AnnotationPurpose.OTHER);
        }
    }
    
    private void analyzeAnnotationUsage(CtClass[] classes, List<CustomAnnotationDefinition> customAnnotations, CustomAnnotationsReport report) {
        try {
            Map<String, CustomAnnotationDefinition> annotationMap = customAnnotations.stream()
                .collect(Collectors.toMap(CustomAnnotationDefinition::getAnnotationName, a -> a));
            
            for (CtClass ctClass : classes) {
                analyzeClassAnnotationUsage(ctClass, annotationMap, report);
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi annotation usage: " + e.getMessage());
        }
    }
    
    private void analyzeClassAnnotationUsage(CtClass ctClass, Map<String, CustomAnnotationDefinition> annotationMap, CustomAnnotationsReport report) {
        try {
            // Analizza class-level annotations
            Object[] classAnnotations = ctClass.getAnnotations();
            for (Object annotation : classAnnotations) {
                String annotationType = annotation.getClass().getInterfaces()[0].getName();
                
                if (annotationMap.containsKey(annotationType)) {
                    AnnotationUsage usage = new AnnotationUsage();
                    usage.setAnnotationType(annotationType);
                    usage.setUsageLocation(UsageLocation.CLASS);
                    usage.setTargetClassName(ctClass.getName());
                    usage.setTargetElement(ctClass.getSimpleName());
                    
                    analyzeAnnotationAttributes(annotation, usage);
                    report.addAnnotationUsage(usage);
                    
                    // Update usage statistics
                    annotationMap.get(annotationType).incrementUsageCount();
                }
            }
            
            // Analizza method-level annotations
            CtMethod[] methods = ctClass.getDeclaredMethods();
            for (CtMethod method : methods) {
                Object[] methodAnnotations = method.getAnnotations();
                for (Object annotation : methodAnnotations) {
                    String annotationType = annotation.getClass().getInterfaces()[0].getName();
                    
                    if (annotationMap.containsKey(annotationType)) {
                        AnnotationUsage usage = new AnnotationUsage();
                        usage.setAnnotationType(annotationType);
                        usage.setUsageLocation(UsageLocation.METHOD);
                        usage.setTargetClassName(ctClass.getName());
                        usage.setTargetElement(method.getName());
                        
                        analyzeAnnotationAttributes(annotation, usage);
                        report.addAnnotationUsage(usage);
                        
                        annotationMap.get(annotationType).incrementUsageCount();
                    }
                }
            }
            
            // Analizza field-level annotations  
            CtField[] fields = ctClass.getDeclaredFields();
            for (CtField field : fields) {
                Object[] fieldAnnotations = field.getAnnotations();
                for (Object annotation : fieldAnnotations) {
                    String annotationType = annotation.getClass().getInterfaces()[0].getName();
                    
                    if (annotationMap.containsKey(annotationType)) {
                        AnnotationUsage usage = new AnnotationUsage();
                        usage.setAnnotationType(annotationType);
                        usage.setUsageLocation(UsageLocation.FIELD);
                        usage.setTargetClassName(ctClass.getName());
                        usage.setTargetElement(field.getName());
                        
                        analyzeAnnotationAttributes(annotation, usage);
                        report.addAnnotationUsage(usage);
                        
                        annotationMap.get(annotationType).incrementUsageCount();
                    }
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi class annotation usage: " + e.getMessage());
        }
    }
    
    private void identifyAnnotationQualityIssues(List<CustomAnnotationDefinition> customAnnotations, CustomAnnotationsReport report) {
        try {
            for (CustomAnnotationDefinition annotation : customAnnotations) {
                
                // Missing documentation
                if (!annotation.isDocumented() || annotation.getDocumentation() == null) {
                    AnnotationIssue issue = new AnnotationIssue();
                    issue.setType(IssueType.MISSING_DOCUMENTATION);
                    issue.setAnnotationName(annotation.getAnnotationName());
                    issue.setSeverity(Severity.MEDIUM);
                    issue.setDescription("Custom annotation lacks proper documentation");
                    issue.setRecommendation("Add @Documented and provide comprehensive JavaDoc");
                    
                    report.addAnnotationIssue(issue);
                }
                
                // Unused annotation
                if (annotation.getUsageCount() == 0) {
                    AnnotationIssue issue = new AnnotationIssue();
                    issue.setType(IssueType.UNUSED_ANNOTATION);
                    issue.setAnnotationName(annotation.getAnnotationName());
                    issue.setSeverity(Severity.LOW);
                    issue.setDescription("Custom annotation is defined but never used");
                    issue.setRecommendation("Remove unused annotation or add usage examples");
                    
                    report.addAnnotationIssue(issue);
                }
                
                // Missing retention policy
                if (annotation.getRetention() == null) {
                    AnnotationIssue issue = new AnnotationIssue();
                    issue.setType(IssueType.MISSING_RETENTION_POLICY);
                    issue.setAnnotationName(annotation.getAnnotationName());
                    issue.setSeverity(Severity.MEDIUM);
                    issue.setDescription("Custom annotation lacks explicit retention policy");
                    issue.setRecommendation("Add @Retention annotation with appropriate policy");
                    
                    report.addAnnotationIssue(issue);
                }
                
                // Missing target specification
                if (annotation.getTargets() == null || annotation.getTargets().isEmpty()) {
                    AnnotationIssue issue = new AnnotationIssue();
                    issue.setType(IssueType.MISSING_TARGET_SPECIFICATION);
                    issue.setAnnotationName(annotation.getAnnotationName());
                    issue.setSeverity(Severity.MEDIUM);
                    issue.setDescription("Custom annotation lacks explicit target specification");
                    issue.setRecommendation("Add @Target annotation to specify valid usage locations");
                    
                    report.addAnnotationIssue(issue);
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'identificazione annotation quality issues: " + e.getMessage());
        }
    }
}

public class CustomAnnotationsReport {
    private List<CustomAnnotationDefinition> customAnnotationDefinitions = new ArrayList<>();
    private List<AnnotationUsage> annotationUsages = new ArrayList<>();
    private List<AnnotationIssue> annotationIssues = new ArrayList<>();
    private AnnotationStatistics annotationStatistics;
    private List<String> errors = new ArrayList<>();
    
    public static class CustomAnnotationDefinition {
        private String annotationName;
        private String simpleName;
        private String packageName;
        private List<String> targets = new ArrayList<>();
        private String retention;
        private boolean documented = false;
        private boolean inherited = false;
        private boolean springStereotype = false;
        private List<String> metaAnnotations = new ArrayList<>();
        private List<AnnotationAttribute> attributes = new ArrayList<>();
        private AnnotationPurpose purpose;
        private String documentation;
        private int usageCount = 0;
        private List<String> errors = new ArrayList<>();
    }
    
    public static class AnnotationUsage {
        private String annotationType;
        private UsageLocation usageLocation;
        private String targetClassName;
        private String targetElement;
        private Map<String, Object> attributeValues = new HashMap<>();
    }
    
    public static class AnnotationIssue {
        private IssueType type;
        private String annotationName;
        private String targetClass;
        private Severity severity;
        private String description;
        private String recommendation;
    }
    
    public enum AnnotationPurpose {
        SECURITY, VALIDATION, CONFIGURATION, AOP_CROSSCUTTING, 
        BUSINESS_LOGIC, TESTING, DOCUMENTATION, OTHER
    }
    
    public enum UsageLocation {
        CLASS, METHOD, FIELD, PARAMETER, CONSTRUCTOR
    }
    
    public enum IssueType {
        MISSING_DOCUMENTATION,
        UNUSED_ANNOTATION,
        MISSING_RETENTION_POLICY,
        MISSING_TARGET_SPECIFICATION,
        INCONSISTENT_USAGE_PATTERN,
        CONFLICTING_ANNOTATIONS
    }
    
    public enum Severity {
        CRITICAL, HIGH, MEDIUM, LOW
    }
}
```

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculateCustomAnnotationsQualityScore(CustomAnnotationsReport result) {
    double score = 100.0;
    
    // Penalizzazioni per problemi custom annotations
    score -= result.getUnusedAnnotations() * 8;                   // -8 per annotation inutilizzate
    score -= result.getMissingDocumentation() * 12;               // -12 per mancanza documentazione
    score -= result.getMissingRetentionPolicies() * 10;           // -10 per retention policy mancanti
    score -= result.getMissingTargetSpecifications() * 10;        // -10 per target specification mancanti
    score -= result.getInconsistentUsagePatterns() * 6;           // -6 per pattern usage inconsistenti
    score -= result.getConflictingAnnotations() * 15;             // -15 per annotation conflittuali
    score -= result.getPoorlyDesignedAnnotations() * 8;           // -8 per annotation mal progettate
    
    // Bonus per buone pratiche custom annotations
    score += result.getWellDocumentedAnnotations() * 3;           // +3 per annotation ben documentate
    score += result.getConsistentUsagePatterns() * 2;             // +2 per pattern usage consistenti
    score += result.getSpringIntegratedAnnotations() * 2;         // +2 per integrazione Spring appropriata
    score += result.getProperlyTargetedAnnotations() * 1;         // +1 per targeting appropriato
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - Custom annotations mal progettate con problemi significativi
- **41-60**: 🟡 SUFFICIENTE - Annotations funzionanti ma con lacune nella progettazione
- **61-80**: 🟢 BUONO - Buone custom annotations con alcuni miglioramenti necessari
- **81-100**: ⭐ ECCELLENTE - Custom annotations ben progettate e documentate

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -15)
1. **Annotation conflittuali**
   - Descrizione: Custom annotations che confliggono con standard annotations
   - Rischio: Behavior unpredictable, framework integration issues
   - Soluzione: Rivedere design per evitare conflitti con annotazioni standard

### 🟠 GRAVITÀ ALTA (Score Impact: -10 to -12)  
2. **Mancanza documentazione**
   - Descrizione: Custom annotations senza @Documented e JavaDoc
   - Rischio: Usage confusion, maintenance difficulties, poor adoption
   - Soluzione: Aggiungere @Documented e documentazione completa

3. **Retention policy mancanti**
   - Descrizione: Annotations senza @Retention esplicita
   - Rischio: Default retention behavior, potential runtime issues
   - Soluzione: Specificare @Retention appropriata per usage inteso

4. **Target specification mancanti**
   - Descrizione: Annotations senza @Target specification
   - Rischio: Usage inappropriato, compilation issues
   - Soluzione: Definire @Target per limitare usage a contesti appropriati

### 🟡 GRAVITÀ MEDIA (Score Impact: -6 to -8)
5. **Pattern usage inconsistenti**
   - Descrizione: Same annotation usata con pattern diversi in codebase
   - Rischio: Code confusion, maintenance overhead, poor readability
   - Soluzione: Standardizzare usage patterns attraverso team

6. **Annotation mal progettate**
   - Descrizione: Annotations con troppe responsabilità o attributes confusing
   - Rischio: Poor usability, maintenance issues, violation SRP
   - Soluzione: Refactoring per seguire single responsibility principle

### 🔵 GRAVITÀ BASSA (Score Impact: -8)
7. **Annotation inutilizzate**
   - Descrizione: Custom annotations definite ma mai utilizzate
   - Rischio: Code bloat, confusion nella codebase, maintenance overhead
   - Soluzione: Rimuovere annotations inutilizzate o aggiungere usage

## Metriche di Valore

- **Code Expressiveness**: Migliora expressiveness attraverso domain-specific annotations
- **Framework Integration**: Facilita integrazione con Spring e altri framework
- **Metadata Management**: Centralizza metadata management attraverso annotations
- **Development Productivity**: Accelera development attraverso reusable annotations

**Tags**: `#custom-annotations` `#metadata-analysis` `#annotation-processing` `#medium-value` `#medium-complexity`