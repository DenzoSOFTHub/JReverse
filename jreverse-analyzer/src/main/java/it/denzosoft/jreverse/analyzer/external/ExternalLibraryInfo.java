package it.denzosoft.jreverse.analyzer.external;

import java.util.Set;

/**
 * Information about an external library dependency.
 */
public class ExternalLibraryInfo {

    private final String name;
    private final String packageName;
    private final String version;
    private final int classCount;
    private final Set<String> licenses;
    private final String description;

    private ExternalLibraryInfo(String name, String packageName, String version,
                               int classCount, Set<String> licenses, String description) {
        this.name = name;
        this.packageName = packageName;
        this.version = version;
        this.classCount = classCount;
        this.licenses = licenses != null ? Set.copyOf(licenses) : Set.of();
        this.description = description;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getName() { return name; }
    public String getPackageName() { return packageName; }
    public String getVersion() { return version; }
    public int getClassCount() { return classCount; }
    public Set<String> getLicenses() { return licenses; }
    public String getDescription() { return description; }

    public static class Builder {
        private String name;
        private String packageName;
        private String version;
        private int classCount = 0;
        private Set<String> licenses = Set.of();
        private String description;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder classCount(int classCount) {
            this.classCount = classCount;
            return this;
        }

        public Builder licenses(Set<String> licenses) {
            this.licenses = licenses;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public ExternalLibraryInfo build() {
            return new ExternalLibraryInfo(name, packageName, version, classCount, licenses, description);
        }
    }
}