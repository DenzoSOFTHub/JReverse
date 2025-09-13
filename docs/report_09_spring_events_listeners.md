# Report 09: Eventi e Listener Spring

## Descrizione
Analisi del sistema di eventi Spring (@EventListener, ApplicationEvents) per tracciare la comunicazione asincrona e i pattern event-driven nell'applicazione.

## Valore per l'Utente
**⭐⭐⭐** - Medio Valore
- Comprensione dei flussi asincroni
- Identificazione di pattern event-driven
- Troubleshooting di problemi di performance
- Analisi dell'architettura reattiva

## Complessità di Implementazione
**🟡 Media** - Analisi di event handling patterns

## Tempo di Realizzazione Stimato
**4-5 giorni** di sviluppo

## Implementazione Javassist

```java
public class SpringEventsAnalyzer {
    
    public EventAnalysis analyzeSpringEvents() {
        List<EventPublisher> publishers = findEventPublishers();
        List<EventListener> listeners = findEventListeners();
        Map<String, List<String>> eventFlow = mapEventFlow(publishers, listeners);
        
        return new EventAnalysis(publishers, listeners, eventFlow);
    }
    
    private List<EventListener> findEventListeners() {
        return classPool.getAllClasses().stream()
            .flatMap(clazz -> Arrays.stream(clazz.getDeclaredMethods()))
            .filter(method -> method.hasAnnotation("org.springframework.context.event.EventListener"))
            .map(this::createEventListenerInfo)
            .collect(Collectors.toList());
    }
}
```

## Implementazione Javassist Completa

