package it.denzosoft.jreverse.core.model;

/**
 * Metrics for service layer analysis.
 */
public class ServiceLayerMetrics {
    
    private final int totalServices;
    private final int transactionalServices;
    private final int lazyServices;
    private final double averageDependencies;
    private final int maxDependencies;
    private final int packagesWithServices;
    
    private ServiceLayerMetrics(Builder builder) {
        this.totalServices = builder.totalServices;
        this.transactionalServices = builder.transactionalServices;
        this.lazyServices = builder.lazyServices;
        this.averageDependencies = builder.averageDependencies;
        this.maxDependencies = builder.maxDependencies;
        this.packagesWithServices = builder.packagesWithServices;
    }
    
    public int getTotalServices() { return totalServices; }
    public int getTransactionalServices() { return transactionalServices; }
    public int getLazyServices() { return lazyServices; }
    public double getAverageDependencies() { return averageDependencies; }
    public int getMaxDependencies() { return maxDependencies; }
    public int getPackagesWithServices() { return packagesWithServices; }
    
    /**
     * Gets the percentage of transactional services.
     */
    public double getTransactionalPercentage() {
        return totalServices > 0 ? (double) transactionalServices / totalServices * 100 : 0.0;
    }
    
    /**
     * Gets the percentage of lazy services.
     */
    public double getLazyPercentage() {
        return totalServices > 0 ? (double) lazyServices / totalServices * 100 : 0.0;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private int totalServices;
        private int transactionalServices;
        private int lazyServices;
        private double averageDependencies;
        private int maxDependencies;
        private int packagesWithServices;
        
        public Builder totalServices(int totalServices) {
            this.totalServices = totalServices;
            return this;
        }
        
        public Builder transactionalServices(int transactionalServices) {
            this.transactionalServices = transactionalServices;
            return this;
        }
        
        public Builder lazyServices(int lazyServices) {
            this.lazyServices = lazyServices;
            return this;
        }
        
        public Builder averageDependencies(double averageDependencies) {
            this.averageDependencies = averageDependencies;
            return this;
        }
        
        public Builder maxDependencies(int maxDependencies) {
            this.maxDependencies = maxDependencies;
            return this;
        }
        
        public Builder packagesWithServices(int packagesWithServices) {
            this.packagesWithServices = packagesWithServices;
            return this;
        }
        
        public ServiceLayerMetrics build() {
            return new ServiceLayerMetrics(this);
        }
    }
}