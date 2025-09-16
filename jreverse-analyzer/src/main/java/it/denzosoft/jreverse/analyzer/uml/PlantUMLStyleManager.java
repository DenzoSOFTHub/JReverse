package it.denzosoft.jreverse.analyzer.uml;

import it.denzosoft.jreverse.core.model.*;
import it.denzosoft.jreverse.core.logging.JReverseLogger;

import java.util.List;

/**
 * Manages PlantUML styling and theme configuration.
 */
public class PlantUMLStyleManager {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(PlantUMLStyleManager.class);
    
    public String generateStyleDirectives(UMLStyleOptions options) {
        if (options == null) {
            return generateDefaultStyles();
        }
        
        StringBuilder styleBuilder = new StringBuilder();
        styleBuilder.append("\n' Style Configuration\n");
        
        // Skin parameters
        styleBuilder.append("!theme ").append(options.getTheme()).append("\n");
        styleBuilder.append("skinparam backgroundColor ").append(options.getBackgroundColor()).append("\n");
        styleBuilder.append("skinparam defaultFontName ").append(options.getFontName()).append("\n");
        styleBuilder.append("skinparam defaultFontSize ").append(options.getFontSize()).append("\n");
        
        // Class styling
        if (options.isUseColors()) {
            styleBuilder.append("skinparam class {\n");
            styleBuilder.append("  BackgroundColor ").append(options.getClassColor()).append("\n");
            styleBuilder.append("  BorderColor Black\n");
            styleBuilder.append("}\n");
            
            styleBuilder.append("skinparam interface {\n");
            styleBuilder.append("  BackgroundColor ").append(options.getInterfaceColor()).append("\n");
            styleBuilder.append("  BorderColor Black\n");
            styleBuilder.append("}\n");
        }
        
        // Spring-specific styling
        if (options.isHighlightSpringComponents()) {
            styleBuilder.append(generateSpringComponentStyles());
        }
        
        // Compact mode adjustments
        if (options.isCompactMode()) {
            styleBuilder.append("skinparam minClassWidth 50\n");
            styleBuilder.append("skinparam nodesep 10\n");
            styleBuilder.append("skinparam ranksep 20\n");
        }
        
        // Package styling
        if (options.isShowPackageNames()) {
            styleBuilder.append("skinparam package {\n");
            styleBuilder.append("  BackgroundColor #FFFFCC\n");
            styleBuilder.append("  BorderColor #CC9900\n");
            styleBuilder.append("  FontStyle bold\n");
            styleBuilder.append("}\n");
        }
        
        return styleBuilder.toString();
    }
    
    public String generatePatternStyles(List<DetectedDesignPattern> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return "";
        }
        
        StringBuilder patternStyles = new StringBuilder();
        patternStyles.append("\n' Pattern-specific Styles\n");
        
        for (DetectedDesignPattern pattern : patterns) {
            String patternStyle = getPatternSpecificStyle(pattern.getPatternType());
            patternStyles.append(patternStyle);
        }
        
