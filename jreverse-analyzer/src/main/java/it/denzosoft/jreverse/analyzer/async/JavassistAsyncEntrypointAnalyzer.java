package it.denzosoft.jreverse.analyzer.async;

import it.denzosoft.jreverse.core.model.AnnotationInfo;
import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.MethodInfo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Javassist-based implementation of AsyncEntrypointAnalyzer.
 * Analyzes async patterns and identifies async-related configurations.
 */
public class JavassistAsyncEntrypointAnalyzer implements AsyncEntrypointAnalyzer {
    
    private static final Set<String> ASYNC_ANNOTATIONS = new HashSet<>();
    private static final Map<String, AsyncEntrypointType> ANNOTATION_TYPE_MAP = new HashMap<>();
    private static final Set<String> ASYNC_RETURN_TYPES = new HashSet<>();
    
    static {
        ASYNC_ANNOTATIONS.add("org.springframework.scheduling.annotation.Async");
        ASYNC_ANNOTATIONS.add("org.springframework.scheduling.annotation.EnableAsync");
        ASYNC_ANNOTATIONS.add("org.springframework.scheduling.annotation.AsyncResult");
        
        ANNOTATION_TYPE_MAP.put("org.springframework.scheduling.annotation.Async", AsyncEntrypointType.ASYNC_METHOD);
        ANNOTATION_TYPE_MAP.put("org.springframework.scheduling.annotation.EnableAsync", AsyncEntrypointType.ENABLE_ASYNC);
        ANNOTATION_TYPE_MAP.put("org.springframework.scheduling.annotation.AsyncResult", AsyncEntrypointType.ASYNC_RESULT);
        
        ASYNC_RETURN_TYPES.add("java.util.concurrent.Future");
        ASYNC_RETURN_TYPES.add("java.util.concurrent.CompletableFuture");
        ASYNC_RETURN_TYPES.add("org.springframework.util.concurrent.ListenableFuture");
        ASYNC_RETURN_TYPES.add("org.springframework.web.context.request.async.DeferredResult");
        ASYNC_RETURN_TYPES.add("java.util.concurrent.Callable");
        ASYNC_RETURN_TYPES.add("reactor.core.publisher.Mono");
        ASYNC_RETURN_TYPES.add("reactor.core.publisher.Flux");
    }
    
    @Override
    public AsyncAnalysisResult analyze(JarContent jarContent) {
        long startTime = System.currentTimeMillis();
        List<AsyncEntrypointInfo> allEntrypoints = new ArrayList<>();
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            if (hasAsyncPatterns(classInfo)) {
                AsyncAnalysisResult classResult = analyzeClass(classInfo);
                allEntrypoints.addAll(classResult.getAsyncEntrypoints());
            }
        }
        
