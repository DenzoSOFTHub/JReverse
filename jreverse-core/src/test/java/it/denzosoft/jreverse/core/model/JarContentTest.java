package it.denzosoft.jreverse.core.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class JarContentTest {

    @TempDir
    Path tempDir;
    
    private JarLocation jarLocation;
    
    @BeforeEach
    void setUp() throws IOException {
        Path jarFile = tempDir.resolve("test.jar");
        Files.createFile(jarFile);
        jarLocation = new JarLocation(jarFile);
    }

    @Test
    void shouldCreateJarContentWithValidData() {
        ClassInfo testClass = createTestClass("com.example.TestClass");
        
        JarContent jarContent = JarContent.builder()
            .location(jarLocation)
            .addClass(testClass)
            .jarType(JarType.REGULAR)
            .build();
        
        assertEquals(jarLocation, jarContent.getLocation());
        assertEquals(1, jarContent.getClassCount());
        assertEquals(JarType.REGULAR, jarContent.getJarType());
        assertTrue(jarContent.getClasses().contains(testClass));
        assertFalse(jarContent.isEmpty());
    }

    @Test
    void shouldThrowExceptionForNullLocation() {
        assertThrows(NullPointerException.class, () -> {
            JarContent.builder()
                .location(null)
                .build();
        });
    }

    @Test
    void shouldExtractPackages() {
        ClassInfo class1 = createTestClass("com.example.service.TestService");
        ClassInfo class2 = createTestClass("com.example.controller.TestController");
        ClassInfo class3 = createTestClass("com.example.service.AnotherService");
        
        JarContent jarContent = JarContent.builder()
            .location(jarLocation)
            .addClass(class1)
            .addClass(class2)
            .addClass(class3)
            .build();
        
        Set<String> packages = jarContent.getPackages();
        assertEquals(2, packages.size());
        assertTrue(packages.contains("com.example.service"));
        assertTrue(packages.contains("com.example.controller"));
    }

    @Test
    void shouldGetClassesInPackage() {
        ClassInfo serviceClass1 = createTestClass("com.example.service.TestService");
        ClassInfo serviceClass2 = createTestClass("com.example.service.AnotherService");
        ClassInfo controllerClass = createTestClass("com.example.controller.TestController");
        
        JarContent jarContent = JarContent.builder()
            .location(jarLocation)
            .addClass(serviceClass1)
            .addClass(serviceClass2)
            .addClass(controllerClass)
            .build();
        
        Set<ClassInfo> serviceClasses = jarContent.getClassesInPackage("com.example.service");
        assertEquals(2, serviceClasses.size());
        assertTrue(serviceClasses.contains(serviceClass1));
        assertTrue(serviceClasses.contains(serviceClass2));
        assertFalse(serviceClasses.contains(controllerClass));
    }

    @Test
    void shouldFindClassByName() {
        ClassInfo testClass = createTestClass("com.example.TestClass");
        
        JarContent jarContent = JarContent.builder()
            .location(jarLocation)
            .addClass(testClass)
            .build();
        
        ClassInfo found = jarContent.getClassByName("com.example.TestClass");
        assertEquals(testClass, found);
        
        ClassInfo notFound = jarContent.getClassByName("com.example.NonExistent");
        assertNull(notFound);
    }

    @Test
    void shouldFindClassesWithAnnotation() {
        AnnotationInfo serviceAnnotation = AnnotationInfo.simple("org.springframework.stereotype.Service");
        AnnotationInfo controllerAnnotation = AnnotationInfo.simple("org.springframework.stereotype.Controller");
        
        ClassInfo serviceClass = ClassInfo.builder()
            .fullyQualifiedName("com.example.TestService")
            .addAnnotation(serviceAnnotation)
            .build();
            
        ClassInfo controllerClass = ClassInfo.builder()
            .fullyQualifiedName("com.example.TestController")
            .addAnnotation(controllerAnnotation)
            .build();
            
        ClassInfo regularClass = createTestClass("com.example.RegularClass");
        
        JarContent jarContent = JarContent.builder()
            .location(jarLocation)
            .addClass(serviceClass)
            .addClass(controllerClass)
            .addClass(regularClass)
            .build();
        
        Set<ClassInfo> serviceClasses = jarContent.getClassesWithAnnotation("org.springframework.stereotype.Service");
        assertEquals(1, serviceClasses.size());
        assertTrue(serviceClasses.contains(serviceClass));
    }

    @Test
    void shouldFilterPublicClasses() {
        ClassInfo publicClass = ClassInfo.builder()
            .fullyQualifiedName("com.example.PublicClass")
            .classType(ClassType.PUBLIC_CLASS)
            .build();
            
        ClassInfo packageClass = ClassInfo.builder()
            .fullyQualifiedName("com.example.PackageClass")
            .classType(ClassType.CLASS)
            .build();
        
        JarContent jarContent = JarContent.builder()
            .location(jarLocation)
            .addClass(publicClass)
            .addClass(packageClass)
            .build();
        
        Set<ClassInfo> publicClasses = jarContent.getPublicClasses();
        assertEquals(1, publicClasses.size());
        assertTrue(publicClasses.contains(publicClass));
    }

    @Test
    void shouldFilterInterfaces() {
        ClassInfo interfaceClass = ClassInfo.builder()
            .fullyQualifiedName("com.example.TestInterface")
            .classType(ClassType.PUBLIC_INTERFACE)
            .build();
            
        ClassInfo regularClass = createTestClass("com.example.RegularClass");
        
        JarContent jarContent = JarContent.builder()
            .location(jarLocation)
            .addClass(interfaceClass)
            .addClass(regularClass)
            .build();
        
        Set<ClassInfo> interfaces = jarContent.getInterfaces();
        assertEquals(1, interfaces.size());
        assertTrue(interfaces.contains(interfaceClass));
    }

    @Test
    void shouldHandleResources() {
        byte[] resourceContent = "test resource content".getBytes();
        
        JarContent jarContent = JarContent.builder()
            .location(jarLocation)
            .addResource("application.properties", resourceContent)
            .build();
        
        assertEquals(1, jarContent.getResourceCount());
        assertTrue(jarContent.hasResource("application.properties"));
        assertArrayEquals(resourceContent, jarContent.getResource("application.properties"));
        assertFalse(jarContent.hasResource("nonexistent.properties"));
        assertNull(jarContent.getResource("nonexistent.properties"));
    }

    @Test
    void shouldBeEmptyWhenNoContent() {
        JarContent jarContent = JarContent.builder()
            .location(jarLocation)
            .build();
        
        assertTrue(jarContent.isEmpty());
        assertEquals(0, jarContent.getClassCount());
        assertEquals(0, jarContent.getResourceCount());
    }

    @Test
    void shouldSetAnalysisTimestamp() {
        long beforeCreation = System.currentTimeMillis();
        
        JarContent jarContent = JarContent.builder()
            .location(jarLocation)
            .analysisTimestamp(beforeCreation)
            .build();
        
        assertEquals(beforeCreation, jarContent.getAnalysisTimestamp());
    }

    @Test
    void shouldDefaultToCurrentTimestamp() {
        long beforeCreation = System.currentTimeMillis();
        
        JarContent jarContent = JarContent.builder()
            .location(jarLocation)
            .build();
        
        long afterCreation = System.currentTimeMillis();
        assertTrue(jarContent.getAnalysisTimestamp() >= beforeCreation);
        assertTrue(jarContent.getAnalysisTimestamp() <= afterCreation);
    }

    private ClassInfo createTestClass(String fullyQualifiedName) {
        return ClassInfo.builder()
            .fullyQualifiedName(fullyQualifiedName)
            .build();
    }
}