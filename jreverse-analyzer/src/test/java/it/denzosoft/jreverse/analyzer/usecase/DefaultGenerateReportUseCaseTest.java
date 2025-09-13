package it.denzosoft.jreverse.analyzer.usecase;

import it.denzosoft.jreverse.core.exception.ReportGenerationException;
import it.denzosoft.jreverse.core.model.*;
import it.denzosoft.jreverse.core.pattern.ReportStrategy;
import it.denzosoft.jreverse.core.port.ReportFormat;
import it.denzosoft.jreverse.core.port.ReportType;
import it.denzosoft.jreverse.core.usecase.GenerateReportUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for DefaultGenerateReportUseCase.
 * Tests all scenarios including single/multiple reports, different formats, and error handling.
 */
@ExtendWith(MockitoExtension.class)
class DefaultGenerateReportUseCaseTest {
    
    @Mock
    private ReportStrategy mockHtmlStrategy;
    
    @Mock
    private ReportStrategy mockJsonStrategy;
    
    @Mock
    private ReportStrategy mockXmlStrategy;
    
    private DefaultGenerateReportUseCase useCase;
    private JarContent testJarContent;
    private GenerateReportUseCase.ReportOptions defaultOptions;
    private Path testPath;
    private JarLocation testJarLocation;
    
    @BeforeEach
    void setUp() {
        testPath = Paths.get("/test/sample.jar");
        testJarLocation = new JarLocation(testPath);
        
        Set<ClassInfo> classes = createTestClassInfoSet();
        testJarContent = JarContent.builder()
            .location(testJarLocation)
            .jarType(JarType.REGULAR_JAR)
            .classes(classes)
            .build();
            
        defaultOptions = GenerateReportUseCase.ReportOptions.defaults();
        
        // Setup basic mock behavior - will be extended per test as needed
        lenient().when(mockHtmlStrategy.getStrategyName()).thenReturn("HTML Strategy");
        lenient().when(mockJsonStrategy.getStrategyName()).thenReturn("JSON Strategy");
        lenient().when(mockXmlStrategy.getStrategyName()).thenReturn("XML Strategy");
    }
    
    @Test
    void testConstructor_SingleStrategy_Success() {
        useCase = new DefaultGenerateReportUseCase(mockHtmlStrategy);
        assertNotNull(useCase);
    }
    
    @Test
    void testConstructor_SingleStrategy_NullThrowsException() {
        assertThrows(NullPointerException.class, 
            () -> new DefaultGenerateReportUseCase((ReportStrategy) null));
    }
    
    @Test
    void testConstructor_MultipleStrategies_Success() {
        Map<ReportFormat, ReportStrategy> strategies = new HashMap<>();
        strategies.put(ReportFormat.HTML, mockHtmlStrategy);
        strategies.put(ReportFormat.JSON, mockJsonStrategy);
        
        useCase = new DefaultGenerateReportUseCase(strategies, mockXmlStrategy);
        assertNotNull(useCase);
    }
    
    @Test
    void testConstructor_MultipleStrategies_NullStrategiesThrowsException() {
        assertThrows(NullPointerException.class, 
            () -> new DefaultGenerateReportUseCase(null, mockHtmlStrategy));
    }
    
    @Test
    void testConstructor_MultipleStrategies_NullDefaultThrowsException() {
        Map<ReportFormat, ReportStrategy> strategies = new HashMap<>();
        strategies.put(ReportFormat.HTML, mockHtmlStrategy);
        
        assertThrows(NullPointerException.class, 
            () -> new DefaultGenerateReportUseCase(strategies, null));
    }
    
    @Test
    void testExecute_NullRequest_ThrowsException() {
        useCase = new DefaultGenerateReportUseCase(mockHtmlStrategy);
        assertThrows(NullPointerException.class, () -> useCase.execute(null));
    }
    
    @Test
    void testExecute_NullJarContent_ThrowsException() {
        useCase = new DefaultGenerateReportUseCase(mockHtmlStrategy);
        
        Set<ReportType> reportTypes = Set.of(ReportType.PACKAGE_CLASS_MAP);
        GenerateReportUseCase.ReportRequest request = new GenerateReportUseCase.ReportRequest(
            null, reportTypes, ReportFormat.HTML, new ByteArrayOutputStream(), defaultOptions);
        
        assertThrows(NullPointerException.class, () -> useCase.execute(request));
    }
    
