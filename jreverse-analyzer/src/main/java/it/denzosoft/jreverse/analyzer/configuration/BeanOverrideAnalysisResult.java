package it.denzosoft.jreverse.analyzer.configuration;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Result of bean override analysis containing detected overrides and conflicts.
 */
public class BeanOverrideAnalysisResult {

    private final List<BeanOverride> nameOverrides;
    private final List<BeanOverride> typeOverrides;
    private final List<BeanConflict> conflicts;

    public BeanOverrideAnalysisResult(List<BeanOverride> nameOverrides,
                                    List<BeanOverride> typeOverrides,
                                    List<BeanConflict> conflicts) {
        this.nameOverrides = Collections.unmodifiableList(nameOverrides);
        this.typeOverrides = Collections.unmodifiableList(typeOverrides);
        this.conflicts = Collections.unmodifiableList(conflicts);
    }

    public List<BeanOverride> getNameOverrides() {
        return nameOverrides;
    }

    public List<BeanOverride> getTypeOverrides() {
        return typeOverrides;
    }

    public List<BeanConflict> getConflicts() {
        return conflicts;
    }

    public boolean hasOverrides() {
        return !nameOverrides.isEmpty() || !typeOverrides.isEmpty();
    }

    public boolean hasConflicts() {
        return !conflicts.isEmpty();
    }

    public int getTotalOverrideCount() {
        return nameOverrides.size() + typeOverrides.size();
    }

    public int getTotalConflictCount() {
        return conflicts.size();
    }

    /**
     * Gets overrides that are potentially problematic.
     */
    public List<BeanOverride> getProblematicOverrides() {
        List<BeanOverride> problematic = nameOverrides.stream()
            .filter(BeanOverride::isPotentialProblem)
            .collect(Collectors.toList());

        problematic.addAll(typeOverrides.stream()
            .filter(BeanOverride::isPotentialProblem)
            .collect(Collectors.toList()));

        return problematic;
    }

    /**
     * Gets conflicts by severity.
     */
    public List<BeanConflict> getConflictsBySeverity(BeanConflict.Severity severity) {
        return conflicts.stream()
            .filter(conflict -> conflict.getSeverity() == severity)
            .collect(Collectors.toList());
    }

    /**
     * Gets high-severity conflicts that likely cause runtime issues.
     */
    public List<BeanConflict> getHighSeverityConflicts() {
        return getConflictsBySeverity(BeanConflict.Severity.HIGH);
    }

    /**
     * Gets a summary of the analysis results.
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Bean Override Analysis Summary:\n");
        summary.append("  Name Overrides: ").append(nameOverrides.size()).append("\n");
        summary.append("  Type Overrides: ").append(typeOverrides.size()).append("\n");
        summary.append("  Conflicts: ").append(conflicts.size());

        if (hasConflicts()) {
            summary.append(" (");
            summary.append(getHighSeverityConflicts().size()).append(" high, ");
            summary.append(getConflictsBySeverity(BeanConflict.Severity.MEDIUM).size()).append(" medium, ");
            summary.append(getConflictsBySeverity(BeanConflict.Severity.LOW).size()).append(" low");
            summary.append(")");
        }

        int problematicOverrides = getProblematicOverrides().size();
        if (problematicOverrides > 0) {
            summary.append("\n  Problematic Overrides: ").append(problematicOverrides);
        }

        return summary.toString();
    }

    @Override
    public String toString() {
        return "BeanOverrideAnalysisResult{" +
                "nameOverrides=" + nameOverrides.size() +
                ", typeOverrides=" + typeOverrides.size() +
                ", conflicts=" + conflicts.size() +
                '}';
    }
}