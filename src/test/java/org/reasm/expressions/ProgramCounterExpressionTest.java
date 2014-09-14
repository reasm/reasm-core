package org.reasm.expressions;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.reasm.UnsignedIntValue;
import org.reasm.Value;
import org.reasm.ValueVisitor;

/**
 * Test class for {@link ProgramCounterExpression}.
 *
 * @author Francis Gagné
 */
public class ProgramCounterExpressionTest {

    private static final EvaluationContext EVALUATION_CONTEXT = new EvaluationContext(null, 1, null, null);

    /**
     * Asserts that {@link ProgramCounterExpression#evaluate(EvaluationContext)} evaluates to the evaluation context's program
     * counter.
     */
    @Test
    public void evaluate() {
        assertThat(ProgramCounterExpression.INSTANCE.evaluate(EVALUATION_CONTEXT), is((Value) new UnsignedIntValue(1)));
    }

    /**
     * Asserts that {@link ProgramCounterExpression#toString()} returns a string representation of the expression.
     */
    @Test
    public void testToString() {
        assertThat(ProgramCounterExpression.INSTANCE.toString(), is("ProgramCounterExpression []"));
    }

    /**
     * Asserts that {@link ProgramCounterExpression#toIdentifier(EvaluationContext, ValueVisitor)} returns the expression's value as
     * an identifier.
     */
    @Test
    public void toIdentifier() {
        assertThat(ProgramCounterExpression.INSTANCE.toIdentifier(EVALUATION_CONTEXT, new ValueToStringVisitor(EVALUATION_CONTEXT,
                "???")), is("1"));
    }

}
