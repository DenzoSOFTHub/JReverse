# Report 44: Analisi di Dipendenze Vulnerabili (CVE)

## Descrizione
Scansione completa delle dipendenze esterne per identificare vulnerabilità note (CVE), versioni obsolete, e raccomandazioni di aggiornamento per migliorare la sicurezza dell'applicazione.

## Valore per l'Utente
**⭐⭐⭐⭐⭐** - Valore Massimo
- Identificazione di vulnerabilità di sicurezza critiche
- Conformità con standard di sicurezza
- Prioritizzazione degli aggiornamenti di sicurezza
- Audit di sicurezza per compliance
- Riduzione del rischio di attacchi

## Complessità di Implementazione
**🔴 Complessa** - Richiede integrazione con database CVE e analisi dipendenze

## Tempo di Realizzazione Stimato
**8-10 giorni** di sviluppo

## Implementazione Javassist

```java
public class VulnerabilityAnalyzer {
    
    private final CveDatabase cveDatabase;
    private final DependencyResolver dependencyResolver;
    
    public VulnerabilityAnalysis analyzeVulnerableDependencies() {
        // 1. Estrai tutte le dipendenze dal classpath
        List<DependencyInfo> dependencies = extractDependencies();
        
        // 2. Analizza ogni dipendenza per vulnerabilità
        List<VulnerabilityInfo> vulnerabilities = scanForVulnerabilities(dependencies);
        
        // 3. Calcola risk score
        RiskAssessment riskAssessment = calculateRiskScore(vulnerabilities);
        
        // 4. Genera raccomandazioni
        List<SecurityRecommendation> recommendations = generateRecommendations(vulnerabilities);
        
        return new VulnerabilityAnalysis(dependencies, vulnerabilities, riskAssessment, recommendations);
    }
    
    private List<DependencyInfo> extractDependencies() {
        List<DependencyInfo> dependencies = new ArrayList<>();
        
        // Analizza il classpath per identificare JAR files
        String classpath = System.getProperty("java.class.path");
        String[] classpathEntries = classpath.split(File.pathSeparator);
        
        for (String entry : classpathEntries) {
            if (entry.endsWith(".jar")) {
                DependencyInfo depInfo = analyzeJarDependency(entry);
                if (depInfo != null) {
                    dependencies.add(depInfo);
                }
            }
        }
        
        return dependencies;
    }
    
    private DependencyInfo analyzeJarDependency(String jarPath) {
        try {
            JarFile jarFile = new JarFile(jarPath);
            Manifest manifest = jarFile.getManifest();
            
            if (manifest != null) {
                Attributes mainAttributes = manifest.getMainAttributes();
                
                DependencyInfo info = new DependencyInfo();
                info.setJarPath(jarPath);
                info.setFileName(Paths.get(jarPath).getFileName().toString());
                
                // Estrai informazioni dal manifest
                info.setImplementationTitle(mainAttributes.getValue("Implementation-Title"));
                info.setImplementationVersion(mainAttributes.getValue("Implementation-Version"));
                info.setImplementationVendor(mainAttributes.getValue("Implementation-Vendor"));
                
                // Cerca di determinare groupId e artifactId dal JAR name o manifest
                parseArtifactInfo(info);
                
                return info;
            }
            
        } catch (IOException e) {
            logger.warn("Error analyzing JAR: {}", jarPath, e);
        }
        
        return null;
    }
    
    private List<VulnerabilityInfo> scanForVulnerabilities(List<DependencyInfo> dependencies) {
        List<VulnerabilityInfo> vulnerabilities = new ArrayList<>();
        
        for (DependencyInfo dependency : dependencies) {
            List<CveEntry> cveEntries = cveDatabase.searchVulnerabilities(
                dependency.getGroupId(),
                dependency.getArtifactId(),
                dependency.getVersion()
            );
            
            for (CveEntry cve : cveEntries) {
                VulnerabilityInfo vulnInfo = new VulnerabilityInfo();
                vulnInfo.setDependency(dependency);
                vulnInfo.setCveId(cve.getCveId());
                vulnInfo.setDescription(cve.getDescription());
                vulnInfo.setSeverity(cve.getSeverity());
                vulnInfo.setCvssScore(cve.getCvssScore());
                vulnInfo.setPublishedDate(cve.getPublishedDate());
                vulnInfo.setLastModifiedDate(cve.getLastModifiedDate());
                vulnInfo.setReferences(cve.getReferences());
                
                // Verifica se la versione corrente è affetta
                if (isVersionAffected(dependency.getVersion(), cve.getAffectedVersionRanges())) {
                    vulnInfo.setAffected(true);
                    vulnerabilities.add(vulnInfo);
                }
            }
        }
        
        return vulnerabilities;
    }
    
    private boolean isVersionAffected(String currentVersion, List<VersionRange> affectedRanges) {
        if (currentVersion == null || affectedRanges.isEmpty()) {
            return false;
        }
        
        Version version = Version.valueOf(currentVersion);
        
        return affectedRanges.stream()
            .anyMatch(range -> range.contains(version));
    }
    
    private RiskAssessment calculateRiskScore(List<VulnerabilityInfo> vulnerabilities) {
        RiskAssessment assessment = new RiskAssessment();
        
        int criticalCount = 0;
        int highCount = 0;
        int mediumCount = 0;
        int lowCount = 0;
        
        double totalCvssScore = 0.0;
        
        for (VulnerabilityInfo vuln : vulnerabilities) {
            if (vuln.isAffected()) {
                switch (vuln.getSeverity()) {
                    case CRITICAL:
                        criticalCount++;
                        break;
                    case HIGH:
                        highCount++;
                        break;
                    case MEDIUM:
                        mediumCount++;
                        break;
                    case LOW:
                        lowCount++;
                        break;
                }
                
                totalCvssScore += vuln.getCvssScore();
            }
        }
        
        assessment.setCriticalVulnerabilities(criticalCount);
        assessment.setHighVulnerabilities(highCount);
        assessment.setMediumVulnerabilities(mediumCount);
        assessment.setLowVulnerabilities(lowCount);
        
        double averageCvssScore = vulnerabilities.isEmpty() ? 0.0 : totalCvssScore / vulnerabilities.size();
        assessment.setAverageCvssScore(averageCvssScore);
        
        // Calcola overall risk level
        RiskLevel riskLevel;
        if (criticalCount > 0) {
            riskLevel = RiskLevel.CRITICAL;
        } else if (highCount > 3) {
            riskLevel = RiskLevel.HIGH;
        } else if (highCount > 0 || mediumCount > 5) {
            riskLevel = RiskLevel.MEDIUM;
        } else {
            riskLevel = RiskLevel.LOW;
        }
        
        assessment.setOverallRiskLevel(riskLevel);
        
        return assessment;
    }
    
    private List<SecurityRecommendation> generateRecommendations(List<VulnerabilityInfo> vulnerabilities) {
        List<SecurityRecommendation> recommendations = new ArrayList<>();
        
        // Raggruppa vulnerabilità per dipendenza
        Map<DependencyInfo, List<VulnerabilityInfo>> vulnsByDependency = vulnerabilities.stream()
            .collect(Collectors.groupingBy(VulnerabilityInfo::getDependency));
        
        for (Map.Entry<DependencyInfo, List<VulnerabilityInfo>> entry : vulnsByDependency.entrySet()) {
            DependencyInfo dependency = entry.getKey();
            List<VulnerabilityInfo> depVulns = entry.getValue();
            
            SecurityRecommendation recommendation = createUpgradeRecommendation(dependency, depVulns);
            recommendations.add(recommendation);
        }
        
        // Ordina per priorità
        recommendations.sort(Comparator.comparing(SecurityRecommendation::getPriority).reversed());
        
        return recommendations;
    }
    
    private SecurityRecommendation createUpgradeRecommendation(DependencyInfo dependency, List<VulnerabilityInfo> vulnerabilities) {
        SecurityRecommendation recommendation = new SecurityRecommendation();
        recommendation.setDependency(dependency);
        recommendation.setVulnerabilities(vulnerabilities);
        recommendation.setRecommendationType(RecommendationType.UPGRADE);
        
        // Trova la versione minima sicura
        String safeVersion = findSafeVersion(dependency, vulnerabilities);
        recommendation.setRecommendedVersion(safeVersion);
        
        // Calcola priorità basata sulla severity
        int priority = calculateRecommendationPriority(vulnerabilities);
        recommendation.setPriority(priority);
        
        // Genera descrizione
        String description = generateRecommendationDescription(dependency, vulnerabilities, safeVersion);
        recommendation.setDescription(description);
        
        return recommendation;
    }
}
```

