package it.denzosoft.jreverse.core.pattern;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Configuration object for application settings.
 * Immutable configuration following Builder pattern.
 */
public final class ApplicationConfiguration {
    
    private final int maxMemoryMB;
    private final int analysisTimeoutSeconds;
    private final int reportTimeoutSeconds;
    private final boolean enableProgressReporting;
    private final boolean enableDetailedLogging;
    private final String tempDirectory;
    private final Map<String, String> customProperties;
    private final AnalysisSettings analysisSettings;
    private final ReportSettings reportSettings;
    
    private ApplicationConfiguration(Builder builder) {
        this.maxMemoryMB = validatePositive(builder.maxMemoryMB, "maxMemoryMB");
        this.analysisTimeoutSeconds = validatePositive(builder.analysisTimeoutSeconds, "analysisTimeoutSeconds");
        this.reportTimeoutSeconds = validatePositive(builder.reportTimeoutSeconds, "reportTimeoutSeconds");
        this.enableProgressReporting = builder.enableProgressReporting;
        this.enableDetailedLogging = builder.enableDetailedLogging;
        this.tempDirectory = builder.tempDirectory;
        this.customProperties = Collections.unmodifiableMap(new HashMap<>(builder.customProperties));
        this.analysisSettings = Objects.requireNonNull(builder.analysisSettings, "analysisSettings cannot be null");
        this.reportSettings = Objects.requireNonNull(builder.reportSettings, "reportSettings cannot be null");
    }
    
    public int getMaxMemoryMB() {
        return maxMemoryMB;
    }
    
    public int getAnalysisTimeoutSeconds() {
        return analysisTimeoutSeconds;
    }
    
    public int getReportTimeoutSeconds() {
        return reportTimeoutSeconds;
    }
    
    public boolean isProgressReportingEnabled() {
        return enableProgressReporting;
    }
    
    public boolean isDetailedLoggingEnabled() {
        return enableDetailedLogging;
    }
    
    public String getTempDirectory() {
        return tempDirectory;
    }
    
    public Map<String, String> getCustomProperties() {
        return customProperties;
    }
    
    public AnalysisSettings getAnalysisSettings() {
        return analysisSettings;
    }
    
    public ReportSettings getReportSettings() {
        return reportSettings;
    }
    
    public Optional<String> getCustomProperty(String key) {
        return Optional.ofNullable(customProperties.get(key));
    }
    
    public String getCustomProperty(String key, String defaultValue) {
        return customProperties.getOrDefault(key, defaultValue);
    }
    
