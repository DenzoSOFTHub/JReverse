# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

JReverse is a Java reverse engineering tool that analyzes JAR files (both regular JAR and Spring Boot) to generate comprehensive HTML reports on application architecture, structure, and code quality. The tool uses **only Javassist** as an external dependency for bytecode analysis and features a Swing desktop interface with a wizard for report selection.

## Architecture

The project follows **Clean Architecture** with multi-module Maven structure:

```
jreverse/
├── jreverse-core/           # Domain layer (entities, value objects)
├── jreverse-analyzer/       # Application layer (use cases, analyzers)
├── jreverse-ui/            # Interface adapter layer (Swing UI)
├── jreverse-reporter/      # Interface adapter layer (HTML generation)
└── jreverse-app/           # Main application assembly
```

### Key Architectural Patterns

- **Factory Pattern**: For analyzer creation (different analyzers for Spring Boot vs regular JAR)
- **Strategy Pattern**: For report generation (50 different report types)
- **Observer Pattern**: For progress notification during analysis
- **Builder Pattern**: For complex object construction (analysis results, configurations)

### Core Components

- **JarLoader**: Extracts and loads JAR content using Javassist
- **SpringBootDetector**: Intelligently detects Spring Boot applications vs regular JARs
- **AnalyzerFactory**: Creates appropriate analyzers based on JAR type
- **ReportGenerator**: Generates HTML reports using strategy pattern

## Development Commands

The project uses Maven for build management:

```bash
# Build the entire project
mvn clean compile

# Run all tests
mvn test

# Run tests for specific module
mvn test -pl jreverse-core

# Run specific test class
mvn test -Dtest=ClassName

# Run tests with coverage report
mvn test jacoco:report

# Package into executable JAR
mvn clean package -pl jreverse-app

# Run the application
java -jar jreverse-app/target/jreverse-app-jar-with-dependencies.jar

# Check code coverage requirements (80% line, 75% branch)
mvn test jacoco:check
```

## 🚨 MANDATORY DEVELOPMENT RULES 🚨

**THESE RULES OVERRIDE ALL OTHER GUIDELINES AND MUST NEVER BE VIOLATED:**

### Rule 1: NO STUB CODE OR FAKE IMPLEMENTATIONS
- **NEVER** create stub implementations that return empty results or throw `UnsupportedOperationException` unless it's the permanent intended behavior
- **NEVER** create fake/mock/dummy implementations just to make tests pass
- **NEVER** simplify functionality or reduce capability to avoid implementation complexity
- **ALWAYS** implement the full, real functionality as specified in requirements

### Rule 2: IMMEDIATE COMPILATION AND TESTING CYCLE
- **COMPILE IMMEDIATELY** after any code change: `mvn compile -pl <module>`
- **FIX ALL COMPILATION ERRORS** immediately - never proceed with broken code
- **WRITE REAL TESTS** that verify actual behavior, not stub behavior
- **RUN TESTS IMMEDIATELY** after writing them: `mvn test -pl <module>`
- **FIX TEST FAILURES** by improving the implementation, never by lowering expectations

### Rule 3: REAL CODE ONLY
- If a feature cannot be fully implemented due to complexity, document the limitation but implement what is feasible
- Tests must verify actual functionality - if code doesn't work, tests should fail until code is fixed
- No shortcuts, no approximations, no "good enough" implementations

### Rule 4: TEST FILE MANAGEMENT
- **PRODUCTION CODE**: Never rewrite from scratch - always fix and improve existing code
- **TEST FILES**: If a test file has too many errors or is fundamentally flawed:
  1. **Study what the test should accomplish** - understand the intended behavior
  2. **Rewrite the entire test file from scratch** with correct expectations
  3. **Focus on testing real behavior** of the actual implementation
  4. **Remove any references to stub/mock behavior** that doesn't reflect reality
- This rule applies ONLY to test files, NEVER to production code

**VIOLATION OF THESE RULES IS UNACCEPTABLE UNDER ANY CIRCUMSTANCES**

---

## Testing Strategy

