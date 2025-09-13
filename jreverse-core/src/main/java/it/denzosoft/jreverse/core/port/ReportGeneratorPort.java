package it.denzosoft.jreverse.core.port;

import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.exception.ReportGenerationException;

import java.io.OutputStream;

/**
 * Output port for report generation functionality.
 * Follows Clean Architecture principles - defines contract for external adapters.
 */
public interface ReportGeneratorPort {
    
    /**
     * Generates a report from analyzed JAR content.
     * 
     * @param jarContent the analyzed JAR content
     * @param reportType the type of report to generate
     * @param output the output stream to write the report to
     * @throws ReportGenerationException if the report cannot be generated
     */
    void generateReport(JarContent jarContent, ReportType reportType, OutputStream output) 
        throws ReportGenerationException;
    
    /**
     * Checks if this generator supports the given report type.
     * 
     * @param reportType the report type to check
     * @return true if this generator can produce the report type
     */
    boolean supportsReportType(ReportType reportType);
    
    /**
     * Gets the supported output formats for this generator.
     * 
     * @return array of supported report formats
     */
    ReportFormat[] getSupportedFormats();
    
    /**
     * Gets the name of this report generator implementation.
     * 
     * @return generator name for identification purposes
     */
    String getGeneratorName();
}