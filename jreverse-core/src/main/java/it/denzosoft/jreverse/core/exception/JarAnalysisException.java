package it.denzosoft.jreverse.core.exception;

/**
 * Base exception for JAR analysis operations.
 * Follows Clean Architecture principles for domain exceptions.
 */
public class JarAnalysisException extends Exception {
    
    private final String jarPath;
    private final ErrorCode errorCode;
    
    public JarAnalysisException(String message, String jarPath, ErrorCode errorCode) {
        super(message);
        this.jarPath = jarPath;
        this.errorCode = errorCode;
    }
    
    public JarAnalysisException(String message, String jarPath, ErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.jarPath = jarPath;
        this.errorCode = errorCode;
    }
    
    public String getJarPath() {
        return jarPath;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    public enum ErrorCode {
        FILE_NOT_FOUND("JAR_FILE_NOT_FOUND"),
        ACCESS_DENIED("JAR_ACCESS_DENIED"),
        INVALID_FORMAT("JAR_INVALID_FORMAT"),
        CORRUPTED_FILE("JAR_CORRUPTED_FILE"),
        ANALYSIS_FAILED("JAR_ANALYSIS_FAILED"),
        UNSUPPORTED_VERSION("JAR_UNSUPPORTED_VERSION"),
        MEMORY_LIMIT_EXCEEDED("JAR_MEMORY_LIMIT_EXCEEDED"),
        TIMEOUT("JAR_ANALYSIS_TIMEOUT");
        
        private final String code;
        
        ErrorCode(String code) {
            this.code = code;
        }
        
        public String getCode() {
            return code;
        }
    }
}