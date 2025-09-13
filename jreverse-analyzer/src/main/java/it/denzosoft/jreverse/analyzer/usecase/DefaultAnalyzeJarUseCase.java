package it.denzosoft.jreverse.analyzer.usecase;

import it.denzosoft.jreverse.core.exception.JarAnalysisException;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.pattern.AnalyzerFactory;
import it.denzosoft.jreverse.core.port.JarAnalyzerPort;
import it.denzosoft.jreverse.core.usecase.AnalyzeJarUseCase;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation of AnalyzeJarUseCase using factory pattern.
 * Implements comprehensive error handling, memory management, and timeout support.
 */
public class DefaultAnalyzeJarUseCase implements AnalyzeJarUseCase {
    
    private static final Logger LOGGER = Logger.getLogger(DefaultAnalyzeJarUseCase.class.getName());
    
    private final AnalyzerFactory analyzerFactory;
    private final ExecutorService executorService;
    
    public DefaultAnalyzeJarUseCase(AnalyzerFactory analyzerFactory) {
        this.analyzerFactory = Objects.requireNonNull(analyzerFactory, "analyzerFactory cannot be null");
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "jar-analysis-thread");
            t.setDaemon(true);
            return t;
        });
        LOGGER.info("DefaultAnalyzeJarUseCase initialized with factory: " + analyzerFactory.getFactoryName());
    }
    
    @Override
    public AnalysisResult execute(AnalysisRequest request) throws JarAnalysisException {
        Objects.requireNonNull(request, "request cannot be null");
        Objects.requireNonNull(request.getJarLocation(), "jarLocation cannot be null");
        
        long startTime = System.currentTimeMillis();
        String analyzerName = "unknown";
        
        LOGGER.info("Starting JAR analysis for: " + request.getJarLocation().getPath());
        
        // Memory check before analysis
        checkMemoryLimits(request.getOptions());
        
        try {
            // Create appropriate analyzer using factory
            JarAnalyzerPort analyzer = analyzerFactory.createAnalyzer(request.getJarLocation());
            analyzerName = analyzer.getAnalyzerName();
            
            LOGGER.info("Selected analyzer: " + analyzerName);
            
            // Execute analysis with timeout
            JarContent jarContent = executeWithTimeout(analyzer, request);
            
            long endTime = System.currentTimeMillis();
            
            // Create successful result
            AnalysisMetadata metadata = AnalysisMetadata.successful(
                analyzerName, startTime, endTime);
            
            AnalysisResult result = new AnalysisResult(jarContent, metadata);
            
            LOGGER.info("Analysis completed successfully in " + metadata.getDurationMs() + "ms. " +
                       "Found " + jarContent.getClassCount() + " classes, " + 
                       jarContent.getResourceCount() + " resources");
            
            return result;
            
        } catch (TimeoutException e) {
            long endTime = System.currentTimeMillis();
            String errorMessage = "Analysis timeout after " + request.getOptions().getTimeoutSeconds() + " seconds";
            LOGGER.severe(errorMessage);
            
            AnalysisMetadata metadata = AnalysisMetadata.failed(
                analyzerName, startTime, endTime, errorMessage);
            
            throw new JarAnalysisException(
                errorMessage, 
                request.getJarLocation().getPath().toString(),
                JarAnalysisException.ErrorCode.TIMEOUT
            );
            
        } catch (OutOfMemoryError e) {
            long endTime = System.currentTimeMillis();
            String errorMessage = "Memory limit exceeded during analysis";
            LOGGER.severe(errorMessage + ": " + e.getMessage());
            
            // Attempt memory recovery
            attemptMemoryRecovery();
            
            AnalysisMetadata metadata = AnalysisMetadata.failed(
                analyzerName, startTime, endTime, errorMessage);
            
            throw new JarAnalysisException(
                errorMessage,
                request.getJarLocation().getPath().toString(),
                JarAnalysisException.ErrorCode.MEMORY_LIMIT_EXCEEDED,
                e
            );
            
        } catch (JarAnalysisException e) {
            long endTime = System.currentTimeMillis();
            LOGGER.severe("JAR analysis failed: " + e.getMessage());
            
            AnalysisMetadata metadata = AnalysisMetadata.failed(
                analyzerName, startTime, endTime, e.getMessage());
            
            // Re-throw with original error code
            throw e;
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            String errorMessage = "Unexpected error during analysis: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMessage, e);
            
            AnalysisMetadata metadata = AnalysisMetadata.failed(
                analyzerName, startTime, endTime, errorMessage);
            
            throw new JarAnalysisException(
                errorMessage,
                request.getJarLocation().getPath().toString(),
                JarAnalysisException.ErrorCode.ANALYSIS_FAILED,
                e
            );
        }
    }
    
    private JarContent executeWithTimeout(JarAnalyzerPort analyzer, AnalysisRequest request) 
            throws JarAnalysisException, TimeoutException {
        
        Future<JarContent> future = executorService.submit(() -> {
            try {
                return analyzer.analyzeJar(request.getJarLocation());
            } catch (JarAnalysisException e) {
                throw new RuntimeException(e);
            }
        });
        
        try {
            return future.get(request.getOptions().getTimeoutSeconds(), TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException && cause.getCause() instanceof JarAnalysisException) {
                throw (JarAnalysisException) cause.getCause();
            }
            throw new JarAnalysisException(
                "Analysis execution failed: " + cause.getMessage(),
                request.getJarLocation().getPath().toString(),
                JarAnalysisException.ErrorCode.ANALYSIS_FAILED,
                cause
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JarAnalysisException(
                "Analysis was interrupted",
                request.getJarLocation().getPath().toString(),
                JarAnalysisException.ErrorCode.ANALYSIS_FAILED,
                e
            );
        } catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        }
    }
    
    private void checkMemoryLimits(AnalysisOptions options) throws JarAnalysisException {
        Runtime runtime = Runtime.getRuntime();
        long maxMemoryMB = runtime.maxMemory() / (1024 * 1024);
        long usedMemoryMB = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        
        LOGGER.fine("Memory status: " + usedMemoryMB + "MB used / " + maxMemoryMB + "MB max");
        
        // Check if requested memory limit exceeds JVM limits
        if (options.getMaxMemoryMB() > maxMemoryMB) {
            String errorMessage = "Requested memory limit (" + options.getMaxMemoryMB() + 
                                 "MB) exceeds JVM maximum (" + maxMemoryMB + "MB)";
            LOGGER.warning(errorMessage);
            // Don't throw, just warn - use JVM limits instead
        }
        
        // Check current memory usage
        long availableMemoryMB = maxMemoryMB - usedMemoryMB;
        if (availableMemoryMB < (options.getMaxMemoryMB() * 0.1)) { // Less than 10% of requested memory available
            String errorMessage = "Insufficient memory available for analysis. " +
                                 "Available: " + availableMemoryMB + "MB, Required: " + options.getMaxMemoryMB() + "MB";
            LOGGER.warning(errorMessage);
        }
    }
    
    private void attemptMemoryRecovery() {
        LOGGER.info("Attempting memory recovery...");
        try {
            // Aggressive garbage collection
            System.gc();
            System.runFinalization();
            System.gc();
            
            // Brief pause to allow GC to complete
            Thread.sleep(100);
            
            Runtime runtime = Runtime.getRuntime();
            long recoveredMemoryMB = runtime.freeMemory() / (1024 * 1024);
            LOGGER.info("Memory recovery completed. Free memory: " + recoveredMemoryMB + "MB");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warning("Memory recovery interrupted");
        }
    }
    
    /**
     * Shuts down the executor service. Should be called when the use case is no longer needed.
     */
    public void shutdown() {
        LOGGER.info("Shutting down AnalyzeJarUseCase executor");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    LOGGER.warning("Executor did not terminate gracefully");
                }
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}