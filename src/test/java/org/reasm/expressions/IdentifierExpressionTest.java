package org.reasm.expressions;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
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
 * Test class for {@link IdentifierExpression}.
 *
 * @author Francis Gagné
 */
public class IdentifierExpressionTest {

    static final IdentifierExpression IDENTIFIER_EXPRESSION = new IdentifierExpression("foo", DummySymbolLookup.DEFAULT);

    /**
     * Asserts that {@link IdentifierExpression#evaluate(EvaluationContext)} evaluates to the value of the symbol identified by the
     * identifier.
     */
    @Test
    public void evaluate() {
        final Value value = new UnsignedIntValue(1);
        final SymbolLookup symbolLookup = new SingleSymbolLookup("foo", new StaticSymbol(value));
        assertThat(new IdentifierExpression("foo", symbolLookup).evaluate(EvaluationContext.DUMMY), is(value));
    }

    /**
     * Asserts that {@link IdentifierExpression#evaluate(EvaluationContext)} returns <code>null</code> when the {@link SymbolLookup}
     * is <code>null</code>.
     */
    @Test
    public void evaluateNullSymbolLookup() {
        assertThat(new IdentifierExpression("foo", null).evaluate(EvaluationContext.DUMMY), is(nullValue()));
    }

    /**
     * Asserts that {@link IdentifierExpression#IdentifierExpression(String, SymbolLookup)} correctly initializes an
     * {@link IdentifierExpression}.
     */
    @Test
    public void identifierExpression() {
        assertThat(IDENTIFIER_EXPRESSION.getIdentifier(), is("foo"));
        assertThat(IDENTIFIER_EXPRESSION.getSymbolLookup(), is((SymbolLookup) DummySymbolLookup.DEFAULT));
    }

    /**
     * Asserts that {@link IdentifierExpression#IdentifierExpression(String, SymbolLookup)} throws a {@link NullPointerException}
     * when the <code>identifier</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void identifierExpressionNullIdentifier() {
        new IdentifierExpression(null, DummySymbolLookup.DEFAULT);
    }

    /**
     * Asserts that {@link IdentifierExpression#toString()} returns a string representation of the expression.
     */
    @Test
    public void testToString() {
        assertThat(IDENTIFIER_EXPRESSION.toString(), is("IdentifierExpression [identifier=foo, symbolLookup="
                + DummySymbolLookup.DEFAULT.toString() + "]"));
    }

    /**
     * Asserts that {@link IdentifierExpression#toIdentifier(EvaluationContext, ValueVisitor)} returns the
     * {@link IdentifierExpression} itself.
     */
    @Test
    public void toIdentifier() {
        assertThat(IDENTIFIER_EXPRESSION.toIdentifier(EvaluationContext.DUMMY, new ValueToStringVisitor(EvaluationContext.DUMMY,
                "???")), is(sameInstance(IDENTIFIER_EXPRESSION)));
    }

}
