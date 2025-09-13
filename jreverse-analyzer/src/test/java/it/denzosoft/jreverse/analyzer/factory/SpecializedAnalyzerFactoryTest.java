package it.denzosoft.jreverse.analyzer.factory;

import it.denzosoft.jreverse.analyzer.beancreation.BeanCreationAnalyzer;
import it.denzosoft.jreverse.analyzer.bootstrap.BootstrapAnalyzer;
import it.denzosoft.jreverse.analyzer.restcontroller.RestControllerAnalyzer;
import it.denzosoft.jreverse.analyzer.restcontroller.RequestMappingAnalyzer;
import it.denzosoft.jreverse.analyzer.restcontroller.ParameterAnalyzer;
import it.denzosoft.jreverse.analyzer.restcontroller.ResponseAnalyzer;
import it.denzosoft.jreverse.analyzer.security.SecurityEntrypointAnalyzer;
import it.denzosoft.jreverse.core.port.MainMethodAnalyzer;
import it.denzosoft.jreverse.core.port.RestEndpointAnalyzer;
import it.denzosoft.jreverse.core.port.ComponentScanAnalyzer;
import it.denzosoft.jreverse.core.port.WebMvcAnalyzer;
import it.denzosoft.jreverse.core.port.ConfigurationAnalyzer;
import it.denzosoft.jreverse.core.port.PropertyAnalyzer;
import it.denzosoft.jreverse.core.port.ServiceLayerAnalyzer;
import it.denzosoft.jreverse.core.port.RepositoryAnalyzer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SpecializedAnalyzerFactory.
 * Tests only implemented analyzers - non-implemented ones correctly throw UnsupportedOperationException.
 */
class SpecializedAnalyzerFactoryTest {
    
    @Test
    void testCreateBeanCreationAnalyzer_ReturnsValidInstance() {
        BeanCreationAnalyzer analyzer = SpecializedAnalyzerFactory.createBeanCreationAnalyzer();
        
        assertNotNull(analyzer);
        assertEquals("it.denzosoft.jreverse.analyzer.beancreation.JavassistBeanCreationAnalyzer", 
                     analyzer.getClass().getName());
    }
    
