package it.denzosoft.jreverse.analyzer.circulardependency;

import it.denzosoft.jreverse.core.model.CircularDependency;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a circular dependency specific to Spring IoC dependency injection,
 * including Spring-specific characteristics like injection types, @Lazy resolution,
 * and Spring context-aware resolution strategies.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3 - T3.3.1)
 */
public class SpringCircularDependency {

    private List<String> cycle = new ArrayList<>();
    private int cycleLength;
    private CircularDependency.CircularDependencySeverity severity;
    private SpringCircularDependencyType type;
    private SpringCircularDependencyRisk risk;

    // Spring-specific characteristics
    private List<SpringDependencyInfo> cycleInjections = new ArrayList<>();
    private boolean hasLazyResolution = false;
    private List<SpringResolutionStrategy> resolutionStrategies = new ArrayList<>();

    // Analysis metadata
    private long detectionTimeMs;
    private String description;
    private List<String> warnings = new ArrayList<>();

    public SpringCircularDependency() {
    }

    public SpringCircularDependency(List<String> cycle) {
        this.cycle = cycle != null ? new ArrayList<>(cycle) : new ArrayList<>();
        this.cycleLength = this.cycle.size() > 1 ? this.cycle.size() - 1 : 0; // Exclude duplicate end node
    }

    // Getters and Setters
    public List<String> getCycle() {
        return cycle;
    }

    public void setCycle(List<String> cycle) {
        this.cycle = cycle != null ? new ArrayList<>(cycle) : new ArrayList<>();
        this.cycleLength = this.cycle.size() > 1 ? this.cycle.size() - 1 : 0;
    }

    public int getCycleLength() {
        return cycleLength;
    }

    public void setCycleLength(int cycleLength) {
        this.cycleLength = cycleLength;
    }

    public CircularDependency.CircularDependencySeverity getSeverity() {
        return severity;
    }

    public void setSeverity(CircularDependency.CircularDependencySeverity severity) {
        this.severity = severity;
    }

    public SpringCircularDependencyType getType() {
        return type;
    }

    public void setType(SpringCircularDependencyType type) {
        this.type = type;
    }

    public SpringCircularDependencyRisk getRisk() {
        return risk;
    }

    public void setRisk(SpringCircularDependencyRisk risk) {
        this.risk = risk;
    }

    public List<SpringDependencyInfo> getCycleInjections() {
        return cycleInjections;
    }

    public void setCycleInjections(List<SpringDependencyInfo> cycleInjections) {
        this.cycleInjections = cycleInjections != null ? new ArrayList<>(cycleInjections) : new ArrayList<>();
    }

    public void addCycleInjection(SpringDependencyInfo injection) {
        if (injection != null) {
            this.cycleInjections.add(injection);
        }
    }

    public boolean isHasLazyResolution() {
        return hasLazyResolution;
    }

    public void setHasLazyResolution(boolean hasLazyResolution) {
        this.hasLazyResolution = hasLazyResolution;
    }

    public List<SpringResolutionStrategy> getResolutionStrategies() {
        return resolutionStrategies;
    }

    public void setResolutionStrategies(List<SpringResolutionStrategy> resolutionStrategies) {
        this.resolutionStrategies = resolutionStrategies != null ? new ArrayList<>(resolutionStrategies) : new ArrayList<>();
    }

    public void addResolutionStrategy(SpringResolutionStrategy strategy) {
        if (strategy != null) {
            this.resolutionStrategies.add(strategy);
        }
    }

    public long getDetectionTimeMs() {
        return detectionTimeMs;
    }

    public void setDetectionTimeMs(long detectionTimeMs) {
        this.detectionTimeMs = detectionTimeMs;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings != null ? new ArrayList<>(warnings) : new ArrayList<>();
    }

    public void addWarning(String warning) {
        if (warning != null && !warning.trim().isEmpty()) {
            this.warnings.add(warning.trim());
        }
    }

    // Utility methods

    /**
     * Gets a human-readable description of the circular dependency.
     */
    public String getCycleDescription() {
        if (cycle.isEmpty()) {
            return "Empty cycle";
        }

        List<String> simpleNames = cycle.stream()
            .map(this::getSimpleClassName)
            .collect(Collectors.toList());

        return String.join(" -> ", simpleNames);
    }

