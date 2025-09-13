package it.denzosoft.jreverse.analyzer.restcontroller;

import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.AnnotationInfo;
import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.MethodInfo;

import java.util.*;

/**
 * Javassist-based implementation of RequestMappingAnalyzer.
 * Analyzes @RequestMapping and HTTP method annotations to extract REST endpoint information
 * from Spring MVC controllers using only Javassist for bytecode analysis.
 * 
 * This analyzer examines methods within REST controllers (identified by RestControllerAnalyzer)
 * and extracts their request mapping configurations including paths, HTTP methods, 
 * content types, and path variables.
 */
public class JavassistRequestMappingAnalyzer implements RequestMappingAnalyzer {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistRequestMappingAnalyzer.class);
    
    // Spring annotation constants
    private static final String REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping";
    private static final String GET_MAPPING = "org.springframework.web.bind.annotation.GetMapping";
    private static final String POST_MAPPING = "org.springframework.web.bind.annotation.PostMapping";
    private static final String PUT_MAPPING = "org.springframework.web.bind.annotation.PutMapping";
    private static final String DELETE_MAPPING = "org.springframework.web.bind.annotation.DeleteMapping";
    private static final String PATCH_MAPPING = "org.springframework.web.bind.annotation.PatchMapping";
    
    // Annotation attribute names
    private static final String VALUE_ATTR = "value";
    private static final String PATH_ATTR = "path";
    private static final String METHOD_ATTR = "method";
    private static final String PRODUCES_ATTR = "produces";
    private static final String CONSUMES_ATTR = "consumes";
    
    @Override
    public RequestMappingAnalysisResult analyzeRequestMappings(JarContent jarContent, RestControllerAnalysisResult restControllerResult) {
        if (!canAnalyze(jarContent, restControllerResult)) {
            LOGGER.warn("Cannot analyze request mappings - invalid input");
            return RequestMappingAnalysisResult.empty();
        }
        
        LOGGER.info("Starting request mapping analysis for {} controllers", restControllerResult.getRestControllers().size());
        
        RequestMappingAnalysisResult.Builder resultBuilder = RequestMappingAnalysisResult.builder();
        int totalEndpoints = 0;
        
        try {
            for (RestControllerInfo controllerInfo : restControllerResult.getRestControllers()) {
                LOGGER.debug("Analyzing request mappings for controller: {}", controllerInfo.getClassName());
                
                List<RestEndpointInfo> controllerEndpoints = analyzeControllerEndpoints(controllerInfo, jarContent);
                resultBuilder.addEndpoints(controllerEndpoints);
                totalEndpoints += controllerEndpoints.size();
                
                LOGGER.debug("Found {} endpoints in controller {}", controllerEndpoints.size(), controllerInfo.getClassName());
            }
            
            LOGGER.info("Request mapping analysis completed. Found {} total endpoints", totalEndpoints);
            return resultBuilder.build();
            
        } catch (Exception e) {
            LOGGER.error("Error during request mapping analysis", e);
            return RequestMappingAnalysisResult.empty();
        }
    }
    
    private List<RestEndpointInfo> analyzeControllerEndpoints(RestControllerInfo controllerInfo, JarContent jarContent) {
        List<RestEndpointInfo> endpoints = new ArrayList<>();
        
        try {
            ClassInfo classInfo = controllerInfo.getClassInfo();
            String basePath = controllerInfo.getBasePath().orElse("");
            
            for (MethodInfo methodInfo : classInfo.getMethods()) {
                if (hasRequestMappingAnnotation(methodInfo)) {
                    RestEndpointInfo endpoint = createEndpointInfo(methodInfo, controllerInfo.getClassName(), basePath);
                    if (endpoint != null) {
                        endpoints.add(endpoint);
                        LOGGER.debug("Created endpoint: {} {} from method {}", 
                                   endpoint.getPrimaryHttpMethod().orElse(null), 
                                   endpoint.getCombinedPath(), 
                                   methodInfo.getName());
                    }
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("Error analyzing controller endpoints for " + controllerInfo.getClassName(), e);
        }
        
        return endpoints;
    }
    
    private boolean hasRequestMappingAnnotation(MethodInfo methodInfo) {
        return methodInfo.getAnnotations().stream()
                .anyMatch(annotation -> isRequestMappingAnnotation(annotation.getType()));
    }
    
    private boolean isRequestMappingAnnotation(String annotationType) {
        return REQUEST_MAPPING.equals(annotationType) ||
               GET_MAPPING.equals(annotationType) ||
               POST_MAPPING.equals(annotationType) ||
               PUT_MAPPING.equals(annotationType) ||
               DELETE_MAPPING.equals(annotationType) ||
               PATCH_MAPPING.equals(annotationType);
    }
    
    private RestEndpointInfo createEndpointInfo(MethodInfo methodInfo, String controllerClassName, String basePath) {
        try {
            // Find the request mapping annotation
            Optional<AnnotationInfo> mappingAnnotation = methodInfo.getAnnotations().stream()
                    .filter(annotation -> isRequestMappingAnnotation(annotation.getType()))
                    .findFirst();
            
            if (!mappingAnnotation.isPresent()) {
                return null;
            }
            
            AnnotationInfo annotation = mappingAnnotation.get();
            RestEndpointInfo.Builder builder = RestEndpointInfo.builder()
                    .methodInfo(methodInfo)
                    .controllerClassName(controllerClassName)
                    .basePath(basePath)
                    .annotationType(getSimpleAnnotationName(annotation.getType()));
            
            // Extract HTTP methods
            extractHttpMethods(annotation, builder);
            
            // Extract paths
            extractPaths(annotation, builder);
            
            // Extract content types
            extractContentTypes(annotation, builder);
            
            return builder.build();
            
        } catch (Exception e) {
            LOGGER.error("Error creating endpoint info for method " + methodInfo.getName(), e);
            return null;
        }
    }
    
    private void extractHttpMethods(AnnotationInfo annotation, RestEndpointInfo.Builder builder) {
        String annotationType = annotation.getType();
        
        // Handle HTTP method-specific annotations
        Optional<HttpMethod> httpMethod = HttpMethod.fromSpringAnnotation(getSimpleAnnotationName(annotationType));
        if (httpMethod.isPresent()) {
            builder.addHttpMethod(httpMethod.get());
            return;
        }
        
        // Handle @RequestMapping with method attribute
        if (REQUEST_MAPPING.equals(annotationType)) {
            Object methodAttr = annotation.getAttribute(METHOD_ATTR);
            if (methodAttr != null) {
                Set<HttpMethod> methods = parseHttpMethods(methodAttr);
                if (!methods.isEmpty()) {
                    for (HttpMethod method : methods) {
                        builder.addHttpMethod(method);
                    }
                } else {
                    // Default to GET for @RequestMapping without method
                    builder.addHttpMethod(HttpMethod.GET);
                }
            } else {
                // Default to GET for @RequestMapping without method
                builder.addHttpMethod(HttpMethod.GET);
            }
        }
    }
    
    private Set<HttpMethod> parseHttpMethods(Object methodAttr) {
        Set<HttpMethod> methods = new LinkedHashSet<>();
        
        try {
            if (methodAttr instanceof String[]) {
                String[] methodArray = (String[]) methodAttr;
                for (String method : methodArray) {
                    parseHttpMethod(method).ifPresent(methods::add);
                }
            } else if (methodAttr instanceof String) {
                parseHttpMethod((String) methodAttr).ifPresent(methods::add);
            } else if (methodAttr instanceof Object[]) {
                Object[] methodArray = (Object[]) methodAttr;
                for (Object method : methodArray) {
                    if (method != null) {
                        parseHttpMethod(method.toString()).ifPresent(methods::add);
                    }
                }
            } else if (methodAttr != null) {
                parseHttpMethod(methodAttr.toString()).ifPresent(methods::add);
            }
        } catch (Exception e) {
            LOGGER.debug("Error parsing HTTP methods from: {}", methodAttr);
        }
        
        return methods;
    }
    
    private Optional<HttpMethod> parseHttpMethod(String methodString) {
        if (methodString == null || methodString.trim().isEmpty()) {
            return Optional.empty();
        }
        
        // Handle enum-like values (e.g., "RequestMethod.GET")
        String method = methodString.trim();
        if (method.contains(".")) {
            int lastDotIndex = method.lastIndexOf('.');
            if (lastDotIndex >= 0 && lastDotIndex < method.length() - 1) {
                method = method.substring(lastDotIndex + 1);
            }
        }
        
        return HttpMethod.fromMethod(method);
    }
    
    private void extractPaths(AnnotationInfo annotation, RestEndpointInfo.Builder builder) {
        // Try 'path' attribute first, then 'value'
        Object pathAttr = annotation.getAttribute(PATH_ATTR);
        if (pathAttr == null) {
            pathAttr = annotation.getAttribute(VALUE_ATTR);
        }
        
        if (pathAttr != null) {
            List<String> paths = parseStringArray(pathAttr);
            for (String path : paths) {
                if (!path.trim().isEmpty()) {
                    builder.addPath(path.trim());
                }
            }
        }
        
        // If no paths specified, add empty path
        if (pathAttr == null) {
            builder.addPath("");
        }
    }
    
    private void extractContentTypes(AnnotationInfo annotation, RestEndpointInfo.Builder builder) {
        // Extract produces
        Object producesAttr = annotation.getAttribute(PRODUCES_ATTR);
        if (producesAttr != null) {
            List<String> produces = parseStringArray(producesAttr);
            for (String contentType : produces) {
                builder.addProduces(contentType.trim());
            }
        }
        
        // Extract consumes
        Object consumesAttr = annotation.getAttribute(CONSUMES_ATTR);
        if (consumesAttr != null) {
            List<String> consumes = parseStringArray(consumesAttr);
            for (String contentType : consumes) {
                builder.addConsumes(contentType.trim());
            }
        }
    }
    
    private List<String> parseStringArray(Object arrayAttr) {
        List<String> result = new ArrayList<>();
        
        try {
            if (arrayAttr instanceof String[]) {
                String[] stringArray = (String[]) arrayAttr;
                for (String str : stringArray) {
                    if (str != null && !str.trim().isEmpty()) {
                        result.add(str.trim());
                    }
                }
            } else if (arrayAttr instanceof String) {
                String singleValue = (String) arrayAttr;
                if (!singleValue.trim().isEmpty()) {
                    result.add(singleValue.trim());
                }
            } else if (arrayAttr instanceof Object[]) {
                Object[] objectArray = (Object[]) arrayAttr;
                for (Object obj : objectArray) {
                    if (obj != null) {
                        String str = obj.toString().trim();
                        if (!str.isEmpty()) {
                            result.add(str);
                        }
                    }
                }
            } else if (arrayAttr != null) {
                String str = arrayAttr.toString().trim();
                if (!str.isEmpty()) {
                    result.add(str);
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error parsing string array from: {}", arrayAttr);
        }
        
        return result;
    }
    
    private String getSimpleAnnotationName(String fullAnnotationType) {
        if (fullAnnotationType == null) {
            return "";
        }
        
        int lastDotIndex = fullAnnotationType.lastIndexOf('.');
        return lastDotIndex >= 0 && lastDotIndex < fullAnnotationType.length() - 1 
               ? fullAnnotationType.substring(lastDotIndex + 1) 
               : fullAnnotationType;
    }
}