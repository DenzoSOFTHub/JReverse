# Report 23: Schema DB Ricostruito dalle Entità

**Valore**: ⭐⭐⭐⭐⭐ **Complessità**: 🔴 Molto Complessa **Tempo**: 12-15 giorni
**Tags**: `#database-schema` `#reverse-engineering` `#ddl-generation`

## Descrizione

Ricostruisce lo schema database completo analizzando le entity JPA, generando DDL statements, identificando constraints, indici, e relazioni per fornire una vista completa della struttura dati.

## Sezioni del Report

### 1. Entity-to-Table Mapping
- @Entity classes analysis and table name mapping
- @Table annotation analysis for schema and catalog
- Column mapping from entity fields to database columns
- Primary key identification and composite key analysis

### 2. Relationship Analysis and Foreign Keys
- @OneToMany, @ManyToOne relationship mapping
- @ManyToMany join table analysis
- Foreign key constraint generation
- Cascade and fetch type analysis

### 3. DDL Generation and Schema Reconstruction
- CREATE TABLE statements generation
- Index creation from @Index annotations
- Constraint definitions (NOT NULL, UNIQUE, CHECK)
- Sequence and auto-generation strategy analysis

### 4. Schema Quality and Consistency Analysis
- Missing constraints identification
- Naming convention compliance
- Redundant or conflicting definitions
- Performance optimization opportunities

## Implementazione Javassist Completa

