package org.reasm.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a string that has a opening quote but no closing quote is encountered.
 *
 * @author Francis Gagné
 */
public class UnterminatedStringErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new UnterminatedStringErrorMessage.
     */
    public UnterminatedStringErrorMessage() {
        super("Unterminated string");
    }

}
