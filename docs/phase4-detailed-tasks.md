# Fase 4: Database & Persistence Layer - Task Dettagliati

## T4.1.1: EntityAnalyzer per @Entity Classes Detection

### Descrizione Dettagliata
Analyzer specializzato nell'identificazione e analisi completa delle classi JPA Entity, mappando strutture delle entità, relazioni database, constrainti e metadati di persistenza per ricostruire il modello dati dell'applicazione.

### Scopo dell'Attività
- Identificare tutte le classi @Entity e @Embeddable
- Analizzare mapping JPA e struttura delle entità
- Estrarre metadati di persistenza (tabelle, colonne, indici)
- Mappare inheritance strategies e discriminatori
- Rilevare pattern e anti-pattern nel data model

### Impatti su Altri Moduli
- **RelationshipAnalyzer**: Base per mappatura relazioni JPA
- **DatabaseSchemaBuilder**: Input per ricostruzione schema
- **ConstraintAnalyzer**: Fonte per validazione constraints
- **Repository Methods Analyzer**: Context per query analysis

### Componenti da Implementare

#### 1. Entity Analyzer Core
```java
public interface EntityAnalyzer {
    EntityAnalysisResult analyzeEntities(JarContent jarContent);
}

public class JavassistEntityAnalyzer implements EntityAnalyzer {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistEntityAnalyzer.class);
    
    private static final Set<String> ENTITY_ANNOTATIONS = Set.of(
        "javax.persistence.Entity",
        "jakarta.persistence.Entity"
    );
    
    private static final Set<String> EMBEDDABLE_ANNOTATIONS = Set.of(
        "javax.persistence.Embeddable",
        "jakarta.persistence.Embeddable"
    );
    
    private final EntityMetadataExtractor metadataExtractor;
    private final EntityInheritanceAnalyzer inheritanceAnalyzer;
    private final EntityValidationAnalyzer validationAnalyzer;
    private final EntityAntiPatternDetector antiPatternDetector;
    
    public JavassistEntityAnalyzer() {
        this.metadataExtractor = new EntityMetadataExtractor();
        this.inheritanceAnalyzer = new EntityInheritanceAnalyzer();
        this.validationAnalyzer = new EntityValidationAnalyzer();
        this.antiPatternDetector = new EntityAntiPatternDetector();
    }
    
    @Override
    public EntityAnalysisResult analyzeEntities(JarContent jarContent) {
        LOGGER.startOperation("Entity analysis");
        long startTime = System.currentTimeMillis();
        
        try {
            EntityAnalysisResult.Builder resultBuilder = EntityAnalysisResult.builder();
            
            // Identifica tutte le entity classes
            List<EntityClass> entityClasses = findEntityClasses(jarContent);
            resultBuilder.entityClasses(entityClasses);
            
            LOGGER.info("Found {} entity classes", entityClasses.size());
            
            // Identifica embeddable classes
            List<EmbeddableClass> embeddableClasses = findEmbeddableClasses(jarContent);
            resultBuilder.embeddableClasses(embeddableClasses);
            
            // Estrai metadati di persistenza per ogni entity
            for (EntityClass entityClass : entityClasses) {
                EntityMetadata metadata = metadataExtractor.extractMetadata(entityClass, jarContent);
                resultBuilder.addEntityMetadata(entityClass.getClassName(), metadata);
            }
            
            // Analizza inheritance hierarchies
            EntityInheritanceHierarchy inheritanceHierarchy = inheritanceAnalyzer.analyzeInheritance(
                entityClasses, jarContent);
            resultBuilder.inheritanceHierarchy(inheritanceHierarchy);
            
            // Valida entity definitions
            List<EntityValidationIssue> validationIssues = validationAnalyzer.validateEntities(
                entityClasses, embeddableClasses);
            resultBuilder.validationIssues(validationIssues);
            
            // Rileva anti-pattern
            List<EntityAntiPattern> antiPatterns = antiPatternDetector.detectAntiPatterns(
                entityClasses, embeddableClasses);
            resultBuilder.antiPatterns(antiPatterns);
            
            // Calcola metriche entity
            EntityMetrics metrics = calculateEntityMetrics(entityClasses, embeddableClasses);
            resultBuilder.metrics(metrics);
            
            return resultBuilder
                .analysisMetadata(AnalysisMetadata.successful(System.currentTimeMillis() - startTime))
                .build();
                
        } catch (Exception e) {
            LOGGER.error("Entity analysis failed", e);
            return EntityAnalysisResult.failed("Analysis failed: " + e.getMessage());
        } finally {
            LOGGER.endOperation("Entity analysis", System.currentTimeMillis() - startTime);
        }
    }
    
    private List<EntityClass> findEntityClasses(JarContent jarContent) {
        return jarContent.getClasses().stream()
            .filter(this::isEntityClass)
            .map(this::createEntityClass)
            .collect(Collectors.toList());
    }
    
    private boolean isEntityClass(ClassInfo classInfo) {
        return classInfo.getAnnotations().stream()
            .anyMatch(annotation -> ENTITY_ANNOTATIONS.contains(annotation.getType()));
    }
    
    private EntityClass createEntityClass(ClassInfo classInfo) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.get(classInfo.getFullyQualifiedName());
            
            return EntityClass.builder()
                .className(classInfo.getFullyQualifiedName())
                .classInfo(classInfo)
                .ctClass(ctClass)
                .entityAnnotation(extractEntityAnnotation(classInfo))
                .tableAnnotation(extractTableAnnotation(classInfo))
                .fields(extractEntityFields(ctClass))
                .methods(extractEntityMethods(ctClass))
                .build();
                
        } catch (Exception e) {
            LOGGER.error("Error creating EntityClass for {}", classInfo.getFullyQualifiedName(), e);
            return EntityClass.builder()
                .className(classInfo.getFullyQualifiedName())
                .classInfo(classInfo)
                .build();
        }
    }
    
    private List<EmbeddableClass> findEmbeddableClasses(JarContent jarContent) {
        return jarContent.getClasses().stream()
            .filter(this::isEmbeddableClass)
            .map(this::createEmbeddableClass)
            .collect(Collectors.toList());
    }
    
    private boolean isEmbeddableClass(ClassInfo classInfo) {
        return classInfo.getAnnotations().stream()
            .anyMatch(annotation -> EMBEDDABLE_ANNOTATIONS.contains(annotation.getType()));
    }
    
    private EntityMetrics calculateEntityMetrics(List<EntityClass> entityClasses, 
                                               List<EmbeddableClass> embeddableClasses) {
        EntityMetrics.Builder builder = EntityMetrics.builder();
        
        // Contatori di base
        builder.totalEntityClasses(entityClasses.size());
        builder.totalEmbeddableClasses(embeddableClasses.size());
        
        // Metriche per entity
        int totalFields = 0;
        int totalRelationships = 0;
        Map<String, Integer> inheritanceStrategyCounts = new HashMap<>();
        
        for (EntityClass entityClass : entityClasses) {
            totalFields += entityClass.getFields().size();
            
            // Conta relationships
            long relationshipFields = entityClass.getFields().stream()
                .filter(this::isRelationshipField)
                .count();
            totalRelationships += relationshipFields;
            
            // Conta inheritance strategies
            String inheritanceStrategy = extractInheritanceStrategy(entityClass);
            if (inheritanceStrategy != null) {
                inheritanceStrategyCounts.merge(inheritanceStrategy, 1, Integer::sum);
            }
        }
        
        builder.averageFieldsPerEntity(entityClasses.isEmpty() ? 0 : (double) totalFields / entityClasses.size());
        builder.totalRelationships(totalRelationships);
        builder.inheritanceStrategyCounts(inheritanceStrategyCounts);
        
        return builder.build();
    }
}
```

