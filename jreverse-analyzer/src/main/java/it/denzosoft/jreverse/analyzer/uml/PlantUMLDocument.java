package it.denzosoft.jreverse.analyzer.uml;

import it.denzosoft.jreverse.core.model.UMLMetadata;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete PlantUML document with all sections.
 */
public final class PlantUMLDocument {
    
    private final String title;
    private final List<String> sections;
    private final UMLMetadata metadata;
    
    private PlantUMLDocument(Builder builder) {
        this.title = builder.title;
        this.sections = new ArrayList<>(builder.sections);
        this.metadata = builder.metadata;
    }
    
    public String getContent() {
        StringBuilder content = new StringBuilder();
        
        // Add title if present
        if (title != null && !title.trim().isEmpty()) {
            content.append("title ").append(title).append("\n\n");
        }
        
        // Add all sections
        for (String section : sections) {
            if (section != null && !section.trim().isEmpty()) {
                content.append(section);
                if (!section.endsWith("\n")) {
                    content.append("\n");
                }
            }
        }
        
        return content.toString();
    }
    
    public String getTitle() {
        return title;
    }
    
    public List<String> getSections() {
        return new ArrayList<>(sections);
    }
    
    public UMLMetadata getMetadata() {
        return metadata;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String title;
        private List<String> sections = new ArrayList<>();
        private UMLMetadata metadata;
        
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder addHeader(String header) {
            this.sections.add(0, header + "\n");
            return this;
        }
        
        public Builder addSection(String section) {
            if (section != null && !section.trim().isEmpty()) {
                this.sections.add(section);
            }
            return this;
        }
        
        public Builder addFooter(String footer) {
            this.sections.add(footer + "\n");
            return this;
        }
        
        public Builder metadata(UMLMetadata metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public PlantUMLDocument build() {
            return new PlantUMLDocument(this);
        }
    }
    
    @Override
    public String toString() {
        return "PlantUMLDocument{" +
                "title='" + title + '\'' +
                ", sections=" + sections.size() +
                ", length=" + getContent().length() +
                '}';
    }
}