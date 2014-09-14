package org.reasm.messages;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.reasm.AssemblyErrorMessage;
import org.reasm.UserSymbol;

/**
 * An error message that is generated during an assembly when a symbol is defined more than once as different types.
 *
 * @author Francis Gagné
 */
public class SymbolRedefinedWithDifferentTypeErrorMessage extends AssemblyErrorMessage {

    @Nonnull
    private final UserSymbol symbol;

    /**
     * Initializes a new SymbolRedefinedWithDifferentTypeErrorMessage.
     *
     * @param symbol
     *            the symbol that was redefined
     */
    public SymbolRedefinedWithDifferentTypeErrorMessage(@Nonnull UserSymbol symbol) {
        super("Symbol with name " + Objects.requireNonNull(symbol, "symbol").getName()
                + " already defined as another type of symbol");
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
