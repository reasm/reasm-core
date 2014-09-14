package org.reasm.expressions;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.reasm.StaticSymbol;
import org.reasm.UnsignedIntValue;
import org.reasm.Value;
import org.reasm.ValueVisitor;
import org.reasm.testhelpers.SingleSymbolLookup;

/**
 * Test class for {@link PeriodExpression}.
 *
 * @author Francis Gagné
 */
public class PeriodExpressionTest {

    private static final Expression LEFT_EXPRESSION = new IdentifierExpression("foo");
    private static final Expression RIGHT_EXPRESSION = new IdentifierExpression("bar");
    static final PeriodExpression PERIOD_EXPRESSION = new PeriodExpression(LEFT_EXPRESSION, RIGHT_EXPRESSION);

    /**
     * Asserts that {@link PeriodExpression#evaluate(EvaluationContext)} evaluates to the value of the symbol identified by the
     * period expression.
     */
    @Test
    public void evaluate() {
        final Value value = new UnsignedIntValue(2);
        final SymbolLookup symbolLookup = new SingleSymbolLookup("foo.bar", new StaticSymbol(value));
        assertThat(PERIOD_EXPRESSION.evaluate(new EvaluationContext(null, 0, symbolLookup, null)), is(value));
    }

    /**
     * Asserts that {@link PeriodExpression#PeriodExpression(Expression, Expression)} correctly initializes a
     * {@link PeriodExpression}.
     */
    @Test
    public void periodExpression() {
        assertThat(PERIOD_EXPRESSION.getLeftExpression(), is(sameInstance(LEFT_EXPRESSION)));
        assertThat(PERIOD_EXPRESSION.getRightExpression(), is(sameInstance(RIGHT_EXPRESSION)));
    }

    /**
     * Asserts that {@link PeriodExpression#PeriodExpression(Expression, Expression)} throws a {@link NullPointerException} when the
     * <code>leftExpression</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void periodExpressionNullLeftExpression() {
        new PeriodExpression(null, RIGHT_EXPRESSION);
    }

    /**
     * Asserts that {@link PeriodExpression#PeriodExpression(Expression, Expression)} throws a {@link NullPointerException} when the
     * <code>rightExpression</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void periodExpressionNullRightExpression() {
        new PeriodExpression(LEFT_EXPRESSION, null);
    }

    /**
     * Asserts that {@link PeriodExpression#simplify(EvaluationContext)} simplifies to an {@link IdentifierExpression} with the
     * correct identifier.
     */
    @Test
    public void simplify() {
        assertThat(PERIOD_EXPRESSION.simplify(new EvaluationContext(null, 0, null, null)),
                is((Expression) new IdentifierExpression("foo.bar")));
    }

    /**
     * Asserts that {@link PeriodExpression#simplify(EvaluationContext)} simplifies to {@link ValueExpression#UNDETERMINED} when the
     * left expression is undetermined.
     */
    @Test
    public void simplifyLeftUndetermined() {
        final PeriodExpression periodExpression = new PeriodExpression(ValueExpression.UNDETERMINED, RIGHT_EXPRESSION);
        assertThat(periodExpression.simplify(new EvaluationContext(null, 0, null, null)),
                is((Expression) ValueExpression.UNDETERMINED));
    }

    /**
     * Asserts that {@link PeriodExpression#simplify(EvaluationContext)} simplifies to {@link ValueExpression#UNDETERMINED} when the
     * right expression is undetermined.
     */
    @Test
    public void simplifyRightUndetermined() {
        final PeriodExpression periodExpression = new PeriodExpression(LEFT_EXPRESSION, new ValueExpression(null));
        assertThat(periodExpression.simplify(new EvaluationContext(null, 0, null, null)),
                is((Expression) ValueExpression.UNDETERMINED));
    }

    /**
     * Asserts that {@link PeriodExpression#toString()} returns a string representation of the expression.
     */
    @Test
    public void testToString() {
        assertThat(PERIOD_EXPRESSION.toString(), is("PeriodExpression [leftExpression=IdentifierExpression [identifier=foo], "
                + "rightExpression=IdentifierExpression [identifier=bar]]"));
    }

    /**
     * Asserts that {@link PeriodExpression#toIdentifier(EvaluationContext, ValueVisitor)} returns an identifier composed of the
     * left expression, a period and the right expression.
     */
    @Test
    public void toIdentifier() {
        assertThat(
                PERIOD_EXPRESSION.toIdentifier(EvaluationContext.DUMMY, new ValueToStringVisitor(EvaluationContext.DUMMY, "???")),
                is("foo.bar"));
    }

}
