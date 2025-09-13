# Report 45: Configurazione SSL/TLS

**Valore**: ⭐⭐⭐⭐⭐ **Complessità**: 🔴 Complessa **Tempo**: 6-8 giorni
**Tags**: `#ssl-tls` `#security-configuration` `#certificates` `#encryption`

## Descrizione

Analizza la configurazione SSL/TLS dell'applicazione, includendo cipher suites, protocolli supportati, configurazioni dei certificati e potenziali vulnerabilità di sicurezza nelle comunicazioni crittografate.

## Sezioni del Report

### 1. SSL/TLS Configuration Overview
- Protocolli SSL/TLS abilitati
- Cipher suites configurate
- Configurazioni dei certificati
- Trust store e key store

### 2. Security Analysis
- Protocolli obsoleti o insicuri
- Cipher suites deboli
- Configurazioni certificate vulnerabili
- Validazione certificati

### 3. Best Practices Compliance
- Conformità agli standard di sicurezza
- Raccomandazioni OWASP
- Configurazioni consigliate
- Miglioramenti suggeriti

## Implementazione con Javassist

```java
public class SSLTLSConfigurationAnalyzer {
    
    private static final Set<String> INSECURE_PROTOCOLS = Set.of(
        "SSL", "SSLv2", "SSLv3", "TLSv1", "TLSv1.0", "TLSv1.1"
    );
    
    private static final Set<String> SECURE_PROTOCOLS = Set.of(
        "TLSv1.2", "TLSv1.3"
    );
    
    private static final Set<String> WEAK_CIPHER_SUITES = Set.of(
        "TLS_RSA_WITH_RC4_128_MD5",
        "TLS_RSA_WITH_RC4_128_SHA",
        "TLS_RSA_WITH_DES_CBC_SHA",
        "TLS_RSA_WITH_3DES_EDE_CBC_SHA",
        "TLS_DHE_RSA_WITH_DES_CBC_SHA",
        "TLS_DHE_DSS_WITH_DES_CBC_SHA",
        "SSL_RSA_WITH_RC4_128_MD5",
        "SSL_RSA_WITH_RC4_128_SHA",
        "SSL_RSA_WITH_3DES_EDE_CBC_SHA"
    );
    
    private static final Map<String, Integer> SSL_SEVERITY_PENALTIES = Map.of(
        "INSECURE_PROTOCOL", -40,           // Uso di protocolli SSL/TLS obsoleti
        "WEAK_CIPHER_SUITE", -35,           // Cipher suite deboli o vulnerabili
        "CERTIFICATE_VALIDATION_DISABLED", -50, // Validazione certificati disabilitata
        "CUSTOM_TRUST_ALL_MANAGER", -45,    // TrustManager che accetta tutti i certificati
        "HARDCODED_CERTIFICATES", -30,      // Certificati hardcoded nel codice
        "EXPIRED_CERTIFICATE", -25,         // Certificati scaduti
        "SELF_SIGNED_CERTIFICATE", -20,     // Certificati self-signed in produzione
        "WEAK_KEY_SIZE", -30,               // Chiavi di dimensione inadeguata (<2048 bit)
        "INSECURE_RANDOM", -25,             // Uso di SecureRandom inadeguato
        "SSL_CONTEXT_MISCONFIG", -20,       // Configurazione SSLContext inadeguata
        "MISSING_HOSTNAME_VERIFICATION", -35 // Mancanza verifica hostname
    );
    
    private static final Map<String, Integer> SSL_SECURITY_BONUSES = Map.of(
        "MODERN_TLS_ONLY", 25,              // Solo TLS 1.2+ abilitato
        "STRONG_CIPHER_SUITES", 20,         // Solo cipher suite sicure
        "CERTIFICATE_PINNING", 20,          // Implementazione certificate pinning
        "PROPER_CERTIFICATE_VALIDATION", 15, // Validazione certificati completa
        "HSTS_IMPLEMENTATION", 15,          // HTTP Strict Transport Security
        "PERFECT_FORWARD_SECRECY", 18,      // Perfect Forward Secrecy abilitato
        "OCSP_STAPLING", 12,                // OCSP stapling implementato
        "STRONG_KEY_EXCHANGE", 10,          // Key exchange algorithms sicuri
        "SECURE_RENEGOTIATION", 8,          // Secure renegotiation abilitata
        "SNI_SUPPORT", 6,                   // Server Name Indication support
        "CUSTOM_SECURITY_PROVIDER", 10      // Provider di sicurezza personalizzati
    );
    
    public SSLTLSConfigReport analyzeSSLTLSConfiguration(CtClass[] classes) {
        SSLTLSConfigReport report = new SSLTLSConfigReport();
        
        for (CtClass ctClass : classes) {
            analyzeSSLContextConfiguration(ctClass, report);
            analyzeSSLSocketFactoryConfiguration(ctClass, report);
            analyzeTrustManagerConfiguration(ctClass, report);
            analyzeCertificateConfiguration(ctClass, report);
            analyzeHostnameVerification(ctClass, report);
            analyzeSecurityProviders(ctClass, report);
        }
        
        evaluateSecurityCompliance(report);
        calculateSSLQualityScore(report);
        return report;
    }
    
    private void analyzeSSLContextConfiguration(CtClass ctClass, SSLTLSConfigReport report) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                // Cerca configurazioni SSLContext
                if (method.getMethodInfo().getCodeAttribute() != null) {
                    String methodBody = method.getMethodInfo().toString();
                    
                    if (methodBody.contains("SSLContext.getInstance")) {
                        extractSSLContextConfig(method, report);
                    }
                    
                    if (methodBody.contains("setEnabledProtocols")) {
                        extractProtocolConfiguration(method, report);
                    }
                    
                    if (methodBody.contains("setEnabledCipherSuites")) {
                        extractCipherSuiteConfiguration(method, report);
                    }
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi SSL Context: " + e.getMessage());
        }
    }
    
    private void analyzeSSLSocketFactoryConfiguration(CtClass ctClass, SSLTLSConfigReport report) {
        try {
            CtField[] fields = ctClass.getDeclaredFields();
            
            for (CtField field : fields) {
                if (field.getType().getName().contains("SSLSocketFactory")) {
                    analyzeSocketFactoryField(field, report);
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi SSL Socket Factory: " + e.getMessage());
        }
    }
    
    private void analyzeTrustManagerConfiguration(CtClass ctClass, SSLTLSConfigReport report) {
        try {
            // Cerca implementazioni custom di TrustManager
            CtClass[] interfaces = ctClass.getInterfaces();
            
            for (CtClass interfaceClass : interfaces) {
                if (interfaceClass.getName().equals("javax.net.ssl.X509TrustManager")) {
                    analyzeTrustManagerImplementation(ctClass, report);
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi Trust Manager: " + e.getMessage());
        }
    }
    
    private void analyzeCertificateConfiguration(CtClass ctClass, SSLTLSConfigReport report) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                String methodBody = method.getMethodInfo().toString();
                
                // Cerca configurazioni di certificati
                if (methodBody.contains("KeyStore") || 
                    methodBody.contains("TrustStore") ||
                    methodBody.contains("Certificate")) {
                    extractCertificateConfiguration(method, report);
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi certificati: " + e.getMessage());
        }
    }
    
    private void analyzeHostnameVerification(CtClass ctClass, SSLTLSConfigReport report) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                String methodBody = getMethodBody(method);
                
                // Cerca configurazioni hostname verification
                if (methodBody.contains("setHostnameVerifier") || 
                    methodBody.contains("HostnameVerifier")) {
                    analyzeHostnameVerifierConfiguration(method, report);
                }
                
                // Cerca disabilitazione hostname verification
                if (methodBody.contains("ALLOW_ALL_HOSTNAME_VERIFIER") ||
                    methodBody.contains("AllowAllHostnameVerifier")) {
                    report.addSecurityVulnerability(new SecurityVulnerability(
                        "MISSING_HOSTNAME_VERIFICATION",
                        "CRITICA",
                        "Hostname verification disabilitata",
                        "Implementare hostname verification appropriata"
                    ));
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi hostname verification: " + e.getMessage());
        }
    }
    
    private void analyzeSecurityProviders(CtClass ctClass, SSLTLSConfigReport report) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                String methodBody = getMethodBody(method);
                
                // Cerca configurazioni security providers
                if (methodBody.contains("Security.addProvider") || 
                    methodBody.contains("Security.insertProviderAt")) {
                    analyzeSecurityProviderConfiguration(method, report);
                }
                
                // Cerca uso di provider insicuri
                if (methodBody.contains("SunJCE") && methodBody.contains("RC4")) {
                    report.addSecurityVulnerability(new SecurityVulnerability(
                        "INSECURE_PROVIDER_CONFIG",
                        "ALTA",
                        "Configurazione provider di sicurezza potenzialmente insicura",
                        "Rivedere la configurazione dei provider di sicurezza"
                    ));
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi security providers: " + e.getMessage());
        }
    }
    
    private void evaluateSecurityCompliance(SSLTLSConfigReport report) {
        SSLSecurityEvaluation evaluation = new SSLSecurityEvaluation();
        
        // Analizza protocolli abilitati
        for (String protocol : report.getEnabledProtocols()) {
            if (INSECURE_PROTOCOLS.contains(protocol)) {
                evaluation.addInsecureProtocol(protocol);
                report.addSecurityVulnerability(new SecurityVulnerability(
                    "INSECURE_PROTOCOL",
                    "CRITICA",
                    "Protocollo insicuro abilitato: " + protocol,
                    "Disabilitare protocolli obsoleti e utilizzare solo TLS 1.2+"
                ));
            } else if (SECURE_PROTOCOLS.contains(protocol)) {
                evaluation.addSecureProtocol(protocol);
            }
        }
        
        // Analizza cipher suites
        for (String cipherSuite : report.getEnabledCipherSuites()) {
            if (WEAK_CIPHER_SUITES.contains(cipherSuite)) {
                evaluation.addWeakCipherSuite(cipherSuite);
                report.addSecurityVulnerability(new SecurityVulnerability(
                    "WEAK_CIPHER_SUITE",
                    "ALTA",
                    "Cipher suite debole: " + cipherSuite,
                    "Utilizzare solo cipher suite moderne e sicure"
                ));
            } else if (isStrongCipherSuite(cipherSuite)) {
                evaluation.addStrongCipherSuite(cipherSuite);
            }
        }
        
        // Analizza certificati
        for (CertificateConfig cert : report.getCertificates()) {
            if (cert.isSelfSigned()) {
                evaluation.addSelfSignedCertificate(cert);
                report.addSecurityVulnerability(new SecurityVulnerability(
                    "SELF_SIGNED_CERTIFICATE",
                    "MEDIA",
                    "Certificato self-signed: " + cert.getType(),
                    "Utilizzare certificati firmati da CA riconosciute"
                ));
            }
            
            if (cert.getExpirationDate() != null && 
                cert.getExpirationDate().before(new Date())) {
                evaluation.addExpiredCertificate(cert);
                report.addSecurityVulnerability(new SecurityVulnerability(
                    "EXPIRED_CERTIFICATE",
                    "ALTA",
                    "Certificato scaduto: " + cert.getType(),
                    "Rinnovare i certificati scaduti immediatamente"
                ));
            }
        }
        
        // Calcola score di compliance
        calculateComplianceScores(evaluation);
        report.setSslSecurityEvaluation(evaluation);
    }
    
    private void calculateSSLQualityScore(SSLTLSConfigReport report) {
        int baseScore = 100;
        int totalPenalties = 0;
        int totalBonuses = 0;
        
        List<QualityIssue> qualityIssues = new ArrayList<>();
        SSLSecurityEvaluation evaluation = report.getSslSecurityEvaluation();
        
        // Penalità per protocolli insicuri
        for (String insecureProtocol : evaluation.getInsecureProtocols()) {
            int penalty = SSL_SEVERITY_PENALTIES.getOrDefault("INSECURE_PROTOCOL", -40);
            totalPenalties += Math.abs(penalty);
            
            qualityIssues.add(new QualityIssue(
                "INSECURE_PROTOCOL",
                "CRITICA",
                "Protocollo insicuro abilitato: " + insecureProtocol,
                "Disabilitare " + insecureProtocol + " e utilizzare solo TLS 1.2+"
            ));
        }
        
        // Penalità per cipher suite deboli
        for (String weakCipher : evaluation.getWeakCipherSuites()) {
            int penalty = SSL_SEVERITY_PENALTIES.getOrDefault("WEAK_CIPHER_SUITE", -35);
            totalPenalties += Math.abs(penalty);
            
            qualityIssues.add(new QualityIssue(
                "WEAK_CIPHER_SUITE",
                "ALTA",
                "Cipher suite debole: " + weakCipher,
                "Rimuovere cipher suite obsolete e utilizzare solo algoritmi sicuri"
            ));
        }
        
        // Penalità per certificati self-signed
        if (!evaluation.getSelfSignedCertificates().isEmpty()) {
            totalPenalties += 20;
            qualityIssues.add(new QualityIssue(
                "SELF_SIGNED_CERTIFICATE",
                "MEDIA",
                String.format("%d certificati self-signed rilevati", 
                             evaluation.getSelfSignedCertificates().size()),
                "Utilizzare certificati firmati da CA riconosciute"
            ));
        }
        
        // Penalità per certificati scaduti
        if (!evaluation.getExpiredCertificates().isEmpty()) {
            totalPenalties += 25;
            qualityIssues.add(new QualityIssue(
                "EXPIRED_CERTIFICATE",
                "ALTA",
                String.format("%d certificati scaduti rilevati", 
                             evaluation.getExpiredCertificates().size()),
                "Rinnovare immediatamente tutti i certificati scaduti"
            ));
        }
        
        // Bonus per protocolli sicuri
        if (evaluation.getSecureProtocols().size() > 0 && 
            evaluation.getInsecureProtocols().isEmpty()) {
            totalBonuses += 25;
        }
        
        // Bonus per cipher suite sicure
        if (evaluation.getStrongCipherSuites().size() > 0 && 
            evaluation.getWeakCipherSuites().isEmpty()) {
            totalBonuses += 20;
        }
        
        // Bonus per certificate pinning
        if (evaluation.hasCertificatePinning()) {
            totalBonuses += 20;
        }
        
        // Bonus per HSTS
        if (evaluation.hasHSTSImplementation()) {
            totalBonuses += 15;
        }
        
        // Calcola score finale
        int finalScore = Math.max(0, Math.min(100, baseScore - totalPenalties + totalBonuses));
        
        SSLTLSQualityScore qualityScore = new SSLTLSQualityScore();
        qualityScore.setOverallScore(finalScore);
        qualityScore.setProtocolSecurityScore(calculateProtocolSecurityScore(evaluation));
        qualityScore.setCipherSuiteSecurityScore(calculateCipherSecurityScore(evaluation));
        qualityScore.setCertificateSecurityScore(calculateCertificateSecurityScore(evaluation));
        qualityScore.setComplianceScore(evaluation.getOverallComplianceScore());
        qualityScore.setQualityLevel(determineQualityLevel(finalScore));
        qualityScore.setQualityIssues(qualityIssues);
        qualityScore.setTotalPenalties(totalPenalties);
        qualityScore.setTotalBonuses(totalBonuses);
        
        report.setQualityScore(qualityScore);
    }
    
    // Helper methods
    private boolean isStrongCipherSuite(String cipherSuite) {
        return cipherSuite.contains("AES_256_GCM") || 
               cipherSuite.contains("CHACHA20_POLY1305") ||
               (cipherSuite.contains("AES_128_GCM") && cipherSuite.contains("ECDHE"));
    }
    
    private void calculateComplianceScores(SSLSecurityEvaluation evaluation) {
        int protocolScore = 100;
        int cipherScore = 100;
        int certScore = 100;
        
        // Protocol compliance
        if (!evaluation.getInsecureProtocols().isEmpty()) {
            protocolScore -= evaluation.getInsecureProtocols().size() * 30;
        }
        
        // Cipher compliance  
        if (!evaluation.getWeakCipherSuites().isEmpty()) {
            cipherScore -= evaluation.getWeakCipherSuites().size() * 20;
        }
        
        // Certificate compliance
        certScore -= evaluation.getSelfSignedCertificates().size() * 15;
        certScore -= evaluation.getExpiredCertificates().size() * 25;
        
        evaluation.setProtocolComplianceScore(Math.max(0, protocolScore));
        evaluation.setCipherComplianceScore(Math.max(0, cipherScore));
        evaluation.setCertificateComplianceScore(Math.max(0, certScore));
        evaluation.setOverallComplianceScore(
            (protocolScore + cipherScore + certScore) / 3
        );
    }
    
    private int calculateProtocolSecurityScore(SSLSecurityEvaluation evaluation) {
        if (evaluation.getInsecureProtocols().isEmpty() && 
            !evaluation.getSecureProtocols().isEmpty()) {
            return 100;
        } else if (evaluation.getInsecureProtocols().isEmpty()) {
            return 80;
        } else {
            return Math.max(0, 100 - evaluation.getInsecureProtocols().size() * 40);
        }
    }
    
    private int calculateCipherSecurityScore(SSLSecurityEvaluation evaluation) {
        if (evaluation.getWeakCipherSuites().isEmpty() && 
            !evaluation.getStrongCipherSuites().isEmpty()) {
            return 100;
        } else if (evaluation.getWeakCipherSuites().isEmpty()) {
            return 75;
        } else {
            return Math.max(0, 100 - evaluation.getWeakCipherSuites().size() * 30);
        }
    }
    
    private int calculateCertificateSecurityScore(SSLSecurityEvaluation evaluation) {
        int score = 100;
        score -= evaluation.getSelfSignedCertificates().size() * 15;
        score -= evaluation.getExpiredCertificates().size() * 30;
        return Math.max(0, score);
    }
    
    private String determineQualityLevel(int score) {
        if (score >= 95) return "ECCELLENTE";
        if (score >= 85) return "BUONO";
        if (score >= 70) return "DISCRETO";
        if (score >= 60) return "SUFFICIENTE";
        return "INSUFFICIENTE";
    }
}

public class SSLTLSConfigReport {
    private List<SSLContextConfig> sslContexts = new ArrayList<>();
    private List<String> enabledProtocols = new ArrayList<>();
    private List<String> enabledCipherSuites = new ArrayList<>();
    private List<CertificateConfig> certificates = new ArrayList<>();
    private List<SecurityVulnerability> vulnerabilities = new ArrayList<>();
    private SSLSecurityEvaluation sslSecurityEvaluation;
    private SSLTLSQualityScore qualityScore;
    private List<String> errors = new ArrayList<>();
    
    public static class SSLContextConfig {
        private String protocol;
        private String provider;
        private String keyManagerAlgorithm;
        private String trustManagerAlgorithm;
        private boolean customTrustManager;
    }
    
    public static class CertificateConfig {
        private String type;
        private String algorithm;
        private String keyStore;
        private String trustStore;
        private boolean selfSigned;
        private Date expirationDate;
    }
    
    public static class SecurityVulnerability {
        private String type;
        private String severity;
        private String description;
        private String recommendation;
        
        public SecurityVulnerability(String type, String severity, String description, String recommendation) {
            this.type = type;
            this.severity = severity;
            this.description = description;
            this.recommendation = recommendation;
        }
        
        // Getters and setters...
    }
    
    public static class SSLSecurityEvaluation {
        private List<String> insecureProtocols = new ArrayList<>();
        private List<String> secureProtocols = new ArrayList<>();
        private List<String> weakCipherSuites = new ArrayList<>();
        private List<String> strongCipherSuites = new ArrayList<>();
        private List<CertificateConfig> selfSignedCertificates = new ArrayList<>();
        private List<CertificateConfig> expiredCertificates = new ArrayList<>();
        private int protocolComplianceScore;
        private int cipherComplianceScore;
        private int certificateComplianceScore;
        private int overallComplianceScore;
        private boolean hasCertificatePinning;
        private boolean hasHSTSImplementation;
        private boolean hasPerfectForwardSecrecy;
        
        public void addInsecureProtocol(String protocol) {
            this.insecureProtocols.add(protocol);
        }
        
        public void addSecureProtocol(String protocol) {
            this.secureProtocols.add(protocol);
        }
        
        public void addWeakCipherSuite(String cipherSuite) {
            this.weakCipherSuites.add(cipherSuite);
        }
        
        public void addStrongCipherSuite(String cipherSuite) {
            this.strongCipherSuites.add(cipherSuite);
        }
        
        public void addSelfSignedCertificate(CertificateConfig cert) {
            this.selfSignedCertificates.add(cert);
        }
        
        public void addExpiredCertificate(CertificateConfig cert) {
            this.expiredCertificates.add(cert);
        }
        
        // Getters and setters...
    }
    
    public static class SSLTLSQualityScore {
        private int overallScore;
        private int protocolSecurityScore;
        private int cipherSuiteSecurityScore;
        private int certificateSecurityScore;
        private int complianceScore;
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
    public void addSecurityVulnerability(SecurityVulnerability vulnerability) {
        this.vulnerabilities.add(vulnerability);
    }
}
```

