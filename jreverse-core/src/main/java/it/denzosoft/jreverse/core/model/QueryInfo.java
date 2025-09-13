package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Information about database queries associated with a method call.
 */
public class QueryInfo {
    
    private final QueryType queryType;
    private final QueryMethod queryMethod;
    private final String entityName;
    private final String jpqlQuery;
    private final String nativeQuery;
    private final List<String> parameters;
    private final boolean isNativeQuery;
    private final boolean isCustomQuery;
    private final int estimatedResultCount;
    private final QueryComplexity complexity;
    private final boolean hasJoins;
    private final boolean hasSubqueries;
    private final boolean isCacheable;
    
    private QueryInfo(Builder builder) {
        this.queryType = builder.queryType;
        this.queryMethod = builder.queryMethod;
        this.entityName = builder.entityName;
        this.jpqlQuery = builder.jpqlQuery;
        this.nativeQuery = builder.nativeQuery;
        this.parameters = List.copyOf(builder.parameters);
        this.isNativeQuery = builder.isNativeQuery;
        this.isCustomQuery = builder.isCustomQuery;
        this.estimatedResultCount = builder.estimatedResultCount;
        this.complexity = builder.complexity;
        this.hasJoins = builder.hasJoins;
        this.hasSubqueries = builder.hasSubqueries;
        this.isCacheable = builder.isCacheable;
    }
    
    public QueryType getQueryType() {
        return queryType;
    }
    
    public QueryMethod getQueryMethod() {
        return queryMethod;
    }
    
    public String getEntityName() {
        return entityName;
    }
    
    public String getJpqlQuery() {
        return jpqlQuery;
    }
    
    public String getNativeQuery() {
        return nativeQuery;
    }
    
    public List<String> getParameters() {
        return parameters;
    }
    
    public boolean isNativeQuery() {
        return isNativeQuery;
    }
    
    public boolean isCustomQuery() {
        return isCustomQuery;
    }
    
    public int getEstimatedResultCount() {
        return estimatedResultCount;
    }
    
    public QueryComplexity getComplexity() {
        return complexity;
    }
    
    public boolean hasJoins() {
        return hasJoins;
    }
    
    public boolean hasSubqueries() {
        return hasSubqueries;
    }
    
    public boolean isCacheable() {
        return isCacheable;
    }
    
    /**
     * Gets the actual query string (JPQL or native).
     */
    public String getQueryString() {
        return isNativeQuery ? nativeQuery : jpqlQuery;
    }
    
    /**
     * Gets a human-readable description of the query.
     */
    public String getDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append(queryType.getDisplayName());
        
        if (entityName != null) {
            desc.append(" ").append(entityName);
        }
        
        if (queryMethod != null) {
            desc.append(" (").append(queryMethod.getDisplayName()).append(")");
        }
        
        if (isNativeQuery) {
            desc.append(" [Native SQL]");
        }
        
        return desc.toString();
    }
    
    /**
     * Checks if this query might have performance issues.
     */
    public boolean hasPerformanceRisk() {
        return complexity == QueryComplexity.HIGH || 
               complexity == QueryComplexity.VERY_HIGH ||
               (estimatedResultCount > 1000 && !isCacheable) ||
               (hasSubqueries && !hasJoins);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private QueryType queryType = QueryType.UNKNOWN;
        private QueryMethod queryMethod = QueryMethod.UNKNOWN;
        private String entityName;
        private String jpqlQuery;
        private String nativeQuery;
        private List<String> parameters = Collections.emptyList();
        private boolean isNativeQuery = false;
        private boolean isCustomQuery = false;
        private int estimatedResultCount = 1;
        private QueryComplexity complexity = QueryComplexity.SIMPLE;
        private boolean hasJoins = false;
        private boolean hasSubqueries = false;
        private boolean isCacheable = false;
        
        public Builder queryType(QueryType queryType) {
            this.queryType = queryType;
            return this;
        }
        
        public Builder queryMethod(QueryMethod queryMethod) {
            this.queryMethod = queryMethod;
            return this;
        }
        
        public Builder entityName(String entityName) {
            this.entityName = entityName;
            return this;
        }
        
        public Builder jpqlQuery(String jpqlQuery) {
            this.jpqlQuery = jpqlQuery;
            return this;
        }
        
        public Builder nativeQuery(String nativeQuery) {
            this.nativeQuery = nativeQuery;
            this.isNativeQuery = true;
            return this;
        }
        
        public Builder parameters(List<String> parameters) {
            this.parameters = parameters != null ? parameters : Collections.emptyList();
            return this;
        }
        
        public Builder isCustomQuery(boolean isCustomQuery) {
            this.isCustomQuery = isCustomQuery;
            return this;
        }
        
        public Builder estimatedResultCount(int estimatedResultCount) {
            this.estimatedResultCount = Math.max(0, estimatedResultCount);
            return this;
        }
        
        public Builder complexity(QueryComplexity complexity) {
            this.complexity = complexity;
            return this;
        }
        
        public Builder hasJoins(boolean hasJoins) {
            this.hasJoins = hasJoins;
            return this;
        }
        
        public Builder hasSubqueries(boolean hasSubqueries) {
            this.hasSubqueries = hasSubqueries;
            return this;
        }
        
        public Builder isCacheable(boolean isCacheable) {
            this.isCacheable = isCacheable;
            return this;
        }
        
        public QueryInfo build() {
            return new QueryInfo(this);
        }
    }
    
    public enum QueryType {
        SELECT("SELECT", "Data retrieval"),
        INSERT("INSERT", "Data insertion"),
        UPDATE("UPDATE", "Data modification"),
        DELETE("DELETE", "Data deletion"),
        COUNT("COUNT", "Count operation"),
        EXISTS("EXISTS", "Existence check"),
        UNKNOWN("UNKNOWN", "Unknown operation");
        
        private final String displayName;
        private final String description;
        
        QueryType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum QueryMethod {
        DERIVED("Derived", "Method name derived query"),
        CUSTOM("Custom", "Custom @Query annotation"),
        CRITERIA("Criteria", "Criteria API query"),
        NATIVE("Native", "Native SQL query"),
        NAMED("Named", "Named query"),
        UNKNOWN("Unknown", "Unknown query method");
        
        private final String displayName;
        private final String description;
        
        QueryMethod(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum QueryComplexity {
        SIMPLE("Simple", "Basic single-table operation"),
        MODERATE("Moderate", "Multi-table with simple joins"),
        COMPLEX("Complex", "Multiple joins with conditions"),
        HIGH("High", "Complex joins with subqueries"),
        VERY_HIGH("Very High", "Highly complex with multiple subqueries");
        
        private final String displayName;
        private final String description;
        
        QueryComplexity(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @Override
    public String toString() {
        return String.format("QueryInfo{type=%s, method=%s, entity=%s, complexity=%s}",
            queryType, queryMethod, entityName, complexity);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        QueryInfo that = (QueryInfo) obj;
        return Objects.equals(jpqlQuery, that.jpqlQuery) &&
               Objects.equals(nativeQuery, that.nativeQuery) &&
               queryType == that.queryType;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(jpqlQuery, nativeQuery, queryType);
    }
}