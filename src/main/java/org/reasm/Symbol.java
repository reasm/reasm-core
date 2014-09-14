package org.reasm;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A symbol. Symbols have a name (optional), a type, a value, and to get their value, they must be {@linkplain SymbolReference
 * referenced}.
 * <p>
 * {@link UserSymbol} represents symbols that are defined by the user in source code. You can subclass Symbol to represent other
 * kinds of symbols that the user can reference but not define directly in source code.
 *
 * @author Francis Gagné
 */
public abstract class Symbol {

    @CheckForNull
    private final String name;
    @Nonnull
    private final SymbolType type;

    /**
     * Initializes a new Symbol.
     *
     * @param name
     *            the symbol's name
     * @param type
     *            the symbol's type
     */
    protected Symbol(@CheckForNull String name, @Nonnull SymbolType type) {
        if (type == null) {
            throw new NullPointerException("type");
        }

        this.name = name;
        this.type = type;
    }

    /**
     * Gets the name of this symbol.
     *
     * @return the symbol's name, or <code>null</code> if the symbol has no name
     */
    @CheckForNull
    public final String getName() {
        return this.name;
    }

    /**
     * Gets the type of this symbol.
     *
     * @return the symbol's type: {@link SymbolType#CONSTANT} if the symbol's value never changes, or {@link SymbolType#VARIABLE} if
     *         the symbol's value may change as the assembly progresses
     */
    @Nonnull
    public final SymbolType getType() {
        return this.type;
    }

    /**
     * Gets the value of this symbol.
     *
     * @return the symbol's value
     */
    @CheckForNull
    public abstract Object getValue();

}
