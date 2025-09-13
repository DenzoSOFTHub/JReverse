# Report 35: Coverage Potenziale dei Test

**Valore**: ⭐⭐⭐⭐ **Complessità**: 🔴 Complessa **Tempo**: 9-11 giorni
**Tags**: `#test-coverage` `#testing-gaps` `#quality-assurance`

## Descrizione

Il Report 35 analizza il codice per identificare il potenziale di copertura dei test, valutando quali classi, metodi e componenti potrebbero beneficiare maggiormente di test aggiuntivi. Utilizza analisi statiche per stimare la complessità del test, identificare gap di copertura e prioritizzare gli sforzi di testing.

## Obiettivo

Questo analyzer rileva e valuta:
- **Test Coverage Gaps**: Identificazione di codice non testato o sotto-testato
- **Testability Assessment**: Valutazione della facilità di testing per classi e metodi
- **Test Complexity Estimation**: Stima della complessità per implementare test appropriati
- **Testing Priority**: Prioritizzazione basata su complessità ciclomatica e valore business
- **Mock Requirement Analysis**: Identificazione di dipendenze che richiedono mocking
- **Integration Test Opportunities**: Rilevazione di scenari per test di integrazione

## Implementazione

### Analyzer Core

```java
package com.jreverse.analyzer.testing;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtField;
import javassist.CtConstructor;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.FieldAccess;
import javassist.expr.NewExpr;
import com.jreverse.analyzer.base.BaseAnalyzer;
import com.jreverse.model.analysis.TestCoverageAnalysis;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzer per valutare il potenziale di coverage dei test
 * Identifica gap di testing e prioritizza sforzi di QA
 */
public class TestCoveragePotentialAnalyzer extends BaseAnalyzer<TestCoverageAnalysis> {
    
    // Costanti per il calcolo del punteggio qualitativo (0-100)
    private static final int BASE_SCORE = 100;
    
    // Penalità per problemi di test coverage
    private static final Map<String, Integer> COVERAGE_SEVERITY_PENALTIES = Map.of(
        "NO_UNIT_TESTS", -40,                   // Classi senza unit test
        "LOW_COVERAGE_CRITICAL_CODE", -35,      // Bassa copertura su codice critico
        "UNTESTABLE_CODE", -30,                 // Codice difficile da testare
        "MISSING_INTEGRATION_TESTS", -25,       // Mancanza test integrazione
        "NO_EXCEPTION_TESTING", -20,            // Eccezioni non testate
        "COMPLEX_METHODS_UNTESTED", -30,        // Metodi complessi senza test
        "EXTERNAL_DEPENDENCIES_UNTESTED", -25,  // Dipendenze esterne non mockatc
        "DATABASE_OPERATIONS_UNTESTED", -28,    // Operazioni DB senza test
        "BUSINESS_LOGIC_GAPS", -35,             // Gap nella logica business
        "EDGE_CASES_MISSING", -22              // Test di casi limite mancanti
    );
    
    // Bonus per buone pratiche di testing
    private static final Map<String, Integer> COVERAGE_QUALITY_BONUSES = Map.of(
        "HIGH_UNIT_COVERAGE", 15,               // Alta copertura unit test
        "INTEGRATION_TESTS_PRESENT", 12,        // Test integrazione presenti
        "MOCKING_STRATEGY_IMPLEMENTED", 10,     // Strategia di mocking
        "EDGE_CASE_COVERAGE", 8,                // Copertura casi limite
        "EXCEPTION_TESTING", 10,                // Test delle eccezioni
        "PARAMETERIZED_TESTS", 8,               // Test parametrizzati
        "TEST_DATA_BUILDERS", 6,                // Builder per dati test
        "TESTABLE_ARCHITECTURE", 12,            // Architettura testabile
        "CONTINUOUS_TESTING", 10,               // Testing continuo
        "BDD_TDD_PRACTICES", 8                  // Pratiche BDD/TDD
    );
    
    // Annotazioni di test framework
    private static final Set<String> TEST_ANNOTATIONS = Set.of(
        "org.junit.Test",
        "org.junit.jupiter.api.Test",
        "org.testng.annotations.Test",
        "org.springframework.boot.test.context.SpringBootTest",
        "org.mockito.Mock",
        "org.mockito.InjectMocks"
    );
    
    // Framework di testing
    private static final Set<String> TESTING_FRAMEWORKS = Set.of(
        "org.junit",
        "org.testng",
        "org.mockito",
        "org.springframework.test",
        "org.assertj",
        "org.hamcrest"
    );
    
    // Indicatori di complessità per testing
    private static final Map<String, Integer> TESTING_COMPLEXITY_INDICATORS = Map.of(
        "EXTERNAL_API_CALLS", 3,                // Chiamate API esterne
        "DATABASE_OPERATIONS", 4,               // Operazioni database
        "FILE_OPERATIONS", 2,                   // Operazioni su file
        "THREADING_CONCURRENCY", 5,             // Concorrenza/threading
        "COMPLEX_BUSINESS_LOGIC", 4,            // Logica business complessa
        "EXTERNAL_SERVICES", 3,                 // Servizi esterni
        "STATE_MANAGEMENT", 3,                  // Gestione stato
        "ASYNC_OPERATIONS", 4                   // Operazioni asincrone
    );
    
    @Override
    public TestCoverageAnalysis analyze(CtClass[] classes) {
        TestCoverageAnalysis.Builder builder = TestCoverageAnalysis.builder();
        
        Map<String, List<CoverageGap>> coverageGaps = new HashMap<>();
        Map<String, TestabilityMetrics> classTestability = new HashMap<>();
        Map<String, List<TestCase>> testCases = new HashMap<>();
        
        // Separa classi di test da classi di produzione
        Set<String> testClasses = Arrays.stream(classes)
            .filter(this::isTestClass)
            .map(CtClass::getName)
            .collect(Collectors.toSet());
        
        for (CtClass ctClass : classes) {
            try {
                if (!isTestClass(ctClass)) {
                    TestabilityMetrics metrics = analyzeClassTestability(ctClass, classes, testClasses);
                    classTestability.put(ctClass.getName(), metrics);
                    
                    List<CoverageGap> gaps = identifyCoverageGaps(ctClass, metrics);
                    if (!gaps.isEmpty()) {
                        coverageGaps.put(ctClass.getName(), gaps);
                    }
                } else {
                    List<TestCase> cases = analyzeTestCases(ctClass);
                    if (!cases.isEmpty()) {
                        testCases.put(ctClass.getName(), cases);
                    }
                }
                
            } catch (Exception e) {
                logger.warn("Errore nell'analisi coverage per {}: {}", 
                           ctClass.getName(), e.getMessage());
            }
        }
        
        // Calcola metriche aggregate
        CoverageSummary summary = calculateCoverageSummary(classTestability, coverageGaps, testCases);
        
        return builder
            .coverageGaps(coverageGaps)
            .classTestability(classTestability)
            .testCases(testCases)
            .summary(summary)
            .qualityScore(calculateQualityScore(coverageGaps, summary))
            .recommendations(generateRecommendations(coverageGaps, summary))
            .build();
    }
    
    private boolean isTestClass(CtClass ctClass) {
        String className = ctClass.getName();
        
        // Verifica naming convention per test
        if (className.endsWith("Test") || className.endsWith("Tests") || 
            className.contains("Test") || className.startsWith("Test")) {
            return true;
        }
        
        // Verifica annotazioni di test
        try {
            for (Object annotation : ctClass.getAnnotations()) {
                String annotationType = annotation.getClass().getName();
                if (TEST_ANNOTATIONS.stream().anyMatch(annotationType::contains)) {
                    return true;
                }
            }
            
            // Verifica metodi con annotazioni test
            for (CtMethod method : ctClass.getDeclaredMethods()) {
                for (Object annotation : method.getAnnotations()) {
                    String annotationType = annotation.getClass().getName();
                    if (TEST_ANNOTATIONS.stream().anyMatch(annotationType::contains)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // Se non riusciamo a leggere annotazioni, assumiamo sia prod code
        }
        
        return false;
    }
    
    private TestabilityMetrics analyzeClassTestability(CtClass ctClass, CtClass[] allClasses, 
                                                     Set<String> testClasses) throws Exception {
        TestabilityMetrics.Builder metricsBuilder = TestabilityMetrics.builder()
            .className(ctClass.getName());
        
        // Verifica se esiste classe di test corrispondente
        String expectedTestClassName = ctClass.getName() + "Test";
        boolean hasCorrespondingTest = testClasses.contains(expectedTestClassName) ||
            testClasses.stream().anyMatch(testClass -> testClass.contains(ctClass.getSimpleName()));
        
        // Analizza complessità di testing per la classe
        TestComplexityAnalysis complexityAnalysis = analyzeTestingComplexity(ctClass);
        
        // Analizza dipendenze che richiedono mocking
        List<String> mockingCandidates = identifyMockingCandidates(ctClass);
        
        // Calcola testability score
        int testabilityScore = calculateTestabilityScore(ctClass, complexityAnalysis, mockingCandidates);
        
        // Identifica metodi che necessitano test
        List<String> methodsNeedingTests = identifyMethodsNeedingTests(ctClass, testClasses);
        
        // Analizza integrazione requirements
        List<String> integrationTestRequirements = identifyIntegrationTestRequirements(ctClass);
        
        return metricsBuilder
            .hasCorrespondingTest(hasCorrespondingTest)
            .testingComplexity(complexityAnalysis)
            .mockingCandidates(mockingCandidates)
            .testabilityScore(testabilityScore)
            .methodsNeedingTests(methodsNeedingTests)
            .integrationTestRequirements(integrationTestRequirements)
            .build();
    }
    
    private TestComplexityAnalysis analyzeTestingComplexity(CtClass ctClass) throws Exception {
        TestComplexityAnalysis.Builder analysisBuilder = TestComplexityAnalysis.builder();
        
        int totalComplexityScore = 0;
        List<String> complexityFactors = new ArrayList<>();
        Map<String, Integer> complexityByMethod = new HashMap<>();
        
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            int methodComplexity = 0;
            List<String> methodFactors = new ArrayList<>();
            
            // Analizza il corpo del metodo per fattori di complessità
            method.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall call) throws Exception {
                    String className = call.getClassName();
                    String methodName = call.getMethodName();
                    
                    // Identifica chiamate a servizi esterni
                    if (className.contains("Service") || className.contains("Repository") ||
                        className.contains("Client") || className.contains("Dao")) {
                        methodFactors.add("EXTERNAL_SERVICES");
                        analysisBuilder.complexity(analysisBuilder.build().getComplexity() + 
                                                 TESTING_COMPLEXITY_INDICATORS.get("EXTERNAL_SERVICES"));
                    }
                    
                    // Identifica operazioni database
                    if (className.contains("Entity") || methodName.contains("save") ||
                        methodName.contains("find") || methodName.contains("delete")) {
                        methodFactors.add("DATABASE_OPERATIONS");
                        analysisBuilder.complexity(analysisBuilder.build().getComplexity() + 
                                                 TESTING_COMPLEXITY_INDICATORS.get("DATABASE_OPERATIONS"));
                    }
                    
                    // Identifica operazioni asincrone
                    if (methodName.contains("async") || methodName.contains("Async") ||
                        className.contains("CompletableFuture") || className.contains("Future")) {
                        methodFactors.add("ASYNC_OPERATIONS");
                        analysisBuilder.complexity(analysisBuilder.build().getComplexity() + 
                                                 TESTING_COMPLEXITY_INDICATORS.get("ASYNC_OPERATIONS"));
                    }
                }
                
                @Override
                public void edit(NewExpr expr) throws Exception {
                    String className = expr.getClassName();
                    if (className.contains("Thread") || className.contains("Executor")) {
                        methodFactors.add("THREADING_CONCURRENCY");
                        analysisBuilder.complexity(analysisBuilder.build().getComplexity() + 
                                                 TESTING_COMPLEXITY_INDICATORS.get("THREADING_CONCURRENCY"));
                    }
                }
            });
            
            // Calcola complessità ciclomatica approssimata
            String methodBody = method.getMethodInfo().getCodeAttribute().toString();
            long branchingStatements = methodBody.chars()
                .filter(ch -> methodBody.contains("if") || methodBody.contains("switch") || 
                             methodBody.contains("while") || methodBody.contains("for"))
                .count();
            
            methodComplexity += (int) branchingStatements;
            complexityByMethod.put(method.getName(), methodComplexity + methodFactors.size());
            
            totalComplexityScore += methodComplexity;
            complexityFactors.addAll(methodFactors);
        }
        
        return analysisBuilder
            .complexity(totalComplexityScore)
            .complexityFactors(complexityFactors.stream().distinct().collect(Collectors.toList()))
            .complexityByMethod(complexityByMethod)
            .build();
    }
    
    private List<String> identifyMockingCandidates(CtClass ctClass) throws Exception {
        List<String> mockingCandidates = new ArrayList<>();
        
        // Analizza campi per dipendenze
        for (CtField field : ctClass.getDeclaredFields()) {
            String fieldType = field.getType().getName();
            
            // Identifica servizi/repository che necessitano mocking
            if (fieldType.contains("Service") || fieldType.contains("Repository") ||
                fieldType.contains("Client") || fieldType.contains("Dao")) {
                mockingCandidates.add(field.getName() + " : " + fieldType);
            }
            
            // Identifica componenti Spring
            try {
                for (Object annotation : field.getAnnotations()) {
                    String annotationType = annotation.getClass().getSimpleName();
                    if (annotationType.contains("Autowired") || annotationType.contains("Inject")) {
                        mockingCandidates.add(field.getName() + " : " + fieldType + " (Injected)");
                        break;
                    }
                }
            } catch (Exception e) {
                // Ignora errori di lettura annotazioni
            }
        }
        
        // Analizza costruttori per dependency injection
        for (CtConstructor constructor : ctClass.getConstructors()) {
            try {
                CtClass[] paramTypes = constructor.getParameterTypes();
                for (int i = 0; i < paramTypes.length; i++) {
                    String paramType = paramTypes[i].getName();
                    if (paramType.contains("Service") || paramType.contains("Repository")) {
                        mockingCandidates.add("Constructor param " + i + " : " + paramType);
                    }
                }
            } catch (Exception e) {
                // Ignora errori di lettura parametri
            }
        }
        
        return mockingCandidates.stream().distinct().collect(Collectors.toList());
    }
    
    private int calculateTestabilityScore(CtClass ctClass, TestComplexityAnalysis complexityAnalysis,
                                        List<String> mockingCandidates) {
        int score = 100;
        
        // Penalizza alta complessità
        if (complexityAnalysis.getComplexity() > 20) {
            score -= 25;
        } else if (complexityAnalysis.getComplexity() > 10) {
            score -= 15;
        }
        
        // Penalizza molte dipendenze da mockare
        if (mockingCandidates.size() > 5) {
            score -= 20;
        } else if (mockingCandidates.size() > 3) {
            score -= 10;
        }
        
        // Bonus per classi più semplici da testare
        if (complexityAnalysis.getComplexity() < 5 && mockingCandidates.size() < 2) {
            score += 15;
        }
        
        // Verifica se è una classe utility (più facile da testare)
        try {
            if (Modifier.isFinal(ctClass.getModifiers()) && 
                Arrays.stream(ctClass.getDeclaredMethods()).allMatch(m -> Modifier.isStatic(m.getModifiers()))) {
                score += 10; // Utility classes sono più facili da testare
            }
        } catch (Exception e) {
            // Ignora errori
        }
        
        return Math.max(0, Math.min(100, score));
    }
    
    private List<String> identifyMethodsNeedingTests(CtClass ctClass, Set<String> testClasses) throws Exception {
        List<String> methodsNeedingTests = new ArrayList<>();
        
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            // Salta metodi privati e getter/setter semplici
            if (Modifier.isPrivate(method.getModifiers()) || 
                isSimpleGetterSetter(method)) {
                continue;
            }
            
            // Verifica se il metodo ha già test
            boolean hasTest = testClasses.stream().anyMatch(testClass -> 
                // Logica semplificata: assumiamo che esistano test se c'è una classe test
                testClass.contains(ctClass.getSimpleName())
            );
            
            if (!hasTest) {
                String methodSignature = method.getName() + "(" + 
                    Arrays.stream(method.getParameterTypes())
                          .map(CtClass::getSimpleName)
                          .collect(Collectors.joining(", ")) + ")";
                methodsNeedingTests.add(methodSignature);
            }
        }
        
        return methodsNeedingTests;
    }
    
    private boolean isSimpleGetterSetter(CtMethod method) {
        String methodName = method.getName();
        try {
            // Getter semplice
            if (methodName.startsWith("get") && method.getParameterTypes().length == 0) {
                return true;
            }
            // Setter semplice
            if (methodName.startsWith("set") && method.getParameterTypes().length == 1) {
                return true;
            }
            // Boolean getter
            if (methodName.startsWith("is") && method.getParameterTypes().length == 0) {
                return true;
            }
        } catch (Exception e) {
            // In caso di errore, assumiamo non sia un getter/setter semplice
        }
        return false;
    }
    
    private List<String> identifyIntegrationTestRequirements(CtClass ctClass) throws Exception {
        List<String> requirements = new ArrayList<>();
        
        // Verifica se è un controller
        try {
            for (Object annotation : ctClass.getAnnotations()) {
                String annotationType = annotation.getClass().getSimpleName();
                if (annotationType.contains("Controller") || annotationType.contains("RestController")) {
                    requirements.add("REST API Integration Tests - MockMvc testing required");
                    break;
                }
            }
        } catch (Exception e) {
            // Ignora errori di lettura annotazioni
        }
        
        // Verifica se ha dipendenze database
        for (CtField field : ctClass.getDeclaredFields()) {
            String fieldType = field.getType().getName();
            if (fieldType.contains("Repository") || fieldType.contains("EntityManager")) {
                requirements.add("Database Integration Tests - @DataJpaTest required");
                break;
            }
        }
        
        // Verifica se è un service con chiamate esterne
        if (ctClass.getName().contains("Service")) {
            requirements.add("Service Integration Tests - @SpringBootTest with test containers");
        }
        
        return requirements.stream().distinct().collect(Collectors.toList());
    }
    
    private List<TestCase> analyzeTestCases(CtClass testClass) throws Exception {
        List<TestCase> testCases = new ArrayList<>();
        
        for (CtMethod method : testClass.getDeclaredMethods()) {
            // Verifica se è un metodo di test
            boolean isTestMethod = false;
            try {
                for (Object annotation : method.getAnnotations()) {
                    String annotationType = annotation.getClass().getName();
                    if (TEST_ANNOTATIONS.stream().anyMatch(annotationType::contains)) {
                        isTestMethod = true;
                        break;
                    }
                }
            } catch (Exception e) {
                // Se non riusciamo a leggere annotazioni, verifichiamo il nome
                if (method.getName().startsWith("test") || method.getName().endsWith("Test")) {
                    isTestMethod = true;
                }
            }
            
            if (isTestMethod) {
                TestCase testCase = TestCase.builder()
                    .testClassName(testClass.getName())
                    .testMethodName(method.getName())
                    .complexity(estimateTestComplexity(method))
                    .coverageType(determineTestCoverageType(method))
                    .build();
                
                testCases.add(testCase);
            }
        }
        
        return testCases;
    }
    
    private int estimateTestComplexity(CtMethod testMethod) throws Exception {
        int complexity = 1; // Base complexity
        
        // Conta le assertion (approssimativamente)
        String methodBody = testMethod.getMethodInfo().toString();
        long assertions = methodBody.chars()
            .filter(ch -> methodBody.contains("assert") || methodBody.contains("verify"))
            .count();
        complexity += (int) assertions;
        
        // Conta setup/teardown
        if (methodBody.contains("setUp") || methodBody.contains("tearDown") || 
            methodBody.contains("@Before") || methodBody.contains("@After")) {
            complexity += 2;
        }
        
        return complexity;
    }
    
    private String determineTestCoverageType(CtMethod testMethod) {
        String methodName = testMethod.getName().toLowerCase();
        
        if (methodName.contains("integration")) {
            return "INTEGRATION";
        } else if (methodName.contains("unit")) {
            return "UNIT";
        } else if (methodName.contains("end") && methodName.contains("end")) {
            return "E2E";
        } else {
            return "UNIT"; // Default assumption
        }
    }
    
    private List<CoverageGap> identifyCoverageGaps(CtClass ctClass, TestabilityMetrics metrics) {
        List<CoverageGap> gaps = new ArrayList<>();
        String className = ctClass.getName();
        
        // Gap 1: Classe senza test
        if (!metrics.isHasCorrespondingTest()) {
            gaps.add(CoverageGap.builder()
                .className(className)
                .gapType("NO_UNIT_TESTS")
                .severity("ALTA")
                .description("Classe senza unit test corrispondenti")
                .location("Class level")
                .testabilityScore(metrics.getTestabilityScore())
                .estimatedEffort(estimateTestingEffort(metrics))
                .recommendation("Creare classe di test " + ctClass.getSimpleName() + "Test con coverage minimo 80%")
                .build());
        }
        
        // Gap 2: Metodi complessi non testati
        if (!metrics.getMethodsNeedingTests().isEmpty() && 
            metrics.getTestingComplexity().getComplexity() > 15) {
            gaps.add(CoverageGap.builder()
                .className(className)
                .gapType("COMPLEX_METHODS_UNTESTED")
                .severity("CRITICA")
                .description(String.format("%d metodi complessi senza test appropriati", 
                           metrics.getMethodsNeedingTests().size()))
                .location(String.join(", ", metrics.getMethodsNeedingTests()))
                .testabilityScore(metrics.getTestabilityScore())
                .estimatedEffort(estimateTestingEffort(metrics))
                .recommendation("Prioritizzare test per metodi con alta complessità ciclomatica")
                .build());
        }
        
        // Gap 3: Dipendenze esterne non mocktate
        if (!metrics.getMockingCandidates().isEmpty()) {
            gaps.add(CoverageGap.builder()
                .className(className)
                .gapType("EXTERNAL_DEPENDENCIES_UNTESTED")
                .severity("MEDIA")
                .description(String.format("%d dipendenze esterne richiedono mocking", 
                           metrics.getMockingCandidates().size()))
                .location(String.join(", ", metrics.getMockingCandidates()))
                .testabilityScore(metrics.getTestabilityScore())
                .estimatedEffort(estimateTestingEffort(metrics))
                .recommendation("Implementare mocking strategy per dipendenze esterne")
                .build());
        }
        
        // Gap 4: Test di integrazione mancanti
        if (!metrics.getIntegrationTestRequirements().isEmpty()) {
            gaps.add(CoverageGap.builder()
                .className(className)
                .gapType("MISSING_INTEGRATION_TESTS")
                .severity("ALTA")
                .description("Test di integrazione richiesti ma mancanti")
                .location(String.join(", ", metrics.getIntegrationTestRequirements()))
                .testabilityScore(metrics.getTestabilityScore())
                .estimatedEffort(estimateTestingEffort(metrics))
                .recommendation("Implementare test di integrazione per componenti architetturali")
                .build());
        }
        
        return gaps;
    }
    
    private String estimateTestingEffort(TestabilityMetrics metrics) {
        int complexity = metrics.getTestingComplexity().getComplexity();
        int mockingNeeded = metrics.getMockingCandidates().size();
        int methodsToTest = metrics.getMethodsNeedingTests().size();
        
        int totalEffortPoints = complexity + (mockingNeeded * 2) + methodsToTest;
        
        if (totalEffortPoints > 30) {
            return "ALTO (8-12 giorni)";
        } else if (totalEffortPoints > 15) {
            return "MEDIO (4-7 giorni)";
        } else {
            return "BASSO (1-3 giorni)";
        }
    }
    
    private CoverageSummary calculateCoverageSummary(Map<String, TestabilityMetrics> classTestability,
                                                   Map<String, List<CoverageGap>> coverageGaps,
                                                   Map<String, List<TestCase>> testCases) {
        
        int totalProductionClasses = classTestability.size();
        int classesWithTests = (int) classTestability.values().stream()
            .filter(TestabilityMetrics::isHasCorrespondingTest)
            .count();
        
        int totalTestCases = testCases.values().stream()
            .mapToInt(List::size)
            .sum();
        
        int totalGaps = coverageGaps.values().stream()
            .mapToInt(List::size)
            .sum();
        
        Map<String, Long> gapsBySeverity = coverageGaps.values().stream()
            .flatMap(List::stream)
            .collect(Collectors.groupingBy(CoverageGap::getSeverity, Collectors.counting()));
        
        // Calcola coverage ratio stimato
        double estimatedCoverageRatio = totalProductionClasses > 0 ? 
            (double) classesWithTests / totalProductionClasses * 100 : 0.0;
        
        // Calcola testability score medio
        double averageTestabilityScore = classTestability.values().stream()
            .mapToDouble(TestabilityMetrics::getTestabilityScore)
            .average()
            .orElse(0.0);
        
        // Identifica classi ad alta priorità per testing
        List<String> highPriorityClasses = classTestability.entrySet().stream()
            .filter(entry -> !entry.getValue().isHasCorrespondingTest() && 
                            entry.getValue().getTestingComplexity().getComplexity() > 10)
            .map(Map.Entry::getKey)
            .limit(10)
            .collect(Collectors.toList());
        
        return CoverageSummary.builder()
            .totalProductionClasses(totalProductionClasses)
            .classesWithTests(classesWithTests)
            .totalTestCases(totalTestCases)
            .totalGaps(totalGaps)
            .gapsBySeverity(gapsBySeverity)
            .estimatedCoverageRatio(estimatedCoverageRatio)
            .averageTestabilityScore(averageTestabilityScore)
            .highPriorityClasses(highPriorityClasses)
            .build();
    }
    
    private int calculateQualityScore(Map<String, List<CoverageGap>> coverageGaps, CoverageSummary summary) {
        int score = BASE_SCORE;
        
        // Applica penalità per gap identificati
        for (List<CoverageGap> gaps : coverageGaps.values()) {
            for (CoverageGap gap : gaps) {
                String gapType = gap.getGapType();
                Integer penalty = COVERAGE_SEVERITY_PENALTIES.get(gapType);
                if (penalty != null) {
                    score += penalty; // penalty è già negativa
                }
            }
        }
        
        // Applica bonus per buone pratiche
        if (summary.getEstimatedCoverageRatio() > 80) {
            score += COVERAGE_QUALITY_BONUSES.get("HIGH_UNIT_COVERAGE");
        } else if (summary.getEstimatedCoverageRatio() > 60) {
            score += COVERAGE_QUALITY_BONUSES.get("HIGH_UNIT_COVERAGE") / 2;
        }
        
        if (summary.getTotalTestCases() > summary.getTotalProductionClasses()) {
            score += COVERAGE_QUALITY_BONUSES.get("TESTABLE_ARCHITECTURE");
        }
        
        if (summary.getAverageTestabilityScore() > 75) {
            score += COVERAGE_QUALITY_BONUSES.get("CONTINUOUS_TESTING");
        }
        
        return Math.max(0, Math.min(100, score));
    }
    
    private List<String> generateRecommendations(Map<String, List<CoverageGap>> coverageGaps,
                                               CoverageSummary summary) {
        List<String> recommendations = new ArrayList<>();
        
        if (summary.getEstimatedCoverageRatio() < 60) {
            recommendations.add("🎯 **Coverage Improvement**: Aumentare coverage al 80% partendo dalle classi ad alta priorità business");
        }
        
        if (!summary.getHighPriorityClasses().isEmpty()) {
            recommendations.add("⚡ **Priority Testing**: " + summary.getHighPriorityClasses().size() + 
                              " classi complesse richiedono test immediati");
        }
        
        long criticalGaps = summary.getGapsBySeverity().getOrDefault("CRITICA", 0L);
        if (criticalGaps > 0) {
            recommendations.add("🚨 **Critical Gap Resolution**: " + criticalGaps + 
                              " gap critici richiedono attenzione immediata");
        }
        
        if (summary.getAverageTestabilityScore() < 60) {
            recommendations.add("🏗️ **Architecture Refactoring**: Migliorare testability attraverso dependency injection e SOLID principles");
        }
        
        if (summary.getTotalTestCases() < summary.getTotalProductionClasses() * 0.5) {
            recommendations.add("🧪 **Test Suite Expansion**: Implementare strategia di testing sistematica con TDD/BDD");
        }
        
        return recommendations;
    }
}
```