    @Test
    void testExecute_NullReportTypes_ThrowsException() {
        useCase = new DefaultGenerateReportUseCase(mockHtmlStrategy);
        
        // The ReportRequest constructor handles null reportTypes by creating an empty set
        // So this actually results in an empty set, which throws ReportGenerationException
        GenerateReportUseCase.ReportRequest request = new GenerateReportUseCase.ReportRequest(
            testJarContent, null, ReportFormat.HTML, new ByteArrayOutputStream(), defaultOptions);
        
        ReportGenerationException thrown = assertThrows(ReportGenerationException.class, 
            () -> useCase.execute(request));
        
        assertEquals(ReportGenerationException.ErrorCode.INSUFFICIENT_DATA, thrown.getErrorCode());
    }
    
    @Test
    void testExecute_EmptyReportTypes_ThrowsException() {
        useCase = new DefaultGenerateReportUseCase(mockHtmlStrategy);
        
        Set<ReportType> emptyReportTypes = Collections.emptySet();
        GenerateReportUseCase.ReportRequest request = new GenerateReportUseCase.ReportRequest(
            testJarContent, emptyReportTypes, ReportFormat.HTML, new ByteArrayOutputStream(), defaultOptions);
        
        ReportGenerationException thrown = assertThrows(ReportGenerationException.class, 
            () -> useCase.execute(request));
        
        assertEquals(ReportGenerationException.ErrorCode.INSUFFICIENT_DATA, thrown.getErrorCode());
    }
    
    @Test
    void testExecute_SingleReportSuccess() throws Exception {
        when(mockHtmlStrategy.supports(ReportType.PACKAGE_CLASS_MAP, ReportFormat.HTML)).thenReturn(true);
        doNothing().when(mockHtmlStrategy).generateReport(
            eq(testJarContent), eq(ReportType.PACKAGE_CLASS_MAP), eq(ReportFormat.HTML), any(OutputStream.class));
        
        useCase = new DefaultGenerateReportUseCase(mockHtmlStrategy);
        
        Set<ReportType> reportTypes = Set.of(ReportType.PACKAGE_CLASS_MAP);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        GenerateReportUseCase.ReportRequest request = new GenerateReportUseCase.ReportRequest(
            testJarContent, reportTypes, ReportFormat.HTML, output, defaultOptions);
        
        GenerateReportUseCase.ReportResult result = useCase.execute(request);
        
        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals(1, result.getGeneratedReports());
        
        GenerateReportUseCase.ReportMetadata metadata = result.getMetadata();
        assertEquals("HTML Strategy", metadata.getGeneratorName());
        assertEquals(1, metadata.getTotalReportTypes());
        assertEquals(1, metadata.getSuccessfulReports());
        assertEquals(0, metadata.getFailedReports());
        assertTrue(metadata.getDurationMs() >= 0);
        
        verify(mockHtmlStrategy).generateReport(testJarContent, ReportType.PACKAGE_CLASS_MAP, ReportFormat.HTML, output);
    }
    
    @Test
    void testExecute_SingleReportUnsupportedFormat() {
        useCase = new DefaultGenerateReportUseCase(mockHtmlStrategy);
        
        when(mockHtmlStrategy.supports(ReportType.PACKAGE_CLASS_MAP, ReportFormat.PDF)).thenReturn(false);
        
        Set<ReportType> reportTypes = Set.of(ReportType.PACKAGE_CLASS_MAP);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        GenerateReportUseCase.ReportRequest request = new GenerateReportUseCase.ReportRequest(
            testJarContent, reportTypes, ReportFormat.PDF, output, defaultOptions);
        
        ReportGenerationException thrown = assertThrows(ReportGenerationException.class, 
            () -> useCase.execute(request));
        
        assertEquals(ReportGenerationException.ErrorCode.UNSUPPORTED_REPORT_TYPE, thrown.getErrorCode());
        assertEquals(ReportFormat.PDF, thrown.getReportFormat());
        assertEquals(ReportType.PACKAGE_CLASS_MAP, thrown.getReportType());
    }
    
