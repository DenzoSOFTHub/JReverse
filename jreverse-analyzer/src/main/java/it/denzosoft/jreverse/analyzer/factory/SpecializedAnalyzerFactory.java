package it.denzosoft.jreverse.analyzer.factory;

import it.denzosoft.jreverse.analyzer.beancreation.BeanCreationAnalyzer;
import it.denzosoft.jreverse.analyzer.beancreation.JavassistBeanCreationAnalyzer;
import it.denzosoft.jreverse.analyzer.bootstrap.BootstrapAnalyzer;
import it.denzosoft.jreverse.analyzer.bootstrap.JavassistBootstrapAnalyzer;
import it.denzosoft.jreverse.core.port.MainMethodAnalyzer;
import it.denzosoft.jreverse.analyzer.mainmethod.JavassistMainMethodAnalyzer;
import it.denzosoft.jreverse.analyzer.restcontroller.RestControllerAnalyzer;
import it.denzosoft.jreverse.analyzer.restcontroller.JavassistRestControllerAnalyzer;
import it.denzosoft.jreverse.analyzer.restcontroller.RequestMappingAnalyzer;
import it.denzosoft.jreverse.analyzer.restcontroller.JavassistRequestMappingAnalyzer;
import it.denzosoft.jreverse.analyzer.restcontroller.ParameterAnalyzer;
import it.denzosoft.jreverse.analyzer.restcontroller.JavassistParameterAnalyzer;
import it.denzosoft.jreverse.analyzer.restcontroller.ResponseAnalyzer;
import it.denzosoft.jreverse.analyzer.restcontroller.JavassistResponseAnalyzer;
import it.denzosoft.jreverse.analyzer.security.SecurityEntrypointAnalyzer;
import it.denzosoft.jreverse.analyzer.security.JavassistSecurityEntrypointAnalyzer;
import it.denzosoft.jreverse.core.port.RestEndpointAnalyzer;
import it.denzosoft.jreverse.analyzer.restendpoint.JavassistRestEndpointAnalyzer;
import it.denzosoft.jreverse.core.port.ComponentScanAnalyzer;
import it.denzosoft.jreverse.core.port.WebMvcAnalyzer;
import it.denzosoft.jreverse.core.port.ConfigurationAnalyzer;
import it.denzosoft.jreverse.core.port.PropertyAnalyzer;
import it.denzosoft.jreverse.analyzer.property.JavassistPropertyAnalyzer;
import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.port.ServiceLayerAnalyzer;
import it.denzosoft.jreverse.analyzer.servicelayer.JavassistServiceLayerAnalyzer;
import it.denzosoft.jreverse.core.port.RepositoryAnalyzer;
import it.denzosoft.jreverse.analyzer.componentscan.JavassistComponentScanAnalyzer;
import it.denzosoft.jreverse.analyzer.repository.JavassistRepositoryAnalyzer;

/**
 * Factory for creating specialized analyzers for different aspects of Spring Boot applications.
 * This factory provides centralized creation of various analyzer components including
 * bean creation analysis, component scan analysis, main method analysis, and bootstrap sequence analysis.
 * 
 * This complements the main JarAnalyzer factories by providing specialized analyzers
 * that can be used for detailed analysis of specific application aspects.
 */
public class SpecializedAnalyzerFactory {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(SpecializedAnalyzerFactory.class);
    
    /**
     * Creates a BeanCreationAnalyzer for analyzing Spring bean creation patterns.
     * 
     * @return a configured BeanCreationAnalyzer instance
     */
    public static BeanCreationAnalyzer createBeanCreationAnalyzer() {
        LOGGER.debug("Creating BeanCreationAnalyzer");
        return new JavassistBeanCreationAnalyzer();
    }
    
    /**
     * Creates a ComponentScanAnalyzer for analyzing @ComponentScan configurations.
     * 
     * @return a configured ComponentScanAnalyzer instance
     */
    public static ComponentScanAnalyzer createComponentScanAnalyzer() {
        LOGGER.debug("Creating ComponentScanAnalyzer");
        return new JavassistComponentScanAnalyzer();
    }
    
