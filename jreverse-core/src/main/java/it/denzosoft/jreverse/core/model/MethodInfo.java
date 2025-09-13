package it.denzosoft.jreverse.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Domain entity representing a Java method with its metadata.
 * Immutable value object following Clean Architecture principles.
 */
public final class MethodInfo {
    
    private final String name;
    private final String returnType;
    private final List<ParameterInfo> parameters;
    private final Set<AnnotationInfo> annotations;
    private final String declaringClassName;
    private final boolean isPublic;
    private final boolean isPrivate;
    private final boolean isProtected;
    private final boolean isStatic;
    private final boolean isFinal;
    private final boolean isAbstract;
    private final boolean isSynchronized;
    private final Set<String> thrownExceptions;
    
    private MethodInfo(Builder builder) {
        this.name = requireNonEmpty(builder.name, "name");
        this.returnType = Objects.requireNonNull(builder.returnType, "returnType cannot be null");
        this.parameters = Collections.unmodifiableList(new ArrayList<>(builder.parameters));
        this.annotations = Collections.unmodifiableSet(new HashSet<>(builder.annotations));
        this.declaringClassName = requireNonEmpty(builder.declaringClassName, "declaringClassName");
        this.isPublic = builder.isPublic;
        this.isPrivate = builder.isPrivate;
        this.isProtected = builder.isProtected;
        this.isStatic = builder.isStatic;
        this.isFinal = builder.isFinal;
        this.isAbstract = builder.isAbstract;
        this.isSynchronized = builder.isSynchronized;
        this.thrownExceptions = Collections.unmodifiableSet(new HashSet<>(builder.thrownExceptions));
    }
    
    public String getName() {
        return name;
    }
    
    public String getReturnType() {
        return returnType;
    }
    
    public List<ParameterInfo> getParameters() {
        return parameters;
    }
    
    public Set<AnnotationInfo> getAnnotations() {
        return annotations;
    }
    
    public String getDeclaringClassName() {
        return declaringClassName;
    }
    
    public boolean isPublic() {
        return isPublic;
    }
    
    public boolean isPrivate() {
        return isPrivate;
    }
    
    public boolean isProtected() {
        return isProtected;
    }
    
    public boolean isStatic() {
        return isStatic;
    }
    
    public boolean isFinal() {
        return isFinal;
    }
    
    public boolean isAbstract() {
        return isAbstract;
    }
    
    public boolean isSynchronized() {
        return isSynchronized;
    }
    
    public Set<String> getThrownExceptions() {
        return thrownExceptions;
    }
    
    public boolean hasAnnotation(String annotationType) {
        return annotations.stream()
            .anyMatch(annotation -> annotation.getType().equals(annotationType));
    }
    
    public String getMethodSignature() {
        StringBuilder signature = new StringBuilder();
        signature.append(returnType).append(" ").append(name).append("(");
        
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) signature.append(", ");
            signature.append(parameters.get(i).getType());
        }
        
        signature.append(")");
        return signature.toString();
    }
    
    public boolean isMainMethod() {
        return "main".equals(name) &&
               isPublic &&
               isStatic &&
               "void".equals(returnType) &&
               parameters.size() == 1 &&
               "java.lang.String[]".equals(parameters.get(0).getType());
    }
    
    public boolean isGetter() {
        return name.startsWith("get") &&
               name.length() > 3 &&
               Character.isUpperCase(name.charAt(3)) &&
               parameters.isEmpty() &&
               !"void".equals(returnType);
    }
    
    public boolean isSetter() {
        return name.startsWith("set") &&
               name.length() > 3 &&
               Character.isUpperCase(name.charAt(3)) &&
               parameters.size() == 1 &&
               "void".equals(returnType);
    }
    
    private String requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        return value.trim();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MethodInfo that = (MethodInfo) obj;
        return Objects.equals(name, that.name) &&
               Objects.equals(declaringClassName, that.declaringClassName) &&
               Objects.equals(parameters, that.parameters);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, declaringClassName, parameters);
    }
    
    @Override
    public String toString() {
        return "MethodInfo{" +
                "name='" + name + '\'' +
                ", returnType='" + returnType + '\'' +
                ", declaringClassName='" + declaringClassName + '\'' +
                ", parameterCount=" + parameters.size() +
                ", annotationCount=" + annotations.size() +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String name;
        private String returnType = "void";
        private List<ParameterInfo> parameters = new ArrayList<>();
        private Set<AnnotationInfo> annotations = new HashSet<>();
        private String declaringClassName;
        private boolean isPublic = false;
        private boolean isPrivate = false;
        private boolean isProtected = false;
        private boolean isStatic = false;
        private boolean isFinal = false;
        private boolean isAbstract = false;
        private boolean isSynchronized = false;
        private Set<String> thrownExceptions = new HashSet<>();
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder returnType(String returnType) {
            this.returnType = returnType;
            return this;
        }
        
        public Builder addParameter(ParameterInfo parameter) {
            if (parameter != null) {
                this.parameters.add(parameter);
            }
            return this;
        }
        
        public Builder parameters(List<ParameterInfo> parameters) {
            this.parameters = new ArrayList<>(parameters != null ? parameters : Collections.emptyList());
            return this;
        }
        
        public Builder addAnnotation(AnnotationInfo annotation) {
            if (annotation != null) {
                this.annotations.add(annotation);
            }
            return this;
        }
        
        public Builder annotations(Set<AnnotationInfo> annotations) {
            this.annotations = new HashSet<>(annotations != null ? annotations : Collections.emptySet());
            return this;
        }
        
        public Builder declaringClassName(String declaringClassName) {
            this.declaringClassName = declaringClassName;
            return this;
        }
        
        public Builder isPublic(boolean isPublic) {
            this.isPublic = isPublic;
            return this;
        }
        
        public Builder isPrivate(boolean isPrivate) {
            this.isPrivate = isPrivate;
            return this;
        }
        
        public Builder isProtected(boolean isProtected) {
            this.isProtected = isProtected;
            return this;
        }
        
        public Builder isStatic(boolean isStatic) {
            this.isStatic = isStatic;
            return this;
        }
        
        public Builder isFinal(boolean isFinal) {
            this.isFinal = isFinal;
            return this;
        }
        
        public Builder isAbstract(boolean isAbstract) {
            this.isAbstract = isAbstract;
            return this;
        }
        
        public Builder isSynchronized(boolean isSynchronized) {
            this.isSynchronized = isSynchronized;
            return this;
        }
        
        public Builder addThrownException(String exceptionType) {
            if (exceptionType != null && !exceptionType.trim().isEmpty()) {
                this.thrownExceptions.add(exceptionType.trim());
            }
            return this;
        }
        
        public Builder thrownExceptions(Set<String> thrownExceptions) {
            this.thrownExceptions = new HashSet<>(thrownExceptions != null ? thrownExceptions : Collections.emptySet());
            return this;
        }
        
        public MethodInfo build() {
            return new MethodInfo(this);
        }
    }
}