package org.reasm.expressions;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.reasm.expressions.ExpressionEvaluationTest.FLOAT_3_25;
import static org.reasm.expressions.ExpressionEvaluationTest.SINT_42;
import static org.reasm.expressions.ExpressionEvaluationTest.STRING_A;
import static org.reasm.expressions.ExpressionEvaluationTest.UINT_42;
import static org.reasm.expressions.ExpressionEvaluationTest.UNDETERMINED_FUNCTION_A_VALUE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reasm.AssemblyMessage;
import org.reasm.Function;
import org.reasm.FunctionValue;
import org.reasm.UnsignedIntValue;
import org.reasm.Value;
import org.reasm.ValueVisitor;
import org.reasm.messages.SubjectOfFunctionCallIsNotFunctionErrorMessage;
import org.reasm.testhelpers.AssemblyMessageCollector;
import org.reasm.testhelpers.EquivalentAssemblyMessage;

/**
 * Test class for {@link FunctionCallExpression}.
 *
 * @author Francis Gagné
 */
public class FunctionCallExpressionTest {

    /**
     * Test class for {@link FunctionCallExpression#simplify(EvaluationContext)}.
     *
     * @author Francis Gagné
     */
    @RunWith(Parameterized.class)
    public static class SimplifyTest {

        private static final List<Object[]> TEST_DATA = Arrays.asList(new Object[][] {

                // An undetermined value
                { null, ValueExpression.UNDETERMINED, null },

                // An unsigned integer
                { UINT_42, ValueExpression.UNDETERMINED, new SubjectOfFunctionCallIsNotFunctionErrorMessage(UINT_42) },

                // A signed integer
                { SINT_42, ValueExpression.UNDETERMINED, new SubjectOfFunctionCallIsNotFunctionErrorMessage(SINT_42) },

                // A floating-point number
                { FLOAT_3_25, ValueExpression.UNDETERMINED, new SubjectOfFunctionCallIsNotFunctionErrorMessage(FLOAT_3_25) },

                // A string
                { STRING_A, ValueExpression.UNDETERMINED, new SubjectOfFunctionCallIsNotFunctionErrorMessage(STRING_A) },

                // A function that returns an undetermined value
                { UNDETERMINED_FUNCTION_A_VALUE, ValueExpression.UNDETERMINED, null },

                // A function that returns 42
                { FOURTY_TWO_FUNCTION_VALUE, new ValueExpression(UINT_42), null },

        });

        /**
         * Returns the test data for this parameterized test class.
         *
         * @return the test data
         */
        @Nonnull
        @Parameters
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        @Nonnull
        private final Expression function;
        @Nonnull
        private final Expression expectedResult;
        @CheckForNull
        private final AssemblyMessage expectedAssemblyMessage;

        /**
         * Initializes a new SimplifyTest.
         *
         * @param function
         *            the subject of the function call
         * @param expectedResult
         *            the expected result of the function call
         * @param expectedAssemblyMessage
         *            the assembly message that is expected to be raised when simplifying the function call expression, or
         *            <code>null</code> if no assembly message is expected to be raised
         */
        public SimplifyTest(@CheckForNull Value function, @Nonnull Expression expectedResult,
                @CheckForNull AssemblyMessage expectedAssemblyMessage) {
            this.function = new ValueExpression(function);
            this.expectedResult = expectedResult;
            this.expectedAssemblyMessage = expectedAssemblyMessage;
        }

        /**
         * Asserts that {@link FunctionCallExpression#simplify(EvaluationContext)} returns the result of the function call if the
         * function expression evaluates to a {@link FunctionValue}, returns <code>null</code> if the function expression evaluates
         * to an undetermined value or emits a {@link SubjectOfFunctionCallIsNotFunctionErrorMessage} if it evaluates to another
         * type of value.
         */
        @Test
        public void simplify() {
            final ArrayList<AssemblyMessage> messages = new ArrayList<>();
            final Expression result = new FunctionCallExpression(this.function).simplify(new EvaluationContext(null, 0,
                    new AssemblyMessageCollector(messages)));
            assertThat(result, is(this.expectedResult));

            // Check for generated assembly messages.
            if (this.expectedAssemblyMessage == null) {
                assertThat(messages, is(empty()));
            } else {
                assertThat(messages, contains(new EquivalentAssemblyMessage(this.expectedAssemblyMessage)));
            }
        }

    }

