package it.denzosoft.jreverse.analyzer.webmvc;

import it.denzosoft.jreverse.core.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for JavassistWebMvcAnalyzer.
 * Tests Spring MVC mapping detection, HTTP method extraction, and path analysis.
 */
@DisplayName("JavassistWebMvcAnalyzer Tests")
class JavassistWebMvcAnalyzerTest {

    private JavassistWebMvcAnalyzer analyzer;
    private JarContent.Builder jarContentBuilder;

    @BeforeEach
    void setUp() {
        analyzer = new JavassistWebMvcAnalyzer();
        jarContentBuilder = JarContent.builder()
            .jarType(JarType.SPRING_BOOT)
            .location(new JarLocation("test.jar"));
    }

    @Nested
    @DisplayName("Analyzer Validation Tests")
    class AnalyzerValidationTests {

        @Test
        @DisplayName("Should not analyze null JAR content")
        void shouldNotAnalyzeNullJarContent() {
            // When
            boolean canAnalyze = analyzer.canAnalyze(null);

            // Then
            assertThat(canAnalyze).isFalse();
        }

        @Test
        @DisplayName("Should not analyze empty JAR content")
        void shouldNotAnalyzeEmptyJarContent() {
            // Given
            JarContent emptyJar = jarContentBuilder.build();

            // When
            boolean canAnalyze = analyzer.canAnalyze(emptyJar);

            // Then
            assertThat(canAnalyze).isFalse();
        }

        @Test
        @DisplayName("Should not analyze JAR without Spring controllers")
        void shouldNotAnalyzeJarWithoutSpringControllers() {
            // Given
            ClassInfo regularClass = ClassInfo.builder()
                .fullyQualifiedName("com.test.RegularClass")
                .classType(ClassType.CLASS)
                .build();

            JarContent jarContent = jarContentBuilder
                .addClass(regularClass)
                .build();

            // When
            boolean canAnalyze = analyzer.canAnalyze(jarContent);

            // Then
            assertThat(canAnalyze).isFalse();
        }

        @Test
        @DisplayName("Should analyze JAR with Spring controllers")
        void shouldAnalyzeJarWithSpringControllers() {
            // Given
            AnnotationInfo restControllerAnnotation = createAnnotation(
                "org.springframework.web.bind.annotation.RestController");

            ClassInfo controllerClass = ClassInfo.builder()
                .fullyQualifiedName("com.test.TestController")
                .classType(ClassType.CLASS)
                .addAnnotation(restControllerAnnotation)
                .build();

            JarContent jarContent = jarContentBuilder
                .addClass(controllerClass)
                .build();

            // When
            boolean canAnalyze = analyzer.canAnalyze(jarContent);

            // Then
            assertThat(canAnalyze).isTrue();
        }
    }

    @Nested
    @DisplayName("Basic MVC Analysis Tests")
    class BasicMvcAnalysisTests {

        @Test
        @DisplayName("Should return no mappings for JAR without controllers")
        void shouldReturnNoMappingsForJarWithoutControllers() {
            // Given
            ClassInfo regularClass = ClassInfo.builder()
                .fullyQualifiedName("com.test.RegularClass")
                .classType(ClassType.CLASS)
                .build();

            JarContent jarContent = jarContentBuilder
                .addClass(regularClass)
                .build();

            // When
            WebMvcAnalysisResult result = analyzer.analyzeWebMvcMappings(jarContent);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getMappings()).isEmpty();
            assertThat(result.getMappingCount()).isEqualTo(0);
            assertThat(result.getControllerCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should analyze controller without mappings")
        void shouldAnalyzeControllerWithoutMappings() {
            // Given
            AnnotationInfo controllerAnnotation = createAnnotation(
                "org.springframework.stereotype.Controller");

            ClassInfo controllerClass = ClassInfo.builder()
                .fullyQualifiedName("com.test.EmptyController")
                .classType(ClassType.CLASS)
                .addAnnotation(controllerAnnotation)
                .build();

            JarContent jarContent = jarContentBuilder
                .addClass(controllerClass)
                .build();

            // When
            WebMvcAnalysisResult result = analyzer.analyzeWebMvcMappings(jarContent);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isSuccessful()).isTrue();
            assertThat(result.getMappings()).isEmpty();
            assertThat(result.getMappingCount()).isEqualTo(0);
            assertThat(result.getAnalysisTimeMs()).isGreaterThan(0L);
        }

