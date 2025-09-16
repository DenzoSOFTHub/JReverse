package it.denzosoft.jreverse.core.model;

/**
 * Types of inheritance relationships in Java.
 */
public enum InheritanceType {
    
    /**
     * Class extends another class.
     */
    EXTENDS("extends", "<|--"),
    
    /**
     * Class implements an interface.
     */
    IMPLEMENTS("implements", "<|.."),
    
    /**
     * Interface extends another interface.
     */
    INTERFACE_EXTENDS("extends", "<|--");
    
    private final String keyword;
    private final String symbol;
    
    InheritanceType(String keyword, String symbol) {
        this.keyword = keyword;
        this.symbol = symbol;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    @Override
    public String toString() {
        return keyword;
    }
}