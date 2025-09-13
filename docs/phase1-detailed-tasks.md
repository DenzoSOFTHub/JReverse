# Fase 1: Foundation & Core Infrastructure - Task Dettagliati

## T1.1.1: Setup Progetto Maven/Gradle con Dependency Javassist

### Descrizione Dettagliata
Inizializzazione del progetto Java con build tool (Maven preferito per semplicità dependency management) e configurazione della dipendenza Javassist. Setup della struttura base del progetto seguendo convenzioni standard.

### Scopo dell'Attività
- Stabilire foundation tecnologica del progetto
- Configurare dependency management centralizzato
- Preparare ambiente per sviluppo multi-modulo

### Impatti su Altri Moduli
- **Tutti i moduli**: Definisce versioni librerie e Java target
- **Build Pipeline**: Base per packaging e distribuzione
- **Testing**: Setup framework di test (JUnit 5)

### Componenti da Implementare (Clean Architecture)

#### 1. Project Structure (Multi-Module)
```
jreverse/
├── jreverse-core/           # Domain layer
├── jreverse-analyzer/       # Application layer  
├── jreverse-ui/            # Interface adapter layer
├── jreverse-reporter/      # Interface adapter layer
└── jreverse-app/           # Main application assembly
```

#### 2. Build Configuration (Maven)
- **Parent POM**: Centralizza versioni e configuration
- **Module POMs**: Specifici per ogni layer
- **Properties**: Java version (11+), encoding, plugin versions

### Principi SOLID Applicati
- **SRP**: Ogni modulo ha responsabilità specifica
- **OCP**: Struttura estendibile per nuovi analyzer
- **DIP**: Dipendenze verso astrazioni, non implementazioni

### Test Unitari da Implementare
```java
// BuildConfigurationTest.java
@Test
void shouldLoadJavassistDependency() {
    // Verifica che Javassist sia disponibile nel classpath
    assertDoesNotThrow(() -> Class.forName("javassist.ClassPool"));
}

@Test
void shouldHaveCorrectJavaVersion() {
    // Verifica versione Java target
    assertTrue(System.getProperty("java.version").startsWith("11") ||
               System.getProperty("java.version").startsWith("17"));
}
```

---

## T1.1.2: Definizione Architettura Modulare

### Descrizione Dettagliata
Progettazione dell'architettura del sistema seguendo Clean Architecture principles con separazione chiara tra layer. Definizione dei package principali e delle interfacce tra componenti.

### Scopo dell'Attività
- Garantire manutenibilità e testabilità del codice
- Permettere estensibilità futura del sistema
- Stabilire boundaries chiari tra responsabilità

### Impatti su Altri Moduli
- **Tutti**: Definisce contratti e interfacce base
- **Future Extensions**: Facilita aggiunta nuovi analyzer
- **Testing**: Permette mocking efficace delle dipendenze

### Componenti da Implementare

#### 1. Domain Layer (jreverse-core)
```java
// Entities
public class ClassInfo {
    private String fullyQualifiedName;
    private Set<MethodInfo> methods;
    private Set<FieldInfo> fields;
    private Set<AnnotationInfo> annotations;
}

public class MethodInfo {
    private String name;
    private String returnType;
    private List<ParameterInfo> parameters;
    private Set<AnnotationInfo> annotations;
}

// Value Objects
public class JarLocation {
    private final Path jarPath;
    // Immutable, validation in constructor
}
```

#### 2. Use Case Interfaces (Application Layer)
```java
public interface AnalyzeJarUseCase {
    AnalysisResult execute(AnalysisRequest request);
}

public interface GenerateReportUseCase {
    ReportResult execute(ReportRequest request);
}
```

#### 3. Port Interfaces (Clean Architecture)
```java
// Output Ports
public interface JarAnalyzerPort {
    Set<ClassInfo> analyzeJar(JarLocation location);
}

public interface ReportGeneratorPort {
    void generateReport(ReportData data, ReportFormat format);
}

// Input Ports  
public interface JarLoaderPort {
    JarContent loadJar(JarLocation location);
}
```

