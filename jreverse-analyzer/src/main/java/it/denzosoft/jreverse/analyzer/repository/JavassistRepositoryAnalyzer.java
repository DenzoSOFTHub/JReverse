package it.denzosoft.jreverse.analyzer.repository;

import it.denzosoft.jreverse.core.model.*;
import it.denzosoft.jreverse.core.port.RepositoryAnalyzer;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Javassist-based implementation for analyzing repository patterns, JPA repositories,
 * and data access layer components in JAR files.
 * 
 * This analyzer identifies:
 * - @Repository annotated classes
 * - JPA Repository interfaces (extending JpaRepository, CrudRepository, etc.)
 * - Custom query methods and native queries
 * - Data access patterns and potential issues
 */
public class JavassistRepositoryAnalyzer implements RepositoryAnalyzer {
    
    private static final Logger LOGGER = Logger.getLogger(JavassistRepositoryAnalyzer.class.getName());
    
    // Spring Data JPA repository interfaces
    private static final Set<String> JPA_REPOSITORY_INTERFACES = Set.of(
        "org.springframework.data.jpa.repository.JpaRepository",
        "org.springframework.data.repository.CrudRepository",
        "org.springframework.data.repository.PagingAndSortingRepository",
        "org.springframework.data.repository.Repository"
    );
    
    // Repository-related annotations
    private static final Set<String> REPOSITORY_ANNOTATIONS = Set.of(
        "org.springframework.stereotype.Repository",
        "javax.persistence.Repository",
        "jakarta.persistence.Repository"
    );
    
    // Transactional annotations
    private static final Set<String> TRANSACTIONAL_ANNOTATIONS = Set.of(
        "org.springframework.transaction.annotation.Transactional",
        "javax.transaction.Transactional",
        "jakarta.transaction.Transactional"
    );
    
    // JPA/Query annotations
    private static final Set<String> QUERY_ANNOTATIONS = Set.of(
        "org.springframework.data.jpa.repository.Query",
        "org.springframework.data.jpa.repository.Modifying",
        "javax.persistence.NamedQuery",
        "javax.persistence.NamedQueries",
        "jakarta.persistence.NamedQuery",
        "jakarta.persistence.NamedQueries"
    );
    
    @Override
    public boolean canAnalyze(JarContent jarContent) {
        return jarContent != null && !jarContent.getClasses().isEmpty();
    }
    
    @Override
    public RepositoryAnalysisResult analyzeRepositories(JarContent jarContent) {
        Objects.requireNonNull(jarContent, "JarContent cannot be null");
        
        LOGGER.info("Starting repository analysis for JAR: " + jarContent.getLocation().getPath());
        long startTime = System.currentTimeMillis();
        
        try {
            // Set up Javassist ClassPool
            ClassPool classPool = setupClassPool(jarContent);
            
            // Collect repository classes and interfaces
            List<RepositoryComponentInfo> repositories = new ArrayList<>();
            List<JpaRepositoryInfo> jpaRepositories = new ArrayList<>();
            List<RepositoryIssue> issues = new ArrayList<>();
            
            // Analyze all classes for repository patterns
            for (ClassInfo classInfo : jarContent.getClasses()) {
                try {
                    analyzeClassForRepository(classPool, classInfo, repositories, jpaRepositories, issues);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to analyze class: " + classInfo.getFullyQualifiedName(), e);
                    issues.add(createAnalysisErrorIssue(classInfo.getFullyQualifiedName(), e.getMessage()));
                }
            }
            
            // Group repositories by package
            Map<String, List<RepositoryComponentInfo>> repositoriesByPackage = repositories.stream()
                .collect(Collectors.groupingBy(RepositoryComponentInfo::getPackageName));
            
            // Calculate metrics
            RepositoryMetrics metrics = calculateRepositoryMetrics(repositories, jpaRepositories);
            
            // Detect repository layer issues
            detectRepositoryIssues(repositories, jpaRepositories, issues);
            
            // Create summary
            RepositorySummary summary = createRepositorySummary(repositories, jpaRepositories, issues, metrics);
            
            long endTime = System.currentTimeMillis();
            LOGGER.info("Repository analysis completed in " + (endTime - startTime) + "ms. " +
                       "Found " + repositories.size() + " repositories, " + 
                       jpaRepositories.size() + " JPA repositories, " +
                       issues.size() + " issues.");
            
            return RepositoryAnalysisResult.builder()
                .repositories(repositories)
                .jpaRepositories(jpaRepositories)
                .repositoriesByPackage(repositoriesByPackage)
                .metrics(metrics)
                .issues(issues)
                .summary(summary)
                .build();
                
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Repository analysis failed", e);
            // Return empty result with error
            List<RepositoryIssue> errorIssues = List.of(
                createAnalysisErrorIssue("ANALYSIS_ERROR", "Repository analysis failed: " + e.getMessage())
            );
            return RepositoryAnalysisResult.builder()
                .repositories(Collections.emptyList())
                .jpaRepositories(Collections.emptyList())
                .repositoriesByPackage(Collections.emptyMap())
                .metrics(RepositoryMetrics.builder().build())
                .issues(errorIssues)
                .summary(RepositorySummary.builder()
                    .totalRepositories(0)
                    .jpaRepositories(0)
                    .customRepositories(0)
                    .totalIssues(errorIssues.size())
                    .qualityRating("Error")
                    .hasGoodDataAccess(false)
                    .build())
                .build();
        }
    }
    