```java
public class DatabaseSchemaReconstructionAnalyzer {
    
    public DatabaseSchemaReport reconstructDatabaseSchema(CtClass[] classes) {
        DatabaseSchemaReport report = new DatabaseSchemaReport();
        
        // 1. Identifica entity classes
        List<EntityDefinition> entities = identifyEntityClasses(classes, report);
        
        // 2. Analizza table mappings
        analyzeTableMappings(entities, report);
        
        // 3. Analizza relationships
        analyzeEntityRelationships(entities, report);
        
        // 4. Genera DDL schema
        generateDDLSchema(entities, report);
        
        // 5. Identifica problemi schema
        identifySchemaIssues(entities, report);
        
        return report;
    }
    
    private List<EntityDefinition> identifyEntityClasses(CtClass[] classes, DatabaseSchemaReport report) {
        List<EntityDefinition> entities = new ArrayList<>();
        
        for (CtClass ctClass : classes) {
            try {
                if (isEntityClass(ctClass)) {
                    EntityDefinition entity = analyzeEntityClass(ctClass, report);
                    entities.add(entity);
                }
            } catch (Exception e) {
                report.addError("Errore nell'analisi entity class: " + e.getMessage());
            }
        }
        
        return entities;
    }
    
    private boolean isEntityClass(CtClass ctClass) {
        try {
            return ctClass.hasAnnotation("javax.persistence.Entity") ||
                   ctClass.hasAnnotation("jakarta.persistence.Entity");
        } catch (Exception e) {
            return false;
        }
    }
    
    private EntityDefinition analyzeEntityClass(CtClass ctClass, DatabaseSchemaReport report) {
        try {
            EntityDefinition entity = new EntityDefinition();
            entity.setEntityClass(ctClass.getName());
            entity.setSimpleName(ctClass.getSimpleName());
            
            // Analizza @Entity annotation
            analyzeEntityAnnotation(ctClass, entity);
            
            // Analizza @Table annotation
            analyzeTableAnnotation(ctClass, entity);
            
            // Analizza fields/properties
            analyzeEntityFields(ctClass, entity, report);
            
            // Analizza primary keys
            analyzePrimaryKeys(ctClass, entity, report);
            
            // Analizza indexes
            analyzeEntityIndexes(ctClass, entity);
            
            return entity;
            
        } catch (Exception e) {
            EntityDefinition errorEntity = new EntityDefinition();
            errorEntity.setEntityClass(ctClass.getName());
            errorEntity.addError("Errore nell'analisi entity definition: " + e.getMessage());
            return errorEntity;
        }
    }
    
    private void analyzeTableAnnotation(CtClass ctClass, EntityDefinition entity) {
        try {
            if (ctClass.hasAnnotation("javax.persistence.Table") ||
                ctClass.hasAnnotation("jakarta.persistence.Table")) {
                
                Annotation tableAnnotation = ctClass.hasAnnotation("javax.persistence.Table") ?
                    ctClass.getAnnotation("javax.persistence.Table") :
                    ctClass.getAnnotation("jakarta.persistence.Table");
                
                String tableName = extractAnnotationValue(tableAnnotation, "name");
                String schema = extractAnnotationValue(tableAnnotation, "schema");
                String catalog = extractAnnotationValue(tableAnnotation, "catalog");
                
                entity.setTableName(tableName != null ? tableName : toSnakeCase(entity.getSimpleName()));
                entity.setSchema(schema);
                entity.setCatalog(catalog);
                
                // Analizza unique constraints
                analyzeUniqueConstraints(tableAnnotation, entity);
            } else {
                // Default table name
                entity.setTableName(toSnakeCase(entity.getSimpleName()));
            }
            
        } catch (Exception e) {
            entity.addError("Errore nell'analisi @Table annotation: " + e.getMessage());
        }
    }
    
    private void analyzeEntityFields(CtClass ctClass, EntityDefinition entity, DatabaseSchemaReport report) {
        try {
            CtField[] fields = ctClass.getDeclaredFields();
            
            for (CtField field : fields) {
                // Skip static and transient fields
                if (Modifier.isStatic(field.getModifiers()) || 
                    Modifier.isTransient(field.getModifiers()) ||
                    field.hasAnnotation("javax.persistence.Transient") ||
                    field.hasAnnotation("jakarta.persistence.Transient")) {
                    continue;
                }
                
                ColumnDefinition column = analyzeEntityField(field, entity, report);
                entity.addColumn(column);
            }
            
        } catch (Exception e) {
            entity.addError("Errore nell'analisi entity fields: " + e.getMessage());
        }
    }
    
    private ColumnDefinition analyzeEntityField(CtField field, EntityDefinition entity, DatabaseSchemaReport report) {
        try {
            ColumnDefinition column = new ColumnDefinition();
            column.setFieldName(field.getName());
            column.setFieldType(field.getType().getName());
            
            // Analizza @Column annotation
            analyzeColumnAnnotation(field, column);
            
            // Determina SQL type
            String sqlType = determineSQLType(field.getType().getName(), column);
            column.setSqlType(sqlType);
            
            // Analizza relationship annotations
            RelationshipType relationshipType = analyzeRelationshipAnnotations(field, column);
            column.setRelationshipType(relationshipType);
            
            // Analizza validation annotations
            analyzeValidationAnnotations(field, column);
            
            return column;
            
        } catch (Exception e) {
            ColumnDefinition errorColumn = new ColumnDefinition();
            errorColumn.setFieldName(field.getName());
            errorColumn.addError("Errore nell'analisi field: " + e.getMessage());
            return errorColumn;
        }
    }
    
    private void analyzeColumnAnnotation(CtField field, ColumnDefinition column) {
        try {
            if (field.hasAnnotation("javax.persistence.Column") ||
                field.hasAnnotation("jakarta.persistence.Column")) {
                
                Annotation columnAnnotation = field.hasAnnotation("javax.persistence.Column") ?
                    field.getAnnotation("javax.persistence.Column") :
                    field.getAnnotation("jakarta.persistence.Column");
                
                String columnName = extractAnnotationValue(columnAnnotation, "name");
                Integer length = extractIntAnnotationValue(columnAnnotation, "length");
                Integer precision = extractIntAnnotationValue(columnAnnotation, "precision");
                Integer scale = extractIntAnnotationValue(columnAnnotation, "scale");
                Boolean nullable = extractBooleanAnnotationValue(columnAnnotation, "nullable");
                Boolean unique = extractBooleanAnnotationValue(columnAnnotation, "unique");
                String columnDefinition = extractAnnotationValue(columnAnnotation, "columnDefinition");
                
                column.setColumnName(columnName != null ? columnName : toSnakeCase(column.getFieldName()));
                column.setLength(length);
                column.setPrecision(precision);
                column.setScale(scale);
                column.setNullable(nullable != null ? nullable : true);
                column.setUnique(unique != null ? unique : false);
                column.setColumnDefinition(columnDefinition);
            } else {
                // Default column name
                column.setColumnName(toSnakeCase(column.getFieldName()));
            }
            
        } catch (Exception e) {
            column.addError("Errore nell'analisi @Column annotation: " + e.getMessage());
        }
    }
    
    private String determineSQLType(String javaType, ColumnDefinition column) {
        // Map Java types to SQL types
        switch (javaType) {
            case "java.lang.String":
                return column.getLength() != null ? "VARCHAR(" + column.getLength() + ")" : "VARCHAR(255)";
            case "java.lang.Integer":
            case "int":
                return "INTEGER";
            case "java.lang.Long":
            case "long":
                return "BIGINT";
            case "java.lang.Boolean":
            case "boolean":
                return "BOOLEAN";
            case "java.math.BigDecimal":
                if (column.getPrecision() != null && column.getScale() != null) {
                    return "DECIMAL(" + column.getPrecision() + "," + column.getScale() + ")";
                }
                return "DECIMAL";
            case "java.util.Date":
            case "java.sql.Date":
                return "DATE";
            case "java.sql.Time":
                return "TIME";
            case "java.sql.Timestamp":
            case "java.time.LocalDateTime":
                return "TIMESTAMP";
            case "java.time.LocalDate":
                return "DATE";
            case "java.time.LocalTime":
                return "TIME";
            case "byte[]":
                return "BLOB";
            default:
                return "VARCHAR(255)"; // Default fallback
        }
    }
    
    private RelationshipType analyzeRelationshipAnnotations(CtField field, ColumnDefinition column) {
        try {
            if (field.hasAnnotation("javax.persistence.OneToOne") ||
                field.hasAnnotation("jakarta.persistence.OneToOne")) {
                return RelationshipType.ONE_TO_ONE;
            } else if (field.hasAnnotation("javax.persistence.OneToMany") ||
                      field.hasAnnotation("jakarta.persistence.OneToMany")) {
                return RelationshipType.ONE_TO_MANY;
            } else if (field.hasAnnotation("javax.persistence.ManyToOne") ||
                      field.hasAnnotation("jakarta.persistence.ManyToOne")) {
                return RelationshipType.MANY_TO_ONE;
            } else if (field.hasAnnotation("javax.persistence.ManyToMany") ||
                      field.hasAnnotation("jakarta.persistence.ManyToMany")) {
                return RelationshipType.MANY_TO_MANY;
            }
            
            return RelationshipType.NONE;
            
        } catch (Exception e) {
            return RelationshipType.NONE;
        }
    }
    
    private void analyzeEntityRelationships(List<EntityDefinition> entities, DatabaseSchemaReport report) {
        try {
            Map<String, EntityDefinition> entityMap = entities.stream()
                .collect(Collectors.toMap(EntityDefinition::getEntityClass, e -> e));
            
            for (EntityDefinition entity : entities) {
                analyzeEntityRelationshipsForEntity(entity, entityMap, report);
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'analisi entity relationships: " + e.getMessage());
        }
    }
    
    private void analyzeEntityRelationshipsForEntity(EntityDefinition entity, Map<String, EntityDefinition> entityMap, DatabaseSchemaReport report) {
        try {
            for (ColumnDefinition column : entity.getColumns()) {
                if (column.getRelationshipType() != RelationshipType.NONE) {
                    RelationshipDefinition relationship = new RelationshipDefinition();
                    relationship.setSourceEntity(entity.getEntityClass());
                    relationship.setSourceTable(entity.getTableName());
                    relationship.setSourceColumn(column.getColumnName());
                    relationship.setRelationshipType(column.getRelationshipType());
                    
                    // Determina target entity (simplified)
                    String targetEntityType = determineTargetEntityType(column);
                    if (entityMap.containsKey(targetEntityType)) {
                        EntityDefinition targetEntity = entityMap.get(targetEntityType);
                        relationship.setTargetEntity(targetEntity.getEntityClass());
                        relationship.setTargetTable(targetEntity.getTableName());
                        
                        // Per ManyToOne, genera foreign key constraint
                        if (column.getRelationshipType() == RelationshipType.MANY_TO_ONE) {
                            ForeignKeyConstraint fk = new ForeignKeyConstraint();
                            fk.setConstraintName("FK_" + entity.getTableName() + "_" + column.getColumnName());
                            fk.setSourceTable(entity.getTableName());
                            fk.setSourceColumn(column.getColumnName());
                            fk.setTargetTable(targetEntity.getTableName());
                            fk.setTargetColumn(targetEntity.getPrimaryKeyColumn());
                            
                            entity.addForeignKey(fk);
                        }
                    }
                    
                    report.addRelationship(relationship);
                }
            }
            
        } catch (Exception e) {
            entity.addError("Errore nell'analisi relationships per entity: " + e.getMessage());
        }
    }
    
    private void generateDDLSchema(List<EntityDefinition> entities, DatabaseSchemaReport report) {
        try {
            StringBuilder ddlSchema = new StringBuilder();
            
            // Generate CREATE TABLE statements
            for (EntityDefinition entity : entities) {
                String createTableDDL = generateCreateTableDDL(entity);
                ddlSchema.append(createTableDDL).append("\n\n");
            }
            
            // Generate ALTER TABLE statements for foreign keys
            for (EntityDefinition entity : entities) {
                for (ForeignKeyConstraint fk : entity.getForeignKeys()) {
                    String alterTableDDL = generateAlterTableForeignKeyDDL(fk);
                    ddlSchema.append(alterTableDDL).append("\n");
                }
            }
            
            // Generate CREATE INDEX statements
            for (EntityDefinition entity : entities) {
                for (IndexDefinition index : entity.getIndexes()) {
                    String createIndexDDL = generateCreateIndexDDL(index);
                    ddlSchema.append(createIndexDDL).append("\n");
                }
            }
            
            report.setDdlSchema(ddlSchema.toString());
            
        } catch (Exception e) {
            report.addError("Errore nella generazione DDL schema: " + e.getMessage());
        }
    }
    
    private String generateCreateTableDDL(EntityDefinition entity) {
        StringBuilder ddl = new StringBuilder();
        
        ddl.append("CREATE TABLE ");
        if (entity.getSchema() != null) {
            ddl.append(entity.getSchema()).append(".");
        }
        ddl.append(entity.getTableName()).append(" (\n");
        
        // Add columns
        List<String> columnDefinitions = new ArrayList<>();
        for (ColumnDefinition column : entity.getColumns()) {
            if (column.getRelationshipType() == RelationshipType.ONE_TO_MANY ||
                column.getRelationshipType() == RelationshipType.MANY_TO_MANY) {
                continue; // Skip collection fields
            }
            
            StringBuilder colDef = new StringBuilder();
            colDef.append("  ").append(column.getColumnName()).append(" ").append(column.getSqlType());
            
            if (!column.isNullable()) {
                colDef.append(" NOT NULL");
            }
            
            if (column.isUnique()) {
                colDef.append(" UNIQUE");
            }
            
            if (column.isPrimaryKey()) {
                colDef.append(" PRIMARY KEY");
            }
            
            columnDefinitions.add(colDef.toString());
        }
        
        ddl.append(String.join(",\n", columnDefinitions));
        
        // Add composite primary key if exists
        if (entity.hasCompositePrimaryKey()) {
            ddl.append(",\n  PRIMARY KEY (").append(String.join(", ", entity.getPrimaryKeyColumns())).append(")");
        }
        
        ddl.append("\n);");
        
        return ddl.toString();
    }
    
    private void identifySchemaIssues(List<EntityDefinition> entities, DatabaseSchemaReport report) {
        try {
            for (EntityDefinition entity : entities) {
                
                // Missing primary key
                if (entity.getPrimaryKeyColumns().isEmpty()) {
                    SchemaIssue issue = new SchemaIssue();
                    issue.setType(IssueType.MISSING_PRIMARY_KEY);
                    issue.setEntityClass(entity.getEntityClass());
                    issue.setTableName(entity.getTableName());
                    issue.setSeverity(Severity.HIGH);
                    issue.setDescription("Entity has no primary key defined");
                    issue.setRecommendation("Add @Id annotation to identify primary key field");
                    
                    report.addSchemaIssue(issue);
                }
                
                // Table name conflicts (simplified check)
                long duplicateTableNames = entities.stream()
                    .filter(e -> e.getTableName().equals(entity.getTableName()))
                    .count();
                
                if (duplicateTableNames > 1) {
                    SchemaIssue issue = new SchemaIssue();
                    issue.setType(IssueType.DUPLICATE_TABLE_NAME);
                    issue.setEntityClass(entity.getEntityClass());
                    issue.setTableName(entity.getTableName());
                    issue.setSeverity(Severity.HIGH);
                    issue.setDescription("Multiple entities map to same table name");
                    issue.setRecommendation("Use @Table annotation to specify unique table names");
                    
                    report.addSchemaIssue(issue);
                }
                
                // Missing indexes on foreign key columns
                for (ColumnDefinition column : entity.getColumns()) {
                    if (column.getRelationshipType() == RelationshipType.MANY_TO_ONE && 
                        !hasIndexOnColumn(entity, column.getColumnName())) {
                        
                        SchemaIssue issue = new SchemaIssue();
                        issue.setType(IssueType.MISSING_FOREIGN_KEY_INDEX);
                        issue.setEntityClass(entity.getEntityClass());
                        issue.setTableName(entity.getTableName());
                        issue.setColumnName(column.getColumnName());
                        issue.setSeverity(Severity.MEDIUM);
                        issue.setDescription("Foreign key column lacks index for performance");
                        issue.setRecommendation("Add @Index annotation or create database index");
                        
                        report.addSchemaIssue(issue);
                    }
                }
            }
            
        } catch (Exception e) {
            report.addError("Errore nell'identificazione schema issues: " + e.getMessage());
        }
    }
}

public class DatabaseSchemaReport {
    private List<EntityDefinition> entities = new ArrayList<>();
    private List<RelationshipDefinition> relationships = new ArrayList<>();
    private List<SchemaIssue> schemaIssues = new ArrayList<>();
    private String ddlSchema;
    private SchemaStatistics schemaStatistics;
    private List<String> errors = new ArrayList<>();
    
    public static class EntityDefinition {
        private String entityClass;
        private String simpleName;
        private String tableName;
        private String schema;
        private String catalog;
        private List<ColumnDefinition> columns = new ArrayList<>();
        private List<String> primaryKeyColumns = new ArrayList<>();
        private boolean compositePrimaryKey = false;
        private List<IndexDefinition> indexes = new ArrayList<>();
        private List<ForeignKeyConstraint> foreignKeys = new ArrayList<>();
        private List<String> errors = new ArrayList<>();
    }
    
    public static class ColumnDefinition {
        private String fieldName;
        private String fieldType;
        private String columnName;
        private String sqlType;
        private Integer length;
        private Integer precision;
        private Integer scale;
        private boolean nullable = true;
        private boolean unique = false;
        private boolean primaryKey = false;
        private String columnDefinition;
        private RelationshipType relationshipType = RelationshipType.NONE;
        private List<String> errors = new ArrayList<>();
    }
    
    public static class SchemaIssue {
        private IssueType type;
        private String entityClass;
        private String tableName;
        private String columnName;
        private Severity severity;
        private String description;
        private String recommendation;
    }
    
    public enum RelationshipType {
        NONE, ONE_TO_ONE, ONE_TO_MANY, MANY_TO_ONE, MANY_TO_MANY
    }
    
    public enum IssueType {
        MISSING_PRIMARY_KEY,
        DUPLICATE_TABLE_NAME,
        MISSING_FOREIGN_KEY_INDEX,
        NAMING_CONVENTION_VIOLATION,
        MISSING_CONSTRAINT,
        REDUNDANT_COLUMN_DEFINITION
    }
    
    public enum Severity {
        CRITICAL, HIGH, MEDIUM, LOW
    }
}
```

