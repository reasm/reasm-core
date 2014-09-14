package org.reasm.messages;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.reasm.AssemblyErrorMessage;
import org.reasm.UserSymbol;

/**
 * An error message that is generated during an assembly when a symbol that doesn't allow redefinition is redefined within a single
 * pass.
 *
 * @author Francis Gagné
 */
public class SymbolRedefinedErrorMessage extends AssemblyErrorMessage {

    @Nonnull
    private final UserSymbol symbol;

    /**
     * Initializes a new SymbolRedefinedErrorMessage.
     *
     * @param symbol
     *            the symbol that was redefined
     */
    public SymbolRedefinedErrorMessage(@Nonnull UserSymbol symbol) {
        super("Symbol " + Objects.requireNonNull(symbol, "symbol").getName() + " redefined");
        this.symbol = symbol;
    }

    /**
     * Gets the symbol that was redefined and that caused the error.
     *
     * @return the symbol
     */
    @Nonnull
    public final UserSymbol getSymbol() {
        return this.symbol;
    }

}