#### 2. Entity Metadata Extractor
```java
public class EntityMetadataExtractor {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(EntityMetadataExtractor.class);
    
    public EntityMetadata extractMetadata(EntityClass entityClass, JarContent jarContent) {
        LOGGER.debug("Extracting metadata for entity: {}", entityClass.getClassName());
        
        EntityMetadata.Builder builder = EntityMetadata.builder()
            .entityName(extractEntityName(entityClass))
            .tableName(extractTableName(entityClass))
            .schema(extractSchema(entityClass))
            .catalog(extractCatalog(entityClass));
        
        // Estrai column mappings
        Map<String, ColumnMapping> columnMappings = extractColumnMappings(entityClass);
        builder.columnMappings(columnMappings);
        
        // Estrai primary key information
        PrimaryKeyInfo primaryKeyInfo = extractPrimaryKeyInfo(entityClass);
        builder.primaryKeyInfo(primaryKeyInfo);
        
        // Estrai index information
        List<IndexInfo> indexInfos = extractIndexes(entityClass);
        builder.indexes(indexInfos);
        
        // Estrai unique constraints
        List<UniqueConstraint> uniqueConstraints = extractUniqueConstraints(entityClass);
        builder.uniqueConstraints(uniqueConstraints);
        
        // Estrai sequence generators
        List<SequenceGenerator> sequenceGenerators = extractSequenceGenerators(entityClass);
        builder.sequenceGenerators(sequenceGenerators);
        
        // Estrai table generators
        List<TableGenerator> tableGenerators = extractTableGenerators(entityClass);
        builder.tableGenerators(tableGenerators);
        
        return builder.build();
    }
    
    private String extractEntityName(EntityClass entityClass) {
        AnnotationInfo entityAnnotation = entityClass.getEntityAnnotation();
        if (entityAnnotation != null) {
            String name = (String) entityAnnotation.getAttributes().get("name");
            if (name != null && !name.isEmpty()) {
                return name;
            }
        }
        
        // Default: simple class name
        String className = entityClass.getClassName();
        return className.substring(className.lastIndexOf('.') + 1);
    }
    
    private String extractTableName(EntityClass entityClass) {
        AnnotationInfo tableAnnotation = entityClass.getTableAnnotation();
        if (tableAnnotation != null) {
            String name = (String) tableAnnotation.getAttributes().get("name");
            if (name != null && !name.isEmpty()) {
                return name;
            }
        }
        
        // Default: entity name in lowercase
        return extractEntityName(entityClass).toLowerCase();
    }
    
    private Map<String, ColumnMapping> extractColumnMappings(EntityClass entityClass) {
        Map<String, ColumnMapping> mappings = new HashMap<>();
        
        for (EntityField field : entityClass.getFields()) {
            if (isBasicField(field) || isRelationshipField(field)) {
                ColumnMapping mapping = extractColumnMapping(field);
                mappings.put(field.getFieldName(), mapping);
            }
        }
        
        return mappings;
    }
    
    private ColumnMapping extractColumnMapping(EntityField field) {
        ColumnMapping.Builder builder = ColumnMapping.builder()
            .fieldName(field.getFieldName())
            .fieldType(field.getFieldType());
        
        // Cerca @Column annotation
        AnnotationInfo columnAnnotation = findAnnotation(field, "javax.persistence.Column", "jakarta.persistence.Column");
        if (columnAnnotation != null) {
            Map<String, Object> attributes = columnAnnotation.getAttributes();
            
            builder.columnName((String) attributes.getOrDefault("name", field.getFieldName()));
            builder.nullable((Boolean) attributes.getOrDefault("nullable", true));
            builder.unique((Boolean) attributes.getOrDefault("unique", false));
            builder.insertable((Boolean) attributes.getOrDefault("insertable", true));
            builder.updatable((Boolean) attributes.getOrDefault("updatable", true));
            builder.length((Integer) attributes.getOrDefault("length", 255));
            builder.precision((Integer) attributes.getOrDefault("precision", 0));
            builder.scale((Integer) attributes.getOrDefault("scale", 0));
            builder.columnDefinition((String) attributes.get("columnDefinition"));
        } else {
            // Default mapping
            builder.columnName(field.getFieldName())
                   .nullable(true)
                   .unique(false)
                   .insertable(true)
                   .updatable(true);
        }
        
        // Determina SQL type basato su Java type
        SqlType sqlType = determineSqlType(field.getFieldType());
        builder.sqlType(sqlType);
        
        return builder.build();
    }
    
    private PrimaryKeyInfo extractPrimaryKeyInfo(EntityClass entityClass) {
        PrimaryKeyInfo.Builder builder = PrimaryKeyInfo.builder();
        
        // Cerca @Id annotations
        List<EntityField> idFields = entityClass.getFields().stream()
            .filter(field -> hasAnnotation(field, "javax.persistence.Id", "jakarta.persistence.Id"))
            .collect(Collectors.toList());
        
        if (idFields.isEmpty()) {
            return PrimaryKeyInfo.noPrimaryKey();
        } else if (idFields.size() == 1) {
            // Single primary key
            EntityField idField = idFields.get(0);
            builder.primaryKeyType(PrimaryKeyType.SINGLE)
                   .idFields(List.of(idField.getFieldName()))
                   .idClass(null);
            
            // Estrai generation strategy
            GenerationStrategy generationStrategy = extractGenerationStrategy(idField);
            builder.generationStrategy(generationStrategy);
            
        } else {
            // Composite primary key
            builder.primaryKeyType(PrimaryKeyType.COMPOSITE)
                   .idFields(idFields.stream().map(EntityField::getFieldName).collect(Collectors.toList()));
            
            // Cerca @IdClass annotation
            AnnotationInfo idClassAnnotation = findAnnotation(entityClass.getClassInfo(), 
                "javax.persistence.IdClass", "jakarta.persistence.IdClass");
            if (idClassAnnotation != null) {
                Class<?> idClass = (Class<?>) idClassAnnotation.getAttributes().get("value");
                builder.idClass(idClass.getName());
            }
        }
        
        return builder.build();
    }
    
    private GenerationStrategy extractGenerationStrategy(EntityField idField) {
        AnnotationInfo generatedValueAnnotation = findAnnotation(idField, 
            "javax.persistence.GeneratedValue", "jakarta.persistence.GeneratedValue");
            
        if (generatedValueAnnotation == null) {
            return GenerationStrategy.none();
        }
        
        Map<String, Object> attributes = generatedValueAnnotation.getAttributes();
        String strategy = (String) attributes.getOrDefault("strategy", "AUTO");
        String generator = (String) attributes.get("generator");
        
        return GenerationStrategy.builder()
            .strategy(GenerationType.valueOf(strategy))
            .generator(generator)
            .build();
    }
    
    private List<IndexInfo> extractIndexes(EntityClass entityClass) {
        List<IndexInfo> indexes = new ArrayList<>();
        
        // Estrai da @Table(indexes = {...})
        AnnotationInfo tableAnnotation = entityClass.getTableAnnotation();
        if (tableAnnotation != null) {
            Object[] indexAnnotations = (Object[]) tableAnnotation.getAttributes().get("indexes");
            if (indexAnnotations != null) {
                for (Object indexAnnotation : indexAnnotations) {
                    IndexInfo indexInfo = parseIndexAnnotation(indexAnnotation);
                    indexes.add(indexInfo);
                }
            }
        }
        
        // Estrai da @Index annotations sui field
        for (EntityField field : entityClass.getFields()) {
            AnnotationInfo indexAnnotation = findAnnotation(field, "javax.persistence.Index", "jakarta.persistence.Index");
            if (indexAnnotation != null) {
                IndexInfo indexInfo = parseIndexAnnotation(indexAnnotation, field.getFieldName());
                indexes.add(indexInfo);
            }
        }
        
        return indexes;
    }
    
    private SqlType determineSqlType(String javaType) {
        return switch (javaType) {
            case "java.lang.String" -> SqlType.VARCHAR;
            case "int", "java.lang.Integer" -> SqlType.INTEGER;
            case "long", "java.lang.Long" -> SqlType.BIGINT;
            case "double", "java.lang.Double" -> SqlType.DOUBLE;
            case "float", "java.lang.Float" -> SqlType.FLOAT;
            case "boolean", "java.lang.Boolean" -> SqlType.BOOLEAN;
            case "java.time.LocalDateTime" -> SqlType.TIMESTAMP;
            case "java.time.LocalDate" -> SqlType.DATE;
            case "java.time.LocalTime" -> SqlType.TIME;
            case "java.math.BigDecimal" -> SqlType.DECIMAL;
            case "java.util.UUID" -> SqlType.UUID;
            case "[B" -> SqlType.BLOB; // byte array
            default -> SqlType.VARCHAR; // Default fallback
        };
    }
}
```