    static final Function FOURTY_TWO_FUNCTION = new Function() {
        @Nonnull
        @Override
        public Expression call(@Nonnull Expression[] arguments, @Nonnull EvaluationContext evaluationContext) {
            return new ValueExpression(UINT_42);
        }
    };

    static final Function IDENTITY_FUNCTION = new Function() {
        @Nonnull
        @Override
        public Expression call(@Nonnull Expression[] arguments, @Nonnull EvaluationContext evaluationContext) {
            return arguments[0];
        }
    };

    private static final Function SELECT_FUNCTION = new Function() {

        @Nonnull
        @Override
        public Expression call(@Nonnull final Expression[] arguments, @Nonnull EvaluationContext evaluationContext) {
            return Value.accept(arguments[0].evaluate(evaluationContext), new ValueVisitor<Expression>() {

                @Override
                public Expression visitFloat(double value) {
                    fail();
                    return null;
                }

                @Override
                public Expression visitFunction(@Nonnull Function value) {
                    fail();
                    return null;
                }

                @Override
                public Expression visitSignedInt(long value) {
                    fail();
                    return null;
                }

                @Override
                public Expression visitString(@Nonnull String value) {
                    fail();
                    return null;
                }

                @Override
                public Expression visitUndetermined() {
                    fail();
                    return null;
                }

                @Override
                public Expression visitUnsignedInt(long value) {
                    return arguments[(int) (value + 1)];
                }

            });
        }

        @Override
        public String toString() {
            return "SelectFunction";
        }

    };

    static final FunctionValue FOURTY_TWO_FUNCTION_VALUE = new FunctionValue(FOURTY_TWO_FUNCTION);
    static final FunctionValue IDENTITY_FUNCTION_VALUE = new FunctionValue(IDENTITY_FUNCTION);

    private static final Expression FUNCTION_EXPRESSION = new ValueExpression(new FunctionValue(SELECT_FUNCTION));
    private static final Expression ARGUMENT_0_EXPRESSION = new ValueExpression(new UnsignedIntValue(1));
    private static final Expression ARGUMENT_1_EXPRESSION = new ValueExpression(new UnsignedIntValue(10));
    private static final Expression ARGUMENT_2_EXPRESSION = new ValueExpression(new UnsignedIntValue(100));
    private static final Expression ARGUMENT_3_EXPRESSION = new ValueExpression(new UnsignedIntValue(1000));
    static final FunctionCallExpression FUNCTION_CALL_EXPRESSION = new FunctionCallExpression(FUNCTION_EXPRESSION,
            ARGUMENT_0_EXPRESSION, ARGUMENT_1_EXPRESSION, ARGUMENT_2_EXPRESSION, ARGUMENT_3_EXPRESSION);

    /**
     * Asserts that {@link FunctionCallExpression#evaluate(EvaluationContext)} evaluates to the result of the function call.
     */
    @Test
    public void evaluate() {
        assertThat(FUNCTION_CALL_EXPRESSION.evaluate(EvaluationContext.DUMMY), is((Value) new UnsignedIntValue(100)));
    }

    /**
     * Asserts that {@link FunctionCallExpression#FunctionCallExpression(Expression, Expression[])} correctly initializes a
     * {@link FunctionCallExpression}.
     */
    @Test
    public void functionCallExpressionExpressionExpressionArray() {
        assertThat(FUNCTION_CALL_EXPRESSION.getFunction(), is(sameInstance(FUNCTION_EXPRESSION)));
        assertThat(FUNCTION_CALL_EXPRESSION.getArguments(),
                is(arrayContaining(ARGUMENT_0_EXPRESSION, ARGUMENT_1_EXPRESSION, ARGUMENT_2_EXPRESSION, ARGUMENT_3_EXPRESSION)));
    }

