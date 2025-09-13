# Fase 6: Security & Final Integration - Task Dettagliati

## T6.1.1: SpringSecurityAnalyzer per Security Configurations

### Descrizione Dettagliata
Analyzer completo per l'identificazione e analisi delle configurazioni di Spring Security, mappando strategie di autenticazione, autorizzazione, protezioni CSRF/XSS, e rilevando potenziali vulnerabilità nella configurazione di sicurezza dell'applicazione.

### Scopo dell'Attività
- Identificare e analizzare tutte le configurazioni Spring Security
- Mappare strategie di autenticazione e autorizzazione
- Rilevare configurazioni di sicurezza insicure o mancanti
- Analizzare filtri di sicurezza e loro ordine
- Verificare protezioni CSRF, CORS, session management

### Impatti su Altri Moduli
- **UnprotectedEndpointDetector**: Correlazione con endpoint analysis
- **SecurityAnnotationAnalyzer**: Validazione annotazioni di sicurezza
- **VulnerabilityScanner**: Input per security vulnerability detection
- **ConfigurationAnalyzer**: Context di configurazione applicazione

### Componenti da Implementare

#### 1. Spring Security Analyzer Core
```java
public interface SpringSecurityAnalyzer {
    SecurityAnalysisResult analyzeSecurityConfiguration(JarContent jarContent);
}

public class JavassistSpringSecurityAnalyzer implements SpringSecurityAnalyzer {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistSpringSecurityAnalyzer.class);
    
    private static final Set<String> SECURITY_CONFIG_ANNOTATIONS = Set.of(
        "org.springframework.security.config.annotation.web.configuration.EnableWebSecurity",
        "org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity",
        "org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication"
    );
    
    private static final Set<String> SECURITY_CLASSES = Set.of(
        "org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter",
        "org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurer",
        "org.springframework.security.config.annotation.SecurityConfigurerAdapter"
    );
    
    private final SecurityConfigurationExtractor configExtractor;
    private final AuthenticationAnalyzer authenticationAnalyzer;
    private final AuthorizationAnalyzer authorizationAnalyzer;
    private final SecurityFilterAnalyzer filterAnalyzer;
    private final SecurityVulnerabilityDetector vulnerabilityDetector;
    
    public JavassistSpringSecurityAnalyzer() {
        this.configExtractor = new SecurityConfigurationExtractor();
        this.authenticationAnalyzer = new AuthenticationAnalyzer();
        this.authorizationAnalyzer = new AuthorizationAnalyzer();
        this.filterAnalyzer = new SecurityFilterAnalyzer();
        this.vulnerabilityDetector = new SecurityVulnerabilityDetector();
    }
    
    @Override
    public SecurityAnalysisResult analyzeSecurityConfiguration(JarContent jarContent) {
        LOGGER.startOperation("Spring Security analysis");
        long startTime = System.currentTimeMillis();
        
        try {
            SecurityAnalysisResult.Builder resultBuilder = SecurityAnalysisResult.builder();
            
            // Identifica classi di configurazione sicurezza
            List<SecurityConfigurationClass> securityConfigs = findSecurityConfigurationClasses(jarContent);
            resultBuilder.securityConfigurationClasses(securityConfigs);
            
            LOGGER.info("Found {} security configuration classes", securityConfigs.size());
            
            if (securityConfigs.isEmpty()) {
                LOGGER.warn("No Spring Security configuration found - application may be unsecured");
                return resultBuilder
                    .securityEnabled(false)
                    .analysisMetadata(AnalysisMetadata.warning("No Spring Security configuration detected"))
                    .build();
            }
            
            resultBuilder.securityEnabled(true);
            
            // Estrai configurazioni dettagliate
            List<SecurityConfiguration> configurations = new ArrayList<>();
            for (SecurityConfigurationClass configClass : securityConfigs) {
                SecurityConfiguration config = configExtractor.extractConfiguration(configClass, jarContent);
                configurations.add(config);
            }
            resultBuilder.configurations(configurations);
            
            // Analizza strategie di autenticazione
            AuthenticationAnalysis authAnalysis = authenticationAnalyzer.analyzeAuthentication(
                configurations, jarContent);
            resultBuilder.authenticationAnalysis(authAnalysis);
            
            // Analizza configurazioni di autorizzazione
            AuthorizationAnalysis authzAnalysis = authorizationAnalyzer.analyzeAuthorization(
                configurations, jarContent);
            resultBuilder.authorizationAnalysis(authzAnalysis);
            
            // Analizza filtri di sicurezza
            SecurityFilterAnalysis filterAnalysis = filterAnalyzer.analyzeSecurityFilters(
                configurations, jarContent);
            resultBuilder.filterAnalysis(filterAnalysis);
            
            // Rileva vulnerabilità nella configurazione
            List<SecurityVulnerability> vulnerabilities = vulnerabilityDetector.detectVulnerabilities(
                configurations, authAnalysis, authzAnalysis, filterAnalysis);
            resultBuilder.vulnerabilities(vulnerabilities);
            
            // Genera raccomandazioni di sicurezza
            List<SecurityRecommendation> recommendations = generateSecurityRecommendations(
                configurations, vulnerabilities, authAnalysis, authzAnalysis);
            resultBuilder.recommendations(recommendations);
            
            // Calcola security posture score
            SecurityPostureScore postureScore = calculateSecurityPosture(
                configurations, vulnerabilities, authAnalysis, authzAnalysis);
            resultBuilder.securityPostureScore(postureScore);
            
            return resultBuilder
                .analysisMetadata(AnalysisMetadata.successful(System.currentTimeMillis() - startTime))
                .build();
                
        } catch (Exception e) {
            LOGGER.error("Spring Security analysis failed", e);
            return SecurityAnalysisResult.failed("Analysis failed: " + e.getMessage());
        } finally {
            LOGGER.endOperation("Spring Security analysis", System.currentTimeMillis() - startTime);
        }
    }
    
    private List<SecurityConfigurationClass> findSecurityConfigurationClasses(JarContent jarContent) {
        List<SecurityConfigurationClass> configClasses = new ArrayList<>();
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            if (isSecurityConfigurationClass(classInfo)) {
                SecurityConfigurationClass configClass = createSecurityConfigurationClass(classInfo, jarContent);
                configClasses.add(configClass);
            }
        }
        
        return configClasses;
    }
    
    private boolean isSecurityConfigurationClass(ClassInfo classInfo) {
        // Check for security annotations
        boolean hasSecurityAnnotations = classInfo.getAnnotations().stream()
            .anyMatch(annotation -> SECURITY_CONFIG_ANNOTATIONS.contains(annotation.getType()));
        
        if (hasSecurityAnnotations) {
            return true;
        }
        
        // Check for inheritance from security configuration classes
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.get(classInfo.getFullyQualifiedName());
            CtClass superClass = ctClass.getSuperclass();
            
            while (superClass != null && !superClass.getName().equals("java.lang.Object")) {
                if (SECURITY_CLASSES.contains(superClass.getName())) {
                    return true;
                }
                superClass = superClass.getSuperclass();
            }
            
        } catch (Exception e) {
            LOGGER.debug("Error checking inheritance for {}", classInfo.getFullyQualifiedName(), e);
        }
        
        return false;
    }
    
    private SecurityPostureScore calculateSecurityPosture(List<SecurityConfiguration> configurations,
                                                         List<SecurityVulnerability> vulnerabilities,
                                                         AuthenticationAnalysis authAnalysis,
                                                         AuthorizationAnalysis authzAnalysis) {
        
        SecurityPostureScore.Builder builder = SecurityPostureScore.builder();
        
        int baseScore = 100; // Perfect security score
        int penalties = 0;
        
        // Penalty for high/critical vulnerabilities
        long criticalVulns = vulnerabilities.stream()
            .filter(v -> v.getSeverity() == VulnerabilitySeverity.CRITICAL)
            .count();
        long highVulns = vulnerabilities.stream()
            .filter(v -> v.getSeverity() == VulnerabilitySeverity.HIGH)
            .count();
        
        penalties += (int) (criticalVulns * 25 + highVulns * 15);
        
        // Penalty for weak authentication
        if (!authAnalysis.hasStrongAuthenticationMethod()) {
            penalties += 20;
        }
        
        // Penalty for missing CSRF protection
        if (!hasCsrfProtection(configurations)) {
            penalties += 15;
        }
        
        // Penalty for insecure session management
        if (!hasSecureSessionManagement(configurations)) {
            penalties += 10;
        }
        
        // Penalty for missing HTTPS enforcement
        if (!hasHttpsEnforcement(configurations)) {
            penalties += 15;
        }
        
        int finalScore = Math.max(0, baseScore - penalties);
        
        SecurityPostureLevel level = determinePostureLevel(finalScore);
        
        return builder
            .score(finalScore)
            .level(level)
            .criticalVulnerabilities((int) criticalVulns)
            .highVulnerabilities((int) highVulns)
            .hasStrongAuthentication(authAnalysis.hasStrongAuthenticationMethod())
            .hasCsrfProtection(hasCsrfProtection(configurations))
            .hasSecureSessionManagement(hasSecureSessionManagement(configurations))
            .hasHttpsEnforcement(hasHttpsEnforcement(configurations))
            .build();
    }
    
    private SecurityPostureLevel determinePostureLevel(int score) {
        if (score >= 90) return SecurityPostureLevel.EXCELLENT;
        else if (score >= 80) return SecurityPostureLevel.GOOD;
        else if (score >= 70) return SecurityPostureLevel.FAIR;
        else if (score >= 60) return SecurityPostureLevel.POOR;
        else return SecurityPostureLevel.CRITICAL;
    }
}
```

