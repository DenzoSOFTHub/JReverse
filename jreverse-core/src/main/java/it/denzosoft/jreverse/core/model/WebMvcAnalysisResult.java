package it.denzosoft.jreverse.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Result of Spring MVC mapping analysis containing all detected configurations.
 */
public class WebMvcAnalysisResult {
    
    private final List<WebMvcMappingInfo> mappings;
    private final Map<String, Integer> mappingsByController;
    private final Map<String, Integer> mappingsByHttpMethod;
    private final Map<String, List<WebMvcMappingInfo>> mappingsByPattern;
    private final AnalysisMetadata metadata;
    private final long analysisTimeMs;
    
    private WebMvcAnalysisResult(List<WebMvcMappingInfo> mappings,
                                Map<String, Integer> mappingsByController,
                                Map<String, Integer> mappingsByHttpMethod,
                                Map<String, List<WebMvcMappingInfo>> mappingsByPattern,
                                AnalysisMetadata metadata,
                                long analysisTimeMs) {
        this.mappings = Collections.unmodifiableList(new ArrayList<>(mappings));
        this.mappingsByController = Collections.unmodifiableMap(new HashMap<>(mappingsByController));
        this.mappingsByHttpMethod = Collections.unmodifiableMap(new HashMap<>(mappingsByHttpMethod));
        this.mappingsByPattern = Collections.unmodifiableMap(new HashMap<>(mappingsByPattern));
        this.metadata = metadata;
        this.analysisTimeMs = analysisTimeMs;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static WebMvcAnalysisResult noMappings() {
        return builder()
            .metadata(AnalysisMetadata.warning("No Spring MVC mappings found"))
            .build();
    }
    
    public static WebMvcAnalysisResult error(String errorMessage) {
        return builder()
            .metadata(AnalysisMetadata.error(errorMessage))
            .build();
    }
    
    public List<WebMvcMappingInfo> getMappings() {
        return mappings;
    }
    
    public Map<String, Integer> getMappingsByController() {
        return mappingsByController;
    }
    
    public Map<String, Integer> getMappingsByHttpMethod() {
        return mappingsByHttpMethod;
    }
    
    public Map<String, List<WebMvcMappingInfo>> getMappingsByPattern() {
        return mappingsByPattern;
    }
    
    public AnalysisMetadata getMetadata() {
        return metadata;
    }
    
    public long getAnalysisTimeMs() {
        return analysisTimeMs;
    }
    
    public int getMappingCount() {
        return mappings.size();
    }
    
    public int getControllerCount() {
        return mappingsByController.size();
    }
    
    public boolean hasMappings() {
        return !mappings.isEmpty();
    }
    
    public boolean isSuccessful() {
        return metadata.isSuccessful();
    }
    
    public List<WebMvcMappingInfo> getMappingsForController(String controllerClass) {
        return mappings.stream()
                .filter(mapping -> controllerClass.equals(mapping.getControllerClass()))
                .collect(Collectors.toList());
    }
    
    public List<WebMvcMappingInfo> getMappingsForHttpMethod(String httpMethod) {
        return mappings.stream()
                .filter(mapping -> mapping.hasHttpMethod(httpMethod))
                .collect(Collectors.toList());
    }
    
    public List<WebMvcMappingInfo> getContentTypeSpecificMappings() {
        return mappings.stream()
                .filter(WebMvcMappingInfo::isContentTypeSpecific)
                .collect(Collectors.toList());
    }
    
    public WebMvcAnalysisResult withAnalysisTime(long analysisTimeMs) {
        return new WebMvcAnalysisResult(mappings, mappingsByController, mappingsByHttpMethod, 
                                       mappingsByPattern, metadata, analysisTimeMs);
    }
    
    @Override
    public String toString() {
        return "WebMvcAnalysisResult{" +
                "mappings=" + mappings.size() +
                ", controllers=" + mappingsByController.size() +
                ", analysisTime=" + analysisTimeMs + "ms" +
                '}';
    }
    
    public static class Builder {
        private List<WebMvcMappingInfo> mappings = new ArrayList<>();
        private AnalysisMetadata metadata = AnalysisMetadata.successful();
        private long analysisTimeMs = 0L;
        
        public Builder addMapping(WebMvcMappingInfo mapping) {
            if (mapping != null) {
                this.mappings.add(mapping);
            }
            return this;
        }
        
        public Builder mappings(List<WebMvcMappingInfo> mappings) {
            this.mappings = new ArrayList<>(mappings != null ? mappings : Collections.emptyList());
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
        
        public WebMvcAnalysisResult build() {
            // Calculate mappings by controller
            Map<String, Integer> mappingsByController = new HashMap<>();
            for (WebMvcMappingInfo mapping : mappings) {
                String controller = mapping.getControllerClass();
                if (controller != null) {
                    mappingsByController.merge(controller, 1, Integer::sum);
                }
            }
            
            // Calculate mappings by HTTP method
            Map<String, Integer> mappingsByHttpMethod = new HashMap<>();
            for (WebMvcMappingInfo mapping : mappings) {
                for (String method : mapping.getHttpMethods()) {
                    mappingsByHttpMethod.merge(method, 1, Integer::sum);
                }
            }
            
            // Group mappings by pattern
            Map<String, List<WebMvcMappingInfo>> mappingsByPattern = mappings.stream()
                    .filter(mapping -> mapping.getPath() != null)
                    .collect(Collectors.groupingBy(WebMvcMappingInfo::getPath));
            
            return new WebMvcAnalysisResult(mappings, mappingsByController, mappingsByHttpMethod, 
                                           mappingsByPattern, metadata, analysisTimeMs);
        }
    }
}