package it.denzosoft.jreverse.analyzer.uml;

import it.denzosoft.jreverse.core.model.*;
import it.denzosoft.jreverse.core.logging.JReverseLogger;

import java.util.Set;

/**
 * Renders individual classes in PlantUML syntax.
 */
public class PlantUMLClassRenderer {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(PlantUMLClassRenderer.class);
    
    public String renderClasses(Set<ClassInfo> classes, DetailLevel detailLevel) {
        if (classes == null || classes.isEmpty()) {
            LOGGER.debug("No classes to render");
            return "";
        }
        
        StringBuilder classesBuilder = new StringBuilder();
        classesBuilder.append("\n' Classes\n");
        
        for (ClassInfo classInfo : classes) {
            String renderedClass = renderClass(classInfo, detailLevel);
            classesBuilder.append(renderedClass).append("\n");
        }
        
        return classesBuilder.toString();
    }
    
    public String renderClass(ClassInfo classInfo, DetailLevel detailLevel) {
        StringBuilder classBuilder = new StringBuilder();
        
        // Determine the UML element type
        String umlElementType = determineUMLElementType(classInfo);
        String className = sanitizeClassName(classInfo.getFullyQualifiedName());
        
        classBuilder.append(umlElementType).append(" ").append(className);
        
        // Add stereotypes if applicable
        String stereotypes = generateStereotypes(classInfo);
        if (!stereotypes.isEmpty()) {
            classBuilder.append(" ").append(stereotypes);
        }
        
        // Add class body if detail level requires it
        if (detailLevel != DetailLevel.MINIMAL) {
            String classBody = renderClassBody(classInfo, detailLevel);
            if (!classBody.trim().isEmpty()) {
                classBuilder.append(" {\n").append(classBody).append("}");
            }
        }
        
        return classBuilder.toString();
    }
    
    private String determineUMLElementType(ClassInfo classInfo) {
        if (classInfo.isInterface()) {
            return "interface";
        } else if (classInfo.isAbstract()) {
            return "abstract class";
        } else if (classInfo.isEnum()) {
            return "enum";
        } else {
            return "class";
        }
    }
    
    private String generateStereotypes(ClassInfo classInfo) {
        StringBuilder stereotypes = new StringBuilder();
        
        // Add Spring stereotypes based on annotations
        if (classInfo.hasAnnotation("org.springframework.stereotype.Service")) {
            stereotypes.append("<<Service>>");
        } else if (classInfo.hasAnnotation("org.springframework.stereotype.Repository")) {
            stereotypes.append("<<Repository>>");
        } else if (classInfo.hasAnnotation("org.springframework.stereotype.Controller") ||
                  classInfo.hasAnnotation("org.springframework.web.bind.annotation.RestController")) {
            stereotypes.append("<<Controller>>");
        } else if (classInfo.hasAnnotation("org.springframework.stereotype.Component")) {
            stereotypes.append("<<Component>>");
        } else if (classInfo.hasAnnotation("org.springframework.context.annotation.Configuration")) {
            stereotypes.append("<<Configuration>>");
        }
        
        // Add JPA stereotypes
        if (classInfo.hasAnnotation("javax.persistence.Entity") ||
            classInfo.hasAnnotation("jakarta.persistence.Entity")) {
            if (stereotypes.length() > 0) stereotypes.append(" ");
            stereotypes.append("<<Entity>>");
        }
        
        // Add common patterns based on naming
        String simpleName = classInfo.getSimpleName().toLowerCase();
        if (simpleName.endsWith("dto") || simpleName.endsWith("vo")) {
            if (stereotypes.length() > 0) stereotypes.append(" ");
            stereotypes.append("<<DTO>>");
        } else if (simpleName.endsWith("builder")) {
            if (stereotypes.length() > 0) stereotypes.append(" ");
            stereotypes.append("<<Builder>>");
        } else if (simpleName.endsWith("factory")) {
            if (stereotypes.length() > 0) stereotypes.append(" ");
            stereotypes.append("<<Factory>>");
        }
        
        return stereotypes.toString();
    }
    
    private String renderClassBody(ClassInfo classInfo, DetailLevel detailLevel) {
        StringBuilder bodyBuilder = new StringBuilder();
        
        // Render fields
        if (classInfo.getFields() != null && !classInfo.getFields().isEmpty()) {
            for (FieldInfo field : classInfo.getFields()) {
                String renderedField = renderField(field, detailLevel);
                if (!renderedField.isEmpty()) {
                    bodyBuilder.append("  ").append(renderedField).append("\n");
                }
            }
        }
        
        // Add separator between fields and methods if both exist
        if (classInfo.getFields() != null && !classInfo.getFields().isEmpty() &&
            classInfo.getMethods() != null && !classInfo.getMethods().isEmpty()) {
            bodyBuilder.append("  --\n");
        }
        
        // Render methods
        if (classInfo.getMethods() != null && !classInfo.getMethods().isEmpty()) {
            for (MethodInfo method : classInfo.getMethods()) {
                String renderedMethod = renderMethod(method, detailLevel);
                if (!renderedMethod.isEmpty()) {
                    bodyBuilder.append("  ").append(renderedMethod).append("\n");
                }
            }
        }
        
        return bodyBuilder.toString();
    }
    
