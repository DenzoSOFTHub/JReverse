package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.List;

/**
 * Information about Spring Boot bean creation patterns and strategies.
 */
public class BeanCreationPattern {
    
    public enum PatternType {
        CONFIGURATION_CLASS,
        COMPONENT_SCAN,
        FACTORY_BEAN,
        CONDITIONAL_BEAN,
        PROFILE_SPECIFIC,
        LAZY_INITIALIZATION,
        PROTOTYPE_SCOPE,
        SINGLETON_SCOPE
    }
    
    private final String patternName;
    private final PatternType patternType;
    private final String className;
    private final List<String> involvedClasses;
    private final String description;
    private final int usage;
    
    private BeanCreationPattern(String patternName,
                               PatternType patternType,
                               String className,
                               List<String> involvedClasses,
                               String description,
                               int usage) {
        this.patternName = patternName;
        this.patternType = patternType;
        this.className = className;
        this.involvedClasses = Collections.unmodifiableList(involvedClasses);
        this.description = description;
        this.usage = usage;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String getPatternName() {
        return patternName;
    }
    
    public PatternType getPatternType() {
        return patternType;
    }
    
    public String getClassName() {
        return className;
    }
    
    public List<String> getInvolvedClasses() {
        return involvedClasses;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getUsage() {
        return usage;
    }
    
    @Override
    public String toString() {
        return "BeanCreationPattern{" +
                "patternName='" + patternName + '\'' +
                ", patternType=" + patternType +
                ", className='" + className + '\'' +
                ", usage=" + usage +
                '}';
    }
    
    public static class Builder {
        private String patternName;
        private PatternType patternType;
        private String className;
        private List<String> involvedClasses = Collections.emptyList();
        private String description;
        private int usage = 1;
        
        public Builder patternName(String patternName) {
            this.patternName = patternName;
            return this;
        }
        
        public Builder patternType(PatternType patternType) {
            this.patternType = patternType;
            return this;
        }
        
        public Builder className(String className) {
            this.className = className;
            return this;
        }
        
        public Builder involvedClasses(List<String> involvedClasses) {
            this.involvedClasses = involvedClasses != null ? involvedClasses : Collections.emptyList();
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder usage(int usage) {
            this.usage = Math.max(1, usage);
            return this;
        }
        
        public BeanCreationPattern build() {
            return new BeanCreationPattern(patternName, patternType, className, 
                                         involvedClasses, description, usage);
        }
    }
}