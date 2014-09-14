package org.reasm.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a division by zero occurs while evaluating an expression.
 *
 * @author Francis Gagné
 */
public class DivisionByZeroErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new DivisionByZeroErrorMessage.
     */
    public DivisionByZeroErrorMessage() {
        super("Division by zero");
    }

}
