import it.denzosoft.jreverse.analyzer.impl.WorkingEnhancedAnalyzer;
import it.denzosoft.jreverse.core.model.*;
import it.denzosoft.jreverse.core.port.*;
import it.denzosoft.jreverse.core.pattern.ReportStrategy;
import it.denzosoft.jreverse.core.usecase.GenerateReportUseCase;
import it.denzosoft.jreverse.analyzer.usecase.DefaultGenerateReportUseCase;
import it.denzosoft.jreverse.reporter.strategy.HtmlReportStrategy;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class to generate HTML report for JAR analysis
 */
public class HtmlReportGenerator {

    public static void main(String[] args) {
        try {
            String jarPath = "/workspace/JReverse/contrp.be-springboot-22.2.57.jar";
            String outputPath = "/workspace/JReverse/contrp-analysis-report.html";

            System.out.println("Starting HTML report generation...");

            // Create JAR location
            JarLocation jarLocation = new JarLocation(jarPath);

            // Initialize analyzer
            JarAnalyzerPort analyzer = new WorkingEnhancedAnalyzer();

            // Perform analysis
            System.out.println("Analyzing JAR content...");
            JarContent jarContent = analyzer.analyzeJar(jarLocation);

            // Initialize HTML report strategy
            ReportStrategy htmlStrategy = new HtmlReportStrategy();

            // Initialize report use case
            GenerateReportUseCase reportUseCase = new DefaultGenerateReportUseCase(htmlStrategy);

            // Create output file
            Path outputFile = Paths.get(outputPath);

            // Generate comprehensive architecture overview report
            try (FileOutputStream fileOut = new FileOutputStream(outputFile.toFile())) {
                System.out.println("Generating architecture overview report...");
                reportUseCase.execute(jarContent, ReportType.PACKAGE_CLASS_MAP, ReportFormat.HTML, fileOut);
                System.out.println("Architecture overview report generated: " + outputPath);
            }

            // Generate REST endpoint report
            String restReportPath = "/workspace/JReverse/contrp-rest-endpoints-report.html";
            try (FileOutputStream fileOut = new FileOutputStream(restReportPath)) {
                System.out.println("Generating REST endpoints report...");
                reportUseCase.execute(jarContent, ReportType.REST_ENDPOINT_MAP, ReportFormat.HTML, fileOut);
                System.out.println("REST endpoints report generated: " + restReportPath);
            }

            // Generate security analysis report
            String securityReportPath = "/workspace/JReverse/contrp-security-analysis-report.html";
            try (FileOutputStream fileOut = new FileOutputStream(securityReportPath)) {
                System.out.println("Generating security analysis report...");
                reportUseCase.execute(jarContent, ReportType.SPRING_SECURITY_CONFIG, ReportFormat.HTML, fileOut);
                System.out.println("Security analysis report generated: " + securityReportPath);
            }

            // Generate dependency analysis report
            String depReportPath = "/workspace/JReverse/contrp-dependency-analysis-report.html";
            try (FileOutputStream fileOut = new FileOutputStream(depReportPath)) {
                System.out.println("Generating dependency analysis report...");
                reportUseCase.execute(jarContent, ReportType.PACKAGE_DEPENDENCIES, ReportFormat.HTML, fileOut);
                System.out.println("Dependency analysis report generated: " + depReportPath);
            }

            System.out.println("\\n=== HTML Report Generation Completed ===");
            System.out.println("Generated reports:");
            System.out.println("1. Architecture Overview: " + outputPath);
            System.out.println("2. REST Endpoints: " + restReportPath);
            System.out.println("3. Security Analysis: " + securityReportPath);
            System.out.println("4. Dependency Analysis: " + depReportPath);

        } catch (Exception e) {
            System.err.println("Error generating HTML report: " + e.getMessage());
            e.printStackTrace();
        }
    }
}