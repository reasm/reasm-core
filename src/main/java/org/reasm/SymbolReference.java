package org.reasm;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

/**
 * A reference to a symbol.
 * <p>
 * Create a symbol reference to look up a {@link Symbol} in an assembly. Use the overloads that take an {@link AssemblyBuilder} for
 * symbol references in the user's code, and the overloads that take an {@link Assembly} for tools that inspect an assembly.
 *
 * @see AssemblyBuilder#defineSymbol(SymbolContext, String, boolean, SymbolType, Object)
 *
 * @author Francis Gagné
 */
public final class SymbolReference {

    private static final ConcurrentMap<SymbolContext<?>, ImmutableList<? extends SymbolContext<?>>> CONTEXT_SINGLETON_CACHE = new ConcurrentHashMap<>();

    @Nonnull
    static ImmutableList<? extends SymbolContext<?>> cachedContextSingleton(@Nonnull SymbolContext<?> context) {
        ImmutableList<? extends SymbolContext<?>> result = CONTEXT_SINGLETON_CACHE.get(context);
        if (result == null) {
            result = ImmutableList.of(context);
            final ImmutableList<? extends SymbolContext<?>> previous = CONTEXT_SINGLETON_CACHE.putIfAbsent(context, result);
            if (previous != null) {
                result = previous;
            }
        }

        return result;
    }

    @Nonnull
    private final List<? extends SymbolContext<?>> contexts;
    @Nonnull
    private final String name;
    private final boolean bypassNamespaceResolution;
    private final boolean isDefinition;
    @Nonnull
    private final SymbolLookupContext lookupContext;
    @CheckForNull
    private final AssemblyStep step;
    @CheckForNull
    private final Scope scope;
    @CheckForNull
    private final SymbolResolutionFallback symbolResolutionFallback;
    @CheckForNull
    private final Symbol symbol;
    @CheckForNull
    private Object value;

    /**
     * Initializes a new SymbolReference.
     *
     * @param contexts
     *            a list of the contexts of the symbol reference
     * @param name
     *            the name of the symbol to look up
     * @param local
     *            <code>true</code> to look up a local symbol; otherwise, <code>false</code>
     * @param bypassNamespaceResolution
     *            <code>true</code> to bypass namespace resolution, or <code>false</code> to perform namespace resolution. If the
     *            assembly is currently in a namespace, the namespace's name will be prepended, followed by a '.', to the specified
     *            symbol name, and this will be repeated for each parent namespace. Specify <code>true</code> if the symbol name is
     *            already fully qualified.
     * @param isDefinition
     *            <code>true</code> if this symbol reference is a definition of the symbol; otherwise, <code>false</code>
     * @param lookupContext
     *            the context in which to perform the symbol lookups
     * @param step
     *            the assembly step in which the reference appears
     * @param symbolResolutionFallback
     *            a {@link SymbolResolutionFallback} object that provides a means of returning a symbol when no existing symbol is
     *            found
     */
    SymbolReference(@Nonnull ImmutableList<? extends SymbolContext<?>> contexts, @Nonnull String name, boolean local,
            boolean bypassNamespaceResolution, boolean isDefinition, @Nonnull SymbolLookupContext lookupContext,
            @CheckForNull AssemblyStep step, @CheckForNull SymbolResolutionFallback symbolResolutionFallback) {
        this.contexts = contexts;
        this.name = lookupContext.expandSymbol(name, local);
        this.bypassNamespaceResolution = bypassNamespaceResolution;
        this.isDefinition = isDefinition;
        this.lookupContext = lookupContext;
        this.step = step;
        if (local) {
            this.scope = lookupContext.getScope();
        } else {
            this.scope = null;
        }

        this.symbolResolutionFallback = symbolResolutionFallback;
        this.symbol = this.lookupContext.resolveSymbolReference(this, false);
        if (this.symbol != null) {
            this.value = this.symbol.getValue();
        }
    }

