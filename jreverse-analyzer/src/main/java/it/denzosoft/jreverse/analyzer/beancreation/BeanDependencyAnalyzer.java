package it.denzosoft.jreverse.analyzer.beancreation;

import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.AnnotationInfo;
import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.FieldInfo;
import it.denzosoft.jreverse.core.model.MethodInfo;
import it.denzosoft.jreverse.core.model.ParameterInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Analyzes bean dependencies including constructor injection, field injection,
 * setter injection, and method parameter injection patterns.
 */
public class BeanDependencyAnalyzer {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(BeanDependencyAnalyzer.class);
    
    // Spring dependency injection annotations
    private static final Set<String> AUTOWIRED_ANNOTATIONS = Set.of(
        "org.springframework.beans.factory.annotation.Autowired",
        "javax.inject.Inject"
    );
    
    private static final Set<String> RESOURCE_ANNOTATIONS = Set.of(
        "javax.annotation.Resource"
    );
    
    private static final Set<String> QUALIFIER_ANNOTATIONS = Set.of(
        "org.springframework.beans.factory.annotation.Qualifier",
        "javax.inject.Named"
    );
    
    private static final Set<String> COLLECTION_TYPES = Set.of(
        "java.util.List",
        "java.util.Set",
        "java.util.Collection",
        "java.util.Map"
    );
    
    /**
     * Analyzes dependencies for a class-level bean (stereotype annotations).
     */
    public List<BeanDependency> analyzeDependencies(ClassInfo classInfo) {
        List<BeanDependency> dependencies = new ArrayList<>();
        
        try {
            // Analyze constructor injection
            dependencies.addAll(analyzeConstructorDependencies(classInfo));
            
            // Analyze field injection
            dependencies.addAll(analyzeFieldDependencies(classInfo));
            
            // Analyze setter injection
            dependencies.addAll(analyzeSetterDependencies(classInfo));
            
            // Analyze other method injection
            dependencies.addAll(analyzeMethodDependencies(classInfo));
            
        } catch (Exception e) {
            LOGGER.warn("Failed to analyze dependencies for class %s: %s", 
                classInfo.getFullyQualifiedName(), e.getMessage());
        }
        
        return dependencies;
    }
    
    /**
     * Analyzes dependencies for a @Bean method based on its parameters.
     */
    public List<BeanDependency> analyzeMethodDependencies(MethodInfo methodInfo) {
        List<BeanDependency> dependencies = new ArrayList<>();
        
        try {
            // For @Bean methods, all parameters are dependencies
            for (ParameterInfo parameter : methodInfo.getParameters()) {
                BeanDependency dependency = createParameterDependency(parameter);
                dependencies.add(dependency);
            }
            
        } catch (Exception e) {
            LOGGER.warn("Failed to analyze method dependencies for %s: %s", 
                methodInfo.getName(), e.getMessage());
        }
        
        return dependencies;
    }
    
    /**
     * Analyzes constructor injection dependencies.
     */
    private List<BeanDependency> analyzeConstructorDependencies(ClassInfo classInfo) {
        List<BeanDependency> dependencies = new ArrayList<>();
        
        // Find constructors with @Autowired or single constructor with parameters
        for (MethodInfo method : classInfo.getMethods()) {
            if (isConstructor(method)) {
                boolean isAutowired = hasAnyAnnotation(method.getAnnotations(), AUTOWIRED_ANNOTATIONS);
                boolean hasParameters = !method.getParameters().isEmpty();
                
                // Spring autowires single constructor with parameters automatically
                if (isAutowired || (hasParameters && isOnlyParameterizedConstructor(classInfo))) {
                    for (ParameterInfo parameter : method.getParameters()) {
                        BeanDependency dependency = BeanDependency.builder()
                            .type(parameter.getType())
                            .name(parameter.getName())
                            .injectionType(DependencyInjectionType.CONSTRUCTOR)
                            .isRequired(isParameterRequired(parameter))
                            .qualifier(extractQualifier(parameter.getAnnotations()))
                            .isCollection(isCollectionType(parameter.getType()))
                            .injectionPoint(parameter.getName())
                            .build();
                        dependencies.add(dependency);
                    }
                }
            }
        }
        
        return dependencies;
    }
    
