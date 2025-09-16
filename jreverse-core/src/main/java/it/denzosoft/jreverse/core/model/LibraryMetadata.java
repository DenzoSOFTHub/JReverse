package it.denzosoft.jreverse.core.model;

import java.util.Collections;
import java.util.Map;

/**
 * Additional metadata for external libraries including checksums,
 * repository information, and custom attributes.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public class LibraryMetadata {

    private final String checksum;
    private final String repository;
    private final String description;
    private final String homepage;
    private final long fileSize;
    private final Map<String, String> customAttributes;

    private LibraryMetadata(Builder builder) {
        this.checksum = builder.checksum;
        this.repository = builder.repository;
        this.description = builder.description;
        this.homepage = builder.homepage;
        this.fileSize = builder.fileSize;
        this.customAttributes = Collections.unmodifiableMap(builder.customAttributes);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static LibraryMetadata empty() {
        return builder().build();
    }

    // Getters
    public String getChecksum() { return checksum; }
    public String getRepository() { return repository; }
    public String getDescription() { return description; }
    public String getHomepage() { return homepage; }
    public long getFileSize() { return fileSize; }
    public Map<String, String> getCustomAttributes() { return customAttributes; }

    // Utility methods
    public boolean hasChecksum() {
        return checksum != null && !checksum.trim().isEmpty();
    }

    public boolean hasRepository() {
        return repository != null && !repository.trim().isEmpty();
    }

    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }

    public String getCustomAttribute(String key) {
        return customAttributes.get(key);
    }

    public boolean hasCustomAttribute(String key) {
        return customAttributes.containsKey(key);
    }

    public static class Builder {
        private String checksum;
        private String repository;
        private String description;
        private String homepage;
        private long fileSize = 0L;
        private Map<String, String> customAttributes = Collections.emptyMap();

        public Builder checksum(String checksum) {
            this.checksum = checksum;
            return this;
        }

        public Builder repository(String repository) {
            this.repository = repository;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder homepage(String homepage) {
            this.homepage = homepage;
            return this;
        }

        public Builder fileSize(long fileSize) {
            this.fileSize = Math.max(0L, fileSize);
            return this;
        }

        public Builder customAttributes(Map<String, String> customAttributes) {
            this.customAttributes = customAttributes != null ?
                Map.copyOf(customAttributes) : Collections.emptyMap();
            return this;
        }

        public LibraryMetadata build() {
            return new LibraryMetadata(this);
        }
    }
}