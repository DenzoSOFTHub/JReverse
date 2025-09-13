package it.denzosoft.jreverse.reporter.migration;

import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.port.ReportType;
import it.denzosoft.jreverse.core.exception.ReportGenerationException;
import it.denzosoft.jreverse.reporter.generator.ReportGeneratorFactory;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Manages backward compatibility for legacy report types and provides migration paths.
 */
public class BackwardCompatibilityManager {
    
    private static final Logger LOGGER = Logger.getLogger(BackwardCompatibilityManager.class.getName());
    
    // Mapping from legacy report types to their enhanced counterparts
    private static final Map<ReportType, ReportType> LEGACY_TO_ENHANCED_MAPPINGS = new HashMap<>();
    
    // Feature flags for gradual rollout
    private static final Map<String, Boolean> FEATURE_FLAGS = new HashMap<>();
    
    static {
        initializeLegacyMappings();
        initializeFeatureFlags();
    }
    
    private static void initializeLegacyMappings() {
        // Original reports that have enhanced versions
        LEGACY_TO_ENHANCED_MAPPINGS.put(ReportType.REST_CONTROLLERS_MAP, ReportType.REST_ENDPOINTS_MAP_ENHANCED);
        LEGACY_TO_ENHANCED_MAPPINGS.put(ReportType.AUTOWIRING_GRAPH, ReportType.COMPREHENSIVE_ENTRY_POINTS);
        LEGACY_TO_ENHANCED_MAPPINGS.put(ReportType.ASYNC_SEQUENCES, ReportType.ASYNC_SEQUENCES_COMPLETE);
        LEGACY_TO_ENHANCED_MAPPINGS.put(ReportType.SECURITY_ANNOTATIONS, ReportType.SECURITY_ANNOTATIONS_ENHANCED);
        LEGACY_TO_ENHANCED_MAPPINGS.put(ReportType.HTTP_CALL_GRAPH, ReportType.HTTP_CALL_GRAPH_ENHANCED);
        
        LOGGER.info("Initialized legacy mappings for " + LEGACY_TO_ENHANCED_MAPPINGS.size() + " report types");
    }
    
    private static void initializeFeatureFlags() {
        // Enhanced reports feature flags
        FEATURE_FLAGS.put("enhanced_rest_endpoints", true);
        FEATURE_FLAGS.put("comprehensive_entry_points", true);
        FEATURE_FLAGS.put("enhanced_security_annotations", true);
        FEATURE_FLAGS.put("enhanced_call_graph", true);
        FEATURE_FLAGS.put("complete_async_sequences", true);
        
        // New specialized reports feature flags
        FEATURE_FLAGS.put("scheduled_tasks_analysis", true);
        FEATURE_FLAGS.put("message_listeners_catalog", true);
        FEATURE_FLAGS.put("event_driven_architecture", true);
        FEATURE_FLAGS.put("async_operations_analysis", true);
        FEATURE_FLAGS.put("cross_entrypoint_security", true);
        
        // Cross-entrypoint correlation features
        FEATURE_FLAGS.put("entrypoint_correlations", false); // Not fully ready yet
        FEATURE_FLAGS.put("interactive_graphs", false);      // Requires additional libraries
        
        LOGGER.info("Initialized " + FEATURE_FLAGS.size() + " feature flags");
    }
    
    /**
     * Maps a legacy report type to its enhanced counterpart if available.
     */
    public static ReportType mapLegacyReportType(ReportType legacyType) {
        ReportType enhancedType = LEGACY_TO_ENHANCED_MAPPINGS.get(legacyType);
        
        if (enhancedType != null) {
            String featureFlag = getFeatureFlagForReportType(enhancedType);
            if (featureFlag != null && isFeatureEnabled(featureFlag)) {
                LOGGER.info(String.format("Mapping legacy report %s to enhanced %s", legacyType, enhancedType));
                return enhancedType;
            } else {
                LOGGER.info(String.format("Enhanced version %s not enabled, keeping legacy %s", enhancedType, legacyType));
                return legacyType;
            }
        }
        
        return legacyType;
    }
    
