package it.denzosoft.jreverse.analyzer.impl;

import it.denzosoft.jreverse.analyzer.detector.SpringBootDetector;
import it.denzosoft.jreverse.core.exception.JarAnalysisException;
import it.denzosoft.jreverse.core.model.*;
import javassist.ClassPool;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * Specialized analyzer for Spring Boot JAR files.
 * Extends DefaultJarAnalyzer with Spring Boot specific analysis capabilities.
 */
public class SpringBootJarAnalyzer extends DefaultJarAnalyzer {
    
    private static final Logger LOGGER = Logger.getLogger(SpringBootJarAnalyzer.class.getName());
    
    private static final String BOOT_INF_CLASSES = "BOOT-INF/classes/";
    private static final String BOOT_INF_LIB = "BOOT-INF/lib/";
    
    private final SpringBootDetector springBootDetector;
    
    @Deprecated
    public SpringBootJarAnalyzer(ClassPool classPool, SpringBootDetector springBootDetector) {
        super(); // FIXED: Use default constructor
        this.springBootDetector = springBootDetector;
    }
    
    @Override
    public JarContent analyzeJar(JarLocation jarLocation) throws JarAnalysisException {
        LOGGER.info("Starting Spring Boot analysis of JAR: " + jarLocation.getPath());
        
        try (JarFile jarFile = new JarFile(jarLocation.getPath().toFile())) {
            Set<ClassInfo> classes = analyzeSpringBootClasses(jarFile, jarLocation);
            JarManifestInfo manifestInfo = analyzeManifest(jarFile);
            
            JarContent jarContent = JarContent.builder()
                .location(jarLocation)
                .jarType(JarType.SPRING_BOOT_JAR)
                .classes(classes)
                .manifest(manifestInfo)
                .build();
                
            LOGGER.info("Spring Boot analysis completed. Found " + classes.size() + " classes in " + 
                       jarLocation.getFileName());
            
            return jarContent;
            
        } catch (IOException e) {
            throw new JarAnalysisException("Failed to analyze Spring Boot JAR: " + jarLocation.getPath(),
                                           e.getMessage(),
                                           JarAnalysisException.ErrorCode.ANALYSIS_FAILED,
                                           e);
        }
    }
    
    private Set<ClassInfo> analyzeSpringBootClasses(JarFile jarFile, JarLocation jarLocation) throws JarAnalysisException {
        Set<ClassInfo> classes = new HashSet<>();
        
        // FIXED: Use protected accessor for ClassPool
        try {
            getSharedClassPool().appendClassPath(jarLocation.getPath().toString());
        } catch (Exception e) {
            LOGGER.warning("Could not add Spring Boot JAR to ClassPool: " + e.getMessage());
        }
        
        // Analyze classes in BOOT-INF/classes/ directory
        jarFile.stream()
            .filter(this::isSpringBootClassFile)
            .forEach(entry -> {
                try {
                    ClassInfo classInfo = analyzeSpringBootClassEntry(entry, jarFile, jarLocation);
                    if (classInfo != null) {
                        classes.add(classInfo);
                    }
                } catch (Exception e) {
                    LOGGER.warning("Failed to analyze Spring Boot class " + entry.getName() + ": " + e.getMessage());
                }
            });
            
        LOGGER.info("Found " + classes.size() + " application classes in BOOT-INF/classes/");
        
        return classes;
    }
    
    private ClassInfo analyzeSpringBootClassEntry(JarEntry entry, JarFile jarFile, JarLocation jarLocation) {
        String className = getSpringBootClassName(entry);
        
        try {
            return ClassInfo.builder()
                .fullyQualifiedName(className)
                .classType(ClassType.CLASS) // Basic implementation
                .build();
        } catch (Exception e) {
            LOGGER.warning("Failed to analyze Spring Boot class " + className + ": " + e.getMessage());
            return null;
        }
    }
    
    private boolean isSpringBootClassFile(JarEntry entry) {
        return !entry.isDirectory() && 
               entry.getName().endsWith(".class") &&
               entry.getName().startsWith(BOOT_INF_CLASSES) &&
               !entry.getName().contains("$"); // Skip inner classes for basic analysis
    }
    
    private String getSpringBootClassName(JarEntry entry) {
        String name = entry.getName();
        
        // Remove BOOT-INF/classes/ prefix and .class suffix
        String classPath = name.substring(BOOT_INF_CLASSES.length());
        
        if (classPath.endsWith(".class")) {
            classPath = classPath.substring(0, classPath.length() - 6);
        }
        
        return classPath.replace('/', '.');
    }
    
    @Override
    protected JarType determineJarType(JarFile jarFile, JarLocation jarLocation) {
        return JarType.SPRING_BOOT_JAR;
    }
    
    /**
     * Analyzes Spring Boot specific configuration and dependencies.
     * 
     * @param jarFile the Spring Boot JAR file
     * @return analysis results specific to Spring Boot
     */
    public SpringBootAnalysisInfo analyzeSpringBootSpecifics(JarFile jarFile) {
        // This would analyze Spring Boot specific configurations
        // For now, return basic info
        return SpringBootAnalysisInfo.builder()
            .hasBootInfStructure(hasBootInfStructure(jarFile))
            .dependencyCount(countBootInfLibraries(jarFile))
            .build();
    }
    
    private boolean hasBootInfStructure(JarFile jarFile) {
        return jarFile.stream()
            .anyMatch(entry -> entry.getName().startsWith(BOOT_INF_CLASSES) || 
                              entry.getName().startsWith(BOOT_INF_LIB));
    }
    
    private int countBootInfLibraries(JarFile jarFile) {
        return (int) jarFile.stream()
            .filter(entry -> entry.getName().startsWith(BOOT_INF_LIB) && 
                           entry.getName().endsWith(".jar"))
            .count();
    }
    
    /**
     * Gets the Spring Boot detector used by this analyzer.
     * 
     * @return the SpringBootDetector instance
     */
    public SpringBootDetector getSpringBootDetector() {
        return springBootDetector;
    }
    
    /**
     * Basic Spring Boot analysis information.
     */
    public static class SpringBootAnalysisInfo {
        private final boolean hasBootInfStructure;
        private final int dependencyCount;
        
        private SpringBootAnalysisInfo(Builder builder) {
            this.hasBootInfStructure = builder.hasBootInfStructure;
            this.dependencyCount = builder.dependencyCount;
        }
        
        public boolean hasBootInfStructure() {
            return hasBootInfStructure;
        }
        
        public int getDependencyCount() {
            return dependencyCount;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private boolean hasBootInfStructure;
            private int dependencyCount;
            
            public Builder hasBootInfStructure(boolean hasBootInfStructure) {
                this.hasBootInfStructure = hasBootInfStructure;
                return this;
            }
            
            public Builder dependencyCount(int dependencyCount) {
                this.dependencyCount = dependencyCount;
                return this;
            }
            
            public SpringBootAnalysisInfo build() {
                return new SpringBootAnalysisInfo(this);
            }
        }
    }
}