    private int validatePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive, got: " + value);
        }
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ApplicationConfiguration that = (ApplicationConfiguration) obj;
        return maxMemoryMB == that.maxMemoryMB &&
               analysisTimeoutSeconds == that.analysisTimeoutSeconds &&
               reportTimeoutSeconds == that.reportTimeoutSeconds &&
               enableProgressReporting == that.enableProgressReporting &&
               enableDetailedLogging == that.enableDetailedLogging &&
               Objects.equals(tempDirectory, that.tempDirectory) &&
               Objects.equals(customProperties, that.customProperties) &&
               Objects.equals(analysisSettings, that.analysisSettings) &&
               Objects.equals(reportSettings, that.reportSettings);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(maxMemoryMB, analysisTimeoutSeconds, reportTimeoutSeconds,
                          enableProgressReporting, enableDetailedLogging, tempDirectory,
                          customProperties, analysisSettings, reportSettings);
    }
    
    @Override
    public String toString() {
        return "ApplicationConfiguration{" +
                "maxMemoryMB=" + maxMemoryMB +
                ", analysisTimeoutSeconds=" + analysisTimeoutSeconds +
                ", reportTimeoutSeconds=" + reportTimeoutSeconds +
                ", enableProgressReporting=" + enableProgressReporting +
                ", enableDetailedLogging=" + enableDetailedLogging +
                ", tempDirectory='" + tempDirectory + '\'' +
                ", customPropertiesCount=" + customProperties.size() +
                '}';
    }
    
    public static ApplicationConfiguration defaults() {
        return builder().build();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private int maxMemoryMB = 2048; // 2GB default
        private int analysisTimeoutSeconds = 300; // 5 minutes
        private int reportTimeoutSeconds = 120; // 2 minutes
        private boolean enableProgressReporting = true;
        private boolean enableDetailedLogging = false;
        private String tempDirectory = System.getProperty("java.io.tmpdir");
        private Map<String, String> customProperties = new HashMap<>();
        private AnalysisSettings analysisSettings = AnalysisSettings.defaults();
        private ReportSettings reportSettings = ReportSettings.defaults();
        
        public Builder maxMemoryMB(int maxMemoryMB) {
            this.maxMemoryMB = maxMemoryMB;
            return this;
        }
        
        public Builder analysisTimeoutSeconds(int analysisTimeoutSeconds) {
            this.analysisTimeoutSeconds = analysisTimeoutSeconds;
            return this;
        }
        
        public Builder reportTimeoutSeconds(int reportTimeoutSeconds) {
            this.reportTimeoutSeconds = reportTimeoutSeconds;
            return this;
        }
        
        public Builder enableProgressReporting(boolean enableProgressReporting) {
            this.enableProgressReporting = enableProgressReporting;
            return this;
        }
        
        public Builder enableDetailedLogging(boolean enableDetailedLogging) {
            this.enableDetailedLogging = enableDetailedLogging;
            return this;
        }
        
        public Builder tempDirectory(String tempDirectory) {
            this.tempDirectory = tempDirectory;
            return this;
        }
        
        public Builder addCustomProperty(String key, String value) {
            if (key != null && value != null) {
                this.customProperties.put(key, value);
            }
            return this;
        }
        
        public Builder customProperties(Map<String, String> customProperties) {
            this.customProperties = new HashMap<>(customProperties != null ? customProperties : Collections.emptyMap());
            return this;
        }
        
        public Builder analysisSettings(AnalysisSettings analysisSettings) {
            this.analysisSettings = analysisSettings;
            return this;
        }
        
        public Builder reportSettings(ReportSettings reportSettings) {
            this.reportSettings = reportSettings;
            return this;
        }
        
        public ApplicationConfiguration build() {
            return new ApplicationConfiguration(this);
        }
    }
    
    /**
     * Configuration settings specific to JAR analysis.
     */
    public static final class AnalysisSettings {
        private final boolean includeResources;
        private final boolean deepAnalysis;
        private final boolean analyzeSpringBootSpecifics;
        private final boolean extractSourceCode;
        
        private AnalysisSettings(boolean includeResources, boolean deepAnalysis, 
                               boolean analyzeSpringBootSpecifics, boolean extractSourceCode) {
            this.includeResources = includeResources;
            this.deepAnalysis = deepAnalysis;
            this.analyzeSpringBootSpecifics = analyzeSpringBootSpecifics;
            this.extractSourceCode = extractSourceCode;
        }
        
        public boolean isIncludeResources() {
            return includeResources;
        }
        
        public boolean isDeepAnalysis() {
            return deepAnalysis;
        }
        
        public boolean isAnalyzeSpringBootSpecifics() {
            return analyzeSpringBootSpecifics;
        }
        
        public boolean isExtractSourceCode() {
            return extractSourceCode;
        }
        
        public static AnalysisSettings defaults() {
            return new AnalysisSettings(true, true, true, false);
        }
        
        public static AnalysisSettings create(boolean includeResources, boolean deepAnalysis, 
                                            boolean analyzeSpringBootSpecifics, boolean extractSourceCode) {
            return new AnalysisSettings(includeResources, deepAnalysis, analyzeSpringBootSpecifics, extractSourceCode);
        }
    }
    
    /**
     * Configuration settings specific to report generation.
     */
    public static final class ReportSettings {
        private final boolean includeCharts;
        private final boolean includeDetails;
        private final int maxDetailLevel;
        private final String templatePath;
        
        private ReportSettings(boolean includeCharts, boolean includeDetails, 
                             int maxDetailLevel, String templatePath) {
            this.includeCharts = includeCharts;
            this.includeDetails = includeDetails;
            this.maxDetailLevel = maxDetailLevel;
            this.templatePath = templatePath;
        }
        
        public boolean isIncludeCharts() {
            return includeCharts;
        }
        
        public boolean isIncludeDetails() {
            return includeDetails;
        }
        
        public int getMaxDetailLevel() {
            return maxDetailLevel;
        }
        
        public String getTemplatePath() {
            return templatePath;
        }
        
        public static ReportSettings defaults() {
            return new ReportSettings(true, true, 3, null);
        }
        
        public static ReportSettings create(boolean includeCharts, boolean includeDetails, 
                                          int maxDetailLevel, String templatePath) {
            return new ReportSettings(includeCharts, includeDetails, maxDetailLevel, templatePath);
        }
    }
}