### Modelli Dati

```java
/**
 * Risultato dell'analisi coverage potenziale
 */
@Data
@Builder
public class TestCoverageAnalysis {
    private Map<String, List<CoverageGap>> coverageGaps;
    private Map<String, TestabilityMetrics> classTestability;
    private Map<String, List<TestCase>> testCases;
    private CoverageSummary summary;
    private int qualityScore;
    private List<String> recommendations;
}

/**
 * Metriche di testability per classe
 */
@Data
@Builder
public class TestabilityMetrics {
    private String className;
    private boolean hasCorrespondingTest;
    private TestComplexityAnalysis testingComplexity;
    private List<String> mockingCandidates;
    private int testabilityScore;
    private List<String> methodsNeedingTests;
    private List<String> integrationTestRequirements;
}

/**
 * Analisi complessità testing
 */
@Data
@Builder
public class TestComplexityAnalysis {
    private int complexity;
    private List<String> complexityFactors;
    private Map<String, Integer> complexityByMethod;
}

/**
 * Gap di copertura identificato
 */
@Data
@Builder
public class CoverageGap {
    private String className;
    private String gapType;
    private String severity;
    private String description;
    private String location;
    private int testabilityScore;
    private String estimatedEffort;
    private String recommendation;
}

/**
 * Caso di test esistente
 */
@Data
@Builder
public class TestCase {
    private String testClassName;
    private String testMethodName;
    private int complexity;
    private String coverageType;
}

/**
 * Summary dell'analisi coverage
 */
@Data
@Builder
public class CoverageSummary {
    private int totalProductionClasses;
    private int classesWithTests;
    private int totalTestCases;
    private int totalGaps;
    private Map<String, Long> gapsBySeverity;
    private double estimatedCoverageRatio;
    private double averageTestabilityScore;
    private List<String> highPriorityClasses;
}
```

