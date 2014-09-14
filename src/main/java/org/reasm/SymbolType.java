package org.reasm;

import javax.annotation.concurrent.Immutable;

/**
 * The type of a symbol. The type depends on how the symbol is defined and determines its behavior.
 *
 * @author Francis Gagné
 */
@Immutable
public enum SymbolType {

    /**
     * A constant. The symbol's value cannot change during a pass, but may change between passes.
     */
    CONSTANT,

    /**
     * A variable. The symbol's value may be changed during a pass.
     */
    VARIABLE;

    /**
     * Determines whether this symbol type allows forward references, i.e. references to the symbol before it has been defined.
     *
     * @return <code>true</code> if this symbol type allows forward references; <code>false</code> otherwise.
     */
    public final boolean allowsForwardReferences() {
        return this == CONSTANT;
    }

    /**
     * Determines whether this symbol type allows redefinition, i.e. setting a value to the symbol after is has already been given a
     * value in the current pass. Note that all symbols can be redefined in a different pass.
     *
     * @return <code>true</code> if this symbol type allows redefinition; <code>false</code> otherwise.
     */
    public final boolean allowsRedefinition() {
        return this == VARIABLE;
    }

}