    @Test
    void testExecute_SingleReportStrategyThrowsException() throws Exception {
        when(mockHtmlStrategy.supports(ReportType.PACKAGE_CLASS_MAP, ReportFormat.HTML)).thenReturn(true);
        
        ReportGenerationException originalException = new ReportGenerationException(
            "Strategy failed", ReportType.PACKAGE_CLASS_MAP, ReportFormat.HTML, 
            ReportGenerationException.ErrorCode.GENERATION_FAILED);
        
        doThrow(originalException).when(mockHtmlStrategy).generateReport(
            eq(testJarContent), eq(ReportType.PACKAGE_CLASS_MAP), eq(ReportFormat.HTML), any(OutputStream.class));
        
        useCase = new DefaultGenerateReportUseCase(mockHtmlStrategy);
        
        Set<ReportType> reportTypes = Set.of(ReportType.PACKAGE_CLASS_MAP);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        GenerateReportUseCase.ReportRequest request = new GenerateReportUseCase.ReportRequest(
            testJarContent, reportTypes, ReportFormat.HTML, output, defaultOptions);
        
        ReportGenerationException thrown = assertThrows(ReportGenerationException.class, 
            () -> useCase.execute(request));
        
        assertEquals(originalException.getErrorCode(), thrown.getErrorCode());
        assertEquals(originalException.getReportType(), thrown.getReportType());
        assertEquals(originalException.getReportFormat(), thrown.getReportFormat());
    }
    
