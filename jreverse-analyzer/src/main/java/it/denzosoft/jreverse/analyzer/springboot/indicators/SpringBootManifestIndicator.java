package it.denzosoft.jreverse.analyzer.springboot.indicators;

import it.denzosoft.jreverse.analyzer.springboot.SpringBootIndicator;
import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.JarManifestInfo;
import it.denzosoft.jreverse.core.model.springboot.IndicatorResult;
import it.denzosoft.jreverse.core.model.springboot.SpringBootIndicatorType;
import it.denzosoft.jreverse.core.model.springboot.SpringBootVersion;

import java.util.HashMap;
import java.util.Map;

/**
 * Indicator that analyzes JAR manifest for Spring Boot specific attributes.
 * This is typically the fastest and most reliable indicator for Spring Boot JARs.
 */
public class SpringBootManifestIndicator implements SpringBootIndicator {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(SpringBootManifestIndicator.class);
    
    // Spring Boot specific manifest attributes with their confidence weights
    private static final Map<String, Double> SPRING_BOOT_MANIFEST_ATTRIBUTES = Map.of(
        "Spring-Boot-Version", 0.85,  // Reduced from 0.95 to allow multiple indicators
        "Start-Class", 0.8,           // Reduced from 0.9
        "Spring-Boot-Lib", 0.75,      // Reduced from 0.85
        "Spring-Boot-Classes", 0.75,  // Reduced from 0.85
        "Spring-Boot-Classpath-Index", 0.7,  // Reduced from 0.8
        "Spring-Boot-Layers-Index", 0.65     // Reduced from 0.75
    );
    
    // Supporting attributes that may indicate Spring Boot
    private static final Map<String, Double> SUPPORTING_ATTRIBUTES = Map.of(
        "Main-Class", 0.3,  // Only if it's Spring Boot launcher
        "Implementation-Title", 0.1,
        "Implementation-Vendor", 0.05
    );
    
    // Known Spring Boot launcher main classes
    private static final Map<String, Double> SPRING_BOOT_LAUNCHERS = Map.of(
        "org.springframework.boot.loader.JarLauncher", 0.95,
        "org.springframework.boot.loader.WarLauncher", 0.95,
        "org.springframework.boot.loader.PropertiesLauncher", 0.9
    );
    
    @Override
    public IndicatorResult analyze(JarContent jarContent) {
        LOGGER.debug("Starting Spring Boot manifest analysis for JAR: %s", 
                    jarContent.getLocation().getFileName());
        
        long startTime = System.currentTimeMillis();
        
        try {
            JarManifestInfo manifest = jarContent.getManifest();
            if (manifest == null) {
                return IndicatorResult.notFound("No manifest found in JAR");
            }
            
            ManifestAnalysisResult analysisResult = analyzeManifest(manifest);
            
            double confidence = calculateConfidence(analysisResult);
            Map<String, Object> evidence = buildEvidence(analysisResult);
            
            long analysisTime = System.currentTimeMillis() - startTime;
            
            LOGGER.debug("Spring Boot manifest analysis completed. Confidence: %.2f, Time: %dms", 
                        confidence, analysisTime);
            
            return IndicatorResult.builder()
                .confidence(confidence)
                .status(confidence > 0.0 ? IndicatorResult.AnalysisStatus.SUCCESS : IndicatorResult.AnalysisStatus.NOT_FOUND)
                .evidence(evidence)
                .analysisTimeMs(analysisTime)
                .build();
                
        } catch (Exception e) {
            long analysisTime = System.currentTimeMillis() - startTime;
            LOGGER.error("Spring Boot manifest analysis failed", e);
            
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
        return SpringBootIndicatorType.MANIFEST;
    }
    
    @Override
    public boolean canAnalyze(JarContent jarContent) {
        return jarContent != null && jarContent.getManifest() != null;
    }
    
    private ManifestAnalysisResult analyzeManifest(JarManifestInfo manifest) {
        ManifestAnalysisResult.Builder resultBuilder = ManifestAnalysisResult.builder();
        
        Map<String, String> customAttributes = manifest.getCustomAttributes();
        if (customAttributes != null) {
            analyzeManifestAttributes(customAttributes, resultBuilder);
        }
        
        // Also check standard manifest attributes
        analyzeStandardAttributes(manifest, resultBuilder);
        
        return resultBuilder.build();
    }
    
    private void analyzeManifestAttributes(Map<String, String> attributes, ManifestAnalysisResult.Builder resultBuilder) {
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String attributeName = entry.getKey();
            String attributeValue = entry.getValue();
            
            // Check for Spring Boot specific attributes
            Double springBootWeight = SPRING_BOOT_MANIFEST_ATTRIBUTES.get(attributeName);
            if (springBootWeight != null) {
                resultBuilder.addSpringBootAttribute(attributeName, attributeValue, springBootWeight);
                
                // Extract specific information
                switch (attributeName) {
                    case "Spring-Boot-Version":
                        SpringBootVersion version = SpringBootVersion.parse(attributeValue);
                        if (version != null) {
                            resultBuilder.springBootVersion(version);
                        }
                        break;
                    case "Start-Class":
                        resultBuilder.startClass(attributeValue);
                        break;
                }
            }
            
            // Check for supporting attributes that might be in custom attributes (only if Spring-related)
            Double supportingWeight = SUPPORTING_ATTRIBUTES.get(attributeName);
            if (supportingWeight != null && isSpringRelatedValue(attributeValue)) {
                resultBuilder.addSupportingAttribute(attributeName, attributeValue, supportingWeight * 2);
            }
        }
    }
    
