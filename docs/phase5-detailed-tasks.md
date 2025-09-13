# Fase 5: Code Quality & Performance Metrics - Task Dettagliati

## T5.1.1: CyclomaticComplexityCalculator per Methods

### Descrizione Dettagliata
Implementazione di un calcolatore di complessità ciclomatica avanzato che analizza il bytecode dei metodi per determinare la complessità attraverso il numero di percorsi di esecuzione indipendenti, fornendo metriche dettagliate per valutare la maintainability del codice.

### Scopo dell'Attività  
- Calcolare complessità ciclomatica per ogni metodo dell'applicazione
- Identificare metodi con complessità eccessiva (hotspot di manutenzione)
- Fornire analisi dettagliata dei control flow paths
- Generare recommendations per semplificazione del codice
- Supportare diverse varianti di complessità (McCabe, modificata, cognitiva)

### Impatti su Altri Moduli
- **CodeSizeAnalyzer**: Correlazione con lunghezza metodi
- **LongMethodDetector**: Input per identificazione metodi problematici  
- **RefactoringAnalyzer**: Prioritization per refactoring efforts
- **QualityMetricsAggregator**: Componente chiave per quality scoring

### Componenti da Implementare

#### 1. Cyclomatic Complexity Calculator Core
```java
public interface CyclomaticComplexityCalculator {
    ComplexityAnalysisResult calculateComplexity(JarContent jarContent);
    MethodComplexity calculateMethodComplexity(MethodInfo methodInfo, CtMethod ctMethod);
}

public class JavassistCyclomaticComplexityCalculator implements CyclomaticComplexityCalculator {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistCyclomaticComplexityCalculator.class);
    
    // Complexity thresholds based on industry standards
    private static final int LOW_COMPLEXITY_THRESHOLD = 10;
    private static final int MODERATE_COMPLEXITY_THRESHOLD = 20;
    private static final int HIGH_COMPLEXITY_THRESHOLD = 40;
    
    private final ControlFlowAnalyzer controlFlowAnalyzer;
    private final CognitiveComplexityCalculator cognitiveCalculator;
    private final ComplexityRecommendationEngine recommendationEngine;
    
    public JavassistCyclomaticComplexityCalculator() {
        this.controlFlowAnalyzer = new ControlFlowAnalyzer();
        this.cognitiveCalculator = new CognitiveComplexityCalculator();
        this.recommendationEngine = new ComplexityRecommendationEngine();
    }
    
    @Override
    public ComplexityAnalysisResult calculateComplexity(JarContent jarContent) {
        LOGGER.startOperation("Cyclomatic complexity analysis");
        long startTime = System.currentTimeMillis();
        
        try {
            ComplexityAnalysisResult.Builder resultBuilder = ComplexityAnalysisResult.builder();
            
            List<MethodComplexity> methodComplexities = new ArrayList<>();
            Map<String, ClassComplexity> classComplexities = new HashMap<>();
            
            for (ClassInfo classInfo : jarContent.getClasses()) {
                ClassComplexity.Builder classComplexityBuilder = ClassComplexity.builder()
                    .className(classInfo.getFullyQualifiedName());
                
                List<MethodComplexity> classMethodComplexities = new ArrayList<>();
                
                try {
                    ClassPool pool = ClassPool.getDefault();
                    CtClass ctClass = pool.get(classInfo.getFullyQualifiedName());
                    
                    for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
                        MethodInfo methodInfo = findMethodInfo(classInfo, ctMethod);
                        if (methodInfo != null) {
                            MethodComplexity complexity = calculateMethodComplexity(methodInfo, ctMethod);
                            classMethodComplexities.add(complexity);
                            methodComplexities.add(complexity);
                        }
                    }
                    
                } catch (Exception e) {
                    LOGGER.error("Error analyzing class {}", classInfo.getFullyQualifiedName(), e);
                    continue;
                }
                
                // Calcola metriche aggregate per la classe
                double averageComplexity = classMethodComplexities.stream()
                    .mapToInt(MethodComplexity::getCyclomaticComplexity)
                    .average()
                    .orElse(0.0);
                
                int maxComplexity = classMethodComplexities.stream()
                    .mapToInt(MethodComplexity::getCyclomaticComplexity)
                    .max()
                    .orElse(0);
                
                ClassComplexity classComplexity = classComplexityBuilder
                    .methodComplexities(classMethodComplexities)
                    .averageComplexity(averageComplexity)
                    .maximumComplexity(maxComplexity)
                    .totalComplexity(classMethodComplexities.stream()
                        .mapToInt(MethodComplexity::getCyclomaticComplexity)
                        .sum())
                    .build();
                
                classComplexities.put(classInfo.getFullyQualifiedName(), classComplexity);
            }
            
            resultBuilder.methodComplexities(methodComplexities);
            resultBuilder.classComplexities(classComplexities);
            
            // Calcola statistiche aggregate
            ComplexityStatistics statistics = calculateComplexityStatistics(methodComplexities);
            resultBuilder.statistics(statistics);
            
            // Identifica hotspot di complessità
            List<ComplexityHotspot> hotspots = identifyComplexityHotspots(methodComplexities, classComplexities);
            resultBuilder.complexityHotspots(hotspots);
            
            // Genera raccomandazioni
            List<ComplexityRecommendation> recommendations = recommendationEngine.generateRecommendations(
                methodComplexities, classComplexities, hotspots);
            resultBuilder.recommendations(recommendations);
            
            LOGGER.info("Analyzed {} methods across {} classes", 
                methodComplexities.size(), classComplexities.size());
            
            return resultBuilder
                .analysisMetadata(AnalysisMetadata.successful(System.currentTimeMillis() - startTime))
                .build();
                
        } catch (Exception e) {
            LOGGER.error("Complexity analysis failed", e);
            return ComplexityAnalysisResult.failed("Analysis failed: " + e.getMessage());
        } finally {
            LOGGER.endOperation("Cyclomatic complexity analysis", System.currentTimeMillis() - startTime);
        }
    }
    
    @Override
    public MethodComplexity calculateMethodComplexity(MethodInfo methodInfo, CtMethod ctMethod) {
        try {
            MethodComplexity.Builder builder = MethodComplexity.builder()
                .className(methodInfo.getDeclaringClass().getFullyQualifiedName())
                .methodName(methodInfo.getName())
                .methodSignature(methodInfo.getSignature());
            
            // Calcola complessità ciclomatica McCabe
            int cyclomaticComplexity = calculateMcCabeComplexity(ctMethod);
            builder.cyclomaticComplexity(cyclomaticComplexity);
            
            // Calcola complessità ciclomatica modificata
            int modifiedComplexity = calculateModifiedComplexity(ctMethod);
            builder.modifiedComplexity(modifiedComplexity);
            
            // Calcola complessità cognitiva  
            int cognitiveComplexity = cognitiveCalculator.calculateCognitiveComplexity(ctMethod);
            builder.cognitiveComplexity(cognitiveComplexity);
            
            // Analizza control flow paths
            ControlFlowAnalysis controlFlowAnalysis = controlFlowAnalyzer.analyzeControlFlow(ctMethod);
            builder.controlFlowAnalysis(controlFlowAnalysis);
            
            // Determina livello di rischio
            ComplexityRiskLevel riskLevel = determineRiskLevel(cyclomaticComplexity);
            builder.riskLevel(riskLevel);
            
            // Calcola decision points
            List<DecisionPoint> decisionPoints = extractDecisionPoints(ctMethod);
            builder.decisionPoints(decisionPoints);
            
            return builder.build();
            
        } catch (Exception e) {
            LOGGER.error("Error calculating complexity for method {}", methodInfo.getName(), e);
            return MethodComplexity.error(methodInfo, e.getMessage());
        }
    }
    
    private int calculateMcCabeComplexity(CtMethod method) throws BadBytecode {
        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        if (codeAttribute == null) {
            return 1; // Abstract method ha complessità 1
        }
        
        int complexity = 1; // Base complexity
        CodeIterator iterator = codeAttribute.iterator();
        
        while (iterator.hasNext()) {
            int index = iterator.next();
            int opcode = iterator.byteAt(index);
            
            // Increment complexity for each decision point
            switch (opcode) {
                // Conditional branches
                case Opcode.IFEQ, Opcode.IFNE, Opcode.IFLT, Opcode.IFGE, 
                     Opcode.IFGT, Opcode.IFLE, Opcode.IF_ICMPEQ, Opcode.IF_ICMPNE,
                     Opcode.IF_ICMPLT, Opcode.IF_ICMPGE, Opcode.IF_ICMPGT, Opcode.IF_ICMPLE,
                     Opcode.IF_ACMPEQ, Opcode.IF_ACMPNE, Opcode.IFNULL, Opcode.IFNONNULL -> {
                    complexity++;
                }
                
                // Switch statements
                case Opcode.TABLESWITCH -> {
                    int defaultByte = (index + 4) & ~3; // 4-byte boundary alignment
                    int low = iterator.s32bitAt(defaultByte + 4);
                    int high = iterator.s32bitAt(defaultByte + 8);
                    complexity += (high - low + 1); // Each case adds complexity
                }
                
                case Opcode.LOOKUPSWITCH -> {
                    int defaultByte = (index + 4) & ~3; // 4-byte boundary alignment
                    int npairs = iterator.s32bitAt(defaultByte + 4);
                    complexity += npairs; // Each case adds complexity
                }
                
                // Exception handlers (try-catch)
                // Note: These are handled separately through exception table analysis
            }
        }
        
        // Add complexity for exception handlers
        ExceptionTable exceptionTable = codeAttribute.getExceptionTable();
        if (exceptionTable != null) {
            complexity += exceptionTable.size(); // Each catch block adds complexity
        }
        
        return complexity;
    }
    
    private int calculateModifiedComplexity(CtMethod method) throws BadBytecode {
        // Modified complexity considers additional factors like:
        // - Boolean operators (&&, ||) which create additional paths
        // - Nested control structures with higher weight
        // - Exception handling complexity
        
        int baseComplexity = calculateMcCabeComplexity(method);
        int modificationFactor = 0;
        
        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        if (codeAttribute == null) {
            return baseComplexity;
        }
        
        CodeIterator iterator = codeAttribute.iterator();
        int nestingLevel = 0;
        
        while (iterator.hasNext()) {
            int index = iterator.next();
            int opcode = iterator.byteAt(index);
            
            switch (opcode) {
                // Increment nesting level for control structures
                case Opcode.IFEQ, Opcode.IFNE, Opcode.IFLT, Opcode.IFGE, 
                     Opcode.IFGT, Opcode.IFLE -> {
                    nestingLevel++;
                    modificationFactor += nestingLevel; // Higher weight for nested conditions
                }
                
                // Logical operators create additional complexity
                case Opcode.IAND, Opcode.IOR, Opcode.IXOR -> {
                    modificationFactor += 1;
                }
            }
        }
        
        return baseComplexity + modificationFactor;
    }
    
    private List<DecisionPoint> extractDecisionPoints(CtMethod method) throws BadBytecode {
        List<DecisionPoint> decisionPoints = new ArrayList<>();
        
        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        if (codeAttribute == null) {
            return decisionPoints;
        }
        
        CodeIterator iterator = codeAttribute.iterator();
        
        while (iterator.hasNext()) {
            int index = iterator.next();
            int opcode = iterator.byteAt(index);
            
            DecisionPointType type = getDecisionPointType(opcode);
            if (type != null) {
                DecisionPoint decisionPoint = DecisionPoint.builder()
                    .bytecodeIndex(index)
                    .opcode(opcode)
                    .type(type)
                    .targetBranches(calculateTargetBranches(iterator, index, opcode))
                    .build();
                
                decisionPoints.add(decisionPoint);
            }
        }
        
        return decisionPoints;
    }
    
    private ComplexityRiskLevel determineRiskLevel(int cyclomaticComplexity) {
        if (cyclomaticComplexity <= LOW_COMPLEXITY_THRESHOLD) {
            return ComplexityRiskLevel.LOW;
        } else if (cyclomaticComplexity <= MODERATE_COMPLEXITY_THRESHOLD) {
            return ComplexityRiskLevel.MODERATE;
        } else if (cyclomaticComplexity <= HIGH_COMPLEXITY_THRESHOLD) {
            return ComplexityRiskLevel.HIGH;
        } else {
            return ComplexityRiskLevel.VERY_HIGH;
        }
    }
    
    private List<ComplexityHotspot> identifyComplexityHotspots(List<MethodComplexity> methodComplexities,
                                                              Map<String, ClassComplexity> classComplexities) {
        List<ComplexityHotspot> hotspots = new ArrayList<>();
        
        // Hotspot per metodi singoli con alta complessità
        methodComplexities.stream()
            .filter(mc -> mc.getCyclomaticComplexity() > HIGH_COMPLEXITY_THRESHOLD)
            .forEach(mc -> {
                ComplexityHotspot hotspot = ComplexityHotspot.builder()
                    .type(HotspotType.HIGH_COMPLEXITY_METHOD)
                    .className(mc.getClassName())
                    .methodName(mc.getMethodName())
                    .complexity(mc.getCyclomaticComplexity())
                    .severity(HotspotSeverity.HIGH)
                    .description(String.format("Method has cyclomatic complexity of %d (threshold: %d)", 
                        mc.getCyclomaticComplexity(), HIGH_COMPLEXITY_THRESHOLD))
                    .recommendation("Consider refactoring this method by extracting smaller methods or simplifying the control flow")
                    .build();
                hotspots.add(hotspot);
            });
        
        // Hotspot per classi con alta complessità media
        classComplexities.entrySet().stream()
            .filter(entry -> entry.getValue().getAverageComplexity() > MODERATE_COMPLEXITY_THRESHOLD)
            .forEach(entry -> {
                ClassComplexity cc = entry.getValue();
                ComplexityHotspot hotspot = ComplexityHotspot.builder()
                    .type(HotspotType.HIGH_COMPLEXITY_CLASS)
                    .className(entry.getKey())
                    .complexity((int) cc.getAverageComplexity())
                    .severity(HotspotSeverity.MODERATE)
                    .description(String.format("Class has average method complexity of %.1f", cc.getAverageComplexity()))
                    .recommendation("Review class responsibilities and consider splitting into smaller classes")
                    .build();
                hotspots.add(hotspot);
            });
        
        // Hotspot per metodi con alta complessità cognitiva
        methodComplexities.stream()
            .filter(mc -> mc.getCognitiveComplexity() > 15) // Industry standard threshold
            .forEach(mc -> {
                ComplexityHotspot hotspot = ComplexityHotspot.builder()
                    .type(HotspotType.HIGH_COGNITIVE_COMPLEXITY)
                    .className(mc.getClassName())
                    .methodName(mc.getMethodName())
                    .complexity(mc.getCognitiveComplexity())
                    .severity(HotspotSeverity.HIGH)
                    .description(String.format("Method has cognitive complexity of %d (threshold: 15)", 
                        mc.getCognitiveComplexity()))
                    .recommendation("Reduce nesting levels and simplify boolean logic")
                    .build();
                hotspots.add(hotspot);
            });
        
        return hotspots.stream()
            .sorted(Comparator.comparing(ComplexityHotspot::getSeverity)
                .thenComparing(ComplexityHotspot::getComplexity).reversed())
            .collect(Collectors.toList());
    }
}
```