    @Test
    void testCreateComponentScanAnalyzer_ThrowsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            SpecializedAnalyzerFactory.createComponentScanAnalyzer();
        });
    }
    
    @Test
    void testCreateWebMvcAnalyzer_ThrowsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            SpecializedAnalyzerFactory.createWebMvcAnalyzer();
        });
    }
    
    @Test
    void testCreateConfigurationAnalyzer_ThrowsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            SpecializedAnalyzerFactory.createConfigurationAnalyzer();
        });
    }
    
    @Test
    void testCreateMainMethodAnalyzer_ReturnsValidInstance() {
        MainMethodAnalyzer analyzer = SpecializedAnalyzerFactory.createMainMethodAnalyzer();
        
        assertNotNull(analyzer);
        assertEquals("it.denzosoft.jreverse.analyzer.mainmethod.JavassistMainMethodAnalyzer",
                     analyzer.getClass().getName());
    }
    
    @Test
    void testCreateBootstrapAnalyzer_ThrowsUnsupportedOperationException() {
        // BootstrapAnalyzer depends on ComponentScanAnalyzer which is not implemented
        assertThrows(UnsupportedOperationException.class, () -> {
            SpecializedAnalyzerFactory.createBootstrapAnalyzer();
        });
    }
    
    @Test
    void testCreateRestControllerAnalyzer_ReturnsValidInstance() {
        RestControllerAnalyzer analyzer = SpecializedAnalyzerFactory.createRestControllerAnalyzer();
        
        assertNotNull(analyzer);
        assertEquals("it.denzosoft.jreverse.analyzer.restcontroller.JavassistRestControllerAnalyzer",
                     analyzer.getClass().getName());
    }
    
    @Test
    void testCreateRequestMappingAnalyzer_ReturnsValidInstance() {
        RequestMappingAnalyzer analyzer = SpecializedAnalyzerFactory.createRequestMappingAnalyzer();
        
        assertNotNull(analyzer);
        assertEquals("it.denzosoft.jreverse.analyzer.restcontroller.JavassistRequestMappingAnalyzer",
                     analyzer.getClass().getName());
    }
    
    @Test
    void testCreateParameterAnalyzer_ReturnsValidInstance() {
        ParameterAnalyzer analyzer = SpecializedAnalyzerFactory.createParameterAnalyzer();
        
        assertNotNull(analyzer);
        assertEquals("it.denzosoft.jreverse.analyzer.restcontroller.JavassistParameterAnalyzer",
                     analyzer.getClass().getName());
    }
    
    @Test
    void testCreateResponseAnalyzer_ReturnsValidInstance() {
        ResponseAnalyzer analyzer = SpecializedAnalyzerFactory.createResponseAnalyzer();
        
        assertNotNull(analyzer);
        assertEquals("it.denzosoft.jreverse.analyzer.restcontroller.JavassistResponseAnalyzer",
                     analyzer.getClass().getName());
    }
    
    @Test
    void testCreateRestEndpointAnalyzer_ReturnsValidInstance() {
        RestEndpointAnalyzer analyzer = SpecializedAnalyzerFactory.createRestEndpointAnalyzer();
        
        assertNotNull(analyzer);
        assertEquals("it.denzosoft.jreverse.analyzer.restendpoint.JavassistRestEndpointAnalyzer",
                     analyzer.getClass().getName());
    }
    
    @Test
    void testCreatePropertyAnalyzer_ReturnsValidInstance() {
        PropertyAnalyzer analyzer = SpecializedAnalyzerFactory.createPropertyAnalyzer();
        
        assertNotNull(analyzer);
        assertEquals("it.denzosoft.jreverse.analyzer.property.JavassistPropertyAnalyzer",
                     analyzer.getClass().getName());
    }
    
    @Test
    void testCreateSecurityEntrypointAnalyzer_ReturnsValidInstance() {
        SecurityEntrypointAnalyzer analyzer = SpecializedAnalyzerFactory.createSecurityEntrypointAnalyzer();
        
        assertNotNull(analyzer);
        assertEquals("it.denzosoft.jreverse.analyzer.security.JavassistSecurityEntrypointAnalyzer",
                     analyzer.getClass().getName());
    }
    
    @Test
    void testCreateServiceLayerAnalyzer_ReturnsValidInstance() {
        ServiceLayerAnalyzer analyzer = SpecializedAnalyzerFactory.createServiceLayerAnalyzer();
        
        assertNotNull(analyzer);
        assertEquals("it.denzosoft.jreverse.analyzer.servicelayer.JavassistServiceLayerAnalyzer",
                     analyzer.getClass().getName());
    }
    
    @Test
    void testCreateRepositoryAnalyzer_ReturnsValidInstance() {
        RepositoryAnalyzer analyzer = SpecializedAnalyzerFactory.createRepositoryAnalyzer();
        
        assertNotNull(analyzer);
        assertEquals("it.denzosoft.jreverse.analyzer.repository.JavassistRepositoryAnalyzer",
                     analyzer.getClass().getName());
    }
    
    @Test 
    void testCreateAnalyzerBundle_ThrowsUnsupportedOperationException() {
        // The analyzer bundle creation should fail because it tries to create unimplemented analyzers
        assertThrows(UnsupportedOperationException.class, () -> {
            SpecializedAnalyzerFactory.createAnalyzerBundle();
        });
    }
    
    @Test
    void testAnalyzerBundleContainsServiceLayerAnalyzer() {
        // When ComponentScanAnalyzer, WebMvcAnalyzer and ConfigurationAnalyzer are implemented,
        // this test will verify that the bundle includes ServiceLayerAnalyzer
        // For now, we just verify that the factory method exists and works
        ServiceLayerAnalyzer analyzer = SpecializedAnalyzerFactory.createServiceLayerAnalyzer();
        assertNotNull(analyzer);
    }
    
    @Test
    void testAnalyzerBundleContainsRepositoryAnalyzer() {
        // When ComponentScanAnalyzer, WebMvcAnalyzer and ConfigurationAnalyzer are implemented,
        // this test will verify that the bundle includes RepositoryAnalyzer
        // For now, we just verify that the factory method exists and works
        RepositoryAnalyzer analyzer = SpecializedAnalyzerFactory.createRepositoryAnalyzer();
        assertNotNull(analyzer);
    }
}