#### 2. Authentication Analyzer
```java
public class AuthenticationAnalyzer {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(AuthenticationAnalyzer.class);
    
    private static final Set<String> STRONG_AUTH_PROVIDERS = Set.of(
        "org.springframework.security.authentication.dao.DaoAuthenticationProvider",
        "org.springframework.security.ldap.authentication.LdapAuthenticationProvider",
        "org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider",
        "org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider"
    );
    
    private static final Set<String> WEAK_AUTH_METHODS = Set.of(
        "inMemoryAuthentication",
        "httpBasic"
    );
    
    public AuthenticationAnalysis analyzeAuthentication(List<SecurityConfiguration> configurations, 
                                                       JarContent jarContent) {
        LOGGER.info("Analyzing authentication configuration");
        
        AuthenticationAnalysis.Builder builder = AuthenticationAnalysis.builder();
        
        List<AuthenticationProvider> providers = new ArrayList<>();
        List<AuthenticationMethod> methods = new ArrayList<>();
        
        for (SecurityConfiguration config : configurations) {
            // Analizza authentication providers configurati
            providers.addAll(extractAuthenticationProviders(config, jarContent));
            
            // Analizza metodi di autenticazione
            methods.addAll(extractAuthenticationMethods(config, jarContent));
        }
        
        builder.authenticationProviders(providers);
        builder.authenticationMethods(methods);
        
        // Determina se ha autenticazione forte
        boolean hasStrongAuth = hasStrongAuthenticationMethod(providers, methods);
        builder.hasStrongAuthentication(hasStrongAuth);
        
        // Analizza password policies
        PasswordPolicyConfiguration passwordPolicy = analyzePasswordPolicy(configurations, jarContent);
        builder.passwordPolicy(passwordPolicy);
        
        // Analizza session management
        SessionManagementConfiguration sessionConfig = analyzeSessionManagement(configurations);
        builder.sessionManagement(sessionConfig);
        
        // Rileva problemi di autenticazione
        List<AuthenticationIssue> issues = detectAuthenticationIssues(providers, methods, passwordPolicy);
        builder.issues(issues);
        
        return builder.build();
    }
    
    private List<AuthenticationProvider> extractAuthenticationProviders(SecurityConfiguration config, 
                                                                       JarContent jarContent) {
        List<AuthenticationProvider> providers = new ArrayList<>();
        
        try {
            // Cerca metodi che configurano AuthenticationManagerBuilder
            for (MethodInfo method : config.getConfigurationMethods()) {
                if (isAuthenticationConfigurationMethod(method)) {
                    AuthenticationProvider provider = analyzeAuthenticationProviderConfiguration(method, jarContent);
                    if (provider != null) {
                        providers.add(provider);
                    }
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("Error extracting authentication providers", e);
        }
        
        return providers;
    }
    
    private boolean isAuthenticationConfigurationMethod(MethodInfo method) {
        // Method che configurano autenticazione tipicamente:
        // - Hanno parametro AuthenticationManagerBuilder
        // - Sono annotati con @Bean e ritornano AuthenticationManager
        // - Chiamano metodi come userDetailsService(), authenticationProvider()
        
        return method.getParameters().stream()
            .anyMatch(param -> param.getType().contains("AuthenticationManagerBuilder")) ||
               (method.hasAnnotation("org.springframework.context.annotation.Bean") &&
                method.getReturnType().contains("AuthenticationManager"));
    }
    
    private AuthenticationProvider analyzeAuthenticationProviderConfiguration(MethodInfo method, 
                                                                             JarContent jarContent) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.get(method.getDeclaringClass().getFullyQualifiedName());
            CtMethod ctMethod = ctClass.getDeclaredMethod(method.getName());
            
            AuthenticationProvider.Builder builder = AuthenticationProvider.builder()
                .configurationMethod(method.getName())
                .configurationClass(method.getDeclaringClass().getFullyQualifiedName());
            
            // Analizza il bytecode per identificare il tipo di provider
            AuthenticationProviderType providerType = analyzeProviderType(ctMethod);
            builder.providerType(providerType);
            
            // Estrai configurazioni specifiche
            Map<String, Object> configuration = extractProviderConfiguration(ctMethod);
            builder.configuration(configuration);
            
            // Determina il livello di sicurezza
            SecurityLevel securityLevel = determineProviderSecurityLevel(providerType, configuration);
            builder.securityLevel(securityLevel);
            
            return builder.build();
            
        } catch (Exception e) {
            LOGGER.error("Error analyzing authentication provider configuration", e);
            return null;
        }
    }
    
    private AuthenticationProviderType analyzeProviderType(CtMethod method) throws BadBytecode {
        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        if (codeAttribute == null) {
            return AuthenticationProviderType.UNKNOWN;
        }
        
        CodeIterator iterator = codeAttribute.iterator();
        
        while (iterator.hasNext()) {
            int index = iterator.next();
            int opcode = iterator.byteAt(index);
            
            if (opcode == Opcode.INVOKEVIRTUAL || opcode == Opcode.INVOKESPECIAL || opcode == Opcode.INVOKESTATIC) {
                String methodRef = getInvokedMethodReference(method.getDeclaringClass(), iterator, index);
                
                if (methodRef.contains("inMemoryAuthentication")) {
                    return AuthenticationProviderType.IN_MEMORY;
                } else if (methodRef.contains("jdbcAuthentication")) {
                    return AuthenticationProviderType.JDBC;
                } else if (methodRef.contains("ldapAuthentication")) {
                    return AuthenticationProviderType.LDAP;
                } else if (methodRef.contains("userDetailsService")) {
                    return AuthenticationProviderType.USER_DETAILS_SERVICE;
                } else if (methodRef.contains("oauth2")) {
                    return AuthenticationProviderType.OAUTH2;
                } else if (methodRef.contains("jwt")) {
                    return AuthenticationProviderType.JWT;
                }
            }
        }
        
        return AuthenticationProviderType.CUSTOM;
    }
    
    private PasswordPolicyConfiguration analyzePasswordPolicy(List<SecurityConfiguration> configurations, 
                                                             JarContent jarContent) {
        PasswordPolicyConfiguration.Builder builder = PasswordPolicyConfiguration.builder();
        
        // Default values
        builder.minimumLength(0)
               .requiresUppercase(false)
               .requiresLowercase(false)
               .requiresNumbers(false)
               .requiresSpecialCharacters(false)
               .passwordExpirationDays(0)
               .maxFailedAttempts(Integer.MAX_VALUE);
        
        // Cerca configurazioni password encoder
        for (SecurityConfiguration config : configurations) {
            PasswordEncoder encoder = findPasswordEncoder(config, jarContent);
            if (encoder != null) {
                builder.passwordEncoder(encoder);
                
                // Analizza la forza dell'encoder
                PasswordEncoderStrength strength = determineEncoderStrength(encoder);
                builder.encoderStrength(strength);
            }
            
            // Cerca custom password policies
            PasswordPolicy customPolicy = findCustomPasswordPolicy(config, jarContent);
            if (customPolicy != null) {
                builder.customPolicy(customPolicy);
            }
        }
        
        return builder.build();
    }
    
    private List<AuthenticationIssue> detectAuthenticationIssues(List<AuthenticationProvider> providers,
                                                               List<AuthenticationMethod> methods,
                                                               PasswordPolicyConfiguration passwordPolicy) {
        List<AuthenticationIssue> issues = new ArrayList<>();
        
        // Issue: In-memory authentication in production
        boolean hasInMemoryAuth = providers.stream()
            .anyMatch(p -> p.getProviderType() == AuthenticationProviderType.IN_MEMORY);
        if (hasInMemoryAuth) {
            issues.add(AuthenticationIssue.builder()
                .type(AuthenticationIssueType.WEAK_AUTHENTICATION_METHOD)
                .severity(IssueSeverity.HIGH)
                .description("In-memory authentication detected - not suitable for production")
                .recommendation("Use a database or LDAP-based authentication provider")
                .build());
        }
        
        // Issue: Weak password encoding
        if (passwordPolicy.getEncoderStrength() == PasswordEncoderStrength.WEAK) {
            issues.add(AuthenticationIssue.builder()
                .type(AuthenticationIssueType.WEAK_PASSWORD_ENCODING)
                .severity(IssueSeverity.HIGH)
                .description("Weak password encoding algorithm detected")
                .recommendation("Use BCrypt, SCrypt, or Argon2 password encoders")
                .build());
        }
        
        // Issue: No password policy
        if (!passwordPolicy.hasPasswordRequirements()) {
            issues.add(AuthenticationIssue.builder()
                .type(AuthenticationIssueType.NO_PASSWORD_POLICY)
                .severity(IssueSeverity.MEDIUM)
                .description("No password complexity requirements found")
                .recommendation("Implement password complexity rules and validation")
                .build());
        }
        
        // Issue: HTTP Basic authentication without HTTPS
        boolean hasBasicAuth = methods.stream()
            .anyMatch(m -> m.getType() == AuthenticationMethodType.HTTP_BASIC);
        if (hasBasicAuth) {
            issues.add(AuthenticationIssue.builder()
                .type(AuthenticationIssueType.INSECURE_TRANSPORT)
                .severity(IssueSeverity.HIGH)
                .description("HTTP Basic authentication requires HTTPS to be secure")
                .recommendation("Ensure HTTPS is enforced for all authentication endpoints")
                .build());
        }
        
        return issues;
    }
}
```

