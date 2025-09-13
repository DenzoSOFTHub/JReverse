package it.denzosoft.jreverse.core.model;

/**
 * Metrics for repository layer analysis.
 */
public class RepositoryMetrics {
    
    private final int totalRepositories;
    private final int jpaRepositories;
    private final int customRepositories;
    private final int totalQueryMethods;
    private final int nativeQueries;
    private final int customQueries;
    private final double averageMethodsPerRepository;
    
    private RepositoryMetrics(Builder builder) {
        this.totalRepositories = builder.totalRepositories;
        this.jpaRepositories = builder.jpaRepositories;
        this.customRepositories = builder.customRepositories;
        this.totalQueryMethods = builder.totalQueryMethods;
        this.nativeQueries = builder.nativeQueries;
        this.customQueries = builder.customQueries;
        this.averageMethodsPerRepository = builder.averageMethodsPerRepository;
    }
    
    public int getTotalRepositories() { return totalRepositories; }
    public int getJpaRepositories() { return jpaRepositories; }
    public int getCustomRepositories() { return customRepositories; }
    public int getTotalQueryMethods() { return totalQueryMethods; }
    public int getNativeQueries() { return nativeQueries; }
    public int getCustomQueries() { return customQueries; }
    public double getAverageMethodsPerRepository() { return averageMethodsPerRepository; }
    
    /**
     * Gets the percentage of JPA repositories.
     */
    public double getJpaRepositoryPercentage() {
        return totalRepositories > 0 ? (double) jpaRepositories / totalRepositories * 100 : 0.0;
    }
    
    /**
     * Gets the percentage of native queries.
     */
    public double getNativeQueryPercentage() {
        return totalQueryMethods > 0 ? (double) nativeQueries / totalQueryMethods * 100 : 0.0;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private int totalRepositories;
        private int jpaRepositories;
        private int customRepositories;
        private int totalQueryMethods;
        private int nativeQueries;
        private int customQueries;
        private double averageMethodsPerRepository;
        
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
        
        public Builder totalQueryMethods(int totalQueryMethods) {
            this.totalQueryMethods = totalQueryMethods;
            return this;
        }
        
        public Builder nativeQueries(int nativeQueries) {
            this.nativeQueries = nativeQueries;
            return this;
        }
        
        public Builder customQueries(int customQueries) {
            this.customQueries = customQueries;
            return this;
        }
        
        public Builder averageMethodsPerRepository(double averageMethodsPerRepository) {
            this.averageMethodsPerRepository = averageMethodsPerRepository;
            return this;
        }
        
        public RepositoryMetrics build() {
            return new RepositoryMetrics(this);
        }
    }
}