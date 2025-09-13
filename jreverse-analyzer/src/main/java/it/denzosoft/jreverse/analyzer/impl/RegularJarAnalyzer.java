package it.denzosoft.jreverse.analyzer.impl;

import it.denzosoft.jreverse.core.exception.JarAnalysisException;
import it.denzosoft.jreverse.core.model.*;
import javassist.ClassPool;

import java.io.IOException;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * Specialized analyzer for regular (non-Spring Boot) JAR files.
 * Provides optimized analysis for standard Java libraries and applications.
 */
public class RegularJarAnalyzer extends DefaultJarAnalyzer {
    
    private static final Logger LOGGER = Logger.getLogger(RegularJarAnalyzer.class.getName());
    
    public RegularJarAnalyzer(ClassPool classPool) {
        super(classPool);
    }
    
    @Override
    public JarContent analyzeJar(JarLocation jarLocation) throws JarAnalysisException {
        LOGGER.info("Starting regular JAR analysis of: " + jarLocation.getPath());
        
        try (JarFile jarFile = new JarFile(jarLocation.getPath().toFile())) {
            Set<ClassInfo> classes = analyzeClasses(jarFile, jarLocation);
            JarManifestInfo manifestInfo = analyzeManifest(jarFile);
            
            JarContent jarContent = JarContent.builder()
                .location(jarLocation)
                .jarType(determineRegularJarType(jarFile, jarLocation))
                .classes(classes)
                .manifest(manifestInfo)
                .build();
                
            LOGGER.info("Regular JAR analysis completed. Found " + classes.size() + " classes in " + 
                       jarLocation.getFileName());
            
            return jarContent;
            
        } catch (IOException e) {
            throw new JarAnalysisException("Failed to analyze regular JAR: " + jarLocation.getPath(),
                                           e.getMessage(),
                                           JarAnalysisException.ErrorCode.ANALYSIS_FAILED,
                                           e);
        }
    }
    
    private JarType determineRegularJarType(JarFile jarFile, JarLocation jarLocation) {
        String fileName = jarLocation.getFileName();
        
        // WAR file detection
        if (fileName.endsWith(".war")) {
            return JarType.WAR_ARCHIVE;
        }
        
        // Check if it's an executable JAR
        try {
            if (jarFile.getManifest() != null) {
                String mainClass = jarFile.getManifest().getMainAttributes().getValue("Main-Class");
                if (mainClass != null) {
                    LOGGER.fine("Detected executable JAR with main class: " + mainClass);
                    return JarType.EXECUTABLE_JAR;
                }
            }
        } catch (IOException e) {
            LOGGER.fine("Could not read manifest: " + e.getMessage());
        }
        
        // Check for common library patterns
        if (isLibraryJar(fileName)) {
            return JarType.LIBRARY_JAR;
        }
        
        return JarType.REGULAR_JAR;
    }
    
    private boolean isLibraryJar(String fileName) {
        // Common library JAR patterns
        return fileName.contains("-") && 
               (fileName.matches(".*-\\d+\\.\\d+.*\\.jar") || // Version pattern
                fileName.contains("commons-") ||
                fileName.contains("spring-") ||
                fileName.contains("jackson-") ||
                fileName.contains("slf4j-") ||
                fileName.contains("logback-"));
    }
    
    /**
     * Analyzes package structure specific to regular JARs.
     * 
     * @param jarFile the JAR file to analyze
     * @return package analysis information
     */
    public PackageAnalysisInfo analyzePackageStructure(JarFile jarFile) {
        Set<String> packages = extractPackages(jarFile);
        
        return PackageAnalysisInfo.builder()
            .totalPackages(packages.size())
            .topLevelPackages(countTopLevelPackages(packages))
            .deepestPackageLevel(findDeepestPackageLevel(packages))
            .build();
    }
    
    private Set<String> extractPackages(JarFile jarFile) {
        return jarFile.stream()
            .filter(entry -> !entry.isDirectory() && entry.getName().endsWith(".class"))
            .map(entry -> {
                String name = entry.getName();
                int lastSlash = name.lastIndexOf('/');
                return lastSlash > 0 ? name.substring(0, lastSlash).replace('/', '.') : "";
            })
            .collect(java.util.stream.Collectors.toSet());
    }
    
    private int countTopLevelPackages(Set<String> packages) {
        return (int) packages.stream()
            .filter(pkg -> !pkg.isEmpty())
            .map(pkg -> pkg.split("\\.")[0])
            .distinct()
            .count();
    }
    
    private int findDeepestPackageLevel(Set<String> packages) {
        return packages.stream()
            .filter(pkg -> !pkg.isEmpty())
            .mapToInt(pkg -> pkg.split("\\.").length)
            .max()
            .orElse(0);
    }
    
    /**
     * Package analysis information for regular JARs.
     */
    public static class PackageAnalysisInfo {
        private final int totalPackages;
        private final int topLevelPackages;
        private final int deepestPackageLevel;
        
        private PackageAnalysisInfo(Builder builder) {
            this.totalPackages = builder.totalPackages;
            this.topLevelPackages = builder.topLevelPackages;
            this.deepestPackageLevel = builder.deepestPackageLevel;
        }
        
        public int getTotalPackages() {
            return totalPackages;
        }
        
        public int getTopLevelPackages() {
            return topLevelPackages;
        }
        
        public int getDeepestPackageLevel() {
            return deepestPackageLevel;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private int totalPackages;
            private int topLevelPackages;
            private int deepestPackageLevel;
            
            public Builder totalPackages(int totalPackages) {
                this.totalPackages = totalPackages;
                return this;
            }
            
            public Builder topLevelPackages(int topLevelPackages) {
                this.topLevelPackages = topLevelPackages;
                return this;
            }
            
            public Builder deepestPackageLevel(int deepestPackageLevel) {
                this.deepestPackageLevel = deepestPackageLevel;
                return this;
            }
            
            public PackageAnalysisInfo build() {
                return new PackageAnalysisInfo(this);
            }
        }
    }
}