package it.denzosoft.jreverse.analyzer.restcontroller;

import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.AnnotationInfo;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable value object representing information about a REST controller class.
 * Contains metadata about REST controller structure, annotations, and configuration.
 */
public class RestControllerInfo {
    
    private final ClassInfo classInfo;
    private final RestControllerType controllerType;
    private final Optional<String> basePath;
    private final List<String> crossOriginPatterns;
    private final List<String> securityAnnotations;
    private final List<String> cachingAnnotations;
    private final boolean hasRequestMappingClass;
    private final boolean hasResponseBodyClass;
    private final String packageName;
    private final Optional<String> parentController;
    
    private RestControllerInfo(Builder builder) {
        this.classInfo = Objects.requireNonNull(builder.classInfo, "classInfo cannot be null");
        this.controllerType = Objects.requireNonNull(builder.controllerType, "controllerType cannot be null");
        this.basePath = Optional.ofNullable(builder.basePath);
        this.crossOriginPatterns = Collections.unmodifiableList(builder.crossOriginPatterns);
        this.securityAnnotations = Collections.unmodifiableList(builder.securityAnnotations);
        this.cachingAnnotations = Collections.unmodifiableList(builder.cachingAnnotations);
        this.hasRequestMappingClass = builder.hasRequestMappingClass;
        this.hasResponseBodyClass = builder.hasResponseBodyClass;
        this.packageName = extractPackageName(classInfo.getFullyQualifiedName());
        this.parentController = Optional.ofNullable(builder.parentController);
    }
    
    private String extractPackageName(String fullyQualifiedName) {
        int lastDotIndex = fullyQualifiedName.lastIndexOf('.');
        return lastDotIndex > 0 ? fullyQualifiedName.substring(0, lastDotIndex) : "";
    }
    
    // Getters
    public ClassInfo getClassInfo() { return classInfo; }
    public RestControllerType getControllerType() { return controllerType; }
    public Optional<String> getBasePath() { return basePath; }
    public List<String> getCrossOriginPatterns() { return crossOriginPatterns; }
    public List<String> getSecurityAnnotations() { return securityAnnotations; }
    public List<String> getCachingAnnotations() { return cachingAnnotations; }
    public boolean hasRequestMappingClass() { return hasRequestMappingClass; }
    public boolean hasResponseBodyClass() { return hasResponseBodyClass; }
    public String getPackageName() { return packageName; }
    public Optional<String> getParentController() { return parentController; }
    
    public String getClassName() { return classInfo.getFullyQualifiedName(); }
    public String getSimpleClassName() { return classInfo.getSimpleName(); }
    
    public boolean hasCrossOriginConfig() { return !crossOriginPatterns.isEmpty(); }
    public boolean hasSecurityAnnotations() { return !securityAnnotations.isEmpty(); }
    public boolean hasCachingAnnotations() { return !cachingAnnotations.isEmpty(); }
    public boolean isInheritanceHierarchy() { return parentController.isPresent(); }
    
    /**
     * Checks if this controller is a pure REST controller (@RestController).
     */
    public boolean isPureRestController() {
        return controllerType == RestControllerType.REST_CONTROLLER;
    }
    
    /**
     * Checks if this controller is a hybrid controller (@Controller + @ResponseBody).
     */
    public boolean isHybridController() {
        return controllerType == RestControllerType.HYBRID_CONTROLLER;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RestControllerInfo that = (RestControllerInfo) obj;
        return Objects.equals(classInfo.getFullyQualifiedName(), that.classInfo.getFullyQualifiedName());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(classInfo.getFullyQualifiedName());
    }
    
    @Override
    public String toString() {
        return "RestControllerInfo{" +
                "className='" + classInfo.getFullyQualifiedName() + '\'' +
                ", controllerType=" + controllerType +
                ", basePath=" + basePath.orElse("none") +
                ", packageName='" + packageName + '\'' +
                ", hasCrossOrigin=" + hasCrossOriginConfig() +
                ", hasSecurityAnnotations=" + hasSecurityAnnotations() +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private ClassInfo classInfo;
        private RestControllerType controllerType;
        private String basePath;
        private List<String> crossOriginPatterns = Collections.emptyList();
        private List<String> securityAnnotations = Collections.emptyList();
        private List<String> cachingAnnotations = Collections.emptyList();
        private boolean hasRequestMappingClass = false;
        private boolean hasResponseBodyClass = false;
        private String parentController;
        
        public Builder classInfo(ClassInfo classInfo) {
            this.classInfo = classInfo;
            return this;
        }
        
        public Builder controllerType(RestControllerType controllerType) {
            this.controllerType = controllerType;
            return this;
        }
        
        public Builder basePath(String basePath) {
            this.basePath = basePath;
            return this;
        }
        
        public Builder crossOriginPatterns(List<String> crossOriginPatterns) {
            this.crossOriginPatterns = crossOriginPatterns != null ? List.copyOf(crossOriginPatterns) : Collections.emptyList();
            return this;
        }
        
        public Builder securityAnnotations(List<String> securityAnnotations) {
            this.securityAnnotations = securityAnnotations != null ? List.copyOf(securityAnnotations) : Collections.emptyList();
            return this;
        }
        
        public Builder cachingAnnotations(List<String> cachingAnnotations) {
            this.cachingAnnotations = cachingAnnotations != null ? List.copyOf(cachingAnnotations) : Collections.emptyList();
            return this;
        }
        
        public Builder hasRequestMappingClass(boolean hasRequestMappingClass) {
            this.hasRequestMappingClass = hasRequestMappingClass;
            return this;
        }
        
        public Builder hasResponseBodyClass(boolean hasResponseBodyClass) {
            this.hasResponseBodyClass = hasResponseBodyClass;
            return this;
        }
        
        public Builder parentController(String parentController) {
            this.parentController = parentController;
            return this;
        }
        
        public RestControllerInfo build() {
            return new RestControllerInfo(this);
        }
    }
    
    /**
     * Enumeration of REST controller types.
     */
    public enum RestControllerType {
        /**
         * Pure REST controller with @RestController annotation.
         */
        REST_CONTROLLER,
        
        /**
         * Hybrid controller with @Controller and @ResponseBody annotations.
         */
        HYBRID_CONTROLLER
    }
}