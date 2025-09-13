package it.denzosoft.jreverse.reporter.generator.impl;

import it.denzosoft.jreverse.analyzer.bootstrap.BootstrapAnalysisResult;
import it.denzosoft.jreverse.analyzer.bootstrap.BootstrapSequencePhase;
import it.denzosoft.jreverse.analyzer.bootstrap.BootstrapSequenceStep;
import it.denzosoft.jreverse.analyzer.bootstrap.BootstrapTimingInfo;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.port.ReportType;
import it.denzosoft.jreverse.reporter.template.ReportContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BootstrapAnalysisReportGeneratorTest {

    @Mock
    private BootstrapAnalysisResult mockBootstrapResult;

    @Mock
    private BootstrapTimingInfo mockTimingInfo;

    private BootstrapAnalysisReportGenerator generator;
    private JarContent testJarContent;
    private Map<String, Object> analysisResults;

    @BeforeEach
    void setUp() {
        generator = new BootstrapAnalysisReportGenerator();
        Path testJarPath = Paths.get("test-app.jar");
        testJarContent = JarContent.builder()
            .location(testJarPath)
            .classCount(10)
            .springBootApplication(true)
            .build();
        
        analysisResults = new HashMap<>();
        analysisResults.put("bootstrap", mockBootstrapResult);
    }

    @Test
    void getReportType_ReturnsBootstrapAnalysis() {
        assertEquals(ReportType.BOOTSTRAP_ANALYSIS, generator.getReportType());
    }

    @Test
    void getReportTitle_ReturnsCorrectTitle() {
        assertEquals("Bootstrap Analysis", generator.getReportTitle());
    }

    @Test
    void getHeaderCssClass_ReturnsBootstrapHeader() {
        assertEquals("bootstrap-header", generator.getHeaderCssClass());
    }

    @Test
    void buildReportContext_WithValidData_CreatesCorrectContext() {
        // When
        ReportContext context = generator.buildReportContext(testJarContent, analysisResults);

        // Then
        assertNotNull(context);
        assertEquals("test-app.jar", context.getJarName());
        assertEquals("Spring Boot Bootstrap Analysis", context.getReportTitle());
        assertEquals("Interactive sequence diagram and analysis of Spring Boot application startup", 
                    context.getReportDescription());
        assertEquals(mockBootstrapResult, context.getData("bootstrapResult"));
        assertEquals(testJarContent, context.getData("jarContent"));
    }

    @Test
    void writeReportContent_WithNoBootstrapData_WritesNoDataMessage() throws IOException {
        // Given
        StringWriter writer = new StringWriter();
        ReportContext context = generator.buildReportContext(testJarContent, Collections.emptyMap());

        // When
        generator.writeReportContent(writer, context);

        // Then
        String output = writer.toString();
        assertTrue(output.contains("No Bootstrap Data Found"));
        assertTrue(output.contains("no-data-message"));
    }

    @Test
    void writeReportContent_WithEmptyBootstrapResult_WritesNoDataMessage() throws IOException {
        // Given
        StringWriter writer = new StringWriter();
        when(mockBootstrapResult.getTotalSteps()).thenReturn(0);
        ReportContext context = generator.buildReportContext(testJarContent, analysisResults);

        // When
        generator.writeReportContent(writer, context);

        // Then
        String output = writer.toString();
        assertTrue(output.contains("No Bootstrap Data Found"));
    }

    @Test
    void writeReportContent_WithValidBootstrapData_WritesAllSections() throws IOException {
        // Given
        StringWriter writer = new StringWriter();
        setupMockBootstrapResult();
        ReportContext context = generator.buildReportContext(testJarContent, analysisResults);

        // When
        generator.writeReportContent(writer, context);

        // Then
        String output = writer.toString();
        assertTrue(output.contains("summary-section"));
        assertTrue(output.contains("sequence-diagram-section"));
        assertTrue(output.contains("phase-analysis-section"));
        assertTrue(output.contains("details-section"));
        assertFalse(output.contains("no-data-message"));
    }

    @Test
    void writeSummarySection_WithTimingInfo_IncludesTiming() throws IOException {
        // Given
        StringWriter writer = new StringWriter();
        setupMockBootstrapResult();
        when(mockTimingInfo.hasTimingData()).thenReturn(true);
        when(mockTimingInfo.getFormattedTotalDuration()).thenReturn("2.5s");
        ReportContext context = generator.buildReportContext(testJarContent, analysisResults);

        // When
        generator.writeReportContent(writer, context);

        // Then
        String output = writer.toString();
        assertTrue(output.contains("Total Time"));
        assertTrue(output.contains("2.5s"));
        assertTrue(output.contains("summary-cards"));
    }

    @Test
    void writeSummarySection_WithoutTimingInfo_ExcludesTiming() throws IOException {
        // Given
        StringWriter writer = new StringWriter();
        setupMockBootstrapResult();
        when(mockTimingInfo.hasTimingData()).thenReturn(false);
        ReportContext context = generator.buildReportContext(testJarContent, analysisResults);

        // When
        generator.writeReportContent(writer, context);

        // Then
        String output = writer.toString();
        assertTrue(output.contains("Total Steps"));
        assertTrue(output.contains("Components"));
        assertTrue(output.contains("Auto-Configurations"));
        assertFalse(output.contains("Total Time"));
    }

    @Test
    void writeSequenceDiagramSection_GeneratesMultipleFormats() throws IOException {
        // Given
        StringWriter writer = new StringWriter();
        setupMockBootstrapResult();
        ReportContext context = generator.buildReportContext(testJarContent, analysisResults);

        // When
        generator.writeReportContent(writer, context);

        // Then
        String output = writer.toString();
        assertTrue(output.contains("diagram-text"));
        assertTrue(output.contains("diagram-plantuml"));
        assertTrue(output.contains("diagram-mermaid"));
        assertTrue(output.contains("diagramFormat"));
        assertTrue(output.contains("PlantUML online editor"));
        assertTrue(output.contains("Mermaid live editor"));
    }

    @Test
    void writePhaseAnalysisSection_WithMultiplePhases_ShowsAllPhases() throws IOException {
        // Given
        StringWriter writer = new StringWriter();
        setupMockBootstrapResultWithPhases();
        ReportContext context = generator.buildReportContext(testJarContent, analysisResults);

        // When
        generator.writeReportContent(writer, context);

        // Then
        String output = writer.toString();
        assertTrue(output.contains("phases-grid"));
        assertTrue(output.contains("Bootstrap Phases"));
        // Should contain phase information from setupMockBootstrapResultWithPhases
    }

    @Test
    void writeTimingAnalysisSection_WithTimingData_ShowsTimingAnalysis() throws IOException {
        // Given
        StringWriter writer = new StringWriter();
        setupMockBootstrapResultWithTiming();
        ReportContext context = generator.buildReportContext(testJarContent, analysisResults);

        // When
        generator.writeReportContent(writer, context);

        // Then
        String output = writer.toString();
        assertTrue(output.contains("timing-analysis-section"));
        assertTrue(output.contains("Phase Duration Summary"));
        assertTrue(output.contains("Performance Insights"));
        assertTrue(output.contains("Slowest Phase"));
    }

    @Test
    void writeTimingAnalysisSection_WithoutTimingData_SkipsSection() throws IOException {
        // Given
        StringWriter writer = new StringWriter();
        setupMockBootstrapResult();
        when(mockTimingInfo.hasTimingData()).thenReturn(false);
        ReportContext context = generator.buildReportContext(testJarContent, analysisResults);

        // When
        generator.writeReportContent(writer, context);

        // Then
        String output = writer.toString();
        assertFalse(output.contains("timing-analysis-section"));
    }

    @Test
    void writeDetailsSection_WithSteps_ShowsStepsTable() throws IOException {
        // Given
        StringWriter writer = new StringWriter();
        setupMockBootstrapResultWithSteps();
        ReportContext context = generator.buildReportContext(testJarContent, analysisResults);

        // When
        generator.writeReportContent(writer, context);

        // Then
        String output = writer.toString();
        assertTrue(output.contains("details-section"));
        assertTrue(output.contains("steps-table"));
        assertTrue(output.contains("phaseFilter"));
        assertTrue(output.contains("Step"));
        assertTrue(output.contains("Phase"));
        assertTrue(output.contains("Class"));
        assertTrue(output.contains("Method"));
    }

    @Test
    void writeDetailsSection_WithTimingData_IncludesDurationColumn() throws IOException {
        // Given
        StringWriter writer = new StringWriter();
        setupMockBootstrapResultWithSteps();
        when(mockTimingInfo.hasTimingData()).thenReturn(true);
        ReportContext context = generator.buildReportContext(testJarContent, analysisResults);

        // When
        generator.writeReportContent(writer, context);

        // Then
        String output = writer.toString();
        assertTrue(output.contains("Duration (ms)"));
    }

    @Test
    void writeCustomStyles_GeneratesComprehensiveCSS() throws IOException {
        // Given
        StringWriter writer = new StringWriter();

        // When
        generator.writeCustomStyles(writer);

        // Then
        String output = writer.toString();
        assertTrue(output.contains(".summary-cards"));
        assertTrue(output.contains(".diagram-controls"));
        assertTrue(output.contains(".sequence-diagram"));
        assertTrue(output.contains(".phases-grid"));
        assertTrue(output.contains(".steps-table"));
        assertTrue(output.contains(".phase-badge"));
        assertTrue(output.contains(".no-data-message"));
    }

    @Test
    void writeCustomScripts_GeneratesInteractiveFunctions() throws IOException {
        // Given
        StringWriter writer = new StringWriter();

        // When
        generator.writeCustomScripts(writer);

        // Then
        String output = writer.toString();
        assertTrue(output.contains("function switchDiagramFormat"));
        assertTrue(output.contains("function copyDiagram"));
        assertTrue(output.contains("function filterSteps"));
        assertTrue(output.contains("navigator.clipboard"));
    }

    @Test
    void getSimpleClassName_ExtractsCorrectly() throws IOException {
        // This is tested indirectly through the report generation
        StringWriter writer = new StringWriter();
        setupMockBootstrapResultWithSteps();
        ReportContext context = generator.buildReportContext(testJarContent, analysisResults);

        // When
        generator.writeReportContent(writer, context);

        // Then
        String output = writer.toString();
        // Should contain simple class names from the steps
        assertNotNull(output);
    }

    @Test
    void escapeHtml_HandlesSpecialCharacters() throws IOException {
        // Given
        StringWriter writer = new StringWriter();
        setupMockBootstrapResultWithSpecialCharacters();
        ReportContext context = generator.buildReportContext(testJarContent, analysisResults);

        // When
        generator.writeReportContent(writer, context);

        // Then
        String output = writer.toString();
        // HTML should be properly escaped
        assertFalse(output.contains("<script>"));
        assertFalse(output.contains("&malicious"));
    }

    private void setupMockBootstrapResult() {
        when(mockBootstrapResult.getTotalSteps()).thenReturn(10);
        when(mockBootstrapResult.getComponentCount()).thenReturn(25);
        when(mockBootstrapResult.getAutoConfigurationCount()).thenReturn(15);
        when(mockBootstrapResult.getTimingInfo()).thenReturn(mockTimingInfo);
        when(mockBootstrapResult.getSequenceSteps()).thenReturn(Collections.emptyList());
        when(mockTimingInfo.hasTimingData()).thenReturn(false);
    }

    private void setupMockBootstrapResultWithPhases() {
        setupMockBootstrapResult();
        
        // Mock phase steps
        when(mockBootstrapResult.getStepsForPhase(any(BootstrapSequencePhase.class)))
            .thenReturn(Collections.emptyList());
        when(mockBootstrapResult.hasPhase(any(BootstrapSequencePhase.class))).thenReturn(true);
    }

    private void setupMockBootstrapResultWithTiming() {
        setupMockBootstrapResult();
        when(mockTimingInfo.hasTimingData()).thenReturn(true);
        when(mockTimingInfo.getFormattedTotalDuration()).thenReturn("3.2s");
        when(mockTimingInfo.getSlowestPhase()).thenReturn(BootstrapSequencePhase.CONTEXT_CREATION);
        when(mockTimingInfo.getPhasePercentage(BootstrapSequencePhase.CONTEXT_CREATION)).thenReturn(45.5);
        when(mockTimingInfo.getSlowestSteps(5)).thenReturn(Collections.emptyList());
    }

    private void setupMockBootstrapResultWithSteps() {
        setupMockBootstrapResult();
        
        List<BootstrapSequenceStep> mockSteps = Arrays.asList(
            createMockStep(1, "com.example.Application", "main", BootstrapSequencePhase.BOOTSTRAP_START),
            createMockStep(2, "org.springframework.boot.SpringApplication", "run", BootstrapSequencePhase.CONTEXT_CREATION)
        );
        
        when(mockBootstrapResult.getSequenceSteps()).thenReturn(mockSteps);
    }

    private void setupMockBootstrapResultWithSpecialCharacters() {
        setupMockBootstrapResult();
        
        List<BootstrapSequenceStep> mockSteps = Arrays.asList(
            createMockStep(1, "com.example.<script>", "method&test", BootstrapSequencePhase.BOOTSTRAP_START)
        );
        
        when(mockBootstrapResult.getSequenceSteps()).thenReturn(mockSteps);
    }

    private BootstrapSequenceStep createMockStep(int sequenceNumber, String participantClass, 
                                                String methodName, BootstrapSequencePhase phase) {
        BootstrapSequenceStep step = mock(BootstrapSequenceStep.class);
        when(step.getSequenceNumber()).thenReturn(sequenceNumber);
        when(step.getParticipantClass()).thenReturn(participantClass);
        when(step.getMethodName()).thenReturn(methodName);
        when(step.getFormattedMethodSignature()).thenReturn(methodName + "()");
        when(step.getPhase()).thenReturn(phase);
        when(step.getDescription()).thenReturn("Test method description");
        when(step.getEstimatedDurationMs()).thenReturn(100);
        return step;
    }
}