## Struttura Output Report

```json
{
  "summary": {
    "totalDependencies": 156,
    "vulnerableDependencies": 12,
    "totalVulnerabilities": 23,
    "riskAssessment": {
      "overallRiskLevel": "HIGH",
      "criticalVulnerabilities": 2,
      "highVulnerabilities": 8,
      "mediumVulnerabilities": 11,
      "lowVulnerabilities": 2,
      "averageCvssScore": 7.2
    }
  },
  "vulnerableDependencies": [
    {
      "dependency": {
        "groupId": "org.springframework",
        "artifactId": "spring-core",
        "version": "5.2.8.RELEASE",
        "fileName": "spring-core-5.2.8.RELEASE.jar"
      },
      "vulnerabilities": [
        {
          "cveId": "CVE-2022-22965",
          "description": "Spring Framework RCE via Data Binding on JDK 9+",
          "severity": "CRITICAL",
          "cvssScore": 9.8,
          "publishedDate": "2022-03-31",
          "affected": true,
          "references": [
            "https://nvd.nist.gov/vuln/detail/CVE-2022-22965"
          ]
        }
      ]
    }
  ],
  "recommendations": [
    {
      "priority": 10,
      "dependency": "spring-core:5.2.8.RELEASE",
      "recommendationType": "UPGRADE",
      "recommendedVersion": "5.3.21",
      "description": "Critical vulnerability CVE-2022-22965 affects this version. Upgrade to 5.3.21 or later immediately.",
      "vulnerabilitiesFixed": 1,
      "estimatedEffort": "LOW"
    }
  ],
  "complianceStatus": {
    "owasp": "NON_COMPLIANT",
    "nist": "NON_COMPLIANT",
    "issues": [
      "Critical vulnerabilities present",
      "Dependencies not regularly updated"
    ]
  }
}
```

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculateVulnerabilityQualityScore(VulnerabilityReport result) {
    double score = 100.0;
    
    // Penalizzazioni per vulnerabilità critiche
    score -= result.getCriticalVulnerabilities() * 30;        // -30 per vulnerabilità critica
    score -= result.getHighVulnerabilities() * 20;            // -20 per vulnerabilità alta
    score -= result.getMediumVulnerabilities() * 10;          // -10 per vulnerabilità media
    score -= result.getLowVulnerabilities() * 3;              // -3 per vulnerabilità bassa
    score -= result.getOutdatedDependencies() * 5;            // -5 per dipendenza obsoleta
    score -= result.getUnpatchedVulnerabilities() * 15;       // -15 per vulnerabilità non patchate
    score -= result.getTransitiveDependencyIssues() * 8;      // -8 per problemi dipendenze transitive
    
    // Bonus per buone pratiche di sicurezza
    score += result.getUpToDateDependencies() * 1;            // +1 per dipendenza aggiornata
    score += result.getProactiveSecurityUpdates() * 2;        // +2 per aggiornamenti proattivi
    score += result.getSecurityScanningAutomation() * 3;      // +3 per scanning automatizzato
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - Gravi vulnerabilità di sicurezza che espongono l'applicazione
- **41-60**: 🟡 SUFFICIENTE - Alcune vulnerabilità presenti, aggiornamenti necessari
- **61-80**: 🟢 BUONO - Dipendenze prevalentemente sicure con minor maintenance
- **81-100**: ⭐ ECCELLENTE - Dependency management sicuro e proattivo

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -20 to -30)
1. **Vulnerabilità CVE Critical (CVSS 9.0-10.0)**
   - Descrizione: Vulnerabilità con score CVSS critico che permettono RCE, privilege escalation
   - Rischio: Compromissione completa dell'applicazione e dei dati
   - Soluzione: Upgrade immediato alla versione sicura o applicare patch

