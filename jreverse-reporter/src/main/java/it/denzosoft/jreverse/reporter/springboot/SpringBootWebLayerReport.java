package it.denzosoft.jreverse.reporter.springboot;

import it.denzosoft.jreverse.core.model.RestEndpointAnalysisResult;
import it.denzosoft.jreverse.core.model.WebMvcAnalysisResult;

/**
 * Specialized report for Spring Boot web layer analysis.
 */
public class SpringBootWebLayerReport {
    
    private final String jarLocation;
    private final RestEndpointAnalysisResult restEndpointAnalysis;
    private final WebMvcAnalysisResult webMvcAnalysis;
    private final long analysisTimeMs;
    private final String errorMessage;
    private final boolean successful;
    
    private SpringBootWebLayerReport(String jarLocation,
                                   RestEndpointAnalysisResult restEndpointAnalysis,
                                   WebMvcAnalysisResult webMvcAnalysis,
                                   long analysisTimeMs,
                                   String errorMessage,
                                   boolean successful) {
        this.jarLocation = jarLocation;
        this.restEndpointAnalysis = restEndpointAnalysis;
        this.webMvcAnalysis = webMvcAnalysis;
        this.analysisTimeMs = analysisTimeMs;
        this.errorMessage = errorMessage;
        this.successful = successful;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String getJarLocation() {
        return jarLocation;
    }
    
    public RestEndpointAnalysisResult getRestEndpointAnalysis() {
        return restEndpointAnalysis;
    }
    
    public WebMvcAnalysisResult getWebMvcAnalysis() {
        return webMvcAnalysis;
    }
    
    public long getAnalysisTimeMs() {
        return analysisTimeMs;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public boolean hasError() {
        return errorMessage != null;
    }
    
    public int getTotalEndpoints() {
        int count = 0;
        if (restEndpointAnalysis != null) {
            count += restEndpointAnalysis.getEndpointCount();
        }
        if (webMvcAnalysis != null) {
            count += webMvcAnalysis.getMappingCount();
        }
        return count;
    }
    
    public int getTotalControllers() {
        int count = 0;
        if (restEndpointAnalysis != null) {
            count += restEndpointAnalysis.getControllerCount();
        }
        if (webMvcAnalysis != null) {
            count += webMvcAnalysis.getControllerCount();
        }
        return count;
    }
    
    @Override
    public String toString() {
        return "SpringBootWebLayerReport{" +
                "jarLocation='" + jarLocation + '\'' +
                ", successful=" + successful +
                ", endpoints=" + getTotalEndpoints() +
                ", controllers=" + getTotalControllers() +
                ", analysisTime=" + analysisTimeMs + "ms" +
                '}';
    }
    
    public static class Builder {
        private String jarLocation;
        private RestEndpointAnalysisResult restEndpointAnalysis;
        private WebMvcAnalysisResult webMvcAnalysis;
        private long analysisTimeMs = 0L;
        private String errorMessage;
        
        public Builder jarLocation(String jarLocation) {
            this.jarLocation = jarLocation;
            return this;
        }
        
        public Builder restEndpointAnalysis(RestEndpointAnalysisResult restEndpointAnalysis) {
            this.restEndpointAnalysis = restEndpointAnalysis;
            return this;
        }
        
        public Builder webMvcAnalysis(WebMvcAnalysisResult webMvcAnalysis) {
            this.webMvcAnalysis = webMvcAnalysis;
            return this;
        }
        
        public Builder analysisTimeMs(long analysisTimeMs) {
            this.analysisTimeMs = Math.max(0L, analysisTimeMs);
            return this;
        }
        
        public Builder error(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
        
        public SpringBootWebLayerReport build() {
            boolean successful = errorMessage == null &&
                                restEndpointAnalysis != null &&
                                webMvcAnalysis != null;
            
            return new SpringBootWebLayerReport(jarLocation, restEndpointAnalysis, webMvcAnalysis,
                                               analysisTimeMs, errorMessage, successful);
        }
    }
}