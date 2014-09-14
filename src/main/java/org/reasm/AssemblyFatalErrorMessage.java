package org.reasm;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A fatal error message that is generated during an assembly.
 *
 * @author Francis Gagné
 */
public abstract class AssemblyFatalErrorMessage extends AssemblyMessage {

    /**
     * Initializes a new assembly fatal error message.
     *
     * @param text
     *            the text of the message, to be displayed to the end-user
     */
    protected AssemblyFatalErrorMessage(@Nonnull String text) {
        super(MessageGravity.FATAL_ERROR, text, null);
    }

    /**
     * Initializes a new assembly fatal error message with a parent message.
     *
     * @param text
     *            the text of the message, to be displayed to the end-user
     * @param parent
     *            the parent message, i.e. the message that caused this message to be generated
     */
    protected AssemblyFatalErrorMessage(@Nonnull String text, @CheckForNull AssemblyMessage parent) {
        super(MessageGravity.FATAL_ERROR, text, parent);
    }

}
