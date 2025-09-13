package it.denzosoft.jreverse.analyzer.property;

import it.denzosoft.jreverse.core.model.*;
import it.denzosoft.jreverse.core.port.PropertyAnalyzer;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Javassist-based implementation for analyzing Spring Boot property configurations and usage.
 */
public class JavassistPropertyAnalyzer implements PropertyAnalyzer {
    
    private static final Logger LOGGER = Logger.getLogger(JavassistPropertyAnalyzer.class.getName());
    
    private static final String VALUE_ANNOTATION = "org.springframework.beans.factory.annotation.Value";
    private static final String CONFIG_PROPS_ANNOTATION = "org.springframework.boot.context.properties.ConfigurationProperties";
    private static final String PROPERTY_SOURCE_ANNOTATION = "org.springframework.context.annotation.PropertySource";
    
    private static final Set<String> VALIDATION_ANNOTATIONS = new HashSet<>(Arrays.asList(
        "javax.validation.constraints.NotNull",
        "javax.validation.constraints.NotEmpty",
        "javax.validation.constraints.NotBlank",
        "javax.validation.constraints.Size",
        "javax.validation.constraints.Min",
        "javax.validation.constraints.Max",
        "javax.validation.constraints.Pattern",
        "javax.validation.Valid"
    ));
    
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("\\$\\{([^}:]+)(?::([^}]*))?\\}");
    private static final Pattern SPEL_PATTERN = Pattern.compile("#\\{.*\\}");
    
    @Override
    public PropertyAnalysisResult analyzeProperties(JarContent jarContent) {
        LOGGER.info("Starting property analysis");
        
        try {
            ClassPool classPool = ClassPool.getDefault();
            loadJarIntoClassPool(classPool, jarContent);
            
            List<PropertyUsageInfo> valueInjections = new ArrayList<>();
            List<ConfigurationPropertiesInfo> configurationProperties = new ArrayList<>();
            List<PropertySourceInfo> propertySources = new ArrayList<>();
            Map<String, List<String>> propertyReferences = new HashMap<>();
            
            for (ClassInfo classInfo : jarContent.getClasses()) {
                String className = classInfo.getFullyQualifiedName();
                try {
                    CtClass ctClass = classPool.get(className);
                    
                    analyzeValueInjections(ctClass, valueInjections);
                    analyzeConfigurationProperties(ctClass, configurationProperties);
                    analyzePropertySources(ctClass, propertySources);
                    analyzePropertyReferences(ctClass, propertyReferences);
                    
                } catch (NotFoundException e) {
                    LOGGER.log(Level.WARNING, "Class not found during analysis: " + className, e);
                }
            }
            
            LOGGER.info("Property analysis completed. Found " + valueInjections.size() + 
                       " @Value injections, " + configurationProperties.size() + " @ConfigurationProperties");
            
            return PropertyAnalysisResult.builder()
                    .valueInjections(valueInjections)
                    .configurationProperties(configurationProperties)
                    .propertySources(propertySources)
                    .propertyReferences(propertyReferences)
                    .metadata(AnalysisMetadata.successful())
                    .build();
                    
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during property analysis", e);
            return PropertyAnalysisResult.builder()
                    .metadata(AnalysisMetadata.error("Property analysis failed: " + e.getMessage()))
                    .build();
        }
    }
    
    @Override
    public boolean canAnalyze(JarContent jarContent) {
        return jarContent != null && !jarContent.getClasses().isEmpty();
    }
    
    private void loadJarIntoClassPool(ClassPool classPool, JarContent jarContent) {
        try {
            classPool.insertClassPath(jarContent.getLocation().getPath().toFile().getAbsolutePath());
        } catch (NotFoundException e) {
            LOGGER.log(Level.WARNING, "Could not load JAR into ClassPool", e);
        }
    }
    
    private void analyzeValueInjections(CtClass ctClass, List<PropertyUsageInfo> valueInjections) {
        try {
            analyzeFieldValueInjections(ctClass, valueInjections);
            analyzeMethodValueInjections(ctClass, valueInjections);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error analyzing @Value injections in class: " + ctClass.getName(), e);
        }
    }
    
