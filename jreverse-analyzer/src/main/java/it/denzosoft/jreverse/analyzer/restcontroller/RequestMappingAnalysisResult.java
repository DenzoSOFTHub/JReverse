package it.denzosoft.jreverse.analyzer.restcontroller;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Immutable container for request mapping analysis results.
 * Contains information about all discovered REST endpoints, their statistics,
 * and organization patterns extracted from Spring MVC request mapping annotations.
 */
public class RequestMappingAnalysisResult {
    
    private final List<RestEndpointInfo> endpoints;
    private final Map<String, List<RestEndpointInfo>> endpointsByController;
    private final Map<HttpMethod, List<RestEndpointInfo>> endpointsByHttpMethod;
    private final Map<String, List<RestEndpointInfo>> endpointsByPath;
    private final Set<String> allPaths;
    private final Set<HttpMethod> allHttpMethods;
    private final Set<String> allContentTypes;
    private final EndpointStatistics statistics;
    
    private RequestMappingAnalysisResult(Builder builder) {
        this.endpoints = Collections.unmodifiableList(new ArrayList<>(builder.endpoints));
        this.endpointsByController = buildControllerMap(this.endpoints);
        this.endpointsByHttpMethod = buildHttpMethodMap(this.endpoints);
        this.endpointsByPath = buildPathMap(this.endpoints);
        this.allPaths = extractAllPaths(this.endpoints);
        this.allHttpMethods = extractAllHttpMethods(this.endpoints);
        this.allContentTypes = extractAllContentTypes(this.endpoints);
        this.statistics = calculateStatistics(this.endpoints);
    }
    
    private Map<String, List<RestEndpointInfo>> buildControllerMap(List<RestEndpointInfo> endpoints) {
        return endpoints.stream()
                .collect(Collectors.groupingBy(
                    RestEndpointInfo::getControllerClassName,
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
    }
    
    private Map<HttpMethod, List<RestEndpointInfo>> buildHttpMethodMap(List<RestEndpointInfo> endpoints) {
        Map<HttpMethod, List<RestEndpointInfo>> methodMap = new LinkedHashMap<>();
        
        for (RestEndpointInfo endpoint : endpoints) {
            for (HttpMethod method : endpoint.getHttpMethods()) {
                methodMap.computeIfAbsent(method, k -> new ArrayList<>()).add(endpoint);
            }
        }
        
        return methodMap.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> Collections.unmodifiableList(entry.getValue()),
                    (v1, v2) -> v1,
                    LinkedHashMap::new
                ));
    }
    