#### 3. Authorization Analyzer
```java
public class AuthorizationAnalyzer {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(AuthorizationAnalyzer.class);
    
    public AuthorizationAnalysis analyzeAuthorization(List<SecurityConfiguration> configurations,
                                                     JarContent jarContent) {
        LOGGER.info("Analyzing authorization configuration");
        
        AuthorizationAnalysis.Builder builder = AuthorizationAnalysis.builder();
        
        // Analizza URL-based authorization
        List<UrlAuthorizationRule> urlRules = extractUrlAuthorizationRules(configurations, jarContent);
        builder.urlAuthorizationRules(urlRules);
        
        // Analizza method-level authorization
        List<MethodAuthorizationRule> methodRules = extractMethodAuthorizationRules(configurations, jarContent);
        builder.methodAuthorizationRules(methodRules);
        
        // Analizza role hierarchy
        RoleHierarchyConfiguration roleHierarchy = analyzeRoleHierarchy(configurations, jarContent);
        builder.roleHierarchy(roleHierarchy);
        
        // Analizza access decision managers
        List<AccessDecisionManager> accessDecisionManagers = findAccessDecisionManagers(configurations, jarContent);
        builder.accessDecisionManagers(accessDecisionManagers);
        
        // Rileva problemi di autorizzazione
        List<AuthorizationIssue> issues = detectAuthorizationIssues(urlRules, methodRules, roleHierarchy);
        builder.issues(issues);
        
        // Analizza copertura autorizzazione
        AuthorizationCoverageAnalysis coverage = analyzeCoverage(urlRules, methodRules, jarContent);
        builder.coverageAnalysis(coverage);
        
        return builder.build();
    }
    
    private List<UrlAuthorizationRule> extractUrlAuthorizationRules(List<SecurityConfiguration> configurations,
                                                                   JarContent jarContent) {
        List<UrlAuthorizationRule> rules = new ArrayList<>();
        
        for (SecurityConfiguration config : configurations) {
            try {
                rules.addAll(extractUrlRulesFromConfiguration(config, jarContent));
            } catch (Exception e) {
                LOGGER.error("Error extracting URL authorization rules from {}", 
                    config.getConfigurationClass(), e);
            }
        }
        
        return rules;
    }
    
    private List<UrlAuthorizationRule> extractUrlRulesFromConfiguration(SecurityConfiguration config,
                                                                       JarContent jarContent) throws Exception {
        List<UrlAuthorizationRule> rules = new ArrayList<>();
        
        // Trova il metodo configure(HttpSecurity http)
        Optional<MethodInfo> configureMethod = config.getConfigurationMethods().stream()
            .filter(method -> isHttpSecurityConfigureMethod(method))
            .findFirst();
        
        if (configureMethod.isEmpty()) {
            return rules;
        }
        
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.get(config.getConfigurationClass());
        CtMethod ctMethod = ctClass.getDeclaredMethod(configureMethod.get().getName());
        
        // Analizza le chiamate a authorizeRequests() e simili
        rules.addAll(analyzeAuthorizeRequestsCalls(ctMethod));
        
        return rules;
    }
    
    private List<UrlAuthorizationRule> analyzeAuthorizeRequestsCalls(CtMethod method) throws BadBytecode {
        List<UrlAuthorizationRule> rules = new ArrayList<>();
        
        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        if (codeAttribute == null) {
            return rules;
        }
        
        CodeIterator iterator = codeAttribute.iterator();
        
        while (iterator.hasNext()) {
            int index = iterator.next();
            int opcode = iterator.byteAt(index);
            
            if (opcode == Opcode.INVOKEVIRTUAL) {
                String methodRef = getInvokedMethodReference(method.getDeclaringClass(), iterator, index);
                
                if (methodRef.contains("antMatchers") || methodRef.contains("requestMatchers")) {
                    UrlAuthorizationRule rule = extractRuleFromMethodCall(method, index, iterator);
                    if (rule != null) {
                        rules.add(rule);
                    }
                }
            }
        }
        
        return rules;
    }
    
    private UrlAuthorizationRule extractRuleFromMethodCall(CtMethod method, int callIndex, 
                                                          CodeIterator iterator) {
        try {
            UrlAuthorizationRule.Builder builder = UrlAuthorizationRule.builder();
            
            // Estrai pattern URL (argomento del metodo antMatchers)
            String urlPattern = extractStringArgument(method, callIndex, iterator);
            if (urlPattern != null) {
                builder.urlPattern(urlPattern);
            }
            
            // Analizza la catena di chiamate per trovare hasRole, permitAll, etc.
            AuthorizationDecision decision = analyzeAuthorizationChain(method, callIndex, iterator);
            builder.authorizationDecision(decision);
            
            // Determina HTTP methods se specificati
            List<String> httpMethods = extractHttpMethods(method, callIndex, iterator);
            builder.httpMethods(httpMethods);
            
            return builder.build();
            
        } catch (Exception e) {
            LOGGER.debug("Error extracting authorization rule", e);
            return null;
        }
    }
    
    private List<AuthorizationIssue> detectAuthorizationIssues(List<UrlAuthorizationRule> urlRules,
                                                              List<MethodAuthorizationRule> methodRules,
                                                              RoleHierarchyConfiguration roleHierarchy) {
        List<AuthorizationIssue> issues = new ArrayList<>();
        
        // Issue: Permissive catch-all rules
        boolean hasPermissiveCatchAll = urlRules.stream()
            .anyMatch(rule -> "/**".equals(rule.getUrlPattern()) && 
                             rule.getAuthorizationDecision().getType() == AuthorizationDecisionType.PERMIT_ALL);
        
        if (hasPermissiveCatchAll) {
            issues.add(AuthorizationIssue.builder()
                .type(AuthorizationIssueType.OVERLY_PERMISSIVE_RULES)
                .severity(IssueSeverity.HIGH)
                .description("Catch-all pattern /** with permitAll() detected")
                .recommendation("Be more specific with URL patterns and require authentication where appropriate")
                .build());
        }
        
        // Issue: Missing deny-by-default
        boolean hasDenyByDefault = urlRules.stream()
            .anyMatch(rule -> rule.getUrlPattern().equals("/**") && 
                             rule.getAuthorizationDecision().getType() == AuthorizationDecisionType.DENY_ALL);
        
        if (!hasDenyByDefault && !urlRules.isEmpty()) {
            issues.add(AuthorizationIssue.builder()
                .type(AuthorizationIssueType.MISSING_DENY_BY_DEFAULT)
                .severity(IssueSeverity.MEDIUM)
                .description("No explicit deny-by-default rule found")
                .recommendation("Add .anyRequest().authenticated() or .anyRequest().denyAll() as the last rule")
                .build());
        }
        
        // Issue: Conflicting authorization rules
        List<UrlAuthorizationRule> conflictingRules = findConflictingRules(urlRules);
        if (!conflictingRules.isEmpty()) {
            issues.add(AuthorizationIssue.builder()
                .type(AuthorizationIssueType.CONFLICTING_RULES)
                .severity(IssueSeverity.MEDIUM)
                .description(String.format("Found %d conflicting authorization rules", conflictingRules.size()))
                .recommendation("Review and resolve conflicting URL patterns and authorization decisions")
                .conflictingRules(conflictingRules)
                .build());
        }
        
        // Issue: Hardcoded roles
        boolean hasHardcodedRoles = urlRules.stream()
            .anyMatch(rule -> rule.getAuthorizationDecision().hasHardcodedRoles());
        
        if (hasHardcodedRoles) {
            issues.add(AuthorizationIssue.builder()
                .type(AuthorizationIssueType.HARDCODED_ROLES)
                .severity(IssueSeverity.LOW)
                .description("Hardcoded role names found in authorization rules")
                .recommendation("Consider using configuration properties or constants for role names")
                .build());
        }
        
        return issues;
    }
}
```

