package it.denzosoft.jreverse.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Information about SpringApplication.run() call found in main method.
 */
public class SpringApplicationCallInfo {
    
    private final SpringApplicationCallType callType;
    private final List<String> sourceClasses;
    private final int argumentCount;
    private final Map<String, Object> additionalProperties;
    private final boolean hasCustomConfiguration;
    
    private SpringApplicationCallInfo(SpringApplicationCallType callType,
                                    List<String> sourceClasses,
                                    int argumentCount,
                                    Map<String, Object> additionalProperties,
                                    boolean hasCustomConfiguration) {
        this.callType = callType;
        this.sourceClasses = Collections.unmodifiableList(new ArrayList<>(sourceClasses));
        this.argumentCount = argumentCount;
        this.additionalProperties = Collections.unmodifiableMap(new HashMap<>(additionalProperties));
        this.hasCustomConfiguration = hasCustomConfiguration;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static SpringApplicationCallInfo unknown() {
        return builder()
            .callType(SpringApplicationCallType.UNKNOWN)
            .build();
    }
    
    public SpringApplicationCallType getCallType() {
        return callType;
    }
    
    public List<String> getSourceClasses() {
        return sourceClasses;
    }
    
    public int getArgumentCount() {
        return argumentCount;
    }
    
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }
    
    public boolean hasCustomConfiguration() {
        return hasCustomConfiguration;
    }
    
    public boolean hasMultipleSourceClasses() {
        return sourceClasses.size() > 1;
    }
    
    public String getPrimarySourceClass() {
        return sourceClasses.isEmpty() ? null : sourceClasses.get(0);
    }
    
    @Override
    public String toString() {
        return "SpringApplicationCallInfo{" +
                "callType=" + callType +
                ", sourceClasses=" + sourceClasses.size() +
                ", argumentCount=" + argumentCount +
                ", hasCustomConfiguration=" + hasCustomConfiguration +
                '}';
    }
    
    public static class Builder {
        private SpringApplicationCallType callType = SpringApplicationCallType.STANDARD;
        private Set<String> sourceClasses = new HashSet<>();
        private int argumentCount = 0;
        private Map<String, Object> additionalProperties = new HashMap<>();
        private boolean hasMultipleSourceClasses = false;
        
        public Builder callType(SpringApplicationCallType callType) {
            this.callType = callType != null ? callType : SpringApplicationCallType.UNKNOWN;
            return this;
        }
        
        public Builder addSourceClass(String className) {
            if (className != null && !className.trim().isEmpty()) {
                this.sourceClasses.add(className.trim());
            }
            return this;
        }
        
        public Builder sourceClasses(List<String> sourceClasses) {
            this.sourceClasses.clear();
            if (sourceClasses != null) {
                sourceClasses.stream()
                    .filter(cls -> cls != null && !cls.trim().isEmpty())
                    .forEach(cls -> this.sourceClasses.add(cls.trim()));
            }
            return this;
        }
        
        public Builder argumentCount(int argumentCount) {
            this.argumentCount = Math.max(0, argumentCount);
            return this;
        }
        
        public Builder addProperty(String key, Object value) {
            if (key != null && !key.trim().isEmpty() && value != null) {
                this.additionalProperties.put(key.trim(), value);
            }
            return this;
        }
        
        public Builder hasMultipleSourceClasses(boolean hasMultiple) {
            this.hasMultipleSourceClasses = hasMultiple;
            return this;
        }
        
        public SpringApplicationCallInfo build() {
            // Determine if has custom configuration
            boolean hasCustomConfig = callType.isCustom() || 
                                    additionalProperties.size() > 0 ||
                                    hasMultipleSourceClasses ||
                                    sourceClasses.size() > 1;
            
            // Auto-adjust call type based on source classes
            SpringApplicationCallType finalCallType = callType;
            if (sourceClasses.size() > 1 && callType == SpringApplicationCallType.STANDARD) {
                finalCallType = SpringApplicationCallType.MULTIPLE_SOURCES;
            }
            
            return new SpringApplicationCallInfo(
                finalCallType,
                new ArrayList<>(sourceClasses),
                argumentCount,
                additionalProperties,
                hasCustomConfig
            );
        }
    }
}