## Raccolta Dati

### 1. Configurazioni SSL Context
- Protocolli specificati in `SSLContext.getInstance()`
- Algoritmi di key manager e trust manager
- Provider di sicurezza utilizzati

### 2. Configurazioni Socket
- Protocolli abilitati tramite `setEnabledProtocols()`
- Cipher suites abilitate tramite `setEnabledCipherSuites()`
- Configurazioni di timeout e handshake

### 3. Gestione Certificati
- Configurazioni di KeyStore e TrustStore
- Implementazioni custom di TrustManager
- Validazioni dei certificati

### 4. Analisi di Sicurezza
- Identificazione di protocolli obsoleti (SSLv2, SSLv3, TLSv1.0)
- Rilevamento di cipher suites deboli
- Validazione delle configurazioni di certificati

## Metriche di Qualità del Codice

Il sistema di scoring valuta la sicurezza delle configurazioni SSL/TLS con focus critici su protocolli, cipher suites, certificati e compliance agli standard di sicurezza.

### Algoritmo di Scoring (0-100)

```java
baseScore = 100

// Penalità principali (-20 a -50 punti)
CERTIFICATE_VALIDATION_DISABLED: -50    // Validazione certificati disabilitata  
CUSTOM_TRUST_ALL_MANAGER: -45          // TrustManager che accetta tutti i certificati
INSECURE_PROTOCOL: -40                 // Uso protocolli SSL/TLS obsoleti
WEAK_CIPHER_SUITE: -35                 // Cipher suite deboli o vulnerabili
MISSING_HOSTNAME_VERIFICATION: -35      // Mancanza verifica hostname
HARDCODED_CERTIFICATES: -30            // Certificati hardcoded nel codice
WEAK_KEY_SIZE: -30                     // Chiavi di dimensione inadeguata (<2048 bit)

// Bonus principali (+6 a +25 punti)
MODERN_TLS_ONLY: +25                   // Solo TLS 1.2+ abilitato
STRONG_CIPHER_SUITES: +20              // Solo cipher suite sicure
CERTIFICATE_PINNING: +20               // Implementazione certificate pinning
PERFECT_FORWARD_SECRECY: +18           // Perfect Forward Secrecy abilitato
PROPER_CERTIFICATE_VALIDATION: +15     // Validazione certificati completa
HSTS_IMPLEMENTATION: +15               // HTTP Strict Transport Security

// Penalità per vulnerabilità specifiche
if (insecureProtocols > 0) penaltyPoints += 40 per protocol
if (weakCipherSuites > 0) penaltyPoints += 35 per cipher
if (expiredCertificates > 0) penaltyPoints += 25 per certificate
if (selfSignedCertificates > 0) penaltyPoints += 20 per certificate

// Bonus per compliance
if (onlySecureProtocols && onlyStrongCiphers) bonusPoints += 45
if (certificatePinning) bonusPoints += 20
if (hstsEnabled) bonusPoints += 15

finalScore = max(0, min(100, baseScore - totalPenalties + totalBonuses))
```