    /**
     * Asserts that {@link FunctionCallExpression#FunctionCallExpression(Expression, Expression[])} throws a
     * {@link NullPointerException} when one of the elements of the <code>arguments</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void functionCallExpressionExpressionExpressionArrayNullArgument() {
        new FunctionCallExpression(FUNCTION_EXPRESSION, ARGUMENT_0_EXPRESSION, null, ARGUMENT_2_EXPRESSION, ARGUMENT_3_EXPRESSION);
    }

    /**
     * Asserts that {@link FunctionCallExpression#FunctionCallExpression(Expression, Expression[])} throws a
     * {@link NullPointerException} when the <code>arguments</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void functionCallExpressionExpressionExpressionArrayNullArguments() {
        new FunctionCallExpression(FUNCTION_EXPRESSION, (Expression[]) null);
    }

    /**
     * Asserts that {@link FunctionCallExpression#FunctionCallExpression(Expression, Expression[])} throws a
     * {@link NullPointerException} when the <code>function</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void functionCallExpressionExpressionExpressionArrayNullFunction() {
        new FunctionCallExpression(null, ARGUMENT_0_EXPRESSION, ARGUMENT_1_EXPRESSION, ARGUMENT_2_EXPRESSION, ARGUMENT_3_EXPRESSION);
    }

    /**
     * Asserts that {@link FunctionCallExpression#FunctionCallExpression(Expression, Expression[])} correctly initializes a
     * {@link FunctionCallExpression} when the <code>arguments</code> argument is an empty array.
     */
    @Test
    public void functionCallExpressionExpressionExpressionArrayZeroArguments() {
        final FunctionCallExpression functionCallExpression = new FunctionCallExpression(FUNCTION_EXPRESSION);
        assertThat(functionCallExpression.getFunction(), is(sameInstance(FUNCTION_EXPRESSION)));
        assertThat(functionCallExpression.getArguments(), is(emptyArray()));
    }

    /**
     * Asserts that {@link FunctionCallExpression#FunctionCallExpression(Expression, List)} correctly initializes a
     * {@link FunctionCallExpression}.
     */
    @Test
    public void functionCallExpressionExpressionListOfExpression() {
        final FunctionCallExpression functionCallExpression = new FunctionCallExpression(FUNCTION_EXPRESSION, Arrays.asList(
                ARGUMENT_0_EXPRESSION, ARGUMENT_1_EXPRESSION, ARGUMENT_2_EXPRESSION, ARGUMENT_3_EXPRESSION));
        assertThat(functionCallExpression.getFunction(), is(sameInstance(FUNCTION_EXPRESSION)));
        assertThat(functionCallExpression.getArguments(),
                is(arrayContaining(ARGUMENT_0_EXPRESSION, ARGUMENT_1_EXPRESSION, ARGUMENT_2_EXPRESSION, ARGUMENT_3_EXPRESSION)));
    }

    /**
     * Asserts that {@link FunctionCallExpression#simplify(EvaluationContext)} returns the result of the function call.
     */
    @Test
    public void simplifyFunctionCallIdentity() {
        final ArrayList<AssemblyMessage> messages = new ArrayList<>();
        final Expression stringAValueExpression = new ValueExpression(STRING_A);
        final Expression result = new FunctionCallExpression(new ValueExpression(IDENTITY_FUNCTION_VALUE), stringAValueExpression)
                .simplify(new EvaluationContext(null, 0, new AssemblyMessageCollector(messages)));
        assertThat(result, is(sameInstance(stringAValueExpression)));
        assertThat(messages, is(empty()));
    }

    /**
     * Asserts that {@link FunctionCallExpression#toString()} returns a string representation of the expression.
     */
    @Test
    public void testToString() {
        assertThat(FUNCTION_CALL_EXPRESSION.toString(),
                is("FunctionCallExpression [function=ValueExpression [value=FunctionValue [function=SelectFunction]], "
                        + "arguments=[ValueExpression [value=UnsignedIntValue [value=1]], "
                        + "ValueExpression [value=UnsignedIntValue [value=10]], "
                        + "ValueExpression [value=UnsignedIntValue [value=100]], "
                        + "ValueExpression [value=UnsignedIntValue [value=1000]]]]"));
    }

    /**
     * Asserts that {@link FunctionCallExpression#toIdentifier(EvaluationContext, ValueVisitor)} returns the result of the function
     * call as an identifier.
     */
    @Test
    public void toIdentifier() {
        assertThat(FUNCTION_CALL_EXPRESSION.toIdentifier(EvaluationContext.DUMMY, new ValueToStringVisitor(EvaluationContext.DUMMY,
                "???")), is(new IdentifierExpression("100", null)));
    }

}
