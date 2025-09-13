package it.denzosoft.jreverse.analyzer.messaging;

/**
 * Enumeration of messaging entrypoint types for Spring applications.
 * Categorizes different messaging patterns and their usage.
 */
public enum MessagingEntrypointType {
    
    JMS_LISTENER("@JmsListener", "JMS message listener", true),
    KAFKA_LISTENER("@KafkaListener", "Kafka message consumer", true),
    RABBIT_LISTENER("@RabbitListener", "RabbitMQ message consumer", true),
    EVENT_LISTENER("@EventListener", "Spring event listener", true),
    TRANSACTION_EVENT_LISTENER("@TransactionalEventListener", "Transactional event listener", true),
    SQS_LISTENER("@SqsListener", "AWS SQS message listener", true),
    JMS_PRODUCER("JmsTemplate", "JMS message producer", false),
    KAFKA_PRODUCER("KafkaTemplate", "Kafka message producer", false),
    RABBIT_PRODUCER("RabbitTemplate", "RabbitMQ message producer", false),
    MESSAGE_MAPPING("@MessageMapping", "WebSocket message mapping", true),
    SUBSCRIBE_MAPPING("@SubscribeMapping", "STOMP subscribe mapping", true),
    SEND_TO("@SendTo", "Message destination routing", true),
    SEND_TO_USER("@SendToUser", "User-specific message routing", true),
    ENABLE_JMS("@EnableJms", "Enable JMS configuration", true),
    ENABLE_KAFKA("@EnableKafka", "Enable Kafka configuration", true),
    ENABLE_RABBIT("@EnableRabbit", "Enable RabbitMQ configuration", true);
    
    private final String displayName;
    private final String description;
    private final boolean isAnnotationBased;
    
    MessagingEntrypointType(String displayName, String description, boolean isAnnotationBased) {
        this.displayName = displayName;
        this.description = description;
        this.isAnnotationBased = isAnnotationBased;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isAnnotationBased() {
        return isAnnotationBased;
    }
    
    /**
     * Determines if this is a message listener type.
     */
    public boolean isListener() {
        return this == JMS_LISTENER || this == KAFKA_LISTENER || this == RABBIT_LISTENER || 
               this == EVENT_LISTENER || this == TRANSACTION_EVENT_LISTENER || 
               this == SQS_LISTENER || this == MESSAGE_MAPPING || this == SUBSCRIBE_MAPPING;
    }
    
    /**
     * Determines if this is a message producer type.
     */
    public boolean isProducer() {
        return this == JMS_PRODUCER || this == KAFKA_PRODUCER || this == RABBIT_PRODUCER;
    }
    
    /**
     * Determines if this is a WebSocket/STOMP type.
     */
    public boolean isWebSocket() {
        return this == MESSAGE_MAPPING || this == SUBSCRIBE_MAPPING || 
               this == SEND_TO || this == SEND_TO_USER;
    }
    
    /**
     * Determines if this is a configuration type.
     */
    public boolean isConfiguration() {
        return this == ENABLE_JMS || this == ENABLE_KAFKA || this == ENABLE_RABBIT;
    }
    
    /**
     * Determines if this is an event-based type.
     */
    public boolean isEventBased() {
        return this == EVENT_LISTENER || this == TRANSACTION_EVENT_LISTENER;
    }
    
    /**
     * Gets the messaging technology category.
     */
    public String getCategory() {
        if (this.name().contains("JMS")) return "JMS";
        if (this.name().contains("KAFKA")) return "Kafka";
        if (this.name().contains("RABBIT")) return "RabbitMQ";
        if (this.name().contains("SQS")) return "AWS SQS";
        if (this.name().contains("EVENT")) return "Spring Events";
        if (isWebSocket()) return "WebSocket/STOMP";
        return "Other";
    }
}