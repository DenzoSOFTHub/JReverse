# Report 41: Configurazioni Spring Security

## Descrizione
Analisi completa delle configurazioni Spring Security incluse authentication providers, authorization rules, security filters, session management, e configurazioni CORS/CSRF.

## Valore per l'Utente
**⭐⭐⭐⭐⭐** - Valore Massimo
- Audit di sicurezza completo
- Identificazione di vulnerabilità di configurazione
- Comprensione dell'architettura di sicurezza
- Compliance con standard di sicurezza
- Base per penetration testing

## Complessità di Implementazione
**🔴 Complessa** - Richiede analisi avanzata di Spring Security

## Tempo di Realizzazione Stimato
**10-12 giorni** di sviluppo

## Implementazione Javassist

```java
public class SpringSecurityAnalyzer {
    
    public SecurityAnalysis analyzeSpringSecurityConfiguration() {
        List<SecurityConfigurationInfo> configs = findSecurityConfigurations();
        Map<String, SecurityRuleInfo> authorizationRules = analyzeAuthorizationRules();
        List<AuthenticationProviderInfo> authProviders = findAuthenticationProviders();
        SessionManagementInfo sessionConfig = analyzeSessionManagement();
        CorsConfigurationInfo corsConfig = analyzeCorsConfiguration();
        CsrfConfigurationInfo csrfConfig = analyzeCsrfConfiguration();
        
        return new SecurityAnalysis(configs, authorizationRules, authProviders, 
                                  sessionConfig, corsConfig, csrfConfig);
    }
    
    private List<SecurityConfigurationInfo> findSecurityConfigurations() {
        return classPool.getAllClasses().stream()
            .filter(this::isSecurityConfiguration)
            .map(this::analyzeSecurityConfiguration)
            .collect(Collectors.toList());
    }
    
    private boolean isSecurityConfiguration(CtClass clazz) {
        return clazz.hasAnnotation("org.springframework.security.config.annotation.web.configuration.EnableWebSecurity") ||
               extendsClass(clazz, "org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter") ||
               implementsInterface(clazz, "org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurer");
    }
    
    private SecurityConfigurationInfo analyzeSecurityConfiguration(CtClass configClass) {
        SecurityConfigurationInfo info = new SecurityConfigurationInfo();
        info.setClassName(configClass.getName());
        
        // Analizza HttpSecurity configuration
        analyzeHttpSecurityConfiguration(configClass, info);
        
        // Analizza AuthenticationManager configuration
        analyzeAuthenticationManagerConfiguration(configClass, info);
        
        // Analizza Password Encoder configuration
        analyzePasswordEncoderConfiguration(configClass, info);
        
        return info;
    }
    
    private void analyzeHttpSecurityConfiguration(CtClass configClass, SecurityConfigurationInfo info) {
        try {
            // Cerca il metodo configure(HttpSecurity http)
            CtMethod configureMethod = findHttpSecurityConfigureMethod(configClass);
            if (configureMethod != null) {
                analyzeHttpSecurityMethodBody(configureMethod, info);
            }
        } catch (Exception e) {
            logger.warn("Error analyzing HttpSecurity configuration", e);
        }
    }
    
    private void analyzeHttpSecurityMethodBody(CtMethod method, SecurityConfigurationInfo info) {
        try {
            method.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall call) throws CannotCompileException {
                    String methodName = call.getMethodName();
                    String className = call.getClassName();
                    
                    if (className.contains("HttpSecurity")) {
                        analyzeHttpSecurityMethodCall(call, methodName, info);
                    }
                }
            });
        } catch (CannotCompileException e) {
            logger.error("Error instrumenting HttpSecurity configure method", e);
        }
    }
    
    private void analyzeHttpSecurityMethodCall(MethodCall call, String methodName, SecurityConfigurationInfo info) {
        switch (methodName) {
            case "authorizeRequests":
            case "authorizeHttpRequests":
                info.setHasAuthorizationRules(true);
                break;
            case "formLogin":
                info.setFormLoginEnabled(true);
                break;
            case "httpBasic":
                info.setBasicAuthEnabled(true);
                break;
            case "oauth2Login":
                info.setOauth2LoginEnabled(true);
                break;
            case "jwt":
                info.setJwtEnabled(true);
                break;
            case "sessionManagement":
                info.setHasSessionManagement(true);
                break;
            case "cors":
                info.setCorsEnabled(true);
                break;
            case "csrf":
                info.setCsrfConfigured(true);
                break;
            case "headers":
                info.setHeaderSecurityConfigured(true);
                break;
        }
    }
}
```

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculateSpringSecurityQualityScore(SecurityConfigurationReport result) {
    double score = 100.0;
    
    // Penalizzazioni per problemi critici di sicurezza
    score -= result.getNoAuthenticationEndpoints() * 25;      // -25 per endpoint senza autenticazione
    score -= result.getWeakPasswordPolicies() * 20;          // -20 per policy password deboli
    score -= result.getMissingCsrfProtection() * 18;         // -18 per mancanza CSRF protection
    score -= result.getInsecureSessionManagement() * 15;     // -15 per gestione sessioni insicura
    score -= result.getMissingSecurityHeaders() * 12;        // -12 per header sicurezza mancanti
    score -= result.getImproperAuthorization() * 10;         // -10 per autorizzazione inappropriata
    score -= result.getUnencryptedCommunication() * 8;       // -8 per comunicazione non crittografata
    score -= result.getVerboseErrorMessages() * 5;           // -5 per messaggi errore troppo dettagliati
    
    // Bonus per best practices di sicurezza
    score += result.getMultiFactorAuthentication() * 5;      // +5 per MFA implementata
    score += result.getProperRoleBasedAccess() * 3;          // +3 per RBAC corretto
    score += result.getSecureSessionConfig() * 2;            // +2 per configurazione sessioni sicura
    score += result.getSecurityHeadersPresent() * 1;         // +1 per header sicurezza presenti
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - Gravi vulnerabilità di sicurezza che espongono l'applicazione
- **41-60**: 🟡 SUFFICIENTE - Sicurezza di base ma con lacune significative
- **61-80**: 🟢 BUONO - Configurazione sicura con alcuni miglioramenti possibili
- **81-100**: ⭐ ECCELLENTE - Configurazione di sicurezza robusta e completa

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -18 to -25)
1. **Endpoint senza autenticazione**
   - Descrizione: Endpoint sensibili accessibili senza autenticazione
   - Rischio: Accesso non autorizzato a dati e funzionalità critiche
   - Soluzione: Configurare `http.authorizeHttpRequests()` con regole appropriate

