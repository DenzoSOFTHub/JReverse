package it.denzosoft.jreverse.analyzer.usecase;

import it.denzosoft.jreverse.core.exception.ReportGenerationException;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.pattern.ReportStrategy;
import it.denzosoft.jreverse.core.port.ReportFormat;
import it.denzosoft.jreverse.core.port.ReportType;
import it.denzosoft.jreverse.core.usecase.GenerateReportUseCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation of GenerateReportUseCase.
 * Coordinates multiple report strategies to generate comprehensive reports.
 * Follows Clean Architecture principles with comprehensive error handling.
 */
public class DefaultGenerateReportUseCase implements GenerateReportUseCase {
    
    private static final Logger LOGGER = Logger.getLogger(DefaultGenerateReportUseCase.class.getName());
    
    private final Map<ReportFormat, ReportStrategy> strategies;
    private final ReportStrategy defaultStrategy;
    
    /**
     * Constructor with single strategy for all formats.
     */
    public DefaultGenerateReportUseCase(ReportStrategy defaultStrategy) {
        this.defaultStrategy = Objects.requireNonNull(defaultStrategy, "defaultStrategy cannot be null");
        this.strategies = new HashMap<>();
        LOGGER.info("DefaultGenerateReportUseCase initialized with single strategy: " + 
                   defaultStrategy.getStrategyName());
    }
    
    /**
     * Constructor with format-specific strategies.
     */
    public DefaultGenerateReportUseCase(Map<ReportFormat, ReportStrategy> strategies, 
                                       ReportStrategy defaultStrategy) {
        this.strategies = Objects.requireNonNull(strategies, "strategies cannot be null");
        this.defaultStrategy = Objects.requireNonNull(defaultStrategy, "defaultStrategy cannot be null");
        LOGGER.info("DefaultGenerateReportUseCase initialized with " + strategies.size() + 
                   " format-specific strategies and default: " + defaultStrategy.getStrategyName());
    }
    
    @Override
    public ReportResult execute(ReportRequest request) throws ReportGenerationException {
        Objects.requireNonNull(request, "request cannot be null");
        Objects.requireNonNull(request.getJarContent(), "jarContent cannot be null");
        Objects.requireNonNull(request.getReportTypes(), "reportTypes cannot be null");
        Objects.requireNonNull(request.getFormat(), "format cannot be null");
        Objects.requireNonNull(request.getOutput(), "output cannot be null");
        
        if (request.getReportTypes().isEmpty()) {
            throw new ReportGenerationException(
                "No report types specified", 
                null, 
                request.getFormat(), 
                ReportGenerationException.ErrorCode.INSUFFICIENT_DATA);
        }
        
        long startTime = System.currentTimeMillis();
        String generatorName = "Multi-Report Generator";
        int totalReports = request.getReportTypes().size();
        int successfulReports = 0;
        int failedReports = 0;
        
        LOGGER.info(String.format("Starting report generation for %d report types in %s format for JAR: %s", 
                   totalReports, request.getFormat(), request.getJarContent().getLocation().getFileName()));
        
        try {
            if (totalReports == 1) {
                // Single report - direct output
                ReportType reportType = request.getReportTypes().iterator().next();
                generateSingleReport(request.getJarContent(), reportType, request.getFormat(), 
                                   request.getOutput(), request.getOptions());
                successfulReports = 1;
                generatorName = getStrategyForFormat(request.getFormat()).getStrategyName();
                
            } else {
                // Multiple reports - combine them
                successfulReports = generateMultipleReports(request);
                failedReports = totalReports - successfulReports;
            }
            
            long endTime = System.currentTimeMillis();
            
            // Create successful result metadata
            ReportMetadata metadata = ReportMetadata.builder()
                .startTime(startTime)
                .endTime(endTime)
                .generatorName(generatorName)
                .totalReportTypes(totalReports)
                .successfulReports(successfulReports)
                .failedReports(failedReports)
                .build();
            
            ReportResult result = new ReportResult(
                failedReports == 0, // successful if no failures
                successfulReports,
                metadata
            );
            
            LOGGER.info(String.format("Report generation completed. Success: %d, Failed: %d, Duration: %dms", 
                       successfulReports, failedReports, metadata.getDurationMs()));
            
            return result;
            
        } catch (ReportGenerationException e) {
            long endTime = System.currentTimeMillis();
            LOGGER.severe("Report generation failed: " + e.getMessage());
            
            ReportMetadata metadata = ReportMetadata.builder()
                .startTime(startTime)
                .endTime(endTime)
                .generatorName(generatorName)
                .totalReportTypes(totalReports)
                .successfulReports(successfulReports)
                .failedReports(totalReports - successfulReports)
                .build();
            
            throw e; // Re-throw with original error information
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            String errorMessage = "Unexpected error during report generation: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMessage, e);
            
            ReportMetadata metadata = ReportMetadata.builder()
                .startTime(startTime)
                .endTime(endTime)
                .generatorName(generatorName)
                .totalReportTypes(totalReports)
                .successfulReports(successfulReports)
                .failedReports(totalReports - successfulReports)
                .build();
            
            throw new ReportGenerationException(
                errorMessage,
                null, // Unknown report type
                request.getFormat(),
                ReportGenerationException.ErrorCode.GENERATION_FAILED,
                e
            );
        }
    }
    
