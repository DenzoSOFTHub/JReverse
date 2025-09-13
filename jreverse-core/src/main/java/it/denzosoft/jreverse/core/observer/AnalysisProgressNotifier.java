package it.denzosoft.jreverse.core.observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Progress notifier implementing Observer pattern for analysis operations.
 * Thread-safe implementation supporting asynchronous notifications.
 */
public class AnalysisProgressNotifier {
    
    private static final Logger LOGGER = Logger.getLogger(AnalysisProgressNotifier.class.getName());
    
    private final List<ProgressObserver> observers;
    private final ExecutorService notificationExecutor;
    private final boolean asyncNotification;
    
    /**
     * Creates a notifier with synchronous notifications.
     */
    public AnalysisProgressNotifier() {
        this(false);
    }
    
    /**
     * Creates a notifier with configurable notification mode.
     * 
     * @param asyncNotification if true, notifications are sent asynchronously
     */
    public AnalysisProgressNotifier(boolean asyncNotification) {
        this.observers = new CopyOnWriteArrayList<>();
        this.asyncNotification = asyncNotification;
        this.notificationExecutor = asyncNotification ? 
            Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "progress-notifier");
                t.setDaemon(true);
                return t;
            }) : null;
            
        LOGGER.fine("AnalysisProgressNotifier created with " + 
                   (asyncNotification ? "asynchronous" : "synchronous") + " notifications");
    }
    
    /**
     * Adds an observer to receive progress notifications.
     * 
     * @param observer the observer to add
     * @throws IllegalArgumentException if observer is null
     */
    public void addObserver(ProgressObserver observer) {
        Objects.requireNonNull(observer, "observer cannot be null");
        
        if (!observers.contains(observer)) {
            observers.add(observer);
            LOGGER.fine("Added progress observer: " + observer.getClass().getSimpleName());
        }
    }
    
    /**
     * Removes an observer from receiving notifications.
     * 
     * @param observer the observer to remove
     */
    public void removeObserver(ProgressObserver observer) {
        if (observer != null && observers.remove(observer)) {
            LOGGER.fine("Removed progress observer: " + observer.getClass().getSimpleName());
        }
    }
    
    /**
     * Removes all observers.
     */
    public void clearObservers() {
        int count = observers.size();
        observers.clear();
        LOGGER.fine("Cleared " + count + " observers");
    }
    
    /**
     * Gets the current number of registered observers.
     * 
     * @return number of observers
     */
    public int getObserverCount() {
        return observers.size();
    }
    
    /**
     * Gets a read-only view of the registered observers.
     * 
     * @return unmodifiable list of observers
     */
    public List<ProgressObserver> getObservers() {
        return Collections.unmodifiableList(new ArrayList<>(observers));
    }
    
    /**
     * Notifies all observers of progress update.
     * 
     * @param percentage completion percentage (0-100)
     * @param currentTask description of current task
     * @param operationType type of operation being performed
     */
    public void notifyProgress(int percentage, String currentTask, String operationType) {
        ProgressEvent event = ProgressEvent.simple(percentage, currentTask, operationType);
        notifyProgressUpdate(event);
    }
    
    /**
     * Notifies all observers of detailed progress update.
     * 
     * @param event the progress event with complete details
     */
    public void notifyProgressUpdate(ProgressEvent event) {
        Objects.requireNonNull(event, "event cannot be null");
        
        if (observers.isEmpty()) {
            return;
        }
        
        LOGGER.fine(String.format("Notifying %d observers of progress: %d%% - %s", 
                   observers.size(), event.getPercentage(), event.getCurrentTask()));
        
        if (asyncNotification && notificationExecutor != null) {
            notificationExecutor.submit(() -> doNotifyProgressUpdate(event));
        } else {
            doNotifyProgressUpdate(event);
        }
    }
    
    /**
     * Notifies all observers of operation completion.
     * 
     * @param event the completion event
     */
    public void notifyOperationComplete(CompletionEvent event) {
        Objects.requireNonNull(event, "event cannot be null");
        
        if (observers.isEmpty()) {
            return;
        }
        
        LOGGER.info("Notifying operation completion: " + event.getOperationType());
        
        if (asyncNotification && notificationExecutor != null) {
            notificationExecutor.submit(() -> doNotifyOperationComplete(event));
        } else {
            doNotifyOperationComplete(event);
        }
    }
    
    /**
     * Notifies all observers of an error.
     * 
     * @param event the error event
     */
    public void notifyError(ErrorEvent event) {
        Objects.requireNonNull(event, "event cannot be null");
        
        if (observers.isEmpty()) {
            return;
        }
        
        LOGGER.warning("Notifying error: " + event.getErrorMessage());
        
        if (asyncNotification && notificationExecutor != null) {
            notificationExecutor.submit(() -> doNotifyError(event));
        } else {
            doNotifyError(event);
        }
    }
    
    private void doNotifyProgressUpdate(ProgressEvent event) {
        for (ProgressObserver observer : observers) {
            try {
                observer.onProgressUpdate(event);
            } catch (Exception e) {
                LOGGER.warning("Observer " + observer.getClass().getSimpleName() + 
                             " threw exception during progress update: " + e.getMessage());
            }
        }
    }
    
    private void doNotifyOperationComplete(CompletionEvent event) {
        for (ProgressObserver observer : observers) {
            try {
                observer.onOperationComplete(event);
            } catch (Exception e) {
                LOGGER.warning("Observer " + observer.getClass().getSimpleName() + 
                             " threw exception during completion notification: " + e.getMessage());
            }
        }
    }
    
    private void doNotifyError(ErrorEvent event) {
        for (ProgressObserver observer : observers) {
            try {
                observer.onError(event);
            } catch (Exception e) {
                LOGGER.warning("Observer " + observer.getClass().getSimpleName() + 
                             " threw exception during error notification: " + e.getMessage());
            }
        }
    }
    
    /**
     * Shuts down the notifier and its resources.
     * Should be called when the notifier is no longer needed.
     */
    public void shutdown() {
        clearObservers();
        
        if (notificationExecutor != null) {
            notificationExecutor.shutdown();
            try {
                if (!notificationExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    notificationExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                notificationExecutor.shutdownNow();
            }
        }
        
        LOGGER.info("AnalysisProgressNotifier shutdown completed");
    }
    
    /**
     * Checks if the notifier is configured for asynchronous notifications.
     * 
     * @return true if notifications are sent asynchronously
     */
    public boolean isAsyncNotification() {
        return asyncNotification;
    }
    
    /**
     * Convenience method to create a simple progress notification.
     * 
     * @param percentage completion percentage
     * @param task current task description
     * @return this notifier for method chaining
     */
    public AnalysisProgressNotifier progress(int percentage, String task) {
        notifyProgress(percentage, task, "Analysis");
        return this;
    }
    
    /**
     * Convenience method to create a completion notification.
     * 
     * @param operationType the type of operation that completed
     * @param durationMs operation duration in milliseconds
     * @return this notifier for method chaining
     */
    public AnalysisProgressNotifier complete(String operationType, long durationMs) {
        CompletionEvent event = CompletionEvent.builder()
            .operationType(operationType)
            .durationMs(durationMs)
            .success(true)
            .build();
        notifyOperationComplete(event);
        return this;
    }
    
    /**
     * Convenience method to create an error notification.
     * 
     * @param message error message
     * @param throwable the exception that occurred
     * @return this notifier for method chaining
     */
    public AnalysisProgressNotifier error(String message, Throwable throwable) {
        ErrorEvent event = ErrorEvent.builder()
            .message(message)
            .throwable(throwable)
            .operationType("Analysis")
            .recoverable(false)
            .build();
        notifyError(event);
        return this;
    }
}