package it.denzosoft.jreverse.core.model;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a complete call chain from HTTP endpoint to database/external services.
 */
public class CallGraphChain {
    
    private final String endpoint;
    private final String httpMethod;
    private final String controllerClass;
    private final String controllerMethod;
    private final CallGraphNode rootNode;
    private final int maxDepth;
    private final int totalCalls;
    private final List<CallGraphIssue> issues;
    private final CallChainMetrics metrics;
    
    private CallGraphChain(Builder builder) {
        this.endpoint = builder.endpoint;
        this.httpMethod = builder.httpMethod;
        this.controllerClass = builder.controllerClass;
        this.controllerMethod = builder.controllerMethod;
        this.rootNode = builder.rootNode;
        this.maxDepth = builder.maxDepth;
        this.totalCalls = builder.totalCalls;
        this.issues = List.copyOf(builder.issues);
        this.metrics = builder.metrics;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public String getHttpMethod() {
        return httpMethod;
    }
    
    public String getControllerClass() {
        return controllerClass;
    }
    
    public String getControllerMethod() {
        return controllerMethod;
    }
    
    public CallGraphNode getRootNode() {
        return rootNode;
    }
    
    public int getMaxDepth() {
        return maxDepth;
    }
    
    public int getTotalCalls() {
        return totalCalls;
    }
    
    public List<CallGraphIssue> getIssues() {
        return issues;
    }
    
    public CallChainMetrics getMetrics() {
        return metrics;
    }
    
    /**
     * Gets the endpoint signature (HTTP method + path).
     */
    public String getEndpointSignature() {
        return httpMethod + " " + endpoint;
    }
    
    /**
     * Gets the simple controller name (without package).
     */
    public String getSimpleControllerName() {
        if (controllerClass == null) return null;
        int lastDot = controllerClass.lastIndexOf('.');
        return lastDot >= 0 ? controllerClass.substring(lastDot + 1) : controllerClass;
    }
    
    /**
     * Gets all unique components involved in this call chain.
     */
    public Set<String> getInvolvedComponents() {
        return collectComponents(rootNode);
    }
    
    /**
     * Checks if this call chain involves a specific component.
     */
    public boolean involvesComponent(String componentName) {
        return getInvolvedComponents().contains(componentName);
    }
    
    /**
     * Gets all database calls in this chain.
     */
    public List<CallGraphNode> getDatabaseCalls() {
        return collectCallsByType(rootNode, CallType.DATABASE_ACCESS, CallType.REPOSITORY_CALL);
    }
    
    /**
     * Gets all external calls in this chain.
     */
    public List<CallGraphNode> getExternalCalls() {
        return collectCallsByType(rootNode, CallType.EXTERNAL_HTTP_CALL, CallType.MESSAGE_PUBLISH);
    }
    
    /**
     * Gets all service calls in this chain.
     */
    public List<CallGraphNode> getServiceCalls() {
        return collectCallsByType(rootNode, CallType.SERVICE_CALL, CallType.BUSINESS_LOGIC);
    }
    
    /**
     * Checks if this chain has performance issues.
     */
    public boolean hasPerformanceIssues() {
        return issues.stream()
            .anyMatch(issue -> issue.getType().isPerformanceRelated()) ||
               metrics.hasPerformanceRisk();
    }
    
    /**
     * Checks if this chain has architectural violations.
     */
    public boolean hasArchitecturalViolations() {
        return issues.stream()
            .anyMatch(issue -> issue.getType().isArchitecturalViolation());
    }
    
    /**
     * Gets the complexity level of this call chain.
     */
    public ComplexityLevel getComplexityLevel() {
        if (maxDepth <= 3 && totalCalls <= 5) {
            return ComplexityLevel.SIMPLE;
        } else if (maxDepth <= 5 && totalCalls <= 15) {
            return ComplexityLevel.MODERATE;
        } else if (maxDepth <= 8 && totalCalls <= 30) {
            return ComplexityLevel.COMPLEX;
        } else {
            return ComplexityLevel.VERY_COMPLEX;
        }
    }
    
    private Set<String> collectComponents(CallGraphNode node) {
        Set<String> components = node.getChildren().stream()
            .map(CallGraphNode::getClassName)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        // Add current node
        if (node.getClassName() != null) {
            components.add(node.getClassName());
        }
        
        // Recursively collect from children
        for (CallGraphNode child : node.getChildren()) {
            components.addAll(collectComponents(child));
        }
        
        return components;
    }
    
    private List<CallGraphNode> collectCallsByType(CallGraphNode node, CallType... types) {
        List<CallGraphNode> calls = new java.util.ArrayList<>();
        
        // Check current node
        for (CallType type : types) {
            if (node.getCallType() == type) {
                calls.add(node);
                break;
            }
        }
        
        // Recursively collect from children
        for (CallGraphNode child : node.getChildren()) {
            calls.addAll(collectCallsByType(child, types));
        }
        
        return calls;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String endpoint;
        private String httpMethod;
        private String controllerClass;
        private String controllerMethod;
        private CallGraphNode rootNode;
        private int maxDepth;
        private int totalCalls;
        private List<CallGraphIssue> issues = List.of();
        private CallChainMetrics metrics;
        
        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }
        
        public Builder httpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }
        
        public Builder controllerClass(String controllerClass) {
            this.controllerClass = controllerClass;
            return this;
        }
        
        public Builder controllerMethod(String controllerMethod) {
            this.controllerMethod = controllerMethod;
            return this;
        }
        
        public Builder rootNode(CallGraphNode rootNode) {
            this.rootNode = rootNode;
            return this;
        }
        
        public Builder maxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }
        
        public Builder totalCalls(int totalCalls) {
            this.totalCalls = totalCalls;
            return this;
        }
        
        public Builder issues(List<CallGraphIssue> issues) {
            this.issues = issues != null ? issues : List.of();
            return this;
        }
        
        public Builder metrics(CallChainMetrics metrics) {
            this.metrics = metrics;
            return this;
        }
        
        public CallGraphChain build() {
            Objects.requireNonNull(endpoint, "Endpoint is required");
            Objects.requireNonNull(rootNode, "Root node is required");
            
            return new CallGraphChain(this);
        }
    }
    
    public enum ComplexityLevel {
        SIMPLE("Simple", "Basic endpoint with minimal dependencies"),
        MODERATE("Moderate", "Standard endpoint with typical service calls"),
        COMPLEX("Complex", "Complex endpoint with multiple layers"),
        VERY_COMPLEX("Very Complex", "Highly complex endpoint with deep call chains");
        
        private final String displayName;
        private final String description;
        
        ComplexityLevel(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @Override
    public String toString() {
        return String.format("CallGraphChain{%s, depth=%d, calls=%d, issues=%d}",
            getEndpointSignature(), maxDepth, totalCalls, issues.size());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CallGraphChain that = (CallGraphChain) obj;
        return Objects.equals(endpoint, that.endpoint) &&
               Objects.equals(httpMethod, that.httpMethod) &&
               Objects.equals(controllerMethod, that.controllerMethod);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(endpoint, httpMethod, controllerMethod);
    }
}