    /**
     * Gets the classes involved in the cycle (excluding the duplicate end node).
     */
    public List<String> getInvolvedClasses() {
        if (cycle.size() <= 1) {
            return new ArrayList<>(cycle);
        }
        // Remove the last element if it's the same as the first (closing the cycle)
        List<String> involved = new ArrayList<>(cycle);
        if (involved.size() > 1 && involved.get(0).equals(involved.get(involved.size() - 1))) {
            involved.remove(involved.size() - 1);
        }
        return involved;
    }

    /**
     * Checks if the cycle involves a specific class.
     */
    public boolean involvesClass(String className) {
        return getInvolvedClasses().contains(className);
    }

    /**
     * Gets the number of unique classes in the cycle.
     */
    public int getUniqueClassCount() {
        return getInvolvedClasses().size();
    }

    /**
     * Checks if this is a self-referencing cycle (single class).
     */
    public boolean isSelfReferencing() {
        List<String> involved = getInvolvedClasses();
        return involved.size() == 1;
    }

    /**
     * Checks if this cycle is considered complex (more than 4 classes).
     */
    public boolean isComplexCycle() {
        return getUniqueClassCount() > 4;
    }

    /**
     * Gets the primary resolution strategy (first one with lowest complexity).
     */
    public SpringResolutionStrategy getPrimaryResolutionStrategy() {
        return resolutionStrategies.stream()
            .min((s1, s2) -> s1.getComplexity().compareTo(s2.getComplexity()))
            .orElse(null);
    }

    /**
     * Checks if this cycle has any critical injection types.
     */
    public boolean hasCriticalInjectionTypes() {
        return cycleInjections.stream()
            .anyMatch(injection -> injection.getInjectionType().isFieldBased());
    }

    /**
     * Gets the injection types present in this cycle.
     */
    public List<String> getInjectionTypes() {
        return cycleInjections.stream()
            .map(injection -> injection.getInjectionType().getDescription())
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * Gets a summary of this circular dependency.
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Circular dependency involving %d classes", getUniqueClassCount()));

        if (severity != null) {
            summary.append(String.format(" [%s severity]", severity.name()));
        }

        if (type != null) {
            summary.append(String.format(" [%s]", type.getDisplayName()));
        }

        if (hasLazyResolution) {
            summary.append(" [Resolved with @Lazy]");
        }

        return summary.toString();
    }

    /**
     * Converts this Spring circular dependency to a generic CircularDependency.
     */
    public CircularDependency toCircularDependency() {
        // Create DependencyNodes from class names
        List<it.denzosoft.jreverse.core.model.DependencyNode> nodes = getInvolvedClasses().stream()
            .map(className -> it.denzosoft.jreverse.core.model.DependencyNode.builder()
                .identifier(className)
                .displayName(getSimpleClassName(className))
                .type(it.denzosoft.jreverse.core.model.DependencyNodeType.SPRING_COMPONENT)
                .build())
            .collect(Collectors.toList());

        // Generate suggestions based on resolution strategies
        List<String> suggestions = resolutionStrategies.stream()
            .map(SpringResolutionStrategy::getDescription)
            .collect(Collectors.toList());

        return CircularDependency.builder()
            .cyclePath(nodes)
            .severity(severity != null ? severity : CircularDependency.CircularDependencySeverity.MEDIUM)
            .description(description != null ? description : getCycleDescription())
            .suggestions(suggestions)
            .build();
    }

    private String getSimpleClassName(String fullyQualifiedName) {
        if (fullyQualifiedName == null) return "";
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        return lastDot != -1 ? fullyQualifiedName.substring(lastDot + 1) : fullyQualifiedName;
    }

    @Override
    public String toString() {
        return String.format("SpringCircularDependency{classes=%d, severity=%s, type=%s, resolved=%s}",
            getUniqueClassCount(), severity, type, hasLazyResolution);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SpringCircularDependency that = (SpringCircularDependency) obj;
        return Objects.equals(getInvolvedClasses(), that.getInvolvedClasses());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInvolvedClasses());
    }
}