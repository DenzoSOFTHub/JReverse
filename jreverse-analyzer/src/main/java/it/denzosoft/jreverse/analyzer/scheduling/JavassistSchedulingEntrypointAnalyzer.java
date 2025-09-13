package it.denzosoft.jreverse.analyzer.scheduling;

import it.denzosoft.jreverse.core.model.AnnotationInfo;
import it.denzosoft.jreverse.core.model.ClassInfo;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.model.MethodInfo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Javassist-based implementation of SchedulingEntrypointAnalyzer.
 * Analyzes scheduling patterns and identifies scheduling-related configurations.
 */
public class JavassistSchedulingEntrypointAnalyzer implements SchedulingEntrypointAnalyzer {
    
    private static final Set<String> SCHEDULING_ANNOTATIONS = new HashSet<>();
    private static final Map<String, SchedulingEntrypointType> ANNOTATION_TYPE_MAP = new HashMap<>();
    
    static {
        SCHEDULING_ANNOTATIONS.add("org.springframework.scheduling.annotation.Scheduled");
        SCHEDULING_ANNOTATIONS.add("org.springframework.scheduling.annotation.EnableScheduling");
        SCHEDULING_ANNOTATIONS.add("org.springframework.scheduling.annotation.Async");
        
        ANNOTATION_TYPE_MAP.put("org.springframework.scheduling.annotation.Scheduled", SchedulingEntrypointType.SCHEDULED_METHOD);
        ANNOTATION_TYPE_MAP.put("org.springframework.scheduling.annotation.EnableScheduling", SchedulingEntrypointType.ENABLE_SCHEDULING);
        ANNOTATION_TYPE_MAP.put("org.springframework.scheduling.annotation.Async", SchedulingEntrypointType.ASYNC_SCHEDULED);
    }
    
    @Override
    public SchedulingAnalysisResult analyze(JarContent jarContent) {
        long startTime = System.currentTimeMillis();
        List<SchedulingEntrypointInfo> allEntrypoints = new ArrayList<>();
        
        for (ClassInfo classInfo : jarContent.getClasses()) {
            if (hasSchedulingPatterns(classInfo)) {
                SchedulingAnalysisResult classResult = analyzeClass(classInfo);
                allEntrypoints.addAll(classResult.getSchedulingEntrypoints());
            }
        }
        
        long analysisTime = System.currentTimeMillis() - startTime;
        String fileName = jarContent.getLocation() != null ? jarContent.getLocation().getFileName() : "";
        return new SchedulingAnalysisResult(allEntrypoints, analysisTime, fileName);
    }
    
    @Override
    public SchedulingAnalysisResult analyzeClass(ClassInfo classInfo) {
        List<SchedulingEntrypointInfo> entrypoints = new ArrayList<>();
        
        // Analyze class-level scheduling annotations (@EnableScheduling)
        List<AnnotationInfo> classAnnotations = getSchedulingAnnotations(new ArrayList<>(classInfo.getAnnotations()));
        for (AnnotationInfo annotation : classAnnotations) {
            SchedulingEntrypointInfo entrypoint = analyzeSchedulingAnnotation(
                annotation, classInfo.getFullyQualifiedName(), "<class-level>", "void"
            );
            if (entrypoint != null) {
                entrypoints.add(entrypoint);
            }
        }
        
        // Analyze method-level scheduling patterns
        for (MethodInfo method : classInfo.getMethods()) {
            SchedulingEntrypointInfo entrypoint = analyzeMethod(method, classInfo.getFullyQualifiedName());
            if (entrypoint != null) {
                entrypoints.add(entrypoint);
            }
        }
        
        return new SchedulingAnalysisResult(entrypoints, 0L, "");
    }
    
    private boolean hasSchedulingPatterns(ClassInfo classInfo) {
        // Check class-level annotations
        if (classInfo.getAnnotations().stream().anyMatch(this::isSchedulingAnnotation)) {
            return true;
        }
        
        // Check method-level patterns
        return classInfo.getMethods().stream()
            .anyMatch(method -> hasSchedulingPattern(method));
    }
    
    private boolean hasSchedulingPattern(MethodInfo method) {
        // Check scheduling annotations
        return method.getAnnotations().stream().anyMatch(this::isSchedulingAnnotation);
    }
    
    private boolean isSchedulingAnnotation(AnnotationInfo annotation) {
        return SCHEDULING_ANNOTATIONS.contains(annotation.getType());
    }
    
