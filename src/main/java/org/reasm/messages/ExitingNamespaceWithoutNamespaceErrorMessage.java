package org.reasm.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an <code>ENDNS</code> directive without a corresponding
 * <code>NAMESPACE</code> directive is encountered.
 *
 * @author Francis Gagné
 */
public class ExitingNamespaceWithoutNamespaceErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new ExitingNamespaceWithoutNamespaceErrorMessage.
     */
    public ExitingNamespaceWithoutNamespaceErrorMessage() {
        super("Exiting a namespace when there is no current namespace");
    }

}
