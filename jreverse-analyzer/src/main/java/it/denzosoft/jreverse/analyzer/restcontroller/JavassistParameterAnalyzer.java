package it.denzosoft.jreverse.analyzer.restcontroller;

import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.AnnotationInfo;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.ParameterInfo;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Javassist-based implementation of ParameterAnalyzer for REST endpoint parameter analysis.
 * Provides comprehensive analysis of Spring MVC parameter annotations, validation,
 * and consistency checking using only static bytecode analysis.
 */
public class JavassistParameterAnalyzer implements ParameterAnalyzer {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistParameterAnalyzer.class);
    
    // Spring parameter binding annotations
    private static final Set<String> SPRING_PARAM_ANNOTATIONS = Set.of(
        "org.springframework.web.bind.annotation.RequestParam",
        "org.springframework.web.bind.annotation.PathVariable", 
        "org.springframework.web.bind.annotation.RequestBody",
        "org.springframework.web.bind.annotation.RequestHeader",
        "org.springframework.web.bind.annotation.CookieValue",
        "org.springframework.web.bind.annotation.RequestAttribute",
        "org.springframework.web.bind.annotation.SessionAttribute",
        "org.springframework.web.bind.annotation.ModelAttribute",
        "org.springframework.web.bind.annotation.MatrixVariable",
        "org.springframework.web.bind.annotation.RequestPart"
    );
    
    // Validation annotations
    private static final Set<String> VALIDATION_ANNOTATIONS = Set.of(
        "javax.validation.Valid",
        "javax.validation.constraints.NotNull",
        "javax.validation.constraints.NotEmpty",
        "javax.validation.constraints.NotBlank",
        "javax.validation.constraints.Size",
        "javax.validation.constraints.Min",
        "javax.validation.constraints.Max",
        "javax.validation.constraints.Pattern",
        "javax.validation.constraints.Email",
        "javax.validation.constraints.Positive",
        "javax.validation.constraints.Negative",
        "javax.validation.constraints.PositiveOrZero",
        "javax.validation.constraints.NegativeOrZero",
        "javax.validation.constraints.Past",
        "javax.validation.constraints.Future",
        "javax.validation.constraints.PastOrPresent",
        "javax.validation.constraints.FutureOrPresent",
        // Jakarta validation (for newer Spring Boot versions)
        "jakarta.validation.Valid",
        "jakarta.validation.constraints.NotNull",
        "jakarta.validation.constraints.NotEmpty",
        "jakarta.validation.constraints.NotBlank",
        "jakarta.validation.constraints.Size",
        "jakarta.validation.constraints.Min",
        "jakarta.validation.constraints.Max",
        "jakarta.validation.constraints.Pattern",
        "jakarta.validation.constraints.Email"
    );
    
    // Servlet API types that are implicitly bound
    private static final Set<String> SERVLET_API_TYPES = Set.of(
        "javax.servlet.http.HttpServletRequest",
        "javax.servlet.http.HttpServletResponse",
        "javax.servlet.ServletRequest",
        "javax.servlet.ServletResponse",
        "javax.servlet.http.HttpSession",
        "javax.servlet.ServletContext"
    );
    
    // Collection and Map patterns
    private static final Pattern COLLECTION_PATTERN = Pattern.compile("java\\.util\\.(List|Set|Collection)<(.+)>");
    private static final Pattern MAP_PATTERN = Pattern.compile("java\\.util\\.Map<(.+),\\s*(.+)>");
    
    @Override
    public boolean canAnalyze(JarContent jarContent) {
        if (jarContent == null) {
            return false;
        }
        
        // Check for Spring Web dependencies
        boolean hasSpringWeb = jarContent.getClassByName("org.springframework.web.bind.annotation.RequestMapping") != null ||
                              jarContent.getClassByName("org.springframework.web.bind.annotation.RestController") != null ||
                              jarContent.getClassByName("org.springframework.stereotype.Controller") != null;
        
        LOGGER.debug("ParameterAnalyzer can analyze: {}", hasSpringWeb);
        return hasSpringWeb;
    }
    
    @Override
    public ParameterAnalysisResult analyzeParameters(List<RestEndpointInfo> endpoints) {
        LOGGER.info("Starting parameter analysis for {} endpoints", endpoints.size());
        
        ParameterAnalysisResult.Builder resultBuilder = ParameterAnalysisResult.builder();
        
        try {
            Map<String, List<EndpointParameterInfo>> endpointParameters = new LinkedHashMap<>();
            List<String> validationErrors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            
            for (RestEndpointInfo endpoint : endpoints) {
                try {
                    String endpointKey = createEndpointKey(endpoint);
                    List<EndpointParameterInfo> parameters = analyzeEndpointParameters(endpoint);
                    
                    endpointParameters.put(endpointKey, parameters);
                    
                    // Validate individual endpoint parameters
                    List<String> endpointErrors = validateParameterConsistency(endpoint);
                    validationErrors.addAll(endpointErrors);
                    
                } catch (Exception e) {
                    String error = "Failed to analyze parameters for endpoint " + 
                                  endpoint.getMethodName() + ": " + e.getMessage();
                    LOGGER.warn(error, e);
                    validationErrors.add(error);
                }
            }
            
            // Cross-endpoint validation
            List<String> crossEndpointErrors = validateCrossEndpointConsistency(endpoints);
            validationErrors.addAll(crossEndpointErrors);
            
            resultBuilder
                .parametersByEndpoint(endpointParameters)
                .validationErrors(validationErrors)
                .warnings(warnings);
            
        } catch (Exception e) {
            LOGGER.error("Error during parameter analysis", e);
            resultBuilder.addValidationError("Parameter analysis failed: " + e.getMessage());
        }
        
        ParameterAnalysisResult result = resultBuilder.build();
        LOGGER.info("Parameter analysis completed. Found {} parameters across {} endpoints", 
                   result.getParameters().size(), endpoints.size());
        
        return result;
    }
    
    @Override
    public List<EndpointParameterInfo> analyzeEndpointParameters(RestEndpointInfo endpoint) {
        List<EndpointParameterInfo> parameters = new ArrayList<>();
        
        try {
            List<ParameterInfo> methodParameters = endpoint.getMethodInfo().getParameters();
            List<String> pathVariables = endpoint.getPathVariables();
            
            for (ParameterInfo paramInfo : methodParameters) {
                EndpointParameterInfo parameterInfo = analyzeIndividualParameter(
                    paramInfo, pathVariables, endpoint);
                parameters.add(parameterInfo);
            }
            
        } catch (Exception e) {
            LOGGER.warn("Failed to analyze parameters for endpoint {}: {}", 
                       endpoint.getMethodName(), e.getMessage());
        }
        
        return parameters;
    }
    
    private EndpointParameterInfo analyzeIndividualParameter(ParameterInfo paramInfo, 
                                                           List<String> pathVariables,
                                                           RestEndpointInfo endpoint) {
        EndpointParameterInfo.Builder builder = EndpointParameterInfo.builder()
            .parameterInfo(paramInfo)
            .pathVariableNames(pathVariables);
        
        // Analyze parameter annotations
        Set<AnnotationInfo> annotations = paramInfo.getAnnotations();
        ParameterType parameterType = ParameterType.UNKNOWN;
        String bindingName = null;
        boolean required = true;
        String defaultValue = null;
        Set<String> validationAnnotations = new LinkedHashSet<>();
        boolean hasValidation = false;
        
        // Process each annotation
        for (AnnotationInfo annotation : annotations) {
            String annotationType = annotation.getType();
            
            if (SPRING_PARAM_ANNOTATIONS.contains(annotationType)) {
                ParameterType type = ParameterType.fromAnnotation(annotationType);
                if (type != ParameterType.UNKNOWN) {
                    parameterType = type;
                    
                    // Extract annotation attributes
                    Map<String, Object> attributes = annotation.getAttributes();
                    if (attributes != null) {
                        bindingName = extractStringAttribute(attributes, "value", "name");
                        required = extractBooleanAttribute(attributes, "required", true);
                        defaultValue = extractStringAttribute(attributes, "defaultValue");
                    }
                }
            } else if (VALIDATION_ANNOTATIONS.contains(annotationType)) {
                hasValidation = true;
                validationAnnotations.add(annotationType);
            }
        }
        
        // If no explicit binding annotation, determine implicit type
        if (parameterType == ParameterType.UNKNOWN) {
            parameterType = ParameterType.fromParameterClass(paramInfo.getType());
        }
        
        // Analyze parameter type complexity
        TypeAnalysis typeAnalysis = analyzeParameterType(paramInfo.getType());
        
        return builder
            .parameterType(parameterType)
            .bindingName(bindingName)
            .required(required)
            .defaultValue(defaultValue)
            .validationAnnotations(validationAnnotations)
            .hasValidation(hasValidation)
            .isCollection(typeAnalysis.isCollection)
            .isMap(typeAnalysis.isMap)
            .collectionElementType(typeAnalysis.collectionElementType)
            .mapKeyType(typeAnalysis.mapKeyType)
            .mapValueType(typeAnalysis.mapValueType)
            .produces(endpoint.getProduces())
            .consumes(endpoint.getConsumes())
            .build();
    }
    
    private TypeAnalysis analyzeParameterType(String parameterType) {
        TypeAnalysis analysis = new TypeAnalysis();
        
        // Check for Collection types
        java.util.regex.Matcher collectionMatcher = COLLECTION_PATTERN.matcher(parameterType);
        if (collectionMatcher.find()) {
            analysis.isCollection = true;
            analysis.collectionElementType = collectionMatcher.group(2).trim();
        }
        
        // Check for Map types
        java.util.regex.Matcher mapMatcher = MAP_PATTERN.matcher(parameterType);
        if (mapMatcher.find()) {
            analysis.isMap = true;
            analysis.mapKeyType = mapMatcher.group(1).trim();
            analysis.mapValueType = mapMatcher.group(2).trim();
        }
        
        return analysis;
    }
    
    @Override
    public List<String> validateParameterConsistency(RestEndpointInfo endpoint) {
        List<String> errors = new ArrayList<>();
        List<EndpointParameterInfo> parameters = analyzeEndpointParameters(endpoint);
        
        // Check for multiple @RequestBody parameters
        long requestBodyCount = parameters.stream()
            .filter(p -> p.getParameterType() == ParameterType.REQUEST_BODY)
            .count();
        if (requestBodyCount > 1) {
            errors.add("Endpoint " + endpoint.getMethodName() + 
                      " has multiple @RequestBody parameters, which is not allowed");
        }
        
        // Validate path variables
        Set<String> pathVariableNames = new HashSet<>(endpoint.getPathVariables());
        for (EndpointParameterInfo param : parameters) {
            if (param.getParameterType() == ParameterType.PATH_VARIABLE) {
                String bindingName = param.getEffectiveName();
                if (!pathVariableNames.contains(bindingName)) {
                    errors.add("Path variable '" + bindingName + "' in parameter " + 
                              param.getName() + " does not match any path variables in " +
                              endpoint.getCombinedPath());
                }
            }
        }
        
        // Check for required parameters with default values (potential issue)
        for (EndpointParameterInfo param : parameters) {
            if (param.isRequired() && param.hasDefaultValue() && 
                param.getParameterType() != ParameterType.REQUEST_PARAM) {
                errors.add("Parameter '" + param.getName() + "' is marked as required but has a default value");
            }
        }
        
        return errors;
    }
    
    @Override
    public List<String> validateCrossEndpointConsistency(List<RestEndpointInfo> endpoints) {
        List<String> errors = new ArrayList<>();
        
        // Collect all parameters for cross-validation
        Map<String, Set<ParameterType>> parameterNameToTypes = new HashMap<>();
        
        for (RestEndpointInfo endpoint : endpoints) {
            List<EndpointParameterInfo> parameters = analyzeEndpointParameters(endpoint);
            
            for (EndpointParameterInfo param : parameters) {
                String name = param.getEffectiveName();
                parameterNameToTypes.computeIfAbsent(name, k -> new HashSet<>())
                    .add(param.getParameterType());
            }
        }
        
        // Check for parameter names used with different types across endpoints
        for (Map.Entry<String, Set<ParameterType>> entry : parameterNameToTypes.entrySet()) {
            if (entry.getValue().size() > 1) {
                errors.add("Parameter name '" + entry.getKey() + 
                          "' is used with different binding types: " + entry.getValue());
            }
        }
        
        return errors;
    }
    
    @Override
    public ParameterPatternAnalysis analyzeParameterPatterns(List<RestEndpointInfo> endpoints) {
        Map<String, Integer> parameterNameCounts = new HashMap<>();
        Map<String, Integer> parameterTypeCounts = new HashMap<>();
        List<String> namingPatterns = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        List<String> potentialIssues = new ArrayList<>();
        
        for (RestEndpointInfo endpoint : endpoints) {
            List<EndpointParameterInfo> parameters = analyzeEndpointParameters(endpoint);
            
            for (EndpointParameterInfo param : parameters) {
                // Count parameter names
                String name = param.getEffectiveName();
                parameterNameCounts.merge(name, 1, Integer::sum);
                
                // Count parameter types
                String type = param.getSimpleType();
                parameterTypeCounts.merge(type, 1, Integer::sum);
            }
        }
        
        // Identify common parameter names (used in multiple endpoints)
        List<String> commonParameterNames = parameterNameCounts.entrySet().stream()
            .filter(entry -> entry.getValue() > 1)
            .map(Map.Entry::getKey)
            .sorted()
            .collect(Collectors.toList());
        
        // Identify common parameter types
        List<String> commonParameterTypes = parameterTypeCounts.entrySet().stream()
            .filter(entry -> entry.getValue() > 2)
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        // Analyze naming patterns
        analyzeNamingPatterns(parameterNameCounts.keySet(), namingPatterns, recommendations, potentialIssues);
        
        return new ParameterPatternAnalysis(
            commonParameterNames,
            commonParameterTypes,
            namingPatterns,
            recommendations,
            potentialIssues
        );
    }
    
    private void analyzeNamingPatterns(Set<String> parameterNames, 
                                     List<String> namingPatterns,
                                     List<String> recommendations, 
                                     List<String> potentialIssues) {
        
        int camelCaseCount = 0;
        int snakeCaseCount = 0;
        int singleWordCount = 0;
        
        for (String name : parameterNames) {
            if (name.contains("_")) {
                snakeCaseCount++;
            } else if (name.matches(".*[a-z][A-Z].*")) {
                camelCaseCount++;
            } else if (!name.contains("_") && name.toLowerCase().equals(name)) {
                singleWordCount++;
            }
        }
        
        if (camelCaseCount > 0) {
            namingPatterns.add("CamelCase naming (" + camelCaseCount + " parameters)");
        }
        if (snakeCaseCount > 0) {
            namingPatterns.add("Snake_case naming (" + snakeCaseCount + " parameters)");
        }
        if (singleWordCount > 0) {
            namingPatterns.add("Single word naming (" + singleWordCount + " parameters)");
        }
        
        // Recommendations
        if (camelCaseCount > snakeCaseCount && snakeCaseCount > 0) {
            recommendations.add("Consider using consistent camelCase naming for all parameters");
        }
        
        // Potential issues
        if (camelCaseCount > 0 && snakeCaseCount > 0) {
            potentialIssues.add("Mixed naming conventions detected (camelCase and snake_case)");
        }
    }
    
    // Utility methods
    private String createEndpointKey(RestEndpointInfo endpoint) {
        return endpoint.getControllerClassName() + "." + endpoint.getMethodName();
    }
    
    private String extractStringAttribute(Map<String, Object> attributes, String... keys) {
        for (String key : keys) {
            Object value = attributes.get(key);
            if (value != null && !value.toString().trim().isEmpty()) {
                String str = value.toString().trim();
                // Remove quotes if present
                if (str.startsWith("\"") && str.endsWith("\"")) {
                    str = str.substring(1, str.length() - 1);
                }
                return str;
            }
        }
        return null;
    }
    
    private boolean extractBooleanAttribute(Map<String, Object> attributes, String key, boolean defaultValue) {
        Object value = attributes.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value != null) {
            return Boolean.parseBoolean(value.toString());
        }
        return defaultValue;
    }
    
    /**
     * Helper class for type analysis results.
     */
    private static class TypeAnalysis {
        boolean isCollection = false;
        boolean isMap = false;
        String collectionElementType = null;
        String mapKeyType = null;
        String mapValueType = null;
    }
}