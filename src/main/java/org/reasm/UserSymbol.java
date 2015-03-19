package org.reasm;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.messages.SymbolDefinedHereInformationMessage;
import org.reasm.messages.SymbolRedefinedErrorMessage;
import org.reasm.messages.SymbolRedefinedWithDifferentTypeErrorMessage;

/**
 * A symbol defined by the user in the source code, or {@linkplain PredefinedSymbol predefined}.
 *
 * @author Francis Gagné
 */
public final class UserSymbol extends Symbol {

    /**
     * Creates a new user symbol with the same name, type and value as a predefined symbol.
     *
     * @param predefinedSymbol
     *            the predefined symbol
     * @return the new symbol
     */
    @Nonnull
    static final UserSymbol fromPredefinedSymbol(@Nonnull PredefinedSymbol predefinedSymbol) {
        // Values are supposed to be immutable, so we can just copy the reference.
        final UserSymbol symbol = new UserSymbol(predefinedSymbol.getContext(), predefinedSymbol.getName(),
                predefinedSymbol.getType());
        symbol.value = predefinedSymbol.getValue();
        symbol.predefined = true;
        return symbol;
    }

    @Nonnull
    private final SymbolContext<?> context;
    @CheckForNull
    private AssemblyStep definition;
    @CheckForNull
    private Object value;
    private boolean predefined;
    private boolean wasDefinedOnLastPass;

    /**
     * Initializes a new user symbol.
     *
     * @param context
     *            the context in which the symbol is defined
     * @param name
     *            the name of the symbol
     * @param type
     *            the type of the symbol
     */
    UserSymbol(@Nonnull SymbolContext<?> context, @Nonnull String name, @Nonnull SymbolType type) {
        super(name, type);
        this.context = context;
    }

    /**
     * Gets the context in which the symbol is defined.
     *
     * @return the symbol's context
     */
    @Nonnull
    public final SymbolContext<?> getContext() {
        return this.context;
    }

    /**
     * Gets the assembly step in which this symbol was defined.
     *
     * @return the assembly step in which the symbol was defined
     */
    @CheckForNull
    public final AssemblyStep getDefinition() {
        return this.definition;
    }

    /**
     * Gets the value of this symbol.
     *
     * To set the value of a symbol during an assembly, use
     * {@link AssemblyBuilder#defineSymbol(SymbolContext, String, boolean, SymbolType, Object)}.
     *
     * @return the symbol's value
     */
    @CheckForNull
    @Override
    public final Object getValue() {
        return this.value;
    }

    /**
     * Returns a value indicating whether the symbol is a {@linkplain PredefinedSymbol predefined symbol}.
     *
     * @return <code>true</code> if the symbol is a predefined symbol, or <code>false</code> if the symbol was defined in the source
     */
    public final boolean isPredefined() {
        return this.predefined;
    }

    /**
     * Defines this symbol.
     *
     * @param assembly
     *            the assembly in which the symbol exists
     * @param step
     *            the assembly step in which the symbol is defined
     * @param type
     *            the type of symbol
     * @param value
     *            the value of the symbol
     * @return <code>true</code> if the symbol was defined successfully, or <code>false</code> if an error occurred
     */
    final boolean define(@Nonnull Assembly assembly, @Nonnull AssemblyStep step, @Nonnull SymbolType type,
            @CheckForNull Object value) {
        // Validate that the value has the right type.
        this.context.getValueType().cast(value);

        if (this.getType() != type) {
            // The symbol is being redefined with a different type: raise an error.
            assembly.addMessage(new SymbolRedefinedWithDifferentTypeErrorMessage(this), step);
            assembly.addMessage(new SymbolDefinedHereInformationMessage(this), this.definition);
            return false;
        }

        if (this.definition != null && !type.allowsRedefinition()) {
            // The symbol is being redefined, but this symbol type doesn't allow redefinition: raise an error.
            assembly.addMessage(new SymbolRedefinedErrorMessage(this), step);
            assembly.addMessage(new SymbolDefinedHereInformationMessage(this), this.definition);
            return false;
        }

        if (this.definition == null) {
            this.definition = step;
        }

        this.value = value;
        return true;
    }

    /**
     * Gets a value indicating whether this symbol exists or pretends not to exist.
     *
     * @param definitionRequired
     *            <code>true</code> if the symbol must have a definition to exist, or <code>false</code> if symbols that don't
     *            {@linkplain SymbolType#allowsRedefinition() allow redefinition} exist without a definition
     * @return <code>true</code> if the symbol exists, or <code>false</code> if the symbol pretends not to exist
     */
    final boolean exists(boolean definitionRequired) {
        // Predefined symbols always exist.
        // This test is required because the definition field is null for them.
        if (this.predefined) {
            return true;
        }

        if (this.definition != null) {
            return true;
        }

        if (!this.getType().allowsRedefinition()) {
            if (!definitionRequired) {
                if (this.wasDefinedOnLastPass) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Prepares this symbol for the start of a new pass.
     */
    final void prepareForNewPass() {
        this.wasDefinedOnLastPass = this.definition != null;
        this.definition = null;
    }

}
