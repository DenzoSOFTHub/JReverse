package it.denzosoft.jreverse.reporter.generator.impl;

import it.denzosoft.jreverse.analyzer.bootstrap.BootstrapAnalyzer;
import it.denzosoft.jreverse.analyzer.bootstrap.BootstrapAnalysisResult;
import it.denzosoft.jreverse.analyzer.bootstrap.BootstrapSequenceGenerator;
import it.denzosoft.jreverse.analyzer.bootstrap.BootstrapSequencePhase;
import it.denzosoft.jreverse.analyzer.bootstrap.BootstrapSequenceStep;
import it.denzosoft.jreverse.core.exception.ReportGenerationException;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.port.ReportType;
import it.denzosoft.jreverse.reporter.generator.AbstractReportGenerator;
import it.denzosoft.jreverse.reporter.template.ReportContext;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * Generator for Bootstrap Analysis Report with interactive sequence diagrams.
 * Creates comprehensive HTML report with PlantUML/Mermaid sequence diagrams showing Spring Boot bootstrap flow.
 */
public class BootstrapAnalysisReportGenerator extends AbstractReportGenerator {
    
    private final BootstrapSequenceGenerator sequenceGenerator;
    
    public BootstrapAnalysisReportGenerator() {
        this.sequenceGenerator = new BootstrapSequenceGenerator();
    }
    
    @Override
    protected ReportContext buildReportContext(JarContent jarContent, Map<String, Object> analysisResults) {
        BootstrapAnalysisResult bootstrapResult = (BootstrapAnalysisResult) analysisResults.get("bootstrap");
        
        ReportContext context = new ReportContext();
        context.setJarContent(jarContent);
        context.setReportTitle("Spring Boot Bootstrap Analysis");
        context.setGenerationTime(java.time.LocalDateTime.now());
        context.getAnalysisResults().put("bootstrapResult", bootstrapResult);
        context.getAnalysisResults().put("jarContent", jarContent);
        return context;
    }
    
    @Override
    protected void writeReportContent(Writer writer, ReportContext context) throws IOException {
        BootstrapAnalysisResult bootstrapResult = (BootstrapAnalysisResult) context.getAnalysisResults().get("bootstrapResult");
        
        if (bootstrapResult == null || bootstrapResult.getTotalSteps() == 0) {
            writeNoDataMessage(writer);
            return;
        }
        
        writeSummarySection(writer, bootstrapResult);
        writeSequenceDiagramSection(writer, bootstrapResult);
        writePhaseAnalysisSection(writer, bootstrapResult);
        writeTimingAnalysisSection(writer, bootstrapResult);
        writeDetailsSection(writer, bootstrapResult);
    }
    
    private void writeNoDataMessage(Writer writer) throws IOException {
        writer.write("<div class='no-data-message'>");
        writer.write("<h2>No Bootstrap Data Found</h2>");
        writer.write("<p>No Spring Boot bootstrap sequence was detected in the analyzed JAR.</p>");
        writer.write("<p>This might be a regular Java application or the analysis was unable to identify Spring Boot components.</p>");
        writer.write("</div>");
    }
    
    private void writeSummarySection(Writer writer, BootstrapAnalysisResult result) throws IOException {
        writer.write("<section class='summary-section'>");
        writer.write("<h2>Bootstrap Summary</h2>");
        
        writer.write("<div class='summary-cards'>");
        writer.write(String.format("<div class='summary-card'><span class='metric'>%d</span><span class='label'>Total Steps</span></div>", 
            result.getTotalSteps()));
        writer.write(String.format("<div class='summary-card'><span class='metric'>%d</span><span class='label'>Components</span></div>", 
            result.getComponentCount()));
        writer.write(String.format("<div class='summary-card'><span class='metric'>%d</span><span class='label'>Auto-Configurations</span></div>", 
            result.getAutoConfigurationCount()));
        
        if (result.getTimingInfo().hasTimingData()) {
            writer.write(String.format("<div class='summary-card'><span class='metric'>%s</span><span class='label'>Total Time</span></div>", 
                result.getTimingInfo().getFormattedTotalDuration()));
        }
        writer.write("</div>");
        
        writer.write("</section>");
    }
    