        return patternStyles.toString();
    }
    
    public String getPatternSpecificStyle(DesignPatternType patternType) {
        switch (patternType) {
            case SINGLETON:
                return generateSingletonStyle();
            case FACTORY_METHOD:
            case ABSTRACT_FACTORY:
                return generateFactoryStyle();
            case OBSERVER:
                return generateObserverStyle();
            case STRATEGY:
                return generateStrategyStyle();
            case DECORATOR:
                return generateDecoratorStyle();
            case ADAPTER:
                return generateAdapterStyle();
            case REPOSITORY:
                return generateRepositoryStyle();
            case SERVICE_LAYER:
                return generateServiceStyle();
            case MVC:
                return generateMvcStyle();
            default:
                return "";
        }
    }
    
    private String generateDefaultStyles() {
        StringBuilder defaultStyles = new StringBuilder();
        defaultStyles.append("\n' Default Styles\n");
        defaultStyles.append("skinparam backgroundColor white\n");
        defaultStyles.append("skinparam defaultFontName Arial\n");
        defaultStyles.append("skinparam defaultFontSize 12\n");
        defaultStyles.append("skinparam class {\n");
        defaultStyles.append("  BackgroundColor #FFFACD\n");
        defaultStyles.append("  BorderColor Black\n");
        defaultStyles.append("}\n");
        defaultStyles.append("skinparam interface {\n");
        defaultStyles.append("  BackgroundColor #E6E6FA\n");
        defaultStyles.append("  BorderColor Black\n");
        defaultStyles.append("}\n");
        return defaultStyles.toString();
    }
    
    private String generateSpringComponentStyles() {
        StringBuilder springStyles = new StringBuilder();
        springStyles.append("\n' Spring Component Styles\n");
        
        // Service components
        springStyles.append("skinparam class {\n");
        springStyles.append("  BackgroundColor<<Service>> #E8F5E8\n");
        springStyles.append("  BorderColor<<Service>> #228B22\n");
        springStyles.append("}\n");
        
        // Repository components
        springStyles.append("skinparam class {\n");
        springStyles.append("  BackgroundColor<<Repository>> #F0E68C\n");
        springStyles.append("  BorderColor<<Repository>> #DAA520\n");
        springStyles.append("}\n");
        
        // Controller components
        springStyles.append("skinparam class {\n");
        springStyles.append("  BackgroundColor<<Controller>> #FFE4E1\n");
        springStyles.append("  BorderColor<<Controller>> #DC143C\n");
        springStyles.append("}\n");
        
        // Configuration components
        springStyles.append("skinparam class {\n");
        springStyles.append("  BackgroundColor<<Configuration>> #E0E6FF\n");
        springStyles.append("  BorderColor<<Configuration>> #4169E1\n");
        springStyles.append("}\n");
        
        // Entity components
        springStyles.append("skinparam class {\n");
        springStyles.append("  BackgroundColor<<Entity>> #FFEFD5\n");
        springStyles.append("  BorderColor<<Entity>> #DEB887\n");
        springStyles.append("}\n");
        
        return springStyles.toString();
    }
    
    private String generateSingletonStyle() {
        return "skinparam class {\n" +
               "  BackgroundColor<<Singleton>> #FFB6C1\n" +
               "  BorderColor<<Singleton>> #DC143C\n" +
               "  FontStyle<<Singleton>> bold\n" +
               "}\n";
    }
    
    private String generateFactoryStyle() {
        return "skinparam class {\n" +
               "  BackgroundColor<<Factory>> #98FB98\n" +
               "  BorderColor<<Factory>> #228B22\n" +
               "  FontStyle<<Factory>> italic\n" +
               "}\n";
    }
    
    private String generateObserverStyle() {
        return "skinparam class {\n" +
               "  BackgroundColor<<Observer>> #87CEEB\n" +
               "  BorderColor<<Observer>> #4682B4\n" +
               "}\n";
    }
    
    private String generateStrategyStyle() {
        return "skinparam class {\n" +
               "  BackgroundColor<<Strategy>> #DDA0DD\n" +
               "  BorderColor<<Strategy>> #8B008B\n" +
               "}\n";
    }
    
    private String generateDecoratorStyle() {
        return "skinparam class {\n" +
               "  BackgroundColor<<Decorator>> #F0E68C\n" +
               "  BorderColor<<Decorator>> #DAA520\n" +
               "}\n";
    }
    
    private String generateAdapterStyle() {
        return "skinparam class {\n" +
               "  BackgroundColor<<Adapter>> #FFA07A\n" +
               "  BorderColor<<Adapter>> #FF4500\n" +
               "}\n";
    }
    
    private String generateRepositoryStyle() {
        return "skinparam class {\n" +
               "  BackgroundColor<<Repository>> #FFFACD\n" +
               "  BorderColor<<Repository>> #B8860B\n" +
               "  FontStyle<<Repository>> bold\n" +
               "}\n";
    }
    
    private String generateServiceStyle() {
        return "skinparam class {\n" +
               "  BackgroundColor<<Service>> #E0FFE0\n" +
               "  BorderColor<<Service>> #32CD32\n" +
               "  FontStyle<<Service>> bold\n" +
               "}\n";
    }
    
    private String generateMvcStyle() {
        return "skinparam class {\n" +
               "  BackgroundColor<<Controller>> #FFE4E1\n" +
               "  BorderColor<<Controller>> #DC143C\n" +
               "  FontStyle<<Controller>> bold\n" +
               "}\n";
    }
}