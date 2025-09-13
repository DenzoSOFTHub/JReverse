# Report 31: Metriche di Complessità Ciclomatica

**Valore**: ⭐⭐⭐⭐ **Complessità**: 🟡 Media **Tempo**: 6-8 giorni
**Tags**: `#cyclomatic-complexity` `#code-quality` `#metrics` `#maintainability`

## Descrizione

Calcola la complessità ciclomatica di McCabe per tutti i metodi dell'applicazione, identificando metodi con alta complessità che necessitano refactoring per migliorare maintainability, testability e comprensibilità del codice.

## Sezioni del Report

### 1. Complexity Overview
- Distribuzione della complessità per classe e metodo
- Metodi con complessità critica (>15)
- Trend di complessità nel tempo
- Hotspot di complessità

### 2. Method-Level Analysis
- Top metodi per complessità ciclomatica
- Analisi path attraverso il codice
- Nested control structures
- Complexity density per classe

### 3. Class-Level Metrics
- Average complexity per classe
- Maximum complexity per classe
- Variance nella complessità dei metodi
- Classes con alta complexity dispersion

### 4. Refactoring Recommendations
- Metodi candidati per split
- Extract method opportunities
- Simplification strategies
- Priority refactoring list

## Implementazione con Javassist

```java
public class CyclomaticComplexityAnalyzer {
    
    public CyclomaticComplexityReport analyzeCyclomaticComplexity(CtClass[] classes) {
        CyclomaticComplexityReport report = new CyclomaticComplexityReport();
        
        for (CtClass ctClass : classes) {
            analyzeClassComplexity(ctClass, report);
        }
        
        calculateComplexityStatistics(report);
        generateRefactoringRecommendations(report);
        
        return report;
    }
    
    private void analyzeClassComplexity(CtClass ctClass, CyclomaticComplexityReport report) {
        try {
            ClassComplexityInfo classInfo = new ClassComplexityInfo();
            classInfo.setClassName(ctClass.getName());
            
            CtMethod[] methods = ctClass.getDeclaredMethods();
            int totalComplexity = 0;
            int maxComplexity = 0;
            List<MethodComplexityInfo> methodInfos = new ArrayList<>();
            
            for (CtMethod method : methods) {
                MethodComplexityInfo methodInfo = analyzeMethodComplexity(method);
                methodInfos.add(methodInfo);
                
                totalComplexity += methodInfo.getCyclomaticComplexity();
                maxComplexity = Math.max(maxComplexity, methodInfo.getCyclomaticComplexity());
                
                // Se complessità alta, aggiungi alla lista hotspots
                if (methodInfo.getCyclomaticComplexity() > 15) {
                    ComplexityHotspot hotspot = new ComplexityHotspot();
                    hotspot.setClassName(ctClass.getName());
                    hotspot.setMethodName(method.getName());
                    hotspot.setComplexity(methodInfo.getCyclomaticComplexity());
                    hotspot.setSeverity(determineComplexitySeverity(methodInfo.getCyclomaticComplexity()));
                    
                    report.addComplexityHotspot(hotspot);
                }
            }
            
            classInfo.setMethods(methodInfos);
            classInfo.setTotalComplexity(totalComplexity);
            classInfo.setAverageComplexity(methods.length > 0 ? (double) totalComplexity / methods.length : 0);
            classInfo.setMaxComplexity(maxComplexity);
            classInfo.setMethodCount(methods.length);
            
            report.addClassComplexityInfo(classInfo);
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi complexity: " + e.getMessage());
        }
    }
    
    private MethodComplexityInfo analyzeMethodComplexity(CtMethod method) {
        MethodComplexityInfo info = new MethodComplexityInfo();
        info.setMethodName(method.getName());
        info.setMethodSignature(method.getSignature());
        
        try {
            // Calcola complessità ciclomatica
            int complexity = calculateCyclomaticComplexity(method);
            info.setCyclomaticComplexity(complexity);
            
            // Analizza nested structures
            int nestedDepth = calculateMaxNestedDepth(method);
            info.setMaxNestedDepth(nestedDepth);
            
            // Conta decision points
            int decisionPoints = countDecisionPoints(method);
            info.setDecisionPoints(decisionPoints);
            
            // Analizza control flow paths
            int pathCount = estimatePathCount(method);
            info.setEstimatedPaths(pathCount);
            
        } catch (Exception e) {
            info.setCyclomaticComplexity(1); // Default complexity
            info.addAnalysisError("Error calculating complexity: " + e.getMessage());
        }
        
        return info;
    }
    
    private int calculateCyclomaticComplexity(CtMethod method) {
        int complexity = 1; // Base complexity
        
        try {
            method.instrument(new ExprEditor() {
                private int complexityPoints = 0;
                
                @Override
                public void edit(MethodCall call) throws CannotCompileException {
                    // Non aggiunge complessità per method calls
                }
                
                @Override 
                public void edit(FieldAccess access) throws CannotCompileException {
                    // Non aggiunge complessità per field access
                }
                
                // Nota: Javassist ha limitazioni nell'analisi del control flow
                // Questa è una semplificazione. Un'implementazione completa 
                // richiederebbe l'analisi dell'AST o bytecode più dettagliata
            });
            
            // Implementazione semplificata: conta keywords di controllo nel body
            String methodBody = getMethodBody(method);
            complexity += countControlFlowKeywords(methodBody);
            
        } catch (Exception e) {
            // Fallback to method signature analysis
            complexity = estimateComplexityFromSignature(method);
        }
        
        return Math.max(1, complexity);
    }
    
    private int countControlFlowKeywords(String methodBody) {
        int count = 0;
        String[] controlKeywords = {
            "if", "else", "while", "for", "switch", "case", 
            "catch", "&&", "||", "?", "break", "continue"
        };
        
        for (String keyword : controlKeywords) {
            count += countOccurrences(methodBody, keyword);
        }
        
        return count;
    }
    
    private void calculateComplexityStatistics(CyclomaticComplexityReport report) {
        List<ClassComplexityInfo> classes = report.getClassComplexityInfos();
        
        if (classes.isEmpty()) return;
        
        ComplexityStatistics stats = new ComplexityStatistics();
        
        // Calcola statistiche globali
        int totalMethods = 0;
        int totalComplexity = 0;
        int maxComplexity = 0;
        List<Integer> allComplexities = new ArrayList<>();
        
        for (ClassComplexityInfo classInfo : classes) {
            totalMethods += classInfo.getMethodCount();
            totalComplexity += classInfo.getTotalComplexity();
            maxComplexity = Math.max(maxComplexity, classInfo.getMaxComplexity());
            
            for (MethodComplexityInfo methodInfo : classInfo.getMethods()) {
                allComplexities.add(methodInfo.getCyclomaticComplexity());
            }
        }
        
        stats.setTotalMethods(totalMethods);
        stats.setTotalComplexity(totalComplexity);
        stats.setAverageComplexity(totalMethods > 0 ? (double) totalComplexity / totalMethods : 0);
        stats.setMaxComplexity(maxComplexity);
        
        // Calcola distribuzione
        Map<String, Integer> distribution = calculateComplexityDistribution(allComplexities);
        stats.setComplexityDistribution(distribution);
        
        report.setComplexityStatistics(stats);
    }
    
    private Map<String, Integer> calculateComplexityDistribution(List<Integer> complexities) {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        
        distribution.put("1-5 (Low)", (int) complexities.stream().filter(c -> c <= 5).count());
        distribution.put("6-10 (Moderate)", (int) complexities.stream().filter(c -> c > 5 && c <= 10).count());
        distribution.put("11-15 (High)", (int) complexities.stream().filter(c -> c > 10 && c <= 15).count());
        distribution.put("16-20 (Very High)", (int) complexities.stream().filter(c -> c > 15 && c <= 20).count());
        distribution.put("21+ (Extreme)", (int) complexities.stream().filter(c -> c > 20).count());
        
        return distribution;
    }
}

public class CyclomaticComplexityReport {
    private List<ClassComplexityInfo> classComplexityInfos = new ArrayList<>();
    private List<ComplexityHotspot> complexityHotspots = new ArrayList<>();
    private ComplexityStatistics complexityStatistics;
    private List<RefactoringRecommendation> refactoringRecommendations = new ArrayList<>();
    private List<String> errors = new ArrayList<>();
    
    public static class MethodComplexityInfo {
        private String methodName;
        private String methodSignature;
        private int cyclomaticComplexity;
        private int maxNestedDepth;
        private int decisionPoints;
        private int estimatedPaths;
        private List<String> analysisErrors = new ArrayList<>();
    }
    
    public static class ComplexityHotspot {
        private String className;
        private String methodName;
        private int complexity;
        private String severity;
        private String recommendation;
    }
    
    public static class ComplexityStatistics {
        private int totalMethods;
        private int totalComplexity;
        private double averageComplexity;
        private int maxComplexity;
        private Map<String, Integer> complexityDistribution = new LinkedHashMap<>();
    }
}
```

