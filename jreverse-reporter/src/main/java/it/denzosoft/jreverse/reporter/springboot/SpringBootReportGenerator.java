package it.denzosoft.jreverse.reporter.springboot;

import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.ComponentScanAnalysisResult;
import it.denzosoft.jreverse.core.model.ConfigurationAnalysisResult;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.MainMethodAnalysisResult;
import it.denzosoft.jreverse.core.model.RestEndpointAnalysisResult;
import it.denzosoft.jreverse.core.model.WebMvcAnalysisResult;
import it.denzosoft.jreverse.core.port.ComponentScanAnalyzer;
import it.denzosoft.jreverse.core.port.ConfigurationAnalyzer;
import it.denzosoft.jreverse.core.port.MainMethodAnalyzer;
import it.denzosoft.jreverse.core.port.RestEndpointAnalyzer;
import it.denzosoft.jreverse.core.port.WebMvcAnalyzer;

/**
 * Report generator specifically designed for Spring Boot applications.
 * Generates comprehensive reports using Spring Boot specific analyzers.
 */
public class SpringBootReportGenerator {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(SpringBootReportGenerator.class);
    
    private final MainMethodAnalyzer mainMethodAnalyzer;
    private final ComponentScanAnalyzer componentScanAnalyzer;
    private final RestEndpointAnalyzer restEndpointAnalyzer;
    private final WebMvcAnalyzer webMvcAnalyzer;
    private final ConfigurationAnalyzer configurationAnalyzer;
    
    public SpringBootReportGenerator(MainMethodAnalyzer mainMethodAnalyzer,
                                   ComponentScanAnalyzer componentScanAnalyzer,
                                   RestEndpointAnalyzer restEndpointAnalyzer,
                                   WebMvcAnalyzer webMvcAnalyzer,
                                   ConfigurationAnalyzer configurationAnalyzer) {
        this.mainMethodAnalyzer = mainMethodAnalyzer;
        this.componentScanAnalyzer = componentScanAnalyzer;
        this.restEndpointAnalyzer = restEndpointAnalyzer;
        this.webMvcAnalyzer = webMvcAnalyzer;
        this.configurationAnalyzer = configurationAnalyzer;
    }
    
