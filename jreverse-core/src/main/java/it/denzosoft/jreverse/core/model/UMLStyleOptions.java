package it.denzosoft.jreverse.core.model;

import java.util.Objects;

/**
 * Configuration options for UML diagram styling and appearance.
 */
public final class UMLStyleOptions {
    
    private final String theme;
    private final boolean useColors;
    private final boolean showStereotypes;
    private final boolean showPackageNames;
    private final String fontName;
    private final int fontSize;
    private final boolean compactMode;
    private final boolean highlightSpringComponents;
    private final String backgroundColor;
    private final String classColor;
    private final String interfaceColor;
    
    private UMLStyleOptions(Builder builder) {
        this.theme = builder.theme;
        this.useColors = builder.useColors;
        this.showStereotypes = builder.showStereotypes;
        this.showPackageNames = builder.showPackageNames;
        this.fontName = builder.fontName;
        this.fontSize = builder.fontSize;
        this.compactMode = builder.compactMode;
        this.highlightSpringComponents = builder.highlightSpringComponents;
        this.backgroundColor = builder.backgroundColor;
        this.classColor = builder.classColor;
        this.interfaceColor = builder.interfaceColor;
    }
    
    public static UMLStyleOptions defaultStyle() {
        return builder().build();
    }
    
    public static UMLStyleOptions compactStyle() {
        return builder()
                .compactMode(true)
                .fontSize(10)
                .showPackageNames(false)
                .build();
    }
    
    public static UMLStyleOptions springStyle() {
        return builder()
                .useColors(true)
                .showStereotypes(true)
                .highlightSpringComponents(true)
                .classColor("#E1F5FE")
                .interfaceColor("#F3E5F5")
                .build();
    }
    
    public String getTheme() {
        return theme;
    }
    
    public boolean isUseColors() {
        return useColors;
    }
    
    public boolean isShowStereotypes() {
        return showStereotypes;
    }
    
    public boolean isShowPackageNames() {
        return showPackageNames;
    }
    
    public String getFontName() {
        return fontName;
    }
    
    public int getFontSize() {
        return fontSize;
    }
    
    public boolean isCompactMode() {
        return compactMode;
    }
    
    public boolean isHighlightSpringComponents() {
        return highlightSpringComponents;
    }
    
    public String getBackgroundColor() {
        return backgroundColor;
    }
    
    public String getClassColor() {
        return classColor;
    }
    
    public String getInterfaceColor() {
        return interfaceColor;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String theme = "default";
        private boolean useColors = true;
        private boolean showStereotypes = true;
        private boolean showPackageNames = true;
        private String fontName = "Arial";
        private int fontSize = 12;
        private boolean compactMode = false;
        private boolean highlightSpringComponents = false;
        private String backgroundColor = "white";
        private String classColor = "#FFFACD";
        private String interfaceColor = "#E6E6FA";
        
        public Builder theme(String theme) {
            this.theme = theme;
            return this;
        }
        
        public Builder useColors(boolean useColors) {
            this.useColors = useColors;
            return this;
        }
        
        public Builder showStereotypes(boolean showStereotypes) {
            this.showStereotypes = showStereotypes;
            return this;
        }
        
        public Builder showPackageNames(boolean showPackageNames) {
            this.showPackageNames = showPackageNames;
            return this;
        }
        
        public Builder fontName(String fontName) {
            this.fontName = fontName;
            return this;
        }
        
        public Builder fontSize(int fontSize) {
            this.fontSize = fontSize;
            return this;
        }
        
        public Builder compactMode(boolean compactMode) {
            this.compactMode = compactMode;
            return this;
        }
        
        public Builder highlightSpringComponents(boolean highlightSpringComponents) {
            this.highlightSpringComponents = highlightSpringComponents;
            return this;
        }
        
        public Builder backgroundColor(String backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }
        
        public Builder classColor(String classColor) {
            this.classColor = classColor;
            return this;
        }
        
        public Builder interfaceColor(String interfaceColor) {
            this.interfaceColor = interfaceColor;
            return this;
        }
        
        public UMLStyleOptions build() {
            return new UMLStyleOptions(this);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UMLStyleOptions that = (UMLStyleOptions) obj;
        return useColors == that.useColors &&
               showStereotypes == that.showStereotypes &&
               showPackageNames == that.showPackageNames &&
               fontSize == that.fontSize &&
               compactMode == that.compactMode &&
               highlightSpringComponents == that.highlightSpringComponents &&
               Objects.equals(theme, that.theme) &&
               Objects.equals(fontName, that.fontName) &&
               Objects.equals(backgroundColor, that.backgroundColor) &&
               Objects.equals(classColor, that.classColor) &&
               Objects.equals(interfaceColor, that.interfaceColor);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(theme, useColors, showStereotypes, showPackageNames, 
                          fontName, fontSize, compactMode, highlightSpringComponents,
                          backgroundColor, classColor, interfaceColor);
    }
    
    @Override
    public String toString() {
        return "UMLStyleOptions{" +
                "theme='" + theme + '\'' +
                ", useColors=" + useColors +
                ", compactMode=" + compactMode +
                ", highlightSpring=" + highlightSpringComponents +
                '}';
    }
}