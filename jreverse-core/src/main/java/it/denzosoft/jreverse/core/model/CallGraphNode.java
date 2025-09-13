package it.denzosoft.jreverse.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a node in a call graph, containing information about a method call
 * and its relationships to other calls in the execution flow.
 */
public class CallGraphNode {
    
    private final String className;
    private final String methodName;
    private final String methodSignature;
    private final CallType callType;
    private final int lineNumber;
    private final int depth;
    private final List<CallGraphNode> children;
    private final CallMetrics metrics;
    private final QueryInfo queryInfo;
    
    private CallGraphNode(Builder builder) {
        this.className = builder.className;
        this.methodName = builder.methodName;
        this.methodSignature = builder.methodSignature;
        this.callType = builder.callType;
        this.lineNumber = builder.lineNumber;
        this.depth = builder.depth;
        this.children = new ArrayList<>(builder.children);
        this.metrics = builder.metrics;
        this.queryInfo = builder.queryInfo;
    }
    
    // Constructor semplice per compatibilità
    public CallGraphNode(String className, String methodName, CallType callType, int depth) {
        this.className = className;
        this.methodName = methodName;
        this.callType = callType;
        this.depth = depth;
        this.methodSignature = null;
        this.lineNumber = 0;
        this.children = new ArrayList<>();
        this.metrics = null;
        this.queryInfo = null;
    }
    
    public String getClassName() {
        return className;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public String getMethodSignature() {
        return methodSignature;
    }
    
    public CallType getCallType() {
        return callType;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public int getDepth() {
        return depth;
    }
    
    public List<CallGraphNode> getChildren() {
        return Collections.unmodifiableList(children);
    }
    
    public CallMetrics getMetrics() {
        return metrics;
    }
    
    public QueryInfo getQueryInfo() {
        return queryInfo;
    }
    
    /**
     * Gets the simple class name (without package).
     */
    public String getSimpleClassName() {
        if (className == null) return null;
        int lastDot = className.lastIndexOf('.');
        return lastDot >= 0 ? className.substring(lastDot + 1) : className;
    }
    
    /**
     * Gets a formatted display name for this call.
     */
    public String getDisplayName() {
        return getSimpleClassName() + "." + methodName + "()";
    }
    
    /**
     * Gets the full method identifier.
     */
    public String getFullMethodId() {
        return className + "." + methodName;
    }
    
    /**
     * Checks if this node has any child calls.
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }
    
    /**
     * Adds a child node to this call graph node.
     */
    public void addChild(CallGraphNode child) {
        if (child != null) {
            this.children.add(child);
        }
    }
    
    /**
     * Gets the total number of children recursively.
     */
    public int getTotalChildCount() {
        int count = children.size();
        for (CallGraphNode child : children) {
            count += child.getTotalChildCount();
        }
        return count;
    }
    
    /**
     * Gets the maximum depth of this subtree.
     */
    public int getMaxDepth() {
        if (children.isEmpty()) {
            return depth;
        }
        
        int maxChildDepth = children.stream()
            .mapToInt(CallGraphNode::getMaxDepth)
            .max()
            .orElse(depth);
        
        return Math.max(depth, maxChildDepth);
    }
    
    /**
     * Finds a child node by method signature.
     */
    public CallGraphNode findChild(String methodSignature) {
        return children.stream()
            .filter(child -> methodSignature.equals(child.getMethodSignature()))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Checks if this call is a database access.
     */
    public boolean isDatabaseAccess() {
        return callType == CallType.DATABASE_ACCESS || callType == CallType.REPOSITORY_CALL;
    }
    
    /**
     * Checks if this call is a service layer call.
     */
    public boolean isServiceCall() {
        return callType == CallType.SERVICE_CALL || callType == CallType.BUSINESS_LOGIC;
    }
    
    /**
     * Checks if this call is external.
     */
    public boolean isExternalCall() {
        return callType == CallType.EXTERNAL_HTTP_CALL || callType == CallType.MESSAGE_PUBLISH;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String className;
        private String methodName;
        private String methodSignature;
        private CallType callType = CallType.BUSINESS_LOGIC;
        private int lineNumber = -1;
        private int depth = 0;
        private List<CallGraphNode> children = new ArrayList<>();
        private CallMetrics metrics;
        private QueryInfo queryInfo;
        
        public Builder className(String className) {
            this.className = className;
            return this;
        }
        
        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }
        
        public Builder methodSignature(String methodSignature) {
            this.methodSignature = methodSignature;
            return this;
        }
        
        public Builder callType(CallType callType) {
            this.callType = callType;
            return this;
        }
        
        public Builder lineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
            return this;
        }
        
        public Builder depth(int depth) {
            this.depth = depth;
            return this;
        }
        
        public Builder addChild(CallGraphNode child) {
            if (child != null) {
                this.children.add(child);
            }
            return this;
        }
        
        public Builder children(List<CallGraphNode> children) {
            this.children = new ArrayList<>(children != null ? children : Collections.emptyList());
            return this;
        }
        
        public Builder metrics(CallMetrics metrics) {
            this.metrics = metrics;
            return this;
        }
        
        public Builder queryInfo(QueryInfo queryInfo) {
            this.queryInfo = queryInfo;
            return this;
        }
        
        public CallGraphNode build() {
            Objects.requireNonNull(className, "Class name is required");
            Objects.requireNonNull(methodName, "Method name is required");
            
            if (methodSignature == null) {
                methodSignature = className + "." + methodName;
            }
            
            return new CallGraphNode(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("CallGraphNode{%s, type=%s, depth=%d, children=%d}",
            getDisplayName(), callType, depth, children.size());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CallGraphNode that = (CallGraphNode) obj;
        return Objects.equals(methodSignature, that.methodSignature) &&
               depth == that.depth;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(methodSignature, depth);
    }
}