#### 4. Security Vulnerability Detector
```java
public class SecurityVulnerabilityDetector {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(SecurityVulnerabilityDetector.class);
    
    private final List<VulnerabilityDetector> detectors;
    
    public SecurityVulnerabilityDetector() {
        this.detectors = initializeDetectors();
    }
    
    public List<SecurityVulnerability> detectVulnerabilities(List<SecurityConfiguration> configurations,
                                                            AuthenticationAnalysis authAnalysis,
                                                            AuthorizationAnalysis authzAnalysis,
                                                            SecurityFilterAnalysis filterAnalysis) {
        LOGGER.info("Detecting security vulnerabilities");
        
        List<SecurityVulnerability> vulnerabilities = new ArrayList<>();
        
        for (VulnerabilityDetector detector : detectors) {
            try {
                List<SecurityVulnerability> detected = detector.detect(
                    configurations, authAnalysis, authzAnalysis, filterAnalysis);
                vulnerabilities.addAll(detected);
                
            } catch (Exception e) {
                LOGGER.error("Error in vulnerability detector {}", detector.getClass().getSimpleName(), e);
            }
        }
        
        // Deduplicate and sort by severity
        return vulnerabilities.stream()
            .distinct()
            .sorted(Comparator.comparing(SecurityVulnerability::getSeverity).reversed())
            .collect(Collectors.toList());
    }
    
    private List<VulnerabilityDetector> initializeDetectors() {
        return List.of(
            new CsrfVulnerabilityDetector(),
            new XssVulnerabilityDetector(),
            new SqlInjectionVulnerabilityDetector(),
            new InsecureTransportDetector(),
            new SessionFixationDetector(),
            new ClickjackingDetector(),
            new InformationLeakageDetector(),
            new AuthenticationBypassDetector(),
            new AuthorizationBypassDetector(),
            new InsecurePasswordStorageDetector()
        );
    }
    
    // Example vulnerability detector
    public static class CsrfVulnerabilityDetector implements VulnerabilityDetector {
        @Override
        public List<SecurityVulnerability> detect(List<SecurityConfiguration> configurations,
                                                 AuthenticationAnalysis authAnalysis,
                                                 AuthorizationAnalysis authzAnalysis,
                                                 SecurityFilterAnalysis filterAnalysis) {
            List<SecurityVulnerability> vulnerabilities = new ArrayList<>();
            
            boolean csrfDisabled = configurations.stream()
                .anyMatch(config -> isCsrfDisabled(config));
            
            if (csrfDisabled) {
                vulnerabilities.add(SecurityVulnerability.builder()
                    .type(VulnerabilityType.CSRF)
                    .severity(VulnerabilitySeverity.HIGH)
                    .title("CSRF Protection Disabled")
                    .description("Cross-Site Request Forgery protection has been explicitly disabled")
                    .impact("Attackers can perform actions on behalf of authenticated users")
                    .recommendation("Enable CSRF protection or ensure proper CSRF tokens are used")
                    .cwe("CWE-352")
                    .build());
            }
            
            // Check for missing CSRF protection on state-changing endpoints
            boolean hasStatefulEndpoints = hasStatefulEndpoints(configurations);
            boolean hasCsrfProtection = hasCsrfProtection(configurations);
            
            if (hasStatefulEndpoints && !hasCsrfProtection) {
                vulnerabilities.add(SecurityVulnerability.builder()
                    .type(VulnerabilityType.CSRF)
                    .severity(VulnerabilitySeverity.MEDIUM)
                    .title("Missing CSRF Protection")
                    .description("State-changing endpoints detected without CSRF protection")
                    .impact("Possible CSRF attacks on state-changing operations")
                    .recommendation("Configure CSRF protection for POST, PUT, DELETE operations")
                    .cwe("CWE-352")
                    .build());
            }
            
            return vulnerabilities;
        }
        
        private boolean isCsrfDisabled(SecurityConfiguration config) {
            // Analizza il bytecode per trovare chiamate a csrf().disable()
            return config.getConfigurationMethods().stream()
                .anyMatch(method -> containsMethodCall(method, "csrf") && 
                                   containsMethodCall(method, "disable"));
        }
    }
    
    public static class InsecureTransportDetector implements VulnerabilityDetector {
        @Override
        public List<SecurityVulnerability> detect(List<SecurityConfiguration> configurations,
                                                 AuthenticationAnalysis authAnalysis,
                                                 AuthorizationAnalysis authzAnalysis,
                                                 SecurityFilterAnalysis filterAnalysis) {
            List<SecurityVulnerability> vulnerabilities = new ArrayList<>();
            
            boolean httpsRequired = configurations.stream()
                .anyMatch(config -> requiresHttpsRedirect(config));
            
            if (!httpsRequired) {
                vulnerabilities.add(SecurityVulnerability.builder()
                    .type(VulnerabilityType.INSECURE_TRANSPORT)
                    .severity(VulnerabilitySeverity.HIGH)
                    .title("Missing HTTPS Enforcement")
                    .description("Application does not enforce HTTPS for secure communications")
                    .impact("Credentials and sensitive data transmitted over insecure HTTP")
                    .recommendation("Configure requiresChannel().requiresSecure() or use HTTPS redirect")
                    .cwe("CWE-319")
                    .build());
            }
            
            // Check for HTTP Basic authentication without HTTPS
            boolean hasBasicAuth = authAnalysis.getAuthenticationMethods().stream()
                .anyMatch(method -> method.getType() == AuthenticationMethodType.HTTP_BASIC);
            
            if (hasBasicAuth && !httpsRequired) {
                vulnerabilities.add(SecurityVulnerability.builder()
                    .type(VulnerabilityType.INSECURE_TRANSPORT)
                    .severity(VulnerabilitySeverity.CRITICAL)
                    .title("HTTP Basic Authentication Over Insecure Channel")
                    .description("HTTP Basic authentication used without HTTPS enforcement")
                    .impact("Credentials transmitted in easily decodable Base64 encoding over HTTP")
                    .recommendation("Enforce HTTPS when using HTTP Basic authentication")
                    .cwe("CWE-319")
                    .build());
            }
            
            return vulnerabilities;
        }
    }
}
```

