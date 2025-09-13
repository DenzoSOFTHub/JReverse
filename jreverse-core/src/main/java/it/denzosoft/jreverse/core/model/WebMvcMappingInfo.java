package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Information about a Spring MVC mapping configuration.
 * Captures detailed mapping annotation attributes and patterns.
 */
public class WebMvcMappingInfo {
    
    private final String path;
    private final Set<String> httpMethods;
    private final Set<String> produces;
    private final Set<String> consumes;
    private final Set<String> headers;
    private final Set<String> params;
    private final String name;
    private final String methodName;
    private final String controllerClass;
    
    private WebMvcMappingInfo(String path,
                             Set<String> httpMethods,
                             Set<String> produces,
                             Set<String> consumes,
                             Set<String> headers,
                             Set<String> params,
                             String name,
                             String methodName,
                             String controllerClass) {
        this.path = path;
        this.httpMethods = Collections.unmodifiableSet(new HashSet<>(httpMethods));
        this.produces = Collections.unmodifiableSet(new HashSet<>(produces));
        this.consumes = Collections.unmodifiableSet(new HashSet<>(consumes));
        this.headers = Collections.unmodifiableSet(new HashSet<>(headers));
        this.params = Collections.unmodifiableSet(new HashSet<>(params));
        this.name = name;
        this.methodName = methodName;
        this.controllerClass = controllerClass;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String getPath() {
        return path;
    }
    
    public Set<String> getHttpMethods() {
        return httpMethods;
    }
    
    public Set<String> getProduces() {
        return produces;
    }
    
    public Set<String> getConsumes() {
        return consumes;
    }
    
    public Set<String> getHeaders() {
        return headers;
    }
    
    public Set<String> getParams() {
        return params;
    }
    
    public String getName() {
        return name;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public String getControllerClass() {
        return controllerClass;
    }
    
    public boolean hasHttpMethod(String method) {
        return httpMethods.contains(method);
    }
    
    public boolean hasCondition(String condition) {
        return headers.contains(condition) || params.contains(condition);
    }
    
    public boolean isContentTypeSpecific() {
        return !produces.isEmpty() || !consumes.isEmpty();
    }
    
    @Override
    public String toString() {
        return "WebMvcMappingInfo{" +
                "path='" + path + '\'' +
                ", methods=" + httpMethods +
                ", controller='" + controllerClass + '\'' +
                ", method='" + methodName + '\'' +
                '}';
    }
    
    public static class Builder {
        private String path;
        private Set<String> httpMethods = new HashSet<>();
        private Set<String> produces = new HashSet<>();
        private Set<String> consumes = new HashSet<>();
        private Set<String> headers = new HashSet<>();
        private Set<String> params = new HashSet<>();
        private String name;
        private String methodName;
        private String controllerClass;
        
        public Builder path(String path) {
            this.path = path;
            return this;
        }
        
        public Builder addHttpMethod(String method) {
            if (method != null && !method.trim().isEmpty()) {
                this.httpMethods.add(method.trim().toUpperCase());
            }
            return this;
        }
        
        public Builder httpMethods(Set<String> methods) {
            this.httpMethods = new HashSet<>(methods != null ? methods : Collections.emptySet());
            return this;
        }
        
        public Builder addProduces(String mediaType) {
            if (mediaType != null && !mediaType.trim().isEmpty()) {
                this.produces.add(mediaType.trim());
            }
            return this;
        }
        
        public Builder produces(Set<String> mediaTypes) {
            this.produces = new HashSet<>(mediaTypes != null ? mediaTypes : Collections.emptySet());
            return this;
        }
        
        public Builder addConsumes(String mediaType) {
            if (mediaType != null && !mediaType.trim().isEmpty()) {
                this.consumes.add(mediaType.trim());
            }
            return this;
        }
        
        public Builder consumes(Set<String> mediaTypes) {
            this.consumes = new HashSet<>(mediaTypes != null ? mediaTypes : Collections.emptySet());
            return this;
        }
        
        public Builder addHeader(String header) {
            if (header != null && !header.trim().isEmpty()) {
                this.headers.add(header.trim());
            }
            return this;
        }
        
        public Builder headers(Set<String> headers) {
            this.headers = new HashSet<>(headers != null ? headers : Collections.emptySet());
            return this;
        }
        
        public Builder addParam(String param) {
            if (param != null && !param.trim().isEmpty()) {
                this.params.add(param.trim());
            }
            return this;
        }
        
        public Builder params(Set<String> params) {
            this.params = new HashSet<>(params != null ? params : Collections.emptySet());
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
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
        
        public WebMvcMappingInfo build() {
            return new WebMvcMappingInfo(path, httpMethods, produces, consumes, 
                                        headers, params, name, methodName, controllerClass);
        }
    }
}