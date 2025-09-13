package it.denzosoft.jreverse.analyzer.mainmethod;

import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.MainMethodAnalysisResult;
import it.denzosoft.jreverse.core.model.MainMethodType;
import it.denzosoft.jreverse.core.model.MethodInfo;
import it.denzosoft.jreverse.core.model.SpringApplicationCallInfo;
import it.denzosoft.jreverse.core.model.SpringApplicationCallType;
import it.denzosoft.jreverse.core.port.MainMethodAnalyzer;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Opcode;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Javassist-based implementation of MainMethodAnalyzer.
 * Analyzes bytecode to detect main methods and SpringApplication.run() calls.
 */
public class JavassistMainMethodAnalyzer implements MainMethodAnalyzer {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistMainMethodAnalyzer.class);
    
    private static final String SPRING_APPLICATION_CLASS = "org.springframework.boot.SpringApplication";
    private static final String SPRING_APPLICATION_BUILDER_CLASS = "org.springframework.boot.builder.SpringApplicationBuilder";
    
    @Override
    public MainMethodAnalysisResult analyzeMainMethod(JarContent jarContent) {
        if (jarContent == null || jarContent.isEmpty()) {
            return MainMethodAnalysisResult.noMainFound();
        }
        
        long startTime = System.currentTimeMillis();
        LOGGER.info("Starting main method analysis for JAR: %s", 
                   jarContent.getLocation() != null ? jarContent.getLocation().getFileName() : "unknown");
        
        try {
            Optional<MainMethodInfo> mainMethodOpt = findMainMethod(jarContent);
            
            if (mainMethodOpt.isEmpty()) {
                LOGGER.info("No main method found in JAR");
                return MainMethodAnalysisResult.noMainFound()
                    .withAnalysisTime(System.currentTimeMillis() - startTime);
            }
            
            MainMethodInfo mainMethodInfo = mainMethodOpt.get();
            LOGGER.info("Found main method in class: %s", mainMethodInfo.getDeclaringClassName());
            
            // Analyze if it's a Spring Boot main method
            Optional<SpringApplicationCallInfo> springCallOpt = analyzeSpringApplicationCall(mainMethodInfo);
            
            MainMethodAnalysisResult result;
            if (springCallOpt.isPresent()) {
                LOGGER.info("Detected Spring Boot main method with call type: %s", 
                           springCallOpt.get().getCallType());
                result = MainMethodAnalysisResult.springBootMain(
                    mainMethodInfo.getMethodInfo(),
                    mainMethodInfo.getDeclaringClassName(),
                    springCallOpt.get()
                );
            } else {
                LOGGER.info("Detected regular Java main method");
                result = MainMethodAnalysisResult.regularMain(
                    mainMethodInfo.getMethodInfo(),
                    mainMethodInfo.getDeclaringClassName()
                );
            }
            
            return result.withAnalysisTime(System.currentTimeMillis() - startTime);
            
        } catch (Exception e) {
            LOGGER.error("Error during main method analysis", e);
            return MainMethodAnalysisResult.error("Analysis failed: " + e.getMessage())
                .withAnalysisTime(System.currentTimeMillis() - startTime);
        }
    }
    
    private Optional<MainMethodInfo> findMainMethod(JarContent jarContent) {
        for (ClassInfo classInfo : jarContent.getClasses()) {
            Optional<MethodInfo> mainMethodOpt = findMainMethodInClass(classInfo);
            if (mainMethodOpt.isPresent()) {
                return Optional.of(new MainMethodInfo(classInfo.getFullyQualifiedName(), mainMethodOpt.get()));
            }
        }
        return Optional.empty();
    }
    
    private Optional<MethodInfo> findMainMethodInClass(ClassInfo classInfo) {
        return classInfo.getMethods().stream()
            .filter(this::isMainMethod)
            .findFirst();
    }
    
    private boolean isMainMethod(MethodInfo method) {
        return "main".equals(method.getName()) &&
               method.isStatic() &&
               method.isPublic() &&
               "void".equals(method.getReturnType()) &&
               hasStringArrayParameter(method);
    }
    
    private boolean hasStringArrayParameter(MethodInfo method) {
        return method.getParameters().size() == 1 &&
               method.getParameters().get(0).getType().equals("java.lang.String[]");
    }
    
    private Optional<SpringApplicationCallInfo> analyzeSpringApplicationCall(MainMethodInfo mainMethodInfo) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.get(mainMethodInfo.getDeclaringClassName());
            CtMethod ctMethod = ctClass.getDeclaredMethod("main");
            
            return extractSpringApplicationCallInfo(ctMethod);
            
        } catch (Exception e) {
            LOGGER.debug("Error analyzing SpringApplication call in %s: %s", 
                        mainMethodInfo.getDeclaringClassName(), e.getMessage());
            return Optional.empty();
        }
    }
    
    private Optional<SpringApplicationCallInfo> extractSpringApplicationCallInfo(CtMethod method) {
        try {
            CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
            if (codeAttribute == null) {
                return Optional.empty();
            }
            
            SpringApplicationCallInfo.Builder builder = SpringApplicationCallInfo.builder();
            boolean foundSpringApplicationCall = false;
            
            CodeIterator iterator = codeAttribute.iterator();
            Set<String> detectedSourceClasses = new HashSet<>();
            
            while (iterator.hasNext()) {
                int index = iterator.next();
                int opcode = iterator.byteAt(index);
                
                switch (opcode) {
                    case Opcode.LDC:
                        String constant = getStringConstant(method.getDeclaringClass(), iterator, index);
                        if (isConfigurationClass(constant)) {
                            detectedSourceClasses.add(constant);
                        }
                        break;
                        
                    case Opcode.INVOKESTATIC:
                        String methodRef = getInvokedMethod(method.getDeclaringClass(), iterator, index);
                        if (isSpringApplicationRunCall(methodRef)) {
                            foundSpringApplicationCall = true;
                            SpringApplicationCallType callType = determineCallType(methodRef);
                            builder.callType(callType);
                            
                            if (callType == SpringApplicationCallType.MULTIPLE_SOURCES) {
                                builder.hasMultipleSourceClasses(true);
                            }
                        }
                        break;
                        
                    case Opcode.ANEWARRAY:
                        // Array creation might indicate multiple source classes
                        builder.hasMultipleSourceClasses(true);
                        break;
                }
            }
            
            if (!foundSpringApplicationCall) {
                return Optional.empty();
            }
            
            // Add detected source classes
            detectedSourceClasses.forEach(builder::addSourceClass);
            
            // If no explicit source classes found, use the declaring class
            if (detectedSourceClasses.isEmpty()) {
                builder.addSourceClass(method.getDeclaringClass().getName());
            }
            
            return Optional.of(builder.build());
            
        } catch (BadBytecode e) {
            LOGGER.debug("Error analyzing bytecode for SpringApplication call: %s", e.getMessage());
            return Optional.empty();
        }
    }
    
    private String getStringConstant(CtClass ctClass, CodeIterator iterator, int index) {
        try {
            int constIndex = iterator.byteAt(index + 1);
            ConstPool constPool = ctClass.getClassFile().getConstPool();
            return constPool.getStringInfo(constIndex);
        } catch (Exception e) {
            return null;
        }
    }
    
    private String getInvokedMethod(CtClass ctClass, CodeIterator iterator, int index) {
        try {
            int constIndex = iterator.u16bitAt(index + 1);
            ConstPool constPool = ctClass.getClassFile().getConstPool();
            return constPool.getMethodrefClassName(constIndex) + "." + 
                   constPool.getMethodrefName(constIndex);
        } catch (Exception e) {
            return "";
        }
    }
    
    private boolean isSpringApplicationRunCall(String methodRef) {
        return methodRef.contains(SPRING_APPLICATION_CLASS + ".run") ||
               methodRef.contains(SPRING_APPLICATION_BUILDER_CLASS);
    }
    
    private SpringApplicationCallType determineCallType(String methodRef) {
        if (methodRef.contains(SPRING_APPLICATION_BUILDER_CLASS)) {
            return SpringApplicationCallType.BUILDER;
        } else if (methodRef.contains("run")) {
            // Check if it's a multi-source call (heuristic based on method signature)
            if (methodRef.contains("([Ljava/lang/Class;")) {
                return SpringApplicationCallType.MULTIPLE_SOURCES;
            }
            return SpringApplicationCallType.STANDARD;
        }
        return SpringApplicationCallType.UNKNOWN;
    }
    
    private boolean isConfigurationClass(String className) {
        return className != null && 
               (className.endsWith("Application") || 
                className.endsWith("Config") ||
                className.contains("Configuration") ||
                className.endsWith("Boot"));
    }
    
    @Override
    public boolean canAnalyze(JarContent jarContent) {
        return jarContent != null && !jarContent.isEmpty();
    }
    
    /**
     * Internal class to hold main method information during analysis.
     */
    private static class MainMethodInfo {
        private final String declaringClassName;
        private final MethodInfo methodInfo;
        
        public MainMethodInfo(String declaringClassName, MethodInfo methodInfo) {
            this.declaringClassName = declaringClassName;
            this.methodInfo = methodInfo;
        }
        
        public String getDeclaringClassName() {
            return declaringClassName;
        }
        
        public MethodInfo getMethodInfo() {
            return methodInfo;
        }
    }
}