### Soglie di Valutazione

| Punteggio | Livello | Descrizione |
|-----------|---------|-------------|
| 95-100 | 🟢 **ECCELLENTE** | Configurazione SSL/TLS di livello enterprise con tutte le best practices |
| 85-94  | 🔵 **BUONO** | Configurazione sicura con protezioni avanzate implementate |
| 70-84  | 🟡 **DISCRETO** | Configurazione decente ma con alcune lacune di sicurezza |
| 60-69  | 🟠 **SUFFICIENTE** | Configurazione base con vulnerabilità significative |
| 0-59   | 🔴 **INSUFFICIENTE** | Configurazione insicura con vulnerabilità critiche |

### Categorie di Problemi per Gravità

#### 🔴 CRITICA (40+ punti penalità)
- **Certificate Validation Disabled** (-50): Validazione certificati completamente disabilitata
- **Trust All Manager** (-45): TrustManager personalizzato che accetta qualsiasi certificato
- **Insecure Protocols** (-40): Uso di SSL v2/v3, TLS 1.0/1.1 vulnerabili ad attacchi

#### 🟠 ALTA (25-39 punti penalità)
- **Weak Cipher Suites** (-35): Cipher suite vulnerabili (RC4, DES, 3DES)
- **Missing Hostname Verification** (-35): Hostname verification disabilitata
- **Hardcoded Certificates** (-30): Certificati embedded nel codice sorgente
- **Weak Key Size** (-30): Chiavi RSA < 2048 bit o equivalenti
- **Expired Certificates** (-25): Certificati scaduti in uso