    /**
     * Analyzes field injection dependencies.
     */
    private List<BeanDependency> analyzeFieldDependencies(ClassInfo classInfo) {
        List<BeanDependency> dependencies = new ArrayList<>();
        
        for (FieldInfo field : classInfo.getFields()) {
            if (hasAnyAnnotation(field.getAnnotations(), AUTOWIRED_ANNOTATIONS) ||
                hasAnyAnnotation(field.getAnnotations(), RESOURCE_ANNOTATIONS)) {
                
                DependencyInjectionType injectionType = hasAnyAnnotation(field.getAnnotations(), RESOURCE_ANNOTATIONS) ?
                    DependencyInjectionType.RESOURCE : DependencyInjectionType.FIELD;
                
                BeanDependency dependency = BeanDependency.builder()
                    .type(field.getType())
                    .name(field.getName())
                    .injectionType(injectionType)
                    .isRequired(isFieldRequired(field))
                    .qualifier(extractQualifier(field.getAnnotations()))
                    .isCollection(isCollectionType(field.getType()))
                    .injectionPoint(field.getName())
                    .build();
                dependencies.add(dependency);
            }
        }
        
        return dependencies;
    }
    
    /**
     * Analyzes setter injection dependencies.
     */
    private List<BeanDependency> analyzeSetterDependencies(ClassInfo classInfo) {
        List<BeanDependency> dependencies = new ArrayList<>();
        
        for (MethodInfo method : classInfo.getMethods()) {
            if (isSetterMethod(method) && 
                hasAnyAnnotation(method.getAnnotations(), AUTOWIRED_ANNOTATIONS)) {
                
                // Setter methods should have exactly one parameter
                if (method.getParameters().size() == 1) {
                    ParameterInfo parameter = method.getParameters().get(0);
                    
                    BeanDependency dependency = BeanDependency.builder()
                        .type(parameter.getType())
                        .name(derivePropertyName(method.getName()))
                        .injectionType(DependencyInjectionType.SETTER)
                        .isRequired(isMethodRequired(method))
                        .qualifier(extractQualifier(method.getAnnotations()))
                        .isCollection(isCollectionType(parameter.getType()))
                        .injectionPoint(method.getName())
                        .build();
                    dependencies.add(dependency);
                }
            }
        }
        
        return dependencies;
    }
    
    /**
     * Analyzes other method injection dependencies (non-setter methods with @Autowired).
     */
    private List<BeanDependency> analyzeMethodDependencies(ClassInfo classInfo) {
        List<BeanDependency> dependencies = new ArrayList<>();
        
        for (MethodInfo method : classInfo.getMethods()) {
            if (!isConstructor(method) && !isSetterMethod(method) &&
                hasAnyAnnotation(method.getAnnotations(), AUTOWIRED_ANNOTATIONS)) {
                
                for (ParameterInfo parameter : method.getParameters()) {
                    BeanDependency dependency = BeanDependency.builder()
                        .type(parameter.getType())
                        .name(parameter.getName())
                        .injectionType(DependencyInjectionType.METHOD)
                        .isRequired(isMethodRequired(method))
                        .qualifier(extractQualifier(parameter.getAnnotations()))
                        .isCollection(isCollectionType(parameter.getType()))
                        .injectionPoint(method.getName() + "." + parameter.getName())
                        .build();
                    dependencies.add(dependency);
                }
            }
        }
        
        return dependencies;
    }
    
