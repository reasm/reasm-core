package org.reasm.messages;

import java.util.Objects;

import org.reasm.AssemblyInformationMessage;
import org.reasm.UserSymbol;

/**
 * An information message that is generated during an assembly when an error related to the redefinition of a symbol occurs to
 * indicate the symbol's original definition.
 *
 * @author Francis Gagné
 */
public class SymbolDefinedHereInformationMessage extends AssemblyInformationMessage {

    private final UserSymbol symbol;

    /**
     * Initializes a new SymbolDefinedHereInformationMessage.
     *
     * @param symbol
     *            the symbol being redefined
     */
    public SymbolDefinedHereInformationMessage(UserSymbol symbol) {
        super("Symbol " + Objects.requireNonNull(symbol, "symbol").getName() + " is defined here");
        this.symbol = symbol;
    }

    /**
     * Gets the symbol that was redefined.
     *
     * @return the symbol
     */
    public final UserSymbol getSymbol() {
        return this.symbol;
    }

}
