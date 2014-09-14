package org.reasm;

import javax.annotation.concurrent.Immutable;

/**
 * The gravity of an {@link AssemblyMessage}.
 *
 * @author Francis Gagné
 */
@Immutable
public enum MessageGravity {

    /**
     * No message gravity.
     */
    NONE,

    /**
     * An informational message. This kind of message usually provides additional information about a previous message.
     */
    INFORMATION,

    /**
     * A warning message. This kind of message informs the user that some code may produce an unexpected result, although it is
     * still valid.
     */
    WARNING,

    /**
     * An error message. This kind of message forces the assembly to abort and fail at the end of the current pass. Error messages
     * are raised when the assembler cannot understand some code.
     */
    ERROR,

    /**
     * A fatal error message. This kind of message forces the assembly to abort and fail immediately; the rest of the source will
     * not be assembled. Fatal error messages are not necessarily errors in the code, but are situations (such as running out of
     * memory) that make the assembler unable to continue normally.
     */
    FATAL_ERROR

}
