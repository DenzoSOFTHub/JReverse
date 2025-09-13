# JReverse - Product Requirements Document

## 1. Panoramica del Prodotto

**Nome Prodotto:** JReverse  
**Versione:** 1.0  
**Tipo:** Tool di Reverse Engineering per applicazioni Java/Spring Boot  

### 1.1 Descrizione
JReverse è uno strumento desktop con interfaccia Swing che permette di analizzare file JAR (semplici e Spring Boot) per generare report HTML dettagliati sull'architettura, struttura e qualità del codice dell'applicazione.

### 1.2 Obiettivi
- Facilitare la comprensione di applicazioni Java esistenti
- Fornire analisi automatizzate per migliorare la manutenibilità del codice
- Identificare problemi di sicurezza e performance
- Generare documentazione tecnica automatica

## 2. Stakeholder

- **Sviluppatori Java/Spring Boot** - utenti primari
- **Architetti Software** - analisi architetturale
- **Team di QA** - analisi qualità codice
- **Security Engineers** - analisi sicurezza

## 3. Requisiti Funzionali

### 3.1 Interfaccia Utente
- **RF-001**: Interfaccia grafica Swing con selezione file JAR
- **RF-002**: Wizard per selezione tipologie di report da generare
- **RF-003**: Progress bar per visualizzare avanzamento analisi
- **RF-004**: Visualizzazione errori e log di elaborazione

### 3.2 Analisi del JAR
- **RF-005**: Caricamento e parsing di JAR semplici
- **RF-006**: Caricamento e parsing di JAR Spring Boot
- **RF-007**: Estrazione metadati classi tramite Javassist
- **RF-008**: Identificazione annotazioni Spring e JPA

### 3.3 Generazione Report (50 tipologie)

#### 3.3.1 Entrypoint e Flussi Principali (1-10)
- **RF-009**: Mappa endpoint REST (path, HTTP method, parametri)
- **RF-010**: Call graph richieste HTTP (controller → service → repository)
- **RF-011**: Identificazione main method Spring Boot
- **RF-012**: Analisi ciclo bootstrap Spring Boot
- **RF-013**: Autowiring graph (dependency injection)
- **RF-014**: Mappa controller REST e metodi
- **RF-015**: Report service layer e metodi business
- **RF-016**: Mappa repository/database layer
- **RF-017**: Analisi eventi e listener Spring
- **RF-018**: Sequenze chiamate asincrone

#### 3.3.2 Architettura e Dipendenze (11-20)
- **RF-019**: Mappa package e classi (albero gerarchico)
- **RF-020**: Diagramma UML classi principali
- **RF-021**: Dipendenze tra package (accoppiamento/cicli)
- **RF-022**: Dipendenze moduli/librerie esterne
- **RF-023**: Bean configuration report
- **RF-024**: Proprietà configurazione (properties/yml)
- **RF-025**: Profili Spring attivi
- **RF-026**: Report annotazioni custom
- **RF-027**: Mapping gestione eccezioni
- **RF-028**: Analisi dependency injection circolari

#### 3.3.3 Persistenza e Database (21-30)
- **RF-029**: Mappa entità JPA
- **RF-030**: Relazioni tra entità
- **RF-031**: Schema DB ricostruito
- **RF-032**: Query native e JPQL
- **RF-033**: Metodi custom repository Spring Data
- **RF-034**: Report configurazioni cache
- **RF-035**: Analisi uso transazioni
- **RF-036**: Analisi datasource configurati
- **RF-037**: Mappa migrazioni DB (Liquibase/Flyway)
- **RF-038**: Ottimizzazione query SQL

#### 3.3.4 Metriche e Qualità Codice (31-40)
- **RF-039**: Metriche complessità ciclomatica
- **RF-040**: Lunghezza media classi/metodi
- **RF-041**: Identificazione code smells
- **RF-042**: Analisi coesione/accoppiamento
- **RF-043**: Coverage potenziale test
- **RF-044**: Analisi uso logging
- **RF-045**: Punti critici performance
- **RF-046**: Identificazione dead code
- **RF-047**: Analisi uso reflection
- **RF-048**: Thread creation diretta

#### 3.3.5 Sicurezza e Robustezza (41-50)
- **RF-049**: Configurazioni Spring Security
- **RF-050**: Endpoint non protetti
- **RF-051**: Annotazioni sicurezza
- **RF-052**: Dipendenze vulnerabili (CVE)
- **RF-053**: Uso crittografia/password
- **RF-054**: Input non validati
- **RF-055**: Eccezioni runtime non gestite
- **RF-056**: Configurazioni pericolose
- **RF-057**: Esposizione stack trace
- **RF-058**: Analisi versioni server embedded

### 3.4 Output e Export
- **RF-059**: Generazione report HTML responsive
- **RF-060**: Navigazione tra sezioni report
- **RF-061**: Export configurazioni analisi
- **RF-062**: Salvataggio risultati per confronti futuri

## 4. Requisiti Non Funzionali

### 4.1 Performance
- **RNF-001**: Analisi JAR fino a 100MB in max 5 minuti
- **RNF-002**: Consumo memoria massimo 2GB
- **RNF-003**: Interfaccia responsiva durante elaborazione

### 4.2 Usabilità
- **RNF-004**: Interfaccia intuitiva senza training
- **RNF-005**: Wizard max 3 step per configurazione
- **RNF-006**: Messaggi errore comprensibili

### 4.3 Compatibilità
- **RNF-007**: Java 8+ compatibilità
- **RNF-008**: JAR Spring Boot versioni 1.5+
- **RNF-009**: Cross-platform (Windows/Linux/macOS)

### 4.4 Manutenibilità
- **RNF-010**: Architettura modulare per estensioni
- **RNF-011**: Logging completo delle operazioni
- **RNF-012**: Configurazione esterna parametri

## 5. Vincoli Tecnici

### 5.1 Tecnologie Obbligatorie
- **Java** come linguaggio principale
- **Javassist** come unica libreria esterna per bytecode analysis
- **Swing** per interfaccia grafica
- **HTML/CSS/JavaScript** per report output

### 5.2 Limitazioni
- No dipendenze esterne oltre Javassist
- No accesso a database runtime dell'applicazione analizzata
- Analisi statica del codice (no esecuzione)

## 6. Criteri di Accettazione

### 6.1 Funzionali
- Analisi corretta di almeno 95% delle classi standard Java/Spring
- Generazione completa di tutti i 50 tipi di report
- Interfaccia funzionante su tutti i SO target

### 6.2 Qualità
- Zero memory leak durante elaborazione
- Gestione errori graceful per JAR corrotti
- Report HTML validato W3C

## 7. Rischi e Mitigazioni

| Rischio | Probabilità | Impatto | Mitigazione |
|---------|-------------|---------|-------------|
| Limitazioni Javassist per analisi complesse | Media | Alto | Prototipo early per validare capacità |
| Performance su JAR grandi | Alta | Medio | Implementazione lazy loading e ottimizzazioni |
| Complessità Spring Boot | Alta | Alto | Focus su versioni più comuni inizialmente |

## 8. Success Metrics

- **Adozione**: 100+ download nel primo mese
- **Utilizzo**: Analisi media di 10+ JAR per utente
- **Qualità**: <5% error rate su JAR standard
- **Performance**: <3 minuti per JAR tipici (20-50MB)