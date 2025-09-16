package it.denzosoft.jreverse.analyzer.uml;

import it.denzosoft.jreverse.core.model.*;
import it.denzosoft.jreverse.core.logging.JReverseLogger;

import java.util.Set;
import java.util.Map;

/**
 * Renders package structure in PlantUML syntax.
 */
public class PlantUMLPackageRenderer {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(PlantUMLPackageRenderer.class);
    
    public String renderPackageStructure(PackageHierarchy hierarchy, Set<ClassInfo> includedClasses) {
        if (hierarchy == null) {
            return "";
        }
        
        StringBuilder packageBuilder = new StringBuilder();
        packageBuilder.append("\n' Package Structure\n");
        
        // Render packages that contain included classes
        Map<String, PackageInfo> packages = hierarchy.getPackages();
        for (PackageInfo packageInfo : packages.values()) {
            boolean hasIncludedClasses = packageInfo.getClasses().stream()
                    .anyMatch(includedClasses::contains);
            
            if (hasIncludedClasses) {
                String packageSection = renderPackage(packageInfo, includedClasses);
                packageBuilder.append(packageSection);
            }
        }
        
        return packageBuilder.toString();
    }
    
    public String renderFullPackageHierarchy(PackageHierarchy hierarchy) {
        if (hierarchy == null) {
            return "";
        }
        
        StringBuilder packageBuilder = new StringBuilder();
        packageBuilder.append("\n' Full Package Hierarchy\n");
        
        Map<String, PackageInfo> packages = hierarchy.getPackages();
        
        // Group packages by depth for better visualization
        for (int depth = 0; depth <= hierarchy.getMaxDepth(); depth++) {
            for (PackageInfo packageInfo : packages.values()) {
                if (hierarchy.getDepth(packageInfo.getName()) == depth) {
                    String packageSection = renderPackageWithHierarchy(packageInfo, hierarchy);
                    packageBuilder.append(packageSection);
                }
            }
        }
        
        return packageBuilder.toString();
    }
    
    private String renderPackage(PackageInfo packageInfo, Set<ClassInfo> includedClasses) {
        StringBuilder packageSection = new StringBuilder();
        String packageName = packageInfo.getName();
        
        if (packageName.isEmpty()) {
            packageName = "default";
        }
        
        packageSection.append("package \"").append(packageName).append("\" {\n");
        
        // Render classes in this package
        for (ClassInfo classInfo : packageInfo.getClasses()) {
            if (includedClasses.contains(classInfo)) {
                String className = classInfo.getSimpleName();
                if (classInfo.isInterface()) {
                    packageSection.append("  interface ").append(className).append("\n");
                } else if (classInfo.isEnum()) {
                    packageSection.append("  enum ").append(className).append("\n");
                } else if (classInfo.isAbstract()) {
                    packageSection.append("  abstract class ").append(className).append("\n");
                } else {
                    packageSection.append("  class ").append(className).append("\n");
                }
            }
        }
        
        packageSection.append("}\n\n");
        return packageSection.toString();
    }
    
    private String renderPackageWithHierarchy(PackageInfo packageInfo, PackageHierarchy hierarchy) {
        StringBuilder packageSection = new StringBuilder();
        String packageName = packageInfo.getName();
        
        if (packageName.isEmpty()) {
            packageName = "default";
        }
        
        int classCount = packageInfo.getClasses().size();
        String packageLabel = packageName + "\\n(" + classCount + " classes)";
        
        packageSection.append("package \"").append(packageLabel).append("\" as ")
                     .append(sanitizePackageName(packageName)).append(" {\n");
        
        // Add package statistics if available
        if (packageInfo.getMetrics() != null) {
            PackageMetrics metrics = packageInfo.getMetrics();
            packageSection.append("  note top : ");
            packageSection.append("Classes: ").append(metrics.getClassCount()).append("\\n");
            packageSection.append("Interfaces: ").append(metrics.getInterfaceCount()).append("\\n");
            packageSection.append("Abstract: ").append(metrics.getAbstractClassCount()).append("\\n");
            packageSection.append("  end note\n");
        }
        
        // Render key classes (limit to avoid clutter)
        int renderedClasses = 0;
        final int MAX_CLASSES_TO_SHOW = 10;
        
        for (ClassInfo classInfo : packageInfo.getClasses()) {
            if (renderedClasses >= MAX_CLASSES_TO_SHOW) {
                packageSection.append("  note bottom : ... and ")
                             .append(classCount - MAX_CLASSES_TO_SHOW)
                             .append(" more classes\n");
                break;
            }
            
            String className = sanitizeClassName(classInfo.getSimpleName());
            
            if (classInfo.isInterface()) {
                packageSection.append("  interface ").append(className).append("\n");
            } else if (classInfo.isEnum()) {
                packageSection.append("  enum ").append(className).append("\n");
            } else if (classInfo.isAbstract()) {
                packageSection.append("  abstract class ").append(className).append("\n");
            } else {
                packageSection.append("  class ").append(className).append("\n");
            }
            
            renderedClasses++;
        }
        
        packageSection.append("}\n\n");
        
        // Add package dependencies
        String dependencies = renderPackageDependencies(packageInfo, hierarchy);
        packageSection.append(dependencies);
        
        return packageSection.toString();
    }
    
    private String renderPackageDependencies(PackageInfo packageInfo, PackageHierarchy hierarchy) {
        StringBuilder dependencies = new StringBuilder();
        
        // This is a simplified version - in full implementation, we would analyze
        // actual dependencies between packages based on class relationships
        String packageName = packageInfo.getName();
        
        // Check if this package depends on other packages based on class imports/usage
        for (ClassInfo classInfo : packageInfo.getClasses()) {
            // Analyze field types, method parameters, etc. to determine dependencies
            // This is simplified - real implementation would be more sophisticated
            for (FieldInfo field : classInfo.getFields()) {
                String fieldType = field.getType();
                String dependentPackage = extractPackageFromType(fieldType);
                
                if (dependentPackage != null && 
                    !dependentPackage.equals(packageName) &&
                    hierarchy.getPackages().containsKey(dependentPackage)) {
                    
                    String sourcePkg = sanitizePackageName(packageName);
                    String targetPkg = sanitizePackageName(dependentPackage);
                    dependencies.append(sourcePkg).append(" ..> ").append(targetPkg).append("\n");
                    break; // Avoid duplicate dependencies
                }
            }
        }
        
        return dependencies.toString();
    }
    
    private String extractPackageFromType(String fullTypeName) {
        if (fullTypeName == null || !fullTypeName.contains(".")) {
            return null;
        }
        
        // Handle generics
        if (fullTypeName.contains("<")) {
            fullTypeName = fullTypeName.substring(0, fullTypeName.indexOf("<"));
        }
        
        // Handle arrays
        if (fullTypeName.contains("[")) {
            fullTypeName = fullTypeName.substring(0, fullTypeName.indexOf("["));
        }
        
        int lastDot = fullTypeName.lastIndexOf(".");
        if (lastDot > 0) {
            return fullTypeName.substring(0, lastDot);
        }
        
        return null;
    }
    
    private String sanitizePackageName(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return "DefaultPackage";
        }
        return packageName.replace(".", "_").replace("-", "_").replace(" ", "_");
    }
    
    private String sanitizeClassName(String className) {
        if (className == null) return "";
        return className.replace("$", "_").replace(" ", "_");
    }
}