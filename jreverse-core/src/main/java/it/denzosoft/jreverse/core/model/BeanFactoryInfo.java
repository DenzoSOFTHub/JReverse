package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.List;

/**
 * Information about a Spring Boot bean factory or factory method.
 */
public class BeanFactoryInfo {
    
    public enum FactoryType {
        CONFIGURATION_METHOD,
        FACTORY_BEAN,
        COMPONENT_FACTORY,
        IMPORT_FACTORY
    }
    
    private final String factoryClassName;
    private final String factoryMethodName;
    private final String beanName;
    private final String beanType;
    private final FactoryType factoryType;
    private final List<String> parameters;
    private final boolean isConditional;
    private final List<String> conditions;
    
    private BeanFactoryInfo(String factoryClassName,
                           String factoryMethodName,
                           String beanName,
                           String beanType,
                           FactoryType factoryType,
                           List<String> parameters,
                           boolean isConditional,
                           List<String> conditions) {
        this.factoryClassName = factoryClassName;
        this.factoryMethodName = factoryMethodName;
        this.beanName = beanName;
        this.beanType = beanType;
        this.factoryType = factoryType;
        this.parameters = Collections.unmodifiableList(parameters);
        this.isConditional = isConditional;
        this.conditions = Collections.unmodifiableList(conditions);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String getFactoryClassName() {
        return factoryClassName;
    }
    
    public String getFactoryMethodName() {
        return factoryMethodName;
    }
    
    public String getBeanName() {
        return beanName;
    }
    
    public String getBeanType() {
        return beanType;
    }
    
    public FactoryType getFactoryType() {
        return factoryType;
    }
    
    public List<String> getParameters() {
        return parameters;
    }
    
    public boolean isConditional() {
        return isConditional;
    }
    
    public List<String> getConditions() {
        return conditions;
    }
    
    public int getParameterCount() {
        return parameters.size();
    }
    
    @Override
    public String toString() {
        return "BeanFactoryInfo{" +
                "factoryClassName='" + factoryClassName + '\'' +
                ", factoryMethodName='" + factoryMethodName + '\'' +
                ", beanName='" + beanName + '\'' +
                ", factoryType=" + factoryType +
                ", parameters=" + parameters.size() +
                ", isConditional=" + isConditional +
                '}';
    }
    
    public static class Builder {
        private String factoryClassName;
        private String factoryMethodName;
        private String beanName;
        private String beanType;
        private FactoryType factoryType = FactoryType.CONFIGURATION_METHOD;
        private List<String> parameters = Collections.emptyList();
        private boolean isConditional = false;
        private List<String> conditions = Collections.emptyList();
        
        public Builder factoryClassName(String factoryClassName) {
            this.factoryClassName = factoryClassName;
            return this;
        }
        
        public Builder factoryMethodName(String factoryMethodName) {
            this.factoryMethodName = factoryMethodName;
            return this;
        }
        
        public Builder beanName(String beanName) {
            this.beanName = beanName;
            return this;
        }
        
        public Builder beanType(String beanType) {
            this.beanType = beanType;
            return this;
        }
        
        public Builder factoryType(FactoryType factoryType) {
            this.factoryType = factoryType != null ? factoryType : FactoryType.CONFIGURATION_METHOD;
            return this;
        }
        
        public Builder parameters(List<String> parameters) {
            this.parameters = parameters != null ? parameters : Collections.emptyList();
            return this;
        }
        
        public Builder isConditional(boolean isConditional) {
            this.isConditional = isConditional;
            return this;
        }
        
        public Builder conditions(List<String> conditions) {
            this.conditions = conditions != null ? conditions : Collections.emptyList();
            return this;
        }
        
        public BeanFactoryInfo build() {
            return new BeanFactoryInfo(factoryClassName, factoryMethodName, beanName, 
                                     beanType, factoryType, parameters, isConditional, conditions);
        }
    }
}