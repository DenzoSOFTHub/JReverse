package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Domain entity representing the analyzed content of a JAR file.
 * Immutable value object containing all extracted metadata.
 */
public final class JarContent {
    
    private final JarLocation location;
    private final Set<ClassInfo> classes;
    private final Map<String, byte[]> resources;
    private final JarManifestInfo manifest;
    private final long analysisTimestamp;
    private final JarType jarType;
    
    private JarContent(Builder builder) {
        this.location = Objects.requireNonNull(builder.location, "location cannot be null");
        this.classes = Collections.unmodifiableSet(new HashSet<>(builder.classes));
        this.resources = Collections.unmodifiableMap(new HashMap<>(builder.resources));
        this.manifest = builder.manifest;
        this.analysisTimestamp = builder.analysisTimestamp > 0 ? builder.analysisTimestamp : System.currentTimeMillis();
        this.jarType = Objects.requireNonNull(builder.jarType, "jarType cannot be null");
    }
    
    public JarLocation getLocation() {
        return location;
    }
    
    public Set<ClassInfo> getClasses() {
        return classes;
    }
    
    public Map<String, byte[]> getResources() {
        return resources;
    }
    
    public JarManifestInfo getManifest() {
        return manifest;
    }
    
    public long getAnalysisTimestamp() {
        return analysisTimestamp;
    }
    
    public JarType getJarType() {
        return jarType;
    }
    
    public int getClassCount() {
        return classes.size();
    }
    
    public int getResourceCount() {
        return resources.size();
    }
    
    public Set<String> getPackages() {
        Set<String> packages = new HashSet<>();
        for (ClassInfo classInfo : classes) {
            String packageName = classInfo.getPackageName();
            if (!packageName.isEmpty()) {
                packages.add(packageName);
            }
        }
        return packages;
    }
    
    public Set<ClassInfo> getClassesInPackage(String packageName) {
        Set<ClassInfo> packageClasses = new HashSet<>();
        for (ClassInfo classInfo : classes) {
            if (packageName.equals(classInfo.getPackageName())) {
                packageClasses.add(classInfo);
            }
        }
        return packageClasses;
    }
    
    public ClassInfo getClassByName(String fullyQualifiedName) {
        return classes.stream()
            .filter(classInfo -> classInfo.getFullyQualifiedName().equals(fullyQualifiedName))
            .findFirst()
            .orElse(null);
    }
    
    public Set<ClassInfo> getClassesWithAnnotation(String annotationType) {
        Set<ClassInfo> annotatedClasses = new HashSet<>();
        for (ClassInfo classInfo : classes) {
            if (classInfo.hasAnnotation(annotationType)) {
                annotatedClasses.add(classInfo);
            }
        }
        return annotatedClasses;
    }
    
    public Set<ClassInfo> getPublicClasses() {
        Set<ClassInfo> publicClasses = new HashSet<>();
        for (ClassInfo classInfo : classes) {
            if (classInfo.isPublic()) {
                publicClasses.add(classInfo);
            }
        }
        return publicClasses;
    }
    
    public Set<ClassInfo> getInterfaces() {
        Set<ClassInfo> interfaces = new HashSet<>();
        for (ClassInfo classInfo : classes) {
            if (classInfo.isInterface()) {
                interfaces.add(classInfo);
            }
        }
        return interfaces;
    }
    
    public Set<ClassInfo> getEnums() {
        Set<ClassInfo> enums = new HashSet<>();
        for (ClassInfo classInfo : classes) {
            if (classInfo.isEnum()) {
                enums.add(classInfo);
            }
        }
        return enums;
    }
    
    public Set<ClassInfo> getAnnotations() {
        Set<ClassInfo> annotations = new HashSet<>();
        for (ClassInfo classInfo : classes) {
            if (classInfo.isAnnotation()) {
                annotations.add(classInfo);
            }
        }
        return annotations;
    }
    
    public boolean hasResource(String resourcePath) {
        return resources.containsKey(resourcePath);
    }
    
    public byte[] getResource(String resourcePath) {
        return resources.get(resourcePath);
    }
    
    public boolean isEmpty() {
        return classes.isEmpty() && resources.isEmpty();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        JarContent that = (JarContent) obj;
        return Objects.equals(location, that.location) &&
               Objects.equals(classes, that.classes);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(location, classes);
    }
    
    @Override
    public String toString() {
        return "JarContent{" +
                "location=" + location.getFileName() +
                ", classCount=" + classes.size() +
                ", resourceCount=" + resources.size() +
                ", jarType=" + jarType +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private JarLocation location;
        private Set<ClassInfo> classes = new HashSet<>();
        private Map<String, byte[]> resources = new HashMap<>();
        private JarManifestInfo manifest;
        private long analysisTimestamp;
        private JarType jarType = JarType.REGULAR;
        
        public Builder location(JarLocation location) {
            this.location = location;
            return this;
        }
        
        public Builder addClass(ClassInfo classInfo) {
            if (classInfo != null) {
                this.classes.add(classInfo);
            }
            return this;
        }
        
        public Builder classes(Set<ClassInfo> classes) {
            this.classes = new HashSet<>(classes != null ? classes : Collections.emptySet());
            return this;
        }
        
        public Builder addResource(String path, byte[] content) {
            if (path != null && !path.trim().isEmpty() && content != null) {
                this.resources.put(path.trim(), content);
            }
            return this;
        }
        
        public Builder resources(Map<String, byte[]> resources) {
            this.resources = new HashMap<>(resources != null ? resources : Collections.emptyMap());
            return this;
        }
        
        public Builder manifest(JarManifestInfo manifest) {
            this.manifest = manifest;
            return this;
        }
        
        public Builder analysisTimestamp(long analysisTimestamp) {
            this.analysisTimestamp = analysisTimestamp;
            return this;
        }
        
        public Builder jarType(JarType jarType) {
            this.jarType = jarType;
            return this;
        }
        
        public JarContent build() {
            return new JarContent(this);
        }
    }
}