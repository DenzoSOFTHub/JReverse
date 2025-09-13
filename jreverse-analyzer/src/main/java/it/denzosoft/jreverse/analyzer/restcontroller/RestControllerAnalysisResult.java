package it.denzosoft.jreverse.analyzer.restcontroller;

import it.denzosoft.jreverse.analyzer.mainmethod.AnalysisMetadata;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Result of REST controller analysis containing information about discovered REST controllers
 * and their characteristics, including statistical summaries and organizational insights.
 */
public class RestControllerAnalysisResult {
    
    private final List<RestControllerInfo> restControllers;
    private final RestControllerStatistics statistics;
    private final AnalysisMetadata metadata;
    
    private RestControllerAnalysisResult(List<RestControllerInfo> restControllers,
                                       RestControllerStatistics statistics,
                                       AnalysisMetadata metadata) {
        this.restControllers = Collections.unmodifiableList(restControllers);
        this.statistics = statistics;
        this.metadata = metadata;
    }
    
    /**
     * Creates a successful analysis result.
     */
    public static RestControllerAnalysisResult success(List<RestControllerInfo> restControllers) {
        RestControllerStatistics statistics = RestControllerStatistics.from(restControllers);
        return new RestControllerAnalysisResult(
            restControllers,
            statistics,
            AnalysisMetadata.successful()
        );
    }
    
    /**
     * Creates a result when no REST controllers are found.
     */
    public static RestControllerAnalysisResult noControllersFound() {
        return new RestControllerAnalysisResult(
            Collections.emptyList(),
            RestControllerStatistics.empty(),
            AnalysisMetadata.warning("No REST controllers found")
        );
    }
    
    /**
     * Creates a result for analysis failure.
     */
    public static RestControllerAnalysisResult error(String errorMessage) {
        return new RestControllerAnalysisResult(
            Collections.emptyList(),
            RestControllerStatistics.empty(),
            AnalysisMetadata.error(errorMessage)
        );
    }
    
    /**
     * Creates a result with timing information.
     */
    public static RestControllerAnalysisResult withTiming(List<RestControllerInfo> restControllers, 
                                                        long analysisTimeMs) {
        RestControllerStatistics statistics = RestControllerStatistics.from(restControllers);
        return new RestControllerAnalysisResult(
            restControllers,
            statistics,
            AnalysisMetadata.withTiming(AnalysisMetadata.AnalysisStatus.SUCCESS, 
                                      "Analysis completed successfully", analysisTimeMs)
        );
    }
    
    // Getters
    public List<RestControllerInfo> getRestControllers() { return restControllers; }
    public RestControllerStatistics getStatistics() { return statistics; }
    public AnalysisMetadata getMetadata() { return metadata; }
    
    public boolean hasRestControllers() { return !restControllers.isEmpty(); }
    public int getControllerCount() { return restControllers.size(); }
    public boolean hasAnalysisWarnings() { return metadata.hasWarnings(); }
    public boolean hasAnalysisErrors() { return metadata.hasErrors(); }
    
