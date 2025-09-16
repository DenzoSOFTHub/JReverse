package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Analysis of property hierarchy, precedence, and profile-specific configurations.
 */
public class PropertyHierarchyAnalysis {

    private final Set<String> detectedProfiles;
    private final Map<String, List<String>> profileProperties;
    private final List<String> precedenceOrder;
    private final Map<String, Set<String>> propertyOverrides;

    private PropertyHierarchyAnalysis(Builder builder) {
        this.detectedProfiles = Collections.unmodifiableSet(new HashSet<>(builder.detectedProfiles));
        this.profileProperties = Collections.unmodifiableMap(copyProfileProperties(builder.profileProperties));
        this.precedenceOrder = Collections.unmodifiableList(new ArrayList<>(builder.precedenceOrder));
        this.propertyOverrides = Collections.unmodifiableMap(copyPropertyOverrides(builder.propertyOverrides));
    }

    private Map<String, List<String>> copyProfileProperties(Map<String, List<String>> original) {
        Map<String, List<String>> copy = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : original.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        }
        return copy;
    }

    private Map<String, Set<String>> copyPropertyOverrides(Map<String, Set<String>> original) {
        Map<String, Set<String>> copy = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : original.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableSet(new HashSet<>(entry.getValue())));
        }
        return copy;
    }

    public Set<String> getDetectedProfiles() {
        return detectedProfiles;
    }

    public Map<String, List<String>> getProfileProperties() {
        return profileProperties;
    }

    public List<String> getPrecedenceOrder() {
        return precedenceOrder;
    }

    public Map<String, Set<String>> getPropertyOverrides() {
        return propertyOverrides;
    }

    /**
     * Gets properties specific to a given profile.
     */
    public List<String> getPropertiesForProfile(String profile) {
        return profileProperties.getOrDefault(profile, Collections.emptyList());
    }

    /**
     * Checks if a profile has been detected.
     */
    public boolean hasProfile(String profile) {
        return detectedProfiles.contains(profile);
    }

    /**
     * Gets the number of detected profiles.
     */
    public int getProfileCount() {
        return detectedProfiles.size();
    }

    /**
     * Checks if there are any profile-specific overrides.
     */
    public boolean hasProfileOverrides() {
        return !propertyOverrides.isEmpty();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Set<String> detectedProfiles = new HashSet<>();
        private Map<String, List<String>> profileProperties = new HashMap<>();
        private List<String> precedenceOrder = new ArrayList<>();
        private Map<String, Set<String>> propertyOverrides = new HashMap<>();

        public Builder detectedProfiles(Set<String> profiles) {
            this.detectedProfiles = new HashSet<>(profiles != null ? profiles : Collections.emptySet());
            return this;
        }

        public Builder addProfile(String profile) {
            if (profile != null) {
                this.detectedProfiles.add(profile);
            }
            return this;
        }

        public Builder profileProperties(Map<String, List<String>> profileProperties) {
            this.profileProperties = new HashMap<>();
            if (profileProperties != null) {
                for (Map.Entry<String, List<String>> entry : profileProperties.entrySet()) {
                    this.profileProperties.put(entry.getKey(),
                        new ArrayList<>(entry.getValue() != null ? entry.getValue() : Collections.emptyList()));
                }
            }
            return this;
        }

        public Builder addProfileProperty(String profile, String property) {
            if (profile != null && property != null) {
                this.profileProperties.computeIfAbsent(profile, k -> new ArrayList<>()).add(property);
            }
            return this;
        }

        public Builder precedenceOrder(List<String> precedenceOrder) {
            this.precedenceOrder = new ArrayList<>(precedenceOrder != null ? precedenceOrder : Collections.emptyList());
            return this;
        }

        public Builder addToPrecedenceOrder(String fileName) {
            if (fileName != null) {
                this.precedenceOrder.add(fileName);
            }
            return this;
        }

        public Builder propertyOverrides(Map<String, Set<String>> propertyOverrides) {
            this.propertyOverrides = new HashMap<>();
            if (propertyOverrides != null) {
                for (Map.Entry<String, Set<String>> entry : propertyOverrides.entrySet()) {
                    this.propertyOverrides.put(entry.getKey(),
                        new HashSet<>(entry.getValue() != null ? entry.getValue() : Collections.emptySet()));
                }
            }
            return this;
        }

        public Builder addPropertyOverride(String propertyName, String overridingProfile) {
            if (propertyName != null && overridingProfile != null) {
                this.propertyOverrides.computeIfAbsent(propertyName, k -> new HashSet<>()).add(overridingProfile);
            }
            return this;
        }

        public PropertyHierarchyAnalysis build() {
            return new PropertyHierarchyAnalysis(this);
        }
    }

    @Override
    public String toString() {
        return String.format("PropertyHierarchyAnalysis{profiles=%d, precedenceFiles=%d, overrides=%d}",
            detectedProfiles.size(), precedenceOrder.size(), propertyOverrides.size());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PropertyHierarchyAnalysis that = (PropertyHierarchyAnalysis) obj;
        return Objects.equals(detectedProfiles, that.detectedProfiles) &&
               Objects.equals(profileProperties, that.profileProperties) &&
               Objects.equals(precedenceOrder, that.precedenceOrder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(detectedProfiles, profileProperties, precedenceOrder);
    }
}