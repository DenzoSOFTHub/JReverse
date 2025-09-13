package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Configuration information for @ComponentScan annotation.
 */
public class ComponentScanConfiguration {
    
    private final String sourceClass;
    private final Set<String> basePackages;
    private final Set<String> basePackageClasses;
    private final List<ComponentScanFilter> includeFilters;
    private final List<ComponentScanFilter> excludeFilters;
    private final boolean useDefaultFilters;
    private final boolean lazyInit;
    
    private ComponentScanConfiguration(String sourceClass,
                                     Set<String> basePackages,
                                     Set<String> basePackageClasses,
                                     List<ComponentScanFilter> includeFilters,
                                     List<ComponentScanFilter> excludeFilters,
                                     boolean useDefaultFilters,
                                     boolean lazyInit) {
        this.sourceClass = sourceClass;
        this.basePackages = Collections.unmodifiableSet(new HashSet<>(basePackages));
        this.basePackageClasses = Collections.unmodifiableSet(new HashSet<>(basePackageClasses));
        this.includeFilters = Collections.unmodifiableList(includeFilters);
        this.excludeFilters = Collections.unmodifiableList(excludeFilters);
        this.useDefaultFilters = useDefaultFilters;
        this.lazyInit = lazyInit;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String getSourceClass() {
        return sourceClass;
    }
    
    public Set<String> getBasePackages() {
        return basePackages;
    }
    
    public Set<String> getBasePackageClasses() {
        return basePackageClasses;
    }
    
    public List<ComponentScanFilter> getIncludeFilters() {
        return includeFilters;
    }
    
    public List<ComponentScanFilter> getExcludeFilters() {
        return excludeFilters;
    }
    
    public boolean isUseDefaultFilters() {
        return useDefaultFilters;
    }
    
    public boolean isLazyInit() {
        return lazyInit;
    }
    
    public static class Builder {
        private String sourceClass;
        private Set<String> basePackages = new HashSet<>();
        private Set<String> basePackageClasses = new HashSet<>();
        private List<ComponentScanFilter> includeFilters = Collections.emptyList();
        private List<ComponentScanFilter> excludeFilters = Collections.emptyList();
        private boolean useDefaultFilters = true;
        private boolean lazyInit = false;
        
        public Builder sourceClass(String sourceClass) {
            this.sourceClass = sourceClass;
            return this;
        }
        
        public Builder addBasePackage(String basePackage) {
            if (basePackage != null && !basePackage.trim().isEmpty()) {
                this.basePackages.add(basePackage.trim());
            }
            return this;
        }
        
        public Builder addBasePackageClass(String basePackageClass) {
            if (basePackageClass != null && !basePackageClass.trim().isEmpty()) {
                this.basePackageClasses.add(basePackageClass.trim());
            }
            return this;
        }
        
        public Builder includeFilters(List<ComponentScanFilter> includeFilters) {
            this.includeFilters = includeFilters != null ? includeFilters : Collections.emptyList();
            return this;
        }
        
        public Builder excludeFilters(List<ComponentScanFilter> excludeFilters) {
            this.excludeFilters = excludeFilters != null ? excludeFilters : Collections.emptyList();
            return this;
        }
        
        public Builder useDefaultFilters(boolean useDefaultFilters) {
            this.useDefaultFilters = useDefaultFilters;
            return this;
        }
        
        public Builder lazyInit(boolean lazyInit) {
            this.lazyInit = lazyInit;
            return this;
        }
        
        public ComponentScanConfiguration build() {
            return new ComponentScanConfiguration(
                sourceClass, basePackages, basePackageClasses,
                includeFilters, excludeFilters, useDefaultFilters, lazyInit
            );
        }
    }
}