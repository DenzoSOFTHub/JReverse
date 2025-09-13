package it.denzosoft.jreverse.core.model;

import java.util.Optional;

/**
 * Result of main method analysis in a JAR file.
 * Contains information about found main methods and Spring Boot patterns.
 */
public class MainMethodAnalysisResult {
    
    private final MainMethodType type;
    private final Optional<MethodInfo> mainMethod;
    private final Optional<SpringApplicationCallInfo> springApplicationCall;
    private final String declaringClassName;
    private final AnalysisMetadata metadata;
    private final long analysisTimeMs;
    
    private MainMethodAnalysisResult(MainMethodType type,
                                   Optional<MethodInfo> mainMethod,
                                   Optional<SpringApplicationCallInfo> springApplicationCall,
                                   String declaringClassName,
                                   AnalysisMetadata metadata,
                                   long analysisTimeMs) {
        this.type = type;
        this.mainMethod = mainMethod;
        this.springApplicationCall = springApplicationCall;
        this.declaringClassName = declaringClassName;
        this.metadata = metadata;
        this.analysisTimeMs = analysisTimeMs;
    }
    
    public static MainMethodAnalysisResult springBootMain(MethodInfo mainMethod,
                                                        String declaringClassName,
                                                        SpringApplicationCallInfo callInfo) {
        return new MainMethodAnalysisResult(
            MainMethodType.SPRING_BOOT,
            Optional.of(mainMethod),
            Optional.of(callInfo),
            declaringClassName,
            AnalysisMetadata.successful(),
            0L
        );
    }
    
    public static MainMethodAnalysisResult regularMain(MethodInfo mainMethod,
                                                     String declaringClassName) {
        return new MainMethodAnalysisResult(
            MainMethodType.REGULAR,
            Optional.of(mainMethod),
            Optional.empty(),
            declaringClassName,
            AnalysisMetadata.successful(),
            0L
        );
    }
    
    public static MainMethodAnalysisResult noMainFound() {
        return new MainMethodAnalysisResult(
            MainMethodType.NONE,
            Optional.empty(),
            Optional.empty(),
            null,
            AnalysisMetadata.warning("No main method found"),
            0L
        );
    }
    
    public static MainMethodAnalysisResult error(String errorMessage) {
        return new MainMethodAnalysisResult(
            MainMethodType.NONE,
            Optional.empty(),
            Optional.empty(),
            null,
            AnalysisMetadata.error(errorMessage),
            0L
        );
    }
    
    public MainMethodType getType() {
        return type;
    }
    
    public Optional<MethodInfo> getMainMethod() {
        return mainMethod;
    }
    
    public Optional<SpringApplicationCallInfo> getSpringApplicationCall() {
        return springApplicationCall;
    }
    
    public String getDeclaringClassName() {
        return declaringClassName;
    }
    
    public AnalysisMetadata getMetadata() {
        return metadata;
    }
    
    public long getAnalysisTimeMs() {
        return analysisTimeMs;
    }
    
    public boolean isSpringBootApplication() {
        return type.isSpringBoot();
    }
    
    public boolean hasMainMethod() {
        return type.hasMainMethod();
    }
    
    public boolean isSuccessful() {
        return metadata.isSuccessful();
    }
    
    public MainMethodAnalysisResult withAnalysisTime(long analysisTimeMs) {
        return new MainMethodAnalysisResult(
            type, mainMethod, springApplicationCall, 
            declaringClassName, metadata, analysisTimeMs
        );
    }
    
    @Override
    public String toString() {
        return "MainMethodAnalysisResult{" +
                "type=" + type +
                ", hasMainMethod=" + mainMethod.isPresent() +
                ", declaringClass='" + declaringClassName + '\'' +
                ", analysisTime=" + analysisTimeMs + "ms" +
                '}';
    }
}