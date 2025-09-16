package it.denzosoft.jreverse.reporter.template;

import it.denzosoft.jreverse.core.port.ReportType;

/**
 * Manages JavaScript functionality for HTML reports.
 */
public class JavaScriptManager {
    
    /**
     * Returns common JavaScript code used across all reports.
     */
    public String getCommonScripts() {
        return "// Common JavaScript functionality\n" +
               "\n" +
               "// Utility functions\n" +
               "function toggleSection(sectionId) {\n" +
               "    const section = document.getElementById(sectionId);\n" +
               "    if (section) {\n" +
               "        section.style.display = section.style.display === 'none' ? 'block' : 'none';\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "function showTab(tabName) {\n" +
               "    // Hide all tab contents\n" +
               "    document.querySelectorAll('.tab-content').forEach(content => {\n" +
               "        content.style.display = 'none';\n" +
               "    });\n" +
               "    \n" +
               "    // Remove active class from all tabs\n" +
               "    document.querySelectorAll('.tab-button').forEach(button => {\n" +
               "        button.classList.remove('active');\n" +
               "    });\n" +
               "    \n" +
               "    // Show selected tab content\n" +
               "    const selectedContent = document.getElementById(tabName + '-content');\n" +
               "    if (selectedContent) {\n" +
               "        selectedContent.style.display = 'block';\n" +
               "    }\n" +
               "    \n" +
               "    // Add active class to clicked tab\n" +
               "    event.target.classList.add('active');\n" +
               "}\n" +
               "\n" +
               getEntrypointCorrelationScript();
    }
    
    /**
     * Returns JavaScript for entrypoint correlation functionality.
     */
    private String getEntrypointCorrelationScript() {
        return "\n// Entrypoint Correlation Manager\n" +
               "class EntrypointCorrelationManager {\n" +
               "    constructor() {\n" +
               "        this.correlations = new Map();\n" +
               "        this.highlightedElements = new Set();\n" +
               "        this.initializeEventListeners();\n" +
               "    }\n" +
               "    \n" +
               "    initializeEventListeners() {\n" +
               "        document.addEventListener('DOMContentLoaded', () => {\n" +
               "            // Add hover events to all entrypoint cards\n" +
               "            document.querySelectorAll('.endpoint-card, .task-card, .listener-card, .event-card').forEach(card => {\n" +
               "                card.addEventListener('mouseenter', (e) => {\n" +
               "                    this.highlightCorrelations(e.currentTarget.dataset.entrypointId);\n" +
               "                });\n" +
               "                \n" +
               "                card.addEventListener('mouseleave', () => {\n" +
               "                    this.clearHighlights();\n" +
               "                });\n" +
               "            });\n" +
               "        });\n" +
               "    }\n" +
               "    \n" +
               "    addCorrelation(sourceId, targetId, type) {\n" +
               "        if (!this.correlations.has(sourceId)) {\n" +
               "            this.correlations.set(sourceId, []);\n" +
               "        }\n" +
               "        this.correlations.get(sourceId).push({ targetId, type });\n" +
               "    }\n" +
               "    \n" +
               "    highlightCorrelations(entrypointId) {\n" +
               "        if (!entrypointId) return;\n" +
               "        \n" +
               "        this.clearHighlights();\n" +
               "        \n" +
               "        const correlations = this.correlations.get(entrypointId) || [];\n" +
               "        correlations.forEach(({ targetId, type }) => {\n" +
               "            const element = document.querySelector(`[data-entrypoint-id=\"${targetId}\"]`);\n" +
               "            if (element) {\n" +
               "                element.classList.add('correlated', `correlation-${type}`);\n" +
               "                this.highlightedElements.add(element);\n" +
               "            }\n" +
               "        });\n" +
               "    }\n" +
               "    \n" +
               "    clearHighlights() {\n" +
               "        this.highlightedElements.forEach(element => {\n" +
               "            element.classList.remove('correlated', 'correlation-scheduled', \n" +
               "                                   'correlation-async', 'correlation-message', 'correlation-event');\n" +
               "        });\n" +
               "        this.highlightedElements.clear();\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "// Initialize correlation manager\n" +
               "const correlationManager = new EntrypointCorrelationManager();\n";
    }
    
    /**
     * Returns report-specific JavaScript code.
     */
    public String getReportSpecificScripts(ReportType reportType) {
        switch (reportType) {
            case REST_ENDPOINT_MAP:
                return getRestEndpointsScripts();
            case HTTP_CALL_GRAPH:
                return getCallGraphScripts();
            case SCHEDULED_TASKS_ANALYSIS:
                return getScheduledTasksScripts();
            case MESSAGING_INTEGRATION_ANALYSIS:
                return getMessageListenersScripts();
            case EVENT_LISTENER_ANALYSIS:
                return getEntryPointsScripts();
            default:
                return "";
        }
    }
    
    private String getRestEndpointsScripts() {
        return "\n// REST Endpoints specific JavaScript\n" +
               "function filterEndpoints(filterType) {\n" +
               "    const cards = document.querySelectorAll('.endpoint-card');\n" +
               "    cards.forEach(card => {\n" +
               "        const shouldShow = filterType === 'all' || \n" +
               "                          card.classList.contains(filterType) ||\n" +
               "                          card.dataset.method === filterType ||\n" +
               "                          card.dataset.security === filterType;\n" +
               "        \n" +
               "        card.style.display = shouldShow ? 'block' : 'none';\n" +
               "    });\n" +
               "}\n" +
               "\n" +
               "function toggleEndpointDetails(cardId) {\n" +
               "    const details = document.querySelector(`#${cardId} .endpoint-details`);\n" +
               "    if (details) {\n" +
               "        details.style.display = details.style.display === 'none' ? 'block' : 'none';\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "// Initialize endpoint filters\n" +
               "document.addEventListener('DOMContentLoaded', function() {\n" +
               "    // Add filter buttons if they exist\n" +
               "    const filterContainer = document.querySelector('.endpoint-filters');\n" +
               "    if (filterContainer) {\n" +
               "        filterContainer.innerHTML = `\n" +
               "            <button onclick=\"filterEndpoints('all')\" class=\"filter-btn active\">All</button>\n" +
               "            <button onclick=\"filterEndpoints('GET')\" class=\"filter-btn\">GET</button>\n" +
               "            <button onclick=\"filterEndpoints('POST')\" class=\"filter-btn\">POST</button>\n" +
               "            <button onclick=\"filterEndpoints('PUT')\" class=\"filter-btn\">PUT</button>\n" +
               "            <button onclick=\"filterEndpoints('DELETE')\" class=\"filter-btn\">DELETE</button>\n" +
               "            <button onclick=\"filterEndpoints('high')\" class=\"filter-btn\">High Security</button>\n" +
               "            <button onclick=\"filterEndpoints('none')\" class=\"filter-btn\">No Security</button>\n" +
               "        `;\n" +
               "    }\n" +
               "});";
    }
    
    private String getCallGraphScripts() {
        return "\n// HTTP Call Graph specific JavaScript\n" +
               "// Note: This would integrate with vis.js or similar graph library\n" +
               "class EntrypointFlowGraph {\n" +
               "    constructor(containerId) {\n" +
               "        this.container = document.getElementById(containerId);\n" +
               "        this.nodes = [];\n" +
               "        this.edges = [];\n" +
               "    }\n" +
               "    \n" +
               "    addRestEndpoint(id, path, method, security) {\n" +
               "        this.nodes.push({\n" +
               "            id: id,\n" +
               "            label: `${method} ${path}`,\n" +
               "            group: 'rest-endpoint',\n" +
               "            color: this.getSecurityColor(security),\n" +
               "            shape: 'box'\n" +
               "        });\n" +
               "    }\n" +
               "    \n" +
               "    addScheduledTask(id, methodName, cron) {\n" +
               "        this.nodes.push({\n" +
               "            id: id,\n" +
               "            label: `⏰ ${methodName}\\n${cron}`,\n" +
               "            group: 'scheduled-task',\n" +
               "            color: '#FF6B35',\n" +
               "            shape: 'diamond'\n" +
               "        });\n" +
               "    }\n" +
               "    \n" +
               "    addMessageListener(id, methodName, destination, type) {\n" +
               "        this.nodes.push({\n" +
               "            id: id,\n" +
               "            label: `📨 ${methodName}\\n${type}: ${destination}`,\n" +
               "            group: 'message-listener',\n" +
               "            color: '#4ECDC4',\n" +
               "            shape: 'ellipse'\n" +
               "        });\n" +
               "    }\n" +
               "    \n" +
               "    addConnection(fromId, toId, type) {\n" +
               "        this.edges.push({\n" +
               "            from: fromId,\n" +
               "            to: toId,\n" +
               "            label: type,\n" +
               "            arrows: 'to',\n" +
               "            color: this.getConnectionColor(type)\n" +
               "        });\n" +
               "    }\n" +
               "    \n" +
               "    getSecurityColor(security) {\n" +
               "        switch(security?.level) {\n" +
               "            case 'HIGH': return '#E74C3C';\n" +
               "            case 'MEDIUM': return '#F39C12';\n" +
               "            case 'LOW': return '#2ECC71';\n" +
               "            default: return '#95A5A6';\n" +
               "        }\n" +
               "    }\n" +
               "    \n" +
               "    getConnectionColor(type) {\n" +
               "        switch(type) {\n" +
               "            case 'triggers-http': return '#FF6B35';\n" +
               "            case 'async-calls': return '#45B7D1';\n" +
               "            case 'secured-by': return '#E74C3C';\n" +
               "            default: return '#95A5A6';\n" +
               "        }\n" +
               "    }\n" +
               "    \n" +
               "    render() {\n" +
               "        if (this.container && typeof vis !== 'undefined') {\n" +
               "            const data = { nodes: this.nodes, edges: this.edges };\n" +
               "            const options = {\n" +
               "                physics: { enabled: true },\n" +
               "                interaction: { hover: true },\n" +
               "                nodes: { font: { size: 14 } }\n" +
               "            };\n" +
               "            new vis.Network(this.container, data, options);\n" +
               "        } else {\n" +
               "            // Fallback to simple text representation\n" +
               "            this.renderFallback();\n" +
               "        }\n" +
               "    }\n" +
               "    \n" +
               "    renderFallback() {\n" +
               "        if (this.container) {\n" +
               "            this.container.innerHTML = `\n" +
               "                <div class=\"graph-fallback\">\n" +
               "                    <h4>Graph Visualization</h4>\n" +
               "                    <p>Interactive graph requires vis.js library</p>\n" +
               "                    <div class=\"node-list\">\n" +
               "                        ${this.nodes.map(node => `<div class=\"node-item\">${node.label}</div>`).join('')}\n" +
               "                    </div>\n" +
               "                </div>\n" +
               "            `;\n" +
               "        }\n" +
               "    }\n" +
               "}";
    }
    
    private String getScheduledTasksScripts() {
        return "\n// Scheduled Tasks specific JavaScript\n" +
               "function showTaskDetails(taskId) {\n" +
               "    const modal = document.getElementById('task-details-modal');\n" +
               "    const content = document.getElementById('task-details-content');\n" +
               "    \n" +
               "    // Load task details (would be populated from server data)\n" +
               "    content.innerHTML = `<h3>Task: ${taskId}</h3><p>Loading details...</p>`;\n" +
               "    \n" +
               "    if (modal) {\n" +
               "        modal.style.display = 'block';\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "function closeTaskDetails() {\n" +
               "    const modal = document.getElementById('task-details-modal');\n" +
               "    if (modal) {\n" +
               "        modal.style.display = 'none';\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "// Task timeline visualization\n" +
               "function renderTaskTimeline() {\n" +
               "    const timelineContainer = document.getElementById('scheduling-timeline');\n" +
               "    if (timelineContainer) {\n" +
               "        // Simple timeline implementation\n" +
               "        timelineContainer.innerHTML = `\n" +
               "            <div class=\"timeline\">\n" +
               "                <div class=\"timeline-item\">\n" +
               "                    <div class=\"timeline-marker\"></div>\n" +
               "                    <div class=\"timeline-content\">\n" +
               "                        <h4>Next Scheduled Executions</h4>\n" +
               "                        <p>Timeline visualization would appear here</p>\n" +
               "                    </div>\n" +
               "                </div>\n" +
               "            </div>\n" +
               "        `;\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "document.addEventListener('DOMContentLoaded', renderTaskTimeline);";
    }
    
    private String getMessageListenersScripts() {
        return "\n// Message Listeners specific JavaScript\n" +
               "function showListenerType(type) {\n" +
               "    // Hide all listener contents\n" +
               "    document.querySelectorAll('.listener-content').forEach(content => {\n" +
               "        content.style.display = 'none';\n" +
               "    });\n" +
               "    \n" +
               "    // Remove active class from all tabs\n" +
               "    document.querySelectorAll('.tab-button').forEach(button => {\n" +
               "        button.classList.remove('active');\n" +
               "    });\n" +
               "    \n" +
               "    // Show selected listener type\n" +
               "    const selectedContent = document.getElementById(type + '-listeners');\n" +
               "    if (selectedContent) {\n" +
               "        selectedContent.style.display = 'block';\n" +
               "    }\n" +
               "    \n" +
               "    // Add active class to clicked tab\n" +
               "    event.target.classList.add('active');\n" +
               "}\n" +
               "\n" +
               "function renderMessagingTopology() {\n" +
               "    const topologyContainer = document.getElementById('messaging-topology-graph');\n" +
               "    if (topologyContainer) {\n" +
               "        // Simplified topology visualization\n" +
               "        topologyContainer.innerHTML = `\n" +
               "            <div class=\"topology-diagram\">\n" +
               "                <div class=\"topology-node producer\">Producers</div>\n" +
               "                <div class=\"topology-flow\">→</div>\n" +
               "                <div class=\"topology-node broker\">Message Broker</div>\n" +
               "                <div class=\"topology-flow\">→</div>\n" +
               "                <div class=\"topology-node consumer\">Consumers/Listeners</div>\n" +
               "            </div>\n" +
               "        `;\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "document.addEventListener('DOMContentLoaded', renderMessagingTopology);";
    }
    
    private String getEntryPointsScripts() {
        return "\n// Comprehensive Entry Points specific JavaScript\n" +
               "function toggleEntrypointCategory(categoryId) {\n" +
               "    const category = document.getElementById(categoryId);\n" +
               "    const toggleBtn = document.querySelector(`[data-toggle=\"${categoryId}\"]`);\n" +
               "    \n" +
               "    if (category && toggleBtn) {\n" +
               "        const isHidden = category.style.display === 'none';\n" +
               "        category.style.display = isHidden ? 'block' : 'none';\n" +
               "        toggleBtn.textContent = isHidden ? '▼' : '▶';\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "function searchEntrypoints(searchTerm) {\n" +
               "    const cards = document.querySelectorAll('.endpoint-card, .task-card, .listener-card, .event-card');\n" +
               "    const term = searchTerm.toLowerCase();\n" +
               "    \n" +
               "    cards.forEach(card => {\n" +
               "        const text = card.textContent.toLowerCase();\n" +
               "        const shouldShow = !term || text.includes(term);\n" +
               "        card.style.display = shouldShow ? 'block' : 'none';\n" +
               "    });\n" +
               "}\n" +
               "\n" +
               "// Initialize search functionality\n" +
               "document.addEventListener('DOMContentLoaded', function() {\n" +
               "    const searchInput = document.getElementById('entrypoint-search');\n" +
               "    if (searchInput) {\n" +
               "        searchInput.addEventListener('input', (e) => {\n" +
               "            searchEntrypoints(e.target.value);\n" +
               "        });\n" +
               "    }\n" +
               "});";
    }
}