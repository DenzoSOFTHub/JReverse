package it.denzosoft.jreverse.analyzer.bootstrap;

import it.denzosoft.jreverse.analyzer.beancreation.BeanCreationAnalyzer;
import it.denzosoft.jreverse.analyzer.beancreation.BeanCreationResult;
import it.denzosoft.jreverse.core.port.ComponentScanAnalyzer;
import it.denzosoft.jreverse.core.model.ComponentScanAnalysisResult;
import it.denzosoft.jreverse.analyzer.factory.SpecializedAnalyzerFactory;
import it.denzosoft.jreverse.core.model.AnalysisMetadata;
import it.denzosoft.jreverse.core.model.MainMethodAnalysisResult;
import it.denzosoft.jreverse.core.port.MainMethodAnalyzer;
import it.denzosoft.jreverse.core.model.SpringApplicationCallInfo;
import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.MethodInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Javassist-based implementation of BootstrapAnalyzer.
 * Analyzes Spring Boot application bootstrap sequences using bytecode analysis
 * to reconstruct the startup flow and generate sequence diagrams.
 */
public class JavassistBootstrapAnalyzer implements BootstrapAnalyzer {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistBootstrapAnalyzer.class);
    
    private final MainMethodAnalyzer mainMethodAnalyzer;
    private final ComponentScanAnalyzer componentScanAnalyzer;
    private final BeanCreationAnalyzer beanCreationAnalyzer;
    
    /**
     * Default constructor that creates its own analyzer dependencies.
     */
    public JavassistBootstrapAnalyzer() {
        this.mainMethodAnalyzer = SpecializedAnalyzerFactory.createMainMethodAnalyzer();
        this.componentScanAnalyzer = SpecializedAnalyzerFactory.createComponentScanAnalyzer();
        this.beanCreationAnalyzer = SpecializedAnalyzerFactory.createBeanCreationAnalyzer();
    }
    
    /**
     * Constructor with dependency injection for testing.
     */
    public JavassistBootstrapAnalyzer(MainMethodAnalyzer mainMethodAnalyzer,
                                     ComponentScanAnalyzer componentScanAnalyzer,
                                     BeanCreationAnalyzer beanCreationAnalyzer) {
        this.mainMethodAnalyzer = mainMethodAnalyzer;
        this.componentScanAnalyzer = componentScanAnalyzer;
        this.beanCreationAnalyzer = beanCreationAnalyzer;
    }
    
    @Override
    public BootstrapAnalysisResult analyzeBootstrap(JarContent jarContent) {
        LOGGER.info("Starting bootstrap analysis for JAR: " + jarContent.getLocation().getFileName());
        long startTime = System.currentTimeMillis();
        
        try {
            // Quick validation
            if (!canAnalyze(jarContent)) {
                LOGGER.warn("Cannot analyze JAR content for bootstrap sequence");
                return BootstrapAnalysisResult.analysisError("Cannot analyze JAR content");
            }
            
            // Determine if this is a Spring Boot application
            if (!isSpringBootApplication(jarContent)) {
                LOGGER.info("JAR does not appear to be a Spring Boot application");
                return analyzeRegularJavaApplication(jarContent, startTime);
            }
            
            LOGGER.info("Detected Spring Boot application, performing comprehensive bootstrap analysis");
            return analyzeSpringBootApplication(jarContent, startTime);
            
        } catch (Exception e) {
            LOGGER.error("Error during bootstrap analysis: " + e.getMessage(), e);
            return BootstrapAnalysisResult.analysisError("Analysis failed: " + e.getMessage());
        }
    }
    
    /**
     * Analyzes a Spring Boot application's bootstrap sequence.
     */
    private BootstrapAnalysisResult analyzeSpringBootApplication(JarContent jarContent, long startTime) {
        LOGGER.info("Performing Spring Boot application bootstrap analysis");
        
        // Step 1: Analyze main method
        MainMethodAnalysisResult mainMethodResult = mainMethodAnalyzer.analyzeMainMethod(jarContent);
        if (!mainMethodResult.hasMainMethod()) {
            LOGGER.warn("No main method found in Spring Boot application");
            return BootstrapAnalysisResult.analysisError("No main method found");
        }
        
        // Step 2: Analyze component scanning
        ComponentScanAnalysisResult componentScanResult = componentScanAnalyzer.analyzeComponentScan(jarContent);
        
        // Step 3: Analyze bean creation
        BeanCreationResult beanCreationResult = beanCreationAnalyzer.analyzeBeanCreation(jarContent);
        
        // Step 4: Build bootstrap sequence
        List<BootstrapSequenceStep> sequenceSteps = buildBootstrapSequence(
            jarContent, mainMethodResult, componentScanResult, beanCreationResult);
        
        // Step 5: Collect auto-configurations and components
        Set<String> autoConfigurations = extractAutoConfigurations(jarContent);
        List<String> discoveredComponents = extractDiscoveredComponents(beanCreationResult);
        
        // Step 6: Calculate timing information
        long analysisTime = System.currentTimeMillis() - startTime;
        BootstrapTimingInfo timingInfo = BootstrapTimingInfo.fromSequenceSteps(sequenceSteps, analysisTime);
        
        LOGGER.info("Bootstrap analysis completed successfully. Found " + sequenceSteps.size() + " sequence steps");
        
        return BootstrapAnalysisResult.springBootApplication(
            sequenceSteps,
            mainMethodResult,
            componentScanResult,
            beanCreationResult,
            timingInfo,
            autoConfigurations,
            discoveredComponents
        );
    }
    
    /**
     * Analyzes a regular Java application (non-Spring Boot).
     */
    private BootstrapAnalysisResult analyzeRegularJavaApplication(JarContent jarContent, long startTime) {
        LOGGER.info("Performing regular Java application analysis");
        
        MainMethodAnalysisResult mainMethodResult = mainMethodAnalyzer.analyzeMainMethod(jarContent);
        long analysisTime = System.currentTimeMillis() - startTime;
        
        if (!mainMethodResult.hasMainMethod()) {
            return BootstrapAnalysisResult.noBootstrapDetected();
        }
        
        AnalysisMetadata metadata = AnalysisMetadata.successful();
        
        return BootstrapAnalysisResult.regularJavaApplication(mainMethodResult, metadata);
    }
    
    /**
     * Builds the complete bootstrap sequence by analyzing the startup flow.
     */
    private List<BootstrapSequenceStep> buildBootstrapSequence(
            JarContent jarContent,
            MainMethodAnalysisResult mainMethodResult,
            ComponentScanAnalysisResult componentScanResult,
            BeanCreationResult beanCreationResult) {
        
        List<BootstrapSequenceStep> steps = new ArrayList<>();
        int sequenceNumber = 1;
        
        // Phase 1: Main method execution
        if (mainMethodResult.hasMainMethod()) {
            steps.addAll(createMainMethodSteps(mainMethodResult, sequenceNumber));
            sequenceNumber += steps.size();
        }
        
        // Phase 2: SpringApplication.run() invocation
        if (mainMethodResult.getSpringApplicationCall().isPresent()) {
            steps.addAll(createSpringApplicationRunSteps(mainMethodResult.getSpringApplicationCall().get(), sequenceNumber));
            sequenceNumber = steps.size() + 1;
        }
        
        // Phase 3: Context preparation
        steps.addAll(createContextPreparationSteps(jarContent, sequenceNumber));
        sequenceNumber = steps.size() + 1;
        
        // Phase 4: Component scanning
        if (componentScanResult != null) {
            steps.addAll(createComponentScanSteps(componentScanResult, sequenceNumber));
            sequenceNumber = steps.size() + 1;
        }
        
        // Phase 5: Auto-configuration
        steps.addAll(createAutoConfigurationSteps(jarContent, sequenceNumber));
        sequenceNumber = steps.size() + 1;
        
        // Phase 6: Bean creation
        if (beanCreationResult != null) {
            steps.addAll(createBeanCreationSteps(beanCreationResult, sequenceNumber));
            sequenceNumber = steps.size() + 1;
        }
        
        // Phase 7: Post-processing
        steps.addAll(createPostProcessingSteps(sequenceNumber));
        sequenceNumber = steps.size() + 1;
        
        // Phase 8: Application ready
        steps.addAll(createApplicationReadySteps(sequenceNumber));
        
        return steps;
    }
    
    /**
     * Creates sequence steps for main method execution.
     */
    private List<BootstrapSequenceStep> createMainMethodSteps(MainMethodAnalysisResult mainMethodResult, int startSequence) {
        List<BootstrapSequenceStep> steps = new ArrayList<>();
        
        if (mainMethodResult.getMainMethod().isPresent()) {
            MethodInfo mainMethod = mainMethodResult.getMainMethod().get();
            
            steps.add(new BootstrapSequenceStep.Builder()
                .sequenceNumber(startSequence)
                .phase(BootstrapSequencePhase.MAIN_METHOD_EXECUTION)
                .participantClass(mainMethod.getDeclaringClassName())
                .methodName("main")
                .description("Application entry point execution")
                .classInfo(null) // Simplified - could get ClassInfo from declaringClassName
                .methodInfo(mainMethod)
                .parameters(List.of("String[] args"))
                .estimatedDurationMs(10)
                .stepType(BootstrapSequenceStep.StepType.METHOD_CALL)
                .build());
        }
        
        return steps;
    }
    
    /**
     * Creates sequence steps for SpringApplication.run() invocation.
     */
    private List<BootstrapSequenceStep> createSpringApplicationRunSteps(SpringApplicationCallInfo callInfo, int startSequence) {
        List<BootstrapSequenceStep> steps = new ArrayList<>();
        
        steps.add(new BootstrapSequenceStep.Builder()
            .sequenceNumber(startSequence)
            .phase(BootstrapSequencePhase.SPRING_APPLICATION_RUN)
            .participantClass("org.springframework.boot.SpringApplication")
            .methodName("run")
            .description("SpringApplication.run() method invocation")
            .parameters(List.of("Class<?> primarySource", "String[] args"))
            .returnType("ConfigurableApplicationContext")
            .estimatedDurationMs(50)
            .stepType(BootstrapSequenceStep.StepType.METHOD_CALL)
            .build());
        
        return steps;
    }
    
    /**
     * Creates sequence steps for application context preparation.
     */
    private List<BootstrapSequenceStep> createContextPreparationSteps(JarContent jarContent, int startSequence) {
        List<BootstrapSequenceStep> steps = new ArrayList<>();
        
        steps.add(new BootstrapSequenceStep.Builder()
            .sequenceNumber(startSequence)
            .phase(BootstrapSequencePhase.CONTEXT_PREPARATION)
            .participantClass("org.springframework.boot.SpringApplication")
            .methodName("prepareContext")
            .description("Application context creation and preparation")
            .estimatedDurationMs(100)
            .stepType(BootstrapSequenceStep.StepType.INTERNAL_PROCESSING)
            .build());
        
        steps.add(new BootstrapSequenceStep.Builder()
            .sequenceNumber(startSequence + 1)
            .phase(BootstrapSequencePhase.CONTEXT_PREPARATION)
            .participantClass("org.springframework.context.annotation.AnnotationConfigApplicationContext")
            .methodName("<init>")
            .description("Annotation-based application context creation")
            .estimatedDurationMs(75)
            .stepType(BootstrapSequenceStep.StepType.METHOD_CALL)
            .build());
        
        return steps;
    }
    
    /**
     * Creates sequence steps for component scanning phase.
     */
    private List<BootstrapSequenceStep> createComponentScanSteps(ComponentScanAnalysisResult componentScanResult, int startSequence) {
        List<BootstrapSequenceStep> steps = new ArrayList<>();
        
        steps.add(new BootstrapSequenceStep.Builder()
            .sequenceNumber(startSequence)
            .phase(BootstrapSequencePhase.COMPONENT_SCANNING)
            .participantClass("org.springframework.context.annotation.ClassPathBeanDefinitionScanner")
            .methodName("scan")
            .description("Scanning for Spring components (@Component, @Service, @Repository, @Controller)")
            .parameters(List.of("String[] basePackages"))
            .estimatedDurationMs(200)
            .stepType(BootstrapSequenceStep.StepType.INTERNAL_PROCESSING)
            .build());
        
        return steps;
    }
    
    /**
     * Creates sequence steps for auto-configuration activation.
     */
    private List<BootstrapSequenceStep> createAutoConfigurationSteps(JarContent jarContent, int startSequence) {
        List<BootstrapSequenceStep> steps = new ArrayList<>();
        
        steps.add(new BootstrapSequenceStep.Builder()
            .sequenceNumber(startSequence)
            .phase(BootstrapSequencePhase.AUTO_CONFIGURATION)
            .participantClass("org.springframework.boot.autoconfigure.AutoConfigurationImportSelector")
            .methodName("selectImports")
            .description("Spring Boot auto-configuration classes activation")
            .estimatedDurationMs(300)
            .stepType(BootstrapSequenceStep.StepType.INTERNAL_PROCESSING)
            .build());
        
        return steps;
    }
    
    /**
     * Creates sequence steps for bean creation and dependency injection.
     */
    private List<BootstrapSequenceStep> createBeanCreationSteps(BeanCreationResult beanCreationResult, int startSequence) {
        List<BootstrapSequenceStep> steps = new ArrayList<>();
        
        steps.add(new BootstrapSequenceStep.Builder()
            .sequenceNumber(startSequence)
            .phase(BootstrapSequencePhase.BEAN_CREATION)
            .participantClass("org.springframework.beans.factory.support.DefaultListableBeanFactory")
            .methodName("preInstantiateSingletons")
            .description("Spring bean instantiation and dependency injection")
            .estimatedDurationMs(500)
            .stepType(BootstrapSequenceStep.StepType.INTERNAL_PROCESSING)
            .build());
        
        return steps;
    }
    
    /**
     * Creates sequence steps for post-processing phase.
     */
    private List<BootstrapSequenceStep> createPostProcessingSteps(int startSequence) {
        List<BootstrapSequenceStep> steps = new ArrayList<>();
        
        steps.add(new BootstrapSequenceStep.Builder()
            .sequenceNumber(startSequence)
            .phase(BootstrapSequencePhase.POST_PROCESSING)
            .participantClass("org.springframework.beans.factory.config.BeanPostProcessor")
            .methodName("postProcessAfterInitialization")
            .description("Bean post-processing and initialization callbacks")
            .estimatedDurationMs(150)
            .stepType(BootstrapSequenceStep.StepType.LIFECYCLE_CALLBACK)
            .build());
        
        return steps;
    }
    
    /**
     * Creates sequence steps for application ready phase.
     */
    private List<BootstrapSequenceStep> createApplicationReadySteps(int startSequence) {
        List<BootstrapSequenceStep> steps = new ArrayList<>();
        
        steps.add(new BootstrapSequenceStep.Builder()
            .sequenceNumber(startSequence)
            .phase(BootstrapSequencePhase.APPLICATION_READY)
            .participantClass("org.springframework.boot.SpringApplication")
            .methodName("publishEvent")
            .description("Application ready event and final initialization")
            .parameters(List.of("ApplicationReadyEvent"))
            .estimatedDurationMs(25)
            .stepType(BootstrapSequenceStep.StepType.EVENT)
            .build());
        
        return steps;
    }
    
    /**
     * Extracts auto-configuration classes from the JAR.
     */
    private Set<String> extractAutoConfigurations(JarContent jarContent) {
        Set<String> autoConfigurations = new HashSet<>();
        
        // Look for auto-configuration classes
        jarContent.getClasses().stream()
            .filter(classInfo -> isAutoConfiguration(classInfo))
            .forEach(classInfo -> autoConfigurations.add(classInfo.getFullyQualifiedName()));
        
        return autoConfigurations;
    }
    
    /**
     * Extracts discovered components from bean creation analysis.
     */
    private List<String> extractDiscoveredComponents(BeanCreationResult beanCreationResult) {
        if (beanCreationResult == null) {
            return List.of();
        }
        
        List<String> components = new ArrayList<>();
        
        // Add all Spring stereotype components
        components.addAll(beanCreationResult.getServiceBeans().stream()
            .map(beanInfo -> beanInfo.getDeclaringClassName())
            .collect(java.util.stream.Collectors.toList()));
        
        components.addAll(beanCreationResult.getRepositoryBeans().stream()
            .map(beanInfo -> beanInfo.getDeclaringClassName())
            .collect(java.util.stream.Collectors.toList()));
        
        components.addAll(beanCreationResult.getControllerBeans().stream()
            .map(beanInfo -> beanInfo.getDeclaringClassName())
            .collect(java.util.stream.Collectors.toList()));
        
        // Add configuration classes
        components.addAll(beanCreationResult.getConfigurationBeans().stream()
            .map(beanInfo -> beanInfo.getDeclaringClassName())
            .collect(java.util.stream.Collectors.toList()));
        
        return components;
    }
    
    /**
     * Checks if a class is an auto-configuration class.
     */
    private boolean isAutoConfiguration(ClassInfo classInfo) {
        return classInfo.getAnnotations().stream().anyMatch(annotation ->
            annotation.getType().contains("AutoConfiguration") ||
            annotation.getType().contains("EnableAutoConfiguration") ||
            classInfo.getFullyQualifiedName().contains("AutoConfiguration")
        );
    }
    
    @Override
    public boolean canAnalyze(JarContent jarContent) {
        if (jarContent == null || jarContent.getClassCount() == 0) {
            return false;
        }
        
        // Must be able to run all sub-analyzers
        return mainMethodAnalyzer.canAnalyze(jarContent) &&
               componentScanAnalyzer.canAnalyze(jarContent) &&
               beanCreationAnalyzer.canAnalyze(jarContent);
    }
    
    @Override
    public boolean isSpringBootApplication(JarContent jarContent) {
        if (!canAnalyze(jarContent)) {
            return false;
        }
        
        // Check for Spring Boot main method
        MainMethodAnalysisResult mainMethodResult = mainMethodAnalyzer.analyzeMainMethod(jarContent);
        if (mainMethodResult.isSpringBootApplication()) {
            return true;
        }
        
        // Check for Spring Boot dependencies using parent interface default method
        return BootstrapAnalyzer.super.isSpringBootApplication(jarContent);
    }
}