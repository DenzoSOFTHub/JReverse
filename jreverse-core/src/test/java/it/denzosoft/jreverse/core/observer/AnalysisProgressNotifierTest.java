package it.denzosoft.jreverse.core.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisProgressNotifierTest {
    
    private AnalysisProgressNotifier notifier;
    
    @Mock
    private ProgressObserver observer1;
    
    @Mock
    private ProgressObserver observer2;
    
    @Mock
    private ProgressObserver faultyObserver;
    
    @BeforeEach
    void setUp() {
        notifier = new AnalysisProgressNotifier();
    }
    
    @Test
    void shouldCreateNotifierWithSynchronousMode() {
        AnalysisProgressNotifier syncNotifier = new AnalysisProgressNotifier(false);
        
        assertThat(syncNotifier.isAsyncNotification()).isFalse();
        assertThat(syncNotifier.getObserverCount()).isZero();
    }
    
    @Test
    void shouldCreateNotifierWithAsynchronousMode() {
        AnalysisProgressNotifier asyncNotifier = new AnalysisProgressNotifier(true);
        
        assertThat(asyncNotifier.isAsyncNotification()).isTrue();
        assertThat(asyncNotifier.getObserverCount()).isZero();
    }
    
    @Test
    void shouldAddAndRemoveObservers() {
        assertThat(notifier.getObserverCount()).isZero();
        
        notifier.addObserver(observer1);
        assertThat(notifier.getObserverCount()).isEqualTo(1);
        
        notifier.addObserver(observer2);
        assertThat(notifier.getObserverCount()).isEqualTo(2);
        
        notifier.removeObserver(observer1);
        assertThat(notifier.getObserverCount()).isEqualTo(1);
        
        notifier.removeObserver(observer2);
        assertThat(notifier.getObserverCount()).isZero();
    }
    
    @Test
    void shouldNotAddDuplicateObservers() {
        notifier.addObserver(observer1);
        notifier.addObserver(observer1); // Try to add the same observer again
        
        assertThat(notifier.getObserverCount()).isEqualTo(1);
    }
    
    @Test
    void shouldHandleRemovalOfNonExistentObserver() {
        notifier.addObserver(observer1);
        notifier.removeObserver(observer2); // Remove observer that wasn't added
        
        assertThat(notifier.getObserverCount()).isEqualTo(1);
    }
    
    @Test
    void shouldClearAllObservers() {
        notifier.addObserver(observer1);
        notifier.addObserver(observer2);
        
        assertThat(notifier.getObserverCount()).isEqualTo(2);
        
        notifier.clearObservers();
        
        assertThat(notifier.getObserverCount()).isZero();
    }
    
    @Test
    void shouldThrowExceptionForNullObserver() {
        assertThatThrownBy(() -> notifier.addObserver(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("observer cannot be null");
    }
    
    @Test
    void shouldReturnUnmodifiableListOfObservers() {
        notifier.addObserver(observer1);
        notifier.addObserver(observer2);
        
        List<ProgressObserver> observers = notifier.getObservers();
        
        assertThat(observers).hasSize(2);
        assertThat(observers).contains(observer1, observer2);
        
        // Should not be able to modify the returned list
        assertThatThrownBy(() -> observers.clear())
            .isInstanceOf(UnsupportedOperationException.class);
    }
    
    @Test
    void shouldNotifyProgressUpdateToAllObservers() {
        notifier.addObserver(observer1);
        notifier.addObserver(observer2);
        
        notifier.notifyProgress(50, "Processing classes", "Analysis");
        
        ArgumentCaptor<ProgressEvent> eventCaptor = ArgumentCaptor.forClass(ProgressEvent.class);
        
        verify(observer1).onProgressUpdate(eventCaptor.capture());
        verify(observer2).onProgressUpdate(eventCaptor.capture());
        
        List<ProgressEvent> events = eventCaptor.getAllValues();
        assertThat(events).hasSize(2);
        
        ProgressEvent event = events.get(0);
        assertThat(event.getPercentage()).isEqualTo(50);
        assertThat(event.getCurrentTask()).isEqualTo("Processing classes");
        assertThat(event.getOperationType()).isEqualTo("Analysis");
    }
    
    @Test
    void shouldNotifyDetailedProgressUpdate() {
        notifier.addObserver(observer1);
        
        ProgressEvent event = ProgressEvent.builder()
            .percentage(75)
            .currentTask("Analyzing dependencies")
            .operationType("Dependency Analysis")
            .details("Extra details")
            .build();
            
        notifier.notifyProgressUpdate(event);
        
        verify(observer1).onProgressUpdate(event);
    }
    
    @Test
    void shouldNotifyOperationCompletion() {
        notifier.addObserver(observer1);
        notifier.addObserver(observer2);
        
        CompletionEvent event = CompletionEvent.builder()
            .operationType("JAR Analysis")
            .durationMs(5000)
            .message("Analysis completed successfully")
            .build();
            
        notifier.notifyOperationComplete(event);
        
        verify(observer1).onOperationComplete(event);
        verify(observer2).onOperationComplete(event);
    }
    
    @Test
    void shouldNotifyError() {
        notifier.addObserver(observer1);
        notifier.addObserver(observer2);
        
        ErrorEvent event = ErrorEvent.builder()
            .operationType("JAR Loading")
            .message("Failed to load JAR file")
            .cause(new RuntimeException("File not found"))
            .build();
            
        notifier.notifyError(event);
        
        verify(observer1).onError(event);
        verify(observer2).onError(event);
    }
    
    @Test
    void shouldHandleObserverExceptionsDuringNotification() {
        notifier.addObserver(faultyObserver);
        notifier.addObserver(observer1);
        
        // Make the faulty observer throw an exception
        doThrow(new RuntimeException("Observer error"))
            .when(faultyObserver).onProgressUpdate(any(ProgressEvent.class));
        
        // Notification should continue to other observers despite the exception
        notifier.notifyProgress(25, "Testing error handling", "Test");
        
        verify(faultyObserver).onProgressUpdate(any(ProgressEvent.class));
        verify(observer1).onProgressUpdate(any(ProgressEvent.class));
    }
    
    @Test
    void shouldNotNotifyWhenNoObservers() {
        // This should not throw any exception
        assertThatNoException().isThrownBy(() -> {
            notifier.notifyProgress(100, "Task completed", "Test");
            
            ProgressEvent event = ProgressEvent.simple(50, "Test task", "Test");
            notifier.notifyProgressUpdate(event);
            
            CompletionEvent completionEvent = CompletionEvent.builder()
                .operationType("Test")
                .build();
            notifier.notifyOperationComplete(completionEvent);
            
            ErrorEvent errorEvent = ErrorEvent.simple("Test", "Test error");
            notifier.notifyError(errorEvent);
        });
    }
    
    @Test
    void shouldThrowExceptionForNullEvents() {
        notifier.addObserver(observer1);
        
        assertThatThrownBy(() -> notifier.notifyProgressUpdate(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("event cannot be null");
            
        assertThatThrownBy(() -> notifier.notifyOperationComplete(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("event cannot be null");
            
        assertThatThrownBy(() -> notifier.notifyError(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("event cannot be null");
    }
    
    @Test
    void shouldProvideConvenienceMethodsForMethodChaining() {
        notifier.addObserver(observer1);
        
        AnalysisProgressNotifier result = notifier
            .progress(25, "Starting analysis")
            .progress(50, "Half way done")
            .complete("Analysis", 2000)
            .error("Test error", new RuntimeException("Test"));
        
        assertThat(result).isSameAs(notifier);
        
        // Verify all notifications were sent
        verify(observer1, times(2)).onProgressUpdate(any(ProgressEvent.class));
        verify(observer1).onOperationComplete(any(CompletionEvent.class));
        verify(observer1).onError(any(ErrorEvent.class));
    }
    
    @Test
    void shouldHandleAsynchronousNotifications() throws InterruptedException {
        AnalysisProgressNotifier asyncNotifier = new AnalysisProgressNotifier(true);
        
        CountDownLatch latch = new CountDownLatch(2);
        
        ProgressObserver asyncObserver1 = mock(ProgressObserver.class);
        ProgressObserver asyncObserver2 = mock(ProgressObserver.class);
        
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(asyncObserver1).onProgressUpdate(any());
        
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(asyncObserver2).onProgressUpdate(any());
        
        asyncNotifier.addObserver(asyncObserver1);
        asyncNotifier.addObserver(asyncObserver2);
        
        asyncNotifier.notifyProgress(100, "Async test", "Test");
        
        // Wait for asynchronous notifications to complete
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        
        verify(asyncObserver1).onProgressUpdate(any(ProgressEvent.class));
        verify(asyncObserver2).onProgressUpdate(any(ProgressEvent.class));
        
        asyncNotifier.shutdown();
    }
    
    @Test
    void shouldShutdownGracefully() {
        AnalysisProgressNotifier asyncNotifier = new AnalysisProgressNotifier(true);
        asyncNotifier.addObserver(observer1);
        
        // Should not throw any exception
        assertThatNoException().isThrownBy(asyncNotifier::shutdown);
        
        // After shutdown, observer count should be zero
        assertThat(asyncNotifier.getObserverCount()).isZero();
    }
    
    @Test
    void shouldHandleShutdownOfSynchronousNotifier() {
        // Synchronous notifier shutdown should also work without issues
        notifier.addObserver(observer1);
        
        assertThatNoException().isThrownBy(notifier::shutdown);
        assertThat(notifier.getObserverCount()).isZero();
    }
}