package it.denzosoft.jreverse.reporter.generator.impl;

import it.denzosoft.jreverse.core.port.ReportType;
import it.denzosoft.jreverse.reporter.generator.AbstractReportGenerator;
import it.denzosoft.jreverse.reporter.template.ReportContext;

import java.io.IOException;
import java.io.Writer;

/**
 * Generator for UML Class Diagram report (Class Summary).
 */
public class ClassSummaryGenerator extends AbstractReportGenerator {
    
    @Override
    protected ReportType getReportType() {
        return ReportType.UML_CLASS_DIAGRAM;
    }
    
    @Override
    protected String getReportTitle() {
        return "UML Class Diagram - Class Summary";
    }
    
    @Override
    protected String getHeaderCssClass() {
        return "class-summary-header";
    }
    
    @Override
    protected void writeReportContent(Writer writer, ReportContext context) throws IOException {
        writeClassSummary(writer, context);
    }
    
    private void writeClassSummary(Writer writer, ReportContext context) throws IOException {
        writer.write("        <section class=\"class-summary-section\">\n");
        writer.write("            <h2>Class Summary</h2>\n");
        writer.write("            <div class=\"class-list\">\n");
        
        context.getJarContent().getClasses().forEach(classInfo -> {
            try {
                writer.write("                <div class=\"class-item\">\n");
                writer.write("                    <h3>" + escapeHtml(classInfo.getFullyQualifiedName()) + "</h3>\n");
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
}