        long analysisTime = System.currentTimeMillis() - startTime;
        String fileName = jarContent.getLocation() != null ? jarContent.getLocation().getFileName() : "";
        return new AsyncAnalysisResult(allEntrypoints, analysisTime, fileName);
    }
    
    @Override
    public AsyncAnalysisResult analyzeClass(ClassInfo classInfo) {
        List<AsyncEntrypointInfo> entrypoints = new ArrayList<>();
        
        // Analyze class-level async annotations (@EnableAsync)
        List<AnnotationInfo> classAnnotations = getAsyncAnnotations(new ArrayList<>(classInfo.getAnnotations()));
        for (AnnotationInfo annotation : classAnnotations) {
            AsyncEntrypointInfo entrypoint = analyzeAsyncAnnotation(
                annotation, classInfo.getFullyQualifiedName(), "<class-level>", "void"
            );
            if (entrypoint != null) {
                entrypoints.add(entrypoint);
            }
        }
        
        // Analyze method-level async patterns
        for (MethodInfo method : classInfo.getMethods()) {
            AsyncEntrypointInfo entrypoint = analyzeMethod(method, classInfo.getFullyQualifiedName());
            if (entrypoint != null) {
                entrypoints.add(entrypoint);
            }
        }
        
        return new AsyncAnalysisResult(entrypoints, 0L, "");
    }
    
    private boolean hasAsyncPatterns(ClassInfo classInfo) {
        // Check class-level annotations
        if (classInfo.getAnnotations().stream().anyMatch(this::isAsyncAnnotation)) {
            return true;
        }
        
        // Check method-level patterns (annotations or return types)
        return classInfo.getMethods().stream()
            .anyMatch(method -> hasAsyncPattern(method));
    }
    
    private boolean hasAsyncPattern(MethodInfo method) {
        // Check async annotations
        if (method.getAnnotations().stream().anyMatch(this::isAsyncAnnotation)) {
            return true;
        }
        
        // Check async return types
        return isAsyncReturnType(method.getReturnType());
    }
    
    private boolean isAsyncAnnotation(AnnotationInfo annotation) {
        return ASYNC_ANNOTATIONS.contains(annotation.getType());
    }
    
    private boolean isAsyncReturnType(String returnType) {
        if (returnType == null) return false;
        
        return ASYNC_RETURN_TYPES.stream()
            .anyMatch(returnType::contains);
    }
    
    private List<AnnotationInfo> getAsyncAnnotations(List<AnnotationInfo> annotations) {
        return annotations.stream()
            .filter(this::isAsyncAnnotation)
            .collect(Collectors.toList());
    }
    
    private AsyncEntrypointInfo analyzeMethod(MethodInfo method, String className) {
        // First check for async annotations
        List<AnnotationInfo> methodAnnotations = getAsyncAnnotations(new ArrayList<>(method.getAnnotations()));
        if (!methodAnnotations.isEmpty()) {
            return analyzeAsyncAnnotation(methodAnnotations.get(0), className, method.getName(), method.getReturnType());
        }
        
        // Then check for async return types
        if (isAsyncReturnType(method.getReturnType())) {
            return analyzeAsyncReturnType(method, className);
        }
        
        return null;
    }
    
    private AsyncEntrypointInfo analyzeAsyncAnnotation(AnnotationInfo annotation, String className, 
                                                       String methodName, String returnType) {
        AsyncEntrypointType type = ANNOTATION_TYPE_MAP.get(annotation.getType());
        if (type == null) {
            return null;
        }
        
        AsyncEntrypointInfo.Builder builder = AsyncEntrypointInfo.builder()
            .methodName(methodName)
            .declaringClass(className)
            .asyncType(type)
            .returnType(returnType)
            .sourceAnnotation(annotation);
        
        // Extract executor name from @Async annotation
        if (type == AsyncEntrypointType.ASYNC_METHOD) {
            String executorName = extractExecutorName(annotation);
            if (executorName != null && !executorName.isEmpty()) {
                builder.executorName(executorName);
            }
        }
        
        // Extract timeout if present
        Long timeout = extractTimeout(annotation);
        if (timeout != null) {
            builder.timeout(timeout);
        }
        
        return builder.build();
    }
    
    private AsyncEntrypointInfo analyzeAsyncReturnType(MethodInfo method, String className) {
        String returnType = method.getReturnType();
        AsyncEntrypointType type = determineTypeFromReturnType(returnType);
        
        AsyncEntrypointInfo.Builder builder = AsyncEntrypointInfo.builder()
            .methodName(method.getName())
            .declaringClass(className)
            .asyncType(type)
            .returnType(returnType);
        
        // Add thrown exceptions for error handling analysis
        if (method.getThrownExceptions() != null && !method.getThrownExceptions().isEmpty()) {
            builder.exceptions(method.getThrownExceptions());
        }
        
        return builder.build();
    }
    
    private AsyncEntrypointType determineTypeFromReturnType(String returnType) {
        if (returnType.contains("CompletableFuture")) {
            return AsyncEntrypointType.COMPLETABLE_FUTURE;
        } else if (returnType.contains("DeferredResult")) {
            return AsyncEntrypointType.DEFERRED_RESULT;
        } else if (returnType.contains("Callable")) {
            return AsyncEntrypointType.CALLABLE_CONTROLLER;
        } else if (returnType.contains("Mono")) {
            return AsyncEntrypointType.WEBFLUX_MONO;
        } else if (returnType.contains("Flux")) {
            return AsyncEntrypointType.WEBFLUX_FLUX;
        } else if (returnType.contains("Future")) {
            return AsyncEntrypointType.COMPLETABLE_FUTURE; // Default Future type
        }
        
        return AsyncEntrypointType.ASYNC_METHOD; // Fallback
    }
    
    private String extractExecutorName(AnnotationInfo annotation) {
        if (annotation.getAttributes() == null || annotation.getAttributes().isEmpty()) {
            return null;
        }
        
        Object value = annotation.getAttributes().get("value");
        if (value instanceof String) {
            return (String) value;
        }
        
        return null;
    }
    
    private Long extractTimeout(AnnotationInfo annotation) {
        if (annotation.getAttributes() == null || annotation.getAttributes().isEmpty()) {
            return null;
        }
        
        Object timeout = annotation.getAttributes().get("timeout");
        if (timeout instanceof Number) {
            return ((Number) timeout).longValue();
        } else if (timeout instanceof String) {
            try {
                return Long.parseLong((String) timeout);
            } catch (NumberFormatException e) {
                // Ignore invalid timeout values
            }
        }
        
        return null;
    }
}