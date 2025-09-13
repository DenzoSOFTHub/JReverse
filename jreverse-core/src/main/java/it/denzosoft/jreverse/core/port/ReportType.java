package it.denzosoft.jreverse.core.port;

/**
 * Enumeration of all supported report types.
 * Based on the 50 analysis types defined in requirements.
 */
public enum ReportType {
    
    // Entrypoint e Flussi Principali (1-10)
    REST_ENDPOINT_MAP(1, "REST Endpoint Map", "Mappa degli endpoint REST"),
    HTTP_CALL_GRAPH(2, "HTTP Call Graph", "Call graph delle richieste HTTP"),
    MAIN_METHOD_ANALYSIS(3, "Main Method Analysis", "Individuazione del main method"),
    BOOTSTRAP_ANALYSIS(4, "Bootstrap Analysis", "Analisi del ciclo di bootstrap Spring Boot"),
    AUTOWIRING_GRAPH(5, "Autowiring Graph", "Autowiring graph"),
    REST_CONTROLLER_MAP(6, "REST Controller Map", "Mappa dei controller REST"),
    SERVICE_LAYER_REPORT(7, "Service Layer Report", "Report dei service layer"),
    REPOSITORY_MAP(8, "Repository Map", "Mappa repository/database"),
    EVENT_LISTENER_ANALYSIS(9, "Event Listener Analysis", "Eventi e listener Spring"),
    ASYNC_CALL_SEQUENCES(10, "Async Call Sequences", "Sequenze di chiamata asincrone"),
    
    // Architettura e Dipendenze (11-20)
    PACKAGE_CLASS_MAP(11, "Package Class Map", "Mappa dei package e delle classi"),
    UML_CLASS_DIAGRAM(12, "UML Class Diagram", "Diagramma UML delle classi principali"),
    PACKAGE_DEPENDENCIES(13, "Package Dependencies", "Dipendenze tra package"),
    MODULE_DEPENDENCIES(14, "Module Dependencies", "Dipendenze tra moduli/librerie"),
    BEAN_CONFIGURATION_REPORT(15, "Bean Configuration Report", "Bean configuration report"),
    CONFIGURATION_PROPERTIES(16, "Configuration Properties", "Proprietà di configurazione"),
    SPRING_PROFILES(17, "Spring Profiles", "Profili Spring attivi"),
    CUSTOM_ANNOTATIONS_REPORT(18, "Custom Annotations Report", "Report delle annotazioni custom"),
    EXCEPTION_MAPPING(19, "Exception Mapping", "Mapping delle eccezioni gestite"),
    CIRCULAR_DEPENDENCY_ANALYSIS(20, "Circular Dependency Analysis", "Analisi delle dependency injection circolari"),
    
    // Persistenza e Database (21-30)
    JPA_ENTITY_MAP(21, "JPA Entity Map", "Mappa delle entità JPA"),
    ENTITY_RELATIONSHIPS(22, "Entity Relationships", "Relazioni tra entità"),
    DATABASE_SCHEMA(23, "Database Schema", "Schema DB ricostruito dalle entità"),
    QUERY_ANALYSIS(24, "Query Analysis", "Query native e JPQL usate"),
    REPOSITORY_METHODS(25, "Repository Methods", "Analisi metodi custom nei repository Spring Data"),
    CACHE_ANALYSIS(26, "Cache Analysis", "Report su cache"),
    TRANSACTION_ANALYSIS(27, "Transaction Analysis", "Uso delle transazioni"),
    DATASOURCE_ANALYSIS(28, "Datasource Analysis", "Analisi dei datasource configurati"),
    DATABASE_MIGRATIONS(29, "Database Migrations", "Mappa delle migrazioni DB"),
    QUERY_OPTIMIZATION(30, "Query Optimization", "Ottimizzazione query"),
    
