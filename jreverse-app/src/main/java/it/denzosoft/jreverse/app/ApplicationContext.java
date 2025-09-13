package it.denzosoft.jreverse.app;

import it.denzosoft.jreverse.analyzer.factory.JavassistAnalyzerFactory;
import it.denzosoft.jreverse.analyzer.usecase.DefaultAnalyzeJarUseCase;
import it.denzosoft.jreverse.analyzer.usecase.DefaultGenerateReportUseCase;
import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.pattern.AnalyzerFactory;
import it.denzosoft.jreverse.core.pattern.ReportStrategy;
import it.denzosoft.jreverse.core.usecase.AnalyzeJarUseCase;
import it.denzosoft.jreverse.core.usecase.GenerateReportUseCase;
import it.denzosoft.jreverse.reporter.strategy.HtmlReportStrategy;

import java.util.Objects;

/**
 * Application context for dependency injection and configuration.
 * Follows Clean Architecture principles with proper dependency management.
 */
public class ApplicationContext {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(ApplicationContext.class);
    
    private final AnalyzerFactory analyzerFactory;
    private final ReportStrategy reportStrategy;
    private final AnalyzeJarUseCase analyzeJarUseCase;
    private final GenerateReportUseCase generateReportUseCase;
    
    public ApplicationContext() {
        LOGGER.info("Initializing application context");
        
        // Initialize core components
        this.analyzerFactory = new JavassistAnalyzerFactory();
        this.reportStrategy = new HtmlReportStrategy();
        
        // Initialize use cases with injected dependencies
        this.analyzeJarUseCase = createAnalyzeJarUseCase();
        this.generateReportUseCase = createGenerateReportUseCase();
        
        LOGGER.info("Application context initialized successfully");
    }
    
    // Package-visible constructor for testing
    ApplicationContext(AnalyzerFactory analyzerFactory, ReportStrategy reportStrategy) {
        this.analyzerFactory = Objects.requireNonNull(analyzerFactory, "analyzerFactory cannot be null");
        this.reportStrategy = Objects.requireNonNull(reportStrategy, "reportStrategy cannot be null");
        
        this.analyzeJarUseCase = createAnalyzeJarUseCase();
        this.generateReportUseCase = createGenerateReportUseCase();
        
        LOGGER.info("Application context initialized with custom dependencies");
    }
    
    public AnalyzerFactory getAnalyzerFactory() {
        return analyzerFactory;
    }
    
    public ReportStrategy getReportStrategy() {
        return reportStrategy;
    }
    
    public AnalyzeJarUseCase getAnalyzeJarUseCase() {
        return analyzeJarUseCase;
    }
    
    public GenerateReportUseCase getGenerateReportUseCase() {
        return generateReportUseCase;
    }
    
    /**
     * Shutdown the application context and clean up resources
     */
    public void shutdown() {
        LOGGER.info("Shutting down application context");
        
        // Clean up the AnalyzeJarUseCase if it implements cleanup
        if (analyzeJarUseCase instanceof DefaultAnalyzeJarUseCase) {
            ((DefaultAnalyzeJarUseCase) analyzeJarUseCase).shutdown();
        }
        
        LOGGER.info("Application context shutdown completed");
    }
    
    private AnalyzeJarUseCase createAnalyzeJarUseCase() {
        return new DefaultAnalyzeJarUseCase(analyzerFactory);
    }
    
    private GenerateReportUseCase createGenerateReportUseCase() {
        // Create use case with the configured report strategy
        // The DefaultGenerateReportUseCase will automatically register the strategy 
        // for all formats it supports
        return new DefaultGenerateReportUseCase(reportStrategy);
    }
}