### Principi SOLID Applicati
- **SRP**: Analyzer separati per authentication, authorization, vulnerability detection
- **OCP**: Facilmente estendibile per nuovi tipi di vulnerability detectors
- **LSP**: Implementazioni intercambiabili per diversi security analyzers
- **ISP**: Interfacce specifiche per configuration extraction, vulnerability detection
- **DIP**: Dipende da abstractions per security analysis e vulnerability detection

### Test Unitari da Implementare
```java
// SpringSecurityAnalyzerTest.java
@Test
void shouldDetectSecurityConfiguration() {
    // Arrange
    JarContent jarContent = createJarWithSpringSecurityConfig();
    SpringSecurityAnalyzer analyzer = new JavassistSpringSecurityAnalyzer();
    
    // Act
    SecurityAnalysisResult result = analyzer.analyzeSecurityConfiguration(jarContent);
    
    // Assert
    assertThat(result.isSecurityEnabled()).isTrue();
    assertThat(result.getSecurityConfigurationClasses()).isNotEmpty();
    assertThat(result.getConfigurations()).isNotEmpty();
}

@Test
void shouldDetectNoSecurityConfiguration() {
    JarContent jarContent = createJarWithoutSecurity();
    SpringSecurityAnalyzer analyzer = new JavassistSpringSecurityAnalyzer();
    
    SecurityAnalysisResult result = analyzer.analyzeSecurityConfiguration(jarContent);
    
    assertThat(result.isSecurityEnabled()).isFalse();
    assertThat(result.getSecurityConfigurationClasses()).isEmpty();
}

@Test
void shouldAnalyzeAuthenticationConfiguration() {
    JarContent jarContent = createJarWithAuthenticationConfig();
    SpringSecurityAnalyzer analyzer = new JavassistSpringSecurityAnalyzer();
    
    SecurityAnalysisResult result = analyzer.analyzeSecurityConfiguration(jarContent);
    
    AuthenticationAnalysis authAnalysis = result.getAuthenticationAnalysis();
    assertThat(authAnalysis.getAuthenticationProviders()).isNotEmpty();
    assertThat(authAnalysis.hasStrongAuthentication()).isTrue();
}

@Test
void shouldDetectSecurityVulnerabilities() {
    JarContent jarContent = createJarWithInsecureConfig(); // CSRF disabled, no HTTPS
    SpringSecurityAnalyzer analyzer = new JavassistSpringSecurityAnalyzer();
    
    SecurityAnalysisResult result = analyzer.analyzeSecurityConfiguration(jarContent);
    
    assertThat(result.getVulnerabilities()).isNotEmpty();
    
    boolean hasCsrfVulnerability = result.getVulnerabilities().stream()
        .anyMatch(vuln -> vuln.getType() == VulnerabilityType.CSRF);
    assertThat(hasCsrfVulnerability).isTrue();
    
    boolean hasInsecureTransport = result.getVulnerabilities().stream()
        .anyMatch(vuln -> vuln.getType() == VulnerabilityType.INSECURE_TRANSPORT);
    assertThat(hasInsecureTransport).isTrue();
}

@Test
void shouldCalculateSecurityPostureScore() {
    JarContent jarContent = createJarWithGoodSecurityConfig();
    SpringSecurityAnalyzer analyzer = new JavassistSpringSecurityAnalyzer();
    
    SecurityAnalysisResult result = analyzer.analyzeSecurityConfiguration(jarContent);
    
    SecurityPostureScore score = result.getSecurityPostureScore();
    assertThat(score.getScore()).isBetween(70, 100); // Good security config
    assertThat(score.getLevel()).isIn(SecurityPostureLevel.GOOD, SecurityPostureLevel.EXCELLENT);
}

// AuthenticationAnalyzerTest.java
@Test
void shouldDetectInMemoryAuthentication() {
    SecurityConfiguration config = createConfigWithInMemoryAuth();
    AuthenticationAnalyzer analyzer = new AuthenticationAnalyzer();
    
    AuthenticationAnalysis analysis = analyzer.analyzeAuthentication(List.of(config), createMockJarContent());
    
    assertThat(analysis.getAuthenticationProviders()).hasSize(1);
    AuthenticationProvider provider = analysis.getAuthenticationProviders().get(0);
    assertThat(provider.getProviderType()).isEqualTo(AuthenticationProviderType.IN_MEMORY);
    assertThat(provider.getSecurityLevel()).isEqualTo(SecurityLevel.LOW);
    
    // Should have issues with in-memory auth
    assertThat(analysis.getIssues()).anyMatch(
        issue -> issue.getType() == AuthenticationIssueType.WEAK_AUTHENTICATION_METHOD
    );
}

@Test
void shouldDetectJdbcAuthentication() {
    SecurityConfiguration config = createConfigWithJdbcAuth();
    AuthenticationAnalyzer analyzer = new AuthenticationAnalyzer();
    
    AuthenticationAnalysis analysis = analyzer.analyzeAuthentication(List.of(config), createMockJarContent());
    
    assertThat(analysis.getAuthenticationProviders()).hasSize(1);
    AuthenticationProvider provider = analysis.getAuthenticationProviders().get(0);
    assertThat(provider.getProviderType()).isEqualTo(AuthenticationProviderType.JDBC);
    assertThat(provider.getSecurityLevel()).isIn(SecurityLevel.MEDIUM, SecurityLevel.HIGH);
}

@Test
void shouldAnalyzePasswordPolicy() {
    SecurityConfiguration config = createConfigWithPasswordEncoder();
    AuthenticationAnalyzer analyzer = new AuthenticationAnalyzer();
    
    AuthenticationAnalysis analysis = analyzer.analyzeAuthentication(List.of(config), createMockJarContent());
    
    PasswordPolicyConfiguration passwordPolicy = analysis.getPasswordPolicy();
    assertThat(passwordPolicy.getPasswordEncoder()).isNotNull();
    assertThat(passwordPolicy.getEncoderStrength()).isNotEqualTo(PasswordEncoderStrength.NONE);
}

// AuthorizationAnalyzerTest.java
@Test
void shouldExtractUrlAuthorizationRules() {
    SecurityConfiguration config = createConfigWithUrlRules();
    AuthorizationAnalyzer analyzer = new AuthorizationAnalyzer();
    
    AuthorizationAnalysis analysis = analyzer.analyzeAuthorization(List.of(config), createMockJarContent());
    
    assertThat(analysis.getUrlAuthorizationRules()).isNotEmpty();
    
    UrlAuthorizationRule adminRule = analysis.getUrlAuthorizationRules().stream()
        .filter(rule -> rule.getUrlPattern().equals("/admin/**"))
        .findFirst()
        .orElseThrow();
    
    assertThat(adminRule.getAuthorizationDecision().getType()).isEqualTo(AuthorizationDecisionType.HAS_ROLE);
    assertThat(adminRule.getAuthorizationDecision().getRequiredRoles()).contains("ADMIN");
}

@Test
void shouldDetectAuthorizationIssues() {
    SecurityConfiguration config = createConfigWithPermissiveCatchAll(); // /** -> permitAll
    AuthorizationAnalyzer analyzer = new AuthorizationAnalyzer();
    
    AuthorizationAnalysis analysis = analyzer.analyzeAuthorization(List.of(config), createMockJarContent());
    
    assertThat(analysis.getIssues()).isNotEmpty();
    assertThat(analysis.getIssues()).anyMatch(
        issue -> issue.getType() == AuthorizationIssueType.OVERLY_PERMISSIVE_RULES
    );
}

// SecurityVulnerabilityDetectorTest.java
@Test
void shouldDetectCsrfVulnerability() {
    SecurityConfiguration config = createConfigWithCsrfDisabled();
    SecurityVulnerabilityDetector detector = new SecurityVulnerabilityDetector();
    
    List<SecurityVulnerability> vulnerabilities = detector.detectVulnerabilities(
        List.of(config), null, null, null);
    
    assertThat(vulnerabilities).anyMatch(
        vuln -> vuln.getType() == VulnerabilityType.CSRF && 
                vuln.getSeverity() == VulnerabilitySeverity.HIGH
    );
}

@Test
void shouldDetectInsecureTransportVulnerability() {
    SecurityConfiguration config = createConfigWithHttpBasicAuthNoHttps();
    AuthenticationAnalysis authAnalysis = createAuthAnalysisWithBasicAuth();
    SecurityVulnerabilityDetector detector = new SecurityVulnerabilityDetector();
    
    List<SecurityVulnerability> vulnerabilities = detector.detectVulnerabilities(
        List.of(config), authAnalysis, null, null);
    
    assertThat(vulnerabilities).anyMatch(
        vuln -> vuln.getType() == VulnerabilityType.INSECURE_TRANSPORT && 
                vuln.getSeverity() == VulnerabilitySeverity.CRITICAL
    );
}

// Integration Test
@Test
void shouldAnalyzeCompleteSecurityConfiguration() throws IOException {
    Path secureSpringBootJar = getTestResourcePath("secure-spring-boot.jar");
    JarContent jarContent = new JarLoader().loadJar(new JarLocation(secureSpringBootJar));
    SpringSecurityAnalyzer analyzer = new JavassistSpringSecurityAnalyzer();
    
    SecurityAnalysisResult result = analyzer.analyzeSecurityConfiguration(jarContent);
    
    // Verifiche su applicazione sicura reale
    assertThat(result.isSecurityEnabled()).isTrue();
    assertThat(result.getSecurityConfigurationClasses()).isNotEmpty();
    assertThat(result.getAuthenticationAnalysis().getAuthenticationProviders()).isNotEmpty();
    assertThat(result.getAuthorizationAnalysis().getUrlAuthorizationRules()).isNotEmpty();
    
    // Should have good security posture
    assertThat(result.getSecurityPostureScore().getScore()).isGreaterThan(70);
    
    // Should have some recommendations
    assertThat(result.getRecommendations()).isNotEmpty();
}

@Test
void shouldAnalyzeInsecureApplication() throws IOException {
    Path insecureJar = getTestResourcePath("insecure-application.jar");
    JarContent jarContent = new JarLoader().loadJar(new JarLocation(insecureJar));
    SpringSecurityAnalyzer analyzer = new JavassistSpringSecurityAnalyzer();
    
    SecurityAnalysisResult result = analyzer.analyzeSecurityConfiguration(jarContent);
    
    // Should detect multiple vulnerabilities
    assertThat(result.getVulnerabilities()).hasSizeGreaterThan(3);
    
    // Should have poor security posture
    assertThat(result.getSecurityPostureScore().getScore()).isLessThan(60);
    assertThat(result.getSecurityPostureScore().getLevel()).isIn(
        SecurityPostureLevel.POOR, SecurityPostureLevel.CRITICAL);
}
```

