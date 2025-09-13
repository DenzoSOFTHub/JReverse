package it.denzosoft.jreverse.analyzer.bootstrap;

import it.denzosoft.jreverse.analyzer.beancreation.BeanCreationResult;
import it.denzosoft.jreverse.core.model.ComponentScanAnalysisResult;
import it.denzosoft.jreverse.core.model.AnalysisMetadata;
import it.denzosoft.jreverse.core.model.MainMethodAnalysisResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Result of Spring Boot bootstrap sequence analysis.
 * Contains comprehensive information about the application startup process,
 * including sequence diagrams, timing information, and phase analysis.
 */
public class BootstrapAnalysisResult {
    
    private final BootstrapAnalysisType analysisType;
    private final List<BootstrapSequenceStep> sequenceSteps;
    private final Map<BootstrapSequencePhase, List<BootstrapSequenceStep>> phaseSteps;
    private final Optional<MainMethodAnalysisResult> mainMethodAnalysis;
    private final Optional<ComponentScanAnalysisResult> componentScanAnalysis;
    private final Optional<BeanCreationResult> beanCreationAnalysis;
    private final BootstrapTimingInfo timingInfo;
    private final Set<String> detectedAutoConfigurations;
    private final List<String> discoveredComponents;
    private final AnalysisMetadata metadata;
    private final LocalDateTime analysisTimestamp;
    
    private BootstrapAnalysisResult(Builder builder) {
        this.analysisType = builder.analysisType;
        this.sequenceSteps = List.copyOf(builder.sequenceSteps);
        this.phaseSteps = Map.copyOf(builder.phaseSteps);
        this.mainMethodAnalysis = builder.mainMethodAnalysis;
        this.componentScanAnalysis = builder.componentScanAnalysis;
        this.beanCreationAnalysis = builder.beanCreationAnalysis;
        this.timingInfo = builder.timingInfo;
        this.detectedAutoConfigurations = Set.copyOf(builder.detectedAutoConfigurations);
        this.discoveredComponents = List.copyOf(builder.discoveredComponents);
        this.metadata = builder.metadata;
        this.analysisTimestamp = builder.analysisTimestamp;
    }
    
    // Getters
    public BootstrapAnalysisType getAnalysisType() { return analysisType; }
    public List<BootstrapSequenceStep> getSequenceSteps() { return sequenceSteps; }
    public Map<BootstrapSequencePhase, List<BootstrapSequenceStep>> getPhaseSteps() { return phaseSteps; }
    public Optional<MainMethodAnalysisResult> getMainMethodAnalysis() { return mainMethodAnalysis; }
    public Optional<ComponentScanAnalysisResult> getComponentScanAnalysis() { return componentScanAnalysis; }
    public Optional<BeanCreationResult> getBeanCreationAnalysis() { return beanCreationAnalysis; }
    public BootstrapTimingInfo getTimingInfo() { return timingInfo; }
    public Set<String> getDetectedAutoConfigurations() { return detectedAutoConfigurations; }
    public List<String> getDiscoveredComponents() { return discoveredComponents; }
    public AnalysisMetadata getMetadata() { return metadata; }
    public LocalDateTime getAnalysisTimestamp() { return analysisTimestamp; }
    
    /**
     * Convenience methods
     */
    public boolean isSpringBootApplication() { 
        return analysisType == BootstrapAnalysisType.SPRING_BOOT; 
    }
    
    public boolean hasMainMethod() {
        return mainMethodAnalysis.isPresent() && mainMethodAnalysis.get().hasMainMethod();
    }
    
    public boolean hasComponentScan() {
        return componentScanAnalysis.isPresent();
    }
    
    public boolean hasBeanCreation() {
        return beanCreationAnalysis.isPresent();
    }
    
    public int getTotalSteps() {
        return sequenceSteps.size();
    }
    
    public int getComponentCount() {
        return discoveredComponents.size();
    }
    
    public int getAutoConfigurationCount() {
        return detectedAutoConfigurations.size();
    }
    
    /**
     * Returns steps for a specific phase.
     */
    public List<BootstrapSequenceStep> getStepsForPhase(BootstrapSequencePhase phase) {
        return phaseSteps.getOrDefault(phase, List.of());
    }
    
    /**
     * Checks if a specific phase has steps.
     */
    public boolean hasPhase(BootstrapSequencePhase phase) {
        return phaseSteps.containsKey(phase) && !phaseSteps.get(phase).isEmpty();
    }
    