2. **Policy password deboli**
   - Descrizione: Mancanza di requisiti minimi per password (lunghezza, complessità)
   - Rischio: Account facilmente compromessi attraverso attacchi brute force
   - Soluzione: Implementare `PasswordEncoder` forte e policy di complessità

3. **Mancanza protezione CSRF**
   - Descrizione: Cross-Site Request Forgery protection disabilitata o mal configurata
   - Rischio: Attacchi CSRF che eseguono azioni non autorizzate
   - Soluzione: Abilitare CSRF protection e configurare correttamente i token

### 🟠 GRAVITÀ ALTA (Score Impact: -10 to -15)
4. **Gestione sessioni insicura**
   - Descrizione: Timeout sessioni troppo lunghi, session fixation non prevenuta
   - Rischio: Hijacking sessioni, accessi prolungati non autorizzati
   - Soluzione: Configurare `sessionManagement()` con timeout appropriati

5. **Header di sicurezza mancanti**
   - Descrizione: Mancanza di X-Frame-Options, X-Content-Type-Options, CSP
   - Rischio: XSS, clickjacking, content injection attacks
   - Soluzione: Configurare `headers()` con tutte le protezioni

### 🟡 GRAVITÀ MEDIA (Score Impact: -5 to -8)
6. **Autorizzazione inappropriata**
   - Descrizione: Controlli di accesso troppo permissivi o mal configurati
   - Rischio: Privilege escalation, accesso a risorse non autorizzate
   - Soluzione: Refinire `@PreAuthorize` e role-based access control

7. **Comunicazione non crittografata**
   - Descrizione: HTTPS non enforced, cookie non sicuri
   - Rischio: Man-in-the-middle attacks, intercettazione credenziali
   - Soluzione: Configurare HTTPS obbligatorio e secure cookies

### 🔵 GRAVITÀ BASSA (Score Impact: -1 to -5)
8. **Messaggi errore troppo dettagliati**
   - Descrizione: Errori di autenticazione/autorizzazione che rivelano informazioni
   - Rischio: Information disclosure che facilita attacchi
   - Soluzione: Genericizzare messaggi di errore pubblici

## Tags per Classificazione
`#spring-security` `#security-audit` `#authentication` `#authorization` `#vulnerability-assessment` `#high-value` `#complex`