2. **Vulnerabilità non patchate da >30 giorni**
   - Descrizione: CVE noti con fix disponibili ma non applicati
   - Rischio: Finestra di attacco prolungata, exploit pubblici disponibili
   - Soluzione: Pianificare upgrade urgente o implementare workaround

### 🟠 GRAVITÀ ALTA (Score Impact: -15 to -20)
3. **Vulnerabilità High Severity (CVSS 7.0-8.9)**
   - Descrizione: Vulnerabilità ad alto impatto che compromettono confidenzialità/integrità
   - Rischio: Data breach, unauthorized access, denial of service
   - Soluzione: Aggiornare entro 7-14 giorni

4. **Dipendenze End-of-Life**
   - Descrizione: Librerie non più mantenute o supportate
   - Rischio: Nessun fix per future vulnerabilità
   - Soluzione: Migrare a alternative mantenute attivamente

### 🟡 GRAVITÀ MEDIA (Score Impact: -8 to -10)
5. **Vulnerabilità Medium Severity (CVSS 4.0-6.9)**
   - Descrizione: Vulnerabilità con impatto moderato
   - Rischio: Information disclosure, availability issues
   - Soluzione: Aggiornare nel prossimo ciclo di maintenance

6. **Dipendenze transitive vulnerabili**
   - Descrizione: Vulnerabilità in librerie di secondo/terzo livello
   - Rischio: Supply chain attacks, vulnerabilità nascoste
   - Soluzione: Upgrade dependency parent o override versione

### 🔵 GRAVITÀ BASSA (Score Impact: -1 to -5)
7. **Dipendenze obsolete (>2 anni)**
   - Descrizione: Versioni vecchie ma senza CVE noti
   - Rischio: Technical debt, compatibilità futura
   - Soluzione: Pianificare upgrade graduale

8. **Vulnerabilità Low Severity (CVSS 0.1-3.9)**
   - Descrizione: Vulnerabilità a basso impatto
   - Rischio: Minor information disclosure
   - Soluzione: Aggiornare quando conveniente

## Tags per Classificazione
`#vulnerability-scanning` `#cve-analysis` `#security-assessment` `#dependency-security` `#compliance` `#high-value` `#complex`