    private void generateSingleReport(JarContent jarContent, ReportType reportType, ReportFormat format, 
                                    OutputStream output, ReportOptions options) throws ReportGenerationException {
        
        ReportStrategy strategy = getStrategyForFormat(format);
        
        if (!strategy.supports(reportType, format)) {
            throw new ReportGenerationException(
                String.format("Strategy %s does not support %s in %s format", 
                             strategy.getStrategyName(), reportType, format),
                reportType,
                format,
                ReportGenerationException.ErrorCode.UNSUPPORTED_REPORT_TYPE
            );
        }
        
        // Apply report options if supported by strategy
        // For future enhancement: pass options to strategy when supported
        LOGGER.fine("Applying report options: " + options);
        
        strategy.generateReport(jarContent, reportType, format, output);
    }
    
    private int generateMultipleReports(ReportRequest request) throws ReportGenerationException {
        ReportFormat format = request.getFormat();
        int successfulReports = 0;
        
        try {
            switch (format) {
                case HTML:
                    successfulReports = generateCombinedHtmlReports(request);
                    break;
                case JSON:
                    successfulReports = generateCombinedJsonReports(request);
                    break;
                case XML:
                    successfulReports = generateCombinedXmlReports(request);
                    break;
                default:
                    successfulReports = generateConcatenatedReports(request);
            }
        } catch (IOException e) {
            throw new ReportGenerationException(
                "Failed to combine multiple reports: " + e.getMessage(),
                null,
                format,
                ReportGenerationException.ErrorCode.OUTPUT_ERROR,
                e
            );
        }
        
        return successfulReports;
    }
    
    private int generateCombinedHtmlReports(ReportRequest request) throws IOException, ReportGenerationException {
        int successful = 0;
        
        try (Writer writer = new OutputStreamWriter(request.getOutput(), StandardCharsets.UTF_8)) {
            // Write HTML document header
            writeHtmlDocumentStart(writer, request.getJarContent());
            
            // Generate each report and extract body content
            for (ReportType reportType : request.getReportTypes()) {
                try (ByteArrayOutputStream tempOutput = new ByteArrayOutputStream()) {
                    generateSingleReport(request.getJarContent(), reportType, request.getFormat(), 
                                       tempOutput, request.getOptions());
                    
                    String reportHtml = tempOutput.toString(StandardCharsets.UTF_8);
                    String bodyContent = extractHtmlBodyContent(reportHtml);
                    
                    writer.write("<section class=\"report-section\" data-report-type=\"" + reportType.name() + "\">\n");
                    writer.write("<h2>" + reportType.getDisplayName() + "</h2>\n");
                    writer.write(bodyContent);
                    writer.write("</section>\n\n");
                    
                    successful++;
                    LOGGER.fine("Successfully generated report: " + reportType.getDisplayName());
                    
                } catch (ReportGenerationException e) {
                    LOGGER.warning("Failed to generate report " + reportType + ": " + e.getMessage());
                    // Continue with other reports
                }
            }
            
            // Write HTML document footer
            writeHtmlDocumentEnd(writer);
            writer.flush();
        }
        
        return successful;
    }
    
    private int generateCombinedJsonReports(ReportRequest request) throws IOException, ReportGenerationException {
        int successful = 0;
        
        try (Writer writer = new OutputStreamWriter(request.getOutput(), StandardCharsets.UTF_8)) {
            writer.write("{\n");
            writer.write("  \"jarName\": \"" + escapeJson(request.getJarContent().getLocation().getFileName()) + "\",\n");
            writer.write("  \"generatedAt\": \"" + java.time.Instant.now().toString() + "\",\n");
            writer.write("  \"reports\": {\n");
            
            boolean first = true;
            for (ReportType reportType : request.getReportTypes()) {
                try (ByteArrayOutputStream tempOutput = new ByteArrayOutputStream()) {
                    generateSingleReport(request.getJarContent(), reportType, request.getFormat(), 
                                       tempOutput, request.getOptions());
                    
                    if (!first) writer.write(",\n");
                    first = false;
                    
                    writer.write("    \"" + reportType.name() + "\": ");
                    writer.write(tempOutput.toString(StandardCharsets.UTF_8));
                    
                    successful++;
                    
                } catch (ReportGenerationException e) {
                    LOGGER.warning("Failed to generate report " + reportType + ": " + e.getMessage());
                    // Continue with other reports
                }
            }
            
            writer.write("\n  }\n");
            writer.write("}\n");
            writer.flush();
        }
        
        return successful;
    }
    
