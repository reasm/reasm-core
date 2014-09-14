package org.reasm.expressions;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.reasm.FloatValue;
import org.reasm.Function;
import org.reasm.FunctionValue;
import org.reasm.SignedIntValue;
import org.reasm.StringValue;
import org.reasm.UnsignedIntValue;
import org.reasm.Value;
import org.reasm.testhelpers.DummyFunction;

/**
 * Test class for {@link Expression}.
 *
 * @author Francis Gagné
 */
public class ExpressionTest {

    private static final Function DUMMY_FUNCTION = new DummyFunction();

    /**
     * Asserts that {@link Expression#quoteString(String)} returns <code>""</code> for the empty string.
     */
    @Test
    public void quoteStringEmpty() {
        assertThat(Expression.quoteString(""), is("\"\""));
    }

    /**
     * Asserts that {@link Expression#quoteString(String)} correctly escapes special characters.
     */
    @Test
    public void quoteStringFancy() {
        assertThat(Expression.quoteString("\0\1\n\r\"a"), is("\"\\0\1\\n\\r\\\"a\""));
    }

    /**
     * Asserts that {@link Expression#valueToString(Value)} returns the string representation of the underlying float for a
     * {@link FloatValue}.
     */
    @Test
    public void valueToStringFloat() {
        assertThat(Expression.valueToString(new FloatValue(3.25)), is("3.25"));
    }

    /**
     * Asserts that {@link Expression#valueToString(Value)} returns <code>&lt;function></code> for a {@link FunctionValue}.
     */
    @Test
    public void valueToStringFunction() {
        assertThat(Expression.valueToString(new FunctionValue(DUMMY_FUNCTION)), is("<function>"));
    }

    /**
     * Asserts that {@link Expression#valueToString(Value)} returns the string representation of the underlying integer for an
     * {@link UnsignedIntValue} when that integer is greater than {@link Long#MAX_VALUE}.
     */
    @Test
    public void valueToStringLargeUnsignedInt() {
        assertThat(Expression.valueToString(new UnsignedIntValue(-42)), is("18446744073709551574"));
    }

    /**
     * Asserts that {@link Expression#valueToString(Value)} returns the string representation of the underlying integer for a
     * {@link SignedIntValue}.
     */
    @Test
    public void valueToStringSignedInt() {
        assertThat(Expression.valueToString(new SignedIntValue(42)), is("42"));
    }

    /**
     * Asserts that {@link Expression#valueToString(Value)} returns the string representation of the underlying integer for a
     * {@link SignedIntValue} when that integer is negative.
     */
    @Test
    public void valueToStringSignedIntNegative() {
        assertThat(Expression.valueToString(new SignedIntValue(-42)), is("-42"));
    }

    /**
     * Asserts that {@link Expression#valueToString(Value)} returns the underlying string as a quoted string for a
     * {@link StringValue}.
     */
    @Test
    public void valueToStringString() {
        assertThat(Expression.valueToString(new StringValue("foo")), is("\"foo\""));
    }

    /**
     * Asserts that {@link Expression#valueToString(Value)} returns <code>&lt;undetermined></code> for <code>null</code>.
     */
    @Test
    public void valueToStringUndetermined() {
        assertThat(Expression.valueToString(null), is("<undetermined>"));
    }

    /**
     * Asserts that {@link Expression#valueToString(Value)} returns the string representation of the underlying integer for an
     * {@link UnsignedIntValue}.
     */
    @Test
    public void valueToStringUnsignedInt() {
        assertThat(Expression.valueToString(new UnsignedIntValue(42)), is("42"));
    }

}
