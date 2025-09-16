package it.denzosoft.jreverse.core.model;

/**
 * Security analysis metadata model.
 */
public class SecurityAnalysisMetadata {
    private final long analysisTimeMs;
    private final String jarFileName;
    private final int classesAnalyzed;

    public SecurityAnalysisMetadata(long analysisTimeMs, String jarFileName, int classesAnalyzed) {
        this.analysisTimeMs = analysisTimeMs;
        this.jarFileName = jarFileName != null ? jarFileName : "";
        this.classesAnalyzed = Math.max(0, classesAnalyzed);
    }

    public long getAnalysisTimeMs() { return analysisTimeMs; }
    public String getJarFileName() { return jarFileName; }
    public int getClassesAnalyzed() { return classesAnalyzed; }
}