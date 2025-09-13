package it.denzosoft.jreverse.analyzer.springboot.indicators;

import it.denzosoft.jreverse.analyzer.springboot.SpringBootAnalysisException;
import it.denzosoft.jreverse.analyzer.springboot.SpringBootIndicator;
import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.MethodInfo;
import it.denzosoft.jreverse.core.model.springboot.IndicatorResult;
import it.denzosoft.jreverse.core.model.springboot.SpringBootIndicatorType;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Opcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Indicator that analyzes main methods for SpringApplication.run() calls using Javassist bytecode analysis.
 * This is a highly reliable indicator for Spring Boot applications.
 */
public class SpringBootMainClassIndicator implements SpringBootIndicator {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(SpringBootMainClassIndicator.class);
    
    private static final String SPRING_APPLICATION_CLASS = "org.springframework.boot.SpringApplication";
    private static final String RUN_METHOD_NAME = "run";
    
    private final ClassPool classPool;
    
    public SpringBootMainClassIndicator() {
        this.classPool = ClassPool.getDefault();
    }
    
    public SpringBootMainClassIndicator(ClassPool classPool) {
        this.classPool = classPool != null ? classPool : ClassPool.getDefault();
    }
    
    @Override
    public IndicatorResult analyze(JarContent jarContent) {
        LOGGER.debug("Starting Spring Boot main class analysis for JAR: %s", 
                    jarContent.getLocation().getFileName());
        
        long startTime = System.currentTimeMillis();
        
        try {
            List<MainClassInfo> mainClasses = findMainClasses(jarContent);
            if (mainClasses.isEmpty()) {
                return IndicatorResult.notFound("No main classes found");
            }
            
            MainClassAnalysisResult analysisResult = analyzeMainClasses(mainClasses, jarContent);
            
            double confidence = calculateConfidence(analysisResult);
            Map<String, Object> evidence = buildEvidence(analysisResult);
            
            long analysisTime = System.currentTimeMillis() - startTime;
            
            LOGGER.debug("Spring Boot main class analysis completed. Confidence: %.2f, Time: %dms", 
                        confidence, analysisTime);
            
            return IndicatorResult.builder()
                .confidence(confidence)
                .status(confidence > 0.0 ? IndicatorResult.AnalysisStatus.SUCCESS : IndicatorResult.AnalysisStatus.NOT_FOUND)
                .evidence(evidence)
                .analysisTimeMs(analysisTime)
                .build();
                
        } catch (Exception e) {
            long analysisTime = System.currentTimeMillis() - startTime;
            LOGGER.error("Spring Boot main class analysis failed", e);
            
            return IndicatorResult.builder()
                .confidence(0.0)
                .status(IndicatorResult.AnalysisStatus.ERROR)
                .errorMessage(e.getMessage())
                .analysisTimeMs(analysisTime)
                .build();
        }
    }
    
    @Override
    public SpringBootIndicatorType getType() {
        return SpringBootIndicatorType.MAIN_CLASS;
    }
    
    @Override
    public boolean canAnalyze(JarContent jarContent) {
        return jarContent != null && jarContent.getClassCount() > 0;
    }
    
    private List<MainClassInfo> findMainClasses(JarContent jarContent) {
        List<MainClassInfo> mainClasses = new ArrayList<>();
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            Optional<MethodInfo> mainMethod = findMainMethod(classInfo);
            if (mainMethod.isPresent()) {
                mainClasses.add(new MainClassInfo(classInfo, mainMethod.get()));
                LOGGER.debug("Found main class: %s", classInfo.getFullyQualifiedName());
            }
        }
        
