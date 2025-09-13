package it.denzosoft.jreverse.analyzer.mainmethod;

import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.MethodInfo;

/**
 * Information about a discovered main method.
 */
public class MainMethodInfo {
    
    private final ClassInfo declaringClass;
    private final MethodInfo methodInfo;
    private final String signature;
    private final boolean isPublic;
    private final boolean isStatic;
    
    public MainMethodInfo(ClassInfo declaringClass, MethodInfo methodInfo) {
        this.declaringClass = declaringClass;
        this.methodInfo = methodInfo;
        this.signature = buildSignature();
        this.isPublic = methodInfo.isPublic();
        this.isStatic = methodInfo.isStatic();
    }
    
    private String buildSignature() {
        return String.format("%s.%s(%s)",
            declaringClass.getFullyQualifiedName(),
            methodInfo.getName(),
            methodInfo.getParameters().stream()
                .map(p -> p.getType())
                .collect(java.util.stream.Collectors.joining(", "))
        );
    }
    
    // Getters
    public ClassInfo getDeclaringClass() { return declaringClass; }
    public MethodInfo getMethodInfo() { return methodInfo; }
    public String getSignature() { return signature; }
    public boolean isPublic() { return isPublic; }
    public boolean isStatic() { return isStatic; }
    
    public String getClassName() {
        return declaringClass.getFullyQualifiedName();
    }
    
    public String getMethodName() {
        return methodInfo.getName();
    }
    
    /**
     * Checks if this is a valid main method signature.
     */
    public boolean isValidMainMethod() {
        return isPublic && 
               isStatic && 
               "main".equals(methodInfo.getName()) &&
               methodInfo.getParameters().size() == 1 &&
               "java.lang.String[]".equals(methodInfo.getParameters().get(0).getType());
    }
    
    /**
     * Checks if the declaring class has Spring Boot annotations.
     */
    public boolean hasSpringBootAnnotations() {
        return declaringClass.getAnnotations().stream()
            .anyMatch(annotation -> isSpringBootAnnotation(annotation.getType()));
    }
    
    private boolean isSpringBootAnnotation(String annotationType) {
        return "org.springframework.boot.autoconfigure.SpringBootApplication".equals(annotationType) ||
               "org.springframework.boot.SpringBootApplication".equals(annotationType) ||
               "org.springframework.boot.autoconfigure.EnableAutoConfiguration".equals(annotationType);
    }
    
    @Override
    public String toString() {
        return signature;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MainMethodInfo that = (MainMethodInfo) obj;
        return signature.equals(that.signature);
    }
    
    @Override
    public int hashCode() {
        return signature.hashCode();
    }
}