package org.reasm.expressions;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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
import org.reasm.FloatValue;
import org.reasm.Function;
import org.reasm.FunctionValue;
import org.reasm.SignedIntValue;
import org.reasm.StringValue;
import org.reasm.UnsignedIntValue;
import org.reasm.Value;
import org.reasm.messages.CannotConvertStringToFloatErrorMessage;
import org.reasm.messages.DivisionByZeroErrorMessage;
import org.reasm.messages.FunctionOperandNotApplicableErrorMessage;
import org.reasm.testhelpers.AssemblyMessageCollector;
import org.reasm.testhelpers.EquivalentAssemblyMessage;

/**
 * Test class for {@link ExpressionEvaluation}.
 *
 * @author Francis Gagné
 */
@SuppressWarnings("javadoc")
public class ExpressionEvaluationTest {

    public static abstract class BaseEvaluateBinaryTest extends BaseEvaluateTest {

        @CheckForNull
        private final Value operand1;
        @CheckForNull
        private final Value operand2;

        public BaseEvaluateBinaryTest(@CheckForNull Value operand1, @CheckForNull Value operand2,
                @CheckForNull Value expectedResult, @CheckForNull AssemblyMessage expectedAssemblyMessage) {
            super(expectedResult, expectedAssemblyMessage);
            this.operand1 = operand1;
            this.operand2 = operand2;
        }

        @CheckForNull
        @Override
        protected Value run(@Nonnull EvaluationContext evaluationContext) {
            return this.run(this.operand1, this.operand2, evaluationContext);
        }

        @CheckForNull
        protected abstract Value run(@CheckForNull Value operand1, @CheckForNull Value operand2,
                @Nonnull EvaluationContext evaluationContext);

    }

    public static abstract class BaseEvaluateTest {

        @CheckForNull
        private final Value expectedResult;
        @CheckForNull
        private final AssemblyMessage expectedAssemblyMessage;

        public BaseEvaluateTest(@CheckForNull Value expectedResult, @CheckForNull AssemblyMessage expectedAssemblyMessage) {
            this.expectedResult = expectedResult;
            this.expectedAssemblyMessage = expectedAssemblyMessage;
        }

        @Test
        public void test() {
            final ArrayList<AssemblyMessage> messages = new ArrayList<>();
            Value result = this.run(new EvaluationContext(null, 0, new AssemblyMessageCollector(messages)));
            assertThat(result, is(this.expectedResult));

            // Check for generated assembly messages.
            if (this.expectedAssemblyMessage == null) {
                assertThat(messages, is(empty()));
            } else {
                assertThat(messages, contains(new EquivalentAssemblyMessage(this.expectedAssemblyMessage)));
            }
        }

        @CheckForNull
        protected abstract Value run(@Nonnull EvaluationContext evaluationContext);

    }

    public static abstract class BaseEvaluateUnaryTest extends BaseEvaluateTest {

        @CheckForNull
        private final Value operand;

        public BaseEvaluateUnaryTest(@CheckForNull Value operand, @CheckForNull Value expectedResult,
                @CheckForNull AssemblyMessage expectedAssemblyMessage) {
            super(expectedResult, expectedAssemblyMessage);
            this.operand = operand;
        }

        @CheckForNull
        @Override
        protected Value run(@Nonnull EvaluationContext evaluationContext) {
            return this.run(this.operand, evaluationContext);
        }

        @CheckForNull
        protected abstract Value run(@CheckForNull Value operand, @Nonnull EvaluationContext evaluationContext);

    }

    /**
     * Parameterized test class for {@link ExpressionEvaluation#booleanToValue(boolean)}.
     */
    @RunWith(Parameterized.class)
    public static class BooleanToValueTest {

        private static final List<Object[]> TEST_DATA = Arrays.asList(new Object[][] {
                // false
                { false, new UnsignedIntValue(0) },

                // true
                { true, new UnsignedIntValue(1) } });

        @Nonnull
        @Parameters
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        private final boolean input;
        @Nonnull
        private final Value expectedResult;

        public BooleanToValueTest(boolean input, @Nonnull Value expectedResult) {
            this.input = input;
            this.expectedResult = expectedResult;
        }

        /**
         * Asserts that {@link ExpressionEvaluation#booleanToValue(boolean)} converts a boolean to an {@link UnsignedIntValue} with
         * a value of 0 or 1.
         */
        @Test
        public void test() {
            Value result = ExpressionEvaluation.booleanToValue(this.input);
            assertThat(result, is(this.expectedResult));
        }

    }

    /**
     * Parameterized test class for {@link ExpressionEvaluation#evaluateAddition(Value, Value, EvaluationContext)}.
     */
    @RunWith(Parameterized.class)
    public static class EvaluateAdditionTest extends BaseEvaluateBinaryTest {

        private static final FunctionOperandNotApplicableErrorMessage FUNCTION_OPERAND_NOT_APPLICABLE_TO_ADDITION = new FunctionOperandNotApplicableErrorMessage(
                "addition");

        private static final Object[][] TEST_DATA_VALUES = new Object[][] {

                // undetermined and undetermined
                { null, null, null, null },

                // undetermined and unsigned integer
                { null, UINT_42, null, null },

                // undetermined and signed integer
                { null, SINT_42, null, null },

                // undetermined and float
                { null, FLOAT_3_25, null, null },

                // undetermined and a string that can be parsed as a number
                { null, STRING_3_25, null, null },

                // undetermined and a string that cannot be parsed as a number
                { null, STRING_A, null, null },

                // undetermined and function
                { null, UNDETERMINED_FUNCTION_A_VALUE, null, null },

                // unsigned integer and undetermined
                { UINT_42, null, null, null },

                // unsigned integer and unsigned integer
                { UINT_42, UINT_42, UINT_84, null },

                // unsigned integer and signed integer
                { UINT_42, SINT_42, SINT_84, null },

                // unsigned integer and float
                { UINT_42, FLOAT_3_25, FLOAT_45_25, null },

                // huge unsigned integer and float
                { UINT_18000000000000000000, FLOAT_3_25, FLOAT_18000000000000000000_PLUS_3_25, null },

                // unsigned integer and a string that can be parsed as a number
                { UINT_42, STRING_3_25, STRING_423_25, null },

                // unsigned integer and a string that cannot be parsed as a number
                { UINT_42, STRING_A, STRING_42A, null },

                // unsigned integer and function
                { UINT_42, UNDETERMINED_FUNCTION_A_VALUE, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_ADDITION },

                // signed integer and undetermined
                { SINT_42, null, null, null },

                // signed integer and unsigned integer
                { SINT_42, UINT_42, SINT_84, null },

                // signed integer and signed integer
                { SINT_42, SINT_42, SINT_84, null },

                // signed integer and float
                { SINT_42, FLOAT_3_25, FLOAT_45_25, null },

                // negative signed integer and float
                { SINT_MINUS_1, FLOAT_3_25, FLOAT_2_25, null },

                // signed integer and a string that can be parsed as a number
                { SINT_42, STRING_3_25, STRING_423_25, null },

                // signed integer and a string that cannot be parsed as a number
                { SINT_42, STRING_A, STRING_42A, null },

                // signed integer and function
                { SINT_42, UNDETERMINED_FUNCTION_A_VALUE, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_ADDITION },

                // float and undetermined
                { FLOAT_3_25, null, null, null },

                // float and unsigned integer
                { FLOAT_3_25, UINT_42, FLOAT_45_25, null },

                // float and huge unsigned integer
                { FLOAT_3_25, UINT_18000000000000000000, FLOAT_18000000000000000000_PLUS_3_25, null },

                // float and signed integer
                { FLOAT_3_25, SINT_42, FLOAT_45_25, null },

                // float and signed integer
                { FLOAT_3_25, SINT_MINUS_1, FLOAT_2_25, null },

                // float and float
                { FLOAT_3_25, FLOAT_3_25, FLOAT_6_5, null },

                // float and a string that can be parsed as a number
                { FLOAT_3_25, STRING_3_25, STRING_3_253_25, null },

                // float and a string that cannot be parsed as a number
                { FLOAT_3_25, STRING_A, STRING_3_25A, null },

                // float and function
                { FLOAT_3_25, UNDETERMINED_FUNCTION_A_VALUE, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_ADDITION },

                // a string that can be parsed as a number and undetermined
                { STRING_3_25, null, null, null },

                // a string that can be parsed as a number and unsigned integer
                { STRING_3_25, UINT_42, STRING_3_2542, null },

                // a string that can be parsed as a number and signed integer
                { STRING_3_25, SINT_42, STRING_3_2542, null },

                // a string that can be parsed as a number and float
                { STRING_3_25, FLOAT_3_25, STRING_3_253_25, null },

                // a string that can be parsed as a number and a string that can be parsed as a number
                { STRING_3_25, STRING_3_25, STRING_3_253_25, null },

                // a string that can be parsed as a number and a string that cannot be parsed as a number
                { STRING_3_25, STRING_A, STRING_3_25A, null },

                // a string that can be parsed as a number and function
                { STRING_3_25, UNDETERMINED_FUNCTION_A_VALUE, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_ADDITION },

                // a string that cannot be parsed as a number and undetermined
                { STRING_A, null, null, null },

                // a string that cannot be parsed as a number and unsigned integer
                { STRING_A, UINT_42, STRING_A42, null },

                // a string that cannot be parsed as a number and signed integer
                { STRING_A, SINT_42, STRING_A42, null },

                // a string that cannot be parsed as a number and float
                { STRING_A, FLOAT_3_25, STRING_A3_25, null },

                // a string that cannot be parsed as a number and a string that can be parsed as a number
                { STRING_A, STRING_3_25, STRING_A3_25, null },

                // a string that cannot be parsed as a number and a string that cannot be parsed as a number
                { STRING_A, STRING_A, STRING_AA, null },

                // a string that cannot be parsed as a number and function
                { STRING_A, UNDETERMINED_FUNCTION_A_VALUE, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_ADDITION },

                // function and undetermined
                { UNDETERMINED_FUNCTION_A_VALUE, null, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_ADDITION },

                // function and unsigned integer
                { UNDETERMINED_FUNCTION_A_VALUE, UINT_42, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_ADDITION },

                // function and signed integer
                { UNDETERMINED_FUNCTION_A_VALUE, SINT_42, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_ADDITION },

                // function and float
                { UNDETERMINED_FUNCTION_A_VALUE, FLOAT_3_25, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_ADDITION },

                // function and a string that can be parsed as a number
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_3_25, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_ADDITION },

                // function and a string that cannot be parsed as a number
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_A, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_ADDITION },

