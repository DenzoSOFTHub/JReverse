# Report 21: Mappa delle Entità JPA

## Descrizione
Catalogazione completa di tutte le entità JPA (@Entity) dell'applicazione con analisi dettagliata degli attributi, annotazioni di mapping, strategie di generazione ID, e configurazioni di persistenza.

## Valore per l'Utente
**⭐⭐⭐⭐⭐** - Valore Massimo
- Comprensione del modello di dati
- Documentazione automatica del domain model
- Identificazione di problemi di design del database
- Base per generazione automatica di DDL
- Analisi dell'architettura di persistenza

## Complessità di Implementazione
**🟡 Media** - Analisi di annotazioni JPA e Hibernate

## Tempo di Realizzazione Stimato
**6-8 giorni** di sviluppo

## Implementazione Javassist

```java
public class JpaEntityAnalyzer {
    
    private final ClassPool classPool;
    private final Map<String, EntityInfo> entities = new HashMap<>();
    
    public EntityAnalysis analyzeJpaEntities() {
        // 1. Trova tutte le entità JPA
        discoverJpaEntities();
        
        // 2. Analizza ogni entità in dettaglio
        for (EntityInfo entity : entities.values()) {
            analyzeEntityDetails(entity);
        }
        
        // 3. Mappa le relazioni tra entità
        mapEntityRelationships();
        
        // 4. Identifica pattern e best practices
        analyzeEntityPatterns();
        
        return new EntityAnalysis(entities);
    }
    
    private void discoverJpaEntities() {
        for (CtClass clazz : classPool.getAllClasses()) {
            if (isJpaEntity(clazz)) {
                EntityInfo entityInfo = createEntityInfo(clazz);
                entities.put(clazz.getName(), entityInfo);
            }
        }
    }
    
    private boolean isJpaEntity(CtClass clazz) {
        return clazz.hasAnnotation("javax.persistence.Entity") ||
               clazz.hasAnnotation("jakarta.persistence.Entity");
    }
    
    private EntityInfo createEntityInfo(CtClass clazz) {
        EntityInfo info = new EntityInfo();
        info.setClassName(clazz.getName());
        info.setSimpleName(clazz.getSimpleName());
        
        // Analizza @Entity
        analyzeEntityAnnotation(clazz, info);
        
        // Analizza @Table
        analyzeTableAnnotation(clazz, info);
        
        // Analizza inheritance strategy
        analyzeInheritanceStrategy(clazz, info);
        
        return info;
    }
    
    private void analyzeEntityAnnotation(CtClass clazz, EntityInfo info) {
        try {
            if (clazz.hasAnnotation("javax.persistence.Entity")) {
                Annotation entityAnnotation = clazz.getAnnotation("javax.persistence.Entity");
                String entityName = getAnnotationValue(entityAnnotation, "name");
                
                if (entityName != null && !entityName.isEmpty()) {
                    info.setEntityName(entityName);
                } else {
                    // Default entity name is the unqualified class name
                    info.setEntityName(clazz.getSimpleName());
                }
            }
        } catch (ClassNotFoundException e) {
            logger.warn("Error analyzing @Entity annotation for {}", clazz.getName());
        }
    }
    
    private void analyzeTableAnnotation(CtClass clazz, EntityInfo info) {
        try {
            if (clazz.hasAnnotation("javax.persistence.Table")) {
                Annotation tableAnnotation = clazz.getAnnotation("javax.persistence.Table");
                
                String tableName = getAnnotationValue(tableAnnotation, "name");
                String schema = getAnnotationValue(tableAnnotation, "schema");
                String catalog = getAnnotationValue(tableAnnotation, "catalog");
                
                TableInfo tableInfo = new TableInfo();
                tableInfo.setTableName(tableName != null ? tableName : info.getEntityName());
                tableInfo.setSchema(schema);
                tableInfo.setCatalog(catalog);
                
                // Analizza unique constraints
                analyzeUniqueConstraints(tableAnnotation, tableInfo);
                
                // Analizza indexes
                analyzeTableIndexes(tableAnnotation, tableInfo);
                
                info.setTableInfo(tableInfo);
            } else {
                // Default table name is entity name
                TableInfo tableInfo = new TableInfo();
                tableInfo.setTableName(info.getEntityName());
                info.setTableInfo(tableInfo);
            }
        } catch (ClassNotFoundException e) {
            logger.warn("Error analyzing @Table annotation for {}", clazz.getName());
        }
    }
    
    private void analyzeEntityDetails(EntityInfo entityInfo) {
        try {
            CtClass entityClass = classPool.get(entityInfo.getClassName());
            
            // Analizza tutti i campi
            for (CtField field : entityClass.getDeclaredFields()) {
                if (isPersistentField(field)) {
                    FieldInfo fieldInfo = analyzeEntityField(field);
                    entityInfo.addField(fieldInfo);
                }
            }
            
            // Analizza metodi getter per property-based access
            for (CtMethod method : entityClass.getDeclaredMethods()) {
                if (isPersistentProperty(method)) {
                    PropertyInfo propInfo = analyzeEntityProperty(method);
                    entityInfo.addProperty(propInfo);
                }
            }
            
            // Identifica la primary key
            identifyPrimaryKey(entityInfo);
            
            // Analizza lifecycle callbacks
            analyzeLifecycleCallbacks(entityClass, entityInfo);
            
        } catch (NotFoundException e) {
            logger.error("Entity class not found: {}", entityInfo.getClassName());
        }
    }
    
    private FieldInfo analyzeEntityField(CtField field) {
        FieldInfo info = new FieldInfo();
        info.setFieldName(field.getName());
        info.setFieldType(field.getType().getName());
        info.setModifiers(field.getModifiers());
        
        // @Id annotation
        if (field.hasAnnotation("javax.persistence.Id")) {
            info.setPrimaryKey(true);
            analyzeIdGeneration(field, info);
        }
        
        // @Column annotation
        if (field.hasAnnotation("javax.persistence.Column")) {
            analyzeColumnMapping(field, info);
        }
        
        // @JoinColumn annotation (for relationships)
        if (field.hasAnnotation("javax.persistence.JoinColumn")) {
            analyzeJoinColumn(field, info);
        }
        
        // Relationship annotations
        analyzeRelationshipAnnotations(field, info);
        
        // Validation annotations
        analyzeValidationAnnotations(field, info);
        
        // @Temporal for date/time fields
        if (field.hasAnnotation("javax.persistence.Temporal")) {
            analyzeTemporalAnnotation(field, info);
        }
        
        // @Enumerated for enum fields
        if (field.hasAnnotation("javax.persistence.Enumerated")) {
            analyzeEnumeratedAnnotation(field, info);
        }
        
        // @Lob for large objects
        if (field.hasAnnotation("javax.persistence.Lob")) {
            info.setLargeObject(true);
        }
        
        return info;
    }
    
    private void analyzeColumnMapping(CtField field, FieldInfo info) {
        try {
            Annotation columnAnnotation = field.getAnnotation("javax.persistence.Column");
            
            ColumnInfo columnInfo = new ColumnInfo();
            columnInfo.setColumnName(getAnnotationValue(columnAnnotation, "name", field.getName()));
            columnInfo.setNullable(getAnnotationBooleanValue(columnAnnotation, "nullable", true));
            columnInfo.setUnique(getAnnotationBooleanValue(columnAnnotation, "unique", false));
            columnInfo.setLength(getAnnotationIntValue(columnAnnotation, "length", 255));
            columnInfo.setPrecision(getAnnotationIntValue(columnAnnotation, "precision", 0));
            columnInfo.setScale(getAnnotationIntValue(columnAnnotation, "scale", 0));
            columnInfo.setInsertable(getAnnotationBooleanValue(columnAnnotation, "insertable", true));
            columnInfo.setUpdatable(getAnnotationBooleanValue(columnAnnotation, "updatable", true));
            
            String columnDefinition = getAnnotationValue(columnAnnotation, "columnDefinition");
            if (columnDefinition != null && !columnDefinition.isEmpty()) {
                columnInfo.setColumnDefinition(columnDefinition);
            }
            
            info.setColumnInfo(columnInfo);
            
        } catch (ClassNotFoundException e) {
            logger.warn("Error analyzing @Column annotation for field {}", field.getName());
        }
    }
    
    private void analyzeRelationshipAnnotations(CtField field, FieldInfo info) {
        // @OneToOne
        if (field.hasAnnotation("javax.persistence.OneToOne")) {
            analyzeOneToOneRelationship(field, info);
        }
        // @OneToMany
        else if (field.hasAnnotation("javax.persistence.OneToMany")) {
            analyzeOneToManyRelationship(field, info);
        }
        // @ManyToOne
        else if (field.hasAnnotation("javax.persistence.ManyToOne")) {
            analyzeManyToOneRelationship(field, info);
        }
        // @ManyToMany
        else if (field.hasAnnotation("javax.persistence.ManyToMany")) {
            analyzeManyToManyRelationship(field, info);
        }
    }
    
    private void analyzeOneToManyRelationship(CtField field, FieldInfo info) {
        try {
            Annotation oneToMany = field.getAnnotation("javax.persistence.OneToMany");
            
            RelationshipInfo relationshipInfo = new RelationshipInfo();
            relationshipInfo.setRelationType(RelationType.ONE_TO_MANY);
            relationshipInfo.setTargetEntity(getAnnotationClassValue(oneToMany, "targetEntity"));
            relationshipInfo.setMappedBy(getAnnotationValue(oneToMany, "mappedBy"));
            relationshipInfo.setFetch(getAnnotationEnumValue(oneToMany, "fetch", "LAZY"));
            relationshipInfo.setCascade(getAnnotationArrayValue(oneToMany, "cascade"));
            relationshipInfo.setOrphanRemoval(getAnnotationBooleanValue(oneToMany, "orphanRemoval", false));
            
            info.setRelationshipInfo(relationshipInfo);
            
        } catch (ClassNotFoundException e) {
            logger.warn("Error analyzing @OneToMany annotation for field {}", field.getName());
        }
    }
    
    private void identifyPrimaryKey(EntityInfo entityInfo) {
        // Simple primary key
        List<FieldInfo> idFields = entityInfo.getFields().stream()
            .filter(FieldInfo::isPrimaryKey)
            .collect(Collectors.toList());
        
        if (idFields.size() == 1) {
            entityInfo.setPrimaryKeyType(PrimaryKeyType.SIMPLE);
            entityInfo.setPrimaryKeyField(idFields.get(0).getFieldName());
        } else if (idFields.size() > 1) {
            entityInfo.setPrimaryKeyType(PrimaryKeyType.COMPOSITE);
            List<String> pkFields = idFields.stream()
                .map(FieldInfo::getFieldName)
                .collect(Collectors.toList());
            entityInfo.setCompositeKeyFields(pkFields);
        }
        
        // Check for @EmbeddedId
        Optional<FieldInfo> embeddedId = entityInfo.getFields().stream()
            .filter(field -> hasAnnotation(field, "javax.persistence.EmbeddedId"))
            .findFirst();
        
        if (embeddedId.isPresent()) {
            entityInfo.setPrimaryKeyType(PrimaryKeyType.EMBEDDED);
            entityInfo.setPrimaryKeyField(embeddedId.get().getFieldName());
        }
    }
}
```

