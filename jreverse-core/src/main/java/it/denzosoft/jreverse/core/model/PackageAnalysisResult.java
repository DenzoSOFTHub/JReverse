package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Result of package structure analysis containing package hierarchy,
 * naming conventions analysis, organizational patterns and metrics.
 */
public final class PackageAnalysisResult {
    
    private final PackageHierarchy hierarchy;
    private final NamingConventionResult namingConventionResult;
    private final OrganizationAnalysisResult organizationResult;
    private final CohesionMetrics cohesionMetrics;
    private final List<ArchitecturalAntiPattern> antiPatterns;
    private final AnalysisMetadata metadata;
    private final long analysisTimeMs;
    
    private PackageAnalysisResult(Builder builder) {
        this.hierarchy = builder.hierarchy;
        this.namingConventionResult = builder.namingConventionResult;
        this.organizationResult = builder.organizationResult;
        this.cohesionMetrics = builder.cohesionMetrics;
        this.antiPatterns = Collections.unmodifiableList(builder.antiPatterns);
        this.metadata = Objects.requireNonNull(builder.metadata, "metadata cannot be null");
        this.analysisTimeMs = builder.analysisTimeMs;
    }
    
    public PackageHierarchy getHierarchy() {
        return hierarchy;
    }
    
    public NamingConventionResult getNamingConventionResult() {
        return namingConventionResult;
    }
    
    public OrganizationAnalysisResult getOrganizationResult() {
        return organizationResult;
    }
    
    public CohesionMetrics getCohesionMetrics() {
        return cohesionMetrics;
    }
    
    public List<ArchitecturalAntiPattern> getAntiPatterns() {
        return antiPatterns;
    }
    
    public AnalysisMetadata getMetadata() {
        return metadata;
    }
    
    public long getAnalysisTimeMs() {
        return analysisTimeMs;
    }
    
    public boolean isSuccessful() {
        return metadata.isSuccessful();
    }
    
    public boolean hasIssues() {
        return !antiPatterns.isEmpty() || 
               (namingConventionResult != null && namingConventionResult.hasViolations()) ||
               (organizationResult != null && organizationResult.hasIssues());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PackageAnalysisResult that = (PackageAnalysisResult) obj;
        return analysisTimeMs == that.analysisTimeMs &&
               Objects.equals(hierarchy, that.hierarchy) &&
               Objects.equals(namingConventionResult, that.namingConventionResult) &&
               Objects.equals(organizationResult, that.organizationResult) &&
               Objects.equals(cohesionMetrics, that.cohesionMetrics) &&
               Objects.equals(antiPatterns, that.antiPatterns) &&
               Objects.equals(metadata, that.metadata);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(hierarchy, namingConventionResult, organizationResult, 
                          cohesionMetrics, antiPatterns, metadata, analysisTimeMs);
    }
    
    @Override
    public String toString() {
        return "PackageAnalysisResult{" +
                "packagesCount=" + (hierarchy != null ? hierarchy.getPackageCount() : 0) +
                ", hasIssues=" + hasIssues() +
                ", analysisTimeMs=" + analysisTimeMs +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static PackageAnalysisResult failed(String errorMessage) {
        return builder()
                .metadata(AnalysisMetadata.error(errorMessage))
                .build();
    }
    
    public static PackageAnalysisResult error(String errorMessage) {
        return failed(errorMessage);
    }
    
    public static class Builder {
        private PackageHierarchy hierarchy;
        private NamingConventionResult namingConventionResult;
        private OrganizationAnalysisResult organizationResult;
        private CohesionMetrics cohesionMetrics;
        private List<ArchitecturalAntiPattern> antiPatterns = Collections.emptyList();
        private AnalysisMetadata metadata;
        private long analysisTimeMs;
        
        public Builder hierarchy(PackageHierarchy hierarchy) {
            this.hierarchy = hierarchy;
            return this;
        }
        
        public Builder namingConventionResult(NamingConventionResult namingConventionResult) {
            this.namingConventionResult = namingConventionResult;
            return this;
        }
        
        public Builder organizationResult(OrganizationAnalysisResult organizationResult) {
            this.organizationResult = organizationResult;
            return this;
        }
        
        public Builder cohesionMetrics(CohesionMetrics cohesionMetrics) {
            this.cohesionMetrics = cohesionMetrics;
            return this;
        }
        
        public Builder antiPatterns(List<ArchitecturalAntiPattern> antiPatterns) {
            this.antiPatterns = antiPatterns != null ? antiPatterns : Collections.emptyList();
            return this;
        }
        
        public Builder metadata(AnalysisMetadata metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Builder analysisTimeMs(long analysisTimeMs) {
            this.analysisTimeMs = analysisTimeMs;
            return this;
        }
        
        public PackageAnalysisResult build() {
            if (metadata == null) {
                metadata = AnalysisMetadata.successful();
            }
            return new PackageAnalysisResult(this);
        }
    }
}