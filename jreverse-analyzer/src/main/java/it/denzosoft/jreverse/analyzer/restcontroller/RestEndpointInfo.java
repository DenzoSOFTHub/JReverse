package it.denzosoft.jreverse.analyzer.restcontroller;

import it.denzosoft.jreverse.core.model.MethodInfo;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Immutable value object representing information about a REST endpoint.
 * Contains complete metadata about a REST endpoint including HTTP method, path,
 * content types, and path variables extracted from Spring request mapping annotations.
 */
public class RestEndpointInfo {
    
    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)\\}");
    
    private final MethodInfo methodInfo;
    private final String controllerClassName;
    private final Set<HttpMethod> httpMethods;
    private final List<String> paths;
    private final Set<String> produces;
    private final Set<String> consumes;
    private final List<String> pathVariables;
    private final String combinedPath;
    private final boolean hasPathVariables;
    private final String annotationType;
    
    private RestEndpointInfo(Builder builder) {
        this.methodInfo = Objects.requireNonNull(builder.methodInfo, "methodInfo cannot be null");
        this.controllerClassName = Objects.requireNonNull(builder.controllerClassName, "controllerClassName cannot be null");
        this.httpMethods = Collections.unmodifiableSet(new LinkedHashSet<>(builder.httpMethods));
        this.paths = Collections.unmodifiableList(new ArrayList<>(builder.paths));
        this.produces = Collections.unmodifiableSet(new LinkedHashSet<>(builder.produces));
        this.consumes = Collections.unmodifiableSet(new LinkedHashSet<>(builder.consumes));
        this.pathVariables = extractPathVariables();
        this.combinedPath = buildCombinedPath(builder.basePath, paths);
        this.hasPathVariables = !this.pathVariables.isEmpty();
        this.annotationType = builder.annotationType;
    }
    
    private List<String> extractPathVariables() {
        List<String> variables = new ArrayList<>();
        for (String path : paths) {
            if (path != null) {
                Matcher matcher = PATH_VARIABLE_PATTERN.matcher(path);
                while (matcher.find()) {
                    String variable = matcher.group(1);
                    if (!variables.contains(variable)) {
                        variables.add(variable);
                    }
                }
            }
        }
        return Collections.unmodifiableList(variables);
    }
    
    private String buildCombinedPath(String basePath, List<String> methodPaths) {
        String basePathNormalized = normalizePath(basePath);
        
        if (methodPaths.isEmpty()) {
            return basePathNormalized.isEmpty() ? "/" : basePathNormalized;
        }
        
        // Use the first method path for combined path
        String methodPath = normalizePath(methodPaths.get(0));
        
        if (basePathNormalized.isEmpty()) {
            return methodPath.isEmpty() ? "/" : methodPath;
        }
        
        if (methodPath.isEmpty() || "/".equals(methodPath)) {
            return basePathNormalized;
        }
        
        // Combine base path and method path
        String combined = basePathNormalized + methodPath;
        return combined.isEmpty() ? "/" : combined;
    }
    
    private String normalizePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "";
        }
        
        String normalized = path.trim();
        
        // Ensure path starts with / if not empty
        if (!normalized.startsWith("/") && !normalized.isEmpty()) {
            normalized = "/" + normalized;
        }
        
        // Remove trailing slash unless it's the root path
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        
        return normalized;
    }
    
    // Getters
    public MethodInfo getMethodInfo() { return methodInfo; }
    public String getControllerClassName() { return controllerClassName; }
    public Set<HttpMethod> getHttpMethods() { return httpMethods; }
    public List<String> getPaths() { return paths; }
    public Set<String> getProduces() { return produces; }
    public Set<String> getConsumes() { return consumes; }
    public List<String> getPathVariables() { return pathVariables; }
    public String getCombinedPath() { return combinedPath; }
    public boolean hasPathVariables() { return hasPathVariables; }
    public String getAnnotationType() { return annotationType; }
    
    // Convenience methods
    public String getMethodName() { return methodInfo.getName(); }
    public String getReturnType() { return methodInfo.getReturnType(); }
    public String getMethodSignature() { return methodInfo.getMethodSignature(); }
    public boolean isPublicMethod() { return methodInfo.isPublic(); }
    
    /**
     * Gets the primary HTTP method for this endpoint.
     * If multiple methods are configured, returns the first one.
     * @return the primary HTTP method, or empty if none configured
     */
    public Optional<HttpMethod> getPrimaryHttpMethod() {
        return httpMethods.stream().findFirst();
    }
    
    /**
     * Checks if this endpoint supports the specified HTTP method.
     * @param method the HTTP method to check
     * @return true if this endpoint supports the method
     */
    public boolean supportsHttpMethod(HttpMethod method) {
        return httpMethods.contains(method);
    }
    
    /**
     * Checks if this endpoint has multiple HTTP methods configured.
     * @return true if multiple HTTP methods are supported
     */
    public boolean hasMultipleHttpMethods() {
        return httpMethods.size() > 1;
    }
    
    /**
     * Checks if this endpoint has content type restrictions.
     * @return true if produces or consumes are configured
     */
    public boolean hasContentTypeRestrictions() {
        return !produces.isEmpty() || !consumes.isEmpty();
    }
    
    /**
     * Checks if this endpoint produces JSON content.
     * @return true if JSON is in the produces set
     */
    public boolean producesJson() {
        return produces.stream().anyMatch(type -> type.contains("json"));
    }
    
    /**
     * Checks if this endpoint consumes JSON content.
     * @return true if JSON is in the consumes set
     */
    public boolean consumesJson() {
        return consumes.stream().anyMatch(type -> type.contains("json"));
    }
    
    /**
     * Checks if this endpoint uses a Spring HTTP method annotation (like @GetMapping).
     * @return true if using a dedicated HTTP method annotation
     */
    public boolean usesHttpMethodAnnotation() {
        return HttpMethod.isSpringHttpAnnotation(annotationType);
    }
    
    /**
     * Checks if this endpoint uses the generic @RequestMapping annotation.
     * @return true if using @RequestMapping
     */
    public boolean usesRequestMapping() {
        return HttpMethod.isRequestMapping(annotationType);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RestEndpointInfo that = (RestEndpointInfo) obj;
        return Objects.equals(controllerClassName, that.controllerClassName) &&
               Objects.equals(methodInfo.getName(), that.methodInfo.getName()) &&
               Objects.equals(combinedPath, that.combinedPath) &&
               Objects.equals(httpMethods, that.httpMethods);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(controllerClassName, methodInfo.getName(), combinedPath, httpMethods);
    }
    
    @Override
    public String toString() {
        return "RestEndpointInfo{" +
                "controller='" + controllerClassName + '\'' +
                ", method='" + methodInfo.getName() + '\'' +
                ", path='" + combinedPath + '\'' +
                ", httpMethods=" + httpMethods +
                ", pathVariables=" + pathVariables +
                ", annotation='" + annotationType + '\'' +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private MethodInfo methodInfo;
        private String controllerClassName;
        private Set<HttpMethod> httpMethods = new LinkedHashSet<>();
        private List<String> paths = new ArrayList<>();
        private Set<String> produces = new LinkedHashSet<>();
        private Set<String> consumes = new LinkedHashSet<>();
        private String basePath;
        private String annotationType;
        
        public Builder methodInfo(MethodInfo methodInfo) {
            this.methodInfo = methodInfo;
            return this;
        }
        
        public Builder controllerClassName(String controllerClassName) {
            this.controllerClassName = controllerClassName;
            return this;
        }
        
        public Builder addHttpMethod(HttpMethod httpMethod) {
            if (httpMethod != null) {
                this.httpMethods.add(httpMethod);
            }
            return this;
        }
        
        public Builder httpMethods(Set<HttpMethod> httpMethods) {
            this.httpMethods = new LinkedHashSet<>(httpMethods != null ? httpMethods : Collections.emptySet());
            return this;
        }
        
        public Builder addPath(String path) {
            if (path != null && !path.trim().isEmpty()) {
                this.paths.add(path.trim());
            }
            return this;
        }
        
        public Builder paths(List<String> paths) {
            this.paths = new ArrayList<>(paths != null ? paths : Collections.emptyList());
            return this;
        }
        
        public Builder addProduces(String contentType) {
            if (contentType != null && !contentType.trim().isEmpty()) {
                this.produces.add(contentType.trim());
            }
            return this;
        }
        
        public Builder produces(Set<String> produces) {
            this.produces = new LinkedHashSet<>(produces != null ? produces : Collections.emptySet());
            return this;
        }
        
        public Builder addConsumes(String contentType) {
            if (contentType != null && !contentType.trim().isEmpty()) {
                this.consumes.add(contentType.trim());
            }
            return this;
        }
        
        public Builder consumes(Set<String> consumes) {
            this.consumes = new LinkedHashSet<>(consumes != null ? consumes : Collections.emptySet());
            return this;
        }
        
        public Builder basePath(String basePath) {
            this.basePath = basePath;
            return this;
        }
        
        public Builder annotationType(String annotationType) {
            this.annotationType = annotationType;
            return this;
        }
        
        public RestEndpointInfo build() {
            return new RestEndpointInfo(this);
        }
    }
}