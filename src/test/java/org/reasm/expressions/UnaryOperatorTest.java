package org.reasm.expressions;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.reasm.SignedIntValue;
import org.reasm.UnsignedIntValue;
import org.reasm.Value;

/**
 * Test class for the enum values of {@link UnaryOperator}.
 *
 * @author Francis Gagné
 */
public class UnaryOperatorTest {

    private static final UnsignedIntValue UINT_42 = new UnsignedIntValue(42);

    private static final EvaluationContext EMPTY_EVALUATION_CONTEXT = new EvaluationContext(null, 0, null, null);

    /**
     * Asserts that {@link UnaryOperator#apply(Value, EvaluationContext)} on {@link UnaryOperator#BITWISE_NOT} performs a bitwise
     * NOT operation.
     */
    @Test
    public void bitwiseNot() {
        final Value result = UnaryOperator.BITWISE_NOT.apply(UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) new UnsignedIntValue(~42)));
    }

    /**
     * Asserts that {@link UnaryOperator#apply(Value, EvaluationContext)} on {@link UnaryOperator#LOGICAL_NOT} performs a logical
     * NOT operation.
     */
    @Test
    public void logicalNot() {
        final Value result = UnaryOperator.LOGICAL_NOT.apply(UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) new UnsignedIntValue(0)));
    }

    /**
     * Asserts that {@link UnaryOperator#apply(Value, EvaluationContext)} on {@link UnaryOperator#NEGATION} performs a negation.
     */
    @Test
    public void negation() {
        final Value result = UnaryOperator.NEGATION.apply(UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) new SignedIntValue(-42)));
    }

    /**
     * Asserts that {@link UnaryOperator#apply(Value, EvaluationContext)} on {@link UnaryOperator#UNARY_PLUS} performs an
     * "unary plus" operation.
     */
    @Test
    public void unaryPlus() {
        final Value result = UnaryOperator.UNARY_PLUS.apply(UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) new SignedIntValue(42)));
    }

}
