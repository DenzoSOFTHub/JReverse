package it.denzosoft.jreverse.core.observer;

/**
 * Observer interface for progress notifications.
 * Follows Observer pattern for loose coupling.
 */
public interface ProgressObserver {
    
    /**
     * Called when progress is updated during analysis or report generation.
     * 
     * @param event the progress event with current status
     */
    void onProgressUpdate(ProgressEvent event);
    
    /**
     * Called when an operation completes successfully.
     * 
     * @param event the completion event with results
     */
    void onOperationComplete(CompletionEvent event);
    
    /**
     * Called when an error occurs during processing.
     * 
     * @param event the error event with exception details
     */
    void onError(ErrorEvent event);
}