    /**
     * Creates a dependency from a method parameter (for @Bean methods).
     */
    private BeanDependency createParameterDependency(ParameterInfo parameter) {
        return BeanDependency.builder()
            .type(parameter.getType())
            .name(parameter.getName())
            .injectionType(DependencyInjectionType.PARAMETER)
            .isRequired(isParameterRequired(parameter))
            .qualifier(extractQualifier(parameter.getAnnotations()))
            .isCollection(isCollectionType(parameter.getType()))
            .injectionPoint(parameter.getName())
            .build();
    }
    
    /**
     * Checks if a method is a constructor.
     */
    private boolean isConstructor(MethodInfo method) {
        return "<init>".equals(method.getName());
    }
    
    /**
     * Checks if a method is a setter method.
     */
    private boolean isSetterMethod(MethodInfo method) {
        return method.getName().startsWith("set") && 
               method.getName().length() > 3 &&
               Character.isUpperCase(method.getName().charAt(3)) &&
               method.getParameters().size() == 1 &&
               "void".equals(method.getReturnType());
    }
    
    /**
     * Checks if this is the only constructor with parameters in the class.
     */
    private boolean isOnlyParameterizedConstructor(ClassInfo classInfo) {
        List<MethodInfo> constructors = classInfo.getMethods().stream()
            .filter(this::isConstructor)
            .collect(java.util.stream.Collectors.toList());
            
        long parameterizedConstructors = constructors.stream()
            .filter(constructor -> !constructor.getParameters().isEmpty())
            .count();
            
        return parameterizedConstructors == 1;
    }
    
    /**
     * Derives property name from setter method name.
     */
    private String derivePropertyName(String setterName) {
        if (setterName.startsWith("set") && setterName.length() > 3) {
            String propertyName = setterName.substring(3);
            return Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
        }
        return setterName;
    }
    
    /**
     * Checks if a parameter is required (not marked as @Autowired(required=false)).
     */
    private boolean isParameterRequired(ParameterInfo parameter) {
        for (AnnotationInfo annotation : parameter.getAnnotations()) {
            if (AUTOWIRED_ANNOTATIONS.contains(annotation.getType())) {
                Boolean required = annotation.getBooleanAttribute("required");
                return required == null || required; // Default is true
            }
        }
        return true;
    }
    
    /**
     * Checks if a field is required for injection.
     */
    private boolean isFieldRequired(FieldInfo field) {
        for (AnnotationInfo annotation : field.getAnnotations()) {
            if (AUTOWIRED_ANNOTATIONS.contains(annotation.getType())) {
                Boolean required = annotation.getBooleanAttribute("required");
                return required == null || required; // Default is true
            }
        }
        return true;
    }
    
    /**
     * Checks if a method is required for injection.
     */
    private boolean isMethodRequired(MethodInfo method) {
        for (AnnotationInfo annotation : method.getAnnotations()) {
            if (AUTOWIRED_ANNOTATIONS.contains(annotation.getType())) {
                Boolean required = annotation.getBooleanAttribute("required");
                return required == null || required; // Default is true
            }
        }
        return true;
    }
    
    /**
     * Extracts qualifier information from annotations.
     */
    private String extractQualifier(Set<AnnotationInfo> annotations) {
        for (AnnotationInfo annotation : annotations) {
            if (QUALIFIER_ANNOTATIONS.contains(annotation.getType())) {
                String qualifier = annotation.getStringAttribute("value");
                if (qualifier != null && !qualifier.trim().isEmpty()) {
                    return qualifier.trim();
                }
            }
        }
        return null;
    }
    
    /**
     * Checks if a type is a collection type.
     */
    private boolean isCollectionType(String type) {
        return COLLECTION_TYPES.contains(type) || 
               type.contains("List<") || 
               type.contains("Set<") || 
               type.contains("Collection<") || 
               type.contains("Map<") ||
               type.endsWith("[]");
    }
    
    /**
     * Checks if any annotation in the set matches one of the target annotations.
     */
    private boolean hasAnyAnnotation(Set<AnnotationInfo> annotations, Set<String> targetAnnotations) {
        return annotations.stream()
            .anyMatch(annotation -> targetAnnotations.contains(annotation.getType()));
    }
}