    /**
     * Factory method for Spring Boot applications.
     */
    public static BootstrapAnalysisResult springBootApplication(
            List<BootstrapSequenceStep> sequenceSteps,
            MainMethodAnalysisResult mainMethodAnalysis,
            ComponentScanAnalysisResult componentScanAnalysis,
            BeanCreationResult beanCreationAnalysis,
            BootstrapTimingInfo timingInfo,
            Set<String> autoConfigurations,
            List<String> components) {
        
        return new Builder()
            .analysisType(BootstrapAnalysisType.SPRING_BOOT)
            .sequenceSteps(sequenceSteps)
            .mainMethodAnalysis(mainMethodAnalysis)
            .componentScanAnalysis(componentScanAnalysis)
            .beanCreationAnalysis(beanCreationAnalysis)
            .timingInfo(timingInfo)
            .detectedAutoConfigurations(autoConfigurations)
            .discoveredComponents(components)
            .metadata(AnalysisMetadata.successful())
            .analysisTimestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * Factory method for regular Java applications.
     */
    public static BootstrapAnalysisResult regularJavaApplication(
            MainMethodAnalysisResult mainMethodAnalysis,
            AnalysisMetadata metadata) {
        
        return new Builder()
            .analysisType(BootstrapAnalysisType.REGULAR_JAVA)
            .mainMethodAnalysis(mainMethodAnalysis)
            .timingInfo(BootstrapTimingInfo.minimal())
            .metadata(metadata)
            .analysisTimestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * Factory method for failed analysis.
     */
    public static BootstrapAnalysisResult analysisError(String errorMessage) {
        return new Builder()
            .analysisType(BootstrapAnalysisType.ANALYSIS_ERROR)
            .timingInfo(BootstrapTimingInfo.empty())
            .metadata(AnalysisMetadata.error(errorMessage))
            .analysisTimestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * Factory method when no bootstrap can be detected.
     */
    public static BootstrapAnalysisResult noBootstrapDetected() {
        return new Builder()
            .analysisType(BootstrapAnalysisType.NO_BOOTSTRAP)
            .timingInfo(BootstrapTimingInfo.empty())
            .metadata(AnalysisMetadata.warning("No bootstrap sequence detected"))
            .analysisTimestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * Builder pattern for constructing BootstrapAnalysisResult instances.
     */
    public static class Builder {
        private BootstrapAnalysisType analysisType = BootstrapAnalysisType.NO_BOOTSTRAP;
        private List<BootstrapSequenceStep> sequenceSteps = List.of();
        private Map<BootstrapSequencePhase, List<BootstrapSequenceStep>> phaseSteps = Map.of();
        private Optional<MainMethodAnalysisResult> mainMethodAnalysis = Optional.empty();
        private Optional<ComponentScanAnalysisResult> componentScanAnalysis = Optional.empty();
        private Optional<BeanCreationResult> beanCreationAnalysis = Optional.empty();
        private BootstrapTimingInfo timingInfo = BootstrapTimingInfo.empty();
        private Set<String> detectedAutoConfigurations = Set.of();
        private List<String> discoveredComponents = List.of();
        private AnalysisMetadata metadata = AnalysisMetadata.successful();
        private LocalDateTime analysisTimestamp = LocalDateTime.now();
        
        public Builder analysisType(BootstrapAnalysisType analysisType) {
            this.analysisType = analysisType;
            return this;
        }
        
        public Builder sequenceSteps(List<BootstrapSequenceStep> sequenceSteps) {
            this.sequenceSteps = sequenceSteps;
            // Group steps by phase
            this.phaseSteps = sequenceSteps.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    BootstrapSequenceStep::getPhase,
                    java.util.LinkedHashMap::new,
                    java.util.stream.Collectors.toList()
                ));
            return this;
        }
        
        public Builder mainMethodAnalysis(MainMethodAnalysisResult mainMethodAnalysis) {
            this.mainMethodAnalysis = Optional.ofNullable(mainMethodAnalysis);
            return this;
        }
        
        public Builder componentScanAnalysis(ComponentScanAnalysisResult componentScanAnalysis) {
            this.componentScanAnalysis = Optional.ofNullable(componentScanAnalysis);
            return this;
        }
        
        public Builder beanCreationAnalysis(BeanCreationResult beanCreationAnalysis) {
            this.beanCreationAnalysis = Optional.ofNullable(beanCreationAnalysis);
            return this;
        }
        
        public Builder timingInfo(BootstrapTimingInfo timingInfo) {
            this.timingInfo = timingInfo;
            return this;
        }
        
        public Builder detectedAutoConfigurations(Set<String> detectedAutoConfigurations) {
            this.detectedAutoConfigurations = detectedAutoConfigurations;
            return this;
        }
        
        public Builder discoveredComponents(List<String> discoveredComponents) {
            this.discoveredComponents = discoveredComponents;
            return this;
        }
        
        public Builder metadata(AnalysisMetadata metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Builder analysisTimestamp(LocalDateTime analysisTimestamp) {
            this.analysisTimestamp = analysisTimestamp;
            return this;
        }
        
        public BootstrapAnalysisResult build() {
            return new BootstrapAnalysisResult(this);
        }
    }
    
    /**
     * Type of bootstrap analysis performed.
     */
    public enum BootstrapAnalysisType {
        SPRING_BOOT,
        REGULAR_JAVA,
        NO_BOOTSTRAP,
        ANALYSIS_ERROR
    }
}