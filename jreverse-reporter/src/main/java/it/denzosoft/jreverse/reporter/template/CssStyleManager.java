package it.denzosoft.jreverse.reporter.template;

import it.denzosoft.jreverse.core.port.ReportType;

/**
 * Manages CSS styles for HTML reports with modular approach.
 */
public class CssStyleManager {
    
    /**
     * Returns common CSS styles used across all reports.
     */
    public String getCommonStyles() {
        return "/* Common Styles */\n" +
               "body {\n" +
               "    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
               "    margin: 0;\n" +
               "    padding: 0;\n" +
               "    background-color: #f5f5f5;\n" +
               "    color: #333;\n" +
               "    line-height: 1.6;\n" +
               "}\n" +
               "\n" +
               ".main-header {\n" +
               "    background: linear-gradient(135deg, #2c3e50, #3498db);\n" +
               "    color: white;\n" +
               "    padding: 2rem;\n" +
               "    text-align: center;\n" +
               "    box-shadow: 0 2px 10px rgba(0,0,0,0.1);\n" +
               "}\n" +
               "\n" +
               ".main-header h1 {\n" +
               "    margin: 0 0 1rem 0;\n" +
               "    font-size: 2.5rem;\n" +
               "    font-weight: 300;\n" +
               "}\n" +
               "\n" +
               ".report-info {\n" +
               "    display: flex;\n" +
               "    justify-content: center;\n" +
               "    gap: 2rem;\n" +
               "    flex-wrap: wrap;\n" +
               "}\n" +
               "\n" +
               ".report-info span {\n" +
               "    background: rgba(255,255,255,0.2);\n" +
               "    padding: 0.5rem 1rem;\n" +
               "    border-radius: 5px;\n" +
               "    font-size: 0.9rem;\n" +
               "}\n" +
               "\n" +
               ".content {\n" +
               "    max-width: 1200px;\n" +
               "    margin: 2rem auto;\n" +
               "    padding: 0 2rem;\n" +
               "}\n" +
               "\n" +
               "section {\n" +
               "    background: white;\n" +
               "    margin: 2rem 0;\n" +
               "    padding: 2rem;\n" +
               "    border-radius: 8px;\n" +
               "    box-shadow: 0 2px 10px rgba(0,0,0,0.1);\n" +
               "}\n" +
               "\n" +
               "section h2 {\n" +
               "    color: #2c3e50;\n" +
               "    border-bottom: 3px solid #3498db;\n" +
               "    padding-bottom: 0.5rem;\n" +
               "    margin-bottom: 1.5rem;\n" +
               "}\n" +
               "\n" +
               ".stats-grid {\n" +
               "    display: grid;\n" +
               "    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));\n" +
               "    gap: 1rem;\n" +
               "    margin-top: 1rem;\n" +
               "}\n" +
               "\n" +
               ".stat-card {\n" +
               "    background: #f8f9fa;\n" +
               "    padding: 1.5rem;\n" +
               "    border-radius: 8px;\n" +
               "    text-align: center;\n" +
               "    border: 1px solid #e9ecef;\n" +
               "}\n" +
               "\n" +
               ".stat-card h3 {\n" +
               "    margin: 0 0 0.5rem 0;\n" +
               "    color: #6c757d;\n" +
               "    font-size: 0.9rem;\n" +
               "    text-transform: uppercase;\n" +
               "}\n" +
               "\n" +
               ".stat-number {\n" +
               "    font-size: 2rem;\n" +
               "    font-weight: bold;\n" +
               "    color: #3498db;\n" +
               "}\n" +
               "\n" +
               ".stat-text {\n" +
               "    font-size: 1.2rem;\n" +
               "    color: #2c3e50;\n" +
               "}\n" +
               "\n" +
               ".main-footer {\n" +
               "    background: #2c3e50;\n" +
               "    color: white;\n" +
               "    text-align: center;\n" +
               "    padding: 1rem;\n" +
               "    margin-top: 3rem;\n" +
               "}\n" +
               "\n" +
               ".main-footer p {\n" +
               "    margin: 0;\n" +
               "    font-size: 0.9rem;\n" +
               "}\n" +
               getEntrypointStyles();
    }
    
