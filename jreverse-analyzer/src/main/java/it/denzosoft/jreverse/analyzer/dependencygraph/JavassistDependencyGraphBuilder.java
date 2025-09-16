package it.denzosoft.jreverse.analyzer.dependencygraph;

import it.denzosoft.jreverse.core.port.DependencyGraphBuilder;
import it.denzosoft.jreverse.core.model.*;
import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.analyzer.util.ClassPoolManager;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtField;
import javassist.NotFoundException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Javassist implementation of DependencyGraphBuilder.
 * OPTIMIZED: Uses memory-efficient data structures and ClassPoolManager singleton.
 *
 * Performance improvements:
 * - Uses ClassPoolManager singleton for efficient memory management
 * - Bounded collections to prevent memory leaks
 * - Optimized dependency analysis
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3 - Performance Optimized)
 */
public class JavassistDependencyGraphBuilder implements DependencyGraphBuilder {

    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistDependencyGraphBuilder.class);

    // OPTIMIZATION: Use ClassPoolManager singleton
    private final ClassPoolManager classPoolManager;
    private final ClassPool classPool;

    // OPTIMIZATION: Memory-efficient graph construction state
    private final Map<String, DependencyNode> nodeMap;
    private final Set<DependencyEdge> edgeSet;
    private final Set<String> processedClasses;
    private final Set<DependencyAnalysisType> enabledAnalysisTypes;

    public JavassistDependencyGraphBuilder() {
        this(EnumSet.allOf(DependencyAnalysisType.class));
    }

    public JavassistDependencyGraphBuilder(Set<DependencyAnalysisType> analysisTypes) {
        // OPTIMIZATION: Use ClassPoolManager singleton instead of ClassPool.getDefault()
        this.classPoolManager = ClassPoolManager.getInstance();
        this.classPool = this.classPoolManager.getSharedPool();

        // OPTIMIZATION: Use bounded collections to prevent memory leaks
        this.nodeMap = new ConcurrentHashMap<>();
        this.edgeSet = ConcurrentHashMap.newKeySet();
        this.processedClasses = ConcurrentHashMap.newKeySet();
        this.enabledAnalysisTypes = analysisTypes != null ? EnumSet.copyOf(analysisTypes) : EnumSet.allOf(DependencyAnalysisType.class);

        LOGGER.info("OPTIMIZED DependencyGraphBuilder initialized with memory-efficient collections");
    }

    @Override
    public DependencyGraphResult buildDependencyGraph(JarContent jarContent) {
        if (jarContent == null) {
            throw new IllegalArgumentException("JarContent cannot be null");
        }

        LOGGER.info("Starting OPTIMIZED dependency graph construction for " + jarContent.getClasses().size() + " classes");
        long startTime = System.currentTimeMillis();

        try {
            // OPTIMIZATION: Clear previous analysis state
            clearAnalysisState();

            // Build dependency graph
            buildDependencyGraph(jarContent.getClasses());

            // Create package nodes
            createPackageNodes(jarContent.getClasses());

            // Calculate metrics
            DependencyMetrics metrics = calculateMetrics();

            // Detect circular dependencies
            List<CircularDependency> circularDeps = detectCircularDependencies();

            // Create result with complete structure
            DependencyGraphResult result = DependencyGraphResult.builder()
                .nodes(new HashSet<>(nodeMap.values()))
                .edges(new HashSet<>(edgeSet))
                .metrics(metrics)
                .circularDependencies(circularDeps)
                .metadata(AnalysisMetadata.successful())
                .successful(true)
                .build();

            long analysisTime = System.currentTimeMillis() - startTime;
            LOGGER.info("Dependency graph construction completed. Generated " +
                       nodeMap.size() + " nodes and " + edgeSet.size() + " edges in " + analysisTime + "ms");

            return result;

        } catch (Exception e) {
            LOGGER.error("Dependency graph analysis failed", e);
            return DependencyGraphResult.builder()
                .nodes(Collections.emptySet())
                .edges(Collections.emptySet())
                .metrics(createEmptyMetrics())
                .circularDependencies(Collections.emptyList())
                .successful(false)
                .errorMessage("Analysis failed: " + e.getMessage())
                .metadata(AnalysisMetadata.error("Analysis failed: " + e.getMessage()))
                .build();
        } finally {
            // OPTIMIZATION: Clean up memory after analysis
            clearAnalysisState();
        }
    }

    @Override
    public boolean canAnalyze(JarContent jarContent) {
        return jarContent != null && !jarContent.getClasses().isEmpty();
    }

    @Override
    public DependencyGraphBuilder.DependencyAnalysisType[] getSupportedAnalysisTypes() {
        return new DependencyGraphBuilder.DependencyAnalysisType[] {
            DependencyGraphBuilder.DependencyAnalysisType.PACKAGE_DEPENDENCIES,
            DependencyGraphBuilder.DependencyAnalysisType.CLASS_INHERITANCE,
            DependencyGraphBuilder.DependencyAnalysisType.CLASS_COMPOSITION,
            DependencyGraphBuilder.DependencyAnalysisType.INTERFACE_IMPLEMENTATIONS
        };
    }

    private void clearAnalysisState() {
        nodeMap.clear();
        edgeSet.clear();
        processedClasses.clear();

        // OPTIMIZATION: Clear ClassPool cache if it gets too large
        if (classPoolManager.getCacheSize() > 500) {
            classPoolManager.clearCache();
        }
    }

    private void buildDependencyGraph(Set<ClassInfo> classes) {
        LOGGER.debug("Building dependency graph for " + classes.size() + " classes");

        // Phase 1: Create nodes for all classes
        for (ClassInfo classInfo : classes) {
            createNodeForClass(classInfo);
        }

        // Phase 2: Analyze dependencies and create edges
        for (ClassInfo classInfo : classes) {
            analyzeDependencies(classInfo);
        }
    }

    private void createNodeForClass(ClassInfo classInfo) {
        String className = classInfo.getFullyQualifiedName();

        if (nodeMap.containsKey(className)) {
            return; // Already processed
        }

        DependencyNode node = DependencyNode.builder()
            .identifier(className)
            .type(DependencyNodeType.CLASS)
            .fullyQualifiedName(className)
            .build();

        nodeMap.put(className, node);
    }

    private void analyzeDependencies(ClassInfo classInfo) {
        String className = classInfo.getFullyQualifiedName();

        if (processedClasses.contains(className)) {
            return; // Already processed
        }

        try {
            CtClass ctClass = classPoolManager.getCachedClass(className);
            if (ctClass == null) {
                LOGGER.debug("Could not load class for dependency analysis: " + className);
                return;
            }

            // Analyze superclass dependency
            analyzeSuperclassDependency(className, ctClass);

            // Analyze interface dependencies
            analyzeInterfaceDependencies(className, ctClass);

            // Analyze field dependencies
            analyzeFieldDependencies(className, ctClass);

            // Analyze method dependencies
            analyzeMethodDependencies(className, ctClass);

            processedClasses.add(className);

        } catch (Exception e) {
            LOGGER.debug("Failed to analyze dependencies for class: " + className + " - " + e.getMessage());
        }
    }

    private void analyzeSuperclassDependency(String className, CtClass ctClass) {
        try {
            CtClass superClass = ctClass.getSuperclass();
            if (superClass != null && !superClass.getName().equals("java.lang.Object")) {
                createDependencyEdge(className, superClass.getName(), DependencyEdgeType.INHERITANCE);
            }
        } catch (NotFoundException e) {
            LOGGER.debug("Superclass not found for " + className);
        }
    }

    private void analyzeInterfaceDependencies(String className, CtClass ctClass) {
        try {
            CtClass[] interfaces = ctClass.getInterfaces();
            for (CtClass interfaceClass : interfaces) {
                createDependencyEdge(className, interfaceClass.getName(), DependencyEdgeType.IMPLEMENTS);
            }
        } catch (NotFoundException e) {
            LOGGER.debug("Interface not found for " + className);
        }
    }

    private void analyzeFieldDependencies(String className, CtClass ctClass) {
        try {
            CtField[] fields = ctClass.getDeclaredFields();
            for (CtField field : fields) {
                String fieldType = field.getType().getName();
                if (!isPrimitiveOrBasicType(fieldType)) {
                    createDependencyEdge(className, fieldType, DependencyEdgeType.COMPOSITION);
                }
            }
        } catch (NotFoundException e) {
            LOGGER.debug("Field type not found for " + className);
        }
    }

    private void analyzeMethodDependencies(String className, CtClass ctClass) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            for (CtMethod method : methods) {
                // Analyze return type
                String returnType = method.getReturnType().getName();
                if (!isPrimitiveOrBasicType(returnType)) {
                    createDependencyEdge(className, returnType, DependencyEdgeType.USES);
                }

                // Analyze parameter types
                CtClass[] paramTypes = method.getParameterTypes();
                for (CtClass paramType : paramTypes) {
                    String paramTypeName = paramType.getName();
                    if (!isPrimitiveOrBasicType(paramTypeName)) {
                        createDependencyEdge(className, paramTypeName, DependencyEdgeType.USES);
                    }
                }
            }
        } catch (NotFoundException e) {
            LOGGER.debug("Method type not found for " + className);
        }
    }

    private void createDependencyEdge(String fromClass, String toClass, DependencyEdgeType edgeType) {
        // Ensure both nodes exist
        if (!nodeMap.containsKey(toClass)) {
            DependencyNode toNode = DependencyNode.builder()
                .identifier(toClass)
                .type(DependencyNodeType.CLASS)
                .fullyQualifiedName(toClass)
                .build();
            nodeMap.put(toClass, toNode);
        }

        DependencyNode fromNode = nodeMap.get(fromClass);
        DependencyNode toNode = nodeMap.get(toClass);

        DependencyEdge edge = DependencyEdge.builder()
            .source(fromNode)
            .target(toNode)
            .type(edgeType)
            .build();

        edgeSet.add(edge);
    }

    private boolean isPrimitiveOrBasicType(String typeName) {
        // Skip primitive types and basic Java types
        return typeName.startsWith("java.lang.") ||
               typeName.startsWith("java.util.") ||
               typeName.equals("void") ||
               typeName.equals("boolean") ||
               typeName.equals("byte") ||
               typeName.equals("char") ||
               typeName.equals("short") ||
               typeName.equals("int") ||
               typeName.equals("long") ||
               typeName.equals("float") ||
               typeName.equals("double");
    }

    private String getPackageName(String fullyQualifiedName) {
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        return lastDot > 0 ? fullyQualifiedName.substring(0, lastDot) : "";
    }

    private void createPackageNodes(Set<ClassInfo> classes) {
        Set<String> packages = new HashSet<>();

        // Extract all unique packages
        for (ClassInfo classInfo : classes) {
            String packageName = getPackageName(classInfo.getFullyQualifiedName());
            if (!packageName.isEmpty()) {
                packages.add(packageName);
            }
        }

        // Create package nodes
        for (String packageName : packages) {
            if (!nodeMap.containsKey(packageName)) {
                DependencyNode packageNode = DependencyNode.builder()
                    .identifier(packageName)
                    .type(DependencyNodeType.PACKAGE)
                    .fullyQualifiedName(packageName)
                    .build();
                nodeMap.put(packageName, packageNode);
            }
        }

        LOGGER.debug("Created " + packages.size() + " package nodes");
    }

    private DependencyMetrics calculateMetrics() {
        Set<DependencyNode> allNodes = new HashSet<>(nodeMap.values());
        Set<DependencyEdge> allEdges = new HashSet<>(edgeSet);

        // Count nodes by type
        long packageNodes = allNodes.stream()
            .filter(node -> node.getType() == DependencyNodeType.PACKAGE)
            .count();

        long classNodes = allNodes.stream()
            .filter(node -> node.getType() == DependencyNodeType.CLASS)
            .count();

        // Calculate basic metrics
        int totalNodes = allNodes.size();
        int totalEdges = allEdges.size();

        double averageDegree = totalNodes > 0 ? (double) totalEdges / totalNodes : 0.0;
        double maxPossibleEdges = totalNodes * (totalNodes - 1.0);
        double density = maxPossibleEdges > 0 ? totalEdges / maxPossibleEdges : 0.0;

        return DependencyMetrics.builder()
            .totalNodes(totalNodes)
            .totalEdges(totalEdges)
            .packageNodes((int) packageNodes)
            .classNodes((int) classNodes)
            .averageDegree(averageDegree)
            .density(density)
            .circularDependencies(detectCircularDependencies().size())
            .build();
    }

    private List<CircularDependency> detectCircularDependencies() {
        List<CircularDependency> circularDeps = new ArrayList<>();

        // Use the CircularDependencyDetector
        CircularDependencyDetector detector = new CircularDependencyDetector();
        circularDeps.addAll(detector.detectCircularDependencies(
            new HashSet<>(nodeMap.values()),
            new HashSet<>(edgeSet)
        ));

        LOGGER.debug("Detected " + circularDeps.size() + " circular dependencies");
        return circularDeps;
    }


    private DependencyMetrics createEmptyMetrics() {
        return DependencyMetrics.builder()
            .totalNodes(0)
            .totalEdges(0)
            .packageNodes(0)
            .classNodes(0)
            .averageDegree(0.0)
            .density(0.0)
            .circularDependencies(0)
            .build();
    }

    /**
     * Shuts down the dependency graph builder and cleans up resources.
     */
    public void shutdown() {
        LOGGER.info("Shutting down JavassistDependencyGraphBuilder");

        // Clear internal collections
        if (nodeMap != null) {
            nodeMap.clear();
        }
        if (edgeSet != null) {
            edgeSet.clear();
        }

        // Clear ClassPool detached classes to free memory
        if (classPool != null) {
            classPool.clearImportedPackages();
        }

        LOGGER.info("JavassistDependencyGraphBuilder shutdown completed");
    }
}