## Raccolta Dati

### 1. Method Analysis
- Calcolo complessità ciclomatica per ogni metodo
- Analisi nested control structures
- Conteggio decision points e branch points
- Stima numero di path attraverso il metodo

### 2. Class Aggregation  
- Total complexity per classe
- Average e max complexity per classe
- Distribuzione della complessità nei metodi
- Variance e standard deviation

### 3. Application Overview
- Distribuzione globale della complessità
- Top methods/classes per complessità
- Trend analysis nel tempo
- Hotspot identification

### 4. Quality Assessment
- Percentage metodi con alta complessità
- Technical debt derivante dalla complessità
- Maintenance effort estimation
- Testing effort correlation

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculateComplexityQualityScore(CyclomaticComplexityReport result) {
    double score = 100.0;
    
    // Penalizzazioni per alta complessità
    score -= result.getMethodsWithComplexity20Plus() * 25;     // -25 per metodo complessità 20+
    score -= result.getMethodsWithComplexity15To19() * 15;     // -15 per metodo complessità 15-19
    score -= result.getMethodsWithComplexity11To14() * 8;      // -8 per metodo complessità 11-14
    score -= result.getMethodsWithComplexity6To10() * 3;       // -3 per metodo complessità 6-10
    score -= result.getClassesWithHighAvgComplexity() * 10;    // -10 per classe con media alta
    score -= result.getDeepNestedMethods() * 12;               // -12 per metodi con nesting profondo
    
    // Bonus per buona struttura
    score += result.getMethodsWithLowComplexity() * 1;         // +1 per metodo semplice (1-5)
    score += result.getClassesWithGoodComplexityDistribution() * 2; // +2 per classe ben bilanciata
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - Codice estremamente complesso, difficile da mantenere
- **41-60**: 🟡 SUFFICIENTE - Complessità moderata ma necessita semplificazione  
- **61-80**: 🟢 BUONO - Complessità gestibile con alcuni metodi da refactorare
- **81-100**: ⭐ ECCELLENTE - Codice semplice e ben strutturato

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -20 to -25)
1. **Metodi con complessità ciclomatica >20**
   - Descrizione: Metodi estremamente complessi con molti path di esecuzione
   - Rischio: Bug difficili da trovare, testing incompleto, manutenzione costosa
   - Soluzione: Refactoring urgente, spezzare in metodi più piccoli

