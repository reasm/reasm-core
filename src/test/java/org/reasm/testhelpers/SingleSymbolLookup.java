package org.reasm.testhelpers;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.Symbol;
import org.reasm.expressions.SymbolLookup;

/**
 * An implementation of {@link SymbolLookup} that knows a single symbol name.
 *
 * @author Francis Gagné
 */
public final class SingleSymbolLookup implements SymbolLookup {

    @Nonnull
    private final String symbolName;
    @Nonnull
    private final Symbol symbol;

    /**
     * Initializes a new SingleSymbolLookup.
     *
     * @param symbolName
     *            the name of the single symbol known by this lookup object
     * @param symbol
     *            the symbol
     */
    public SingleSymbolLookup(@Nonnull String symbolName, @Nonnull Symbol symbol) {
        this.symbolName = symbolName;
        this.symbol = symbol;
    }

    @CheckForNull
    @Override
    public Symbol getSymbol(@Nonnull String name) {
        if (this.symbolName.equalsIgnoreCase(name)) {
            return this.symbol;
        }

        throw new AssertionError("Unexpected symbol lookup attempted on name \"" + name + "\"");
    }

}
