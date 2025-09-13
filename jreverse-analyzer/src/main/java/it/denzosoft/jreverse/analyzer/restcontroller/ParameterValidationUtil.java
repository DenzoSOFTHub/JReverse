package it.denzosoft.jreverse.analyzer.restcontroller;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class for validating REST endpoint parameter configurations.
 * Provides comprehensive validation rules for Spring MVC parameter binding
 * and consistency checking across endpoints.
 */
public final class ParameterValidationUtil {
    
    // Path variable pattern for extracting variable names from paths
    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)\\}");
    
    // Valid parameter name pattern (alphanumeric and underscore)
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$");
    
    // Servlet API types that should not have binding annotations
    private static final Set<String> SERVLET_API_TYPES = Set.of(
        "javax.servlet.http.HttpServletRequest",
        "javax.servlet.http.HttpServletResponse",
        "javax.servlet.ServletRequest", 
        "javax.servlet.ServletResponse",
        "javax.servlet.http.HttpSession",
        "javax.servlet.ServletContext",
        "org.springframework.ui.Model",
        "org.springframework.ui.ModelMap",
        "org.springframework.validation.BindingResult",
        "org.springframework.web.servlet.mvc.support.RedirectAttributes"
    );
    
    private ParameterValidationUtil() {
        // Utility class
    }
    
    /**
     * Validates a single endpoint's parameter configuration.
     * 
     * @param endpoint the endpoint to validate
     * @param parameters the parameters to validate
     * @return list of validation errors
     */
    public static List<String> validateEndpointParameters(RestEndpointInfo endpoint, 
                                                         List<EndpointParameterInfo> parameters) {
        List<String> errors = new ArrayList<>();
        
        validateRequestBodyParameters(endpoint, parameters, errors);
        validatePathVariableParameters(endpoint, parameters, errors);
        validateParameterNaming(parameters, errors);
        validateParameterTypeConsistency(parameters, errors);
        validateServletApiParameters(parameters, errors);
        validateValidationAnnotations(parameters, errors);
        
        return errors;
    }
    
    /**
     * Validates request body parameter configuration.
     */
    private static void validateRequestBodyParameters(RestEndpointInfo endpoint,
                                                    List<EndpointParameterInfo> parameters,
                                                    List<String> errors) {
        List<EndpointParameterInfo> requestBodyParams = parameters.stream()
            .filter(p -> p.getParameterType() == ParameterType.REQUEST_BODY)
            .collect(Collectors.toList());
        
        if (requestBodyParams.size() > 1) {
            errors.add(String.format("Endpoint %s.%s has %d @RequestBody parameters, only one is allowed",
                endpoint.getControllerClassName(), endpoint.getMethodName(), requestBodyParams.size()));
        }
        
        // Check for request body with GET method
        if (!requestBodyParams.isEmpty() && 
            endpoint.getPrimaryHttpMethod().isPresent() && 
            endpoint.getPrimaryHttpMethod().get() == HttpMethod.GET) {
            errors.add(String.format("Endpoint %s.%s uses GET method with @RequestBody parameter, which is not recommended",
                endpoint.getControllerClassName(), endpoint.getMethodName()));
        }
        
        // Check for required request body with default value
        for (EndpointParameterInfo param : requestBodyParams) {
            if (param.isRequired() && param.hasDefaultValue()) {
                errors.add(String.format("@RequestBody parameter '%s' is marked as required but has a default value",
                    param.getEffectiveName()));
            }
        }
    }
    
    /**
     * Validates path variable parameter configuration.
     */
    private static void validatePathVariableParameters(RestEndpointInfo endpoint,
                                                     List<EndpointParameterInfo> parameters,
                                                     List<String> errors) {
        Set<String> pathVariables = new HashSet<>(endpoint.getPathVariables());
        
        List<EndpointParameterInfo> pathVarParams = parameters.stream()
            .filter(p -> p.getParameterType() == ParameterType.PATH_VARIABLE)
            .collect(Collectors.toList());
        
        // Check if all path variables have corresponding parameters
        Set<String> declaredPathVars = pathVarParams.stream()
            .map(EndpointParameterInfo::getEffectiveName)
            .collect(Collectors.toSet());
        
        for (String pathVar : pathVariables) {
            if (!declaredPathVars.contains(pathVar)) {
                errors.add(String.format("Path variable '%s' in path '%s' has no corresponding @PathVariable parameter",
                    pathVar, endpoint.getCombinedPath()));
            }
        }
        
        // Check if all path variable parameters have corresponding path variables
        for (EndpointParameterInfo param : pathVarParams) {
            String paramName = param.getEffectiveName();
            if (!pathVariables.contains(paramName)) {
                errors.add(String.format("@PathVariable parameter '%s' does not match any path variable in '%s'",
                    paramName, endpoint.getCombinedPath()));
            }
        }
    }
    
    /**
     * Validates parameter naming conventions.
     */
    private static void validateParameterNaming(List<EndpointParameterInfo> parameters, 
                                               List<String> errors) {
        for (EndpointParameterInfo param : parameters) {
            String effectiveName = param.getEffectiveName();
            
            if (effectiveName != null && !VALID_NAME_PATTERN.matcher(effectiveName).matches()) {
                errors.add(String.format("Parameter name '%s' does not follow valid naming convention",
                    effectiveName));
            }
            
            // Check for reserved parameter names that might conflict
            if (isReservedParameterName(effectiveName)) {
                errors.add(String.format("Parameter name '%s' conflicts with reserved keyword or common framework parameter",
                    effectiveName));
            }
        }
    }
    
    /**
     * Validates parameter type consistency.
     */
    private static void validateParameterTypeConsistency(List<EndpointParameterInfo> parameters,
                                                        List<String> errors) {
        // Check for parameters that should be collections but aren't marked as such
        for (EndpointParameterInfo param : parameters) {
            String type = param.getType();
            
            if (type.startsWith("java.util.List") || type.startsWith("java.util.Set") ||
                type.startsWith("java.util.Collection")) {
                if (!param.isCollection()) {
                    errors.add(String.format("Parameter '%s' appears to be a collection type but is not marked as collection",
                        param.getEffectiveName()));
                }
            }
            
            if (type.startsWith("java.util.Map")) {
                if (!param.isMap()) {
                    errors.add(String.format("Parameter '%s' appears to be a Map type but is not marked as map",
                        param.getEffectiveName()));
                }
            }
        }
    }
    
    /**
     * Validates servlet API parameter configuration.
     */
    private static void validateServletApiParameters(List<EndpointParameterInfo> parameters,
                                                    List<String> errors) {
        for (EndpointParameterInfo param : parameters) {
            String type = param.getType();
            
            if (SERVLET_API_TYPES.contains(type)) {
                if (param.getParameterType() != ParameterType.SERVLET_API && 
                    param.getParameterType() != ParameterType.IMPLICIT) {
                    errors.add(String.format("Servlet API parameter '%s' of type '%s' should not have binding annotations",
                        param.getEffectiveName(), type));
                }
            }
        }
    }
    
    /**
     * Validates validation annotation usage.
     */
    private static void validateValidationAnnotations(List<EndpointParameterInfo> parameters,
                                                     List<String> errors) {
        for (EndpointParameterInfo param : parameters) {
            if (param.hasValidation()) {
                // Check if validation is used appropriately
                if (param.getParameterType() == ParameterType.SERVLET_API ||
                    param.getParameterType() == ParameterType.PRINCIPAL) {
                    errors.add(String.format("Validation annotations on servlet API parameter '%s' are not meaningful",
                        param.getEffectiveName()));
                }
                
                // Check for conflicting validation annotations
                Set<String> validationAnnotations = param.getValidationAnnotations();
                if (validationAnnotations.contains("javax.validation.constraints.NotNull") &&
                    validationAnnotations.contains("javax.validation.constraints.NotEmpty")) {
                    errors.add(String.format("Parameter '%s' has both @NotNull and @NotEmpty annotations, @NotEmpty implies @NotNull",
                        param.getEffectiveName()));
                }
            }
        }
    }
    
    /**
     * Validates parameter consistency across multiple endpoints.
     * 
     * @param endpoints the endpoints to validate
     * @param parametersByEndpoint map of endpoint parameters
     * @return list of validation errors
     */
    public static List<String> validateCrossEndpointConsistency(List<RestEndpointInfo> endpoints,
                                                               Map<String, List<EndpointParameterInfo>> parametersByEndpoint) {
        List<String> errors = new ArrayList<>();
        
        Map<String, Set<ParameterInfo>> parameterNameToInfos = new HashMap<>();
        Map<String, Set<String>> parameterNameToTypes = new HashMap<>();
        
        // Collect all parameter information
        for (Map.Entry<String, List<EndpointParameterInfo>> entry : parametersByEndpoint.entrySet()) {
            for (EndpointParameterInfo param : entry.getValue()) {
                String name = param.getEffectiveName();
                
                parameterNameToInfos.computeIfAbsent(name, k -> new HashSet<>())
                    .add(new ParameterInfo(param.getParameterType(), param.getType()));
                
                parameterNameToTypes.computeIfAbsent(name, k -> new HashSet<>())
                    .add(param.getType());
            }
        }
        
        // Check for parameter names used with different types
        for (Map.Entry<String, Set<String>> entry : parameterNameToTypes.entrySet()) {
            String paramName = entry.getKey();
            Set<String> types = entry.getValue();
            
            if (types.size() > 1) {
                errors.add(String.format("Parameter name '%s' is used with different types across endpoints: %s",
                    paramName, String.join(", ", types)));
            }
        }
        
        // Check for parameter names used with different binding types
        for (Map.Entry<String, Set<ParameterInfo>> entry : parameterNameToInfos.entrySet()) {
            String paramName = entry.getKey();
            Set<ParameterType> bindingTypes = entry.getValue().stream()
                .map(info -> info.parameterType)
                .collect(Collectors.toSet());
            
            if (bindingTypes.size() > 1) {
                errors.add(String.format("Parameter name '%s' is used with different binding types across endpoints: %s",
                    paramName, bindingTypes.stream().map(ParameterType::getDisplayName).collect(Collectors.joining(", "))));
            }
        }
        
        return errors;
    }
    
    /**
     * Validates parameter naming patterns across all endpoints.
     * 
     * @param parameters all parameters from all endpoints
     * @return list of warnings about naming patterns
     */
    public static List<String> validateNamingPatterns(List<EndpointParameterInfo> parameters) {
        List<String> warnings = new ArrayList<>();
        
        Map<String, Integer> namingStyles = new HashMap<>();
        
        for (EndpointParameterInfo param : parameters) {
            String name = param.getEffectiveName();
            if (name != null) {
                if (name.contains("_")) {
                    namingStyles.merge("snake_case", 1, Integer::sum);
                } else if (name.matches(".*[a-z][A-Z].*")) {
                    namingStyles.merge("camelCase", 1, Integer::sum);
                } else {
                    namingStyles.merge("lowercase", 1, Integer::sum);
                }
            }
        }
        
        if (namingStyles.size() > 1) {
            warnings.add("Mixed parameter naming conventions detected: " + 
                        namingStyles.entrySet().stream()
                            .map(e -> e.getKey() + " (" + e.getValue() + " parameters)")
                            .collect(Collectors.joining(", ")));
        }
        
        return warnings;
    }
    
    /**
     * Checks if a parameter name is reserved or might cause conflicts.
     */
    private static boolean isReservedParameterName(String name) {
        if (name == null) return false;
        
        // Java keywords
        Set<String> javaKeywords = Set.of("class", "interface", "enum", "package", "import",
            "public", "private", "protected", "static", "final", "abstract", "synchronized",
            "volatile", "transient", "native", "strictfp", "if", "else", "while", "for",
            "do", "break", "continue", "return", "switch", "case", "default", "try", "catch",
            "finally", "throw", "throws", "new", "instanceof", "super", "this", "null",
            "true", "false", "void", "boolean", "byte", "char", "short", "int", "long",
            "float", "double");
        
        // Common framework parameter names that might conflict
        Set<String> frameworkNames = Set.of("request", "response", "session", "model",
            "errors", "bindingResult", "locale", "principal", "authentication");
        
        return javaKeywords.contains(name.toLowerCase()) || frameworkNames.contains(name.toLowerCase());
    }
    
    /**
     * Helper class to store parameter information for cross-validation.
     */
    private static class ParameterInfo {
        final ParameterType parameterType;
        final String type;
        
        ParameterInfo(ParameterType parameterType, String type) {
            this.parameterType = parameterType;
            this.type = type;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ParameterInfo that = (ParameterInfo) obj;
            return parameterType == that.parameterType && Objects.equals(type, that.type);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(parameterType, type);
        }
    }
}