### Principi SOLID Applicati
- **SRP**: Ogni interfaccia ha una responsabilità specifica
- **ISP**: Interfacce piccole e coese
- **DIP**: Use cases dipendono da astrazioni, non implementazioni

### Test Unitari da Implementare
```java
// ArchitectureTest.java (ArchUnit)
@Test
void domainLayerShouldNotDependOnOtherLayers() {
    JavaClasses classes = new ClassFileImporter().importPackages("com.jreverse.core");
    
    noClasses().that().resideInAPackage("..core..")
        .should().dependOnClassesThat().resideInAnyPackage("..analyzer..", "..ui..", "..reporter..")
        .check(classes);
}

@Test
void useCasesShouldOnlyDependOnDomainAndPorts() {
    // Verifica dipendenze use cases
}
```

---

## T1.1.3: Implementazione Pattern Base

### Descrizione Dettagliata
Implementazione dei design pattern fondamentali: Factory per creazione analyzer, Strategy per diversi tipi di report, Observer per progress notification, Builder per configurazioni complesse.

### Scopo dell'Attività
- Garantire estensibilità del sistema
- Ridurre accoppiamento tra componenti
- Facilitare testing e mocking

### Impatti su Altri Moduli
- **Analyzer Module**: Usa Factory pattern per instantiation
- **Reporter Module**: Implementa Strategy pattern per formati
- **UI Module**: Riceve notifiche via Observer pattern

### Componenti da Implementare

#### 1. Factory Pattern (Analyzer Creation)
```java
public interface AnalyzerFactory {
    ClassAnalyzer createClassAnalyzer();
    MethodAnalyzer createMethodAnalyzer();
    AnnotationAnalyzer createAnnotationAnalyzer();
}

public class JavassistAnalyzerFactory implements AnalyzerFactory {
    @Override
    public ClassAnalyzer createClassAnalyzer() {
        return new JavassistClassAnalyzer(createClassPool());
    }
    
    private ClassPool createClassPool() {
        // Setup ClassPool con configurazioni appropriate
    }
}
```

#### 2. Strategy Pattern (Report Generation)
```java
public interface ReportStrategy {
    void generateReport(AnalysisResult result, OutputStream output);
    String getSupportedFormat();
}

public class HtmlReportStrategy implements ReportStrategy {
    @Override
    public void generateReport(AnalysisResult result, OutputStream output) {
        // Implementation per HTML generation
    }
}

public class ReportContext {
    private ReportStrategy strategy;
    
    public void setStrategy(ReportStrategy strategy) {
        this.strategy = strategy;
    }
    
    public void executeReport(AnalysisResult result, OutputStream output) {
        strategy.generateReport(result, output);
    }
}
```

#### 3. Observer Pattern (Progress Notification)
```java
public interface ProgressObserver {
    void onProgressUpdate(ProgressEvent event);
    void onAnalysisComplete(AnalysisResult result);
    void onError(AnalysisError error);
}

public class AnalysisProgressNotifier {
    private List<ProgressObserver> observers = new ArrayList<>();
    
    public void addObserver(ProgressObserver observer) {
        observers.add(observer);
    }
    
    public void notifyProgress(int percentage, String currentTask) {
        ProgressEvent event = new ProgressEvent(percentage, currentTask);
        observers.forEach(observer -> observer.onProgressUpdate(event));
    }
}
```

### Principi SOLID/Clean Code Applicati
- **OCP**: Nuovi analyzer/reporter senza modificare codice esistente
- **LSP**: Implementazioni intercambiabili via interfacce
- **DRY**: Pattern riutilizzabili, no code duplication

