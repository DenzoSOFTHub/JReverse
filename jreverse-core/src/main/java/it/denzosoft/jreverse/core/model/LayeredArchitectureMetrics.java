package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Metrics related to layered architecture analysis.
 * This is an immutable value object following Clean Architecture principles.
 */
public final class LayeredArchitectureMetrics {
    
    private final int totalLayers;
    private final int totalViolations;
    private final Map<LayerType, Integer> classesByLayer;
    private final Map<LayerViolation.Severity, Integer> violationsBySeverity;
    private final Map<LayerViolation.Type, Integer> violationsByType;
    private final double layeringCompliance; // 0.0 - 1.0
    private final double architecturalIntegrity; // 0.0 - 1.0
    private final int totalDependencies;
    private final int validDependencies;
    private final int invalidDependencies;
    private final double averageCouplingPerLayer;
    private final double averageCohesionPerLayer;
    private final Set<String> mostViolatedLayers;
    private final Set<String> bestOrganizedLayers;
    
    private LayeredArchitectureMetrics(Builder builder) {
        this.totalLayers = Math.max(0, builder.totalLayers);
        this.totalViolations = Math.max(0, builder.totalViolations);
        this.classesByLayer = Collections.unmodifiableMap(new HashMap<>(builder.classesByLayer));
        this.violationsBySeverity = Collections.unmodifiableMap(new HashMap<>(builder.violationsBySeverity));
        this.violationsByType = Collections.unmodifiableMap(new HashMap<>(builder.violationsByType));
        this.layeringCompliance = Math.min(1.0, Math.max(0.0, builder.layeringCompliance));
        this.architecturalIntegrity = Math.min(1.0, Math.max(0.0, builder.architecturalIntegrity));
        this.totalDependencies = Math.max(0, builder.totalDependencies);
        this.validDependencies = Math.max(0, builder.validDependencies);
        this.invalidDependencies = Math.max(0, builder.invalidDependencies);
        this.averageCouplingPerLayer = Math.max(0.0, builder.averageCouplingPerLayer);
        this.averageCohesionPerLayer = Math.min(1.0, Math.max(0.0, builder.averageCohesionPerLayer));
        this.mostViolatedLayers = Collections.unmodifiableSet(new HashSet<>(builder.mostViolatedLayers));
        this.bestOrganizedLayers = Collections.unmodifiableSet(new HashSet<>(builder.bestOrganizedLayers));
    }
    
    public int getTotalLayers() {
        return totalLayers;
    }
    
    public int getTotalViolations() {
        return totalViolations;
    }
    
    public Map<LayerType, Integer> getClassesByLayer() {
        return classesByLayer;
    }
    
    public Map<LayerViolation.Severity, Integer> getViolationsBySeverity() {
        return violationsBySeverity;
    }
    
    public Map<LayerViolation.Type, Integer> getViolationsByType() {
        return violationsByType;
    }
    
    public double getLayeringCompliance() {
        return layeringCompliance;
    }
    
    public double getArchitecturalIntegrity() {
        return architecturalIntegrity;
    }
    
    public int getTotalDependencies() {
        return totalDependencies;
    }
    
    public int getValidDependencies() {
        return validDependencies;
    }
    
    public int getInvalidDependencies() {
        return invalidDependencies;
    }
    
    public double getAverageCouplingPerLayer() {
        return averageCouplingPerLayer;
    }
    
    public double getAverageCohesionPerLayer() {
        return averageCohesionPerLayer;
    }
    
    public Set<String> getMostViolatedLayers() {
        return mostViolatedLayers;
    }
    
    public Set<String> getBestOrganizedLayers() {
        return bestOrganizedLayers;
    }
    