    private Map<String, List<RestEndpointInfo>> buildPathMap(List<RestEndpointInfo> endpoints) {
        return endpoints.stream()
                .collect(Collectors.groupingBy(
                    RestEndpointInfo::getCombinedPath,
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
    }
    
    private Set<String> extractAllPaths(List<RestEndpointInfo> endpoints) {
        return endpoints.stream()
                .map(RestEndpointInfo::getCombinedPath)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
    
    private Set<HttpMethod> extractAllHttpMethods(List<RestEndpointInfo> endpoints) {
        return endpoints.stream()
                .flatMap(endpoint -> endpoint.getHttpMethods().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
    
    private Set<String> extractAllContentTypes(List<RestEndpointInfo> endpoints) {
        Set<String> contentTypes = new LinkedHashSet<>();
        for (RestEndpointInfo endpoint : endpoints) {
            contentTypes.addAll(endpoint.getProduces());
            contentTypes.addAll(endpoint.getConsumes());
        }
        return contentTypes;
    }
    
    private EndpointStatistics calculateStatistics(List<RestEndpointInfo> endpoints) {
        long totalEndpoints = endpoints.size();
        long publicEndpoints = endpoints.stream().mapToLong(e -> e.isPublicMethod() ? 1 : 0).sum();
        long endpointsWithPathVariables = endpoints.stream().mapToLong(e -> e.hasPathVariables() ? 1 : 0).sum();
        long endpointsWithContentTypes = endpoints.stream().mapToLong(e -> e.hasContentTypeRestrictions() ? 1 : 0).sum();
        long jsonEndpoints = endpoints.stream().mapToLong(e -> (e.producesJson() || e.consumesJson()) ? 1 : 0).sum();
        long httpMethodAnnotations = endpoints.stream().mapToLong(e -> e.usesHttpMethodAnnotation() ? 1 : 0).sum();
        long requestMappingAnnotations = endpoints.stream().mapToLong(e -> e.usesRequestMapping() ? 1 : 0).sum();
        long multiMethodEndpoints = endpoints.stream().mapToLong(e -> e.hasMultipleHttpMethods() ? 1 : 0).sum();
        
        Map<HttpMethod, Long> methodCounts = new LinkedHashMap<>();
        for (HttpMethod method : HttpMethod.values()) {
            long count = endpoints.stream().mapToLong(e -> e.supportsHttpMethod(method) ? 1 : 0).sum();
            if (count > 0) {
                methodCounts.put(method, count);
            }
        }
        
        return new EndpointStatistics(
            totalEndpoints,
            publicEndpoints,
            endpointsWithPathVariables,
            endpointsWithContentTypes,
            jsonEndpoints,
            httpMethodAnnotations,
            requestMappingAnnotations,
            multiMethodEndpoints,
            Collections.unmodifiableMap(methodCounts)
        );
    }
    
    // Getters
    public List<RestEndpointInfo> getEndpoints() { return endpoints; }
    public Map<String, List<RestEndpointInfo>> getEndpointsByController() { return endpointsByController; }
    public Map<HttpMethod, List<RestEndpointInfo>> getEndpointsByHttpMethod() { return endpointsByHttpMethod; }
    public Map<String, List<RestEndpointInfo>> getEndpointsByPath() { return endpointsByPath; }
    public Set<String> getAllPaths() { return allPaths; }
    public Set<HttpMethod> getAllHttpMethods() { return allHttpMethods; }
    public Set<String> getAllContentTypes() { return allContentTypes; }
    public EndpointStatistics getStatistics() { return statistics; }
    
    // Convenience methods
    public int getEndpointCount() { return endpoints.size(); }
    public int getControllerCount() { return endpointsByController.size(); }
    public int getUniquePathCount() { return allPaths.size(); }
    public int getHttpMethodCount() { return allHttpMethods.size(); }
    
    public boolean hasEndpoints() { return !endpoints.isEmpty(); }
    public boolean hasMultipleControllers() { return endpointsByController.size() > 1; }
    public boolean hasPathVariables() { return endpoints.stream().anyMatch(RestEndpointInfo::hasPathVariables); }
    public boolean hasContentTypeRestrictions() { return endpoints.stream().anyMatch(RestEndpointInfo::hasContentTypeRestrictions); }
    
    /**
     * Gets endpoints for a specific controller.
     * @param controllerClassName the controller class name
     * @return list of endpoints for the controller, or empty list if not found
     */
    public List<RestEndpointInfo> getEndpointsForController(String controllerClassName) {
        return endpointsByController.getOrDefault(controllerClassName, Collections.emptyList());
    }
    
    /**
     * Gets endpoints for a specific HTTP method.
     * @param httpMethod the HTTP method
     * @return list of endpoints for the method, or empty list if not found
     */
    public List<RestEndpointInfo> getEndpointsForHttpMethod(HttpMethod httpMethod) {
        return endpointsByHttpMethod.getOrDefault(httpMethod, Collections.emptyList());
    }
    
    /**
     * Gets endpoints for a specific path.
     * @param path the endpoint path
     * @return list of endpoints for the path, or empty list if not found
     */
    public List<RestEndpointInfo> getEndpointsForPath(String path) {
        return endpointsByPath.getOrDefault(path, Collections.emptyList());
    }
    
    /**
     * Finds endpoints that produce JSON content.
     * @return list of endpoints that produce JSON
     */
    public List<RestEndpointInfo> getJsonProducingEndpoints() {
        return endpoints.stream()
                .filter(RestEndpointInfo::producesJson)
                .collect(Collectors.toList());
    }
    
    /**
     * Finds endpoints that consume JSON content.
     * @return list of endpoints that consume JSON
     */
    public List<RestEndpointInfo> getJsonConsumingEndpoints() {
        return endpoints.stream()
                .filter(RestEndpointInfo::consumesJson)
                .collect(Collectors.toList());
    }
    
    /**
     * Finds endpoints with path variables.
     * @return list of endpoints that have path variables
     */
    public List<RestEndpointInfo> getEndpointsWithPathVariables() {
        return endpoints.stream()
                .filter(RestEndpointInfo::hasPathVariables)
                .collect(Collectors.toList());
    }
    
    @Override
    public String toString() {
        return "RequestMappingAnalysisResult{" +
                "endpointCount=" + getEndpointCount() +
                ", controllerCount=" + getControllerCount() +
                ", uniquePathCount=" + getUniquePathCount() +
                ", httpMethodCount=" + getHttpMethodCount() +
                ", statistics=" + statistics +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static RequestMappingAnalysisResult empty() {
        return builder().build();
    }
    
    public static class Builder {
        private List<RestEndpointInfo> endpoints = new ArrayList<>();
        
        public Builder addEndpoint(RestEndpointInfo endpoint) {
            if (endpoint != null) {
                this.endpoints.add(endpoint);
            }
            return this;
        }
        
        public Builder endpoints(List<RestEndpointInfo> endpoints) {
            this.endpoints = new ArrayList<>(endpoints != null ? endpoints : Collections.emptyList());
            return this;
        }
        
        public Builder addEndpoints(Collection<RestEndpointInfo> endpoints) {
            if (endpoints != null) {
                this.endpoints.addAll(endpoints);
            }
            return this;
        }
        
        public RequestMappingAnalysisResult build() {
            return new RequestMappingAnalysisResult(this);
        }
    }
    
    /**
     * Statistics about the analyzed endpoints.
     */
    public static class EndpointStatistics {
        private final long totalEndpoints;
        private final long publicEndpoints;
        private final long endpointsWithPathVariables;
        private final long endpointsWithContentTypes;
        private final long jsonEndpoints;
        private final long httpMethodAnnotations;
        private final long requestMappingAnnotations;
        private final long multiMethodEndpoints;
        private final Map<HttpMethod, Long> methodCounts;
        
        public EndpointStatistics(long totalEndpoints, long publicEndpoints, long endpointsWithPathVariables,
                                long endpointsWithContentTypes, long jsonEndpoints, long httpMethodAnnotations,
                                long requestMappingAnnotations, long multiMethodEndpoints, Map<HttpMethod, Long> methodCounts) {
            this.totalEndpoints = totalEndpoints;
            this.publicEndpoints = publicEndpoints;
            this.endpointsWithPathVariables = endpointsWithPathVariables;
            this.endpointsWithContentTypes = endpointsWithContentTypes;
            this.jsonEndpoints = jsonEndpoints;
            this.httpMethodAnnotations = httpMethodAnnotations;
            this.requestMappingAnnotations = requestMappingAnnotations;
            this.multiMethodEndpoints = multiMethodEndpoints;
            this.methodCounts = methodCounts;
        }
        
        // Getters
        public long getTotalEndpoints() { return totalEndpoints; }
        public long getPublicEndpoints() { return publicEndpoints; }
        public long getEndpointsWithPathVariables() { return endpointsWithPathVariables; }
        public long getEndpointsWithContentTypes() { return endpointsWithContentTypes; }
        public long getJsonEndpoints() { return jsonEndpoints; }
        public long getHttpMethodAnnotations() { return httpMethodAnnotations; }
        public long getRequestMappingAnnotations() { return requestMappingAnnotations; }
        public long getMultiMethodEndpoints() { return multiMethodEndpoints; }
        public Map<HttpMethod, Long> getMethodCounts() { return methodCounts; }
        
        // Convenience methods
        public double getPublicEndpointRatio() {
            return totalEndpoints > 0 ? (double) publicEndpoints / totalEndpoints : 0.0;
        }
        
        public double getPathVariableRatio() {
            return totalEndpoints > 0 ? (double) endpointsWithPathVariables / totalEndpoints : 0.0;
        }
        
        public double getJsonEndpointRatio() {
            return totalEndpoints > 0 ? (double) jsonEndpoints / totalEndpoints : 0.0;
        }
        
        public double getHttpMethodAnnotationRatio() {
            return totalEndpoints > 0 ? (double) httpMethodAnnotations / totalEndpoints : 0.0;
        }
        
        @Override
        public String toString() {
            return "EndpointStatistics{" +
                    "totalEndpoints=" + totalEndpoints +
                    ", publicEndpoints=" + publicEndpoints +
                    ", withPathVariables=" + endpointsWithPathVariables +
                    ", withContentTypes=" + endpointsWithContentTypes +
                    ", jsonEndpoints=" + jsonEndpoints +
                    ", methodCounts=" + methodCounts +
                    '}';
        }
    }
}