- **Unit Tests**: Each analyzer and component has comprehensive unit tests testing real behavior
- **Integration Tests**: Test real JAR analysis workflows
- **Architecture Tests**: Using ArchUnit to validate Clean Architecture boundaries
- **Minimum Coverage**: 80% line coverage, 75% branch coverage

## Implementation Workflow

For each requirement implementation, follow this **mandatory workflow**:

1. **Existing Component Analysis**: Before any new development, thoroughly analyze existing codebase to identify components that already implement (fully or partially) the required functionality:
   - **Search Strategy**: Use `Grep`, `Glob`, and `Task` tools to search for:
     - Similar class names, method names, or functionality patterns
     - Related annotations, interfaces, or base classes
     - Existing analyzer implementations that could be extended
     - Domain models that could be reused or adapted
   - **Discovery Process**:
     - Search for classes with similar names (e.g., when implementing "RestControllerAnalyzer", search for "*Controller*", "*Rest*", "*Endpoint*")
     - Look for interfaces that match the pattern (e.g., "*Analyzer", "*Detector", "*Builder")
     - Check existing factories and configuration classes
     - Review similar analyzers in related modules
   - **Reuse Strategy**:
     - **If exact component exists**: Adapt and extend existing implementation
     - **If partial functionality exists**: Leverage existing components and build upon them
     - **If similar pattern exists**: Follow established patterns and conventions
     - **If base interfaces exist**: Implement using existing port interfaces
   - **Documentation Required**: Document what existing components were found and how they influenced the implementation approach

2. **Technical Analysis & Agent Consultation**: After component analysis, analyze remaining technical challenges and consult specialized agents for optimal approach:
   - Use `/agents` command to launch relevant specialist agents
   - For Java reverse engineering: `java-reverse-engineering-expert`
   - For Swing UI work: `swing-ui-expert` 
   - For complex architecture questions: `general-purpose` agent
   - Ask user directly if no appropriate agent exists
   - Document agent recommendations and rationale

3. **API Deep Analysis**: Before implementation, perform thorough analysis of all APIs and classes that will be used:
   - **Class Structure Analysis**: For each class to be used:
     - Read the complete class to understand all available methods
     - Identify constructors, factory methods, and builder patterns
     - Understand method signatures, parameters, and return types
     - Check for deprecated methods or annotations
   - **Interface Contract Analysis**:
     - Review all interfaces the class implements
     - Understand expected behavior from interface documentation
     - Identify abstract methods that must be implemented
   - **Dependency Analysis**:
     - Map all dependencies required by the classes
     - Understand initialization order and lifecycle
     - Identify potential circular dependencies
   - **Error Handling Patterns**:
     - Identify exceptions thrown by methods
     - Understand error recovery mechanisms
     - Check for null-safety and validation requirements
   - **Usage Pattern Discovery**:
     - Search for existing usage examples in the codebase
     - Understand common patterns and best practices
     - Identify anti-patterns to avoid
   - **Documentation Required**: Create a "Class Usage Guide" documenting:
     - Correct initialization sequences
     - Common method call patterns
     - Error handling requirements
     - Performance considerations

4. **Implement the requirement** following the detailed task specifications, leveraging existing components, API analysis insights, and applying agent recommendations

5. **Immediate Compilation Check**: After implementation, immediately compile to catch errors early:
   - **Compile main sources**: `mvn compile -pl <module-name>`
   - **Verify no compilation errors** in the implementation
   - **If compilation fails**:
     - Analyze error messages carefully
     - Check for missing imports, incorrect API usage, type mismatches
     - Fix all compilation errors before proceeding
     - Re-compile until successful
   - **Why this step**: Catching compilation errors before writing tests saves time and ensures the implementation is syntactically correct
   - **Documentation Required**: Document any API adjustments or fixes needed during compilation

