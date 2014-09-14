package org.reasm.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a function is called with the wrong number of arguments.
 *
 * @author Francis Gagné
 */
public class WrongNumberOfArgumentsErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new WrongNumberOfArgumentsErrorMessage.
     */
    public WrongNumberOfArgumentsErrorMessage() {
        super("Wrong number of arguments to function");
    }

}
