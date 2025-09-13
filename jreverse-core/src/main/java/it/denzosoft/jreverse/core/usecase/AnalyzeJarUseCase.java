package it.denzosoft.jreverse.core.usecase;

import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.JarLocation;
import it.denzosoft.jreverse.core.exception.JarAnalysisException;

/**
 * Use case for analyzing JAR files.
 * Follows Clean Architecture principles - contains business logic.
 */
public interface AnalyzeJarUseCase {
    
    /**
     * Executes JAR analysis for the given location.
     * 
     * @param request the analysis request containing JAR location and options
     * @return analysis result with JAR content and metadata
     * @throws JarAnalysisException if analysis fails
     */
    AnalysisResult execute(AnalysisRequest request) throws JarAnalysisException;
    
    /**
     * Request object for JAR analysis.
     */
    class AnalysisRequest {
        private final JarLocation jarLocation;
        private final AnalysisOptions options;
        
        public AnalysisRequest(JarLocation jarLocation, AnalysisOptions options) {
            this.jarLocation = jarLocation;
            this.options = options != null ? options : AnalysisOptions.defaults();
        }
        
        public JarLocation getJarLocation() {
            return jarLocation;
        }
        
        public AnalysisOptions getOptions() {
            return options;
        }
    }
    
    /**
     * Result object for JAR analysis.
     */
    class AnalysisResult {
        private final JarContent jarContent;
        private final AnalysisMetadata metadata;
        
        public AnalysisResult(JarContent jarContent, AnalysisMetadata metadata) {
            this.jarContent = jarContent;
            this.metadata = metadata;
        }
        
        public JarContent getJarContent() {
            return jarContent;
        }
        
        public AnalysisMetadata getMetadata() {
            return metadata;
        }
    }
    
    /**
     * Analysis options for customizing the analysis process.
     */
    class AnalysisOptions {
        private final boolean includeResources;
        private final boolean deepAnalysis;
        private final int maxMemoryMB;
        private final int timeoutSeconds;
        
        private AnalysisOptions(Builder builder) {
            this.includeResources = builder.includeResources;
            this.deepAnalysis = builder.deepAnalysis;
            this.maxMemoryMB = builder.maxMemoryMB;
            this.timeoutSeconds = builder.timeoutSeconds;
        }
        
        public boolean isIncludeResources() {
            return includeResources;
        }
        
        public boolean isDeepAnalysis() {
            return deepAnalysis;
        }
        
        public int getMaxMemoryMB() {
            return maxMemoryMB;
        }
        
        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }
        
        public static AnalysisOptions defaults() {
            return builder().build();
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private boolean includeResources = true;
            private boolean deepAnalysis = true;
            private int maxMemoryMB = 2048; // 2GB default
            private int timeoutSeconds = 300; // 5 minutes default
            
            public Builder includeResources(boolean includeResources) {
                this.includeResources = includeResources;
                return this;
            }
            
            public Builder deepAnalysis(boolean deepAnalysis) {
                this.deepAnalysis = deepAnalysis;
                return this;
            }
            
            public Builder maxMemoryMB(int maxMemoryMB) {
                this.maxMemoryMB = maxMemoryMB;
                return this;
            }
            
            public Builder timeoutSeconds(int timeoutSeconds) {
                this.timeoutSeconds = timeoutSeconds;
                return this;
            }
            
            public AnalysisOptions build() {
                return new AnalysisOptions(this);
            }
        }
    }
    
    /**
     * Metadata about the analysis process.
     */
    class AnalysisMetadata {
        private final long startTime;
        private final long endTime;
        private final String analyzerName;
        private final boolean successful;
        private final String errorMessage;
        
        private AnalysisMetadata(Builder builder) {
            this.startTime = builder.startTime;
            this.endTime = builder.endTime;
            this.analyzerName = builder.analyzerName;
            this.successful = builder.successful;
            this.errorMessage = builder.errorMessage;
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
        
        public String getAnalyzerName() {
            return analyzerName;
        }
        
        public boolean isSuccessful() {
            return successful;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public static AnalysisMetadata successful(String analyzerName, long startTime, long endTime) {
            return builder()
                .analyzerName(analyzerName)
                .startTime(startTime)
                .endTime(endTime)
                .successful(true)
                .build();
        }
        
        public static AnalysisMetadata failed(String analyzerName, long startTime, long endTime, String errorMessage) {
            return builder()
                .analyzerName(analyzerName)
                .startTime(startTime)
                .endTime(endTime)
                .successful(false)
                .errorMessage(errorMessage)
                .build();
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private long startTime = System.currentTimeMillis();
            private long endTime = System.currentTimeMillis();
            private String analyzerName;
            private boolean successful = true;
            private String errorMessage;
            
            public Builder startTime(long startTime) {
                this.startTime = startTime;
                return this;
            }
            
            public Builder endTime(long endTime) {
                this.endTime = endTime;
                return this;
            }
            
            public Builder analyzerName(String analyzerName) {
                this.analyzerName = analyzerName;
                return this;
            }
            
            public Builder successful(boolean successful) {
                this.successful = successful;
                return this;
            }
            
            public Builder errorMessage(String errorMessage) {
                this.errorMessage = errorMessage;
                return this;
            }
            
            public AnalysisMetadata build() {
                return new AnalysisMetadata(this);
            }
        }
    }
}