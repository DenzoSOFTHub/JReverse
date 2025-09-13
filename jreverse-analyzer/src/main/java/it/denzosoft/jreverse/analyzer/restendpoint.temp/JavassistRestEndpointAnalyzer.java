package it.denzosoft.jreverse.analyzer.restendpoint;

import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.AnnotationInfo;
import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.MethodInfo;
import it.denzosoft.jreverse.core.model.RestEndpointAnalysisResult;
import it.denzosoft.jreverse.core.model.RestEndpointInfo;
import it.denzosoft.jreverse.core.port.RestEndpointAnalyzer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Javassist-based implementation of RestEndpointAnalyzer.
 * Analyzes Spring controllers and REST endpoints.
 */
public class JavassistRestEndpointAnalyzer implements RestEndpointAnalyzer {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistRestEndpointAnalyzer.class);
    
    private static final Set<String> CONTROLLER_ANNOTATIONS = Set.of(
        "org.springframework.stereotype.Controller",
        "org.springframework.web.bind.annotation.RestController"
    );
    
    private static final Set<String> REQUEST_MAPPING_ANNOTATIONS = Set.of(
        "org.springframework.web.bind.annotation.RequestMapping",
        "org.springframework.web.bind.annotation.GetMapping",
        "org.springframework.web.bind.annotation.PostMapping",
        "org.springframework.web.bind.annotation.PutMapping",
        "org.springframework.web.bind.annotation.DeleteMapping",
        "org.springframework.web.bind.annotation.PatchMapping"
    );
    
    @Override
    public RestEndpointAnalysisResult analyzeRestEndpoints(JarContent jarContent) {
        LOGGER.info("Starting REST endpoint analysis for JAR: %s", jarContent.getLocation().getFileName());
        long startTime = System.currentTimeMillis();
        
        RestEndpointAnalysisResult.Builder resultBuilder = RestEndpointAnalysisResult.builder();
        
        try {
            // Find all controller classes
            List<ClassInfo> controllerClasses = findControllerClasses(jarContent);
            LOGGER.debug("Found %d controller classes", controllerClasses.size());
            
            // Analyze each controller to extract endpoints
            for (ClassInfo controllerClass : controllerClasses) {
                List<RestEndpointInfo> endpoints = analyzeControllerEndpoints(controllerClass);
                for (RestEndpointInfo endpoint : endpoints) {
                    resultBuilder.addEndpoint(endpoint);
                }
            }
            
            long analysisTime = System.currentTimeMillis() - startTime;
            LOGGER.info("REST endpoint analysis completed for %s: %d endpoints in %dms",
                    jarContent.getLocation().getFileName(), 
                    resultBuilder.build().getEndpointCount(), analysisTime);
            
            return resultBuilder.analysisTimeMs(analysisTime).build();
            
        } catch (Exception e) {
            LOGGER.error("Error during REST endpoint analysis: " + e.getMessage(), e);
            return RestEndpointAnalysisResult.error("Analysis failed: " + e.getMessage());
        }
    }
    
    /**
     * Finds all classes annotated with controller annotations.
     */
    private List<ClassInfo> findControllerClasses(JarContent jarContent) {
        return jarContent.getClasses().stream()
                .filter(this::isControllerClass)
                .collect(Collectors.toList());
    }
    
    /**
     * Checks if a class is annotated with controller annotations.
     */
    private boolean isControllerClass(ClassInfo classInfo) {
        return classInfo.getAnnotations().stream()
                .anyMatch(annotation -> CONTROLLER_ANNOTATIONS.contains(annotation.getType()));
    }
    
    /**
     * Analyzes a controller class to extract all REST endpoints.
     */
    private List<RestEndpointInfo> analyzeControllerEndpoints(ClassInfo controllerClass) {
        String classPath = extractClassLevelPath(controllerClass);
        
        return controllerClass.getMethods().stream()
                .filter(this::isEndpointMethod)
                .map(method -> createEndpointInfo(method, controllerClass, classPath))
                .collect(Collectors.toList());
    }
    
    /**
     * Extracts the base path from class-level @RequestMapping annotation.
     */
    private String extractClassLevelPath(ClassInfo controllerClass) {
        return controllerClass.getAnnotations().stream()
                .filter(annotation -> "org.springframework.web.bind.annotation.RequestMapping".equals(annotation.getType()))
                .map(this::extractPathFromAnnotation)
                .findFirst()
                .orElse("");
    }
    
    /**
     * Checks if a method has request mapping annotations.
     */
    private boolean isEndpointMethod(MethodInfo method) {
        return method.getAnnotations().stream()
                .anyMatch(annotation -> REQUEST_MAPPING_ANNOTATIONS.contains(annotation.getType()));
    }
    
    /**
     * Creates RestEndpointInfo from a method and its class context.
     */
    private RestEndpointInfo createEndpointInfo(MethodInfo method, ClassInfo controllerClass, String classPath) {
        RestEndpointInfo.Builder builder = RestEndpointInfo.builder()
                .methodName(method.getName())
                .controllerClass(controllerClass.getFullyQualifiedName())
                .returnType(method.getReturnType())
                .isAsync(isAsyncMethod(method));
        
        // Process each request mapping annotation
        for (AnnotationInfo annotation : method.getAnnotations()) {
            if (REQUEST_MAPPING_ANNOTATIONS.contains(annotation.getType())) {
                processRequestMappingAnnotation(annotation, builder, classPath);
                break; // Use first mapping annotation found
            }
        }
        
        return builder.build();
    }
    
    /**
     * Processes a request mapping annotation to extract endpoint details.
     */
    private void processRequestMappingAnnotation(AnnotationInfo annotation, RestEndpointInfo.Builder builder, String classPath) {
        Map<String, Object> attributes = annotation.getAttributes();
        
        // Extract path
        String methodPath = extractPathFromAnnotation(annotation);
        String fullPath = combinePaths(classPath, methodPath);
        builder.path(fullPath);
        
        // Extract HTTP methods
        Set<RestEndpointInfo.HttpMethod> httpMethods = extractHttpMethods(annotation);
        builder.httpMethods(httpMethods);
        
        // Extract consumes and produces
        String consumes = extractStringArrayAttribute(attributes, "consumes");
        String produces = extractStringArrayAttribute(attributes, "produces");
        builder.consumes(consumes).produces(produces);
    }
    
    /**
     * Extracts path from annotation attributes.
     */
    private String extractPathFromAnnotation(AnnotationInfo annotation) {
        Map<String, Object> attributes = annotation.getAttributes();
        
        // Try "path" first, then "value" as fallback
        Object path = attributes.get("path");
        if (path == null) {
            path = attributes.get("value");
        }
        
        if (path instanceof String[]) {
            String[] pathArray = (String[]) path;
            return pathArray.length > 0 ? pathArray[0] : "";
        } else if (path instanceof String) {
            return (String) path;
        }
        
        return "";
    }
    
    /**
     * Extracts HTTP methods from annotation.
     */
    private Set<RestEndpointInfo.HttpMethod> extractHttpMethods(AnnotationInfo annotation) {
        Set<RestEndpointInfo.HttpMethod> methods = new HashSet<>();
        
        // For specific mapping annotations, derive method from annotation type
        String annotationType = annotation.getType();
        if ("org.springframework.web.bind.annotation.GetMapping".equals(annotationType)) {
            methods.add(RestEndpointInfo.HttpMethod.GET);
        } else if ("org.springframework.web.bind.annotation.PostMapping".equals(annotationType)) {
            methods.add(RestEndpointInfo.HttpMethod.POST);
        } else if ("org.springframework.web.bind.annotation.PutMapping".equals(annotationType)) {
            methods.add(RestEndpointInfo.HttpMethod.PUT);
        } else if ("org.springframework.web.bind.annotation.DeleteMapping".equals(annotationType)) {
            methods.add(RestEndpointInfo.HttpMethod.DELETE);
        } else if ("org.springframework.web.bind.annotation.PatchMapping".equals(annotationType)) {
            methods.add(RestEndpointInfo.HttpMethod.PATCH);
        } else if ("org.springframework.web.bind.annotation.RequestMapping".equals(annotationType)) {
            // Extract from method attribute
            Object methodAttr = annotation.getAttributes().get("method");
            if (methodAttr instanceof String[]) {
                for (String methodName : (String[]) methodAttr) {
                    try {
                        RestEndpointInfo.HttpMethod httpMethod = RestEndpointInfo.HttpMethod.valueOf(
                            methodName.replace("RequestMethod.", "").replace("org.springframework.web.bind.annotation.RequestMethod.", "")
                        );
                        methods.add(httpMethod);
                    } catch (IllegalArgumentException e) {
                        LOGGER.debug("Unknown HTTP method: %s", methodName);
                    }
                }
            } else if (methodAttr instanceof String) {
                try {
                    String methodName = ((String) methodAttr).replace("RequestMethod.", "").replace("org.springframework.web.bind.annotation.RequestMethod.", "");
                    RestEndpointInfo.HttpMethod httpMethod = RestEndpointInfo.HttpMethod.valueOf(methodName);
                    methods.add(httpMethod);
                } catch (IllegalArgumentException e) {
                    LOGGER.debug("Unknown HTTP method: %s", methodAttr);
                }
            }
        }
        
        // If no methods found, default to GET for compatibility
        if (methods.isEmpty()) {
            methods.add(RestEndpointInfo.HttpMethod.GET);
        }
        
        return methods;
    }
    
    /**
     * Combines class path and method path correctly.
     */
    private String combinePaths(String classPath, String methodPath) {
        if (classPath == null || classPath.isEmpty()) {
            return methodPath != null ? methodPath : "";
        }
        if (methodPath == null || methodPath.isEmpty()) {
            return classPath;
        }
        
        // Normalize paths
        String normalizedClassPath = classPath.startsWith("/") ? classPath : "/" + classPath;
        String normalizedMethodPath = methodPath.startsWith("/") ? methodPath : "/" + methodPath;
        
        if (normalizedClassPath.endsWith("/")) {
            normalizedClassPath = normalizedClassPath.substring(0, normalizedClassPath.length() - 1);
        }
        
        return normalizedClassPath + normalizedMethodPath;
    }
    
    /**
     * Checks if a method is async (returns CompletableFuture, DeferredResult, etc.).
     */
    private boolean isAsyncMethod(MethodInfo method) {
        String returnType = method.getReturnType();
        return returnType != null && (
            returnType.contains("CompletableFuture") ||
            returnType.contains("DeferredResult") ||
            returnType.contains("ListenableFuture") ||
            returnType.contains("ResponseEntity") ||
            returnType.contains("Callable")
        );
    }
    
    /**
     * Extracts string array attribute and returns first value or concatenation.
     */
    private String extractStringArrayAttribute(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        if (value instanceof String[]) {
            String[] array = (String[]) value;
            return array.length > 0 ? String.join(", ", array) : null;
        } else if (value instanceof String) {
            return (String) value;
        }
        return null;
    }
    
    @Override
    public boolean canAnalyze(JarContent jarContent) {
        return jarContent != null && !jarContent.isEmpty();
    }
}