package org.reasm;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * A context for symbol lookup. Different contexts can be used to lookup symbols that are used in different contexts in the source
 * code, such as register aliases or macro names. A context can choose the type of the value of symbols defined within the context
 * so that symbols store information in a useful manner. It is strongly suggested that this type be immutable. When looking up a
 * particular symbol, it is necessary to have a reference to the symbol context to find the symbol in that context. Therefore, in
 * general, an architecture that defines a symbol context will be able to define symbols that are available to itself (when it asks
 * for them) but are not visible to the other architectures because they are not aware of that context.
 * <p>
 * reasm-core provides a context for values that appear in expressions, named {@link #VALUE} and whose value type is {@link Value}.
 * Note in particular that there is no generic context for register names, because although many architectures are expected to have
 * registers, each architecture has its own set of registers, so a register alias defined for architecture X is not useful for
 * architecture Y.
 *
 * @param <TValue>
 *            the type of the value of symbols defined within the context
 *
 * @author Francis Gagné
 */
@Immutable
public final class SymbolContext<TValue> {

    /** The context for values that appear in expressions. */
    public static final SymbolContext<Value> VALUE = new SymbolContext<>(Value.class);

    @Nonnull
    private final Class<TValue> valueType;

    /**
     * Initializes a new SymbolContext.
     *
     * @param valueType
     *            the type of the value for symbols within this context
     */
    public SymbolContext(@Nonnull Class<TValue> valueType) {
        if (valueType == null) {
            throw new NullPointerException("valueType");
        }

        this.valueType = valueType;
    }

    /**
     * Gets the type of the value for symbols within this context
     *
     * @return the value type
     */
    @Nonnull
    public final Class<TValue> getValueType() {
        return this.valueType;
    }

}
