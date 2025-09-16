package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Represents the inheritance hierarchy of classes in the analyzed JAR.
 * Placeholder implementation for Phase 3 dependency.
 */
public final class InheritanceHierarchy {
    
    private final List<InheritanceRelation> allRelations;
    private final Map<String, Set<String>> parentToChildren;
    private final Map<String, String> childToParent;
    
    private InheritanceHierarchy(Builder builder) {
        this.allRelations = Collections.unmodifiableList(new ArrayList<>(builder.relations));
        this.parentToChildren = buildParentToChildrenMap(builder.relations);
        this.childToParent = buildChildToParentMap(builder.relations);
    }
    
    private Map<String, Set<String>> buildParentToChildrenMap(List<InheritanceRelation> relations) {
        Map<String, Set<String>> map = new HashMap<>();
        for (InheritanceRelation relation : relations) {
            map.computeIfAbsent(relation.getParent(), k -> new HashSet<>()).add(relation.getChild());
        }
        return Collections.unmodifiableMap(map);
    }
    
    private Map<String, String> buildChildToParentMap(List<InheritanceRelation> relations) {
        Map<String, String> map = new HashMap<>();
        for (InheritanceRelation relation : relations) {
            map.put(relation.getChild(), relation.getParent());
        }
        return Collections.unmodifiableMap(map);
    }
    
    public List<InheritanceRelation> getAllRelations() {
        return allRelations;
    }
    
    public Set<String> getChildren(String parentClass) {
        return parentToChildren.getOrDefault(parentClass, Collections.emptySet());
    }
    
    public String getParent(String childClass) {
        return childToParent.get(childClass);
    }
    
    public boolean hasParent(String className) {
        return childToParent.containsKey(className);
    }
    
    public boolean hasChildren(String className) {
        return parentToChildren.containsKey(className) && !parentToChildren.get(className).isEmpty();
    }
    
    public int getDepth(String className) {
        int depth = 0;
        String current = className;
        while (hasParent(current)) {
            current = getParent(current);
            depth++;
            if (depth > 100) break; // Prevent infinite loops
        }
        return depth;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private List<InheritanceRelation> relations = new ArrayList<>();
        
        public Builder addRelation(InheritanceRelation relation) {
            this.relations.add(relation);
            return this;
        }
        
        public Builder addRelation(String parent, String child, InheritanceType type) {
            this.relations.add(new InheritanceRelation(parent, child, type));
            return this;
        }
        
        public InheritanceHierarchy build() {
            return new InheritanceHierarchy(this);
        }
    }
    
    @Override
    public String toString() {
        return "InheritanceHierarchy{relations=" + allRelations.size() + "}";
    }
}