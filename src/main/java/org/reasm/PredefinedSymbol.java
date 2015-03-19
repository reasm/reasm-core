package org.reasm;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * A predefined symbol in a {@link Configuration}. When an {@linkplain Assembly#Assembly(Configuration) assembly is created} from a
 * {@link Configuration} that contains predefined symbols, a {@link UserSymbol} is defined for each predefined symbol, using the
 * values from the predefined symbol.
 *
 * @author Francis Gagné
 */
@Immutable
public final class PredefinedSymbol {

    @Nonnull
    private final SymbolContext<?> context;
    @Nonnull
    private final String name;
    @Nonnull
    private final SymbolType type;
    @CheckForNull
    private final Object value;

    /**
     * Initializes a new predefined symbol with the specified name and value.
     *
     * @param context
     *            the context in which this predefined symbol is defined; must not be <code>null</code>
     * @param name
     *            the name of the predefined symbol; must not be <code>null</code>
     * @param type
     *            the type of the predefined symbol; must not be <code>null</code>
     * @param value
     *            the value of the predefined symbol
     * @param <TValue>
     *            the type of the symbol's value
     */
    public <TValue> PredefinedSymbol(@Nonnull SymbolContext<TValue> context, @Nonnull String name, @Nonnull SymbolType type,
            @CheckForNull TValue value) {
        if (context == null) {
            throw new NullPointerException("context");
        }

        if (name == null) {
            throw new NullPointerException("name");
        }

        if (type == null) {
            throw new NullPointerException("type");
        }

        this.context = context;
        this.name = name;
        this.type = type;
        this.value = context.getValueType().cast(value);
    }

    /**
     * Gets the context in which this predefined symbol is defined.
     *
     * @return the context
     */
    @Nonnull
    public final SymbolContext<?> getContext() {
        return this.context;
    }

    /**
     * Gets the name of this predefined symbol.
     *
     * @return the name
     */
    @Nonnull
    public final String getName() {
        return this.name;
    }

    /**
     * Gets the type of this predefined symbol.
     *
     * @return the type
     */
    @Nonnull
    public final SymbolType getType() {
        return this.type;
    }

    /**
     * Gets the value of this predefined symbol.
     *
     * @return the value
     */
    @CheckForNull
    public final Object getValue() {
        return this.value;
    }

}
