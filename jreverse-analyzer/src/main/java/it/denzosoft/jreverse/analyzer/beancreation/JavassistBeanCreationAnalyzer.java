package it.denzosoft.jreverse.analyzer.beancreation;

import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.AnnotationInfo;
import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.MethodInfo;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of BeanCreationAnalyzer that analyzes Spring bean creation patterns,
 * dependencies, and relationships using the existing ClassInfo model from JReverse core.
 */
public class JavassistBeanCreationAnalyzer implements BeanCreationAnalyzer {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistBeanCreationAnalyzer.class);
    
    // Spring bean creation annotations
    private static final Set<String> BEAN_CREATION_ANNOTATIONS = Set.of(
        "org.springframework.context.annotation.Bean",
        "org.springframework.stereotype.Component",
        "org.springframework.stereotype.Service",
        "org.springframework.stereotype.Repository",
        "org.springframework.stereotype.Controller",
        "org.springframework.web.bind.annotation.RestController",
        "org.springframework.context.annotation.Configuration"
    );
    
    // Spring dependency injection annotations
    private static final Set<String> INJECTION_ANNOTATIONS = Set.of(
        "org.springframework.beans.factory.annotation.Autowired",
        "org.springframework.beans.factory.annotation.Value",
        "org.springframework.beans.factory.annotation.Qualifier",
        "javax.inject.Inject",
        "javax.annotation.Resource"
    );
    
    // Spring scope annotations
    private static final Set<String> SCOPE_ANNOTATIONS = Set.of(
        "org.springframework.context.annotation.Scope",
        "org.springframework.web.context.annotation.RequestScope",
        "org.springframework.web.context.annotation.SessionScope",
        "org.springframework.web.context.annotation.ApplicationScope"
    );
    
    private final BeanDependencyAnalyzer dependencyAnalyzer;
    
    public JavassistBeanCreationAnalyzer() {
        this.dependencyAnalyzer = new BeanDependencyAnalyzer();
    }
    
    @Override
    public BeanCreationResult analyzeBeanCreation(JarContent jarContent) {
        if (jarContent == null) {
            LOGGER.warn("Cannot analyze null JarContent");
            return BeanCreationResult.error("JAR content cannot be null");
        }
        
        long startTime = System.currentTimeMillis();
        
        LOGGER.info("Starting bean creation analysis for JAR: %s", jarContent.getLocation().getFileName());
        
        try {
            List<BeanInfo> allBeans = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            
            // Analyze each class for bean creation patterns
            for (ClassInfo classInfo : jarContent.getClasses()) {
                try {
                    List<BeanInfo> classBeansInfo = analyzeClass(classInfo);
                    allBeans.addAll(classBeansInfo);
                } catch (Exception e) {
                    String warning = String.format("Failed to analyze class %s: %s", 
                        classInfo.getFullyQualifiedName(), e.getMessage());
                    warnings.add(warning);
                    LOGGER.warn(warning, e);
                }
            }
            
            LOGGER.info("Found %d Spring beans in %d classes", allBeans.size(), jarContent.getClassCount());
            
            if (allBeans.isEmpty()) {
                return BeanCreationResult.noBeans();
            }
            
            return warnings.isEmpty() ? 
                BeanCreationResult.success(allBeans) : 
                BeanCreationResult.withWarnings(allBeans, warnings);
                
        } catch (Exception e) {
            long analysisTime = System.currentTimeMillis() - startTime;
            LOGGER.error("Bean creation analysis failed after " + analysisTime + "ms", e);
            return BeanCreationResult.error("Analysis failed: " + e.getMessage());
            
        } finally {
            long analysisTime = System.currentTimeMillis() - startTime;
            LOGGER.info("Bean creation analysis completed in %dms", analysisTime);
        }
    }
    
    /**
     * Analyzes a single class for Spring bean creation patterns.
     */
    private List<BeanInfo> analyzeClass(ClassInfo classInfo) throws Exception {
        List<BeanInfo> beans = new ArrayList<>();
        
        // Check if class itself is a bean (stereotype annotations)
        BeanCreationType classCreationType = getClassBeanCreationType(classInfo);
        if (classCreationType != null) {
            BeanInfo classBean = createClassBean(classInfo, classCreationType);
            beans.add(classBean);
        }
        
        // Check for @Bean methods if class is a @Configuration
        if (hasConfigurationAnnotation(classInfo)) {
            List<BeanInfo> beanMethodBeans = analyzeBeanMethods(classInfo);
            beans.addAll(beanMethodBeans);
        }
        
        return beans;
    }
    
    /**
     * Determines if a class is a Spring bean based on stereotype annotations.
     */
    private BeanCreationType getClassBeanCreationType(ClassInfo classInfo) {
        for (AnnotationInfo annotation : classInfo.getAnnotations()) {
            BeanCreationType type = BeanCreationType.fromAnnotation(annotation.getType());
            if (type != null && type != BeanCreationType.BEAN_METHOD) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * Creates a BeanInfo for a class-level bean (stereotype annotations).
     */
    private BeanInfo createClassBean(ClassInfo classInfo, BeanCreationType creationType) throws Exception {
        String beanName = deriveBeanName(classInfo, creationType);
        String beanType = classInfo.getFullyQualifiedName();
        
        BeanInfo.Builder builder = BeanInfo.builder()
            .beanName(beanName)
            .beanType(beanType)
            .creationType(creationType)
            .declaringClass(classInfo);
        
        // Extract annotation information
        List<String> annotations = classInfo.getAnnotations().stream()
            .map(AnnotationInfo::getType)
            .collect(Collectors.toList());
        builder.annotations(annotations);
        
        // Extract scope information
        BeanScope scope = extractScope(classInfo.getAnnotations());
        builder.scope(scope);
        
        // Extract lazy and primary information
        boolean isLazy = hasAnnotation(classInfo.getAnnotations(), "org.springframework.context.annotation.Lazy");
        boolean isPrimary = hasAnnotation(classInfo.getAnnotations(), "org.springframework.context.annotation.Primary");
        builder.isLazy(isLazy).isPrimary(isPrimary);
        
        // Extract profile information
        Set<String> profiles = extractProfiles(classInfo.getAnnotations());
        builder.profiles(profiles);
        
        // Extract qualifier information
        String qualifier = extractQualifier(classInfo.getAnnotations());
        if (qualifier != null) {
            builder.qualifier(qualifier);
        }
        
        // Analyze dependencies
        List<BeanDependency> dependencies = dependencyAnalyzer.analyzeDependencies(classInfo);
        builder.dependencies(dependencies);
        
        return builder.build();
    }
    
    /**
     * Checks if class has @Configuration annotation.
     */
    private boolean hasConfigurationAnnotation(ClassInfo classInfo) {
        return hasAnnotation(classInfo.getAnnotations(), "org.springframework.context.annotation.Configuration");
    }
    
    /**
     * Analyzes @Bean methods in a @Configuration class.
     */
    private List<BeanInfo> analyzeBeanMethods(ClassInfo classInfo) throws Exception {
        List<BeanInfo> beanMethods = new ArrayList<>();
        
        for (it.denzosoft.jreverse.core.model.MethodInfo methodInfo : classInfo.getMethods()) {
            if (hasAnnotation(methodInfo.getAnnotations(), "org.springframework.context.annotation.Bean")) {
                BeanInfo beanMethodBean = createBeanMethodBean(classInfo, methodInfo);
                beanMethods.add(beanMethodBean);
            }
        }
        
        return beanMethods;
    }
    
    /**
     * Creates a BeanInfo for a @Bean method.
     */
    private BeanInfo createBeanMethodBean(ClassInfo classInfo, it.denzosoft.jreverse.core.model.MethodInfo methodInfo) {
        String beanName = methodInfo.getName(); // Default bean name is method name
        String beanType = methodInfo.getReturnType();
        
        BeanInfo.Builder builder = BeanInfo.builder()
            .beanName(beanName)
            .beanType(beanType)
            .creationType(BeanCreationType.BEAN_METHOD)
            .declaringClass(classInfo)
            .creationMethod(methodInfo);
        
        // Extract method annotation information
        List<String> annotations = methodInfo.getAnnotations().stream()
            .map(AnnotationInfo::getType)
            .collect(Collectors.toList());
        builder.annotations(annotations);
        
        // Extract scope information from method annotations
        BeanScope scope = extractScope(methodInfo.getAnnotations());
        builder.scope(scope);
        
        // Extract lazy and primary information from method annotations
        boolean isLazy = hasAnnotation(methodInfo.getAnnotations(), "org.springframework.context.annotation.Lazy");
        boolean isPrimary = hasAnnotation(methodInfo.getAnnotations(), "org.springframework.context.annotation.Primary");
        builder.isLazy(isLazy).isPrimary(isPrimary);
        
        // Extract profile information from method annotations
        Set<String> profiles = extractProfiles(methodInfo.getAnnotations());
        builder.profiles(profiles);
        
        // Extract qualifier information from method annotations
        String qualifier = extractQualifier(methodInfo.getAnnotations());
        if (qualifier != null) {
            builder.qualifier(qualifier);
        }
        
        // Analyze method parameter dependencies
        List<BeanDependency> dependencies = dependencyAnalyzer.analyzeMethodDependencies(methodInfo);
        builder.dependencies(dependencies);
        
        return builder.build();
    }
    
    /**
     * Derives the bean name based on class and creation type.
     */
    private String deriveBeanName(ClassInfo classInfo, BeanCreationType creationType) {
        // Check if there's a custom name specified in the annotation
        for (AnnotationInfo annotation : classInfo.getAnnotations()) {
            if (BEAN_CREATION_ANNOTATIONS.contains(annotation.getType())) {
                String customName = annotation.getStringAttribute("value");
                if (customName != null && !customName.trim().isEmpty()) {
                    return customName.trim();
                }
                
                String[] nameArray = annotation.getStringArrayAttribute("value");
                if (nameArray != null && nameArray.length > 0 && !nameArray[0].trim().isEmpty()) {
                    return nameArray[0].trim();
                }
            }
        }
        
        // Default to class simple name with first letter lowercase
        String simpleName = classInfo.getSimpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }
    
    /**
     * Extracts scope information from annotations.
     */
    private BeanScope extractScope(Set<AnnotationInfo> annotations) {
        for (AnnotationInfo annotation : annotations) {
            if ("org.springframework.context.annotation.Scope".equals(annotation.getType())) {
                String scopeValue = annotation.getStringAttribute("value");
                if (scopeValue != null) {
                    return BeanScope.fromScopeName(scopeValue);
                }
                scopeValue = annotation.getStringAttribute("scopeName");
                if (scopeValue != null) {
                    return BeanScope.fromScopeName(scopeValue);
                }
            }
            
            // Check for specific scope annotations
            switch (annotation.getType()) {
                case "org.springframework.web.context.annotation.RequestScope":
                    return BeanScope.REQUEST;
                case "org.springframework.web.context.annotation.SessionScope":
                    return BeanScope.SESSION;
                case "org.springframework.web.context.annotation.ApplicationScope":
                    return BeanScope.APPLICATION;
            }
        }
        
        return BeanScope.SINGLETON; // Default scope
    }
    
    /**
     * Extracts profile information from annotations.
     */
    private Set<String> extractProfiles(Set<AnnotationInfo> annotations) {
        Set<String> profiles = new HashSet<>();
        
        for (AnnotationInfo annotation : annotations) {
            if ("org.springframework.context.annotation.Profile".equals(annotation.getType())) {
                String[] profileArray = annotation.getStringArrayAttribute("value");
                if (profileArray != null) {
                    profiles.addAll(Arrays.asList(profileArray));
                }
                
                String singleProfile = annotation.getStringAttribute("value");
                if (singleProfile != null && !singleProfile.trim().isEmpty()) {
                    profiles.add(singleProfile.trim());
                }
            }
        }
        
        return profiles;
    }
    
    /**
     * Extracts qualifier information from annotations.
     */
    private String extractQualifier(Set<AnnotationInfo> annotations) {
        for (AnnotationInfo annotation : annotations) {
            if ("org.springframework.beans.factory.annotation.Qualifier".equals(annotation.getType())) {
                String qualifier = annotation.getStringAttribute("value");
                if (qualifier != null && !qualifier.trim().isEmpty()) {
                    return qualifier.trim();
                }
            }
        }
        return null;
    }
    
    /**
     * Checks if a set of annotations contains a specific annotation type.
     */
    private boolean hasAnnotation(Set<AnnotationInfo> annotations, String annotationType) {
        return annotations.stream()
            .anyMatch(annotation -> annotation.getType().equals(annotationType));
    }
}