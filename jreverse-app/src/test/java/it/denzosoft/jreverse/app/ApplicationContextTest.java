package it.denzosoft.jreverse.app;

import it.denzosoft.jreverse.core.pattern.AnalyzerFactory;
import it.denzosoft.jreverse.core.pattern.ReportStrategy;
import it.denzosoft.jreverse.core.usecase.AnalyzeJarUseCase;
import it.denzosoft.jreverse.core.usecase.GenerateReportUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ApplicationContext
 */
public class ApplicationContextTest {
    
    private ApplicationContext applicationContext;
    
    @BeforeEach
    void setUp() {
        applicationContext = new ApplicationContext();
    }
    
    @Test
    void shouldInitializeWithDefaultDependencies() {
        assertThat(applicationContext.getAnalyzerFactory()).isNotNull();
        assertThat(applicationContext.getReportStrategy()).isNotNull();
        assertThat(applicationContext.getAnalyzeJarUseCase()).isNotNull();
        assertThat(applicationContext.getGenerateReportUseCase()).isNotNull();
    }
    
    @Test
    void shouldProvideAnalyzerFactoryInstance() {
        AnalyzerFactory analyzerFactory = applicationContext.getAnalyzerFactory();
        
        assertThat(analyzerFactory).isNotNull();
        assertThat(analyzerFactory.getFactoryName()).isEqualTo("Javassist Analyzer Factory");
    }
    
    @Test
    void shouldProvideReportStrategyInstance() {
        ReportStrategy reportStrategy = applicationContext.getReportStrategy();
        
        assertThat(reportStrategy).isNotNull();
        assertThat(reportStrategy.getStrategyName()).isEqualTo("HTML Report Generator");
    }
    
    @Test
    void shouldProvideUseCaseInstances() {
        AnalyzeJarUseCase analyzeJarUseCase = applicationContext.getAnalyzeJarUseCase();
        GenerateReportUseCase generateReportUseCase = applicationContext.getGenerateReportUseCase();
        
        assertThat(analyzeJarUseCase).isNotNull();
        assertThat(generateReportUseCase).isNotNull();
    }
    
    @Test
    void shouldHandleShutdownGracefully() {
        assertThatCode(() -> applicationContext.shutdown())
            .doesNotThrowAnyException();
    }
    
    @Test
    void shouldReturnSameInstancesOnMultipleCalls() {
        // Test that repeated calls return the same instances
        assertThat(applicationContext.getAnalyzerFactory()).isSameAs(applicationContext.getAnalyzerFactory());
        assertThat(applicationContext.getReportStrategy()).isSameAs(applicationContext.getReportStrategy());
        assertThat(applicationContext.getAnalyzeJarUseCase()).isSameAs(applicationContext.getAnalyzeJarUseCase());
        assertThat(applicationContext.getGenerateReportUseCase()).isSameAs(applicationContext.getGenerateReportUseCase());
    }
    
    @Test
    void shouldThrowUnsupportedOperationForUseCases() {
        AnalyzeJarUseCase analyzeJarUseCase = applicationContext.getAnalyzeJarUseCase();
        GenerateReportUseCase generateReportUseCase = applicationContext.getGenerateReportUseCase();
        
        // Test that use cases throw UnsupportedOperationException since they're not yet implemented
        assertThatThrownBy(() -> analyzeJarUseCase.execute(null))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("AnalyzeJarUseCase not yet implemented");
            
        assertThatThrownBy(() -> generateReportUseCase.execute(null))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("GenerateReportUseCase not yet implemented");
    }
    
    @Test 
    void shouldHaveProperToStringRepresentation() {
        String contextString = applicationContext.toString();
        assertThat(contextString).contains("ApplicationContext");
    }
}