    /**
     * Returns CSS styles specific to entrypoint annotations.
     */
    private String getEntrypointStyles() {
        return "\n/* Entrypoint Annotation Styles */\n" +
               ".entrypoint-categories {\n" +
               "    display: grid;\n" +
               "    grid-template-columns: 1fr;\n" +
               "    gap: 2rem;\n" +
               "    margin-top: 2rem;\n" +
               "}\n" +
               "\n" +
               ".category {\n" +
               "    background: white;\n" +
               "    border-radius: 8px;\n" +
               "    padding: 1.5rem;\n" +
               "    box-shadow: 0 2px 10px rgba(0,0,0,0.1);\n" +
               "}\n" +
               "\n" +
               ".category h3 {\n" +
               "    color: #2c3e50;\n" +
               "    border-bottom: 3px solid;\n" +
               "    padding-bottom: 0.5rem;\n" +
               "    margin-bottom: 1rem;\n" +
               "}\n" +
               "\n" +
               ".category.rest-endpoints h3 { border-color: #3498db; }\n" +
               ".category.scheduled-tasks h3 { border-color: #e74c3c; }\n" +
               ".category.message-listeners h3 { border-color: #2ecc71; }\n" +
               ".category.event-listeners h3 { border-color: #f39c12; }\n" +
               "\n" +
               ".entrypoint-grid, .task-grid, .listener-grid, .event-grid {\n" +
               "    display: grid;\n" +
               "    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));\n" +
               "    gap: 1rem;\n" +
               "    margin-top: 1rem;\n" +
               "}\n" +
               "\n" +
               ".task-card, .listener-card, .event-card, .endpoint-card {\n" +
               "    background: #f8f9fa;\n" +
               "    padding: 1rem;\n" +
               "    border-radius: 5px;\n" +
               "    border-left: 4px solid;\n" +
               "    transition: transform 0.2s ease, box-shadow 0.2s ease;\n" +
               "}\n" +
               "\n" +
               ".task-card:hover, .listener-card:hover, .event-card:hover, .endpoint-card:hover {\n" +
               "    transform: translateY(-2px);\n" +
               "    box-shadow: 0 4px 15px rgba(0,0,0,0.15);\n" +
               "}\n" +
               "\n" +
               ".task-card { border-left-color: #e74c3c; }\n" +
               ".listener-card { border-left-color: #2ecc71; }\n" +
               ".event-card { border-left-color: #f39c12; }\n" +
               ".endpoint-card { border-left-color: #3498db; }\n" +
               "\n" +
               ".security-level {\n" +
               "    display: inline-block;\n" +
               "    padding: 0.2rem 0.5rem;\n" +
               "    border-radius: 3px;\n" +
               "    font-size: 0.8rem;\n" +
               "    font-weight: bold;\n" +
               "    text-transform: uppercase;\n" +
               "    margin-left: 0.5rem;\n" +
               "}\n" +
               "\n" +
               ".security-level.high { background: #e74c3c; color: white; }\n" +
               ".security-level.medium { background: #f39c12; color: white; }\n" +
               ".security-level.low { background: #2ecc71; color: white; }\n" +
               ".security-level.none { background: #95a5a6; color: white; }\n" +
               "\n" +
               ".correlation-list {\n" +
               "    list-style: none;\n" +
               "    padding: 0;\n" +
               "    margin: 0.5rem 0;\n" +
               "}\n" +
               "\n" +
               ".correlation-list li {\n" +
               "    display: flex;\n" +
               "    align-items: center;\n" +
               "    padding: 0.25rem 0;\n" +
               "    font-size: 0.9rem;\n" +
               "}\n" +
               "\n" +
               ".correlation-list li::before {\n" +
               "    content: '●';\n" +
               "    margin-right: 0.5rem;\n" +
               "    font-size: 1.2rem;\n" +
               "}\n" +
               "\n" +
               ".scheduled-task::before { color: #e74c3c; }\n" +
               ".async-method::before { color: #3498db; }\n" +
               ".message-listener::before { color: #2ecc71; }\n" +
               ".event-listener::before { color: #f39c12; }\n" +
               "\n" +
               ".endpoint-security {\n" +
               "    margin-top: 1rem;\n" +
               "    padding: 1rem;\n" +
               "    background: #f1f3f4;\n" +
               "    border-radius: 5px;\n" +
               "    border-left: 4px solid #e74c3c;\n" +
               "}\n" +
               "\n" +
               ".endpoint-correlations {\n" +
               "    margin-top: 1rem;\n" +
               "    padding: 1rem;\n" +
               "    background: #f0f8f0;\n" +
               "    border-radius: 5px;\n" +
               "    border-left: 4px solid #2ecc71;\n" +
               "}\n" +
               "\n" +
               ".correlated {\n" +
               "    border: 2px solid #f39c12 !important;\n" +
               "    background-color: #fff3cd !important;\n" +
               "}\n" +
               "\n" +
               ".correlation-scheduled { border-color: #e74c3c !important; }\n" +
               ".correlation-async { border-color: #3498db !important; }\n" +
               ".correlation-message { border-color: #2ecc71 !important; }\n" +
               "\n" +
               "@media (max-width: 768px) {\n" +
               "    .content {\n" +
               "        padding: 0 1rem;\n" +
               "    }\n" +
               "    \n" +
               "    .main-header {\n" +
               "        padding: 1rem;\n" +
               "    }\n" +
               "    \n" +
               "    .main-header h1 {\n" +
               "        font-size: 2rem;\n" +
               "    }\n" +
               "    \n" +
               "    .report-info {\n" +
               "        flex-direction: column;\n" +
               "        gap: 0.5rem;\n" +
               "    }\n" +
               "    \n" +
               "    .entrypoint-grid, .task-grid, .listener-grid, .event-grid {\n" +
               "        grid-template-columns: 1fr;\n" +
               "    }\n" +
               "}";
    }
    
