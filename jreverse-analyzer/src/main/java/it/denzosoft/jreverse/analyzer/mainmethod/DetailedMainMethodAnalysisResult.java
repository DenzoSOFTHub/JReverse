package it.denzosoft.jreverse.analyzer.mainmethod;

import java.util.Optional;

/**
 * Result of main method analysis containing information about discovered main methods
 * and their characteristics, particularly for Spring Boot applications.
 */
public class DetailedMainMethodAnalysisResult {
    
    private final MainMethodType type;
    private final Optional<MainMethodInfo> mainMethod;
    private final Optional<SpringApplicationCallInfo> springApplicationCall;
    private final AnalysisMetadata metadata;
    
    private DetailedMainMethodAnalysisResult(MainMethodType type,
                                   Optional<MainMethodInfo> mainMethod,
                                   Optional<SpringApplicationCallInfo> springApplicationCall,
                                   AnalysisMetadata metadata) {
        this.type = type;
        this.mainMethod = mainMethod;
        this.springApplicationCall = springApplicationCall;
        this.metadata = metadata;
    }
    
    /**
     * Creates a result for a Spring Boot main method.
     */
    public static DetailedMainMethodAnalysisResult springBootMain(MainMethodInfo mainMethod,
                                                         SpringApplicationCallInfo callInfo) {
        return new DetailedMainMethodAnalysisResult(
            MainMethodType.SPRING_BOOT,
            Optional.of(mainMethod),
            Optional.of(callInfo),
            AnalysisMetadata.successful()
        );
    }
    
    /**
     * Creates a result for a regular main method.
     */
    public static DetailedMainMethodAnalysisResult regularMain(MainMethodInfo mainMethod) {
        return new DetailedMainMethodAnalysisResult(
            MainMethodType.REGULAR,
            Optional.of(mainMethod),
            Optional.empty(),
            AnalysisMetadata.successful()
        );
    }
    
    /**
     * Creates a result when no main method is found.
     */
    public static DetailedMainMethodAnalysisResult noMainFound() {
        return new DetailedMainMethodAnalysisResult(
            MainMethodType.NONE,
            Optional.empty(),
            Optional.empty(),
            AnalysisMetadata.warning("No main method found")
        );
    }
    
    /**
     * Creates a result for analysis failure.
     */
    public static DetailedMainMethodAnalysisResult error(String errorMessage) {
        return new DetailedMainMethodAnalysisResult(
            MainMethodType.ERROR,
            Optional.empty(),
            Optional.empty(),
            AnalysisMetadata.error(errorMessage)
        );
    }
    
    // Getters
    public MainMethodType getType() { return type; }
    public Optional<MainMethodInfo> getMainMethod() { return mainMethod; }
    public Optional<SpringApplicationCallInfo> getSpringApplicationCall() { return springApplicationCall; }
    public AnalysisMetadata getMetadata() { return metadata; }
    
    public boolean hasMainMethod() { return mainMethod.isPresent(); }
    public boolean isSpringBootApplication() { return type == MainMethodType.SPRING_BOOT; }
    public boolean hasAnalysisWarnings() { return metadata.hasWarnings(); }
    public boolean hasAnalysisErrors() { return metadata.hasErrors(); }
}