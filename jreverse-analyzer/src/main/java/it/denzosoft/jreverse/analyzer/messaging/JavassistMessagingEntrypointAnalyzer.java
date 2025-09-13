package it.denzosoft.jreverse.analyzer.messaging;

import it.denzosoft.jreverse.core.model.AnnotationInfo;
import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.MethodInfo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Javassist-based implementation of MessagingEntrypointAnalyzer.
 * Analyzes messaging patterns and identifies messaging-related configurations.
 */
public class JavassistMessagingEntrypointAnalyzer implements MessagingEntrypointAnalyzer {
    
    private static final Set<String> MESSAGING_ANNOTATIONS = new HashSet<>();
    private static final Map<String, MessagingEntrypointType> ANNOTATION_TYPE_MAP = new HashMap<>();
    private static final Set<String> MESSAGING_TEMPLATES = new HashSet<>();
    
    static {
        // JMS annotations
        MESSAGING_ANNOTATIONS.add("org.springframework.jms.annotation.JmsListener");
        MESSAGING_ANNOTATIONS.add("org.springframework.jms.annotation.EnableJms");
        
        // Kafka annotations
        MESSAGING_ANNOTATIONS.add("org.springframework.kafka.annotation.KafkaListener");
        MESSAGING_ANNOTATIONS.add("org.springframework.kafka.annotation.EnableKafka");
        
        // RabbitMQ annotations
        MESSAGING_ANNOTATIONS.add("org.springframework.amqp.rabbit.annotation.RabbitListener");
        MESSAGING_ANNOTATIONS.add("org.springframework.amqp.rabbit.annotation.EnableRabbit");
        
        // Event annotations
        MESSAGING_ANNOTATIONS.add("org.springframework.context.event.EventListener");
        MESSAGING_ANNOTATIONS.add("org.springframework.transaction.event.TransactionalEventListener");
        
        // WebSocket annotations
        MESSAGING_ANNOTATIONS.add("org.springframework.messaging.handler.annotation.MessageMapping");
        MESSAGING_ANNOTATIONS.add("org.springframework.messaging.simp.annotation.SubscribeMapping");
        MESSAGING_ANNOTATIONS.add("org.springframework.messaging.handler.annotation.SendTo");
        MESSAGING_ANNOTATIONS.add("org.springframework.messaging.simp.annotation.SendToUser");
        
        // AWS SQS
        MESSAGING_ANNOTATIONS.add("org.springframework.cloud.aws.messaging.listener.SqsListener");
        
        // Annotation type mapping
        ANNOTATION_TYPE_MAP.put("org.springframework.jms.annotation.JmsListener", MessagingEntrypointType.JMS_LISTENER);
        ANNOTATION_TYPE_MAP.put("org.springframework.jms.annotation.EnableJms", MessagingEntrypointType.ENABLE_JMS);
        ANNOTATION_TYPE_MAP.put("org.springframework.kafka.annotation.KafkaListener", MessagingEntrypointType.KAFKA_LISTENER);
        ANNOTATION_TYPE_MAP.put("org.springframework.kafka.annotation.EnableKafka", MessagingEntrypointType.ENABLE_KAFKA);
        ANNOTATION_TYPE_MAP.put("org.springframework.amqp.rabbit.annotation.RabbitListener", MessagingEntrypointType.RABBIT_LISTENER);
        ANNOTATION_TYPE_MAP.put("org.springframework.amqp.rabbit.annotation.EnableRabbit", MessagingEntrypointType.ENABLE_RABBIT);
        ANNOTATION_TYPE_MAP.put("org.springframework.context.event.EventListener", MessagingEntrypointType.EVENT_LISTENER);
        ANNOTATION_TYPE_MAP.put("org.springframework.transaction.event.TransactionalEventListener", MessagingEntrypointType.TRANSACTION_EVENT_LISTENER);
        ANNOTATION_TYPE_MAP.put("org.springframework.messaging.handler.annotation.MessageMapping", MessagingEntrypointType.MESSAGE_MAPPING);
        ANNOTATION_TYPE_MAP.put("org.springframework.messaging.simp.annotation.SubscribeMapping", MessagingEntrypointType.SUBSCRIBE_MAPPING);
        ANNOTATION_TYPE_MAP.put("org.springframework.messaging.handler.annotation.SendTo", MessagingEntrypointType.SEND_TO);
        ANNOTATION_TYPE_MAP.put("org.springframework.messaging.simp.annotation.SendToUser", MessagingEntrypointType.SEND_TO_USER);
        ANNOTATION_TYPE_MAP.put("org.springframework.cloud.aws.messaging.listener.SqsListener", MessagingEntrypointType.SQS_LISTENER);
        
        // Template classes for producer detection
        MESSAGING_TEMPLATES.add("org.springframework.jms.core.JmsTemplate");
        MESSAGING_TEMPLATES.add("org.springframework.kafka.core.KafkaTemplate");
        MESSAGING_TEMPLATES.add("org.springframework.amqp.rabbit.core.RabbitTemplate");
    }
    