## Struttura Output Report

```json
{
  "summary": {
    "totalEntities": 23,
    "entitiesWithRelationships": 18,
    "primaryKeyTypes": {
      "simple": 20,
      "composite": 2,
      "embedded": 1
    },
    "inheritanceStrategies": {
      "none": 18,
      "joined": 3,
      "singleTable": 2
    }
  },
  "entities": [
    {
      "className": "com.example.User",
      "entityName": "User",
      "tableName": "users",
      "primaryKeyType": "SIMPLE",
      "primaryKeyField": "id",
      "fields": [
        {
          "fieldName": "id",
          "fieldType": "java.lang.Long",
          "primaryKey": true,
          "columnInfo": {
            "columnName": "id",
            "nullable": false,
            "unique": true
          },
          "generationType": "IDENTITY"
        },
        {
          "fieldName": "username",
          "fieldType": "java.lang.String",
          "columnInfo": {
            "columnName": "username",
            "nullable": false,
            "unique": true,
            "length": 50
          },
          "validationRules": ["@NotBlank", "@Size(max=50)"]
        },
        {
          "fieldName": "orders",
          "fieldType": "java.util.List<Order>",
          "relationshipInfo": {
            "relationType": "ONE_TO_MANY",
            "mappedBy": "user",
            "fetch": "LAZY",
            "cascade": ["PERSIST", "MERGE"],
            "orphanRemoval": true
          }
        }
      ],
      "relationships": [
        {
          "relatedEntity": "Order",
          "relationshipType": "ONE_TO_MANY",
          "owningEntity": false,
          "mappedBy": "user"
        }
      ],
      "lifecycleCallbacks": [
        {
          "method": "prePersist",
          "callbackType": "PRE_PERSIST"
        }
      ]
    }
  ],
  "relationshipMatrix": {
    "User": ["Order", "Role"],
    "Order": ["OrderItem", "User"],
    "OrderItem": ["Product", "Order"]
  },
  "databaseSchema": {
    "tables": 23,
    "foreignKeys": 15,
    "uniqueConstraints": 8,
    "indexes": 12
  }
}
```

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculateJPAEntityQualityScore(EntityAnalysisResult result) {
    double score = 100.0;
    
    // Penalizzazioni per problemi critici
    score -= result.getEntitiesWithoutIds() * 20;             // -20 per entity senza @Id
    score -= result.getBidirectionalWithoutMappedBy() * 15;   // -15 per relazioni bidirezionali senza mappedBy
    score -= result.getMissingLazyLoading() * 12;             // -12 per relazioni eager inappropriate
    score -= result.getNPlusOnePotentialIssues() * 10;        // -10 per potenziali problemi N+1
    score -= result.getInconsistentColumnNaming() * 8;        // -8 per naming colonne inconsistente
    score -= result.getMissingValidationConstraints() * 7;    // -7 per mancanza di validazione
    score -= result.getUnidirectionalManyToMany() * 6;        // -6 per ManyToMany unidirezionali
    score -= result.getMissingCascadeTypes() * 5;             // -5 per cascade type non specificati
    
    // Bonus per best practices
    score += result.getProperlyIndexedFields() * 2;           // +2 per campi correttamente indicizzati
    score += result.getOptimalFetchStrategies() * 3;          // +3 per strategie fetch ottimali
    score += result.getWellDefinedConstraints() * 1;          // +1 per constraint ben definiti
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - Modello dati con gravi problemi strutturali
- **41-60**: 🟡 SUFFICIENTE - Modello funzionale ma necessita ottimizzazioni
- **61-80**: 🟢 BUONO - Modello ben progettato con alcuni miglioramenti possibili
- **81-100**: ⭐ ECCELLENTE - Modello JPA ottimale che segue le best practices

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -15 to -20)
1. **Entity senza identificatore primario**
   - Descrizione: Classi `@Entity` senza campo `@Id` o `@EmbeddedId`
   - Rischio: Impossibilità di persistere correttamente l'entity
   - Soluzione: Aggiungere un campo ID appropriato