    private void writeSequenceDiagramSection(Writer writer, BootstrapAnalysisResult result) throws IOException {
        writer.write("<section class='sequence-diagram-section'>");
        writer.write("<h2>Bootstrap Sequence Diagram</h2>");
        
        // Diagram format selection
        writer.write("<div class='diagram-controls'>");
        writer.write("<label for='diagramFormat'>Format:</label>");
        writer.write("<select id='diagramFormat' onchange='switchDiagramFormat()'>");
        writer.write("<option value='text' selected>Text Format</option>");
        writer.write("<option value='plantuml'>PlantUML</option>");
        writer.write("<option value='mermaid'>Mermaid</option>");
        writer.write("</select>");
        writer.write("<button onclick='copyDiagram()'>Copy Diagram</button>");
        writer.write("</div>");
        
        // Generate diagrams in all formats
        String textDiagram = sequenceGenerator.generateSequenceDiagram(result, BootstrapAnalyzer.SequenceDiagramFormat.TEXT);
        String plantUMLDiagram = sequenceGenerator.generateSequenceDiagram(result, BootstrapAnalyzer.SequenceDiagramFormat.PLANTUML);
        String mermaidDiagram = sequenceGenerator.generateSequenceDiagram(result, BootstrapAnalyzer.SequenceDiagramFormat.MERMAID);
        
        // Text diagram (default)
        writer.write("<div id='diagram-text' class='diagram-container'>");
        writer.write("<pre class='sequence-diagram'>");
        writer.write(escapeHtml(textDiagram));
        writer.write("</pre>");
        writer.write("</div>");
        
        // PlantUML diagram
        writer.write("<div id='diagram-plantuml' class='diagram-container' style='display: none;'>");
        writer.write("<pre class='sequence-diagram'>");
        writer.write(escapeHtml(plantUMLDiagram));
        writer.write("</pre>");
        writer.write("<div class='diagram-note'>");
        writer.write("<p><strong>PlantUML:</strong> Copy this content to <a href='http://plantuml.com/plantuml/' target='_blank'>PlantUML online editor</a> for visual rendering.</p>");
        writer.write("</div>");
        writer.write("</div>");
        
        // Mermaid diagram
        writer.write("<div id='diagram-mermaid' class='diagram-container' style='display: none;'>");
        writer.write("<pre class='sequence-diagram'>");
        writer.write(escapeHtml(mermaidDiagram));
        writer.write("</pre>");
        writer.write("<div class='diagram-note'>");
        writer.write("<p><strong>Mermaid:</strong> Copy this content to <a href='https://mermaid.live/' target='_blank'>Mermaid live editor</a> for visual rendering.</p>");
        writer.write("</div>");
        writer.write("</div>");
        
        writer.write("</section>");
    }
    
    private void writePhaseAnalysisSection(Writer writer, BootstrapAnalysisResult result) throws IOException {
        writer.write("<section class='phase-analysis-section'>");
        writer.write("<h2>Bootstrap Phases</h2>");
        
        writer.write("<div class='phases-grid'>");
        for (BootstrapSequencePhase phase : BootstrapSequencePhase.values()) {
            List<BootstrapSequenceStep> phaseSteps = result.getStepsForPhase(phase);
            if (!phaseSteps.isEmpty()) {
                writer.write("<div class='phase-card'>");
                writer.write(String.format("<h3>%s</h3>", phase.getDisplayName()));
                writer.write(String.format("<div class='phase-stat'>%d steps</div>", phaseSteps.size()));
                
                if (result.getTimingInfo().hasTimingData()) {
                    writer.write(String.format("<div class='phase-stat'>%s duration</div>", 
                        result.getTimingInfo().getFormattedPhaseDuration(phase)));
                    writer.write(String.format("<div class='phase-stat'>%.1f%% of total</div>", 
                        result.getTimingInfo().getPhasePercentage(phase)));
                }
                
                writer.write("<div class='phase-description'>");
                writer.write(phase.getDescription());
                writer.write("</div>");
                writer.write("</div>");
            }
        }
        writer.write("</div>");
        
        writer.write("</section>");
    }
    
