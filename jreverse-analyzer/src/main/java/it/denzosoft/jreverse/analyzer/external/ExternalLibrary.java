package it.denzosoft.jreverse.analyzer.external;

import java.util.Set;

/**
 * Legacy external library class for test compatibility.
 * This is an alias for ExternalLibraryInfo to maintain test compatibility.
 */
public class ExternalLibrary {

    private final String name;
    private final String packageName;
    private final String version;
    private final int classCount;
    private final Set<String> licenses;
    private final String description;

    public ExternalLibrary(String name, String packageName, String version,
                          int classCount, Set<String> licenses, String description) {
        this.name = name;
        this.packageName = packageName;
        this.version = version;
        this.classCount = classCount;
        this.licenses = licenses != null ? Set.copyOf(licenses) : Set.of();
        this.description = description;
    }

    // Convert from ExternalLibraryInfo
    public static ExternalLibrary fromExternalLibraryInfo(ExternalLibraryInfo info) {
        return new ExternalLibrary(
            info.getName(),
            info.getPackageName(),
            info.getVersion(),
            info.getClassCount(),
            info.getLicenses(),
            info.getDescription()
        );
    }

    // Getters
    public String getName() { return name; }
    public String getPackageName() { return packageName; }
    public String getVersion() { return version; }
    public int getClassCount() { return classCount; }
    public Set<String> getLicenses() { return licenses; }
    public String getDescription() { return description; }
}