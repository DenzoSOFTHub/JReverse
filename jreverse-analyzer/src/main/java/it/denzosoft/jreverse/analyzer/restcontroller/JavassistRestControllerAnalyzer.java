package it.denzosoft.jreverse.analyzer.restcontroller;

import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.AnnotationInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Javassist-based implementation of RestControllerAnalyzer that uses bytecode analysis
 * to find and analyze REST controllers, with support for both @RestController and
 * @Controller + @ResponseBody patterns.
 */
public class JavassistRestControllerAnalyzer implements RestControllerAnalyzer {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistRestControllerAnalyzer.class);
    
    // Spring MVC annotations
    private static final String REST_CONTROLLER_ANNOTATION = "org.springframework.web.bind.annotation.RestController";
    private static final String CONTROLLER_ANNOTATION = "org.springframework.stereotype.Controller";
    private static final String RESPONSE_BODY_ANNOTATION = "org.springframework.web.bind.annotation.ResponseBody";
    private static final String REQUEST_MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.RequestMapping";
    
    // CORS annotations
    private static final String CROSS_ORIGIN_ANNOTATION = "org.springframework.web.bind.annotation.CrossOrigin";
    
    // Security annotations
    private static final List<String> SECURITY_ANNOTATIONS = Arrays.asList(
        "org.springframework.security.access.prepost.PreAuthorize",
        "org.springframework.security.access.prepost.PostAuthorize",
        "org.springframework.security.access.annotation.Secured",
        "javax.annotation.security.RolesAllowed",
        "javax.annotation.security.PermitAll",
        "javax.annotation.security.DenyAll"
    );
    
    // Caching annotations
    private static final List<String> CACHING_ANNOTATIONS = Arrays.asList(
        "org.springframework.cache.annotation.Cacheable",
        "org.springframework.cache.annotation.CacheEvict",
        "org.springframework.cache.annotation.CachePut",
        "org.springframework.cache.annotation.Caching"
    );
    
    @Override
    public RestControllerAnalysisResult analyzeRestControllers(JarContent jarContent) {
        long startTime = System.currentTimeMillis();
        
        LOGGER.info("Starting REST controller analysis for JAR: %s", jarContent.getLocation().getFileName());
        
        try {
            // Find all REST controller classes
            List<RestControllerInfo> restControllers = findRestControllers(jarContent);
            LOGGER.debug("Found %d REST controllers", restControllers.size());
            
            if (restControllers.isEmpty()) {
                return RestControllerAnalysisResult.noControllersFound();
            }
            
            long analysisTime = System.currentTimeMillis() - startTime;
            LOGGER.info("REST controller analysis completed in %dms, found %d controllers", 
                       analysisTime, restControllers.size());
            
            return RestControllerAnalysisResult.withTiming(restControllers, analysisTime);
            
        } catch (Exception e) {
            long analysisTime = System.currentTimeMillis() - startTime;
            LOGGER.error("REST controller analysis failed after " + analysisTime + "ms", e);
            return RestControllerAnalysisResult.error("Analysis failed: " + e.getMessage());
        }
    }
    
    /**
     * Finds all REST controller classes in the JAR content.
     */
    private List<RestControllerInfo> findRestControllers(JarContent jarContent) {
        return jarContent.getClasses().stream()
            .filter(this::isRestController)
            .map(this::createRestControllerInfo)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }
    
    /**
     * Checks if a class is a REST controller.
     */
    private boolean isRestController(ClassInfo classInfo) {
        // Check for @RestController annotation
        if (classInfo.hasAnnotation(REST_CONTROLLER_ANNOTATION)) {
            return true;
        }
        
        // Check for @Controller + @ResponseBody combination
        if (classInfo.hasAnnotation(CONTROLLER_ANNOTATION) && 
            classInfo.hasAnnotation(RESPONSE_BODY_ANNOTATION)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Creates RestControllerInfo from a ClassInfo.
     */
    private Optional<RestControllerInfo> createRestControllerInfo(ClassInfo classInfo) {
        try {
            // Determine controller type
            RestControllerInfo.RestControllerType controllerType = determineControllerType(classInfo);
            
            // Extract base path from class-level @RequestMapping
            Optional<String> basePath = extractBasePath(classInfo);
            
            // Extract CORS patterns
            List<String> crossOriginPatterns = extractCrossOriginPatterns(classInfo);
            
            // Extract security annotations
            List<String> securityAnnotations = extractSecurityAnnotations(classInfo);
            
            // Extract caching annotations
            List<String> cachingAnnotations = extractCachingAnnotations(classInfo);
            
            // Check for annotations
            boolean hasRequestMappingClass = classInfo.hasAnnotation(REQUEST_MAPPING_ANNOTATION);
            boolean hasResponseBodyClass = classInfo.hasAnnotation(RESPONSE_BODY_ANNOTATION);
            
            // Find parent controller (if any)
            Optional<String> parentController = findParentController(classInfo);
            
            return Optional.of(RestControllerInfo.builder()
                .classInfo(classInfo)
                .controllerType(controllerType)
                .basePath(basePath.orElse(null))
                .crossOriginPatterns(crossOriginPatterns)
                .securityAnnotations(securityAnnotations)
                .cachingAnnotations(cachingAnnotations)
                .hasRequestMappingClass(hasRequestMappingClass)
                .hasResponseBodyClass(hasResponseBodyClass)
                .parentController(parentController.orElse(null))
                .build());
            
        } catch (Exception e) {
            LOGGER.warn("Failed to create RestControllerInfo for class %s: %s", 
                       classInfo.getFullyQualifiedName(), e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Determines the type of REST controller.
     */
    private RestControllerInfo.RestControllerType determineControllerType(ClassInfo classInfo) {
        if (classInfo.hasAnnotation(REST_CONTROLLER_ANNOTATION)) {
            return RestControllerInfo.RestControllerType.REST_CONTROLLER;
        }
        
        if (classInfo.hasAnnotation(CONTROLLER_ANNOTATION) && 
            classInfo.hasAnnotation(RESPONSE_BODY_ANNOTATION)) {
            return RestControllerInfo.RestControllerType.HYBRID_CONTROLLER;
        }
        
        // Default to REST_CONTROLLER if we got here (shouldn't happen due to isRestController filter)
        return RestControllerInfo.RestControllerType.REST_CONTROLLER;
    }
    
    /**
     * Extracts the base path from class-level @RequestMapping annotation.
     */
    private Optional<String> extractBasePath(ClassInfo classInfo) {
        return classInfo.getAnnotations().stream()
            .filter(annotation -> REQUEST_MAPPING_ANNOTATION.equals(annotation.getType()))
            .findFirst()
            .flatMap(this::extractPathFromRequestMapping);
    }
    
    /**
     * Extracts path value from @RequestMapping annotation.
     */
    private Optional<String> extractPathFromRequestMapping(AnnotationInfo annotation) {
        // Try to extract path from annotation attributes
        // This is simplified - in a real implementation, you'd parse the annotation values
        Object pathValueObj = annotation.getAttributes().get("value");
        String pathValue = pathValueObj != null ? pathValueObj.toString() : null;
        if (pathValue == null || pathValue.isEmpty()) {
            Object pathObj = annotation.getAttributes().get("path");
            pathValue = pathObj != null ? pathObj.toString() : null;
        }
        
        if (pathValue != null && !pathValue.isEmpty()) {
            // Clean up the path value (remove quotes, brackets)
            pathValue = cleanupPathValue(pathValue);
            return Optional.of(pathValue);
        }
        
        return Optional.empty();
    }
    
    /**
     * Cleans up extracted path values by removing quotes and array brackets.
     */
    private String cleanupPathValue(String pathValue) {
        if (pathValue == null) return "";
        
        // Remove common annotation value formatting
        pathValue = pathValue.replaceAll("^[\"'\\[]*", "");
        pathValue = pathValue.replaceAll("[\"'\\]]*$", "");
        pathValue = pathValue.trim();
        
        return pathValue;
    }
    
    /**
     * Extracts CORS patterns from @CrossOrigin annotation.
     */
    private List<String> extractCrossOriginPatterns(ClassInfo classInfo) {
        return classInfo.getAnnotations().stream()
            .filter(annotation -> CROSS_ORIGIN_ANNOTATION.equals(annotation.getType()))
            .flatMap(annotation -> extractOriginsFromCrossOrigin(annotation).stream())
            .collect(Collectors.toList());
    }
    
    /**
     * Extracts origins from @CrossOrigin annotation.
     */
    private List<String> extractOriginsFromCrossOrigin(AnnotationInfo annotation) {
        // This is simplified - in a real implementation, you'd properly parse annotation arrays
        Object originsObj = annotation.getAttributes().get("origins");
        String origins = originsObj != null ? originsObj.toString() : null;
        if (origins == null) {
            Object valueObj = annotation.getAttributes().get("value");
            origins = valueObj != null ? valueObj.toString() : null;
        }
        
        if (origins != null && !origins.isEmpty()) {
            // Handle array format: ["origin1","origin2"] or single values "origin"
            origins = origins.trim();
            
            if (origins.startsWith("[") && origins.endsWith("]")) {
                // Array format - remove brackets and split by comma
                String arrayContent = origins.substring(1, origins.length() - 1);
                return Arrays.stream(arrayContent.split(","))
                    .map(s -> s.trim().replaceAll("^\"|\"$", "")) // Remove quotes from each element
                    .collect(Collectors.toList());
            } else {
                // Single value - remove quotes
                origins = origins.replaceAll("^\"|\"$", "");
                return Collections.singletonList(origins);
            }
        }
        
        return Collections.emptyList();
    }
    
    /**
     * Extracts security annotations from class.
     */
    private List<String> extractSecurityAnnotations(ClassInfo classInfo) {
        return classInfo.getAnnotations().stream()
            .map(AnnotationInfo::getType)
            .filter(SECURITY_ANNOTATIONS::contains)
            .collect(Collectors.toList());
    }
    
    /**
     * Extracts caching annotations from class.
     */
    private List<String> extractCachingAnnotations(ClassInfo classInfo) {
        return classInfo.getAnnotations().stream()
            .map(AnnotationInfo::getType)
            .filter(CACHING_ANNOTATIONS::contains)
            .collect(Collectors.toList());
    }
    
    /**
     * Finds parent controller if this class extends another controller.
     */
    private Optional<String> findParentController(ClassInfo classInfo) {
        String superClassName = classInfo.getSuperClassName();
        if (superClassName != null && 
            !superClassName.equals("java.lang.Object") &&
            !superClassName.startsWith("java.")) {
            return Optional.of(superClassName);
        }
        
        return Optional.empty();
    }
}