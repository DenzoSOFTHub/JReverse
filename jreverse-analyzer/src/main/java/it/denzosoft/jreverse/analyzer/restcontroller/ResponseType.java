package it.denzosoft.jreverse.analyzer.restcontroller;

/**
 * Enumeration of REST endpoint response types.
 * Categorizes different kinds of response patterns found in Spring MVC controllers.
 */
public enum ResponseType {
    
    VOID("Void Response", "Method returns void, typically for operations with no response body"),
    PRIMITIVE("Primitive Response", "Method returns a primitive type (int, boolean, etc.)"),
    STRING("String Response", "Method returns a String value"),
    OBJECT("Object Response", "Method returns a custom object or standard Java class"),
    COLLECTION("Collection Response", "Method returns a Collection (List, Set, etc.)"),
    MAP("Map Response", "Method returns a Map structure"),
    ARRAY("Array Response", "Method returns an array"),
    RESPONSE_ENTITY("ResponseEntity", "Method returns Spring's ResponseEntity wrapper"),
    OPTIONAL("Optional Response", "Method returns Optional wrapper"),
    FUTURE("Future Response", "Method returns Future/CompletableFuture for async responses"),
    REACTIVE("Reactive Response", "Method returns reactive types (Mono, Flux)"),
    VIEW("View Response", "Method returns view-related types (ModelAndView, View)"),
    UNKNOWN("Unknown Response", "Method return type could not be categorized");
    
    private final String displayName;
    private final String description;
    
    ResponseType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Determines if this response type represents a complex object structure.
     */
    public boolean isComplexType() {
        return this == OBJECT || this == COLLECTION || this == MAP || this == ARRAY;
    }
    
    /**
     * Determines if this response type is wrapped in a container.
     */
    public boolean isWrapped() {
        return this == RESPONSE_ENTITY || this == OPTIONAL || this == FUTURE || this == REACTIVE;
    }
    
    /**
     * Determines if this response type indicates asynchronous processing.
     */
    public boolean isAsync() {
        return this == FUTURE || this == REACTIVE;
    }
    
    /**
     * Determines if this response type typically has a response body.
     */
    public boolean hasResponseBody() {
        return this != VOID && this != VIEW;
    }
}