```java
public class SpringEventsListenersAnalyzer {
    
    public SpringEventsReport analyzeEventsAndListeners(CtClass[] classes) {
        SpringEventsReport report = new SpringEventsReport();
        
        for (CtClass ctClass : classes) {
            analyzeEventComponents(ctClass, report);
        }
        
        analyzeEventFlow(report);
        identifyEventIssues(report);
        
        return report;
    }
    
    private void analyzeEventComponents(CtClass ctClass, SpringEventsReport report) {
        try {
            // Analizza Event Listeners
            analyzeEventListeners(ctClass, report);
            
            // Analizza Event Publishers  
            analyzeEventPublishers(ctClass, report);
            
            // Analizza Custom Events
            if (isEventClass(ctClass)) {
                analyzeCustomEvent(ctClass, report);
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi event components: " + e.getMessage());
        }
    }
    
    private void analyzeEventListeners(CtClass ctClass, SpringEventsReport report) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                if (method.hasAnnotation("org.springframework.context.event.EventListener")) {
                    analyzeEventListenerMethod(method, ctClass, report);
                }
                
                // Analizza anche @Async su listener
                if (method.hasAnnotation("org.springframework.scheduling.annotation.Async")) {
                    analyzeAsyncEventListener(method, ctClass, report);
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi event listeners: " + e.getMessage());
        }
    }
    
    private void analyzeEventListenerMethod(CtMethod method, CtClass ctClass, SpringEventsReport report) {
        try {
            EventListenerInfo listenerInfo = new EventListenerInfo();
            listenerInfo.setClassName(ctClass.getName());
            listenerInfo.setMethodName(method.getName());
            listenerInfo.setMethodSignature(method.getSignature());
            
            // Estrai parametri dell'annotazione @EventListener
            Annotation eventListener = method.getAnnotation("org.springframework.context.event.EventListener");
            
            Class<?>[] eventTypes = extractClassArrayAnnotationValue(eventListener, "value");
            if (eventTypes.length == 0) {
                // Se non specificato, deriva dal tipo parametro
                CtClass[] paramTypes = method.getParameterTypes();
                if (paramTypes.length > 0) {
                    listenerInfo.setEventType(paramTypes[0].getName());
                }
            } else {
                listenerInfo.setEventType(eventTypes[0].getName());
            }
            
            String condition = extractAnnotationValue(eventListener, "condition");
            if (condition != null) {
                listenerInfo.setCondition(condition);
            }
            
            // Verifica se è asincrono
            boolean isAsync = method.hasAnnotation("org.springframework.scheduling.annotation.Async");
            listenerInfo.setAsync(isAsync);
            
            // Verifica se ha @Order
            if (method.hasAnnotation("org.springframework.core.annotation.Order")) {
                Annotation order = method.getAnnotation("org.springframework.core.annotation.Order");
                int orderValue = extractIntAnnotationValue(order, "value");
                listenerInfo.setOrder(orderValue);
            }
            
            // Analizza il contenuto del listener
            analyzeListenerContent(method, listenerInfo, report);
            
            // Verifica potenziali problemi
            validateEventListener(method, listenerInfo, report);
            
            report.addEventListener(listenerInfo);
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi event listener method: " + e.getMessage());
        }
    }
    
    private void analyzeListenerContent(CtMethod method, EventListenerInfo listenerInfo, SpringEventsReport report) {
        try {
            method.instrument(new ExprEditor() {
                private boolean hasTransactionAnnotation = false;
                private boolean hasExceptionHandling = false;
                private boolean hasLogging = false;
                private List<String> externalCalls = new ArrayList<>();
                
                @Override
                public void edit(MethodCall call) throws CannotCompileException {
                    String className = call.getClassName();
                    String methodName = call.getMethodName();
                    
                    // Identifica chiamate a repository/database
                    if (isDatabaseCall(className, methodName)) {
                        listenerInfo.addDatabaseOperation(className + "." + methodName);
                    }
                    
                    // Identifica chiamate a servizi esterni
                    if (isExternalServiceCall(className, methodName)) {
                        externalCalls.add(className + "." + methodName);
                        listenerInfo.addExternalCall(className + "." + methodName);
                    }
                    
                    // Identifica logging
                    if (isLoggingCall(className, methodName)) {
                        hasLogging = true;
                    }
                    
                    // Identifica pubblicazione di altri eventi
                    if (isEventPublication(className, methodName)) {
                        listenerInfo.addEventPublication(className + "." + methodName);
                    }
                }
                
                @Override
                public void edit(Handler handler) throws CannotCompileException {
                    hasExceptionHandling = true;
                }
            });
            
            listenerInfo.setHasExceptionHandling(hasExceptionHandling);
            listenerInfo.setHasLogging(hasLogging);
            
            // Verifica se listener fa troppo lavoro
            if (listenerInfo.getDatabaseOperations().size() > 5) {
                EventIssue issue = new EventIssue();
                issue.setType(IssueType.HEAVY_EVENT_LISTENER);
                issue.setClassName(method.getDeclaringClass().getName());
                issue.setMethodName(method.getName());
                issue.setSeverity(Severity.MEDIUM);
                issue.setDescription("Event listener performs too many database operations");
                issue.setRecommendation("Consider using async processing or splitting into multiple listeners");
                
                report.addEventIssue(issue);
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi listener content: " + e.getMessage());
        }
    }
    
    private void analyzeEventPublishers(CtClass ctClass, SpringEventsReport report) {
        try {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            
            for (CtMethod method : methods) {
                method.instrument(new ExprEditor() {
                    @Override
                    public void edit(MethodCall call) throws CannotCompileException {
                        String className = call.getClassName();
                        String methodName = call.getMethodName();
                        
                        // Identifica pubblicazioni di eventi
                        if (isEventPublicationCall(className, methodName)) {
                            EventPublisherInfo publisherInfo = new EventPublisherInfo();
                            publisherInfo.setClassName(ctClass.getName());
                            publisherInfo.setMethodName(method.getName());
                            publisherInfo.setPublicationCall(className + "." + methodName);
                            
                            // Cerca di determinare tipo evento pubblicato
                            try {
                                String eventType = extractEventTypeFromCall(call);
                                publisherInfo.setEventType(eventType);
                            } catch (Exception e) {
                                publisherInfo.setEventType("Unknown");
                            }
                            
                            report.addEventPublisher(publisherInfo);
                        }
                    }
                });
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi event publishers: " + e.getMessage());
        }
    }
    
    private void analyzeCustomEvent(CtClass ctClass, SpringEventsReport report) {
        try {
            CustomEventInfo eventInfo = new CustomEventInfo();
            eventInfo.setClassName(ctClass.getName());
            
            // Verifica se estende ApplicationEvent
            CtClass superClass = ctClass.getSuperclass();
            boolean extendsApplicationEvent = false;
            
            while (superClass != null) {
                if ("org.springframework.context.ApplicationEvent".equals(superClass.getName())) {
                    extendsApplicationEvent = true;
                    break;
                }
                superClass = superClass.getSuperclass();
            }
            
            eventInfo.setExtendsApplicationEvent(extendsApplicationEvent);
            
            if (!extendsApplicationEvent) {
                // Evento POJO (Spring 4.2+)
                eventInfo.setPojoEvent(true);
            }
            
            // Analizza fields dell'evento
            CtField[] fields = ctClass.getDeclaredFields();
            for (CtField field : fields) {
                EventFieldInfo fieldInfo = new EventFieldInfo();
                fieldInfo.setFieldName(field.getName());
                fieldInfo.setFieldType(field.getType().getName());
                fieldInfo.setFinal(Modifier.isFinal(field.getModifiers()));
                
                eventInfo.addField(fieldInfo);
            }
            
            // Verifica immutabilità dell'evento
            boolean isImmutable = fields.length > 0 && 
                Arrays.stream(fields).allMatch(field -> Modifier.isFinal(field.getModifiers()));
            eventInfo.setImmutable(isImmutable);
            
            if (!isImmutable) {
                EventIssue issue = new EventIssue();
                issue.setType(IssueType.MUTABLE_EVENT);
                issue.setClassName(ctClass.getName());
                issue.setSeverity(Severity.LOW);
                issue.setDescription("Event class has mutable fields");
                issue.setRecommendation("Make event fields final for immutability");
                
                report.addEventIssue(issue);
            }
            
            report.addCustomEvent(eventInfo);
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi custom event: " + e.getMessage());
        }
    }
    
    private void validateEventListener(CtMethod method, EventListenerInfo listenerInfo, SpringEventsReport report) {
        try {
            // Verifica se listener sincrono fa operazioni lunghe
            if (!listenerInfo.isAsync() && hasLongRunningOperations(listenerInfo)) {
                EventIssue issue = new EventIssue();
                issue.setType(IssueType.SYNCHRONOUS_LONG_RUNNING_LISTENER);
                issue.setClassName(method.getDeclaringClass().getName());
                issue.setMethodName(method.getName());
                issue.setSeverity(Severity.HIGH);
                issue.setDescription("Synchronous event listener with long-running operations");
                issue.setRecommendation("Add @Async annotation for non-blocking processing");
                
                report.addEventIssue(issue);
            }
            
            // Verifica se listener non ha exception handling
            if (!listenerInfo.isHasExceptionHandling()) {
                EventIssue issue = new EventIssue();
                issue.setType(IssueType.NO_EXCEPTION_HANDLING);
                issue.setClassName(method.getDeclaringClass().getName());
                issue.setMethodName(method.getName());
                issue.setSeverity(Severity.MEDIUM);
                issue.setDescription("Event listener without exception handling");
                issue.setRecommendation("Add try-catch blocks to handle potential exceptions");
                
                report.addEventIssue(issue);
            }
            
            // Verifica se listener non ha logging
            if (!listenerInfo.isHasLogging()) {
                EventIssue issue = new EventIssue();
                issue.setType(IssueType.NO_LOGGING_IN_LISTENER);
                issue.setClassName(method.getDeclaringClass().getName());
                issue.setMethodName(method.getName());
                issue.setSeverity(Severity.LOW);
                issue.setDescription("Event listener without logging");
                issue.setRecommendation("Add logging for monitoring and debugging purposes");
                
                report.addEventIssue(issue);
            }
            
        } catch (Exception e) {
            report.addError("Errore nella validazione event listener: " + e.getMessage());
        }
    }
    
    private void analyzeEventFlow(SpringEventsReport report) {
        // Mappa il flusso eventi: Publisher -> Event -> Listeners
        Map<String, List<String>> eventFlow = new HashMap<>();
        
        for (EventPublisherInfo publisher : report.getEventPublishers()) {
            String eventType = publisher.getEventType();
            
            List<String> listenersForEvent = report.getEventListeners().stream()
                .filter(listener -> eventType.equals(listener.getEventType()))
                .map(listener -> listener.getClassName() + "." + listener.getMethodName())
                .collect(Collectors.toList());
                
            eventFlow.put(publisher.getClassName() + "." + publisher.getMethodName() + 
                         " -> " + eventType, listenersForEvent);
        }
        
        report.setEventFlow(eventFlow);
    }
}

public class SpringEventsReport {
    private List<EventListenerInfo> eventListeners = new ArrayList<>();
    private List<EventPublisherInfo> eventPublishers = new ArrayList<>();
    private List<CustomEventInfo> customEvents = new ArrayList<>();
    private List<EventIssue> eventIssues = new ArrayList<>();
    private Map<String, List<String>> eventFlow = new HashMap<>();
    private EventStatistics statistics;
    private List<String> errors = new ArrayList<>();
    
    public static class EventListenerInfo {
        private String className;
        private String methodName;
        private String methodSignature;
        private String eventType;
        private String condition;
        private boolean isAsync;
        private int order = Integer.MAX_VALUE;
        private boolean hasExceptionHandling;
        private boolean hasLogging;
        private List<String> databaseOperations = new ArrayList<>();
        private List<String> externalCalls = new ArrayList<>();
        private List<String> eventPublications = new ArrayList<>();
    }
    
    public static class EventPublisherInfo {
        private String className;
        private String methodName;
        private String eventType;
        private String publicationCall;
    }
    
    public static class CustomEventInfo {
        private String className;
        private boolean extendsApplicationEvent;
        private boolean pojoEvent;
        private boolean immutable;
        private List<EventFieldInfo> fields = new ArrayList<>();
    }
    
    public static class EventIssue {
        private IssueType type;
        private String className;
        private String methodName;
        private Severity severity;
        private String description;
        private String recommendation;
    }
    
    public enum IssueType {
        SYNCHRONOUS_LONG_RUNNING_LISTENER,
        HEAVY_EVENT_LISTENER,
        NO_EXCEPTION_HANDLING,
        NO_LOGGING_IN_LISTENER,
        MUTABLE_EVENT,
        UNUSED_EVENT_LISTENER,
        CIRCULAR_EVENT_DEPENDENCY
    }
    
    public enum Severity {
        CRITICAL, HIGH, MEDIUM, LOW
    }
}
```

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculateEventsListenersQualityScore(SpringEventsReport result) {
    double score = 100.0;
    
    // Penalizzazioni per problemi event handling critici
    score -= result.getSynchronousLongRunningListeners() * 18;    // -18 per listener sincroni con operazioni lunghe
    score -= result.getHeavyEventListeners() * 12;                // -12 per listener che fanno troppo lavoro
    score -= result.getListenersWithoutExceptionHandling() * 10;  // -10 per listener senza exception handling
    score -= result.getCircularEventDependencies() * 15;          // -15 per dipendenze circolari negli eventi
    score -= result.getMutableEvents() * 6;                       // -6 per eventi mutabili
    score -= result.getListenersWithoutLogging() * 4;             // -4 per listener senza logging
    score -= result.getUnusedEventListeners() * 8;                // -8 per listener non utilizzati
    
    // Bonus per buone pratiche event handling
    score += result.getAsyncEventListeners() * 3;                 // +3 per listener asincroni appropriati
    score += result.getWellStructuredEvents() * 2;                // +2 per eventi ben strutturati
    score += result.getOrderedEventListeners() * 1;               // +1 per listener con @Order appropriato
    score += result.getImmutableEvents() * 2;                     // +2 per eventi immutabili
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - Sistema eventi problematico con rischio stabilità
- **41-60**: 🟡 SUFFICIENTE - Event handling funzionante ma non ottimizzato
- **61-80**: 🟢 BUONO - Sistema eventi ben implementato con alcuni miglioramenti
- **81-100**: ⭐ ECCELLENTE - Event-driven architecture ottimale

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -15 to -18)
1. **Listener sincroni con operazioni lunghe**
   - Descrizione: @EventListener senza @Async che eseguono operazioni time-consuming
   - Rischio: Thread blocking, scalabilità compromessa, performance degradation
   - Soluzione: Aggiungere @Async per processing non-bloccante

