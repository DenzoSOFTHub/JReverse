package it.denzosoft.jreverse.reporter.strategy;

import it.denzosoft.jreverse.analyzer.bootstrap.BootstrapAnalysisResult;
import it.denzosoft.jreverse.analyzer.bootstrap.BootstrapAnalyzer;
import it.denzosoft.jreverse.analyzer.bootstrap.BootstrapSequenceGenerator;
import it.denzosoft.jreverse.analyzer.bootstrap.BootstrapSequencePhase;
import it.denzosoft.jreverse.analyzer.bootstrap.BootstrapSequenceStep;
import it.denzosoft.jreverse.analyzer.factory.SpecializedAnalyzerFactory;
import it.denzosoft.jreverse.core.exception.ReportGenerationException;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.pattern.ReportStrategy;
import it.denzosoft.jreverse.core.port.ReportFormat;
import it.denzosoft.jreverse.core.port.ReportType;
import it.denzosoft.jreverse.reporter.generator.AbstractReportGenerator;
import it.denzosoft.jreverse.reporter.generator.ReportGeneratorFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * HTML Report Strategy implementation.
 * Generates comprehensive HTML reports with CSS styling and interactive elements.
 */
public class HtmlReportStrategy implements ReportStrategy {
    
    private static final Logger LOGGER = Logger.getLogger(HtmlReportStrategy.class.getName());
    
    private static final String STRATEGY_NAME = "HTML Report Generator";
    private static final int STRATEGY_PRIORITY = 10;
    
    private static final ReportType[] SUPPORTED_REPORT_TYPES = ReportGeneratorFactory.getSupportedReportTypes();
    
    private static final ReportFormat[] SUPPORTED_FORMATS = {
        ReportFormat.HTML
    };
    
    @Override
    public void generateReport(JarContent jarContent, ReportType reportType, ReportFormat format, OutputStream output) 
            throws ReportGenerationException {
        
        Objects.requireNonNull(jarContent, "jarContent cannot be null");
        Objects.requireNonNull(reportType, "reportType cannot be null");
        Objects.requireNonNull(format, "format cannot be null");
        Objects.requireNonNull(output, "output cannot be null");
        
        if (!supports(reportType, format)) {
            throw new ReportGenerationException(
                String.format("Unsupported combination: %s with %s", reportType, format),
                reportType, 
                format, 
                ReportGenerationException.ErrorCode.UNSUPPORTED_REPORT_TYPE);
        }
        
        LOGGER.info(String.format("Generating %s report in %s format for JAR: %s", 
                   reportType, format, jarContent.getLocation().getFileName()));
        
        try {
            // Use new modular system if generator is available
            if (ReportGeneratorFactory.isSupported(reportType)) {
                AbstractReportGenerator generator = ReportGeneratorFactory.create(reportType);
                Map<String, Object> analysisResults = gatherAnalysisResults(jarContent, generator);
                generator.generate(jarContent, output, analysisResults);
            } else {
                // Fallback to legacy implementation for unsupported reports
                try (Writer writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
                    generateLegacyHtmlReport(jarContent, reportType, writer);
                    writer.flush();
                }
            }
            
            LOGGER.info("Report generation completed successfully");
        } catch (IOException e) {
            throw new ReportGenerationException(
                "Failed to write HTML report", 
                reportType, 
                format, 
                ReportGenerationException.ErrorCode.OUTPUT_ERROR, 
                e);
        }
    }
    
