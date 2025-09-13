package it.denzosoft.jreverse.reporter.generator;

import it.denzosoft.jreverse.core.port.ReportType;
import it.denzosoft.jreverse.reporter.generator.impl.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Factory for creating report generators based on report type.
 */
public class ReportGeneratorFactory {
    
    private static final Map<ReportType, Supplier<AbstractReportGenerator>> GENERATOR_SUPPLIERS = new HashMap<>();
    
    static {
        // Original reports
        GENERATOR_SUPPLIERS.put(ReportType.PACKAGE_CLASS_MAP, ArchitectureOverviewGenerator::new);
        GENERATOR_SUPPLIERS.put(ReportType.UML_CLASS_DIAGRAM, ClassSummaryGenerator::new);
        GENERATOR_SUPPLIERS.put(ReportType.PACKAGE_DEPENDENCIES, DependencyAnalysisGenerator::new);
        GENERATOR_SUPPLIERS.put(ReportType.MODULE_DEPENDENCIES, PackageStructureGenerator::new);
        GENERATOR_SUPPLIERS.put(ReportType.BOOTSTRAP_ANALYSIS, BootstrapAnalysisReportGenerator::new);
        
        // Enhanced reports with entrypoint support
        GENERATOR_SUPPLIERS.put(ReportType.REST_ENDPOINT_MAP, RestEndpointsEnhancedGenerator::new);
        GENERATOR_SUPPLIERS.put(ReportType.AUTOWIRING_GRAPH, AutowiringGraphReportGenerator::new);
        GENERATOR_SUPPLIERS.put(ReportType.HTTP_CALL_GRAPH, () -> new GeneratorStubs.HttpCallGraphEnhancedGenerator());
        GENERATOR_SUPPLIERS.put(ReportType.EVENT_LISTENER_ANALYSIS, () -> new GeneratorStubs.ComprehensiveEntryPointsGenerator());
        GENERATOR_SUPPLIERS.put(ReportType.ASYNC_CALL_SEQUENCES, () -> new GeneratorStubs.AsyncSequencesCompleteGenerator());
        GENERATOR_SUPPLIERS.put(ReportType.SECURITY_ANNOTATIONS, () -> new GeneratorStubs.SecurityAnnotationsEnhancedGenerator());
        
        // New specialized reports (51-55)
        GENERATOR_SUPPLIERS.put(ReportType.SCHEDULED_TASKS_ANALYSIS, ScheduledTasksAnalysisGenerator::new);
        GENERATOR_SUPPLIERS.put(ReportType.ASYNC_PROCESSING_ANALYSIS, () -> new GeneratorStubs.AsyncProcessingAnalysisGenerator());
        GENERATOR_SUPPLIERS.put(ReportType.MESSAGING_INTEGRATION_ANALYSIS, () -> new GeneratorStubs.MessagingIntegrationAnalysisGenerator());
        GENERATOR_SUPPLIERS.put(ReportType.EVENT_DRIVEN_ANALYSIS, () -> new GeneratorStubs.EventDrivenAnalysisGenerator());
        GENERATOR_SUPPLIERS.put(ReportType.SECURITY_ENTRYPOINT_MATRIX, () -> new GeneratorStubs.SecurityEntrypointMatrixGenerator());
    }
    
    /**
     * Creates a report generator for the specified report type.
     */
    public static AbstractReportGenerator create(ReportType reportType) {
        Supplier<AbstractReportGenerator> supplier = GENERATOR_SUPPLIERS.get(reportType);
        if (supplier == null) {
            throw new IllegalArgumentException("No generator available for report type: " + reportType);
        }
        return supplier.get();
    }
    
    /**
     * Checks if a generator is available for the specified report type.
     */
    public static boolean isSupported(ReportType reportType) {
        return GENERATOR_SUPPLIERS.containsKey(reportType);
    }
    
    /**
     * Returns all supported report types.
     */
    public static ReportType[] getSupportedReportTypes() {
        return GENERATOR_SUPPLIERS.keySet().toArray(new ReportType[0]);
    }
}