package it.denzosoft.jreverse.core.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Test handler to capture log records for testing purposes
 */
public class TestLogHandler extends Handler {
    
    private final List<LogRecord> records = new ArrayList<>();
    
    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record)) {
            records.add(record);
        }
    }
    
    @Override
    public void flush() {
        // No-op for testing
    }
    
    @Override
    public void close() throws SecurityException {
        records.clear();
    }
    
    public List<LogRecord> getRecords() {
        return new ArrayList<>(records);
    }
    
    public void clearRecords() {
        records.clear();
    }
    
    public boolean hasRecordWithMessage(String message) {
        return records.stream()
                .anyMatch(record -> record.getMessage().contains(message));
    }
    
    public LogRecord getLastRecord() {
        return records.isEmpty() ? null : records.get(records.size() - 1);
    }
}