#### 2. Cognitive Complexity Calculator
```java
public class CognitiveComplexityCalculator {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(CognitiveComplexityCalculator.class);
    
    // Cognitive complexity increments based on SonarQube rules
    private static final int IF_WHILE_FOR_SWITCH_INCREMENT = 1;
    private static final int CATCH_INCREMENT = 1;
    private static final int BOOLEAN_OPERATOR_INCREMENT = 1;
    private static final int NESTING_INCREMENT = 1; // Per level of nesting
    
    public int calculateCognitiveComplexity(CtMethod method) {
        try {
            CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
            if (codeAttribute == null) {
                return 0; // Abstract methods have no cognitive complexity
            }
            
            CognitiveComplexityVisitor visitor = new CognitiveComplexityVisitor();
            visitBytecode(codeAttribute, visitor);
            
            return visitor.getTotalComplexity();
            
        } catch (Exception e) {
            LOGGER.error("Error calculating cognitive complexity for {}", method.getLongName(), e);
            return 0;
        }
    }
    
    private void visitBytecode(CodeAttribute codeAttribute, CognitiveComplexityVisitor visitor) 
            throws BadBytecode {
        
        CodeIterator iterator = codeAttribute.iterator();
        
        while (iterator.hasNext()) {
            int index = iterator.next();
            int opcode = iterator.byteAt(index);
            
            visitor.visitInstruction(opcode, index);
            
            switch (opcode) {
                // Control flow statements
                case Opcode.IFEQ, Opcode.IFNE, Opcode.IFLT, Opcode.IFGE, 
                     Opcode.IFGT, Opcode.IFLE, Opcode.IF_ICMPEQ, Opcode.IF_ICMPNE,
                     Opcode.IF_ICMPLT, Opcode.IF_ICMPGE, Opcode.IF_ICMPGT, Opcode.IF_ICMPLE,
                     Opcode.IF_ACMPEQ, Opcode.IF_ACMPNE, Opcode.IFNULL, Opcode.IFNONNULL -> {
                    visitor.visitConditionalBranch();
                }
                
                // Switch statements
                case Opcode.TABLESWITCH, Opcode.LOOKUPSWITCH -> {
                    visitor.visitSwitch();
                }
                
                // Loops (detected through backward jumps)
                case Opcode.GOTO -> {
                    int target = iterator.s16bitAt(index + 1) + index;
                    if (target < index) { // Backward jump indicates loop
                        visitor.visitLoop();
                    }
                }
                
                // Boolean operators
                case Opcode.IAND, Opcode.IOR -> {
                    visitor.visitBooleanOperator();
                }
            }
        }
        
        // Analyze exception handlers for catch complexity
        ExceptionTable exceptionTable = codeAttribute.getExceptionTable();
        if (exceptionTable != null) {
            for (int i = 0; i < exceptionTable.size(); i++) {
                visitor.visitCatchClause();
            }
        }
    }
    
    private static class CognitiveComplexityVisitor {
        private int totalComplexity = 0;
        private int nestingLevel = 0;
        private final Stack<Integer> nestingStack = new Stack<>();
        
        public void visitConditionalBranch() {
            totalComplexity += IF_WHILE_FOR_SWITCH_INCREMENT;
            totalComplexity += nestingLevel; // Add nesting penalty
            enterNesting();
        }
        
        public void visitSwitch() {
            totalComplexity += IF_WHILE_FOR_SWITCH_INCREMENT;
            totalComplexity += nestingLevel; // Add nesting penalty
            enterNesting();
        }
        
        public void visitLoop() {
            totalComplexity += IF_WHILE_FOR_SWITCH_INCREMENT;
            totalComplexity += nestingLevel; // Add nesting penalty
            enterNesting();
        }
        
        public void visitCatchClause() {
            totalComplexity += CATCH_INCREMENT;
            totalComplexity += nestingLevel; // Add nesting penalty
        }
        
        public void visitBooleanOperator() {
            totalComplexity += BOOLEAN_OPERATOR_INCREMENT;
        }
        
        public void visitInstruction(int opcode, int index) {
            // Track nesting levels based on control flow
            // This is a simplified implementation - real implementation would need
            // more sophisticated control flow analysis
        }
        
        private void enterNesting() {
            nestingStack.push(nestingLevel);
            nestingLevel++;
        }
        
        private void exitNesting() {
            if (!nestingStack.isEmpty()) {
                nestingLevel = nestingStack.pop();
            }
        }
        
        public int getTotalComplexity() {
            return totalComplexity;
        }
    }
}
```

