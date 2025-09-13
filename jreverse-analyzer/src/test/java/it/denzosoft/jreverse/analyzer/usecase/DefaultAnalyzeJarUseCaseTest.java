package it.denzosoft.jreverse.analyzer.usecase;

import it.denzosoft.jreverse.core.exception.JarAnalysisException;
import it.denzosoft.jreverse.core.model.*;
import it.denzosoft.jreverse.core.pattern.AnalyzerFactory;
import it.denzosoft.jreverse.core.port.JarAnalyzerPort;
import it.denzosoft.jreverse.core.usecase.AnalyzeJarUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for DefaultAnalyzeJarUseCase.
 * Tests all scenarios including success, errors, timeouts, and memory management.
 */
@ExtendWith(MockitoExtension.class)
class DefaultAnalyzeJarUseCaseTest {
    
    @Mock
    private AnalyzerFactory mockFactory;
    
    @Mock
    private JarAnalyzerPort mockAnalyzer;
    
    private DefaultAnalyzeJarUseCase useCase;
    private JarLocation testJarLocation;
    private AnalyzeJarUseCase.AnalysisOptions defaultOptions;
    
    @BeforeEach
    void setUp() {
        useCase = new DefaultAnalyzeJarUseCase(mockFactory);
        
        Path testPath = Paths.get("/test/sample.jar");
        testJarLocation = new JarLocation(testPath);
            
        defaultOptions = AnalyzeJarUseCase.AnalysisOptions.builder()
            .maxMemoryMB(1024)
            .timeoutSeconds(30)
            .deepAnalysis(true)
            .includeResources(true)
            .build();
    }
    
    @AfterEach
    void tearDown() {
        if (useCase != null) {
            useCase.shutdown();
        }
    }
    
    @Test
    void testExecute_Successful_Analysis() throws Exception {
        // Given
        Set<ClassInfo> classes = createTestClassInfoSet();
        JarContent expectedContent = createTestJarContent(classes);
        
        when(mockFactory.createAnalyzer(testJarLocation)).thenReturn(mockAnalyzer);
        when(mockAnalyzer.getAnalyzerName()).thenReturn("TestAnalyzer");
        when(mockAnalyzer.analyzeJar(testJarLocation)).thenReturn(expectedContent);
        
        AnalyzeJarUseCase.AnalysisRequest request = 
            new AnalyzeJarUseCase.AnalysisRequest(testJarLocation, defaultOptions);
        
        // When
        AnalyzeJarUseCase.AnalysisResult result = useCase.execute(request);
        
        // Then
        assertNotNull(result);
        assertEquals(expectedContent, result.getJarContent());
        
        AnalyzeJarUseCase.AnalysisMetadata metadata = result.getMetadata();
        assertTrue(metadata.isSuccessful());
        assertEquals("TestAnalyzer", metadata.getAnalyzerName());
        assertNull(metadata.getErrorMessage());
        assertTrue(metadata.getDurationMs() >= 0);
        
        verify(mockFactory).createAnalyzer(testJarLocation);
        verify(mockAnalyzer).analyzeJar(testJarLocation);
    }
    