## Metriche di Qualità del Codice

### Algoritmo di Calcolo (0-100)

```java
public int calculateDatabaseSchemaQualityScore(DatabaseSchemaReport result) {
    double score = 100.0;
    
    // Penalizzazioni per problemi database schema critici
    score -= result.getMissingPrimaryKeys() * 20;                 // -20 per entity senza primary key
    score -= result.getDuplicateTableNames() * 18;                // -18 per nomi tabella duplicati
    score -= result.getMissingForeignKeyIndexes() * 12;           // -12 per indici FK mancanti
    score -= result.getNamingConventionViolations() * 8;          // -8 per violazioni naming convention
    score -= result.getMissingConstraints() * 10;                 // -10 per constraint mancanti
    score -= result.getRedundantColumnDefinitions() * 6;          // -6 per definizioni colonna ridondanti
    score -= result.getInconsistentDataTypes() * 5;               // -5 per tipi dati inconsistenti
    
    // Bonus per buone pratiche database schema
    score += result.getWellDefinedEntities() * 3;                 // +3 per entity ben definite
    score += result.getProperIndexUsage() * 2;                    // +2 per uso appropriato indici
    score += result.getConsistentNamingConventions() * 2;         // +2 per naming convention consistenti
    score += result.getOptimalDataTypes() * 1;                    // +1 per tipi dati ottimali
    
    return Math.max(0, Math.min(100, (int) score));
}
```