#### 3. Control Flow Analyzer
```java
public class ControlFlowAnalyzer {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(ControlFlowAnalyzer.class);
    
    public ControlFlowAnalysis analyzeControlFlow(CtMethod method) {
        try {
            CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
            if (codeAttribute == null) {
                return ControlFlowAnalysis.empty();
            }
            
            // Build control flow graph
            ControlFlowGraph cfg = buildControlFlowGraph(codeAttribute);
            
            // Calculate metrics
            int numberOfNodes = cfg.getNodes().size();
            int numberOfEdges = cfg.getEdges().size();
            int numberOfDecisionNodes = (int) cfg.getNodes().stream()
                .filter(node -> node.getOutDegree() > 1)
                .count();
            
            // Calculate essential complexity (complexity that cannot be reduced by structured programming)
            int essentialComplexity = calculateEssentialComplexity(cfg);
            
            // Identify strongly connected components (potential loops)
            List<List<ControlFlowNode>> stronglyConnectedComponents = findStronglyConnectedComponents(cfg);
            
            // Calculate depth metrics
            ControlFlowDepthMetrics depthMetrics = calculateDepthMetrics(cfg);
            
            return ControlFlowAnalysis.builder()
                .numberOfNodes(numberOfNodes)
                .numberOfEdges(numberOfEdges)
                .numberOfDecisionNodes(numberOfDecisionNodes)
                .essentialComplexity(essentialComplexity)
                .stronglyConnectedComponents(stronglyConnectedComponents)
                .depthMetrics(depthMetrics)
                .controlFlowGraph(cfg)
                .build();
                
        } catch (Exception e) {
            LOGGER.error("Error analyzing control flow for {}", method.getLongName(), e);
            return ControlFlowAnalysis.error(e.getMessage());
        }
    }
    
    private ControlFlowGraph buildControlFlowGraph(CodeAttribute codeAttribute) throws BadBytecode {
        ControlFlowGraph.Builder graphBuilder = ControlFlowGraph.builder();
        
        // Create nodes for each basic block
        List<BasicBlock> basicBlocks = identifyBasicBlocks(codeAttribute);
        Map<Integer, ControlFlowNode> nodeMap = new HashMap<>();
        
        for (BasicBlock block : basicBlocks) {
            ControlFlowNode node = ControlFlowNode.builder()
                .id(block.getStartIndex())
                .startIndex(block.getStartIndex())
                .endIndex(block.getEndIndex())
                .instructions(block.getInstructions())
                .build();
                
            nodeMap.put(block.getStartIndex(), node);
            graphBuilder.addNode(node);
        }
        
        // Create edges based on control flow
        createControlFlowEdges(codeAttribute, nodeMap, graphBuilder);
        
        return graphBuilder.build();
    }
    
    private List<BasicBlock> identifyBasicBlocks(CodeAttribute codeAttribute) throws BadBytecode {
        List<BasicBlock> basicBlocks = new ArrayList<>();
        Set<Integer> leaders = new HashSet<>(); // Start of basic blocks
        
        // First instruction is always a leader
        leaders.add(0);
        
        CodeIterator iterator = codeAttribute.iterator();
        
        // Find all leaders (start of basic blocks)
        while (iterator.hasNext()) {
            int index = iterator.next();
            int opcode = iterator.byteAt(index);
            
            // Target of jump is a leader
            if (isJumpInstruction(opcode)) {
                int target = calculateJumpTarget(iterator, index, opcode);
                if (target >= 0) {
                    leaders.add(target);
                }
                
                // Instruction after jump is a leader (if it exists)
                if (iterator.hasNext()) {
                    leaders.add(iterator.lookAhead());
                }
            }
        }
        
        // Add exception handler entry points as leaders
        ExceptionTable exceptionTable = codeAttribute.getExceptionTable();
        if (exceptionTable != null) {
            for (int i = 0; i < exceptionTable.size(); i++) {
                leaders.add(exceptionTable.handlerPc(i));
            }
        }
        
        // Create basic blocks
        List<Integer> sortedLeaders = new ArrayList<>(leaders);
        Collections.sort(sortedLeaders);
        
        for (int i = 0; i < sortedLeaders.size(); i++) {
            int start = sortedLeaders.get(i);
            int end = (i + 1 < sortedLeaders.size()) ? 
                sortedLeaders.get(i + 1) - 1 : codeAttribute.getCodeLength() - 1;
            
            BasicBlock block = BasicBlock.builder()
                .startIndex(start)
                .endIndex(end)
                .instructions(extractInstructions(codeAttribute, start, end))
                .build();
                
            basicBlocks.add(block);
        }
        
        return basicBlocks;
    }
    
    private void createControlFlowEdges(CodeAttribute codeAttribute, 
                                       Map<Integer, ControlFlowNode> nodeMap,
                                       ControlFlowGraph.Builder graphBuilder) throws BadBytecode {
        
        CodeIterator iterator = codeAttribute.iterator();
        
        while (iterator.hasNext()) {
            int index = iterator.next();
            int opcode = iterator.byteAt(index);
            
            ControlFlowNode currentNode = findNodeContaining(index, nodeMap);
            if (currentNode == null) continue;
            
            switch (opcode) {
                // Conditional branches create two edges
                case Opcode.IFEQ, Opcode.IFNE, Opcode.IFLT, Opcode.IFGE, 
                     Opcode.IFGT, Opcode.IFLE, Opcode.IF_ICMPEQ, Opcode.IF_ICMPNE,
                     Opcode.IF_ICMPLT, Opcode.IF_ICMPGE, Opcode.IF_ICMPGT, Opcode.IF_ICMPLE,
                     Opcode.IF_ACMPEQ, Opcode.IF_ACMPNE, Opcode.IFNULL, Opcode.IFNONNULL -> {
                    
                    // Branch target
                    int branchTarget = iterator.s16bitAt(index + 1) + index;
                    ControlFlowNode targetNode = findNodeStartingAt(branchTarget, nodeMap);
                    if (targetNode != null) {
                        ControlFlowEdge branchEdge = ControlFlowEdge.builder()
                            .source(currentNode)
                            .target(targetNode)
                            .edgeType(EdgeType.CONDITIONAL_TRUE)
                            .build();
                        graphBuilder.addEdge(branchEdge);
                    }
                    
                    // Fall-through target
                    int fallThroughTarget = index + 3; // Most conditional branches are 3 bytes
                    ControlFlowNode fallThroughNode = findNodeStartingAt(fallThroughTarget, nodeMap);
                    if (fallThroughNode != null) {
                        ControlFlowEdge fallThroughEdge = ControlFlowEdge.builder()
                            .source(currentNode)
                            .target(fallThroughNode)
                            .edgeType(EdgeType.CONDITIONAL_FALSE)
                            .build();
                        graphBuilder.addEdge(fallThroughEdge);
                    }
                }
                
                // Unconditional jumps
                case Opcode.GOTO -> {
                    int target = iterator.s16bitAt(index + 1) + index;
                    ControlFlowNode targetNode = findNodeStartingAt(target, nodeMap);
                    if (targetNode != null) {
                        ControlFlowEdge edge = ControlFlowEdge.builder()
                            .source(currentNode)
                            .target(targetNode)
                            .edgeType(EdgeType.UNCONDITIONAL)
                            .build();
                        graphBuilder.addEdge(edge);
                    }
                }
                
                // Return instructions have no outgoing edges
                case Opcode.RETURN, Opcode.IRETURN, Opcode.LRETURN, 
                     Opcode.FRETURN, Opcode.DRETURN, Opcode.ARETURN -> {
                    // No outgoing edges
                }
            }
        }
        
        // Add exception handler edges
        addExceptionHandlerEdges(codeAttribute, nodeMap, graphBuilder);
    }
}
```