### Test Unitari da Implementare
```java
// AnalyzerFactoryTest.java
@Test
void shouldCreateCorrectAnalyzerTypes() {
    AnalyzerFactory factory = new JavassistAnalyzerFactory();
    
    ClassAnalyzer classAnalyzer = factory.createClassAnalyzer();
    assertThat(classAnalyzer).isInstanceOf(JavassistClassAnalyzer.class);
}

// ReportStrategyTest.java
@Test
void shouldGenerateHtmlReport() {
    ReportStrategy strategy = new HtmlReportStrategy();
    AnalysisResult mockResult = createMockAnalysisResult();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    
    strategy.generateReport(mockResult, output);
    
    String html = output.toString();
    assertThat(html).contains("<!DOCTYPE html>");
    assertThat(html).contains("<html>");
}

// ProgressObserverTest.java
@Test
void shouldNotifyAllObservers() {
    AnalysisProgressNotifier notifier = new AnalysisProgressNotifier();
    ProgressObserver observer1 = mock(ProgressObserver.class);
    ProgressObserver observer2 = mock(ProgressObserver.class);
    
    notifier.addObserver(observer1);
    notifier.addObserver(observer2);
    notifier.notifyProgress(50, "Analyzing classes");
    
    verify(observer1).onProgressUpdate(any(ProgressEvent.class));
    verify(observer2).onProgressUpdate(any(ProgressEvent.class));
}
```

---

## T1.1.4: Setup Logging Framework

### Descrizione Dettagliata
Configurazione del sistema di logging utilizzando java.util.logging con configurazione centralizzata, diversi appender (console, file) e livelli appropriati per debugging e produzione.

### Scopo dell'Attività
- Facilitare debugging durante sviluppo
- Fornire audit trail per operazioni di analisi
- Gestire errori in modo tracciabile

### Impatti su Altri Moduli
- **Tutti i moduli**: Logging centralizzato e configurabile
- **Error Handling**: Tracciamento errori dettagliato
- **Performance Monitoring**: Tempi di elaborazione

### Componenti da Implementare

#### 1. Logging Configuration
```java
public class LoggingConfiguration {
    private static final Logger ROOT_LOGGER = Logger.getLogger("");
    
    public static void configureLogging(LogLevel level, boolean enableFileLogging) {
        ROOT_LOGGER.setLevel(convertToJavaLevel(level));
        
        // Console Handler
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new CustomFormatter());
        ROOT_LOGGER.addHandler(consoleHandler);
        
        // File Handler (optional)
        if (enableFileLogging) {
            try {
                FileHandler fileHandler = new FileHandler("jreverse.log", true);
                fileHandler.setFormatter(new CustomFormatter());
                ROOT_LOGGER.addHandler(fileHandler);
            } catch (IOException e) {
                ROOT_LOGGER.severe("Cannot create file handler: " + e.getMessage());
            }
        }
    }
}
```

#### 2. Custom Formatter
```java
public class CustomFormatter extends Formatter {
    private static final String FORMAT = "[%1$tF %1$tT] [%2$s] %3$s: %4$s%n";
    
    @Override
    public String format(LogRecord record) {
        return String.format(FORMAT,
            new Date(record.getMillis()),
            record.getLevel(),
            record.getLoggerName(),
            record.getMessage());
    }
}
```

#### 3. Logging Utility Class
```java
public class JReverseLogger {
    private final Logger logger;
    
    private JReverseLogger(String name) {
        this.logger = Logger.getLogger(name);
    }
    
    public static JReverseLogger getLogger(Class<?> clazz) {
        return new JReverseLogger(clazz.getName());
    }
    
    public void info(String message, Object... params) {
        logger.info(String.format(message, params));
    }
    
    public void debug(String message, Object... params) {
        logger.fine(String.format(message, params));
    }
    
    public void error(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }
    
    public void startOperation(String operation) {
        logger.info("Starting operation: " + operation);
    }
    
    public void endOperation(String operation, long durationMs) {
        logger.info("Completed operation: " + operation + " in " + durationMs + "ms");
    }
}
```

### Principi Clean Code Applicati
- **SRP**: Separate formatter, configuration, utility
- **DRY**: Centralized logging configuration
- **Readability**: Clear, structured log messages

