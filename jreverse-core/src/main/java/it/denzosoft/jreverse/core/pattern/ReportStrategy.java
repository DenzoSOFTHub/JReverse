package it.denzosoft.jreverse.core.pattern;

import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.port.ReportFormat;
import it.denzosoft.jreverse.core.port.ReportType;
import it.denzosoft.jreverse.core.exception.ReportGenerationException;

import java.io.OutputStream;

/**
 * Strategy interface for different report generation approaches.
 * Follows Strategy Pattern for flexible report generation.
 */
public interface ReportStrategy {
    
    /**
     * Generates a report using this strategy.
     * 
     * @param jarContent the analyzed JAR content
     * @param reportType the type of report to generate
     * @param format the output format
     * @param output the output stream
     * @throws ReportGenerationException if generation fails
     */
    void generateReport(JarContent jarContent, ReportType reportType, ReportFormat format, OutputStream output) 
        throws ReportGenerationException;
    
    /**
     * Checks if this strategy supports the given report type and format combination.
     * 
     * @param reportType the report type
     * @param format the output format
     * @return true if supported
     */
    boolean supports(ReportType reportType, ReportFormat format);
    
    /**
     * Gets the strategy name for identification.
     * 
     * @return strategy name
     */
    String getStrategyName();
    
    /**
     * Gets the supported report types by this strategy.
     * 
     * @return array of supported report types
     */
    ReportType[] getSupportedReportTypes();
    
    /**
     * Gets the supported output formats by this strategy.
     * 
     * @return array of supported formats
     */
    ReportFormat[] getSupportedFormats();
    
    /**
     * Gets the priority of this strategy (higher number = higher priority).
     * Used when multiple strategies support the same report type.
     * 
     * @return strategy priority
     */
    int getPriority();
}