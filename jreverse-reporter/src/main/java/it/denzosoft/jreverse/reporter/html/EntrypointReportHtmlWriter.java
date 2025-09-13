package it.denzosoft.jreverse.reporter.html;

import it.denzosoft.jreverse.core.logging.JReverseLogger;
import it.denzosoft.jreverse.core.model.BeanDefinitionInfo;
import it.denzosoft.jreverse.core.model.RestEndpointInfo;
import it.denzosoft.jreverse.core.model.WebMvcMappingInfo;
import it.denzosoft.jreverse.reporter.springboot.SpringBootEntrypointReport;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * HTML writer for Spring Boot entrypoint analysis reports.
 * Generates comprehensive HTML reports with CSS styling and JavaScript interactivity.
 */
public class EntrypointReportHtmlWriter {
    
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(EntrypointReportHtmlWriter.class);
    
    private static final String REPORT_TITLE = "Spring Boot Entrypoint Analysis Report";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Writes a Spring Boot entrypoint report to HTML format.
     */
    public void writeReport(SpringBootEntrypointReport report, Writer writer) throws IOException {
        LOGGER.info("Writing Spring Boot entrypoint report to HTML");
        
        writer.write("<!DOCTYPE html>\n");
        writer.write("<html lang=\"en\">\n");
        writeHead(writer);
        writer.write("<body>\n");
        
        writeHeader(report, writer);
        writeExecutiveSummary(report, writer);
        writeMainMethodSection(report, writer);
        writeComponentScanSection(report, writer);
        writeRestEndpointsSection(report, writer);
        writeWebMvcSection(report, writer);
        writeConfigurationSection(report, writer);
        writeFooter(writer);
        
        writer.write("</body>\n");
        writer.write("</html>\n");
    }
    
    /**
     * Generates HTML as a String for the given report.
     */
    public String generateHtmlReport(SpringBootEntrypointReport report) {
        try (StringWriter writer = new StringWriter()) {
            writeReport(report, writer);
            return writer.toString();
        } catch (IOException e) {
            LOGGER.error("Error generating HTML report", e);
            return generateErrorReport(e.getMessage());
        }
    }
    
