package org.reasm;

import javax.annotation.CheckForNull;

/**
 * A symbol that has a fixed value, specified at construction.
 *
 * @author Francis Gagné
 */
public final class StaticSymbol extends Symbol {

    @CheckForNull
    private final Value value;

    /**
     * Initializes a new StaticSymbol with the specified value.
     *
     * @param value
     *            the symbol's value
     */
    public StaticSymbol(@CheckForNull Value value) {
        super(null, SymbolType.CONSTANT);
        this.value = value;
    }

    @CheckForNull
    @Override
    public final Value getValue() {
        return this.value;
    }

}
