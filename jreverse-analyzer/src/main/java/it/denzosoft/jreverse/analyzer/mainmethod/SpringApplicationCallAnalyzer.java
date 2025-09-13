package it.denzosoft.jreverse.analyzer.mainmethod;

import it.denzosoft.jreverse.core.logging.JReverseLogger;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Opcode;

/**
 * Analyzer for extracting detailed information about SpringApplication.run() calls.
 */
public class SpringApplicationCallAnalyzer {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(SpringApplicationCallAnalyzer.class);
    
    private static final String SPRING_APPLICATION_CLASS = "org.springframework.boot.SpringApplication";
    private static final String SPRING_APPLICATION_BUILDER_CLASS = "org.springframework.boot.builder.SpringApplicationBuilder";
    
    /**
     * Analyzes a SpringApplication call in the given main method.
     */
    public SpringApplicationCallInfo analyzeCall(MainMethodInfo mainMethod) {
        if (mainMethod == null) {
            LOGGER.warn("Cannot analyze null MainMethodInfo");
            return SpringApplicationCallInfo.unknown();
        }
        
        LOGGER.debug("Analyzing SpringApplication call in %s", mainMethod.getSignature());
        
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.get(mainMethod.getClassName());
            CtMethod ctMethod = ctClass.getDeclaredMethod("main", 
                new CtClass[]{pool.get("java.lang.String[]")});
            
            return extractCallInformation(ctMethod);
            
        } catch (Exception e) {
            LOGGER.error("Error analyzing SpringApplication call in " + mainMethod.getSignature(), e);
            return SpringApplicationCallInfo.unknown();
        }
    }
    
    /**
     * Extracts detailed information about the SpringApplication call from bytecode.
     */
    private SpringApplicationCallInfo extractCallInformation(CtMethod method) {
        SpringApplicationCallInfo.Builder builder = SpringApplicationCallInfo.builder();
        
        try {
            CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
            if (codeAttribute == null) {
                return SpringApplicationCallInfo.unknown();
            }
            
            ConstPool constPool = codeAttribute.getConstPool();
            CodeIterator iterator = codeAttribute.iterator();
            
            while (iterator.hasNext()) {
                int index = iterator.next();
                int opcode = iterator.byteAt(index);
                
                switch (opcode) {
                    case Opcode.LDC:
                        // Load constant - may be a class reference
                        String constant = getStringConstant(constPool, iterator, index);
                        if (isConfigurationClass(constant)) {
                            builder.addSourceClass(constant);
                        }
                        break;
                    case Opcode.LDC2_W:
                        // Load constant for Class.class references
                        String classConstant = getClassConstant(constPool, iterator, index);
                        if (classConstant != null && isConfigurationClass(classConstant)) {
                            builder.addSourceClass(classConstant);
                        }
                        break;
                    case Opcode.INVOKESTATIC:
                        // Static method call - SpringApplication.run()
                        String methodRef = getMethodReference(constPool, iterator, index);
                        if (isSpringApplicationRunCall(methodRef)) {
                            SpringApplicationCallType callType = determineCallType(methodRef);
                            builder.callType(callType);
                            builder.argumentCount(2); // Typical: Class and String[] args
                        }
                        break;
                    case Opcode.INVOKESPECIAL:
                        // Constructor call - new SpringApplication()
                        String methodRef2 = getMethodReference(constPool, iterator, index);
                        if (methodRef2.contains(SPRING_APPLICATION_CLASS) && methodRef2.contains("<init>")) {
                            builder.callType(SpringApplicationCallType.INSTANCE_BASED);
                        }
                        break;
                    case Opcode.ANEWARRAY:
                        // Array creation - multiple source classes
                        builder.hasMultipleSourceClasses(true);
                        builder.callType(SpringApplicationCallType.MULTIPLE_SOURCES);
                        break;
                }
            }
            
            return builder.build();
            
        } catch (BadBytecode e) {
            LOGGER.error("Error extracting SpringApplication call information", e);
            return SpringApplicationCallInfo.unknown();
        }
    }
    
    /**
     * Gets string constant from bytecode.
     */
    private String getStringConstant(ConstPool constPool, CodeIterator iterator, int index) {
        try {
            int constIndex = iterator.byteAt(index + 1);
            return constPool.getStringInfo(constIndex);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Gets class constant from bytecode.
     */
    private String getClassConstant(ConstPool constPool, CodeIterator iterator, int index) {
        try {
            int constIndex = iterator.u16bitAt(index + 1);
            return constPool.getClassInfo(constIndex);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Gets method reference from bytecode.
     */
    private String getMethodReference(ConstPool constPool, CodeIterator iterator, int index) {
        try {
            int constIndex = iterator.u16bitAt(index + 1);
            String className = constPool.getMethodrefClassName(constIndex);
            String methodName = constPool.getMethodrefName(constIndex);
            String methodType = constPool.getMethodrefType(constIndex);
            return className + "." + methodName + methodType;
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Checks if a method reference is a SpringApplication.run() call.
     */
    private boolean isSpringApplicationRunCall(String methodRef) {
        return methodRef.contains(SPRING_APPLICATION_CLASS) && methodRef.contains("run");
    }
    
    /**
     * Determines the type of SpringApplication call based on method signature.
     */
    private SpringApplicationCallType determineCallType(String methodRef) {
        if (methodRef.contains(SPRING_APPLICATION_BUILDER_CLASS)) {
            return SpringApplicationCallType.BUILDER_BASED;
        }
        
        if (methodRef.contains("([Ljava/lang/Class;")) {
            return SpringApplicationCallType.MULTIPLE_SOURCES;
        }
        
        if (methodRef.contains("(Ljava/lang/Class;")) {
            return SpringApplicationCallType.STANDARD;
        }
        
        return SpringApplicationCallType.CUSTOM;
    }
    
    /**
     * Heuristic to identify if a string/class is likely a configuration class.
     */
    private boolean isConfigurationClass(String className) {
        if (className == null) {
            return false;
        }
        
        // Remove package prefixes to get simple class name
        String simpleName = className.substring(className.lastIndexOf('.') + 1);
        
        return simpleName.endsWith("Application") ||
               simpleName.endsWith("Config") ||
               simpleName.endsWith("Configuration") ||
               simpleName.contains("Main") ||
               simpleName.contains("Boot");
    }
}