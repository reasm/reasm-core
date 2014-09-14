package org.reasm.expressions;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.Symbol;
import org.reasm.Value;

/**
 * An interface to look up symbols by their name. Those symbols are expected to have a value of type {@link Value}.
 *
 * @author Francis Gagné
 */
public interface SymbolLookup {

    /**
     * Gets the symbol with the specified name.
     *
     * @param name
     *            the name of the symbol
     * @return the symbol, or <code>null</code> if no symbol with that name exists
     */
    @CheckForNull
    Symbol getSymbol(@Nonnull String name);

}
