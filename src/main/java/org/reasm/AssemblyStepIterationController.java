package org.reasm;

/**
 * Contains a method that indicates whether to perform a new iteration over the same sequence of source locations or not.
 *
 * @author Francis Gagné
 */
public interface AssemblyStepIterationController {

    /**
     * Determines whether to perform a new iteration over the same sequence of source locations or not.
     *
     * @return <code>true</code> to perform a new iteration, or <code>false</code> to stop iterating
     */
    boolean hasNextIteration();

}
