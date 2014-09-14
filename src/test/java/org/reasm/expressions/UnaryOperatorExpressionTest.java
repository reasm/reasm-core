package org.reasm.expressions;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.reasm.SignedIntValue;
import org.reasm.UnsignedIntValue;
import org.reasm.Value;
import org.reasm.ValueVisitor;

/**
 * Test class for {@link UnaryOperatorExpression}.
 *
 * @author Francis Gagné
 */
public class UnaryOperatorExpressionTest {

    private static final Expression OPERAND = new ValueExpression(new UnsignedIntValue(2));
    static final UnaryOperatorExpression UNARY_OPERATOR_EXPRESSION = new UnaryOperatorExpression(UnaryOperator.NEGATION, OPERAND);

    /**
     * Asserts that {@link UnaryOperatorExpression#evaluate(EvaluationContext)} evaluates to the result of applying the expression's
     * operator to the expression's operand.
     */
    @Test
    public void evaluate() {
        assertThat(UNARY_OPERATOR_EXPRESSION.evaluate(new EvaluationContext(null, 0, null, null)),
                is((Value) new SignedIntValue(-2)));
    }

    /**
     * Asserts that {@link UnaryOperatorExpression#toString()} returns a string representation of the expression.
     */
    @Test
    public void testToString() {
        assertThat(UNARY_OPERATOR_EXPRESSION.toString(),
                is("UnaryOperatorExpression [operator=NEGATION, operand=ValueExpression [value=UnsignedIntValue [value=2]]]"));
    }

    /**
     * Asserts that {@link UnaryOperatorExpression#toIdentifier(EvaluationContext, ValueVisitor)} returns the expression's value as
     * an identifier.
     */
    @Test
    public void toIdentifier() {
        assertThat(UNARY_OPERATOR_EXPRESSION.toIdentifier(EvaluationContext.DUMMY, new ValueToStringVisitor(
                EvaluationContext.DUMMY, "???")), is("-2"));
    }

    /**
     * Asserts that {@link UnaryOperatorExpression#UnaryOperatorExpression(UnaryOperator, Expression)} correctly initializes an
     * {@link UnaryOperatorExpression}.
     */
    @Test
    public void unaryOperatorExpression() {
        final UnaryOperatorExpression expression = UNARY_OPERATOR_EXPRESSION;
        assertThat(expression.getOperator(), is(UnaryOperator.NEGATION));
        assertThat(expression.getOperand(), is(OPERAND));
    }

    /**
     * Asserts that {@link UnaryOperatorExpression#UnaryOperatorExpression(UnaryOperator, Expression)} throws a
     * {@link NullPointerException} when the <code>operand</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void unaryOperatorExpressionNullOperand() {
        new UnaryOperatorExpression(UnaryOperator.NEGATION, null);
    }

    /**
     * Asserts that {@link UnaryOperatorExpression#UnaryOperatorExpression(UnaryOperator, Expression)} throws a
     * {@link NullPointerException} when the <code>operator</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void unaryOperatorExpressionNullOperator() {
        new UnaryOperatorExpression(null, OPERAND);
    }

}
