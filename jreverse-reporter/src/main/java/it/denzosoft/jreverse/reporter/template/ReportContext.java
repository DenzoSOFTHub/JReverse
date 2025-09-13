package it.denzosoft.jreverse.reporter.template;

import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.port.ReportType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Context object containing all data needed for report generation.
 */
public class ReportContext {
    
    private ReportType reportType;
    private JarContent jarContent;
    private Map<String, Object> analysisResults = new HashMap<>();
    private LocalDateTime generationTime;
    private String reportTitle;
    private Map<String, Object> additionalProperties = new HashMap<>();
    
    public ReportType getReportType() {
        return reportType;
    }
    
    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }
    
    public JarContent getJarContent() {
        return jarContent;
    }
    
    public void setJarContent(JarContent jarContent) {
        this.jarContent = jarContent;
    }
    
    public Map<String, Object> getAnalysisResults() {
        return analysisResults;
    }
    
    public void setAnalysisResults(Map<String, Object> analysisResults) {
        this.analysisResults = analysisResults != null ? analysisResults : new HashMap<>();
    }
    
    public LocalDateTime getGenerationTime() {
        return generationTime;
    }
    
    public void setGenerationTime(LocalDateTime generationTime) {
        this.generationTime = generationTime;
    }
    
    public String getReportTitle() {
        return reportTitle;
    }
    
    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }
    
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }
    
    public void addProperty(String key, Object value) {
        this.additionalProperties.put(key, value);
    }
    
    public Object getProperty(String key) {
        return this.additionalProperties.get(key);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, Class<T> type) {
        Object value = getProperty(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
}