### Soglie di Valutazione
- **0-40**: 🔴 CRITICO - Schema database con gravi problemi strutturali
- **41-60**: 🟡 SUFFICIENTE - Schema funzionante ma con lacune significative
- **61-80**: 🟢 BUONO - Schema ben strutturato con alcuni miglioramenti
- **81-100**: ⭐ ECCELLENTE - Schema database ottimale e ben progettato

## Segnalazioni per Gravità

### 🔴 GRAVITÀ CRITICA (Score Impact: -18 to -20)
1. **Primary key mancanti**
   - Descrizione: Entity JPA senza @Id o primary key definita
   - Rischio: Problemi ORM, performance query, data integrity issues
   - Soluzione: Aggiungere @Id annotation su campo primary key

2. **Nomi tabella duplicati**
   - Descrizione: Multiple entity che mappano stesso nome tabella
   - Rischio: Conflitti schema, deployment failures, data corruption
   - Soluzione: Usare @Table per specificare nomi tabella univoci

### 🟠 GRAVITÀ ALTA (Score Impact: -10 to -12)  
3. **Indici foreign key mancanti**
   - Descrizione: Colonne foreign key senza indici per performance
   - Rischio: Query lente, performance degradation su joins
   - Soluzione: Aggiungere @Index o creare indici database appropriati

