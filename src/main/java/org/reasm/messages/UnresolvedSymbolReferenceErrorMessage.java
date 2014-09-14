package org.reasm.messages;

import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.AssemblyErrorMessage;
import org.reasm.SymbolReference;

/**
 * An error message that is generated during an assembly when a reference to a symbol could not be resolved.
 *
 * @author Francis Gagné
 */
public class UnresolvedSymbolReferenceErrorMessage extends AssemblyErrorMessage {

    @CheckForNull
    private final SymbolReference symbolReference;

    /**
     * Initializes a new UnresolvedSymbolReferenceErrorMessage.
     *
     * @param name
     *            the name of the referenced symbol
     */
    public UnresolvedSymbolReferenceErrorMessage(@Nonnull String name) {
        super("Unresolved reference to symbol " + name);
        this.symbolReference = null;
    }

    /**
     * Initializes a new UnresolvedSymbolReferenceErrorMessage.
     *
     * @param symbolReference
     *            the unresolved symbol reference
     */
    public UnresolvedSymbolReferenceErrorMessage(@Nonnull SymbolReference symbolReference) {
        super("Unresolved reference to symbol " + Objects.requireNonNull(symbolReference, "symbolReference").getName());
        this.symbolReference = symbolReference;
    }

    /**
     * Gets the unresolved symbol reference that caused this error.
     *
     * @return the symbol reference
     */
    @CheckForNull
    public final SymbolReference getSymbolReference() {
        return this.symbolReference;
    }

}
