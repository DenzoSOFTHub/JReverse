package it.denzosoft.jreverse.core.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe implementation of the Observer pattern for progress notifications.
 * Manages a list of observers and notifies them of events.
 */
public class ProgressNotifier {
    
    private final List<ProgressObserver> observers = new CopyOnWriteArrayList<>();
    private final String operationType;
    
    public ProgressNotifier(String operationType) {
        this.operationType = operationType;
    }
    
    /**
     * Adds an observer to receive progress notifications.
     * 
     * @param observer the observer to add
     */
    public void addObserver(ProgressObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    /**
     * Removes an observer from receiving notifications.
     * 
     * @param observer the observer to remove
     */
    public void removeObserver(ProgressObserver observer) {
        observers.remove(observer);
    }
    
    /**
     * Removes all observers.
     */
    public void removeAllObservers() {
        observers.clear();
    }
    
    /**
     * Gets the number of registered observers.
     * 
     * @return number of observers
     */
    public int getObserverCount() {
        return observers.size();
    }
    
    /**
     * Gets the operation type for this notifier.
     * 
     * @return the operation type
     */
    public String getOperationType() {
        return operationType;
    }
    
    /**
     * Notifies all observers of a progress update.
     * 
     * @param percentage completion percentage (0-100)
     * @param currentTask description of current task
     */
    public void notifyProgress(int percentage, String currentTask) {
        if (observers.isEmpty()) return;
        
        ProgressEvent event = ProgressEvent.simple(percentage, currentTask, operationType);
        notifyObservers(observer -> observer.onProgressUpdate(event));
    }
    
    /**
     * Notifies all observers of a progress update with details.
     * 
     * @param percentage completion percentage (0-100)
     * @param currentTask description of current task
     * @param details additional details about the progress
     */
    public void notifyProgress(int percentage, String currentTask, Object details) {
        if (observers.isEmpty()) return;
        
        ProgressEvent event = ProgressEvent.builder()
            .percentage(percentage)
            .currentTask(currentTask)
            .operationType(operationType)
            .details(details)
            .build();
            
        notifyObservers(observer -> observer.onProgressUpdate(event));
    }
    
    /**
     * Notifies all observers of successful completion.
     * 
     * @param startTime when the operation started
     * @param result the result of the operation
     */
    public void notifyCompletion(long startTime, Object result) {
        if (observers.isEmpty()) return;
        
        CompletionEvent event = CompletionEvent.simple(operationType, startTime, result);
        notifyObservers(observer -> observer.onOperationComplete(event));
    }
    
    /**
     * Notifies all observers of successful completion with message.
     * 
     * @param startTime when the operation started
     * @param result the result of the operation
     * @param message completion message
     */
    public void notifyCompletion(long startTime, Object result, String message) {
        if (observers.isEmpty()) return;
        
        CompletionEvent event = CompletionEvent.builder()
            .operationType(operationType)
            .startTime(startTime)
            .result(result)
            .message(message)
            .build();
            
        notifyObservers(observer -> observer.onOperationComplete(event));
    }
    
    /**
     * Notifies all observers of an error.
     * 
     * @param errorMessage the error message
     */
    public void notifyError(String errorMessage) {
        if (observers.isEmpty()) return;
        
        ErrorEvent event = ErrorEvent.simple(operationType, errorMessage);
        notifyObservers(observer -> observer.onError(event));
    }
    
    /**
     * Notifies all observers of an error with cause.
     * 
     * @param errorMessage the error message
     * @param cause the underlying cause of the error
     */
    public void notifyError(String errorMessage, Throwable cause) {
        if (observers.isEmpty()) return;
        
        ErrorEvent event = ErrorEvent.withCause(operationType, errorMessage, cause);
        notifyObservers(observer -> observer.onError(event));
    }
    
    /**
     * Notifies all observers of an error with full details.
     * 
     * @param errorMessage the error message
     * @param cause the underlying cause
     * @param errorCode error code for categorization
     * @param recoverable whether the error is recoverable
     */
    public void notifyError(String errorMessage, Throwable cause, String errorCode, boolean recoverable) {
        if (observers.isEmpty()) return;
        
        ErrorEvent event = ErrorEvent.builder()
            .operationType(operationType)
            .errorMessage(errorMessage)
            .cause(cause)
            .errorCode(errorCode)
            .recoverable(recoverable)
            .build();
            
        notifyObservers(observer -> observer.onError(event));
    }
    
    private void notifyObservers(ObserverAction action) {
        for (ProgressObserver observer : observers) {
            try {
                action.execute(observer);
            } catch (Exception e) {
                // Observer should not break the notification process
                // In a real implementation, we might want to log this
                // but for now we silently continue
            }
        }
    }
    
    @FunctionalInterface
    private interface ObserverAction {
        void execute(ProgressObserver observer) throws Exception;
    }
}