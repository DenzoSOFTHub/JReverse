package it.denzosoft.jreverse.core.usecase;

import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.port.ReportFormat;
import it.denzosoft.jreverse.core.port.ReportType;
import it.denzosoft.jreverse.core.exception.ReportGenerationException;

import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;

/**
 * Use case for generating reports from analyzed JAR content.
 * Follows Clean Architecture principles - contains business logic.
 */
public interface GenerateReportUseCase {
    
    /**
     * Executes report generation for the given request.
     * 
     * @param request the report generation request
     * @return report generation result with metadata
     * @throws ReportGenerationException if report generation fails
     */
    ReportResult execute(ReportRequest request) throws ReportGenerationException;
    
    /**
     * Request object for report generation.
     */
    class ReportRequest {
        private final JarContent jarContent;
        private final Set<ReportType> reportTypes;
        private final ReportFormat format;
        private final OutputStream output;
        private final ReportOptions options;
        
        public ReportRequest(JarContent jarContent, Set<ReportType> reportTypes, ReportFormat format, 
                           OutputStream output, ReportOptions options) {
            this.jarContent = jarContent;
            this.reportTypes = reportTypes != null ? reportTypes : Collections.emptySet();
            this.format = format;
            this.output = output;
            this.options = options != null ? options : ReportOptions.defaults();
        }
        
        public JarContent getJarContent() {
            return jarContent;
        }
        
        public Set<ReportType> getReportTypes() {
            return reportTypes;
        }
        
        public ReportFormat getFormat() {
            return format;
        }
        
        public OutputStream getOutput() {
            return output;
        }
        
        public ReportOptions getOptions() {
            return options;
        }
    }
    
    /**
     * Result object for report generation.
     */
    class ReportResult {
        private final boolean successful;
        private final int generatedReports;
        private final ReportMetadata metadata;
        
        public ReportResult(boolean successful, int generatedReports, ReportMetadata metadata) {
            this.successful = successful;
            this.generatedReports = generatedReports;
            this.metadata = metadata;
        }
        
        public boolean isSuccessful() {
            return successful;
        }
        
        public int getGeneratedReports() {
            return generatedReports;
        }
        
        public ReportMetadata getMetadata() {
            return metadata;
        }
    }
    
    /**
     * Report generation options.
     */
    class ReportOptions {
        private final boolean includeDetails;
        private final boolean includeCharts;
        private final boolean includeSourceCode;
        private final String templatePath;
        private final int maxDetailLevel;
        
        private ReportOptions(Builder builder) {
            this.includeDetails = builder.includeDetails;
            this.includeCharts = builder.includeCharts;
            this.includeSourceCode = builder.includeSourceCode;
            this.templatePath = builder.templatePath;
            this.maxDetailLevel = builder.maxDetailLevel;
        }
        
        public boolean isIncludeDetails() {
            return includeDetails;
        }
        
        public boolean isIncludeCharts() {
            return includeCharts;
        }
        
        public boolean isIncludeSourceCode() {
            return includeSourceCode;
        }
        
        public String getTemplatePath() {
            return templatePath;
        }
        
        public int getMaxDetailLevel() {
            return maxDetailLevel;
        }
        
        public static ReportOptions defaults() {
            return builder().build();
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private boolean includeDetails = true;
            private boolean includeCharts = true;
            private boolean includeSourceCode = false;
            private String templatePath;
            private int maxDetailLevel = 3;
            
            public Builder includeDetails(boolean includeDetails) {
                this.includeDetails = includeDetails;
                return this;
            }
            
            public Builder includeCharts(boolean includeCharts) {
                this.includeCharts = includeCharts;
                return this;
            }
            
            public Builder includeSourceCode(boolean includeSourceCode) {
                this.includeSourceCode = includeSourceCode;
                return this;
            }
            
            public Builder templatePath(String templatePath) {
                this.templatePath = templatePath;
                return this;
            }
            
            public Builder maxDetailLevel(int maxDetailLevel) {
                this.maxDetailLevel = maxDetailLevel;
                return this;
            }
            
            public ReportOptions build() {
                return new ReportOptions(this);
            }
        }
    }
    
    /**
     * Metadata about the report generation process.
     */
    class ReportMetadata {
        private final long startTime;
        private final long endTime;
        private final String generatorName;
        private final int totalReportTypes;
        private final int successfulReports;
        private final int failedReports;
        
        private ReportMetadata(Builder builder) {
            this.startTime = builder.startTime;
            this.endTime = builder.endTime;
            this.generatorName = builder.generatorName;
            this.totalReportTypes = builder.totalReportTypes;
            this.successfulReports = builder.successfulReports;
            this.failedReports = builder.failedReports;
        }
        
        public long getStartTime() {
            return startTime;
        }
        
        public long getEndTime() {
            return endTime;
        }
        
        public long getDurationMs() {
            return endTime - startTime;
        }
        
        public String getGeneratorName() {
            return generatorName;
        }
        
        public int getTotalReportTypes() {
            return totalReportTypes;
        }
        
        public int getSuccessfulReports() {
            return successfulReports;
        }
        
        public int getFailedReports() {
            return failedReports;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private long startTime = System.currentTimeMillis();
            private long endTime = System.currentTimeMillis();
            private String generatorName;
            private int totalReportTypes;
            private int successfulReports;
            private int failedReports;
            
            public Builder startTime(long startTime) {
                this.startTime = startTime;
                return this;
            }
            
            public Builder endTime(long endTime) {
                this.endTime = endTime;
                return this;
            }
            
            public Builder generatorName(String generatorName) {
                this.generatorName = generatorName;
                return this;
            }
            
            public Builder totalReportTypes(int totalReportTypes) {
                this.totalReportTypes = totalReportTypes;
                return this;
            }
            
            public Builder successfulReports(int successfulReports) {
                this.successfulReports = successfulReports;
                return this;
            }
            
            public Builder failedReports(int failedReports) {
                this.failedReports = failedReports;
                return this;
            }
            
            public ReportMetadata build() {
                return new ReportMetadata(this);
            }
        }
    }
}