                // function and function
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_A_VALUE, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_ADDITION },

        };

        private static final List<Object[]> TEST_DATA = Arrays.asList(TEST_DATA_VALUES);

        @Nonnull
        @Parameters(name = "{0} + {1}")
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        public EvaluateAdditionTest(@CheckForNull Value operand1, @CheckForNull Value operand2, @CheckForNull Value expectedResult,
                @CheckForNull AssemblyMessage expectedAssemblyMessage) {
            super(operand1, operand2, expectedResult, expectedAssemblyMessage);
        }

        @CheckForNull
        @Override
        protected Value run(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateAddition(operand1, operand2, evaluationContext);
        }

    }

    /**
     * Parameterized test class for
     * {@link ExpressionEvaluation#evaluateArithmetic(Value, Value, EvaluationContext, ExpressionEvaluation.Arithmetic)}.
     */
    @RunWith(Parameterized.class)
    public static class EvaluateArithmeticTest extends BaseEvaluateBinaryTest {

        private static final ExpressionEvaluation.Arithmetic MULTIPLICATION = ExpressionEvaluation.Arithmetic.MULTIPLICATION;
        private static final ExpressionEvaluation.Arithmetic DIVISION = ExpressionEvaluation.Arithmetic.DIVISION;
        private static final ExpressionEvaluation.Arithmetic MODULUS = ExpressionEvaluation.Arithmetic.MODULUS;
        private static final ExpressionEvaluation.Arithmetic SUBTRACTION = ExpressionEvaluation.Arithmetic.SUBTRACTION;

        private static final FunctionOperandNotApplicableErrorMessage FUNCTION_OPERAND_NOT_APPLICABLE_TO_MULTIPLICATION = new FunctionOperandNotApplicableErrorMessage(
                "multiplication");
        private static final FunctionOperandNotApplicableErrorMessage FUNCTION_OPERAND_NOT_APPLICABLE_TO_DIVISION = new FunctionOperandNotApplicableErrorMessage(
                "division");
        private static final FunctionOperandNotApplicableErrorMessage FUNCTION_OPERAND_NOT_APPLICABLE_TO_MODULUS = new FunctionOperandNotApplicableErrorMessage(
                "modulus");
        private static final FunctionOperandNotApplicableErrorMessage FUNCTION_OPERAND_NOT_APPLICABLE_TO_SUBTRACTION = new FunctionOperandNotApplicableErrorMessage(
                "subtraction");
        private static final DivisionByZeroErrorMessage DIVISION_BY_ZERO = new DivisionByZeroErrorMessage();

        private static final Object[][] TEST_DATA_VALUES = new Object[][] {

                // Multiplication: undetermined and undetermined
                { null, null, MULTIPLICATION, null, null },

                // Multiplication: undetermined and unsigned integer
                { null, UINT_42, MULTIPLICATION, null, null },

                // Multiplication: undetermined and signed integer
                { null, SINT_42, MULTIPLICATION, null, null },

                // Multiplication: undetermined and float
                { null, FLOAT_3_25, MULTIPLICATION, null, null },

                // Multiplication: undetermined and a string that can be parsed as a number
                { null, STRING_3_25, MULTIPLICATION, null, null },

                // Multiplication: undetermined and a string that cannot be parsed as a number
                { null, STRING_A, MULTIPLICATION, null, null },

                // Multiplication: undetermined and function
                { null, UNDETERMINED_FUNCTION_A_VALUE, MULTIPLICATION, null, null },

                // Multiplication: unsigned integer and undetermined
                { UINT_42, null, MULTIPLICATION, null, null },

                // Multiplication: unsigned integer and unsigned integer
                { UINT_42, UINT_42, MULTIPLICATION, UINT_42_TIMES_42, null },

                // Multiplication: 0 as an unsigned integer and unsigned integer
                { UINT_0, UINT_42, MULTIPLICATION, UINT_0, null },

                // Multiplication: unsigned integer and 0 as an unsigned integer
                { UINT_42, UINT_0, MULTIPLICATION, UINT_0, null },

                // Multiplication: unsigned integer and signed integer
                { UINT_42, SINT_42, MULTIPLICATION, SINT_42_TIMES_42, null },

                // Multiplication: 0 as an unsigned integer and signed integer
                { UINT_0, SINT_42, MULTIPLICATION, SINT_0, null },

                // Multiplication: unsigned integer and 0 as a signed integer
                { UINT_42, SINT_0, MULTIPLICATION, SINT_0, null },

                // Multiplication: unsigned integer and float
                { UINT_42, FLOAT_3_25, MULTIPLICATION, FLOAT_42_TIMES_3_25, null },

                // Multiplication: 0 as an unsigned integer and float
                { UINT_0, FLOAT_3_25, MULTIPLICATION, FLOAT_0, null },

                // Multiplication: unsigned integer and 0 as a float
                { UINT_42, FLOAT_0, MULTIPLICATION, FLOAT_0, null },

                // Multiplication: huge unsigned integer and float
                { UINT_18000000000000000000, FLOAT_3_25, MULTIPLICATION, FLOAT_18000000000000000000_TIMES_3_25, null },

                // Multiplication: unsigned integer and a string that can be parsed as a number
                { UINT_42, STRING_3_25, MULTIPLICATION, FLOAT_42_TIMES_3_25, null },

                // Multiplication: unsigned integer and a string that cannot be parsed as a number
                { UINT_42, STRING_A, MULTIPLICATION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Multiplication: unsigned integer and function
                { UINT_42, UNDETERMINED_FUNCTION_A_VALUE, MULTIPLICATION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_MULTIPLICATION },

                // Multiplication: signed integer and undetermined
                { SINT_42, null, MULTIPLICATION, null, null },

                // Multiplication: signed integer and unsigned integer
                { SINT_42, UINT_42, MULTIPLICATION, SINT_42_TIMES_42, null },

                // Multiplication: 0 as a signed integer and unsigned integer
                { SINT_0, UINT_42, MULTIPLICATION, SINT_0, null },

                // Multiplication: signed integer and 0 as an unsigned integer
                { SINT_42, UINT_0, MULTIPLICATION, SINT_0, null },

                // Multiplication: signed integer and signed integer
                { SINT_42, SINT_42, MULTIPLICATION, SINT_42_TIMES_42, null },

                // Multiplication: 0 as a signed integer and signed integer
                { SINT_0, SINT_42, MULTIPLICATION, SINT_0, null },

                // Multiplication: signed integer and 0 as a signed integer
                { SINT_42, SINT_0, MULTIPLICATION, SINT_0, null },

                // Multiplication: signed integer and float
                { SINT_42, FLOAT_3_25, MULTIPLICATION, FLOAT_42_TIMES_3_25, null },

                // Multiplication: 0 as a signed integer and float
                { SINT_0, FLOAT_3_25, MULTIPLICATION, FLOAT_0, null },

                // Multiplication: negative signed integer and float
                { SINT_MINUS_1, FLOAT_3_25, MULTIPLICATION, FLOAT_MINUS_3_25, null },

                // Multiplication: signed integer and 0 as a float
                { SINT_42, FLOAT_0, MULTIPLICATION, FLOAT_0, null },

                // Multiplication: signed integer and a string that can be parsed as a number
                { SINT_42, STRING_3_25, MULTIPLICATION, FLOAT_42_TIMES_3_25, null },

                // Multiplication: signed integer and a string that cannot be parsed as a number
                { SINT_42, STRING_A, MULTIPLICATION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Multiplication: signed integer and function
                { SINT_42, UNDETERMINED_FUNCTION_A_VALUE, MULTIPLICATION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_MULTIPLICATION },

                // Multiplication: float and undetermined
                { FLOAT_3_25, null, MULTIPLICATION, null, null },

                // Multiplication: float and unsigned integer
                { FLOAT_3_25, UINT_42, MULTIPLICATION, FLOAT_42_TIMES_3_25, null },

                // Multiplication: 0 as a float and unsigned integer
                { FLOAT_0, UINT_42, MULTIPLICATION, FLOAT_0, null },

                // Multiplication: float and 0 as an unsigned integer
                { FLOAT_3_25, UINT_0, MULTIPLICATION, FLOAT_0, null },

                // Multiplication: float and huge unsigned integer
                { FLOAT_3_25, UINT_18000000000000000000, MULTIPLICATION, FLOAT_18000000000000000000_TIMES_3_25, null },

                // Multiplication: float and signed integer
                { FLOAT_3_25, SINT_42, MULTIPLICATION, FLOAT_42_TIMES_3_25, null },

                // Multiplication: 0 as a float and signed integer
                { FLOAT_0, SINT_42, MULTIPLICATION, FLOAT_0, null },

                // Multiplication: float and 0 as a signed integer
                { FLOAT_3_25, SINT_0, MULTIPLICATION, FLOAT_0, null },

                // Multiplication: float and negative signed integer
                { FLOAT_3_25, SINT_MINUS_1, MULTIPLICATION, FLOAT_MINUS_3_25, null },

                // Multiplication: float and float
                { FLOAT_3_25, FLOAT_3_25, MULTIPLICATION, FLOAT_3_25_TIMES_3_25, null },

                // Multiplication: 0 as a float and float
                { FLOAT_0, FLOAT_3_25, MULTIPLICATION, FLOAT_0, null },

                // Multiplication: float and 0 as a float
                { FLOAT_3_25, FLOAT_0, MULTIPLICATION, FLOAT_0, null },

                // Multiplication: float and a string that can be parsed as a number
                { FLOAT_3_25, STRING_3_25, MULTIPLICATION, FLOAT_3_25_TIMES_3_25, null },

                // Multiplication: float and a string that cannot be parsed as a number
                { FLOAT_3_25, STRING_A, MULTIPLICATION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Multiplication: float and function
                { FLOAT_3_25, UNDETERMINED_FUNCTION_A_VALUE, MULTIPLICATION, null,
                        FUNCTION_OPERAND_NOT_APPLICABLE_TO_MULTIPLICATION },

                // Multiplication: a string that can be parsed as a number and undetermined
                { STRING_3_25, null, MULTIPLICATION, null, null },

                // Multiplication: a string that can be parsed as a number and unsigned integer
                { STRING_3_25, UINT_42, MULTIPLICATION, FLOAT_42_TIMES_3_25, null },

                // Multiplication: a string that can be parsed as a number and signed integer
                { STRING_3_25, SINT_42, MULTIPLICATION, FLOAT_42_TIMES_3_25, null },

                // Multiplication: a string that can be parsed as a number and float
                { STRING_3_25, FLOAT_3_25, MULTIPLICATION, FLOAT_3_25_TIMES_3_25, null },

                // Multiplication: a string that can be parsed as a number and a string that can be parsed as a number
                { STRING_3_25, STRING_3_25, MULTIPLICATION, FLOAT_3_25_TIMES_3_25, null },

                // Multiplication: a string that can be parsed as a number and a string that cannot be parsed as a number
                { STRING_3_25, STRING_A, MULTIPLICATION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Multiplication: a string that can be parsed as a number and function
                { STRING_3_25, UNDETERMINED_FUNCTION_A_VALUE, MULTIPLICATION, null,
                        FUNCTION_OPERAND_NOT_APPLICABLE_TO_MULTIPLICATION },

                // Multiplication: a string that cannot be parsed as a number and undetermined
                { STRING_A, null, MULTIPLICATION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Multiplication: a string that cannot be parsed as a number and unsigned integer
                { STRING_A, UINT_42, MULTIPLICATION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Multiplication: a string that cannot be parsed as a number and signed integer
                { STRING_A, SINT_42, MULTIPLICATION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Multiplication: a string that cannot be parsed as a number and float
                { STRING_A, FLOAT_3_25, MULTIPLICATION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Multiplication: a string that cannot be parsed as a number and a string that can be parsed as a number
                { STRING_A, STRING_3_25, MULTIPLICATION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Multiplication: a string that cannot be parsed as a number and a string that cannot be parsed as a number
                { STRING_A, STRING_A, MULTIPLICATION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Multiplication: a string that cannot be parsed as a number and function
                { STRING_A, UNDETERMINED_FUNCTION_A_VALUE, MULTIPLICATION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Multiplication: function and undetermined
                { UNDETERMINED_FUNCTION_A_VALUE, null, MULTIPLICATION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_MULTIPLICATION },

                // Multiplication: function and unsigned integer
                { UNDETERMINED_FUNCTION_A_VALUE, UINT_42, MULTIPLICATION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_MULTIPLICATION },

                // Multiplication: function and signed integer
                { UNDETERMINED_FUNCTION_A_VALUE, SINT_42, MULTIPLICATION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_MULTIPLICATION },

                // Multiplication: function and float
                { UNDETERMINED_FUNCTION_A_VALUE, FLOAT_3_25, MULTIPLICATION, null,
                        FUNCTION_OPERAND_NOT_APPLICABLE_TO_MULTIPLICATION },

                // Multiplication: function and a string that can be parsed as a number
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_3_25, MULTIPLICATION, null,
                        FUNCTION_OPERAND_NOT_APPLICABLE_TO_MULTIPLICATION },

                // Multiplication: function and a string that cannot be parsed as a number
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_A, MULTIPLICATION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_MULTIPLICATION },

                // Multiplication: function and function
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_A_VALUE, MULTIPLICATION, null,
                        FUNCTION_OPERAND_NOT_APPLICABLE_TO_MULTIPLICATION },

                // Division: undetermined and undetermined
                { null, null, DIVISION, null, null },

                // Division: undetermined and unsigned integer
                { null, UINT_42, DIVISION, null, null },

                // Division: undetermined and signed integer
                { null, SINT_42, DIVISION, null, null },

                // Division: undetermined and float
                { null, FLOAT_3_25, DIVISION, null, null },

                // Division: undetermined and a string that can be parsed as a number
                { null, STRING_3_25, DIVISION, null, null },

                // Division: undetermined and a string that cannot be parsed as a number
                { null, STRING_A, DIVISION, null, null },

                // Division: undetermined and function
                { null, UNDETERMINED_FUNCTION_A_VALUE, DIVISION, null, null },

                // Division: unsigned integer and undetermined
                { UINT_42, null, DIVISION, null, null },

                // Division: unsigned integer and unsigned integer
                { UINT_42, UINT_4, DIVISION, UINT_10, null },

                // Division: 0 as an unsigned integer and unsigned integer
                { UINT_0, UINT_4, DIVISION, UINT_0, null },

                // Division: unsigned integer and 0 as an unsigned integer
                { UINT_42, UINT_0, DIVISION, null, DIVISION_BY_ZERO },

                // Division: unsigned integer and signed integer
                { UINT_42, SINT_4, DIVISION, SINT_10, null },

                // Division: 0 as an unsigned integer and signed integer
                { UINT_0, SINT_4, DIVISION, SINT_0, null },

                // Division: unsigned integer and 0 as a signed integer
                { UINT_42, SINT_0, DIVISION, null, DIVISION_BY_ZERO },

                // Division: unsigned integer and float
                { UINT_42, FLOAT_3_25, DIVISION, FLOAT_42_DIVIDED_BY_3_25, null },

                // Division: 0 as an unsigned integer and float
                { UINT_0, FLOAT_3_25, DIVISION, FLOAT_0, null },

                // Division: unsigned integer and 0 as a float
                { UINT_42, FLOAT_0, DIVISION, null, DIVISION_BY_ZERO },

                // Division: huge unsigned integer and float
                { UINT_18000000000000000000, FLOAT_3_25, DIVISION, FLOAT_18000000000000000000_DIVIDED_BY_3_25, null },

                // Division: unsigned integer and a string that can be parsed as a number
                { UINT_42, STRING_3_25, DIVISION, FLOAT_42_DIVIDED_BY_3_25, null },

                // Division: unsigned integer and a string that cannot be parsed as a number
                { UINT_42, STRING_A, DIVISION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Division: unsigned integer and function
                { UINT_42, UNDETERMINED_FUNCTION_A_VALUE, DIVISION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_DIVISION },

                // Division: signed integer and undetermined
                { SINT_42, null, DIVISION, null, null },

                // Division: signed integer and unsigned integer
                { SINT_42, UINT_4, DIVISION, SINT_10, null },

                // Division: 0 as a signed integer and unsigned integer
                { SINT_0, UINT_4, DIVISION, SINT_0, null },

                // Division: signed integer and 0 as an unsigned integer
                { SINT_42, UINT_0, DIVISION, null, DIVISION_BY_ZERO },

                // Division: signed integer and signed integer
                { SINT_42, SINT_4, DIVISION, SINT_10, null },

                // Division: 0 as a signed integer and signed integer
                { SINT_0, SINT_4, DIVISION, SINT_0, null },

                // Division: signed integer and 0 as a signed integer
                { SINT_42, SINT_0, DIVISION, null, DIVISION_BY_ZERO },

                // Division: signed integer and float
                { SINT_42, FLOAT_3_25, DIVISION, FLOAT_42_DIVIDED_BY_3_25, null },

                // Division: 0 as a signed integer and float
                { SINT_0, FLOAT_3_25, DIVISION, FLOAT_0, null },

                // Division: negative signed integer and float
                { SINT_MINUS_1, FLOAT_3_25, DIVISION, FLOAT_MINUS_1_DIVIDED_BY_3_25, null },

                // Division: signed integer and 0 as a float
                { SINT_42, FLOAT_0, DIVISION, null, DIVISION_BY_ZERO },

                // Division: signed integer and a string that can be parsed as a number
                { SINT_42, STRING_3_25, DIVISION, FLOAT_42_DIVIDED_BY_3_25, null },

                // Division: signed integer and a string that cannot be parsed as a number
                { SINT_42, STRING_A, DIVISION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Division: signed integer and function
                { SINT_42, UNDETERMINED_FUNCTION_A_VALUE, DIVISION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_DIVISION },

                // Division: float and undetermined
                { FLOAT_3_25, null, DIVISION, null, null },

                // Division: float and unsigned integer
                { FLOAT_3_25, UINT_42, DIVISION, FLOAT_3_25_DIVIDED_BY_42, null },

                // Division: 0 as a float and unsigned integer
                { FLOAT_0, UINT_42, DIVISION, FLOAT_0, null },

                // Division: float and 0 as an unsigned integer
                { FLOAT_3_25, UINT_0, DIVISION, null, DIVISION_BY_ZERO },

                // Division: float and huge unsigned integer
                { FLOAT_3_25, UINT_18000000000000000000, DIVISION, FLOAT_3_25_DIVIDED_BY_18000000000000000000, null },

                // Division: float and signed integer
                { FLOAT_3_25, SINT_42, DIVISION, FLOAT_3_25_DIVIDED_BY_42, null },

                // Division: 0 as a float and signed integer
                { FLOAT_0, SINT_42, DIVISION, FLOAT_0, null },

                // Division: float and 0 as a signed integer
                { FLOAT_3_25, SINT_0, DIVISION, null, DIVISION_BY_ZERO },

                // Division: float and negative signed integer
                { FLOAT_3_25, SINT_MINUS_1, DIVISION, FLOAT_MINUS_3_25, null },

                // Division: float and float
                { FLOAT_3_25, FLOAT_0_5, DIVISION, FLOAT_6_5, null },

                // Division: 0 as a float and float
                { FLOAT_0, FLOAT_0_5, DIVISION, FLOAT_0, null },

                // Division: float and 0 as a float
                { FLOAT_3_25, FLOAT_0, DIVISION, null, DIVISION_BY_ZERO },

                // Division: float and a string that can be parsed as a number
                { FLOAT_3_25, STRING_0_5, DIVISION, FLOAT_6_5, null },

                // Division: float and a string that cannot be parsed as a number
                { FLOAT_3_25, STRING_A, DIVISION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Division: float and function
                { FLOAT_3_25, UNDETERMINED_FUNCTION_A_VALUE, DIVISION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_DIVISION },

                // Division: a string that can be parsed as a number and undetermined
                { STRING_3_25, null, DIVISION, null, null },

                // Division: a string that can be parsed as a number and unsigned integer
                { STRING_3_25, UINT_42, DIVISION, FLOAT_3_25_DIVIDED_BY_42, null },

                // Division: a string that can be parsed as a number and signed integer
                { STRING_3_25, SINT_42, DIVISION, FLOAT_3_25_DIVIDED_BY_42, null },

                // Division: a string that can be parsed as a number and float
                { STRING_3_25, FLOAT_0_5, DIVISION, FLOAT_6_5, null },

                // Division: a string that can be parsed as a number and a string that can be parsed as a number
                { STRING_3_25, STRING_0_5, DIVISION, FLOAT_6_5, null },

                // Division: a string that can be parsed as a number and a string that cannot be parsed as a number
                { STRING_3_25, STRING_A, DIVISION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Division: a string that can be parsed as a number and function
                { STRING_3_25, UNDETERMINED_FUNCTION_A_VALUE, DIVISION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_DIVISION },

                // Division: a string that cannot be parsed as a number and undetermined
                { STRING_A, null, DIVISION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Division: a string that cannot be parsed as a number and unsigned integer
                { STRING_A, UINT_42, DIVISION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Division: a string that cannot be parsed as a number and signed integer
                { STRING_A, SINT_42, DIVISION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Division: a string that cannot be parsed as a number and float
                { STRING_A, FLOAT_3_25, DIVISION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Division: a string that cannot be parsed as a number and a string that can be parsed as a number
                { STRING_A, STRING_3_25, DIVISION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Division: a string that cannot be parsed as a number and a string that cannot be parsed as a number
                { STRING_A, STRING_A, DIVISION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Division: a string that cannot be parsed as a number and function
                { STRING_A, UNDETERMINED_FUNCTION_A_VALUE, DIVISION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Division: function and undetermined
                { UNDETERMINED_FUNCTION_A_VALUE, null, DIVISION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_DIVISION },

                // Division: function and unsigned integer
                { UNDETERMINED_FUNCTION_A_VALUE, UINT_42, DIVISION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_DIVISION },

                // Division: function and signed integer
                { UNDETERMINED_FUNCTION_A_VALUE, SINT_42, DIVISION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_DIVISION },

                // Division: function and float
                { UNDETERMINED_FUNCTION_A_VALUE, FLOAT_3_25, DIVISION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_DIVISION },

                // Division: function and a string that can be parsed as a number
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_3_25, DIVISION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_DIVISION },

                // Division: function and a string that cannot be parsed as a number
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_A, DIVISION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_DIVISION },

                // Division: function and function
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_A_VALUE, DIVISION, null,
                        FUNCTION_OPERAND_NOT_APPLICABLE_TO_DIVISION },

                // Modulus: undetermined and undetermined
                { null, null, MODULUS, null, null },

                // Modulus: undetermined and unsigned integer
                { null, UINT_42, MODULUS, null, null },

                // Modulus: undetermined and signed integer
                { null, SINT_42, MODULUS, null, null },

                // Modulus: undetermined and float
                { null, FLOAT_3_25, MODULUS, null, null },

                // Modulus: undetermined and a string that can be parsed as a number
                { null, STRING_3_25, MODULUS, null, null },

                // Modulus: undetermined and a string that cannot be parsed as a number
                { null, STRING_A, MODULUS, null, null },

                // Modulus: undetermined and function
                { null, UNDETERMINED_FUNCTION_A_VALUE, MODULUS, null, null },

                // Modulus: unsigned integer and undetermined
                { UINT_42, null, MODULUS, null, null },

                // Modulus: unsigned integer and unsigned integer
                { UINT_42, UINT_4, MODULUS, UINT_2, null },

                // Modulus: 0 as an unsigned integer and unsigned integer
                { UINT_0, UINT_4, MODULUS, UINT_0, null },

                // Modulus: unsigned integer and 0 as an unsigned integer
                { UINT_42, UINT_0, MODULUS, null, DIVISION_BY_ZERO },

                // Modulus: unsigned integer and signed integer
                { UINT_42, SINT_4, MODULUS, SINT_2, null },

                // Modulus: 0 as an unsigned integer and signed integer
                { UINT_0, SINT_4, MODULUS, SINT_0, null },

                // Modulus: unsigned integer and 0 as a signed integer
                { UINT_42, SINT_0, MODULUS, null, DIVISION_BY_ZERO },

                // Modulus: unsigned integer and float
                { UINT_42, FLOAT_3_25, MODULUS, FLOAT_42_MODULUS_3_25, null },

                // Modulus: 0 as an unsigned integer and float
                { UINT_0, FLOAT_3_25, MODULUS, FLOAT_0, null },

                // Modulus: unsigned integer and 0 as a float
                { UINT_42, FLOAT_0, MODULUS, null, DIVISION_BY_ZERO },

                // Modulus: huge unsigned integer and float
                { UINT_18000000000000000000, FLOAT_3_25, MODULUS, FLOAT_18000000000000000000_MODULUS_3_25, null },

                // Modulus: unsigned integer and a string that can be parsed as a number
                { UINT_42, STRING_3_25, MODULUS, FLOAT_42_MODULUS_3_25, null },

                // Modulus: unsigned integer and a string that cannot be parsed as a number
                { UINT_42, STRING_A, MODULUS, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Modulus: unsigned integer and function
                { UINT_42, UNDETERMINED_FUNCTION_A_VALUE, MODULUS, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_MODULUS },

                // Modulus: signed integer and undetermined
                { SINT_42, null, MODULUS, null, null },

                // Modulus: signed integer and unsigned integer
                { SINT_42, UINT_4, MODULUS, SINT_2, null },

                // Modulus: 0 as a signed integer and unsigned integer
                { SINT_0, UINT_4, MODULUS, SINT_0, null },

                // Modulus: signed integer and 0 as an unsigned integer
                { SINT_42, UINT_0, MODULUS, null, DIVISION_BY_ZERO },

                // Modulus: signed integer and signed integer
                { SINT_42, SINT_4, MODULUS, SINT_2, null },

                // Modulus: 0 as a signed integer and signed integer
                { SINT_0, SINT_4, MODULUS, SINT_0, null },

                // Modulus: signed integer and 0 as a signed integer
                { SINT_42, SINT_0, MODULUS, null, DIVISION_BY_ZERO },

                // Modulus: signed integer and float
                { SINT_42, FLOAT_3_25, MODULUS, FLOAT_42_MODULUS_3_25, null },

                // Modulus: 0 as a signed integer and float
                { SINT_0, FLOAT_3_25, MODULUS, FLOAT_0, null },

                // Modulus: negative signed integer and float
                { SINT_MINUS_1, FLOAT_3_25, MODULUS, FLOAT_MINUS_1_MODULUS_3_25, null },

                // Modulus: signed integer and 0 as a float
                { SINT_42, FLOAT_0, MODULUS, null, DIVISION_BY_ZERO },

                // Modulus: signed integer and a string that can be parsed as a number
                { SINT_42, STRING_3_25, MODULUS, FLOAT_42_MODULUS_3_25, null },

                // Modulus: signed integer and a string that cannot be parsed as a number
                { SINT_42, STRING_A, MODULUS, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Modulus: signed integer and function
                { SINT_42, UNDETERMINED_FUNCTION_A_VALUE, MODULUS, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_MODULUS },

                // Modulus: float and undetermined
                { FLOAT_3_25, null, MODULUS, null, null },

                // Modulus: float and unsigned integer
                { FLOAT_3_25, UINT_42, MODULUS, FLOAT_3_25_MODULUS_42, null },

                // Modulus: 0 as a float and unsigned integer
                { FLOAT_0, UINT_42, MODULUS, FLOAT_0, null },

                // Modulus: float and 0 as an unsigned integer
                { FLOAT_3_25, UINT_0, MODULUS, null, DIVISION_BY_ZERO },

                // Modulus: float and huge unsigned integer
                { FLOAT_3_25, UINT_18000000000000000000, MODULUS, FLOAT_3_25_MODULUS_18000000000000000000, null },

                // Modulus: float and signed integer
                { FLOAT_3_25, SINT_42, MODULUS, FLOAT_3_25_MODULUS_42, null },

                // Modulus: 0 as a float and signed integer
                { FLOAT_0, SINT_42, MODULUS, FLOAT_0, null },

                // Modulus: float and 0 as a signed integer
                { FLOAT_3_25, SINT_0, MODULUS, null, DIVISION_BY_ZERO },

                // Modulus: float and negative signed integer
                { FLOAT_3_25, SINT_MINUS_1, MODULUS, FLOAT_0_25, null },

                // Modulus: float and float
                { FLOAT_3_25, FLOAT_0_5, MODULUS, FLOAT_0_25, null },

                // Modulus: 0 as a float and float
                { FLOAT_0, FLOAT_0_5, MODULUS, FLOAT_0, null },

                // Modulus: float and 0 as a float
                { FLOAT_3_25, FLOAT_0, MODULUS, null, DIVISION_BY_ZERO },

                // Modulus: float and a string that can be parsed as a number
                { FLOAT_3_25, STRING_0_5, MODULUS, FLOAT_0_25, null },

                // Modulus: float and a string that cannot be parsed as a number
                { FLOAT_3_25, STRING_A, MODULUS, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Modulus: float and function
                { FLOAT_3_25, UNDETERMINED_FUNCTION_A_VALUE, MODULUS, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_MODULUS },

                // Modulus: a string that can be parsed as a number and undetermined
                { STRING_3_25, null, MODULUS, null, null },

                // Modulus: a string that can be parsed as a number and unsigned integer
                { STRING_3_25, UINT_42, MODULUS, FLOAT_3_25_MODULUS_42, null },

                // Modulus: a string that can be parsed as a number and signed integer
                { STRING_3_25, SINT_42, MODULUS, FLOAT_3_25_MODULUS_42, null },

                // Modulus: a string that can be parsed as a number and float
                { STRING_3_25, FLOAT_0_5, MODULUS, FLOAT_0_25, null },

                // Modulus: a string that can be parsed as a number and a string that can be parsed as a number
                { STRING_3_25, STRING_0_5, MODULUS, FLOAT_0_25, null },

                // Modulus: a string that can be parsed as a number and a string that cannot be parsed as a number
                { STRING_3_25, STRING_A, MODULUS, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Modulus: a string that can be parsed as a number and function
                { STRING_3_25, UNDETERMINED_FUNCTION_A_VALUE, MODULUS, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_MODULUS },

                // Modulus: a string that cannot be parsed as a number and undetermined
                { STRING_A, null, MODULUS, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Modulus: a string that cannot be parsed as a number and unsigned integer
                { STRING_A, UINT_42, MODULUS, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Modulus: a string that cannot be parsed as a number and signed integer
                { STRING_A, SINT_42, MODULUS, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Modulus: a string that cannot be parsed as a number and float
                { STRING_A, FLOAT_3_25, MODULUS, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Modulus: a string that cannot be parsed as a number and a string that can be parsed as a number
                { STRING_A, STRING_3_25, MODULUS, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Modulus: a string that cannot be parsed as a number and a string that cannot be parsed as a number
                { STRING_A, STRING_A, MODULUS, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Modulus: a string that cannot be parsed as a number and function
                { STRING_A, UNDETERMINED_FUNCTION_A_VALUE, MODULUS, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Modulus: function and undetermined
                { UNDETERMINED_FUNCTION_A_VALUE, null, MODULUS, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_MODULUS },

                // Modulus: function and unsigned integer
                { UNDETERMINED_FUNCTION_A_VALUE, UINT_42, MODULUS, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_MODULUS },

                // Modulus: function and signed integer
                { UNDETERMINED_FUNCTION_A_VALUE, SINT_42, MODULUS, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_MODULUS },

                // Modulus: function and float
                { UNDETERMINED_FUNCTION_A_VALUE, FLOAT_3_25, MODULUS, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_MODULUS },

                // Modulus: function and a string that can be parsed as a number
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_3_25, MODULUS, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_MODULUS },

                // Modulus: function and a string that cannot be parsed as a number
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_A, MODULUS, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_MODULUS },

                // Modulus: function and function
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_A_VALUE, MODULUS, null,
                        FUNCTION_OPERAND_NOT_APPLICABLE_TO_MODULUS },

                // Subtraction: undetermined and undetermined
                { null, null, SUBTRACTION, null, null },

                // Subtraction: undetermined and unsigned integer
                { null, UINT_42, SUBTRACTION, null, null },

                // Subtraction: undetermined and signed integer
                { null, SINT_42, SUBTRACTION, null, null },

                // Subtraction: undetermined and float
                { null, FLOAT_3_25, SUBTRACTION, null, null },

                // Subtraction: undetermined and a string that can be parsed as a number
                { null, STRING_3_25, SUBTRACTION, null, null },

                // Subtraction: undetermined and a string that cannot be parsed as a number
                { null, STRING_A, SUBTRACTION, null, null },

                // Subtraction: undetermined and function
                { null, UNDETERMINED_FUNCTION_A_VALUE, SUBTRACTION, null, null },

                // Subtraction: unsigned integer and undetermined
                { UINT_42, null, SUBTRACTION, null, null },

                // Subtraction: unsigned integer and unsigned integer
                { UINT_42, UINT_4, SUBTRACTION, UINT_38, null },

                // Subtraction: unsigned integer and unsigned integer, negative result
                { UINT_4, UINT_42, SUBTRACTION, SINT_MINUS_38, null },

                // Subtraction: 0 as an unsigned integer and unsigned integer
                { UINT_0, UINT_42, SUBTRACTION, SINT_MINUS_42, null },

                // Subtraction: unsigned integer and 0 as an unsigned integer
                { UINT_42, UINT_0, SUBTRACTION, UINT_42, null },

                // Subtraction: unsigned integer and signed integer
                { UINT_42, SINT_4, SUBTRACTION, SINT_38, null },

                // Subtraction: 0 as an unsigned integer and signed integer
                { UINT_0, SINT_42, SUBTRACTION, SINT_MINUS_42, null },

                // Subtraction: unsigned integer and 0 as a signed integer
                { UINT_42, SINT_0, SUBTRACTION, SINT_42, null },

                // Subtraction: unsigned integer and float
                { UINT_42, FLOAT_3_25, SUBTRACTION, FLOAT_42_MINUS_3_25, null },

                // Subtraction: 0 as an unsigned integer and float
                { UINT_0, FLOAT_3_25, SUBTRACTION, FLOAT_MINUS_3_25, null },

                // Subtraction: unsigned integer and 0 as a float
                { UINT_42, FLOAT_0, SUBTRACTION, FLOAT_42, null },

                // Subtraction: huge unsigned integer and float
                { UINT_18000000000000000000, FLOAT_3_25, SUBTRACTION, FLOAT_18000000000000000000_MINUS_3_25, null },

                // Subtraction: unsigned integer and a string that can be parsed as a number
                { UINT_42, STRING_3_25, SUBTRACTION, FLOAT_42_MINUS_3_25, null },

                // Subtraction: unsigned integer and a string that cannot be parsed as a number
                { UINT_42, STRING_A, SUBTRACTION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Subtraction: unsigned integer and function
                { UINT_42, UNDETERMINED_FUNCTION_A_VALUE, SUBTRACTION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_SUBTRACTION },

                // Subtraction: signed integer and undetermined
                { SINT_42, null, SUBTRACTION, null, null },

                // Subtraction: signed integer and unsigned integer
                { SINT_42, UINT_4, SUBTRACTION, SINT_38, null },

                // Subtraction: 0 as a signed integer and unsigned integer
                { SINT_0, UINT_42, SUBTRACTION, SINT_MINUS_42, null },

                // Subtraction: signed integer and 0 as an unsigned integer
                { SINT_42, UINT_0, SUBTRACTION, SINT_42, null },

                // Subtraction: signed integer and signed integer
                { SINT_42, SINT_4, SUBTRACTION, SINT_38, null },

                // Subtraction: 0 as a signed integer and signed integer
                { SINT_0, SINT_42, SUBTRACTION, SINT_MINUS_42, null },

                // Subtraction: signed integer and 0 as a signed integer
                { SINT_42, SINT_0, SUBTRACTION, SINT_42, null },

                // Subtraction: signed integer and float
                { SINT_42, FLOAT_3_25, SUBTRACTION, FLOAT_42_MINUS_3_25, null },

                // Subtraction: 0 as a signed integer and float
                { SINT_0, FLOAT_3_25, SUBTRACTION, FLOAT_MINUS_3_25, null },

                // Subtraction: negative signed integer and float
                { SINT_MINUS_1, FLOAT_3_25, SUBTRACTION, FLOAT_MINUS_4_25, null },

                // Subtraction: signed integer and 0 as a float
                { SINT_42, FLOAT_0, SUBTRACTION, FLOAT_42, null },

                // Subtraction: signed integer and a string that can be parsed as a number
                { SINT_42, STRING_3_25, SUBTRACTION, FLOAT_42_MINUS_3_25, null },

                // Subtraction: signed integer and a string that cannot be parsed as a number
                { SINT_42, STRING_A, SUBTRACTION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Subtraction: signed integer and function
                { SINT_42, UNDETERMINED_FUNCTION_A_VALUE, SUBTRACTION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_SUBTRACTION },

                // Subtraction: float and undetermined
                { FLOAT_3_25, null, SUBTRACTION, null, null },

                // Subtraction: float and unsigned integer
                { FLOAT_3_25, UINT_42, SUBTRACTION, FLOAT_3_25_MINUS_42, null },

                // Subtraction: 0 as a float and unsigned integer
                { FLOAT_0, UINT_42, SUBTRACTION, FLOAT_MINUS_42, null },

                // Subtraction: float and 0 as an unsigned integer
                { FLOAT_3_25, UINT_0, SUBTRACTION, FLOAT_3_25, null },

                // Subtraction: float and huge unsigned integer
                { FLOAT_3_25, UINT_18000000000000000000, SUBTRACTION, FLOAT_3_25_MINUS_18000000000000000000, null },

                // Subtraction: float and signed integer
                { FLOAT_3_25, SINT_42, SUBTRACTION, FLOAT_3_25_MINUS_42, null },

                // Subtraction: 0 as a float and signed integer
                { FLOAT_0, SINT_42, SUBTRACTION, FLOAT_MINUS_42, null },

                // Subtraction: float and 0 as a signed integer
                { FLOAT_3_25, SINT_0, SUBTRACTION, FLOAT_3_25, null },

                // Subtraction: float and negative signed integer
                { FLOAT_3_25, SINT_MINUS_1, SUBTRACTION, FLOAT_4_25, null },

                // Subtraction: float and float
                { FLOAT_3_25, FLOAT_0_5, SUBTRACTION, FLOAT_2_75, null },

                // Subtraction: 0 as a float and float
                { FLOAT_0, FLOAT_3_25, SUBTRACTION, FLOAT_MINUS_3_25, null },

                // Subtraction: float and 0 as a float
                { FLOAT_3_25, FLOAT_0, SUBTRACTION, FLOAT_3_25, null },

                // Subtraction: float and a string that can be parsed as a number
                { FLOAT_3_25, STRING_0_5, SUBTRACTION, FLOAT_2_75, null },

                // Subtraction: float and a string that cannot be parsed as a number
                { FLOAT_3_25, STRING_A, SUBTRACTION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Subtraction: float and function
                { FLOAT_3_25, UNDETERMINED_FUNCTION_A_VALUE, SUBTRACTION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_SUBTRACTION },

                // Subtraction: a string that can be parsed as a number and undetermined
                { STRING_3_25, null, SUBTRACTION, null, null },

                // Subtraction: a string that can be parsed as a number and unsigned integer
                { STRING_3_25, UINT_42, SUBTRACTION, FLOAT_3_25_MINUS_42, null },

                // Subtraction: a string that can be parsed as a number and signed integer
                { STRING_3_25, SINT_42, SUBTRACTION, FLOAT_3_25_MINUS_42, null },

                // Subtraction: a string that can be parsed as a number and float
                { STRING_3_25, FLOAT_0_5, SUBTRACTION, FLOAT_2_75, null },

                // Subtraction: a string that can be parsed as a number and a string that can be parsed as a number
                { STRING_3_25, STRING_0_5, SUBTRACTION, FLOAT_2_75, null },

                // Subtraction: a string that can be parsed as a number and a string that cannot be parsed as a number
                { STRING_3_25, STRING_A, SUBTRACTION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Subtraction: a string that can be parsed as a number and function
                { STRING_3_25, UNDETERMINED_FUNCTION_A_VALUE, SUBTRACTION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_SUBTRACTION },

                // Subtraction: a string that cannot be parsed as a number and undetermined
                { STRING_A, null, SUBTRACTION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Subtraction: a string that cannot be parsed as a number and unsigned integer
                { STRING_A, UINT_42, SUBTRACTION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Subtraction: a string that cannot be parsed as a number and signed integer
                { STRING_A, SINT_42, SUBTRACTION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Subtraction: a string that cannot be parsed as a number and float
                { STRING_A, FLOAT_3_25, SUBTRACTION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Subtraction: a string that cannot be parsed as a number and a string that can be parsed as a number
                { STRING_A, STRING_3_25, SUBTRACTION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Subtraction: a string that cannot be parsed as a number and a string that cannot be parsed as a number
                { STRING_A, STRING_A, SUBTRACTION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Subtraction: a string that cannot be parsed as a number and function
                { STRING_A, UNDETERMINED_FUNCTION_A_VALUE, SUBTRACTION, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Subtraction: function and undetermined
                { UNDETERMINED_FUNCTION_A_VALUE, null, SUBTRACTION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_SUBTRACTION },

                // Subtraction: function and unsigned integer
                { UNDETERMINED_FUNCTION_A_VALUE, UINT_42, SUBTRACTION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_SUBTRACTION },

                // Subtraction: function and signed integer
                { UNDETERMINED_FUNCTION_A_VALUE, SINT_42, SUBTRACTION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_SUBTRACTION },

                // Subtraction: function and float
                { UNDETERMINED_FUNCTION_A_VALUE, FLOAT_3_25, SUBTRACTION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_SUBTRACTION },

                // Subtraction: function and a string that can be parsed as a number
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_3_25, SUBTRACTION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_SUBTRACTION },

                // Subtraction: function and a string that cannot be parsed as a number
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_A, SUBTRACTION, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_SUBTRACTION },

                // Subtraction: function and function
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_A_VALUE, SUBTRACTION, null,
                        FUNCTION_OPERAND_NOT_APPLICABLE_TO_SUBTRACTION },

        };

        private static final List<Object[]> TEST_DATA = Arrays.asList(TEST_DATA_VALUES);

        @Nonnull
        @Parameters(name = "{0} {2} {1}")
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        @Nonnull
        private final ExpressionEvaluation.Arithmetic operator;

        public EvaluateArithmeticTest(@CheckForNull Value operand1, @CheckForNull Value operand2,
                @Nonnull ExpressionEvaluation.Arithmetic operator, @CheckForNull Value expectedResult,
                @CheckForNull AssemblyMessage expectedAssemblyMessage) {
            super(operand1, operand2, expectedResult, expectedAssemblyMessage);
            this.operator = operator;
        }

        @CheckForNull
        @Override
        protected Value run(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateArithmetic(operand1, operand2, evaluationContext, this.operator);
        }

    }

    /**
     * Parameterized test class for
     * {@link ExpressionEvaluation#evaluateBinaryBitwise(Value, Value, EvaluationContext, ExpressionEvaluation.BinaryBitwise)}.
     */
    @RunWith(Parameterized.class)
    public static class EvaluateBinaryBitwiseTest extends BaseEvaluateBinaryTest {

        private static final ExpressionEvaluation.BinaryBitwise BITWISE_AND = ExpressionEvaluation.BinaryBitwise.BITWISE_AND;
        private static final ExpressionEvaluation.BinaryBitwise BITWISE_XOR = ExpressionEvaluation.BinaryBitwise.BITWISE_XOR;
        private static final ExpressionEvaluation.BinaryBitwise BITWISE_OR = ExpressionEvaluation.BinaryBitwise.BITWISE_OR;

        private static final FunctionOperandNotApplicableErrorMessage FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_AND = new FunctionOperandNotApplicableErrorMessage(
                "bitwise AND");
        private static final FunctionOperandNotApplicableErrorMessage FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_XOR = new FunctionOperandNotApplicableErrorMessage(
                "bitwise XOR");
        private static final FunctionOperandNotApplicableErrorMessage FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_OR = new FunctionOperandNotApplicableErrorMessage(
                "bitwise OR");

        private static final Object[][] TEST_DATA_VALUES = new Object[][] {

                // Bitwise AND: undetermined and undetermined
                { null, null, BITWISE_AND, null, null },

                // Bitwise AND: undetermined and unsigned integer
                { null, UINT_10, BITWISE_AND, null, null },

                // Bitwise AND: undetermined and signed integer
                { null, SINT_10, BITWISE_AND, null, null },

                // Bitwise AND: undetermined and float
                { null, FLOAT_3_25, BITWISE_AND, null, null },

                // Bitwise AND: undetermined and a string that can be parsed as a number
                { null, STRING_3_25, BITWISE_AND, null, null },

                // Bitwise AND: undetermined and a string that cannot be parsed as a number
                { null, STRING_A, BITWISE_AND, null, null },

                // Bitwise AND: undetermined and function
                { null, UNDETERMINED_FUNCTION_A_VALUE, BITWISE_AND, null, null },

                // Bitwise AND: unsigned integer and undetermined
                { UINT_42, null, BITWISE_AND, null, null },

                // Bitwise AND: unsigned integer and unsigned integer
                { UINT_42, UINT_15, BITWISE_AND, UINT_10, null },

                // Bitwise AND: unsigned integer and signed integer
                { UINT_42, SINT_15, BITWISE_AND, UINT_10, null },

                // Bitwise AND: unsigned integer and float
                { UINT_42, FLOAT_3_25, BITWISE_AND, UINT_2, null },

                // Bitwise AND: unsigned integer and a string that can be parsed as a number
                { UINT_42, STRING_3_25, BITWISE_AND, UINT_2, null },

                // Bitwise AND: unsigned integer and a string that cannot be parsed as a number
                { UINT_42, STRING_A, BITWISE_AND, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise AND: unsigned integer and function
                { UINT_42, UNDETERMINED_FUNCTION_A_VALUE, BITWISE_AND, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_AND },

                // Bitwise AND: signed integer and undetermined
                { SINT_42, null, BITWISE_AND, null, null },

                // Bitwise AND: signed integer and unsigned integer
                { SINT_42, UINT_15, BITWISE_AND, UINT_10, null },

                // Bitwise AND: signed integer and signed integer
                { SINT_42, SINT_15, BITWISE_AND, UINT_10, null },

                // Bitwise AND: signed integer and float
                { SINT_42, FLOAT_3_25, BITWISE_AND, UINT_2, null },

                // Bitwise AND: signed integer and a string that can be parsed as a number
                { SINT_42, STRING_3_25, BITWISE_AND, UINT_2, null },

                // Bitwise AND: signed integer and a string that cannot be parsed as a number
                { SINT_42, STRING_A, BITWISE_AND, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise AND: signed integer and function
                { SINT_42, UNDETERMINED_FUNCTION_A_VALUE, BITWISE_AND, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_AND },

                // Bitwise AND: float and undetermined
                { FLOAT_3_25, null, BITWISE_AND, null, null },

                // Bitwise AND: float and unsigned integer
                { FLOAT_3_25, UINT_42, BITWISE_AND, UINT_2, null },

                // Bitwise AND: float and signed integer
                { FLOAT_3_25, SINT_42, BITWISE_AND, UINT_2, null },

                // Bitwise AND: float and float
                { FLOAT_3_25, FLOAT_42, BITWISE_AND, UINT_2, null },

                // Bitwise AND: float and a string that can be parsed as a number
                { FLOAT_3_25, STRING_42, BITWISE_AND, UINT_2, null },

                // Bitwise AND: float and a string that cannot be parsed as a number
                { FLOAT_3_25, STRING_A, BITWISE_AND, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise AND: float and function
                { FLOAT_3_25, UNDETERMINED_FUNCTION_A_VALUE, BITWISE_AND, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_AND },

                // Bitwise AND: a string that can be parsed as a number and undetermined
                { STRING_3_25, null, BITWISE_AND, null, null },

                // Bitwise AND: a string that can be parsed as a number and unsigned integer
                { STRING_3_25, UINT_42, BITWISE_AND, UINT_2, null },

                // Bitwise AND: a string that can be parsed as a number and signed integer
                { STRING_3_25, SINT_42, BITWISE_AND, UINT_2, null },

                // Bitwise AND: a string that can be parsed as a number and float
                { STRING_3_25, FLOAT_42, BITWISE_AND, UINT_2, null },

                // Bitwise AND: a string that can be parsed as a number and a string that can be parsed as a number
                { STRING_3_25, STRING_42, BITWISE_AND, UINT_2, null },

                // Bitwise AND: a string that can be parsed as a number and a string that cannot be parsed as a number
                { STRING_3_25, STRING_A, BITWISE_AND, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise AND: a string that can be parsed as a number and function
                { STRING_3_25, UNDETERMINED_FUNCTION_A_VALUE, BITWISE_AND, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_AND },

                // Bitwise AND: a string that cannot be parsed as a number and undetermined
                { STRING_A, null, BITWISE_AND, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise AND: a string that cannot be parsed as a number and unsigned integer
                { STRING_A, UINT_42, BITWISE_AND, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise AND: a string that cannot be parsed as a number and signed integer
                { STRING_A, SINT_42, BITWISE_AND, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise AND: a string that cannot be parsed as a number and float
                { STRING_A, FLOAT_3_25, BITWISE_AND, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise AND: a string that cannot be parsed as a number and a string that can be parsed as a number
                { STRING_A, STRING_3_25, BITWISE_AND, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise AND: a string that cannot be parsed as a number and a string that cannot be parsed as a number
                { STRING_A, STRING_A, BITWISE_AND, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise AND: a string that cannot be parsed as a number and function
                { STRING_A, UNDETERMINED_FUNCTION_A_VALUE, BITWISE_AND, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise AND: function and undetermined
                { UNDETERMINED_FUNCTION_A_VALUE, null, BITWISE_AND, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_AND },

                // Bitwise AND: function and unsigned integer
                { UNDETERMINED_FUNCTION_A_VALUE, UINT_42, BITWISE_AND, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_AND },

                // Bitwise AND: function and signed integer
                { UNDETERMINED_FUNCTION_A_VALUE, SINT_42, BITWISE_AND, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_AND },

                // Bitwise AND: function and float
                { UNDETERMINED_FUNCTION_A_VALUE, FLOAT_3_25, BITWISE_AND, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_AND },

                // Bitwise AND: function and a string that can be parsed as a number
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_3_25, BITWISE_AND, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_AND },

                // Bitwise AND: function and a string that cannot be parsed as a number
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_A, BITWISE_AND, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_AND },

                // Bitwise AND: function and function
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_A_VALUE, BITWISE_AND, null,
                        FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_AND },

                // Bitwise XOR: undetermined and undetermined
                { null, null, BITWISE_XOR, null, null },

                // Bitwise XOR: undetermined and unsigned integer
                { null, UINT_10, BITWISE_XOR, null, null },

                // Bitwise XOR: undetermined and signed integer
                { null, SINT_10, BITWISE_XOR, null, null },

                // Bitwise XOR: undetermined and float
                { null, FLOAT_3_25, BITWISE_XOR, null, null },

                // Bitwise XOR: undetermined and a string that can be parsed as a number
                { null, STRING_3_25, BITWISE_XOR, null, null },

                // Bitwise XOR: undetermined and a string that cannot be parsed as a number
                { null, STRING_A, BITWISE_XOR, null, null },

                // Bitwise XOR: undetermined and function
                { null, UNDETERMINED_FUNCTION_A_VALUE, BITWISE_XOR, null, null },

                // Bitwise XOR: unsigned integer and undetermined
                { UINT_42, null, BITWISE_XOR, null, null },

                // Bitwise XOR: unsigned integer and unsigned integer
                { UINT_42, UINT_15, BITWISE_XOR, UINT_37, null },

                // Bitwise XOR: unsigned integer and signed integer
                { UINT_42, SINT_15, BITWISE_XOR, UINT_37, null },

                // Bitwise XOR: unsigned integer and float
                { UINT_42, FLOAT_3_25, BITWISE_XOR, UINT_41, null },

                // Bitwise XOR: unsigned integer and a string that can be parsed as a number
                { UINT_42, STRING_3_25, BITWISE_XOR, UINT_41, null },

                // Bitwise XOR: unsigned integer and a string that cannot be parsed as a number
                { UINT_42, STRING_A, BITWISE_XOR, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise XOR: unsigned integer and function
                { UINT_42, UNDETERMINED_FUNCTION_A_VALUE, BITWISE_XOR, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_XOR },

                // Bitwise XOR: signed integer and undetermined
                { SINT_42, null, BITWISE_XOR, null, null },

                // Bitwise XOR: signed integer and unsigned integer
                { SINT_42, UINT_15, BITWISE_XOR, UINT_37, null },

                // Bitwise XOR: signed integer and signed integer
                { SINT_42, SINT_15, BITWISE_XOR, UINT_37, null },

                // Bitwise XOR: signed integer and float
                { SINT_42, FLOAT_3_25, BITWISE_XOR, UINT_41, null },

                // Bitwise XOR: signed integer and a string that can be parsed as a number
                { SINT_42, STRING_3_25, BITWISE_XOR, UINT_41, null },

                // Bitwise XOR: signed integer and a string that cannot be parsed as a number
                { SINT_42, STRING_A, BITWISE_XOR, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise XOR: signed integer and function
                { SINT_42, UNDETERMINED_FUNCTION_A_VALUE, BITWISE_XOR, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_XOR },

                // Bitwise XOR: float and undetermined
                { FLOAT_3_25, null, BITWISE_XOR, null, null },

                // Bitwise XOR: float and unsigned integer
                { FLOAT_3_25, UINT_42, BITWISE_XOR, UINT_41, null },

                // Bitwise XOR: float and signed integer
                { FLOAT_3_25, SINT_42, BITWISE_XOR, UINT_41, null },

                // Bitwise XOR: float and float
                { FLOAT_3_25, FLOAT_42, BITWISE_XOR, UINT_41, null },

                // Bitwise XOR: float and a string that can be parsed as a number
                { FLOAT_3_25, STRING_42, BITWISE_XOR, UINT_41, null },

                // Bitwise XOR: float and a string that cannot be parsed as a number
                { FLOAT_3_25, STRING_A, BITWISE_XOR, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise XOR: float and function
                { FLOAT_3_25, UNDETERMINED_FUNCTION_A_VALUE, BITWISE_XOR, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_XOR },

                // Bitwise XOR: a string that can be parsed as a number and undetermined
                { STRING_3_25, null, BITWISE_XOR, null, null },

                // Bitwise XOR: a string that can be parsed as a number and unsigned integer
                { STRING_3_25, UINT_42, BITWISE_XOR, UINT_41, null },

                // Bitwise XOR: a string that can be parsed as a number and signed integer
                { STRING_3_25, SINT_42, BITWISE_XOR, UINT_41, null },

                // Bitwise XOR: a string that can be parsed as a number and float
                { STRING_3_25, FLOAT_42, BITWISE_XOR, UINT_41, null },

                // Bitwise XOR: a string that can be parsed as a number and a string that can be parsed as a number
                { STRING_3_25, STRING_42, BITWISE_XOR, UINT_41, null },

                // Bitwise XOR: a string that can be parsed as a number and a string that cannot be parsed as a number
                { STRING_3_25, STRING_A, BITWISE_XOR, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise XOR: a string that can be parsed as a number and function
                { STRING_3_25, UNDETERMINED_FUNCTION_A_VALUE, BITWISE_XOR, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_XOR },

                // Bitwise XOR: a string that cannot be parsed as a number and undetermined
                { STRING_A, null, BITWISE_XOR, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise XOR: a string that cannot be parsed as a number and unsigned integer
                { STRING_A, UINT_42, BITWISE_XOR, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise XOR: a string that cannot be parsed as a number and signed integer
                { STRING_A, SINT_42, BITWISE_XOR, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise XOR: a string that cannot be parsed as a number and float
                { STRING_A, FLOAT_3_25, BITWISE_XOR, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise XOR: a string that cannot be parsed as a number and a string that can be parsed as a number
                { STRING_A, STRING_3_25, BITWISE_XOR, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise XOR: a string that cannot be parsed as a number and a string that cannot be parsed as a number
                { STRING_A, STRING_A, BITWISE_XOR, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise XOR: a string that cannot be parsed as a number and function
                { STRING_A, UNDETERMINED_FUNCTION_A_VALUE, BITWISE_XOR, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise XOR: function and undetermined
                { UNDETERMINED_FUNCTION_A_VALUE, null, BITWISE_XOR, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_XOR },

                // Bitwise XOR: function and unsigned integer
                { UNDETERMINED_FUNCTION_A_VALUE, UINT_42, BITWISE_XOR, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_XOR },

                // Bitwise XOR: function and signed integer
                { UNDETERMINED_FUNCTION_A_VALUE, SINT_42, BITWISE_XOR, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_XOR },

                // Bitwise XOR: function and float
                { UNDETERMINED_FUNCTION_A_VALUE, FLOAT_3_25, BITWISE_XOR, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_XOR },

                // Bitwise XOR: function and a string that can be parsed as a number
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_3_25, BITWISE_XOR, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_XOR },

                // Bitwise XOR: function and a string that cannot be parsed as a number
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_A, BITWISE_XOR, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_XOR },

                // Bitwise XOR: function and function
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_A_VALUE, BITWISE_XOR, null,
                        FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_XOR },

                // Bitwise OR: undetermined and undetermined
                { null, null, BITWISE_OR, null, null },

                // Bitwise OR: undetermined and unsigned integer
                { null, UINT_10, BITWISE_OR, null, null },

                // Bitwise OR: undetermined and signed integer
                { null, SINT_10, BITWISE_OR, null, null },

                // Bitwise OR: undetermined and float
                { null, FLOAT_3_25, BITWISE_OR, null, null },

                // Bitwise OR: undetermined and a string that can be parsed as a number
                { null, STRING_3_25, BITWISE_OR, null, null },

                // Bitwise OR: undetermined and a string that cannot be parsed as a number
                { null, STRING_A, BITWISE_OR, null, null },

                // Bitwise OR: undetermined and function
                { null, UNDETERMINED_FUNCTION_A_VALUE, BITWISE_OR, null, null },

                // Bitwise OR: unsigned integer and undetermined
                { UINT_42, null, BITWISE_OR, null, null },

                // Bitwise OR: unsigned integer and unsigned integer
                { UINT_42, UINT_15, BITWISE_OR, UINT_47, null },

                // Bitwise OR: unsigned integer and signed integer
                { UINT_42, SINT_15, BITWISE_OR, UINT_47, null },

                // Bitwise OR: unsigned integer and float
                { UINT_42, FLOAT_3_25, BITWISE_OR, UINT_43, null },

                // Bitwise OR: unsigned integer and a string that can be parsed as a number
                { UINT_42, STRING_3_25, BITWISE_OR, UINT_43, null },

                // Bitwise OR: unsigned integer and a string that cannot be parsed as a number
                { UINT_42, STRING_A, BITWISE_OR, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise OR: unsigned integer and function
                { UINT_42, UNDETERMINED_FUNCTION_A_VALUE, BITWISE_OR, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_OR },

                // Bitwise OR: signed integer and undetermined
                { SINT_42, null, BITWISE_OR, null, null },

                // Bitwise OR: signed integer and unsigned integer
                { SINT_42, UINT_15, BITWISE_OR, UINT_47, null },

                // Bitwise OR: signed integer and signed integer
                { SINT_42, SINT_15, BITWISE_OR, UINT_47, null },

                // Bitwise OR: signed integer and float
                { SINT_42, FLOAT_3_25, BITWISE_OR, UINT_43, null },

                // Bitwise OR: signed integer and a string that can be parsed as a number
                { SINT_42, STRING_3_25, BITWISE_OR, UINT_43, null },

                // Bitwise OR: signed integer and a string that cannot be parsed as a number
                { SINT_42, STRING_A, BITWISE_OR, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise OR: signed integer and function
                { SINT_42, UNDETERMINED_FUNCTION_A_VALUE, BITWISE_OR, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_OR },

                // Bitwise OR: float and undetermined
                { FLOAT_3_25, null, BITWISE_OR, null, null },

                // Bitwise OR: float and unsigned integer
                { FLOAT_3_25, UINT_42, BITWISE_OR, UINT_43, null },

                // Bitwise OR: float and signed integer
                { FLOAT_3_25, SINT_42, BITWISE_OR, UINT_43, null },

                // Bitwise OR: float and float
                { FLOAT_3_25, FLOAT_42, BITWISE_OR, UINT_43, null },

                // Bitwise OR: float and a string that can be parsed as a number
                { FLOAT_3_25, STRING_42, BITWISE_OR, UINT_43, null },

                // Bitwise OR: float and a string that cannot be parsed as a number
                { FLOAT_3_25, STRING_A, BITWISE_OR, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise OR: float and function
                { FLOAT_3_25, UNDETERMINED_FUNCTION_A_VALUE, BITWISE_OR, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_OR },

                // Bitwise OR: a string that can be parsed as a number and undetermined
                { STRING_3_25, null, BITWISE_OR, null, null },

                // Bitwise OR: a string that can be parsed as a number and unsigned integer
                { STRING_3_25, UINT_42, BITWISE_OR, UINT_43, null },

                // Bitwise OR: a string that can be parsed as a number and signed integer
                { STRING_3_25, SINT_42, BITWISE_OR, UINT_43, null },

                // Bitwise OR: a string that can be parsed as a number and float
                { STRING_3_25, FLOAT_42, BITWISE_OR, UINT_43, null },

                // Bitwise OR: a string that can be parsed as a number and a string that can be parsed as a number
                { STRING_3_25, STRING_42, BITWISE_OR, UINT_43, null },

                // Bitwise OR: a string that can be parsed as a number and a string that cannot be parsed as a number
                { STRING_3_25, STRING_A, BITWISE_OR, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise OR: a string that can be parsed as a number and function
                { STRING_3_25, UNDETERMINED_FUNCTION_A_VALUE, BITWISE_OR, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_OR },

                // Bitwise OR: a string that cannot be parsed as a number and undetermined
                { STRING_A, null, BITWISE_OR, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise OR: a string that cannot be parsed as a number and unsigned integer
                { STRING_A, UINT_42, BITWISE_OR, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise OR: a string that cannot be parsed as a number and signed integer
                { STRING_A, SINT_42, BITWISE_OR, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise OR: a string that cannot be parsed as a number and float
                { STRING_A, FLOAT_3_25, BITWISE_OR, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise OR: a string that cannot be parsed as a number and a string that can be parsed as a number
                { STRING_A, STRING_3_25, BITWISE_OR, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise OR: a string that cannot be parsed as a number and a string that cannot be parsed as a number
                { STRING_A, STRING_A, BITWISE_OR, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise OR: a string that cannot be parsed as a number and function
                { STRING_A, UNDETERMINED_FUNCTION_A_VALUE, BITWISE_OR, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bitwise OR: function and undetermined
                { UNDETERMINED_FUNCTION_A_VALUE, null, BITWISE_OR, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_OR },

                // Bitwise OR: function and unsigned integer
                { UNDETERMINED_FUNCTION_A_VALUE, UINT_42, BITWISE_OR, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_OR },

                // Bitwise OR: function and signed integer
                { UNDETERMINED_FUNCTION_A_VALUE, SINT_42, BITWISE_OR, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_OR },

                // Bitwise OR: function and float
                { UNDETERMINED_FUNCTION_A_VALUE, FLOAT_3_25, BITWISE_OR, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_OR },

                // Bitwise OR: function and a string that can be parsed as a number
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_3_25, BITWISE_OR, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_OR },

                // Bitwise OR: function and a string that cannot be parsed as a number
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_A, BITWISE_OR, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_OR },

                // Bitwise OR: function and function
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_A_VALUE, BITWISE_OR, null,
                        FUNCTION_OPERAND_NOT_APPLICABLE_TO_BITWISE_OR },

        };

        private static final List<Object[]> TEST_DATA = Arrays.asList(TEST_DATA_VALUES);

        @Nonnull
        @Parameters(name = "{0} {2} {1}")
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        @Nonnull
        private final ExpressionEvaluation.BinaryBitwise operator;

        public EvaluateBinaryBitwiseTest(@CheckForNull Value operand1, @CheckForNull Value operand2,
                @Nonnull ExpressionEvaluation.BinaryBitwise operator, @CheckForNull Value expectedResult,
                @CheckForNull AssemblyMessage expectedAssemblyMessage) {
            super(operand1, operand2, expectedResult, expectedAssemblyMessage);
            this.operator = operator;
        }

        @CheckForNull
        @Override
        protected Value run(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateBinaryBitwise(operand1, operand2, evaluationContext, this.operator);
        }

    }

    /**
     * Parameterized test class for
     * {@link ExpressionEvaluation#evaluateBinaryLogical(Value, Value, ExpressionEvaluation.BinaryLogical)}.
     */
    @RunWith(Parameterized.class)
    public static class EvaluateBinaryLogicalTest extends BaseEvaluateBinaryTest {

        private static final ExpressionEvaluation.BinaryLogical LOGICAL_AND = ExpressionEvaluation.BinaryLogical.LOGICAL_AND;
        private static final ExpressionEvaluation.BinaryLogical LOGICAL_OR = ExpressionEvaluation.BinaryLogical.LOGICAL_OR;

        private static final Object[][] TEST_DATA_VALUES = new Object[][] {

                // Logical AND: undetermined and undetermined
                { null, null, LOGICAL_AND, null, null },

                // Logical AND: undetermined and the zero unsigned integer
                { null, UINT_0, LOGICAL_AND, null, null },

                // Logical AND: undetermined and unsigned integer
                { null, UINT_42, LOGICAL_AND, null, null },

                // Logical AND: undetermined and the zero signed integer
                { null, SINT_0, LOGICAL_AND, null, null },

                // Logical AND: undetermined and signed integer
                { null, SINT_42, LOGICAL_AND, null, null },

                // Logical AND: undetermined and the zero float
                { null, FLOAT_0, LOGICAL_AND, null, null },

                // Logical AND: undetermined and float
                { null, FLOAT_3_25, LOGICAL_AND, null, null },

                // Logical AND: undetermined and empty string
                { null, STRING_EMPTY, LOGICAL_AND, null, null },

                // Logical AND: undetermined and string
                { null, STRING_A, LOGICAL_AND, null, null },

                // Logical AND: undetermined and function
                { null, UNDETERMINED_FUNCTION_A_VALUE, LOGICAL_AND, null, null },

                // Logical AND: the zero unsigned integer and undetermined
                { UINT_0, null, LOGICAL_AND, UINT_0, null },

                // Logical AND: the zero unsigned integer and the zero unsigned integer
                { UINT_0, UINT_0, LOGICAL_AND, UINT_0, null },

                // Logical AND: the zero unsigned integer and unsigned integer
                { UINT_0, UINT_42, LOGICAL_AND, UINT_0, null },

                // Logical AND: the zero unsigned integer and the zero signed integer
                { UINT_0, SINT_0, LOGICAL_AND, UINT_0, null },

                // Logical AND: the zero unsigned integer and signed integer
                { UINT_0, SINT_42, LOGICAL_AND, UINT_0, null },

                // Logical AND: the zero unsigned integer and the zero float
                { UINT_0, FLOAT_0, LOGICAL_AND, UINT_0, null },

                // Logical AND: the zero unsigned integer and float
                { UINT_0, FLOAT_3_25, LOGICAL_AND, UINT_0, null },

                // Logical AND: the zero unsigned integer and empty string
                { UINT_0, STRING_EMPTY, LOGICAL_AND, UINT_0, null },

                // Logical AND: the zero unsigned integer and string
                { UINT_0, STRING_A, LOGICAL_AND, UINT_0, null },

                // Logical AND: the zero unsigned integer and function
                { UINT_0, UNDETERMINED_FUNCTION_A_VALUE, LOGICAL_AND, UINT_0, null },

                // Logical AND: unsigned integer and undetermined
                { UINT_42, null, LOGICAL_AND, null, null },

                // Logical AND: unsigned integer and the zero unsigned integer
                { UINT_42, UINT_0, LOGICAL_AND, UINT_0, null },

                // Logical AND: unsigned integer and unsigned integer
                { UINT_42, UINT_42, LOGICAL_AND, UINT_42, null },

                // Logical AND: unsigned integer and the zero signed integer
                { UINT_42, SINT_0, LOGICAL_AND, SINT_0, null },

                // Logical AND: unsigned integer and signed integer
                { UINT_42, SINT_42, LOGICAL_AND, SINT_42, null },

                // Logical AND: unsigned integer and the zero float
                { UINT_42, FLOAT_0, LOGICAL_AND, FLOAT_0, null },

                // Logical AND: unsigned integer and float
                { UINT_42, FLOAT_3_25, LOGICAL_AND, FLOAT_3_25, null },

                // Logical AND: unsigned integer and empty string
                { UINT_42, STRING_EMPTY, LOGICAL_AND, STRING_EMPTY, null },

                // Logical AND: unsigned integer and string
                { UINT_42, STRING_A, LOGICAL_AND, STRING_A, null },

                // Logical AND: unsigned integer and function
                { UINT_42, UNDETERMINED_FUNCTION_A_VALUE, LOGICAL_AND, UNDETERMINED_FUNCTION_A_VALUE, null },

                // Logical AND: the zero signed integer and undetermined
                { SINT_0, null, LOGICAL_AND, SINT_0, null },

                // Logical AND: the zero signed integer and the zero unsigned integer
                { SINT_0, UINT_0, LOGICAL_AND, SINT_0, null },

                // Logical AND: the zero signed integer and unsigned integer
                { SINT_0, UINT_42, LOGICAL_AND, SINT_0, null },

                // Logical AND: the zero signed integer and the zero signed integer
                { SINT_0, SINT_0, LOGICAL_AND, SINT_0, null },

                // Logical AND: the zero signed integer and signed integer
                { SINT_0, SINT_42, LOGICAL_AND, SINT_0, null },

                // Logical AND: the zero signed integer and the zero float
                { SINT_0, FLOAT_0, LOGICAL_AND, SINT_0, null },

                // Logical AND: the zero signed integer and float
                { SINT_0, FLOAT_3_25, LOGICAL_AND, SINT_0, null },

                // Logical AND: the zero signed integer and empty string
                { SINT_0, STRING_EMPTY, LOGICAL_AND, SINT_0, null },

                // Logical AND: the zero signed integer and string
                { SINT_0, STRING_A, LOGICAL_AND, SINT_0, null },

                // Logical AND: the zero signed integer and function
                { SINT_0, UNDETERMINED_FUNCTION_A_VALUE, LOGICAL_AND, SINT_0, null },

                // Logical AND: signed integer and undetermined
                { SINT_42, null, LOGICAL_AND, null, null },

                // Logical AND: signed integer and the zero unsigned integer
                { SINT_42, UINT_0, LOGICAL_AND, UINT_0, null },

                // Logical AND: signed integer and unsigned integer
                { SINT_42, UINT_42, LOGICAL_AND, UINT_42, null },

                // Logical AND: signed integer and the zero signed integer
                { SINT_42, SINT_0, LOGICAL_AND, SINT_0, null },

                // Logical AND: signed integer and signed integer
                { SINT_42, SINT_42, LOGICAL_AND, SINT_42, null },

                // Logical AND: signed integer and the zero float
                { SINT_42, FLOAT_0, LOGICAL_AND, FLOAT_0, null },

                // Logical AND: signed integer and float
                { SINT_42, FLOAT_3_25, LOGICAL_AND, FLOAT_3_25, null },

                // Logical AND: signed integer and empty string
                { SINT_42, STRING_EMPTY, LOGICAL_AND, STRING_EMPTY, null },

                // Logical AND: signed integer and string
                { SINT_42, STRING_A, LOGICAL_AND, STRING_A, null },

                // Logical AND: signed integer and function
                { SINT_42, UNDETERMINED_FUNCTION_A_VALUE, LOGICAL_AND, UNDETERMINED_FUNCTION_A_VALUE, null },

                // Logical AND: the zero float and undetermined
                { FLOAT_0, null, LOGICAL_AND, FLOAT_0, null },

                // Logical AND: the zero float and the zero unsigned integer
                { FLOAT_0, UINT_0, LOGICAL_AND, FLOAT_0, null },

                // Logical AND: the zero float and unsigned integer
                { FLOAT_0, UINT_42, LOGICAL_AND, FLOAT_0, null },

                // Logical AND: the zero float and the zero signed integer
                { FLOAT_0, SINT_0, LOGICAL_AND, FLOAT_0, null },

                // Logical AND: the zero float and signed integer
                { FLOAT_0, SINT_42, LOGICAL_AND, FLOAT_0, null },

                // Logical AND: the zero float and the zero float
                { FLOAT_0, FLOAT_0, LOGICAL_AND, FLOAT_0, null },

                // Logical AND: the zero float and float
                { FLOAT_0, FLOAT_3_25, LOGICAL_AND, FLOAT_0, null },

                // Logical AND: the zero float and empty string
                { FLOAT_0, STRING_EMPTY, LOGICAL_AND, FLOAT_0, null },

                // Logical AND: the zero float and string
                { FLOAT_0, STRING_A, LOGICAL_AND, FLOAT_0, null },

                // Logical AND: the zero float and function
                { FLOAT_0, UNDETERMINED_FUNCTION_A_VALUE, LOGICAL_AND, FLOAT_0, null },

                // Logical AND: float and undetermined
                { FLOAT_3_25, null, LOGICAL_AND, null, null },

                // Logical AND: float and the zero unsigned integer
                { FLOAT_3_25, UINT_0, LOGICAL_AND, UINT_0, null },

                // Logical AND: float and unsigned integer
                { FLOAT_3_25, UINT_42, LOGICAL_AND, UINT_42, null },

                // Logical AND: float and the zero signed integer
                { FLOAT_3_25, SINT_0, LOGICAL_AND, SINT_0, null },

                // Logical AND: float and signed integer
                { FLOAT_3_25, SINT_42, LOGICAL_AND, SINT_42, null },

                // Logical AND: float and the zero float
                { FLOAT_3_25, FLOAT_0, LOGICAL_AND, FLOAT_0, null },

                // Logical AND: float and float
                { FLOAT_3_25, FLOAT_3_25, LOGICAL_AND, FLOAT_3_25, null },

                // Logical AND: float and empty string
                { FLOAT_3_25, STRING_EMPTY, LOGICAL_AND, STRING_EMPTY, null },

                // Logical AND: float and string
                { FLOAT_3_25, STRING_A, LOGICAL_AND, STRING_A, null },

                // Logical AND: float and function
                { FLOAT_3_25, UNDETERMINED_FUNCTION_A_VALUE, LOGICAL_AND, UNDETERMINED_FUNCTION_A_VALUE, null },

                // Logical AND: empty string and undetermined
                { STRING_EMPTY, null, LOGICAL_AND, STRING_EMPTY, null },

                // Logical AND: empty string and the zero unsigned integer
                { STRING_EMPTY, UINT_0, LOGICAL_AND, STRING_EMPTY, null },

                // Logical AND: empty string and unsigned integer
                { STRING_EMPTY, UINT_42, LOGICAL_AND, STRING_EMPTY, null },

                // Logical AND: empty string and the zero signed integer
                { STRING_EMPTY, SINT_0, LOGICAL_AND, STRING_EMPTY, null },

                // Logical AND: empty string and signed integer
                { STRING_EMPTY, SINT_42, LOGICAL_AND, STRING_EMPTY, null },

                // Logical AND: empty string and the zero float
                { STRING_EMPTY, FLOAT_0, LOGICAL_AND, STRING_EMPTY, null },

                // Logical AND: empty string and float
                { STRING_EMPTY, FLOAT_3_25, LOGICAL_AND, STRING_EMPTY, null },

                // Logical AND: empty string and empty string
                { STRING_EMPTY, STRING_EMPTY, LOGICAL_AND, STRING_EMPTY, null },

                // Logical AND: empty string and string
                { STRING_EMPTY, STRING_A, LOGICAL_AND, STRING_EMPTY, null },

                // Logical AND: empty string and function
                { STRING_EMPTY, UNDETERMINED_FUNCTION_A_VALUE, LOGICAL_AND, STRING_EMPTY, null },

                // Logical AND: string and undetermined
                { STRING_A, null, LOGICAL_AND, null, null },

                // Logical AND: string and the zero unsigned integer
                { STRING_A, UINT_0, LOGICAL_AND, UINT_0, null },

                // Logical AND: string and unsigned integer
                { STRING_A, UINT_42, LOGICAL_AND, UINT_42, null },

                // Logical AND: string and the zero signed integer
                { STRING_A, SINT_0, LOGICAL_AND, SINT_0, null },

                // Logical AND: string and signed integer
                { STRING_A, SINT_42, LOGICAL_AND, SINT_42, null },

                // Logical AND: string and the zero float
                { STRING_A, FLOAT_0, LOGICAL_AND, FLOAT_0, null },

                // Logical AND: string and float
                { STRING_A, FLOAT_3_25, LOGICAL_AND, FLOAT_3_25, null },

                // Logical AND: string and empty string
                { STRING_A, STRING_EMPTY, LOGICAL_AND, STRING_EMPTY, null },

                // Logical AND: string and string
                { STRING_A, STRING_A, LOGICAL_AND, STRING_A, null },

                // Logical AND: string and function
                { STRING_A, UNDETERMINED_FUNCTION_A_VALUE, LOGICAL_AND, UNDETERMINED_FUNCTION_A_VALUE, null },

                // Logical AND: function and undetermined
                { UNDETERMINED_FUNCTION_A_VALUE, null, LOGICAL_AND, null, null },

                // Logical AND: function and the zero unsigned integer
                { UNDETERMINED_FUNCTION_A_VALUE, UINT_0, LOGICAL_AND, UINT_0, null },

                // Logical AND: function and unsigned integer
                { UNDETERMINED_FUNCTION_A_VALUE, UINT_42, LOGICAL_AND, UINT_42, null },

                // Logical AND: function and the zero signed integer
                { UNDETERMINED_FUNCTION_A_VALUE, SINT_0, LOGICAL_AND, SINT_0, null },

                // Logical AND: function and signed integer
                { UNDETERMINED_FUNCTION_A_VALUE, SINT_42, LOGICAL_AND, SINT_42, null },

                // Logical AND: function and the zero float
                { UNDETERMINED_FUNCTION_A_VALUE, FLOAT_0, LOGICAL_AND, FLOAT_0, null },

                // Logical AND: function and float
                { UNDETERMINED_FUNCTION_A_VALUE, FLOAT_3_25, LOGICAL_AND, FLOAT_3_25, null },

                // Logical AND: function and empty string
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_EMPTY, LOGICAL_AND, STRING_EMPTY, null },

                // Logical AND: function and string
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_A, LOGICAL_AND, STRING_A, null },

                // Logical AND: function and function
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_A_VALUE, LOGICAL_AND, UNDETERMINED_FUNCTION_A_VALUE, null },

                // Logical OR: undetermined and undetermined
                { null, null, LOGICAL_OR, null, null },

                // Logical OR: undetermined and the zero unsigned integer
                { null, UINT_0, LOGICAL_OR, null, null },

                // Logical OR: undetermined and unsigned integer
                { null, UINT_42, LOGICAL_OR, null, null },

                // Logical OR: undetermined and the zero signed integer
                { null, SINT_0, LOGICAL_OR, null, null },

                // Logical OR: undetermined and signed integer
                { null, SINT_42, LOGICAL_OR, null, null },

                // Logical OR: undetermined and the zero float
                { null, FLOAT_0, LOGICAL_OR, null, null },

                // Logical OR: undetermined and float
                { null, FLOAT_3_25, LOGICAL_OR, null, null },

                // Logical OR: undetermined and empty string
                { null, STRING_EMPTY, LOGICAL_OR, null, null },

                // Logical OR: undetermined and string
                { null, STRING_A, LOGICAL_OR, null, null },

                // Logical OR: undetermined and function
                { null, UNDETERMINED_FUNCTION_A_VALUE, LOGICAL_OR, null, null },

                // Logical OR: the zero unsigned integer and undetermined
                { UINT_0, null, LOGICAL_OR, null, null },

                // Logical OR: the zero unsigned integer and the zero unsigned integer
                { UINT_0, UINT_0, LOGICAL_OR, UINT_0, null },

                // Logical OR: the zero unsigned integer and unsigned integer
                { UINT_0, UINT_42, LOGICAL_OR, UINT_42, null },

                // Logical OR: the zero unsigned integer and the zero signed integer
                { UINT_0, SINT_0, LOGICAL_OR, SINT_0, null },

                // Logical OR: the zero unsigned integer and signed integer
                { UINT_0, SINT_42, LOGICAL_OR, SINT_42, null },

                // Logical OR: the zero unsigned integer and the zero float
                { UINT_0, FLOAT_0, LOGICAL_OR, FLOAT_0, null },

                // Logical OR: the zero unsigned integer and float
                { UINT_0, FLOAT_3_25, LOGICAL_OR, FLOAT_3_25, null },

                // Logical OR: the zero unsigned integer and empty string
                { UINT_0, STRING_EMPTY, LOGICAL_OR, STRING_EMPTY, null },

                // Logical OR: the zero unsigned integer and string
                { UINT_0, STRING_A, LOGICAL_OR, STRING_A, null },

                // Logical OR: the zero unsigned integer and function
                { UINT_0, UNDETERMINED_FUNCTION_A_VALUE, LOGICAL_OR, UNDETERMINED_FUNCTION_A_VALUE, null },

                // Logical OR: unsigned integer and undetermined
                { UINT_42, null, LOGICAL_OR, UINT_42, null },

                // Logical OR: unsigned integer and the zero unsigned integer
                { UINT_42, UINT_0, LOGICAL_OR, UINT_42, null },

                // Logical OR: unsigned integer and unsigned integer
                { UINT_42, UINT_42, LOGICAL_OR, UINT_42, null },

                // Logical OR: unsigned integer and the zero signed integer
                { UINT_42, SINT_0, LOGICAL_OR, UINT_42, null },

                // Logical OR: unsigned integer and signed integer
                { UINT_42, SINT_42, LOGICAL_OR, UINT_42, null },

                // Logical OR: unsigned integer and the zero float
                { UINT_42, FLOAT_0, LOGICAL_OR, UINT_42, null },

                // Logical OR: unsigned integer and float
                { UINT_42, FLOAT_3_25, LOGICAL_OR, UINT_42, null },

                // Logical OR: unsigned integer and empty string
                { UINT_42, STRING_EMPTY, LOGICAL_OR, UINT_42, null },

                // Logical OR: unsigned integer and string
                { UINT_42, STRING_A, LOGICAL_OR, UINT_42, null },

                // Logical OR: unsigned integer and function
                { UINT_42, UNDETERMINED_FUNCTION_A_VALUE, LOGICAL_OR, UINT_42, null },

                // Logical OR: the zero signed integer and undetermined
                { SINT_0, null, LOGICAL_OR, null, null },

                // Logical OR: the zero signed integer and the zero unsigned integer
                { SINT_0, UINT_0, LOGICAL_OR, UINT_0, null },

                // Logical OR: the zero signed integer and unsigned integer
                { SINT_0, UINT_42, LOGICAL_OR, UINT_42, null },

                // Logical OR: the zero signed integer and the zero signed integer
                { SINT_0, SINT_0, LOGICAL_OR, SINT_0, null },

                // Logical OR: the zero signed integer and signed integer
                { SINT_0, SINT_42, LOGICAL_OR, SINT_42, null },

                // Logical OR: the zero signed integer and the zero float
                { SINT_0, FLOAT_0, LOGICAL_OR, FLOAT_0, null },

                // Logical OR: the zero signed integer and float
                { SINT_0, FLOAT_3_25, LOGICAL_OR, FLOAT_3_25, null },

                // Logical OR: the zero signed integer and empty string
                { SINT_0, STRING_EMPTY, LOGICAL_OR, STRING_EMPTY, null },

                // Logical OR: the zero signed integer and string
                { SINT_0, STRING_A, LOGICAL_OR, STRING_A, null },

                // Logical OR: the zero signed integer and function
                { SINT_0, UNDETERMINED_FUNCTION_A_VALUE, LOGICAL_OR, UNDETERMINED_FUNCTION_A_VALUE, null },

                // Logical OR: signed integer and undetermined
                { SINT_42, null, LOGICAL_OR, SINT_42, null },

                // Logical OR: signed integer and the zero unsigned integer
                { SINT_42, UINT_0, LOGICAL_OR, SINT_42, null },

                // Logical OR: signed integer and unsigned integer
                { SINT_42, UINT_42, LOGICAL_OR, SINT_42, null },

                // Logical OR: signed integer and the zero signed integer
                { SINT_42, SINT_0, LOGICAL_OR, SINT_42, null },

                // Logical OR: signed integer and signed integer
                { SINT_42, SINT_42, LOGICAL_OR, SINT_42, null },

                // Logical OR: signed integer and the zero float
                { SINT_42, FLOAT_0, LOGICAL_OR, SINT_42, null },

                // Logical OR: signed integer and float
                { SINT_42, FLOAT_3_25, LOGICAL_OR, SINT_42, null },

                // Logical OR: signed integer and empty string
                { SINT_42, STRING_EMPTY, LOGICAL_OR, SINT_42, null },

                // Logical OR: signed integer and string
                { SINT_42, STRING_A, LOGICAL_OR, SINT_42, null },

                // Logical OR: signed integer and function
                { SINT_42, UNDETERMINED_FUNCTION_A_VALUE, LOGICAL_OR, SINT_42, null },

                // Logical OR: the zero float and undetermined
                { FLOAT_0, null, LOGICAL_OR, null, null },

                // Logical OR: the zero float and the zero unsigned integer
                { FLOAT_0, UINT_0, LOGICAL_OR, UINT_0, null },

                // Logical OR: the zero float and unsigned integer
                { FLOAT_0, UINT_42, LOGICAL_OR, UINT_42, null },

                // Logical OR: the zero float and the zero signed integer
                { FLOAT_0, SINT_0, LOGICAL_OR, SINT_0, null },

                // Logical OR: the zero float and signed integer
                { FLOAT_0, SINT_42, LOGICAL_OR, SINT_42, null },

                // Logical OR: the zero float and the zero float
                { FLOAT_0, FLOAT_0, LOGICAL_OR, FLOAT_0, null },

                // Logical OR: the zero float and float
                { FLOAT_0, FLOAT_3_25, LOGICAL_OR, FLOAT_3_25, null },

                // Logical OR: the zero float and empty string
                { FLOAT_0, STRING_EMPTY, LOGICAL_OR, STRING_EMPTY, null },

                // Logical OR: the zero float and string
                { FLOAT_0, STRING_A, LOGICAL_OR, STRING_A, null },

                // Logical OR: the zero float and function
                { FLOAT_0, UNDETERMINED_FUNCTION_A_VALUE, LOGICAL_OR, UNDETERMINED_FUNCTION_A_VALUE, null },

                // Logical OR: float and undetermined
                { FLOAT_3_25, null, LOGICAL_OR, FLOAT_3_25, null },

                // Logical OR: float and the zero unsigned integer
                { FLOAT_3_25, UINT_0, LOGICAL_OR, FLOAT_3_25, null },

                // Logical OR: float and unsigned integer
                { FLOAT_3_25, UINT_42, LOGICAL_OR, FLOAT_3_25, null },

                // Logical OR: float and the zero signed integer
                { FLOAT_3_25, SINT_0, LOGICAL_OR, FLOAT_3_25, null },

                // Logical OR: float and signed integer
                { FLOAT_3_25, SINT_42, LOGICAL_OR, FLOAT_3_25, null },

                // Logical OR: float and the zero float
                { FLOAT_3_25, FLOAT_0, LOGICAL_OR, FLOAT_3_25, null },

                // Logical OR: float and float
                { FLOAT_3_25, FLOAT_3_25, LOGICAL_OR, FLOAT_3_25, null },

                // Logical OR: float and empty string
                { FLOAT_3_25, STRING_EMPTY, LOGICAL_OR, FLOAT_3_25, null },

                // Logical OR: float and string
                { FLOAT_3_25, STRING_A, LOGICAL_OR, FLOAT_3_25, null },

                // Logical OR: float and function
                { FLOAT_3_25, UNDETERMINED_FUNCTION_A_VALUE, LOGICAL_OR, FLOAT_3_25, null },

                // Logical OR: empty string and undetermined
                { STRING_EMPTY, null, LOGICAL_OR, null, null },

                // Logical OR: empty string and the zero unsigned integer
                { STRING_EMPTY, UINT_0, LOGICAL_OR, UINT_0, null },

                // Logical OR: empty string and unsigned integer
                { STRING_EMPTY, UINT_42, LOGICAL_OR, UINT_42, null },

                // Logical OR: empty string and the zero signed integer
                { STRING_EMPTY, SINT_0, LOGICAL_OR, SINT_0, null },

                // Logical OR: empty string and signed integer
                { STRING_EMPTY, SINT_42, LOGICAL_OR, SINT_42, null },

                // Logical OR: empty string and the zero float
                { STRING_EMPTY, FLOAT_0, LOGICAL_OR, FLOAT_0, null },

                // Logical OR: empty string and float
                { STRING_EMPTY, FLOAT_3_25, LOGICAL_OR, FLOAT_3_25, null },

                // Logical OR: empty string and empty string
                { STRING_EMPTY, STRING_EMPTY, LOGICAL_OR, STRING_EMPTY, null },

                // Logical OR: empty string and string
                { STRING_EMPTY, STRING_A, LOGICAL_OR, STRING_A, null },

                // Logical OR: empty string and function
                { STRING_EMPTY, UNDETERMINED_FUNCTION_A_VALUE, LOGICAL_OR, UNDETERMINED_FUNCTION_A_VALUE, null },

                // Logical OR: string and undetermined
                { STRING_A, null, LOGICAL_OR, STRING_A, null },

                // Logical OR: string and the zero unsigned integer
                { STRING_A, UINT_0, LOGICAL_OR, STRING_A, null },

                // Logical OR: string and unsigned integer
                { STRING_A, UINT_42, LOGICAL_OR, STRING_A, null },

                // Logical OR: string and the zero signed integer
                { STRING_A, SINT_0, LOGICAL_OR, STRING_A, null },

                // Logical OR: string and signed integer
                { STRING_A, SINT_42, LOGICAL_OR, STRING_A, null },

                // Logical OR: string and the zero float
                { STRING_A, FLOAT_0, LOGICAL_OR, STRING_A, null },

                // Logical OR: string and float
                { STRING_A, FLOAT_3_25, LOGICAL_OR, STRING_A, null },

                // Logical OR: string and empty string
                { STRING_A, STRING_EMPTY, LOGICAL_OR, STRING_A, null },

                // Logical OR: string and string
                { STRING_A, STRING_A, LOGICAL_OR, STRING_A, null },

                // Logical OR: string and function
                { STRING_A, UNDETERMINED_FUNCTION_A_VALUE, LOGICAL_OR, STRING_A, null },

                // Logical OR: function and undetermined
                { UNDETERMINED_FUNCTION_A_VALUE, null, LOGICAL_OR, UNDETERMINED_FUNCTION_A_VALUE, null },

                // Logical OR: function and the zero unsigned integer
                { UNDETERMINED_FUNCTION_A_VALUE, UINT_0, LOGICAL_OR, UNDETERMINED_FUNCTION_A_VALUE, null },

                // Logical OR: function and unsigned integer
                { UNDETERMINED_FUNCTION_A_VALUE, UINT_42, LOGICAL_OR, UNDETERMINED_FUNCTION_A_VALUE, null },

                // Logical OR: function and the zero signed integer
                { UNDETERMINED_FUNCTION_A_VALUE, SINT_0, LOGICAL_OR, UNDETERMINED_FUNCTION_A_VALUE, null },

                // Logical OR: function and signed integer
                { UNDETERMINED_FUNCTION_A_VALUE, SINT_42, LOGICAL_OR, UNDETERMINED_FUNCTION_A_VALUE, null },

                // Logical OR: function and the zero float
                { UNDETERMINED_FUNCTION_A_VALUE, FLOAT_0, LOGICAL_OR, UNDETERMINED_FUNCTION_A_VALUE, null },

                // Logical OR: function and float
                { UNDETERMINED_FUNCTION_A_VALUE, FLOAT_3_25, LOGICAL_OR, UNDETERMINED_FUNCTION_A_VALUE, null },

                // Logical OR: function and empty string
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_EMPTY, LOGICAL_OR, UNDETERMINED_FUNCTION_A_VALUE, null },

                // Logical OR: function and string
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_A, LOGICAL_OR, UNDETERMINED_FUNCTION_A_VALUE, null },

                // Logical OR: function and function
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_A_VALUE, LOGICAL_OR, UNDETERMINED_FUNCTION_A_VALUE, null },

        };

        private static final List<Object[]> TEST_DATA = Arrays.asList(TEST_DATA_VALUES);

        @Nonnull
        @Parameters(name = "{0} {2} {1}")
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        @Nonnull
        private final ExpressionEvaluation.BinaryLogical operator;

        public EvaluateBinaryLogicalTest(@CheckForNull Value operand1, @CheckForNull Value operand2,
                @Nonnull ExpressionEvaluation.BinaryLogical operator, @CheckForNull Value expectedResult,
                @CheckForNull AssemblyMessage expectedAssemblyMessage) {
            super(operand1, operand2, expectedResult, expectedAssemblyMessage);
            this.operator = operator;
        }

        @CheckForNull
        @Override
        protected Value run(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateBinaryLogical(operand1, operand2, this.operator);
        }

    }

    /**
     * Parameterized test class for
     * {@link ExpressionEvaluation#evaluateBitShift(Value, Value, EvaluationContext, ExpressionEvaluation.BitShift)}.
     */
    @RunWith(Parameterized.class)
    public static class EvaluateBitShiftTest extends BaseEvaluateBinaryTest {

        private static final ExpressionEvaluation.BitShift BIT_SHIFT_LEFT = ExpressionEvaluation.BitShift.BIT_SHIFT_LEFT;
        private static final ExpressionEvaluation.BitShift BIT_SHIFT_RIGHT = ExpressionEvaluation.BitShift.BIT_SHIFT_RIGHT;

        private static final FunctionOperandNotApplicableErrorMessage FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_LEFT = new FunctionOperandNotApplicableErrorMessage(
                "bit shift left");
        private static final FunctionOperandNotApplicableErrorMessage FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_RIGHT = new FunctionOperandNotApplicableErrorMessage(
                "bit shift right");

        private static final Object[][] TEST_DATA_VALUES = new Object[][] {

                // Bit shift left: undetermined and undetermined
                { null, null, BIT_SHIFT_LEFT, null, null },

                // Bit shift left: undetermined and unsigned integer
                { null, UINT_10, BIT_SHIFT_LEFT, null, null },

                // Bit shift left: undetermined and signed integer
                { null, SINT_10, BIT_SHIFT_LEFT, null, null },

                // Bit shift left: undetermined and float
                { null, FLOAT_3_25, BIT_SHIFT_LEFT, null, null },

                // Bit shift left: undetermined and a string that can be parsed as a number
                { null, STRING_3_25, BIT_SHIFT_LEFT, null, null },

                // Bit shift left: undetermined and a string that cannot be parsed as a number
                { null, STRING_A, BIT_SHIFT_LEFT, null, null },

                // Bit shift left: undetermined and function
                { null, UNDETERMINED_FUNCTION_A_VALUE, BIT_SHIFT_LEFT, null, null },

                // Bit shift left: unsigned integer and undetermined
                { UINT_42, null, BIT_SHIFT_LEFT, null, null },

                // Bit shift left: unsigned integer and unsigned integer
                { UINT_42, UINT_10, BIT_SHIFT_LEFT, UINT_42_SHL_10, null },

                // Bit shift left: unsigned integer and signed integer
                { UINT_42, SINT_10, BIT_SHIFT_LEFT, UINT_42_SHL_10, null },

                // Bit shift left: unsigned integer and float
                { UINT_42, FLOAT_3_25, BIT_SHIFT_LEFT, UINT_42_SHL_3, null },

                // Bit shift left: unsigned integer and a string that can be parsed as a number
                { UINT_42, STRING_3_25, BIT_SHIFT_LEFT, UINT_42_SHL_3, null },

                // Bit shift left: unsigned integer and a string that cannot be parsed as a number
                { UINT_42, STRING_A, BIT_SHIFT_LEFT, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bit shift left: unsigned integer and function
                { UINT_42, UNDETERMINED_FUNCTION_A_VALUE, BIT_SHIFT_LEFT, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_LEFT },

                // Bit shift left: signed integer and undetermined
                { SINT_42, null, BIT_SHIFT_LEFT, null, null },

                // Bit shift left: signed integer and unsigned integer
                { SINT_42, UINT_10, BIT_SHIFT_LEFT, SINT_42_SHL_10, null },

                // Bit shift left: signed integer and signed integer
                { SINT_42, SINT_10, BIT_SHIFT_LEFT, SINT_42_SHL_10, null },

                // Bit shift left: signed integer and negative signed integer
                { SINT_42, SINT_MINUS_1, BIT_SHIFT_LEFT, SINT_42_SHL_MINUS_1, null },

                // Bit shift left: signed integer and float
                { SINT_42, FLOAT_3_25, BIT_SHIFT_LEFT, SINT_42_SHL_3, null },

                // Bit shift left: signed integer and a string that can be parsed as a number
                { SINT_42, STRING_3_25, BIT_SHIFT_LEFT, SINT_42_SHL_3, null },

                // Bit shift left: signed integer and a string that cannot be parsed as a number
                { SINT_42, STRING_A, BIT_SHIFT_LEFT, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bit shift left: signed integer and function
                { SINT_42, UNDETERMINED_FUNCTION_A_VALUE, BIT_SHIFT_LEFT, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_LEFT },

                // Bit shift left: float and undetermined
                { FLOAT_3_25, null, BIT_SHIFT_LEFT, null, null },

                // Bit shift left: float and unsigned integer
                { FLOAT_3_25, UINT_10, BIT_SHIFT_LEFT, SINT_3_SHL_10, null },

                // Bit shift left: float and signed integer
                { FLOAT_3_25, SINT_10, BIT_SHIFT_LEFT, SINT_3_SHL_10, null },

                // Bit shift left: float and negative signed integer
                { FLOAT_3_25, SINT_MINUS_1, BIT_SHIFT_LEFT, SINT_3_SHL_MINUS_1, null },

                // Bit shift left: float and float
                { FLOAT_3_25, FLOAT_3_25, BIT_SHIFT_LEFT, SINT_3_SHL_3, null },

                // Bit shift left: float and a string that can be parsed as a number
                { FLOAT_3_25, STRING_3_25, BIT_SHIFT_LEFT, SINT_3_SHL_3, null },

                // Bit shift left: float and a string that cannot be parsed as a number
                { FLOAT_3_25, STRING_A, BIT_SHIFT_LEFT, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bit shift left: float and function
                { FLOAT_3_25, UNDETERMINED_FUNCTION_A_VALUE, BIT_SHIFT_LEFT, null,
                        FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_LEFT },

                // Bit shift left: a string that can be parsed as a number and undetermined
                { STRING_3_25, null, BIT_SHIFT_LEFT, null, null },

                // Bit shift left: a string that can be parsed as a number and unsigned integer
                { STRING_3_25, UINT_10, BIT_SHIFT_LEFT, SINT_3_SHL_10, null },

                // Bit shift left: a string that can be parsed as a number and signed integer
                { STRING_3_25, SINT_10, BIT_SHIFT_LEFT, SINT_3_SHL_10, null },

                // Bit shift left: a string that can be parsed as a number and negative signed integer
                { STRING_3_25, SINT_MINUS_1, BIT_SHIFT_LEFT, SINT_3_SHL_MINUS_1, null },

                // Bit shift left: a string that can be parsed as a number and float
                { STRING_3_25, FLOAT_3_25, BIT_SHIFT_LEFT, SINT_3_SHL_3, null },

                // Bit shift left: a string that can be parsed as a number and a string that can be parsed as a number
                { STRING_3_25, STRING_3_25, BIT_SHIFT_LEFT, SINT_3_SHL_3, null },

                // Bit shift left: a string that can be parsed as a number and a string that cannot be parsed as a number
                { STRING_3_25, STRING_A, BIT_SHIFT_LEFT, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bit shift left: a string that can be parsed as a number and function
                { STRING_3_25, UNDETERMINED_FUNCTION_A_VALUE, BIT_SHIFT_LEFT, null,
                        FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_LEFT },

                // Bit shift left: a string that cannot be parsed as a number and undetermined
                { STRING_A, null, BIT_SHIFT_LEFT, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bit shift left: a string that cannot be parsed as a number and unsigned integer
                { STRING_A, UINT_42, BIT_SHIFT_LEFT, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bit shift left: a string that cannot be parsed as a number and signed integer
                { STRING_A, SINT_42, BIT_SHIFT_LEFT, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bit shift left: a string that cannot be parsed as a number and float
                { STRING_A, FLOAT_3_25, BIT_SHIFT_LEFT, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bit shift left: a string that cannot be parsed as a number and a string that can be parsed as a number
                { STRING_A, STRING_3_25, BIT_SHIFT_LEFT, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bit shift left: a string that cannot be parsed as a number and a string that cannot be parsed as a number
                { STRING_A, STRING_A, BIT_SHIFT_LEFT, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bit shift left: a string that cannot be parsed as a number and function
                { STRING_A, UNDETERMINED_FUNCTION_A_VALUE, BIT_SHIFT_LEFT, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bit shift left: function and undetermined
                { UNDETERMINED_FUNCTION_A_VALUE, null, BIT_SHIFT_LEFT, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_LEFT },

                // Bit shift left: function and unsigned integer
                { UNDETERMINED_FUNCTION_A_VALUE, UINT_42, BIT_SHIFT_LEFT, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_LEFT },

                // Bit shift left: function and signed integer
                { UNDETERMINED_FUNCTION_A_VALUE, SINT_42, BIT_SHIFT_LEFT, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_LEFT },

                // Bit shift left: function and float
                { UNDETERMINED_FUNCTION_A_VALUE, FLOAT_3_25, BIT_SHIFT_LEFT, null,
                        FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_LEFT },

                // Bit shift left: function and a string that can be parsed as a number
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_3_25, BIT_SHIFT_LEFT, null,
                        FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_LEFT },

                // Bit shift left: function and a string that cannot be parsed as a number
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_A, BIT_SHIFT_LEFT, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_LEFT },

                // Bit shift left: function and function
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_A_VALUE, BIT_SHIFT_LEFT, null,
                        FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_LEFT },

                // Bit shift right: undetermined and undetermined
                { null, null, BIT_SHIFT_RIGHT, null, null },

                // Bit shift right: undetermined and unsigned integer
                { null, UINT_42, BIT_SHIFT_RIGHT, null, null },

                // Bit shift right: undetermined and signed integer
                { null, SINT_42, BIT_SHIFT_RIGHT, null, null },

                // Bit shift right: undetermined and float
                { null, FLOAT_3_25, BIT_SHIFT_RIGHT, null, null },

                // Bit shift right: undetermined and a string that can be parsed as a number
                { null, STRING_3_25, BIT_SHIFT_RIGHT, null, null },

                // Bit shift right: undetermined and a string that cannot be parsed as a number
                { null, STRING_A, BIT_SHIFT_RIGHT, null, null },

                // Bit shift right: undetermined and function
                { null, UNDETERMINED_FUNCTION_A_VALUE, BIT_SHIFT_RIGHT, null, null },

                // Bit shift right: unsigned integer and undetermined
                { UINT_42, null, BIT_SHIFT_RIGHT, null, null },

                // Bit shift right: unsigned integer and unsigned integer
                { UINT_42, UINT_4, BIT_SHIFT_RIGHT, UINT_2, null },

                // Bit shift right: unsigned integer and signed integer
                { UINT_42, SINT_4, BIT_SHIFT_RIGHT, UINT_2, null },

                // Bit shift right: unsigned integer and float
                { UINT_42, FLOAT_3_25, BIT_SHIFT_RIGHT, UINT_5, null },

                // Bit shift right: unsigned integer and a string that can be parsed as a number
                { UINT_42, STRING_3_25, BIT_SHIFT_RIGHT, UINT_5, null },

                // Bit shift right: unsigned integer and a string that cannot be parsed as a number
                { UINT_42, STRING_A, BIT_SHIFT_RIGHT, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bit shift right: unsigned integer and function
                { UINT_42, UNDETERMINED_FUNCTION_A_VALUE, BIT_SHIFT_RIGHT, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_RIGHT },

                // Bit shift right: signed integer and undetermined
                { SINT_42, null, BIT_SHIFT_RIGHT, null, null },

                // Bit shift right: signed integer and unsigned integer
                { SINT_42, UINT_4, BIT_SHIFT_RIGHT, SINT_2, null },

                // Bit shift right: negative signed integer and unsigned integer
                { SINT_MINUS_42, UINT_4, BIT_SHIFT_RIGHT, SINT_MINUS_3, null },

                // Bit shift right: signed integer and signed integer
                { SINT_42, SINT_4, BIT_SHIFT_RIGHT, SINT_2, null },

                // Bit shift right: negative signed integer and signed integer
                { SINT_MINUS_42, SINT_4, BIT_SHIFT_RIGHT, SINT_MINUS_3, null },

                // Bit shift right: signed integer and negative signed integer
                { SINT_42, SINT_MINUS_1, BIT_SHIFT_RIGHT, SINT_42_SHR_MINUS_1, null },

                // Bit shift right: signed integer and float
                { SINT_42, FLOAT_3_25, BIT_SHIFT_RIGHT, SINT_5, null },

                // Bit shift right: signed integer and a string that can be parsed as a number
                { SINT_42, STRING_3_25, BIT_SHIFT_RIGHT, SINT_5, null },

                // Bit shift right: signed integer and a string that cannot be parsed as a number
                { SINT_42, STRING_A, BIT_SHIFT_RIGHT, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bit shift right: signed integer and function
                { SINT_42, UNDETERMINED_FUNCTION_A_VALUE, BIT_SHIFT_RIGHT, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_RIGHT },

                // Bit shift right: float and undetermined
                { FLOAT_45_25, null, BIT_SHIFT_RIGHT, null, null },

                // Bit shift right: float and unsigned integer
                { FLOAT_45_25, UINT_4, BIT_SHIFT_RIGHT, SINT_2, null },

                // Bit shift right: float and signed integer
                { FLOAT_45_25, SINT_4, BIT_SHIFT_RIGHT, SINT_2, null },

                // Bit shift right: float and negative signed integer
                { FLOAT_45_25, SINT_MINUS_1, BIT_SHIFT_RIGHT, SINT_45_SHR_MINUS_1, null },

                // Bit shift right: float and float
                { FLOAT_45_25, FLOAT_3_25, BIT_SHIFT_RIGHT, SINT_5, null },

                // Bit shift right: float and a string that can be parsed as a number
                { FLOAT_45_25, STRING_3_25, BIT_SHIFT_RIGHT, SINT_5, null },

                // Bit shift right: float and a string that cannot be parsed as a number
                { FLOAT_45_25, STRING_A, BIT_SHIFT_RIGHT, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bit shift right: float and function
                { FLOAT_45_25, UNDETERMINED_FUNCTION_A_VALUE, BIT_SHIFT_RIGHT, null,
                        FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_RIGHT },

                // Bit shift right: a string that can be parsed as a number and undetermined
                { STRING_45_25, null, BIT_SHIFT_RIGHT, null, null },

                // Bit shift right: a string that can be parsed as a number and unsigned integer
                { STRING_45_25, UINT_4, BIT_SHIFT_RIGHT, SINT_2, null },

                // Bit shift right: a string that can be parsed as a number and signed integer
                { STRING_45_25, SINT_4, BIT_SHIFT_RIGHT, SINT_2, null },

                // Bit shift right: a string that can be parsed as a number and negative signed integer
                { STRING_45_25, SINT_MINUS_1, BIT_SHIFT_RIGHT, SINT_45_SHR_MINUS_1, null },

                // Bit shift right: a string that can be parsed as a number and float
                { STRING_45_25, FLOAT_3_25, BIT_SHIFT_RIGHT, SINT_5, null },

                // Bit shift right: a string that can be parsed as a number and a string that can be parsed as a number
                { STRING_45_25, STRING_3_25, BIT_SHIFT_RIGHT, SINT_5, null },

                // Bit shift right: a string that can be parsed as a number and a string that cannot be parsed as a number
                { STRING_45_25, STRING_A, BIT_SHIFT_RIGHT, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bit shift right: a string that can be parsed as a number and function
                { STRING_45_25, UNDETERMINED_FUNCTION_A_VALUE, BIT_SHIFT_RIGHT, null,
                        FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_RIGHT },

                // Bit shift right: a string that cannot be parsed as a number and undetermined
                { STRING_A, null, BIT_SHIFT_RIGHT, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bit shift right: a string that cannot be parsed as a number and unsigned integer
                { STRING_A, UINT_42, BIT_SHIFT_RIGHT, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bit shift right: a string that cannot be parsed as a number and signed integer
                { STRING_A, SINT_42, BIT_SHIFT_RIGHT, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bit shift right: a string that cannot be parsed as a number and float
                { STRING_A, FLOAT_3_25, BIT_SHIFT_RIGHT, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bit shift right: a string that cannot be parsed as a number and a string that can be parsed as a number
                { STRING_A, STRING_3_25, BIT_SHIFT_RIGHT, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bit shift right: a string that cannot be parsed as a number and a string that cannot be parsed as a number
                { STRING_A, STRING_A, BIT_SHIFT_RIGHT, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bit shift right: a string that cannot be parsed as a number and function
                { STRING_A, UNDETERMINED_FUNCTION_A_VALUE, BIT_SHIFT_RIGHT, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // Bit shift right: function and undetermined
                { UNDETERMINED_FUNCTION_A_VALUE, null, BIT_SHIFT_RIGHT, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_RIGHT },

                // Bit shift right: function and unsigned integer
                { UNDETERMINED_FUNCTION_A_VALUE, UINT_42, BIT_SHIFT_RIGHT, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_RIGHT },

                // Bit shift right: function and signed integer
                { UNDETERMINED_FUNCTION_A_VALUE, SINT_42, BIT_SHIFT_RIGHT, null, FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_RIGHT },

                // Bit shift right: function and float
                { UNDETERMINED_FUNCTION_A_VALUE, FLOAT_3_25, BIT_SHIFT_RIGHT, null,
                        FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_RIGHT },

                // Bit shift right: function and a string that can be parsed as a number
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_3_25, BIT_SHIFT_RIGHT, null,
                        FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_RIGHT },

                // Bit shift right: function and a string that cannot be parsed as a number
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_A, BIT_SHIFT_RIGHT, null,
                        FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_RIGHT },

                // Bit shift right: function and function
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_A_VALUE, BIT_SHIFT_RIGHT, null,
                        FUNCTION_OPERAND_NOT_APPLICABLE_TO_BIT_SHIFT_RIGHT },

        };

        private static final List<Object[]> TEST_DATA = Arrays.asList(TEST_DATA_VALUES);

        @Nonnull
        @Parameters(name = "{0} {2} {1}")
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        @Nonnull
        private final ExpressionEvaluation.BitShift operator;

        public EvaluateBitShiftTest(@CheckForNull Value operand1, @CheckForNull Value operand2,
                @Nonnull ExpressionEvaluation.BitShift operator, @CheckForNull Value expectedResult,
                @Nonnull AssemblyMessage expectedAssemblyMessage) {
            super(operand1, operand2, expectedResult, expectedAssemblyMessage);
            this.operator = operator;
        }

        @CheckForNull
        @Override
        protected Value run(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateBitShift(operand1, operand2, evaluationContext, this.operator);
        }

    }

    /**
     * Parameterized test class for {@link ExpressionEvaluation#evaluateBitwiseNot(Value, EvaluationContext)}.
     */
    @RunWith(Parameterized.class)
    public static class EvaluateBitwiseNotTest extends BaseEvaluateUnaryTest {

        private static final List<Object[]> TEST_DATA = Arrays.asList(new Object[][] {

                // An undetermined value
                { null, null, null },

                // An unsigned integer
                { UINT_42, UINT_NOT_42, null },

                // A signed integer
                { SINT_42, UINT_NOT_42, null },

                // A positive floating-point number
                { FLOAT_3_25, UINT_NOT_3, null },

                // A negative floating-point number
                { FLOAT_MINUS_3_25, UINT_2, null },

                // A string that can be parsed as a number
                { STRING_3_25, UINT_NOT_3, null },

                // A string that cannot be parsed as a number
                { STRING_A, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // A function
                { UNDETERMINED_FUNCTION_A_VALUE, null, new FunctionOperandNotApplicableErrorMessage("bitwise NOT") },

        });

        @Nonnull
        @Parameters
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        public EvaluateBitwiseNotTest(@CheckForNull Value input, @CheckForNull Value expectedResult,
                @CheckForNull AssemblyMessage expectedAssemblyMessage) {
            super(input, expectedResult, expectedAssemblyMessage);
        }

        @CheckForNull
        @Override
        protected Value run(@CheckForNull Value operand, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateBitwiseNot(operand, evaluationContext);
        }

    }

    /**
     * Parameterized test class for
     * {@link ExpressionEvaluation#evaluateComparison(Value, Value, EvaluationContext, ExpressionEvaluation.Comparison)}.
     */
    @RunWith(Parameterized.class)
    public static class EvaluateComparisonTest extends BaseEvaluateBinaryTest {

        private static final ExpressionEvaluation.Comparison LESS_THAN = ExpressionEvaluation.Comparison.LESS_THAN;
        private static final ExpressionEvaluation.Comparison LESS_THAN_OR_EQUAL_TO = ExpressionEvaluation.Comparison.LESS_THAN_OR_EQUAL_TO;
        private static final ExpressionEvaluation.Comparison GREATER_THAN = ExpressionEvaluation.Comparison.GREATER_THAN;
        private static final ExpressionEvaluation.Comparison GREATER_THAN_OR_EQUAL_TO = ExpressionEvaluation.Comparison.GREATER_THAN_OR_EQUAL_TO;
        private static final ExpressionEvaluation.Comparison EQUAL_TO = ExpressionEvaluation.Comparison.EQUAL_TO;
        private static final ExpressionEvaluation.Comparison DIFFERENT_FROM = ExpressionEvaluation.Comparison.DIFFERENT_FROM;

        private static final Object[][] TEST_DATA_VALUES = new Object[][] {

                // Less than: undetermined and undetermined
                { null, null, LESS_THAN, null, null },

                // Less than: undetermined and unsigned integer
                { null, UINT_42, LESS_THAN, null, null },

                // Less than: undetermined and signed integer
                { null, SINT_42, LESS_THAN, null, null },

                // Less than: undetermined and float
                { null, FLOAT_3_25, LESS_THAN, null, null },

                // Less than: undetermined and string
                { null, STRING_A, LESS_THAN, null, null },

                // Less than: undetermined and function
                { null, UNDETERMINED_FUNCTION_A_VALUE, LESS_THAN, null, null },

                // Less than: unsigned integer and undetermined
                { UINT_42, null, LESS_THAN, null, null },

                // Less than: unsigned integer and unsigned integer (<)
                { UINT_4, UINT_42, LESS_THAN, UINT_1, null },

                // Less than: unsigned integer and unsigned integer (===)
                { UINT_42, UINT_42, LESS_THAN, UINT_0, null },

                // Less than: unsigned integer and unsigned integer (>)
                { UINT_42, UINT_4, LESS_THAN, UINT_0, null },

                // Less than: unsigned integer and signed integer (<)
                { UINT_4, SINT_42, LESS_THAN, UINT_1, null },

                // Less than: unsigned integer and signed integer (==)
                { UINT_42, SINT_42, LESS_THAN, UINT_0, null },

                // Less than: unsigned integer and signed integer (>)
                { UINT_42, SINT_4, LESS_THAN, UINT_0, null },

                // Less than: huge unsigned integer and signed integer (>)
                { UINT_18000000000000000000, SINT_42, LESS_THAN, UINT_0, null },

                // Less than: unsigned integer and negative signed integer (>)
                { UINT_42, SINT_MINUS_1, LESS_THAN, UINT_0, null },

                // Less than: unsigned integer and float (<)
                { UINT_4, FLOAT_42, LESS_THAN, UINT_1, null },

                // Less than: unsigned integer and float (==)
                { UINT_42, FLOAT_42, LESS_THAN, UINT_0, null },

                // Less than: unsigned integer and float (>)
                { UINT_42, FLOAT_3_25, LESS_THAN, UINT_0, null },

                // Less than: unsigned integer and string (<)
                { UINT_42, STRING_A, LESS_THAN, UINT_1, null },

                // Less than: unsigned integer and string (==)
                { UINT_42, STRING_42, LESS_THAN, UINT_0, null },

                // Less than: unsigned integer and string (>)
                { UINT_42, STRING_ASTERISK, LESS_THAN, UINT_0, null },

                // Less than: unsigned integer and function (always <)
                { UINT_42, UNDETERMINED_FUNCTION_A_VALUE, LESS_THAN, UINT_1, null },

                // Less than: signed integer and undetermined
                { SINT_42, null, LESS_THAN, null, null },

                // Less than: signed integer and unsigned integer (<)
                { SINT_4, UINT_42, LESS_THAN, UINT_1, null },

                // Less than: negative signed integer and unsigned integer (<)
                { SINT_MINUS_1, UINT_42, LESS_THAN, UINT_1, null },

                // Less than: signed integer and huge unsigned integer (<)
                { SINT_4, UINT_18000000000000000000, LESS_THAN, UINT_1, null },

                // Less than: signed integer and unsigned integer (==)
                { SINT_42, UINT_42, LESS_THAN, UINT_0, null },

                // Less than: signed integer and unsigned integer (>)
                { SINT_42, UINT_4, LESS_THAN, UINT_0, null },

                // Less than: signed integer and signed integer (<)
                { SINT_4, SINT_42, LESS_THAN, UINT_1, null },

                // Less than: signed integer and signed integer (===)
                { SINT_42, SINT_42, LESS_THAN, UINT_0, null },

                // Less than: signed integer and signed integer (>)
                { SINT_42, SINT_4, LESS_THAN, UINT_0, null },

                // Less than: signed integer and float (<)
                { SINT_4, FLOAT_42, LESS_THAN, UINT_1, null },

                // Less than: signed integer and float (==)
                { SINT_42, FLOAT_42, LESS_THAN, UINT_0, null },

                // Less than: signed integer and float (>)
                { SINT_42, FLOAT_3_25, LESS_THAN, UINT_0, null },

                // Less than: signed integer and string (<)
                { SINT_42, STRING_A, LESS_THAN, UINT_1, null },

                // Less than: signed integer and string (==)
                { SINT_42, STRING_42, LESS_THAN, UINT_0, null },

                // Less than: signed integer and string (>)
                { SINT_42, STRING_ASTERISK, LESS_THAN, UINT_0, null },

                // Less than: signed integer and function (always <)
                { SINT_42, UNDETERMINED_FUNCTION_A_VALUE, LESS_THAN, UINT_1, null },

                // Less than: float and undetermined
                { FLOAT_3_25, null, LESS_THAN, null, null },

                // Less than: float and unsigned integer (<)
                { FLOAT_3_25, UINT_42, LESS_THAN, UINT_1, null },

                // Less than: float and unsigned integer (==)
                { FLOAT_42, UINT_42, LESS_THAN, UINT_0, null },

                // Less than: float and unsigned integer (>)
                { FLOAT_45_25, UINT_42, LESS_THAN, UINT_0, null },

                // Less than: float and signed integer (<)
                { FLOAT_3_25, SINT_42, LESS_THAN, UINT_1, null },

                // Less than: float and signed integer (==)
                { FLOAT_42, SINT_42, LESS_THAN, UINT_0, null },

                // Less than: float and signed integer (>)
                { FLOAT_45_25, SINT_4, LESS_THAN, UINT_0, null },

                // Less than: float and float (<)
                { FLOAT_0_5, FLOAT_3_25, LESS_THAN, UINT_1, null },

                // Less than: float and float (===)
                { FLOAT_3_25, FLOAT_3_25, LESS_THAN, UINT_0, null },

                // Less than: float and float (>)
                { FLOAT_3_25, FLOAT_0_5, LESS_THAN, UINT_0, null },

                // Less than: float and string (<)
                { FLOAT_3_25, STRING_A, LESS_THAN, UINT_1, null },

                // Less than: float and string (==)
                { FLOAT_3_25, STRING_3_25, LESS_THAN, UINT_0, null },

                // Less than: float and string (>)
                { FLOAT_3_25, STRING_ASTERISK, LESS_THAN, UINT_0, null },

                // Less than: float and function (always <)
                { FLOAT_3_25, UNDETERMINED_FUNCTION_A_VALUE, LESS_THAN, UINT_1, null },

                // Less than: string and undetermined
                { STRING_A, null, LESS_THAN, null, null },

                // Less than: string and unsigned integer (<)
                { STRING_ASTERISK, UINT_42, LESS_THAN, UINT_1, null },

                // Less than: string and unsigned integer (==)
                { STRING_42, UINT_42, LESS_THAN, UINT_0, null },

                // Less than: string and unsigned integer (>)
                { STRING_A, UINT_42, LESS_THAN, UINT_0, null },

                // Less than: string and signed integer (<)
                { STRING_ASTERISK, SINT_42, LESS_THAN, UINT_1, null },

                // Less than: string and signed integer (==)
                { STRING_42, SINT_42, LESS_THAN, UINT_0, null },

                // Less than: string and signed integer (>)
                { STRING_A, SINT_4, LESS_THAN, UINT_0, null },

                // Less than: string and float (<)
                { STRING_ASTERISK, FLOAT_3_25, LESS_THAN, UINT_1, null },

                // Less than: string and float (==)
                { STRING_3_25, FLOAT_3_25, LESS_THAN, UINT_0, null },

                // Less than: string and float (>)
                { STRING_A, FLOAT_3_25, LESS_THAN, UINT_0, null },

                // Less than: string and string (<)
                { STRING_ASTERISK, STRING_A, LESS_THAN, UINT_1, null },

                // Less than: string and string (===)
                { STRING_A, STRING_A, LESS_THAN, UINT_0, null },

                // Less than: string and string (>)
                { STRING_AA, STRING_A, LESS_THAN, UINT_0, null },

                // Less than: string and function (always <)
                { STRING_A, UNDETERMINED_FUNCTION_A_VALUE, LESS_THAN, UINT_1, null },

                // Less than: function and undetermined (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, null, LESS_THAN, null, null },

                // Less than: function and unsigned integer (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, UINT_42, LESS_THAN, UINT_0, null },

                // Less than: function and signed integer (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, SINT_42, LESS_THAN, UINT_0, null },

                // Less than: function and float (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, FLOAT_3_25, LESS_THAN, UINT_0, null },

                // Less than: function and string (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_A, LESS_THAN, UINT_0, null },

                // Less than: function and function (<)
                // NOTE: The order of the tests for comparison of function values is important: see the implementation of
                // evaluateComparison() for details. Luckily, the Parameterized test runner runs the tests in a predictable order.
                // Also note that these tests currently use null for the assembly.
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_B_VALUE, LESS_THAN, UINT_1, null },

                // Less than: function and function (===)
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_A_VALUE, LESS_THAN, UINT_0, null },

                // Less than: function and function (>)
                { UNDETERMINED_FUNCTION_B_VALUE, UNDETERMINED_FUNCTION_A_VALUE, LESS_THAN, UINT_0, null },

                // Less than or equal to: undetermined and undetermined
                { null, null, LESS_THAN_OR_EQUAL_TO, null, null },

                // Less than or equal to: undetermined and unsigned integer
                { null, UINT_42, LESS_THAN_OR_EQUAL_TO, null, null },

                // Less than or equal to: undetermined and signed integer
                { null, SINT_42, LESS_THAN_OR_EQUAL_TO, null, null },

                // Less than or equal to: undetermined and float
                { null, FLOAT_3_25, LESS_THAN_OR_EQUAL_TO, null, null },

                // Less than or equal to: undetermined and string
                { null, STRING_A, LESS_THAN_OR_EQUAL_TO, null, null },

                // Less than or equal to: undetermined and function
                { null, UNDETERMINED_FUNCTION_A_VALUE, LESS_THAN_OR_EQUAL_TO, null, null },

                // Less than or equal to: unsigned integer and undetermined
                { UINT_42, null, LESS_THAN_OR_EQUAL_TO, null, null },

                // Less than or equal to: unsigned integer and unsigned integer (<)
                { UINT_4, UINT_42, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: unsigned integer and unsigned integer (===)
                { UINT_42, UINT_42, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: unsigned integer and unsigned integer (>)
                { UINT_42, UINT_4, LESS_THAN_OR_EQUAL_TO, UINT_0, null },

                // Less than or equal to: unsigned integer and signed integer (<)
                { UINT_4, SINT_42, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: unsigned integer and signed integer (==)
                { UINT_42, SINT_42, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: unsigned integer and signed integer (>)
                { UINT_42, SINT_4, LESS_THAN_OR_EQUAL_TO, UINT_0, null },

                // Less than or equal to: huge unsigned integer and signed integer (>)
                { UINT_18000000000000000000, SINT_42, LESS_THAN_OR_EQUAL_TO, UINT_0, null },

                // Less than or equal to: unsigned integer and negative signed integer (>)
                { UINT_42, SINT_MINUS_1, LESS_THAN_OR_EQUAL_TO, UINT_0, null },

                // Less than or equal to: unsigned integer and float (<)
                { UINT_4, FLOAT_42, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: unsigned integer and float (==)
                { UINT_42, FLOAT_42, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: unsigned integer and float (>)
                { UINT_42, FLOAT_3_25, LESS_THAN_OR_EQUAL_TO, UINT_0, null },

                // Less than or equal to: unsigned integer and string (<)
                { UINT_42, STRING_A, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: unsigned integer and string (==)
                { UINT_42, STRING_42, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: unsigned integer and string (>)
                { UINT_42, STRING_ASTERISK, LESS_THAN_OR_EQUAL_TO, UINT_0, null },

                // Less than or equal to: unsigned integer and function (always <)
                { UINT_42, UNDETERMINED_FUNCTION_A_VALUE, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: signed integer and undetermined
                { SINT_42, null, LESS_THAN_OR_EQUAL_TO, null, null },

                // Less than or equal to: signed integer and unsigned integer (<)
                { SINT_4, UINT_42, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: negative signed integer and unsigned integer (<)
                { SINT_MINUS_1, UINT_42, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: signed integer and huge unsigned integer (<)
                { SINT_4, UINT_18000000000000000000, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: signed integer and unsigned integer (==)
                { SINT_42, UINT_42, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: signed integer and unsigned integer (>)
                { SINT_42, UINT_4, LESS_THAN_OR_EQUAL_TO, UINT_0, null },

                // Less than or equal to: signed integer and signed integer (<)
                { SINT_4, SINT_42, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: signed integer and signed integer (===)
                { SINT_42, SINT_42, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: signed integer and signed integer (>)
                { SINT_42, SINT_4, LESS_THAN_OR_EQUAL_TO, UINT_0, null },

                // Less than or equal to: signed integer and float (<)
                { SINT_4, FLOAT_42, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: signed integer and float (==)
                { SINT_42, FLOAT_42, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: signed integer and float (>)
                { SINT_42, FLOAT_3_25, LESS_THAN_OR_EQUAL_TO, UINT_0, null },

                // Less than or equal to: signed integer and string (<)
                { SINT_42, STRING_A, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: signed integer and string (==)
                { SINT_42, STRING_42, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: signed integer and string (>)
                { SINT_42, STRING_ASTERISK, LESS_THAN_OR_EQUAL_TO, UINT_0, null },

                // Less than or equal to: signed integer and function (always <)
                { SINT_42, UNDETERMINED_FUNCTION_A_VALUE, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: float and undetermined
                { FLOAT_3_25, null, LESS_THAN_OR_EQUAL_TO, null, null },

                // Less than or equal to: float and unsigned integer (<)
                { FLOAT_3_25, UINT_42, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: float and unsigned integer (==)
                { FLOAT_42, UINT_42, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: float and unsigned integer (>)
                { FLOAT_45_25, UINT_42, LESS_THAN_OR_EQUAL_TO, UINT_0, null },

                // Less than or equal to: float and signed integer (<)
                { FLOAT_3_25, SINT_42, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: float and signed integer (==)
                { FLOAT_42, SINT_42, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: float and signed integer (>)
                { FLOAT_45_25, SINT_4, LESS_THAN_OR_EQUAL_TO, UINT_0, null },

                // Less than or equal to: float and float (<)
                { FLOAT_0_5, FLOAT_3_25, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: float and float (===)
                { FLOAT_3_25, FLOAT_3_25, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: float and float (>)
                { FLOAT_3_25, FLOAT_0_5, LESS_THAN_OR_EQUAL_TO, UINT_0, null },

                // Less than or equal to: float and string (<)
                { FLOAT_3_25, STRING_A, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: float and string (==)
                { FLOAT_3_25, STRING_3_25, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: float and string (>)
                { FLOAT_3_25, STRING_ASTERISK, LESS_THAN_OR_EQUAL_TO, UINT_0, null },

                // Less than or equal to: float and function (always <)
                { FLOAT_3_25, UNDETERMINED_FUNCTION_A_VALUE, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: string and undetermined
                { STRING_A, null, LESS_THAN_OR_EQUAL_TO, null, null },

                // Less than or equal to: string and unsigned integer (<)
                { STRING_ASTERISK, UINT_42, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: string and unsigned integer (==)
                { STRING_42, UINT_42, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: string and unsigned integer (>)
                { STRING_A, UINT_42, LESS_THAN_OR_EQUAL_TO, UINT_0, null },

                // Less than or equal to: string and signed integer (<)
                { STRING_ASTERISK, SINT_42, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: string and signed integer (==)
                { STRING_42, SINT_42, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: string and signed integer (>)
                { STRING_A, SINT_4, LESS_THAN_OR_EQUAL_TO, UINT_0, null },

                // Less than or equal to: string and float (<)
                { STRING_ASTERISK, FLOAT_3_25, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: string and float (==)
                { STRING_3_25, FLOAT_3_25, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: string and float (>)
                { STRING_A, FLOAT_3_25, LESS_THAN_OR_EQUAL_TO, UINT_0, null },

                // Less than or equal to: string and string (<)
                { STRING_ASTERISK, STRING_A, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: string and string (===)
                { STRING_A, STRING_A, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: string and string (>)
                { STRING_AA, STRING_A, LESS_THAN_OR_EQUAL_TO, UINT_0, null },

                // Less than or equal to: string and function (always <)
                { STRING_A, UNDETERMINED_FUNCTION_A_VALUE, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: function and undetermined (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, null, LESS_THAN_OR_EQUAL_TO, null, null },

                // Less than or equal to: function and unsigned integer (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, UINT_42, LESS_THAN_OR_EQUAL_TO, UINT_0, null },

                // Less than or equal to: function and signed integer (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, SINT_42, LESS_THAN_OR_EQUAL_TO, UINT_0, null },

                // Less than or equal to: function and float (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, FLOAT_3_25, LESS_THAN_OR_EQUAL_TO, UINT_0, null },

                // Less than or equal to: function and string (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_A, LESS_THAN_OR_EQUAL_TO, UINT_0, null },

                // Less than or equal to: function and function (<)
                // NOTE: The order of the tests for comparison of function values is important: see the implementation of
                // evaluateComparison() for details. Luckily, the Parameterized test runner runs the tests in a predictable order.
                // Also note that these tests currently use null for the assembly.
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_B_VALUE, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: function and function (===)
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_A_VALUE, LESS_THAN_OR_EQUAL_TO, UINT_1, null },

                // Less than or equal to: function and function (>)
                { UNDETERMINED_FUNCTION_B_VALUE, UNDETERMINED_FUNCTION_A_VALUE, LESS_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than: undetermined and undetermined
                { null, null, GREATER_THAN, null, null },

                // Greater than: undetermined and unsigned integer
                { null, UINT_42, GREATER_THAN, null, null },

                // Greater than: undetermined and signed integer
                { null, SINT_42, GREATER_THAN, null, null },

                // Greater than: undetermined and float
                { null, FLOAT_3_25, GREATER_THAN, null, null },

                // Greater than: undetermined and string
                { null, STRING_A, GREATER_THAN, null, null },

                // Greater than: undetermined and function
                { null, UNDETERMINED_FUNCTION_A_VALUE, GREATER_THAN, null, null },

                // Greater than: unsigned integer and undetermined
                { UINT_42, null, GREATER_THAN, null, null },

                // Greater than: unsigned integer and unsigned integer (<)
                { UINT_4, UINT_42, GREATER_THAN, UINT_0, null },

                // Greater than: unsigned integer and unsigned integer (===)
                { UINT_42, UINT_42, GREATER_THAN, UINT_0, null },

                // Greater than: unsigned integer and unsigned integer (>)
                { UINT_42, UINT_4, GREATER_THAN, UINT_1, null },

                // Greater than: unsigned integer and signed integer (<)
                { UINT_4, SINT_42, GREATER_THAN, UINT_0, null },

                // Greater than: unsigned integer and signed integer (==)
                { UINT_42, SINT_42, GREATER_THAN, UINT_0, null },

                // Greater than: unsigned integer and signed integer (>)
                { UINT_42, SINT_4, GREATER_THAN, UINT_1, null },

                // Greater than: huge unsigned integer and signed integer (>)
                { UINT_18000000000000000000, SINT_42, GREATER_THAN, UINT_1, null },

                // Greater than: unsigned integer and negative signed integer (>)
                { UINT_42, SINT_MINUS_1, GREATER_THAN, UINT_1, null },

                // Greater than: unsigned integer and float (<)
                { UINT_4, FLOAT_42, GREATER_THAN, UINT_0, null },

                // Greater than: unsigned integer and float (==)
                { UINT_42, FLOAT_42, GREATER_THAN, UINT_0, null },

                // Greater than: unsigned integer and float (>)
                { UINT_42, FLOAT_3_25, GREATER_THAN, UINT_1, null },

                // Greater than: unsigned integer and string (<)
                { UINT_42, STRING_A, GREATER_THAN, UINT_0, null },

                // Greater than: unsigned integer and string (==)
                { UINT_42, STRING_42, GREATER_THAN, UINT_0, null },

                // Greater than: unsigned integer and string (>)
                { UINT_42, STRING_ASTERISK, GREATER_THAN, UINT_1, null },

                // Greater than: unsigned integer and function (always <)
                { UINT_42, UNDETERMINED_FUNCTION_A_VALUE, GREATER_THAN, UINT_0, null },

                // Greater than: signed integer and undetermined
                { SINT_42, null, GREATER_THAN, null, null },

                // Greater than: signed integer and unsigned integer (<)
                { SINT_4, UINT_42, GREATER_THAN, UINT_0, null },

                // Greater than: negative signed integer and unsigned integer (<)
                { SINT_MINUS_1, UINT_42, GREATER_THAN, UINT_0, null },

                // Greater than: signed integer and huge unsigned integer (<)
                { SINT_4, UINT_18000000000000000000, GREATER_THAN, UINT_0, null },

                // Greater than: signed integer and unsigned integer (==)
                { SINT_42, UINT_42, GREATER_THAN, UINT_0, null },

                // Greater than: signed integer and unsigned integer (>)
                { SINT_42, UINT_4, GREATER_THAN, UINT_1, null },

                // Greater than: signed integer and signed integer (<)
                { SINT_4, SINT_42, GREATER_THAN, UINT_0, null },

                // Greater than: signed integer and signed integer (===)
                { SINT_42, SINT_42, GREATER_THAN, UINT_0, null },

                // Greater than: signed integer and signed integer (>)
                { SINT_42, SINT_4, GREATER_THAN, UINT_1, null },

                // Greater than: signed integer and float (<)
                { SINT_4, FLOAT_42, GREATER_THAN, UINT_0, null },

                // Greater than: signed integer and float (==)
                { SINT_42, FLOAT_42, GREATER_THAN, UINT_0, null },

                // Greater than: signed integer and float (>)
                { SINT_42, FLOAT_3_25, GREATER_THAN, UINT_1, null },

                // Greater than: signed integer and string (<)
                { SINT_42, STRING_A, GREATER_THAN, UINT_0, null },

                // Greater than: signed integer and string (==)
                { SINT_42, STRING_42, GREATER_THAN, UINT_0, null },

                // Greater than: signed integer and string (>)
                { SINT_42, STRING_ASTERISK, GREATER_THAN, UINT_1, null },

                // Greater than: signed integer and function (always <)
                { SINT_42, UNDETERMINED_FUNCTION_A_VALUE, GREATER_THAN, UINT_0, null },

                // Greater than: float and undetermined
                { FLOAT_3_25, null, GREATER_THAN, null, null },

                // Greater than: float and unsigned integer (<)
                { FLOAT_3_25, UINT_42, GREATER_THAN, UINT_0, null },

                // Greater than: float and unsigned integer (==)
                { FLOAT_42, UINT_42, GREATER_THAN, UINT_0, null },

                // Greater than: float and unsigned integer (>)
                { FLOAT_45_25, UINT_42, GREATER_THAN, UINT_1, null },

                // Greater than: float and signed integer (<)
                { FLOAT_3_25, SINT_42, GREATER_THAN, UINT_0, null },

                // Greater than: float and signed integer (==)
                { FLOAT_42, SINT_42, GREATER_THAN, UINT_0, null },

                // Greater than: float and signed integer (>)
                { FLOAT_45_25, SINT_4, GREATER_THAN, UINT_1, null },

                // Greater than: float and float (<)
                { FLOAT_0_5, FLOAT_3_25, GREATER_THAN, UINT_0, null },

                // Greater than: float and float (===)
                { FLOAT_3_25, FLOAT_3_25, GREATER_THAN, UINT_0, null },

                // Greater than: float and float (>)
                { FLOAT_3_25, FLOAT_0_5, GREATER_THAN, UINT_1, null },

                // Greater than: float and string (<)
                { FLOAT_3_25, STRING_A, GREATER_THAN, UINT_0, null },

                // Greater than: float and string (==)
                { FLOAT_3_25, STRING_3_25, GREATER_THAN, UINT_0, null },

                // Greater than: float and string (>)
                { FLOAT_3_25, STRING_ASTERISK, GREATER_THAN, UINT_1, null },

                // Greater than: float and function (always <)
                { FLOAT_3_25, UNDETERMINED_FUNCTION_A_VALUE, GREATER_THAN, UINT_0, null },

                // Greater than: string and undetermined
                { STRING_A, null, GREATER_THAN, null, null },

                // Greater than: string and unsigned integer (<)
                { STRING_ASTERISK, UINT_42, GREATER_THAN, UINT_0, null },

                // Greater than: string and unsigned integer (==)
                { STRING_42, UINT_42, GREATER_THAN, UINT_0, null },

                // Greater than: string and unsigned integer (>)
                { STRING_A, UINT_42, GREATER_THAN, UINT_1, null },

                // Greater than: string and signed integer (<)
                { STRING_ASTERISK, SINT_42, GREATER_THAN, UINT_0, null },

                // Greater than: string and signed integer (==)
                { STRING_42, SINT_42, GREATER_THAN, UINT_0, null },

                // Greater than: string and signed integer (>)
                { STRING_A, SINT_4, GREATER_THAN, UINT_1, null },

                // Greater than: string and float (<)
                { STRING_ASTERISK, FLOAT_3_25, GREATER_THAN, UINT_0, null },

                // Greater than: string and float (==)
                { STRING_3_25, FLOAT_3_25, GREATER_THAN, UINT_0, null },

                // Greater than: string and float (>)
                { STRING_A, FLOAT_3_25, GREATER_THAN, UINT_1, null },

                // Greater than: string and string (<)
                { STRING_ASTERISK, STRING_A, GREATER_THAN, UINT_0, null },

                // Greater than: string and string (===)
                { STRING_A, STRING_A, GREATER_THAN, UINT_0, null },

                // Greater than: string and string (>)
                { STRING_AA, STRING_A, GREATER_THAN, UINT_1, null },

                // Greater than: string and function (always <)
                { STRING_A, UNDETERMINED_FUNCTION_A_VALUE, GREATER_THAN, UINT_0, null },

                // Greater than: function and undetermined (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, null, GREATER_THAN, null, null },

                // Greater than: function and unsigned integer (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, UINT_42, GREATER_THAN, UINT_1, null },

                // Greater than: function and signed integer (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, SINT_42, GREATER_THAN, UINT_1, null },

                // Greater than: function and float (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, FLOAT_3_25, GREATER_THAN, UINT_1, null },

                // Greater than: function and string (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_A, GREATER_THAN, UINT_1, null },

                // Greater than: function and function (<)
                // NOTE: The order of the tests for comparison of function values is important: see the implementation of
                // evaluateComparison() for details. Luckily, the Parameterized test runner runs the tests in a predictable order.
                // Also note that these tests currently use null for the assembly.
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_B_VALUE, GREATER_THAN, UINT_0, null },

                // Greater than: function and function (===)
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_A_VALUE, GREATER_THAN, UINT_0, null },

                // Greater than: function and function (>)
                { UNDETERMINED_FUNCTION_B_VALUE, UNDETERMINED_FUNCTION_A_VALUE, GREATER_THAN, UINT_1, null },

                // Greater than or equal to: undetermined and undetermined
                { null, null, GREATER_THAN_OR_EQUAL_TO, null, null },

                // Greater than or equal to: undetermined and unsigned integer
                { null, UINT_42, GREATER_THAN_OR_EQUAL_TO, null, null },

                // Greater than or equal to: undetermined and signed integer
                { null, SINT_42, GREATER_THAN_OR_EQUAL_TO, null, null },

                // Greater than or equal to: undetermined and float
                { null, FLOAT_3_25, GREATER_THAN_OR_EQUAL_TO, null, null },

                // Greater than or equal to: undetermined and string
                { null, STRING_A, GREATER_THAN_OR_EQUAL_TO, null, null },

                // Greater than or equal to: undetermined and function
                { null, UNDETERMINED_FUNCTION_A_VALUE, GREATER_THAN_OR_EQUAL_TO, null, null },

                // Greater than or equal to: unsigned integer and undetermined
                { UINT_42, null, GREATER_THAN_OR_EQUAL_TO, null, null },

                // Greater than or equal to: unsigned integer and unsigned integer (<)
                { UINT_4, UINT_42, GREATER_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than or equal to: unsigned integer and unsigned integer (===)
                { UINT_42, UINT_42, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: unsigned integer and unsigned integer (>)
                { UINT_42, UINT_4, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: unsigned integer and signed integer (<)
                { UINT_4, SINT_42, GREATER_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than or equal to: unsigned integer and signed integer (==)
                { UINT_42, SINT_42, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: unsigned integer and signed integer (>)
                { UINT_42, SINT_4, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: huge unsigned integer and signed integer (>)
                { UINT_18000000000000000000, SINT_42, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: unsigned integer and negative signed integer (>)
                { UINT_42, SINT_MINUS_1, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: unsigned integer and float (<)
                { UINT_4, FLOAT_42, GREATER_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than or equal to: unsigned integer and float (==)
                { UINT_42, FLOAT_42, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: unsigned integer and float (>)
                { UINT_42, FLOAT_3_25, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: unsigned integer and string (<)
                { UINT_42, STRING_A, GREATER_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than or equal to: unsigned integer and string (==)
                { UINT_42, STRING_42, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: unsigned integer and string (>)
                { UINT_42, STRING_ASTERISK, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: unsigned integer and function (always <)
                { UINT_42, UNDETERMINED_FUNCTION_A_VALUE, GREATER_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than or equal to: signed integer and undetermined
                { SINT_42, null, GREATER_THAN_OR_EQUAL_TO, null, null },

                // Greater than or equal to: signed integer and unsigned integer (<)
                { SINT_4, UINT_42, GREATER_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than or equal to: negative signed integer and unsigned integer (<)
                { SINT_MINUS_1, UINT_42, GREATER_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than or equal to: signed integer and huge unsigned integer (<)
                { SINT_4, UINT_18000000000000000000, GREATER_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than or equal to: signed integer and unsigned integer (==)
                { SINT_42, UINT_42, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: signed integer and unsigned integer (>)
                { SINT_42, UINT_4, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: signed integer and signed integer (<)
                { SINT_4, SINT_42, GREATER_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than or equal to: signed integer and signed integer (===)
                { SINT_42, SINT_42, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: signed integer and signed integer (>)
                { SINT_42, SINT_4, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: signed integer and float (<)
                { SINT_4, FLOAT_42, GREATER_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than or equal to: signed integer and float (==)
                { SINT_42, FLOAT_42, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: signed integer and float (>)
                { SINT_42, FLOAT_3_25, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: signed integer and string (<)
                { SINT_42, STRING_A, GREATER_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than or equal to: signed integer and string (==)
                { SINT_42, STRING_42, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: signed integer and string (>)
                { SINT_42, STRING_ASTERISK, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: signed integer and function (always <)
                { SINT_42, UNDETERMINED_FUNCTION_A_VALUE, GREATER_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than or equal to: float and undetermined
                { FLOAT_3_25, null, GREATER_THAN_OR_EQUAL_TO, null, null },

                // Greater than or equal to: float and unsigned integer (<)
                { FLOAT_3_25, UINT_42, GREATER_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than or equal to: float and unsigned integer (==)
                { FLOAT_42, UINT_42, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: float and unsigned integer (>)
                { FLOAT_45_25, UINT_42, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: float and signed integer (<)
                { FLOAT_3_25, SINT_42, GREATER_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than or equal to: float and signed integer (==)
                { FLOAT_42, SINT_42, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: float and signed integer (>)
                { FLOAT_45_25, SINT_4, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: float and float (<)
                { FLOAT_0_5, FLOAT_3_25, GREATER_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than or equal to: float and float (===)
                { FLOAT_3_25, FLOAT_3_25, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: float and float (>)
                { FLOAT_3_25, FLOAT_0_5, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: float and string (<)
                { FLOAT_3_25, STRING_A, GREATER_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than or equal to: float and string (==)
                { FLOAT_3_25, STRING_3_25, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: float and string (>)
                { FLOAT_3_25, STRING_ASTERISK, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: float and function (always <)
                { FLOAT_3_25, UNDETERMINED_FUNCTION_A_VALUE, GREATER_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than or equal to: string and undetermined
                { STRING_A, null, GREATER_THAN_OR_EQUAL_TO, null, null },

                // Greater than or equal to: string and unsigned integer (<)
                { STRING_ASTERISK, UINT_42, GREATER_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than or equal to: string and unsigned integer (==)
                { STRING_42, UINT_42, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: string and unsigned integer (>)
                { STRING_A, UINT_42, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: string and signed integer (<)
                { STRING_ASTERISK, SINT_42, GREATER_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than or equal to: string and signed integer (==)
                { STRING_42, SINT_42, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: string and signed integer (>)
                { STRING_A, SINT_4, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: string and float (<)
                { STRING_ASTERISK, FLOAT_3_25, GREATER_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than or equal to: string and float (==)
                { STRING_3_25, FLOAT_3_25, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: string and float (>)
                { STRING_A, FLOAT_3_25, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: string and string (<)
                { STRING_ASTERISK, STRING_A, GREATER_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than or equal to: string and string (===)
                { STRING_A, STRING_A, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: string and string (>)
                { STRING_AA, STRING_A, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: string and function (always <)
                { STRING_A, UNDETERMINED_FUNCTION_A_VALUE, GREATER_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than or equal to: function and undetermined (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, null, GREATER_THAN_OR_EQUAL_TO, null, null },

                // Greater than or equal to: function and unsigned integer (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, UINT_42, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: function and signed integer (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, SINT_42, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: function and float (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, FLOAT_3_25, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: function and string (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_A, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: function and function (<)
                // NOTE: The order of the tests for comparison of function values is important: see the implementation of
                // evaluateComparison() for details. Luckily, the Parameterized test runner runs the tests in a predictable order.
                // Also note that these tests currently use null for the assembly.
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_B_VALUE, GREATER_THAN_OR_EQUAL_TO, UINT_0, null },

                // Greater than or equal to: function and function (===)
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_A_VALUE, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Greater than or equal to: function and function (>)
                { UNDETERMINED_FUNCTION_B_VALUE, UNDETERMINED_FUNCTION_A_VALUE, GREATER_THAN_OR_EQUAL_TO, UINT_1, null },

                // Equal to: undetermined and undetermined
                { null, null, EQUAL_TO, null, null },

                // Equal to: undetermined and unsigned integer
                { null, UINT_42, EQUAL_TO, null, null },

                // Equal to: undetermined and signed integer
                { null, SINT_42, EQUAL_TO, null, null },

                // Equal to: undetermined and float
                { null, FLOAT_3_25, EQUAL_TO, null, null },

                // Equal to: undetermined and string
                { null, STRING_A, EQUAL_TO, null, null },

                // Equal to: undetermined and function
                { null, UNDETERMINED_FUNCTION_A_VALUE, EQUAL_TO, null, null },

                // Equal to: unsigned integer and undetermined
                { UINT_42, null, EQUAL_TO, null, null },

                // Equal to: unsigned integer and unsigned integer (<)
                { UINT_4, UINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: unsigned integer and unsigned integer (===)
                { UINT_42, UINT_42, EQUAL_TO, UINT_1, null },

                // Equal to: unsigned integer and unsigned integer (>)
                { UINT_42, UINT_4, EQUAL_TO, UINT_0, null },

                // Equal to: unsigned integer and signed integer (<)
                { UINT_4, SINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: unsigned integer and signed integer (==)
                { UINT_42, SINT_42, EQUAL_TO, UINT_1, null },

                // Equal to: unsigned integer and signed integer (>)
                { UINT_42, SINT_4, EQUAL_TO, UINT_0, null },

                // Equal to: huge unsigned integer and signed integer (>)
                { UINT_18000000000000000000, SINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: unsigned integer and negative signed integer (>)
                { UINT_42, SINT_MINUS_1, EQUAL_TO, UINT_0, null },

                // Equal to: unsigned integer and float (<)
                { UINT_4, FLOAT_42, EQUAL_TO, UINT_0, null },

                // Equal to: unsigned integer and float (==)
                { UINT_42, FLOAT_42, EQUAL_TO, UINT_1, null },

                // Equal to: unsigned integer and float (>)
                { UINT_42, FLOAT_3_25, EQUAL_TO, UINT_0, null },

                // Equal to: unsigned integer and string (<)
                { UINT_42, STRING_A, EQUAL_TO, UINT_0, null },

                // Equal to: unsigned integer and string (==)
                { UINT_42, STRING_42, EQUAL_TO, UINT_1, null },

                // Equal to: unsigned integer and string (>)
                { UINT_42, STRING_ASTERISK, EQUAL_TO, UINT_0, null },

                // Equal to: unsigned integer and function (always <)
                { UINT_42, UNDETERMINED_FUNCTION_A_VALUE, EQUAL_TO, UINT_0, null },

                // Equal to: signed integer and undetermined
                { SINT_42, null, EQUAL_TO, null, null },

                // Equal to: signed integer and unsigned integer (<)
                { SINT_4, UINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: negative signed integer and unsigned integer (<)
                { SINT_MINUS_1, UINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: signed integer and huge unsigned integer (<)
                { SINT_4, UINT_18000000000000000000, EQUAL_TO, UINT_0, null },

                // Equal to: signed integer and unsigned integer (==)
                { SINT_42, UINT_42, EQUAL_TO, UINT_1, null },

                // Equal to: signed integer and unsigned integer (>)
                { SINT_42, UINT_4, EQUAL_TO, UINT_0, null },

                // Equal to: signed integer and signed integer (<)
                { SINT_4, SINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: signed integer and signed integer (===)
                { SINT_42, SINT_42, EQUAL_TO, UINT_1, null },

                // Equal to: signed integer and signed integer (>)
                { SINT_42, SINT_4, EQUAL_TO, UINT_0, null },

                // Equal to: signed integer and float (<)
                { SINT_4, FLOAT_42, EQUAL_TO, UINT_0, null },

                // Equal to: signed integer and float (==)
                { SINT_42, FLOAT_42, EQUAL_TO, UINT_1, null },

                // Equal to: signed integer and float (>)
                { SINT_42, FLOAT_3_25, EQUAL_TO, UINT_0, null },

                // Equal to: signed integer and string (<)
                { SINT_42, STRING_A, EQUAL_TO, UINT_0, null },

                // Equal to: signed integer and string (==)
                { SINT_42, STRING_42, EQUAL_TO, UINT_1, null },

                // Equal to: signed integer and string (>)
                { SINT_42, STRING_ASTERISK, EQUAL_TO, UINT_0, null },

                // Equal to: signed integer and function (always <)
                { SINT_42, UNDETERMINED_FUNCTION_A_VALUE, EQUAL_TO, UINT_0, null },

                // Equal to: float and undetermined
                { FLOAT_3_25, null, EQUAL_TO, null, null },

                // Equal to: float and unsigned integer (<)
                { FLOAT_3_25, UINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: float and unsigned integer (==)
                { FLOAT_42, UINT_42, EQUAL_TO, UINT_1, null },

                // Equal to: float and unsigned integer (>)
                { FLOAT_45_25, UINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: float and signed integer (<)
                { FLOAT_3_25, SINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: float and signed integer (==)
                { FLOAT_42, SINT_42, EQUAL_TO, UINT_1, null },

                // Equal to: float and signed integer (>)
                { FLOAT_45_25, SINT_4, EQUAL_TO, UINT_0, null },

                // Equal to: float and float (<)
                { FLOAT_0_5, FLOAT_3_25, EQUAL_TO, UINT_0, null },

                // Equal to: float and float (===)
                { FLOAT_3_25, FLOAT_3_25, EQUAL_TO, UINT_1, null },

                // Equal to: float and float (>)
                { FLOAT_3_25, FLOAT_0_5, EQUAL_TO, UINT_0, null },

                // Equal to: float and string (<)
                { FLOAT_3_25, STRING_A, EQUAL_TO, UINT_0, null },

                // Equal to: float and string (==)
                { FLOAT_3_25, STRING_3_25, EQUAL_TO, UINT_1, null },

                // Equal to: float and string (>)
                { FLOAT_3_25, STRING_ASTERISK, EQUAL_TO, UINT_0, null },

                // Equal to: float and function (always <)
                { FLOAT_3_25, UNDETERMINED_FUNCTION_A_VALUE, EQUAL_TO, UINT_0, null },

                // Equal to: string and undetermined
                { STRING_A, null, EQUAL_TO, null, null },

                // Equal to: string and unsigned integer (<)
                { STRING_ASTERISK, UINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: string and unsigned integer (==)
                { STRING_42, UINT_42, EQUAL_TO, UINT_1, null },

                // Equal to: string and unsigned integer (>)
                { STRING_A, UINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: string and signed integer (<)
                { STRING_ASTERISK, SINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: string and signed integer (==)
                { STRING_42, SINT_42, EQUAL_TO, UINT_1, null },

                // Equal to: string and signed integer (>)
                { STRING_A, SINT_4, EQUAL_TO, UINT_0, null },

                // Equal to: string and float (<)
                { STRING_ASTERISK, FLOAT_3_25, EQUAL_TO, UINT_0, null },

                // Equal to: string and float (==)
                { STRING_3_25, FLOAT_3_25, EQUAL_TO, UINT_1, null },

                // Equal to: string and float (>)
                { STRING_A, FLOAT_3_25, EQUAL_TO, UINT_0, null },

                // Equal to: string and string (<)
                { STRING_ASTERISK, STRING_A, EQUAL_TO, UINT_0, null },

                // Equal to: string and string (===)
                { STRING_A, STRING_A, EQUAL_TO, UINT_1, null },

                // Equal to: string and string (>)
                { STRING_AA, STRING_A, EQUAL_TO, UINT_0, null },

                // Equal to: string and function (always <)
                { STRING_A, UNDETERMINED_FUNCTION_A_VALUE, EQUAL_TO, UINT_0, null },

                // Equal to: function and undetermined (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, null, EQUAL_TO, null, null },

                // Equal to: function and unsigned integer (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, UINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: function and signed integer (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, SINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: function and float (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, FLOAT_3_25, EQUAL_TO, UINT_0, null },

                // Equal to: function and string (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_A, EQUAL_TO, UINT_0, null },

                // Equal to: function and function (<)
                // NOTE: The order of the tests for comparison of function values is important: see the implementation of
                // evaluateComparison() for details. Luckily, the Parameterized test runner runs the tests in a predictable order.
                // Also note that these tests currently use null for the assembly.
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_B_VALUE, EQUAL_TO, UINT_0, null },

                // Equal to: function and function (===)
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_A_VALUE, EQUAL_TO, UINT_1, null },

                // Equal to: function and function (>)
                { UNDETERMINED_FUNCTION_B_VALUE, UNDETERMINED_FUNCTION_A_VALUE, EQUAL_TO, UINT_0, null },

                // Different from: undetermined and undetermined
                { null, null, DIFFERENT_FROM, null, null },

                // Different from: undetermined and unsigned integer
                { null, UINT_42, DIFFERENT_FROM, null, null },

                // Different from: undetermined and signed integer
                { null, SINT_42, DIFFERENT_FROM, null, null },

                // Different from: undetermined and float
                { null, FLOAT_3_25, DIFFERENT_FROM, null, null },

                // Different from: undetermined and string
                { null, STRING_A, DIFFERENT_FROM, null, null },

                // Different from: undetermined and function
                { null, UNDETERMINED_FUNCTION_A_VALUE, DIFFERENT_FROM, null, null },

                // Different from: unsigned integer and undetermined
                { UINT_42, null, DIFFERENT_FROM, null, null },

                // Different from: unsigned integer and unsigned integer (<)
                { UINT_4, UINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: unsigned integer and unsigned integer (===)
                { UINT_42, UINT_42, DIFFERENT_FROM, UINT_0, null },

                // Different from: unsigned integer and unsigned integer (>)
                { UINT_42, UINT_4, DIFFERENT_FROM, UINT_1, null },

                // Different from: unsigned integer and signed integer (<)
                { UINT_4, SINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: unsigned integer and signed integer (==)
                { UINT_42, SINT_42, DIFFERENT_FROM, UINT_0, null },

                // Different from: unsigned integer and signed integer (>)
                { UINT_42, SINT_4, DIFFERENT_FROM, UINT_1, null },

                // Different from: huge unsigned integer and signed integer (>)
                { UINT_18000000000000000000, SINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: unsigned integer and negative signed integer (>)
                { UINT_42, SINT_MINUS_1, DIFFERENT_FROM, UINT_1, null },

                // Different from: unsigned integer and float (<)
                { UINT_4, FLOAT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: unsigned integer and float (==)
                { UINT_42, FLOAT_42, DIFFERENT_FROM, UINT_0, null },

                // Different from: unsigned integer and float (>)
                { UINT_42, FLOAT_3_25, DIFFERENT_FROM, UINT_1, null },

                // Different from: unsigned integer and string (<)
                { UINT_42, STRING_A, DIFFERENT_FROM, UINT_1, null },

                // Different from: unsigned integer and string (==)
                { UINT_42, STRING_42, DIFFERENT_FROM, UINT_0, null },

                // Different from: unsigned integer and string (>)
                { UINT_42, STRING_ASTERISK, DIFFERENT_FROM, UINT_1, null },

                // Different from: unsigned integer and function (always <)
                { UINT_42, UNDETERMINED_FUNCTION_A_VALUE, DIFFERENT_FROM, UINT_1, null },

                // Different from: signed integer and undetermined
                { SINT_42, null, DIFFERENT_FROM, null, null },

                // Different from: signed integer and unsigned integer (<)
                { SINT_4, UINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: negative signed integer and unsigned integer (<)
                { SINT_MINUS_1, UINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: signed integer and huge unsigned integer (<)
                { SINT_4, UINT_18000000000000000000, DIFFERENT_FROM, UINT_1, null },

                // Different from: signed integer and unsigned integer (==)
                { SINT_42, UINT_42, DIFFERENT_FROM, UINT_0, null },

                // Different from: signed integer and unsigned integer (>)
                { SINT_42, UINT_4, DIFFERENT_FROM, UINT_1, null },

                // Different from: signed integer and signed integer (<)
                { SINT_4, SINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: signed integer and signed integer (===)
                { SINT_42, SINT_42, DIFFERENT_FROM, UINT_0, null },

                // Different from: signed integer and signed integer (>)
                { SINT_42, SINT_4, DIFFERENT_FROM, UINT_1, null },

                // Different from: signed integer and float (<)
                { SINT_4, FLOAT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: signed integer and float (==)
                { SINT_42, FLOAT_42, DIFFERENT_FROM, UINT_0, null },

                // Different from: signed integer and float (>)
                { SINT_42, FLOAT_3_25, DIFFERENT_FROM, UINT_1, null },

                // Different from: signed integer and string (<)
                { SINT_42, STRING_A, DIFFERENT_FROM, UINT_1, null },

                // Different from: signed integer and string (==)
                { SINT_42, STRING_42, DIFFERENT_FROM, UINT_0, null },

                // Different from: signed integer and string (>)
                { SINT_42, STRING_ASTERISK, DIFFERENT_FROM, UINT_1, null },

                // Different from: signed integer and function (always <)
                { SINT_42, UNDETERMINED_FUNCTION_A_VALUE, DIFFERENT_FROM, UINT_1, null },

                // Different from: float and undetermined
                { FLOAT_3_25, null, DIFFERENT_FROM, null, null },

                // Different from: float and unsigned integer (<)
                { FLOAT_3_25, UINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: float and unsigned integer (==)
                { FLOAT_42, UINT_42, DIFFERENT_FROM, UINT_0, null },

                // Different from: float and unsigned integer (>)
                { FLOAT_45_25, UINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: float and signed integer (<)
                { FLOAT_3_25, SINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: float and signed integer (==)
                { FLOAT_42, SINT_42, DIFFERENT_FROM, UINT_0, null },

                // Different from: float and signed integer (>)
                { FLOAT_45_25, SINT_4, DIFFERENT_FROM, UINT_1, null },

                // Different from: float and float (<)
                { FLOAT_0_5, FLOAT_3_25, DIFFERENT_FROM, UINT_1, null },

                // Different from: float and float (===)
                { FLOAT_3_25, FLOAT_3_25, DIFFERENT_FROM, UINT_0, null },

                // Different from: float and float (>)
                { FLOAT_3_25, FLOAT_0_5, DIFFERENT_FROM, UINT_1, null },

                // Different from: float and string (<)
                { FLOAT_3_25, STRING_A, DIFFERENT_FROM, UINT_1, null },

                // Different from: float and string (==)
                { FLOAT_3_25, STRING_3_25, DIFFERENT_FROM, UINT_0, null },

                // Different from: float and string (>)
                { FLOAT_3_25, STRING_ASTERISK, DIFFERENT_FROM, UINT_1, null },

                // Different from: float and function (always <)
                { FLOAT_3_25, UNDETERMINED_FUNCTION_A_VALUE, DIFFERENT_FROM, UINT_1, null },

                // Different from: string and undetermined
                { STRING_A, null, DIFFERENT_FROM, null, null },

                // Different from: string and unsigned integer (<)
                { STRING_ASTERISK, UINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: string and unsigned integer (==)
                { STRING_42, UINT_42, DIFFERENT_FROM, UINT_0, null },

                // Different from: string and unsigned integer (>)
                { STRING_A, UINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: string and signed integer (<)
                { STRING_ASTERISK, SINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: string and signed integer (==)
                { STRING_42, SINT_42, DIFFERENT_FROM, UINT_0, null },

                // Different from: string and signed integer (>)
                { STRING_A, SINT_4, DIFFERENT_FROM, UINT_1, null },

                // Different from: string and float (<)
                { STRING_ASTERISK, FLOAT_3_25, DIFFERENT_FROM, UINT_1, null },

                // Different from: string and float (==)
                { STRING_3_25, FLOAT_3_25, DIFFERENT_FROM, UINT_0, null },

                // Different from: string and float (>)
                { STRING_A, FLOAT_3_25, DIFFERENT_FROM, UINT_1, null },

                // Different from: string and string (<)
                { STRING_ASTERISK, STRING_A, DIFFERENT_FROM, UINT_1, null },

                // Different from: string and string (===)
                { STRING_A, STRING_A, DIFFERENT_FROM, UINT_0, null },

                // Different from: string and string (>)
                { STRING_AA, STRING_A, DIFFERENT_FROM, UINT_1, null },

                // Different from: string and function (always <)
                { STRING_A, UNDETERMINED_FUNCTION_A_VALUE, DIFFERENT_FROM, UINT_1, null },

                // Different from: function and undetermined (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, null, DIFFERENT_FROM, null, null },

                // Different from: function and unsigned integer (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, UINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: function and signed integer (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, SINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: function and float (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, FLOAT_3_25, DIFFERENT_FROM, UINT_1, null },

                // Different from: function and string (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_A, DIFFERENT_FROM, UINT_1, null },

                // Different from: function and function (<)
                // NOTE: The order of the tests for comparison of function values is important: see the implementation of
                // evaluateComparison() for details. Luckily, the Parameterized test runner runs the tests in a predictable order.
                // Also note that these tests currently use null for the assembly.
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_B_VALUE, DIFFERENT_FROM, UINT_1, null },

                // Different from: function and function (===)
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_A_VALUE, DIFFERENT_FROM, UINT_0, null },

                // Different from: function and function (>)
                { UNDETERMINED_FUNCTION_B_VALUE, UNDETERMINED_FUNCTION_A_VALUE, DIFFERENT_FROM, UINT_1, null },

        };

        private static final List<Object[]> TEST_DATA = Arrays.asList(TEST_DATA_VALUES);

        @Nonnull
        @Parameters(name = "{0} {2} {1}")
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        @Nonnull
        private final ExpressionEvaluation.Comparison operator;

        public EvaluateComparisonTest(@CheckForNull Value operand1, @CheckForNull Value operand2,
                @Nonnull ExpressionEvaluation.Comparison operator, @CheckForNull Value expectedResult,
                @CheckForNull AssemblyMessage expectedAssemblyMessage) {
            super(operand1, operand2, expectedResult, expectedAssemblyMessage);
            this.operator = operator;
        }

        @CheckForNull
        @Override
        protected Value run(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateComparison(operand1, operand2, evaluationContext, this.operator);
        }

    }

    /**
     * Parameterized test class for {@link ExpressionEvaluation#evaluateLogicalNot(Value)}.
     */
    @RunWith(Parameterized.class)
    public static class EvaluateLogicalNotTest extends BaseEvaluateUnaryTest {

        private static final List<Object[]> TEST_DATA = Arrays.asList(new Object[][] {

                // An undetermined value
                { null, null, null },

                // The zero unsigned integer
                { UINT_0, UINT_1, null },

                // An unsigned integer
                { UINT_42, UINT_0, null },

                // The zero signed integer
                { SINT_0, UINT_1, null },

                // A signed integer
                { SINT_42, UINT_0, null },

                // The zero floating-point number
                { FLOAT_0, UINT_1, null },

                // A floating-point number
                { FLOAT_3_25, UINT_0, null },

                // The empty string
                { STRING_EMPTY, UINT_1, null },

                // A non-empty string
                { STRING_A, UINT_0, null },

                // A function
                { UNDETERMINED_FUNCTION_A_VALUE, UINT_0, null },

        });

        @Nonnull
        @Parameters
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        public EvaluateLogicalNotTest(@CheckForNull Value input, @CheckForNull Value expectedResult,
                @CheckForNull AssemblyMessage expectedAssemblyMessage) {
            super(input, expectedResult, expectedAssemblyMessage);
        }

        @CheckForNull
        @Override
        protected Value run(@CheckForNull Value operand, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateLogicalNot(operand);
        }

    }

    /**
     * Parameterized test class for {@link ExpressionEvaluation#evaluateNegation(Value, EvaluationContext)}.
     */
    @RunWith(Parameterized.class)
    public static class EvaluateNegationTest extends BaseEvaluateUnaryTest {

        private static final List<Object[]> TEST_DATA = Arrays.asList(new Object[][] {

                // An undetermined value
                { null, null, null },

                // An unsigned integer
                { UINT_42, SINT_MINUS_42, null },

                // A signed integer
                { SINT_MINUS_42, SINT_42, null },

                // A positive floating-point number
                { FLOAT_3_25, FLOAT_MINUS_3_25, null },

                // A negative floating-point number
                { FLOAT_MINUS_3_25, FLOAT_3_25, null },

                // A string that can be parsed as a number
                { STRING_3_25, FLOAT_MINUS_3_25, null },

                // A string that cannot be parsed as a number
                { STRING_A, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // A function
                { UNDETERMINED_FUNCTION_A_VALUE, null, new FunctionOperandNotApplicableErrorMessage("negation") },

        });

        @Nonnull
        @Parameters
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        public EvaluateNegationTest(@CheckForNull Value input, @CheckForNull Value expectedResult,
                @CheckForNull AssemblyMessage expectedAssemblyMessage) {
            super(input, expectedResult, expectedAssemblyMessage);
        }

        @CheckForNull
        @Override
        protected Value run(@CheckForNull Value operand, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateNegation(operand, evaluationContext);
        }

    }

    /**
     * Parameterized test class for
     * {@link ExpressionEvaluation#evaluateStrictEquality(Value, Value, ExpressionEvaluation.StrictEquality)}.
     */
    @RunWith(Parameterized.class)
    public static class EvaluateStrictEqualityTest extends BaseEvaluateBinaryTest {

        private static final ExpressionEvaluation.StrictEquality EQUAL_TO = ExpressionEvaluation.StrictEquality.STRICTLY_EQUAL_TO;
        private static final ExpressionEvaluation.StrictEquality DIFFERENT_FROM = ExpressionEvaluation.StrictEquality.STRICTLY_DIFFERENT_FROM;

        private static final Object[][] TEST_DATA_VALUES = new Object[][] {

                // Equal to: undetermined and undetermined
                { null, null, EQUAL_TO, null, null },

                // Equal to: undetermined and unsigned integer
                { null, UINT_42, EQUAL_TO, null, null },

                // Equal to: undetermined and signed integer
                { null, SINT_42, EQUAL_TO, null, null },

                // Equal to: undetermined and float
                { null, FLOAT_3_25, EQUAL_TO, null, null },

                // Equal to: undetermined and string
                { null, STRING_A, EQUAL_TO, null, null },

                // Equal to: undetermined and function
                { null, UNDETERMINED_FUNCTION_A_VALUE, EQUAL_TO, null, null },

                // Equal to: unsigned integer and undetermined
                { UINT_42, null, EQUAL_TO, null, null },

                // Equal to: unsigned integer and unsigned integer (<)
                { UINT_4, UINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: unsigned integer and unsigned integer (===)
                { UINT_42, UINT_42, EQUAL_TO, UINT_1, null },

                // Equal to: unsigned integer and unsigned integer (>)
                { UINT_42, UINT_4, EQUAL_TO, UINT_0, null },

                // Equal to: unsigned integer and signed integer (<)
                { UINT_4, SINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: unsigned integer and signed integer (==)
                { UINT_42, SINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: unsigned integer and signed integer (>)
                { UINT_42, SINT_4, EQUAL_TO, UINT_0, null },

                // Equal to: huge unsigned integer and signed integer (>)
                { UINT_18000000000000000000, SINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: unsigned integer and negative signed integer (>)
                { UINT_42, SINT_MINUS_1, EQUAL_TO, UINT_0, null },

                // Equal to: unsigned integer and float (<)
                { UINT_4, FLOAT_42, EQUAL_TO, UINT_0, null },

                // Equal to: unsigned integer and float (==)
                { UINT_42, FLOAT_42, EQUAL_TO, UINT_0, null },

                // Equal to: unsigned integer and float (>)
                { UINT_42, FLOAT_3_25, EQUAL_TO, UINT_0, null },

                // Equal to: unsigned integer and string (<)
                { UINT_42, STRING_A, EQUAL_TO, UINT_0, null },

                // Equal to: unsigned integer and string (==)
                { UINT_42, STRING_42, EQUAL_TO, UINT_0, null },

                // Equal to: unsigned integer and string (>)
                { UINT_42, STRING_ASTERISK, EQUAL_TO, UINT_0, null },

                // Equal to: unsigned integer and function (always <)
                { UINT_42, UNDETERMINED_FUNCTION_A_VALUE, EQUAL_TO, UINT_0, null },

                // Equal to: signed integer and undetermined
                { SINT_42, null, EQUAL_TO, null, null },

                // Equal to: signed integer and unsigned integer (<)
                { SINT_4, UINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: negative signed integer and unsigned integer (<)
                { SINT_MINUS_1, UINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: signed integer and huge unsigned integer (<)
                { SINT_4, UINT_18000000000000000000, EQUAL_TO, UINT_0, null },

                // Equal to: signed integer and unsigned integer (==)
                { SINT_42, UINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: signed integer and unsigned integer (>)
                { SINT_42, UINT_4, EQUAL_TO, UINT_0, null },

                // Equal to: signed integer and signed integer (<)
                { SINT_4, SINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: signed integer and signed integer (===)
                { SINT_42, SINT_42, EQUAL_TO, UINT_1, null },

                // Equal to: signed integer and signed integer (>)
                { SINT_42, SINT_4, EQUAL_TO, UINT_0, null },

                // Equal to: signed integer and float (<)
                { SINT_4, FLOAT_42, EQUAL_TO, UINT_0, null },

                // Equal to: signed integer and float (==)
                { SINT_42, FLOAT_42, EQUAL_TO, UINT_0, null },

                // Equal to: signed integer and float (>)
                { SINT_42, FLOAT_3_25, EQUAL_TO, UINT_0, null },

                // Equal to: signed integer and string (<)
                { SINT_42, STRING_A, EQUAL_TO, UINT_0, null },

                // Equal to: signed integer and string (==)
                { SINT_42, STRING_42, EQUAL_TO, UINT_0, null },

                // Equal to: signed integer and string (>)
                { SINT_42, STRING_ASTERISK, EQUAL_TO, UINT_0, null },

                // Equal to: signed integer and function (always <)
                { SINT_42, UNDETERMINED_FUNCTION_A_VALUE, EQUAL_TO, UINT_0, null },

                // Equal to: float and undetermined
                { FLOAT_3_25, null, EQUAL_TO, null, null },

                // Equal to: float and unsigned integer (<)
                { FLOAT_3_25, UINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: float and unsigned integer (==)
                { FLOAT_42, UINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: float and unsigned integer (>)
                { FLOAT_45_25, UINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: float and signed integer (<)
                { FLOAT_3_25, SINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: float and signed integer (==)
                { FLOAT_42, SINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: float and signed integer (>)
                { FLOAT_45_25, SINT_4, EQUAL_TO, UINT_0, null },

                // Equal to: float and float (<)
                { FLOAT_0_5, FLOAT_3_25, EQUAL_TO, UINT_0, null },

                // Equal to: float and float (===)
                { FLOAT_3_25, FLOAT_3_25, EQUAL_TO, UINT_1, null },

                // Equal to: float and float (>)
                { FLOAT_3_25, FLOAT_0_5, EQUAL_TO, UINT_0, null },

                // Equal to: float and string (<)
                { FLOAT_3_25, STRING_A, EQUAL_TO, UINT_0, null },

                // Equal to: float and string (==)
                { FLOAT_3_25, STRING_3_25, EQUAL_TO, UINT_0, null },

                // Equal to: float and string (>)
                { FLOAT_3_25, STRING_ASTERISK, EQUAL_TO, UINT_0, null },

                // Equal to: float and function (always <)
                { FLOAT_3_25, UNDETERMINED_FUNCTION_A_VALUE, EQUAL_TO, UINT_0, null },

                // Equal to: string and undetermined
                { STRING_A, null, EQUAL_TO, null, null },

                // Equal to: string and unsigned integer (<)
                { STRING_ASTERISK, UINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: string and unsigned integer (==)
                { STRING_42, UINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: string and unsigned integer (>)
                { STRING_A, UINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: string and signed integer (<)
                { STRING_ASTERISK, SINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: string and signed integer (==)
                { STRING_42, SINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: string and signed integer (>)
                { STRING_A, SINT_4, EQUAL_TO, UINT_0, null },

                // Equal to: string and float (<)
                { STRING_ASTERISK, FLOAT_3_25, EQUAL_TO, UINT_0, null },

                // Equal to: string and float (==)
                { STRING_3_25, FLOAT_3_25, EQUAL_TO, UINT_0, null },

                // Equal to: string and float (>)
                { STRING_A, FLOAT_3_25, EQUAL_TO, UINT_0, null },

                // Equal to: string and string (<)
                { STRING_ASTERISK, STRING_A, EQUAL_TO, UINT_0, null },

                // Equal to: string and string (===)
                { STRING_A, STRING_A, EQUAL_TO, UINT_1, null },

                // Equal to: string and string (>)
                { STRING_AA, STRING_A, EQUAL_TO, UINT_0, null },

                // Equal to: string and function (always <)
                { STRING_A, UNDETERMINED_FUNCTION_A_VALUE, EQUAL_TO, UINT_0, null },

                // Equal to: function and undetermined (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, null, EQUAL_TO, null, null },

                // Equal to: function and unsigned integer (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, UINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: function and signed integer (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, SINT_42, EQUAL_TO, UINT_0, null },

                // Equal to: function and float (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, FLOAT_3_25, EQUAL_TO, UINT_0, null },

                // Equal to: function and string (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_A, EQUAL_TO, UINT_0, null },

                // Equal to: function and function (<)
                // NOTE: The order of the tests for comparison of function values is important: see the implementation of
                // evaluateComparison() for details. Luckily, the Parameterized test runner runs the tests in a predictable order.
                // Also note that these tests currently use null for the assembly.
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_B_VALUE, EQUAL_TO, UINT_0, null },

                // Equal to: function and function (===)
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_A_VALUE, EQUAL_TO, UINT_1, null },

                // Equal to: function and function (>)
                { UNDETERMINED_FUNCTION_B_VALUE, UNDETERMINED_FUNCTION_A_VALUE, EQUAL_TO, UINT_0, null },

                // Different from: undetermined and undetermined
                { null, null, DIFFERENT_FROM, null, null },

                // Different from: undetermined and unsigned integer
                { null, UINT_42, DIFFERENT_FROM, null, null },

                // Different from: undetermined and signed integer
                { null, SINT_42, DIFFERENT_FROM, null, null },

                // Different from: undetermined and float
                { null, FLOAT_3_25, DIFFERENT_FROM, null, null },

                // Different from: undetermined and string
                { null, STRING_A, DIFFERENT_FROM, null, null },

                // Different from: undetermined and function
                { null, UNDETERMINED_FUNCTION_A_VALUE, DIFFERENT_FROM, null, null },

                // Different from: unsigned integer and undetermined
                { UINT_42, null, DIFFERENT_FROM, null, null },

                // Different from: unsigned integer and unsigned integer (<)
                { UINT_4, UINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: unsigned integer and unsigned integer (===)
                { UINT_42, UINT_42, DIFFERENT_FROM, UINT_0, null },

                // Different from: unsigned integer and unsigned integer (>)
                { UINT_42, UINT_4, DIFFERENT_FROM, UINT_1, null },

                // Different from: unsigned integer and signed integer (<)
                { UINT_4, SINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: unsigned integer and signed integer (==)
                { UINT_42, SINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: unsigned integer and signed integer (>)
                { UINT_42, SINT_4, DIFFERENT_FROM, UINT_1, null },

                // Different from: huge unsigned integer and signed integer (>)
                { UINT_18000000000000000000, SINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: unsigned integer and negative signed integer (>)
                { UINT_42, SINT_MINUS_1, DIFFERENT_FROM, UINT_1, null },

                // Different from: unsigned integer and float (<)
                { UINT_4, FLOAT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: unsigned integer and float (==)
                { UINT_42, FLOAT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: unsigned integer and float (>)
                { UINT_42, FLOAT_3_25, DIFFERENT_FROM, UINT_1, null },

                // Different from: unsigned integer and string (<)
                { UINT_42, STRING_A, DIFFERENT_FROM, UINT_1, null },

                // Different from: unsigned integer and string (==)
                { UINT_42, STRING_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: unsigned integer and string (>)
                { UINT_42, STRING_ASTERISK, DIFFERENT_FROM, UINT_1, null },

                // Different from: unsigned integer and function (always <)
                { UINT_42, UNDETERMINED_FUNCTION_A_VALUE, DIFFERENT_FROM, UINT_1, null },

                // Different from: signed integer and undetermined
                { SINT_42, null, DIFFERENT_FROM, null, null },

                // Different from: signed integer and unsigned integer (<)
                { SINT_4, UINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: negative signed integer and unsigned integer (<)
                { SINT_MINUS_1, UINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: signed integer and huge unsigned integer (<)
                { SINT_4, UINT_18000000000000000000, DIFFERENT_FROM, UINT_1, null },

                // Different from: signed integer and unsigned integer (==)
                { SINT_42, UINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: signed integer and unsigned integer (>)
                { SINT_42, UINT_4, DIFFERENT_FROM, UINT_1, null },

                // Different from: signed integer and signed integer (<)
                { SINT_4, SINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: signed integer and signed integer (===)
                { SINT_42, SINT_42, DIFFERENT_FROM, UINT_0, null },

                // Different from: signed integer and signed integer (>)
                { SINT_42, SINT_4, DIFFERENT_FROM, UINT_1, null },

                // Different from: signed integer and float (<)
                { SINT_4, FLOAT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: signed integer and float (==)
                { SINT_42, FLOAT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: signed integer and float (>)
                { SINT_42, FLOAT_3_25, DIFFERENT_FROM, UINT_1, null },

                // Different from: signed integer and string (<)
                { SINT_42, STRING_A, DIFFERENT_FROM, UINT_1, null },

                // Different from: signed integer and string (==)
                { SINT_42, STRING_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: signed integer and string (>)
                { SINT_42, STRING_ASTERISK, DIFFERENT_FROM, UINT_1, null },

                // Different from: signed integer and function (always <)
                { SINT_42, UNDETERMINED_FUNCTION_A_VALUE, DIFFERENT_FROM, UINT_1, null },

                // Different from: float and undetermined
                { FLOAT_3_25, null, DIFFERENT_FROM, null, null },

                // Different from: float and unsigned integer (<)
                { FLOAT_3_25, UINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: float and unsigned integer (==)
                { FLOAT_42, UINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: float and unsigned integer (>)
                { FLOAT_45_25, UINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: float and signed integer (<)
                { FLOAT_3_25, SINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: float and signed integer (==)
                { FLOAT_42, SINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: float and signed integer (>)
                { FLOAT_45_25, SINT_4, DIFFERENT_FROM, UINT_1, null },

                // Different from: float and float (<)
                { FLOAT_0_5, FLOAT_3_25, DIFFERENT_FROM, UINT_1, null },

                // Different from: float and float (===)
                { FLOAT_3_25, FLOAT_3_25, DIFFERENT_FROM, UINT_0, null },

                // Different from: float and float (>)
                { FLOAT_3_25, FLOAT_0_5, DIFFERENT_FROM, UINT_1, null },

                // Different from: float and string (<)
                { FLOAT_3_25, STRING_A, DIFFERENT_FROM, UINT_1, null },

                // Different from: float and string (==)
                { FLOAT_3_25, STRING_3_25, DIFFERENT_FROM, UINT_1, null },

                // Different from: float and string (>)
                { FLOAT_3_25, STRING_ASTERISK, DIFFERENT_FROM, UINT_1, null },

                // Different from: float and function (always <)
                { FLOAT_3_25, UNDETERMINED_FUNCTION_A_VALUE, DIFFERENT_FROM, UINT_1, null },

                // Different from: string and undetermined
                { STRING_A, null, DIFFERENT_FROM, null, null },

                // Different from: string and unsigned integer (<)
                { STRING_ASTERISK, UINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: string and unsigned integer (==)
                { STRING_42, UINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: string and unsigned integer (>)
                { STRING_A, UINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: string and signed integer (<)
                { STRING_ASTERISK, SINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: string and signed integer (==)
                { STRING_42, SINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: string and signed integer (>)
                { STRING_A, SINT_4, DIFFERENT_FROM, UINT_1, null },

                // Different from: string and float (<)
                { STRING_ASTERISK, FLOAT_3_25, DIFFERENT_FROM, UINT_1, null },

                // Different from: string and float (==)
                { STRING_3_25, FLOAT_3_25, DIFFERENT_FROM, UINT_1, null },

                // Different from: string and float (>)
                { STRING_A, FLOAT_3_25, DIFFERENT_FROM, UINT_1, null },

                // Different from: string and string (<)
                { STRING_ASTERISK, STRING_A, DIFFERENT_FROM, UINT_1, null },

                // Different from: string and string (===)
                { STRING_A, STRING_A, DIFFERENT_FROM, UINT_0, null },

                // Different from: string and string (>)
                { STRING_AA, STRING_A, DIFFERENT_FROM, UINT_1, null },

                // Different from: string and function (always <)
                { STRING_A, UNDETERMINED_FUNCTION_A_VALUE, DIFFERENT_FROM, UINT_1, null },

                // Different from: function and undetermined (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, null, DIFFERENT_FROM, null, null },

                // Different from: function and unsigned integer (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, UINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: function and signed integer (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, SINT_42, DIFFERENT_FROM, UINT_1, null },

                // Different from: function and float (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, FLOAT_3_25, DIFFERENT_FROM, UINT_1, null },

                // Different from: function and string (always >)
                { UNDETERMINED_FUNCTION_A_VALUE, STRING_A, DIFFERENT_FROM, UINT_1, null },

                // Different from: function and function (<)
                // NOTE: The order of the tests for comparison of function values is important: see the implementation of
                // evaluateComparison() for details. Luckily, the Parameterized test runner runs the tests in a predictable order.
                // Also note that these tests currently use null for the assembly.
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_B_VALUE, DIFFERENT_FROM, UINT_1, null },

                // Different from: function and function (===)
                { UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_A_VALUE, DIFFERENT_FROM, UINT_0, null },

                // Different from: function and function (>)
                { UNDETERMINED_FUNCTION_B_VALUE, UNDETERMINED_FUNCTION_A_VALUE, DIFFERENT_FROM, UINT_1, null },

        };

        private static final List<Object[]> TEST_DATA = Arrays.asList(TEST_DATA_VALUES);

        @Nonnull
        @Parameters(name = "{0} {2} {1}")
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        @Nonnull
        private final ExpressionEvaluation.StrictEquality operator;

        public EvaluateStrictEqualityTest(@CheckForNull Value operand1, @CheckForNull Value operand2,
                @Nonnull ExpressionEvaluation.StrictEquality operator, @CheckForNull Value expectedResult,
                @CheckForNull AssemblyMessage expectedAssemblyMessage) {
            super(operand1, operand2, expectedResult, expectedAssemblyMessage);
            this.operator = operator;
        }

        @CheckForNull
        @Override
        protected Value run(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateStrictEquality(operand1, operand2, this.operator);
        }

    }

    /**
     * Parameterized test class for {@link ExpressionEvaluation#evaluateUnaryPlus(Value, EvaluationContext)}.
     */
    @RunWith(Parameterized.class)
    public static class EvaluateUnaryPlusTest extends BaseEvaluateUnaryTest {

        private static final List<Object[]> TEST_DATA = Arrays.asList(new Object[][] {

                // An undetermined value
                { null, null, null },

                // An unsigned integer
                { UINT_42, SINT_42, null },

                // A signed integer
                { SINT_42, SINT_42, null },

                // A floating-point number
                { FLOAT_3_25, FLOAT_3_25, null },

                // A string that can be parsed as a number
                { STRING_3_25, FLOAT_3_25, null },

                // A string that cannot be parsed as a number
                { STRING_A, null, CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE },

                // A function
                { UNDETERMINED_FUNCTION_A_VALUE, null, new FunctionOperandNotApplicableErrorMessage("unary +") },

        });

        @Nonnull
        @Parameters
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        public EvaluateUnaryPlusTest(@CheckForNull Value input, @CheckForNull Value expectedResult,
                @CheckForNull AssemblyMessage expectedAssemblyMessage) {
            super(input, expectedResult, expectedAssemblyMessage);
        }

        @CheckForNull
        @Override
        protected Value run(@CheckForNull Value operand, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateUnaryPlus(operand, evaluationContext);
        }

    }

    /**
     * Parameterized test class for {@link ExpressionEvaluation#parseFloat(String, EvaluationContext)}.
     */
    @RunWith(Parameterized.class)
    public static class ParseFloatTest {

        private static final List<Object[]> TEST_DATA = Arrays.asList(new Object[][] {
                // An empty string (invalid)
                { "", null },

                // A space (invalid)
                { " ", null },

                // The letter "a" (invalid)
                { "a", null },

                // A short integer
                { "1", 1. },

                // A long integer
                { "123456789", 123456789. },

                // An integer surrounded by spaces
                { " 1 ", 1. },

                // A plus sign (invalid)
                { "+", null },

                // A minus sign (invalid)
                { "-", null },

                // A plus sign followed by an integer
                { "+1", 1. },

                // A minus sign followed by an integer
                { "-1", -1. },

                // An integer followed by an 'A' (invalid)
                { "1A", null },

                // An integer followed by a point
                { "1.", 1. },

                // A point and a fractional digit
                { ".5", .5 },

                // An integer, a point and a fractional digit
                { "1.5", 1.5 },

                // An integer, a point and many fractional digits
                { "189.625", 189.625 },

                // An integer followed by an 'E' (invalid)
                { "1E", null },

                // An integer, an 'E' and an exponent
                { "1E3", 1000. },

                // An integer, an 'e' and an exponent
                { "1e3", 1000. },

                // An integer, an 'E', a '+' and an exponent
                { "1E+3", 1000. },

                // An integer, an 'E', a '-' and an exponent
                { "1E-3", 0.001 },

                // An integer, an 'E' and a larger exponent
                { "155E26", 155E26 },

                // An integer, a point, a fractional digit, an 'E' and an exponent
                { "1.5E3", 1500. },

                // An integer, a point, a fractional digit, an 'E' and an exponent, surrounded with spaces
                { " 1.5E3 ", 1500. } });

        @Nonnull
        @Parameters(name = "{0}")
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        @Nonnull
        private final String input;
        @CheckForNull
        private final Double expectedResult;

        public ParseFloatTest(@Nonnull String input, @CheckForNull Double expectedResult) {
            this.input = input;
            this.expectedResult = expectedResult;
        }

        /**
         * Asserts that {@link ExpressionEvaluation#parseFloat(String, EvaluationContext)} correctly parses floats and rejects
         * invalid values.
         */
        @Test
        public void test() {
            final ArrayList<AssemblyMessage> messages = new ArrayList<>();
            Double result = ExpressionEvaluation.parseFloat(this.input, new EvaluationContext(null, 0,
                    new AssemblyMessageCollector(messages)));
            assertThat(result, is(this.expectedResult));
            if (this.expectedResult == null) {
                assertThat(messages,
                        contains(new EquivalentAssemblyMessage(new CannotConvertStringToFloatErrorMessage(this.input))));
            } else {
                assertThat(messages, is(empty()));
            }
        }

    }

    /**
     * Parameterized test class for {@link ExpressionEvaluation#unsignedToFloat(long)}.
     */
    @RunWith(Parameterized.class)
    public static class UnsignedToFloatTest {

        private static final List<Object[]> TEST_DATA = Arrays.asList(new Object[][] {
                // Zero
                { 0, 0. },

                // One
                { 1, 1. },

                // The largest signed 64-bit integer (2^63 - 1, 9223372036854775807)
                { 0x7fffffffffffffffL, 9.223372036854776E18 },

                // 2^63, 9223372036854775808
                { 0x8000000000000000L, 9.223372036854776E18 },

                // The largest unsigned 64-bit integer (2^64 - 1, 18446744073709551615)
                { 0xffffffffffffffffL, 1.8446744073709552E19 },

                // 18000000000000000000
                { -446744073709551616L, 1.8E19 } });

        @Nonnull
        @Parameters
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        private final long input;
        private final double expectedResult;

        public UnsignedToFloatTest(long input, double expectedResult) {
            this.input = input;
            this.expectedResult = expectedResult;
        }

        /**
         * Asserts that {@link ExpressionEvaluation#unsignedToFloat(long)} correctly converts an unsigned integer to a float.
         */
        @Test
        public void test() {
            double result = ExpressionEvaluation.unsignedToFloat(this.input);
            assertThat(result, is(this.expectedResult));
        }

    }

    /**
     * Parameterized test class for {@link ExpressionEvaluation#valueToBoolean(Value)}.
     */
    @RunWith(Parameterized.class)
    public static class ValueToBooleanTest {

        private static final List<Object[]> TEST_DATA = Arrays.asList(new Object[][] {
                // An undetermined value
                { null, null },

                // The unsigned integer 0
                { UINT_0, false },

                // The unsigned integer 42
                { UINT_42, true },

                // The signed integer 0
                { SINT_0, false },

                // The signed integer 42
                { SINT_42, true },

                // The floating-point number 0
                { FLOAT_0, false },

                // The floating-point number 0.1
                { FLOAT_0_1, true },

                // The empty string
                { STRING_EMPTY, false },

                // A non-empty string
                { new StringValue("false"), true },

                // A function value
                { UNDETERMINED_FUNCTION_A_VALUE, true } });

        @Nonnull
        @Parameters
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        @CheckForNull
        private final Value input;
        @CheckForNull
        private final Boolean expectedResult;

        public ValueToBooleanTest(@CheckForNull Value input, @CheckForNull Boolean expectedResult) {
            this.input = input;
            this.expectedResult = expectedResult;
        }

        /**
         * Asserts that {@link ExpressionEvaluation#valueToBoolean(Value)} converts a {@link Value} to a {@link Boolean}, or to
         * <code>null</code> if the value is undetermined.
         */
        @Test
        public void test() {
            Boolean result = ExpressionEvaluation.valueToBoolean(this.input);
            assertThat(result, is(this.expectedResult));
        }

    }

    private static final class UndeterminedFunction implements Function {

        UndeterminedFunction() {
        }

        @Nonnull
        @Override
        public Expression call(@Nonnull Expression[] arguments, @Nonnull EvaluationContext evaluationContext) {
            return ValueExpression.UNDETERMINED;
        }

    }

    static final Function UNDETERMINED_FUNCTION_A = new UndeterminedFunction();
    static final Function UNDETERMINED_FUNCTION_B = new UndeterminedFunction();
    static final Function UNDETERMINED_FUNCTION_C = new UndeterminedFunction();
    static final UnsignedIntValue UINT_0 = new UnsignedIntValue(0);
    static final UnsignedIntValue UINT_1 = new UnsignedIntValue(1);
    static final UnsignedIntValue UINT_2 = new UnsignedIntValue(2);
    static final UnsignedIntValue UINT_4 = new UnsignedIntValue(4);
    static final UnsignedIntValue UINT_5 = new UnsignedIntValue(5);
    static final UnsignedIntValue UINT_10 = new UnsignedIntValue(10);
    static final UnsignedIntValue UINT_15 = new UnsignedIntValue(15);
    static final UnsignedIntValue UINT_37 = new UnsignedIntValue(37);
    static final UnsignedIntValue UINT_38 = new UnsignedIntValue(38);
    static final UnsignedIntValue UINT_41 = new UnsignedIntValue(41);
    static final UnsignedIntValue UINT_42 = new UnsignedIntValue(42);
    static final UnsignedIntValue UINT_43 = new UnsignedIntValue(43);
    static final UnsignedIntValue UINT_47 = new UnsignedIntValue(47);
    static final UnsignedIntValue UINT_84 = new UnsignedIntValue(84);
    static final UnsignedIntValue UINT_18000000000000000000 = new UnsignedIntValue(-446744073709551616L);
    static final UnsignedIntValue UINT_42_TIMES_42 = new UnsignedIntValue(42 * 42);
    static final UnsignedIntValue UINT_NOT_42 = new UnsignedIntValue(~42);
    static final UnsignedIntValue UINT_NOT_3 = new UnsignedIntValue(~3);
    static final UnsignedIntValue UINT_42_SHL_3 = new UnsignedIntValue(42 << 3);
    static final UnsignedIntValue UINT_42_SHL_10 = new UnsignedIntValue(42 << 10);
    static final SignedIntValue SINT_0 = new SignedIntValue(0);
    static final SignedIntValue SINT_1 = new SignedIntValue(1);
    static final SignedIntValue SINT_2 = new SignedIntValue(2);
    static final SignedIntValue SINT_4 = new SignedIntValue(4);
    static final SignedIntValue SINT_5 = new SignedIntValue(5);
    static final SignedIntValue SINT_10 = new SignedIntValue(10);
    static final SignedIntValue SINT_15 = new SignedIntValue(15);
    static final SignedIntValue SINT_38 = new SignedIntValue(38);
    static final SignedIntValue SINT_42 = new SignedIntValue(42);
    static final SignedIntValue SINT_84 = new SignedIntValue(84);
    static final SignedIntValue SINT_42_TIMES_42 = new SignedIntValue(42 * 42);
    static final SignedIntValue SINT_MINUS_42 = new SignedIntValue(-42);
    static final SignedIntValue SINT_MINUS_38 = new SignedIntValue(-38);
    static final SignedIntValue SINT_MINUS_3 = new SignedIntValue(-3);
    static final SignedIntValue SINT_MINUS_1 = new SignedIntValue(-1);
    static final SignedIntValue SINT_3_SHL_3 = new SignedIntValue(3 << 3);
    static final SignedIntValue SINT_3_SHL_10 = new SignedIntValue(3 << 10);
    static final SignedIntValue SINT_3_SHL_MINUS_1 = new SignedIntValue(3L << -1L);
    static final SignedIntValue SINT_42_SHL_3 = new SignedIntValue(42 << 3);
    static final SignedIntValue SINT_42_SHL_10 = new SignedIntValue(42 << 10);
    static final SignedIntValue SINT_42_SHL_MINUS_1 = new SignedIntValue(42L << -1L);
    static final SignedIntValue SINT_42_SHR_MINUS_1 = new SignedIntValue(42L >> -1L);
    static final SignedIntValue SINT_45_SHR_MINUS_1 = new SignedIntValue(45L >> -1L);
    static final FloatValue FLOAT_0 = new FloatValue(0.);
    static final FloatValue FLOAT_0_1 = new FloatValue(0.1);
    static final FloatValue FLOAT_0_25 = new FloatValue(0.25);
    static final FloatValue FLOAT_0_5 = new FloatValue(0.5);
    static final FloatValue FLOAT_1 = new FloatValue(1.);
    static final FloatValue FLOAT_2_25 = new FloatValue(2.25);
    static final FloatValue FLOAT_2_75 = new FloatValue(2.75);
    static final FloatValue FLOAT_3_25 = new FloatValue(3.25);
    static final FloatValue FLOAT_4_25 = new FloatValue(4.25);
    static final FloatValue FLOAT_6_5 = new FloatValue(6.5);
    static final FloatValue FLOAT_42 = new FloatValue(42.);
    static final FloatValue FLOAT_45_25 = new FloatValue(45.25);
    static final FloatValue FLOAT_MINUS_4_25 = new FloatValue(-4.25);
    static final FloatValue FLOAT_MINUS_3_25 = new FloatValue(-3.25);
    static final FloatValue FLOAT_42_TIMES_3_25 = new FloatValue(42 * 3.25);
    static final FloatValue FLOAT_18000000000000000000_TIMES_3_25 = new FloatValue(1.8E19 * 3.25);
    static final FloatValue FLOAT_3_25_TIMES_3_25 = new FloatValue(3.25 * 3.25);
    static final FloatValue FLOAT_42_DIVIDED_BY_3_25 = new FloatValue(42 / 3.25);
    static final FloatValue FLOAT_MINUS_1_DIVIDED_BY_3_25 = new FloatValue(-1 / 3.25);
    static final FloatValue FLOAT_18000000000000000000_DIVIDED_BY_3_25 = new FloatValue(1.8E19 / 3.25);
    static final FloatValue FLOAT_3_25_DIVIDED_BY_42 = new FloatValue(3.25 / 42);
    static final FloatValue FLOAT_3_25_DIVIDED_BY_18000000000000000000 = new FloatValue(3.25 / 1.8E19);
    static final FloatValue FLOAT_42_MODULUS_3_25 = new FloatValue(42 % 3.25);
    static final FloatValue FLOAT_MINUS_1_MODULUS_3_25 = new FloatValue(-1 % 3.25);
    static final FloatValue FLOAT_18000000000000000000_MODULUS_3_25 = new FloatValue(1.8E19 % 3.25);
    static final FloatValue FLOAT_3_25_MODULUS_42 = new FloatValue(3.25 % 42);
    static final FloatValue FLOAT_3_25_MODULUS_18000000000000000000 = new FloatValue(3.25 % 1.8E19);
    static final FloatValue FLOAT_42_MINUS_3_25 = new FloatValue(42 - 3.25);
    static final FloatValue FLOAT_18000000000000000000_MINUS_3_25 = new FloatValue(1.8E19 - 3.25);
    static final FloatValue FLOAT_3_25_MINUS_42 = new FloatValue(3.25 - 42);
    static final FloatValue FLOAT_MINUS_42 = new FloatValue(-42.);
    static final FloatValue FLOAT_3_25_MINUS_18000000000000000000 = new FloatValue(3.25 - 1.8E19);
    static final FloatValue FLOAT_18000000000000000000_PLUS_3_25 = new FloatValue(1.8E19 + 3.25);
    static final StringValue STRING_EMPTY = new StringValue("");
    static final StringValue STRING_0_5 = new StringValue("0.5");
    static final StringValue STRING_3_25 = new StringValue("3.25");
    static final StringValue STRING_3_253_25 = new StringValue("3.253.25");
    static final StringValue STRING_3_2542 = new StringValue("3.2542");
    static final StringValue STRING_3_25A = new StringValue("3.25a");
    static final StringValue STRING_42 = new StringValue("42");
    static final StringValue STRING_423_25 = new StringValue("423.25");
    static final StringValue STRING_42A = new StringValue("42a");
    static final StringValue STRING_45_25 = new StringValue("45.25");
    static final StringValue STRING_ASTERISK = new StringValue("*");
    static final StringValue STRING_A = new StringValue("a");
    static final StringValue STRING_A3_25 = new StringValue("a3.25");
    static final StringValue STRING_A42 = new StringValue("a42");
    static final StringValue STRING_AA = new StringValue("aa");
    static final FunctionValue UNDETERMINED_FUNCTION_A_VALUE = new FunctionValue(UNDETERMINED_FUNCTION_A);
    static final FunctionValue UNDETERMINED_FUNCTION_B_VALUE = new FunctionValue(UNDETERMINED_FUNCTION_B);
    static final FunctionValue UNDETERMINED_FUNCTION_C_VALUE = new FunctionValue(UNDETERMINED_FUNCTION_C);
    static final AssemblyMessage CANNOT_CONVERT_STRING_A_TO_FLOAT_ERROR_MESSAGE = new CannotConvertStringToFloatErrorMessage("a");

    /**
     * Asserts that
     * {@link ExpressionEvaluation#evaluateComparison(Value, Value, EvaluationContext, ExpressionEvaluation.Comparison)} behaves
     * correctly after a garbage collection.
     */
    @Test
    public void evaluateComparisonFunctionsGarbageCollection() {
        // This test method is only to get more code coverage. There are no useful asserts here.

        final ArrayList<AssemblyMessage> messages = new ArrayList<>();

        ExpressionEvaluation.evaluateComparison(new FunctionValue(new UndeterminedFunction()), new FunctionValue(
                new UndeterminedFunction()), new EvaluationContext(null, 0, new AssemblyMessageCollector(messages)),
                ExpressionEvaluation.Comparison.LESS_THAN);
        assertThat(messages, is(empty()));

        // Force a garbage collection to trigger the path where the referent of a WeakReference is null.
        System.gc();

        ExpressionEvaluation.evaluateComparison(UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_B_VALUE,
                new EvaluationContext(null, 0, new AssemblyMessageCollector(messages)), ExpressionEvaluation.Comparison.LESS_THAN);
        assertThat(messages, is(empty()));
    }

    /**
     * Asserts that
     * {@link ExpressionEvaluation#evaluateComparison(Value, Value, EvaluationContext, ExpressionEvaluation.Comparison)} behaves
     * correctly when processing a {@link FunctionValue} with a {@link Function} that was seen previously.
     */
    @Test
    public void evaluateComparisonFunctionsLoop() {
        final ArrayList<AssemblyMessage> messages = new ArrayList<>();

        Value result = ExpressionEvaluation.evaluateComparison(UNDETERMINED_FUNCTION_A_VALUE, UNDETERMINED_FUNCTION_B_VALUE,
                new EvaluationContext(null, 0, new AssemblyMessageCollector(messages)), ExpressionEvaluation.Comparison.LESS_THAN);
        assertThat(result, is((Value) UINT_1));
        assertThat(messages, is(empty()));

        ExpressionEvaluation.evaluateComparison(UNDETERMINED_FUNCTION_B_VALUE, UNDETERMINED_FUNCTION_C_VALUE,
                new EvaluationContext(null, 0, new AssemblyMessageCollector(messages)), ExpressionEvaluation.Comparison.LESS_THAN);
        assertThat(result, is((Value) UINT_1));
        assertThat(messages, is(empty()));
    }

}