    private ClassPool setupClassPool(JarContent jarContent) throws NotFoundException {
        ClassPool classPool = ClassPool.getDefault();
        
        // Add JAR path to classpath if possible
        try {
            classPool.appendClassPath(jarContent.getLocation().getPath().toString());
        } catch (NotFoundException e) {
            LOGGER.warning("Could not add JAR to classpath: " + e.getMessage());
        }
        
        return classPool;
    }
    
    private void analyzeClassForRepository(ClassPool classPool, ClassInfo classInfo, 
                                         List<RepositoryComponentInfo> repositories,
                                         List<JpaRepositoryInfo> jpaRepositories,
                                         List<RepositoryIssue> issues) throws NotFoundException {
        
        CtClass ctClass = classPool.get(classInfo.getFullyQualifiedName());
        
        // Check for @Repository annotation
        if (isRepositoryComponent(ctClass)) {
            repositories.add(analyzeRepositoryComponent(ctClass));
        }
        
        // Check for JPA Repository interface
        if (isJpaRepositoryInterface(ctClass)) {
            jpaRepositories.add(analyzeJpaRepository(ctClass));
        }
    }
    
    private boolean isRepositoryComponent(CtClass ctClass) {
        try {
            // Check for @Repository annotation
            if (hasRepositoryAnnotation(ctClass)) {
                return true;
            }
            
            // Check naming conventions - classes ending with "Repository" or "Dao"
            String className = ctClass.getSimpleName();
            if (className.endsWith("Repository") || className.endsWith("Dao")) {
                return true;
            }
            
            // Check if class is in repository package
            String packageName = ctClass.getPackageName();
            if (packageName != null && packageName.contains("repository")) {
                return true;
            }
            
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Error checking repository component: " + ctClass.getName(), e);
            return false;
        }
    }
    
    private boolean isJpaRepositoryInterface(CtClass ctClass) {
        try {
            if (!ctClass.isInterface()) {
                return false;
            }
            
            // Check if extends known JPA repository interfaces
            return extendsJpaRepositoryInterface(ctClass);
            
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Error checking JPA repository: " + ctClass.getName(), e);
            return false;
        }
    }
    