#### 🟡 MEDIA (15-24 punti penalità)
- **Self-Signed Certificates** (-20): Certificati self-signed in ambiente produttivo
- **SSL Context Misconfiguration** (-20): Configurazione SSLContext inadeguata
- **Insecure Random** (-25): Uso di generatori random inadeguati per crittografia

#### 🔵 BASSA (< 15 punti penalità)
- **Missing SNI Support** (-6): Mancanza supporto Server Name Indication
- **Suboptimal Configuration** (-10): Configurazioni non ottimali ma non critiche

### Esempio Output HTML

```html
<div class="ssl-tls-quality-score">
    <h3>🔒 SSL/TLS Security Score: 78/100 (DISCRETO)</h3>
    
    <div class="score-breakdown">
        <div class="metric">
            <span class="label">Protocol Security:</span>
            <div class="bar security-medium"><div class="fill" style="width: 75%"></div></div>
            <span class="value">75%</span>
        </div>
        
        <div class="metric">
            <span class="label">Cipher Suite Security:</span>
            <div class="bar security-high"><div class="fill" style="width: 88%"></div></div>
            <span class="value">88%</span>
        </div>
        
        <div class="metric">
            <span class="label">Certificate Security:</span>
            <div class="bar security-low"><div class="fill" style="width: 65%"></div></div>
            <span class="value">65%</span>
        </div>
        
        <div class="metric">
            <span class="label">Compliance Score:</span>
            <div class="bar"><div class="fill" style="width: 73%"></div></div>
            <span class="value">73%</span>
        </div>
    </div>

    <div class="critical-issues">
        <h4>🚨 Vulnerabilità Critiche</h4>
        <div class="issue critical">
            🔴 <strong>INSECURE_PROTOCOL</strong>: TLS 1.0 abilitato
            <br>→ <em>Disabilitare immediatamente TLS 1.0 e utilizzare solo TLS 1.2+</em>
        </div>
        
        <div class="issue high">
            🟠 <strong>WEAK_CIPHER_SUITE</strong>: 3 cipher suite vulnerabili rilevate
            <br>→ <em>Rimuovere RC4 e DES cipher suite dalla configurazione</em>
        </div>
    </div>
    
    <div class="security-compliance">
        <h4>📋 Security Compliance</h4>
        <div class="compliance-item passed">
            ✅ <strong>Modern TLS</strong>: TLS 1.2 correttamente configurato
        </div>
        <div class="compliance-item failed">
            ❌ <strong>Legacy Protocols</strong>: TLS 1.0 ancora abilitato
        </div>
        <div class="compliance-item warning">
            ⚠️ <strong>Certificate Validation</strong>: Validazione parziale implementata
        </div>
        <div class="compliance-item passed">
            ✅ <strong>Strong Encryption</strong>: AES-256-GCM cipher suite utilizzata
        </div>
    </div>
    
    <div class="recommendations">
        <h4>🎯 Raccomandazioni Prioritarie</h4>
        <ol>
            <li>Disabilitare protocolli legacy (SSL v3, TLS 1.0/1.1)</li>
            <li>Implementare certificate pinning per applicazioni critiche</li>
            <li>Abilitare HSTS con max-age appropriato</li>
            <li>Configurare Perfect Forward Secrecy</li>
            <li>Implementare OCSP stapling per ottimizzare validazione certificati</li>
        </ol>
    </div>
</div>
```