    /**
     * Gets a value indicating whether this symbol reference bypasses namespace resolution.
     *
     * @return <code>true</code> if this symbol reference bypasses namespace resolution; otherwise, <code>false</code>
     */
    public final boolean bypassNamespaceResolution() {
        return this.bypassNamespaceResolution;
    }

    /**
     * Gets the assembly that contains this symbol reference.
     *
     * @return the assembly
     */
    @Nonnull
    public final Assembly getAssembly() {
        return this.lookupContext.getAssembly();
    }

    /**
     * Gets the contexts of this symbol reference.
     *
     * @return the contexts
     */
    @Nonnull
    public final List<? extends SymbolContext<?>> getContexts() {
        return this.contexts;
    }

    /**
     * Gets the name of the symbol being referenced by this symbol reference.
     *
     * @return the name
     */
    @Nonnull
    public final String getName() {
        return this.name;
    }

    /**
     * Gets the step in which this symbol reference appears.
     *
     * @return the step
     */
    @CheckForNull
    public final AssemblyStep getStep() {
        return this.step;
    }

    /**
     * Gets the symbol that this symbol reference references.
     *
     * @return the symbol, or <code>null</code> if the reference couldn't be resolved
     */
    @CheckForNull
    public final Symbol getSymbol() {
        return this.symbol;
    }

    /**
     * Gets the value that the symbol that this symbol reference references had at the time this symbol reference was constructed.
     *
     * @return the symbol's value, or <code>null</code> if the reference couldn't be resolved
     */
    @CheckForNull
    public final Object getValue() {
        return this.value;
    }

    /**
     * Gets a value indicating whether this symbol reference is a definition of the symbol.
     *
     * @return <code>true</code> if this symbol reference is a definition of the symbol; otherwise, <code>false</code>
     */
    public final boolean isDefinition() {
        return this.isDefinition;
    }

    /**
     * Gets a value indicating whether a local symbol is being referenced by this symbol reference.
     *
     * @return <code>true</code> if this symbol reference references a local symbol; otherwise, <code>false</code>
     */
    public final boolean isLocal() {
        return this.scope != null;
    }

    @CheckForNull
    final Scope getScope() {
        return this.scope;
    }

    final boolean isStale() {
        // Resolve the symbol reference again.
        //
        // There are some situations where the reference
        // will resolve to a different symbol if we perform a new pass.
        //
        // - If a reference to a symbol appears before the symbol's definition,
        //   the reference will initially resolve to null.
        //   Resolving the reference now (and again in the next pass) will find the symbol.
        //
        // - We might get a different symbol if the reference is in a namespace
        //   and a constant is defined later in that namespace that matches the reference.
        //
        // - If the symbol reference specifies many contexts
        //   and a symbol is defined later in a context that has higher priority in this reference,
        //   the reference will resolve to that new symbol.
        final Symbol resolvedSymbol = this.lookupContext.resolveSymbolReference(this, true);

        // If the symbol reference resolves to a different symbol...
        if (this.symbol != resolvedSymbol) {
            // If the reference did resolve to a symbol initially,
            // but doesn't resolve to a symbol now, then the reference is stale.
            if (resolvedSymbol == null) {
                return true;
            }

            // If the newly resolved symbol is a constant, then the reference is stale.
            if (resolvedSymbol.getType().allowsForwardReferences()) {
                return true;
            }
        }

        // If the reference did not resolve to a symbol initially, then the reference is not stale.
        if (this.symbol == null) {
            return false;
        }

        // If the initially resolved symbol is a variable, then the reference is not stale.
        if (this.symbol.getType().allowsRedefinition()) {
            return false;
        }

        // If the symbol's value didn't change, then the reference is not stale.
        if (Objects.equals(this.value, this.symbol.getValue())) {
            return false;
        }

        // Otherwise, the reference is stale.
        return true;
    }

    @CheckForNull
    final Symbol resolveFallbackSymbol() {
        if (this.symbolResolutionFallback != null) {
            return this.symbolResolutionFallback.resolve(this);
        }

        return null;
    }

    final void setValue(Object value) {
        this.value = value;
    }

}
