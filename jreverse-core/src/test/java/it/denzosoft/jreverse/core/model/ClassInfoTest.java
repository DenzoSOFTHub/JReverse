package it.denzosoft.jreverse.core.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClassInfoTest {

    @Test
    void shouldCreateClassInfoWithValidData() {
        ClassInfo classInfo = ClassInfo.builder()
            .fullyQualifiedName("com.example.TestClass")
            .classType(ClassType.PUBLIC_CLASS)
            .build();
        
        assertEquals("com.example.TestClass", classInfo.getFullyQualifiedName());
        assertEquals("com.example", classInfo.getPackageName());
        assertEquals("TestClass", classInfo.getSimpleName());
        assertEquals(ClassType.PUBLIC_CLASS, classInfo.getClassType());
        assertTrue(classInfo.isPublic());
        assertFalse(classInfo.isInterface());
    }

    @Test
    void shouldThrowExceptionForNullName() {
        assertThrows(IllegalArgumentException.class, () -> {
            ClassInfo.builder()
                .fullyQualifiedName(null)
                .build();
        });
    }

    @Test
    void shouldThrowExceptionForEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> {
            ClassInfo.builder()
                .fullyQualifiedName("   ")
                .build();
        });
    }

    @Test
    void shouldHandleClassWithoutPackage() {
        ClassInfo classInfo = ClassInfo.builder()
            .fullyQualifiedName("TestClass")
            .build();
        
        assertEquals("TestClass", classInfo.getFullyQualifiedName());
        assertEquals("", classInfo.getPackageName());
        assertEquals("TestClass", classInfo.getSimpleName());
    }

    @Test
    void shouldDetectAnnotation() {
        AnnotationInfo annotation = AnnotationInfo.simple("org.springframework.stereotype.Service");
        
        ClassInfo classInfo = ClassInfo.builder()
            .fullyQualifiedName("com.example.TestService")
            .addAnnotation(annotation)
            .build();
        
        assertTrue(classInfo.hasAnnotation("org.springframework.stereotype.Service"));
        assertFalse(classInfo.hasAnnotation("org.springframework.stereotype.Controller"));
    }

    @Test
    void shouldAddMethodsAndFields() {
        MethodInfo method = MethodInfo.builder()
            .name("testMethod")
            .returnType("void")
            .declaringClassName("com.example.TestClass")
            .build();
            
        FieldInfo field = FieldInfo.builder()
            .name("testField")
            .type("java.lang.String")
            .declaringClassName("com.example.TestClass")
            .build();
        
        ClassInfo classInfo = ClassInfo.builder()
            .fullyQualifiedName("com.example.TestClass")
            .addMethod(method)
            .addField(field)
            .build();
        
        assertEquals(1, classInfo.getMethods().size());
        assertEquals(1, classInfo.getFields().size());
        assertTrue(classInfo.getMethods().contains(method));
        assertTrue(classInfo.getFields().contains(field));
    }

    @Test
    void shouldBeEqualBasedOnFullyQualifiedName() {
        ClassInfo class1 = ClassInfo.builder()
            .fullyQualifiedName("com.example.TestClass")
            .build();
            
        ClassInfo class2 = ClassInfo.builder()
            .fullyQualifiedName("com.example.TestClass")
            .classType(ClassType.INTERFACE)
            .build();
        
        assertEquals(class1, class2);
        assertEquals(class1.hashCode(), class2.hashCode());
    }

    @Test
    void shouldDetectInterfaceType() {
        ClassInfo interfaceClass = ClassInfo.builder()
            .fullyQualifiedName("com.example.TestInterface")
            .classType(ClassType.PUBLIC_INTERFACE)
            .build();
        
        assertTrue(interfaceClass.isInterface());
        assertTrue(interfaceClass.isPublic());
        assertFalse(interfaceClass.isEnum());
        assertFalse(interfaceClass.isAnnotation());
    }

    @Test
    void shouldDetectEnumType() {
        ClassInfo enumClass = ClassInfo.builder()
            .fullyQualifiedName("com.example.TestEnum")
            .classType(ClassType.PUBLIC_ENUM)
            .build();
        
        assertTrue(enumClass.isEnum());
        assertTrue(enumClass.isPublic());
        assertFalse(enumClass.isInterface());
        assertFalse(enumClass.isAnnotation());
    }

    @Test
    void shouldDetectAnnotationType() {
        ClassInfo annotationClass = ClassInfo.builder()
            .fullyQualifiedName("com.example.TestAnnotation")
            .classType(ClassType.PUBLIC_ANNOTATION)
            .build();
        
        assertTrue(annotationClass.isAnnotation());
        assertTrue(annotationClass.isPublic());
        assertFalse(annotationClass.isInterface());
        assertFalse(annotationClass.isEnum());
    }
}