### Principi SOLID Applicati
- **SRP**: Calculator, analyzer, extractor separati per responsabilità specifiche
- **OCP**: Facilmente estendibile per nuovi tipi di complexity metrics
- **LSP**: Implementazioni intercambiabili per diversi algoritmi di complexity
- **ISP**: Interfacce specifiche per calculation, analysis, recommendation
- **DIP**: Dipende da abstractions per bytecode analysis e control flow

### Test Unitari da Implementare
```java
// CyclomaticComplexityCalculatorTest.java
@Test
void shouldCalculateBasicMethodComplexity() {
    // Arrange
    MethodInfo simpleMethod = createSimpleMethodInfo();
    CtMethod ctMethod = createSimpleCtMethod(); // method with no branches
    CyclomaticComplexityCalculator calculator = new JavassistCyclomaticComplexityCalculator();
    
    // Act
    MethodComplexity complexity = calculator.calculateMethodComplexity(simpleMethod, ctMethod);
    
    // Assert
    assertThat(complexity.getCyclomaticComplexity()).isEqualTo(1); // Base complexity
    assertThat(complexity.getRiskLevel()).isEqualTo(ComplexityRiskLevel.LOW);
    assertThat(complexity.getDecisionPoints()).isEmpty();
}

@Test
void shouldCalculateComplexityForMethodWithConditionals() {
    MethodInfo methodWithIfs = createMethodWithIfStatements(3); // 3 if statements
    CtMethod ctMethod = createCtMethodWithConditionals(3);
    CyclomaticComplexityCalculator calculator = new JavassistCyclomaticComplexityCalculator();
    
    MethodComplexity complexity = calculator.calculateMethodComplexity(methodWithIfs, ctMethod);
    
    assertThat(complexity.getCyclomaticComplexity()).isEqualTo(4); // 1 + 3 conditions
    assertThat(complexity.getDecisionPoints()).hasSize(3);
    assertThat(complexity.getRiskLevel()).isEqualTo(ComplexityRiskLevel.LOW);
}

@Test
void shouldCalculateComplexityForMethodWithSwitchStatement() {
    CtMethod methodWithSwitch = createCtMethodWithSwitch(5); // switch with 5 cases
    MethodInfo methodInfo = createMethodInfo();
    CyclomaticComplexityCalculator calculator = new JavassistCyclomaticComplexityCalculator();
    
    MethodComplexity complexity = calculator.calculateMethodComplexity(methodInfo, methodWithSwitch);
    
    assertThat(complexity.getCyclomaticComplexity()).isEqualTo(6); // 1 + 5 cases
    assertThat(complexity.getDecisionPoints()).hasSize(1);
    assertThat(complexity.getDecisionPoints().get(0).getType()).isEqualTo(DecisionPointType.SWITCH);
}

@Test
void shouldCalculateComplexityForMethodWithExceptionHandling() {
    CtMethod methodWithTryCatch = createCtMethodWithExceptionHandling(2); // 2 catch blocks
    MethodInfo methodInfo = createMethodInfo();
    CyclomaticComplexityCalculator calculator = new JavassistCyclomaticComplexityCalculator();
    
    MethodComplexity complexity = calculator.calculateMethodComplexity(methodInfo, methodWithTryCatch);
    
    assertThat(complexity.getCyclomaticComplexity()).isEqualTo(3); // 1 + 2 catch blocks
}

@Test
void shouldIdentifyHighComplexityMethods() {
    JarContent jarContent = createJarWithVariousComplexityMethods();
    CyclomaticComplexityCalculator calculator = new JavassistCyclomaticComplexityCalculator();
    
    ComplexityAnalysisResult result = calculator.calculateComplexity(jarContent);
    
    assertThat(result.isSuccessful()).isTrue();
    assertThat(result.getComplexityHotspots()).isNotEmpty();
    
    // Check that high complexity methods are identified
    boolean hasHighComplexityHotspot = result.getComplexityHotspots().stream()
        .anyMatch(hotspot -> hotspot.getType() == HotspotType.HIGH_COMPLEXITY_METHOD);
    assertThat(hasHighComplexityHotspot).isTrue();
}

@Test
void shouldCalculateClassComplexityStatistics() {
    JarContent jarContent = createJarWithMultipleClasses();
    CyclomaticComplexityCalculator calculator = new JavassistCyclomaticComplexityCalculator();
    
    ComplexityAnalysisResult result = calculator.calculateComplexity(jarContent);
    
    assertThat(result.getClassComplexities()).isNotEmpty();
    
    ClassComplexity classComplexity = result.getClassComplexities().values().iterator().next();
    assertThat(classComplexity.getAverageComplexity()).isGreaterThan(0.0);
    assertThat(classComplexity.getMaximumComplexity()).isGreaterThanOrEqualTo(classComplexity.getAverageComplexity());
    assertThat(classComplexity.getTotalComplexity()).isGreaterThan(0);
}

// CognitiveComplexityCalculatorTest.java
@Test
void shouldCalculateCognitiveComplexity() {
    CtMethod methodWithNesting = createMethodWithNestedConditions(2); // 2 levels of nesting
    CognitiveComplexityCalculator calculator = new CognitiveComplexityCalculator();
    
    int cognitiveComplexity = calculator.calculateCognitiveComplexity(methodWithNesting);
    
    // Each condition adds 1 + nesting level penalty
    // Level 0: if (1) -> complexity += 1
    // Level 1: nested if (1 + 1 nesting penalty) -> complexity += 2
    // Total: 3
    assertThat(cognitiveComplexity).isEqualTo(3);
}

@Test
void shouldHandleBooleanOperators() {
    CtMethod methodWithBooleanLogic = createMethodWithBooleanOperators(3); // 3 && or || operators
    CognitiveComplexityCalculator calculator = new CognitiveComplexityCalculator();
    
    int cognitiveComplexity = calculator.calculateCognitiveComplexity(methodWithBooleanLogic);
    
    assertThat(cognitiveComplexity).isGreaterThanOrEqualTo(3); // At least 1 per boolean operator
}

// ControlFlowAnalyzerTest.java
@Test
void shouldBuildControlFlowGraph() {
    CtMethod methodWithBranches = createMethodWithMultipleBranches();
    ControlFlowAnalyzer analyzer = new ControlFlowAnalyzer();
    
    ControlFlowAnalysis analysis = analyzer.analyzeControlFlow(methodWithBranches);
    
    assertThat(analysis.getNumberOfNodes()).isGreaterThan(1);
    assertThat(analysis.getNumberOfEdges()).isGreaterThan(0);
    assertThat(analysis.getControlFlowGraph()).isNotNull();
}

@Test
void shouldIdentifyDecisionNodes() {
    CtMethod methodWithDecisions = createMethodWithConditionals(3);
    ControlFlowAnalyzer analyzer = new ControlFlowAnalyzer();
    
    ControlFlowAnalysis analysis = analyzer.analyzeControlFlow(methodWithDecisions);
    
    assertThat(analysis.getNumberOfDecisionNodes()).isGreaterThan(0);
    assertThat(analysis.getNumberOfDecisionNodes()).isLessThanOrEqualTo(analysis.getNumberOfNodes());
}

@Test
void shouldCalculateEssentialComplexity() {
    CtMethod methodWithLoop = createMethodWithWhileLoop();
    ControlFlowAnalyzer analyzer = new ControlFlowAnalyzer();
    
    ControlFlowAnalysis analysis = analyzer.analyzeControlFlow(methodWithLoop);
    
    assertThat(analysis.getEssentialComplexity()).isGreaterThan(0);
    // Essential complexity should be less than or equal to cyclomatic complexity
}

// Integration Test
@Test
void shouldAnalyzeComplexRealWorldApplication() throws IOException {
    Path springBootJar = getTestResourcePath("complex-spring-boot.jar");
    JarContent jarContent = new JarLoader().loadJar(new JarLocation(springBootJar));
    CyclomaticComplexityCalculator calculator = new JavassistCyclomaticComplexityCalculator();
    
    ComplexityAnalysisResult result = calculator.calculateComplexity(jarContent);
    
    // Verifiche su applicazione complessa reale
    assertThat(result.getMethodComplexities()).hasSizeGreaterThan(100);
    assertThat(result.getClassComplexities()).hasSizeGreaterThan(20);
    assertThat(result.getStatistics().getAverageComplexity()).isBetween(1.0, 10.0);
    assertThat(result.getStatistics().getMaximumComplexity()).isGreaterThan(5);
    
    // Should have some recommendations for improvement
    assertThat(result.getRecommendations()).isNotEmpty();
}

// Performance Test
@Test
void shouldCompleteAnalysisInReasonableTime() {
    JarContent largeJarContent = createLargeJarContent(1000); // 1000 classes
    CyclomaticComplexityCalculator calculator = new JavassistCyclomaticComplexityCalculator();
    
    long startTime = System.currentTimeMillis();
    ComplexityAnalysisResult result = calculator.calculateComplexity(largeJarContent);
    long duration = System.currentTimeMillis() - startTime;
    
    assertThat(result.isSuccessful()).isTrue();
    assertThat(duration).isLessThan(30000); // Should complete within 30 seconds
}
```