#### 3. Entity Inheritance Analyzer
```java
public class EntityInheritanceAnalyzer {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(EntityInheritanceAnalyzer.class);
    
    public EntityInheritanceHierarchy analyzeInheritance(List<EntityClass> entityClasses, JarContent jarContent) {
        LOGGER.info("Analyzing entity inheritance hierarchy");
        
        EntityInheritanceHierarchy.Builder builder = EntityInheritanceHierarchy.builder();
        
        // Identifica root entities (con @Inheritance)
        List<EntityClass> rootEntities = findRootEntities(entityClasses);
        
        for (EntityClass rootEntity : rootEntities) {
            EntityInheritanceTree tree = buildInheritanceTree(rootEntity, entityClasses);
            builder.addInheritanceTree(tree);
        }
        
        // Identifica mapped superclasses
        List<MappedSuperclass> mappedSuperclasses = findMappedSuperclasses(jarContent);
        builder.mappedSuperclasses(mappedSuperclasses);
        
        return builder.build();
    }
    
    private List<EntityClass> findRootEntities(List<EntityClass> entityClasses) {
        return entityClasses.stream()
            .filter(this::hasInheritanceAnnotation)
            .collect(Collectors.toList());
    }
    
    private boolean hasInheritanceAnnotation(EntityClass entityClass) {
        return hasAnnotation(entityClass.getClassInfo(), "javax.persistence.Inheritance", "jakarta.persistence.Inheritance");
    }
    
    private EntityInheritanceTree buildInheritanceTree(EntityClass rootEntity, List<EntityClass> allEntities) {
        EntityInheritanceTree.Builder builder = EntityInheritanceTree.builder()
            .rootEntity(rootEntity);
        
        // Estrai inheritance strategy
        InheritanceType inheritanceType = extractInheritanceType(rootEntity);
        builder.inheritanceType(inheritanceType);
        
        // Estrai discriminator information
        DiscriminatorInfo discriminatorInfo = extractDiscriminatorInfo(rootEntity);
        builder.discriminatorInfo(discriminatorInfo);
        
        // Trova child entities
        List<EntityClass> childEntities = findChildEntities(rootEntity, allEntities);
        builder.childEntities(childEntities);
        
        // Per ogni child, estrai discriminator value
        Map<String, String> discriminatorValues = new HashMap<>();
        for (EntityClass childEntity : childEntities) {
            String discriminatorValue = extractDiscriminatorValue(childEntity);
            if (discriminatorValue != null) {
                discriminatorValues.put(childEntity.getClassName(), discriminatorValue);
            }
        }
        builder.discriminatorValues(discriminatorValues);
        
        return builder.build();
    }
    
    private InheritanceType extractInheritanceType(EntityClass entityClass) {
        AnnotationInfo inheritanceAnnotation = findAnnotation(entityClass.getClassInfo(), 
            "javax.persistence.Inheritance", "jakarta.persistence.Inheritance");
            
        if (inheritanceAnnotation != null) {
            String strategy = (String) inheritanceAnnotation.getAttributes().getOrDefault("strategy", "SINGLE_TABLE");
            return InheritanceType.valueOf(strategy);
        }
        
        return InheritanceType.SINGLE_TABLE; // Default
    }
    
    private DiscriminatorInfo extractDiscriminatorInfo(EntityClass entityClass) {
        DiscriminatorInfo.Builder builder = DiscriminatorInfo.builder();
        
        // @DiscriminatorColumn
        AnnotationInfo discriminatorColumnAnnotation = findAnnotation(entityClass.getClassInfo(),
            "javax.persistence.DiscriminatorColumn", "jakarta.persistence.DiscriminatorColumn");
            
        if (discriminatorColumnAnnotation != null) {
            Map<String, Object> attributes = discriminatorColumnAnnotation.getAttributes();
            builder.columnName((String) attributes.getOrDefault("name", "DTYPE"))
                   .discriminatorType(DiscriminatorType.valueOf((String) attributes.getOrDefault("discriminatorType", "STRING")))
                   .length((Integer) attributes.getOrDefault("length", 31));
        } else {
            builder.columnName("DTYPE")
                   .discriminatorType(DiscriminatorType.STRING)
                   .length(31);
        }
        
        return builder.build();
    }
    
    private List<EntityClass> findChildEntities(EntityClass parentEntity, List<EntityClass> allEntities) {
        String parentClassName = parentEntity.getClassName();
        
        return allEntities.stream()
            .filter(entity -> !entity.getClassName().equals(parentClassName))
            .filter(entity -> isChildEntity(entity, parentClassName))
            .collect(Collectors.toList());
    }
    
    private boolean isChildEntity(EntityClass entityClass, String parentClassName) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.get(entityClass.getClassName());
            CtClass superClass = ctClass.getSuperclass();
            
            while (superClass != null && !superClass.getName().equals("java.lang.Object")) {
                if (superClass.getName().equals(parentClassName)) {
                    return true;
                }
                superClass = superClass.getSuperclass();
            }
            
            return false;
        } catch (Exception e) {
            LOGGER.debug("Error checking inheritance for {}", entityClass.getClassName(), e);
            return false;
        }
    }
    
    private List<MappedSuperclass> findMappedSuperclasses(JarContent jarContent) {
        return jarContent.getClasses().stream()
            .filter(classInfo -> hasAnnotation(classInfo, "javax.persistence.MappedSuperclass", "jakarta.persistence.MappedSuperclass"))
            .map(this::createMappedSuperclass)
            .collect(Collectors.toList());
    }
    
    private MappedSuperclass createMappedSuperclass(ClassInfo classInfo) {
        return MappedSuperclass.builder()
            .className(classInfo.getFullyQualifiedName())
            .fields(extractMappedSuperclassFields(classInfo))
            .build();
    }
}
```