    private void analyzeFieldValueInjections(CtClass ctClass, List<PropertyUsageInfo> valueInjections) {
        try {
            for (CtField field : ctClass.getDeclaredFields()) {
                FieldInfo fieldInfo = field.getFieldInfo();
                AnnotationsAttribute attr = (AnnotationsAttribute) fieldInfo.getAttribute(AnnotationsAttribute.visibleTag);
                
                if (attr != null) {
                    for (Annotation annotation : attr.getAnnotations()) {
                        if (VALUE_ANNOTATION.equals(annotation.getTypeName())) {
                            PropertyUsageInfo usage = createFieldValueUsage(ctClass, field, annotation);
                            if (usage != null) {
                                valueInjections.add(usage);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error analyzing field @Value injections", e);
        }
    }
    
    private void analyzeMethodValueInjections(CtClass ctClass, List<PropertyUsageInfo> valueInjections) {
        try {
            for (CtMethod method : ctClass.getDeclaredMethods()) {
                MethodInfo methodInfo = method.getMethodInfo();
                AnnotationsAttribute methodAttr = (AnnotationsAttribute) methodInfo.getAttribute(AnnotationsAttribute.visibleTag);
                ParameterAnnotationsAttribute paramAttr = (ParameterAnnotationsAttribute) methodInfo.getAttribute(ParameterAnnotationsAttribute.visibleTag);
                
                if (methodAttr != null) {
                    for (Annotation annotation : methodAttr.getAnnotations()) {
                        if (VALUE_ANNOTATION.equals(annotation.getTypeName())) {
                            PropertyUsageInfo usage = createMethodValueUsage(ctClass, method, annotation);
                            if (usage != null) {
                                valueInjections.add(usage);
                            }
                        }
                    }
                }
                
                if (paramAttr != null) {
                    analyzeParameterValueInjections(ctClass, method, paramAttr, valueInjections);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error analyzing method @Value injections", e);
        }
    }
    
    private void analyzeParameterValueInjections(CtClass ctClass, CtMethod method, 
                                                ParameterAnnotationsAttribute paramAttr, 
                                                List<PropertyUsageInfo> valueInjections) {
        try {
            Annotation[][] paramAnnotations = paramAttr.getAnnotations();
            CtClass[] paramTypes = method.getParameterTypes();
            
            for (int i = 0; i < paramAnnotations.length; i++) {
                for (Annotation annotation : paramAnnotations[i]) {
                    if (VALUE_ANNOTATION.equals(annotation.getTypeName())) {
                        PropertyUsageInfo usage = createParameterValueUsage(ctClass, method, annotation, i, paramTypes);
                        if (usage != null) {
                            valueInjections.add(usage);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error analyzing parameter @Value injections", e);
        }
    }
    
    private void analyzeConfigurationProperties(CtClass ctClass, List<ConfigurationPropertiesInfo> configurationProperties) {
        try {
            ClassFile classFile = ctClass.getClassFile();
            AnnotationsAttribute classAttr = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag);
            
            if (classAttr != null) {
                for (Annotation annotation : classAttr.getAnnotations()) {
                    if (CONFIG_PROPS_ANNOTATION.equals(annotation.getTypeName())) {
                        ConfigurationPropertiesInfo configProps = createConfigurationPropertiesInfo(ctClass, annotation);
                        if (configProps != null) {
                            configurationProperties.add(configProps);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error analyzing @ConfigurationProperties in class: " + ctClass.getName(), e);
        }
    }
    
    private void analyzePropertySources(CtClass ctClass, List<PropertySourceInfo> propertySources) {
        try {
            ClassFile classFile = ctClass.getClassFile();
            AnnotationsAttribute classAttr = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag);
            
            if (classAttr != null) {
                for (Annotation annotation : classAttr.getAnnotations()) {
                    if (PROPERTY_SOURCE_ANNOTATION.equals(annotation.getTypeName())) {
                        PropertySourceInfo sourceInfo = createPropertySourceInfo(ctClass, annotation);
                        if (sourceInfo != null) {
                            propertySources.add(sourceInfo);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error analyzing @PropertySource in class: " + ctClass.getName(), e);
        }
    }
    
    private void analyzePropertyReferences(CtClass ctClass, Map<String, List<String>> propertyReferences) {
        try {
            Set<String> classProperties = new HashSet<>();
            
            for (CtField field : ctClass.getDeclaredFields()) {
                FieldInfo fieldInfo = field.getFieldInfo();
                AnnotationsAttribute attr = (AnnotationsAttribute) fieldInfo.getAttribute(AnnotationsAttribute.visibleTag);
                
                if (attr != null) {
                    for (Annotation annotation : attr.getAnnotations()) {
                        if (VALUE_ANNOTATION.equals(annotation.getTypeName())) {
                            String expression = getStringMemberValue(annotation, "value");
                            if (expression != null) {
                                extractPropertyKeys(expression, classProperties);
                            }
                        }
                    }
                }
            }
            
            if (!classProperties.isEmpty()) {
                propertyReferences.put(ctClass.getName(), new ArrayList<>(classProperties));
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error analyzing property references in class: " + ctClass.getName(), e);
        }
    }
    
    private PropertyUsageInfo createFieldValueUsage(CtClass ctClass, CtField field, Annotation annotation) {
        try {
            String expression = getStringMemberValue(annotation, "value");
            if (expression == null) return null;
            
            String propertyKey = extractPropertyKey(expression);
            String defaultValue = extractDefaultValue(expression);
            boolean hasSpEL = hasSpELExpression(expression);
            
            return PropertyUsageInfo.builder()
                    .propertyKey(propertyKey)
                    .defaultValue(defaultValue)
                    .targetClass(ctClass.getName())
                    .targetMember(field.getName())
                    .injectionTarget(PropertyUsageInfo.InjectionTarget.FIELD)
                    .targetType(field.getType().getName())
                    .hasSpELExpression(hasSpEL)
                    .fullExpression(expression)
                    .build();
                    
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error creating field @Value usage info", e);
            return null;
        }
    }
    
    private PropertyUsageInfo createMethodValueUsage(CtClass ctClass, CtMethod method, Annotation annotation) {
        try {
            String expression = getStringMemberValue(annotation, "value");
            if (expression == null) return null;
            
            String propertyKey = extractPropertyKey(expression);
            String defaultValue = extractDefaultValue(expression);
            boolean hasSpEL = hasSpELExpression(expression);
            
            PropertyUsageInfo.InjectionTarget target = method.getName().startsWith("set") ? 
                PropertyUsageInfo.InjectionTarget.SETTER_METHOD : 
                PropertyUsageInfo.InjectionTarget.METHOD_PARAMETER;
            
            return PropertyUsageInfo.builder()
                    .propertyKey(propertyKey)
                    .defaultValue(defaultValue)
                    .targetClass(ctClass.getName())
                    .targetMember(method.getName())
                    .injectionTarget(target)
                    .targetType(method.getReturnType().getName())
                    .hasSpELExpression(hasSpEL)
                    .fullExpression(expression)
                    .build();
                    
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error creating method @Value usage info", e);
            return null;
        }
    }
    
    private PropertyUsageInfo createParameterValueUsage(CtClass ctClass, CtMethod method, 
                                                       Annotation annotation, int paramIndex, 
                                                       CtClass[] paramTypes) {
        try {
            String expression = getStringMemberValue(annotation, "value");
            if (expression == null) return null;
            
            String propertyKey = extractPropertyKey(expression);
            String defaultValue = extractDefaultValue(expression);
            boolean hasSpEL = hasSpELExpression(expression);
            
            PropertyUsageInfo.InjectionTarget target = method.getName().equals("<init>") ? 
                PropertyUsageInfo.InjectionTarget.CONSTRUCTOR_PARAMETER : 
                PropertyUsageInfo.InjectionTarget.METHOD_PARAMETER;
            
            String targetType = paramIndex < paramTypes.length ? paramTypes[paramIndex].getName() : "unknown";
            
            return PropertyUsageInfo.builder()
                    .propertyKey(propertyKey)
                    .defaultValue(defaultValue)
                    .targetClass(ctClass.getName())
                    .targetMember(method.getName() + "[" + paramIndex + "]")
                    .injectionTarget(target)
                    .targetType(targetType)
                    .hasSpELExpression(hasSpEL)
                    .fullExpression(expression)
                    .build();
                    
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error creating parameter @Value usage info", e);
            return null;
        }
    }
    
    private ConfigurationPropertiesInfo createConfigurationPropertiesInfo(CtClass ctClass, Annotation annotation) {
        try {
            String prefix = getStringMemberValue(annotation, "prefix");
            if (prefix == null) {
                prefix = getStringMemberValue(annotation, "value");
            }
            
            boolean ignoreInvalidFields = getBooleanMemberValue(annotation, "ignoreInvalidFields", false);
            boolean ignoreUnknownFields = getBooleanMemberValue(annotation, "ignoreUnknownFields", true);
            
            List<PropertyFieldInfo> properties = analyzeConfigurationPropertiesFields(ctClass, prefix);
            boolean isValidated = hasValidationAnnotations(ctClass);
            
            return ConfigurationPropertiesInfo.builder()
                    .prefix(prefix)
                    .targetClass(ctClass.getName())
                    .bindingType(ConfigurationPropertiesInfo.BindingType.CLASS_LEVEL)
                    .properties(properties)
                    .ignoreInvalidFields(ignoreInvalidFields)
                    .ignoreUnknownFields(ignoreUnknownFields)
                    .isValidated(isValidated)
                    .build();
                    
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error creating @ConfigurationProperties info", e);
            return null;
        }
    }
    
    private PropertySourceInfo createPropertySourceInfo(CtClass ctClass, Annotation annotation) {
        try {
            String[] locations = getStringArrayMemberValue(annotation, "value");
            if (locations == null || locations.length == 0) {
                locations = getStringArrayMemberValue(annotation, "location");
            }
            
            String name = getStringMemberValue(annotation, "name");
            boolean ignoreResourceNotFound = getBooleanMemberValue(annotation, "ignoreResourceNotFound", false);
            String encoding = getStringMemberValue(annotation, "encoding");
            String factory = getStringMemberValue(annotation, "factory");
            
            PropertySourceInfo.SourceType sourceType = determineSourceType(locations);
            
            return PropertySourceInfo.builder()
                    .name(name)
                    .locations(Arrays.asList(locations))
                    .sourceType(sourceType)
                    .ignoreResourceNotFound(ignoreResourceNotFound)
                    .encoding(encoding)
                    .factory(factory)
                    .targetClass(ctClass.getName())
                    .build();
                    
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error creating @PropertySource info", e);
            return null;
        }
    }
    
    private List<PropertyFieldInfo> analyzeConfigurationPropertiesFields(CtClass ctClass, String prefix) {
        List<PropertyFieldInfo> fields = new ArrayList<>();
        
        try {
            for (CtField field : ctClass.getDeclaredFields()) {
                FieldInfo fieldInfo = field.getFieldInfo();
                AnnotationsAttribute attr = (AnnotationsAttribute) fieldInfo.getAttribute(AnnotationsAttribute.visibleTag);
                
                List<String> validationAnnotations = extractValidationAnnotations(attr);
                boolean isRequired = validationAnnotations.contains("javax.validation.constraints.NotNull");
                
                String propertyPath = prefix != null ? prefix + "." + field.getName() : field.getName();
                
                PropertyFieldInfo fieldInfo2 = PropertyFieldInfo.builder()
                        .fieldName(field.getName())
                        .fieldType(field.getType().getName())
                        .propertyPath(propertyPath)
                        .isRequired(isRequired)
                        .validationAnnotations(validationAnnotations)
                        .isNested(isNestedConfigurationProperties(field.getType()))
                        .isCollection(isCollectionType(field.getType()))
                        .build();
                        
                fields.add(fieldInfo2);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error analyzing @ConfigurationProperties fields", e);
        }
        
        return fields;
    }
    
    private String extractPropertyKey(String expression) {
        if (expression == null) return null;
        
        Matcher matcher = PROPERTY_PATTERN.matcher(expression);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return expression.startsWith("${") && expression.endsWith("}") ? 
            expression.substring(2, expression.length() - 1) : expression;
    }
    
    private String extractDefaultValue(String expression) {
        if (expression == null) return null;
        
        Matcher matcher = PROPERTY_PATTERN.matcher(expression);
        if (matcher.find()) {
            return matcher.group(2);
        }
        
        return null;
    }
    
    private boolean hasSpELExpression(String expression) {
        return expression != null && SPEL_PATTERN.matcher(expression).find();
    }
    
    private void extractPropertyKeys(String expression, Set<String> propertyKeys) {
        if (expression == null) return;
        
        Matcher matcher = PROPERTY_PATTERN.matcher(expression);
        while (matcher.find()) {
            propertyKeys.add(matcher.group(1));
        }
    }
    
    private boolean hasValidationAnnotations(CtClass ctClass) {
        try {
            ClassFile classFile = ctClass.getClassFile();
            AnnotationsAttribute classAttr = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag);
            
            if (classAttr != null) {
                for (Annotation annotation : classAttr.getAnnotations()) {
                    if ("javax.validation.Valid".equals(annotation.getTypeName())) {
                        return true;
                    }
                }
            }
            
            for (CtField field : ctClass.getDeclaredFields()) {
                FieldInfo fieldInfo = field.getFieldInfo();
                AnnotationsAttribute attr = (AnnotationsAttribute) fieldInfo.getAttribute(AnnotationsAttribute.visibleTag);
                
                if (attr != null) {
                    for (Annotation annotation : attr.getAnnotations()) {
                        if (VALIDATION_ANNOTATIONS.contains(annotation.getTypeName())) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking validation annotations", e);
        }
        
        return false;
    }
    
    private List<String> extractValidationAnnotations(AnnotationsAttribute attr) {
        List<String> validations = new ArrayList<>();
        
        if (attr != null) {
            for (Annotation annotation : attr.getAnnotations()) {
                if (VALIDATION_ANNOTATIONS.contains(annotation.getTypeName())) {
                    validations.add(annotation.getTypeName());
                }
            }
        }
        
        return validations;
    }
    
    private boolean isNestedConfigurationProperties(CtClass type) {
        try {
            ClassFile classFile = type.getClassFile();
            AnnotationsAttribute classAttr = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag);
            
            if (classAttr != null) {
                for (Annotation annotation : classAttr.getAnnotations()) {
                    if (CONFIG_PROPS_ANNOTATION.equals(annotation.getTypeName())) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // Not a nested configuration properties class
        }
        
        return false;
    }
    
    private boolean isCollectionType(CtClass type) {
        try {
            String typeName = type.getName();
            return typeName.startsWith("java.util.List") || 
                   typeName.startsWith("java.util.Set") || 
                   typeName.startsWith("java.util.Collection") ||
                   type.isArray();
        } catch (Exception e) {
            return false;
        }
    }
    
    private PropertySourceInfo.SourceType determineSourceType(String[] locations) {
        if (locations == null || locations.length == 0) {
            return PropertySourceInfo.SourceType.PROPERTIES_FILE;
        }
        
        String firstLocation = locations[0].toLowerCase();
        
        if (firstLocation.endsWith(".yml") || firstLocation.endsWith(".yaml")) {
            return PropertySourceInfo.SourceType.YAML_FILE;
        } else if (firstLocation.endsWith(".xml")) {
            return PropertySourceInfo.SourceType.XML_FILE;
        } else if (firstLocation.startsWith("classpath:")) {
            return PropertySourceInfo.SourceType.CLASSPATH_RESOURCE;
        } else if (firstLocation.startsWith("file:")) {
            return PropertySourceInfo.SourceType.FILE_RESOURCE;
        } else if (firstLocation.startsWith("http:") || firstLocation.startsWith("https:")) {
            return PropertySourceInfo.SourceType.URL_RESOURCE;
        }
        
        return PropertySourceInfo.SourceType.PROPERTIES_FILE;
    }
    
    private String getStringMemberValue(Annotation annotation, String memberName) {
        MemberValue value = annotation.getMemberValue(memberName);
        if (value instanceof StringMemberValue) {
            return ((StringMemberValue) value).getValue();
        }
        return null;
    }
    
    private String[] getStringArrayMemberValue(Annotation annotation, String memberName) {
        MemberValue value = annotation.getMemberValue(memberName);
        if (value instanceof ArrayMemberValue) {
            ArrayMemberValue arrayValue = (ArrayMemberValue) value;
            MemberValue[] values = arrayValue.getValue();
            String[] result = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof StringMemberValue) {
                    result[i] = ((StringMemberValue) values[i]).getValue();
                }
            }
            return result;
        } else if (value instanceof StringMemberValue) {
            return new String[]{((StringMemberValue) value).getValue()};
        }
        return new String[0];
    }
    
    private boolean getBooleanMemberValue(Annotation annotation, String memberName, boolean defaultValue) {
        MemberValue value = annotation.getMemberValue(memberName);
        if (value instanceof BooleanMemberValue) {
            return ((BooleanMemberValue) value).getValue();
        }
        return defaultValue;
    }
}