4. **Constraint mancanti**
   - Descrizione: Mancanza constraint NOT NULL, UNIQUE, CHECK appropriati
   - Rischio: Data integrity compromessa, invalid data storage
   - Soluzione: Aggiungere validation annotations e constraint database

### 🟡 GRAVITÀ MEDIA (Score Impact: -6 to -8)
5. **Violazioni naming convention**
   - Descrizione: Nomi tabella/colonna non seguono convention standard
   - Rischio: Confusion team, maintenance difficulties, inconsistency
   - Soluzione: Standardizzare naming usando snake_case per database

6. **Definizioni colonna ridondanti**
   - Descrizione: Definizioni colonna duplicate o non necessarie
   - Rischio: Schema bloat, maintenance overhead, confusion
   - Soluzione: Consolidare e semplificare definizioni colonna

### 🔵 GRAVITÀ BASSA (Score Impact: -5)
7. **Tipi dati inconsistenti**
   - Descrizione: Same logical data rappresentato con tipi diversi
   - Rischio: Confusion, potential data conversion issues
   - Soluzione: Standardizzare tipi dati per consistency across schema

## Metriche di Valore

- **Data Integrity**: Assicura integrità database attraverso constraint appropriati
- **Performance Optimization**: Identifica opportunità ottimizzazione query e indici
- **Schema Documentation**: Fornisce documentazione completa struttura database
- **Migration Planning**: Facilita planning migration e schema evolution

**Tags**: `#database-schema` `#reverse-engineering` `#ddl-generation` `#very-high-value` `#very-complex`