        @Test
        @DisplayName("Should analyze REST controller with GET mapping")
        void shouldAnalyzeRestControllerWithGetMapping() {
            // Given
            AnnotationInfo restControllerAnnotation = createAnnotation(
                "org.springframework.web.bind.annotation.RestController");

            AnnotationInfo getMappingAnnotation = createAnnotation(
                "org.springframework.web.bind.annotation.GetMapping",
                Map.of("value", "/users"));

            MethodInfo getMethod = MethodInfo.builder()
                .name("getUsers")
                .returnType("java.util.List")
                .declaringClassName("com.test.UserController")
                .addAnnotation(getMappingAnnotation)
                .build();

            ClassInfo controllerClass = ClassInfo.builder()
                .fullyQualifiedName("com.test.UserController")
                .classType(ClassType.CLASS)
                .addAnnotation(restControllerAnnotation)
                .addMethod(getMethod)
                .build();

            JarContent jarContent = jarContentBuilder
                .addClass(controllerClass)
                .build();

            // When
            WebMvcAnalysisResult result = analyzer.analyzeWebMvcMappings(jarContent);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isSuccessful()).isTrue();
            assertThat(result.getMappingCount()).isEqualTo(1);
            assertThat(result.getControllerCount()).isEqualTo(1);

            WebMvcMappingInfo mapping = result.getMappings().get(0);
            assertThat(mapping.getControllerClass()).isEqualTo("com.test.UserController");
            assertThat(mapping.getMethodName()).isEqualTo("getUsers");
            assertThat(mapping.getPath()).isEqualTo("/users");
            assertThat(mapping.getHttpMethods()).containsExactly("GET");
        }
    }

    @Nested
    @DisplayName("HTTP Method Detection Tests")
    class HttpMethodDetectionTests {

        @Test
        @DisplayName("Should detect POST mapping")
        void shouldDetectPostMapping() {
            // Given
            WebMvcMappingInfo mapping = createMappingWithAnnotation(
                "org.springframework.web.bind.annotation.PostMapping",
                Map.of("value", "/users"));

            // Then
            assertThat(mapping.getHttpMethods()).containsExactly("POST");
            assertThat(mapping.getPath()).isEqualTo("/users");
        }

        @Test
        @DisplayName("Should detect PUT mapping")
        void shouldDetectPutMapping() {
            // Given
            WebMvcMappingInfo mapping = createMappingWithAnnotation(
                "org.springframework.web.bind.annotation.PutMapping",
                Map.of("value", "/users/{id}"));

            // Then
            assertThat(mapping.getHttpMethods()).containsExactly("PUT");
            assertThat(mapping.getPath()).isEqualTo("/users/{id}");
        }

        @Test
        @DisplayName("Should detect DELETE mapping")
        void shouldDetectDeleteMapping() {
            // Given
            WebMvcMappingInfo mapping = createMappingWithAnnotation(
                "org.springframework.web.bind.annotation.DeleteMapping",
                Map.of("value", "/users/{id}"));

            // Then
            assertThat(mapping.getHttpMethods()).containsExactly("DELETE");
            assertThat(mapping.getPath()).isEqualTo("/users/{id}");
        }

        @Test
        @DisplayName("Should detect PATCH mapping")
        void shouldDetectPatchMapping() {
            // Given
            WebMvcMappingInfo mapping = createMappingWithAnnotation(
                "org.springframework.web.bind.annotation.PatchMapping",
                Map.of("value", "/users/{id}"));

            // Then
            assertThat(mapping.getHttpMethods()).containsExactly("PATCH");
            assertThat(mapping.getPath()).isEqualTo("/users/{id}");
        }

        @Test
        @DisplayName("Should detect RequestMapping with explicit method")
        void shouldDetectRequestMappingWithExplicitMethod() {
            // Given
            WebMvcMappingInfo mapping = createMappingWithAnnotation(
                "org.springframework.web.bind.annotation.RequestMapping",
                Map.of("value", "/users", "method", "POST"));

            // Then
            assertThat(mapping.getHttpMethods()).containsExactly("POST");
            assertThat(mapping.getPath()).isEqualTo("/users");
        }

        @Test
        @DisplayName("Should default to GET for RequestMapping without method")
        void shouldDefaultToGetForRequestMappingWithoutMethod() {
            // Given
            WebMvcMappingInfo mapping = createMappingWithAnnotation(
                "org.springframework.web.bind.annotation.RequestMapping",
                Map.of("value", "/users"));

            // Then
            assertThat(mapping.getHttpMethods()).containsExactly("GET");
            assertThat(mapping.getPath()).isEqualTo("/users");
        }
    }

    @Nested
    @DisplayName("Path Analysis Tests")
    class PathAnalysisTests {

        @Test
        @DisplayName("Should handle empty path")
        void shouldHandleEmptyPath() {
            // Given
            WebMvcMappingInfo mapping = createMappingWithAnnotation(
                "org.springframework.web.bind.annotation.GetMapping",
                Map.of());

            // Then
            assertThat(mapping.getPath()).isEqualTo("/");
        }

        @Test
        @DisplayName("Should clean up path values")
        void shouldCleanUpPathValues() {
            // Given
            WebMvcMappingInfo mapping = createMappingWithAnnotation(
                "org.springframework.web.bind.annotation.GetMapping",
                Map.of("value", "\"/api/users\""));

            // Then
            assertThat(mapping.getPath()).isEqualTo("/api/users");
        }

        @Test
        @DisplayName("Should combine base path with method path")
        void shouldCombineBasePathWithMethodPath() {
            // Given
            AnnotationInfo restControllerAnnotation = createAnnotation(
                "org.springframework.web.bind.annotation.RestController");

            AnnotationInfo requestMappingAnnotation = createAnnotation(
                "org.springframework.web.bind.annotation.RequestMapping",
                Map.of("value", "/api"));

            AnnotationInfo getMappingAnnotation = createAnnotation(
                "org.springframework.web.bind.annotation.GetMapping",
                Map.of("value", "/users"));

            MethodInfo getMethod = MethodInfo.builder()
                .name("getUsers")
                .returnType("java.util.List")
                .declaringClassName("com.test.UserController")
                .addAnnotation(getMappingAnnotation)
                .build();

            ClassInfo controllerClass = ClassInfo.builder()
                .fullyQualifiedName("com.test.UserController")
                .classType(ClassType.CLASS)
                .addAnnotation(restControllerAnnotation)
                .addAnnotation(requestMappingAnnotation)
                .addMethod(getMethod)
                .build();

            JarContent jarContent = jarContentBuilder
                .addClass(controllerClass)
                .build();

            // When
            WebMvcAnalysisResult result = analyzer.analyzeWebMvcMappings(jarContent);

            // Then
            assertThat(result.getMappingCount()).isEqualTo(1);
            WebMvcMappingInfo mapping = result.getMappings().get(0);
            assertThat(mapping.getPath()).isEqualTo("/api/users");
        }
    }

    @Nested
    @DisplayName("Content Type Analysis Tests")
    class ContentTypeAnalysisTests {

        @Test
        @DisplayName("Should extract produces attribute")
        void shouldExtractProducesAttribute() {
            // Given
            WebMvcMappingInfo mapping = createMappingWithAnnotation(
                "org.springframework.web.bind.annotation.GetMapping",
                Map.of("value", "/users", "produces", "application/json"));

            // Then
            assertThat(mapping.getProduces()).containsExactly("application/json");
            assertThat(mapping.isContentTypeSpecific()).isTrue();
        }

        @Test
        @DisplayName("Should extract consumes attribute")
        void shouldExtractConsumesAttribute() {
            // Given
            WebMvcMappingInfo mapping = createMappingWithAnnotation(
                "org.springframework.web.bind.annotation.PostMapping",
                Map.of("value", "/users", "consumes", "application/json"));

            // Then
            assertThat(mapping.getConsumes()).containsExactly("application/json");
            assertThat(mapping.isContentTypeSpecific()).isTrue();
        }

        @Test
        @DisplayName("Should extract headers and params")
        void shouldExtractHeadersAndParams() {
            // Given
            WebMvcMappingInfo mapping = createMappingWithAnnotation(
                "org.springframework.web.bind.annotation.GetMapping",
                Map.of("value", "/users",
                      "headers", "X-API-Version=1.0",
                      "params", "active=true"));

            // Then
            assertThat(mapping.getHeaders()).containsExactly("X-API-Version=1.0");
            assertThat(mapping.getParams()).containsExactly("active=true");
            assertThat(mapping.hasCondition("X-API-Version=1.0")).isTrue();
            assertThat(mapping.hasCondition("active=true")).isTrue();
        }
    }

    @Nested
    @DisplayName("Multiple Controller Analysis Tests")
    class MultipleControllerAnalysisTests {

        @Test
        @DisplayName("Should analyze multiple controllers")
        void shouldAnalyzeMultipleControllers() {
            // Given
            ClassInfo userController = createControllerWithMapping("UserController", "/users");
            ClassInfo productController = createControllerWithMapping("ProductController", "/products");

            JarContent jarContent = jarContentBuilder
                .addClass(userController)
                .addClass(productController)
                .build();

            // When
            WebMvcAnalysisResult result = analyzer.analyzeWebMvcMappings(jarContent);

            // Then
            assertThat(result.getMappingCount()).isEqualTo(2);
            assertThat(result.getControllerCount()).isEqualTo(2);
            assertThat(result.getMappingsByController()).hasSize(2);
        }

        @Test
        @DisplayName("Should group mappings by controller")
        void shouldGroupMappingsByController() {
            // Given
            ClassInfo controller = createControllerWithMultipleMappings();

            JarContent jarContent = jarContentBuilder
                .addClass(controller)
                .build();

            // When
            WebMvcAnalysisResult result = analyzer.analyzeWebMvcMappings(jarContent);

            // Then
            assertThat(result.getMappingCount()).isEqualTo(2);
            assertThat(result.getMappingsForController("com.test.UserController")).hasSize(2);
        }
    }

    // Helper methods

    private AnnotationInfo createAnnotation(String type) {
        return createAnnotation(type, Map.of());
    }

    private AnnotationInfo createAnnotation(String type, Map<String, Object> attributes) {
        return AnnotationInfo.builder()
            .type(type)
            .attributes(new HashMap<>(attributes))
            .build();
    }

    private WebMvcMappingInfo createMappingWithAnnotation(String annotationType, Map<String, Object> attributes) {
        AnnotationInfo restControllerAnnotation = createAnnotation(
            "org.springframework.web.bind.annotation.RestController");

        AnnotationInfo mappingAnnotation = createAnnotation(annotationType, attributes);

        MethodInfo method = MethodInfo.builder()
            .name("testMethod")
            .returnType("java.lang.String")
            .declaringClassName("com.test.TestController")
            .addAnnotation(mappingAnnotation)
            .build();

        ClassInfo controllerClass = ClassInfo.builder()
            .fullyQualifiedName("com.test.TestController")
            .classType(ClassType.CLASS)
            .addAnnotation(restControllerAnnotation)
            .addMethod(method)
            .build();

        JarContent jarContent = jarContentBuilder
            .addClass(controllerClass)
            .build();

        WebMvcAnalysisResult result = analyzer.analyzeWebMvcMappings(jarContent);
        return result.getMappings().get(0);
    }

    private ClassInfo createControllerWithMapping(String className, String path) {
        AnnotationInfo restControllerAnnotation = createAnnotation(
            "org.springframework.web.bind.annotation.RestController");

        AnnotationInfo getMappingAnnotation = createAnnotation(
            "org.springframework.web.bind.annotation.GetMapping",
            Map.of("value", path));

        MethodInfo getMethod = MethodInfo.builder()
            .name("getAll")
            .returnType("java.util.List")
            .declaringClassName("com.test.ApiController")
            .addAnnotation(getMappingAnnotation)
            .build();

        return ClassInfo.builder()
            .fullyQualifiedName("com.test." + className)
            .classType(ClassType.CLASS)
            .addAnnotation(restControllerAnnotation)
            .addMethod(getMethod)
            .build();
    }

    private ClassInfo createControllerWithMultipleMappings() {
        AnnotationInfo restControllerAnnotation = createAnnotation(
            "org.springframework.web.bind.annotation.RestController");

        AnnotationInfo getMappingAnnotation = createAnnotation(
            "org.springframework.web.bind.annotation.GetMapping",
            Map.of("value", "/users"));

        AnnotationInfo postMappingAnnotation = createAnnotation(
            "org.springframework.web.bind.annotation.PostMapping",
            Map.of("value", "/users"));

        MethodInfo getMethod = MethodInfo.builder()
            .name("getUsers")
            .returnType("java.util.List")
            .declaringClassName("com.test.UserController")
            .addAnnotation(getMappingAnnotation)
            .build();

        MethodInfo postMethod = MethodInfo.builder()
            .name("createUser")
            .returnType("com.test.User")
            .declaringClassName("com.test.UserController")
            .addAnnotation(postMappingAnnotation)
            .build();

        return ClassInfo.builder()
            .fullyQualifiedName("com.test.UserController")
            .classType(ClassType.CLASS)
            .addAnnotation(restControllerAnnotation)
            .addMethod(getMethod)
            .addMethod(postMethod)
            .build();
    }
}