package it.denzosoft.jreverse.core.port;

/**
 * Enumeration of supported report output formats.
 */
public enum ReportFormat {
    HTML("html", "text/html", "HTML Report"),
    PDF("pdf", "application/pdf", "PDF Report"),
    JSON("json", "application/json", "JSON Report"),
    XML("xml", "application/xml", "XML Report"),
    CSV("csv", "text/csv", "CSV Report"),
    MARKDOWN("md", "text/markdown", "Markdown Report");
    
    private final String extension;
    private final String mimeType;
    private final String displayName;
    
    ReportFormat(String extension, String mimeType, String displayName) {
        this.extension = extension;
        this.mimeType = mimeType;
        this.displayName = displayName;
    }
    
    public String getExtension() {
        return extension;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getFileName(String baseName) {
        return baseName + "." + extension;
    }
}