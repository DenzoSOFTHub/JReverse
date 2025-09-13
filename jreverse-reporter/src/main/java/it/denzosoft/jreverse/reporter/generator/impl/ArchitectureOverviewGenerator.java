package it.denzosoft.jreverse.reporter.generator.impl;

import it.denzosoft.jreverse.core.port.ReportType;
import it.denzosoft.jreverse.reporter.generator.AbstractReportGenerator;
import it.denzosoft.jreverse.reporter.template.ReportContext;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generator for Package Class Map report (Architecture Overview).
 */
public class ArchitectureOverviewGenerator extends AbstractReportGenerator {
    
    @Override
    protected ReportType getReportType() {
        return ReportType.PACKAGE_CLASS_MAP;
    }
    
    @Override
    protected String getReportTitle() {
        return "Package Class Map - Architecture Overview";
    }
    
    @Override
    protected String getHeaderCssClass() {
        return "architecture-header";
    }
    
    @Override
    protected void writeReportContent(Writer writer, ReportContext context) throws IOException {
        writeArchitectureOverview(writer, context);
    }
    
    private void writeArchitectureOverview(Writer writer, ReportContext context) throws IOException {
        writer.write("        <section class=\"architecture-section\">\n");
        writer.write("            <h2>Architecture Overview</h2>\n");
        
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("Total Classes", context.getJarContent().getClasses().size());
        stats.put("JAR Type", context.getJarContent().getJarType().getDisplayName());
        
        writeStatsGrid(writer, stats);
        writer.write("        </section>\n");
    }
}