package it.denzosoft.jreverse.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Result of component scan analysis containing all detected configurations.
 */
public class ComponentScanAnalysisResult {
    
    private final List<ComponentScanConfiguration> configurations;
    private final Set<String> effectivePackages;
    private final AnalysisMetadata metadata;
    private final long analysisTimeMs;
    
    private ComponentScanAnalysisResult(List<ComponentScanConfiguration> configurations,
                                      Set<String> effectivePackages,
                                      AnalysisMetadata metadata,
                                      long analysisTimeMs) {
        this.configurations = Collections.unmodifiableList(new ArrayList<>(configurations));
        this.effectivePackages = Collections.unmodifiableSet(new HashSet<>(effectivePackages));
        this.metadata = metadata;
        this.analysisTimeMs = analysisTimeMs;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static ComponentScanAnalysisResult noConfigurations() {
        return builder()
            .metadata(AnalysisMetadata.warning("No component scan configurations found"))
            .build();
    }
    
    public static ComponentScanAnalysisResult error(String errorMessage) {
        return builder()
            .metadata(AnalysisMetadata.error(errorMessage))
            .build();
    }
    
    public List<ComponentScanConfiguration> getConfigurations() {
        return configurations;
    }
    
    public Set<String> getEffectivePackages() {
        return effectivePackages;
    }
    
    public AnalysisMetadata getMetadata() {
        return metadata;
    }
    
    public long getAnalysisTimeMs() {
        return analysisTimeMs;
    }
    
    public boolean hasConfigurations() {
        return !configurations.isEmpty();
    }
    
    public boolean isSuccessful() {
        return metadata.isSuccessful();
    }
    
    public int getConfigurationCount() {
        return configurations.size();
    }
    
    public ComponentScanAnalysisResult withAnalysisTime(long analysisTimeMs) {
        return new ComponentScanAnalysisResult(configurations, effectivePackages, metadata, analysisTimeMs);
    }
    
    @Override
    public String toString() {
        return "ComponentScanAnalysisResult{" +
                "configurations=" + configurations.size() +
                ", effectivePackages=" + effectivePackages.size() +
                ", analysisTime=" + analysisTimeMs + "ms" +
                '}';
    }
    
    public static class Builder {
        private List<ComponentScanConfiguration> configurations = new ArrayList<>();
        private Set<String> effectivePackages = new HashSet<>();
        private AnalysisMetadata metadata = AnalysisMetadata.successful();
        private long analysisTimeMs = 0L;
        
        public Builder addConfiguration(ComponentScanConfiguration configuration) {
            if (configuration != null) {
                this.configurations.add(configuration);
            }
            return this;
        }
        
        public Builder configurations(List<ComponentScanConfiguration> configurations) {
            this.configurations = new ArrayList<>(configurations != null ? configurations : Collections.emptyList());
            return this;
        }
        
        public Builder addEffectivePackage(String packageName) {
            if (packageName != null && !packageName.trim().isEmpty()) {
                this.effectivePackages.add(packageName.trim());
            }
            return this;
        }
        
        public Builder effectivePackages(Set<String> effectivePackages) {
            this.effectivePackages = new HashSet<>(effectivePackages != null ? effectivePackages : Collections.emptySet());
            return this;
        }
        
        public Builder metadata(AnalysisMetadata metadata) {
            this.metadata = metadata != null ? metadata : AnalysisMetadata.successful();
            return this;
        }
        
        public Builder analysisTimeMs(long analysisTimeMs) {
            this.analysisTimeMs = Math.max(0L, analysisTimeMs);
            return this;
        }
        
        public ComponentScanAnalysisResult build() {
            return new ComponentScanAnalysisResult(configurations, effectivePackages, metadata, analysisTimeMs);
        }
    }
}