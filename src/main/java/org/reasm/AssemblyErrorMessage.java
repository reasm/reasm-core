package org.reasm;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * An error message that is generated during an assembly.
 *
 * @author Francis Gagné
 */
public abstract class AssemblyErrorMessage extends AssemblyMessage {

    /**
     * Initializes a new assembly error message.
     *
     * @param text
     *            the text of the message, to be displayed to the end-user
     */
    protected AssemblyErrorMessage(@Nonnull String text) {
        super(MessageGravity.ERROR, text, null);
    }

    /**
     * Initializes a new assembly error message with a parent message.
     *
     * @param text
     *            the text of the message, to be displayed to the end-user
     * @param parent
     *            the parent message, i.e. the message that caused this message to be generated
     */
    protected AssemblyErrorMessage(@Nonnull String text, @CheckForNull AssemblyMessage parent) {
        super(MessageGravity.ERROR, text, parent);
    }

}