## Metriche di Qualità del Codice

### Punteggio Qualitativo (0-100)

Il sistema calcola un punteggio qualitativo basato su:

**Penalità Applicate (-175 punti massimi):**
- 🔴 **CRITICA** (-40): Nessun unit test, logica business non testata
- 🟠 **ALTA** (-35): Bassa copertura su codice critico, test integrazione mancanti
- 🟡 **MEDIA** (-30): Codice difficile da testare, dipendenze non mocktate  
- 🔵 **BASSA** (-25): Casi limite non testati, eccezioni non coperte

**Bonus Assegnati (+95 punti massimi):**
- ✅ **High Unit Coverage** (+15): Copertura unit test > 80%
- ✅ **Testable Architecture** (+12): Architettura favorisce testing
- ✅ **Integration Tests** (+12): Test integrazione implementati
- ✅ **Continuous Testing** (+10): Pratiche testing continue

### Soglie di Qualità

- **🟢 ECCELLENTE (90-100)**: Coverage > 80%, architettura testabile
- **🔵 BUONA (75-89)**: Coverage 60-80%, alcuni gap minori
- **🟡 ACCETTABILE (60-74)**: Coverage 40-60%, gap significativi
- **🟠 SCARSA (40-59)**: Coverage < 40%, molti gap critici
- **🔴 CRITICA (0-39)**: Coverage < 20%, logica business non testata