---

## T5.1.2: CodeSizeAnalyzer per Class/Method Length

### Descrizione Dettagliata
Analyzer completo per misurare le dimensioni del codice a livello di classi e metodi, fornendo metriche dettagliate su lunghezza, dimensioni e distribuzioni per identificare classi e metodi eccessivamente grandi che necessitano refactoring.

### Scopo dell'Attività
- Misurare lunghezza di classi e metodi in linee di codice (LOC)
- Calcolare metriche di dimensione (bytecode size, instruction count)
- Identificare outlier e hotspot dimensionali
- Fornire distribuzioni statistiche delle dimensioni
- Generare raccomandazioni per refactoring dimensionale

### Impatti su Altri Moduli
- **LongMethodDetector**: Correlazione e cross-validation
- **LargeClassDetector**: Input per identificazione classi problematiche
- **ComplexityCalculator**: Correlazione size-complexity
- **RefactoringAnalyzer**: Prioritization basata su dimensioni

### Componenti da Implementare

#### 1. Code Size Analyzer Core
```java
public interface CodeSizeAnalyzer {
    CodeSizeAnalysisResult analyzeCodeSize(JarContent jarContent);
}

public class JavassistCodeSizeAnalyzer implements CodeSizeAnalyzer {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistCodeSizeAnalyzer.class);
    
    // Industry standard thresholds
    private static final int LARGE_METHOD_LOC_THRESHOLD = 50;
    private static final int VERY_LARGE_METHOD_LOC_THRESHOLD = 100;
    private static final int LARGE_CLASS_LOC_THRESHOLD = 500;
    private static final int VERY_LARGE_CLASS_LOC_THRESHOLD = 1000;
    
    private final MethodSizeCalculator methodSizeCalculator;
    private final ClassSizeCalculator classSizeCalculator;
    private final SizeDistributionAnalyzer distributionAnalyzer;
    private final SizeCorrelationAnalyzer correlationAnalyzer;
    
    public JavassistCodeSizeAnalyzer() {
        this.methodSizeCalculator = new MethodSizeCalculator();
        this.classSizeCalculator = new ClassSizeCalculator();
        this.distributionAnalyzer = new SizeDistributionAnalyzer();
        this.correlationAnalyzer = new SizeCorrelationAnalyzer();
    }
    
    @Override
    public CodeSizeAnalysisResult analyzeCodeSize(JarContent jarContent) {
        LOGGER.startOperation("Code size analysis");
        long startTime = System.currentTimeMillis();
        
        try {
            CodeSizeAnalysisResult.Builder resultBuilder = CodeSizeAnalysisResult.builder();
            
            List<MethodSize> methodSizes = new ArrayList<>();
            List<ClassSize> classSizes = new ArrayList<>();
            
            // Analizza ogni classe
            for (ClassInfo classInfo : jarContent.getClasses()) {
                try {
                    ClassPool pool = ClassPool.getDefault();
                    CtClass ctClass = pool.get(classInfo.getFullyQualifiedName());
                    
                    // Calcola dimensioni della classe
                    ClassSize classSize = classSizeCalculator.calculateClassSize(classInfo, ctClass);
                    classSizes.add(classSize);
                    
                    // Calcola dimensioni dei metodi
                    for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
                        MethodInfo methodInfo = findMethodInfo(classInfo, ctMethod);
                        if (methodInfo != null) {
                            MethodSize methodSize = methodSizeCalculator.calculateMethodSize(
                                methodInfo, ctMethod, classInfo.getFullyQualifiedName());
                            methodSizes.add(methodSize);
                        }
                    }
                    
                } catch (Exception e) {
                    LOGGER.error("Error analyzing size for class {}", classInfo.getFullyQualifiedName(), e);
                    continue;
                }
            }
            
            resultBuilder.methodSizes(methodSizes);
            resultBuilder.classSizes(classSizes);
            
            // Calcola statistiche di distribuzione
            SizeDistributionStatistics distributionStats = distributionAnalyzer.analyzeDistributions(
                methodSizes, classSizes);
            resultBuilder.distributionStatistics(distributionStats);
            
            // Identifica outlier
            List<SizeOutlier> sizeOutliers = identifySizeOutliers(methodSizes, classSizes, distributionStats);
            resultBuilder.sizeOutliers(sizeOutliers);
            
            // Analizza correlazioni
            SizeCorrelationAnalysis correlationAnalysis = correlationAnalyzer.analyzeCorrelations(
                methodSizes, classSizes);
            resultBuilder.correlationAnalysis(correlationAnalysis);
            
            // Genera metriche aggregate
            CodeSizeMetrics metrics = calculateCodeSizeMetrics(methodSizes, classSizes);
            resultBuilder.metrics(metrics);
            
            // Genera raccomandazioni
            List<SizeRecommendation> recommendations = generateSizeRecommendations(
                methodSizes, classSizes, sizeOutliers);
            resultBuilder.recommendations(recommendations);
            
            LOGGER.info("Analyzed {} methods across {} classes", 
                methodSizes.size(), classSizes.size());
            
            return resultBuilder
                .analysisMetadata(AnalysisMetadata.successful(System.currentTimeMillis() - startTime))
                .build();
                
        } catch (Exception e) {
            LOGGER.error("Code size analysis failed", e);
            return CodeSizeAnalysisResult.failed("Analysis failed: " + e.getMessage());
        } finally {
            LOGGER.endOperation("Code size analysis", System.currentTimeMillis() - startTime);
        }
    }
    
    private List<SizeOutlier> identifySizeOutliers(List<MethodSize> methodSizes, 
                                                  List<ClassSize> classSizes,
                                                  SizeDistributionStatistics distributionStats) {
        List<SizeOutlier> outliers = new ArrayList<>();
        
        // Metodi outlier basati su LOC
        double methodLocThreshold = distributionStats.getMethodLoc().getPercentile(95);
        methodSizes.stream()
            .filter(ms -> ms.getLinesOfCode() > Math.max(methodLocThreshold, LARGE_METHOD_LOC_THRESHOLD))
            .forEach(ms -> {
                SizeOutlierSeverity severity = ms.getLinesOfCode() > VERY_LARGE_METHOD_LOC_THRESHOLD ? 
                    SizeOutlierSeverity.HIGH : SizeOutlierSeverity.MODERATE;
                
                outliers.add(SizeOutlier.builder()
                    .type(OutlierType.LARGE_METHOD)
                    .className(ms.getClassName())
                    .methodName(ms.getMethodName())
                    .size(ms.getLinesOfCode())
                    .severity(severity)
                    .description(String.format("Method has %d lines of code (threshold: %d)", 
                        ms.getLinesOfCode(), LARGE_METHOD_LOC_THRESHOLD))
                    .recommendation("Consider breaking this method into smaller, more focused methods")
                    .build());
            });
        
        // Classi outlier basate su LOC
        double classLocThreshold = distributionStats.getClassLoc().getPercentile(95);
        classSizes.stream()
            .filter(cs -> cs.getLinesOfCode() > Math.max(classLocThreshold, LARGE_CLASS_LOC_THRESHOLD))
            .forEach(cs -> {
                SizeOutlierSeverity severity = cs.getLinesOfCode() > VERY_LARGE_CLASS_LOC_THRESHOLD ? 
                    SizeOutlierSeverity.HIGH : SizeOutlierSeverity.MODERATE;
                
                outliers.add(SizeOutlier.builder()
                    .type(OutlierType.LARGE_CLASS)
                    .className(cs.getClassName())
                    .size(cs.getLinesOfCode())
                    .severity(severity)
                    .description(String.format("Class has %d lines of code (threshold: %d)", 
                        cs.getLinesOfCode(), LARGE_CLASS_LOC_THRESHOLD))
                    .recommendation("Consider splitting this class into smaller, more cohesive classes")
                    .build());
            });
        
        // Metodi outlier basati su bytecode size
        double methodBytecodeThreshold = distributionStats.getMethodBytecodeSize().getPercentile(99);
        methodSizes.stream()
            .filter(ms -> ms.getBytecodeSize() > methodBytecodeThreshold)
            .filter(ms -> ms.getBytecodeSize() > 1000) // Absolute threshold
            .forEach(ms -> {
                outliers.add(SizeOutlier.builder()
                    .type(OutlierType.LARGE_METHOD_BYTECODE)
                    .className(ms.getClassName())
                    .methodName(ms.getMethodName())
                    .size(ms.getBytecodeSize())
                    .severity(SizeOutlierSeverity.MODERATE)
                    .description(String.format("Method has %d bytes of bytecode", ms.getBytecodeSize()))
                    .recommendation("Large bytecode size may indicate complex logic that could be simplified")
                    .build());
            });
        
        return outliers.stream()
            .sorted(Comparator.comparing(SizeOutlier::getSeverity)
                .thenComparing(SizeOutlier::getSize).reversed())
            .collect(Collectors.toList());
    }
}
```

