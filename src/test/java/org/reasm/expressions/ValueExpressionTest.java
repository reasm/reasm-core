package org.reasm.expressions;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.reasm.UnsignedIntValue;
import org.reasm.Value;
import org.reasm.ValueVisitor;

/**
 * Test class for {@link ValueExpression}.
 *
 * @author Francis Gagné
 */
public class ValueExpressionTest {

    private static final Value VALUE = new UnsignedIntValue(1);
    private static final ValueExpression VALUE_EXPRESSION = new ValueExpression(VALUE);

    /**
     * Asserts that {@link ValueExpression#evaluate(EvaluationContext)} evaluates to the expression's value.
     */
    @Test
    public void evaluate() {
        assertThat(VALUE_EXPRESSION.evaluate(EvaluationContext.DUMMY), is(sameInstance(VALUE)));
    }

    /**
     * Asserts that {@link ValueExpression#toString()} returns a string representation of the expression.
     */
    @Test
    public void testToString() {
        assertThat(VALUE_EXPRESSION.toString(), is("ValueExpression [value=" + VALUE + "]"));
    }

    /**
     * Asserts that {@link ValueExpression#toIdentifier(EvaluationContext, ValueVisitor)} returns the expression's value as an
     * identifier.
     */
    @Test
    public void toIdentifier() {
        assertThat(
                VALUE_EXPRESSION.toIdentifier(EvaluationContext.DUMMY, new ValueToStringVisitor(EvaluationContext.DUMMY, "???")),
                is(new IdentifierExpression("1", null)));
    }

    /**
     * Asserts that {@link ValueExpression#ValueExpression(Value)} correctly initializes a {@link ValueExpression}.
     */
    @Test
    public void valueExpression() {
        assertThat(VALUE_EXPRESSION.getValue(), is(sameInstance(VALUE)));
    }

}