### Esempio Output HTML

```html
<div class="test-coverage-analysis">
    <div class="quality-score score-72">
        <h3>🧪 Test Coverage Quality Score: 72/100</h3>
        <div class="score-bar">
            <div class="score-fill" style="width: 72%"></div>
        </div>
        <span class="score-label">🟡 ACCETTABILE</span>
    </div>
    
    <div class="coverage-metrics">
        <div class="metric-card">
            <h4>📊 Coverage Metrics</h4>
            <ul>
                <li><strong>Classi con Test:</strong> 28/45 (62.2%)</li>
                <li><strong>Coverage Stimata:</strong> 58.7%</li>
                <li><strong>Test Cases Totali:</strong> 142</li>
                <li><strong>Testability Score Medio:</strong> 68.5/100</li>
                <li><strong>Gap Critici:</strong> 8</li>
            </ul>
        </div>
    </div>
    
    <div class="high-priority-classes">
        <h4>⚡ Classi Prioritarie per Testing</h4>
        <ol>
            <li><strong>PaymentService:</strong> Complexity: 25, No tests</li>
            <li><strong>OrderProcessor:</strong> Complexity: 22, Partial coverage</li>
            <li><strong>UserSecurityService:</strong> Complexity: 18, Integration tests missing</li>
        </ol>
    </div>
    
    <div class="coverage-gaps-by-severity">
        <h4>🔍 Gap per Gravità</h4>
        <div class="severity-breakdown">
            <span class="severity-critical">🔴 CRITICA: 3</span>
            <span class="severity-high">🟠 ALTA: 8</span>
            <span class="severity-medium">🟡 MEDIA: 12</span>
            <span class="severity-low">🔵 BASSA: 6</span>
        </div>
    </div>
    
    <div class="testing-effort-estimation">
        <h4>⏱️ Stima Sforzi Testing</h4>
        <ul>
            <li><strong>ALTO:</strong> 5 classi (8-12 giorni ciascuna)</li>
            <li><strong>MEDIO:</strong> 12 classi (4-7 giorni ciascuna)</li>
            <li><strong>BASSO:</strong> 18 classi (1-3 giorni ciascuna)</li>
        </ul>
    </div>
    
    <div class="recommendations">
        <h4>💡 Raccomandazioni Testing</h4>
        <ol>
            <li><strong>🎯 Priority Classes:</strong> Focus su PaymentService, OrderProcessor (logica business critica)</li>
            <li><strong>🏗️ Architecture Refactoring:</strong> Migliorare dependency injection per testability</li>
            <li><strong>🧪 Integration Tests:</strong> Implementare @SpringBootTest per componenti architetturali</li>
            <li><strong>🚨 Critical Gaps:</strong> 3 gap critici richiedono risoluzione immediata</li>
        </ol>
    </div>
</div>
```

