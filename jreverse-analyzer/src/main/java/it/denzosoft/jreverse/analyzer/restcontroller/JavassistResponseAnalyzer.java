package it.denzosoft.jreverse.analyzer.restcontroller;

import it.denzosoft.jreverse.core.model.AnnotationInfo;
import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.MethodInfo;

import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

/**
 * Javassist-based implementation for analyzing REST endpoint response patterns.
 * Analyzes method return types, @ResponseBody annotations, and response configuration
 * using Javassist bytecode analysis.
 */
public class JavassistResponseAnalyzer implements ResponseAnalyzer {
    
    // Spring response-related annotations
    private static final Set<String> RESPONSE_BODY_ANNOTATIONS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("org.springframework.web.bind.annotation.ResponseBody"))
    );
    
    private static final Set<String> REST_CONTROLLER_ANNOTATIONS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("org.springframework.web.bind.annotation.RestController"))
    );
    
    private static final Set<String> RESPONSE_STATUS_ANNOTATIONS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("org.springframework.web.bind.annotation.ResponseStatus"))
    );
    
    // Type patterns for categorization
    private static final Pattern GENERIC_TYPE_PATTERN = Pattern.compile("([^<]+)<(.+)>");
    private static final Pattern ARRAY_TYPE_PATTERN = Pattern.compile("(.+)\\[\\]");
    
    // Common type mappings
    private static final Map<String, ResponseType> TYPE_MAPPINGS;
    static {
        Map<String, ResponseType> mappings = new HashMap<>();
        mappings.put("void", ResponseType.VOID);
        mappings.put("java.lang.String", ResponseType.STRING);
        mappings.put("java.lang.Integer", ResponseType.PRIMITIVE);
        mappings.put("java.lang.Long", ResponseType.PRIMITIVE);
        mappings.put("java.lang.Boolean", ResponseType.PRIMITIVE);
        mappings.put("java.lang.Double", ResponseType.PRIMITIVE);
        mappings.put("java.lang.Float", ResponseType.PRIMITIVE);
        mappings.put("org.springframework.http.ResponseEntity", ResponseType.RESPONSE_ENTITY);
        mappings.put("java.util.Optional", ResponseType.OPTIONAL);
        mappings.put("java.util.concurrent.Future", ResponseType.FUTURE);
        mappings.put("java.util.concurrent.CompletableFuture", ResponseType.FUTURE);
        mappings.put("reactor.core.publisher.Mono", ResponseType.REACTIVE);
        mappings.put("reactor.core.publisher.Flux", ResponseType.REACTIVE);
        mappings.put("org.springframework.web.servlet.ModelAndView", ResponseType.VIEW);
        mappings.put("org.springframework.web.servlet.View", ResponseType.VIEW);
        TYPE_MAPPINGS = Collections.unmodifiableMap(mappings);
    }
    
    @Override
    public ResponseAnalysisResult analyzeResponses(List<ClassInfo> restControllers) {
        long startTime = System.currentTimeMillis();
        
        List<ResponseInfo> allResponses = restControllers.stream()
            .flatMap(controllerClass -> analyzeClassResponses(controllerClass).stream())
            .collect(Collectors.toList());
        
        long analysisTime = System.currentTimeMillis() - startTime;
        String jarFileName = restControllers.isEmpty() ? "" : "analyzed-jar";
        
        return new ResponseAnalysisResult(allResponses, analysisTime, jarFileName);
    }
    
    @Override
    public List<ResponseInfo> analyzeClassResponses(ClassInfo controllerClass) {
        return controllerClass.getMethods().stream()
            .filter(this::isRestEndpointMethod)
            .map(method -> analyzeMethodResponse(method, controllerClass))
            .collect(Collectors.toList());
    }
    
    @Override
    public ResponseInfo analyzeMethodResponse(MethodInfo method, ClassInfo controllerClass) {
        ResponseInfo.Builder builder = ResponseInfo.builder()
            .methodName(method.getName())
            .declaringClass(controllerClass.getFullyQualifiedName())
            .returnType(method.getReturnType());
        
        // Categorize return type
        ResponseType responseType = categorizeReturnType(method.getReturnType());
        builder.responseType(responseType);
        
        // Analyze response body
        boolean hasResponseBody = hasResponseBody(method, controllerClass);
        builder.hasResponseBody(hasResponseBody);
        
        // Analyze response annotations
        analyzeResponseAnnotations(method, builder);
        
        // Analyze return type details
        analyzeReturnTypeDetails(method.getReturnType(), builder, responseType);
        
        // Extract content types from mapping annotations
        extractContentTypes(method, controllerClass, builder);
        
        // Determine if async
        boolean isAsync = responseType.isAsync();
        builder.isAsync(isAsync);
        
        return builder.build();
    }
    
    @Override
    public ResponseType categorizeReturnType(String returnType) {
        if (returnType == null || returnType.trim().isEmpty()) {
            return ResponseType.UNKNOWN;
        }
        
        String cleanType = cleanReturnType(returnType);
        
        // Direct mappings first
        ResponseType directMapping = TYPE_MAPPINGS.get(cleanType);
        if (directMapping != null) {
            return directMapping;
        }
        
        // Primitive types
        if (isPrimitiveType(cleanType)) {
            return ResponseType.PRIMITIVE;
        }
        
        // Array types
        if (ARRAY_TYPE_PATTERN.matcher(cleanType).matches()) {
            return ResponseType.ARRAY;
        }
        
        // Generic types
        if (GENERIC_TYPE_PATTERN.matcher(cleanType).matches()) {
            return categorizeGenericType(cleanType);
        }
        
        // Collection interfaces
        if (isCollectionType(cleanType)) {
            return ResponseType.COLLECTION;
        }
        
        // Map interfaces
        if (isMapType(cleanType)) {
            return ResponseType.MAP;
        }
        
        // Default to object for custom types
        return ResponseType.OBJECT;
    }
    
    @Override
    public boolean hasResponseBody(MethodInfo method, ClassInfo controllerClass) {
        // Check method-level @ResponseBody
        if (hasAnnotation(method.getAnnotations(), RESPONSE_BODY_ANNOTATIONS)) {
            return true;
        }
        
        // Check if controller is @RestController (implies @ResponseBody on all methods)
        if (hasAnnotation(controllerClass.getAnnotations(), REST_CONTROLLER_ANNOTATIONS)) {
            return true;
        }
        
        // Methods returning void or view types typically don't have response body
        ResponseType responseType = categorizeReturnType(method.getReturnType());
        return responseType.hasResponseBody();
    }
    
    private boolean isRestEndpointMethod(MethodInfo method) {
        Set<String> mappingAnnotations = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "org.springframework.web.bind.annotation.RequestMapping",
            "org.springframework.web.bind.annotation.GetMapping",
            "org.springframework.web.bind.annotation.PostMapping",
            "org.springframework.web.bind.annotation.PutMapping",
            "org.springframework.web.bind.annotation.DeleteMapping",
            "org.springframework.web.bind.annotation.PatchMapping"
        )));
        
        return hasAnnotation(method.getAnnotations(), mappingAnnotations);
    }
    
    private void analyzeResponseAnnotations(MethodInfo method, ResponseInfo.Builder builder) {
        for (AnnotationInfo annotation : method.getAnnotations()) {
            if (RESPONSE_STATUS_ANNOTATIONS.contains(annotation.getType())) {
                analyzeResponseStatusAnnotation(annotation, builder);
            }
            
            if (RESPONSE_BODY_ANNOTATIONS.contains(annotation.getType())) {
                builder.addResponseAnnotation(annotation);
            }
        }
    }
    
    private void analyzeResponseStatusAnnotation(AnnotationInfo annotation, ResponseInfo.Builder builder) {
        Object valueObj = annotation.getAttributes().get("value");
        if (valueObj != null) {
            builder.httpStatus(extractStatusCode(valueObj.toString()));
        }
        
        Object codeObj = annotation.getAttributes().get("code");
        if (codeObj != null) {
            builder.httpStatus(extractStatusCode(codeObj.toString()));
        }
        
        Object reasonObj = annotation.getAttributes().get("reason");
        if (reasonObj != null) {
            builder.httpStatusReason(cleanupStringValue(reasonObj.toString()));
        }
    }
    
    private void analyzeReturnTypeDetails(String returnType, ResponseInfo.Builder builder, ResponseType responseType) {
        if (GENERIC_TYPE_PATTERN.matcher(returnType).matches()) {
            java.util.regex.Matcher matcher = GENERIC_TYPE_PATTERN.matcher(returnType);
            if (matcher.find()) {
                String containerType = matcher.group(1);
                String innerType = matcher.group(2);
                
                // For wrapped types like ResponseEntity<User>
                if (TYPE_MAPPINGS.containsKey(containerType)) {
                    builder.wrappedType(innerType);
                }
                
                // For collections like List<String>
                if (responseType == ResponseType.COLLECTION) {
                    builder.elementType(innerType);
                }
                
                // For maps like Map<String, Object>
                if (responseType == ResponseType.MAP && innerType.contains(",")) {
                    String[] types = innerType.split(",", 2);
                    if (types.length == 2) {
                        builder.keyType(types[0].trim());
                        builder.valueType(types[1].trim());
                    }
                }
            }
        }
        
        if (ARRAY_TYPE_PATTERN.matcher(returnType).matches()) {
            java.util.regex.Matcher matcher = ARRAY_TYPE_PATTERN.matcher(returnType);
            if (matcher.find()) {
                builder.elementType(matcher.group(1));
            }
        }
    }
    
    private void extractContentTypes(MethodInfo method, ClassInfo controllerClass, ResponseInfo.Builder builder) {
        // Check method-level mapping annotations
        extractContentTypesFromMethod(method, builder);
        
        // Check class-level mapping annotations
        extractContentTypesFromClass(controllerClass, builder);
    }
    
    private void extractContentTypesFromMethod(MethodInfo method, ResponseInfo.Builder builder) {
        Set<String> mappingAnnotations = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "org.springframework.web.bind.annotation.RequestMapping",
            "org.springframework.web.bind.annotation.GetMapping",
            "org.springframework.web.bind.annotation.PostMapping",
            "org.springframework.web.bind.annotation.PutMapping",
            "org.springframework.web.bind.annotation.DeleteMapping",
            "org.springframework.web.bind.annotation.PatchMapping"
        )));
        
        for (AnnotationInfo annotation : method.getAnnotations()) {
            if (mappingAnnotations.contains(annotation.getType())) {
                Object producesObj = annotation.getAttributes().get("produces");
                if (producesObj != null) {
                    List<String> contentTypes = parseContentTypes(producesObj.toString());
                    for (String contentType : contentTypes) {
                        builder.addProducesContentType(contentType);
                    }
                }
            }
        }
    }
    
    private void extractContentTypesFromClass(ClassInfo controllerClass, ResponseInfo.Builder builder) {
        for (AnnotationInfo annotation : controllerClass.getAnnotations()) {
            if ("org.springframework.web.bind.annotation.RequestMapping".equals(annotation.getType())) {
                Object producesObj = annotation.getAttributes().get("produces");
                if (producesObj != null) {
                    List<String> contentTypes = parseContentTypes(producesObj.toString());
                    contentTypes.forEach(builder::addProducesContentType);
                }
            }
        }
    }
    
    private ResponseType categorizeGenericType(String cleanType) {
        java.util.regex.Matcher matcher = GENERIC_TYPE_PATTERN.matcher(cleanType);
        if (matcher.find()) {
            String containerType = matcher.group(1);
            
            // Check if container type is in our mappings
            ResponseType containerResponseType = TYPE_MAPPINGS.get(containerType);
            if (containerResponseType != null) {
                return containerResponseType;
            }
            
            // Check collection types
            if (isCollectionType(containerType)) {
                return ResponseType.COLLECTION;
            }
            
            // Check map types
            if (isMapType(containerType)) {
                return ResponseType.MAP;
            }
        }
        
        return ResponseType.OBJECT;
    }
    
    private String cleanReturnType(String returnType) {
        return returnType.trim().replaceAll("\\s+", " ");
    }
    
    private boolean isPrimitiveType(String type) {
        Set<String> primitives = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "boolean", "byte", "char", "short", "int", "long", "float", "double"
        )));
        return primitives.contains(type) || type.startsWith("java.lang.") && 
               (type.endsWith("Integer") || type.endsWith("Long") || type.endsWith("Double") || 
                type.endsWith("Float") || type.endsWith("Boolean") || type.endsWith("Byte") ||
                type.endsWith("Short") || type.endsWith("Character"));
    }
    
    private boolean isCollectionType(String type) {
        return type.equals("java.util.List") || type.equals("java.util.Set") || 
               type.equals("java.util.Collection") || type.equals("java.util.ArrayList") ||
               type.equals("java.util.LinkedList") || type.equals("java.util.HashSet") ||
               type.equals("java.util.TreeSet") || type.equals("java.util.LinkedHashSet");
    }
    
    private boolean isMapType(String type) {
        return type.equals("java.util.Map") || type.equals("java.util.HashMap") ||
               type.equals("java.util.LinkedHashMap") || type.equals("java.util.TreeMap") ||
               type.equals("java.util.ConcurrentHashMap");
    }
    
    private boolean hasAnnotation(Set<AnnotationInfo> annotations, Set<String> targetAnnotations) {
        return annotations.stream()
            .anyMatch(annotation -> targetAnnotations.contains(annotation.getType()));
    }
    
    private String extractStatusCode(String statusValue) {
        // Handle enum values like "HttpStatus.OK" or direct codes like "200"
        if (statusValue.contains(".")) {
            String[] parts = statusValue.split("\\.");
            if (parts.length >= 2) {
                return parts[parts.length - 1]; // Get the enum name like "OK"
            }
        }
        return statusValue.replaceAll("[^\\w]", ""); // Clean up the value
    }
    
    private String cleanupStringValue(String value) {
        if (value == null) return null;
        return value.trim().replaceAll("^\"|\"$", ""); // Remove surrounding quotes
    }
    
    private List<String> parseContentTypes(String contentTypesStr) {
        if (contentTypesStr == null || contentTypesStr.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        String cleaned = contentTypesStr.trim();
        
        // Handle array format: ["application/json", "application/xml"]
        if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        
        return Arrays.stream(cleaned.split(","))
            .map(String::trim)
            .map(s -> s.replaceAll("^\"|\"$", "")) // Remove quotes
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }
}