    private boolean hasRepositoryAnnotation(CtClass ctClass) {
        try {
            ClassFile classFile = ctClass.getClassFile();
            AnnotationsAttribute visible = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag);
            
            if (visible != null) {
                for (Annotation annotation : visible.getAnnotations()) {
                    if (REPOSITORY_ANNOTATIONS.contains(annotation.getTypeName())) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Error checking repository annotation", e);
            return false;
        }
    }
    
    private boolean extendsJpaRepositoryInterface(CtClass ctClass) {
        try {
            // Check direct interfaces
            CtClass[] interfaces = ctClass.getInterfaces();
            for (CtClass iface : interfaces) {
                String interfaceName = iface.getName();
                if (JPA_REPOSITORY_INTERFACES.contains(interfaceName)) {
                    return true;
                }
                
                // Check if interface name contains repository keywords
                if (interfaceName.contains("Repository") || interfaceName.contains("repository")) {
                    return true;
                }
            }
            
            // Check superclass interfaces recursively
            CtClass superClass = ctClass.getSuperclass();
            if (superClass != null && !superClass.getName().equals("java.lang.Object")) {
                return extendsJpaRepositoryInterface(superClass);
            }
            
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Error checking JPA repository interface", e);
            return false;
        }
    }
    
    private RepositoryComponentInfo analyzeRepositoryComponent(CtClass ctClass) {
        try {
            String className = ctClass.getName();
            String packageName = ctClass.getPackageName();
            String repositoryName = extractRepositoryName(className);
            String entityType = extractEntityType(ctClass);
            List<String> customMethods = extractCustomMethods(ctClass);
            boolean isJpaRepository = extendsJpaRepositoryInterface(ctClass);
            boolean isTransactional = hasTransactionalAnnotation(ctClass);
            String scope = extractScope(ctClass);
            
            return RepositoryComponentInfo.builder()
                .className(className)
                .packageName(packageName)
                .repositoryName(repositoryName)
                .entityType(entityType)
                .customMethods(customMethods)
                .isJpaRepository(isJpaRepository)
                .isTransactional(isTransactional)
                .scope(scope)
                .build();
                
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error analyzing repository component: " + ctClass.getName(), e);
            return RepositoryComponentInfo.builder()
                .className(ctClass.getName())
                .packageName(ctClass.getPackageName())
                .repositoryName(ctClass.getSimpleName())
                .build();
        }
    }
    
    private JpaRepositoryInfo analyzeJpaRepository(CtClass ctClass) {
        try {
            String interfaceName = ctClass.getName();
            String packageName = ctClass.getPackageName();
            String[] entityAndIdTypes = extractEntityAndIdTypes(ctClass);
            String entityType = entityAndIdTypes[0];
            String idType = entityAndIdTypes[1];
            List<String> queryMethods = extractQueryMethods(ctClass);
            List<String> customQueries = extractCustomQueries(ctClass);
            boolean hasNativeQueries = hasNativeQueries(ctClass);
            boolean hasNamedQueries = hasNamedQueries(ctClass);
            
            return JpaRepositoryInfo.builder()
                .interfaceName(interfaceName)
                .packageName(packageName)
                .entityType(entityType)
                .idType(idType)
                .queryMethods(queryMethods)
                .customQueries(customQueries)
                .hasNativeQueries(hasNativeQueries)
                .hasNamedQueries(hasNamedQueries)
                .build();
                
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error analyzing JPA repository: " + ctClass.getName(), e);
            return JpaRepositoryInfo.builder()
                .interfaceName(ctClass.getName())
                .packageName(ctClass.getPackageName())
                .build();
        }
    }
    
    private String extractRepositoryName(String className) {
        String simpleName = className.substring(className.lastIndexOf('.') + 1);
        if (simpleName.endsWith("Repository")) {
            return simpleName.substring(0, simpleName.length() - "Repository".length());
        }
        if (simpleName.endsWith("Dao")) {
            return simpleName.substring(0, simpleName.length() - "Dao".length());
        }
        return simpleName;
    }
    
    private String extractEntityType(CtClass ctClass) {
        try {
            // Try to infer from generic type parameters in interfaces
            CtClass[] interfaces = ctClass.getInterfaces();
            for (CtClass iface : interfaces) {
                String interfaceName = iface.getName();
                if (JPA_REPOSITORY_INTERFACES.contains(interfaceName)) {
                    // For simplicity, use naming convention
                    String repositoryName = extractRepositoryName(ctClass.getName());
                    if (!repositoryName.isEmpty()) {
                        return ctClass.getPackageName().replace(".repository", ".entity") + "." + repositoryName;
                    }
                }
            }
            
            // Fallback to naming convention
            String repositoryName = extractRepositoryName(ctClass.getName());
            return repositoryName.isEmpty() ? "Unknown" : repositoryName;
            
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    private String[] extractEntityAndIdTypes(CtClass ctClass) {
        try {
            // Simplified extraction - in real implementation would parse generic signatures
            String repositoryName = extractRepositoryName(ctClass.getName());
            String entityType = repositoryName.isEmpty() ? "Unknown" : repositoryName;
            String idType = "Long"; // Common default
            
            return new String[]{entityType, idType};
        } catch (Exception e) {
            return new String[]{"Unknown", "Unknown"};
        }
    }
    
    private List<String> extractCustomMethods(CtClass ctClass) {
        List<String> methods = new ArrayList<>();
        try {
            CtMethod[] ctMethods = ctClass.getDeclaredMethods();
            for (CtMethod method : ctMethods) {
                // Skip standard CRUD methods
                String methodName = method.getName();
                if (!isStandardCrudMethod(methodName)) {
                    methods.add(methodName);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Error extracting custom methods", e);
        }
        return methods;
    }
    
    private List<String> extractQueryMethods(CtClass ctClass) {
        List<String> queryMethods = new ArrayList<>();
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            for (CtMethod method : methods) {
                String methodName = method.getName();
                // JPA query method naming conventions
                if (methodName.startsWith("find") || methodName.startsWith("get") || 
                    methodName.startsWith("read") || methodName.startsWith("query") ||
                    methodName.startsWith("count") || methodName.startsWith("exists")) {
                    queryMethods.add(methodName);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Error extracting query methods", e);
        }
        return queryMethods;
    }
    
    private List<String> extractCustomQueries(CtClass ctClass) {
        List<String> customQueries = new ArrayList<>();
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            for (CtMethod method : methods) {
                if (hasQueryAnnotation(method)) {
                    customQueries.add(method.getName());
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Error extracting custom queries", e);
        }
        return customQueries;
    }
    
    private boolean hasTransactionalAnnotation(CtClass ctClass) {
        try {
            ClassFile classFile = ctClass.getClassFile();
            AnnotationsAttribute visible = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag);
            
            if (visible != null) {
                for (Annotation annotation : visible.getAnnotations()) {
                    if (TRANSACTIONAL_ANNOTATIONS.contains(annotation.getTypeName())) {
                        return true;
                    }
                }
            }
            
            // Check methods for @Transactional
            CtMethod[] methods = ctClass.getDeclaredMethods();
            for (CtMethod method : methods) {
                if (hasTransactionalAnnotation(method)) {
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean hasTransactionalAnnotation(CtMethod method) {
        try {
            AnnotationsAttribute visible = (AnnotationsAttribute) method.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
            if (visible != null) {
                for (Annotation annotation : visible.getAnnotations()) {
                    if (TRANSACTIONAL_ANNOTATIONS.contains(annotation.getTypeName())) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean hasQueryAnnotation(CtMethod method) {
        try {
            AnnotationsAttribute visible = (AnnotationsAttribute) method.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
            if (visible != null) {
                for (Annotation annotation : visible.getAnnotations()) {
                    if (QUERY_ANNOTATIONS.contains(annotation.getTypeName())) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean hasNativeQueries(CtClass ctClass) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            for (CtMethod method : methods) {
                if (hasNativeQueryAnnotation(method)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean hasNativeQueryAnnotation(CtMethod method) {
        // Simplified check - would need to parse annotation values for nativeQuery=true
        return hasQueryAnnotation(method);
    }
    
    private boolean hasNamedQueries(CtClass ctClass) {
        try {
            ClassFile classFile = ctClass.getClassFile();
            AnnotationsAttribute visible = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag);
            
            if (visible != null) {
                for (Annotation annotation : visible.getAnnotations()) {
                    String typeName = annotation.getTypeName();
                    if (typeName.contains("NamedQuery") || typeName.contains("NamedQueries")) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    private String extractScope(CtClass ctClass) {
        // Simplified scope detection - would parse @Scope annotation in real implementation
        return "singleton";
    }
    
    private boolean isStandardCrudMethod(String methodName) {
        return methodName.equals("save") || methodName.equals("delete") || 
               methodName.equals("findById") || methodName.equals("findAll") ||
               methodName.equals("count") || methodName.equals("existsById");
    }
    
    private RepositoryMetrics calculateRepositoryMetrics(List<RepositoryComponentInfo> repositories,
                                                        List<JpaRepositoryInfo> jpaRepositories) {
        int totalRepositories = repositories.size() + jpaRepositories.size();
        int jpaRepoCount = jpaRepositories.size();
        int customRepoCount = repositories.size();
        
        int totalQueryMethods = jpaRepositories.stream()
            .mapToInt(repo -> repo.getQueryMethods().size())
            .sum();
        
        int nativeQueries = (int) jpaRepositories.stream()
            .filter(JpaRepositoryInfo::hasNativeQueries)
            .count();
        
        int customQueries = jpaRepositories.stream()
            .mapToInt(repo -> repo.getCustomQueries().size())
            .sum();
        
        double averageMethodsPerRepository = totalRepositories > 0 ? 
            (double) totalQueryMethods / totalRepositories : 0.0;
        
        return RepositoryMetrics.builder()
            .totalRepositories(totalRepositories)
            .jpaRepositories(jpaRepoCount)
            .customRepositories(customRepoCount)
            .totalQueryMethods(totalQueryMethods)
            .nativeQueries(nativeQueries)
            .customQueries(customQueries)
            .averageMethodsPerRepository(averageMethodsPerRepository)
            .build();
    }
    
    private void detectRepositoryIssues(List<RepositoryComponentInfo> repositories,
                                       List<JpaRepositoryInfo> jpaRepositories,
                                       List<RepositoryIssue> issues) {
        
        // Check repository components
        for (RepositoryComponentInfo repo : repositories) {
            // Check for missing @Transactional
            if (!repo.isTransactional()) {
                issues.add(RepositoryIssue.builder()
                    .type(RepositoryIssue.IssueType.MISSING_TRANSACTIONAL)
                    .severity(RepositoryIssue.Severity.WARNING)
                    .repositoryName(repo.getClassName())
                    .description("Repository class is missing @Transactional annotation")
                    .recommendation("Add @Transactional to ensure proper transaction management")
                    .build());
            }
            
            // Check for no custom methods
            if (repo.getCustomMethods().isEmpty()) {
                issues.add(RepositoryIssue.builder()
                    .type(RepositoryIssue.IssueType.NO_CUSTOM_METHODS)
                    .severity(RepositoryIssue.Severity.INFO)
                    .repositoryName(repo.getClassName())
                    .description("Repository has no custom methods")
                    .recommendation("Consider adding custom query methods if needed")
                    .build());
            }
        }
        
        // Check JPA repositories
        for (JpaRepositoryInfo jpaRepo : jpaRepositories) {
            // Check for excessive query methods
            if (jpaRepo.getQueryMethods().size() > 20) {
                issues.add(RepositoryIssue.builder()
                    .type(RepositoryIssue.IssueType.TOO_MANY_QUERY_METHODS)
                    .severity(RepositoryIssue.Severity.WARNING)
                    .repositoryName(jpaRepo.getInterfaceName())
                    .description("Repository has too many query methods (" + jpaRepo.getQueryMethods().size() + ")")
                    .recommendation("Consider splitting into multiple repositories by domain")
                    .build());
            }
            
            // Check for excessive native queries
            if (jpaRepo.hasNativeQueries() && jpaRepo.getCustomQueries().size() > 5) {
                issues.add(RepositoryIssue.builder()
                    .type(RepositoryIssue.IssueType.NATIVE_QUERY_OVERUSE)
                    .severity(RepositoryIssue.Severity.WARNING)
                    .repositoryName(jpaRepo.getInterfaceName())
                    .description("Repository has excessive native queries")
                    .recommendation("Consider using JPQL or Criteria API instead of native queries")
                    .build());
            }
        }
    }
    
    private RepositorySummary createRepositorySummary(List<RepositoryComponentInfo> repositories,
                                                     List<JpaRepositoryInfo> jpaRepositories,
                                                     List<RepositoryIssue> issues,
                                                     RepositoryMetrics metrics) {
        
        int totalRepositories = repositories.size() + jpaRepositories.size();
        int totalIssues = issues.size();
        
        // Calculate quality rating
        String qualityRating;
        if (totalIssues == 0) {
            qualityRating = "Excellent";
        } else if (totalIssues <= 2) {
            qualityRating = "Good";
        } else if (totalIssues <= 5) {
            qualityRating = "Fair";
        } else {
            qualityRating = "Poor";
        }
        
        boolean hasGoodDataAccess = totalRepositories > 0 && 
                                   (double) jpaRepositories.size() / totalRepositories >= 0.5;
        
        return RepositorySummary.builder()
            .totalRepositories(totalRepositories)
            .jpaRepositories(jpaRepositories.size())
            .customRepositories(repositories.size())
            .totalIssues(totalIssues)
            .qualityRating(qualityRating)
            .hasGoodDataAccess(hasGoodDataAccess)
            .build();
    }
    
    private RepositoryIssue createAnalysisErrorIssue(String componentName, String errorMessage) {
        return RepositoryIssue.builder()
            .type(RepositoryIssue.IssueType.MISSING_JPA_ANNOTATIONS) // Reuse existing type
            .severity(RepositoryIssue.Severity.ERROR)
            .repositoryName(componentName)
            .description("Analysis error: " + errorMessage)
            .recommendation("Check the repository implementation and dependencies")
            .build();
    }
}