    @Override
    public MessagingAnalysisResult analyze(JarContent jarContent) {
        long startTime = System.currentTimeMillis();
        List<MessagingEntrypointInfo> allEntrypoints = new ArrayList<>();
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            if (hasMessagingPatterns(classInfo)) {
                MessagingAnalysisResult classResult = analyzeClass(classInfo);
                allEntrypoints.addAll(classResult.getMessagingEntrypoints());
            }
        }
        
        long analysisTime = System.currentTimeMillis() - startTime;
        String fileName = jarContent.getLocation() != null ? jarContent.getLocation().getFileName() : "";
        return new MessagingAnalysisResult(allEntrypoints, analysisTime, fileName);
    }
    
    @Override
    public MessagingAnalysisResult analyzeClass(ClassInfo classInfo) {
        List<MessagingEntrypointInfo> entrypoints = new ArrayList<>();
        
        // Analyze class-level messaging annotations (@EnableJms, @EnableKafka, etc.)
        List<AnnotationInfo> classAnnotations = getMessagingAnnotations(new ArrayList<>(classInfo.getAnnotations()));
        for (AnnotationInfo annotation : classAnnotations) {
            MessagingEntrypointInfo entrypoint = analyzeMessagingAnnotation(
                annotation, classInfo.getFullyQualifiedName(), "<class-level>", "void"
            );
            if (entrypoint != null) {
                entrypoints.add(entrypoint);
            }
        }
        
        // Analyze method-level messaging patterns
        for (MethodInfo method : classInfo.getMethods()) {
            MessagingEntrypointInfo entrypoint = analyzeMethod(method, classInfo.getFullyQualifiedName());
            if (entrypoint != null) {
                entrypoints.add(entrypoint);
            }
        }
        
        return new MessagingAnalysisResult(entrypoints, 0L, "");
    }
    
    private boolean hasMessagingPatterns(ClassInfo classInfo) {
        // Check class-level annotations
        if (classInfo.getAnnotations().stream().anyMatch(this::isMessagingAnnotation)) {
            return true;
        }
        
        // Check method-level patterns
        return classInfo.getMethods().stream()
            .anyMatch(method -> hasMessagingPattern(method));
    }
    
    private boolean hasMessagingPattern(MethodInfo method) {
        // Check messaging annotations
        if (method.getAnnotations().stream().anyMatch(this::isMessagingAnnotation)) {
            return true;
        }
        
        // Check for messaging template usage (basic heuristic)
        String returnType = method.getReturnType();
        return returnType != null && MESSAGING_TEMPLATES.stream()
            .anyMatch(template -> returnType.contains(template));
    }
    
    private boolean isMessagingAnnotation(AnnotationInfo annotation) {
        return MESSAGING_ANNOTATIONS.contains(annotation.getType());
    }
    
    private List<AnnotationInfo> getMessagingAnnotations(List<AnnotationInfo> annotations) {
        return annotations.stream()
            .filter(this::isMessagingAnnotation)
            .collect(Collectors.toList());
    }
    
    private MessagingEntrypointInfo analyzeMethod(MethodInfo method, String className) {
        // Check for messaging annotations
        List<AnnotationInfo> methodAnnotations = getMessagingAnnotations(new ArrayList<>(method.getAnnotations()));
        if (!methodAnnotations.isEmpty()) {
            return analyzeMessagingAnnotation(methodAnnotations.get(0), className, method.getName(), method.getReturnType());
        }
        
        // Check for template-based producers
        String returnType = method.getReturnType();
        if (returnType != null) {
            for (String template : MESSAGING_TEMPLATES) {
                if (returnType.contains(template)) {
                    return analyzeTemplateProducer(template, method, className);
                }
            }
        }
        
        return null;
    }
    
    private MessagingEntrypointInfo analyzeMessagingAnnotation(AnnotationInfo annotation, String className, 
                                                             String methodName, String returnType) {
        MessagingEntrypointType type = ANNOTATION_TYPE_MAP.get(annotation.getType());
        if (type == null) {
            return null;
        }
        
        MessagingEntrypointInfo.Builder builder = MessagingEntrypointInfo.builder()
            .methodName(methodName)
            .declaringClass(className)
            .messagingType(type)
            .isVoid("void".equals(returnType))
            .sourceAnnotation(annotation);
        
        // Extract messaging parameters based on annotation type
        extractMessagingParameters(annotation, type, builder);
        
        // Check for transaction support
        boolean hasTransaction = hasTransactionalAnnotation(annotation) || 
                               type == MessagingEntrypointType.TRANSACTION_EVENT_LISTENER;
        builder.hasTransaction(hasTransaction);
        
        return builder.build();
    }
    
    private MessagingEntrypointInfo analyzeTemplateProducer(String template, MethodInfo method, String className) {
        MessagingEntrypointType type;
        if (template.contains("JmsTemplate")) {
            type = MessagingEntrypointType.JMS_PRODUCER;
        } else if (template.contains("KafkaTemplate")) {
            type = MessagingEntrypointType.KAFKA_PRODUCER;
        } else if (template.contains("RabbitTemplate")) {
            type = MessagingEntrypointType.RABBIT_PRODUCER;
        } else {
            return null;
        }
        
        return MessagingEntrypointInfo.builder()
            .methodName(method.getName())
            .declaringClass(className)
            .messagingType(type)
            .isVoid("void".equals(method.getReturnType()))
            .build();
    }
    
    private void extractMessagingParameters(AnnotationInfo annotation, MessagingEntrypointType type, 
                                          MessagingEntrypointInfo.Builder builder) {
        if (annotation.getAttributes() == null || annotation.getAttributes().isEmpty()) {
            return;
        }
        
        // Common destination parameters
        Object destination = annotation.getAttributes().get("destination");
        if (destination instanceof String && !((String) destination).trim().isEmpty()) {
            builder.destination((String) destination);
        }
        
        // JMS specific
        if (type == MessagingEntrypointType.JMS_LISTENER) {
            extractJmsParameters(annotation, builder);
        }
        
        // Kafka specific
        else if (type == MessagingEntrypointType.KAFKA_LISTENER) {
            extractKafkaParameters(annotation, builder);
        }
        
        // RabbitMQ specific
        else if (type == MessagingEntrypointType.RABBIT_LISTENER) {
            extractRabbitParameters(annotation, builder);
        }
        
        // WebSocket specific
        else if (type.isWebSocket()) {
            extractWebSocketParameters(annotation, builder);
        }
        
        // Event specific
        else if (type.isEventBased()) {
            extractEventParameters(annotation, builder);
        }
        
        // Common parameters
        extractCommonParameters(annotation, builder);
    }
    
    private void extractJmsParameters(AnnotationInfo annotation, MessagingEntrypointInfo.Builder builder) {
        Object containerFactory = annotation.getAttributes().get("containerFactory");
        if (containerFactory instanceof String) {
            builder.containerFactory((String) containerFactory);
        }
    }
    
    private void extractKafkaParameters(AnnotationInfo annotation, MessagingEntrypointInfo.Builder builder) {
        Object topics = annotation.getAttributes().get("topics");
        if (topics instanceof String) {
            builder.topic((String) topics);
        } else if (topics instanceof String[]) {
            String[] topicArray = (String[]) topics;
            if (topicArray.length > 0) {
                builder.topic(topicArray[0]); // Use first topic
            }
        }
        
        Object containerFactory = annotation.getAttributes().get("containerFactory");
        if (containerFactory instanceof String) {
            builder.containerFactory((String) containerFactory);
        }
    }
    
    private void extractRabbitParameters(AnnotationInfo annotation, MessagingEntrypointInfo.Builder builder) {
        Object queues = annotation.getAttributes().get("queues");
        if (queues instanceof String) {
            builder.queue((String) queues);
        } else if (queues instanceof String[]) {
            String[] queueArray = (String[]) queues;
            if (queueArray.length > 0) {
                builder.queue(queueArray[0]); // Use first queue
            }
        }
        
        Object containerFactory = annotation.getAttributes().get("containerFactory");
        if (containerFactory instanceof String) {
            builder.containerFactory((String) containerFactory);
        }
    }
    
    private void extractWebSocketParameters(AnnotationInfo annotation, MessagingEntrypointInfo.Builder builder) {
        Object value = annotation.getAttributes().get("value");
        if (value instanceof String) {
            builder.destination((String) value);
        } else if (value instanceof String[]) {
            String[] valueArray = (String[]) value;
            if (valueArray.length > 0) {
                builder.destination(valueArray[0]);
            }
        }
    }
    
    private void extractEventParameters(AnnotationInfo annotation, MessagingEntrypointInfo.Builder builder) {
        // Event listeners typically don't have destination parameters
        // but may have conditions or other parameters
        Object condition = annotation.getAttributes().get("condition");
        if (condition instanceof String) {
            builder.addMetadata("condition", condition);
        }
    }
    
    private void extractCommonParameters(AnnotationInfo annotation, MessagingEntrypointInfo.Builder builder) {
        // Error handler
        Object errorHandler = annotation.getAttributes().get("errorHandler");
        if (errorHandler instanceof String && !((String) errorHandler).trim().isEmpty()) {
            builder.errorHandler((String) errorHandler);
        }
    }
    
    private boolean hasTransactionalAnnotation(AnnotationInfo annotation) {
        return "org.springframework.transaction.event.TransactionalEventListener".equals(annotation.getType());
    }
}