        return mainClasses;
    }
    
    private Optional<MethodInfo> findMainMethod(ClassInfo classInfo) {
        return classInfo.getMethods().stream()
            .filter(this::isMainMethod)
            .findFirst();
    }
    
    private boolean isMainMethod(MethodInfo method) {
        return "main".equals(method.getName()) &&
               method.isStatic() &&
               method.isPublic() &&
               method.getParameters().size() == 1 &&
               "java.lang.String[]".equals(method.getParameters().get(0).getType());
    }
    
    private MainClassAnalysisResult analyzeMainClasses(List<MainClassInfo> mainClasses, JarContent jarContent) 
            throws SpringBootAnalysisException {
        MainClassAnalysisResult.Builder resultBuilder = MainClassAnalysisResult.builder();
        
        for (MainClassInfo mainClassInfo : mainClasses) {
            try {
                SpringApplicationCallDetails callDetails = analyzeMainClassBytecode(mainClassInfo, jarContent);
                if (callDetails.hasSpringApplicationCall()) {
                    resultBuilder.addSpringBootMainClass(mainClassInfo.getClassInfo().getFullyQualifiedName(), callDetails);
                } else {
                    resultBuilder.addRegularMainClass(mainClassInfo.getClassInfo().getFullyQualifiedName());
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to analyze main class %s: %s", 
                           mainClassInfo.getClassInfo().getFullyQualifiedName(), e.getMessage());
                resultBuilder.addFailedAnalysis(mainClassInfo.getClassInfo().getFullyQualifiedName(), e.getMessage());
            }
        }
        
        return resultBuilder.build();
    }
    
    private SpringApplicationCallDetails analyzeMainClassBytecode(MainClassInfo mainClassInfo, JarContent jarContent) 
            throws SpringBootAnalysisException {
        try {
            String className = mainClassInfo.getClassInfo().getFullyQualifiedName();
            CtClass ctClass = loadClassSafely(className, jarContent);
            
            if (ctClass == null) {
                return SpringApplicationCallDetails.notFound("Class not loadable: " + className);
            }
            
            CtMethod[] mainMethods = ctClass.getDeclaredMethods("main");
            for (CtMethod method : mainMethods) {
                if (isValidMainMethod(method)) {
                    return analyzeBytecodeForSpringApplication(method, ctClass);
                }
            }
            
            return SpringApplicationCallDetails.notFound("No valid main method found");
            
        } catch (Exception e) {
            throw SpringBootAnalysisException.bytecodeAnalysisFailed(
                mainClassInfo.getClassInfo().getFullyQualifiedName(),
                jarContent.getLocation().toString(), 
                e);
        }
    }
    
    private CtClass loadClassSafely(String className, JarContent jarContent) {
        try {
            return classPool.get(className);
        } catch (NotFoundException e) {
            LOGGER.debug("Class not found in ClassPool: %s", className);
            return null;
        }
    }
    
    private boolean isValidMainMethod(CtMethod method) {
        try {
            return method.getModifiers() == (javassist.Modifier.PUBLIC | javassist.Modifier.STATIC) &&
                   "void".equals(method.getReturnType().getName()) &&
                   method.getParameterTypes().length == 1 &&
                   "[Ljava.lang.String;".equals(method.getParameterTypes()[0].getName());
        } catch (NotFoundException e) {
            return false;
        }
    }
    
    private SpringApplicationCallDetails analyzeBytecodeForSpringApplication(CtMethod method, CtClass ctClass) 
            throws BadBytecode {
        SpringApplicationCallDetails.Builder detailsBuilder = SpringApplicationCallDetails.builder();
        
        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        if (codeAttribute == null) {
            return detailsBuilder.build();
        }
        
        ConstPool constPool = method.getMethodInfo().getConstPool();
        CodeIterator iterator = codeAttribute.iterator();
        
        while (iterator.hasNext()) {
            int index = iterator.next();
            int opcode = iterator.byteAt(index);
            
            if (opcode == Opcode.INVOKESTATIC) {
                int constIndex = iterator.u16bitAt(index + 1);
                String methodRef = constPool.getMethodrefClassName(constIndex);
                String methodName = constPool.getMethodrefName(constIndex);
                String methodSignature = constPool.getMethodrefType(constIndex);
                
                if (SPRING_APPLICATION_CLASS.equals(methodRef) && RUN_METHOD_NAME.equals(methodName)) {
                    detailsBuilder.springApplicationCall(true)
                                  .callLocation(index)
                                  .methodSignature(methodSignature);
                    
                    // Try to extract source classes (the arguments to run())
                    List<String> sourceClasses = extractSourceClasses(iterator, constPool, index);
                    detailsBuilder.sourceClasses(sourceClasses);
                    
                    LOGGER.debug("Found SpringApplication.run() call in %s at bytecode index %d", 
                                ctClass.getName(), index);
                }
            }
        }
        
        return detailsBuilder.build();
    }
    
    private List<String> extractSourceClasses(CodeIterator iterator, ConstPool constPool, int callIndex) {
        List<String> sourceClasses = new ArrayList<>();
        
        // This is a simplified implementation - in a real scenario, we'd need more sophisticated
        // analysis to track the stack and determine what classes are being passed to run()
        try {
            // Look backwards for LDC instructions that might contain class references
            for (int i = Math.max(0, callIndex - 20); i < callIndex; i++) {
                int opcode = iterator.byteAt(i);
                if (opcode == Opcode.LDC || opcode == Opcode.LDC_W) {
                    int constIndex = opcode == Opcode.LDC ? 
                        iterator.byteAt(i + 1) : iterator.u16bitAt(i + 1);
                    
                    String constant = constPool.getStringInfo(constIndex);
                    if (constant != null && isLikelyClassName(constant)) {
                        sourceClasses.add(constant);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to extract source classes: %s", e.getMessage());
        }
        
        return sourceClasses;
    }
    
    private boolean isLikelyClassName(String constant) {
        return constant != null && 
               constant.contains(".") && 
               Character.isUpperCase(constant.charAt(constant.lastIndexOf('.') + 1));
    }
    
    private double calculateConfidence(MainClassAnalysisResult result) {
        if (result.getSpringBootMainClasses().isEmpty()) {
            return 0.0;
        }
        
        double baseConfidence = 0.85; // High confidence for SpringApplication.run() detection
        
        // Boost for multiple Spring Boot main classes (unusual but possible)
        if (result.getSpringBootMainClasses().size() > 1) {
            baseConfidence = Math.min(1.0, baseConfidence * 1.1);
        }
        
        // Boost for source classes detected
        boolean hasSourceClasses = result.getSpringBootMainClasses().values().stream()
            .anyMatch(details -> !details.getSourceClasses().isEmpty());
        if (hasSourceClasses) {
            baseConfidence = Math.min(1.0, baseConfidence * 1.05);
        }
        
        return baseConfidence;
    }
    
    private Map<String, Object> buildEvidence(MainClassAnalysisResult result) {
        Map<String, Object> evidence = new HashMap<>();
        
        evidence.put("springBootMainClasses", result.getSpringBootMainClasses().keySet());
        evidence.put("regularMainClasses", result.getRegularMainClasses());
        evidence.put("springBootMainClassCount", result.getSpringBootMainClasses().size());
        evidence.put("totalMainClassCount", result.getSpringBootMainClasses().size() + result.getRegularMainClasses().size());
        
        if (!result.getFailedAnalyses().isEmpty()) {
            evidence.put("failedAnalyses", result.getFailedAnalyses());
        }
        
        // Include details about the calls found
        Map<String, Object> callDetails = new HashMap<>();
        result.getSpringBootMainClasses().forEach((className, details) -> {
            Map<String, Object> classDetails = new HashMap<>();
            classDetails.put("hasCall", details.hasSpringApplicationCall());
            classDetails.put("sourceClasses", details.getSourceClasses());
            classDetails.put("methodSignature", details.getMethodSignature());
            if (details.getCallLocation() >= 0) {
                classDetails.put("bytecodeLocation", details.getCallLocation());
            }
            callDetails.put(className, classDetails);
        });
        evidence.put("callDetails", callDetails);
        
        return evidence;
    }
    
    // Helper classes
    
    private static class MainClassInfo {
        private final ClassInfo classInfo;
        private final MethodInfo mainMethod;
        
        public MainClassInfo(ClassInfo classInfo, MethodInfo mainMethod) {
            this.classInfo = classInfo;
            this.mainMethod = mainMethod;
        }
        
        public ClassInfo getClassInfo() {
            return classInfo;
        }
        
        public MethodInfo getMainMethod() {
            return mainMethod;
        }
    }
    
    private static class SpringApplicationCallDetails {
        private final boolean hasSpringApplicationCall;
        private final List<String> sourceClasses;
        private final int callLocation;
        private final String methodSignature;
        
        private SpringApplicationCallDetails(Builder builder) {
            this.hasSpringApplicationCall = builder.hasSpringApplicationCall;
            this.sourceClasses = List.copyOf(builder.sourceClasses);
            this.callLocation = builder.callLocation;
            this.methodSignature = builder.methodSignature;
        }
        
        public boolean hasSpringApplicationCall() {
            return hasSpringApplicationCall;
        }
        
        public List<String> getSourceClasses() {
            return sourceClasses;
        }
        
        public int getCallLocation() {
            return callLocation;
        }
        
        public String getMethodSignature() {
            return methodSignature;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static SpringApplicationCallDetails notFound(String reason) {
            return builder().build();
        }
        
        public static class Builder {
            private boolean hasSpringApplicationCall = false;
            private List<String> sourceClasses = new ArrayList<>();
            private int callLocation = -1;
            private String methodSignature;
            
            public Builder springApplicationCall(boolean hasCall) {
                this.hasSpringApplicationCall = hasCall;
                return this;
            }
            
            public Builder sourceClasses(List<String> classes) {
                this.sourceClasses = new ArrayList<>(classes != null ? classes : List.of());
                return this;
            }
            
            public Builder callLocation(int location) {
                this.callLocation = location;
                return this;
            }
            
            public Builder methodSignature(String signature) {
                this.methodSignature = signature;
                return this;
            }
            
            public SpringApplicationCallDetails build() {
                return new SpringApplicationCallDetails(this);
            }
        }
    }
    
    private static class MainClassAnalysisResult {
        private final Map<String, SpringApplicationCallDetails> springBootMainClasses;
        private final List<String> regularMainClasses;
        private final Map<String, String> failedAnalyses;
        
        private MainClassAnalysisResult(Builder builder) {
            this.springBootMainClasses = Map.copyOf(builder.springBootMainClasses);
            this.regularMainClasses = List.copyOf(builder.regularMainClasses);
            this.failedAnalyses = Map.copyOf(builder.failedAnalyses);
        }
        
        public Map<String, SpringApplicationCallDetails> getSpringBootMainClasses() {
            return springBootMainClasses;
        }
        
        public List<String> getRegularMainClasses() {
            return regularMainClasses;
        }
        
        public Map<String, String> getFailedAnalyses() {
            return failedAnalyses;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private final Map<String, SpringApplicationCallDetails> springBootMainClasses = new HashMap<>();
            private final List<String> regularMainClasses = new ArrayList<>();
            private final Map<String, String> failedAnalyses = new HashMap<>();
            
            public Builder addSpringBootMainClass(String className, SpringApplicationCallDetails details) {
                springBootMainClasses.put(className, details);
                return this;
            }
            
            public Builder addRegularMainClass(String className) {
                regularMainClasses.add(className);
                return this;
            }
            
            public Builder addFailedAnalysis(String className, String error) {
                failedAnalyses.put(className, error);
                return this;
            }
            
            public MainClassAnalysisResult build() {
                return new MainClassAnalysisResult(this);
            }
        }
    }
}