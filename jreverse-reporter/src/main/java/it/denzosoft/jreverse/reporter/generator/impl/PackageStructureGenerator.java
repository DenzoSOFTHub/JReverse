package it.denzosoft.jreverse.reporter.generator.impl;

import it.denzosoft.jreverse.core.port.ReportType;
import it.denzosoft.jreverse.reporter.generator.AbstractReportGenerator;
import it.denzosoft.jreverse.reporter.template.ReportContext;

import java.io.IOException;
import java.io.Writer;

public class PackageStructureGenerator extends AbstractReportGenerator {
    @Override
    protected ReportType getReportType() { return ReportType.MODULE_DEPENDENCIES; }
    @Override
    protected String getReportTitle() { return "Module Dependencies - Package Structure"; }
    @Override
    protected String getHeaderCssClass() { return "package-header"; }
    @Override
    protected void writeReportContent(Writer writer, ReportContext context) throws IOException {
        writer.write("        <section class=\"package-section\">\n");
        writer.write("            <h2>Package Structure</h2>\n");
        writer.write("            <div class=\"package-tree\">\n");
        writer.write("                <p>Package analysis for JAR: " + context.getJarContent().getLocation().getFileName() + "</p>\n");
        writer.write("            </div>\n");
        writer.write("        </section>\n");
    }
}