#### 4. Entity Validation Analyzer
```java
public class EntityValidationAnalyzer {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(EntityValidationAnalyzer.class);
    
    private final List<EntityValidationRule> validationRules;
    
    public EntityValidationAnalyzer() {
        this.validationRules = initializeValidationRules();
    }
    
    public List<EntityValidationIssue> validateEntities(List<EntityClass> entityClasses, 
                                                       List<EmbeddableClass> embeddableClasses) {
        LOGGER.info("Validating {} entities and {} embeddables", entityClasses.size(), embeddableClasses.size());
        
        List<EntityValidationIssue> issues = new ArrayList<>();
        
        // Valida ogni entity
        for (EntityClass entityClass : entityClasses) {
            for (EntityValidationRule rule : validationRules) {
                issues.addAll(rule.validate(entityClass));
            }
        }
        
        // Valida cross-entity relationships
        issues.addAll(validateCrossEntityRelationships(entityClasses));
        
        LOGGER.info("Found {} validation issues", issues.size());
        return issues;
    }
    
    private List<EntityValidationRule> initializeValidationRules() {
        List<EntityValidationRule> rules = new ArrayList<>();
        
        // Basic entity validation rules
        rules.add(new EntityMustHavePrimaryKeyRule());
        rules.add(new EntityMustHaveNoArgConstructorRule());
        rules.add(new EntityFieldsValidationRule());
        rules.add(new EntityNamingConventionRule());
        
        // JPA-specific rules
        rules.add(new SerializableIdClassRule());
        rules.add(new ValidGenerationStrategyRule());
        rules.add(new ValidRelationshipMappingRule());
        rules.add(new NoCircularRelationshipRule());
        
        // Performance rules
        rules.add(new AvoidLazyInitializationIssuesRule());
        rules.add(new IndexRecommendationRule());
        
        return rules;
    }
    
    // Example validation rule
    public static class EntityMustHavePrimaryKeyRule implements EntityValidationRule {
        @Override
        public List<EntityValidationIssue> validate(EntityClass entityClass) {
            List<EntityValidationIssue> issues = new ArrayList<>();
            
            boolean hasPrimaryKey = entityClass.getFields().stream()
                .anyMatch(field -> hasAnnotation(field, "javax.persistence.Id", "jakarta.persistence.Id"));
                
            if (!hasPrimaryKey) {
                issues.add(EntityValidationIssue.builder()
                    .severity(ValidationSeverity.ERROR)
                    .ruleType(ValidationRuleType.MISSING_PRIMARY_KEY)
                    .entityClass(entityClass.getClassName())
                    .message("Entity must have a primary key field annotated with @Id")
                    .suggestion("Add an @Id annotation to a field or create a composite primary key")
                    .build());
            }
            
            return issues;
        }
    }
    
    public static class EntityMustHaveNoArgConstructorRule implements EntityValidationRule {
        @Override
        public List<EntityValidationIssue> validate(EntityClass entityClass) {
            List<EntityValidationIssue> issues = new ArrayList<>();
            
            try {
                ClassPool pool = ClassPool.getDefault();
                CtClass ctClass = pool.get(entityClass.getClassName());
                
                boolean hasNoArgConstructor = false;
                boolean hasOnlyDefaultConstructor = ctClass.getConstructors().length == 0;
                
                if (hasOnlyDefaultConstructor) {
                    hasNoArgConstructor = true; // Default constructor provided by compiler
                } else {
                    for (CtConstructor constructor : ctClass.getConstructors()) {
                        if (constructor.getParameterTypes().length == 0) {
                            hasNoArgConstructor = true;
                            break;
                        }
                    }
                }
                
                if (!hasNoArgConstructor) {
                    issues.add(EntityValidationIssue.builder()
                        .severity(ValidationSeverity.ERROR)
                        .ruleType(ValidationRuleType.MISSING_NO_ARG_CONSTRUCTOR)
                        .entityClass(entityClass.getClassName())
                        .message("Entity must have a no-argument constructor")
                        .suggestion("Add a public or protected no-argument constructor")
                        .build());
                }
                
            } catch (Exception e) {
                // Log error but don't fail validation
                LOGGER.debug("Error validating constructor for {}", entityClass.getClassName(), e);
            }
            
            return issues;
        }
    }
    
    private List<EntityValidationIssue> validateCrossEntityRelationships(List<EntityClass> entityClasses) {
        List<EntityValidationIssue> issues = new ArrayList<>();
        
        // Valida che le relazioni puntino a entità esistenti
        for (EntityClass entityClass : entityClasses) {
            for (EntityField field : entityClass.getFields()) {
                if (isRelationshipField(field)) {
                    String targetEntity = extractRelationshipTargetEntity(field);
                    if (targetEntity != null && !isEntityClassExists(targetEntity, entityClasses)) {
                        issues.add(EntityValidationIssue.builder()
                            .severity(ValidationSeverity.ERROR)
                            .ruleType(ValidationRuleType.INVALID_RELATIONSHIP_TARGET)
                            .entityClass(entityClass.getClassName())
                            .fieldName(field.getFieldName())
                            .message(String.format("Relationship field '%s' targets non-existent entity '%s'", 
                                field.getFieldName(), targetEntity))
                            .suggestion("Ensure the target entity class exists and is properly annotated with @Entity")
                            .build());
                    }
                }
            }
        }
        
        return issues;
    }
    
    private boolean isEntityClassExists(String className, List<EntityClass> entityClasses) {
        return entityClasses.stream()
            .anyMatch(entity -> entity.getClassName().equals(className));
    }
}
```

