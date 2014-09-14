package org.reasm.expressions;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.reasm.UnsignedIntValue;
import org.reasm.Value;
import org.reasm.ValueVisitor;

/**
 * Test class for {@link BinaryOperatorExpression}.
 *
 * @author Francis Gagné
 */
public class BinaryOperatorExpressionTest {

    private static final Expression OPERAND1 = new ValueExpression(new UnsignedIntValue(2));
    private static final Expression OPERAND2 = new ValueExpression(new UnsignedIntValue(3));
    static final BinaryOperatorExpression BINARY_OPERATOR_EXPRESSION = new BinaryOperatorExpression(BinaryOperator.ADDITION,
            OPERAND1, OPERAND2);

    /**
     * Asserts that {@link BinaryOperatorExpression#BinaryOperatorExpression(BinaryOperator, Expression, Expression)} correctly
     * initializes a {@link BinaryOperatorExpression}.
     */
    @Test
    public void binaryOperatorExpression() {
        assertThat(BINARY_OPERATOR_EXPRESSION.getOperator(), is(BinaryOperator.ADDITION));
        assertThat(BINARY_OPERATOR_EXPRESSION.getOperand1(), is(OPERAND1));
        assertThat(BINARY_OPERATOR_EXPRESSION.getOperand2(), is(OPERAND2));
    }

    /**
     * Asserts that {@link BinaryOperatorExpression#BinaryOperatorExpression(BinaryOperator, Expression, Expression)} throws a
     * {@link NullPointerException} when the <code>operand1</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void binaryOperatorExpressionNullOperand1() {
        new BinaryOperatorExpression(BinaryOperator.ADDITION, null, OPERAND2);
    }

    /**
     * Asserts that {@link BinaryOperatorExpression#BinaryOperatorExpression(BinaryOperator, Expression, Expression)} throws a
     * {@link NullPointerException} when the <code>operand2</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void binaryOperatorExpressionNullOperand2() {
        new BinaryOperatorExpression(BinaryOperator.ADDITION, OPERAND1, null);
    }

    /**
     * Asserts that {@link BinaryOperatorExpression#BinaryOperatorExpression(BinaryOperator, Expression, Expression)} throws a
     * {@link NullPointerException} when the <code>operator</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void binaryOperatorExpressionNullOperator() {
        new BinaryOperatorExpression(null, OPERAND1, OPERAND2);
    }

    /**
     * Asserts that {@link BinaryOperatorExpression#evaluate(EvaluationContext)} evaluates to the result of applying the
     * expression's operator to the expression's operands.
     */
    @Test
    public void evaluate() {
        assertThat(BINARY_OPERATOR_EXPRESSION.evaluate(EvaluationContext.DUMMY), is((Value) new UnsignedIntValue(5)));
    }

    /**
     * Asserts that {@link BinaryOperatorExpression#toString()} returns a string representation of the expression.
     */
    @Test
    public void testToString() {
        assertThat(
                BINARY_OPERATOR_EXPRESSION.toString(),
                is("BinaryOperatorExpression [operator=ADDITION, operand1=ValueExpression [value=UnsignedIntValue [value=2]], operand2=ValueExpression [value=UnsignedIntValue [value=3]]]"));
    }

    /**
     * Asserts that {@link BinaryOperatorExpression#toIdentifier(EvaluationContext, ValueVisitor)} returns the expression's value as
     * an identifier.
     */
    @Test
    public void toIdentifier() {
        assertThat(BINARY_OPERATOR_EXPRESSION.toIdentifier(EvaluationContext.DUMMY, new ValueToStringVisitor(
                EvaluationContext.DUMMY, "???")), is("5"));
    }

}
