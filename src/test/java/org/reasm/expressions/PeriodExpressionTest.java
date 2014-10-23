package org.reasm.expressions;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.reasm.StaticSymbol;
import org.reasm.UnsignedIntValue;
import org.reasm.Value;
import org.reasm.ValueVisitor;
import org.reasm.testhelpers.DummySymbolLookup;
import org.reasm.testhelpers.SingleSymbolLookup;

/**
 * Test class for {@link PeriodExpression}.
 *
 * @author Francis Gagné
 */
public class PeriodExpressionTest {

    private static final Expression LEFT_EXPRESSION = new IdentifierExpression("foo", DummySymbolLookup.DEFAULT);
    private static final Expression RIGHT_EXPRESSION = new IdentifierExpression("bar", DummySymbolLookup.DEFAULT);
    private static final PeriodExpression PERIOD_EXPRESSION = new PeriodExpression(LEFT_EXPRESSION, RIGHT_EXPRESSION,
            DummySymbolLookup.DEFAULT);

    /**
     * Asserts that {@link PeriodExpression#evaluate(EvaluationContext)} evaluates to the value of the symbol identified by the
     * period expression.
     */
    @Test
    public void evaluate() {
        final Value value = new UnsignedIntValue(2);
        final SymbolLookup symbolLookup = new SingleSymbolLookup("foo.bar", new StaticSymbol(value));
        final Expression leftExpression = new IdentifierExpression("foo", symbolLookup);
        final Expression rightExpression = new IdentifierExpression("bar", symbolLookup);
        final PeriodExpression periodExpression = new PeriodExpression(leftExpression, rightExpression, DummySymbolLookup.DEFAULT);
        assertThat(periodExpression.evaluate(EvaluationContext.DUMMY), is(value));
    }

    /**
     * Asserts that {@link PeriodExpression#PeriodExpression(Expression, Expression, SymbolLookup)} correctly initializes a
     * {@link PeriodExpression}.
     */
    @Test
    public void periodExpression() {
        assertThat(PERIOD_EXPRESSION.getLeftExpression(), is(sameInstance(LEFT_EXPRESSION)));
        assertThat(PERIOD_EXPRESSION.getRightExpression(), is(sameInstance(RIGHT_EXPRESSION)));
        assertThat(PERIOD_EXPRESSION.getFallbackSymbolLookup(), is(sameInstance((SymbolLookup) DummySymbolLookup.DEFAULT)));
    }

    /**
     * Asserts that {@link PeriodExpression#PeriodExpression(Expression, Expression, SymbolLookup)} throws a
     * {@link NullPointerException} when the <code>leftExpression</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void periodExpressionNullLeftExpression() {
        new PeriodExpression(null, RIGHT_EXPRESSION, DummySymbolLookup.DEFAULT);
    }

    /**
     * Asserts that {@link PeriodExpression#PeriodExpression(Expression, Expression, SymbolLookup)} throws a
     * {@link NullPointerException} when the <code>rightExpression</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void periodExpressionNullRightExpression() {
        new PeriodExpression(LEFT_EXPRESSION, null, DummySymbolLookup.DEFAULT);
    }

    /**
     * Asserts that {@link PeriodExpression#simplify(EvaluationContext)} simplifies to an {@link IdentifierExpression} with the
     * correct identifier.
     */
    @Test
    public void simplify() {
        final Expression expected = new IdentifierExpression("foo.bar", DummySymbolLookup.DEFAULT);
        assertThat(PERIOD_EXPRESSION.simplify(EvaluationContext.DUMMY), is(expected));
    }

    /**
     * Asserts that {@link PeriodExpression#simplify(EvaluationContext)} simplifies to {@link ValueExpression#UNDETERMINED} when the
     * left expression is undetermined.
     */
    @Test
    public void simplifyLeftUndetermined() {
        final PeriodExpression periodExpression = new PeriodExpression(ValueExpression.UNDETERMINED, RIGHT_EXPRESSION,
                DummySymbolLookup.DEFAULT);
        final Expression expected = ValueExpression.UNDETERMINED;
        assertThat(periodExpression.simplify(EvaluationContext.DUMMY), is(expected));
    }

    /**
     * Asserts that {@link PeriodExpression#simplify(EvaluationContext)} simplifies to an {@link IdentifierExpression} with the
     * correct identifier when the left expression has no {@link SymbolLookup} by using the {@link SymbolLookup} from the
     * {@link PeriodExpression}.
     */
    @Test
    public void simplifyLeftWithNullSymbolLookup() {
        final SymbolLookup symbolLookup = new DummySymbolLookup();
        final Expression expected = new IdentifierExpression("foo.bar", symbolLookup);
        final Expression leftExpression = new IdentifierExpression("foo", null);
        final Expression rightExpression = new IdentifierExpression("bar", DummySymbolLookup.DEFAULT);
        final PeriodExpression periodExpression = new PeriodExpression(leftExpression, rightExpression, symbolLookup);
        assertThat(periodExpression.simplify(EvaluationContext.DUMMY), is(expected));
    }

    /**
     * Asserts that {@link PeriodExpression#simplify(EvaluationContext)} simplifies to {@link ValueExpression#UNDETERMINED} when the
     * right expression is undetermined.
     */
    @Test
    public void simplifyRightUndetermined() {
        final PeriodExpression periodExpression = new PeriodExpression(LEFT_EXPRESSION, new ValueExpression(null),
                DummySymbolLookup.DEFAULT);
        final Expression expected = ValueExpression.UNDETERMINED;
        assertThat(periodExpression.simplify(EvaluationContext.DUMMY), is(expected));
    }

    /**
     * Asserts that {@link PeriodExpression#toString()} returns a string representation of the expression.
     */
    @Test
    public void testToString() {
        assertThat(
                PERIOD_EXPRESSION.toString(),
                is("PeriodExpression [leftExpression=" + LEFT_EXPRESSION.toString() + ", " + "rightExpression="
                        + RIGHT_EXPRESSION.toString() + ", fallbackSymbolLookup=" + DummySymbolLookup.DEFAULT.toString() + "]"));
    }

    /**
     * Asserts that {@link PeriodExpression#toIdentifier(EvaluationContext, ValueVisitor)} returns an identifier composed of the
     * left expression, a period and the right expression.
     */
    @Test
    public void toIdentifier() {
        assertThat(
                PERIOD_EXPRESSION.toIdentifier(EvaluationContext.DUMMY, new ValueToStringVisitor(EvaluationContext.DUMMY, "???")),
                is(new IdentifierExpression("foo.bar", DummySymbolLookup.DEFAULT)));
    }

}
