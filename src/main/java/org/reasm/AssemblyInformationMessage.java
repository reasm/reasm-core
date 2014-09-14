package org.reasm;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * An information message that is generated during an assembly.
 *
 * @author Francis Gagné
 */
public abstract class AssemblyInformationMessage extends AssemblyMessage {

    /**
     * Initializes a new assembly information message.
     *
     * @param text
     *            the text of the message, to be displayed to the end-user
     */
    protected AssemblyInformationMessage(@Nonnull String text) {
        super(MessageGravity.INFORMATION, text, null);
    }

    /**
     * Initializes a new assembly information message with a parent message.
     *
     * @param text
     *            the text of the message, to be displayed to the end-user
     * @param parent
     *            the parent message, i.e. the message that caused this message to be generated
     */
    protected AssemblyInformationMessage(@Nonnull String text, @CheckForNull AssemblyMessage parent) {
        super(MessageGravity.INFORMATION, text, parent);
    }

}
