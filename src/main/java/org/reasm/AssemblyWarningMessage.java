package org.reasm;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A warning message that is generated during an assembly.
 *
 * @author Francis Gagné
 */
public abstract class AssemblyWarningMessage extends AssemblyMessage {

    /**
     * Initializes a new assembly warning message.
     *
     * @param text
     *            the text of the message, to be displayed to the end-user
     */
    protected AssemblyWarningMessage(@Nonnull String text) {
        super(MessageGravity.WARNING, text, null);
    }

    /**
     * Initializes a new assembly warning message with a parent message.
     *
     * @param text
     *            the text of the message, to be displayed to the end-user
     * @param parent
     *            the parent message, i.e. the message that caused this message to be generated
     */
    protected AssemblyWarningMessage(@Nonnull String text, @CheckForNull AssemblyMessage parent) {
        super(MessageGravity.WARNING, text, parent);
    }

}
