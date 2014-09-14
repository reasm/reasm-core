package org.reasm;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Provides a means of obtaining a symbol after the main symbol lookup failed to find a {@linkplain UserSymbol user-defined symbol}
 * for a symbol reference.
 *
 * @author Francis Gagné
 * @see Assembly#resolveSymbolReference(List, String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)
 * @see Assembly#resolveSymbolReference(SymbolContext, String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)
 * @see Assembly#resolveSymbolReference(SymbolContext[], String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)
 * @see AssemblyBuilder#resolveSymbolReference(List, String, boolean, boolean, SymbolLookupContext, SymbolResolutionFallback)
 * @see AssemblyBuilder#resolveSymbolReference(SymbolContext, String, boolean, boolean, SymbolLookupContext,
 *      SymbolResolutionFallback)
 * @see AssemblyBuilder#resolveSymbolReference(SymbolContext[], String, boolean, boolean, SymbolLookupContext,
 *      SymbolResolutionFallback)
 * @see SymbolReference
 */
public interface SymbolResolutionFallback {

    /**
     * Resolves the specified symbol reference.
     *
     * @param symbolReference
     *            the symbol reference
     * @return the symbol being referenced, or <code>null</code> if no such symbol exists
     */
    @CheckForNull
    Symbol resolve(@Nonnull SymbolReference symbolReference);

}
