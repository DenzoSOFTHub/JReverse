package it.denzosoft.jreverse.reporter.generator.impl;

import it.denzosoft.jreverse.core.port.ReportType;
import it.denzosoft.jreverse.reporter.generator.AbstractReportGenerator;
import it.denzosoft.jreverse.reporter.template.ReportContext;

import java.io.IOException;
import java.io.Writer;

public class DependencyAnalysisGenerator extends AbstractReportGenerator {
    @Override
    protected ReportType getReportType() { return ReportType.PACKAGE_DEPENDENCIES; }
    @Override
    protected String getReportTitle() { return "Package Dependencies Analysis"; }
    @Override
    protected String getHeaderCssClass() { return "dependency-header"; }
    @Override
    protected void writeReportContent(Writer writer, ReportContext context) throws IOException {
        writer.write("        <section class=\"dependency-section\">\n");
        writer.write("            <h2>Dependency Analysis</h2>\n");
        writer.write("            <p>Analyzing dependencies for " + context.getJarContent().getClasses().size() + " classes...</p>\n");
        writer.write("        </section>\n");
    }
}