package it.denzosoft.jreverse.core.model;

/**
 * Summary of service layer analysis.
 */
public class ServiceLayerSummary {
    
    private final int totalServices;
    private final int totalPackages;
    private final int totalIssues;
    private final String qualityRating;
    private final boolean hasGoodArchitecture;
    
    private ServiceLayerSummary(Builder builder) {
        this.totalServices = builder.totalServices;
        this.totalPackages = builder.totalPackages;
        this.totalIssues = builder.totalIssues;
        this.qualityRating = builder.qualityRating;
        this.hasGoodArchitecture = builder.hasGoodArchitecture;
    }
    
    public int getTotalServices() { return totalServices; }
    public int getTotalPackages() { return totalPackages; }
    public int getTotalIssues() { return totalIssues; }
    public String getQualityRating() { return qualityRating; }
    public boolean hasGoodArchitecture() { return hasGoodArchitecture; }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private int totalServices;
        private int totalPackages;
        private int totalIssues;
        private String qualityRating = "Good";
        private boolean hasGoodArchitecture = true;
        
        public Builder totalServices(int totalServices) {
            this.totalServices = totalServices;
            return this;
        }
        
        public Builder totalPackages(int totalPackages) {
            this.totalPackages = totalPackages;
            return this;
        }
        
        public Builder totalIssues(int totalIssues) {
            this.totalIssues = totalIssues;
            return this;
        }
        
        public Builder qualityRating(String qualityRating) {
            this.qualityRating = qualityRating;
            return this;
        }
        
        public Builder hasGoodArchitecture(boolean hasGoodArchitecture) {
            this.hasGoodArchitecture = hasGoodArchitecture;
            return this;
        }
        
        public ServiceLayerSummary build() {
            return new ServiceLayerSummary(this);
        }
    }
}