package org.reasm.testhelpers;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.junit.Assert;
import org.reasm.Function;
import org.reasm.expressions.EvaluationContext;
import org.reasm.expressions.Expression;

/**
 * A dummy function object.
 *
 * @author Francis Gagné
 */
@Immutable
public final class DummyFunction implements Function {

    @Nonnull
    @Override
    public Expression call(@Nonnull Expression[] arguments, @Nonnull EvaluationContext evaluationContext) {
        Assert.fail("DummyFunction.call() should not be called");
        return null; // unreachable
    }

}
