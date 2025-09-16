package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Represents composition and aggregation relationships between classes.
 * Placeholder implementation for Phase 3 dependency.
 */
public final class CompositionGraph {
    
    private final List<CompositionRelationship> allRelationships;
    private final Map<String, Set<CompositionRelationship>> classToRelationships;
    
    private CompositionGraph(Builder builder) {
        this.allRelationships = Collections.unmodifiableList(new ArrayList<>(builder.relationships));
        this.classToRelationships = buildClassRelationshipMap(builder.relationships);
    }
    
    private Map<String, Set<CompositionRelationship>> buildClassRelationshipMap(List<CompositionRelationship> relationships) {
        Map<String, Set<CompositionRelationship>> map = new HashMap<>();
        for (CompositionRelationship rel : relationships) {
            map.computeIfAbsent(rel.getSource(), k -> new HashSet<>()).add(rel);
            map.computeIfAbsent(rel.getTarget(), k -> new HashSet<>()).add(rel);
        }
        return Collections.unmodifiableMap(map);
    }
    
    public List<CompositionRelationship> getAllRelationships() {
        return allRelationships;
    }
    
    public Set<CompositionRelationship> getRelationshipsFor(String className) {
        return classToRelationships.getOrDefault(className, Collections.emptySet());
    }
    
    public List<CompositionRelationship> getCompositionRelationships() {
        return allRelationships.stream()
                .filter(rel -> rel.getCompositionType() == CompositionType.COMPOSITION)
                .collect(java.util.stream.Collectors.toList());
    }
    
    public List<CompositionRelationship> getAggregationRelationships() {
        return allRelationships.stream()
                .filter(rel -> rel.getCompositionType() == CompositionType.AGGREGATION)
                .collect(java.util.stream.Collectors.toList());
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private List<CompositionRelationship> relationships = new ArrayList<>();
        
        public Builder addRelationship(CompositionRelationship relationship) {
            this.relationships.add(relationship);
            return this;
        }
        
        public Builder addComposition(String source, String target, String multiplicity) {
            this.relationships.add(new CompositionRelationship(source, target, CompositionType.COMPOSITION, multiplicity));
            return this;
        }
        
        public Builder addAggregation(String source, String target, String multiplicity) {
            this.relationships.add(new CompositionRelationship(source, target, CompositionType.AGGREGATION, multiplicity));
            return this;
        }
        
        public CompositionGraph build() {
            return new CompositionGraph(this);
        }
    }
    
    @Override
    public String toString() {
        return "CompositionGraph{relationships=" + allRelationships.size() + "}";
    }
}