    /**
     * Checks if a report type is a legacy type that has been replaced.
     */
    public static boolean isLegacyReport(ReportType reportType) {
        return LEGACY_TO_ENHANCED_MAPPINGS.containsKey(reportType);
    }
    
    /**
     * Checks if a feature is enabled via feature flags.
     */
    public static boolean isFeatureEnabled(String featureName) {
        return FEATURE_FLAGS.getOrDefault(featureName, false);
    }
    
    /**
     * Gets the appropriate feature flag name for a report type.
     */
    private static String getFeatureFlagForReportType(ReportType reportType) {
        switch (reportType) {
            case REST_ENDPOINTS_MAP_ENHANCED:
                return "enhanced_rest_endpoints";
            case COMPREHENSIVE_ENTRY_POINTS:
                return "comprehensive_entry_points";
            case SECURITY_ANNOTATIONS_ENHANCED:
                return "enhanced_security_annotations";
            case HTTP_CALL_GRAPH_ENHANCED:
                return "enhanced_call_graph";
            case ASYNC_SEQUENCES_COMPLETE:
                return "complete_async_sequences";
            case SCHEDULED_TASKS_ANALYSIS:
                return "scheduled_tasks_analysis";
            case MESSAGE_LISTENERS_CATALOG:
                return "message_listeners_catalog";
            case EVENT_DRIVEN_ARCHITECTURE:
                return "event_driven_architecture";
            case ASYNC_OPERATIONS_ANALYSIS:
                return "async_operations_analysis";
            case CROSS_ENTRYPOINT_SECURITY:
                return "cross_entrypoint_security";
            default:
                return null;
        }
    }
    
    /**
     * Determines the best report type to use given the requested type and current feature flags.
     */
    public static ReportType getBestAvailableReportType(ReportType requestedType) {
        // First, check if this is a legacy type that should be mapped
        ReportType mappedType = mapLegacyReportType(requestedType);
        
        // If the mapped type is supported by the factory, use it
        if (ReportGeneratorFactory.isSupported(mappedType)) {
            return mappedType;
        }
        
        // Otherwise, fall back to the original requested type
        return requestedType;
    }
    
    /**
     * Provides migration guidance for a legacy report type.
     */
    public static MigrationGuidance getMigrationGuidance(ReportType legacyType) {
        ReportType enhancedType = LEGACY_TO_ENHANCED_MAPPINGS.get(legacyType);
        
        if (enhancedType == null) {
            return new MigrationGuidance(
                legacyType,
                null,
                MigrationStatus.NO_MIGRATION_NEEDED,
                "This report type is current and requires no migration."
            );
        }
        
        String featureFlag = getFeatureFlagForReportType(enhancedType);
        boolean isEnhancedEnabled = featureFlag != null && isFeatureEnabled(featureFlag);
        boolean isEnhancedSupported = ReportGeneratorFactory.isSupported(enhancedType);
        
        if (isEnhancedEnabled && isEnhancedSupported) {
            return new MigrationGuidance(
                legacyType,
                enhancedType,
                MigrationStatus.READY_TO_MIGRATE,
                String.format("Enhanced version %s is available and ready to use. " +
                    "It provides additional features including entrypoint correlations and enhanced visualizations.",
                    enhancedType.name())
            );
        } else if (isEnhancedSupported && !isEnhancedEnabled) {
            return new MigrationGuidance(
                legacyType,
                enhancedType,
                MigrationStatus.AVAILABLE_BUT_DISABLED,
                String.format("Enhanced version %s is available but currently disabled via feature flag '%s'. " +
                    "Contact your administrator to enable enhanced reporting features.",
                    enhancedType.name(), featureFlag)
            );
        } else {
            return new MigrationGuidance(
                legacyType,
                enhancedType,
                MigrationStatus.IN_DEVELOPMENT,
                String.format("Enhanced version %s is in development. " +
                    "Continue using the current version until the enhanced version becomes available.",
                    enhancedType.name())
            );
        }
    }
    
    /**
     * Updates a feature flag value (for testing or configuration purposes).
     */
    public static void setFeatureFlag(String featureName, boolean enabled) {
        FEATURE_FLAGS.put(featureName, enabled);
        LOGGER.info(String.format("Feature flag '%s' set to %s", featureName, enabled));
    }
    
