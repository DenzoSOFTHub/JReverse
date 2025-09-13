package it.denzosoft.jreverse.core.observer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ProgressEventTest {
    
    @Test
    void shouldCreateProgressEventWithBuilder() {
        long timestamp = System.currentTimeMillis();
        Object details = "Additional details";
        
        ProgressEvent event = ProgressEvent.builder()
            .percentage(75)
            .currentTask("Processing classes")
            .operationType("JAR Analysis")
            .timestamp(timestamp)
            .details(details)
            .build();
            
        assertThat(event.getPercentage()).isEqualTo(75);
        assertThat(event.getCurrentTask()).isEqualTo("Processing classes");
        assertThat(event.getOperationType()).isEqualTo("JAR Analysis");
        assertThat(event.getTimestamp()).isEqualTo(timestamp);
        assertThat(event.getDetails()).isEqualTo(details);
        assertThat(event.hasDetails()).isTrue();
        assertThat(event.isCompleted()).isFalse();
    }
    
    @Test
    void shouldCreateSimpleProgressEvent() {
        ProgressEvent event = ProgressEvent.simple(50, "Current task", "Analysis");
        
        assertThat(event.getPercentage()).isEqualTo(50);
        assertThat(event.getCurrentTask()).isEqualTo("Current task");
        assertThat(event.getOperationType()).isEqualTo("Analysis");
        assertThat(event.getTimestamp()).isGreaterThan(0);
        assertThat(event.hasDetails()).isFalse();
        assertThat(event.isCompleted()).isFalse();
    }
    
    @Test
    void shouldDetectCompletedStatus() {
        ProgressEvent completedEvent = ProgressEvent.simple(100, "Done", "Test");
        ProgressEvent incompleteEvent = ProgressEvent.simple(99, "Almost done", "Test");
        
        assertThat(completedEvent.isCompleted()).isTrue();
        assertThat(incompleteEvent.isCompleted()).isFalse();
    }
    
    @Test
    void shouldValidatePercentageRange() {
        assertThatThrownBy(() -> ProgressEvent.simple(-1, "Invalid", "Test"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Percentage must be between 0 and 100, got: -1");
            
        assertThatThrownBy(() -> ProgressEvent.simple(101, "Invalid", "Test"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Percentage must be between 0 and 100, got: 101");
    }
    
    @Test
    void shouldAllowValidPercentageRange() {
        assertThatNoException().isThrownBy(() -> {
            ProgressEvent.simple(0, "Start", "Test");
            ProgressEvent.simple(50, "Middle", "Test");
            ProgressEvent.simple(100, "End", "Test");
        });
    }
    
    @Test
    void shouldRequireNonNullFields() {
        assertThatThrownBy(() -> 
            ProgressEvent.builder()
                .percentage(50)
                .currentTask(null)
                .operationType("Test")
                .build())
            .isInstanceOf(NullPointerException.class)
            .hasMessage("currentTask cannot be null");
            
        assertThatThrownBy(() -> 
            ProgressEvent.builder()
                .percentage(50)
                .currentTask("Task")
                .operationType(null)
                .build())
            .isInstanceOf(NullPointerException.class)
            .hasMessage("operationType cannot be null");
    }
    
    @Test
    void shouldUseCurrentTimeWhenTimestampNotProvided() {
        long before = System.currentTimeMillis();
        ProgressEvent event = ProgressEvent.simple(25, "Task", "Test");
        long after = System.currentTimeMillis();
        
        assertThat(event.getTimestamp()).isBetween(before, after);
    }
    
    @Test
    void shouldUseCurrentTimeWhenInvalidTimestampProvided() {
        ProgressEvent event = ProgressEvent.builder()
            .percentage(25)
            .currentTask("Task")
            .operationType("Test")
            .timestamp(0) // Invalid timestamp
            .build();
            
        assertThat(event.getTimestamp()).isGreaterThan(0);
    }
    
    @Test
    void shouldHandleNullDetails() {
        ProgressEvent event = ProgressEvent.builder()
            .percentage(25)
            .currentTask("Task")
            .operationType("Test")
            .details(null)
            .build();
            
        assertThat(event.hasDetails()).isFalse();
        assertThat(event.getDetails()).isNull();
    }
    
    @Test
    void shouldImplementEqualsAndHashCodeCorrectly() {
        long timestamp = System.currentTimeMillis();
        
        ProgressEvent event1 = ProgressEvent.builder()
            .percentage(50)
            .currentTask("Task")
            .operationType("Test")
            .timestamp(timestamp)
            .build();
            
        ProgressEvent event2 = ProgressEvent.builder()
            .percentage(50)
            .currentTask("Task")
            .operationType("Test")
            .timestamp(timestamp)
            .build();
            
        ProgressEvent event3 = ProgressEvent.builder()
            .percentage(75)
            .currentTask("Task")
            .operationType("Test")
            .timestamp(timestamp)
            .build();
        
        // Test equals
        assertThat(event1).isEqualTo(event2);
        assertThat(event1).isNotEqualTo(event3);
        assertThat(event1).isNotEqualTo(null);
        assertThat(event1).isEqualTo(event1);
        
        // Test hashCode
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        assertThat(event1.hashCode()).isNotEqualTo(event3.hashCode());
    }
    
    @Test
    void shouldHaveInformativeToString() {
        ProgressEvent event = ProgressEvent.builder()
            .percentage(80)
            .currentTask("Finalizing")
            .operationType("Generation")
            .timestamp(1234567890L)
            .build();
            
        String toString = event.toString();
        
        assertThat(toString).contains("ProgressEvent");
        assertThat(toString).contains("percentage=80");
        assertThat(toString).contains("currentTask='Finalizing'");
        assertThat(toString).contains("operationType='Generation'");
        assertThat(toString).contains("timestamp=1234567890");
    }
    
    @Test
    void shouldSupportMethodChainingInBuilder() {
        ProgressEvent event = ProgressEvent.builder()
            .percentage(60)
            .currentTask("Chaining test")
            .operationType("Builder test")
            .timestamp(System.currentTimeMillis())
            .details("Test details")
            .build();
            
        assertThat(event.getPercentage()).isEqualTo(60);
        assertThat(event.getCurrentTask()).isEqualTo("Chaining test");
        assertThat(event.getOperationType()).isEqualTo("Builder test");
        assertThat(event.hasDetails()).isTrue();
    }
    
    @Test
    void shouldHandleDifferentDetailTypes() {
        String stringDetails = "String details";
        Integer integerDetails = 42;
        Object objectDetails = new Object();
        
        ProgressEvent stringEvent = ProgressEvent.builder()
            .percentage(33)
            .currentTask("String test")
            .operationType("Test")
            .details(stringDetails)
            .build();
            
        ProgressEvent integerEvent = ProgressEvent.builder()
            .percentage(66)
            .currentTask("Integer test")
            .operationType("Test")
            .details(integerDetails)
            .build();
            
        ProgressEvent objectEvent = ProgressEvent.builder()
            .percentage(99)
            .currentTask("Object test")
            .operationType("Test")
            .details(objectDetails)
            .build();
            
        assertThat(stringEvent.getDetails()).isEqualTo(stringDetails);
        assertThat(integerEvent.getDetails()).isEqualTo(integerDetails);
        assertThat(objectEvent.getDetails()).isEqualTo(objectDetails);
    }
    
    @Test
    void shouldCreateNewBuilderInstance() {
        ProgressEvent.Builder builder1 = ProgressEvent.builder();
        ProgressEvent.Builder builder2 = ProgressEvent.builder();
        
        assertThat(builder1).isNotSameAs(builder2);
    }
}