    private void analyzeStandardAttributes(JarManifestInfo manifest, ManifestAnalysisResult.Builder resultBuilder) {
        // Check Main-Class attribute
        String mainClass = manifest.getMainClass();
        if (mainClass != null) {
            Double launcherWeight = SPRING_BOOT_LAUNCHERS.get(mainClass);
            if (launcherWeight != null) {
                resultBuilder.addSpringBootAttribute("Launcher-Main-Class", mainClass, launcherWeight);
                resultBuilder.launcherMainClass(mainClass);
            } else {
                Double supportingWeight = SUPPORTING_ATTRIBUTES.get("Main-Class");
                if (supportingWeight != null) {
                    resultBuilder.addSupportingAttribute("Main-Class", mainClass, supportingWeight);
                    resultBuilder.customMainClass(mainClass);
                }
            }
        }
        
        // Check Implementation-Title (only add if Spring-related)
        String implTitle = manifest.getImplementationTitle();
        if (implTitle != null && isSpringRelatedValue(implTitle)) {
            Double supportingWeight = SUPPORTING_ATTRIBUTES.get("Implementation-Title");
            if (supportingWeight != null) {
                resultBuilder.addSupportingAttribute("Implementation-Title", implTitle, supportingWeight * 2);
            }
        }
        
        // Check Implementation-Vendor (only add if Spring-related)
        String implVendor = manifest.getImplementationVendor();
        if (implVendor != null && isSpringRelatedValue(implVendor)) {
            Double supportingWeight = SUPPORTING_ATTRIBUTES.get("Implementation-Vendor");
            if (supportingWeight != null) {
                resultBuilder.addSupportingAttribute("Implementation-Vendor", implVendor, supportingWeight * 2);
            }
        }
    }
    
    private boolean isSpringRelatedValue(String value) {
        if (value == null) return false;
        String lowerValue = value.toLowerCase();
        return lowerValue.contains("spring") || 
               lowerValue.contains("boot") ||
               lowerValue.contains("pivotal") ||
               lowerValue.contains("vmware");
    }
    
