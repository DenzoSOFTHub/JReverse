package it.denzosoft.jreverse.core.pattern;

import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.port.ReportFormat;
import it.denzosoft.jreverse.core.port.ReportType;
import it.denzosoft.jreverse.core.exception.ReportGenerationException;
import it.denzosoft.jreverse.core.observer.ProgressNotifier;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Context class for Strategy pattern implementation.
 * Manages multiple report strategies and selects the appropriate one.
 */
public class ReportContext {
    
    private final List<ReportStrategy> strategies = new CopyOnWriteArrayList<>();
    private final ProgressNotifier progressNotifier;
    
    public ReportContext() {
        this.progressNotifier = new ProgressNotifier("ReportGeneration");
    }
    
    /**
     * Adds a report strategy to the available strategies.
     * 
     * @param strategy the strategy to add
     */
    public void addStrategy(ReportStrategy strategy) {
        if (strategy != null && !strategies.contains(strategy)) {
            strategies.add(strategy);
            // Sort by priority (higher priority first)
            strategies.sort(Comparator.comparingInt(ReportStrategy::getPriority).reversed());
        }
    }
    
    /**
     * Removes a strategy from the available strategies.
     * 
     * @param strategy the strategy to remove
     */
    public void removeStrategy(ReportStrategy strategy) {
        strategies.remove(strategy);
    }
    
    /**
     * Gets all registered strategies.
     * 
     * @return list of strategies
     */
    public List<ReportStrategy> getStrategies() {
        return strategies;
    }
    
    /**
     * Gets the progress notifier for this context.
     * 
     * @return progress notifier
     */
    public ProgressNotifier getProgressNotifier() {
        return progressNotifier;
    }
    
    /**
     * Generates a report using the best available strategy.
     * 
     * @param jarContent the JAR content to analyze
     * @param reportType the type of report to generate
     * @param format the output format
     * @param output the output stream
     * @throws ReportGenerationException if no suitable strategy is found or generation fails
     */
    public void generateReport(JarContent jarContent, ReportType reportType, ReportFormat format, OutputStream output) 
        throws ReportGenerationException {
        
        ReportStrategy strategy = findBestStrategy(reportType, format);
        if (strategy == null) {
            throw new ReportGenerationException(
                "No strategy found for report type: " + reportType + " with format: " + format,
                reportType,
                format,
                ReportGenerationException.ErrorCode.UNSUPPORTED_REPORT_TYPE
            );
        }
        
        progressNotifier.notifyProgress(0, "Starting report generation with " + strategy.getStrategyName());
        
        try {
            strategy.generateReport(jarContent, reportType, format, output);
            progressNotifier.notifyProgress(100, "Report generation completed");
        } catch (ReportGenerationException e) {
            progressNotifier.notifyError("Report generation failed: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            progressNotifier.notifyError("Unexpected error during report generation", e);
            throw new ReportGenerationException(
                "Unexpected error during report generation: " + e.getMessage(),
                reportType,
                format,
                ReportGenerationException.ErrorCode.GENERATION_FAILED,
                e
            );
        }
    }
    
    /**
     * Finds the best strategy for the given report type and format.
     * 
     * @param reportType the report type
     * @param format the output format
     * @return the best strategy or null if none found
     */
    public ReportStrategy findBestStrategy(ReportType reportType, ReportFormat format) {
        return strategies.stream()
            .filter(strategy -> strategy.supports(reportType, format))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Gets all strategies that support the given report type.
     * 
     * @param reportType the report type
     * @return list of supporting strategies
     */
    public List<ReportStrategy> getStrategiesForReportType(ReportType reportType) {
        return strategies.stream()
            .filter(strategy -> Arrays.asList(strategy.getSupportedReportTypes()).contains(reportType))
            .toList();
    }
    
    /**
     * Gets all strategies that support the given format.
     * 
     * @param format the output format
     * @return list of supporting strategies
     */
    public List<ReportStrategy> getStrategiesForFormat(ReportFormat format) {
        return strategies.stream()
            .filter(strategy -> Arrays.asList(strategy.getSupportedFormats()).contains(format))
            .toList();
    }
    
    /**
     * Checks if any strategy supports the given report type and format.
     * 
     * @param reportType the report type
     * @param format the output format
     * @return true if supported
     */
    public boolean isSupported(ReportType reportType, ReportFormat format) {
        return findBestStrategy(reportType, format) != null;
    }
    
    /**
     * Gets the number of registered strategies.
     * 
     * @return number of strategies
     */
    public int getStrategyCount() {
        return strategies.size();
    }
    
    /**
     * Clears all registered strategies.
     */
    public void clearStrategies() {
        strategies.clear();
    }
}