package it.denzosoft.jreverse.analyzer.impl;

import it.denzosoft.jreverse.core.exception.JarAnalysisException;
import it.denzosoft.jreverse.core.model.*;
import it.denzosoft.jreverse.core.port.JarAnalyzerPort;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * Default JAR analyzer implementation using Javassist.
 * Provides basic analysis capabilities for all types of JAR files.
 */
public class DefaultJarAnalyzer implements JarAnalyzerPort {
    
    private static final Logger LOGGER = Logger.getLogger(DefaultJarAnalyzer.class.getName());
    
    protected final ClassPool classPool;
    
    public DefaultJarAnalyzer(ClassPool classPool) {
        this.classPool = classPool != null ? classPool : ClassPool.getDefault();
    }
    
    @Override
    public JarContent analyzeJar(JarLocation jarLocation) throws JarAnalysisException {
        LOGGER.info("Starting analysis of JAR: " + jarLocation.getPath());
        
        try (JarFile jarFile = new JarFile(jarLocation.getPath().toFile())) {
            Set<ClassInfo> classes = analyzeClasses(jarFile, jarLocation);
            JarManifestInfo manifestInfo = analyzeManifest(jarFile);
            
            JarContent jarContent = JarContent.builder()
                .location(jarLocation)
                .jarType(determineJarType(jarFile, jarLocation))
                .classes(classes)
                .manifest(manifestInfo)
                .build();
                
            LOGGER.info("Analysis completed. Found " + classes.size() + " classes in " + 
                       jarLocation.getFileName());
            
            return jarContent;
            
        } catch (IOException e) {
            throw new JarAnalysisException("Failed to analyze JAR file: " + jarLocation.getPath(), 
                                           e.getMessage(), 
                                           JarAnalysisException.ErrorCode.ANALYSIS_FAILED, 
                                           e);
        }
    }
    
    protected Set<ClassInfo> analyzeClasses(JarFile jarFile, JarLocation jarLocation) throws JarAnalysisException {
        Set<ClassInfo> classes = new HashSet<>();
        
        // Add JAR to ClassPool
        try {
            classPool.appendClassPath(jarLocation.getPath().toString());
        } catch (Exception e) {
            LOGGER.warning("Could not add JAR to ClassPool: " + e.getMessage());
        }
        
        jarFile.stream()
            .filter(this::isClassFile)
            .forEach(entry -> {
                try {
                    ClassInfo classInfo = analyzeClassEntry(entry, jarFile, jarLocation);
                    if (classInfo != null) {
                        classes.add(classInfo);
                    }
                } catch (Exception e) {
                    LOGGER.warning("Failed to analyze class " + entry.getName() + ": " + e.getMessage());
                }
            });
            
        return classes;
    }
    
    protected ClassInfo analyzeClassEntry(JarEntry entry, JarFile jarFile, JarLocation jarLocation) {
        String className = getClassName(entry);
        
        try {
            CtClass ctClass = classPool.get(className);
            
            return ClassInfo.builder()
                .fullyQualifiedName(className)
                .classType(determineClassType(ctClass))
                .build();
                
        } catch (NotFoundException e) {
            LOGGER.fine("Class not found in pool: " + className + " - " + e.getMessage());
            
            // Create basic ClassInfo from entry name
            return ClassInfo.builder()
                .fullyQualifiedName(className)
                .classType(ClassType.CLASS)
                .build();
        } catch (Exception e) {
            LOGGER.warning("Failed to analyze class " + className + ": " + e.getMessage());
            return null;
        }
    }
    
    protected JarManifestInfo analyzeManifest(JarFile jarFile) throws IOException {
        java.util.jar.Manifest manifest = jarFile.getManifest();
        if (manifest == null) {
            return JarManifestInfo.builder().build();
        }
        
        java.util.jar.Attributes attrs = manifest.getMainAttributes();
        return JarManifestInfo.builder()
            .mainClass(attrs.getValue("Main-Class"))
            .manifestVersion(attrs.getValue("Manifest-Version"))
            .implementationTitle(attrs.getValue("Implementation-Title"))
            .implementationVersion(attrs.getValue("Implementation-Version"))
            .implementationVendor(attrs.getValue("Implementation-Vendor"))
            .specificationTitle(attrs.getValue("Specification-Title"))
            .specificationVersion(attrs.getValue("Specification-Version"))
            .specificationVendor(attrs.getValue("Specification-Vendor"))
            .builtBy(attrs.getValue("Built-By"))
            .buildJdk(attrs.getValue("Build-Jdk"))
            .createdBy(attrs.getValue("Created-By"))
            .build();
    }
    
    protected JarType determineJarType(JarFile jarFile, JarLocation jarLocation) {
        // Basic JAR type detection
        if (jarLocation.getFileName().endsWith(".war")) {
            return JarType.WAR_ARCHIVE;
        }
        
        // Check for Spring Boot indicators (basic check)
        if (jarFile.getEntry("BOOT-INF/") != null) {
            return JarType.SPRING_BOOT_JAR;
        }
        
        return JarType.REGULAR_JAR;
    }
    
    protected boolean isClassFile(JarEntry entry) {
        return !entry.isDirectory() && 
               entry.getName().endsWith(".class") &&
               !entry.getName().contains("$") && // Skip inner classes for basic analysis
               !entry.getName().startsWith("META-INF/");
    }
    
    protected String getClassName(JarEntry entry) {
        String name = entry.getName();
        // Convert path to class name: com/example/Class.class -> com.example.Class
        return name.substring(0, name.length() - 6) // Remove .class
                   .replace('/', '.');
    }
    
    protected ClassType determineClassType(CtClass ctClass) {
        try {
            if (ctClass.isInterface()) {
                return ClassType.INTERFACE;
            } else if (ctClass.isEnum()) {
                return ClassType.ENUM;
            } else if (ctClass.isAnnotation()) {
                return ClassType.ANNOTATION;
            } else {
                return ClassType.CLASS;
            }
        } catch (Exception e) {
            LOGGER.fine("Could not determine class type for " + ctClass.getName() + ": " + e.getMessage());
            return ClassType.CLASS;
        }
    }
    
    @Override
    public boolean supportsJar(JarLocation location) {
        // Default analyzer supports all JAR files
        return true;
    }
    
    @Override
    public String getAnalyzerName() {
        return "Default JAR Analyzer";
    }
    
    /**
     * Gets the ClassPool used by this analyzer.
     * 
     * @return the ClassPool instance
     */
    public ClassPool getClassPool() {
        return classPool;
    }
}