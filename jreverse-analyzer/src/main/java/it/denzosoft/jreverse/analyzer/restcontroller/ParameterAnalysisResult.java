package it.denzosoft.jreverse.analyzer.restcontroller;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Immutable result container for parameter analysis of REST endpoints.
 * Contains comprehensive statistics and detailed information about all parameters
 * found across analyzed REST endpoints.
 */
public class ParameterAnalysisResult {
    
    private final List<EndpointParameterInfo> parameters;
    private final Map<String, List<EndpointParameterInfo>> parametersByEndpoint;
    private final Map<ParameterType, List<EndpointParameterInfo>> parametersByType;
    private final List<String> validationErrors;
    private final List<String> warnings;
    private final ParameterStatistics statistics;
    private final boolean analysisSuccessful;
    
    private ParameterAnalysisResult(Builder builder) {
        this.parameters = Collections.unmodifiableList(new ArrayList<>(builder.parameters));
        this.parametersByEndpoint = Collections.unmodifiableMap(builder.parametersByEndpoint.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> Collections.unmodifiableList(new ArrayList<>(e.getValue()))
            )));
        this.parametersByType = Collections.unmodifiableMap(builder.parametersByType.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> Collections.unmodifiableList(new ArrayList<>(e.getValue()))
            )));
        this.validationErrors = Collections.unmodifiableList(new ArrayList<>(builder.validationErrors));
        this.warnings = Collections.unmodifiableList(new ArrayList<>(builder.warnings));
        this.statistics = calculateStatistics();
        this.analysisSuccessful = validationErrors.isEmpty();
    }
    
    private ParameterStatistics calculateStatistics() {
        int totalParameters = parameters.size();
        int requiredParameters = (int) parameters.stream().filter(EndpointParameterInfo::isRequired).count();
        int optionalParameters = totalParameters - requiredParameters;
        int validatedParameters = (int) parameters.stream().filter(EndpointParameterInfo::hasValidation).count();
        int complexTypeParameters = (int) parameters.stream().filter(EndpointParameterInfo::isComplexType).count();
        int pathVariableParameters = (int) parameters.stream().filter(p -> p.getParameterType() == ParameterType.PATH_VARIABLE).count();
        int requestBodyParameters = (int) parameters.stream().filter(p -> p.getParameterType() == ParameterType.REQUEST_BODY).count();
        int implicitParameters = (int) parameters.stream().filter(EndpointParameterInfo::isImplicitlyBound).count();
        
        Map<ParameterType, Long> typeDistribution = parameters.stream()
            .collect(Collectors.groupingBy(EndpointParameterInfo::getParameterType, Collectors.counting()));
        
        Map<String, Long> validationTypeDistribution = parameters.stream()
            .filter(EndpointParameterInfo::hasValidation)
            .flatMap(p -> p.getValidationAnnotations().stream())
            .collect(Collectors.groupingBy(
                annotation -> {
                    int lastDotIndex = annotation.lastIndexOf('.');
                    return lastDotIndex >= 0 ? annotation.substring(lastDotIndex + 1) : annotation;
                },
                Collectors.counting()
            ));
        
        Set<String> uniqueParameterNames = parameters.stream()
            .map(EndpointParameterInfo::getEffectiveName)
            .collect(Collectors.toSet());
        
        return new ParameterStatistics(
            totalParameters,
            requiredParameters,
            optionalParameters,
            validatedParameters,
            complexTypeParameters,
            pathVariableParameters,
            requestBodyParameters,
            implicitParameters,
            typeDistribution,
            validationTypeDistribution,
            uniqueParameterNames.size(),
            parametersByEndpoint.size()
        );
    }
    
    // Getters
    public List<EndpointParameterInfo> getParameters() { return parameters; }
    public Map<String, List<EndpointParameterInfo>> getParametersByEndpoint() { return parametersByEndpoint; }
    public Map<ParameterType, List<EndpointParameterInfo>> getParametersByType() { return parametersByType; }
    public List<String> getValidationErrors() { return validationErrors; }
    public List<String> getWarnings() { return warnings; }
    public ParameterStatistics getStatistics() { return statistics; }
    public boolean isAnalysisSuccessful() { return analysisSuccessful; }
    
    /**
     * Gets parameters for a specific endpoint.
     * 
     * @param endpointKey the endpoint key (typically controller.method)
     * @return list of parameters for the endpoint
     */
    public List<EndpointParameterInfo> getParametersForEndpoint(String endpointKey) {
        return parametersByEndpoint.getOrDefault(endpointKey, Collections.emptyList());
    }
    
    /**
     * Gets parameters of a specific type.
     * 
     * @param parameterType the parameter type to filter by
     * @return list of parameters of the specified type
     */
    public List<EndpointParameterInfo> getParametersOfType(ParameterType parameterType) {
        return parametersByType.getOrDefault(parameterType, Collections.emptyList());
    }
    
    /**
     * Gets all validation errors related to parameter analysis.
     * 
     * @return list of validation error messages
     */
    public List<String> getAllValidationErrors() {
        List<String> allErrors = new ArrayList<>(validationErrors);
        
        // Add parameter-specific validation errors
        for (EndpointParameterInfo param : parameters) {
            if (param.requiresPathVariableValidation() && !param.isValidPathVariable()) {
                allErrors.add("Path variable parameter '" + param.getEffectiveName() + 
                             "' in " + param.getParameterInfo().getName() + 
                             " does not match any path variables");
            }
        }
        
        return allErrors;
    }
    
    /**
     * Gets parameters that have potential issues or inconsistencies.
     * 
     * @return list of parameters with issues
     */
    public List<EndpointParameterInfo> getProblematicParameters() {
        return parameters.stream()
            .filter(param -> 
                !param.isValidPathVariable() ||
                (param.getParameterType() == ParameterType.REQUEST_BODY && param.isRequired() && param.hasDefaultValue()) ||
                (param.getParameterType() == ParameterType.UNKNOWN && !param.isImplicitlyBound())
            )
            .collect(Collectors.toList());
    }
    
    /**
     * Gets parameters that use validation annotations.
     * 
     * @return list of validated parameters
     */
    public List<EndpointParameterInfo> getValidatedParameters() {
        return parameters.stream()
            .filter(EndpointParameterInfo::hasValidation)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets parameters that require request content (body or parts).
     * 
     * @return list of content-bound parameters
     */
    public List<EndpointParameterInfo> getContentParameters() {
        return parameters.stream()
            .filter(EndpointParameterInfo::isContentBound)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets endpoints that have multiple request body parameters (potential issue).
     * 
     * @return map of endpoint keys to their request body parameters
     */
    public Map<String, List<EndpointParameterInfo>> getEndpointsWithMultipleRequestBodies() {
        return parametersByEndpoint.entrySet().stream()
            .filter(entry -> entry.getValue().stream()
                .filter(p -> p.getParameterType() == ParameterType.REQUEST_BODY)
                .count() > 1)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream()
                    .filter(p -> p.getParameterType() == ParameterType.REQUEST_BODY)
                    .collect(Collectors.toList())
            ));
    }
    
    /**
     * Checks if the analysis found any parameters.
     * 
     * @return true if parameters were found
     */
    public boolean hasParameters() {
        return !parameters.isEmpty();
    }
    
    /**
     * Checks if any validation errors were found.
     * 
     * @return true if validation errors exist
     */
    public boolean hasValidationErrors() {
        return !validationErrors.isEmpty();
    }
    
    /**
     * Checks if any warnings were generated.
     * 
     * @return true if warnings exist
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    @Override
    public String toString() {
        return "ParameterAnalysisResult{" +
                "totalParameters=" + parameters.size() +
                ", endpointCount=" + parametersByEndpoint.size() +
                ", validationErrors=" + validationErrors.size() +
                ", warnings=" + warnings.size() +
                ", successful=" + analysisSuccessful +
                '}';
    }
    
    /**
     * Statistics container for parameter analysis results.
     */
    public static class ParameterStatistics {
        private final int totalParameters;
        private final int requiredParameters;
        private final int optionalParameters;
        private final int validatedParameters;
        private final int complexTypeParameters;
        private final int pathVariableParameters;
        private final int requestBodyParameters;
        private final int implicitParameters;
        private final Map<ParameterType, Long> typeDistribution;
        private final Map<String, Long> validationTypeDistribution;
        private final int uniqueParameterNames;
        private final int analyzedEndpoints;
        
        public ParameterStatistics(int totalParameters, int requiredParameters, int optionalParameters,
                                 int validatedParameters, int complexTypeParameters, int pathVariableParameters,
                                 int requestBodyParameters, int implicitParameters,
                                 Map<ParameterType, Long> typeDistribution,
                                 Map<String, Long> validationTypeDistribution,
                                 int uniqueParameterNames, int analyzedEndpoints) {
            this.totalParameters = totalParameters;
            this.requiredParameters = requiredParameters;
            this.optionalParameters = optionalParameters;
            this.validatedParameters = validatedParameters;
            this.complexTypeParameters = complexTypeParameters;
            this.pathVariableParameters = pathVariableParameters;
            this.requestBodyParameters = requestBodyParameters;
            this.implicitParameters = implicitParameters;
            this.typeDistribution = Collections.unmodifiableMap(new LinkedHashMap<>(typeDistribution));
            this.validationTypeDistribution = Collections.unmodifiableMap(new LinkedHashMap<>(validationTypeDistribution));
            this.uniqueParameterNames = uniqueParameterNames;
            this.analyzedEndpoints = analyzedEndpoints;
        }
        
        // Getters
        public int getTotalParameters() { return totalParameters; }
        public int getRequiredParameters() { return requiredParameters; }
        public int getOptionalParameters() { return optionalParameters; }
        public int getValidatedParameters() { return validatedParameters; }
        public int getComplexTypeParameters() { return complexTypeParameters; }
        public int getPathVariableParameters() { return pathVariableParameters; }
        public int getRequestBodyParameters() { return requestBodyParameters; }
        public int getImplicitParameters() { return implicitParameters; }
        public Map<ParameterType, Long> getTypeDistribution() { return typeDistribution; }
        public Map<String, Long> getValidationTypeDistribution() { return validationTypeDistribution; }
        public int getUniqueParameterNames() { return uniqueParameterNames; }
        public int getAnalyzedEndpoints() { return analyzedEndpoints; }
        
        // Calculated percentages
        public double getRequiredPercentage() {
            return totalParameters > 0 ? (requiredParameters * 100.0) / totalParameters : 0.0;
        }
        
        public double getValidatedPercentage() {
            return totalParameters > 0 ? (validatedParameters * 100.0) / totalParameters : 0.0;
        }
        
        public double getComplexTypePercentage() {
            return totalParameters > 0 ? (complexTypeParameters * 100.0) / totalParameters : 0.0;
        }
        
        public double getAverageParametersPerEndpoint() {
            return analyzedEndpoints > 0 ? (double) totalParameters / analyzedEndpoints : 0.0;
        }
        
        @Override
        public String toString() {
            return "ParameterStatistics{" +
                    "total=" + totalParameters +
                    ", required=" + requiredParameters +
                    ", validated=" + validatedParameters +
                    ", complex=" + complexTypeParameters +
                    ", endpoints=" + analyzedEndpoints +
                    '}';
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private List<EndpointParameterInfo> parameters = new ArrayList<>();
        private Map<String, List<EndpointParameterInfo>> parametersByEndpoint = new LinkedHashMap<>();
        private Map<ParameterType, List<EndpointParameterInfo>> parametersByType = new LinkedHashMap<>();
        private List<String> validationErrors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        
        public Builder addParameter(EndpointParameterInfo parameter) {
            if (parameter != null) {
                this.parameters.add(parameter);
                
                // Group by type
                parametersByType.computeIfAbsent(parameter.getParameterType(), k -> new ArrayList<>())
                    .add(parameter);
            }
            return this;
        }
        
        public Builder parameters(List<EndpointParameterInfo> parameters) {
            this.parameters = new ArrayList<>(parameters != null ? parameters : Collections.emptyList());
            
            // Rebuild type grouping
            this.parametersByType.clear();
            for (EndpointParameterInfo param : this.parameters) {
                parametersByType.computeIfAbsent(param.getParameterType(), k -> new ArrayList<>())
                    .add(param);
            }
            return this;
        }
        
        public Builder addEndpointParameters(String endpointKey, List<EndpointParameterInfo> endpointParameters) {
            if (endpointKey != null && endpointParameters != null) {
                parametersByEndpoint.put(endpointKey, new ArrayList<>(endpointParameters));
                
                // Add to main parameters list if not already present
                for (EndpointParameterInfo param : endpointParameters) {
                    if (!parameters.contains(param)) {
                        addParameter(param);
                    }
                }
            }
            return this;
        }
        
        public Builder parametersByEndpoint(Map<String, List<EndpointParameterInfo>> parametersByEndpoint) {
            this.parametersByEndpoint = new LinkedHashMap<>();
            if (parametersByEndpoint != null) {
                for (Map.Entry<String, List<EndpointParameterInfo>> entry : parametersByEndpoint.entrySet()) {
                    this.parametersByEndpoint.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                    
                    // Add to main parameters list
                    for (EndpointParameterInfo param : entry.getValue()) {
                        if (!parameters.contains(param)) {
                            addParameter(param);
                        }
                    }
                }
            }
            return this;
        }
        
        public Builder addValidationError(String error) {
            if (error != null && !error.trim().isEmpty()) {
                this.validationErrors.add(error.trim());
            }
            return this;
        }
        
        public Builder validationErrors(List<String> validationErrors) {
            this.validationErrors = new ArrayList<>(validationErrors != null ? validationErrors : Collections.emptyList());
            return this;
        }
        
        public Builder addWarning(String warning) {
            if (warning != null && !warning.trim().isEmpty()) {
                this.warnings.add(warning.trim());
            }
            return this;
        }
        
        public Builder warnings(List<String> warnings) {
            this.warnings = new ArrayList<>(warnings != null ? warnings : Collections.emptyList());
            return this;
        }
        
        public ParameterAnalysisResult build() {
            return new ParameterAnalysisResult(this);
        }
    }
}