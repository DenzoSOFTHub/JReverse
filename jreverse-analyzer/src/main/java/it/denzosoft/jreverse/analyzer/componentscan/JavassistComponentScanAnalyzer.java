package it.denzosoft.jreverse.analyzer.componentscan;

import it.denzosoft.jreverse.core.model.*;
import it.denzosoft.jreverse.core.port.ComponentScanAnalyzer;
import it.denzosoft.jreverse.core.logging.JReverseLogger;

import java.util.*;

/**
 * Javassist-based implementation for analyzing @ComponentScan configurations
 * in Spring Boot applications. Identifies component scanning strategies, 
 * base packages, filters, and scanning configurations.
 */
public class JavassistComponentScanAnalyzer implements ComponentScanAnalyzer {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistComponentScanAnalyzer.class);
    
    private static final String COMPONENT_SCAN_ANNOTATION = "org.springframework.context.annotation.ComponentScan";
    private static final String SPRING_BOOT_APPLICATION = "org.springframework.boot.autoconfigure.SpringBootApplication";
    
    @Override
    public ComponentScanAnalysisResult analyzeComponentScan(JarContent jarContent) {
        LOGGER.info("Starting component scan analysis for JAR: " + jarContent.getLocation().getFileName());
        
        long startTime = System.currentTimeMillis();
        ComponentScanAnalysisResult.Builder resultBuilder = ComponentScanAnalysisResult.builder();
        Set<String> effectivePackages = new HashSet<>();
        
        try {
            int configurationsFound = 0;
            
            // Search for classes with @ComponentScan or @SpringBootApplication
            Set<ClassInfo> componentScanClasses = jarContent.getClassesWithAnnotation(COMPONENT_SCAN_ANNOTATION);
            Set<ClassInfo> springBootAppClasses = jarContent.getClassesWithAnnotation(SPRING_BOOT_APPLICATION);
            
            // Process @ComponentScan annotations
            for (ClassInfo classInfo : componentScanClasses) {
                ComponentScanConfiguration config = extractComponentScanConfig(classInfo);
                if (config != null) {
                    resultBuilder.addConfiguration(config);
                    configurationsFound++;
                    
                    // Add effective packages
                    effectivePackages.addAll(config.getBasePackages());
                    
                    // If no base packages specified, use the package of the annotated class
                    if (config.getBasePackages().isEmpty() && config.getBasePackageClasses().isEmpty()) {
                        String packageName = classInfo.getPackageName();
                        if (packageName != null && !packageName.isEmpty()) {
                            effectivePackages.add(packageName);
                        }
                    }
                }
            }
            
            // Process @SpringBootApplication annotations  
            for (ClassInfo classInfo : springBootAppClasses) {
                ComponentScanConfiguration config = extractSpringBootApplicationConfig(classInfo);
                if (config != null) {
                    resultBuilder.addConfiguration(config);
                    configurationsFound++;
                    
                    // Add effective packages
                    effectivePackages.addAll(config.getBasePackages());
                    
                    // If no base packages specified, use the package of the SpringBootApplication class
                    if (config.getBasePackages().isEmpty() && config.getBasePackageClasses().isEmpty()) {
                        String packageName = classInfo.getPackageName();
                        if (packageName != null && !packageName.isEmpty()) {
                            effectivePackages.add(packageName);
                        }
                    }
                }
            }
            
            // Set effective packages
            resultBuilder.effectivePackages(effectivePackages);
            
            // Set metadata
            if (configurationsFound == 0) {
                resultBuilder.metadata(AnalysisMetadata.warning("No @ComponentScan configurations found"));
            } else {
                resultBuilder.metadata(AnalysisMetadata.successful());
            }
            
            long analysisTime = System.currentTimeMillis() - startTime;
            resultBuilder.analysisTimeMs(analysisTime);
            
            LOGGER.info(String.format("Component scan analysis completed in %dms. Found %d configurations",
                                     analysisTime, configurationsFound));
            
            return resultBuilder.build();
            
        } catch (Exception e) {
            LOGGER.severe("Error during component scan analysis: " + e.getMessage());
            return ComponentScanAnalysisResult.error("Analysis failed: " + e.getMessage());
        }
    }
    
    @Override
    public boolean canAnalyze(JarContent jarContent) {
        return jarContent != null && jarContent.getClassCount() > 0;
    }
    
    private ComponentScanConfiguration extractComponentScanConfig(ClassInfo classInfo) {
        try {
            // Check for @ComponentScan annotation
            AnnotationInfo componentScanAnnotation = classInfo.getAnnotation(COMPONENT_SCAN_ANNOTATION);
            if (componentScanAnnotation != null) {
                return parseComponentScanAnnotation(classInfo.getFullyQualifiedName(), componentScanAnnotation);
            }
            
        } catch (Exception e) {
            LOGGER.severe("Error extracting component scan config from " + classInfo.getFullyQualifiedName() + ": " + e.getMessage());
        }
        
        return null;
    }
    
    private ComponentScanConfiguration extractSpringBootApplicationConfig(ClassInfo classInfo) {
        try {
            // Check for @SpringBootApplication annotation
            AnnotationInfo springBootAppAnnotation = classInfo.getAnnotation(SPRING_BOOT_APPLICATION);
            if (springBootAppAnnotation != null) {
                return parseSpringBootApplicationAnnotation(classInfo.getFullyQualifiedName(), springBootAppAnnotation);
            }
            
        } catch (Exception e) {
            LOGGER.severe("Error extracting SpringBootApplication config from " + classInfo.getFullyQualifiedName() + ": " + e.getMessage());
        }
        
        return null;
    }
    
    private ComponentScanConfiguration parseComponentScanAnnotation(String sourceClass, AnnotationInfo annotation) {
        ComponentScanConfiguration.Builder builder = ComponentScanConfiguration.builder()
            .sourceClass(sourceClass);
        
        try {
            // Extract base packages from "basePackages" or "value" attribute
            Object basePackagesValue = annotation.getAttributeValue("basePackages");
            if (basePackagesValue == null) {
                basePackagesValue = annotation.getAttributeValue("value");
            }
            if (basePackagesValue != null) {
                Set<String> basePackages = extractStringArrayFromAttribute(basePackagesValue);
                for (String pkg : basePackages) {
                    if (pkg != null && !pkg.trim().isEmpty()) {
                        builder.addBasePackage(pkg.trim());
                    }
                }
            }
            
            // Extract base package classes
            Object basePackageClassesValue = annotation.getAttributeValue("basePackageClasses");
            if (basePackageClassesValue != null) {
                Set<String> basePackageClasses = extractClassArrayFromAttribute(basePackageClassesValue);
                for (String cls : basePackageClasses) {
                    if (cls != null && !cls.trim().isEmpty()) {
                        builder.addBasePackageClass(cls.trim());
                        // Also add the package of each class
                        String pkg = getPackageName(cls);
                        if (pkg != null && !pkg.isEmpty()) {
                            builder.addBasePackage(pkg);
                        }
                    }
                }
            }
            
            // Extract useDefaultFilters
            Object useDefaultFiltersValue = annotation.getAttributeValue("useDefaultFilters");
            if (useDefaultFiltersValue instanceof Boolean) {
                builder.useDefaultFilters((Boolean) useDefaultFiltersValue);
            }
            
            // Extract lazyInit
            Object lazyInitValue = annotation.getAttributeValue("lazyInit");
            if (lazyInitValue instanceof Boolean) {
                builder.lazyInit((Boolean) lazyInitValue);
            }
            
            // Note: For now, we skip include/exclude filters as they are complex to parse from AnnotationInfo
            // This can be enhanced in a future version if needed
            
        } catch (Exception e) {
            LOGGER.severe("Error parsing @ComponentScan annotation: " + e.getMessage());
        }
        
        return builder.build();
    }
    
    private ComponentScanConfiguration parseSpringBootApplicationAnnotation(String sourceClass, AnnotationInfo annotation) {
        ComponentScanConfiguration.Builder builder = ComponentScanConfiguration.builder()
            .sourceClass(sourceClass)
            .useDefaultFilters(true); // SpringBootApplication uses default filters
        
        try {
            // @SpringBootApplication can have scanBasePackages
            Object scanBasePackagesValue = annotation.getAttributeValue("scanBasePackages");
            if (scanBasePackagesValue != null) {
                Set<String> basePackages = extractStringArrayFromAttribute(scanBasePackagesValue);
                for (String pkg : basePackages) {
                    if (pkg != null && !pkg.trim().isEmpty()) {
                        builder.addBasePackage(pkg.trim());
                    }
                }
            }
            
            // @SpringBootApplication can have scanBasePackageClasses
            Object scanBasePackageClassesValue = annotation.getAttributeValue("scanBasePackageClasses");
            if (scanBasePackageClassesValue != null) {
                Set<String> basePackageClasses = extractClassArrayFromAttribute(scanBasePackageClassesValue);
                for (String cls : basePackageClasses) {
                    if (cls != null && !cls.trim().isEmpty()) {
                        builder.addBasePackageClass(cls.trim());
                        String pkg = getPackageName(cls);
                        if (pkg != null && !pkg.isEmpty()) {
                            builder.addBasePackage(pkg);
                        }
                    }
                }
            }
            
            // Note: For now, we skip exclude/excludeName filters as they are complex to parse from AnnotationInfo
            // This can be enhanced in a future version if needed
            
        } catch (Exception e) {
            LOGGER.severe("Error parsing @SpringBootApplication annotation: " + e.getMessage());
        }
        
        return builder.build();
    }
    
    private Set<String> extractStringArrayFromAttribute(Object value) {
        Set<String> result = new HashSet<>();
        
        try {
            if (value instanceof String[]) {
                String[] arrayValues = (String[]) value;
                for (String strValue : arrayValues) {
                    if (strValue != null && !strValue.trim().isEmpty()) {
                        result.add(strValue.trim());
                    }
                }
            } else if (value instanceof String) {
                String strValue = (String) value;
                if (!strValue.trim().isEmpty()) {
                    result.add(strValue.trim());
                }
            } else if (value instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> listValues = (List<Object>) value;
                for (Object obj : listValues) {
                    if (obj instanceof String) {
                        String strValue = (String) obj;
                        if (!strValue.trim().isEmpty()) {
                            result.add(strValue.trim());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Error extracting string array from attribute: " + e.getMessage());
        }
        
        return result;
    }
    
    private Set<String> extractClassArrayFromAttribute(Object value) {
        Set<String> result = new HashSet<>();
        
        try {
            if (value instanceof Class[]) {
                Class<?>[] classValues = (Class<?>[]) value;
                for (Class<?> clsValue : classValues) {
                    if (clsValue != null) {
                        result.add(clsValue.getName());
                    }
                }
            } else if (value instanceof Class) {
                Class<?> clsValue = (Class<?>) value;
                result.add(clsValue.getName());
            } else if (value instanceof String[]) {
                String[] stringValues = (String[]) value;
                for (String strValue : stringValues) {
                    if (strValue != null && !strValue.trim().isEmpty()) {
                        result.add(strValue.trim());
                    }
                }
            } else if (value instanceof String) {
                String strValue = (String) value;
                if (!strValue.trim().isEmpty()) {
                    result.add(strValue.trim());
                }
            } else if (value instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> listValues = (List<Object>) value;
                for (Object obj : listValues) {
                    if (obj instanceof Class) {
                        result.add(((Class<?>) obj).getName());
                    } else if (obj instanceof String) {
                        String strValue = (String) obj;
                        if (!strValue.trim().isEmpty()) {
                            result.add(strValue.trim());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Error extracting class array from attribute: " + e.getMessage());
        }
        
        return result;
    }
    
    private String getPackageName(String className) {
        if (className == null) {
            return null;
        }
        int lastDot = className.lastIndexOf('.');
        return lastDot > 0 ? className.substring(0, lastDot) : "";
    }
}