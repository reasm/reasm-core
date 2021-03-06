package org.reasm;

import java.util.List;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Stores a copy of some attributes from an {@link Assembly} to perform symbol lookups later using these attributes. These
 * attributes include the current namespace, the scope for local symbols, the last non-suffix symbol that will be used as a prefix
 * for suffix symbols and the counters for anonymous symbols.
 *
 * @author Francis Gagné
 * @see Assembly#getCurrentSymbolLookupContext()
 * @see Assembly#resolveSymbolReference(List, String, boolean, SymbolLookupContext, SymbolResolutionFallback)
 * @see Assembly#resolveSymbolReference(SymbolContext, String, boolean, SymbolLookupContext, SymbolResolutionFallback)
 * @see Assembly#resolveSymbolReference(SymbolContext[], String, boolean, SymbolLookupContext, SymbolResolutionFallback)
 * @see AssemblyBuilder#resolveSymbolReference(List, String, boolean, SymbolLookupContext, SymbolResolutionFallback)
 * @see AssemblyBuilder#resolveSymbolReference(SymbolContext, String, boolean, SymbolLookupContext, SymbolResolutionFallback)
 * @see AssemblyBuilder#resolveSymbolReference(SymbolContext[], String, boolean, SymbolLookupContext, SymbolResolutionFallback)
 */
public final class SymbolLookupContext {

    @CheckForNull
    private static UserSymbol lookupSymbol(@Nonnull SymbolReference symbolReference, @Nonnull SymbolTable symbolTable,
            @Nonnull String name, boolean definitionRequired) {
        final List<? extends SymbolContext<?>> contexts = symbolReference.getContexts();

        for (int i = 0; i < contexts.size(); i++) {
            final UserSymbol symbol = symbolTable.getSymbol(contexts.get(i), name);
            if (symbol != null && (symbol.exists(definitionRequired) || symbolReference.getDefinedName() != null)) {
                return symbol;
            }
        }

        return null;
    }

    @Nonnull
    private final Assembly assembly;
    @CheckForNull
    private final Namespace namespace;
    @CheckForNull
    private final AssemblyStepLocation scopeKey;
    @CheckForNull
    private final UserSymbol lastNonSuffixSymbol;
    private final int forwCounter;
    private final int backCounter;

    SymbolLookupContext(@Nonnull Assembly assembly, @CheckForNull Namespace namespace, @CheckForNull AssemblyStepLocation scopeKey,
            @CheckForNull UserSymbol lastNonSuffixSymbol, int forwCounter, int backCounter) {
        this.assembly = assembly;
        this.namespace = namespace;
        this.scopeKey = scopeKey;
        this.lastNonSuffixSymbol = lastNonSuffixSymbol;
        this.forwCounter = forwCounter;
        this.backCounter = backCounter;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        final SymbolLookupContext other = (SymbolLookupContext) obj;
        if (!this.assembly.equals(other.assembly)) {
            return false;
        }

        if (!Objects.equals(this.namespace, other.namespace)) {
            return false;
        }

        if (!Objects.equals(this.scopeKey, other.scopeKey)) {
            return false;
        }

        if (this.forwCounter != other.forwCounter) {
            return false;
        }

        if (this.backCounter != other.backCounter) {
            return false;
        }

        return true;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.assembly.hashCode();
        result = prime * result + Objects.hashCode(this.namespace);
        result = prime * result + Objects.hashCode(this.scopeKey);
        result = prime * result + this.forwCounter;
        result = prime * result + this.backCounter;
        return result;
    }

    final String expandSymbol(@Nonnull String name, boolean local) {
        // The order of expansion isn't important, because both expansions are mutually exclusive.
        return this.expandSuffixSymbol(this.expandAnonymousSymbol(name), local);
    }

    final Assembly getAssembly() {
        return this.assembly;
    }

    @Nonnull
    final String getDefinedName(@Nonnull String name, boolean local, boolean isSuffixSymbol) {
        if (local || isSuffixSymbol) {
            return name;
        }

        return Assembly.buildNamespacedSymbolName(this.namespace, name);
    }

    final Scope getScope() {
        return this.assembly.getScope(this.scopeKey);
    }

    @CheckForNull
    final Symbol resolveSymbolReference(@Nonnull SymbolReference symbolReference, boolean definitionRequired) {
        SymbolTable symbolTable = this.assembly.getSymbolTable();
        final Scope scope = symbolReference.getScope();
        if (scope != null) {
            symbolTable = scope.getLocalSymbolTable();
        }

        final String symbolDefinedName = symbolReference.getDefinedName();
        if (symbolDefinedName != null) {
            // If the symbol reference is a definition,
            // only look up existing symbols in the current namespace,
            // not in parent namespaces.
            final UserSymbol symbol = lookupSymbol(symbolReference, symbolTable, symbolDefinedName, definitionRequired);
            if (symbol != null) {
                return symbol;
            }

            return symbolReference.resolveFallbackSymbol();
        }

        final String symbolName = symbolReference.getName();
        if (scope == null) {
            for (Namespace namespace = this.namespace; namespace != null; namespace = namespace.getParent()) {
                final String namespacedSymbolName = Assembly.buildNamespacedSymbolName(namespace, symbolName);
                final UserSymbol symbol = lookupSymbol(symbolReference, symbolTable, namespacedSymbolName, definitionRequired);
                if (symbol != null) {
                    return symbol;
                }
            }
        }

        final UserSymbol symbol = lookupSymbol(symbolReference, symbolTable, symbolName, definitionRequired);
        if (symbol != null) {
            return symbol;
        }

        return symbolReference.resolveFallbackSymbol();
    }

    /**
     * Expands the name of an anonymous symbol.
     *
     * @param name
     *            the name of the symbol. If it matches an anonymous symbol name (i.e. all <code>+</code> or all <code>-</code>
     *            characters), then the name is expanded. Otherwise, the name is returned as is.
     * @return the expanded name
     */
    @Nonnull
    private final String expandAnonymousSymbol(@Nonnull String name) {
        if (Assembly.isAnonymousSymbolReference(name)) {
            // TODO: use a different symbol table for anonymous symbols?
            String anonymousName;
            if (name.charAt(0) == '+') {
                anonymousName = String.format("__forw%d", this.forwCounter + name.length());
            } else {
                anonymousName = String.format("__back%d", this.backCounter - (name.length() - 1));
            }

            return anonymousName;
        }

        return name;
    }

    /**
     * Expands the name of a suffix symbol.
     *
     * @param name
     *            the name of the symbol. If it matches a suffix symbol name (i.e. the name starts with '.') and a non-suffix symbol
     *            was defined previously, then the name is expanded. Otherwise, the name is returned as is.
     * @param local
     *            <code>true</code> if the symbol is a local symbol, or <code>false</code> otherwise. If the symbol is a local
     *            symbol, the name is returned as is.
     * @return the expanded name
     */
    private final String expandSuffixSymbol(@Nonnull String name, boolean local) {
        if (!local) {
            if (this.lastNonSuffixSymbol != null) {
                if (Assembly.isSuffixSymbolName(name)) {
                    return this.lastNonSuffixSymbol.getName() + name;
                }
            }
        }

        return name;
    }

}
