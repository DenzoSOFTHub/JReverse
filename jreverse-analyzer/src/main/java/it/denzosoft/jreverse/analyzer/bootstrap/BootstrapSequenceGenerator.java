package it.denzosoft.jreverse.analyzer.bootstrap;

import it.denzosoft.jreverse.analyzer.bootstrap.BootstrapAnalyzer.SequenceDiagramFormat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generator for creating sequence diagrams from Spring Boot bootstrap analysis results.
 * Supports multiple output formats including PlantUML, Mermaid, and text-based diagrams.
 */
public class BootstrapSequenceGenerator {
    
    private static final String PLANTUML_THEME = "cerulean-outline";
    private static final int MAX_CLASS_NAME_LENGTH = 20;
    
    /**
     * Generates a sequence diagram from bootstrap analysis results.
     * 
     * @param analysisResult the bootstrap analysis result
     * @param format the desired output format
     * @return the generated sequence diagram as a string
     */
    public String generateSequenceDiagram(BootstrapAnalysisResult analysisResult, 
                                         SequenceDiagramFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("Sequence diagram format cannot be null");
        }
        
        if (analysisResult == null || analysisResult.getSequenceSteps().isEmpty()) {
            return generateEmptyDiagram(format);
        }
        
        switch (format) {
            case PLANTUML:
                return generatePlantUMLDiagram(analysisResult);
            case MERMAID:
                return generateMermaidDiagram(analysisResult);
            case TEXT:
                return generateTextDiagram(analysisResult);
            default:
                throw new IllegalArgumentException("Unsupported diagram format: " + format);
        }
    }
    
    /**
     * Generates a PlantUML sequence diagram.
     */
    private String generatePlantUMLDiagram(BootstrapAnalysisResult analysisResult) {
        StringBuilder diagram = new StringBuilder();
        
        // Header
        diagram.append("@startuml\n");
        diagram.append("!theme ").append(PLANTUML_THEME).append("\n");
        diagram.append("title Spring Boot Bootstrap Sequence\n\n");
        
        // Participants
        Set<String> participants = getUniqueParticipants(analysisResult.getSequenceSteps());
        for (String participant : participants) {
            String shortName = getShortClassName(participant);
            diagram.append("participant \"").append(shortName).append("\" as ").append(getParticipantId(participant)).append("\n");
        }
        diagram.append("\n");
        
        // Sequence steps grouped by phase
        for (BootstrapSequencePhase phase : BootstrapSequencePhase.values()) {
            List<BootstrapSequenceStep> phaseSteps = analysisResult.getStepsForPhase(phase);
            if (!phaseSteps.isEmpty()) {
                diagram.append("== ").append(phase.getDisplayName()).append(" ==\n");
                
                for (BootstrapSequenceStep step : phaseSteps) {
                    generatePlantUMLStep(diagram, step);
                }
                diagram.append("\n");
            }
        }
        
        // Footer with timing information if available
        if (analysisResult.getTimingInfo().hasTimingData()) {
            diagram.append("note over ").append(getFirstParticipant(participants)).append("\n");
            diagram.append("Total estimated time: ").append(analysisResult.getTimingInfo().getFormattedTotalDuration()).append("\n");
            diagram.append("Components discovered: ").append(analysisResult.getComponentCount()).append("\n");
            diagram.append("Auto-configurations: ").append(analysisResult.getAutoConfigurationCount()).append("\n");
            diagram.append("end note\n");
        }
        
        diagram.append("@enduml");
        return diagram.toString();
    }
    
    /**
     * Generates a Mermaid sequence diagram.
     */
    private String generateMermaidDiagram(BootstrapAnalysisResult analysisResult) {
        StringBuilder diagram = new StringBuilder();
        
        // Header
        diagram.append("sequenceDiagram\n");
        diagram.append("    title Spring Boot Bootstrap Sequence\n\n");
        
        // Sequence steps grouped by phase
        for (BootstrapSequencePhase phase : BootstrapSequencePhase.values()) {
            List<BootstrapSequenceStep> phaseSteps = analysisResult.getStepsForPhase(phase);
            if (!phaseSteps.isEmpty()) {
                diagram.append("    Note over ").append(getShortClassName(phaseSteps.get(0).getParticipantClass()))
                        .append(": ").append(phase.getDisplayName()).append("\n");
                
                for (BootstrapSequenceStep step : phaseSteps) {
                    generateMermaidStep(diagram, step);
                }
                diagram.append("\n");
            }
        }
        
        // Footer with summary
        if (analysisResult.getTimingInfo().hasTimingData()) {
            String firstParticipant = getShortClassName(analysisResult.getSequenceSteps().get(0).getParticipantClass());
            diagram.append("    Note over ").append(firstParticipant).append(": ");
            diagram.append("Total time: ").append(analysisResult.getTimingInfo().getFormattedTotalDuration());
            diagram.append(", Components: ").append(analysisResult.getComponentCount()).append("\n");
        }
        
        return diagram.toString();
    }
    
    /**
     * Generates a text-based sequence diagram.
     */
    private String generateTextDiagram(BootstrapAnalysisResult analysisResult) {
        StringBuilder diagram = new StringBuilder();
        
        diagram.append("Spring Boot Bootstrap Sequence\n");
        diagram.append("=" .repeat(50)).append("\n\n");
        
        // Summary
        diagram.append("Summary:\n");
        diagram.append("- Total steps: ").append(analysisResult.getTotalSteps()).append("\n");
        diagram.append("- Components discovered: ").append(analysisResult.getComponentCount()).append("\n");
        diagram.append("- Auto-configurations: ").append(analysisResult.getAutoConfigurationCount()).append("\n");
        if (analysisResult.getTimingInfo().hasTimingData()) {
            diagram.append("- Estimated total time: ").append(analysisResult.getTimingInfo().getFormattedTotalDuration()).append("\n");
        }
        diagram.append("\n");
        
        // Sequence steps by phase
        for (BootstrapSequencePhase phase : BootstrapSequencePhase.values()) {
            List<BootstrapSequenceStep> phaseSteps = analysisResult.getStepsForPhase(phase);
            if (!phaseSteps.isEmpty()) {
                diagram.append("Phase: ").append(phase.getDisplayName()).append("\n");
                diagram.append("-".repeat(phase.getDisplayName().length() + 7)).append("\n");
                
                for (BootstrapSequenceStep step : phaseSteps) {
                    generateTextStep(diagram, step);
                }
                diagram.append("\n");
            }
        }
        
        diagram.append("End of Sequence");
        return diagram.toString();
    }
    
    /**
     * Generates a single PlantUML step.
     */
    private void generatePlantUMLStep(StringBuilder diagram, BootstrapSequenceStep step) {
        String fromParticipant = getParticipantId(step.getParticipantClass());
        String toParticipant = fromParticipant; // Self-call for method execution
        
        if (step.isEvent()) {
            diagram.append(fromParticipant).append(" --> ").append(toParticipant).append(": ");
        } else {
            diagram.append(fromParticipant).append(" -> ").append(toParticipant).append(": ");
        }
        
        diagram.append(step.getFormattedMethodSignature());
        
        if (step.getEstimatedDurationMs() > 0) {
            diagram.append(" (").append(step.getEstimatedDurationMs()).append("ms)");
        }
        
        diagram.append("\n");
        
        // Add note with description if it's different from method name
        if (!step.getDescription().equals(step.getMethodName())) {
            diagram.append("note right: ").append(step.getDescription()).append("\n");
        }
    }
    
    /**
     * Generates a single Mermaid step.
     */
    private void generateMermaidStep(StringBuilder diagram, BootstrapSequenceStep step) {
        String fromParticipant = getShortClassName(step.getParticipantClass());
        String toParticipant = fromParticipant;
        
        diagram.append("    ").append(fromParticipant).append("->>").append(toParticipant).append(": ");
        diagram.append(step.getFormattedMethodSignature());
        
        if (step.getEstimatedDurationMs() > 0) {
            diagram.append(" (").append(step.getEstimatedDurationMs()).append("ms)");
        }
        
        diagram.append("\n");
    }
    
    /**
     * Generates a single text step.
     */
    private void generateTextStep(StringBuilder diagram, BootstrapSequenceStep step) {
        diagram.append("  ").append(step.getSequenceNumber()).append(". ");
        diagram.append(getShortClassName(step.getParticipantClass())).append(".").append(step.getFormattedMethodSignature());
        
        if (step.getEstimatedDurationMs() > 0) {
            diagram.append(" (").append(step.getEstimatedDurationMs()).append("ms)");
        }
        
        diagram.append("\n");
        
        if (!step.getDescription().equals(step.getMethodName())) {
            diagram.append("     ↳ ").append(step.getDescription()).append("\n");
        }
    }
    
    /**
     * Generates an empty diagram for cases where no sequence is available.
     */
    private String generateEmptyDiagram(SequenceDiagramFormat format) {
        switch (format) {
            case PLANTUML:
                return "@startuml\ntitle No Bootstrap Sequence Detected\nnote as N1\nNo Spring Boot bootstrap sequence\ncould be analyzed from this JAR\nend note\n@enduml";
            case MERMAID:
                return "sequenceDiagram\n    title No Bootstrap Sequence Detected\n    Note over Application: No Spring Boot sequence detected";
            case TEXT:
                return "Spring Boot Bootstrap Sequence\n" +
                       "================================\n\n" +
                       "No bootstrap sequence could be detected in this JAR file.\n" +
                       "This might be a regular Java application or the analysis was unable\n" +
                       "to identify Spring Boot components.\n\n" +
                       "End of Sequence";
            default:
                return "No sequence diagram available";
        }
    }
    
    /**
     * Extracts unique participants from sequence steps.
     */
    private Set<String> getUniqueParticipants(List<BootstrapSequenceStep> steps) {
        return steps.stream()
                .map(BootstrapSequenceStep::getParticipantClass)
                .collect(Collectors.toSet());
    }
    
    /**
     * Converts a class name to a PlantUML participant ID.
     */
    private String getParticipantId(String className) {
        return getShortClassName(className).replaceAll("[^a-zA-Z0-9]", "");
    }
    
    /**
     * Gets the first participant for diagram annotations.
     */
    private String getFirstParticipant(Set<String> participants) {
        return participants.stream()
                .findFirst()
                .map(this::getParticipantId)
                .orElse("Application");
    }
    
    /**
     * Extracts short class name from fully qualified name.
     */
    private String getShortClassName(String fullyQualifiedName) {
        if (fullyQualifiedName == null || fullyQualifiedName.isEmpty()) {
            return "Unknown";
        }
        
        String shortName = fullyQualifiedName.contains(".") ? 
            fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('.') + 1) : 
            fullyQualifiedName;
        
        // Truncate if too long
        if (shortName.length() > MAX_CLASS_NAME_LENGTH) {
            shortName = shortName.substring(0, MAX_CLASS_NAME_LENGTH - 3) + "...";
        }
        
        return shortName;
    }
    
    /**
     * Generates a summary diagram showing phase timing distribution.
     */
    public String generatePhaseSummaryDiagram(BootstrapAnalysisResult analysisResult, 
                                            SequenceDiagramFormat format) {
        if (!analysisResult.getTimingInfo().hasTimingData()) {
            return generateEmptyDiagram(format);
        }
        
        StringBuilder diagram = new StringBuilder();
        
        if (format == SequenceDiagramFormat.TEXT) {
            diagram.append("Bootstrap Phase Summary\n");
            diagram.append("=".repeat(25)).append("\n\n");
            
            for (BootstrapSequencePhase phase : BootstrapSequencePhase.values()) {
                if (analysisResult.hasPhase(phase)) {
                    long duration = analysisResult.getTimingInfo().getPhaseDuration(phase);
                    double percentage = analysisResult.getTimingInfo().getPhasePercentage(phase);
                    int stepCount = analysisResult.getStepsForPhase(phase).size();
                    
                    diagram.append(String.format("%-25s: %6s (%.1f%%) - %d steps\n",
                        phase.getDisplayName(),
                        analysisResult.getTimingInfo().getFormattedPhaseDuration(phase),
                        percentage,
                        stepCount));
                }
            }
        } else {
            // For PlantUML and Mermaid, generate a simple timing chart
            return generateTimingChart(analysisResult, format);
        }
        
        return diagram.toString();
    }
    
    /**
     * Generates a timing chart for phase durations.
     */
    private String generateTimingChart(BootstrapAnalysisResult analysisResult, 
                                     SequenceDiagramFormat format) {
        // This would be a more complex chart generation
        // For now, return a simplified version
        return generateEmptyDiagram(format);
    }
}