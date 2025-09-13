package it.denzosoft.jreverse.reporter.generator.impl;

import it.denzosoft.jreverse.core.model.*;
import it.denzosoft.jreverse.core.port.ReportType;
import it.denzosoft.jreverse.reporter.template.ReportContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AutowiringGraphReportGenerator.
 */
@ExtendWith(MockitoExtension.class)
class AutowiringGraphReportGeneratorTest {
    
    @Mock
    private JarContent jarContent;
    
    @Mock
    private AutowiredAnalysisResult autowiredResult;
    
    private AutowiringGraphReportGenerator generator;
    
    @BeforeEach
    void setUp() {
        generator = new AutowiringGraphReportGenerator();
    }
    
    @Test
    void testGetReportType() {
        ReportType result = generator.getReportType();
        
        assertEquals(ReportType.AUTOWIRING_GRAPH, result);
    }
    
    @Test
    void testBuildReportContext_WithValidData() {
        when(jarContent.getLocation()).thenReturn(Paths.get("test.jar"));
        
        Map<String, Object> analysisResults = new HashMap<>();
        analysisResults.put("autowiring", autowiredResult);
        
        ReportContext context = generator.buildReportContext(jarContent, analysisResults);
        
        assertNotNull(context);
        assertEquals(jarContent, context.getJarContent());
        assertEquals("Autowiring Dependency Graph", context.getReportTitle());
        assertNotNull(context.getGenerationTime());
        assertEquals(autowiredResult, context.getAnalysisResults().get("autowiredResult"));
        assertEquals(jarContent, context.getAnalysisResults().get("jarContent"));
    }
    
    @Test
    void testBuildReportContext_WithNullAutowiredResult() {
        when(jarContent.getLocation()).thenReturn(Paths.get("test.jar"));
        
        Map<String, Object> analysisResults = new HashMap<>();
        analysisResults.put("autowiring", null);
        
        ReportContext context = generator.buildReportContext(jarContent, analysisResults);
        
        assertNotNull(context);
        assertEquals(jarContent, context.getJarContent());
        assertEquals("Autowiring Dependency Graph", context.getReportTitle());
        assertNull(context.getAnalysisResults().get("autowiredResult"));
    }
    
    @Test
    void testWriteReportContent_WithNullAutowiredResult() throws Exception {
        ReportContext context = new ReportContext();
        context.getAnalysisResults().put("autowiredResult", null);
        
        StringWriter writer = new StringWriter();
        generator.writeReportContent(writer, context);
        
        String output = writer.toString();
        assertTrue(output.contains("Autowiring Dependency Graph"));
        assertTrue(output.contains("No autowiring dependencies found"));
    }
    
    @Test
    void testWriteReportContent_WithEmptyDependencies() throws Exception {
        when(autowiredResult.getDependencies()).thenReturn(new ArrayList<>());
        
        ReportContext context = new ReportContext();
        context.getAnalysisResults().put("autowiredResult", autowiredResult);
        
        StringWriter writer = new StringWriter();
        generator.writeReportContent(writer, context);
        
        String output = writer.toString();
        assertTrue(output.contains("Autowiring Dependency Graph"));
        assertTrue(output.contains("No autowiring dependencies found"));
    }
    
    @Test
    void testWriteReportContent_WithValidDependencies() throws Exception {
        List<Object> mockDependencies = new ArrayList<>();
        mockDependencies.add("dependency1");
        mockDependencies.add("dependency2");
        mockDependencies.add("dependency3");
        
        when(autowiredResult.getDependencies()).thenReturn(mockDependencies);
        
        ReportContext context = new ReportContext();
        context.getAnalysisResults().put("autowiredResult", autowiredResult);
        
        StringWriter writer = new StringWriter();
        generator.writeReportContent(writer, context);
        
        String output = writer.toString();
        assertTrue(output.contains("Autowiring Dependency Graph"));
        assertTrue(output.contains("Found 3 autowired dependencies"));
        assertTrue(output.contains("Report generation completed successfully"));
    }
    
    @Test
    void testGenerateReport_Integration() throws Exception {
        when(jarContent.getLocation()).thenReturn(Paths.get("test-app.jar"));
        
        List<Object> mockDependencies = new ArrayList<>();
        mockDependencies.add("com.example.ServiceA");
        mockDependencies.add("com.example.ServiceB");
        
        when(autowiredResult.getDependencies()).thenReturn(mockDependencies);
        
        Map<String, Object> analysisResults = new HashMap<>();
        analysisResults.put("autowiring", autowiredResult);
        
        StringWriter writer = new StringWriter();
        generator.generateReport(writer, jarContent, analysisResults);
        
        String output = writer.toString();
        
        // Verify HTML structure
        assertTrue(output.contains("<!DOCTYPE html>"));
        assertTrue(output.contains("<html"));
        assertTrue(output.contains("Autowiring Dependency Graph"));
        assertTrue(output.contains("Found 2 autowired dependencies"));
        assertTrue(output.contains("</html>"));
    }
    
    @Test
    void testGenerateReport_WithNullAnalysisResults() throws Exception {
        when(jarContent.getLocation()).thenReturn(Paths.get("empty.jar"));
        
        Map<String, Object> analysisResults = new HashMap<>();
        
        StringWriter writer = new StringWriter();
        generator.generateReport(writer, jarContent, analysisResults);
        
        String output = writer.toString();
        assertTrue(output.contains("No autowiring dependencies found"));
    }
}