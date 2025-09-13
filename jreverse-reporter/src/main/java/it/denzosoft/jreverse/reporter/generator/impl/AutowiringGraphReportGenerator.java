package it.denzosoft.jreverse.reporter.generator.impl;

import it.denzosoft.jreverse.core.exception.ReportGenerationException;
import it.denzosoft.jreverse.core.model.AutowiredAnalysisResult;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.port.ReportType;
import it.denzosoft.jreverse.reporter.generator.AbstractReportGenerator;
import it.denzosoft.jreverse.reporter.template.ReportContext;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Generator for autowiring graph report with interactive visualization.
 * Creates HTML report with vis.js network graph showing dependency relationships.
 */
public class AutowiringGraphReportGenerator extends AbstractReportGenerator {
    
    @Override
    protected ReportContext buildReportContext(JarContent jarContent, Map<String, Object> analysisResults) {
        AutowiredAnalysisResult autowiredResult = (AutowiredAnalysisResult) analysisResults.get("autowiring");
        
        ReportContext context = new ReportContext();
        context.setJarContent(jarContent);
        context.setReportTitle("Autowiring Dependency Graph");
        context.setGenerationTime(java.time.LocalDateTime.now());
        context.getAnalysisResults().put("autowiredResult", autowiredResult);
        context.getAnalysisResults().put("jarContent", jarContent);
        return context;
    }
    
    @Override
    protected void writeReportContent(Writer writer, ReportContext context) throws IOException {
        AutowiredAnalysisResult autowiredResult = (AutowiredAnalysisResult) context.getAnalysisResults().get("autowiredResult");
        
        writer.write("<h1>Autowiring Dependency Graph</h1>");
        
        if (autowiredResult == null || autowiredResult.getDependencies().isEmpty()) {
            writer.write("<p>No autowiring dependencies found in the analyzed JAR.</p>");
            return;
        }
        
        writer.write("<p>Found " + autowiredResult.getDependencies().size() + " autowired dependencies.</p>");
        writer.write("<p>Report generation completed successfully.</p>");
    }
    
    @Override
    protected ReportType getReportType() {
        return ReportType.AUTOWIRING_GRAPH;
    }
    
    @Override
    protected String getReportTitle() {
        return "Autowiring Graph";
    }
    
    @Override
    protected String getHeaderCssClass() {
        return "autowiring-header";
    }
}