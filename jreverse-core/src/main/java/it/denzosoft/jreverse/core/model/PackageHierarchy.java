package it.denzosoft.jreverse.core.model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the hierarchical structure of packages in the analyzed JAR,
 * including parent-child relationships and organizational metrics.
 */
public final class PackageHierarchy {
    
    private final Map<String, PackageInfo> packages;
    private final PackageInfo rootPackage;
    private final Map<String, List<String>> parentChildRelations;
    private final int maxDepth;
    private final double averageDepth;
    
    private PackageHierarchy(Builder builder) {
        this.packages = Collections.unmodifiableMap(new HashMap<>(builder.packages));
        this.rootPackage = builder.rootPackage;
        this.parentChildRelations = Collections.unmodifiableMap(builder.parentChildRelations);
        this.maxDepth = builder.maxDepth;
        this.averageDepth = builder.averageDepth;
    }
    
    public Map<String, PackageInfo> getPackages() {
        return packages;
    }
    
    public PackageInfo getRootPackage() {
        return rootPackage;
    }
    
    public Map<String, List<String>> getParentChildRelations() {
        return parentChildRelations;
    }
    
    public int getMaxDepth() {
        return maxDepth;
    }
    
    public double getAverageDepth() {
        return averageDepth;
    }
    
    public int getPackageCount() {
        return packages.size();
    }
    
    public Set<String> getRootPackageNames() {
        return packages.values().stream()
                .filter(PackageInfo::isRootLevel)
                .map(PackageInfo::getName)
                .collect(Collectors.toSet());
    }
    
    public List<PackageInfo> getLeafPackages() {
        return packages.values().stream()
                .filter(pkg -> !parentChildRelations.containsKey(pkg.getName()))
                .collect(Collectors.toList());
    }
    
    public PackageInfo getPackage(String packageName) {
        return packages.get(packageName);
    }
    
    public List<String> getChildPackages(String packageName) {
        return parentChildRelations.getOrDefault(packageName, Collections.emptyList());
    }
    
    public String getParentPackage(String packageName) {
        int lastDot = packageName.lastIndexOf('.');
        return lastDot > 0 ? packageName.substring(0, lastDot) : null;
    }
    
    public int getDepth(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return 0;
        }
        return (int) packageName.chars().filter(ch -> ch == '.').count() + 1;
    }
    
    public List<PackageInfo> getPackagesAtDepth(int depth) {
        return packages.values().stream()
                .filter(pkg -> getDepth(pkg.getName()) == depth)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PackageHierarchy that = (PackageHierarchy) obj;
        return maxDepth == that.maxDepth &&
               Double.compare(that.averageDepth, averageDepth) == 0 &&
               Objects.equals(packages, that.packages) &&
               Objects.equals(rootPackage, that.rootPackage) &&
               Objects.equals(parentChildRelations, that.parentChildRelations);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(packages, rootPackage, parentChildRelations, maxDepth, averageDepth);
    }
    
    @Override
    public String toString() {
        return "PackageHierarchy{" +
                "packageCount=" + packages.size() +
                ", maxDepth=" + maxDepth +
                ", averageDepth=" + String.format("%.2f", averageDepth) +
                ", rootPackages=" + getRootPackageNames().size() +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Map<String, PackageInfo> packages = new HashMap<>();
        private PackageInfo rootPackage;
        private Map<String, List<String>> parentChildRelations = new HashMap<>();
        private int maxDepth = 0;
        private double averageDepth = 0.0;
        
        public Builder addPackage(PackageInfo packageInfo) {
            if (packageInfo != null) {
                packages.put(packageInfo.getName(), packageInfo);
                updateDepthMetrics(packageInfo.getName());
                updateParentChildRelations(packageInfo);
            }
            return this;
        }
        
        public Builder packages(Map<String, PackageInfo> packages) {
            this.packages = new HashMap<>(packages != null ? packages : Collections.emptyMap());
            recalculateMetrics();
            return this;
        }
        
        public Builder rootPackage(PackageInfo rootPackage) {
            this.rootPackage = rootPackage;
            return this;
        }
        
        public Builder maxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }
        
        public Builder averageDepth(double averageDepth) {
            this.averageDepth = averageDepth;
            return this;
        }
        
        private void updateDepthMetrics(String packageName) {
            int depth = (int) packageName.chars().filter(ch -> ch == '.').count() + 1;
            maxDepth = Math.max(maxDepth, depth);
            
            // Recalculate average depth
            if (!packages.isEmpty()) {
                averageDepth = packages.keySet().stream()
                        .mapToInt(name -> (int) name.chars().filter(ch -> ch == '.').count() + 1)
                        .average()
                        .orElse(0.0);
            }
        }
        
        private void updateParentChildRelations(PackageInfo packageInfo) {
            String packageName = packageInfo.getName();
            String parentName = getParentPackageName(packageName);
            
            if (parentName != null) {
                parentChildRelations.computeIfAbsent(parentName, k -> new ArrayList<>())
                        .add(packageName);
            }
        }
        
        private String getParentPackageName(String packageName) {
            int lastDot = packageName.lastIndexOf('.');
            return lastDot > 0 ? packageName.substring(0, lastDot) : null;
        }
        
        private void recalculateMetrics() {
            // Recalculate parent-child relations
            parentChildRelations.clear();
            for (PackageInfo pkg : packages.values()) {
                updateParentChildRelations(pkg);
            }
            
            // Recalculate depth metrics
            maxDepth = packages.keySet().stream()
                    .mapToInt(name -> (int) name.chars().filter(ch -> ch == '.').count() + 1)
                    .max()
                    .orElse(0);
            
            averageDepth = packages.keySet().stream()
                    .mapToInt(name -> (int) name.chars().filter(ch -> ch == '.').count() + 1)
                    .average()
                    .orElse(0.0);
        }
        
        public PackageHierarchy build() {
            return new PackageHierarchy(this);
        }
    }
}