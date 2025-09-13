package it.denzosoft.jreverse.core.model;

/**
 * Summary statistics for autowiring analysis.
 */
public class AutowiredSummary {
    
    private final int totalClasses;
    private final int classesWithDependencies;
    private final int totalDependencies;
    private final int constructorInjections;
    private final int fieldInjections;
    private final int setterInjections;
    private final int methodInjections;
    private final int resourceInjections;
    private final int injectAnnotations;
    private final int qualifiedDependencies;
    private final int optionalDependencies;
    private final int collectionDependencies;
    private final int totalIssues;
    private final int warningIssues;
    private final int errorIssues;
    
    private AutowiredSummary(Builder builder) {
        this.totalClasses = builder.totalClasses;
        this.classesWithDependencies = builder.classesWithDependencies;
        this.totalDependencies = builder.totalDependencies;
        this.constructorInjections = builder.constructorInjections;
        this.fieldInjections = builder.fieldInjections;
        this.setterInjections = builder.setterInjections;
        this.methodInjections = builder.methodInjections;
        this.resourceInjections = builder.resourceInjections;
        this.injectAnnotations = builder.injectAnnotations;
        this.qualifiedDependencies = builder.qualifiedDependencies;
        this.optionalDependencies = builder.optionalDependencies;
        this.collectionDependencies = builder.collectionDependencies;
        this.totalIssues = builder.totalIssues;
        this.warningIssues = builder.warningIssues;
        this.errorIssues = builder.errorIssues;
    }
    
    // Getters
    public int getTotalClasses() { return totalClasses; }
    public int getClassesWithDependencies() { return classesWithDependencies; }
    public int getTotalDependencies() { return totalDependencies; }
    public int getConstructorInjections() { return constructorInjections; }
    public int getFieldInjections() { return fieldInjections; }
    public int getSetterInjections() { return setterInjections; }
    public int getMethodInjections() { return methodInjections; }
    public int getResourceInjections() { return resourceInjections; }
    public int getInjectAnnotations() { return injectAnnotations; }
    public int getQualifiedDependencies() { return qualifiedDependencies; }
    public int getOptionalDependencies() { return optionalDependencies; }
    public int getCollectionDependencies() { return collectionDependencies; }
    public int getTotalIssues() { return totalIssues; }
    public int getWarningIssues() { return warningIssues; }
    public int getErrorIssues() { return errorIssues; }
    
    /**
     * Gets the percentage of classes that have dependencies.
     */
    public double getDependencyPercentage() {
        return totalClasses > 0 ? (double) classesWithDependencies / totalClasses * 100 : 0.0;
    }
    
    /**
     * Gets the percentage of constructor injections (recommended pattern).
     */
    public double getConstructorInjectionPercentage() {
        return totalDependencies > 0 ? (double) constructorInjections / totalDependencies * 100 : 0.0;
    }
    
    /**
     * Gets the percentage of field injections (discouraged pattern).
     */
    public double getFieldInjectionPercentage() {
        return totalDependencies > 0 ? (double) fieldInjections / totalDependencies * 100 : 0.0;
    }
    
    /**
     * Checks if the autowiring patterns follow Spring best practices.
     */
    public boolean hasGoodAutowiringPractices() {
        double constructorPercentage = getConstructorInjectionPercentage();
        double fieldPercentage = getFieldInjectionPercentage();
        
        // Good practices: >70% constructor injection, <30% field injection
        return constructorPercentage >= 70.0 && fieldPercentage <= 30.0;
    }
    
    /**
     * Gets a descriptive assessment of autowiring quality.
     */
    public String getQualityAssessment() {
        double constructorPercentage = getConstructorInjectionPercentage();
        
        if (constructorPercentage >= 80.0) {
            return "Excellent - Mostly constructor injection";
        } else if (constructorPercentage >= 60.0) {
            return "Good - Balanced injection patterns";
        } else if (constructorPercentage >= 40.0) {
            return "Fair - Mixed injection patterns";
        } else {
            return "Poor - Mostly field/setter injection";
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private int totalClasses;
        private int classesWithDependencies;
        private int totalDependencies;
        private int constructorInjections;
        private int fieldInjections;
        private int setterInjections;
        private int methodInjections;
        private int resourceInjections;
        private int injectAnnotations;
        private int qualifiedDependencies;
        private int optionalDependencies;
        private int collectionDependencies;
        private int totalIssues;
        private int warningIssues;
        private int errorIssues;
        
        public Builder totalClasses(int totalClasses) {
            this.totalClasses = totalClasses;
            return this;
        }
        
        public Builder classesWithDependencies(int classesWithDependencies) {
            this.classesWithDependencies = classesWithDependencies;
            return this;
        }
        
        public Builder totalDependencies(int totalDependencies) {
            this.totalDependencies = totalDependencies;
            return this;
        }
        
        public Builder constructorInjections(int constructorInjections) {
            this.constructorInjections = constructorInjections;
            return this;
        }
        
        public Builder fieldInjections(int fieldInjections) {
            this.fieldInjections = fieldInjections;
            return this;
        }
        
        public Builder setterInjections(int setterInjections) {
            this.setterInjections = setterInjections;
            return this;
        }
        
        public Builder methodInjections(int methodInjections) {
            this.methodInjections = methodInjections;
            return this;
        }
        
        public Builder resourceInjections(int resourceInjections) {
            this.resourceInjections = resourceInjections;
            return this;
        }
        
        public Builder injectAnnotations(int injectAnnotations) {
            this.injectAnnotations = injectAnnotations;
            return this;
        }
        
        public Builder qualifiedDependencies(int qualifiedDependencies) {
            this.qualifiedDependencies = qualifiedDependencies;
            return this;
        }
        
        public Builder optionalDependencies(int optionalDependencies) {
            this.optionalDependencies = optionalDependencies;
            return this;
        }
        
        public Builder collectionDependencies(int collectionDependencies) {
            this.collectionDependencies = collectionDependencies;
            return this;
        }
        
        public Builder totalIssues(int totalIssues) {
            this.totalIssues = totalIssues;
            return this;
        }
        
        public Builder warningIssues(int warningIssues) {
            this.warningIssues = warningIssues;
            return this;
        }
        
        public Builder errorIssues(int errorIssues) {
            this.errorIssues = errorIssues;
            return this;
        }
        
        public AutowiredSummary build() {
            return new AutowiredSummary(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("AutowiredSummary{classes=%d/%d, dependencies=%d, constructor=%d%%, field=%d%%, issues=%d}",
            classesWithDependencies, totalClasses, totalDependencies,
            Math.round(getConstructorInjectionPercentage()), Math.round(getFieldInjectionPercentage()), totalIssues);
    }
}