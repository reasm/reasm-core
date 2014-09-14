package org.reasm.messages;

import java.util.Objects;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a symbol is defined with an invalid name.
 *
 * @author Francis Gagné
 */
public class IllegalSymbolNameErrorMessage extends AssemblyErrorMessage {

    private final String symbolName;

    /**
     * Initializes a new IllegalSymbolNameErrorMessage.
     *
     * @param symbolName
     *            the invalid symbol name
     */
    public IllegalSymbolNameErrorMessage(String symbolName) {
        super("Illegal symbol name: " + Objects.requireNonNull(symbolName, "symbolName"));
        this.symbolName = symbolName;
    }

    /**
     * Gets the invalid symbol name that caused this error.
     *
     * @return the symbol name
     */
    public final String getSymbolName() {
        return this.symbolName;
    }

}