    /**
     * Creates a MainMethodAnalyzer for analyzing main methods and Spring Boot applications.
     * 
     * @return a configured MainMethodAnalyzer instance
     */
    public static MainMethodAnalyzer createMainMethodAnalyzer() {
        LOGGER.debug("Creating MainMethodAnalyzer");
        return new JavassistMainMethodAnalyzer();
    }
    
    /**
     * Creates a BootstrapAnalyzer for analyzing Spring Boot bootstrap sequences.
     * 
     * @return a configured BootstrapAnalyzer instance
     */
    public static BootstrapAnalyzer createBootstrapAnalyzer() {
        LOGGER.debug("Creating BootstrapAnalyzer");
        return new JavassistBootstrapAnalyzer();
    }
    
    /**
     * Creates a RestControllerAnalyzer for analyzing REST controllers and endpoints.
     * 
     * @return a configured RestControllerAnalyzer instance
     */
    public static RestControllerAnalyzer createRestControllerAnalyzer() {
        LOGGER.debug("Creating RestControllerAnalyzer");
        return new JavassistRestControllerAnalyzer();
    }
    
    /**
     * Creates a RequestMappingAnalyzer for analyzing REST endpoint mappings.
     * 
     * @return a configured RequestMappingAnalyzer instance
     */
    public static RequestMappingAnalyzer createRequestMappingAnalyzer() {
        LOGGER.debug("Creating RequestMappingAnalyzer");
        return new JavassistRequestMappingAnalyzer();
    }
    
    /**
     * Creates a ParameterAnalyzer for analyzing REST endpoint parameters.
     * 
     * @return a configured ParameterAnalyzer instance
     */
    public static ParameterAnalyzer createParameterAnalyzer() {
        LOGGER.debug("Creating ParameterAnalyzer");
        return new JavassistParameterAnalyzer();
    }
    
    /**
     * Creates a ResponseAnalyzer for analyzing REST endpoint responses.
     * 
     * @return a configured ResponseAnalyzer instance
     */
    public static ResponseAnalyzer createResponseAnalyzer() {
        LOGGER.debug("Creating ResponseAnalyzer");
        return new JavassistResponseAnalyzer();
    }
    
    /**
     * Creates a RestEndpointAnalyzer for analyzing REST endpoints.
     * 
     * @return a configured RestEndpointAnalyzer instance
     */
    public static RestEndpointAnalyzer createRestEndpointAnalyzer() {
        LOGGER.debug("Creating RestEndpointAnalyzer");
        return new JavassistRestEndpointAnalyzer();
    }
    
    /**
     * Creates a WebMvcAnalyzer for analyzing Spring MVC mappings.
     * 
     * @return a configured WebMvcAnalyzer instance
     */
    public static WebMvcAnalyzer createWebMvcAnalyzer() {
        LOGGER.debug("Creating WebMvcAnalyzer");
        throw new UnsupportedOperationException("WebMvcAnalyzer not implemented yet");
    }
    
    /**
     * Creates a ConfigurationAnalyzer for analyzing Spring configurations.
     * 
     * @return a configured ConfigurationAnalyzer instance
     */
    public static ConfigurationAnalyzer createConfigurationAnalyzer() {
        LOGGER.debug("Creating ConfigurationAnalyzer");
        throw new UnsupportedOperationException("ConfigurationAnalyzer not implemented yet");
    }
    
    /**
     * Creates a PropertyAnalyzer for analyzing Spring Boot property configurations.
     * 
     * @return a configured PropertyAnalyzer instance
     */
    public static PropertyAnalyzer createPropertyAnalyzer() {
        LOGGER.debug("Creating PropertyAnalyzer");
        return new JavassistPropertyAnalyzer();
    }
    
    /**
     * Creates a SecurityEntrypointAnalyzer for analyzing security annotations and entrypoints.
     * 
     * @return a configured SecurityEntrypointAnalyzer instance
     */
    public static SecurityEntrypointAnalyzer createSecurityEntrypointAnalyzer() {
        LOGGER.debug("Creating SecurityEntrypointAnalyzer");
        return new JavassistSecurityEntrypointAnalyzer();
    }
    