    /**
     * Returns report-specific CSS styles.
     */
    public String getReportSpecificStyles(ReportType reportType) {
        switch (reportType) {
            case BOOTSTRAP_ANALYSIS:
                return getBootstrapSpecificStyles();
            case REST_ENDPOINTS_MAP_ENHANCED:
                return getRestEndpointsSpecificStyles();
            case SCHEDULED_TASKS_ANALYSIS:
                return getScheduledTasksSpecificStyles();
            case MESSAGE_LISTENERS_CATALOG:
                return getMessageListenersSpecificStyles();
            default:
                return "";
        }
    }
    
    private String getBootstrapSpecificStyles() {
        return "\n/* Bootstrap Analysis Specific Styles */\n" +
               ".bootstrap-summary {\n" +
               "    margin-bottom: 2rem;\n" +
               "}\n" +
               "\n" +
               ".sequence-diagram-section {\n" +
               "    margin: 2rem 0;\n" +
               "}\n" +
               "\n" +
               ".diagram-container {\n" +
               "    background: #f8f9fa;\n" +
               "    border: 1px solid #dee2e6;\n" +
               "    border-radius: 5px;\n" +
               "    padding: 1rem;\n" +
               "    margin: 1rem 0;\n" +
               "}\n" +
               "\n" +
               ".sequence-diagram {\n" +
               "    background: white;\n" +
               "    padding: 1rem;\n" +
               "    border: 1px solid #e9ecef;\n" +
               "    border-radius: 3px;\n" +
               "    font-family: 'Courier New', monospace;\n" +
               "    font-size: 0.85rem;\n" +
               "    line-height: 1.4;\n" +
               "    overflow-x: auto;\n" +
               "}";
    }
    
    private String getRestEndpointsSpecificStyles() {
        return "\n/* REST Endpoints Enhanced Specific Styles */\n" +
               ".endpoint-method {\n" +
               "    display: inline-block;\n" +
               "    padding: 0.25rem 0.5rem;\n" +
               "    border-radius: 3px;\n" +
               "    font-weight: bold;\n" +
               "    font-size: 0.8rem;\n" +
               "    margin-right: 0.5rem;\n" +
               "}\n" +
               "\n" +
               ".method-get { background: #2ecc71; color: white; }\n" +
               ".method-post { background: #3498db; color: white; }\n" +
               ".method-put { background: #f39c12; color: white; }\n" +
               ".method-delete { background: #e74c3c; color: white; }\n" +
               ".method-patch { background: #9b59b6; color: white; }";
    }
    
    private String getScheduledTasksSpecificStyles() {
        return "\n/* Scheduled Tasks Specific Styles */\n" +
               ".scheduling-header {\n" +
               "    background: linear-gradient(135deg, #e74c3c, #c0392b);\n" +
               "}\n" +
               "\n" +
               ".task-timeline {\n" +
               "    background: #fff;\n" +
               "    border-radius: 8px;\n" +
               "    padding: 2rem;\n" +
               "    margin: 2rem 0;\n" +
               "}\n" +
               "\n" +
               ".cron-expression {\n" +
               "    font-family: 'Courier New', monospace;\n" +
               "    background: #f8f9fa;\n" +
               "    padding: 0.25rem 0.5rem;\n" +
               "    border-radius: 3px;\n" +
               "    font-size: 0.9rem;\n" +
               "}";
    }
    
    private String getMessageListenersSpecificStyles() {
        return "\n/* Message Listeners Specific Styles */\n" +
               ".messaging-header {\n" +
               "    background: linear-gradient(135deg, #2ecc71, #27ae60);\n" +
               "}\n" +
               "\n" +
               ".listener-type-tabs {\n" +
               "    display: flex;\n" +
               "    border-bottom: 2px solid #dee2e6;\n" +
               "    margin-bottom: 2rem;\n" +
               "}\n" +
               "\n" +
               ".tab-button {\n" +
               "    background: none;\n" +
               "    border: none;\n" +
               "    padding: 1rem 2rem;\n" +
               "    cursor: pointer;\n" +
               "    font-size: 1rem;\n" +
               "    border-bottom: 3px solid transparent;\n" +
               "    transition: all 0.2s ease;\n" +
               "}\n" +
               "\n" +
               ".tab-button:hover {\n" +
               "    background: #f8f9fa;\n" +
               "}\n" +
               "\n" +
               ".tab-button.active {\n" +
               "    border-bottom-color: #2ecc71;\n" +
               "    color: #2ecc71;\n" +
               "    font-weight: bold;\n" +
               "}";
    }
}