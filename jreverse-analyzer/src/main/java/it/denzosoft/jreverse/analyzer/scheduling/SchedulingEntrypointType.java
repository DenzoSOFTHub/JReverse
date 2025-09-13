package it.denzosoft.jreverse.analyzer.scheduling;

/**
 * Enumeration of scheduling entrypoint types for Spring applications.
 * Categorizes different scheduling patterns and their usage.
 */
public enum SchedulingEntrypointType {
    
    SCHEDULED_METHOD("@Scheduled", "Scheduled method execution", true),
    ENABLE_SCHEDULING("@EnableScheduling", "Enable scheduling configuration", true),
    SCHEDULED_CONFIGURER("SchedulingConfigurer", "Custom scheduling configuration", false),
    TASK_SCHEDULER("TaskScheduler", "Direct task scheduler usage", false),
    CRON_TRIGGER("CronTrigger", "Cron-based trigger", false),
    FIXED_RATE("FixedRate", "Fixed rate scheduling", false),
    FIXED_DELAY("FixedDelay", "Fixed delay scheduling", false),
    INITIAL_DELAY("InitialDelay", "Initial delay configuration", false),
    ASYNC_SCHEDULED("AsyncScheduled", "Asynchronous scheduled task", true);
    
    private final String displayName;
    private final String description;
    private final boolean isAnnotationBased;
    
    SchedulingEntrypointType(String displayName, String description, boolean isAnnotationBased) {
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
     * Determines if this is a time-based scheduling type.
     */
    public boolean isTimeBased() {
        return this == CRON_TRIGGER || this == FIXED_RATE || this == FIXED_DELAY;
    }
    
    /**
     * Determines if this requires special scheduler configuration.
     */
    public boolean requiresScheduler() {
        return this == SCHEDULED_METHOD || this == TASK_SCHEDULER || this == ASYNC_SCHEDULED;
    }
    
    /**
     * Determines if this is a configuration type.
     */
    public boolean isConfiguration() {
        return this == ENABLE_SCHEDULING || this == SCHEDULED_CONFIGURER;
    }
}