### Principi SOLID Applicati
- **SRP**: Extractor, analyzer, validator separati per responsabilità specifiche
- **OCP**: Facilmente estendibile per nuovi validation rules e pattern detection
- **LSP**: Implementazioni sostituibili per diversi provider JPA
- **ISP**: Interfacce specifiche per metadata extraction, validation, analysis
- **DIP**: Dipende da abstractions per entity processing

### Test Unitari da Implementare
```java
// EntityAnalyzerTest.java
@Test
void shouldFindEntityClasses() {
    // Arrange
    JarContent jarContent = createJarWithEntityClasses();
    EntityAnalyzer analyzer = new JavassistEntityAnalyzer();
    
    // Act
    EntityAnalysisResult result = analyzer.analyzeEntities(jarContent);
    
    // Assert
    assertThat(result.isSuccessful()).isTrue();
    assertThat(result.getEntityClasses()).hasSize(3);
    
    EntityClass userEntity = result.findEntityClass("com.example.entity.User");
    assertThat(userEntity).isNotNull();
    assertThat(userEntity.getEntityAnnotation()).isNotNull();
}

@Test
void shouldExtractEntityMetadata() {
    EntityClass userEntity = createUserEntityClass();
    JarContent jarContent = JarContent.builder().addClass(userEntity.getClassInfo()).build();
    
    EntityMetadataExtractor extractor = new EntityMetadataExtractor();
    EntityMetadata metadata = extractor.extractMetadata(userEntity, jarContent);
    
    assertThat(metadata.getEntityName()).isEqualTo("User");
    assertThat(metadata.getTableName()).isEqualTo("users");
    assertThat(metadata.getPrimaryKeyInfo().getPrimaryKeyType()).isEqualTo(PrimaryKeyType.SINGLE);
    assertThat(metadata.getColumnMappings()).containsKey("username");
}

@Test
void shouldAnalyzeEntityInheritance() {
    List<EntityClass> entityClasses = createInheritanceHierarchy(); // Person -> Employee, Customer
    EntityInheritanceAnalyzer analyzer = new EntityInheritanceAnalyzer();
    
    EntityInheritanceHierarchy hierarchy = analyzer.analyzeInheritance(entityClasses, createMockJarContent());
    
    assertThat(hierarchy.getInheritanceTrees()).hasSize(1);
    EntityInheritanceTree tree = hierarchy.getInheritanceTrees().get(0);
    assertThat(tree.getRootEntity().getClassName()).contains("Person");
    assertThat(tree.getChildEntities()).hasSize(2);
    assertThat(tree.getInheritanceType()).isEqualTo(InheritanceType.SINGLE_TABLE);
}

@Test
void shouldValidateEntityDefinitions() {
    EntityClass invalidEntity = createEntityWithoutPrimaryKey();
    EntityValidationAnalyzer validator = new EntityValidationAnalyzer();
    
    List<EntityValidationIssue> issues = validator.validateEntities(List.of(invalidEntity), List.of());
    
    assertThat(issues).isNotEmpty();
    assertThat(issues).anyMatch(issue -> 
        issue.getRuleType() == ValidationRuleType.MISSING_PRIMARY_KEY
    );
}

@Test
void shouldDetectEntityAntiPatterns() {
    EntityClass godEntity = createGodEntity(50); // Entity with 50+ fields
    EntityAntiPatternDetector detector = new EntityAntiPatternDetector();
    
    List<EntityAntiPattern> antiPatterns = detector.detectAntiPatterns(List.of(godEntity), List.of());
    
    assertThat(antiPatterns).anyMatch(pattern -> 
        pattern.getPatternType() == EntityAntiPatternType.GOD_ENTITY
    );
}

// EntityMetadataExtractorTest.java
@Test
void shouldExtractColumnMappings() {
    EntityClass entityWithColumns = createEntityWithVariousColumnTypes();
    EntityMetadataExtractor extractor = new EntityMetadataExtractor();
    
    EntityMetadata metadata = extractor.extractMetadata(entityWithColumns, createMockJarContent());
    
    Map<String, ColumnMapping> columns = metadata.getColumnMappings();
    assertThat(columns).containsKey("name");
    assertThat(columns.get("name").getColumnName()).isEqualTo("user_name");
    assertThat(columns.get("name").isNullable()).isFalse();
    assertThat(columns.get("name").getLength()).isEqualTo(100);
}

@Test
void shouldExtractGenerationStrategy() {
    EntityField idFieldWithSequence = createIdFieldWithSequenceGenerator();
    EntityMetadataExtractor extractor = new EntityMetadataExtractor();
    
    GenerationStrategy strategy = extractor.extractGenerationStrategy(idFieldWithSequence);
    
    assertThat(strategy.getStrategy()).isEqualTo(GenerationType.SEQUENCE);
    assertThat(strategy.getGenerator()).isEqualTo("user_sequence");
}

@Test
void shouldExtractIndexInformation() {
    EntityClass entityWithIndexes = createEntityWithIndexes();
    EntityMetadataExtractor extractor = new EntityMetadataExtractor();
    
    EntityMetadata metadata = extractor.extractMetadata(entityWithIndexes, createMockJarContent());
    
    List<IndexInfo> indexes = metadata.getIndexes();
    assertThat(indexes).isNotEmpty();
    assertThat(indexes).anyMatch(index -> 
        index.getName().equals("idx_user_email") && index.isUnique()
    );
}

// EntityValidationAnalyzerTest.java
@Test
void shouldValidateNoArgConstructor() {
    EntityClass entityWithoutNoArgConstructor = createEntityWithParameterizedConstructorOnly();
    EntityMustHaveNoArgConstructorRule rule = new EntityMustHaveNoArgConstructorRule();
    
    List<EntityValidationIssue> issues = rule.validate(entityWithoutNoArgConstructor);
    
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).getRuleType()).isEqualTo(ValidationRuleType.MISSING_NO_ARG_CONSTRUCTOR);
    assertThat(issues.get(0).getSeverity()).isEqualTo(ValidationSeverity.ERROR);
}

@Test
void shouldValidateRelationshipTargets() {
    EntityClass orderEntity = createOrderEntityWithInvalidCustomerReference();
    List<EntityClass> allEntities = List.of(orderEntity); // Missing Customer entity
    EntityValidationAnalyzer validator = new EntityValidationAnalyzer();
    
    List<EntityValidationIssue> issues = validator.validateEntities(allEntities, List.of());
    
    assertThat(issues).anyMatch(issue -> 
        issue.getRuleType() == ValidationRuleType.INVALID_RELATIONSHIP_TARGET
    );
}

// Integration Test
@Test
void shouldAnalyzeCompleteEntityModel() throws IOException {
    Path springBootJar = getTestResourcePath("jpa-application.jar");
    JarContent jarContent = new JarLoader().loadJar(new JarLocation(springBootJar));
    EntityAnalyzer analyzer = new JavassistEntityAnalyzer();
    
    EntityAnalysisResult result = analyzer.analyzeEntities(jarContent);
    
    // Verifiche su applicazione con JPA reale
    assertThat(result.getEntityClasses()).hasSizeGreaterThan(5);
    assertThat(result.getMetrics().getAverageFieldsPerEntity()).isBetween(3.0, 20.0);
    assertThat(result.getValidationIssues()).isNotNull();
    
    // Verifica che esistano relazioni
    boolean hasRelationships = result.getEntityClasses().stream()
        .anyMatch(entity -> entity.getFields().stream()
            .anyMatch(field -> isRelationshipField(field)));
    assertThat(hasRelationships).isTrue();
}
```

