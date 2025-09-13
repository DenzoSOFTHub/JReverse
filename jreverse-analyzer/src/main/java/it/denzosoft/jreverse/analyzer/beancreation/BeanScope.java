package it.denzosoft.jreverse.analyzer.beancreation;

/**
 * Enumeration of Spring bean scopes.
 */
public enum BeanScope {
    
    /**
     * Singleton scope - one instance per Spring container (default).
     */
    SINGLETON("singleton"),
    
    /**
     * Prototype scope - new instance on each request.
     */
    PROTOTYPE("prototype"),
    
    /**
     * Request scope - one instance per HTTP request.
     */
    REQUEST("request"),
    
    /**
     * Session scope - one instance per HTTP session.
     */
    SESSION("session"),
    
    /**
     * Application scope - one instance per ServletContext.
     */
    APPLICATION("application"),
    
    /**
     * WebSocket scope - one instance per WebSocket session.
     */
    WEBSOCKET("websocket"),
    
    /**
     * Custom scope - application-defined scope.
     */
    CUSTOM("custom");
    
    private final String scopeName;
    
    BeanScope(String scopeName) {
        this.scopeName = scopeName;
    }
    
    public String getScopeName() {
        return scopeName;
    }
    
    /**
     * Parses a scope name to the corresponding BeanScope enum.
     * 
     * @param scopeName the scope name to parse
     * @return the corresponding BeanScope, or SINGLETON if unknown
     */
    public static BeanScope fromScopeName(String scopeName) {
        if (scopeName == null || scopeName.trim().isEmpty()) {
            return SINGLETON;
        }
        
        String normalizedScope = scopeName.trim().toLowerCase();
        switch (normalizedScope) {
            case "singleton":
                return SINGLETON;
            case "prototype":
                return PROTOTYPE;
            case "request":
                return REQUEST;
            case "session":
                return SESSION;
            case "application":
            case "globalSession": // Spring 4.x compatibility
                return APPLICATION;
            case "websocket":
                return WEBSOCKET;
            default:
                return CUSTOM;
        }
    }
    
    /**
     * Checks if this is a web-related scope.
     */
    public boolean isWebScope() {
        return this == REQUEST || this == SESSION || this == APPLICATION || this == WEBSOCKET;
    }
    
    /**
     * Checks if this scope creates multiple instances.
     */
    public boolean isMultiInstance() {
        return this != SINGLETON;
    }
}