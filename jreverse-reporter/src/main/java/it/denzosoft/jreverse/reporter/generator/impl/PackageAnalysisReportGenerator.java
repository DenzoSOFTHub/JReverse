package it.denzosoft.jreverse.reporter.generator.impl;

import it.denzosoft.jreverse.core.port.ReportType;
import it.denzosoft.jreverse.core.port.PackageAnalyzer;
import it.denzosoft.jreverse.core.model.PackageAnalysisResult;
import it.denzosoft.jreverse.core.model.PackageHierarchy;
import it.denzosoft.jreverse.core.model.PackageInfo;
import it.denzosoft.jreverse.analyzer.packageanalyzer.JavassistPackageAnalyzer;
import it.denzosoft.jreverse.reporter.generator.AbstractReportGenerator;
import it.denzosoft.jreverse.reporter.template.ReportContext;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Generator for Package Dependencies report using PackageAnalyzer.
 * Provides detailed package structure analysis and hierarchy visualization.
 */
public class PackageAnalysisReportGenerator extends AbstractReportGenerator {

    private final PackageAnalyzer packageAnalyzer;

    public PackageAnalysisReportGenerator() {
        this.packageAnalyzer = new JavassistPackageAnalyzer();
    }

    @Override
    protected ReportType getReportType() {
        return ReportType.PACKAGE_DEPENDENCIES;
    }

    @Override
    protected String getReportTitle() {
        return "Package Dependencies Analysis";
    }

    @Override
    protected String getHeaderCssClass() {
        return "package-analysis-header";
    }

    @Override
    protected void writeReportContent(Writer writer, ReportContext context) throws IOException {
        writePackageAnalysisContent(writer, context);
    }

    private void writePackageAnalysisContent(Writer writer, ReportContext context) throws IOException {
        if (!packageAnalyzer.canAnalyze(context.getJarContent())) {
            writer.write("        <section class=\"warning\">\\n");
            writer.write("            <h2>Analysis Not Available</h2>\\n");
            writer.write("            <p>Package analysis is not available for this JAR content.</p>\\n");
            writer.write("        </section>\\n");
            return;
        }

        try {
            PackageAnalysisResult result = packageAnalyzer.analyzePackageStructure(context.getJarContent());
            writePackageHierarchy(writer, result.getHierarchy());
            writePackageMetrics(writer, result.getHierarchy());

        } catch (Exception e) {
            writer.write("        <section class=\"error\">\\n");
            writer.write("            <h2>Analysis Error</h2>\\n");
            writer.write("            <p>Error during package analysis: " + escapeHtml(e.getMessage()) + "</p>\\n");
            writer.write("        </section>\\n");
        }
    }

    private void writePackageHierarchy(Writer writer, PackageHierarchy hierarchy) throws IOException {
        writer.write("        <section class=\"package-hierarchy\">\\n");
        writer.write("            <h2>Package Hierarchy</h2>\\n");

        if (hierarchy.getPackageCount() == 0) {
            writer.write("            <p>No packages found in this JAR.</p>\\n");
        } else {
            writer.write("            <div class=\"hierarchy-stats\">\\n");
            writer.write("                <div class=\"stat\">\\n");
            writer.write("                    <span class=\"stat-label\">Total Packages:</span>\\n");
            writer.write("                    <span class=\"stat-value\">" + hierarchy.getPackageCount() + "</span>\\n");
            writer.write("                </div>\\n");
            writer.write("                <div class=\"stat\">\\n");
            writer.write("                    <span class=\"stat-label\">Max Depth:</span>\\n");
            writer.write("                    <span class=\"stat-value\">" + hierarchy.getMaxDepth() + "</span>\\n");
            writer.write("                </div>\\n");
            writer.write("                <div class=\"stat\">\\n");
            writer.write("                    <span class=\"stat-label\">Average Depth:</span>\\n");
            writer.write("                    <span class=\"stat-value\">" + String.format("%.1f", hierarchy.getAverageDepth()) + "</span>\\n");
            writer.write("                </div>\\n");
            writer.write("            </div>\\n");

            writePackageList(writer, hierarchy.getPackages());
        }

        writer.write("        </section>\\n");
    }

