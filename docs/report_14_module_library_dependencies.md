# Report 14: Dipendenze tra Moduli/Librerie

## Descrizione
Catalogazione completa delle dipendenze esterne (JAR di terze parti) con analisi delle versioni, licenze, e impatto sulla sicurezza dell'applicazione.

## Valore per l'Utente
**⭐⭐⭐⭐** - Alto Valore

## Complessità di Implementazione
**🟡 Media**

## Tempo di Realizzazione Stimato
**4-5 giorni**

## Sezioni del Report

### 1. External Dependencies Discovery
- JAR file analysis and metadata extraction
- Maven/Gradle dependency tree reconstruction
- Version identification and conflict detection
- Transitive dependency analysis

### 2. License Compliance Analysis
- License identification for each dependency
- License compatibility assessment
- Commercial license requirements
- GPL/LGPL compliance verification

### 3. Security Vulnerability Assessment
- Known vulnerability database lookup
- CVE identification and severity assessment
- Outdated dependency detection
- Security risk scoring

### 4. Dependency Health Evaluation
- Update availability analysis
- Maintenance status assessment
- Community activity evaluation
- Alternative dependency suggestions

## Implementazione Javassist Completa

```java
public class ModuleLibraryDependenciesAnalyzer {
    
    public DependencyAnalysisReport analyzeModuleLibraryDependencies(CtClass[] classes) {
        DependencyAnalysisReport report = new DependencyAnalysisReport();
        
        // 1. Scopri dipendenze esterne
        Set<ExternalDependency> dependencies = discoverExternalDependencies(classes, report);
        
        // 2. Analizza metadati e versioni
        analyzeDependencyMetadata(dependencies, report);
        
        // 3. Verifica licenze
        analyzeLicenseCompliance(dependencies, report);
        
        // 4. Valuta sicurezza
        assessSecurityVulnerabilities(dependencies, report);
        
        // 5. Identifica problemi di dipendenza
        identifyDependencyIssues(dependencies, report);
        
        return report;
    }
    
    private Set<ExternalDependency> discoverExternalDependencies(CtClass[] classes, DependencyAnalysisReport report) {
        Set<ExternalDependency> dependencies = new HashSet<>();
        Set<String> processedJars = new HashSet<>();
        
        for (CtClass ctClass : classes) {
            try {
                String className = ctClass.getName();
                
                // Skip classes from application packages
                if (isApplicationClass(className)) {
                    continue;
                }
                
                // Extract JAR information
                String jarPath = extractJarPath(ctClass);
                if (jarPath != null && !processedJars.contains(jarPath)) {
                    processedJars.add(jarPath);
                    
                    ExternalDependency dependency = analyzeDependencyJar(jarPath, report);
                    if (dependency != null) {
                        dependencies.add(dependency);
                    }
                }
                
                // Analyze import dependencies
                analyzeImportDependencies(ctClass, dependencies, report);
                
            } catch (Exception e) {
                report.addError("Errore nell'analisi dipendenze per classe " + ctClass.getName() + ": " + e.getMessage());
            }
        }
        
        return dependencies;
    }
    
    private ExternalDependency analyzeDependencyJar(String jarPath, DependencyAnalysisReport report) {
        try {
            ExternalDependency dependency = new ExternalDependency();
            dependency.setJarPath(jarPath);
            
            // Extract JAR name and potential version from path
            String jarName = extractJarName(jarPath);
            dependency.setArtifactId(extractArtifactId(jarName));
            dependency.setVersion(extractVersionFromJarName(jarName));
            
            // Try to read Maven metadata from JAR
            MavenMetadata mavenMetadata = extractMavenMetadata(jarPath);
            if (mavenMetadata != null) {
                dependency.setGroupId(mavenMetadata.getGroupId());
                dependency.setArtifactId(mavenMetadata.getArtifactId());
                dependency.setVersion(mavenMetadata.getVersion());
            }
            
            // Analyze JAR contents
            analyzeJarContents(jarPath, dependency, report);
            
            return dependency;
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi JAR " + jarPath + ": " + e.getMessage());
            return null;
        }
    }
    
    private void analyzeJarContents(String jarPath, ExternalDependency dependency, DependencyAnalysisReport report) {
        try {
            // Analyze manifest
            Manifest manifest = readJarManifest(jarPath);
            if (manifest != null) {
                extractManifestInfo(manifest, dependency);
            }
            
            // Count classes and packages
            JarStats stats = analyzeJarStatistics(jarPath);
            dependency.setClassCount(stats.getClassCount());
            dependency.setPackageCount(stats.getPackageCount());
            dependency.setJarSize(stats.getJarSize());
            
            // Detect framework type
            DependencyType dependencyType = detectDependencyType(jarPath, dependency);
            dependency.setDependencyType(dependencyType);
            
            // Extract license information
            String license = extractLicenseFromJar(jarPath);
            if (license != null) {
                dependency.setLicense(license);
            }
            
        } catch (Exception e) {
            dependency.addError("Errore nell'analisi contenuti JAR: " + e.getMessage());
        }
    }
    
    private DependencyType detectDependencyType(String jarPath, ExternalDependency dependency) {
        String artifactId = dependency.getArtifactId().toLowerCase();
        
        // Spring Framework
        if (artifactId.contains("spring")) {
            return DependencyType.SPRING_FRAMEWORK;
        }
        
        // Logging frameworks
        if (artifactId.contains("slf4j") || artifactId.contains("logback") || 
            artifactId.contains("log4j") || artifactId.contains("commons-logging")) {
            return DependencyType.LOGGING;
        }
        
        // Testing frameworks
        if (artifactId.contains("junit") || artifactId.contains("testng") || 
            artifactId.contains("mockito") || artifactId.contains("hamcrest")) {
            return DependencyType.TESTING;
        }
        
        // Database drivers
        if (artifactId.contains("postgresql") || artifactId.contains("mysql") || 
            artifactId.contains("oracle") || artifactId.contains("h2")) {
            return DependencyType.DATABASE_DRIVER;
        }
        
        // JSON/XML processing
        if (artifactId.contains("jackson") || artifactId.contains("gson") || 
            artifactId.contains("jaxb") || artifactId.contains("json")) {
            return DependencyType.SERIALIZATION;
        }
        
        // HTTP clients
        if (artifactId.contains("httpclient") || artifactId.contains("okhttp") || 
            artifactId.contains("resttemplate")) {
            return DependencyType.HTTP_CLIENT;
        }
        
        // Validation
        if (artifactId.contains("validation") || artifactId.contains("hibernate-validator")) {
            return DependencyType.VALIDATION;
        }
        
        // Security
        if (artifactId.contains("security") || artifactId.contains("oauth") || 
            artifactId.contains("jwt") || artifactId.contains("crypto")) {
            return DependencyType.SECURITY;
        }
        
        return DependencyType.OTHER;
    }
    
    private void analyzeLicenseCompliance(Set<ExternalDependency> dependencies, DependencyAnalysisReport report) {
        try {
            for (ExternalDependency dependency : dependencies) {
                LicenseAnalysis licenseAnalysis = analyzeDependencyLicense(dependency);
                dependency.setLicenseAnalysis(licenseAnalysis);
                
                // Check for problematic licenses
                if (licenseAnalysis.isProblematic()) {
                    DependencyIssue issue = new DependencyIssue();
                    issue.setType(IssueType.PROBLEMATIC_LICENSE);
                    issue.setDependencyId(dependency.getArtifactId());
                    issue.setVersion(dependency.getVersion());
                    issue.setSeverity(licenseAnalysis.getLicenseSeverity());
                    issue.setDescription("Dependency has problematic license: " + licenseAnalysis.getLicense());
                    issue.setRecommendation(licenseAnalysis.getRecommendation());
                    
                    report.addDependencyIssue(issue);
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi license compliance: " + e.getMessage());
        }
    }
    
    private LicenseAnalysis analyzeDependencyLicense(ExternalDependency dependency) {
        LicenseAnalysis analysis = new LicenseAnalysis();
        String license = dependency.getLicense();
        
        if (license == null || license.trim().isEmpty()) {
            analysis.setLicense("UNKNOWN");
            analysis.setProblematic(true);
            analysis.setLicenseSeverity(Severity.MEDIUM);
            analysis.setRecommendation("Investigate license terms before production use");
            return analysis;
        }
        
        license = license.toLowerCase();
        
        // GPL licenses - potentially problematic
        if (license.contains("gpl") && !license.contains("lgpl")) {
            analysis.setLicense(license);
            analysis.setProblematic(true);
            analysis.setLicenseSeverity(Severity.HIGH);
            analysis.setRecommendation("GPL license may require source code disclosure - review carefully");
        }
        // LGPL - usually acceptable
        else if (license.contains("lgpl")) {
            analysis.setLicense(license);
            analysis.setProblematic(false);
            analysis.setLicenseSeverity(Severity.LOW);
            analysis.setRecommendation("LGPL license is generally compatible with commercial use");
        }
        // Permissive licenses
        else if (license.contains("apache") || license.contains("mit") || 
                 license.contains("bsd") || license.contains("mozilla")) {
            analysis.setLicense(license);
            analysis.setProblematic(false);
            analysis.setLicenseSeverity(Severity.LOW);
            analysis.setRecommendation("Permissive license - generally safe for commercial use");
        }
        // Commercial/proprietary
        else if (license.contains("commercial") || license.contains("proprietary")) {
            analysis.setLicense(license);
            analysis.setProblematic(true);
            analysis.setLicenseSeverity(Severity.HIGH);
            analysis.setRecommendation("Commercial license - verify compliance and payment obligations");
        }
        else {
            analysis.setLicense(license);
            analysis.setProblematic(true);
            analysis.setLicenseSeverity(Severity.MEDIUM);
            analysis.setRecommendation("Unknown license type - manual review required");
        }
        
        return analysis;
    }
    
    private void assessSecurityVulnerabilities(Set<ExternalDependency> dependencies, DependencyAnalysisReport report) {
        try {
            for (ExternalDependency dependency : dependencies) {
                SecurityAssessment securityAssessment = assessDependencySecurity(dependency);
                dependency.setSecurityAssessment(securityAssessment);
                
                // Report high-severity vulnerabilities
                for (Vulnerability vuln : securityAssessment.getVulnerabilities()) {
                    if (vuln.getSeverity() == Severity.HIGH || vuln.getSeverity() == Severity.CRITICAL) {
                        DependencyIssue issue = new DependencyIssue();
                        issue.setType(IssueType.SECURITY_VULNERABILITY);
                        issue.setDependencyId(dependency.getArtifactId());
                        issue.setVersion(dependency.getVersion());
                        issue.setSeverity(vuln.getSeverity());
                        issue.setDescription("Security vulnerability: " + vuln.getCveId() + " - " + vuln.getDescription());
                        issue.setRecommendation("Update to version " + vuln.getFixedInVersion() + " or later");
                        
                        report.addDependencyIssue(issue);
                    }
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nella valutazione vulnerabilità sicurezza: " + e.getMessage());
        }
    }
    
    private SecurityAssessment assessDependencySecurity(ExternalDependency dependency) {
        SecurityAssessment assessment = new SecurityAssessment();
        
        // Check for known vulnerable versions (simplified - in real implementation would use vulnerability databases)
        List<Vulnerability> vulnerabilities = checkKnownVulnerabilities(dependency);
        assessment.setVulnerabilities(vulnerabilities);
        
        // Check if version is outdated
        boolean isOutdated = isVersionOutdated(dependency);
        assessment.setOutdated(isOutdated);
        
        if (isOutdated) {
            assessment.addRecommendation("Update to latest stable version");
        }
        
        // Calculate overall security risk
        SecurityRisk risk = calculateSecurityRisk(vulnerabilities, isOutdated);
        assessment.setSecurityRisk(risk);
        
        return assessment;
    }
    
    private void identifyDependencyIssues(Set<ExternalDependency> dependencies, DependencyAnalysisReport report) {
        try {
            // Check for version conflicts
            identifyVersionConflicts(dependencies, report);
            
            // Check for unused dependencies
            identifyUnusedDependencies(dependencies, report);
            
            // Check for duplicate functionality
            identifyDuplicateFunctionality(dependencies, report);
            
            // Check for maintenance issues
            identifyMaintenanceIssues(dependencies, report);
            
        } catch (Exception e) {
            report.addError("Errore nell'identificazione problemi dipendenze: " + e.getMessage());
        }
    }
    
    private void identifyVersionConflicts(Set<ExternalDependency> dependencies, DependencyAnalysisReport report) {
        Map<String, List<ExternalDependency>> groupedByArtifact = dependencies.stream()
            .filter(d -> d.getArtifactId() != null)
            .collect(Collectors.groupingBy(ExternalDependency::getArtifactId));
        
        for (Map.Entry<String, List<ExternalDependency>> entry : groupedByArtifact.entrySet()) {
            if (entry.getValue().size() > 1) {
                // Multiple versions of same artifact
                DependencyIssue issue = new DependencyIssue();
                issue.setType(IssueType.VERSION_CONFLICT);
                issue.setDependencyId(entry.getKey());
                issue.setSeverity(Severity.MEDIUM);
                
                List<String> versions = entry.getValue().stream()
                    .map(ExternalDependency::getVersion)
                    .distinct()
                    .collect(Collectors.toList());
                
                issue.setDescription("Multiple versions of " + entry.getKey() + " found: " + String.join(", ", versions));
                issue.setRecommendation("Resolve version conflict by using dependency management");
                
                report.addDependencyIssue(issue);
            }
        }
    }
}

public class DependencyAnalysisReport {
    private Set<ExternalDependency> externalDependencies = new HashSet<>();
    private List<DependencyIssue> dependencyIssues = new ArrayList<>();
    private DependencyStatistics statistics;
    private LicenseCompliance licenseCompliance;
    private SecuritySummary securitySummary;
    private List<String> errors = new ArrayList<>();
    
    public static class ExternalDependency {
        private String groupId;
        private String artifactId;
        private String version;
        private String jarPath;
        private String license;
        private DependencyType dependencyType;
        private int classCount;
        private int packageCount;
        private long jarSize;
        private LicenseAnalysis licenseAnalysis;
        private SecurityAssessment securityAssessment;
        private List<String> errors = new ArrayList<>();
    }
    
    public static class DependencyIssue {
        private IssueType type;
        private String dependencyId;
        private String version;
        private Severity severity;
        private String description;
        private String recommendation;
    }
    
    public static class Vulnerability {
        private String cveId;
        private String description;
        private Severity severity;
        private String fixedInVersion;
        private String referenceUrl;
    }
    
    public enum DependencyType {
        SPRING_FRAMEWORK, LOGGING, TESTING, DATABASE_DRIVER, 
        SERIALIZATION, HTTP_CLIENT, VALIDATION, SECURITY, OTHER
    }
    
    public enum IssueType {
        PROBLEMATIC_LICENSE,
        SECURITY_VULNERABILITY,
        VERSION_CONFLICT,
        UNUSED_DEPENDENCY,
        OUTDATED_VERSION,
        DUPLICATE_FUNCTIONALITY,
        MAINTENANCE_RISK
    }
    
    public enum SecurityRisk {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public enum Severity {
        CRITICAL, HIGH, MEDIUM, LOW
    }
}
```

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculateDependencyQualityScore(DependencyAnalysisReport result) {
    double score = 100.0;
    
    // Penalizzazioni per problemi dipendenze critici
    score -= result.getCriticalSecurityVulnerabilities() * 25;      // -25 per vulnerabilità critiche
    score -= result.getHighSecurityVulnerabilities() * 15;          // -15 per vulnerabilità ad alto rischio
    score -= result.getProblematicLicenses() * 20;                  // -20 per licenze problematiche
    score -= result.getVersionConflicts() * 12;                     // -12 per conflitti di versione
    score -= result.getOutdatedDependencies() * 8;                  // -8 per dipendenze obsolete
    score -= result.getUnusedDependencies() * 6;                    // -6 per dipendenze inutilizzate
    score -= result.getDuplicateFunctionality() * 10;               // -10 per funzionalità duplicate
    score -= result.getMaintenanceRisks() * 5;                      // -5 per rischi manutenzione
    
    // Bonus per buone pratiche gestione dipendenze
    score += result.getSecureDependencies() * 2;                    // +2 per dipendenze sicure
    score += result.getPermissiveLicenses() * 1;                    // +1 per licenze permissive
    score += result.getUpToDateDependencies() * 2;                  // +2 per dipendenze aggiornate
    score += result.getWellMaintainedDependencies() * 1;            // +1 per dipendenze ben mantenute
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - Gravi problemi di sicurezza e licensing nelle dipendenze
- **41-60**: 🟡 SUFFICIENTE - Gestione dipendenze di base con lacune significative
- **61-80**: 🟢 BUONO - Buona gestione dipendenze con alcuni miglioramenti necessari
- **81-100**: ⭐ ECCELLENTE - Gestione dipendenze ottimale e sicura

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -20 to -25)
1. **Vulnerabilità di sicurezza critiche**
   - Descrizione: Dipendenze con CVE critical o high severity
   - Rischio: Compromise sicurezza applicazione, data breach
   - Soluzione: Aggiornare immediatamente alle versioni sicure