2. **Relazioni bidirezionali senza mappedBy**
   - Descrizione: Relazioni `@OneToMany`/`@OneToOne` bidirezionali senza `mappedBy`
   - Rischio: Tabelle di join non necessarie, performance degradate
   - Soluzione: Specificare `mappedBy` nel lato "one" della relazione

### 🟠 GRAVITÀ ALTA (Score Impact: -10 to -12)
3. **Fetch strategy eager inappropriate**
   - Descrizione: Relazioni con `FetchType.EAGER` su collection o oggetti pesanti
   - Rischio: Problemi di performance, caricamento dati non necessari
   - Soluzione: Utilizzare `FetchType.LAZY` e fetch espliciti quando necessario

4. **Potenziali problemi N+1**
   - Descrizione: Relazioni che potrebbero causare query N+1
   - Rischio: Performance degradate con numerose query al database
   - Soluzione: Utilizzare `@EntityGraph`, `JOIN FETCH`, o batch loading

### 🟡 GRAVITÀ MEDIA (Score Impact: -5 to -8)
5. **Naming inconsistente delle colonne**
   - Descrizione: Convenzioni di naming non uniformi per `@Column`
   - Rischio: Confusione nella struttura database
   - Soluzione: Definire e seguire naming strategy consistente

6. **Mancanza di constraint di validazione**
   - Descrizione: Campi senza `@NotNull`, `@Size`, o altre validation annotations
   - Rischio: Dati inconsistenti nel database
   - Soluzione: Implementare Bean Validation appropriata

7. **Relazioni ManyToMany unidirezionali**
   - Descrizione: Relazioni `@ManyToMany` non bidirezionali quando appropiate
   - Rischio: Complessità nelle query, possibili inconsistenze
   - Soluzione: Valutare se rendere bidirezionale o creare entity intermedia

### 🔵 GRAVITÀ BASSA (Score Impact: -1 to -5)
8. **Cascade type non specificati**
   - Descrizione: Relazioni senza `cascade` esplicito
   - Rischio: Comportamento imprevedibile nelle operazioni CRUD
   - Soluzione: Specificare cascade type appropriati

## Tags per Classificazione
`#jpa-entities` `#orm-mapping` `#database-modeling` `#entity-relationships` `#persistence` `#high-value` `#medium-complexity`