    private void writeTimingAnalysisSection(Writer writer, BootstrapAnalysisResult result) throws IOException {
        if (!result.getTimingInfo().hasTimingData()) {
            return;
        }
        
        writer.write("<section class='timing-analysis-section'>");
        writer.write("<h2>Timing Analysis</h2>");
        
        // Phase timing summary
        String phaseSummary = sequenceGenerator.generatePhaseSummaryDiagram(result, BootstrapAnalyzer.SequenceDiagramFormat.TEXT);
        writer.write("<div class='timing-summary'>");
        writer.write("<h3>Phase Duration Summary</h3>");
        writer.write("<pre>");
        writer.write(escapeHtml(phaseSummary));
        writer.write("</pre>");
        writer.write("</div>");
        
        // Performance insights
        writer.write("<div class='performance-insights'>");
        writer.write("<h3>Performance Insights</h3>");
        writer.write("<ul>");
        
        BootstrapSequencePhase slowestPhase = result.getTimingInfo().getLongestPhase();
        if (slowestPhase != null) {
            writer.write(String.format("<li><strong>Slowest Phase:</strong> %s (%.1f%% of total time)</li>",
                slowestPhase.getDisplayName(),
                result.getTimingInfo().getPhasePercentage(slowestPhase)));
        }
        
        // Note: getSlowestSteps method not available in BootstrapTimingInfo
        // Using phase-based analysis instead
        writer.write("<li><strong>Performance Summary:</strong> ");
        writer.write(String.format("Total %d steps analyzed in %d phases</li>",
            result.getTimingInfo().getTotalStepCount(),
            result.getTimingInfo().getPhaseDurations().size()));
        
        writer.write("</ul>");
        writer.write("</div>");
        
        writer.write("</section>");
    }
    
    private void writeDetailsSection(Writer writer, BootstrapAnalysisResult result) throws IOException {
        writer.write("<section class='details-section'>");
        writer.write("<h2>Detailed Steps</h2>");
        
        writer.write("<div class='steps-filter'>");
        writer.write("<label for='phaseFilter'>Filter by Phase:</label>");
        writer.write("<select id='phaseFilter' onchange='filterSteps()'>");
        writer.write("<option value='all'>All Phases</option>");
        for (BootstrapSequencePhase phase : BootstrapSequencePhase.values()) {
            if (result.hasPhase(phase)) {
                writer.write(String.format("<option value='%s'>%s</option>", 
                    phase.name().toLowerCase(), phase.getDisplayName()));
            }
        }
        writer.write("</select>");
        writer.write("</div>");
        
        writer.write("<div class='steps-table-container'>");
        writer.write("<table class='steps-table'>");
        writer.write("<thead>");
        writer.write("<tr>");
        writer.write("<th>Step</th>");
        writer.write("<th>Phase</th>");
        writer.write("<th>Class</th>");
        writer.write("<th>Method</th>");
        writer.write("<th>Description</th>");
        if (result.getTimingInfo().hasTimingData()) {
            writer.write("<th>Duration (ms)</th>");
        }
        writer.write("</tr>");
        writer.write("</thead>");
        writer.write("<tbody>");
        
        for (BootstrapSequenceStep step : result.getSequenceSteps()) {
            writer.write(String.format("<tr class='step-row phase-%s'>", 
                step.getPhase().name().toLowerCase()));
            writer.write(String.format("<td>%d</td>", step.getSequenceNumber()));
            writer.write(String.format("<td class='phase-badge phase-%s'>%s</td>", 
                step.getPhase().name().toLowerCase(), step.getPhase().getDisplayName()));
            writer.write(String.format("<td>%s</td>", escapeHtml(getSimpleClassName(step.getParticipantClass()))));
            writer.write(String.format("<td><code>%s</code></td>", escapeHtml(step.getFormattedMethodSignature())));
            writer.write(String.format("<td>%s</td>", escapeHtml(step.getDescription())));
            if (result.getTimingInfo().hasTimingData()) {
                writer.write(String.format("<td class='duration'>%d</td>", step.getEstimatedDurationMs()));
            }
            writer.write("</tr>");
        }
        
        writer.write("</tbody>");
        writer.write("</table>");
        writer.write("</div>");
        
        writer.write("</section>");
    }
    
    private String getSimpleClassName(String fullyQualifiedName) {
        if (fullyQualifiedName == null) return "";
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        return lastDot >= 0 ? fullyQualifiedName.substring(lastDot + 1) : fullyQualifiedName;
    }
    
