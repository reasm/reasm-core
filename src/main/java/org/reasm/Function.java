package org.reasm;

import javax.annotation.Nonnull;

import org.reasm.expressions.EvaluationContext;
import org.reasm.expressions.Expression;

/**
 * A function that produces an {@link Expression} when called.
 *
 * @author Francis Gagné
 */
public interface Function {

    /**
     * Calls the function with an array of arguments.
     *
     * @param arguments
     *            the arguments to pass to the function
     * @param evaluationContext
     *            the context in which the function is called
     * @return the result of the function
     */
    @Nonnull
    Expression call(@Nonnull Expression[] arguments, @Nonnull EvaluationContext evaluationContext);

}
