package it.denzosoft.jreverse.core.model;

import java.util.Objects;

/**
 * Information about custom bean scopes beyond the standard singleton/prototype scopes.
 */
public class BeanScopeInfo {

    private final String scopeName;
    private final String scopeImplementationClass;
    private final ScopeType scopeType;
    private final String description;
    private final boolean isCustomScope;

    private BeanScopeInfo(Builder builder) {
        this.scopeName = builder.scopeName;
        this.scopeImplementationClass = builder.scopeImplementationClass;
        this.scopeType = builder.scopeType;
        this.description = builder.description;
        this.isCustomScope = builder.isCustomScope;
    }

    // Getters
    public String getScopeName() { return scopeName; }
    public String getScopeImplementationClass() { return scopeImplementationClass; }
    public ScopeType getScopeType() { return scopeType; }
    public String getDescription() { return description; }
    public boolean isCustomScope() { return isCustomScope; }

    /**
     * Checks if this is a web-specific scope.
     */
    public boolean isWebScope() {
        return scopeType == ScopeType.REQUEST ||
               scopeType == ScopeType.SESSION ||
               scopeType == ScopeType.APPLICATION ||
               scopeType == ScopeType.WEBSOCKET;
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum ScopeType {
        SINGLETON("singleton", "Single instance per Spring container"),
        PROTOTYPE("prototype", "New instance on each request"),
        REQUEST("request", "Single instance per HTTP request"),
        SESSION("session", "Single instance per HTTP session"),
        APPLICATION("application", "Single instance per servlet context"),
        WEBSOCKET("websocket", "Single instance per WebSocket session"),
        CUSTOM("custom", "Custom user-defined scope");

        private final String scopeName;
        private final String description;

        ScopeType(String scopeName, String description) {
            this.scopeName = scopeName;
            this.description = description;
        }

        public String getScopeName() {
            return scopeName;
        }

        public String getDescription() {
            return description;
        }

        public static ScopeType fromScopeName(String scopeName) {
            if (scopeName == null) {
                return SINGLETON;
            }

            for (ScopeType type : values()) {
                if (type.scopeName.equalsIgnoreCase(scopeName)) {
                    return type;
                }
            }
            return CUSTOM;
        }
    }

    public static class Builder {
        private String scopeName;
        private String scopeImplementationClass;
        private ScopeType scopeType;
        private String description;
        private boolean isCustomScope;

        public Builder scopeName(String scopeName) {
            this.scopeName = scopeName;
            return this;
        }

        public Builder scopeImplementationClass(String scopeImplementationClass) {
            this.scopeImplementationClass = scopeImplementationClass;
            return this;
        }

        public Builder scopeType(ScopeType scopeType) {
            this.scopeType = scopeType;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder isCustomScope(boolean isCustomScope) {
            this.isCustomScope = isCustomScope;
            return this;
        }

        public BeanScopeInfo build() {
            Objects.requireNonNull(scopeName, "Scope name is required");

            // Auto-detect scope type if not provided
            if (scopeType == null) {
                scopeType = ScopeType.fromScopeName(scopeName);
                isCustomScope = (scopeType == ScopeType.CUSTOM);
            }

            return new BeanScopeInfo(this);
        }
    }

    @Override
    public String toString() {
        return String.format("BeanScopeInfo{name='%s', type=%s, custom=%b}",
            scopeName, scopeType, isCustomScope);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BeanScopeInfo that = (BeanScopeInfo) obj;
        return Objects.equals(scopeName, that.scopeName) &&
               scopeType == that.scopeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(scopeName, scopeType);
    }
}