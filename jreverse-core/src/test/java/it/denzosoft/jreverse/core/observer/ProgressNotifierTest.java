package it.denzosoft.jreverse.core.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProgressNotifierTest {

    @Mock
    private ProgressObserver observer1;
    
    @Mock
    private ProgressObserver observer2;
    
    private ProgressNotifier notifier;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        notifier = new ProgressNotifier("TestOperation");
    }

    @Test
    void shouldAddAndRemoveObservers() {
        assertEquals(0, notifier.getObserverCount());
        
        notifier.addObserver(observer1);
        assertEquals(1, notifier.getObserverCount());
        
        notifier.addObserver(observer2);
        assertEquals(2, notifier.getObserverCount());
        
        // Adding same observer should not increase count
        notifier.addObserver(observer1);
        assertEquals(2, notifier.getObserverCount());
        
        notifier.removeObserver(observer1);
        assertEquals(1, notifier.getObserverCount());
        
        notifier.removeAllObservers();
        assertEquals(0, notifier.getObserverCount());
    }

    @Test
    void shouldNotifyProgressToAllObservers() {
        notifier.addObserver(observer1);
        notifier.addObserver(observer2);
        
        notifier.notifyProgress(50, "Processing classes");
        
        ArgumentCaptor<ProgressEvent> eventCaptor = ArgumentCaptor.forClass(ProgressEvent.class);
        verify(observer1).onProgressUpdate(eventCaptor.capture());
        verify(observer2).onProgressUpdate(eventCaptor.capture());
        
        ProgressEvent event = eventCaptor.getValue();
        assertEquals(50, event.getPercentage());
        assertEquals("Processing classes", event.getCurrentTask());
        assertEquals("TestOperation", event.getOperationType());
    }

    @Test
    void shouldNotifyProgressWithDetails() {
        notifier.addObserver(observer1);
        
        String details = "Additional info";
        notifier.notifyProgress(75, "Generating report", details);
        
        ArgumentCaptor<ProgressEvent> eventCaptor = ArgumentCaptor.forClass(ProgressEvent.class);
        verify(observer1).onProgressUpdate(eventCaptor.capture());
        
        ProgressEvent event = eventCaptor.getValue();
        assertEquals(75, event.getPercentage());
        assertEquals("Generating report", event.getCurrentTask());
        assertEquals(details, event.getDetails());
        assertTrue(event.hasDetails());
    }

    @Test
    void shouldNotifyCompletion() {
        notifier.addObserver(observer1);
        
        long startTime = System.currentTimeMillis() - 1000;
        String result = "Success";
        
        notifier.notifyCompletion(startTime, result);
        
        ArgumentCaptor<CompletionEvent> eventCaptor = ArgumentCaptor.forClass(CompletionEvent.class);
        verify(observer1).onOperationComplete(eventCaptor.capture());
        
        CompletionEvent event = eventCaptor.getValue();
        assertEquals("TestOperation", event.getOperationType());
        assertEquals(startTime, event.getStartTime());
        assertEquals(result, event.getResult());
        assertTrue(event.getDurationMs() >= 0);
    }

    @Test
    void shouldNotifyCompletionWithMessage() {
        notifier.addObserver(observer1);
        
        long startTime = System.currentTimeMillis() - 500;
        String result = "Analysis complete";
        String message = "Successfully processed 100 classes";
        
        notifier.notifyCompletion(startTime, result, message);
        
        ArgumentCaptor<CompletionEvent> eventCaptor = ArgumentCaptor.forClass(CompletionEvent.class);
        verify(observer1).onOperationComplete(eventCaptor.capture());
        
        CompletionEvent event = eventCaptor.getValue();
        assertEquals(message, event.getMessage());
        assertTrue(event.hasMessage());
    }

    @Test
    void shouldNotifyError() {
        notifier.addObserver(observer1);
        
        String errorMessage = "Failed to analyze JAR";
        notifier.notifyError(errorMessage);
        
        ArgumentCaptor<ErrorEvent> eventCaptor = ArgumentCaptor.forClass(ErrorEvent.class);
        verify(observer1).onError(eventCaptor.capture());
        
        ErrorEvent event = eventCaptor.getValue();
        assertEquals("TestOperation", event.getOperationType());
        assertEquals(errorMessage, event.getErrorMessage());
        assertFalse(event.hasCause());
        assertFalse(event.isRecoverable());
    }

    @Test
    void shouldNotifyErrorWithCause() {
        notifier.addObserver(observer1);
        
        String errorMessage = "Analysis failed";
        Exception cause = new RuntimeException("Root cause");
        
        notifier.notifyError(errorMessage, cause);
        
        ArgumentCaptor<ErrorEvent> eventCaptor = ArgumentCaptor.forClass(ErrorEvent.class);
        verify(observer1).onError(eventCaptor.capture());
        
        ErrorEvent event = eventCaptor.getValue();
        assertEquals(errorMessage, event.getErrorMessage());
        assertEquals(cause, event.getCause());
        assertTrue(event.hasCause());
    }

    @Test
    void shouldNotifyErrorWithFullDetails() {
        notifier.addObserver(observer1);
        
        String errorMessage = "Critical error";
        Exception cause = new RuntimeException("Root cause");
        String errorCode = "ERR_001";
        boolean recoverable = true;
        
        notifier.notifyError(errorMessage, cause, errorCode, recoverable);
        
        ArgumentCaptor<ErrorEvent> eventCaptor = ArgumentCaptor.forClass(ErrorEvent.class);
        verify(observer1).onError(eventCaptor.capture());
        
        ErrorEvent event = eventCaptor.getValue();
        assertEquals(errorMessage, event.getErrorMessage());
        assertEquals(cause, event.getCause());
        assertEquals(errorCode, event.getErrorCode());
        assertEquals(recoverable, event.isRecoverable());
        assertTrue(event.hasErrorCode());
    }

    @Test
    void shouldHandleObserverExceptions() {
        // Observer that throws exception
        ProgressObserver faultyObserver = mock(ProgressObserver.class);
        doThrow(new RuntimeException("Observer error")).when(faultyObserver).onProgressUpdate(any());
        
        notifier.addObserver(faultyObserver);
        notifier.addObserver(observer1);
        
        // Should not throw exception and should still notify other observers
        assertDoesNotThrow(() -> notifier.notifyProgress(50, "Testing error handling"));
        
        verify(observer1).onProgressUpdate(any(ProgressEvent.class));
    }

    @Test
    void shouldNotNotifyWhenNoObservers() {
        // Should not throw exceptions when no observers are registered
        assertDoesNotThrow(() -> {
            notifier.notifyProgress(50, "Test");
            notifier.notifyCompletion(System.currentTimeMillis(), "Result");
            notifier.notifyError("Error");
        });
    }

    @Test
    void shouldIgnoreNullObserver() {
        notifier.addObserver(null);
        assertEquals(0, notifier.getObserverCount());
        
        // Should not throw when trying to remove null
        assertDoesNotThrow(() -> notifier.removeObserver(null));
    }
}