6. **Write comprehensive unit tests** covering all functionality and edge cases
7. **Run test compilation** and ensure test code compiles: `mvn test-compile -pl <module-name>`
8. **Execute all tests** and ensure they pass: `mvn test -pl <module-name>`
9. **Resolve any issues** found during testing
10. **Only after all tests pass**, mark the requirement as implemented
11. **Document in implemented.md** with:
   - Detailed requirement description
   - Implementation approach and design decisions
   - **Existing component analysis results** and reuse decisions
   - Agent consultation results and adopted recommendations
   - **API usage guide** from deep analysis (step 3)
   - **Compilation fixes** applied during step 5 (if any)
   - List of modified/created files
   - Test cases and how to run them
   - Any known limitations or considerations

**No requirement can be considered complete until all tests pass and documentation is updated.**

## Analysis Categories

The tool generates 50 types of analysis reports organized in 5 categories:

1. **Entrypoint & Main Flows (1-10)**: REST endpoints, call graphs, Spring Boot bootstrap
2. **Architecture & Dependencies (11-20)**: Package structure, UML diagrams, dependency analysis
3. **Persistence & Database (21-30)**: JPA entities, relationships, query analysis
4. **Code Quality & Performance (31-40)**: Complexity metrics, code smells, performance bottlenecks
5. **Security & Robustness (41-50)**: Spring Security analysis, vulnerability detection, unsafe configurations

## Development Phases

The project is developed in 6 phases (18 weeks total):

- **Phase 1** (Weeks 1-3): Foundation & Core Infrastructure
- **Phase 2** (Weeks 4-6): Spring Boot Detection & Entrypoint Analysis
- **Phase 3** (Weeks 7-9): Architecture & Dependency Analysis
- **Phase 4** (Weeks 10-12): Database & Persistence Layer Analysis
- **Phase 5** (Weeks 13-15): Code Quality & Performance Metrics
- **Phase 6** (Weeks 16-18): Security Analysis & Final Integration

Detailed task breakdowns for each phase are available in `docs/phase[1-6]-detailed-tasks.md`.

## Key Constraints

- **Single External Dependency**: Only Javassist allowed for bytecode analysis
- **Static Analysis Only**: No runtime execution of analyzed applications
- **Performance Target**: Analyze 100MB JARs in under 5 minutes with max 2GB memory
- **Compatibility**: Java 11+, Spring Boot 1.5+, cross-platform support

## Code Quality Standards

- Follow **SOLID principles** strictly
- Apply **Clean Code** practices
- Use **DRY principle** to avoid duplication
- Implement comprehensive **error handling** with proper logging
- All public APIs must have **Javadoc documentation**
- **No reflection usage** except via Javassist

## Important Files

- `docs/PRD-JReverse.md`: Complete product requirements document
- `docs/Roadmap-JReverse.md`: Detailed 6-phase development roadmap
- `docs/TipiDiAnalisi.txt`: List of all 50 analysis types to implement
- `docs/phase[1-6]-detailed-tasks.md`: Detailed implementation tasks per phase
- `implemented.md`: Tracks completed requirements with implementation details and test coverage

## Diagram Generation Guidelines

For reports requiring visual diagrams (sequence diagrams, UML class diagrams, dependency graphs, etc.), generate **HTML files with embedded JavaScript libraries** for client-side rendering:

- **Recommended Libraries**:
  - **Mermaid.js**: For sequence diagrams, flowcharts, and class diagrams
  - **D3.js**: For complex dependency graphs and network visualizations
  - **vis.js**: For network graphs and hierarchical diagrams
  - **PlantUML.js**: For UML diagrams (client-side PlantUML rendering)

- **Implementation Pattern**:
  ```html
  <div id="diagram-container"></div>
  <script src="https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js"></script>
  <script>
    mermaid.initialize({startOnLoad: true});
    // Diagram definition here
  </script>
  ```

- **Advantages**:
  - No additional dependencies beyond Javassist
  - Interactive diagrams (zoom, pan, click events)
  - Responsive and modern visualization
  - Easy to embed in HTML reports
  - Cross-platform compatibility

- **Avoid**: Server-side diagram generation that would require additional Java libraries

## Testing Real Applications

When implementing analyzers, test against real Spring Boot applications to validate accuracy. The tool should correctly analyze at least 95% of standard Java/Spring Boot classes and patterns.