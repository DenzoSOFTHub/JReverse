package it.denzosoft.jreverse.core.exception;

import it.denzosoft.jreverse.core.port.ReportFormat;
import it.denzosoft.jreverse.core.port.ReportType;

/**
 * Exception thrown when report generation fails.
 * Follows Clean Architecture principles for domain exceptions.
 */
public class ReportGenerationException extends Exception {
    
    private final ReportType reportType;
    private final ReportFormat reportFormat;
    private final ErrorCode errorCode;
    
    public ReportGenerationException(String message, ReportType reportType, ReportFormat reportFormat, ErrorCode errorCode) {
        super(message);
        this.reportType = reportType;
        this.reportFormat = reportFormat;
        this.errorCode = errorCode;
    }
    
    public ReportGenerationException(String message, ReportType reportType, ReportFormat reportFormat, ErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.reportType = reportType;
        this.reportFormat = reportFormat;
        this.errorCode = errorCode;
    }
    
    public ReportType getReportType() {
        return reportType;
    }
    
    public ReportFormat getReportFormat() {
        return reportFormat;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    public enum ErrorCode {
        UNSUPPORTED_REPORT_TYPE("REPORT_UNSUPPORTED_TYPE"),
        UNSUPPORTED_FORMAT("REPORT_UNSUPPORTED_FORMAT"),
        TEMPLATE_NOT_FOUND("REPORT_TEMPLATE_NOT_FOUND"),
        GENERATION_FAILED("REPORT_GENERATION_FAILED"),
        OUTPUT_ERROR("REPORT_OUTPUT_ERROR"),
        INSUFFICIENT_DATA("REPORT_INSUFFICIENT_DATA"),
        MEMORY_LIMIT_EXCEEDED("REPORT_MEMORY_LIMIT_EXCEEDED"),
        TIMEOUT("REPORT_GENERATION_TIMEOUT");
        
        private final String code;
        
        ErrorCode(String code) {
            this.code = code;
        }
        
        public String getCode() {
            return code;
        }
    }
}