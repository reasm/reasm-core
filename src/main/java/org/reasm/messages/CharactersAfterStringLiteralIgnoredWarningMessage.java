package org.reasm.messages;

import org.reasm.AssemblyWarningMessage;

/**
 * A warning message that is generated during an assembly when characters are found (and ignored) after a string literal.
 *
 * @author Francis Gagné
 */
public class CharactersAfterStringLiteralIgnoredWarningMessage extends AssemblyWarningMessage {

    /**
     * Initializes a new CharactersAfterStringLiteralIgnoredWarningMessage.
     */
    public CharactersAfterStringLiteralIgnoredWarningMessage() {
        super("Characters after string literal ignored");
    }

}
