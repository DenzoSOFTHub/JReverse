package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Information about a specific package including its classes, metrics and organizational data.
 */
public final class PackageInfo {
    
    private final String name;
    private final Set<ClassInfo> classes;
    private final PackageMetrics metrics;
    private final List<String> subPackages;
    private final String parentPackage;
    private final boolean isRootLevel;
    
    private PackageInfo(Builder builder) {
        this.name = builder.name != null ? builder.name : "";
        this.classes = Collections.unmodifiableSet(new HashSet<>(builder.classes));
        this.metrics = builder.metrics;
        this.subPackages = Collections.unmodifiableList(new ArrayList<>(builder.subPackages));
        this.parentPackage = builder.parentPackage;
        this.isRootLevel = builder.parentPackage == null || builder.parentPackage.isEmpty();
    }
    
    public String getName() {
        return name;
    }
    
    public Set<ClassInfo> getClasses() {
        return classes;
    }
    
    public PackageMetrics getMetrics() {
        return metrics;
    }
    
    public List<String> getSubPackages() {
        return subPackages;
    }
    
    public String getParentPackage() {
        return parentPackage;
    }
    
    public boolean isRootLevel() {
        return isRootLevel;
    }
    
    public int getClassCount() {
        return classes.size();
    }
    
    public int getDepth() {
        if (name.isEmpty()) return 0;
        return (int) name.chars().filter(ch -> ch == '.').count() + 1;
    }
    
    public String getSimpleName() {
        int lastDot = name.lastIndexOf('.');
        return lastDot >= 0 ? name.substring(lastDot + 1) : name;
    }
    
    public Set<String> getClassNames() {
        return classes.stream()
                .collect(HashSet::new, 
                        (set, classInfo) -> set.add(classInfo.getFullyQualifiedName()),
                        HashSet::addAll);
    }
    
    public Set<ClassInfo> getPublicClasses() {
        return classes.stream()
                .filter(ClassInfo::isPublic)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }
    
    public Set<ClassInfo> getInterfaces() {
        return classes.stream()
                .filter(ClassInfo::isInterface)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }
    
    public Set<ClassInfo> getAbstractClasses() {
        return classes.stream()
                .filter(ClassInfo::isAbstract)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }
    
    public boolean hasClasses() {
        return !classes.isEmpty();
    }
    
    public boolean hasSubPackages() {
        return !subPackages.isEmpty();
    }
    
    private String requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        return value.trim();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PackageInfo that = (PackageInfo) obj;
        return isRootLevel == that.isRootLevel &&
               Objects.equals(name, that.name) &&
               Objects.equals(classes, that.classes) &&
               Objects.equals(metrics, that.metrics) &&
               Objects.equals(subPackages, that.subPackages) &&
               Objects.equals(parentPackage, that.parentPackage);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, classes, metrics, subPackages, parentPackage, isRootLevel);
    }
    
    @Override
    public String toString() {
        return "PackageInfo{" +
                "name='" + name + '\'' +
                ", classCount=" + classes.size() +
                ", subPackagesCount=" + subPackages.size() +
                ", depth=" + getDepth() +
                ", isRootLevel=" + isRootLevel +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String name;
        private Set<ClassInfo> classes = new HashSet<>();
        private PackageMetrics metrics;
        private List<String> subPackages = new ArrayList<>();
        private String parentPackage;
        
        public Builder name(String name) {
            this.name = name;
            // Auto-determine parent package
            if (name != null && !name.isEmpty()) {
                int lastDot = name.lastIndexOf('.');
                this.parentPackage = lastDot > 0 ? name.substring(0, lastDot) : null;
            }
            return this;
        }
        
        public Builder addClass(ClassInfo classInfo) {
            if (classInfo != null) {
                classes.add(classInfo);
            }
            return this;
        }
        
        public Builder classes(Set<ClassInfo> classes) {
            this.classes = new HashSet<>(classes != null ? classes : Collections.emptySet());
            return this;
        }
        
        public Builder metrics(PackageMetrics metrics) {
            this.metrics = metrics;
            return this;
        }
        
        public Builder addSubPackage(String subPackageName) {
            if (subPackageName != null && !subPackageName.trim().isEmpty()) {
                subPackages.add(subPackageName.trim());
            }
            return this;
        }
        
        public Builder subPackages(List<String> subPackages) {
            this.subPackages = new ArrayList<>(subPackages != null ? subPackages : Collections.emptyList());
            return this;
        }
        
        public Builder parentPackage(String parentPackage) {
            this.parentPackage = parentPackage;
            return this;
        }
        
        public PackageInfo build() {
            return new PackageInfo(this);
        }
    }
}