    /**
     * Generates a comprehensive Spring Boot entrypoint analysis report.
     */
    public SpringBootEntrypointReport generateEntrypointReport(JarContent jarContent) {
        LOGGER.info("Generating Spring Boot entrypoint report for: %s", jarContent.getLocation().getFileName());
        long startTime = System.currentTimeMillis();
        
        SpringBootEntrypointReport.Builder reportBuilder = SpringBootEntrypointReport.builder()
                .jarLocation(jarContent.getLocation().toString());
        
        try {
            // Analyze main method and SpringApplication.run()
            MainMethodAnalysisResult mainMethodResult = mainMethodAnalyzer.analyzeMainMethod(jarContent);
            reportBuilder.mainMethodAnalysis(mainMethodResult);
            
            // Analyze component scan configurations
            ComponentScanAnalysisResult componentScanResult = componentScanAnalyzer.analyzeComponentScan(jarContent);
            reportBuilder.componentScanAnalysis(componentScanResult);
            
            // Analyze REST endpoints
            RestEndpointAnalysisResult restEndpointResult = restEndpointAnalyzer.analyzeRestEndpoints(jarContent);
            reportBuilder.restEndpointAnalysis(restEndpointResult);
            
            // Analyze Spring MVC mappings
            WebMvcAnalysisResult webMvcResult = webMvcAnalyzer.analyzeWebMvcMappings(jarContent);
            reportBuilder.webMvcAnalysis(webMvcResult);
            
            // Analyze Spring configurations
            ConfigurationAnalysisResult configurationResult = configurationAnalyzer.analyzeConfigurations(jarContent);
            reportBuilder.configurationAnalysis(configurationResult);
            
            long analysisTime = System.currentTimeMillis() - startTime;
            reportBuilder.analysisTimeMs(analysisTime);
            
            LOGGER.info("Spring Boot entrypoint report generated in %dms", analysisTime);
            return reportBuilder.build();
            
        } catch (Exception e) {
            LOGGER.error("Error generating Spring Boot entrypoint report: " + e.getMessage(), e);
            return SpringBootEntrypointReport.builder()
                    .jarLocation(jarContent.getLocation().toString())
                    .error("Failed to generate report: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Generates a comprehensive Spring Boot architecture report.
     */
    public SpringBootArchitectureReport generateArchitectureReport(JarContent jarContent) {
        LOGGER.info("Generating Spring Boot architecture report for: %s", jarContent.getLocation().getFileName());
        long startTime = System.currentTimeMillis();
        
        SpringBootArchitectureReport.Builder reportBuilder = SpringBootArchitectureReport.builder()
                .jarLocation(jarContent.getLocation().toString());
        
        try {
            // Analyze configurations and bean definitions
            ConfigurationAnalysisResult configurationResult = configurationAnalyzer.analyzeConfigurations(jarContent);
            reportBuilder.configurationAnalysis(configurationResult);
            
            // Analyze component scanning strategy
            ComponentScanAnalysisResult componentScanResult = componentScanAnalyzer.analyzeComponentScan(jarContent);
            reportBuilder.componentScanAnalysis(componentScanResult);
            
            // Analyze web layer architecture
            RestEndpointAnalysisResult restEndpointResult = restEndpointAnalyzer.analyzeRestEndpoints(jarContent);
            WebMvcAnalysisResult webMvcResult = webMvcAnalyzer.analyzeWebMvcMappings(jarContent);
            reportBuilder.restEndpointAnalysis(restEndpointResult)
                         .webMvcAnalysis(webMvcResult);
            
            long analysisTime = System.currentTimeMillis() - startTime;
            reportBuilder.analysisTimeMs(analysisTime);
            
            LOGGER.info("Spring Boot architecture report generated in %dms", analysisTime);
            return reportBuilder.build();
            
        } catch (Exception e) {
            LOGGER.error("Error generating Spring Boot architecture report: " + e.getMessage(), e);
            return SpringBootArchitectureReport.builder()
                    .jarLocation(jarContent.getLocation().toString())
                    .error("Failed to generate report: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Generates a Spring Boot web layer analysis report.
     */
    public SpringBootWebLayerReport generateWebLayerReport(JarContent jarContent) {
        LOGGER.info("Generating Spring Boot web layer report for: %s", jarContent.getLocation().getFileName());
        long startTime = System.currentTimeMillis();
        
        SpringBootWebLayerReport.Builder reportBuilder = SpringBootWebLayerReport.builder()
                .jarLocation(jarContent.getLocation().toString());
        
        try {
            // Analyze REST endpoints
            RestEndpointAnalysisResult restEndpointResult = restEndpointAnalyzer.analyzeRestEndpoints(jarContent);
            reportBuilder.restEndpointAnalysis(restEndpointResult);
            
            // Analyze Spring MVC mappings and patterns
            WebMvcAnalysisResult webMvcResult = webMvcAnalyzer.analyzeWebMvcMappings(jarContent);
            reportBuilder.webMvcAnalysis(webMvcResult);
            
            long analysisTime = System.currentTimeMillis() - startTime;
            reportBuilder.analysisTimeMs(analysisTime);
            
            LOGGER.info("Spring Boot web layer report generated in %dms", analysisTime);
            return reportBuilder.build();
            
        } catch (Exception e) {
            LOGGER.error("Error generating Spring Boot web layer report: " + e.getMessage(), e);
            return SpringBootWebLayerReport.builder()
                    .jarLocation(jarContent.getLocation().toString())
                    .error("Failed to generate report: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Checks if this generator can process the given JAR content.
     */
    public boolean canGenerate(JarContent jarContent) {
        return jarContent != null && 
               mainMethodAnalyzer.canAnalyze(jarContent) &&
               componentScanAnalyzer.canAnalyze(jarContent) &&
               restEndpointAnalyzer.canAnalyze(jarContent) &&
               webMvcAnalyzer.canAnalyze(jarContent) &&
               configurationAnalyzer.canAnalyze(jarContent);
    }
}