    /**
     * Creates a ServiceLayerAnalyzer for analyzing Spring service layer components.
     * 
     * @return a configured ServiceLayerAnalyzer instance
     */
    public static ServiceLayerAnalyzer createServiceLayerAnalyzer() {
        LOGGER.debug("Creating ServiceLayerAnalyzer");
        return new JavassistServiceLayerAnalyzer(createBeanCreationAnalyzer());
    }
    
    /**
     * Creates a RepositoryAnalyzer for analyzing Spring repository components.
     * 
     * @return a configured RepositoryAnalyzer instance
     */
    public static RepositoryAnalyzer createRepositoryAnalyzer() {
        LOGGER.debug("Creating RepositoryAnalyzer");
        return new JavassistRepositoryAnalyzer();
    }
    
    /**
     * Creates all specialized analyzers for comprehensive Spring Boot application analysis.
     * 
     * @return a container with all specialized analyzers
     */
    public static AnalyzerBundle createAnalyzerBundle() {
        LOGGER.info("Creating complete analyzer bundle");
        return new AnalyzerBundle(
            createBeanCreationAnalyzer(),
            createComponentScanAnalyzer(),
            createMainMethodAnalyzer(),
            createBootstrapAnalyzer(),
            createRestControllerAnalyzer(),
            createRequestMappingAnalyzer(),
            createParameterAnalyzer(),
            createResponseAnalyzer(),
            createRestEndpointAnalyzer(),
            createWebMvcAnalyzer(),
            createConfigurationAnalyzer(),
            createPropertyAnalyzer(),
            createSecurityEntrypointAnalyzer(),
            createServiceLayerAnalyzer(),
            createRepositoryAnalyzer()
        );
    }
    
    /**
     * Container class for holding multiple specialized analyzers.
     * Provides convenient access to all analyzer types for comprehensive analysis.
     */
    public static class AnalyzerBundle {
        private final BeanCreationAnalyzer beanCreationAnalyzer;
        private final ComponentScanAnalyzer componentScanAnalyzer;
        private final MainMethodAnalyzer mainMethodAnalyzer;
        private final BootstrapAnalyzer bootstrapAnalyzer;
        private final RestControllerAnalyzer restControllerAnalyzer;
        private final RequestMappingAnalyzer requestMappingAnalyzer;
        private final ParameterAnalyzer parameterAnalyzer;
        private final ResponseAnalyzer responseAnalyzer;
        private final RestEndpointAnalyzer restEndpointAnalyzer;
        private final WebMvcAnalyzer webMvcAnalyzer;
        private final ConfigurationAnalyzer configurationAnalyzer;
        private final PropertyAnalyzer propertyAnalyzer;
        private final SecurityEntrypointAnalyzer securityEntrypointAnalyzer;
        private final ServiceLayerAnalyzer serviceLayerAnalyzer;
        private final RepositoryAnalyzer repositoryAnalyzer;
        
