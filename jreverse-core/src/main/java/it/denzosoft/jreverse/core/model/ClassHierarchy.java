package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Represents a class hierarchy.
 * Extended for full test compatibility.
 */
public final class ClassHierarchy {
    
    private final String rootClass;
    private final String parentClass;
    private final Set<String> childClasses;
    private final Set<String> implementedInterfaces;
    private final List<String> hierarchyPath;
    private final Map<String, String> parentMap;
    private final int hierarchyDepth;
    private final boolean isInterface;
    private final boolean isAbstract;
    private final boolean isLeafClass;
    
    private ClassHierarchy(Builder builder) {
        this.rootClass = Objects.requireNonNull(builder.rootClass, "Root class cannot be null");
        this.parentClass = builder.parentClass;
        this.childClasses = Collections.unmodifiableSet(new HashSet<>(builder.childClasses));
        this.implementedInterfaces = Collections.unmodifiableSet(new HashSet<>(builder.implementedInterfaces));
        this.hierarchyPath = Collections.unmodifiableList(new ArrayList<>(builder.hierarchyPath));
        this.parentMap = Collections.unmodifiableMap(new HashMap<>(builder.parentMap));
        this.hierarchyDepth = builder.hierarchyDepth;
        this.isInterface = builder.isInterface;
        this.isAbstract = builder.isAbstract;
        this.isLeafClass = builder.childClasses.isEmpty();
    }
    
    // Constructor for backward compatibility
    public ClassHierarchy(String rootClass, Set<String> childClasses, Map<String, String> parentMap, int depth) {
        this.rootClass = Objects.requireNonNull(rootClass, "Root class cannot be null");
        this.parentClass = parentMap.get(rootClass);
        this.childClasses = Collections.unmodifiableSet(new HashSet<>(childClasses));
        this.implementedInterfaces = Collections.emptySet();
        this.hierarchyPath = Collections.singletonList(rootClass);
        this.parentMap = Collections.unmodifiableMap(new HashMap<>(parentMap));
        this.hierarchyDepth = depth;
        this.isInterface = false;
        this.isAbstract = false;
        this.isLeafClass = childClasses.isEmpty();
    }
    
    public String getRootClass() {
        return rootClass;
    }
    
    public Set<String> getChildClasses() {
        return childClasses;
    }
    
    public Map<String, String> getParentMap() {
        return parentMap;
    }
    
    public int getDepth() {
        return hierarchyDepth;
    }
    
    public int getHierarchyDepth() {
        return hierarchyDepth;
    }
    
    public String getParentClass() {
        return parentClass;
    }
    
    public Set<String> getImplementedInterfaces() {
        return implementedInterfaces;
    }
    
    public List<String> getHierarchyPath() {
        return hierarchyPath;
    }
    
    public int getChildrenCount() {
        return childClasses.size();
    }
    
    public boolean isInterface() {
        return isInterface;
    }
    
    public boolean isAbstract() {
        return isAbstract;
    }
    
    public boolean isLeafClass() {
        return isLeafClass;
    }
    
    public boolean implementsInterfaces() {
        return !implementedInterfaces.isEmpty();
    }
    
    public boolean contains(String className) {
        return rootClass.equals(className) || childClasses.contains(className);
    }
    
    public String getParent(String className) {
        return parentMap.get(className);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClassHierarchy that = (ClassHierarchy) obj;
        return hierarchyDepth == that.hierarchyDepth &&
               Objects.equals(rootClass, that.rootClass) &&
               Objects.equals(childClasses, that.childClasses) &&
               Objects.equals(parentMap, that.parentMap);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(rootClass, childClasses, parentMap, hierarchyDepth);
    }
    
    // Factory methods
    public static ClassHierarchy forClass(String className) {
        return builder()
                .rootClass(className)
                .hierarchyDepth(0)
                .build();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String rootClass;
        private String parentClass;
        private Set<String> childClasses = new HashSet<>();
        private Set<String> implementedInterfaces = new HashSet<>();
        private List<String> hierarchyPath = new ArrayList<>();
        private Map<String, String> parentMap = new HashMap<>();
        private int hierarchyDepth = 0;
        private boolean isInterface = false;
        private boolean isAbstract = false;
        
        public Builder rootClass(String rootClass) {
            this.rootClass = rootClass;
            return this;
        }
        
        public Builder parentClass(String parentClass) {
            this.parentClass = parentClass;
            if (parentClass != null && rootClass != null) {
                this.parentMap.put(rootClass, parentClass);
            }
            return this;
        }
        
        public Builder addChildClass(String childClass) {
            this.childClasses.add(childClass);
            if (childClass != null && rootClass != null) {
                this.parentMap.put(childClass, rootClass);
            }
            return this;
        }
        
        public Builder childClasses(Set<String> childClasses) {
            this.childClasses.addAll(childClasses);
            return this;
        }
        
        public Builder implementedInterfaces(Set<String> interfaces) {
            this.implementedInterfaces.addAll(interfaces);
            return this;
        }
        
        public Builder hierarchyPath(List<String> path) {
            this.hierarchyPath.addAll(path);
            return this;
        }
        
        public Builder hierarchyDepth(int depth) {
            this.hierarchyDepth = depth;
            return this;
        }
        
        public Builder isInterface(boolean isInterface) {
            this.isInterface = isInterface;
            return this;
        }
        
        public Builder isAbstract(boolean isAbstract) {
            this.isAbstract = isAbstract;
            return this;
        }
        
        public ClassHierarchy build() {
            return new ClassHierarchy(this);
        }
    }
    
    @Override
    public String toString() {
        return "ClassHierarchy{" +
                "root='" + rootClass + '\'' +
                ", children=" + childClasses.size() +
                ", depth=" + hierarchyDepth +
                ", interfaces=" + implementedInterfaces.size() +
                '}';
    }
}