    // Custom styles for bootstrap analysis report
    protected void writeCustomStyles(Writer writer) throws IOException {
        writer.write("<style>\n" +
            ".summary-cards {\n" +
            "    display: grid;\n" +
            "    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));\n" +
            "    gap: 20px;\n" +
            "    margin: 20px 0;\n" +
            "}\n" +
            "\n" +
            ".summary-card {\n" +
            "    background: #f8f9fa;\n" +
            "    border: 1px solid #dee2e6;\n" +
            "    border-radius: 8px;\n" +
            "    padding: 20px;\n" +
            "    text-align: center;\n" +
            "}\n" +
            "\n" +
            ".summary-card .metric {\n" +
            "    display: block;\n" +
            "    font-size: 2em;\n" +
            "    font-weight: bold;\n" +
            "    color: #007bff;\n" +
            "}\n" +
            "\n" +
            ".summary-card .label {\n" +
            "    display: block;\n" +
            "    margin-top: 5px;\n" +
            "    color: #6c757d;\n" +
            "    font-size: 0.9em;\n" +
            "}\n" +
            "\n" +
            ".diagram-controls {\n" +
            "    margin: 20px 0;\n" +
            "    padding: 10px;\n" +
            "    background: #f8f9fa;\n" +
            "    border-radius: 5px;\n" +
            "    display: flex;\n" +
            "    align-items: center;\n" +
            "    gap: 10px;\n" +
            "}\n" +
            "\n" +
            ".diagram-container {\n" +
            "    margin: 20px 0;\n" +
            "}\n" +
            "\n" +
            ".sequence-diagram {\n" +
            "    background: #f8f9fa;\n" +
            "    border: 1px solid #dee2e6;\n" +
            "    border-radius: 5px;\n" +
            "    padding: 15px;\n" +
            "    overflow-x: auto;\n" +
            "    font-family: 'Courier New', monospace;\n" +
            "    font-size: 0.9em;\n" +
            "    line-height: 1.4;\n" +
            "}\n" +
            "\n" +
            ".diagram-note {\n" +
            "    margin-top: 10px;\n" +
            "    padding: 10px;\n" +
            "    background: #e7f3ff;\n" +
            "    border-left: 4px solid #007bff;\n" +
            "    border-radius: 0 5px 5px 0;\n" +
            "}\n" +
            "\n" +
            ".phases-grid {\n" +
            "    display: grid;\n" +
            "    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));\n" +
            "    gap: 20px;\n" +
            "    margin: 20px 0;\n" +
            "}\n" +
            "\n" +
            ".phase-card {\n" +
            "    background: #fff;\n" +
            "    border: 1px solid #dee2e6;\n" +
            "    border-radius: 8px;\n" +
            "    padding: 20px;\n" +
            "    box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n" +
            "}\n" +
            "\n" +
            ".phase-card h3 {\n" +
            "    margin: 0 0 15px 0;\n" +
            "    color: #007bff;\n" +
            "    border-bottom: 2px solid #007bff;\n" +
            "    padding-bottom: 5px;\n" +
            "}\n" +
            "\n" +
            ".phase-stat {\n" +
            "    background: #e7f3ff;\n" +
            "    color: #0066cc;\n" +
            "    padding: 5px 10px;\n" +
            "    margin: 5px 0;\n" +
            "    border-radius: 15px;\n" +
            "    display: inline-block;\n" +
            "    font-weight: bold;\n" +
            "    font-size: 0.9em;\n" +
            "}\n" +
            "\n" +
            ".phase-description {\n" +
            "    margin-top: 15px;\n" +
            "    color: #6c757d;\n" +
            "    font-style: italic;\n" +
            "}\n" +
            "\n" +
            ".timing-summary {\n" +
            "    margin: 20px 0;\n" +
            "}\n" +
            "\n" +
            ".performance-insights {\n" +
            "    margin: 20px 0;\n" +
            "    background: #fff3cd;\n" +
            "    border: 1px solid #ffeaa7;\n" +
            "    border-radius: 5px;\n" +
            "    padding: 15px;\n" +
            "}\n" +
            "\n" +
            ".steps-filter {\n" +
            "    margin: 20px 0;\n" +
            "    padding: 10px;\n" +
            "    background: #f8f9fa;\n" +
            "    border-radius: 5px;\n" +
            "}\n" +
            "\n" +
            ".steps-table-container {\n" +
            "    overflow-x: auto;\n" +
            "    margin: 20px 0;\n" +
            "}\n" +
            "\n" +
            ".steps-table {\n" +
            "    width: 100%;\n" +
            "    border-collapse: collapse;\n" +
            "    background: white;\n" +
            "    box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n" +
            "}\n" +
            "\n" +
            ".steps-table th,\n" +
            ".steps-table td {\n" +
            "    padding: 12px;\n" +
            "    text-align: left;\n" +
            "    border-bottom: 1px solid #dee2e6;\n" +
            "}\n" +
            "\n" +
            ".steps-table th {\n" +
            "    background: #f8f9fa;\n" +
            "    font-weight: bold;\n" +
            "    color: #495057;\n" +
            "}\n" +
            "\n" +
            ".steps-table tr:hover {\n" +
            "    background: #f8f9fa;\n" +
            "}\n" +
            "\n" +
            ".phase-badge {\n" +
            "    padding: 4px 8px;\n" +
            "    border-radius: 12px;\n" +
            "    font-size: 0.8em;\n" +
            "    font-weight: bold;\n" +
            "    text-align: center;\n" +
            "}\n" +
            "\n" +
            ".phase-bootstrap_start { background: #d4edda; color: #155724; }\n" +
            ".phase-environment_preparation { background: #cce5ff; color: #004085; }\n" +
            ".phase-context_creation { background: #fff3cd; color: #856404; }\n" +
            ".phase-bean_factory_preparation { background: #f8d7da; color: #721c24; }\n" +
            ".phase-auto_configuration { background: #e2e3e5; color: #383d41; }\n" +
            ".phase-context_refresh { background: #d1ecf1; color: #0c5460; }\n" +
            ".phase-post_processing { background: #ffeaa7; color: #6c5ce7; }\n" +
            ".phase-application_ready { background: #d4edda; color: #155724; }\n" +
            "\n" +
            ".duration {\n" +
            "    text-align: right;\n" +
            "    font-weight: bold;\n" +
            "    color: #6c5ce7;\n" +
            "}\n" +
            "\n" +
            ".no-data-message {\n" +
            "    text-align: center;\n" +
            "    padding: 60px 20px;\n" +
            "    color: #6c757d;\n" +
            "    background: #f8f9fa;\n" +
            "    border-radius: 8px;\n" +
            "    margin: 20px 0;\n" +
            "}\n" +
            "\n" +
            ".no-data-message h2 {\n" +
            "    color: #495057;\n" +
            "    margin-bottom: 20px;\n" +
            "}\n" +
            "</style>");
    }
    