    // Metriche, Performance, Qualità del Codice (31-40)
    CYCLOMATIC_COMPLEXITY(31, "Cyclomatic Complexity", "Metriche di complessità ciclomatica"),
    CODE_SIZE_METRICS(32, "Code Size Metrics", "Lunghezza media delle classi e dei metodi"),
    CODE_SMELLS(33, "Code Smells", "Code smells"),
    COHESION_COUPLING(34, "Cohesion Coupling", "Analisi di coesione e accoppiamento"),
    TEST_COVERAGE_POTENTIAL(35, "Test Coverage Potential", "Coverage potenziale dei test"),
    LOGGING_ANALYSIS(36, "Logging Analysis", "Uso di log e livello logging"),
    PERFORMANCE_HOTSPOTS(37, "Performance Hotspots", "Punti critici di performance"),
    DEAD_CODE_ANALYSIS(38, "Dead Code Analysis", "Identificazione di dead code"),
    REFLECTION_ANALYSIS(39, "Reflection Analysis", "Analisi della reflection"),
    THREAD_ANALYSIS(40, "Thread Analysis", "Individuazione di thread creation diretta"),
    
    // Sicurezza e Robustezza (41-50)
    SPRING_SECURITY_CONFIG(41, "Spring Security Config", "Mappa delle configurazioni Spring Security"),
    UNPROTECTED_ENDPOINTS(42, "Unprotected Endpoints", "Endpoint non protetti o pubblici"),
    SECURITY_ANNOTATIONS(43, "Security Annotations", "Uso di @PreAuthorize, @Secured"),
    VULNERABILITY_ANALYSIS(44, "Vulnerability Analysis", "Analisi di dipendenze vulnerabili"),
    CRYPTOGRAPHY_USAGE(45, "Cryptography Usage", "Uso di crittografia e gestione password"),
    INPUT_VALIDATION(46, "Input Validation", "Gestione input non validati"),
    EXCEPTION_HANDLING(47, "Exception Handling", "Gestione eccezioni runtime non catturate"),
    DANGEROUS_CONFIGURATIONS(48, "Dangerous Configurations", "Configurazioni pericolose"),
    STACK_TRACE_EXPOSURE(49, "Stack Trace Exposure", "Analisi di esposizione di stack trace"),
    EMBEDDED_SERVER_ANALYSIS(50, "Embedded Server Analysis", "Controllo delle librerie embedded"),
    
    // Entrypoint Analysis Extensions (51-55)
    SCHEDULED_TASKS_ANALYSIS(51, "Scheduled Tasks Analysis", "Analisi completa delle operazioni scheduling"),
    ASYNC_PROCESSING_ANALYSIS(52, "Async Processing Analysis", "Analisi dei flussi asincroni e thread safety"),
    MESSAGING_INTEGRATION_ANALYSIS(53, "Messaging Integration Analysis", "Analisi dei pattern JMS, Kafka, RabbitMQ"),
    EVENT_DRIVEN_ANALYSIS(54, "Event-Driven Architecture Analysis", "Analisi dei pattern @EventListener"),
    SECURITY_ENTRYPOINT_MATRIX(55, "Security Entrypoint Matrix", "Matrice completa della sicurezza degli entrypoint");
    
    private final int id;
    private final String displayName;
    private final String description;
    
    ReportType(int id, String displayName, String description) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }
    
    public int getId() {
        return id;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getCategory() {
        if (id <= 10) return "Entrypoint e Flussi Principali";
        if (id <= 20) return "Architettura e Dipendenze";
        if (id <= 30) return "Persistenza e Database";
        if (id <= 40) return "Metriche, Performance, Qualità del Codice";
        if (id <= 50) return "Sicurezza e Robustezza";
        return "Entrypoint Analysis Extensions";
    }
    
    public boolean isEntrypointAnalysis() {
        return id <= 10 || (id >= 51 && id <= 55);
    }
    
    public boolean isArchitectureAnalysis() {
        return id > 10 && id <= 20;
    }
    
    public boolean isPersistenceAnalysis() {
        return id > 20 && id <= 30;
    }
    
    public boolean isQualityAnalysis() {
        return id > 30 && id <= 40;
    }
    
    public boolean isSecurityAnalysis() {
        return (id > 40 && id <= 50) || id == 55;
    }
    
    public boolean isExtendedEntrypointAnalysis() {
        return id >= 51 && id <= 55;
    }
    
    public boolean requiresNewAnalyzers() {
        return id >= 51 && id <= 55;
    }
}