    private void writeHead(Writer writer) throws IOException {
        writer.write("<head>\n");
        writer.write("    <meta charset=\"UTF-8\">\n");
        writer.write("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        writer.write("    <title>" + REPORT_TITLE + "</title>\n");
        writeCSS(writer);
        writer.write("</head>\n");
    }
    
    private void writeCSS(Writer writer) throws IOException {
        writer.write("    <style>\n");
        writer.write("        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }\n");
        writer.write("        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n");
        writer.write("        .header { border-bottom: 3px solid #007bff; padding-bottom: 20px; margin-bottom: 30px; }\n");
        writer.write("        .header h1 { color: #007bff; margin: 0; font-size: 2.5em; }\n");
        writer.write("        .header .meta { color: #666; margin-top: 10px; }\n");
        writer.write("        .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 30px; }\n");
        writer.write("        .summary-card { background: #f8f9fa; padding: 20px; border-radius: 6px; border-left: 4px solid #007bff; }\n");
        writer.write("        .summary-card h3 { margin: 0 0 10px 0; color: #333; }\n");
        writer.write("        .summary-card .value { font-size: 2em; font-weight: bold; color: #007bff; }\n");
        writer.write("        .section { margin-bottom: 40px; }\n");
        writer.write("        .section h2 { color: #333; border-bottom: 2px solid #dee2e6; padding-bottom: 10px; }\n");
        writer.write("        .section h3 { color: #495057; margin-top: 25px; }\n");
        writer.write("        .table-container { overflow-x: auto; }\n");
        writer.write("        table { width: 100%; border-collapse: collapse; margin-top: 15px; }\n");
        writer.write("        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #dee2e6; }\n");
        writer.write("        th { background-color: #f8f9fa; font-weight: 600; color: #495057; }\n");
        writer.write("        tr:hover { background-color: #f8f9fa; }\n");
        writer.write("        .badge { display: inline-block; padding: 4px 8px; border-radius: 4px; font-size: 0.8em; font-weight: 500; }\n");
        writer.write("        .badge-primary { background-color: #007bff; color: white; }\n");
        writer.write("        .badge-success { background-color: #28a745; color: white; }\n");
        writer.write("        .badge-warning { background-color: #ffc107; color: #212529; }\n");
        writer.write("        .badge-danger { background-color: #dc3545; color: white; }\n");
        writer.write("        .badge-info { background-color: #17a2b8; color: white; }\n");
        writer.write("        .method-get { color: #28a745; }\n");
        writer.write("        .method-post { color: #007bff; }\n");
        writer.write("        .method-put { color: #ffc107; }\n");
        writer.write("        .method-delete { color: #dc3545; }\n");
        writer.write("        .code { background-color: #f8f9fa; padding: 2px 6px; border-radius: 3px; font-family: 'Courier New', monospace; }\n");
        writer.write("        .footer { margin-top: 50px; padding-top: 20px; border-top: 1px solid #dee2e6; text-align: center; color: #6c757d; }\n");
        writer.write("        .error { background-color: #f8d7da; color: #721c24; padding: 15px; border-radius: 6px; border: 1px solid #f5c6cb; }\n");
        writer.write("        .no-data { text-align: center; color: #6c757d; font-style: italic; padding: 20px; }\n");
        writer.write("    </style>\n");
    }
    
    private void writeHeader(SpringBootEntrypointReport report, Writer writer) throws IOException {
        writer.write("    <div class=\"container\">\n");
        writer.write("        <div class=\"header\">\n");
        writer.write("            <h1>" + REPORT_TITLE + "</h1>\n");
        writer.write("            <div class=\"meta\">\n");
        writer.write("                <strong>JAR Location:</strong> " + escapeHtml(report.getJarLocation()) + "<br>\n");
        writer.write("                <strong>Generated:</strong> " + LocalDateTime.now().format(DATE_FORMAT) + "<br>\n");
        writer.write("                <strong>Analysis Time:</strong> " + report.getAnalysisTimeMs() + "ms<br>\n");
        writer.write("                <strong>Status:</strong> ");
        if (report.isSuccessful()) {
            writer.write("<span class=\"badge badge-success\">Success</span>\n");
        } else {
            writer.write("<span class=\"badge badge-danger\">Failed</span>\n");
        }
        writer.write("            </div>\n");
        writer.write("        </div>\n");
    }
    
    private void writeExecutiveSummary(SpringBootEntrypointReport report, Writer writer) throws IOException {
        writer.write("        <div class=\"section\">\n");
        writer.write("            <h2>Executive Summary</h2>\n");
        writer.write("            <div class=\"summary\">\n");
        
        writer.write("                <div class=\"summary-card\">\n");
        writer.write("                    <h3>Total Endpoints</h3>\n");
        writer.write("                    <div class=\"value\">" + report.getTotalEndpoints() + "</div>\n");
        writer.write("                </div>\n");
        
        writer.write("                <div class=\"summary-card\">\n");
        writer.write("                    <h3>Configurations</h3>\n");
        writer.write("                    <div class=\"value\">" + report.getTotalConfigurations() + "</div>\n");
        writer.write("                </div>\n");
        
        writer.write("                <div class=\"summary-card\">\n");
        writer.write("                    <h3>Bean Definitions</h3>\n");
        writer.write("                    <div class=\"value\">" + report.getTotalBeanDefinitions() + "</div>\n");
        writer.write("                </div>\n");
        
        if (report.getComponentScanAnalysis() != null) {
            writer.write("                <div class=\"summary-card\">\n");
            writer.write("                    <h3>Scanned Packages</h3>\n");
            writer.write("                    <div class=\"value\">" + report.getComponentScanAnalysis().getEffectivePackages().size() + "</div>\n");
            writer.write("                </div>\n");
        }
        
        writer.write("            </div>\n");
        writer.write("        </div>\n");
    }
    
    private void writeMainMethodSection(SpringBootEntrypointReport report, Writer writer) throws IOException {
        writer.write("        <div class=\"section\">\n");
        writer.write("            <h2>Main Method Analysis</h2>\n");
        
        if (report.getMainMethodAnalysis() != null && report.getMainMethodAnalysis().isSuccessful()) {
            writer.write("            <p><strong>Main Method Type:</strong> ");
            writer.write("<span class=\"badge badge-primary\">" + report.getMainMethodAnalysis().getMainMethodType() + "</span></p>\n");
            
            if (report.getMainMethodAnalysis().getDeclaringClassName() != null) {
                writer.write("            <p><strong>Declaring Class:</strong> <span class=\"code\">" + 
                           escapeHtml(report.getMainMethodAnalysis().getDeclaringClassName()) + "</span></p>\n");
            }
        } else {
            writer.write("            <div class=\"no-data\">No main method analysis available</div>\n");
        }
        
        writer.write("        </div>\n");
    }
    
    private void writeComponentScanSection(SpringBootEntrypointReport report, Writer writer) throws IOException {
        writer.write("        <div class=\"section\">\n");
        writer.write("            <h2>Component Scan Configuration</h2>\n");
        
        if (report.getComponentScanAnalysis() != null && report.getComponentScanAnalysis().hasConfigurations()) {
            writer.write("            <h3>Configuration Classes</h3>\n");
            writer.write("            <ul>\n");
            for (String config : report.getComponentScanAnalysis().getConfigurationClasses()) {
                writer.write("                <li><span class=\"code\">" + escapeHtml(config) + "</span></li>\n");
            }
            writer.write("            </ul>\n");
            
            writer.write("            <h3>Effective Packages</h3>\n");
            writer.write("            <ul>\n");
            for (String pkg : report.getComponentScanAnalysis().getEffectivePackages()) {
                writer.write("                <li><span class=\"code\">" + escapeHtml(pkg) + "</span></li>\n");
            }
            writer.write("            </ul>\n");
        } else {
            writer.write("            <div class=\"no-data\">No component scan configurations found</div>\n");
        }
        
        writer.write("        </div>\n");
    }
    
    private void writeRestEndpointsSection(SpringBootEntrypointReport report, Writer writer) throws IOException {
        writer.write("        <div class=\"section\">\n");
        writer.write("            <h2>REST Endpoints</h2>\n");
        
        if (report.getRestEndpointAnalysis() != null && report.getRestEndpointAnalysis().hasEndpoints()) {
            writer.write("            <div class=\"table-container\">\n");
            writer.write("                <table>\n");
            writer.write("                    <thead>\n");
            writer.write("                        <tr>\n");
            writer.write("                            <th>Path</th>\n");
            writer.write("                            <th>HTTP Methods</th>\n");
            writer.write("                            <th>Controller</th>\n");
            writer.write("                            <th>Method</th>\n");
            writer.write("                            <th>Return Type</th>\n");
            writer.write("                        </tr>\n");
            writer.write("                    </thead>\n");
            writer.write("                    <tbody>\n");
            
            for (RestEndpointInfo endpoint : report.getRestEndpointAnalysis().getEndpoints()) {
                writer.write("                        <tr>\n");
                writer.write("                            <td><span class=\"code\">" + escapeHtml(endpoint.getPath()) + "</span></td>\n");
                writer.write("                            <td>");
                for (RestEndpointInfo.HttpMethod method : endpoint.getHttpMethods()) {
                    writer.write("<span class=\"badge badge-" + getMethodBadgeClass(method.name()) + "\">" + method.name() + "</span> ");
                }
                writer.write("</td>\n");
                writer.write("                            <td><span class=\"code\">" + escapeHtml(getSimpleClassName(endpoint.getControllerClass())) + "</span></td>\n");
                writer.write("                            <td><span class=\"code\">" + escapeHtml(endpoint.getMethodName()) + "</span></td>\n");
                writer.write("                            <td><span class=\"code\">" + escapeHtml(getSimpleClassName(endpoint.getReturnType())) + "</span></td>\n");
                writer.write("                        </tr>\n");
            }
            
            writer.write("                    </tbody>\n");
            writer.write("                </table>\n");
            writer.write("            </div>\n");
        } else {
            writer.write("            <div class=\"no-data\">No REST endpoints found</div>\n");
        }
        
        writer.write("        </div>\n");
    }
    
    private void writeWebMvcSection(SpringBootEntrypointReport report, Writer writer) throws IOException {
        writer.write("        <div class=\"section\">\n");
        writer.write("            <h2>Spring MVC Mappings</h2>\n");
        
        if (report.getWebMvcAnalysis() != null && report.getWebMvcAnalysis().hasMappings()) {
            writer.write("            <h3>Mapping Statistics</h3>\n");
            writer.write("            <div class=\"table-container\">\n");
            writer.write("                <table>\n");
            writer.write("                    <thead>\n");
            writer.write("                        <tr><th>HTTP Method</th><th>Count</th></tr>\n");
            writer.write("                    </thead>\n");
            writer.write("                    <tbody>\n");
            
            for (Map.Entry<String, Integer> entry : report.getWebMvcAnalysis().getMappingsByHttpMethod().entrySet()) {
                writer.write("                        <tr>\n");
                writer.write("                            <td><span class=\"badge badge-" + getMethodBadgeClass(entry.getKey()) + "\">" + entry.getKey() + "</span></td>\n");
                writer.write("                            <td>" + entry.getValue() + "</td>\n");
                writer.write("                        </tr>\n");
            }
            
            writer.write("                    </tbody>\n");
            writer.write("                </table>\n");
            writer.write("            </div>\n");
        } else {
            writer.write("            <div class=\"no-data\">No Spring MVC mappings found</div>\n");
        }
        
        writer.write("        </div>\n");
    }
    
    private void writeConfigurationSection(SpringBootEntrypointReport report, Writer writer) throws IOException {
        writer.write("        <div class=\"section\">\n");
        writer.write("            <h2>Spring Configuration</h2>\n");
        
        if (report.getConfigurationAnalysis() != null && report.getConfigurationAnalysis().hasBeanDefinitions()) {
            writer.write("            <h3>Bean Definitions by Scope</h3>\n");
            writer.write("            <div class=\"table-container\">\n");
            writer.write("                <table>\n");
            writer.write("                    <thead>\n");
            writer.write("                        <tr><th>Scope</th><th>Count</th></tr>\n");
            writer.write("                    </thead>\n");
            writer.write("                    <tbody>\n");
            
            for (Map.Entry<BeanDefinitionInfo.BeanScope, Integer> entry : report.getConfigurationAnalysis().getBeansByScope().entrySet()) {
                writer.write("                        <tr>\n");
                writer.write("                            <td><span class=\"badge badge-info\">" + entry.getKey().getValue() + "</span></td>\n");
                writer.write("                            <td>" + entry.getValue() + "</td>\n");
                writer.write("                        </tr>\n");
            }
            
            writer.write("                    </tbody>\n");
            writer.write("                </table>\n");
            writer.write("            </div>\n");
            
            List<BeanDefinitionInfo> primaryBeans = report.getConfigurationAnalysis().getPrimaryBeans();
            if (!primaryBeans.isEmpty()) {
                writer.write("            <h3>Primary Beans</h3>\n");
                writer.write("            <ul>\n");
                for (BeanDefinitionInfo bean : primaryBeans) {
                    writer.write("                <li><span class=\"code\">" + escapeHtml(bean.getBeanName()) + "</span> " +
                               "(<span class=\"code\">" + escapeHtml(getSimpleClassName(bean.getBeanClass())) + "</span>)</li>\n");
                }
                writer.write("            </ul>\n");
            }
        } else {
            writer.write("            <div class=\"no-data\">No Spring configuration found</div>\n");
        }
        
        writer.write("        </div>\n");
    }
    
    private void writeFooter(Writer writer) throws IOException {
        writer.write("        <div class=\"footer\">\n");
        writer.write("            <p>Generated by JReverse - Java Reverse Engineering Tool</p>\n");
        writer.write("        </div>\n");
        writer.write("    </div>\n");
    }
    
    private String generateErrorReport(String errorMessage) {
        return "<!DOCTYPE html>\n" +
               "<html><head><title>Report Generation Error</title></head>\n" +
               "<body><div class=\"error\"><h2>Error</h2><p>" + escapeHtml(errorMessage) + "</p></div></body>\n" +
               "</html>";
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
    
    private String getSimpleClassName(String fullyQualifiedName) {
        if (fullyQualifiedName == null) return "";
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        return lastDot > 0 ? fullyQualifiedName.substring(lastDot + 1) : fullyQualifiedName;
    }
    
    private String getMethodBadgeClass(String httpMethod) {
        switch (httpMethod.toUpperCase()) {
            case "GET": return "success";
            case "POST": return "primary";
            case "PUT": return "warning";
            case "DELETE": return "danger";
            default: return "info";
        }
    }
}