    private int generateCombinedXmlReports(ReportRequest request) throws IOException, ReportGenerationException {
        int successful = 0;
        
        try (Writer writer = new OutputStreamWriter(request.getOutput(), StandardCharsets.UTF_8)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<multiReport>\n");
            writer.write("  <jarName>" + escapeXml(request.getJarContent().getLocation().getFileName()) + "</jarName>\n");
            writer.write("  <generatedAt>" + java.time.Instant.now().toString() + "</generatedAt>\n");
            writer.write("  <reports>\n");
            
            for (ReportType reportType : request.getReportTypes()) {
                try (ByteArrayOutputStream tempOutput = new ByteArrayOutputStream()) {
                    generateSingleReport(request.getJarContent(), reportType, request.getFormat(), 
                                       tempOutput, request.getOptions());
                    
                    writer.write("    <report type=\"" + reportType.name() + "\">\n");
                    writer.write("      <title>" + escapeXml(reportType.getDisplayName()) + "</title>\n");
                    writer.write("      <content><![CDATA[");
                    writer.write(tempOutput.toString(StandardCharsets.UTF_8));
                    writer.write("]]></content>\n");
                    writer.write("    </report>\n");
                    
                    successful++;
                    
                } catch (ReportGenerationException e) {
                    LOGGER.warning("Failed to generate report " + reportType + ": " + e.getMessage());
                    // Continue with other reports
                }
            }
            
            writer.write("  </reports>\n");
            writer.write("</multiReport>\n");
            writer.flush();
        }
        
        return successful;
    }
    
    private int generateConcatenatedReports(ReportRequest request) throws IOException, ReportGenerationException {
        int successful = 0;
        String separator = "\n\n" + "=".repeat(80) + "\n\n";
        
        for (ReportType reportType : request.getReportTypes()) {
            try {
                if (successful > 0) {
                    request.getOutput().write(separator.getBytes(StandardCharsets.UTF_8));
                }
                
                generateSingleReport(request.getJarContent(), reportType, request.getFormat(), 
                                   request.getOutput(), request.getOptions());
                successful++;
                
            } catch (ReportGenerationException e) {
                LOGGER.warning("Failed to generate report " + reportType + ": " + e.getMessage());
                // Continue with other reports
            }
        }
        
        return successful;
    }
    
    private ReportStrategy getStrategyForFormat(ReportFormat format) {
        return strategies.getOrDefault(format, defaultStrategy);
    }
    
    private void writeHtmlDocumentStart(Writer writer, JarContent jarContent) throws IOException {
        writer.write("<!DOCTYPE html>\n");
        writer.write("<html lang=\"en\">\n");
        writer.write("<head>\n");
        writer.write("    <meta charset=\"UTF-8\">\n");
        writer.write("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        writer.write("    <title>JReverse - Multi-Report Analysis</title>\n");
        writer.write("    <style>\n");
        writer.write("        body { font-family: Arial, sans-serif; margin: 20px; }\n");
        writer.write("        .report-section { margin: 2rem 0; border: 1px solid #ddd; padding: 1rem; }\n");
        writer.write("        .report-section h2 { color: #333; border-bottom: 2px solid #007bff; }\n");
        writer.write("    </style>\n");
        writer.write("</head>\n");
        writer.write("<body>\n");
        writer.write("    <h1>JReverse Analysis Report</h1>\n");
        writer.write("    <p>Analysis for: " + escapeHtml(jarContent.getLocation().getFileName()) + "</p>\n");
    }
    
    private void writeHtmlDocumentEnd(Writer writer) throws IOException {
        writer.write("</body>\n");
        writer.write("</html>\n");
    }
    
    private String extractHtmlBodyContent(String htmlDocument) {
        // Simple extraction - find content between <body> tags or use entire content if no body tags
        int bodyStart = htmlDocument.indexOf("<body");
        if (bodyStart >= 0) {
            bodyStart = htmlDocument.indexOf(">", bodyStart) + 1;
            int bodyEnd = htmlDocument.lastIndexOf("</body>");
            if (bodyEnd > bodyStart) {
                return htmlDocument.substring(bodyStart, bodyEnd).trim();
            }
        }
        // Return entire content if no body tags found (might be a fragment)
        return htmlDocument;
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }
    
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }
}