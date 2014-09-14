package org.reasm.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an attempt to close a transformation block while no transformation
 * block is open occurs.
 *
 * @author Francis Gagné
 */
public class NoOpenTransformationBlockErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new NoOpenTransformationBlockErrorMessage.
     */
    public NoOpenTransformationBlockErrorMessage() {
        super("Attempt to close a transformation block while no transformation block is open");
    }

}
