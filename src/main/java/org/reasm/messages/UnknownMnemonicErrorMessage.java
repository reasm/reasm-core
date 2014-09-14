package org.reasm.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an unknown mnemonic is encountered.
 *
 * @author Francis Gagné
 */
public class UnknownMnemonicErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new UnknownMnemonicErrorMessage.
     */
    public UnknownMnemonicErrorMessage() {
        super("Unknown mnemonic");
    }

}
