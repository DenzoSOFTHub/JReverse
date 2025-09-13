package it.denzosoft.jreverse.analyzer.bootstrap;

import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.MethodInfo;

import java.util.List;
import java.util.Optional;

/**
 * Represents a single step in the Spring Boot bootstrap sequence.
 * Each step corresponds to a method call or significant action during the startup process.
 */
public class BootstrapSequenceStep {
    
    private final int sequenceNumber;
    private final BootstrapSequencePhase phase;
    private final String participantClass;
    private final String methodName;
    private final String description;
    private final Optional<ClassInfo> classInfo;
    private final Optional<MethodInfo> methodInfo;
    private final List<String> parameters;
    private final Optional<String> returnType;
    private final long estimatedDurationMs;
    private final StepType stepType;
    
    private BootstrapSequenceStep(Builder builder) {
        this.sequenceNumber = builder.sequenceNumber;
        this.phase = builder.phase;
        this.participantClass = builder.participantClass;
        this.methodName = builder.methodName;
        this.description = builder.description;
        this.classInfo = builder.classInfo;
        this.methodInfo = builder.methodInfo;
        this.parameters = List.copyOf(builder.parameters);
        this.returnType = builder.returnType;
        this.estimatedDurationMs = builder.estimatedDurationMs;
        this.stepType = builder.stepType;
    }
    
    // Getters
    public int getSequenceNumber() { return sequenceNumber; }
    public BootstrapSequencePhase getPhase() { return phase; }
    public String getParticipantClass() { return participantClass; }
    public String getMethodName() { return methodName; }
    public String getDescription() { return description; }
    public Optional<ClassInfo> getClassInfo() { return classInfo; }
    public Optional<MethodInfo> getMethodInfo() { return methodInfo; }
    public List<String> getParameters() { return parameters; }
    public Optional<String> getReturnType() { return returnType; }
    public long getEstimatedDurationMs() { return estimatedDurationMs; }
    public StepType getStepType() { return stepType; }
    
    /**
     * Returns a formatted method signature for sequence diagrams.
     */
    public String getFormattedMethodSignature() {
        if (parameters.isEmpty()) {
            return methodName + "()";
        }
        return String.format("%s(%s)", methodName, String.join(", ", parameters));
    }
    
    /**
     * Returns a short class name without package prefix.
     */
    public String getShortClassName() {
        if (participantClass.contains(".")) {
            return participantClass.substring(participantClass.lastIndexOf('.') + 1);
        }
        return participantClass;
    }
    
    /**
     * Checks if this step represents a method call.
     */
    public boolean isMethodCall() {
        return stepType == StepType.METHOD_CALL;
    }
    
    /**
     * Checks if this step represents an event or notification.
     */
    public boolean isEvent() {
        return stepType == StepType.EVENT;
    }
    
    /**
     * Builder pattern for constructing BootstrapSequenceStep instances.
     */
    public static class Builder {
        private int sequenceNumber;
        private BootstrapSequencePhase phase;
        private String participantClass;
        private String methodName;
        private String description;
        private Optional<ClassInfo> classInfo = Optional.empty();
        private Optional<MethodInfo> methodInfo = Optional.empty();
        private List<String> parameters = List.of();
        private Optional<String> returnType = Optional.empty();
        private long estimatedDurationMs = 0;
        private StepType stepType = StepType.METHOD_CALL;
        
        public Builder sequenceNumber(int sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
            return this;
        }
        
        public Builder phase(BootstrapSequencePhase phase) {
            this.phase = phase;
            return this;
        }
        
        public Builder participantClass(String participantClass) {
            this.participantClass = participantClass;
            return this;
        }
        
        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder classInfo(ClassInfo classInfo) {
            this.classInfo = Optional.ofNullable(classInfo);
            return this;
        }
        
        public Builder methodInfo(MethodInfo methodInfo) {
            this.methodInfo = Optional.ofNullable(methodInfo);
            return this;
        }
        
        public Builder parameters(List<String> parameters) {
            this.parameters = parameters;
            return this;
        }
        
        public Builder returnType(String returnType) {
            this.returnType = Optional.ofNullable(returnType);
            return this;
        }
        
        public Builder estimatedDurationMs(long estimatedDurationMs) {
            this.estimatedDurationMs = estimatedDurationMs;
            return this;
        }
        
        public Builder stepType(StepType stepType) {
            this.stepType = stepType;
            return this;
        }
        
        public BootstrapSequenceStep build() {
            if (phase == null) {
                throw new IllegalArgumentException("Phase is required");
            }
            if (participantClass == null || participantClass.trim().isEmpty()) {
                throw new IllegalArgumentException("Participant class is required");
            }
            if (methodName == null || methodName.trim().isEmpty()) {
                throw new IllegalArgumentException("Method name is required");
            }
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException("Description is required");
            }
            
            return new BootstrapSequenceStep(this);
        }
    }
    
    /**
     * Type of sequence step.
     */
    public enum StepType {
        /**
         * Represents a method call between classes.
         */
        METHOD_CALL,
        
        /**
         * Represents an event or notification.
         */
        EVENT,
        
        /**
         * Represents a lifecycle callback.
         */
        LIFECYCLE_CALLBACK,
        
        /**
         * Represents internal framework processing.
         */
        INTERNAL_PROCESSING
    }
    
    @Override
    public String toString() {
        return String.format("Step %d: %s.%s() [%s]", 
            sequenceNumber, getShortClassName(), methodName, phase.getDisplayName());
    }
}