package org.reasm.messages;

import org.reasm.Assembly;
import org.reasm.AssemblyFatalErrorMessage;

/**
 * A fatal error message that is generated during an assembly when the assembly process is interrupted. This message is generated
 * when a {@link ThreadDeath} error is caught in {@link Assembly#step()}.
 *
 * @author Francis Gagné
 */
public class AssemblyInterruptedErrorMessage extends AssemblyFatalErrorMessage {

    /**
     * Initializes a new AssemblyInterruptedErrorMessage.
     */
    public AssemblyInterruptedErrorMessage() {
        super("Assembly interrupted");
    }

}
