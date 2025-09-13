package it.denzosoft.jreverse.core.pattern;

import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.JarLocation;
import it.denzosoft.jreverse.core.model.JarType;
import it.denzosoft.jreverse.core.port.ReportFormat;
import it.denzosoft.jreverse.core.port.ReportType;
import it.denzosoft.jreverse.core.exception.ReportGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReportContextTest {

    @TempDir
    Path tempDir;
    
    @Mock
    private ReportStrategy htmlStrategy;
    
    @Mock
    private ReportStrategy pdfStrategy;
    
    private ReportContext reportContext;
    private JarContent jarContent;
    
    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        reportContext = new ReportContext();
        
        // Create test JAR content
        Path jarFile = tempDir.resolve("test.jar");
        Files.createFile(jarFile);
        JarLocation jarLocation = new JarLocation(jarFile);
        jarContent = JarContent.builder()
            .location(jarLocation)
            .jarType(JarType.REGULAR)
            .build();
    }

    @Test
    void shouldAddAndRemoveStrategies() {
        assertEquals(0, reportContext.getStrategyCount());
        
        reportContext.addStrategy(htmlStrategy);
        assertEquals(1, reportContext.getStrategyCount());
        
        reportContext.addStrategy(pdfStrategy);
        assertEquals(2, reportContext.getStrategyCount());
        
        // Adding same strategy should not increase count
        reportContext.addStrategy(htmlStrategy);
        assertEquals(2, reportContext.getStrategyCount());
        
        reportContext.removeStrategy(htmlStrategy);
        assertEquals(1, reportContext.getStrategyCount());
        
        reportContext.clearStrategies();
        assertEquals(0, reportContext.getStrategyCount());
    }

    @Test
    void shouldSortStrategiesByPriority() {
        when(htmlStrategy.getPriority()).thenReturn(10);
        when(pdfStrategy.getPriority()).thenReturn(20);
        
        reportContext.addStrategy(htmlStrategy);
        reportContext.addStrategy(pdfStrategy);
        
        List<ReportStrategy> strategies = reportContext.getStrategies();
        assertEquals(pdfStrategy, strategies.get(0)); // Higher priority first
        assertEquals(htmlStrategy, strategies.get(1));
    }

    @Test
    void shouldFindBestStrategy() {
        when(htmlStrategy.supports(ReportType.REST_ENDPOINT_MAP, ReportFormat.HTML)).thenReturn(true);
        when(htmlStrategy.supports(ReportType.REST_ENDPOINT_MAP, ReportFormat.PDF)).thenReturn(false);
        when(pdfStrategy.supports(ReportType.REST_ENDPOINT_MAP, ReportFormat.PDF)).thenReturn(true);
        
        reportContext.addStrategy(htmlStrategy);
        reportContext.addStrategy(pdfStrategy);
        
        ReportStrategy foundHtml = reportContext.findBestStrategy(ReportType.REST_ENDPOINT_MAP, ReportFormat.HTML);
        assertEquals(htmlStrategy, foundHtml);
        
        ReportStrategy foundPdf = reportContext.findBestStrategy(ReportType.REST_ENDPOINT_MAP, ReportFormat.PDF);
        assertEquals(pdfStrategy, foundPdf);
        
        ReportStrategy notFound = reportContext.findBestStrategy(ReportType.UML_CLASS_DIAGRAM, ReportFormat.CSV);
        assertNull(notFound);
    }

    @Test
    void shouldGenerateReportSuccessfully() throws Exception {
        when(htmlStrategy.supports(ReportType.REST_ENDPOINT_MAP, ReportFormat.HTML)).thenReturn(true);
        when(htmlStrategy.getStrategyName()).thenReturn("HTML Strategy");
        
        reportContext.addStrategy(htmlStrategy);
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        reportContext.generateReport(jarContent, ReportType.REST_ENDPOINT_MAP, ReportFormat.HTML, output);
        
        verify(htmlStrategy).generateReport(jarContent, ReportType.REST_ENDPOINT_MAP, ReportFormat.HTML, output);
    }

    @Test
    void shouldThrowExceptionWhenNoStrategyFound() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        ReportGenerationException exception = assertThrows(ReportGenerationException.class, () -> {
            reportContext.generateReport(jarContent, ReportType.REST_ENDPOINT_MAP, ReportFormat.HTML, output);
        });
        
        assertEquals(ReportGenerationException.ErrorCode.UNSUPPORTED_REPORT_TYPE, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("No strategy found"));
    }

    @Test
    void shouldHandleStrategyException() throws Exception {
        when(htmlStrategy.supports(ReportType.REST_ENDPOINT_MAP, ReportFormat.HTML)).thenReturn(true);
        when(htmlStrategy.getStrategyName()).thenReturn("HTML Strategy");
        
        ReportGenerationException strategyException = new ReportGenerationException(
            "Strategy failed",
            ReportType.REST_ENDPOINT_MAP,
            ReportFormat.HTML,
            ReportGenerationException.ErrorCode.GENERATION_FAILED
        );
        doThrow(strategyException).when(htmlStrategy).generateReport(any(), any(), any(), any());
        
        reportContext.addStrategy(htmlStrategy);
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        ReportGenerationException exception = assertThrows(ReportGenerationException.class, () -> {
            reportContext.generateReport(jarContent, ReportType.REST_ENDPOINT_MAP, ReportFormat.HTML, output);
        });
        
        assertEquals(strategyException, exception);
    }

    @Test
    void shouldHandleUnexpectedException() throws Exception {
        when(htmlStrategy.supports(ReportType.REST_ENDPOINT_MAP, ReportFormat.HTML)).thenReturn(true);
        when(htmlStrategy.getStrategyName()).thenReturn("HTML Strategy");
        
        RuntimeException unexpectedException = new RuntimeException("Unexpected error");
        doThrow(unexpectedException).when(htmlStrategy).generateReport(any(), any(), any(), any());
        
        reportContext.addStrategy(htmlStrategy);
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        ReportGenerationException exception = assertThrows(ReportGenerationException.class, () -> {
            reportContext.generateReport(jarContent, ReportType.REST_ENDPOINT_MAP, ReportFormat.HTML, output);
        });
        
        assertEquals(ReportGenerationException.ErrorCode.GENERATION_FAILED, exception.getErrorCode());
        assertEquals(unexpectedException, exception.getCause());
        assertTrue(exception.getMessage().contains("Unexpected error"));
    }

    @Test
    void shouldGetStrategiesForReportType() {
        when(htmlStrategy.getSupportedReportTypes()).thenReturn(new ReportType[]{
            ReportType.REST_ENDPOINT_MAP, 
            ReportType.UML_CLASS_DIAGRAM
        });
        when(pdfStrategy.getSupportedReportTypes()).thenReturn(new ReportType[]{
            ReportType.REST_ENDPOINT_MAP
        });
        
        reportContext.addStrategy(htmlStrategy);
        reportContext.addStrategy(pdfStrategy);
        
        List<ReportStrategy> strategiesForRest = reportContext.getStrategiesForReportType(ReportType.REST_ENDPOINT_MAP);
        assertEquals(2, strategiesForRest.size());
        
        List<ReportStrategy> strategiesForUml = reportContext.getStrategiesForReportType(ReportType.UML_CLASS_DIAGRAM);
        assertEquals(1, strategiesForUml.size());
        assertEquals(htmlStrategy, strategiesForUml.get(0));
    }

    @Test
    void shouldGetStrategiesForFormat() {
        when(htmlStrategy.getSupportedFormats()).thenReturn(new ReportFormat[]{
            ReportFormat.HTML, 
            ReportFormat.JSON
        });
        when(pdfStrategy.getSupportedFormats()).thenReturn(new ReportFormat[]{
            ReportFormat.PDF
        });
        
        reportContext.addStrategy(htmlStrategy);
        reportContext.addStrategy(pdfStrategy);
        
        List<ReportStrategy> strategiesForHtml = reportContext.getStrategiesForFormat(ReportFormat.HTML);
        assertEquals(1, strategiesForHtml.size());
        assertEquals(htmlStrategy, strategiesForHtml.get(0));
        
        List<ReportStrategy> strategiesForPdf = reportContext.getStrategiesForFormat(ReportFormat.PDF);
        assertEquals(1, strategiesForPdf.size());
        assertEquals(pdfStrategy, strategiesForPdf.get(0));
    }

    @Test
    void shouldCheckIfSupported() {
        when(htmlStrategy.supports(ReportType.REST_ENDPOINT_MAP, ReportFormat.HTML)).thenReturn(true);
        when(htmlStrategy.supports(ReportType.UML_CLASS_DIAGRAM, ReportFormat.PDF)).thenReturn(false);
        
        reportContext.addStrategy(htmlStrategy);
        
        assertTrue(reportContext.isSupported(ReportType.REST_ENDPOINT_MAP, ReportFormat.HTML));
        assertFalse(reportContext.isSupported(ReportType.UML_CLASS_DIAGRAM, ReportFormat.PDF));
    }

    @Test
    void shouldHaveProgressNotifier() {
        assertNotNull(reportContext.getProgressNotifier());
        assertEquals("ReportGeneration", reportContext.getProgressNotifier().getOperationType());
    }

    @Test
    void shouldIgnoreNullStrategy() {
        reportContext.addStrategy(null);
        assertEquals(0, reportContext.getStrategyCount());
        
        // Should not throw when trying to remove null
        assertDoesNotThrow(() -> reportContext.removeStrategy(null));
    }

    private String getOperationType() {
        return "ReportGeneration";
    }
}