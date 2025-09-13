package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Domain entity representing JAR manifest information.
 * Immutable value object containing manifest attributes.
 */
public final class JarManifestInfo {
    
    private final String mainClass;
    private final String manifestVersion;
    private final String implementationTitle;
    private final String implementationVersion;
    private final String implementationVendor;
    private final String specificationTitle;
    private final String specificationVersion;
    private final String specificationVendor;
    private final String builtBy;
    private final String buildJdk;
    private final String createdBy;
    private final Map<String, String> customAttributes;
    
    private JarManifestInfo(Builder builder) {
        this.mainClass = builder.mainClass;
        this.manifestVersion = builder.manifestVersion;
        this.implementationTitle = builder.implementationTitle;
        this.implementationVersion = builder.implementationVersion;
        this.implementationVendor = builder.implementationVendor;
        this.specificationTitle = builder.specificationTitle;
        this.specificationVersion = builder.specificationVersion;
        this.specificationVendor = builder.specificationVendor;
        this.builtBy = builder.builtBy;
        this.buildJdk = builder.buildJdk;
        this.createdBy = builder.createdBy;
        this.customAttributes = Collections.unmodifiableMap(new HashMap<>(builder.customAttributes));
    }
    
    public String getMainClass() {
        return mainClass;
    }
    
    public String getManifestVersion() {
        return manifestVersion;
    }
    
    public String getImplementationTitle() {
        return implementationTitle;
    }
    
    public String getImplementationVersion() {
        return implementationVersion;
    }
    
    public String getImplementationVendor() {
        return implementationVendor;
    }
    
    public String getSpecificationTitle() {
        return specificationTitle;
    }
    
    public String getSpecificationVersion() {
        return specificationVersion;
    }
    
    public String getSpecificationVendor() {
        return specificationVendor;
    }
    
    public String getBuiltBy() {
        return builtBy;
    }
    
    public String getBuildJdk() {
        return buildJdk;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public Map<String, String> getCustomAttributes() {
        return customAttributes;
    }
    
    public String getCustomAttribute(String name) {
        return customAttributes.get(name);
    }
    
    public boolean hasMainClass() {
        return mainClass != null && !mainClass.trim().isEmpty();
    }
    
    public boolean isExecutable() {
        return hasMainClass();
    }
    
    public boolean isSpringBootJar() {
        return createdBy != null && createdBy.contains("Spring Boot") ||
               implementationTitle != null && implementationTitle.contains("Spring Boot") ||
               mainClass != null && mainClass.contains("JarLauncher");
    }
    
    public boolean hasBuildInfo() {
        return builtBy != null || buildJdk != null || createdBy != null;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        JarManifestInfo that = (JarManifestInfo) obj;
        return Objects.equals(mainClass, that.mainClass) &&
               Objects.equals(implementationTitle, that.implementationTitle) &&
               Objects.equals(implementationVersion, that.implementationVersion) &&
               Objects.equals(customAttributes, that.customAttributes);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(mainClass, implementationTitle, implementationVersion, customAttributes);
    }
    
    @Override
    public String toString() {
        return "JarManifestInfo{" +
                "mainClass='" + mainClass + '\'' +
                ", implementationTitle='" + implementationTitle + '\'' +
                ", implementationVersion='" + implementationVersion + '\'' +
                ", customAttributeCount=" + customAttributes.size() +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String mainClass;
        private String manifestVersion;
        private String implementationTitle;
        private String implementationVersion;
        private String implementationVendor;
        private String specificationTitle;
        private String specificationVersion;
        private String specificationVendor;
        private String builtBy;
        private String buildJdk;
        private String createdBy;
        private Map<String, String> customAttributes = new HashMap<>();
        
        public Builder mainClass(String mainClass) {
            this.mainClass = mainClass;
            return this;
        }
        
        public Builder manifestVersion(String manifestVersion) {
            this.manifestVersion = manifestVersion;
            return this;
        }
        
        public Builder implementationTitle(String implementationTitle) {
            this.implementationTitle = implementationTitle;
            return this;
        }
        
        public Builder implementationVersion(String implementationVersion) {
            this.implementationVersion = implementationVersion;
            return this;
        }
        
        public Builder implementationVendor(String implementationVendor) {
            this.implementationVendor = implementationVendor;
            return this;
        }
        
        public Builder specificationTitle(String specificationTitle) {
            this.specificationTitle = specificationTitle;
            return this;
        }
        
        public Builder specificationVersion(String specificationVersion) {
            this.specificationVersion = specificationVersion;
            return this;
        }
        
        public Builder specificationVendor(String specificationVendor) {
            this.specificationVendor = specificationVendor;
            return this;
        }
        
        public Builder builtBy(String builtBy) {
            this.builtBy = builtBy;
            return this;
        }
        
        public Builder buildJdk(String buildJdk) {
            this.buildJdk = buildJdk;
            return this;
        }
        
        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }
        
        public Builder addCustomAttribute(String name, String value) {
            if (name != null && !name.trim().isEmpty() && value != null) {
                this.customAttributes.put(name.trim(), value);
            }
            return this;
        }
        
        public Builder customAttributes(Map<String, String> customAttributes) {
            this.customAttributes = new HashMap<>(customAttributes != null ? customAttributes : Collections.emptyMap());
            return this;
        }
        
        public JarManifestInfo build() {
            return new JarManifestInfo(this);
        }
    }
}