    private double calculateConfidence(ManifestAnalysisResult result) {
        double confidence = 0.0;
        
        // Primary confidence from Spring Boot specific attributes
        double springBootConfidence = result.getSpringBootAttributes().values().stream()
            .mapToDouble(Double::doubleValue)
            .max()
            .orElse(0.0);
        
        // Boost from supporting attributes
        double supportingBoost = Math.min(0.15, result.getSupportingAttributes().values().stream()
            .mapToDouble(Double::doubleValue)
            .sum() * 0.3);
        
        confidence = springBootConfidence + supportingBoost;
        
        // Additional boost for specific combinations
        if (result.getSpringBootVersion() != null && result.getStartClass() != null) {
            confidence = Math.min(0.95, confidence * 1.05);  // Reduced boost and max cap
        }
        
        if (result.getLauncherMainClass() != null) {
            confidence = Math.min(0.95, confidence * 1.02);  // Reduced boost and max cap
        }
        
        // Boost for multiple Spring Boot attributes
        if (result.getSpringBootAttributes().size() > 2) {
            confidence = Math.min(0.95, confidence * 1.01);  // Reduced boost and max cap
        }
        
        return Math.max(0.0, Math.min(1.0, confidence));
    }
    
    private Map<String, Object> buildEvidence(ManifestAnalysisResult result) {
        Map<String, Object> evidence = new HashMap<>();
        
        evidence.put("springBootAttributes", result.getSpringBootAttributes());
        evidence.put("supportingAttributes", result.getSupportingAttributes());
        evidence.put("attributeCount", result.getSpringBootAttributes().size());
        
        if (result.getSpringBootVersion() != null) {
            evidence.put("springBootVersion", result.getSpringBootVersion().getVersionString());
            evidence.put("springBootMajorVersion", result.getSpringBootVersion().getMajor());
            evidence.put("springBootGeneration", result.getSpringBootVersion().getGeneration());
        }
        
        if (result.getStartClass() != null) {
            evidence.put("startClass", result.getStartClass());
        }
        
        if (result.getLauncherMainClass() != null) {
            evidence.put("launcherMainClass", result.getLauncherMainClass());
            evidence.put("isSpringBootJar", true);
        }
        
        if (result.getCustomMainClass() != null) {
            evidence.put("customMainClass", result.getCustomMainClass());
        }
        
        return evidence;
    }
    
    /**
     * Internal class to hold manifest analysis results.
     */
    private static class ManifestAnalysisResult {
        private final Map<String, Double> springBootAttributes;
        private final Map<String, Double> supportingAttributes;
        private final SpringBootVersion springBootVersion;
        private final String startClass;
        private final String launcherMainClass;
        private final String customMainClass;
        
        private ManifestAnalysisResult(Builder builder) {
            this.springBootAttributes = Map.copyOf(builder.springBootAttributes);
            this.supportingAttributes = Map.copyOf(builder.supportingAttributes);
            this.springBootVersion = builder.springBootVersion;
            this.startClass = builder.startClass;
            this.launcherMainClass = builder.launcherMainClass;
            this.customMainClass = builder.customMainClass;
        }
        
        public Map<String, Double> getSpringBootAttributes() {
            return springBootAttributes;
        }
        
        public Map<String, Double> getSupportingAttributes() {
            return supportingAttributes;
        }
        
        public SpringBootVersion getSpringBootVersion() {
            return springBootVersion;
        }
        
        public String getStartClass() {
            return startClass;
        }
        
        public String getLauncherMainClass() {
            return launcherMainClass;
        }
        
        public String getCustomMainClass() {
            return customMainClass;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private final Map<String, Double> springBootAttributes = new HashMap<>();
            private final Map<String, Double> supportingAttributes = new HashMap<>();
            private SpringBootVersion springBootVersion;
            private String startClass;
            private String launcherMainClass;
            private String customMainClass;
            
            public Builder addSpringBootAttribute(String name, String value, double weight) {
                springBootAttributes.put(name, weight);
                return this;
            }
            
            public Builder addSupportingAttribute(String name, String value, double weight) {
                supportingAttributes.put(name, weight);
                return this;
            }
            
            public Builder springBootVersion(SpringBootVersion version) {
                this.springBootVersion = version;
                return this;
            }
            
            public Builder startClass(String startClass) {
                this.startClass = startClass;
                return this;
            }
            
            public Builder launcherMainClass(String launcherMainClass) {
                this.launcherMainClass = launcherMainClass;
                return this;
            }
            
            public Builder customMainClass(String customMainClass) {
                this.customMainClass = customMainClass;
                return this;
            }
            
            public ManifestAnalysisResult build() {
                return new ManifestAnalysisResult(this);
            }
        }
    }
}