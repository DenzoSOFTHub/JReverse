package it.denzosoft.jreverse.analyzer.messaging;

import it.denzosoft.jreverse.core.model.AnnotationInfo;

import java.util.*;

/**
 * Immutable value object representing detailed information about a messaging entrypoint.
 * Contains metadata about message consumers, producers, and routing configurations.
 */
public class MessagingEntrypointInfo {
    
    private final String methodName;
    private final String declaringClass;
    private final MessagingEntrypointType messagingType;
    private final String destination;
    private final String topic;
    private final String queue;
    private final String exchange;
    private final String routingKey;
    private final String containerFactory;
    private final String errorHandler;
    private final boolean isVoid;
    private final boolean isAsync;
    private final boolean hasTransaction;
    private final Set<String> messageTypes;
    private final Set<String> headers;
    private final AnnotationInfo sourceAnnotation;
    private final Map<String, Object> metadata;
    
    private MessagingEntrypointInfo(Builder builder) {
        this.methodName = Objects.requireNonNull(builder.methodName, "methodName cannot be null");
        this.declaringClass = Objects.requireNonNull(builder.declaringClass, "declaringClass cannot be null");
        this.messagingType = Objects.requireNonNull(builder.messagingType, "messagingType cannot be null");
        this.destination = builder.destination;
        this.topic = builder.topic;
        this.queue = builder.queue;
        this.exchange = builder.exchange;
        this.routingKey = builder.routingKey;
        this.containerFactory = builder.containerFactory;
        this.errorHandler = builder.errorHandler;
        this.isVoid = builder.isVoid;
        this.isAsync = builder.isAsync;
        this.hasTransaction = builder.hasTransaction;
        this.messageTypes = Collections.unmodifiableSet(new LinkedHashSet<>(builder.messageTypes));
        this.headers = Collections.unmodifiableSet(new LinkedHashSet<>(builder.headers));
        this.sourceAnnotation = builder.sourceAnnotation;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(builder.metadata));
    }
    
    // Getters
    public String getMethodName() { return methodName; }
    public String getDeclaringClass() { return declaringClass; }
    public MessagingEntrypointType getMessagingType() { return messagingType; }
    public String getDestination() { return destination; }
    public String getTopic() { return topic; }
    public String getQueue() { return queue; }
    public String getExchange() { return exchange; }
    public String getRoutingKey() { return routingKey; }
    public String getContainerFactory() { return containerFactory; }
    public String getErrorHandler() { return errorHandler; }
    public boolean isVoid() { return isVoid; }
    public boolean isAsync() { return isAsync; }
    public boolean hasTransaction() { return hasTransaction; }
    public Set<String> getMessageTypes() { return messageTypes; }
    public Set<String> getHeaders() { return headers; }
    public AnnotationInfo getSourceAnnotation() { return sourceAnnotation; }
    public Map<String, Object> getMetadata() { return metadata; }
    
    /**
     * Gets the fully qualified method identifier.
     */
    public String getMethodIdentifier() {
        return declaringClass + "." + methodName;
    }
    
    /**
     * Gets the primary destination (topic, queue, or destination).
     */
    public String getPrimaryDestination() {
        if (destination != null && !destination.trim().isEmpty()) {
            return destination;
        }
        if (topic != null && !topic.trim().isEmpty()) {
            return topic;
        }
        if (queue != null && !queue.trim().isEmpty()) {
            return queue;
        }
        return "unknown";
    }
    
    /**
     * Determines if this has proper error handling.
     */
    public boolean hasProperErrorHandling() {
        return errorHandler != null && !errorHandler.trim().isEmpty();
    }
    
    /**
     * Determines if this uses custom container factory.
     */
    public boolean hasCustomContainerFactory() {
        return containerFactory != null && !containerFactory.trim().isEmpty() && 
               !"default".equals(containerFactory);
    }
    
    /**
     * Gets the messaging technology category.
     */
    public String getCategory() {
        return messagingType.getCategory();
    }
    
    /**
     * Gets a complexity score for this messaging operation (0-100).
     */
    public int getComplexityScore() {
        int score = 10; // Base score
        
        // Add complexity for multiple destinations
        int destinations = 0;
        if (destination != null) destinations++;
        if (topic != null) destinations++;
        if (queue != null) destinations++;
        score += destinations * 15;
        
        // Add complexity for routing configurations
        if (exchange != null && !exchange.trim().isEmpty()) score += 20;
        if (routingKey != null && !routingKey.trim().isEmpty()) score += 15;
        
        // Add complexity for custom configurations
        if (hasCustomContainerFactory()) score += 10;
        if (hasTransaction) score += 15;
        
        // Add complexity for multiple message types
        score += messageTypes.size() * 5;
        
        // Add complexity for header processing
        score += headers.size() * 3;
        
        return Math.min(100, score);
    }
    
    /**
     * Gets a risk score for this messaging operation (0-100).
     */
    public int getRiskScore() {
        int score = 0;
        
        // Risk for void methods (fire and forget)
        if (isVoid) {
            score += 20;
        }
        
        // Risk for no error handling
        if (!hasProperErrorHandling()) {
            score += 30;
        }
        
        // Risk for transactions without proper handling
        if (hasTransaction && !hasProperErrorHandling()) {
            score += 25;
        }
        
        // Risk for async processing without error handling
        if (isAsync && !hasProperErrorHandling()) {
            score += 20;
        }
        
        // Risk for complex routing without validation
        if ((exchange != null || routingKey != null) && !hasProperErrorHandling()) {
            score += 15;
        }
        
        return Math.min(100, score);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MessagingEntrypointInfo that = (MessagingEntrypointInfo) obj;
        return Objects.equals(methodName, that.methodName) &&
               Objects.equals(declaringClass, that.declaringClass) &&
               Objects.equals(messagingType, that.messagingType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(methodName, declaringClass, messagingType);
    }
    
    @Override
    public String toString() {
        return "MessagingEntrypointInfo{" +
                "method='" + getMethodIdentifier() + '\'' +
                ", type=" + messagingType +
                ", destination='" + getPrimaryDestination() + '\'' +
                ", category='" + getCategory() + '\'' +
                ", complexity=" + getComplexityScore() +
                ", risk=" + getRiskScore() +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String methodName;
        private String declaringClass;
        private MessagingEntrypointType messagingType;
        private String destination;
        private String topic;
        private String queue;
        private String exchange;
        private String routingKey;
        private String containerFactory = "default";
        private String errorHandler;
        private boolean isVoid;
        private boolean isAsync;
        private boolean hasTransaction;
        private Set<String> messageTypes = new LinkedHashSet<>();
        private Set<String> headers = new LinkedHashSet<>();
        private AnnotationInfo sourceAnnotation;
        private Map<String, Object> metadata = new HashMap<>();
        
        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }
        
        public Builder declaringClass(String declaringClass) {
            this.declaringClass = declaringClass;
            return this;
        }
        
        public Builder messagingType(MessagingEntrypointType messagingType) {
            this.messagingType = messagingType;
            return this;
        }
        
        public Builder destination(String destination) {
            this.destination = destination;
            return this;
        }
        
        public Builder topic(String topic) {
            this.topic = topic;
            return this;
        }
        
        public Builder queue(String queue) {
            this.queue = queue;
            return this;
        }
        
        public Builder exchange(String exchange) {
            this.exchange = exchange;
            return this;
        }
        
        public Builder routingKey(String routingKey) {
            this.routingKey = routingKey;
            return this;
        }
        
        public Builder containerFactory(String containerFactory) {
            this.containerFactory = containerFactory;
            return this;
        }
        
        public Builder errorHandler(String errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }
        
        public Builder isVoid(boolean isVoid) {
            this.isVoid = isVoid;
            return this;
        }
        
        public Builder isAsync(boolean isAsync) {
            this.isAsync = isAsync;
            return this;
        }
        
        public Builder hasTransaction(boolean hasTransaction) {
            this.hasTransaction = hasTransaction;
            return this;
        }
        
        public Builder addMessageType(String messageType) {
            if (messageType != null && !messageType.trim().isEmpty()) {
                this.messageTypes.add(messageType.trim());
            }
            return this;
        }
        
        public Builder messageTypes(Set<String> messageTypes) {
            this.messageTypes = new LinkedHashSet<>(messageTypes != null ? messageTypes : Collections.emptySet());
            return this;
        }
        
        public Builder addHeader(String header) {
            if (header != null && !header.trim().isEmpty()) {
                this.headers.add(header.trim());
            }
            return this;
        }
        
        public Builder headers(Set<String> headers) {
            this.headers = new LinkedHashSet<>(headers != null ? headers : Collections.emptySet());
            return this;
        }
        
        public Builder sourceAnnotation(AnnotationInfo sourceAnnotation) {
            this.sourceAnnotation = sourceAnnotation;
            return this;
        }
        
        public Builder addMetadata(String key, Object value) {
            if (key != null && value != null) {
                this.metadata.put(key, value);
            }
            return this;
        }
        
        public MessagingEntrypointInfo build() {
            return new MessagingEntrypointInfo(this);
        }
    }
}