### Test Unitari da Implementare
```java
// LoggingConfigurationTest.java
@Test
void shouldConfigureConsoleLogging() {
    LoggingConfiguration.configureLogging(LogLevel.INFO, false);
    
    Logger rootLogger = Logger.getLogger("");
    assertThat(rootLogger.getLevel()).isEqualTo(Level.INFO);
    assertThat(rootLogger.getHandlers()).hasSize(1);
    assertThat(rootLogger.getHandlers()[0]).isInstanceOf(ConsoleHandler.class);
}

@Test
void shouldConfigureFileLogging() throws IOException {
    Path tempFile = Files.createTempFile("test-log", ".log");
    tempFile.toFile().deleteOnExit();
    
    LoggingConfiguration.configureLogging(LogLevel.DEBUG, true);
    
    Logger rootLogger = Logger.getLogger("");
    boolean hasFileHandler = Arrays.stream(rootLogger.getHandlers())
        .anyMatch(handler -> handler instanceof FileHandler);
    assertThat(hasFileHandler).isTrue();
}

// JReverseLoggerTest.java
@Test
void shouldFormatLogMessages() {
    JReverseLogger logger = JReverseLogger.getLogger(getClass());
    
    // Capture log output per verification
    TestLogHandler testHandler = new TestLogHandler();
    Logger.getLogger(getClass().getName()).addHandler(testHandler);
    
    logger.info("Test message with param: %s", "value");
    
    List<LogRecord> records = testHandler.getRecords();
    assertThat(records).hasSize(1);
    assertThat(records.get(0).getMessage()).isEqualTo("Test message with param: value");
}
```

---

## T1.1.5: Configurazione Build con JAR Eseguibile

### Descrizione Dettagliata
Setup del processo di build Maven per generare JAR eseguibile con tutte le dipendenze (fat JAR), configurazione del manifest principale, e setup per distribuzione.

### Scopo dell'Attività
- Facilitare distribuzione dell'applicazione
- Garantire portabilità su diverse piattaforme
- Semplificare deployment per end user

### Impatti su Altri Moduli
- **Distribution**: Package finale dell'applicazione
- **Dependencies**: Gestione transitive dependencies
- **Testing**: JAR eseguibile per integration test

### Componenti da Implementare

#### 1. Maven Assembly Configuration
```xml
<!-- pom.xml in jreverse-app module -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-assembly-plugin</artifactId>
    <version>3.3.0</version>
    <configuration>
        <descriptorRefs>
            <descriptorRef>jar-with-dependencies</descriptorRef>
        </descriptorRefs>
        <archive>
            <manifest>
                <mainClass>com.jreverse.app.JReverseApplication</mainClass>
                <addDefaultImplementationEntries>true</addDefaultImplementationEntries>
                <addDefaultSpecificationEntries>true</addDefaultSpecificationEntries>
            </manifest>
            <manifestEntries>
                <Built-By>JReverse Build System</Built-By>
                <Build-Timestamp>${maven.build.timestamp}</Build-Timestamp>
            </manifestEntries>
        </archive>
    </configuration>
    <executions>
        <execution>
            <id>make-assembly</id>
            <phase>package</phase>
            <goals>
                <goal>single</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

#### 2. Application Main Class
```java
public class JReverseApplication {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JReverseApplication.class);
    
    public static void main(String[] args) {
        try {
            // Initialize logging
            LoggingConfiguration.configureLogging(getLogLevel(args), shouldLogToFile(args));
            
            LOGGER.info("Starting JReverse Application v%s", getVersion());
            
            // Check Java version compatibility
            validateJavaVersion();
            
            // Initialize application context
            ApplicationContext context = new ApplicationContext();
            
            // Launch UI
            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
                    new MainWindow(context).setVisible(true);
                } catch (Exception e) {
                    LOGGER.error("Failed to start UI", e);
                    showErrorDialog("Failed to start application: " + e.getMessage());
                    System.exit(1);
                }
            });
            
        } catch (Exception e) {
            LOGGER.error("Failed to start application", e);
            System.exit(1);
        }
    }
    
    private static void validateJavaVersion() {
        String version = System.getProperty("java.version");
        if (!isJavaVersionSupported(version)) {
            throw new RuntimeException("Java 11 or higher is required. Current version: " + version);
        }
    }
    
    private static String getVersion() {
        Package pkg = JReverseApplication.class.getPackage();
        return pkg.getImplementationVersion() != null ? 
               pkg.getImplementationVersion() : "development";
    }
}
```

#### 3. Build Validation Scripts
```java
public class BuildValidator {
    public static void main(String[] args) {
        validateJarStructure();
        validateMainClassExecution();
        validateDependencies();
    }
    
