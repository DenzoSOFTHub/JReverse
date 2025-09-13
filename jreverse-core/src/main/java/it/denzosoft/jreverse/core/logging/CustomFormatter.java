package it.denzosoft.jreverse.core.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Custom formatter for JReverse logging providing structured output
 * Format: [YYYY-MM-DD HH:mm:ss] [LEVEL] logger_name: message
 */
public class CustomFormatter extends Formatter {
    
    private static final String FORMAT = "[%1$tF %1$tT] [%2$s] %3$s: %4$s%n";
    
    @Override
    public String format(LogRecord record) {
        String formattedMessage = formatMessage(record);
        
        // Add exception stack trace if present
        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            record.getThrown().printStackTrace(pw);
            formattedMessage += sw.toString();
        }
        
        return String.format(FORMAT,
            new Date(record.getMillis()),
            record.getLevel(),
            getSimpleClassName(record.getLoggerName()),
            formattedMessage);
    }
    
    /**
     * Get simple class name from fully qualified name for cleaner output
     */
    private String getSimpleClassName(String fullyQualifiedName) {
        if (fullyQualifiedName == null || fullyQualifiedName.isEmpty()) {
            return "ROOT";
        }
        
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        if (lastDot >= 0) {
            return fullyQualifiedName.substring(lastDot + 1);
        }
        
        return fullyQualifiedName;
    }
}