    private String renderField(FieldInfo field, DetailLevel detailLevel) {
        StringBuilder fieldBuilder = new StringBuilder();
        
        // Skip private fields unless detail level includes them
        if (field.isPrivate() && !detailLevel.includePrivateMembers()) {
            return "";
        }
        
        // Add visibility modifier
        if (field.isPublic()) {
            fieldBuilder.append("+");
        } else if (field.isPrivate()) {
            fieldBuilder.append("-");
        } else if (field.isProtected()) {
            fieldBuilder.append("#");
        } else {
            fieldBuilder.append("~"); // package-private
        }
        
        // Add field name
        fieldBuilder.append(field.getName());
        
        // Add type if detail level includes it
        if (detailLevel.includeFieldTypes()) {
            fieldBuilder.append(" : ").append(simplifyTypeName(field.getType()));
        }
        
        // Add static/final modifiers
        if (field.isStatic()) {
            fieldBuilder.append(" {static}");
        }
        if (field.isFinal()) {
            fieldBuilder.append(" {readonly}");
        }
        
        return fieldBuilder.toString();
    }
    
    private String renderMethod(MethodInfo method, DetailLevel detailLevel) {
        StringBuilder methodBuilder = new StringBuilder();
        
        // Skip private methods unless detail level includes them
        if (method.isPrivate() && !detailLevel.includePrivateMembers()) {
            return "";
        }
        
        // Skip getters/setters in summary mode unless it's a detailed view
        if (detailLevel == DetailLevel.SUMMARY && isGetterOrSetter(method)) {
            return "";
        }
        
        // Add visibility modifier
        if (method.isPublic()) {
            methodBuilder.append("+");
        } else if (method.isPrivate()) {
            methodBuilder.append("-");
        } else if (method.isProtected()) {
            methodBuilder.append("#");
        } else {
            methodBuilder.append("~"); // package-private
        }
        
        // Add method name
        methodBuilder.append(method.getName());
        
        // Add parameters if detail level includes them
        if (detailLevel.includeMethodParameters()) {
            methodBuilder.append("(");
            if (method.getParameters() != null && !method.getParameters().isEmpty()) {
                for (int i = 0; i < method.getParameters().size(); i++) {
                    if (i > 0) methodBuilder.append(", ");
                    methodBuilder.append(simplifyTypeName(method.getParameters().get(i).getType()));
                }
            }
            methodBuilder.append(")");
        } else {
            methodBuilder.append("()");
        }
        
        // Add return type
        if (method.getReturnType() != null && !method.getReturnType().equals("void")) {
            methodBuilder.append(" : ").append(simplifyTypeName(method.getReturnType()));
        }
        
        // Add static/abstract modifiers
        if (method.isStatic()) {
            methodBuilder.append(" {static}");
        }
        if (method.isAbstract()) {
            methodBuilder.append(" {abstract}");
        }
        
        return methodBuilder.toString();
    }
    
    private boolean isGetterOrSetter(MethodInfo method) {
        String name = method.getName();
        return (name.startsWith("get") && name.length() > 3) ||
               (name.startsWith("set") && name.length() > 3) ||
               (name.startsWith("is") && name.length() > 2);
    }
    
    private String simplifyTypeName(String fullTypeName) {
        if (fullTypeName == null) return "";
        
        // Remove package names for common types
        if (fullTypeName.startsWith("java.lang.")) {
            return fullTypeName.substring("java.lang.".length());
        }
        if (fullTypeName.startsWith("java.util.")) {
            return fullTypeName.substring("java.util.".length());
        }
        
        // Handle generics
        if (fullTypeName.contains("<")) {
            String baseName = fullTypeName.substring(0, fullTypeName.indexOf("<"));
            String genericPart = fullTypeName.substring(fullTypeName.indexOf("<"));
            return simplifyTypeName(baseName) + genericPart;
        }
        
        // Return simple class name
        int lastDot = fullTypeName.lastIndexOf(".");
        if (lastDot > 0) {
            return fullTypeName.substring(lastDot + 1);
        }
        
        return fullTypeName;
    }
    
    private String sanitizeClassName(String className) {
        return className.replace("$", "_").replace(" ", "_");
    }
}