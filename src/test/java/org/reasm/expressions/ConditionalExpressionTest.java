package org.reasm.expressions;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.reasm.UnsignedIntValue;
import org.reasm.Value;
import org.reasm.ValueVisitor;

/**
 * Test class for {@link ConditionalExpression}.
 *
 * @author Francis Gagné
 */
public class ConditionalExpressionTest {

    private static final Expression FALSE_CONDITION = new ValueExpression(new UnsignedIntValue(0));
    private static final Expression TRUE_CONDITION = new ValueExpression(new UnsignedIntValue(1));
    private static final Expression TRUE_PART = new ValueExpression(new UnsignedIntValue(2));
    private static final Expression FALSE_PART = new ValueExpression(new UnsignedIntValue(3));

    static final ConditionalExpression FALSE_CONDITIONAL_EXPRESSION = new ConditionalExpression(FALSE_CONDITION, TRUE_PART,
            FALSE_PART);
    private static final ConditionalExpression TRUE_CONDITIONAL_EXPRESSION = new ConditionalExpression(TRUE_CONDITION, TRUE_PART,
            FALSE_PART);
    private static final ConditionalExpression UNDEFINED_CONDITIONAL_EXPRESSION = new ConditionalExpression(
            ValueExpression.UNDETERMINED, TRUE_PART, FALSE_PART);

    /**
     * Asserts that {@link ConditionalExpression#ConditionalExpression(Expression, Expression, Expression)} correctly initializes a
     * {@link ConditionalExpression}.
     */
    @Test
    public void conditionalExpression() {
        assertThat(FALSE_CONDITIONAL_EXPRESSION.getCondition(), is(sameInstance(FALSE_CONDITION)));
        assertThat(FALSE_CONDITIONAL_EXPRESSION.getTruePart(), is(sameInstance(TRUE_PART)));
        assertThat(FALSE_CONDITIONAL_EXPRESSION.getFalsePart(), is(sameInstance(FALSE_PART)));
    }

    /**
     * Asserts that {@link ConditionalExpression#ConditionalExpression(Expression, Expression, Expression)} throws a
     * {@link NullPointerException} when the <code>condition</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void conditionalExpressionNullCondition() {
        new ConditionalExpression(null, TRUE_PART, FALSE_PART);
    }

    /**
     * Asserts that {@link ConditionalExpression#ConditionalExpression(Expression, Expression, Expression)} throws a
     * {@link NullPointerException} when the <code>falsePart</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void conditionalExpressionNullFalsePart() {
        new ConditionalExpression(FALSE_CONDITION, TRUE_PART, null);
    }

    /**
     * Asserts that {@link ConditionalExpression#ConditionalExpression(Expression, Expression, Expression)} throws a
     * {@link NullPointerException} when the <code>truePart</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void conditionalExpressionNullTruePart() {
        new ConditionalExpression(FALSE_CONDITION, null, FALSE_PART);
    }

    /**
     * Asserts that {@link ConditionalExpression#evaluate(EvaluationContext)} returns the value of the false part of the
     * {@link ConditionalExpression} when the condition evaluates to a false value.
     */
    @Test
    public void evaluateFalseCondition() {
        assertThat(FALSE_CONDITIONAL_EXPRESSION.evaluate(EvaluationContext.DUMMY), is((Value) new UnsignedIntValue(3)));
    }

    /**
     * Asserts that {@link ConditionalExpression#evaluate(EvaluationContext)} returns the value of the true part of the
     * {@link ConditionalExpression} when the condition evaluates to a true value.
     */
    @Test
    public void evaluateTrueCondition() {
        assertThat(TRUE_CONDITIONAL_EXPRESSION.evaluate(EvaluationContext.DUMMY), is((Value) new UnsignedIntValue(2)));
    }

    /**
     * Asserts that {@link ConditionalExpression#evaluate(EvaluationContext)} returns <code>null</code> when the condition evaluates
     * to an undetermined value.
     */
    @Test
    public void evaluateUndeterminedCondition() {
        assertThat(UNDEFINED_CONDITIONAL_EXPRESSION.evaluate(EvaluationContext.DUMMY), is(nullValue()));
    }

    /**
     * Asserts that {@link ConditionalExpression#simplify(EvaluationContext)} simplifies to the false part of a
     * {@link ConditionalExpression} when the condition evaluates to a false value.
     */
    @Test
    public void simplifyFalseCondition() {
        assertThat(FALSE_CONDITIONAL_EXPRESSION.simplify(EvaluationContext.DUMMY), is(sameInstance(FALSE_PART)));
    }

    /**
     * Asserts that {@link ConditionalExpression#simplify(EvaluationContext)} simplifies to the true part of a
     * {@link ConditionalExpression} when the condition evaluates to a true value.
     */
    @Test
    public void simplifyTrueCondition() {
        assertThat(TRUE_CONDITIONAL_EXPRESSION.simplify(EvaluationContext.DUMMY), is(sameInstance(TRUE_PART)));
    }

    /**
     * Asserts that {@link ConditionalExpression#simplify(EvaluationContext)} simplifies to {@link ValueExpression#UNDETERMINED}
     * when the condition evaluates to an undetermined value.
     */
    @Test
    public void simplifyUndeterminedCondition() {
        assertThat(UNDEFINED_CONDITIONAL_EXPRESSION.simplify(EvaluationContext.DUMMY),
                is(sameInstance((Expression) ValueExpression.UNDETERMINED)));
    }

    /**
     * Asserts that {@link ConditionalExpression#toString()} returns a string representation of the expression.
     */
    @Test
    public void testToString() {
        assertThat(FALSE_CONDITIONAL_EXPRESSION.toString(),
                is("ConditionalExpression [condition=ValueExpression [value=UnsignedIntValue [value=0]], "
                        + "truePart=ValueExpression [value=UnsignedIntValue [value=2]], "
                        + "falsePart=ValueExpression [value=UnsignedIntValue [value=3]]]"));
    }

    /**
     * Asserts that {@link ConditionalExpression#toIdentifier(EvaluationContext, ValueVisitor)} returns the false part of a
     * {@link ConditionalExpression} as an identifier when the condition evaluates to a false value.
     */
    @Test
    public void toIdentifierFalseCondition() {
        assertThat(FALSE_CONDITIONAL_EXPRESSION.toIdentifier(EvaluationContext.DUMMY, new ValueToStringVisitor(
                EvaluationContext.DUMMY, "???")), is(new IdentifierExpression("3", null)));
    }

    /**
     * Asserts that {@link ConditionalExpression#toIdentifier(EvaluationContext, ValueVisitor)} returns the true part of a
     * {@link ConditionalExpression} as an identifier when the condition evaluates to a true value.
     */
    @Test
    public void toIdentifierTrueCondition() {
        assertThat(TRUE_CONDITIONAL_EXPRESSION.toIdentifier(EvaluationContext.DUMMY, new ValueToStringVisitor(
                EvaluationContext.DUMMY, "???")), is(new IdentifierExpression("2", null)));
    }

}
