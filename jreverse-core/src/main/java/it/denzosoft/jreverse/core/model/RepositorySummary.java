package it.denzosoft.jreverse.core.model;

/**
 * Summary of repository layer analysis.
 */
public class RepositorySummary {
    
    private final int totalRepositories;
    private final int jpaRepositories;
    private final int customRepositories;
    private final int totalIssues;
    private final String qualityRating;
    private final boolean hasGoodDataAccess;
    
    private RepositorySummary(Builder builder) {
        this.totalRepositories = builder.totalRepositories;
        this.jpaRepositories = builder.jpaRepositories;
        this.customRepositories = builder.customRepositories;
        this.totalIssues = builder.totalIssues;
        this.qualityRating = builder.qualityRating;
        this.hasGoodDataAccess = builder.hasGoodDataAccess;
    }
    
    public int getTotalRepositories() { return totalRepositories; }
    public int getJpaRepositories() { return jpaRepositories; }
    public int getCustomRepositories() { return customRepositories; }
    public int getTotalIssues() { return totalIssues; }
    public String getQualityRating() { return qualityRating; }
    public boolean hasGoodDataAccess() { return hasGoodDataAccess; }
    
    /**
     * Gets the percentage of JPA repositories.
     */
    public double getJpaPercentage() {
        return totalRepositories > 0 ? (double) jpaRepositories / totalRepositories * 100 : 0.0;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private int totalRepositories;
        private int jpaRepositories;
        private int customRepositories;
        private int totalIssues;
        private String qualityRating = "Good";
        private boolean hasGoodDataAccess = true;
        
        public Builder totalRepositories(int totalRepositories) {
            this.totalRepositories = totalRepositories;
            return this;
        }
        
        public Builder jpaRepositories(int jpaRepositories) {
            this.jpaRepositories = jpaRepositories;
            return this;
        }
        
        public Builder customRepositories(int customRepositories) {
            this.customRepositories = customRepositories;
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
        
        public Builder hasGoodDataAccess(boolean hasGoodDataAccess) {
            this.hasGoodDataAccess = hasGoodDataAccess;
            return this;
        }
        
        public RepositorySummary build() {
            return new RepositorySummary(this);
        }
    }
}