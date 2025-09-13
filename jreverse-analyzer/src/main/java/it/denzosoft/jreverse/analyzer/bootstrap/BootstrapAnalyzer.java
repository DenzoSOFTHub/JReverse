package it.denzosoft.jreverse.analyzer.bootstrap;

import it.denzosoft.jreverse.core.model.JarContent;

/**
 * Analyzer for identifying and analyzing Spring Boot bootstrap sequences.
 * Analyzes the application startup flow from main method through SpringApplication.run()
 * to provide comprehensive sequence diagrams and timing information.
 * 
 * This analyzer integrates with MainMethodAnalyzer, ComponentScanAnalyzer, and BeanCreationAnalyzer
 * to provide a complete picture of the Spring Boot bootstrap process.
 */
public interface BootstrapAnalyzer {
    
    /**
     * Analyzes the JAR content to identify and reconstruct the Spring Boot bootstrap sequence.
     * This includes:
     * - Main method identification and analysis
     * - SpringApplication.run() call flow
     * - Component scanning sequence
     * - Bean creation and dependency injection order
     * - Auto-configuration activation
     * - Application ready event timeline
     *
     * @param jarContent the JAR content to analyze
     * @return comprehensive bootstrap analysis result with sequence diagrams and timing
     */
    BootstrapAnalysisResult analyzeBootstrap(JarContent jarContent);
    
    /**
     * Checks if this analyzer can analyze the given JAR content.
     * The analyzer can process content that contains Java classes and potentially Spring Boot components.
     *
     * @param jarContent the JAR content to check
     * @return true if the analyzer can process this content
     */
    default boolean canAnalyze(JarContent jarContent) {
        return jarContent != null && jarContent.getClassCount() > 0;
    }
    
    /**
     * Checks if the given JAR content appears to be a Spring Boot application.
     * This is used to determine the appropriate analysis strategy.
     *
     * @param jarContent the JAR content to check
     * @return true if the content appears to be a Spring Boot application
     */
    default boolean isSpringBootApplication(JarContent jarContent) {
        if (!canAnalyze(jarContent)) {
            return false;
        }
        
        // Check for Spring Boot indicators
        return jarContent.getClasses().stream().anyMatch(classInfo -> 
            classInfo.getAnnotations().stream().anyMatch(annotation ->
                annotation.getType().contains("SpringBootApplication") ||
                annotation.getType().contains("EnableAutoConfiguration")
            )
        ) || hasSpringBootDependencies(jarContent);
    }
    
    /**
     * Checks for Spring Boot dependencies in the JAR manifest or class presence.
     *
     * @param jarContent the JAR content to check
     * @return true if Spring Boot dependencies are detected
     */
    default boolean hasSpringBootDependencies(JarContent jarContent) {
        // Check for key Spring Boot classes
        return jarContent.getClasses().stream().anyMatch(classInfo -> {
            String className = classInfo.getFullyQualifiedName();
            return className.startsWith("org.springframework.boot.") ||
                   className.equals("org.springframework.boot.SpringApplication") ||
                   className.startsWith("org.springframework.boot.autoconfigure.");
        });
    }
    
    /**
     * Returns the supported sequence diagram formats for this analyzer.
     * 
     * @return array of supported diagram formats
     */
    default SequenceDiagramFormat[] getSupportedDiagramFormats() {
        return new SequenceDiagramFormat[] {
            SequenceDiagramFormat.PLANTUML,
            SequenceDiagramFormat.MERMAID,
            SequenceDiagramFormat.TEXT
        };
    }
    
    /**
     * Supported sequence diagram formats.
     */
    enum SequenceDiagramFormat {
        /**
         * PlantUML sequence diagram format.
         */
        PLANTUML("plantuml", "@startuml", "@enduml"),
        
        /**
         * Mermaid sequence diagram format.
         */
        MERMAID("mermaid", "sequenceDiagram", ""),
        
        /**
         * Simple text-based sequence format.
         */
        TEXT("text", "Sequence Diagram:", "End of Sequence");
        
        private final String formatName;
        private final String startMarker;
        private final String endMarker;
        
        SequenceDiagramFormat(String formatName, String startMarker, String endMarker) {
            this.formatName = formatName;
            this.startMarker = startMarker;
            this.endMarker = endMarker;
        }
        
        public String getFormatName() { return formatName; }
        public String getStartMarker() { return startMarker; }
        public String getEndMarker() { return endMarker; }
    }
}