    // Custom scripts for bootstrap analysis report
    protected void writeCustomScripts(Writer writer) throws IOException {
        writer.write("<script>\n" +
            "function switchDiagramFormat() {\n" +
            "    const format = document.getElementById('diagramFormat').value;\n" +
            "    \n" +
            "    // Hide all diagrams\n" +
            "    document.getElementById('diagram-text').style.display = 'none';\n" +
            "    document.getElementById('diagram-plantuml').style.display = 'none';\n" +
            "    document.getElementById('diagram-mermaid').style.display = 'none';\n" +
            "    \n" +
            "    // Show selected diagram\n" +
            "    document.getElementById('diagram-' + format).style.display = 'block';\n" +
            "}\n" +
            "\n" +
            "function copyDiagram() {\n" +
            "    const format = document.getElementById('diagramFormat').value;\n" +
            "    const diagramElement = document.querySelector('#diagram-' + format + ' pre');\n" +
            "    \n" +
            "    if (diagramElement) {\n" +
            "        navigator.clipboard.writeText(diagramElement.textContent).then(() => {\n" +
            "            alert('Diagram copied to clipboard!');\n" +
            "        }).catch(err => {\n" +
            "            console.error('Failed to copy: ', err);\n" +
            "        });\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "function filterSteps() {\n" +
            "    const selectedPhase = document.getElementById('phaseFilter').value;\n" +
            "    const rows = document.querySelectorAll('.step-row');\n" +
            "    \n" +
            "    rows.forEach(row => {\n" +
            "        if (selectedPhase === 'all') {\n" +
            "            row.style.display = '';\n" +
            "        } else {\n" +
            "            const hasPhaseClass = row.classList.contains('phase-' + selectedPhase);\n" +
            "            row.style.display = hasPhaseClass ? '' : 'none';\n" +
            "        }\n" +
            "    });\n" +
            "}\n" +
            "</script>");
    }
    
    @Override
    protected ReportType getReportType() {
        return ReportType.BOOTSTRAP_ANALYSIS;
    }
    
    @Override
    protected String getReportTitle() {
        return "Bootstrap Analysis";
    }
    
    @Override
    protected String getHeaderCssClass() {
        return "bootstrap-header";
    }
}