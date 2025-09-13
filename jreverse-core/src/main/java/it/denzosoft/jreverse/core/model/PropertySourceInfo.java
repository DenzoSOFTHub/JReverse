package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.List;

/**
 * Information about Spring Boot @PropertySource configuration.
 */
public class PropertySourceInfo {
    
    public enum SourceType {
        PROPERTIES_FILE,
        YAML_FILE,
        XML_FILE,
        SYSTEM_PROPERTIES,
        ENVIRONMENT_VARIABLES,
        CLASSPATH_RESOURCE,
        FILE_RESOURCE,
        URL_RESOURCE
    }
    
    private final String name;
    private final List<String> locations;
    private final SourceType sourceType;
    private final boolean ignoreResourceNotFound;
    private final String encoding;
    private final String factory;
    private final String targetClass;
    private final boolean isDefault;
    
    private PropertySourceInfo(String name,
                              List<String> locations,
                              SourceType sourceType,
                              boolean ignoreResourceNotFound,
                              String encoding,
                              String factory,
                              String targetClass,
                              boolean isDefault) {
        this.name = name;
        this.locations = Collections.unmodifiableList(locations);
        this.sourceType = sourceType;
        this.ignoreResourceNotFound = ignoreResourceNotFound;
        this.encoding = encoding;
        this.factory = factory;
        this.targetClass = targetClass;
        this.isDefault = isDefault;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String getName() {
        return name;
    }
    
    public List<String> getLocations() {
        return locations;
    }
    
    public SourceType getSourceType() {
        return sourceType;
    }
    
    public boolean isIgnoreResourceNotFound() {
        return ignoreResourceNotFound;
    }
    
    public String getEncoding() {
        return encoding;
    }
    
    public String getFactory() {
        return factory;
    }
    
    public String getTargetClass() {
        return targetClass;
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    public int getLocationCount() {
        return locations.size();
    }
    
    @Override
    public String toString() {
        return "PropertySourceInfo{" +
                "name='" + name + '\'' +
                ", locations=" + locations.size() +
                ", sourceType=" + sourceType +
                ", targetClass='" + targetClass + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
    
    public static class Builder {
        private String name;
        private List<String> locations = Collections.emptyList();
        private SourceType sourceType = SourceType.PROPERTIES_FILE;
        private boolean ignoreResourceNotFound = false;
        private String encoding = "UTF-8";
        private String factory;
        private String targetClass;
        private boolean isDefault = false;
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder locations(List<String> locations) {
            this.locations = locations != null ? locations : Collections.emptyList();
            return this;
        }
        
        public Builder sourceType(SourceType sourceType) {
            this.sourceType = sourceType != null ? sourceType : SourceType.PROPERTIES_FILE;
            return this;
        }
        
        public Builder ignoreResourceNotFound(boolean ignoreResourceNotFound) {
            this.ignoreResourceNotFound = ignoreResourceNotFound;
            return this;
        }
        
        public Builder encoding(String encoding) {
            this.encoding = encoding != null ? encoding : "UTF-8";
            return this;
        }
        
        public Builder factory(String factory) {
            this.factory = factory;
            return this;
        }
        
        public Builder targetClass(String targetClass) {
            this.targetClass = targetClass;
            return this;
        }
        
        public Builder isDefault(boolean isDefault) {
            this.isDefault = isDefault;
            return this;
        }
        
        public PropertySourceInfo build() {
            return new PropertySourceInfo(name, locations, sourceType, 
                                        ignoreResourceNotFound, encoding, factory, 
                                        targetClass, isDefault);
        }
    }
}