package org.reasm.messages;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.reasm.AssemblyErrorMessage;
import org.reasm.UserSymbol;

/**
 * An error message that is generated during an assembly when a symbol is defined with an undetermined value (represented as
 * <code>null</code>) and no other pass is necessary.
 *
 * @author Francis Gagné
 */
public class SymbolDefinedWithUndeterminedValueErrorMessage extends AssemblyErrorMessage {

    @Nonnull
    private final UserSymbol symbol;

    /**
     * Initializes a new SymbolDefinedWithUndeterminedValueErrorMessage.
     *
     * @param symbol
     *            the symbol with an undetermined value
     */
    public SymbolDefinedWithUndeterminedValueErrorMessage(@Nonnull UserSymbol symbol) {
        super("Symbol " + Objects.requireNonNull(symbol, "symbol").getName() + " is defined with an undetermined value");
        this.symbol = symbol;
    }

    /**
     * Gets the symbol that caused this error message.
     *
     * @return the symbol
     */
    @Nonnull
    public final UserSymbol getSymbol() {
        return this.symbol;
    }

}
