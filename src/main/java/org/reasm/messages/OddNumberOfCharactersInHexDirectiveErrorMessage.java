package org.reasm.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an operand in an <code>HEX</code> directive contains an odd number of
 * characters.
 *
 * @author Francis Gagné
 */
public class OddNumberOfCharactersInHexDirectiveErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new OddNumberOfCharactersInHexDirectiveErrorMessage.
     */
    public OddNumberOfCharactersInHexDirectiveErrorMessage() {
        super("Odd number of characters in HEX directive");
    }

}
