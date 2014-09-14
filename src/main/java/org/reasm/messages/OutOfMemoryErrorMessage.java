package org.reasm.messages;

import org.reasm.AssemblyFatalErrorMessage;

/**
 * A fatal error message that is generated during an assembly when the assembler runs out of memory.
 *
 * @author Francis Gagné
 */
public final class OutOfMemoryErrorMessage extends AssemblyFatalErrorMessage {

    /**
     * Initializes a new OutOfMemoryErrorMessage.
     */
    public OutOfMemoryErrorMessage() {
        super("Out of memory");
    }

}
