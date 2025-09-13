package it.denzosoft.jreverse.analyzer.springboot;

import it.denzosoft.jreverse.core.exception.JarAnalysisException;
import it.denzosoft.jreverse.core.exception.JarAnalysisException.ErrorCode;

/**
 * Exception thrown during Spring Boot analysis operations.
 * Extends the base JAR analysis exception with Spring Boot specific context.
 */
public class SpringBootAnalysisException extends JarAnalysisException {
    
    private final String indicatorType;
    private final String jarPath;
    
    /**
     * Creates a new Spring Boot analysis exception.
     * 
     * @param message the error message
     * @param jarPath the JAR path being analyzed
     */
    public SpringBootAnalysisException(String message, String jarPath) {
        super(message, jarPath, ErrorCode.ANALYSIS_FAILED);
        this.indicatorType = null;
        this.jarPath = jarPath;
    }
    
    /**
     * Creates a new Spring Boot analysis exception with cause.
     * 
     * @param message the error message
     * @param jarPath the JAR path being analyzed
     * @param cause the underlying cause
     */
    public SpringBootAnalysisException(String message, String jarPath, Throwable cause) {
        super(message, jarPath, ErrorCode.ANALYSIS_FAILED, cause);
        this.indicatorType = null;
        this.jarPath = jarPath;
    }
    
    /**
     * Creates a new Spring Boot analysis exception with indicator context.
     * 
     * @param message the error message
     * @param indicatorType the indicator that failed
     * @param jarPath the JAR path being analyzed
     */
    public SpringBootAnalysisException(String message, String indicatorType, String jarPath) {
        super(message, jarPath, ErrorCode.ANALYSIS_FAILED);
        this.indicatorType = indicatorType;
        this.jarPath = jarPath;
    }
    
    /**
     * Creates a new Spring Boot analysis exception with full context.
     * 
     * @param message the error message
     * @param cause the underlying cause
     * @param indicatorType the indicator that failed
     * @param jarPath the JAR path being analyzed
     */
    public SpringBootAnalysisException(String message, Throwable cause, String indicatorType, String jarPath) {
        super(message, jarPath, ErrorCode.ANALYSIS_FAILED, cause);
        this.indicatorType = indicatorType;
        this.jarPath = jarPath;
    }
    
    /**
     * Gets the indicator type that failed, if available.
     */
    public String getIndicatorType() {
        return indicatorType;
    }
    
    /**
     * Gets the JAR path that was being analyzed, if available.
     */
    public String getJarPath() {
        return jarPath;
    }
    
    /**
     * Checks if this exception has indicator context.
     */
    public boolean hasIndicatorContext() {
        return indicatorType != null;
    }
    
    /**
     * Checks if this exception has JAR context.
     */
    public boolean hasJarContext() {
        return jarPath != null;
    }
    
    @Override
    public String getMessage() {
        StringBuilder message = new StringBuilder(super.getMessage());
        
        if (hasIndicatorContext()) {
            message.append(" [Indicator: ").append(indicatorType).append("]");
        }
        
        if (hasJarContext()) {
            message.append(" [JAR: ").append(jarPath).append("]");
        }
        
        return message.toString();
    }
    
    /**
     * Creates an exception for indicator analysis failure.
     */
    public static SpringBootAnalysisException indicatorFailed(String indicatorType, String jarPath, Throwable cause) {
        return new SpringBootAnalysisException(
            "Spring Boot indicator analysis failed",
            cause,
            indicatorType,
            jarPath
        );
    }
    
    /**
     * Creates an exception for detection engine failure.
     */
    public static SpringBootAnalysisException engineFailed(String engineName, String jarPath, Throwable cause) {
        return new SpringBootAnalysisException(
            "Spring Boot detection engine '" + engineName + "' failed",
            cause,
            null,
            jarPath
        );
    }
    
    /**
     * Creates an exception for bytecode analysis failure.
     */
    public static SpringBootAnalysisException bytecodeAnalysisFailed(String className, String jarPath, Throwable cause) {
        return new SpringBootAnalysisException(
            "Bytecode analysis failed for class: " + className,
            jarPath,
            cause
        );
    }
    
    /**
     * Creates an exception for JAR structure analysis failure.
     */
    public static SpringBootAnalysisException jarStructureAnalysisFailed(String jarPath, Throwable cause) {
        return new SpringBootAnalysisException(
            "JAR structure analysis failed",
            cause,
            "JAR_STRUCTURE",
            jarPath
        );
    }
    
    /**
     * Creates an exception for manifest analysis failure.
     */
    public static SpringBootAnalysisException manifestAnalysisFailed(String jarPath, Throwable cause) {
        return new SpringBootAnalysisException(
            "JAR manifest analysis failed",
            cause,
            "MANIFEST",
            jarPath
        );
    }
}