    /**
     * Gets REST controllers filtered by type.
     */
    public List<RestControllerInfo> getControllersByType(RestControllerInfo.RestControllerType type) {
        return restControllers.stream()
            .filter(controller -> controller.getControllerType() == type)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets REST controllers grouped by package.
     */
    public Map<String, List<RestControllerInfo>> getControllersByPackage() {
        return restControllers.stream()
            .collect(Collectors.groupingBy(RestControllerInfo::getPackageName));
    }
    
    /**
     * Gets REST controllers that have base paths defined.
     */
    public List<RestControllerInfo> getControllersWithBasePaths() {
        return restControllers.stream()
            .filter(controller -> controller.getBasePath().isPresent())
            .collect(Collectors.toList());
    }
    
    /**
     * Gets REST controllers that have security annotations.
     */
    public List<RestControllerInfo> getControllersWithSecurity() {
        return restControllers.stream()
            .filter(RestControllerInfo::hasSecurityAnnotations)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets REST controllers that have CORS configuration.
     */
    public List<RestControllerInfo> getControllersWithCors() {
        return restControllers.stream()
            .filter(RestControllerInfo::hasCrossOriginConfig)
            .collect(Collectors.toList());
    }
    
    /**
     * Statistics about the REST controller analysis.
     */
    public static class RestControllerStatistics {
        
        private final int totalControllers;
        private final int pureRestControllers;
        private final int hybridControllers;
        private final int controllersWithBasePath;
        private final int controllersWithSecurity;
        private final int controllersWithCors;
        private final int controllersInHierarchy;
        private final int uniquePackages;
        
        private RestControllerStatistics(int totalControllers,
                                       int pureRestControllers,
                                       int hybridControllers,
                                       int controllersWithBasePath,
                                       int controllersWithSecurity,
                                       int controllersWithCors,
                                       int controllersInHierarchy,
                                       int uniquePackages) {
            this.totalControllers = totalControllers;
            this.pureRestControllers = pureRestControllers;
            this.hybridControllers = hybridControllers;
            this.controllersWithBasePath = controllersWithBasePath;
            this.controllersWithSecurity = controllersWithSecurity;
            this.controllersWithCors = controllersWithCors;
            this.controllersInHierarchy = controllersInHierarchy;
            this.uniquePackages = uniquePackages;
        }
        
        public static RestControllerStatistics from(List<RestControllerInfo> controllers) {
            if (controllers.isEmpty()) {
                return empty();
            }
            
            int pureRest = (int) controllers.stream()
                .filter(RestControllerInfo::isPureRestController)
                .count();
            
            int hybrid = (int) controllers.stream()
                .filter(RestControllerInfo::isHybridController)
                .count();
            
            int withBasePath = (int) controllers.stream()
                .filter(c -> c.getBasePath().isPresent())
                .count();
            
            int withSecurity = (int) controllers.stream()
                .filter(RestControllerInfo::hasSecurityAnnotations)
                .count();
            
            int withCors = (int) controllers.stream()
                .filter(RestControllerInfo::hasCrossOriginConfig)
                .count();
            
            int inHierarchy = (int) controllers.stream()
                .filter(RestControllerInfo::isInheritanceHierarchy)
                .count();
            
            int uniquePackages = (int) controllers.stream()
                .map(RestControllerInfo::getPackageName)
                .distinct()
                .count();
            
            return new RestControllerStatistics(
                controllers.size(),
                pureRest,
                hybrid,
                withBasePath,
                withSecurity,
                withCors,
                inHierarchy,
                uniquePackages
            );
        }
        
        public static RestControllerStatistics empty() {
            return new RestControllerStatistics(0, 0, 0, 0, 0, 0, 0, 0);
        }
        
        // Getters
        public int getTotalControllers() { return totalControllers; }
        public int getPureRestControllers() { return pureRestControllers; }
        public int getHybridControllers() { return hybridControllers; }
        public int getControllersWithBasePath() { return controllersWithBasePath; }
        public int getControllersWithSecurity() { return controllersWithSecurity; }
        public int getControllersWithCors() { return controllersWithCors; }
        public int getControllersInHierarchy() { return controllersInHierarchy; }
        public int getUniquePackages() { return uniquePackages; }
        
        public double getPureRestPercentage() {
            return totalControllers > 0 ? (double) pureRestControllers / totalControllers * 100 : 0;
        }
        
        public double getHybridPercentage() {
            return totalControllers > 0 ? (double) hybridControllers / totalControllers * 100 : 0;
        }
        
        public double getSecurityCoverage() {
            return totalControllers > 0 ? (double) controllersWithSecurity / totalControllers * 100 : 0;
        }
        
        public double getCorsCoverage() {
            return totalControllers > 0 ? (double) controllersWithCors / totalControllers * 100 : 0;
        }
        
        @Override
        public String toString() {
            return "RestControllerStatistics{" +
                    "totalControllers=" + totalControllers +
                    ", pureRestControllers=" + pureRestControllers +
                    ", hybridControllers=" + hybridControllers +
                    ", controllersWithBasePath=" + controllersWithBasePath +
                    ", controllersWithSecurity=" + controllersWithSecurity +
                    ", controllersWithCors=" + controllersWithCors +
                    ", uniquePackages=" + uniquePackages +
                    '}';
        }
    }
}