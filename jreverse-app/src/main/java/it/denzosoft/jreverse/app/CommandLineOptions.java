package it.denzosoft.jreverse.app;

import it.denzosoft.jreverse.core.logging.LogLevel;

/**
 * Immutable value object representing command line options for JReverse application.
 * Uses builder pattern for convenient construction.
 */
public final class CommandLineOptions {
    
    private final LogLevel logLevel;
    private final boolean fileLogging;
    private final boolean guiMode;
    
    private CommandLineOptions(Builder builder) {
        this.logLevel = builder.logLevel != null ? builder.logLevel : LogLevel.INFO;
        this.fileLogging = builder.fileLogging;
        this.guiMode = builder.guiMode;
    }
    
    public LogLevel getLogLevel() {
        return logLevel;
    }
    
    public boolean isFileLogging() {
        return fileLogging;
    }
    
    public boolean isGuiMode() {
        return guiMode;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @Override
    public String toString() {
        return "CommandLineOptions{" +
               "logLevel=" + logLevel +
               ", fileLogging=" + fileLogging +
               ", guiMode=" + guiMode +
               '}';
    }
    
    public static class Builder {
        private LogLevel logLevel = LogLevel.INFO;
        private boolean fileLogging = false;
        private boolean guiMode = true; // Default to GUI mode
        
        public Builder logLevel(LogLevel logLevel) {
            this.logLevel = logLevel;
            return this;
        }
        
        public Builder fileLogging(boolean fileLogging) {
            this.fileLogging = fileLogging;
            return this;
        }
        
        public Builder guiMode(boolean guiMode) {
            this.guiMode = guiMode;
            return this;
        }
        
        public CommandLineOptions build() {
            return new CommandLineOptions(this);
        }
    }
}