### Metriche Business Value

#### 🛡️ Security Impact
- **Data Protection**: Protezione end-to-end delle comunicazioni sensibili
- **Compliance Achievement**: Conformità a PCI-DSS, HIPAA, GDPR e altri standard
- **Attack Surface Reduction**: Eliminazione di vettori di attacco comuni (downgrade, MITM)
- **Vulnerability Mitigation**: Protezione da exploit noti (POODLE, BEAST, CRIME, BREACH)

#### 🔍 Risk Assessment
- **Communication Security**: Valutazione sicurezza canali di comunicazione
- **Certificate Management**: Identificazione certificati scaduti o vulnerabili
- **Protocol Vulnerability**: Rilevamento protocolli obsoleti e vulnerabili
- **Cipher Strength Analysis**: Valutazione forza crittografica algoritmi utilizzati

#### 💰 Operational Benefits
- **Incident Prevention**: Prevenzione proattiva di security incident
- **Audit Readiness**: Preparazione per audit di sicurezza e compliance
- **Trust Enhancement**: Miglioramento della fiducia utenti e partner
- **Regulatory Compliance**: Riduzione rischi di sanzioni regulatory

#### 👥 Development Impact
- **Security Awareness**: Educazione team su best practices SSL/TLS
- **Configuration Standards**: Standardizzazione configurazioni sicure
- **Monitoring Integration**: Integrazione con sistemi di security monitoring
- **Documentation Improvement**: Documentazione configurazioni e procedure

### Raccomandazioni Prioritarie

1. **Eliminate Legacy Protocols**: Disabilitare SSL v2/v3 e TLS 1.0/1.1 immediatamente
2. **Implement Certificate Pinning**: Implementare certificate pinning per applicazioni critiche
3. **Enable HSTS**: Configurare HTTP Strict Transport Security con max-age appropriato
4. **Configure Perfect Forward Secrecy**: Utilizzare cipher suite con PFS (ECDHE, DHE)
5. **Certificate Lifecycle Management**: Implementare monitoring automatico scadenza certificati

## Metriche di Valore

- **Sicurezza**: Identifica vulnerabilità critiche nelle comunicazioni crittografate
- **Compliance**: Verifica conformità agli standard di sicurezza
- **Risk Assessment**: Valuta i rischi associati alle configurazioni SSL/TLS
- **Best Practices**: Fornisce raccomandazioni per migliorare la sicurezza

## Classificazione

**Categoria**: Security & Robustness
**Priorità**: Critica - Le configurazioni SSL/TLS influenzano direttamente la sicurezza delle comunicazioni
**Stakeholder**: Security team, DevOps, System administrators