### Metriche Business Value

**Impatto Qualità:**
- **Bug Reduction**: Coverage 80%+ riduce bug produzione del 60-75%
- **Regression Prevention**: Test suite completa previene 85% regressioni  
- **Refactoring Safety**: Coverage alta abilita refactoring sicuri
- **Documentation Value**: Test servono come documentazione vivente

**Costi di Manutenzione:**
- **Debug Time Reduction**: Coverage alta riduce tempo debugging del 50%
- **QA Cycles**: Test automatici riducono cicli QA manuali del 70%
- **Production Issues**: Coverage appropriata riduce issue produzione del 80%
- **Team Confidence**: Test suite robusta aumenta velocità sviluppo del 40%

**ROI Stimato per Testing Investment:**
- **Unit Tests Implementation**: 15-25 giorni → 60% riduzione bug
- **Integration Tests**: 8-12 giorni → 85% riduzione integration issues
- **Test Architecture Improvement**: 5-8 giorni → 40% velocità sviluppo
- **Mocking Strategy**: 3-5 giorni → Testability score +30 punti

### Priorità di Intervento

1. **🚨 URGENTE**: Logica business senza test, gap coverage critici
2. **⚠️ ALTA**: Classi complesse non testate, integration test mancanti
3. **📋 MEDIA**: Dipendenze non mocktate, testability architecture
4. **🔧 BASSA**: Edge cases, test parametrizzati, documentation

La qualità del coverage test è fondamentale per la manutenibilità e affidabilità del software.