        public AnalyzerBundle(BeanCreationAnalyzer beanCreationAnalyzer,
                             ComponentScanAnalyzer componentScanAnalyzer,
                             MainMethodAnalyzer mainMethodAnalyzer,
                             BootstrapAnalyzer bootstrapAnalyzer,
                             RestControllerAnalyzer restControllerAnalyzer,
                             RequestMappingAnalyzer requestMappingAnalyzer,
                             ParameterAnalyzer parameterAnalyzer,
                             ResponseAnalyzer responseAnalyzer,
                             RestEndpointAnalyzer restEndpointAnalyzer,
                             WebMvcAnalyzer webMvcAnalyzer,
                             ConfigurationAnalyzer configurationAnalyzer,
                             PropertyAnalyzer propertyAnalyzer,
                             SecurityEntrypointAnalyzer securityEntrypointAnalyzer,
                             ServiceLayerAnalyzer serviceLayerAnalyzer,
                             RepositoryAnalyzer repositoryAnalyzer) {
            this.beanCreationAnalyzer = beanCreationAnalyzer;
            this.componentScanAnalyzer = componentScanAnalyzer;
            this.mainMethodAnalyzer = mainMethodAnalyzer;
            this.bootstrapAnalyzer = bootstrapAnalyzer;
            this.restControllerAnalyzer = restControllerAnalyzer;
            this.requestMappingAnalyzer = requestMappingAnalyzer;
            this.parameterAnalyzer = parameterAnalyzer;
            this.responseAnalyzer = responseAnalyzer;
            this.restEndpointAnalyzer = restEndpointAnalyzer;
            this.webMvcAnalyzer = webMvcAnalyzer;
            this.configurationAnalyzer = configurationAnalyzer;
            this.propertyAnalyzer = propertyAnalyzer;
            this.securityEntrypointAnalyzer = securityEntrypointAnalyzer;
            this.serviceLayerAnalyzer = serviceLayerAnalyzer;
            this.repositoryAnalyzer = repositoryAnalyzer;
        }
        
        public BeanCreationAnalyzer getBeanCreationAnalyzer() {
            return beanCreationAnalyzer;
        }
        
        public ComponentScanAnalyzer getComponentScanAnalyzer() {
            return componentScanAnalyzer;
        }
        
        public MainMethodAnalyzer getMainMethodAnalyzer() {
            return mainMethodAnalyzer;
        }
        
        public BootstrapAnalyzer getBootstrapAnalyzer() {
            return bootstrapAnalyzer;
        }
        
        public RestControllerAnalyzer getRestControllerAnalyzer() {
            return restControllerAnalyzer;
        }
        
        public RequestMappingAnalyzer getRequestMappingAnalyzer() {
            return requestMappingAnalyzer;
        }
        
        public ParameterAnalyzer getParameterAnalyzer() {
            return parameterAnalyzer;
        }
        
        public ResponseAnalyzer getResponseAnalyzer() {
            return responseAnalyzer;
        }
        
        public RestEndpointAnalyzer getRestEndpointAnalyzer() {
            return restEndpointAnalyzer;
        }
        
        public WebMvcAnalyzer getWebMvcAnalyzer() {
            return webMvcAnalyzer;
        }
        
        public ConfigurationAnalyzer getConfigurationAnalyzer() {
            return configurationAnalyzer;
        }
        
        public PropertyAnalyzer getPropertyAnalyzer() {
            return propertyAnalyzer;
        }
        
        public SecurityEntrypointAnalyzer getSecurityEntrypointAnalyzer() {
            return securityEntrypointAnalyzer;
        }
        
        public ServiceLayerAnalyzer getServiceLayerAnalyzer() {
            return serviceLayerAnalyzer;
        }
        
        public RepositoryAnalyzer getRepositoryAnalyzer() {
            return repositoryAnalyzer;
        }
        
        /**
         * Checks if all analyzers in the bundle can analyze the given JAR content.
         * 
         * @param jarContent the JAR content to check
         * @return true if all analyzers can process the content
         */
        public boolean canAnalyzeAll(it.denzosoft.jreverse.core.model.JarContent jarContent) {
            return beanCreationAnalyzer.canAnalyze(jarContent) &&
                   componentScanAnalyzer.canAnalyze(jarContent) &&
                   mainMethodAnalyzer.canAnalyze(jarContent) &&
                   bootstrapAnalyzer.canAnalyze(jarContent) &&
                   restControllerAnalyzer.canAnalyze(jarContent) &&
                   requestMappingAnalyzer.canAnalyze(jarContent) &&
                   parameterAnalyzer.canAnalyze(jarContent) &&
                   restEndpointAnalyzer.canAnalyze(jarContent) &&
                   webMvcAnalyzer.canAnalyze(jarContent) &&
                   configurationAnalyzer.canAnalyze(jarContent) &&
                   propertyAnalyzer.canAnalyze(jarContent);
        }
    }
}