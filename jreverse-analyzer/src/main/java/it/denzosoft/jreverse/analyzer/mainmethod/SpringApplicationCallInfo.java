package it.denzosoft.jreverse.analyzer.mainmethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Information about a SpringApplication.run() call found in a main method.
 */
public class SpringApplicationCallInfo {
    
    private final SpringApplicationCallType callType;
    private final List<String> sourceClasses;
    private final int argumentCount;
    private final Map<String, Object> additionalProperties;
    private final boolean hasCustomConfiguration;
    private final boolean hasMultipleSourceClasses;
    
    private SpringApplicationCallInfo(SpringApplicationCallType callType,
                                    List<String> sourceClasses,
                                    int argumentCount,
                                    Map<String, Object> additionalProperties,
                                    boolean hasMultipleSourceClasses) {
        this.callType = callType;
        this.sourceClasses = List.copyOf(sourceClasses);
        this.argumentCount = argumentCount;
        this.additionalProperties = Map.copyOf(additionalProperties);
        this.hasMultipleSourceClasses = hasMultipleSourceClasses;
        this.hasCustomConfiguration = hasMultipleSourceClasses || sourceClasses.size() > 1;
    }
    
    /**
     * Creates a SpringApplication call info for unknown/error cases.
     */
    public static SpringApplicationCallInfo unknown() {
        return new SpringApplicationCallInfo(
            SpringApplicationCallType.UNKNOWN,
            List.of(),
            0,
            Map.of(),
            false
        );
    }
    
    // Getters
    public SpringApplicationCallType getCallType() { return callType; }
    public List<String> getSourceClasses() { return sourceClasses; }
    public int getArgumentCount() { return argumentCount; }
    public Map<String, Object> getAdditionalProperties() { return additionalProperties; }
    public boolean hasCustomConfiguration() { return hasCustomConfiguration; }
    
    public boolean hasMultipleSourceClasses() {
        return hasMultipleSourceClasses || sourceClasses.size() > 1;
    }
    
    public String getPrimarySourceClass() {
        return sourceClasses.isEmpty() ? null : sourceClasses.get(0);
    }
    
    /**
     * Builder for SpringApplicationCallInfo.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private SpringApplicationCallType callType = SpringApplicationCallType.STANDARD;
        private Set<String> sourceClasses = new HashSet<>();
        private int argumentCount = 0;
        private Map<String, Object> additionalProperties = new HashMap<>();
        private boolean hasMultipleSourceClasses = false;
        
        public Builder callType(SpringApplicationCallType callType) {
            this.callType = callType;
            return this;
        }
        
        public Builder addSourceClass(String className) {
            this.sourceClasses.add(className);
            return this;
        }
        
        public Builder argumentCount(int count) {
            this.argumentCount = count;
            return this;
        }
        
        public Builder hasMultipleSourceClasses(boolean hasMultiple) {
            this.hasMultipleSourceClasses = hasMultiple;
            return this;
        }
        
        public Builder addProperty(String key, Object value) {
            this.additionalProperties.put(key, value);
            return this;
        }
        
        public SpringApplicationCallInfo build() {
            return new SpringApplicationCallInfo(
                callType,
                new ArrayList<>(sourceClasses),
                argumentCount,
                Map.copyOf(additionalProperties),
                hasMultipleSourceClasses || sourceClasses.size() > 1
            );
        }
    }
}