package it.denzosoft.jreverse.analyzer.scheduling;

import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.JarContent;

/**
 * Analyzer for scheduling entrypoints in Spring applications.
 * Detects and analyzes scheduling patterns including @Scheduled, cron expressions, TaskScheduler, etc.
 */
public interface SchedulingEntrypointAnalyzer {
    
    /**
     * Analyzes scheduling entrypoints in the provided JAR.
     *
     * @param jarContent the JAR content containing classes to analyze
     * @return comprehensive scheduling analysis results
     */
    SchedulingAnalysisResult analyze(JarContent jarContent);
    
    /**
     * Analyzes scheduling entrypoints in a specific class.
     *
     * @param classInfo the class to analyze for scheduling entrypoints
     * @return scheduling analysis result for the specific class
     */
    SchedulingAnalysisResult analyzeClass(ClassInfo classInfo);
}