---

## T6.1.2: UnprotectedEndpointDetector per Public Endpoints

### Descrizione Dettagliata
Detector avanzato per l'identificazione di endpoint REST non protetti che potrebbero essere accessibili pubblicamente senza autenticazione o autorizzazione appropriata, analizzando controller mappings e configurazioni di sicurezza.

### Scopo dell'Attività
- Identificare tutti gli endpoint REST nell'applicazione
- Determinare quali endpoint mancano di protezione di sicurezza
- Analizzare mapping URL e metodi HTTP esposti
- Rilevare potenziali endpoint sensibili non protetti
- Correlazione con configurazioni Spring Security

### Impatti su Altri Moduli
- **SpringSecurityAnalyzer**: Correlazione con authorization rules
- **SecurityAnnotationAnalyzer**: Validazione annotazioni di sicurezza
- **VulnerabilityScanner**: Input per security assessment
- **EndpointAnalyzer**: Base dati per endpoint analysis

### Componenti da Implementare

#### 1. Unprotected Endpoint Detector Core
```java
public interface UnprotectedEndpointDetector {
    UnprotectedEndpointAnalysisResult detectUnprotectedEndpoints(JarContent jarContent, 
                                                                SecurityAnalysisResult securityAnalysis);
}

public class JavassistUnprotectedEndpointDetector implements UnprotectedEndpointDetector {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistUnprotectedEndpointDetector.class);
    
    private static final Set<String> CONTROLLER_ANNOTATIONS = Set.of(
        "org.springframework.stereotype.Controller",
        "org.springframework.web.bind.annotation.RestController"
    );
    
    private static final Set<String> MAPPING_ANNOTATIONS = Set.of(
        "org.springframework.web.bind.annotation.RequestMapping",
        "org.springframework.web.bind.annotation.GetMapping",
        "org.springframework.web.bind.annotation.PostMapping",
        "org.springframework.web.bind.annotation.PutMapping",
        "org.springframework.web.bind.annotation.DeleteMapping",
        "org.springframework.web.bind.annotation.PatchMapping"
    );
    
    private static final Set<String> SENSITIVE_ENDPOINT_PATTERNS = Set.of(
        "/admin", "/management", "/actuator", "/api/admin", "/internal",
        "/debug", "/config", "/env", "/health", "/metrics", "/dump"
    );
    
    private final EndpointExtractor endpointExtractor;
    private final SecurityMatcher securityMatcher;
    private final EndpointRiskAssessor riskAssessor;
    
    public JavassistUnprotectedEndpointDetector() {
        this.endpointExtractor = new EndpointExtractor();
        this.securityMatcher = new SecurityMatcher();
        this.riskAssessor = new EndpointRiskAssessor();
    }
    
    @Override
    public UnprotectedEndpointAnalysisResult detectUnprotectedEndpoints(JarContent jarContent,
                                                                        SecurityAnalysisResult securityAnalysis) {
        LOGGER.startOperation("Unprotected endpoint detection");
        long startTime = System.currentTimeMillis();
        
        try {
            UnprotectedEndpointAnalysisResult.Builder resultBuilder = UnprotectedEndpointAnalysisResult.builder();
            
            // Estrai tutti gli endpoint dall'applicazione
            List<EndpointInfo> allEndpoints = endpointExtractor.extractEndpoints(jarContent);
            resultBuilder.totalEndpoints(allEndpoints.size());
            
            LOGGER.info("Found {} total endpoints", allEndpoints.size());
            
            // Determina quali endpoint sono protetti dalle regole di sicurezza
            List<ProtectedEndpoint> protectedEndpoints = new ArrayList<>();
            List<UnprotectedEndpoint> unprotectedEndpoints = new ArrayList<>();
            
            for (EndpointInfo endpoint : allEndpoints) {
                EndpointSecurityStatus status = securityMatcher.determineSecurityStatus(
                    endpoint, securityAnalysis);
                
                if (status.isProtected()) {
                    protectedEndpoints.add(ProtectedEndpoint.builder()
                        .endpointInfo(endpoint)
                        .securityStatus(status)
                        .build());
                } else {
                    UnprotectedEndpoint unprotectedEndpoint = UnprotectedEndpoint.builder()
                        .endpointInfo(endpoint)
                        .securityStatus(status)
                        .riskLevel(riskAssessor.assessRisk(endpoint))
                        .reasons(status.getUnprotectedReasons())
                        .build();
                    unprotectedEndpoints.add(unprotectedEndpoint);
                }
            }
            
            resultBuilder.protectedEndpoints(protectedEndpoints);
            resultBuilder.unprotectedEndpoints(unprotectedEndpoints);
            
            // Categorizza endpoint non protetti per severità
            Map<EndpointRiskLevel, List<UnprotectedEndpoint>> riskCategories = unprotectedEndpoints.stream()
                .collect(Collectors.groupingBy(UnprotectedEndpoint::getRiskLevel));
            resultBuilder.endpointsByRiskLevel(riskCategories);
            
            // Identifica endpoint critici non protetti
            List<CriticalUnprotectedEndpoint> criticalEndpoints = identifyCriticalEndpoints(unprotectedEndpoints);
            resultBuilder.criticalUnprotectedEndpoints(criticalEndpoints);
            
            // Analizza pattern di endpoint non protetti
            EndpointPatternAnalysis patternAnalysis = analyzeEndpointPatterns(unprotectedEndpoints);
            resultBuilder.patternAnalysis(patternAnalysis);
            
            // Genera raccomandazioni
            List<EndpointSecurityRecommendation> recommendations = generateRecommendations(
                unprotectedEndpoints, criticalEndpoints, patternAnalysis);
            resultBuilder.recommendations(recommendations);
            
            // Calcola metriche di copertura sicurezza
            EndpointSecurityCoverage coverage = calculateSecurityCoverage(
                allEndpoints.size(), protectedEndpoints.size(), unprotectedEndpoints.size());
            resultBuilder.securityCoverage(coverage);
            
            LOGGER.info("Found {} protected and {} unprotected endpoints", 
                protectedEndpoints.size(), unprotectedEndpoints.size());
            
            return resultBuilder
                .analysisMetadata(AnalysisMetadata.successful(System.currentTimeMillis() - startTime))
                .build();
                
        } catch (Exception e) {
            LOGGER.error("Unprotected endpoint detection failed", e);
            return UnprotectedEndpointAnalysisResult.failed("Detection failed: " + e.getMessage());
        } finally {
            LOGGER.endOperation("Unprotected endpoint detection", System.currentTimeMillis() - startTime);
        }
    }
    
    private List<CriticalUnprotectedEndpoint> identifyCriticalEndpoints(List<UnprotectedEndpoint> unprotectedEndpoints) {
        return unprotectedEndpoints.stream()
            .filter(endpoint -> endpoint.getRiskLevel() == EndpointRiskLevel.CRITICAL || 
                               endpoint.getRiskLevel() == EndpointRiskLevel.HIGH)
            .filter(this::isCriticalEndpoint)
            .map(this::createCriticalUnprotectedEndpoint)
            .collect(Collectors.toList());
    }
    
    private boolean isCriticalEndpoint(UnprotectedEndpoint endpoint) {
        String path = endpoint.getEndpointInfo().getPath();
        
        // Check for sensitive patterns
        boolean isSensitivePattern = SENSITIVE_ENDPOINT_PATTERNS.stream()
            .anyMatch(pattern -> path.toLowerCase().contains(pattern));
        
        if (isSensitivePattern) return true;
        
        // Check for state-changing operations without protection
        boolean isStatefulOperation = endpoint.getEndpointInfo().getHttpMethods().stream()
            .anyMatch(method -> Set.of("POST", "PUT", "DELETE", "PATCH").contains(method));
        
        if (isStatefulOperation) return true;
        
        // Check for endpoints that likely handle sensitive data
        return containsSensitiveKeywords(path) || 
               hasParametersThatSuggestSensitivity(endpoint.getEndpointInfo());
    }
    
    private boolean containsSensitiveKeywords(String path) {
        String lowerPath = path.toLowerCase();
        Set<String> sensitiveKeywords = Set.of(
            "password", "secret", "token", "key", "credential", "auth", "login",
            "user", "account", "payment", "billing", "card", "bank", "financial",
            "personal", "private", "confidential", "restricted"
        );
        
        return sensitiveKeywords.stream().anyMatch(lowerPath::contains);
    }
}
```

Continua con il resto del task T6.1.2 e gli altri task della Fase 6...

<function_calls>
<invoke name="TodoWrite">
<parameter name="todos">[{"content": "Create detailed task breakdown for Phase 3: Architecture & Dependency Analysis", "status": "completed", "activeForm": "Creating detailed task breakdown for Phase 3"}, {"content": "Create detailed task breakdown for Phase 4: Database & Persistence Layer", "status": "completed", "activeForm": "Creating detailed task breakdown for Phase 4"}, {"content": "Create detailed task breakdown for Phase 5: Code Quality & Performance Metrics", "status": "completed", "activeForm": "Creating detailed task breakdown for Phase 5"}, {"content": "Create detailed task breakdown for Phase 6: Security & Final Integration", "status": "completed", "activeForm": "Creating detailed task breakdown for Phase 6"}]