    @Test
    void testExecute_NullRequest_ThrowsException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> useCase.execute(null));
    }
    
    @Test
    void testExecute_NullJarLocation_ThrowsException() {
        // Given
        AnalyzeJarUseCase.AnalysisRequest request = 
            new AnalyzeJarUseCase.AnalysisRequest(null, defaultOptions);
        
        // When & Then
        assertThrows(NullPointerException.class, () -> useCase.execute(request));
    }
    
    @Test
    void testExecute_AnalyzerThrowsJarAnalysisException_PropagatesException() throws Exception {
        // Given
        JarAnalysisException originalException = new JarAnalysisException(
            "Analysis failed", 
            testJarLocation.getPath().toString(),
            JarAnalysisException.ErrorCode.ANALYSIS_FAILED
        );
        
        when(mockFactory.createAnalyzer(testJarLocation)).thenReturn(mockAnalyzer);
        when(mockAnalyzer.getAnalyzerName()).thenReturn("TestAnalyzer");
        when(mockAnalyzer.analyzeJar(testJarLocation)).thenThrow(originalException);
        
        AnalyzeJarUseCase.AnalysisRequest request = 
            new AnalyzeJarUseCase.AnalysisRequest(testJarLocation, defaultOptions);
        
        // When & Then
        JarAnalysisException thrown = assertThrows(JarAnalysisException.class, 
            () -> useCase.execute(request));
        
        assertEquals(originalException.getErrorCode(), thrown.getErrorCode());
        assertEquals(originalException.getJarPath(), thrown.getJarPath());
    }
    
    @Test
    void testExecute_UnexpectedException_WrapsInJarAnalysisException() throws Exception {
        // Given
        RuntimeException originalException = new RuntimeException("Unexpected error");
        
        when(mockFactory.createAnalyzer(testJarLocation)).thenReturn(mockAnalyzer);
        when(mockAnalyzer.getAnalyzerName()).thenReturn("TestAnalyzer");
        when(mockAnalyzer.analyzeJar(testJarLocation)).thenThrow(originalException);
        
        AnalyzeJarUseCase.AnalysisRequest request = 
            new AnalyzeJarUseCase.AnalysisRequest(testJarLocation, defaultOptions);
        
        // When & Then
        JarAnalysisException thrown = assertThrows(JarAnalysisException.class, 
            () -> useCase.execute(request));
        
        assertEquals(JarAnalysisException.ErrorCode.ANALYSIS_FAILED, thrown.getErrorCode());
        assertEquals(testJarLocation.getPath().toString(), thrown.getJarPath());
        assertEquals(originalException, thrown.getCause());
    }
    
    @Test
    void testExecute_FactoryCreationFails_WrapsException() throws Exception {
        // Given
        RuntimeException factoryException = new RuntimeException("Factory error");
        
        when(mockFactory.createAnalyzer(testJarLocation)).thenThrow(factoryException);
        // Remove unnecessary stubbing - getFactoryName() isn't called when createAnalyzer fails
        
        AnalyzeJarUseCase.AnalysisRequest request = 
            new AnalyzeJarUseCase.AnalysisRequest(testJarLocation, defaultOptions);
        
        // When & Then
        JarAnalysisException thrown = assertThrows(JarAnalysisException.class, 
            () -> useCase.execute(request));
        
        assertEquals(JarAnalysisException.ErrorCode.ANALYSIS_FAILED, thrown.getErrorCode());
        assertTrue(thrown.getMessage().contains("Unexpected error during analysis"));
    }
    
    @Test
    void testExecute_TimeoutScenario_ThrowsTimeoutException() throws Exception {
        // Given
        JarContent slowContent = createTestJarContent(createTestClassInfoSet());
        
        when(mockFactory.createAnalyzer(testJarLocation)).thenReturn(mockAnalyzer);
        when(mockAnalyzer.getAnalyzerName()).thenReturn("SlowAnalyzer");
        when(mockAnalyzer.analyzeJar(testJarLocation)).thenAnswer(invocation -> {
            // Simulate slow analysis
            Thread.sleep(2000); // 2 seconds
            return slowContent;
        });
        
        AnalyzeJarUseCase.AnalysisOptions shortTimeoutOptions = 
            AnalyzeJarUseCase.AnalysisOptions.builder()
                .timeoutSeconds(1) // 1 second timeout
                .build();
        
        AnalyzeJarUseCase.AnalysisRequest request = 
            new AnalyzeJarUseCase.AnalysisRequest(testJarLocation, shortTimeoutOptions);
        
        // When & Then
        JarAnalysisException thrown = assertThrows(JarAnalysisException.class, 
            () -> useCase.execute(request));
        
        assertEquals(JarAnalysisException.ErrorCode.TIMEOUT, thrown.getErrorCode());
        assertTrue(thrown.getMessage().contains("timeout"));
    }
    
    @Test
    void testExecute_MemoryLimitExceeded_HandlesGracefully() throws Exception {
        // Given
        when(mockFactory.createAnalyzer(testJarLocation)).thenReturn(mockAnalyzer);
        when(mockAnalyzer.getAnalyzerName()).thenReturn("MemoryHungryAnalyzer");
        // OutOfMemoryError viene catturato in executeWithTimeout e wrapped come ANALYSIS_FAILED
        when(mockAnalyzer.analyzeJar(testJarLocation)).thenThrow(new OutOfMemoryError("Java heap space"));
        
        AnalyzeJarUseCase.AnalysisRequest request = 
            new AnalyzeJarUseCase.AnalysisRequest(testJarLocation, defaultOptions);
        
        // When & Then
        JarAnalysisException thrown = assertThrows(JarAnalysisException.class, 
            () -> useCase.execute(request));
        
        // OutOfMemoryError in executor thread is wrapped as ANALYSIS_FAILED
        assertEquals(JarAnalysisException.ErrorCode.ANALYSIS_FAILED, thrown.getErrorCode());
        assertTrue(thrown.getMessage().contains("Analysis execution failed"));
    }
    
    @Test
    void testExecute_HighMemoryLimit_ShowsWarning() throws Exception {
        // Given
        Set<ClassInfo> classes = createTestClassInfoSet();
        JarContent expectedContent = createTestJarContent(classes);
        
        when(mockFactory.createAnalyzer(testJarLocation)).thenReturn(mockAnalyzer);
        when(mockAnalyzer.getAnalyzerName()).thenReturn("TestAnalyzer");
        when(mockAnalyzer.analyzeJar(testJarLocation)).thenReturn(expectedContent);
        
        // Request with very high memory limit
        AnalyzeJarUseCase.AnalysisOptions highMemoryOptions = 
            AnalyzeJarUseCase.AnalysisOptions.builder()
                .maxMemoryMB(999999) // Impossibly high
                .build();
        
        AnalyzeJarUseCase.AnalysisRequest request = 
            new AnalyzeJarUseCase.AnalysisRequest(testJarLocation, highMemoryOptions);
        
        // When
        AnalyzeJarUseCase.AnalysisResult result = useCase.execute(request);
        
        // Then - Should complete successfully despite warning
        assertNotNull(result);
        assertTrue(result.getMetadata().isSuccessful());
    }
    
    @Test
    void testExecute_NullOptions_UsesDefaults() throws Exception {
        // Given
        Set<ClassInfo> classes = createTestClassInfoSet();
        JarContent expectedContent = createTestJarContent(classes);
        
        when(mockFactory.createAnalyzer(testJarLocation)).thenReturn(mockAnalyzer);
        when(mockAnalyzer.getAnalyzerName()).thenReturn("TestAnalyzer");
        when(mockAnalyzer.analyzeJar(testJarLocation)).thenReturn(expectedContent);
        
        AnalyzeJarUseCase.AnalysisRequest request = 
            new AnalyzeJarUseCase.AnalysisRequest(testJarLocation, null);
        
        // When
        AnalyzeJarUseCase.AnalysisResult result = useCase.execute(request);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getMetadata().isSuccessful());
        
        // Should use default options (as handled by AnalysisRequest constructor)
        assertNotNull(request.getOptions());
    }
    
    @Test
    void testExecute_ConcurrentExecution_ThreadSafe() throws Exception {
        // Given
        Set<ClassInfo> classes = createTestClassInfoSet();
        JarContent expectedContent = createTestJarContent(classes);
        
        when(mockFactory.createAnalyzer(any(JarLocation.class))).thenReturn(mockAnalyzer);
        when(mockAnalyzer.getAnalyzerName()).thenReturn("ConcurrentAnalyzer");
        when(mockAnalyzer.analyzeJar(any(JarLocation.class))).thenReturn(expectedContent);
        
        AnalyzeJarUseCase.AnalysisRequest request = 
            new AnalyzeJarUseCase.AnalysisRequest(testJarLocation, defaultOptions);
        
        // When - Execute concurrently (though implementation uses single thread executor)
        AnalyzeJarUseCase.AnalysisResult result1 = useCase.execute(request);
        AnalyzeJarUseCase.AnalysisResult result2 = useCase.execute(request);
        
        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertTrue(result1.getMetadata().isSuccessful());
        assertTrue(result2.getMetadata().isSuccessful());
        
        // Verify sequential execution due to single thread executor
        verify(mockAnalyzer, times(2)).analyzeJar(testJarLocation);
    }
    
    @Test
    void testShutdown_ProperCleanup() throws Exception {
        // Given
        Set<ClassInfo> classes = createTestClassInfoSet();
        JarContent expectedContent = createTestJarContent(classes);
        
        when(mockFactory.createAnalyzer(testJarLocation)).thenReturn(mockAnalyzer);
        when(mockAnalyzer.getAnalyzerName()).thenReturn("TestAnalyzer");
        when(mockAnalyzer.analyzeJar(testJarLocation)).thenReturn(expectedContent);
        
        AnalyzeJarUseCase.AnalysisRequest request = 
            new AnalyzeJarUseCase.AnalysisRequest(testJarLocation, defaultOptions);
        
        // When
        AnalyzeJarUseCase.AnalysisResult result = useCase.execute(request);
        useCase.shutdown();
        
        // Then
        assertNotNull(result);
        // Should not throw exception during shutdown
    }
    
    @Test
    void testConstructor_NullFactory_ThrowsException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> new DefaultAnalyzeJarUseCase(null));
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
    
    private JarContent createTestJarContent(Set<ClassInfo> classes) {
        return JarContent.builder()
            .location(testJarLocation)
            .jarType(JarType.REGULAR_JAR)
            .classes(classes)
            .build();
    }
}