    private static void validateJarStructure() {
        // Verifica che il JAR contenga tutte le dipendenze necessarie
        try (JarFile jarFile = new JarFile("jreverse-app-jar-with-dependencies.jar")) {
            assertJarContainsEntry(jarFile, "javassist/");
            assertJarContainsEntry(jarFile, "com/jreverse/");
            assertJarContainsEntry(jarFile, "META-INF/MANIFEST.MF");
        }
    }
    
    private static void validateMainClassExecution() {
        // Test che il JAR si avvii correttamente
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", "jreverse-app-jar-with-dependencies.jar", "--version");
        // Execute and verify output
    }
}
```

### Principi Clean Code Applicati
- **SRP**: Separate build, validation, main application concerns
- **Fail Fast**: Early validation of environment requirements
- **Error Handling**: Graceful degradation and user feedback

### Test Unitari da Implementare
```java
// ApplicationStartupTest.java
@Test
void shouldStartApplicationWithValidJavaVersion() {
    // Mock system properties
    System.setProperty("java.version", "11.0.1");
    
    assertDoesNotThrow(() -> JReverseApplication.validateJavaVersion());
}

@Test
void shouldFailWithInvalidJavaVersion() {
    System.setProperty("java.version", "8.0.1");
    
    assertThrows(RuntimeException.class, () -> JReverseApplication.validateJavaVersion());
}

// BuildValidationTest.java
@Test
void shouldHaveCorrectMainClassInManifest() throws IOException {
    JarFile jarFile = new JarFile("target/jreverse-app-jar-with-dependencies.jar");
    Manifest manifest = jarFile.getManifest();
    
    String mainClass = manifest.getMainAttributes().getValue("Main-Class");
    assertThat(mainClass).isEqualTo("com.jreverse.app.JReverseApplication");
}

@Test
void shouldContainAllRequiredDependencies() throws IOException {
    JarFile jarFile = new JarFile("target/jreverse-app-jar-with-dependencies.jar");
    
    boolean hasJavassist = jarFile.stream()
        .anyMatch(entry -> entry.getName().startsWith("javassist/"));
    assertThat(hasJavassist).isTrue();
}
```

---

## Test Coverage e Quality Gates

### Coverage Requirements
- **Minimum Line Coverage**: 80%
- **Minimum Branch Coverage**: 75%
- **Critical Components Coverage**: 95% (Factory, Strategy implementations)

### Quality Gates
```xml
<!-- Surefire configuration for test execution -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <includes>
            <include>**/*Test.java</include>
            <include>**/*Tests.java</include>
        </includes>
        <excludes>
            <exclude>**/*IntegrationTest.java</exclude>
        </excludes>
    </configuration>
</plugin>

<!-- JaCoCo for coverage -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <rules>
            <rule>
                <element>BUNDLE</element>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.80</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</plugin>
```

### Integration Test Strategy
```java
// Phase1IntegrationTest.java
@Test
void shouldBootstrapApplicationSuccessfully() {
    // Test completo startup sequence
    ApplicationContext context = new ApplicationContext();
    assertThat(context.getAnalyzerFactory()).isNotNull();
    assertThat(context.getReportStrategies()).isNotEmpty();
}

@Test
void shouldHandleInvalidJarGracefully() {
    // Test error handling per JAR corrupts
    Path invalidJar = createInvalidJarFile();
    
    assertThrows(JarAnalysisException.class, () -> {
        new JarLoader().loadJar(new JarLocation(invalidJar));
    });
}
```