#### 2. Method Size Calculator
```java
public class MethodSizeCalculator {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(MethodSizeCalculator.class);
    
    public MethodSize calculateMethodSize(MethodInfo methodInfo, CtMethod ctMethod, String className) {
        try {
            MethodSize.Builder builder = MethodSize.builder()
                .className(className)
                .methodName(methodInfo.getName())
                .methodSignature(methodInfo.getSignature());
            
            // Calcola Lines of Code (approssimazione basata su bytecode)
            int linesOfCode = calculateLinesOfCode(ctMethod);
            builder.linesOfCode(linesOfCode);
            
            // Calcola dimensione bytecode
            int bytecodeSize = calculateBytecodeSize(ctMethod);
            builder.bytecodeSize(bytecodeSize);
            
            // Conta numero di istruzioni
            int instructionCount = countInstructions(ctMethod);
            builder.instructionCount(instructionCount);
            
            // Calcola numero di parametri
            int parameterCount = ctMethod.getParameterTypes().length;
            builder.parameterCount(parameterCount);
            
            // Conta numero di variabili locali
            int localVariableCount = countLocalVariables(ctMethod);
            builder.localVariableCount(localVariableCount);
            
            // Calcola stack depth massimo
            int maxStackDepth = calculateMaxStackDepth(ctMethod);
            builder.maxStackDepth(maxStackDepth);
            
            // Determina categoria di dimensione
            MethodSizeCategory category = determineMethodSizeCategory(linesOfCode, bytecodeSize);
            builder.sizeCategory(category);
            
            // Calcola metriche di complessità strutturale
            StructuralComplexityMetrics structuralMetrics = calculateStructuralMetrics(ctMethod);
            builder.structuralMetrics(structuralMetrics);
            
            return builder.build();
            
        } catch (Exception e) {
            LOGGER.error("Error calculating method size for {}.{}", className, methodInfo.getName(), e);
            return MethodSize.error(className, methodInfo.getName(), e.getMessage());
        }
    }
    
    private int calculateLinesOfCode(CtMethod method) throws BadBytecode {
        // Approssimazione delle LOC basata su:
        // 1. Numero di istruzioni bytecode
        // 2. Presenza di debug information (LineNumberTable)
        // 3. Pattern di bytecode che indicano statement multipli
        
        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        if (codeAttribute == null) {
            return 1; // Abstract method
        }
        
        // Prova a usare LineNumberTable se disponibile
        LineNumberAttribute lineNumberAttr = (LineNumberAttribute) codeAttribute.getAttribute(LineNumberAttribute.tag);
        if (lineNumberAttr != null) {
            int minLine = Integer.MAX_VALUE;
            int maxLine = Integer.MIN_VALUE;
            
            for (int i = 0; i < lineNumberAttr.tableLength(); i++) {
                int lineNumber = lineNumberAttr.lineNumber(i);
                minLine = Math.min(minLine, lineNumber);
                maxLine = Math.max(maxLine, lineNumber);
            }
            
            if (minLine != Integer.MAX_VALUE && maxLine != Integer.MIN_VALUE) {
                return maxLine - minLine + 1;
            }
        }
        
        // Fallback: stima basata su bytecode patterns
        return estimateLinesOfCodeFromBytecode(codeAttribute);
    }
    
    private int estimateLinesOfCodeFromBytecode(CodeAttribute codeAttribute) throws BadBytecode {
        int estimatedLines = 1; // Base case
        CodeIterator iterator = codeAttribute.iterator();
        
        while (iterator.hasNext()) {
            int index = iterator.next();
            int opcode = iterator.byteAt(index);
            
            // Ogni statement logico aggiunge circa 1 linea
            switch (opcode) {
                // Assignment/calculation operations
                case Opcode.ISTORE, Opcode.LSTORE, Opcode.FSTORE, Opcode.DSTORE, Opcode.ASTORE,
                     Opcode.ISTORE_0, Opcode.ISTORE_1, Opcode.ISTORE_2, Opcode.ISTORE_3,
                     Opcode.LSTORE_0, Opcode.LSTORE_1, Opcode.LSTORE_2, Opcode.LSTORE_3,
                     Opcode.FSTORE_0, Opcode.FSTORE_1, Opcode.FSTORE_2, Opcode.FSTORE_3,
                     Opcode.DSTORE_0, Opcode.DSTORE_1, Opcode.DSTORE_2, Opcode.DSTORE_3,
                     Opcode.ASTORE_0, Opcode.ASTORE_1, Opcode.ASTORE_2, Opcode.ASTORE_3 -> {
                    estimatedLines++;
                }
                
                // Method calls
                case Opcode.INVOKEVIRTUAL, Opcode.INVOKESPECIAL, Opcode.INVOKESTATIC, 
                     Opcode.INVOKEINTERFACE, Opcode.INVOKEDYNAMIC -> {
                    estimatedLines++;
                }
                
                // Control flow statements
                case Opcode.IFEQ, Opcode.IFNE, Opcode.IFLT, Opcode.IFGE, 
                     Opcode.IFGT, Opcode.IFLE, Opcode.IF_ICMPEQ, Opcode.IF_ICMPNE,
                     Opcode.IF_ICMPLT, Opcode.IF_ICMPGE, Opcode.IF_ICMPGT, Opcode.IF_ICMPLE,
                     Opcode.IF_ACMPEQ, Opcode.IF_ACMPNE, Opcode.IFNULL, Opcode.IFNONNULL -> {
                    estimatedLines++;
                }
                
                // Return statements
                case Opcode.RETURN, Opcode.IRETURN, Opcode.LRETURN, 
                     Opcode.FRETURN, Opcode.DRETURN, Opcode.ARETURN -> {
                    estimatedLines++;
                }
            }
        }
        
        return Math.max(1, estimatedLines / 3); // Adjust for bytecode verbosity
    }
    
    private int calculateBytecodeSize(CtMethod method) {
        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        return codeAttribute != null ? codeAttribute.getCodeLength() : 0;
    }
    
    private int countInstructions(CtMethod method) throws BadBytecode {
        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        if (codeAttribute == null) {
            return 0;
        }
        
        int count = 0;
        CodeIterator iterator = codeAttribute.iterator();
        
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        
        return count;
    }
    
    private int countLocalVariables(CtMethod method) {
        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        if (codeAttribute == null) {
            return 0;
        }
        
        // Get from LocalVariableAttribute if available
        LocalVariableAttribute localVarAttr = (LocalVariableAttribute) 
            codeAttribute.getAttribute(LocalVariableAttribute.tag);
        
        if (localVarAttr != null) {
            return localVarAttr.tableLength();
        }
        
        // Fallback: use max locals from code attribute
        return codeAttribute.getMaxLocals();
    }
    
    private int calculateMaxStackDepth(CtMethod method) {
        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        return codeAttribute != null ? codeAttribute.getMaxStack() : 0;
    }
    
    private MethodSizeCategory determineMethodSizeCategory(int linesOfCode, int bytecodeSize) {
        if (linesOfCode <= 10 && bytecodeSize <= 100) {
            return MethodSizeCategory.VERY_SMALL;
        } else if (linesOfCode <= 25 && bytecodeSize <= 300) {
            return MethodSizeCategory.SMALL;
        } else if (linesOfCode <= 50 && bytecodeSize <= 600) {
            return MethodSizeCategory.MEDIUM;
        } else if (linesOfCode <= 100 && bytecodeSize <= 1200) {
            return MethodSizeCategory.LARGE;
        } else {
            return MethodSizeCategory.VERY_LARGE;
        }
    }
    
    private StructuralComplexityMetrics calculateStructuralMetrics(CtMethod method) throws BadBytecode {
        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        if (codeAttribute == null) {
            return StructuralComplexityMetrics.empty();
        }
        
        StructuralComplexityMetrics.Builder builder = StructuralComplexityMetrics.builder();
        
        int nestingDepth = 0;
        int maxNestingDepth = 0;
        int branchingFactor = 0;
        int loopCount = 0;
        
        CodeIterator iterator = codeAttribute.iterator();
        Stack<Integer> nestingStack = new Stack<>();
        
        while (iterator.hasNext()) {
            int index = iterator.next();
            int opcode = iterator.byteAt(index);
            
            switch (opcode) {
                // Conditional branches increase nesting
                case Opcode.IFEQ, Opcode.IFNE, Opcode.IFLT, Opcode.IFGE, 
                     Opcode.IFGT, Opcode.IFLE, Opcode.IF_ICMPEQ, Opcode.IF_ICMPNE,
                     Opcode.IF_ICMPLT, Opcode.IF_ICMPGE, Opcode.IF_ICMPGT, Opcode.IF_ICMPLE,
                     Opcode.IF_ACMPEQ, Opcode.IF_ACMPNE, Opcode.IFNULL, Opcode.IFNONNULL -> {
                    nestingStack.push(index);
                    nestingDepth++;
                    maxNestingDepth = Math.max(maxNestingDepth, nestingDepth);
                    branchingFactor++;
                }
                
                // Loops (detected by backward jumps)
                case Opcode.GOTO -> {
                    int target = iterator.s16bitAt(index + 1) + index;
                    if (target < index) { // Backward jump
                        loopCount++;
                    }
                }
                
                // Switch statements
                case Opcode.TABLESWITCH, Opcode.LOOKUPSWITCH -> {
                    branchingFactor += 5; // Average case count estimation
                }
            }
        }
        
        return builder
            .maxNestingDepth(maxNestingDepth)
            .branchingFactor(branchingFactor)
            .loopCount(loopCount)
            .build();
    }
}
```

