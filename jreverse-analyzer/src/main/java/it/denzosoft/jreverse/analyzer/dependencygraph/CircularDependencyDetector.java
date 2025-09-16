package it.denzosoft.jreverse.analyzer.dependencygraph;

import it.denzosoft.jreverse.core.model.CircularDependency;
import it.denzosoft.jreverse.core.model.DependencyNode;
import it.denzosoft.jreverse.core.model.DependencyEdge;
import it.denzosoft.jreverse.core.model.DependencyNodeType;
import it.denzosoft.jreverse.core.logging.JReverseLogger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Detects circular dependencies in dependency graphs using various algorithms.
 * Supports detection at package, class, and method levels with severity assessment.
 * 
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public class CircularDependencyDetector {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(CircularDependencyDetector.class);
    
    // Analysis configuration
    private static final int MAX_CYCLE_LENGTH = 50; // Prevent infinite loops
    private static final int MAX_CYCLES_TO_REPORT = 100; // Limit output size
    
    /**
     * Detects all circular dependencies in the given dependency graph.
     * 
     * @param nodes all nodes in the dependency graph
     * @param edges all edges in the dependency graph
     * @return list of detected circular dependencies
     */
    public List<CircularDependency> detectCircularDependencies(Set<DependencyNode> nodes, Set<DependencyEdge> edges) {
        LOGGER.startOperation("Circular dependency detection");
        long startTime = System.currentTimeMillis();
        
        try {
            List<CircularDependency> allCycles = new ArrayList<>();
            
            // Build adjacency map for efficient graph traversal
            Map<DependencyNode, Set<DependencyNode>> adjacencyMap = buildAdjacencyMap(nodes, edges);
            
            // Detect cycles at different levels
            allCycles.addAll(detectPackageLevelCycles(adjacencyMap));
            allCycles.addAll(detectClassLevelCycles(adjacencyMap));
            allCycles.addAll(detectMethodLevelCycles(adjacencyMap));
            
            // Remove duplicates and limit results
            List<CircularDependency> uniqueCycles = removeDuplicateCycles(allCycles);
            List<CircularDependency> limitedResults = limitResults(uniqueCycles);
            
            long analysisTime = System.currentTimeMillis() - startTime;
            LOGGER.info(String.format("Detected %d circular dependencies in %dms", 
                limitedResults.size(), analysisTime));
                
            return limitedResults;
            
        } catch (Exception e) {
            LOGGER.error("Circular dependency detection failed", e);
            return Collections.emptyList();
            
        } finally {
            LOGGER.endOperation("Circular dependency detection", System.currentTimeMillis() - startTime);
        }
    }
    
    /**
     * Builds an adjacency map for efficient graph traversal.
     */
    private Map<DependencyNode, Set<DependencyNode>> buildAdjacencyMap(Set<DependencyNode> nodes, Set<DependencyEdge> edges) {
        Map<DependencyNode, Set<DependencyNode>> adjacencyMap = new HashMap<>();
        
        // Initialize with empty sets for all nodes
        for (DependencyNode node : nodes) {
            adjacencyMap.put(node, new HashSet<>());
        }
        
        // Populate adjacency relationships
        for (DependencyEdge edge : edges) {
            DependencyNode source = edge.getSource();
            DependencyNode target = edge.getTarget();
            
            adjacencyMap.computeIfAbsent(source, k -> new HashSet<>()).add(target);
        }
        
        return adjacencyMap;
    }
    
    /**
     * Detects circular dependencies at the package level.
     */
    private List<CircularDependency> detectPackageLevelCycles(Map<DependencyNode, Set<DependencyNode>> adjacencyMap) {
        LOGGER.debug("Detecting package-level circular dependencies");
        
        Set<DependencyNode> packageNodes = adjacencyMap.keySet().stream()
            .filter(node -> node.getType() == DependencyNodeType.PACKAGE)
            .collect(Collectors.toSet());
        
        List<List<DependencyNode>> cycles = detectCyclesInSubgraph(adjacencyMap, packageNodes);
        
        return cycles.stream()
            .map(cycle -> CircularDependency.packageCycle(cycle))
            .collect(Collectors.toList());
    }
    
    /**
     * Detects circular dependencies at the class level.
     */
    private List<CircularDependency> detectClassLevelCycles(Map<DependencyNode, Set<DependencyNode>> adjacencyMap) {
        LOGGER.debug("Detecting class-level circular dependencies");
        
        Set<DependencyNode> classNodes = adjacencyMap.keySet().stream()
            .filter(node -> node.getType() == DependencyNodeType.CLASS)
            .collect(Collectors.toSet());
        
        List<List<DependencyNode>> cycles = detectCyclesInSubgraph(adjacencyMap, classNodes);
        
        return cycles.stream()
            .map(cycle -> CircularDependency.classCycle(cycle))
            .collect(Collectors.toList());
    }
    
    /**
     * Detects circular dependencies at the method level.
     */
    private List<CircularDependency> detectMethodLevelCycles(Map<DependencyNode, Set<DependencyNode>> adjacencyMap) {
        LOGGER.debug("Detecting method-level circular dependencies");
        
        Set<DependencyNode> methodNodes = adjacencyMap.keySet().stream()
            .filter(node -> node.getType() == DependencyNodeType.METHOD)
            .collect(Collectors.toSet());
        
        List<List<DependencyNode>> cycles = detectCyclesInSubgraph(adjacencyMap, methodNodes);
        
        // Method-level cycles are usually less severe
        return cycles.stream()
            .map(cycle -> CircularDependency.builder()
                .cyclePath(cycle)
                .severity(CircularDependency.CircularDependencySeverity.LOW)
                .description("Method-level circular dependency detected")
                .suggestions(List.of(
                    "Refactor methods to eliminate mutual recursion",
                    "Extract common logic to utility methods"
                ))
                .build())
            .collect(Collectors.toList());
    }
    
    /**
     * Detects cycles in a subgraph using Tarjan's strongly connected components algorithm.
     */
    private List<List<DependencyNode>> detectCyclesInSubgraph(Map<DependencyNode, Set<DependencyNode>> adjacencyMap,
                                                             Set<DependencyNode> subgraphNodes) {
        List<List<DependencyNode>> cycles = new ArrayList<>();
        
        // Filter adjacency map to include only subgraph nodes
        Map<DependencyNode, Set<DependencyNode>> subgraphMap = new HashMap<>();
        for (DependencyNode node : subgraphNodes) {
            Set<DependencyNode> targets = adjacencyMap.getOrDefault(node, Collections.emptySet())
                .stream()
                .filter(subgraphNodes::contains)
                .collect(Collectors.toSet());
            subgraphMap.put(node, targets);
        }
        
        // Use Tarjan's algorithm to find strongly connected components
        TarjanSCCAlgorithm tarjan = new TarjanSCCAlgorithm(subgraphMap);
        List<List<DependencyNode>> stronglyConnectedComponents = tarjan.findSCCs();
        
        // Filter out single-node SCCs (not cycles unless self-referencing)
        for (List<DependencyNode> scc : stronglyConnectedComponents) {
            if (scc.size() > 1) {
                cycles.add(scc);
            } else if (scc.size() == 1) {
                // Check for self-reference
                DependencyNode node = scc.get(0);
                if (subgraphMap.get(node).contains(node)) {
                    cycles.add(scc);
                }
            }
        }
        
        return cycles;
    }
    
    /**
     * Removes duplicate cycles by comparing cycle paths.
     */
    private List<CircularDependency> removeDuplicateCycles(List<CircularDependency> cycles) {
        Set<Set<String>> seenCycles = new HashSet<>();
        List<CircularDependency> uniqueCycles = new ArrayList<>();
        
        for (CircularDependency cycle : cycles) {
            Set<String> cycleSignature = cycle.getCyclePath().stream()
                .map(node -> node.getIdentifier())
                .collect(Collectors.toSet());
            
            if (!seenCycles.contains(cycleSignature)) {
                seenCycles.add(cycleSignature);
                uniqueCycles.add(cycle);
            }
        }
        
        return uniqueCycles;
    }
    
    /**
     * Limits the number of results to prevent overwhelming output.
     */
    private List<CircularDependency> limitResults(List<CircularDependency> cycles) {
        if (cycles.size() <= MAX_CYCLES_TO_REPORT) {
            return cycles;
        }
        
        // Sort by severity (highest first) and take top results
        return cycles.stream()
            .sorted((c1, c2) -> c2.getSeverity().compareTo(c1.getSeverity()))
            .limit(MAX_CYCLES_TO_REPORT)
            .collect(Collectors.toList());
    }
    
    /**
     * Inner class implementing Tarjan's strongly connected components algorithm.
     */
    private static class TarjanSCCAlgorithm {
        private final Map<DependencyNode, Set<DependencyNode>> graph;
        private final Map<DependencyNode, Integer> indices;
        private final Map<DependencyNode, Integer> lowLinks;
        private final Set<DependencyNode> onStack;
        private final Deque<DependencyNode> stack;
        private final List<List<DependencyNode>> stronglyConnectedComponents;
        private int index;
        
        public TarjanSCCAlgorithm(Map<DependencyNode, Set<DependencyNode>> graph) {
            this.graph = graph;
            this.indices = new HashMap<>();
            this.lowLinks = new HashMap<>();
            this.onStack = new HashSet<>();
            this.stack = new ArrayDeque<>();
            this.stronglyConnectedComponents = new ArrayList<>();
            this.index = 0;
        }
        
        public List<List<DependencyNode>> findSCCs() {
            for (DependencyNode node : graph.keySet()) {
                if (!indices.containsKey(node)) {
                    strongConnect(node);
                }
            }
            return stronglyConnectedComponents;
        }
        
        private void strongConnect(DependencyNode node) {
            // Set the depth index for node to the smallest unused index
            indices.put(node, index);
            lowLinks.put(node, index);
            index++;
            
            stack.push(node);
            onStack.add(node);
            
            // Consider successors of node
            for (DependencyNode successor : graph.getOrDefault(node, Collections.emptySet())) {
                if (!indices.containsKey(successor)) {
                    // Successor has not yet been visited; recurse on it
                    strongConnect(successor);
                    lowLinks.put(node, Math.min(lowLinks.get(node), lowLinks.get(successor)));
                } else if (onStack.contains(successor)) {
                    // Successor is in stack and hence in the current SCC
                    lowLinks.put(node, Math.min(lowLinks.get(node), indices.get(successor)));
                }
            }
            
            // If node is a root node, pop the stack and create an SCC
            if (lowLinks.get(node).equals(indices.get(node))) {
                List<DependencyNode> component = new ArrayList<>();
                DependencyNode w;
                do {
                    w = stack.pop();
                    onStack.remove(w);
                    component.add(w);
                } while (!w.equals(node));
                
                stronglyConnectedComponents.add(component);
            }
        }
    }
}