---

## T4.1.2: RelationshipAnalyzer per @OneToMany, @ManyToOne, etc.

### Descrizione Dettagliata
Analyzer avanzato per mappare e analizzare tutte le relazioni JPA tra entità, identificando ownership, cascade operations, fetch strategies e possibili problemi di performance nelle relazioni database.

### Scopo dell'Attività
- Identificare e classificare tutte le relazioni JPA (@OneToOne, @OneToMany, @ManyToOne, @ManyToMany)
- Analizzare ownership delle relazioni e foreign key mappings
- Rilevare cascade operations e fetch strategies
- Identificare problemi di performance (N+1 queries, lazy loading issues)
- Mappare join tables e join columns

### Impatti su Altri Moduli
- **DatabaseSchemaBuilder**: Relazioni per foreign keys e join tables
- **QueryAnalyzer**: Context per query optimization analysis
- **PerformanceAnalyzer**: Input per lazy loading e N+1 detection
- **EntityAnalyzer**: Completamento dell'analisi entity model

### Componenti da Implementare

#### 1. Relationship Analyzer Core
```java
public interface RelationshipAnalyzer {
    RelationshipAnalysisResult analyzeRelationships(List<EntityClass> entityClasses, JarContent jarContent);
}

public class JavassistRelationshipAnalyzer implements RelationshipAnalyzer {
    private static final JReverseLogger LOGGER = JReverseLogger.getLogger(JavassistRelationshipAnalyzer.class);
    
    private static final Map<String, RelationType> RELATIONSHIP_ANNOTATIONS = Map.of(
        "javax.persistence.OneToOne", RelationType.ONE_TO_ONE,
        "jakarta.persistence.OneToOne", RelationType.ONE_TO_ONE,
        "javax.persistence.OneToMany", RelationType.ONE_TO_MANY,
        "jakarta.persistence.OneToMany", RelationType.ONE_TO_MANY,
        "javax.persistence.ManyToOne", RelationType.MANY_TO_ONE,
        "jakarta.persistence.ManyToOne", RelationType.MANY_TO_ONE,
        "javax.persistence.ManyToMany", RelationType.MANY_TO_MANY,
        "jakarta.persistence.ManyToMany", RelationType.MANY_TO_MANY
    );
    
    private final RelationshipExtractor relationshipExtractor;
    private final RelationshipValidator relationshipValidator;
    private final CascadeOperationAnalyzer cascadeAnalyzer;
    private final FetchStrategyAnalyzer fetchAnalyzer;
    private final RelationshipPerformanceAnalyzer performanceAnalyzer;
    
    public JavassistRelationshipAnalyzer() {
        this.relationshipExtractor = new RelationshipExtractor();
        this.relationshipValidator = new RelationshipValidator();
        this.cascadeAnalyzer = new CascadeOperationAnalyzer();
        this.fetchAnalyzer = new FetchStrategyAnalyzer();
        this.performanceAnalyzer = new RelationshipPerformanceAnalyzer();
    }
    
    @Override
    public RelationshipAnalysisResult analyzeRelationships(List<EntityClass> entityClasses, JarContent jarContent) {
        LOGGER.startOperation("Relationship analysis");
        long startTime = System.currentTimeMillis();
        
        try {
            RelationshipAnalysisResult.Builder resultBuilder = RelationshipAnalysisResult.builder();
            
            // Estrai tutte le relazioni
            List<EntityRelationship> relationships = relationshipExtractor.extractRelationships(entityClasses);
            resultBuilder.relationships(relationships);
            
            LOGGER.info("Found {} relationships", relationships.size());
            
            // Analizza ownership delle relazioni
            RelationshipOwnershipAnalysis ownershipAnalysis = analyzeOwnership(relationships);
            resultBuilder.ownershipAnalysis(ownershipAnalysis);
            
            // Analizza cascade operations
            CascadeAnalysisResult cascadeAnalysis = cascadeAnalyzer.analyzeCascadeOperations(relationships);
            resultBuilder.cascadeAnalysis(cascadeAnalysis);
            
            // Analizza fetch strategies
            FetchStrategyAnalysisResult fetchAnalysis = fetchAnalyzer.analyzeFetchStrategies(relationships);
            resultBuilder.fetchAnalysis(fetchAnalysis);
            
            // Valida relazioni
            List<RelationshipValidationIssue> validationIssues = relationshipValidator.validateRelationships(relationships, entityClasses);
            resultBuilder.validationIssues(validationIssues);
            
            // Analizza performance implications
            RelationshipPerformanceAnalysis performanceAnalysis = performanceAnalyzer.analyzePerformanceImplications(relationships);
            resultBuilder.performanceAnalysis(performanceAnalysis);
            
            // Costruisci relationship graph
            RelationshipGraph relationshipGraph = buildRelationshipGraph(relationships, entityClasses);
            resultBuilder.relationshipGraph(relationshipGraph);
            
            // Calcola metriche
            RelationshipMetrics metrics = calculateRelationshipMetrics(relationships, entityClasses);
            resultBuilder.metrics(metrics);
            
            return resultBuilder
                .analysisMetadata(AnalysisMetadata.successful(System.currentTimeMillis() - startTime))
                .build();
                
        } catch (Exception e) {
            LOGGER.error("Relationship analysis failed", e);
            return RelationshipAnalysisResult.failed("Analysis failed: " + e.getMessage());
        } finally {
            LOGGER.endOperation("Relationship analysis", System.currentTimeMillis() - startTime);
        }
    }
    
    private RelationshipOwnershipAnalysis analyzeOwnership(List<EntityRelationship> relationships) {
        RelationshipOwnershipAnalysis.Builder builder = RelationshipOwnershipAnalysis.builder();
        
        // Raggruppa per coppie di entità
        Map<EntityPair, List<EntityRelationship>> relationshipPairs = relationships.stream()
            .collect(Collectors.groupingBy(rel -> 
                new EntityPair(rel.getSourceEntity(), rel.getTargetEntity())));
        
        for (Map.Entry<EntityPair, List<EntityRelationship>> entry : relationshipPairs.entrySet()) {
            EntityPair pair = entry.getKey();
            List<EntityRelationship> pairRelationships = entry.getValue();
            
            RelationshipOwnership ownership = determineOwnership(pairRelationships);
            builder.addOwnership(pair, ownership);
        }
        
        return builder.build();
    }
    
    private RelationshipOwnership determineOwnership(List<EntityRelationship> relationships) {
        // Cerca @JoinColumn per determinare ownership
        for (EntityRelationship rel : relationships) {
            if (rel.getJoinColumn() != null) {
                return RelationshipOwnership.builder()
                    .ownerEntity(rel.getSourceEntity())
                    .ownedEntity(rel.getTargetEntity())
                    .ownershipType(OwnershipType.FOREIGN_KEY)
                    .joinColumn(rel.getJoinColumn())
                    .build();
            }
        }
        
        // Cerca mappedBy per determinare inverse side
        for (EntityRelationship rel : relationships) {
            if (rel.getMappedBy() != null && !rel.getMappedBy().isEmpty()) {
                return RelationshipOwnership.builder()
                    .ownerEntity(findOwnerFromMappedBy(rel, relationships))
                    .ownedEntity(rel.getSourceEntity())
                    .ownershipType(OwnershipType.MAPPED_BY)
                    .mappedByField(rel.getMappedBy())
                    .build();
            }
        }
        
        // Default: first entity is owner (per convenzione JPA)
        EntityRelationship firstRel = relationships.get(0);
        return RelationshipOwnership.builder()
            .ownerEntity(firstRel.getSourceEntity())
            .ownedEntity(firstRel.getTargetEntity())
            .ownershipType(OwnershipType.DEFAULT)
            .build();
    }
    
    private RelationshipGraph buildRelationshipGraph(List<EntityRelationship> relationships, 
                                                   List<EntityClass> entityClasses) {
        RelationshipGraph.Builder graphBuilder = RelationshipGraph.builder();
        
        // Aggiungi tutti i nodi (entità)
        for (EntityClass entityClass : entityClasses) {
            graphBuilder.addNode(entityClass.getClassName());
        }
        
        // Aggiungi gli archi (relazioni)
        for (EntityRelationship relationship : relationships) {
            RelationshipEdge edge = RelationshipEdge.builder()
                .sourceEntity(relationship.getSourceEntity())
                .targetEntity(relationship.getTargetEntity())
                .relationType(relationship.getRelationType())
                .fieldName(relationship.getFieldName())
                .cascadeOperations(relationship.getCascadeOperations())
                .fetchType(relationship.getFetchType())
                .build();
                
            graphBuilder.addEdge(edge);
        }
        
        return graphBuilder.build();
    }
}
```

Continua con il resto del file per la Fase 4...