    public int getTotalClasses() {
        return classesByLayer.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    public int getClassCountForLayer(LayerType layerType) {
        return classesByLayer.getOrDefault(layerType, 0);
    }
    
    public int getViolationCount(LayerViolation.Severity severity) {
        return violationsBySeverity.getOrDefault(severity, 0);
    }
    
    public int getViolationCount(LayerViolation.Type type) {
        return violationsByType.getOrDefault(type, 0);
    }
    
    public int getCriticalViolations() {
        return getViolationCount(LayerViolation.Severity.CRITICAL);
    }
    
    public int getHighSeverityViolations() {
        return getViolationCount(LayerViolation.Severity.HIGH);
    }
    
    public int getMediumSeverityViolations() {
        return getViolationCount(LayerViolation.Severity.MEDIUM);
    }
    
    public int getLowSeverityViolations() {
        return getViolationCount(LayerViolation.Severity.LOW);
    }
    
    public double getDependencyValidityRatio() {
        if (totalDependencies == 0) return 1.0;
        return (double) validDependencies / totalDependencies;
    }
    
    public double getViolationRate() {
        int totalClasses = getTotalClasses();
        if (totalClasses == 0) return 0.0;
        return (double) totalViolations / totalClasses;
    }
    
    public boolean hasExcellentCompliance() {
        return layeringCompliance >= 0.9;
    }
    
    public boolean hasGoodCompliance() {
        return layeringCompliance >= 0.7 && layeringCompliance < 0.9;
    }
    
    public boolean hasDecentCompliance() {
        return layeringCompliance >= 0.5 && layeringCompliance < 0.7;
    }
    
    public boolean hasPoorCompliance() {
        return layeringCompliance < 0.5;
    }
    
    public boolean hasHighIntegrity() {
        return architecturalIntegrity >= 0.8;
    }
    
    public boolean hasMediumIntegrity() {
        return architecturalIntegrity >= 0.6 && architecturalIntegrity < 0.8;
    }
    
    public boolean hasLowIntegrity() {
        return architecturalIntegrity < 0.6;
    }
    
    public boolean hasHighCoupling() {
        return averageCouplingPerLayer >= 10.0;
    }
    
    public boolean hasModerateCoupling() {
        return averageCouplingPerLayer >= 5.0 && averageCouplingPerLayer < 10.0;
    }
    
    public boolean hasLowCoupling() {
        return averageCouplingPerLayer < 5.0;
    }
    
    public boolean hasHighCohesion() {
        return averageCohesionPerLayer >= 0.7;
    }
    
    public boolean hasModerateCohesion() {
        return averageCohesionPerLayer >= 0.5 && averageCohesionPerLayer < 0.7;
    }
    
    public boolean hasLowCohesion() {
        return averageCohesionPerLayer < 0.5;
    }
    
    public String getComplianceLevel() {
        if (hasExcellentCompliance()) return "Excellent";
        if (hasGoodCompliance()) return "Good";
        if (hasDecentCompliance()) return "Decent";
        return "Poor";
    }
    
    public String getIntegrityLevel() {
        if (hasHighIntegrity()) return "High";
        if (hasMediumIntegrity()) return "Medium";
        return "Low";
    }
    
    public String getCouplingLevel() {
        if (hasHighCoupling()) return "High";
        if (hasModerateCoupling()) return "Moderate";
        return "Low";
    }
    
    public String getCohesionLevel() {
        if (hasHighCohesion()) return "High";
        if (hasModerateCohesion()) return "Moderate";
        return "Low";
    }
    
    public LayerType getLargestLayer() {
        return classesByLayer.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
    
    public LayerType getSmallestLayer() {
        return classesByLayer.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
    
    public LayerViolation.Type getMostCommonViolationType() {
        return violationsByType.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LayeredArchitectureMetrics that = (LayeredArchitectureMetrics) obj;
        return totalLayers == that.totalLayers &&
               totalViolations == that.totalViolations &&
               Double.compare(that.layeringCompliance, layeringCompliance) == 0 &&
               Double.compare(that.architecturalIntegrity, architecturalIntegrity) == 0 &&
               totalDependencies == that.totalDependencies &&
               validDependencies == that.validDependencies &&
               invalidDependencies == that.invalidDependencies &&
               Double.compare(that.averageCouplingPerLayer, averageCouplingPerLayer) == 0 &&
               Double.compare(that.averageCohesionPerLayer, averageCohesionPerLayer) == 0 &&
               Objects.equals(classesByLayer, that.classesByLayer) &&
               Objects.equals(violationsBySeverity, that.violationsBySeverity) &&
               Objects.equals(violationsByType, that.violationsByType) &&
               Objects.equals(mostViolatedLayers, that.mostViolatedLayers) &&
               Objects.equals(bestOrganizedLayers, that.bestOrganizedLayers);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(totalLayers, totalViolations, classesByLayer, violationsBySeverity,
                violationsByType, layeringCompliance, architecturalIntegrity, totalDependencies,
                validDependencies, invalidDependencies, averageCouplingPerLayer, averageCohesionPerLayer,
                mostViolatedLayers, bestOrganizedLayers);
    }
    
    @Override
    public String toString() {
        return "LayeredArchitectureMetrics{" +
                "layers=" + totalLayers +
                ", violations=" + totalViolations +
                ", compliance=" + String.format("%.2f", layeringCompliance) +
                ", integrity=" + String.format("%.2f", architecturalIntegrity) +
                ", coupling=" + String.format("%.2f", averageCouplingPerLayer) +
                ", cohesion=" + String.format("%.2f", averageCohesionPerLayer) +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private int totalLayers = 0;
        private int totalViolations = 0;
        private Map<LayerType, Integer> classesByLayer = new HashMap<>();
        private Map<LayerViolation.Severity, Integer> violationsBySeverity = new HashMap<>();
        private Map<LayerViolation.Type, Integer> violationsByType = new HashMap<>();
        private double layeringCompliance = 1.0;
        private double architecturalIntegrity = 1.0;
        private int totalDependencies = 0;
        private int validDependencies = 0;
        private int invalidDependencies = 0;
        private double averageCouplingPerLayer = 0.0;
        private double averageCohesionPerLayer = 1.0;
        private Set<String> mostViolatedLayers = new HashSet<>();
        private Set<String> bestOrganizedLayers = new HashSet<>();
        
        public Builder totalLayers(int totalLayers) {
            this.totalLayers = totalLayers;
            return this;
        }
        
        public Builder totalViolations(int totalViolations) {
            this.totalViolations = totalViolations;
            return this;
        }
        
        public Builder addLayer(LayerType layerType, int classCount) {
            classesByLayer.put(layerType, classCount);
            return this;
        }
        
        public Builder incrementLayer(LayerType layerType) {
            classesByLayer.merge(layerType, 1, Integer::sum);
            return this;
        }
        
        public Builder classesByLayer(Map<LayerType, Integer> classesByLayer) {
            this.classesByLayer = new HashMap<>(classesByLayer != null ? classesByLayer : Collections.emptyMap());
            return this;
        }
        
        public Builder addViolation(LayerViolation.Severity severity, int count) {
            violationsBySeverity.put(severity, count);
            return this;
        }
        
        public Builder incrementViolation(LayerViolation.Severity severity) {
            violationsBySeverity.merge(severity, 1, Integer::sum);
            totalViolations++;
            return this;
        }
        
        public Builder violationsBySeverity(Map<LayerViolation.Severity, Integer> violations) {
            this.violationsBySeverity = new HashMap<>(violations != null ? violations : Collections.emptyMap());
            return this;
        }
        
        public Builder addViolationType(LayerViolation.Type type, int count) {
            violationsByType.put(type, count);
            return this;
        }
        
        public Builder incrementViolationType(LayerViolation.Type type) {
            violationsByType.merge(type, 1, Integer::sum);
            return this;
        }
        
        public Builder violationsByType(Map<LayerViolation.Type, Integer> violations) {
            this.violationsByType = new HashMap<>(violations != null ? violations : Collections.emptyMap());
            return this;
        }
        
        public Builder layeringCompliance(double layeringCompliance) {
            this.layeringCompliance = layeringCompliance;
            return this;
        }
        
        public Builder architecturalIntegrity(double architecturalIntegrity) {
            this.architecturalIntegrity = architecturalIntegrity;
            return this;
        }
        
        public Builder totalDependencies(int totalDependencies) {
            this.totalDependencies = totalDependencies;
            return this;
        }
        
        public Builder validDependencies(int validDependencies) {
            this.validDependencies = validDependencies;
            return this;
        }
        
        public Builder invalidDependencies(int invalidDependencies) {
            this.invalidDependencies = invalidDependencies;
            return this;
        }
        
        public Builder averageCouplingPerLayer(double averageCouplingPerLayer) {
            this.averageCouplingPerLayer = averageCouplingPerLayer;
            return this;
        }
        
        public Builder averageCohesionPerLayer(double averageCohesionPerLayer) {
            this.averageCohesionPerLayer = averageCohesionPerLayer;
            return this;
        }
        
        public Builder addMostViolatedLayer(String layerName) {
            if (layerName != null && !layerName.trim().isEmpty()) {
                mostViolatedLayers.add(layerName.trim());
            }
            return this;
        }
        
        public Builder mostViolatedLayers(Set<String> layers) {
            this.mostViolatedLayers = new HashSet<>(layers != null ? layers : Collections.emptySet());
            return this;
        }
        
        public Builder addBestOrganizedLayer(String layerName) {
            if (layerName != null && !layerName.trim().isEmpty()) {
                bestOrganizedLayers.add(layerName.trim());
            }
            return this;
        }
        
        public Builder bestOrganizedLayers(Set<String> layers) {
            this.bestOrganizedLayers = new HashSet<>(layers != null ? layers : Collections.emptySet());
            return this;
        }
        
        public LayeredArchitectureMetrics build() {
            // Calculate derived metrics if not provided
            if (totalLayers == 0) {
                totalLayers = classesByLayer.size();
            }
            
            if (totalViolations == 0) {
                totalViolations = violationsBySeverity.values().stream().mapToInt(Integer::intValue).sum();
            }
            
            if (totalDependencies > 0 && layeringCompliance == 1.0) {
                layeringCompliance = (double) validDependencies / totalDependencies;
            }
            
            return new LayeredArchitectureMetrics(this);
        }
    }
}