#### 3. Class Size Calculator
```java
public class ClassSizeCalculator {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(ClassSizeCalculator.class);
    
    public ClassSize calculateClassSize(ClassInfo classInfo, CtClass ctClass) {
        try {
            ClassSize.Builder builder = ClassSize.builder()
                .className(classInfo.getFullyQualifiedName());
            
            // Calcola LOC per la classe (somma di tutti i metodi)
            int totalLinesOfCode = 0;
            int totalBytecodeSize = 0;
            int totalInstructionCount = 0;
            
            List<MethodSize> methodSizes = new ArrayList<>();
            MethodSizeCalculator methodCalculator = new MethodSizeCalculator();
            
            for (CtMethod method : ctClass.getDeclaredMethods()) {
                MethodInfo methodInfo = createMethodInfo(method);
                MethodSize methodSize = methodCalculator.calculateMethodSize(
                    methodInfo, method, classInfo.getFullyQualifiedName());
                methodSizes.add(methodSize);
                
                totalLinesOfCode += methodSize.getLinesOfCode();
                totalBytecodeSize += methodSize.getBytecodeSize();
                totalInstructionCount += methodSize.getInstructionCount();
            }
            
            builder.linesOfCode(totalLinesOfCode);
            builder.totalBytecodeSize(totalBytecodeSize);
            builder.totalInstructionCount(totalInstructionCount);
            builder.methodSizes(methodSizes);
            
            // Conta campi
            int fieldCount = ctClass.getDeclaredFields().length;
            builder.fieldCount(fieldCount);
            
            // Conta metodi
            int methodCount = ctClass.getDeclaredMethods().length;
            builder.methodCount(methodCount);
            
            // Conta constructors
            int constructorCount = ctClass.getConstructors().length;
            builder.constructorCount(constructorCount);
            
            // Calcola dimensione file bytecode
            byte[] bytecode = ctClass.toBytecode();
            int classFileSize = bytecode.length;
            builder.classFileSize(classFileSize);
            
            // Analizza inner classes
            int innerClassCount = countInnerClasses(ctClass);
            builder.innerClassCount(innerClassCount);
            
            // Calcola metriche di distribuzione dei metodi
            MethodSizeDistribution methodDistribution = calculateMethodSizeDistribution(methodSizes);
            builder.methodSizeDistribution(methodDistribution);
            
            // Determina categoria di dimensione
            ClassSizeCategory category = determineClassSizeCategory(
                totalLinesOfCode, methodCount, fieldCount, classFileSize);
            builder.sizeCategory(category);
            
            // Calcola indici di coesione strutturale
            StructuralCohesionMetrics cohesionMetrics = calculateStructuralCohesion(
                ctClass, methodSizes, fieldCount);
            builder.cohesionMetrics(cohesionMetrics);
            
            return builder.build();
            
        } catch (Exception e) {
            LOGGER.error("Error calculating class size for {}", classInfo.getFullyQualifiedName(), e);
            return ClassSize.error(classInfo.getFullyQualifiedName(), e.getMessage());
        }
    }
    
    private int countInnerClasses(CtClass ctClass) {
        try {
            CtClass[] nestedClasses = ctClass.getNestedClasses();
            return nestedClasses != null ? nestedClasses.length : 0;
        } catch (Exception e) {
            LOGGER.debug("Could not count inner classes for {}", ctClass.getName(), e);
            return 0;
        }
    }
    
    private MethodSizeDistribution calculateMethodSizeDistribution(List<MethodSize> methodSizes) {
        if (methodSizes.isEmpty()) {
            return MethodSizeDistribution.empty();
        }
        
        List<Integer> sizes = methodSizes.stream()
            .map(MethodSize::getLinesOfCode)
            .sorted()
            .collect(Collectors.toList());
        
        double mean = sizes.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double median = calculateMedian(sizes);
        int min = sizes.get(0);
        int max = sizes.get(sizes.size() - 1);
        double standardDeviation = calculateStandardDeviation(sizes, mean);
        
        // Distribuzione per categoria
        Map<MethodSizeCategory, Long> categoryDistribution = methodSizes.stream()
            .collect(Collectors.groupingBy(
                MethodSize::getSizeCategory,
                Collectors.counting()
            ));
        
        return MethodSizeDistribution.builder()
            .mean(mean)
            .median(median)
            .minimum(min)
            .maximum(max)
            .standardDeviation(standardDeviation)
            .categoryDistribution(categoryDistribution)
            .build();
    }
    
    private ClassSizeCategory determineClassSizeCategory(int linesOfCode, int methodCount, 
                                                        int fieldCount, int classFileSize) {
        // Multi-dimensional categorization based on multiple factors
        int score = 0;
        
        // LOC component
        if (linesOfCode > 1000) score += 4;
        else if (linesOfCode > 500) score += 3;
        else if (linesOfCode > 200) score += 2;
        else if (linesOfCode > 100) score += 1;
        
        // Method count component
        if (methodCount > 50) score += 3;
        else if (methodCount > 25) score += 2;
        else if (methodCount > 15) score += 1;
        
        // Field count component  
        if (fieldCount > 30) score += 2;
        else if (fieldCount > 15) score += 1;
        
        // Class file size component
        if (classFileSize > 100000) score += 2; // > 100KB
        else if (classFileSize > 50000) score += 1;  // > 50KB
        
        return switch (score) {
            case 0, 1 -> ClassSizeCategory.VERY_SMALL;
            case 2, 3 -> ClassSizeCategory.SMALL;
            case 4, 5, 6 -> ClassSizeCategory.MEDIUM;
            case 7, 8, 9 -> ClassSizeCategory.LARGE;
            default -> ClassSizeCategory.VERY_LARGE;
        };
    }
    
    private StructuralCohesionMetrics calculateStructuralCohesion(CtClass ctClass, 
                                                                 List<MethodSize> methodSizes,
                                                                 int fieldCount) {
        try {
            // LCOM (Lack of Cohesion of Methods) metric
            double lcom = calculateLCOM(ctClass);
            
            // Method-to-field ratio
            double methodToFieldRatio = fieldCount > 0 ? 
                (double) methodSizes.size() / fieldCount : methodSizes.size();
            
            // Average method size consistency
            double methodSizeConsistency = calculateMethodSizeConsistency(methodSizes);
            
            // Complexity distribution evenness
            double complexityEvenness = calculateComplexityEvenness(methodSizes);
            
            return StructuralCohesionMetrics.builder()
                .lcom(lcom)
                .methodToFieldRatio(methodToFieldRatio)
                .methodSizeConsistency(methodSizeConsistency)
                .complexityEvenness(complexityEvenness)
                .build();
                
        } catch (Exception e) {
            LOGGER.debug("Error calculating structural cohesion for {}", ctClass.getName(), e);
            return StructuralCohesionMetrics.empty();
        }
    }
    
    private double calculateLCOM(CtClass ctClass) throws NotFoundException {
        // Simplified LCOM calculation based on field usage
        CtField[] fields = ctClass.getDeclaredFields();
        CtMethod[] methods = ctClass.getDeclaredMethods();
        
        if (methods.length < 2) return 0.0;
        
        int sharingPairs = 0;
        int totalPairs = 0;
        
        for (int i = 0; i < methods.length; i++) {
            for (int j = i + 1; j < methods.length; j++) {
                totalPairs++;
                if (shareCommonFields(methods[i], methods[j], fields)) {
                    sharingPairs++;
                }
            }
        }
        
        return totalPairs > 0 ? 1.0 - ((double) sharingPairs / totalPairs) : 0.0;
    }
    
    private boolean shareCommonFields(CtMethod method1, CtMethod method2, CtField[] fields) {
        // Simplified field sharing detection
        // In a real implementation, this would analyze bytecode to detect field access
        try {
            Set<String> fields1 = extractFieldAccess(method1);
            Set<String> fields2 = extractFieldAccess(method2);
            
            fields1.retainAll(fields2); // Intersection
            return !fields1.isEmpty();
            
        } catch (Exception e) {
            return false;
        }
    }
    
    private Set<String> extractFieldAccess(CtMethod method) throws BadBytecode {
        Set<String> accessedFields = new HashSet<>();
        
        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        if (codeAttribute == null) return accessedFields;
        
        CodeIterator iterator = codeAttribute.iterator();
        
        while (iterator.hasNext()) {
            int index = iterator.next();
            int opcode = iterator.byteAt(index);
            
            if (opcode == Opcode.GETFIELD || opcode == Opcode.PUTFIELD) {
                int constIndex = iterator.u16bitAt(index + 1);
                ConstPool constPool = method.getDeclaringClass().getClassFile().getConstPool();
                String fieldName = constPool.getFieldrefName(constIndex);
                accessedFields.add(fieldName);
            }
        }
        
        return accessedFields;
    }
}
```

