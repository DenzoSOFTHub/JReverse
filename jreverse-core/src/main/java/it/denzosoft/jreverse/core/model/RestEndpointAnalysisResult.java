package it.denzosoft.jreverse.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Result of REST endpoint analysis containing all discovered endpoints.
 */
public class RestEndpointAnalysisResult {
    
    private final List<RestEndpointInfo> endpoints;
    private final List<String> controllerClasses;
    private final Map<String, Integer> endpointsByMethod;
    private final AnalysisMetadata metadata;
    private final long analysisTimeMs;
    
    private RestEndpointAnalysisResult(List<RestEndpointInfo> endpoints,
                                     List<String> controllerClasses,
                                     Map<String, Integer> endpointsByMethod,
                                     AnalysisMetadata metadata,
                                     long analysisTimeMs) {
        this.endpoints = Collections.unmodifiableList(new ArrayList<>(endpoints));
        this.controllerClasses = Collections.unmodifiableList(new ArrayList<>(controllerClasses));
        this.endpointsByMethod = Collections.unmodifiableMap(new HashMap<>(endpointsByMethod));
        this.metadata = metadata;
        this.analysisTimeMs = analysisTimeMs;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static RestEndpointAnalysisResult noEndpoints() {
        return builder()
            .metadata(AnalysisMetadata.warning("No REST endpoints found"))
            .build();
    }
    
    public static RestEndpointAnalysisResult error(String errorMessage) {
        return builder()
            .metadata(AnalysisMetadata.error(errorMessage))
            .build();
    }
    
    public List<RestEndpointInfo> getEndpoints() {
        return endpoints;
    }
    
    public List<String> getControllerClasses() {
        return controllerClasses;
    }
    
    public Map<String, Integer> getEndpointsByMethod() {
        return endpointsByMethod;
    }
    
    public AnalysisMetadata getMetadata() {
        return metadata;
    }
    
    public long getAnalysisTimeMs() {
        return analysisTimeMs;
    }
    
    public int getEndpointCount() {
        return endpoints.size();
    }
    
    public int getControllerCount() {
        return controllerClasses.size();
    }
    
    public boolean hasEndpoints() {
        return !endpoints.isEmpty();
    }
    
    public boolean isSuccessful() {
        return metadata.isSuccessful();
    }
    
    public List<RestEndpointInfo> getEndpointsByController(String controllerClass) {
        return endpoints.stream()
            .filter(endpoint -> controllerClass.equals(endpoint.getControllerClass()))
            .collect(Collectors.toList());
    }
    
    public List<RestEndpointInfo> getEndpointsByHttpMethod(RestEndpointInfo.HttpMethod httpMethod) {
        return endpoints.stream()
            .filter(endpoint -> endpoint.getHttpMethods().contains(httpMethod))
            .collect(Collectors.toList());
    }
    
    public RestEndpointAnalysisResult withAnalysisTime(long analysisTimeMs) {
        return new RestEndpointAnalysisResult(endpoints, controllerClasses, endpointsByMethod, metadata, analysisTimeMs);
    }
    
    @Override
    public String toString() {
        return "RestEndpointAnalysisResult{" +
                "endpoints=" + endpoints.size() +
                ", controllers=" + controllerClasses.size() +
                ", analysisTime=" + analysisTimeMs + "ms" +
                '}';
    }
    
    public static class Builder {
        private List<RestEndpointInfo> endpoints = new ArrayList<>();
        private List<String> controllerClasses = new ArrayList<>();
        private AnalysisMetadata metadata = AnalysisMetadata.successful();
        private long analysisTimeMs = 0L;
        
        public Builder addEndpoint(RestEndpointInfo endpoint) {
            if (endpoint != null) {
                this.endpoints.add(endpoint);
                String controllerClass = endpoint.getControllerClass();
                if (controllerClass != null && !controllerClasses.contains(controllerClass)) {
                    this.controllerClasses.add(controllerClass);
                }
            }
            return this;
        }
        
        public Builder endpoints(List<RestEndpointInfo> endpoints) {
            this.endpoints = new ArrayList<>(endpoints != null ? endpoints : Collections.emptyList());
            // Update controller classes
            Set<String> uniqueControllers = this.endpoints.stream()
                .map(RestEndpointInfo::getControllerClass)
                .filter(cls -> cls != null)
                .collect(Collectors.toSet());
            this.controllerClasses = new ArrayList<>(uniqueControllers);
            return this;
        }
        
        public Builder metadata(AnalysisMetadata metadata) {
            this.metadata = metadata != null ? metadata : AnalysisMetadata.successful();
            return this;
        }
        
        public Builder analysisTimeMs(long analysisTimeMs) {
            this.analysisTimeMs = Math.max(0L, analysisTimeMs);
            return this;
        }
        
        public RestEndpointAnalysisResult build() {
            // Calculate endpoints by HTTP method
            Map<String, Integer> endpointsByMethod = new HashMap<>();
            for (RestEndpointInfo endpoint : endpoints) {
                for (RestEndpointInfo.HttpMethod method : endpoint.getHttpMethods()) {
                    endpointsByMethod.merge(method.name(), 1, Integer::sum);
                }
            }
            
            return new RestEndpointAnalysisResult(endpoints, controllerClasses, endpointsByMethod, metadata, analysisTimeMs);
        }
    }
}