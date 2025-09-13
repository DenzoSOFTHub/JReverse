package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Information about a REST endpoint found in Spring application.
 */
public class RestEndpointInfo {
    
    private final String path;
    private final Set<HttpMethod> httpMethods;
    private final String methodName;
    private final String controllerClass;
    private final String returnType;
    private final Set<String> pathVariables;
    private final Set<String> requestParameters;
    private final boolean isAsync;
    private final String consumes;
    private final String produces;
    
    private RestEndpointInfo(String path,
                            Set<HttpMethod> httpMethods,
                            String methodName,
                            String controllerClass,
                            String returnType,
                            Set<String> pathVariables,
                            Set<String> requestParameters,
                            boolean isAsync,
                            String consumes,
                            String produces) {
        this.path = path;
        this.httpMethods = Collections.unmodifiableSet(new HashSet<>(httpMethods));
        this.methodName = methodName;
        this.controllerClass = controllerClass;
        this.returnType = returnType;
        this.pathVariables = Collections.unmodifiableSet(new HashSet<>(pathVariables));
        this.requestParameters = Collections.unmodifiableSet(new HashSet<>(requestParameters));
        this.isAsync = isAsync;
        this.consumes = consumes;
        this.produces = produces;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String getPath() {
        return path;
    }
    
    public Set<HttpMethod> getHttpMethods() {
        return httpMethods;
    }
    
    /**
     * Gets the primary HTTP method for this endpoint.
     * If multiple methods are supported, returns the first one.
     */
    public String getHttpMethod() {
        return httpMethods.isEmpty() ? "GET" : httpMethods.iterator().next().name();
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    /**
     * Alias for getMethodName() to match CallGraphChain expectations.
     */
    public String getControllerMethod() {
        return methodName;
    }
    
    public String getControllerClass() {
        return controllerClass;
    }
    
    public String getReturnType() {
        return returnType;
    }
    
    public Set<String> getPathVariables() {
        return pathVariables;
    }
    
    public Set<String> getRequestParameters() {
        return requestParameters;
    }
    
    public boolean isAsync() {
        return isAsync;
    }
    
    public String getConsumes() {
        return consumes;
    }
    
    public String getProduces() {
        return produces;
    }
    
    public String getFullPath() {
        return path != null ? path : "";
    }
    
    public boolean supportsMethod(HttpMethod method) {
        return httpMethods.contains(method);
    }
    
    @Override
    public String toString() {
        return "RestEndpointInfo{" +
                "path='" + path + '\'' +
                ", methods=" + httpMethods +
                ", controller='" + controllerClass + '\'' +
                ", method='" + methodName + '\'' +
                '}';
    }
    
    public enum HttpMethod {
        GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD, TRACE
    }
    
    public static class Builder {
        private String path;
        private Set<HttpMethod> httpMethods = new HashSet<>();
        private String methodName;
        private String controllerClass;
        private String returnType;
        private Set<String> pathVariables = new HashSet<>();
        private Set<String> requestParameters = new HashSet<>();
        private boolean isAsync = false;
        private String consumes;
        private String produces;
        
        public Builder path(String path) {
            this.path = path;
            return this;
        }
        
        public Builder addHttpMethod(HttpMethod method) {
            if (method != null) {
                this.httpMethods.add(method);
            }
            return this;
        }
        
        public Builder httpMethods(Set<HttpMethod> methods) {
            this.httpMethods = new HashSet<>(methods != null ? methods : Collections.emptySet());
            return this;
        }
        
        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }
        
        public Builder controllerClass(String controllerClass) {
            this.controllerClass = controllerClass;
            return this;
        }
        
        public Builder returnType(String returnType) {
            this.returnType = returnType;
            return this;
        }
        
        public Builder addPathVariable(String pathVariable) {
            if (pathVariable != null && !pathVariable.trim().isEmpty()) {
                this.pathVariables.add(pathVariable.trim());
            }
            return this;
        }
        
        public Builder addRequestParameter(String requestParameter) {
            if (requestParameter != null && !requestParameter.trim().isEmpty()) {
                this.requestParameters.add(requestParameter.trim());
            }
            return this;
        }
        
        public Builder isAsync(boolean isAsync) {
            this.isAsync = isAsync;
            return this;
        }
        
        public Builder consumes(String consumes) {
            this.consumes = consumes;
            return this;
        }
        
        public Builder produces(String produces) {
            this.produces = produces;
            return this;
        }
        
        public RestEndpointInfo build() {
            return new RestEndpointInfo(
                path, httpMethods, methodName, controllerClass, returnType,
                pathVariables, requestParameters, isAsync, consumes, produces
            );
        }
    }
}