    @Test
    void testExecute_MultipleReportsHtmlSuccess() throws Exception {
        when(mockHtmlStrategy.supports(any(ReportType.class), eq(ReportFormat.HTML))).thenReturn(true);
        
        // Mock HTML responses
        doAnswer(invocation -> {
            OutputStream output = invocation.getArgument(3);
            String content = "<html><body><h1>Test Report</h1><p>Content</p></body></html>";
            output.write(content.getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(mockHtmlStrategy).generateReport(any(), any(), eq(ReportFormat.HTML), any());
        
        useCase = new DefaultGenerateReportUseCase(mockHtmlStrategy);
        
        Set<ReportType> reportTypes = Set.of(ReportType.PACKAGE_CLASS_MAP, ReportType.UML_CLASS_DIAGRAM);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        GenerateReportUseCase.ReportRequest request = new GenerateReportUseCase.ReportRequest(
            testJarContent, reportTypes, ReportFormat.HTML, output, defaultOptions);
        
        GenerateReportUseCase.ReportResult result = useCase.execute(request);
        
        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals(2, result.getGeneratedReports());
        
        String generatedHtml = output.toString(StandardCharsets.UTF_8);
        assertTrue(generatedHtml.contains("<!DOCTYPE html>"));
        assertTrue(generatedHtml.contains("JReverse Analysis Report"));
        assertTrue(generatedHtml.contains("Package Class Map"));
        assertTrue(generatedHtml.contains("UML Class Diagram"));
        
        verify(mockHtmlStrategy, times(2)).generateReport(any(), any(), eq(ReportFormat.HTML), any());
    }
    
    @Test
    void testExecute_MultipleReportsJsonSuccess() throws Exception {
        when(mockJsonStrategy.supports(any(ReportType.class), eq(ReportFormat.JSON))).thenReturn(true);
        
        // Mock JSON responses
        doAnswer(invocation -> {
            OutputStream output = invocation.getArgument(3);
            String content = "{\"reportType\": \"test\", \"data\": []}";
            output.write(content.getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(mockJsonStrategy).generateReport(any(), any(), eq(ReportFormat.JSON), any());
        
        useCase = new DefaultGenerateReportUseCase(mockJsonStrategy);
        
        Set<ReportType> reportTypes = Set.of(ReportType.PACKAGE_CLASS_MAP, ReportType.UML_CLASS_DIAGRAM);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        GenerateReportUseCase.ReportRequest request = new GenerateReportUseCase.ReportRequest(
            testJarContent, reportTypes, ReportFormat.JSON, output, defaultOptions);
        
        GenerateReportUseCase.ReportResult result = useCase.execute(request);
        
        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals(2, result.getGeneratedReports());
        
        String generatedJson = output.toString(StandardCharsets.UTF_8);
        assertTrue(generatedJson.contains("\"jarName\":"));
        assertTrue(generatedJson.contains("\"PACKAGE_CLASS_MAP\":"));
        assertTrue(generatedJson.contains("\"UML_CLASS_DIAGRAM\":"));
        
        verify(mockJsonStrategy, times(2)).generateReport(any(), any(), eq(ReportFormat.JSON), any());
    }
    
    @Test
    void testExecute_MultipleReportsXmlSuccess() throws Exception {
        when(mockXmlStrategy.supports(any(ReportType.class), eq(ReportFormat.XML))).thenReturn(true);
        
        // Mock XML responses
        doAnswer(invocation -> {
            OutputStream output = invocation.getArgument(3);
            String content = "<report><type>test</type><data></data></report>";
            output.write(content.getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(mockXmlStrategy).generateReport(any(), any(), eq(ReportFormat.XML), any());
        
        useCase = new DefaultGenerateReportUseCase(mockXmlStrategy);
        
        Set<ReportType> reportTypes = Set.of(ReportType.PACKAGE_CLASS_MAP, ReportType.UML_CLASS_DIAGRAM);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        GenerateReportUseCase.ReportRequest request = new GenerateReportUseCase.ReportRequest(
            testJarContent, reportTypes, ReportFormat.XML, output, defaultOptions);
        
        GenerateReportUseCase.ReportResult result = useCase.execute(request);
        
        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals(2, result.getGeneratedReports());
        
        String generatedXml = output.toString(StandardCharsets.UTF_8);
        assertTrue(generatedXml.contains("<?xml version=\"1.0\""));
        assertTrue(generatedXml.contains("<multiReport>"));
        assertTrue(generatedXml.contains("type=\"PACKAGE_CLASS_MAP\""));
        assertTrue(generatedXml.contains("type=\"UML_CLASS_DIAGRAM\""));
        
        verify(mockXmlStrategy, times(2)).generateReport(any(), any(), eq(ReportFormat.XML), any());
    }
    
    @Test
    void testExecute_MultipleReportsPartialFailure() throws Exception {
        when(mockHtmlStrategy.supports(any(ReportType.class), eq(ReportFormat.HTML))).thenReturn(true);
        
        // First report succeeds, second fails
        doAnswer(invocation -> {
            OutputStream output = invocation.getArgument(3);
            ReportType reportType = invocation.getArgument(1);
            
            if (reportType == ReportType.PACKAGE_CLASS_MAP) {
                String content = "<html><body><h1>Success</h1></body></html>";
                output.write(content.getBytes(StandardCharsets.UTF_8));
                return null;
            } else {
                throw new ReportGenerationException("Failed", reportType, ReportFormat.HTML, 
                    ReportGenerationException.ErrorCode.GENERATION_FAILED);
            }
        }).when(mockHtmlStrategy).generateReport(any(), any(), eq(ReportFormat.HTML), any());
        
        useCase = new DefaultGenerateReportUseCase(mockHtmlStrategy);
        
        Set<ReportType> reportTypes = Set.of(ReportType.PACKAGE_CLASS_MAP, ReportType.UML_CLASS_DIAGRAM);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        GenerateReportUseCase.ReportRequest request = new GenerateReportUseCase.ReportRequest(
            testJarContent, reportTypes, ReportFormat.HTML, output, defaultOptions);
        
        GenerateReportUseCase.ReportResult result = useCase.execute(request);
        
        assertNotNull(result);
        assertFalse(result.isSuccessful()); // Not fully successful due to partial failure
        assertEquals(1, result.getGeneratedReports());
        
        GenerateReportUseCase.ReportMetadata metadata = result.getMetadata();
        assertEquals(2, metadata.getTotalReportTypes());
        assertEquals(1, metadata.getSuccessfulReports());
        assertEquals(1, metadata.getFailedReports());
        
        String generatedHtml = output.toString(StandardCharsets.UTF_8);
        assertTrue(generatedHtml.contains("Package Class Map"));
        assertFalse(generatedHtml.contains("UML Class Diagram"));
    }
    
    @Test
    void testExecute_ConcatenatedReportsForUnsupportedFormat() throws Exception {
        // Mock for CSV format (unsupported by our specific format combiners)
        when(mockHtmlStrategy.supports(any(ReportType.class), eq(ReportFormat.CSV))).thenReturn(true);
        
        doAnswer(invocation -> {
            OutputStream output = invocation.getArgument(3);
            String content = "report,data\ntest,value";
            output.write(content.getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(mockHtmlStrategy).generateReport(any(), any(), eq(ReportFormat.CSV), any());
        
        useCase = new DefaultGenerateReportUseCase(mockHtmlStrategy);
        
        Set<ReportType> reportTypes = Set.of(ReportType.PACKAGE_CLASS_MAP, ReportType.UML_CLASS_DIAGRAM);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        GenerateReportUseCase.ReportRequest request = new GenerateReportUseCase.ReportRequest(
            testJarContent, reportTypes, ReportFormat.CSV, output, defaultOptions);
        
        GenerateReportUseCase.ReportResult result = useCase.execute(request);
        
        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals(2, result.getGeneratedReports());
        
        String generatedContent = output.toString(StandardCharsets.UTF_8);
        assertTrue(generatedContent.contains("report,data"));
        assertTrue(generatedContent.contains("=".repeat(80))); // Separator
    }
    
    @Test
    void testExecute_FormatSpecificStrategySelection() throws Exception {
        Map<ReportFormat, ReportStrategy> strategies = new HashMap<>();
        strategies.put(ReportFormat.HTML, mockHtmlStrategy);
        strategies.put(ReportFormat.JSON, mockJsonStrategy);
        
        when(mockJsonStrategy.supports(ReportType.PACKAGE_CLASS_MAP, ReportFormat.JSON)).thenReturn(true);
        doNothing().when(mockJsonStrategy).generateReport(
            eq(testJarContent), eq(ReportType.PACKAGE_CLASS_MAP), eq(ReportFormat.JSON), any(OutputStream.class));
        
        useCase = new DefaultGenerateReportUseCase(strategies, mockXmlStrategy);
        
        Set<ReportType> reportTypes = Set.of(ReportType.PACKAGE_CLASS_MAP);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        GenerateReportUseCase.ReportRequest request = new GenerateReportUseCase.ReportRequest(
            testJarContent, reportTypes, ReportFormat.JSON, output, defaultOptions);
        
        GenerateReportUseCase.ReportResult result = useCase.execute(request);
        
        assertNotNull(result);
        assertTrue(result.isSuccessful());
        
        verify(mockJsonStrategy).generateReport(testJarContent, ReportType.PACKAGE_CLASS_MAP, ReportFormat.JSON, output);
        verify(mockHtmlStrategy, never()).generateReport(any(), any(), any(), any());
        verify(mockXmlStrategy, never()).generateReport(any(), any(), any(), any());
    }
    
    @Test
    void testExecute_DefaultStrategyUsedWhenFormatNotMapped() throws Exception {
        Map<ReportFormat, ReportStrategy> strategies = new HashMap<>();
        strategies.put(ReportFormat.HTML, mockHtmlStrategy);
        // PDF not mapped, should use default (XML strategy)
        
        useCase = new DefaultGenerateReportUseCase(strategies, mockXmlStrategy);
        
        when(mockXmlStrategy.supports(ReportType.PACKAGE_CLASS_MAP, ReportFormat.PDF)).thenReturn(true);
        doNothing().when(mockXmlStrategy).generateReport(
            eq(testJarContent), eq(ReportType.PACKAGE_CLASS_MAP), eq(ReportFormat.PDF), any(OutputStream.class));
        
        Set<ReportType> reportTypes = Set.of(ReportType.PACKAGE_CLASS_MAP);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        GenerateReportUseCase.ReportRequest request = new GenerateReportUseCase.ReportRequest(
            testJarContent, reportTypes, ReportFormat.PDF, output, defaultOptions);
        
        GenerateReportUseCase.ReportResult result = useCase.execute(request);
        
        assertNotNull(result);
        assertTrue(result.isSuccessful());
        
        verify(mockXmlStrategy).generateReport(testJarContent, ReportType.PACKAGE_CLASS_MAP, ReportFormat.PDF, output);
        verify(mockHtmlStrategy, never()).generateReport(any(), any(), any(), any());
        verify(mockJsonStrategy, never()).generateReport(any(), any(), any(), any());
    }
    
    @Test
    void testExecute_UnexpectedExceptionWrapped() throws Exception {
        when(mockHtmlStrategy.supports(ReportType.PACKAGE_CLASS_MAP, ReportFormat.HTML)).thenReturn(true);
        doThrow(new RuntimeException("Unexpected error")).when(mockHtmlStrategy).generateReport(
            eq(testJarContent), eq(ReportType.PACKAGE_CLASS_MAP), eq(ReportFormat.HTML), any(OutputStream.class));
        
        useCase = new DefaultGenerateReportUseCase(mockHtmlStrategy);
        
        Set<ReportType> reportTypes = Set.of(ReportType.PACKAGE_CLASS_MAP);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        GenerateReportUseCase.ReportRequest request = new GenerateReportUseCase.ReportRequest(
            testJarContent, reportTypes, ReportFormat.HTML, output, defaultOptions);
        
        ReportGenerationException thrown = assertThrows(ReportGenerationException.class, 
            () -> useCase.execute(request));
        
        assertEquals(ReportGenerationException.ErrorCode.GENERATION_FAILED, thrown.getErrorCode());
        assertTrue(thrown.getMessage().contains("Unexpected error during report generation"));
        assertInstanceOf(RuntimeException.class, thrown.getCause());
    }
    
    // Helper methods
    
    private Set<ClassInfo> createTestClassInfoSet() {
        Set<ClassInfo> classes = new HashSet<>();
        
        ClassInfo testClass = ClassInfo.builder()
            .fullyQualifiedName("com.example.TestClass")
            .classType(ClassType.CLASS)
            .build();
            
        classes.add(testClass);
        return classes;
    }
}