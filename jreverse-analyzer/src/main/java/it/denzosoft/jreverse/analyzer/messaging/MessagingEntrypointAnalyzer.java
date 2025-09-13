package it.denzosoft.jreverse.analyzer.messaging;

import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.JarContent;

/**
 * Analyzer for messaging entrypoints in Spring applications.
 * Detects and analyzes messaging patterns including JMS, Kafka, RabbitMQ, WebSocket, and event listeners.
 */
public interface MessagingEntrypointAnalyzer {
    
    /**
     * Analyzes messaging entrypoints in the provided JAR.
     *
     * @param jarContent the JAR content containing classes to analyze
     * @return comprehensive messaging analysis results
     */
    MessagingAnalysisResult analyze(JarContent jarContent);
    
    /**
     * Analyzes messaging entrypoints in a specific class.
     *
     * @param classInfo the class to analyze for messaging entrypoints
     * @return messaging analysis result for the specific class
     */
    MessagingAnalysisResult analyzeClass(ClassInfo classInfo);
}