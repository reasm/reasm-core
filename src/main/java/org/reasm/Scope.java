package org.reasm;

import javax.annotation.Nonnull;

/**
 * Represents a scope for local symbols, delimited by nonlocal symbols.
 *
 * @author Francis Gagné
 */
public final class Scope {

    @Nonnull
    private final SymbolTable localSymbolTable = new SymbolTable();

    @Nonnull
    private final Iterable<UserSymbol> localSymbolsProxy = new IterableProxy<>(this.localSymbolTable);

    /**
     * Initializes a new scope.
     */
    Scope() {
    }

    /**
     * Gets the local symbols for this scope.
     *
     * @return the local symbols
     */
    @Nonnull
    public final Iterable<UserSymbol> getLocalSymbols() {
        return this.localSymbolsProxy;
    }

    @Nonnull
    final SymbolTable getLocalSymbolTable() {
        return this.localSymbolTable;
    }

}