2. **Licenze problematiche (GPL, commercial)**
   - Descrizione: Dipendenze con licenze GPL o commerciali non verificate
   - Rischio: Violazioni legali, obblighi disclosure source code
   - Soluzione: Sostituire con alternative permissive o verificare compliance

### 🟠 GRAVITÀ ALTA (Score Impact: -12 to -15)  
3. **Vulnerabilità di sicurezza ad alto rischio**
   - Descrizione: Dipendenze con vulnerabilità note ad alto impatto
   - Rischio: Potenziali security breach, exploit disponibili
   - Soluzione: Pianificare aggiornamento urgente alle versioni patched

4. **Conflitti di versione**
   - Descrizione: Multiple versioni stessa libreria nel classpath
   - Rischio: Runtime errors, comportamento imprevedibile, instabilità
   - Soluzione: Risolvere conflicts attraverso dependency management

### 🟡 GRAVITÀ MEDIA (Score Impact: -8 to -10)
5. **Dipendenze obsolete**
   - Descrizione: Librerie con versioni molto datate rispetto all'ultima
   - Rischio: Bug non risolti, mancanza security patches, technical debt
   - Soluzione: Pianificare aggiornamenti regolari delle dipendenze

6. **Funzionalità duplicate**
   - Descrizione: Multiple librerie che forniscono stessa funzionalità
   - Rischio: Bloat applicazione, confusion architetturale, conflicts
   - Soluzione: Standardizzare su una libreria per funzionalità

### 🔵 GRAVITÀ BASSA (Score Impact: -5 to -6)
7. **Dipendenze inutilizzate**
   - Descrizione: Librerie incluse ma non referenziate nel codice
   - Rischio: Aumento dimensioni artifact, surface attack aumentata
   - Soluzione: Rimuovere dipendenze non utilizzate dal build

8. **Rischi manutenzione**
   - Descrizione: Dipendenze con progetti non più attivamente mantenuti
   - Rischio: Nessun supporto futuro, vulnerabilità non patchate
   - Soluzione: Valutare alternative attivamente mantenute

## Metriche di Valore

- **Security Posture**: Riduce superficie di attacco attraverso gestione sicurezza dipendenze
- **Legal Compliance**: Assicura conformità licensing per uso commerciale
- **Technical Debt**: Previene accumulo debt da dipendenze obsolete
- **Maintenance Efficiency**: Semplifica gestione e aggiornamenti dipendenze

## Tags
`#module-dependencies` `#external-libraries` `#dependency-analysis` `#licensing` `#security-assessment` `#high-value` `#medium-complexity`