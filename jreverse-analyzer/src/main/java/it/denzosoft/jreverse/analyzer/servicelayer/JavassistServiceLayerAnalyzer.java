package it.denzosoft.jreverse.analyzer.servicelayer;

import it.denzosoft.jreverse.analyzer.beancreation.BeanCreationAnalyzer;
import it.denzosoft.jreverse.analyzer.beancreation.BeanCreationResult;
import it.denzosoft.jreverse.analyzer.beancreation.BeanInfo;
import it.denzosoft.jreverse.core.model.*;
import it.denzosoft.jreverse.core.port.ServiceLayerAnalyzer;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Javassist-based implementation for analyzing Spring Boot service layer components.
 * Delegates to BeanCreationAnalyzer and transforms results into service-focused format.
 */
public class JavassistServiceLayerAnalyzer implements ServiceLayerAnalyzer {
    
    private static final Logger LOGGER = Logger.getLogger(JavassistServiceLayerAnalyzer.class.getName());
    
    private final BeanCreationAnalyzer beanCreationAnalyzer;
    
    public JavassistServiceLayerAnalyzer(BeanCreationAnalyzer beanCreationAnalyzer) {
        this.beanCreationAnalyzer = Objects.requireNonNull(beanCreationAnalyzer, 
            "BeanCreationAnalyzer is required");
    }
    
    @Override
    public ServiceLayerAnalysisResult analyzeServiceLayer(JarContent jarContent) {
        LOGGER.info("Starting service layer analysis for JAR: " + jarContent.getLocation().getFileName());
        
        try {
            // Delegate to existing bean creation analyzer
            BeanCreationResult beanResult = beanCreationAnalyzer.analyzeBeanCreation(jarContent);
            
            // Extract service-specific information
            List<ServiceComponentInfo> serviceComponents = transformServiceComponents(beanResult.getServiceBeans());
            ServiceLayerMetrics metrics = calculateServiceMetrics(beanResult);
            List<ServiceLayerIssue> issues = analyzeServiceIssues(serviceComponents, beanResult);
            ServiceLayerSummary summary = buildServiceSummary(serviceComponents, metrics, issues);
            
            LOGGER.info("Service layer analysis completed. Found " + serviceComponents.size() + 
                       " service components with " + issues.size() + " issues");
            
            return ServiceLayerAnalysisResult.builder()
                .serviceComponents(serviceComponents)
                .metrics(metrics)
                .issues(issues)
                .summary(summary)
                .build();
                
        } catch (Exception e) {
            LOGGER.severe("Error during service layer analysis: " + e.getMessage());
            throw new RuntimeException("Failed to analyze service layer", e);
        }
    }
    
    @Override
    public boolean canAnalyze(JarContent jarContent) {
        return beanCreationAnalyzer.canAnalyze(jarContent);
    }
    
    private List<ServiceComponentInfo> transformServiceComponents(List<BeanInfo> serviceBeans) {
        return serviceBeans.stream()
            .map(this::transformToServiceComponent)
            .collect(Collectors.toList());
    }
    
    private ServiceComponentInfo transformToServiceComponent(BeanInfo beanInfo) {
        return ServiceComponentInfo.builder()
            .className(beanInfo.getDeclaringClassName())
            .serviceName(extractServiceName(beanInfo))
            .scope(beanInfo.getScope() != null ? beanInfo.getScope().name() : "SINGLETON")
            .isLazy(beanInfo.isLazy())
            .dependencies(beanInfo.getDependencies() != null ? 
                         beanInfo.getDependencies().stream()
                             .map(dep -> dep.getType())
                             .collect(java.util.stream.Collectors.toList()) : new ArrayList<>())
            .profiles(beanInfo.getProfiles() != null ? 
                     new ArrayList<>(beanInfo.getProfiles()) : new ArrayList<>())
            .isTransactional(isTransactional(beanInfo))
            .build();
    }
    