2. **Classi con complessità media >15**  
   - Descrizione: Classi con metodi mediamente molto complessi
   - Rischio: Modulo difficile da comprendere e modificare
   - Soluzione: Redesign architetturale, separazione responsabilità

### 🟠 GRAVITÀ ALTA (Score Impact: -12 to -15)
3. **Metodi con complessità 15-19**
   - Descrizione: Metodi ad alta complessità che superano soglie raccomandate
   - Rischio: Difficoltà testing, probabilità errori elevata
   - Soluzione: Extract method, semplificare logica condizionale

4. **Nesting eccessivo (>4 livelli)**
   - Descrizione: Strutture di controllo annidate troppo profondamente  
   - Rischio: Codice illeggibile, logica difficile da seguire
   - Soluzione: Early returns, guard clauses, extract methods

### 🟡 GRAVITÀ MEDIA (Score Impact: -6 to -8)  
5. **Metodi con complessità 11-14**
   - Descrizione: Metodi con complessità moderata-alta
   - Rischio: Testing più difficile, maggiore probabilità di bug
   - Soluzione: Considerare refactoring quando si modifica

6. **Distribuzione complessità squilibrata**
   - Descrizione: Classi con pochi metodi molto complessi e molti semplici
   - Rischio: Hotspot di complessità, responsabilità mal distribuite
   - Soluzione: Ribalancciare complessità tra metodi

### 🔵 GRAVITÀ BASSA (Score Impact: -1 to -3)
7. **Metodi con complessità 6-10**  
   - Descrizione: Metodi con complessità leggermente elevata
   - Rischio: Minor impact su maintainability
   - Soluzione: Tenere sotto controllo, ottimizzare se opportuno

## Metriche di Valore

- **Maintainability Index**: Correlazione complessità-maintainability
- **Testing Effort**: Effort necessario per test completo basato su complessità
- **Bug Prediction**: Probabilità di bug basata su complessità storica
- **Refactoring Priority**: Prioritizzazione interventi basata su impact

## Classificazione

**Categoria**: Code Quality & Performance  
**Priorità**: Alta - La complessità impatta direttamente maintainability e quality
**Stakeholder**: Development team, Technical leads, QA team