    /**
     * Gets all current feature flag states.
     */
    public static Map<String, Boolean> getAllFeatureFlags() {
        return new HashMap<>(FEATURE_FLAGS);
    }
    
    /**
     * Provides detailed information about available report enhancements.
     */
    public static ReportEnhancementInfo getEnhancementInfo(ReportType reportType) {
        ReportType enhancedType = LEGACY_TO_ENHANCED_MAPPINGS.get(reportType);
        
        if (enhancedType == null) {
            return new ReportEnhancementInfo(reportType, false, null, null);
        }
        
        String[] newFeatures = getEnhancementFeatures(enhancedType);
        String migrationPath = getMigrationPath(reportType, enhancedType);
        
        return new ReportEnhancementInfo(reportType, true, newFeatures, migrationPath);
    }
    
    private static String[] getEnhancementFeatures(ReportType enhancedType) {
        switch (enhancedType) {
            case REST_ENDPOINTS_MAP_ENHANCED:
                return new String[]{
                    "Security annotations integration (@PreAuthorize, @PostAuthorize)",
                    "Correlation with scheduled tasks and message listeners",
                    "Enhanced security analysis and vulnerability detection",
                    "Interactive filtering and search capabilities",
                    "Cross-entrypoint relationship visualization"
                };
            case COMPREHENSIVE_ENTRY_POINTS:
                return new String[]{
                    "Unified view of all entry points (REST, Scheduled, Messaging, Events)",
                    "Cross-entrypoint correlations and dependencies",
                    "Security coverage across all entry point types",
                    "Performance impact analysis per entry point category",
                    "Interactive correlation highlighting"
                };
            case SECURITY_ANNOTATIONS_ENHANCED:
                return new String[]{
                    "Cross-entrypoint security analysis",
                    "Correlation between security annotations and other entry points",
                    "Security pattern consistency analysis across all entry point types",
                    "Advanced expression analysis for @PreAuthorize/@PostAuthorize",
                    "Security gap identification across application layers"
                };
            default:
                return new String[]{"Enhanced functionality and improved visualizations"};
        }
    }
    
    private static String getMigrationPath(ReportType legacyType, ReportType enhancedType) {
        return String.format(
            "To migrate from %s to %s:\n" +
            "1. Ensure all required analyzers are available\n" +
            "2. Enable the corresponding feature flag\n" +
            "3. Test the enhanced report with your JAR files\n" +
            "4. Update any automation scripts to use the new report type\n" +
            "5. Train users on the new features and visualizations",
            legacyType.name(), enhancedType.name()
        );
    }
    
    // Helper classes for migration information
    
    public static class MigrationGuidance {
        private final ReportType legacyType;
        private final ReportType enhancedType;
        private final MigrationStatus status;
        private final String guidance;
        
        public MigrationGuidance(ReportType legacyType, ReportType enhancedType, 
                               MigrationStatus status, String guidance) {
            this.legacyType = legacyType;
            this.enhancedType = enhancedType;
            this.status = status;
            this.guidance = guidance;
        }
        
        public ReportType getLegacyType() { return legacyType; }
        public ReportType getEnhancedType() { return enhancedType; }
        public MigrationStatus getStatus() { return status; }
        public String getGuidance() { return guidance; }
    }
    
    public static class ReportEnhancementInfo {
        private final ReportType originalType;
        private final boolean hasEnhancement;
        private final String[] newFeatures;
        private final String migrationPath;
        
        public ReportEnhancementInfo(ReportType originalType, boolean hasEnhancement, 
                                   String[] newFeatures, String migrationPath) {
            this.originalType = originalType;
            this.hasEnhancement = hasEnhancement;
            this.newFeatures = newFeatures;
            this.migrationPath = migrationPath;
        }
        
        public ReportType getOriginalType() { return originalType; }
        public boolean hasEnhancement() { return hasEnhancement; }
        public String[] getNewFeatures() { return newFeatures; }
        public String getMigrationPath() { return migrationPath; }
    }
    
    public enum MigrationStatus {
        NO_MIGRATION_NEEDED,
        READY_TO_MIGRATE,
        AVAILABLE_BUT_DISABLED,
        IN_DEVELOPMENT
    }
}