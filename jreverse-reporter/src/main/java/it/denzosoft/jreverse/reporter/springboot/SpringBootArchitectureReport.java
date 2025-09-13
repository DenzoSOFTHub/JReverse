package it.denzosoft.jreverse.reporter.springboot;

import it.denzosoft.jreverse.core.model.ComponentScanAnalysisResult;
import it.denzosoft.jreverse.core.model.ConfigurationAnalysisResult;
import it.denzosoft.jreverse.core.model.RestEndpointAnalysisResult;
import it.denzosoft.jreverse.core.model.WebMvcAnalysisResult;

/**
 * Comprehensive report for Spring Boot application architecture analysis.
 */
public class SpringBootArchitectureReport {
    
    private final String jarLocation;
    private final ConfigurationAnalysisResult configurationAnalysis;
    private final ComponentScanAnalysisResult componentScanAnalysis;
    private final RestEndpointAnalysisResult restEndpointAnalysis;
    private final WebMvcAnalysisResult webMvcAnalysis;
    private final long analysisTimeMs;
    private final String errorMessage;
    private final boolean successful;
    
    private SpringBootArchitectureReport(String jarLocation,
                                       ConfigurationAnalysisResult configurationAnalysis,
                                       ComponentScanAnalysisResult componentScanAnalysis,
                                       RestEndpointAnalysisResult restEndpointAnalysis,
                                       WebMvcAnalysisResult webMvcAnalysis,
                                       long analysisTimeMs,
                                       String errorMessage,
                                       boolean successful) {
        this.jarLocation = jarLocation;
        this.configurationAnalysis = configurationAnalysis;
        this.componentScanAnalysis = componentScanAnalysis;
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
    
    public ConfigurationAnalysisResult getConfigurationAnalysis() {
        return configurationAnalysis;
    }
    
    public ComponentScanAnalysisResult getComponentScanAnalysis() {
        return componentScanAnalysis;
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
    
    @Override
    public String toString() {
        return "SpringBootArchitectureReport{" +
                "jarLocation='" + jarLocation + '\'' +
                ", successful=" + successful +
                ", analysisTime=" + analysisTimeMs + "ms" +
                '}';
    }
    
    public static class Builder {
        private String jarLocation;
        private ConfigurationAnalysisResult configurationAnalysis;
        private ComponentScanAnalysisResult componentScanAnalysis;
        private RestEndpointAnalysisResult restEndpointAnalysis;
        private WebMvcAnalysisResult webMvcAnalysis;
        private long analysisTimeMs = 0L;
        private String errorMessage;
        
        public Builder jarLocation(String jarLocation) {
            this.jarLocation = jarLocation;
            return this;
        }
        
        public Builder configurationAnalysis(ConfigurationAnalysisResult configurationAnalysis) {
            this.configurationAnalysis = configurationAnalysis;
            return this;
        }
        
        public Builder componentScanAnalysis(ComponentScanAnalysisResult componentScanAnalysis) {
            this.componentScanAnalysis = componentScanAnalysis;
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
        
        public SpringBootArchitectureReport build() {
            boolean successful = errorMessage == null &&
                                configurationAnalysis != null &&
                                componentScanAnalysis != null &&
                                restEndpointAnalysis != null &&
                                webMvcAnalysis != null;
            
            return new SpringBootArchitectureReport(jarLocation, configurationAnalysis, componentScanAnalysis,
                                                   restEndpointAnalysis, webMvcAnalysis, analysisTimeMs,
                                                   errorMessage, successful);
        }
    }
}