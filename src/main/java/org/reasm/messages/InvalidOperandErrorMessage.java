package org.reasm.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * A message that is generated during an assembly when an invalid operand is encountered.
 *
 * @author Francis Gagné
 */
public class InvalidOperandErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new InvalidOperandErrorMessage.
     */
    public InvalidOperandErrorMessage() {
        super("Invalid operand");
    }

}