    private String extractServiceName(BeanInfo beanInfo) {
        // Extract service name from bean name or class name
        String beanName = beanInfo.getBeanName();
        if (beanName != null && !beanName.isEmpty()) {
            return beanName;
        }
        
        // Derive from class name
        String className = beanInfo.getDeclaringClassName();
        if (className != null) {
            int lastDot = className.lastIndexOf('.');
            String simpleName = lastDot >= 0 ? className.substring(lastDot + 1) : className;
            
            // Convert to camelCase service name
            if (simpleName.endsWith("Service")) {
                simpleName = simpleName.substring(0, simpleName.length() - 7);
            }
            return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1) + "Service";
        }
        
        return "unknownService";
    }
    
    private boolean isTransactional(BeanInfo beanInfo) {
        // Check if the service has transactional annotations
        List<String> annotations = beanInfo.getAnnotations();
        return annotations != null && annotations.stream()
            .anyMatch(ann -> ann.contains("Transactional"));
    }
    
    
    private ServiceLayerMetrics calculateServiceMetrics(BeanCreationResult beanResult) {
        List<BeanInfo> serviceBeans = beanResult.getServiceBeans();
        
        int totalServices = serviceBeans.size();
        int transactionalServices = (int) serviceBeans.stream()
            .filter(this::isTransactional)
            .count();
        int lazyServices = (int) serviceBeans.stream()
            .filter(BeanInfo::isLazy)
            .count();
        int primaryServices = (int) serviceBeans.stream()
            .filter(BeanInfo::isPrimary)
            .count();
        // TODO: Add circular dependency detection
        int servicesWithCircularDeps = 0;
        
        // Calculate average dependencies per service
        double avgDependencies = serviceBeans.stream()
            .filter(bean -> bean.getDependencies() != null)
            .mapToInt(bean -> bean.getDependencies().size())
            .average()
            .orElse(0.0);
        
        // Identify most used service (simplistic - by name patterns)
        Map<String, Integer> serviceUsage = calculateServiceUsage(serviceBeans);
        String mostUsedService = serviceUsage.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");
        
        return ServiceLayerMetrics.builder()
            .totalServices(totalServices)
            .transactionalServices(transactionalServices)
            .lazyServices(lazyServices)
            .averageDependencies(avgDependencies)
            .maxDependencies(serviceBeans.stream()
                .filter(bean -> bean.getDependencies() != null)
                .mapToInt(bean -> bean.getDependencies().size())
                .max().orElse(0))
            .packagesWithServices(1) // Simplified
            .build();
    }
    
    private Map<String, Integer> calculateServiceUsage(List<BeanInfo> serviceBeans) {
        Map<String, Integer> usage = new HashMap<>();
        
        for (BeanInfo service : serviceBeans) {
            String serviceName = extractServiceName(service);
            usage.put(serviceName, 1); // Simplified - could analyze actual usage
        }
        
        return usage;
    }
    
    private List<ServiceLayerIssue> analyzeServiceIssues(List<ServiceComponentInfo> services, 
                                                        BeanCreationResult beanResult) {
        List<ServiceLayerIssue> issues = new ArrayList<>();
        
        // Check for services without @Transactional where expected
        for (ServiceComponentInfo service : services) {
            if (!service.isTransactional() && seemsToNeedTransaction(service)) {
                issues.add(ServiceLayerIssue.builder()
                    .type(ServiceLayerIssue.IssueType.MISSING_TRANSACTIONAL)
                    .severity(ServiceLayerIssue.Severity.WARNING)
                    .className(service.getClassName())
                    .description("Service appears to perform data operations but lacks @Transactional annotation")
                    .recommendation("Consider adding @Transactional annotation for proper transaction management")
                    .build());
            }
            
            // Check for too many dependencies (God service)
            if (service.getDependencies().size() > 10) {
                issues.add(ServiceLayerIssue.builder()
                    .type(ServiceLayerIssue.IssueType.TOO_MANY_DEPENDENCIES)
                    .severity(ServiceLayerIssue.Severity.WARNING)
                    .className(service.getClassName())
                    .description("Service has too many dependencies (" + service.getDependencies().size() + ")")
                    .recommendation("Consider breaking down the service into smaller, more focused services")
                    .build());
            }
            
            // Check for circular dependencies - simplified check
            // TODO: Add real circular dependency detection in future version
            
            // Check for services without interfaces
            if (!hasInterface(service)) {
                issues.add(ServiceLayerIssue.builder()
                    .type(ServiceLayerIssue.IssueType.SERVICE_WITHOUT_INTERFACE)
                    .severity(ServiceLayerIssue.Severity.INFO)
                    .className(service.getClassName())
                    .description("Service does not implement an interface")
                    .recommendation("Consider creating an interface for better testability and decoupling")
                    .build());
            }
        }
        
        // Check for inappropriate scope (simplified check)
        for (ServiceComponentInfo service : services) {
            if (!"singleton".equals(service.getScope()) && !"SINGLETON".equals(service.getScope())) {
                issues.add(ServiceLayerIssue.builder()
                    .type(ServiceLayerIssue.IssueType.INAPPROPRIATE_SCOPE)
                    .severity(ServiceLayerIssue.Severity.WARNING)
                    .className(service.getClassName())
                    .description("Service has non-singleton scope: " + service.getScope())
                    .recommendation("Consider using singleton scope for stateless services")
                    .build());
            }
        }
        
        return issues;
    }
    
    private boolean seemsToNeedTransaction(ServiceComponentInfo service) {
        // Heuristic: if service has repository dependencies, it likely needs transactions
        return service.getDependencies().stream()
            .anyMatch(dep -> dep.contains("Repository") || dep.contains("Dao"));
    }
    
    private boolean seemsToBeDataAccessOnly(ServiceComponentInfo service) {
        // Heuristic: if service has only repository dependencies and simple name pattern
        return service.getDependencies().stream()
            .allMatch(dep -> dep.contains("Repository") || dep.contains("Dao")) &&
            service.getDependencies().size() <= 2;
    }
    
    private boolean hasControllers(BeanCreationResult beanResult) {
        return !beanResult.getControllerBeans().isEmpty();
    }
    
    private ServiceLayerSummary buildServiceSummary(List<ServiceComponentInfo> services, 
                                                  ServiceLayerMetrics metrics, 
                                                  List<ServiceLayerIssue> issues) {
        
        // Count issues by severity
        Map<ServiceLayerIssue.Severity, Long> issueCounts = issues.stream()
            .collect(Collectors.groupingBy(
                ServiceLayerIssue::getSeverity,
                Collectors.counting()
            ));
        
        String qualityRating = calculateQualityRating(services, issues);
        boolean hasGoodServiceDesign = evaluateServiceDesign(services, issues);
        
        return ServiceLayerSummary.builder()
            .totalServices(services.size())
            .totalPackages(1) // Simplified
            .totalIssues(issues.size())
            .qualityRating(qualityRating)
            .hasGoodArchitecture(hasGoodServiceDesign)
            .build();
    }
    
    private String calculateQualityRating(List<ServiceComponentInfo> services, List<ServiceLayerIssue> issues) {
        if (services.isEmpty()) {
            return "N/A - No Services";
        }
        
        double issueRatio = (double) issues.size() / services.size();
        long errorCount = issues.stream().filter(i -> i.getSeverity() == ServiceLayerIssue.Severity.ERROR).count();
        
        if (errorCount > 0) {
            return "Poor - Has Critical Issues";
        } else if (issueRatio <= 0.1) {
            return "Excellent";
        } else if (issueRatio <= 0.3) {
            return "Good";
        } else if (issueRatio <= 0.5) {
            return "Fair";
        } else {
            return "Poor";
        }
    }
    
    private boolean evaluateServiceDesign(List<ServiceComponentInfo> services, List<ServiceLayerIssue> issues) {
        if (services.isEmpty()) {
            return false;
        }
        
        // Good design criteria:
        // - Most services are transactional
        // - Average dependencies per service is reasonable
        
        // TODO: Add circular dependency detection
        boolean hasCircularDeps = false;
        double transactionalRatio = (double) services.stream().mapToInt(s -> s.isTransactional() ? 1 : 0).sum() / services.size();
        double avgDeps = services.stream().mapToDouble(s -> s.getDependencies().size()).average().orElse(0.0);
        
        return !hasCircularDeps && transactionalRatio >= 0.7 && avgDeps <= 5.0;
    }
    
    private boolean hasInterface(ServiceComponentInfo service) {
        // Simplified - in real implementation would check if class implements interfaces
        return true;
    }
}