    @Override
    public boolean supports(ReportType reportType, ReportFormat format) {
        return format == ReportFormat.HTML && 
               Arrays.asList(SUPPORTED_REPORT_TYPES).contains(reportType);
    }
    
    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }
    
    @Override
    public ReportType[] getSupportedReportTypes() {
        return SUPPORTED_REPORT_TYPES.clone();
    }
    
    @Override
    public ReportFormat[] getSupportedFormats() {
        return SUPPORTED_FORMATS.clone();
    }
    
    @Override
    public int getPriority() {
        return STRATEGY_PRIORITY;
    }
    
    /**
     * Gathers analysis results needed for the report generator.
     */
    private Map<String, Object> gatherAnalysisResults(JarContent jarContent, AbstractReportGenerator generator) {
        Map<String, Object> results = new HashMap<>();
        
        // Always include basic class analysis
        // results.put("classAnalysis", basicAnalyzer.analyze(jarContent));
        
        try {
            // Gather analysis results based on generator requirements
            if (generator.requiresSchedulingAnalysis()) {
                // results.put("schedulingAnalysis", schedulingAnalyzer.analyze(jarContent));
                LOGGER.info("Scheduling analysis would be performed here");
            }
            
            if (generator.requiresAsyncAnalysis()) {
                // results.put("asyncAnalysis", asyncAnalyzer.analyze(jarContent));
                LOGGER.info("Async analysis would be performed here");
            }
            
            if (generator.requiresMessagingAnalysis()) {
                // results.put("messagingAnalysis", messagingAnalyzer.analyze(jarContent));
                LOGGER.info("Messaging analysis would be performed here");
            }
            
            if (generator.requiresSecurityAnalysis()) {
                // results.put("securityAnalysis", securityAnalyzer.analyze(jarContent));
                LOGGER.info("Security analysis would be performed here");
            }
            
        } catch (Exception e) {
            LOGGER.warning("Failed to gather some analysis results: " + e.getMessage());
        }
        
        return results;
    }
    
    /**
     * Legacy HTML report generation method for backward compatibility.
     */
    private void generateLegacyHtmlReport(JarContent jarContent, ReportType reportType, Writer writer) throws IOException {
        writeHtmlHeader(writer, reportType, jarContent);
        writeHtmlBody(writer, reportType, jarContent);
        writeHtmlFooter(writer);
    }
    
    private void writeHtmlHeader(Writer writer, ReportType reportType, JarContent jarContent) throws IOException {
        writer.write("<!DOCTYPE html>\n");
        writer.write("<html lang=\"en\">\n");
        writer.write("<head>\n");
        writer.write("    <meta charset=\"UTF-8\">\n");
        writer.write("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        writer.write("    <title>JReverse - " + reportType.getDisplayName() + "</title>\n");
        writer.write("    <style>\n");
        writer.write(getEmbeddedCss());
        writer.write("    </style>\n");
        writer.write("</head>\n");
    }
    
    private void writeHtmlBody(Writer writer, ReportType reportType, JarContent jarContent) throws IOException {
        writer.write("<body>\n");
        writer.write("    <header class=\"main-header\">\n");
        writer.write("        <h1>JReverse Analysis Report</h1>\n");
        writer.write("        <div class=\"report-info\">\n");
        writer.write("            <span class=\"report-type\">" + reportType.getDisplayName() + "</span>\n");
        writer.write("            <span class=\"jar-name\">" + jarContent.getLocation().getFileName() + "</span>\n");
        writer.write("            <span class=\"generation-time\">" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</span>\n");
        writer.write("        </div>\n");
        writer.write("    </header>\n");
        
        writer.write("    <main class=\"content\">\n");
        
        switch (reportType) {
            case PACKAGE_CLASS_MAP:
                writeArchitectureOverview(writer, jarContent);
                break;
            case UML_CLASS_DIAGRAM:
                writeClassSummary(writer, jarContent);
                break;
            case PACKAGE_DEPENDENCIES:
                writeDependencyAnalysis(writer, jarContent);
                break;
            case MODULE_DEPENDENCIES:
                writePackageStructure(writer, jarContent);
                break;
            case BOOTSTRAP_ANALYSIS:
                writeBootstrapAnalysis(writer, jarContent);
                break;
            default:
                writeDefaultReport(writer, jarContent);
        }
        
        writer.write("    </main>\n");
        writer.write("</body>\n");
    }
    
    private void writeHtmlFooter(Writer writer) throws IOException {
        writer.write("    <footer class=\"main-footer\">\n");
        writer.write("        <p>Generated by JReverse v1.0.0 - Java Reverse Engineering Tool</p>\n");
        writer.write("    </footer>\n");
        writer.write("</html>\n");
    }
    
    private void writeArchitectureOverview(Writer writer, JarContent jarContent) throws IOException {
        writer.write("        <section class=\"architecture-section\">\n");
        writer.write("            <h2>Architecture Overview</h2>\n");
        writer.write("            <div class=\"stats-grid\">\n");
        writer.write("                <div class=\"stat-card\">\n");
        writer.write("                    <h3>Total Classes</h3>\n");
        writer.write("                    <span class=\"stat-number\">" + jarContent.getClasses().size() + "</span>\n");
        writer.write("                </div>\n");
        writer.write("                <div class=\"stat-card\">\n");
        writer.write("                    <h3>JAR Type</h3>\n");
        writer.write("                    <span class=\"stat-text\">" + jarContent.getJarType().getDisplayName() + "</span>\n");
        writer.write("                </div>\n");
        writer.write("            </div>\n");
        writer.write("        </section>\n");
    }
    
    private void writeClassSummary(Writer writer, JarContent jarContent) throws IOException {
        writer.write("        <section class=\"class-summary-section\">\n");
        writer.write("            <h2>Class Summary</h2>\n");
        writer.write("            <div class=\"class-list\">\n");
        
        jarContent.getClasses().forEach(classInfo -> {
            try {
                writer.write("                <div class=\"class-item\">\n");
                writer.write("                    <h3>" + classInfo.getFullyQualifiedName() + "</h3>\n");
                writer.write("                    <p>Type: " + classInfo.getClassType().name() + "</p>\n");
                writer.write("                    <p>Methods: " + classInfo.getMethods().size() + "</p>\n");
                writer.write("                    <p>Fields: " + classInfo.getFields().size() + "</p>\n");
                writer.write("                </div>\n");
            } catch (IOException e) {
                LOGGER.warning("Failed to write class info: " + e.getMessage());
            }
        });
        
        writer.write("            </div>\n");
        writer.write("        </section>\n");
    }
    
    private void writeDependencyAnalysis(Writer writer, JarContent jarContent) throws IOException {
        writer.write("        <section class=\"dependency-section\">\n");
        writer.write("            <h2>Dependency Analysis</h2>\n");
        writer.write("            <p>Analyzing dependencies for " + jarContent.getClasses().size() + " classes...</p>\n");
        writer.write("        </section>\n");
    }
    
    private void writePackageStructure(Writer writer, JarContent jarContent) throws IOException {
        writer.write("        <section class=\"package-section\">\n");
        writer.write("            <h2>Package Structure</h2>\n");
        writer.write("            <div class=\"package-tree\">\n");
        writer.write("                <p>Package analysis for JAR: " + jarContent.getLocation().getFileName() + "</p>\n");
        writer.write("            </div>\n");
        writer.write("        </section>\n");
    }
    
    private void writeBootstrapAnalysis(Writer writer, JarContent jarContent) throws IOException {
        writer.write("        <section class=\"bootstrap-analysis-section\">\n");
        writer.write("            <h2>Spring Boot Bootstrap Analysis</h2>\n");
        
        try {
            // Perform bootstrap analysis
            BootstrapAnalyzer bootstrapAnalyzer = SpecializedAnalyzerFactory.createBootstrapAnalyzer();
            BootstrapAnalysisResult analysisResult = bootstrapAnalyzer.analyzeBootstrap(jarContent);
            
            // Write analysis summary
            writeBootstrapSummary(writer, analysisResult);
            
            // Write sequence diagram
            writeBootstrapSequenceDiagram(writer, analysisResult);
            
            // Write phase breakdown
            writeBootstrapPhaseBreakdown(writer, analysisResult);
            
            // Write discovered components
            writeBootstrapComponents(writer, analysisResult);
            
        } catch (Exception e) {
            LOGGER.warning("Error during bootstrap analysis: " + e.getMessage());
            writer.write("            <div class=\"error-message\">\n");
            writer.write("                <h3>Analysis Error</h3>\n");
            writer.write("                <p>Unable to analyze bootstrap sequence: " + e.getMessage() + "</p>\n");
            writer.write("            </div>\n");
        }
        
        writer.write("        </section>\n");
    }
    
    private void writeBootstrapSummary(Writer writer, BootstrapAnalysisResult analysisResult) throws IOException {
        writer.write("            <div class=\"bootstrap-summary\">\n");
        writer.write("                <h3>Bootstrap Summary</h3>\n");
        writer.write("                <div class=\"stats-grid\">\n");
        
        writer.write("                    <div class=\"stat-card\">\n");
        writer.write("                        <h4>Application Type</h4>\n");
        writer.write("                        <span class=\"stat-text\">" + analysisResult.getAnalysisType() + "</span>\n");
        writer.write("                    </div>\n");
        
        writer.write("                    <div class=\"stat-card\">\n");
        writer.write("                        <h4>Sequence Steps</h4>\n");
        writer.write("                        <span class=\"stat-number\">" + analysisResult.getTotalSteps() + "</span>\n");
        writer.write("                    </div>\n");
        
        writer.write("                    <div class=\"stat-card\">\n");
        writer.write("                        <h4>Components Found</h4>\n");
        writer.write("                        <span class=\"stat-number\">" + analysisResult.getComponentCount() + "</span>\n");
        writer.write("                    </div>\n");
        
        if (analysisResult.getTimingInfo().hasTimingData()) {
            writer.write("                    <div class=\"stat-card\">\n");
            writer.write("                        <h4>Estimated Time</h4>\n");
            writer.write("                        <span class=\"stat-text\">" + analysisResult.getTimingInfo().getFormattedTotalDuration() + "</span>\n");
            writer.write("                    </div>\n");
        }
        
        writer.write("                </div>\n");
        writer.write("            </div>\n");
    }
    
    private void writeBootstrapSequenceDiagram(Writer writer, BootstrapAnalysisResult analysisResult) throws IOException {
        writer.write("            <div class=\"sequence-diagram-section\">\n");
        writer.write("                <h3>Bootstrap Sequence Diagram</h3>\n");
        
        if (analysisResult.getTotalSteps() > 0) {
            BootstrapSequenceGenerator generator = new BootstrapSequenceGenerator();
            
            // Generate PlantUML diagram for web rendering (could be enhanced with actual PlantUML rendering)
            String textDiagram = generator.generateSequenceDiagram(analysisResult, BootstrapAnalyzer.SequenceDiagramFormat.TEXT);
            
            writer.write("                <div class=\"diagram-container\">\n");
            writer.write("                    <pre class=\"sequence-diagram\">" + escapeHtml(textDiagram) + "</pre>\n");
            writer.write("                </div>\n");
            
            // Add PlantUML source for copy-paste
            String plantUmlDiagram = generator.generateSequenceDiagram(analysisResult, BootstrapAnalyzer.SequenceDiagramFormat.PLANTUML);
            writer.write("                <div class=\"diagram-source\">\n");
            writer.write("                    <h4>PlantUML Source</h4>\n");
            writer.write("                    <textarea readonly class=\"plantuml-source\">" + escapeHtml(plantUmlDiagram) + "</textarea>\n");
            writer.write("                    <p class=\"diagram-note\">Copy this PlantUML code to render the sequence diagram in your preferred PlantUML renderer.</p>\n");
            writer.write("                </div>\n");
        } else {
            writer.write("                <div class=\"no-sequence\">\n");
            writer.write("                    <p>No bootstrap sequence detected in this application.</p>\n");
            writer.write("                </div>\n");
        }
        
        writer.write("            </div>\n");
    }
    
    private void writeBootstrapPhaseBreakdown(Writer writer, BootstrapAnalysisResult analysisResult) throws IOException {
        writer.write("            <div class=\"phase-breakdown-section\">\n");
        writer.write("                <h3>Phase Breakdown</h3>\n");
        
        if (analysisResult.getTotalSteps() > 0) {
            for (BootstrapSequencePhase phase : BootstrapSequencePhase.values()) {
                List<BootstrapSequenceStep> phaseSteps = analysisResult.getStepsForPhase(phase);
                if (!phaseSteps.isEmpty()) {
                    writer.write("                <div class=\"phase-card\">\n");
                    writer.write("                    <h4>" + phase.getDisplayName() + "</h4>\n");
                    writer.write("                    <p class=\"phase-description\">" + phase.getDescription() + "</p>\n");
                    writer.write("                    <div class=\"phase-stats\">\n");
                    writer.write("                        <span class=\"phase-step-count\">" + phaseSteps.size() + " steps</span>\n");
                    
                    if (analysisResult.getTimingInfo().hasTimingData()) {
                        writer.write("                        <span class=\"phase-duration\">" + 
                            analysisResult.getTimingInfo().getFormattedPhaseDuration(phase) + "</span>\n");
                    }
                    
                    writer.write("                    </div>\n");
                    writer.write("                    <div class=\"phase-steps\">\n");
                    
                    for (BootstrapSequenceStep step : phaseSteps) {
                        writer.write("                        <div class=\"step-item\">\n");
                        writer.write("                            <span class=\"step-number\">" + step.getSequenceNumber() + "</span>\n");
                        writer.write("                            <span class=\"step-class\">" + step.getShortClassName() + "</span>\n");
                        writer.write("                            <span class=\"step-method\">" + step.getFormattedMethodSignature() + "</span>\n");
                        writer.write("                        </div>\n");
                    }
                    
                    writer.write("                    </div>\n");
                    writer.write("                </div>\n");
                }
            }
        } else {
            writer.write("                <p>No phases detected.</p>\n");
        }
        
        writer.write("            </div>\n");
    }
    
    private void writeBootstrapComponents(Writer writer, BootstrapAnalysisResult analysisResult) throws IOException {
        writer.write("            <div class=\"components-section\">\n");
        writer.write("                <h3>Discovered Components</h3>\n");
        
        if (analysisResult.getComponentCount() > 0) {
            writer.write("                <div class=\"components-list\">\n");
            for (String component : analysisResult.getDiscoveredComponents()) {
                writer.write("                    <div class=\"component-item\">\n");
                writer.write("                        <span class=\"component-name\">" + component + "</span>\n");
                writer.write("                    </div>\n");
            }
            writer.write("                </div>\n");
        } else {
            writer.write("                <p>No Spring components found.</p>\n");
        }
        
        if (analysisResult.getAutoConfigurationCount() > 0) {
            writer.write("                <div class=\"autoconfig-section\">\n");
            writer.write("                    <h4>Auto-Configurations (" + analysisResult.getAutoConfigurationCount() + ")</h4>\n");
            writer.write("                    <div class=\"autoconfig-list\">\n");
            for (String autoConfig : analysisResult.getDetectedAutoConfigurations()) {
                writer.write("                        <div class=\"autoconfig-item\">\n");
                writer.write("                            <span class=\"autoconfig-name\">" + autoConfig + "</span>\n");
                writer.write("                        </div>\n");
            }
            writer.write("                    </div>\n");
            writer.write("                </div>\n");
        }
        
        writer.write("            </div>\n");
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }
    
    private void writeDefaultReport(Writer writer, JarContent jarContent) throws IOException {
        writer.write("        <section class=\"default-section\">\n");
        writer.write("            <h2>Report Content</h2>\n");
        writer.write("            <p>This report type is not yet fully implemented.</p>\n");
        writer.write("            <p>JAR analyzed: " + jarContent.getLocation().getFileName() + "</p>\n");
        writer.write("        </section>\n");
    }
    
    private String getEmbeddedCss() {
        return "body {\n" +
               "    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
               "    margin: 0;\n" +
               "    padding: 0;\n" +
               "    background-color: #f5f5f5;\n" +
               "    color: #333;\n" +
               "    line-height: 1.6;\n" +
               "}\n" +
               "\n" +
               ".main-header {\n" +
               "    background: linear-gradient(135deg, #2c3e50, #3498db);\n" +
               "    color: white;\n" +
               "    padding: 2rem;\n" +
               "    text-align: center;\n" +
               "    box-shadow: 0 2px 10px rgba(0,0,0,0.1);\n" +
               "}\n" +
               "\n" +
               ".main-header h1 {\n" +
               "    margin: 0 0 1rem 0;\n" +
               "    font-size: 2.5rem;\n" +
               "    font-weight: 300;\n" +
               "}\n" +
               "\n" +
               ".report-info {\n" +
               "    display: flex;\n" +
               "    justify-content: center;\n" +
               "    gap: 2rem;\n" +
               "    flex-wrap: wrap;\n" +
               "}\n" +
               "\n" +
               ".report-info span {\n" +
               "    background: rgba(255,255,255,0.2);\n" +
               "    padding: 0.5rem 1rem;\n" +
               "    border-radius: 5px;\n" +
               "    font-size: 0.9rem;\n" +
               "}\n" +
               "\n" +
               ".content {\n" +
               "    max-width: 1200px;\n" +
               "    margin: 2rem auto;\n" +
               "    padding: 0 2rem;\n" +
               "}\n" +
               "\n" +
               "section {\n" +
               "    background: white;\n" +
               "    margin: 2rem 0;\n" +
               "    padding: 2rem;\n" +
               "    border-radius: 8px;\n" +
               "    box-shadow: 0 2px 10px rgba(0,0,0,0.1);\n" +
               "}\n" +
               "\n" +
               "section h2 {\n" +
               "    color: #2c3e50;\n" +
               "    border-bottom: 3px solid #3498db;\n" +
               "    padding-bottom: 0.5rem;\n" +
               "    margin-bottom: 1.5rem;\n" +
               "}\n" +
               "\n" +
               ".stats-grid {\n" +
               "    display: grid;\n" +
               "    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));\n" +
               "    gap: 1rem;\n" +
               "    margin-top: 1rem;\n" +
               "}\n" +
               "\n" +
               ".stat-card {\n" +
               "    background: #f8f9fa;\n" +
               "    padding: 1.5rem;\n" +
               "    border-radius: 8px;\n" +
               "    text-align: center;\n" +
               "    border: 1px solid #e9ecef;\n" +
               "}\n" +
               "\n" +
               ".stat-card h3 {\n" +
               "    margin: 0 0 0.5rem 0;\n" +
               "    color: #6c757d;\n" +
               "    font-size: 0.9rem;\n" +
               "    text-transform: uppercase;\n" +
               "}\n" +
               "\n" +
               ".stat-number {\n" +
               "    font-size: 2rem;\n" +
               "    font-weight: bold;\n" +
               "    color: #3498db;\n" +
               "}\n" +
               "\n" +
               ".stat-text {\n" +
               "    font-size: 1.2rem;\n" +
               "    color: #2c3e50;\n" +
               "}\n" +
               "\n" +
               ".class-list {\n" +
               "    display: grid;\n" +
               "    gap: 1rem;\n" +
               "}\n" +
               "\n" +
               ".class-item {\n" +
               "    background: #f8f9fa;\n" +
               "    padding: 1rem;\n" +
               "    border-radius: 5px;\n" +
               "    border-left: 4px solid #3498db;\n" +
               "}\n" +
               "\n" +
               ".class-item h3 {\n" +
               "    margin: 0 0 0.5rem 0;\n" +
               "    color: #2c3e50;\n" +
               "}\n" +
               "\n" +
               ".class-item p {\n" +
               "    margin: 0.25rem 0;\n" +
               "    color: #6c757d;\n" +
               "    font-size: 0.9rem;\n" +
               "}\n" +
               "\n" +
               ".main-footer {\n" +
               "    background: #2c3e50;\n" +
               "    color: white;\n" +
               "    text-align: center;\n" +
               "    padding: 1rem;\n" +
               "    margin-top: 3rem;\n" +
               "}\n" +
               "\n" +
               ".main-footer p {\n" +
               "    margin: 0;\n" +
               "    font-size: 0.9rem;\n" +
               "}\n" +
               "\n" +
               "/* Bootstrap Analysis Specific Styles */\n" +
               ".bootstrap-summary {\n" +
               "    margin-bottom: 2rem;\n" +
               "}\n" +
               "\n" +
               ".sequence-diagram-section {\n" +
               "    margin: 2rem 0;\n" +
               "}\n" +
               "\n" +
               ".diagram-container {\n" +
               "    background: #f8f9fa;\n" +
               "    border: 1px solid #dee2e6;\n" +
               "    border-radius: 5px;\n" +
               "    padding: 1rem;\n" +
               "    margin: 1rem 0;\n" +
               "}\n" +
               "\n" +
               ".sequence-diagram {\n" +
               "    background: white;\n" +
               "    padding: 1rem;\n" +
               "    border: 1px solid #e9ecef;\n" +
               "    border-radius: 3px;\n" +
               "    font-family: 'Courier New', monospace;\n" +
               "    font-size: 0.85rem;\n" +
               "    line-height: 1.4;\n" +
               "    overflow-x: auto;\n" +
               "}\n" +
               "\n" +
               ".diagram-source {\n" +
               "    margin-top: 1rem;\n" +
               "}\n" +
               "\n" +
               ".plantuml-source {\n" +
               "    width: 100%;\n" +
               "    height: 200px;\n" +
               "    font-family: 'Courier New', monospace;\n" +
               "    font-size: 0.8rem;\n" +
               "    background: #f8f9fa;\n" +
               "    border: 1px solid #dee2e6;\n" +
               "    border-radius: 3px;\n" +
               "    padding: 0.5rem;\n" +
               "    resize: vertical;\n" +
               "}\n" +
               "\n" +
               ".diagram-note {\n" +
               "    font-size: 0.8rem;\n" +
               "    color: #6c757d;\n" +
               "    margin-top: 0.5rem;\n" +
               "}\n" +
               "\n" +
               ".phase-breakdown-section {\n" +
               "    margin: 2rem 0;\n" +
               "}\n" +
               "\n" +
               ".phase-card {\n" +
               "    background: #f8f9fa;\n" +
               "    border-left: 4px solid #3498db;\n" +
               "    margin: 1rem 0;\n" +
               "    padding: 1rem;\n" +
               "    border-radius: 0 5px 5px 0;\n" +
               "}\n" +
               "\n" +
               ".phase-description {\n" +
               "    color: #6c757d;\n" +
               "    font-style: italic;\n" +
               "    margin: 0.5rem 0;\n" +
               "}\n" +
               "\n" +
               ".phase-stats {\n" +
               "    display: flex;\n" +
               "    gap: 1rem;\n" +
               "    margin: 0.5rem 0;\n" +
               "}\n" +
               "\n" +
               ".phase-step-count, .phase-duration {\n" +
               "    background: #e9ecef;\n" +
               "    padding: 0.25rem 0.5rem;\n" +
               "    border-radius: 3px;\n" +
               "    font-size: 0.85rem;\n" +
               "    font-weight: 500;\n" +
               "}\n" +
               "\n" +
               ".phase-steps {\n" +
               "    margin-top: 1rem;\n" +
               "}\n" +
               "\n" +
               ".step-item {\n" +
               "    display: grid;\n" +
               "    grid-template-columns: auto 1fr auto;\n" +
               "    gap: 1rem;\n" +
               "    padding: 0.5rem;\n" +
               "    border-bottom: 1px solid #e9ecef;\n" +
               "    align-items: center;\n" +
               "}\n" +
               "\n" +
               ".step-item:last-child {\n" +
               "    border-bottom: none;\n" +
               "}\n" +
               "\n" +
               ".step-number {\n" +
               "    background: #3498db;\n" +
               "    color: white;\n" +
               "    border-radius: 50%;\n" +
               "    width: 24px;\n" +
               "    height: 24px;\n" +
               "    display: flex;\n" +
               "    align-items: center;\n" +
               "    justify-content: center;\n" +
               "    font-size: 0.75rem;\n" +
               "    font-weight: bold;\n" +
               "}\n" +
               "\n" +
               ".step-class {\n" +
               "    font-weight: 500;\n" +
               "    color: #2c3e50;\n" +
               "}\n" +
               "\n" +
               ".step-method {\n" +
               "    font-family: 'Courier New', monospace;\n" +
               "    font-size: 0.85rem;\n" +
               "    color: #495057;\n" +
               "}\n" +
               "\n" +
               ".components-section {\n" +
               "    margin: 2rem 0;\n" +
               "}\n" +
               "\n" +
               ".components-list, .autoconfig-list {\n" +
               "    display: grid;\n" +
               "    gap: 0.5rem;\n" +
               "    margin: 1rem 0;\n" +
               "}\n" +
               "\n" +
               ".component-item, .autoconfig-item {\n" +
               "    background: #e7f3ff;\n" +
               "    border-left: 3px solid #3498db;\n" +
               "    padding: 0.75rem;\n" +
               "    border-radius: 0 3px 3px 0;\n" +
               "}\n" +
               "\n" +
               ".autoconfig-item {\n" +
               "    background: #fff3cd;\n" +
               "    border-left-color: #ffc107;\n" +
               "}\n" +
               "\n" +
               ".component-name, .autoconfig-name {\n" +
               "    font-family: 'Courier New', monospace;\n" +
               "    font-size: 0.9rem;\n" +
               "    font-weight: 500;\n" +
               "}\n" +
               "\n" +
               ".autoconfig-section {\n" +
               "    margin-top: 2rem;\n" +
               "    border-top: 1px solid #dee2e6;\n" +
               "    padding-top: 1rem;\n" +
               "}\n" +
               "\n" +
               ".error-message {\n" +
               "    background: #f8d7da;\n" +
               "    border: 1px solid #f5c2c7;\n" +
               "    border-radius: 5px;\n" +
               "    padding: 1rem;\n" +
               "    margin: 1rem 0;\n" +
               "}\n" +
               "\n" +
               ".error-message h3 {\n" +
               "    color: #842029;\n" +
               "    margin-top: 0;\n" +
               "}\n" +
               "\n" +
               ".no-sequence {\n" +
               "    text-align: center;\n" +
               "    padding: 2rem;\n" +
               "    background: #f8f9fa;\n" +
               "    border-radius: 5px;\n" +
               "    color: #6c757d;\n" +
               "}\n" +
               "\n" +
               "@media (max-width: 768px) {\n" +
               "    .content {\n" +
               "        padding: 0 1rem;\n" +
               "    }\n" +
               "    \n" +
               "    .main-header {\n" +
               "        padding: 1rem;\n" +
               "    }\n" +
               "    \n" +
               "    .main-header h1 {\n" +
               "        font-size: 2rem;\n" +
               "    }\n" +
               "    \n" +
               "    .report-info {\n" +
               "        flex-direction: column;\n" +
               "        gap: 0.5rem;\n" +
               "    }\n" +
               "    \n" +
               "    .step-item {\n" +
               "        grid-template-columns: auto 1fr;\n" +
               "        grid-template-rows: auto auto;\n" +
               "    }\n" +
               "    \n" +
               "    .step-method {\n" +
               "        grid-column: 1 / -1;\n" +
               "        margin-top: 0.5rem;\n" +
               "    }\n" +
               "    \n" +
               "    .phase-stats {\n" +
               "        flex-direction: column;\n" +
               "        gap: 0.5rem;\n" +
               "    }\n" +
               "}";
    }
}