2. **Dipendenze circolari negli eventi**
   - Descrizione: Eventi che si pubblicano a catena creando cicli
   - Rischio: Stack overflow, infinite loops, system instability
   - Soluzione: Ristrutturare event flow per eliminare cicli

### 🟠 GRAVITÀ ALTA (Score Impact: -10 to -12)  
3. **Heavy event listeners**
   - Descrizione: Listener che eseguono troppe operazioni database/external
   - Rischio: Performance bottlenecks, resource exhaustion
   - Soluzione: Scomporre in più listener o usare async processing

4. **Listener senza exception handling**
   - Descrizione: @EventListener methods senza try-catch appropriato
   - Rischio: Errori non gestiti, event processing failure
   - Soluzione: Implementare exception handling robusto

### 🟡 GRAVITÀ MEDIA (Score Impact: -6 to -8)
5. **Eventi mutabili**
   - Descrizione: Custom event classes con campi non-final
   - Rischio: Side effects, thread safety issues, data corruption
   - Soluzione: Rendere eventi immutabili con campi final

6. **Listener non utilizzati**
   - Descrizione: @EventListener che non ricevono mai eventi
   - Rischio: Codice morto, confusion nella architettura
   - Soluzione: Rimuovere listener inutilizzati o correggere event types

### 🔵 GRAVITÀ BASSA (Score Impact: -4)
7. **Listener senza logging**
   - Descrizione: Event listeners senza logging per monitoring
   - Rischio: Difficile debugging, monitoring limitato
   - Soluzione: Aggiungere logging appropriato per event processing

## Metriche di Valore

- **Async Processing**: Migliora scalabilità attraverso event-driven patterns
- **Loose Coupling**: Riduce dipendenze dirette tra componenti applicativi
- **System Observability**: Fornisce visibilità sui flussi asincroni
- **Error Resilience**: Identifica punti di fallimento nei flussi eventi

## Tags per Classificazione
`#spring-events` `#async-processing` `#event-driven` `#listeners` `#medium-complexity` `#medium-value`