package org.reasm;

import javax.annotation.concurrent.Immutable;

/**
 * The completion status of an assembly.
 *
 * @author Francis Gagné
 * @see Assembly#step()
 */
@Immutable
public enum AssemblyCompletionStatus {

    /**
     * The assembly is complete.
     */
    COMPLETE,

    /**
     * The assembly is pending completion.
     */
    PENDING,

    /**
     * The assembly is pending completion and just started a new pass.
     */
    STARTED_NEW_PASS

}
