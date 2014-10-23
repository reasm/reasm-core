package org.reasm.expressions;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.reasm.UnsignedIntValue;
import org.reasm.Value;
import org.reasm.ValueVisitor;
import org.reasm.testhelpers.DummySymbolLookup;

/**
 * Test class for {@link GroupingExpression}.
 *
 * @author Francis Gagné
 */
public class GroupingExpressionTest {

    private static final Expression CHILD_EXPRESSION = new ValueExpression(new UnsignedIntValue(2));
    static final GroupingExpression GROUPING_EXPRESSION = new GroupingExpression(CHILD_EXPRESSION);

    /**
     * Asserts that {@link GroupingExpression#evaluate(EvaluationContext)} evaluates to the value of the child expression.
     */
    @Test
    public void evaluate() {
        assertThat(GROUPING_EXPRESSION.evaluate(EvaluationContext.DUMMY), is((Value) new UnsignedIntValue(2)));
    }

    /**
     * Asserts that {@link GroupingExpression#GroupingExpression(Expression)} correctly initializes a {@link GroupingExpression}.
     */
    @Test
    public void groupingExpression() {
        assertThat(GROUPING_EXPRESSION.getChildExpression(), is(sameInstance(CHILD_EXPRESSION)));
    }

    /**
     * Asserts that {@link GroupingExpression#GroupingExpression(Expression)} throws a {@link NullPointerException} when the
     * <code>childExpression</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void groupingExpressionNullChildExpression() {
        new GroupingExpression(null);
    }

    /**
     * Asserts that {@link GroupingExpression#simplify(EvaluationContext)} simplifies to the child expression.
     */
    @Test
    public void simplify() {
        assertThat(GROUPING_EXPRESSION.simplify(EvaluationContext.DUMMY), is(sameInstance(CHILD_EXPRESSION)));
    }

    /**
     * Asserts that {@link GroupingExpression#toString()} returns a string representation of the expression.
     */
    @Test
    public void testToString() {
        assertThat(GROUPING_EXPRESSION.toString(),
                is("GroupingExpression [childExpression=ValueExpression [value=UnsignedIntValue [value=2]]]"));
    }

    /**
     * Asserts that {@link GroupingExpression#toIdentifier(EvaluationContext, ValueVisitor)} returns the child expression's
     * identifier when the child expression is an {@link IdentifierExpression}.
     */
    @Test
    public void toIdentifierChildIsIdentifier() {
        assertThat(new GroupingExpression(new IdentifierExpression("foo", DummySymbolLookup.DEFAULT)).toIdentifier(
                EvaluationContext.DUMMY, new ValueToStringVisitor(EvaluationContext.DUMMY, "???")), is(new IdentifierExpression(
                "foo", DummySymbolLookup.DEFAULT)));
    }

    /**
     * Asserts that {@link GroupingExpression#toIdentifier(EvaluationContext, ValueVisitor)} returns the child expression as an
     * identifier when the child expression is a {@link ValueOrientedExpression}.
     */
    @Test
    public void toIdentifierChildIsValue() {
        assertThat(
                GROUPING_EXPRESSION.toIdentifier(EvaluationContext.DUMMY, new ValueToStringVisitor(EvaluationContext.DUMMY, "???")),
                is(new IdentifierExpression("2", null)));
    }

}