    private void writePackageList(Writer writer, Map<String, PackageInfo> packages) throws IOException {
        writer.write("            <div class=\"package-list\">\\n");
        writer.write("                <h3>Package Details</h3>\\n");
        writer.write("                <table class=\"package-table\">\\n");
        writer.write("                    <thead>\\n");
        writer.write("                        <tr>\\n");
        writer.write("                            <th>Package Name</th>\\n");
        writer.write("                            <th>Classes</th>\\n");
        writer.write("                            <th>Interfaces</th>\\n");
        writer.write("                            <th>Complexity</th>\\n");
        writer.write("                        </tr>\\n");
        writer.write("                    </thead>\\n");
        writer.write("                    <tbody>\\n");

        packages.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                try {
                    writePackageRow(writer, entry.getKey(), entry.getValue());
                } catch (IOException e) {
                    // Log error but continue processing
                }
            });

        writer.write("                    </tbody>\\n");
        writer.write("                </table>\\n");
        writer.write("            </div>\\n");
    }

    private void writePackageRow(Writer writer, String packageName, PackageInfo packageInfo) throws IOException {
        writer.write("                        <tr>\\n");
        writer.write("                            <td class=\"package-name\">" + escapeHtml(packageName) + "</td>\\n");
        writer.write("                            <td class=\"class-count\">" + packageInfo.getClasses().size() + "</td>\\n");

        long interfaceCount = packageInfo.getClasses().stream()
            .filter(classInfo -> classInfo.getClassType().toString().equals("INTERFACE"))
            .count();

        writer.write("                            <td class=\"interface-count\">" + interfaceCount + "</td>\\n");

        if (packageInfo.getMetrics() != null) {
            writer.write("                            <td class=\"complexity\">" + String.format("%.1f", packageInfo.getMetrics().getComplexityScore()) + "</td>\\n");
        } else {
            writer.write("                            <td class=\"complexity\">N/A</td>\\n");
        }

        writer.write("                        </tr>\\n");
    }

    private void writePackageMetrics(Writer writer, PackageHierarchy hierarchy) throws IOException {
        writer.write("        <section class=\"package-metrics\">\\n");
        writer.write("            <h2>Package Metrics Summary</h2>\\n");

        if (hierarchy.getPackageCount() > 0) {
            int totalClasses = hierarchy.getPackages().values().stream()
                .mapToInt(pkg -> pkg.getClasses().size())
                .sum();

            double averageClassesPerPackage = (double) totalClasses / hierarchy.getPackageCount();

            writer.write("            <div class=\"metrics-grid\">\\n");
            writer.write("                <div class=\"metric\">\\n");
            writer.write("                    <h3>Structure</h3>\\n");
            writer.write("                    <p>Average Classes per Package: " + String.format("%.1f", averageClassesPerPackage) + "</p>\\n");
            writer.write("                    <p>Hierarchy Depth: " + hierarchy.getMaxDepth() + " levels</p>\\n");
            writer.write("                </div>\\n");
            writer.write("                <div class=\"metric\">\\n");
            writer.write("                    <h3>Distribution</h3>\\n");

            String largestPackage = hierarchy.getPackages().entrySet().stream()
                .max(Map.Entry.comparingByValue((p1, p2) -> Integer.compare(p1.getClasses().size(), p2.getClasses().size())))
                .map(Map.Entry::getKey)
                .orElse("N/A");

            writer.write("                    <p>Largest Package: " + escapeHtml(largestPackage) + "</p>\\n");
            writer.write("                    <p>Total Classes: " + totalClasses + "</p>\\n");
            writer.write("                </div>\\n");
            writer.write("            </div>\\n");
        } else {
            writer.write("            <p>No metrics available - no packages found.</p>\\n");
        }

        writer.write("        </section>\\n");
    }

    @Override
    protected String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
}