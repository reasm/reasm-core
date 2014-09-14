package org.reasm.expressions;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.reasm.StaticSymbol;
import org.reasm.UnsignedIntValue;
import org.reasm.Value;
import org.reasm.ValueVisitor;
import org.reasm.testhelpers.SingleSymbolLookup;

/**
 * Test class for {@link IdentifierExpression}.
 *
 * @author Francis Gagné
 */
public class IdentifierExpressionTest {

    static final IdentifierExpression IDENTIFIER_EXPRESSION = new IdentifierExpression("foo");

    /**
     * Asserts that {@link IdentifierExpression#evaluate(EvaluationContext)} evaluates to the value of the symbol identified by the
     * identifier.
     */
    @Test
    public void evaluate() {
        final Value value = new UnsignedIntValue(1);
        final SymbolLookup symbolLookup = new SingleSymbolLookup("foo", new StaticSymbol(value));
        assertThat(IDENTIFIER_EXPRESSION.evaluate(new EvaluationContext(null, 0, symbolLookup, null)), is(value));
    }

    /**
     * Asserts that {@link IdentifierExpression#evaluate(EvaluationContext)} evaluates to <code>null</code> when the identifier
     * identifies no known symbol.
     */
    @Test
    public void evaluateNullSymbolLookup() {
        assertThat(IDENTIFIER_EXPRESSION.evaluate(EvaluationContext.DUMMY), is(nullValue()));
    }

    /**
     * Asserts that {@link IdentifierExpression#IdentifierExpression(String)} correctly initializes an {@link IdentifierExpression}.
     */
    @Test
    public void identifierExpression() {
        assertThat(IDENTIFIER_EXPRESSION.getIdentifier(), is("foo"));
    }

    /**
     * Asserts that {@link IdentifierExpression#IdentifierExpression(String)} throws a {@link NullPointerException} when the
     * <code>identifier</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void identifierExpressionNullIdentifier() {
        new IdentifierExpression(null);
    }

    /**
     * Asserts that {@link IdentifierExpression#toString()} returns a string representation of the expression.
     */
    @Test
    public void testToString() {
        assertThat(IDENTIFIER_EXPRESSION.toString(), is("IdentifierExpression [identifier=foo]"));
    }

    /**
     * Asserts that {@link IdentifierExpression#toIdentifier(EvaluationContext, ValueVisitor)} returns the expression's identifier.
     */
    @Test
    public void toIdentifier() {
        assertThat(IDENTIFIER_EXPRESSION.toIdentifier(EvaluationContext.DUMMY, new ValueToStringVisitor(EvaluationContext.DUMMY,
                "???")), is("foo"));
    }

}