### Principi SOLID Applicati
- **SRP**: Calculator separati per metodi, classi, distribuzioni, correlazioni
- **OCP**: Facilmente estendibile per nuove metriche di dimensione
- **LSP**: Implementazioni intercambiabili per diverse strategie di misurazione
- **ISP**: Interfacce specifiche per calculation, analysis, distribution
- **DIP**: Dipende da abstractions per bytecode analysis

### Test Unitari da Implementare
```java
// CodeSizeAnalyzerTest.java
@Test
void shouldCalculateBasicMethodSizes() {
    // Arrange
    JarContent jarContent = createJarWithVariousSizeMethods();
    CodeSizeAnalyzer analyzer = new JavassistCodeSizeAnalyzer();
    
    // Act
    CodeSizeAnalysisResult result = analyzer.analyzeCodeSize(jarContent);
    
    // Assert
    assertThat(result.isSuccessful()).isTrue();
    assertThat(result.getMethodSizes()).isNotEmpty();
    
    MethodSize smallMethod = result.findMethodSize("com.example.Utils", "simpleGetter");
    assertThat(smallMethod.getLinesOfCode()).isLessThan(10);
    assertThat(smallMethod.getSizeCategory()).isEqualTo(MethodSizeCategory.VERY_SMALL);
}

@Test
void shouldCalculateClassSizes() {
    JarContent jarContent = createJarWithVariousSizeClasses();
    CodeSizeAnalyzer analyzer = new JavassistCodeSizeAnalyzer();
    
    CodeSizeAnalysisResult result = analyzer.analyzeCodeSize(jarContent);
    
    assertThat(result.getClassSizes()).isNotEmpty();
    
    ClassSize largeClass = result.findClassSize("com.example.LargeService");
    assertThat(largeClass.getMethodCount()).isGreaterThan(20);
    assertThat(largeClass.getLinesOfCode()).isGreaterThan(500);
    assertThat(largeClass.getSizeCategory()).isIn(ClassSizeCategory.LARGE, ClassSizeCategory.VERY_LARGE);
}

@Test
void shouldIdentifySizeOutliers() {
    JarContent jarContent = createJarWithSizeOutliers();
    CodeSizeAnalyzer analyzer = new JavassistCodeSizeAnalyzer();
    
    CodeSizeAnalysisResult result = analyzer.analyzeCodeSize(jarContent);
    
    assertThat(result.getSizeOutliers()).isNotEmpty();
    
    boolean hasLargeMethodOutlier = result.getSizeOutliers().stream()
        .anyMatch(outlier -> outlier.getType() == OutlierType.LARGE_METHOD);
    assertThat(hasLargeMethodOutlier).isTrue();
}

@Test
void shouldCalculateDistributionStatistics() {
    JarContent jarContent = createJarWithNormalSizeDistribution();
    CodeSizeAnalyzer analyzer = new JavassistCodeSizeAnalyzer();
    
    CodeSizeAnalysisResult result = analyzer.analyzeCodeSize(jarContent);
    
    SizeDistributionStatistics stats = result.getDistributionStatistics();
    assertThat(stats.getMethodLoc().getMean()).isGreaterThan(0);
    assertThat(stats.getMethodLoc().getStandardDeviation()).isGreaterThan(0);
    assertThat(stats.getClassLoc().getPercentile(95)).isGreaterThan(stats.getClassLoc().getMedian());
}

// MethodSizeCalculatorTest.java
@Test
void shouldCalculateMethodLinesOfCode() {
    CtMethod method = createMethodWithKnownSize(25); // 25 lines
    MethodInfo methodInfo = createMethodInfo();
    MethodSizeCalculator calculator = new MethodSizeCalculator();
    
    MethodSize size = calculator.calculateMethodSize(methodInfo, method, "com.example.TestClass");
    
    assertThat(size.getLinesOfCode()).isCloseTo(25, Offset.offset(5)); // Allow some approximation error
}

@Test
void shouldCalculateBytecodeSize() {
    CtMethod method = createMethodWithBytecode();
    MethodInfo methodInfo = createMethodInfo();
    MethodSizeCalculator calculator = new MethodSizeCalculator();
    
    MethodSize size = calculator.calculateMethodSize(methodInfo, method, "com.example.TestClass");
    
    assertThat(size.getBytecodeSize()).isGreaterThan(0);
    assertThat(size.getInstructionCount()).isGreaterThan(0);
}

@Test
void shouldDetermineCorrectSizeCategory() {
    CtMethod smallMethod = createSmallMethod(5); // 5 lines
    CtMethod largeMethod = createLargeMethod(150); // 150 lines
    
    MethodSizeCalculator calculator = new MethodSizeCalculator();
    
    MethodSize smallSize = calculator.calculateMethodSize(createMethodInfo(), smallMethod, "TestClass");
    MethodSize largeSize = calculator.calculateMethodSize(createMethodInfo(), largeMethod, "TestClass");
    
    assertThat(smallSize.getSizeCategory()).isEqualTo(MethodSizeCategory.VERY_SMALL);
    assertThat(largeSize.getSizeCategory()).isEqualTo(MethodSizeCategory.VERY_LARGE);
}

@Test
void shouldCalculateStructuralComplexity() {
    CtMethod complexMethod = createMethodWithNesting();
    MethodSizeCalculator calculator = new MethodSizeCalculator();
    
    MethodSize size = calculator.calculateMethodSize(createMethodInfo(), complexMethod, "TestClass");
    
    StructuralComplexityMetrics structural = size.getStructuralMetrics();
    assertThat(structural.getMaxNestingDepth()).isGreaterThan(0);
    assertThat(structural.getBranchingFactor()).isGreaterThan(0);
}

// ClassSizeCalculatorTest.java
@Test
void shouldCalculateClassTotalSize() {
    CtClass classWithMultipleMethods = createClassWithMethods(10); // 10 methods
    ClassInfo classInfo = createClassInfo();
    ClassSizeCalculator calculator = new ClassSizeCalculator();
    
    ClassSize size = calculator.calculateClassSize(classInfo, classWithMultipleMethods);
    
    assertThat(size.getMethodCount()).isEqualTo(10);
    assertThat(size.getLinesOfCode()).isGreaterThan(0);
    assertThat(size.getTotalBytecodeSize()).isGreaterThan(0);
}

@Test
void shouldCalculateMethodSizeDistribution() {
    CtClass classWithVariedMethods = createClassWithVariedMethodSizes();
    ClassInfo classInfo = createClassInfo();
    ClassSizeCalculator calculator = new ClassSizeCalculator();
    
    ClassSize size = calculator.calculateClassSize(classInfo, classWithVariedMethods);
    
    MethodSizeDistribution distribution = size.getMethodSizeDistribution();
    assertThat(distribution.getMean()).isGreaterThan(0);
    assertThat(distribution.getStandardDeviation()).isGreaterThan(0);
    assertThat(distribution.getCategoryDistribution()).isNotEmpty();
}

@Test
void shouldCalculateCohesionMetrics() {
    CtClass cohesiveClass = createCohesiveClass();
    ClassInfo classInfo = createClassInfo();
    ClassSizeCalculator calculator = new ClassSizeCalculator();
    
    ClassSize size = calculator.calculateClassSize(classInfo, cohesiveClass);
    
    StructuralCohesionMetrics cohesion = size.getCohesionMetrics();
    assertThat(cohesion.getLcom()).isBetween(0.0, 1.0);
    assertThat(cohesion.getMethodToFieldRatio()).isGreaterThan(0);
}

// Integration Test
@Test
void shouldAnalyzeRealWorldCodeSizes() throws IOException {
    Path springBootJar = getTestResourcePath("spring-boot-application.jar");
    JarContent jarContent = new JarLoader().loadJar(new JarLocation(springBootJar));
    CodeSizeAnalyzer analyzer = new JavassistCodeSizeAnalyzer();
    
    CodeSizeAnalysisResult result = analyzer.analyzeCodeSize(jarContent);
    
    // Verifiche su applicazione reale
    assertThat(result.getMethodSizes()).hasSizeGreaterThan(100);
    assertThat(result.getClassSizes()).hasSizeGreaterThan(20);
    
    // Should have some size outliers
    assertThat(result.getSizeOutliers()).isNotEmpty();
    
    // Distribution statistics should be reasonable
    SizeDistributionStatistics stats = result.getDistributionStatistics();
    assertThat(stats.getMethodLoc().getMean()).isBetween(5.0, 50.0);
    assertThat(stats.getClassLoc().getMean()).isBetween(50.0, 1000.0);
}

@Test
void shouldProvideUsefulRecommendations() {
    JarContent jarContent = createJarWithLargeMethods();
    CodeSizeAnalyzer analyzer = new JavassistCodeSizeAnalyzer();
    
    CodeSizeAnalysisResult result = analyzer.analyzeCodeSize(jarContent);
    
    assertThat(result.getRecommendations()).isNotEmpty();
    
    boolean hasRefactoringRecommendation = result.getRecommendations().stream()
        .anyMatch(rec -> rec.getType() == RecommendationType.METHOD_REFACTORING);
    assertThat(hasRefactoringRecommendation).isTrue();
}
```