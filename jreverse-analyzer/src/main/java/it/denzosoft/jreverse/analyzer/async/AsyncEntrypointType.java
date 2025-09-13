package it.denzosoft.jreverse.analyzer.async;

/**
 * Enumeration of async entrypoint types for Spring applications.
 * Categorizes different async patterns and their usage.
 */
public enum AsyncEntrypointType {
    
    ASYNC_METHOD("@Async", "Asynchronous method execution", true),
    ASYNC_RESULT("@AsyncResult", "Asynchronous result handling", false),
    ENABLE_ASYNC("@EnableAsync", "Enable async processing configuration", false),
    ASYNC_CONFIGURER("AsyncConfigurer", "Custom async configuration", false),
    COMPLETABLE_FUTURE("CompletableFuture", "CompletableFuture return type", false),
    DEFERRED_RESULT("DeferredResult", "Spring MVC deferred result", false),
    CALLABLE_CONTROLLER("Callable", "Spring MVC callable return", false),
    WEBFLUX_MONO("Mono", "Reactive Mono stream", false),
    WEBFLUX_FLUX("Flux", "Reactive Flux stream", false),
    TASK_EXECUTOR("TaskExecutor", "Direct task executor usage", false);
    
    private final String displayName;
    private final String description;
    private final boolean isAnnotationBased;
    
    AsyncEntrypointType(String displayName, String description, boolean isAnnotationBased) {
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
     * Determines if this is a reactive programming type.
     */
    public boolean isReactive() {
        return this == WEBFLUX_MONO || this == WEBFLUX_FLUX;
    }
    
    /**
     * Determines if this is a Spring MVC async type.
     */
    public boolean isMvcAsync() {
        return this == DEFERRED_RESULT || this == CALLABLE_CONTROLLER;
    }
    
    /**
     * Determines if this requires special executor configuration.
     */
    public boolean requiresExecutor() {
        return this == ASYNC_METHOD || this == TASK_EXECUTOR;
    }
}