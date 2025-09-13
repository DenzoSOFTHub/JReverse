package it.denzosoft.jreverse.analyzer.detector;

import it.denzosoft.jreverse.core.model.JarLocation;
import it.denzosoft.jreverse.core.model.JarType;

import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Logger;

/**
 * Detector for Spring Boot applications.
 * Analyzes JAR files to determine if they are Spring Boot applications.
 */
public class SpringBootDetector {
    
    private static final Logger LOGGER = Logger.getLogger(SpringBootDetector.class.getName());
    
    // Spring Boot specific indicators
    private static final String SPRING_BOOT_LOADER_CLASS = "org/springframework/boot/loader/JarLauncher.class";
    private static final String SPRING_BOOT_MAIN_CLASS_ATTRIBUTE = "Start-Class";
    private static final String SPRING_BOOT_VERSION_ATTRIBUTE = "Spring-Boot-Version";
    private static final String BOOT_INF_CLASSES = "BOOT-INF/classes/";
    private static final String BOOT_INF_LIB = "BOOT-INF/lib/";
    
    /**
     * Determines if the given JAR location represents a Spring Boot application.
     * 
     * @param jarLocation the JAR file location to analyze
     * @return true if the JAR is detected as a Spring Boot application
     */
    public boolean isSpringBootJar(JarLocation jarLocation) {
        LOGGER.fine("Detecting Spring Boot JAR: " + jarLocation.getPath());
        
        try (JarFile jarFile = new JarFile(jarLocation.getPath().toFile())) {
            return hasSpringBootIndicators(jarFile);
        } catch (IOException e) {
            LOGGER.warning("Failed to analyze JAR for Spring Boot detection: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets the detected JAR type based on Spring Boot analysis.
     * 
     * @param jarLocation the JAR file location to analyze
     * @return the detected JAR type
     */
    public JarType detectJarType(JarLocation jarLocation) {
        if (isSpringBootJar(jarLocation)) {
            return JarType.SPRING_BOOT_JAR;
        }
        
        // Additional type detection logic can be added here
        if (jarLocation.getFileName().endsWith(".war")) {
            return JarType.WAR_ARCHIVE;
        }
        
        return JarType.REGULAR_JAR;
    }
    
    private boolean hasSpringBootIndicators(JarFile jarFile) throws IOException {
        // Check for Spring Boot Loader classes
        if (jarFile.getEntry(SPRING_BOOT_LOADER_CLASS) != null) {
            LOGGER.fine("Found Spring Boot loader class indicator");
            return true;
        }
        
        // Check for BOOT-INF directory structure
        if (hasBootInfStructure(jarFile)) {
            LOGGER.fine("Found BOOT-INF directory structure");
            return true;
        }
        
        // Check manifest for Spring Boot attributes
        if (hasSpringBootManifestAttributes(jarFile)) {
            LOGGER.fine("Found Spring Boot manifest attributes");
            return true;
        }
        
        return false;
    }
    
    private boolean hasBootInfStructure(JarFile jarFile) {
        // Check for BOOT-INF/classes/ and BOOT-INF/lib/ directories
        return jarFile.entries().asIterator().hasNext() && 
               (jarFile.getEntry(BOOT_INF_CLASSES) != null || 
                jarFile.getEntry(BOOT_INF_LIB) != null ||
                jarFile.stream().anyMatch(entry -> 
                    entry.getName().startsWith(BOOT_INF_CLASSES) || 
                    entry.getName().startsWith(BOOT_INF_LIB)));
    }
    
    private boolean hasSpringBootManifestAttributes(JarFile jarFile) throws IOException {
        Manifest manifest = jarFile.getManifest();
        if (manifest == null) {
            return false;
        }
        
        String startClass = manifest.getMainAttributes().getValue(SPRING_BOOT_MAIN_CLASS_ATTRIBUTE);
        String springBootVersion = manifest.getMainAttributes().getValue(SPRING_BOOT_VERSION_ATTRIBUTE);
        
        return startClass != null || springBootVersion != null;
    }
    
    /**
     * Extracts Spring Boot version from the JAR if available.
     * 
     * @param jarLocation the JAR file location
     * @return Spring Boot version or null if not found
     */
    public String getSpringBootVersion(JarLocation jarLocation) {
        try (JarFile jarFile = new JarFile(jarLocation.getPath().toFile())) {
            Manifest manifest = jarFile.getManifest();
            if (manifest != null) {
                return manifest.getMainAttributes().getValue(SPRING_BOOT_VERSION_ATTRIBUTE);
            }
        } catch (IOException e) {
            LOGGER.warning("Failed to read Spring Boot version: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Extracts the main application class from Spring Boot JAR.
     * 
     * @param jarLocation the JAR file location
     * @return main class name or null if not found
     */
    public String getMainClass(JarLocation jarLocation) {
        try (JarFile jarFile = new JarFile(jarLocation.getPath().toFile())) {
            Manifest manifest = jarFile.getManifest();
            if (manifest != null) {
                // Try Start-Class first (Spring Boot specific)
                String startClass = manifest.getMainAttributes().getValue(SPRING_BOOT_MAIN_CLASS_ATTRIBUTE);
                if (startClass != null) {
                    return startClass;
                }
                
                // Fall back to Main-Class
                return manifest.getMainAttributes().getValue("Main-Class");
            }
        } catch (IOException e) {
            LOGGER.warning("Failed to read main class: " + e.getMessage());
        }
        return null;
    }
}