    private List<AnnotationInfo> getSchedulingAnnotations(List<AnnotationInfo> annotations) {
        return annotations.stream()
            .filter(this::isSchedulingAnnotation)
            .collect(Collectors.toList());
    }
    
    private SchedulingEntrypointInfo analyzeMethod(MethodInfo method, String className) {
        // Check for scheduling annotations
        List<AnnotationInfo> methodAnnotations = getSchedulingAnnotations(new ArrayList<>(method.getAnnotations()));
        if (!methodAnnotations.isEmpty()) {
            return analyzeSchedulingAnnotation(methodAnnotations.get(0), className, method.getName(), method.getReturnType());
        }
        
        return null;
    }
    
    private SchedulingEntrypointInfo analyzeSchedulingAnnotation(AnnotationInfo annotation, String className, 
                                                               String methodName, String returnType) {
        SchedulingEntrypointType type = ANNOTATION_TYPE_MAP.get(annotation.getType());
        if (type == null) {
            return null;
        }
        
        SchedulingEntrypointInfo.Builder builder = SchedulingEntrypointInfo.builder()
            .methodName(methodName)
            .declaringClass(className)
            .schedulingType(type)
            .isVoid("void".equals(returnType))
            .sourceAnnotation(annotation);
        
        // Extract scheduling parameters from @Scheduled annotation
        if (type == SchedulingEntrypointType.SCHEDULED_METHOD) {
            extractSchedulingParameters(annotation, builder);
        }
        
        // Check if it's also async
        boolean isAsync = hasAsyncAnnotation(annotation) || 
                         (methodName != null && !methodName.equals("<class-level>") && 
                          className != null && isMethodAsync(className, methodName));
        builder.isAsync(isAsync);
        
        return builder.build();
    }
    
    private void extractSchedulingParameters(AnnotationInfo annotation, SchedulingEntrypointInfo.Builder builder) {
        if (annotation.getAttributes() == null || annotation.getAttributes().isEmpty()) {
            return;
        }
        
        // Extract cron expression
        Object cron = annotation.getAttributes().get("cron");
        if (cron instanceof String && !((String) cron).trim().isEmpty()) {
            builder.cronExpression((String) cron);
        }
        
        // Extract fixed rate
        Object fixedRate = annotation.getAttributes().get("fixedRate");
        if (fixedRate instanceof Number) {
            builder.fixedRate(((Number) fixedRate).longValue());
        } else if (fixedRate instanceof String) {
            try {
                builder.fixedRate(Long.parseLong((String) fixedRate));
            } catch (NumberFormatException e) {
                // Ignore invalid values
            }
        }
        
        // Extract fixed delay
        Object fixedDelay = annotation.getAttributes().get("fixedDelay");
        if (fixedDelay instanceof Number) {
            builder.fixedDelay(((Number) fixedDelay).longValue());
        } else if (fixedDelay instanceof String) {
            try {
                builder.fixedDelay(Long.parseLong((String) fixedDelay));
            } catch (NumberFormatException e) {
                // Ignore invalid values
            }
        }
        
        // Extract initial delay
        Object initialDelay = annotation.getAttributes().get("initialDelay");
        if (initialDelay instanceof Number) {
            builder.initialDelay(((Number) initialDelay).longValue());
        } else if (initialDelay instanceof String) {
            try {
                builder.initialDelay(Long.parseLong((String) initialDelay));
            } catch (NumberFormatException e) {
                // Ignore invalid values
            }
        }
        
        // Extract time unit
        Object timeUnit = annotation.getAttributes().get("timeUnit");
        if (timeUnit instanceof String) {
            builder.timeUnit((String) timeUnit);
        }
        
        // Extract zone
        Object zone = annotation.getAttributes().get("zone");
        if (zone instanceof String && !((String) zone).trim().isEmpty()) {
            builder.zone((String) zone);
        }
        
        // Extract scheduler name
        Object scheduler = annotation.getAttributes().get("scheduler");
        if (scheduler instanceof String && !((String) scheduler).trim().isEmpty()) {
            builder.schedulerName((String) scheduler);
        }
    }
    
    private boolean hasAsyncAnnotation(AnnotationInfo annotation) {
        return "org.springframework.scheduling.annotation.Async".equals(annotation.getType());
    }
    
    private boolean isMethodAsync(String className, String methodName) {
        // This would require cross-referencing with other analysis results
        // For now, we'll use a simple heuristic
        return false;
    }
}