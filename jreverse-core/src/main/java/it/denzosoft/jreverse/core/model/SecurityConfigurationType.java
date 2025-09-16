package it.denzosoft.jreverse.core.model;

/**
 * Enumeration of Spring Security configuration types.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public enum SecurityConfigurationType {
    WEB_SECURITY_CONFIGURER_ADAPTER("WebSecurityConfigurerAdapter", true, false),
    SECURITY_FILTER_CHAIN("SecurityFilterChain", true, false),
    ENABLE_WEB_SECURITY("@EnableWebSecurity", true, false),
    ENABLE_GLOBAL_METHOD_SECURITY("@EnableGlobalMethodSecurity", false, true),
    OAUTH2_RESOURCE_SERVER("OAuth2ResourceServer", true, false),
    JWT_DECODER("JwtDecoder", false, false),
    AUTHENTICATION_MANAGER("AuthenticationManager", false, false),
    USER_DETAILS_SERVICE("UserDetailsService", false, false),
    PASSWORD_ENCODER("PasswordEncoder", false, false),
    CUSTOM_SECURITY_FILTER("Custom Security Filter", true, false),
    SPRING_BOOT_SECURITY_AUTO_CONFIG("Spring Boot Security Auto Config", true, false),
    UNKNOWN("Unknown", false, false);

    private final String displayName;
    private final boolean webSecurityConfiguration;
    private final boolean methodSecurityConfiguration;

    SecurityConfigurationType(String displayName, boolean webSecurityConfiguration, boolean methodSecurityConfiguration) {
        this.displayName = displayName;
        this.webSecurityConfiguration = webSecurityConfiguration;
        this.methodSecurityConfiguration = methodSecurityConfiguration;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isWebSecurityConfiguration() {
        return webSecurityConfiguration;
    }

    public boolean isMethodSecurityConfiguration() {
        return methodSecurityConfiguration;
    }

    public static SecurityConfigurationType fromClassName(String className) {
        if (className.contains("WebSecurityConfigurerAdapter")) return WEB_SECURITY_CONFIGURER_ADAPTER;
        if (className.contains("SecurityFilterChain")) return SECURITY_FILTER_CHAIN;
        if (className.contains("OAuth2ResourceServer")) return OAUTH2_RESOURCE_SERVER;
        if (className.contains("JwtDecoder")) return JWT_DECODER;
        if (className.contains("AuthenticationManager")) return AUTHENTICATION_MANAGER;
        if (className.contains("UserDetailsService")) return USER_DETAILS_SERVICE;
        if (className.contains("PasswordEncoder")) return PASSWORD_ENCODER;
        if (className.contains("Filter")) return CUSTOM_SECURITY_FILTER;
        return UNKNOWN;
    }
}