package it.denzosoft.jreverse.analyzer.callgraph;

import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.*;
import it.denzosoft.jreverse.core.port.CallGraphAnalyzer;
import javassist.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Javassist-based implementation of CallGraphAnalyzer.
 * Analyzes method call graphs, execution flows, and dependency chains.
 */
public class JavassistCallGraphAnalyzer implements CallGraphAnalyzer {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistCallGraphAnalyzer.class);
    
    private final Set<String> httpAnnotations;
    private final Set<String> serviceAnnotations;
    private final Set<String> repositoryAnnotations;
    
    public JavassistCallGraphAnalyzer() {
        this.httpAnnotations = Set.of(
            "RequestMapping", "GetMapping", "PostMapping", "PutMapping", 
            "DeleteMapping", "PatchMapping", "RestController", "Controller"
        );
        this.serviceAnnotations = Set.of(
            "Service", "Component", "Bean"
        );
        this.repositoryAnnotations = Set.of(
            "Repository", "JpaRepository", "CrudRepository"
        );
        
        LOGGER.info("CallGraphAnalyzer initialized with {} HTTP annotations, {} service annotations, {} repository annotations",
                   httpAnnotations.size(), serviceAnnotations.size(), repositoryAnnotations.size());
    }
    
    @Override
    public CallGraphAnalysisResult analyzeCallGraphs(JarContent jarContent) {
        if (jarContent == null) {
            throw new IllegalArgumentException("JarContent cannot be null");
        }

        long startTime = System.currentTimeMillis();
        LOGGER.startOperation("Call graph analysis");
        
        try {
            // Find entry points (HTTP endpoints)
            List<CallGraphNode> entryPoints = findEntryPoints(jarContent);
            LOGGER.info("Found {} entry points", entryPoints.size());
            
            // Build call chains from each entry point
            List<CallGraphChain> callChains = new ArrayList<>();
            Map<String, CallGraphNode> rootNodes = new HashMap<>();
            
            for (CallGraphNode entryPoint : entryPoints) {
                CallGraphChain chain = buildCallChain(entryPoint, jarContent);
                callChains.add(chain);
                rootNodes.put(entryPoint.getMethodName(), entryPoint);
            }
            
            // Analyze architectural metrics
            ArchitectureMetrics metrics = calculateArchitectureMetrics(callChains, jarContent);
            
            // Detect issues and hotspots
            List<CallGraphIssue> issues = detectCallGraphIssues(callChains);
            List<PerformanceHotspot> hotspots = identifyPerformanceHotspots(callChains);
            
            // Calculate component usage statistics
            Map<String, Integer> usageStats = calculateComponentUsage(callChains);
            
            // Build summary
            CallGraphSummary summary = CallGraphSummary.builder()
                .totalEndpoints(entryPoints.size())
                .totalCallChains(callChains.size())
                .averageDepth(calculateAverageChainDepth(callChains))
                .maxDepth(calculateMaxChainDepth(callChains))
                .totalIssues(issues.size())
                .criticalIssues((int) issues.stream().filter(i -> i.getSeverity() == CallGraphIssue.Severity.CRITICAL).count())
                .build();
            
            CallGraphAnalysisResult result = CallGraphAnalysisResult.builder()
                .callChains(callChains)
                .rootNodes(rootNodes)
                .summary(summary)
                .issues(issues)
                .componentUsageStats(usageStats)
                .hotspots(hotspots)
                .architectureMetrics(metrics)
                .build();
            
            LOGGER.info("Call graph analysis completed: {} chains, {} issues, {} hotspots", 
                       callChains.size(), issues.size(), hotspots.size());
            
            return result;
            
        } catch (Exception e) {
            LOGGER.error("Call graph analysis failed", e);
            
            // Return minimal result with error summary
            return CallGraphAnalysisResult.builder()
                .summary(CallGraphSummary.builder()
                    .totalEndpoints(0)
                    .totalCallChains(0)
                    .averageDepth(0.0)
                    .maxDepth(0)
                    .totalIssues(1)
                    .criticalIssues(1)
                    .build())
                .issues(List.of(CallGraphIssue.builder()
                    .type(CallGraphIssue.IssueType.ANALYSIS_ERROR)
                    .severity(CallGraphIssue.Severity.CRITICAL)
                    .description("Call graph analysis failed: " + e.getMessage())
                    .location("Analysis Engine")
                    .build()))
                .build();
                
        } finally {
            LOGGER.endOperation("Call graph analysis", System.currentTimeMillis() - startTime);
        }
    }
    
    @Override
    public boolean canAnalyze(JarContent jarContent) {
        // Check for Spring Boot or web application indicators
        return jarContent.getClasses().stream()
            .anyMatch(this::isHttpEndpoint);
    }
    
    private List<CallGraphNode> findEntryPoints(JarContent jarContent) {
        List<CallGraphNode> entryPoints = new ArrayList<>();
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            if (isController(classInfo)) {
                for (MethodInfo method : classInfo.getMethods()) {
                    if (isHttpEndpoint(method)) {
                        CallGraphNode node = createCallGraphNode(classInfo, method);
                        entryPoints.add(node);
                    }
                }
            }
        }
        
        return entryPoints;
    }
    
    private boolean isController(ClassInfo classInfo) {
        return classInfo.getAnnotations().stream()
            .anyMatch(annotation -> httpAnnotations.contains(getSimpleAnnotationName(annotation.getType())));
    }
    
    private boolean isHttpEndpoint(ClassInfo classInfo) {
        return classInfo.getAnnotations().stream()
            .anyMatch(annotation -> httpAnnotations.contains(getSimpleAnnotationName(annotation.getType())));
    }
    
    private boolean isHttpEndpoint(MethodInfo method) {
        return method.getAnnotations().stream()
            .anyMatch(annotation -> httpAnnotations.contains(getSimpleAnnotationName(annotation.getType())));
    }
    
    private String getSimpleAnnotationName(String fullName) {
        int lastDot = fullName.lastIndexOf('.');
        return lastDot >= 0 ? fullName.substring(lastDot + 1) : fullName;
    }
    
    private CallGraphNode createCallGraphNode(ClassInfo classInfo, MethodInfo method) {
        return new CallGraphNode(
            classInfo.getFullyQualifiedName(),
            method.getName(),
            determineCallType(classInfo),
            0  // depth
        );
    }
    
    private CallType determineCallType(ClassInfo classInfo) {
        boolean hasServiceAnnotation = classInfo.getAnnotations().stream()
            .anyMatch(annotation -> serviceAnnotations.contains(getSimpleAnnotationName(annotation.getType())));
        
        boolean hasRepositoryAnnotation = classInfo.getAnnotations().stream()
            .anyMatch(annotation -> repositoryAnnotations.contains(getSimpleAnnotationName(annotation.getType())));
        
        if (hasRepositoryAnnotation) {
            return CallType.REPOSITORY_CALL;
        } else if (hasServiceAnnotation) {
            return CallType.SERVICE_CALL;
        } else if (isController(classInfo)) {
            return CallType.CONTROLLER;
        } else {
            return CallType.BUSINESS_LOGIC;
        }
    }
    
    private CallGraphChain buildCallChain(CallGraphNode entryPoint, JarContent jarContent) {
        String endpoint = extractEndpointPath(entryPoint);
        
        // Build call graph from entry point
        Set<CallGraphNode> visitedNodes = new HashSet<>();
        List<CallGraphNode> nodes = new ArrayList<>();
        
        traverseCallGraph(entryPoint, jarContent, visitedNodes, nodes);
        
        return CallGraphChain.builder()
            .endpoint(endpoint)
            .httpMethod(extractHttpMethod(entryPoint))
            .controllerClass(entryPoint.getClassName())
            .controllerMethod(entryPoint.getMethodName())
            .rootNode(entryPoint)
            .maxDepth(nodes.size())
            .totalCalls(nodes.size())
            .build();
    }
    
    private void traverseCallGraph(CallGraphNode currentNode, JarContent jarContent, 
                                  Set<CallGraphNode> visitedNodes, List<CallGraphNode> nodes) {
        
        if (visitedNodes.contains(currentNode)) {
            return; // Avoid cycles
        }
        
        visitedNodes.add(currentNode);
        nodes.add(currentNode);
        
        // Find method calls from current node
        ClassInfo classInfo = findClassInfo(currentNode.getClassName(), jarContent);
        if (classInfo != null) {
            MethodInfo methodInfo = findMethodInfo(currentNode.getMethodName(), classInfo);
            if (methodInfo != null) {
                List<CallGraphNode> calledNodes = findMethodCalls(methodInfo, jarContent);
                for (CallGraphNode calledNode : calledNodes) {
                    traverseCallGraph(calledNode, jarContent, visitedNodes, nodes);
                }
            }
        }
    }
    
    private ClassInfo findClassInfo(String className, JarContent jarContent) {
        return jarContent.getClasses().stream()
            .filter(c -> c.getFullyQualifiedName().equals(className))
            .findFirst()
            .orElse(null);
    }
    
    private MethodInfo findMethodInfo(String methodName, ClassInfo classInfo) {
        return classInfo.getMethods().stream()
            .filter(m -> m.getName().equals(methodName))
            .findFirst()
            .orElse(null);
    }
    
    private List<CallGraphNode> findMethodCalls(MethodInfo method, JarContent jarContent) {
        // Simplified method call detection - would need bytecode analysis for full implementation
        List<CallGraphNode> calls = new ArrayList<>();
        
        // Look for field injections and typical Spring patterns
        ClassInfo containingClass = findClassContainingMethod(method, jarContent);
        if (containingClass != null) {
            for (FieldInfo field : containingClass.getFields()) {
                if (isServiceField(field)) {
                    ClassInfo serviceClass = findClassInfo(field.getType(), jarContent);
                    if (serviceClass != null) {
                        // Add typical service method calls
                        for (MethodInfo serviceMethod : serviceClass.getMethods()) {
                            if (serviceMethod.isPublic() && !serviceMethod.getName().startsWith("get")) {
                                CallGraphNode node = createCallGraphNode(serviceClass, serviceMethod);
                                calls.add(node);
                            }
                        }
                    }
                }
            }
        }
        
        return calls;
    }
    
    private ClassInfo findClassContainingMethod(MethodInfo method, JarContent jarContent) {
        return jarContent.getClasses().stream()
            .filter(c -> c.getMethods().contains(method))
            .findFirst()
            .orElse(null);
    }
    
    private boolean isServiceField(FieldInfo field) {
        return field.getAnnotations().stream()
            .anyMatch(annotation -> "Autowired".equals(getSimpleAnnotationName(annotation.getType())) ||
                                   "Inject".equals(getSimpleAnnotationName(annotation.getType())));
    }
    
    private String extractEndpointPath(CallGraphNode entryPoint) {
        // Simplified endpoint extraction
        return "/" + entryPoint.getMethodName().toLowerCase();
    }
    
    private String extractHttpMethod(CallGraphNode entryPoint) {
        // Default to GET for simplification
        return "GET";
    }
    
    // Removed - complexity is calculated in CallGraphChain itself
    
    private ArchitectureMetrics calculateArchitectureMetrics(List<CallGraphChain> callChains, JarContent jarContent) {
        int totalLayers = calculateTotalLayers(callChains);
        double layerCoupling = calculateLayerCoupling(callChains);
        double serviceCohesion = calculateServiceCohesion(jarContent);
        
        return ArchitectureMetrics.builder()
            .couplingScore(layerCoupling)
            .cohesionScore(serviceCohesion)
            .build();
    }
    
    private int calculateTotalLayers(List<CallGraphChain> callChains) {
        Set<CallType> layersFound = new HashSet<>();
        for (CallGraphChain chain : callChains) {
            collectCallTypes(chain.getRootNode(), layersFound);
        }
        return layersFound.size();
    }
    
    private void collectCallTypes(CallGraphNode node, Set<CallType> types) {
        if (node != null) {
            types.add(node.getCallType());
            for (CallGraphNode child : node.getChildren()) {
                collectCallTypes(child, types);
            }
        }
    }
    
    private double calculateLayerCoupling(List<CallGraphChain> callChains) {
        // Simplified coupling calculation
        return callChains.stream()
            .mapToDouble(CallGraphChain::getTotalCalls)
            .average()
            .orElse(0.0) / 10.0; // Normalize to 0-1 scale
    }
    
    private double calculateServiceCohesion(JarContent jarContent) {
        // Simplified cohesion calculation
        long serviceClasses = jarContent.getClasses().stream()
            .filter(this::isServiceClass)
            .count();
        
        return serviceClasses > 0 ? Math.min(1.0, serviceClasses / 10.0) : 0.0;
    }
    
    private boolean isServiceClass(ClassInfo classInfo) {
        return classInfo.getAnnotations().stream()
            .anyMatch(annotation -> serviceAnnotations.contains(getSimpleAnnotationName(annotation.getType())));
    }
    
    private List<CallGraphIssue> detectCallGraphIssues(List<CallGraphChain> callChains) {
        List<CallGraphIssue> issues = new ArrayList<>();
        
        // Detect deep call chains
        for (CallGraphChain chain : callChains) {
            if (chain.getMaxDepth() > 10) {
                issues.add(CallGraphIssue.builder()
                    .type(CallGraphIssue.IssueType.DEEP_CALL_CHAIN)
                    .severity(CallGraphIssue.Severity.HIGH)
                    .description("Call chain depth exceeds recommended limit: " + chain.getMaxDepth())
                    .location(chain.getEndpoint())
                    .build());
            }
        }
        
        // Detect circular dependencies
        issues.addAll(detectCircularDependencies(callChains));
        
        return issues;
    }
    
    private List<CallGraphIssue> detectCircularDependencies(List<CallGraphChain> callChains) {
        List<CallGraphIssue> issues = new ArrayList<>();
        
        // Simplified circular dependency detection
        for (CallGraphChain chain : callChains) {
            Set<String> classNames = collectClassNames(chain.getRootNode());
            int totalNodes = countNodes(chain.getRootNode());
            
            if (classNames.size() < totalNodes) {
                issues.add(CallGraphIssue.builder()
                    .type(CallGraphIssue.IssueType.CIRCULAR_DEPENDENCY)
                    .severity(CallGraphIssue.Severity.WARNING)
                    .description("Potential circular dependency detected in call chain")
                    .location(chain.getEndpoint())
                    .build());
            }
        }
        
        return issues;
    }
    
    private List<PerformanceHotspot> identifyPerformanceHotspots(List<CallGraphChain> callChains) {
        List<PerformanceHotspot> hotspots = new ArrayList<>();
        
        // Identify chains with high complexity
        for (CallGraphChain chain : callChains) {
            if (chain.getComplexityLevel() == CallGraphChain.ComplexityLevel.VERY_COMPLEX) {
                hotspots.add(PerformanceHotspot.builder()
                    .componentName(chain.getEndpoint())
                    .description("Complex call chain with " + chain.getMaxDepth() + " method calls")
                    .performanceImpact(calculatePerformanceImpact(chain))
                    .riskLevel(PerformanceHotspot.RiskLevel.HIGH)
                    .build());
            }
        }
        
        return hotspots;
    }
    
    private Map<String, Integer> calculateComponentUsage(List<CallGraphChain> callChains) {
        Map<String, Integer> usageStats = new HashMap<>();
        
        for (CallGraphChain chain : callChains) {
            collectUsageStats(chain.getRootNode(), usageStats);
        }
        
        return usageStats;
    }
    
    private double calculateAverageChainDepth(List<CallGraphChain> callChains) {
        return callChains.stream()
            .mapToInt(CallGraphChain::getMaxDepth)
            .average()
            .orElse(0.0);
    }
    
    private int calculateMaxChainDepth(List<CallGraphChain> callChains) {
        return callChains.stream()
            .mapToInt(CallGraphChain::getMaxDepth)
            .max()
            .orElse(0);
    }
    
    private double calculatePerformanceImpact(CallGraphChain chain) {
        return Math.min(1.0, chain.getMaxDepth() / 15.0); // Normalize to 0-1
    }
    
    // Helper methods for node traversal
    
    private Set<String> collectClassNames(CallGraphNode node) {
        Set<String> classNames = new HashSet<>();
        if (node != null) {
            classNames.add(node.getClassName());
            for (CallGraphNode child : node.getChildren()) {
                classNames.addAll(collectClassNames(child));
            }
        }
        return classNames;
    }
    
    private int countNodes(CallGraphNode node) {
        if (node == null) return 0;
        
        int count = 1;
        for (CallGraphNode child : node.getChildren()) {
            count += countNodes(child);
        }
        return count;
    }
    
    private void collectUsageStats(CallGraphNode node, Map<String, Integer> usageStats) {
        if (node != null) {
            usageStats.merge(node.getClassName(), 1, Integer::sum);
            for (CallGraphNode child : node.getChildren()) {
                collectUsageStats(child, usageStats);
            }
        }
    }
}