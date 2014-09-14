package org.reasm.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a directive with the wrong number of operands is encountered.
 *
 * @author Francis Gagné
 */
public class WrongNumberOfOperandsErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new WrongNumberOfOperandsErrorMessage.
     */
    public WrongNumberOfOperandsErrorMessage() {
        super("Wrong number of operands");
    }

}
