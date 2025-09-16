package it.denzosoft.jreverse.core.model;

import java.util.*;

/**
 * Represents the parsed content of a property file (application.properties or application.yml).
 */
public class PropertyFileContent {

    private final String fileName;
    private final Map<String, String> properties;
    private final Map<Integer, String> comments;
    private final PropertyFileType fileType;

    private PropertyFileContent(Builder builder) {
        this.fileName = builder.fileName;
        this.properties = Collections.unmodifiableMap(new HashMap<>(builder.properties));
        this.comments = Collections.unmodifiableMap(new HashMap<>(builder.comments));
        this.fileType = builder.fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public Map<Integer, String> getComments() {
        return comments;
    }

    public PropertyFileType getFileType() {
        return fileType;
    }

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public int getPropertyCount() {
        return properties.size();
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum PropertyFileType {
        PROPERTIES("properties", "Java Properties format"),
        YAML("yaml", "YAML format"),
        YML("yml", "YAML format (yml extension)");

        private final String extension;
        private final String description;

        PropertyFileType(String extension, String description) {
            this.extension = extension;
            this.description = description;
        }

        public String getExtension() {
            return extension;
        }

        public String getDescription() {
            return description;
        }

        public static PropertyFileType fromFileName(String fileName) {
            if (fileName.endsWith(".properties")) return PROPERTIES;
            if (fileName.endsWith(".yml")) return YML;
            if (fileName.endsWith(".yaml")) return YAML;
            return PROPERTIES;
        }
    }

    public static class Builder {
        private String fileName;
        private Map<String, String> properties = new HashMap<>();
        private Map<Integer, String> comments = new HashMap<>();
        private PropertyFileType fileType = PropertyFileType.PROPERTIES;

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            if (fileName != null) {
                this.fileType = PropertyFileType.fromFileName(fileName);
            }
            return this;
        }

        public Builder addProperty(String key, String value) {
            if (key != null) {
                this.properties.put(key, value != null ? value : "");
            }
            return this;
        }

        public Builder properties(Map<String, String> properties) {
            this.properties = new HashMap<>(properties != null ? properties : Collections.emptyMap());
            return this;
        }

        public Builder addComment(int lineNumber, String comment) {
            if (comment != null) {
                this.comments.put(lineNumber, comment);
            }
            return this;
        }

        public Builder comments(Map<Integer, String> comments) {
            this.comments = new HashMap<>(comments != null ? comments : Collections.emptyMap());
            return this;
        }

        public Builder fileType(PropertyFileType fileType) {
            this.fileType = fileType != null ? fileType : PropertyFileType.PROPERTIES;
            return this;
        }

        public PropertyFileContent build() {
            Objects.requireNonNull(fileName, "File name is required");
            return new PropertyFileContent(this);
        }
    }

    @Override
    public String toString() {
        return String.format("PropertyFileContent{fileName='%s', propertyCount=%d, fileType=%s}",
            fileName, properties.size(), fileType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PropertyFileContent that = (PropertyFileContent) obj;
        return Objects.equals(fileName, that.fileName) &&
               Objects.equals(properties, that.properties) &&
               Objects.equals(fileType, that.fileType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, properties, fileType);
    }
}