package it.denzosoft.jreverse.reporter.springboot;

import it.denzosoft.jreverse.core.model.ComponentScanAnalysisResult;
import it.denzosoft.jreverse.core.model.ConfigurationAnalysisResult;
import it.denzosoft.jreverse.core.model.MainMethodAnalysisResult;
import it.denzosoft.jreverse.core.model.RestEndpointAnalysisResult;
import it.denzosoft.jreverse.core.model.WebMvcAnalysisResult;

/**
 * Comprehensive report for Spring Boot application entrypoints and bootstrap analysis.
 */
public class SpringBootEntrypointReport {
    
    private final String jarLocation;
    private final MainMethodAnalysisResult mainMethodAnalysis;
    private final ComponentScanAnalysisResult componentScanAnalysis;
    private final RestEndpointAnalysisResult restEndpointAnalysis;
    private final WebMvcAnalysisResult webMvcAnalysis;
    private final ConfigurationAnalysisResult configurationAnalysis;
    private final long analysisTimeMs;
    private final String errorMessage;
    private final boolean successful;
    
    private SpringBootEntrypointReport(String jarLocation,
                                     MainMethodAnalysisResult mainMethodAnalysis,
                                     ComponentScanAnalysisResult componentScanAnalysis,
                                     RestEndpointAnalysisResult restEndpointAnalysis,
                                     WebMvcAnalysisResult webMvcAnalysis,
                                     ConfigurationAnalysisResult configurationAnalysis,
                                     long analysisTimeMs,
                                     String errorMessage,
                                     boolean successful) {
        this.jarLocation = jarLocation;
        this.mainMethodAnalysis = mainMethodAnalysis;
        this.componentScanAnalysis = componentScanAnalysis;
        this.restEndpointAnalysis = restEndpointAnalysis;
        this.webMvcAnalysis = webMvcAnalysis;
        this.configurationAnalysis = configurationAnalysis;
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
    
    public MainMethodAnalysisResult getMainMethodAnalysis() {
        return mainMethodAnalysis;
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
    
    public ConfigurationAnalysisResult getConfigurationAnalysis() {
        return configurationAnalysis;
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
    
    public int getTotalConfigurations() {
        return configurationAnalysis != null ? configurationAnalysis.getConfigurationCount() : 0;
    }
    
    public int getTotalBeanDefinitions() {
        return configurationAnalysis != null ? configurationAnalysis.getBeanDefinitionCount() : 0;
    }
    
    @Override
    public String toString() {
        return "SpringBootEntrypointReport{" +
                "jarLocation='" + jarLocation + '\'' +
                ", successful=" + successful +
                ", endpoints=" + getTotalEndpoints() +
                ", configurations=" + getTotalConfigurations() +
                ", analysisTime=" + analysisTimeMs + "ms" +
                '}';
    }
    
    public static class Builder {
        private String jarLocation;
        private MainMethodAnalysisResult mainMethodAnalysis;
        private ComponentScanAnalysisResult componentScanAnalysis;
        private RestEndpointAnalysisResult restEndpointAnalysis;
        private WebMvcAnalysisResult webMvcAnalysis;
        private ConfigurationAnalysisResult configurationAnalysis;
        private long analysisTimeMs = 0L;
        private String errorMessage;
        
        public Builder jarLocation(String jarLocation) {
            this.jarLocation = jarLocation;
            return this;
        }
        
        public Builder mainMethodAnalysis(MainMethodAnalysisResult mainMethodAnalysis) {
            this.mainMethodAnalysis = mainMethodAnalysis;
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
        
        public Builder configurationAnalysis(ConfigurationAnalysisResult configurationAnalysis) {
            this.configurationAnalysis = configurationAnalysis;
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
        
        public SpringBootEntrypointReport build() {
            boolean successful = errorMessage == null &&
                                mainMethodAnalysis != null &&
                                componentScanAnalysis != null &&
                                restEndpointAnalysis != null &&
                                webMvcAnalysis != null &&
                                configurationAnalysis != null;
            
            return new SpringBootEntrypointReport(jarLocation, mainMethodAnalysis, componentScanAnalysis,
                                                 restEndpointAnalysis, webMvcAnalysis, configurationAnalysis,
                                                 analysisTimeMs, errorMessage, successful);
        }
    }
}