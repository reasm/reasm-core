package org.reasm.expressions;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.reasm.UnsignedIntValue;
import org.reasm.Value;

/**
 * Test class for the enum values of {@link BinaryOperator}.
 *
 * @author Francis Gagné
 */
public class BinaryOperatorTest {

    private static final UnsignedIntValue UINT_42 = new UnsignedIntValue(42);

    private static final EvaluationContext EMPTY_EVALUATION_CONTEXT = new EvaluationContext(null, 0, null, null);

    /**
     * Asserts that {@link BinaryOperator#apply(Value, Value, EvaluationContext)} on {@link BinaryOperator#ADDITION} performs an
     * addition.
     */
    @Test
    public void addition() {
        final Value result = BinaryOperator.ADDITION.apply(UINT_42, UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) new UnsignedIntValue(42 + 42)));
    }

    /**
     * Asserts that {@link BinaryOperator#apply(Value, Value, EvaluationContext)} on {@link BinaryOperator#BIT_SHIFT_LEFT} performs
     * a bit shift left operation.
     */
    @Test
    public void bitShiftLeft() {
        final Value result = BinaryOperator.BIT_SHIFT_LEFT.apply(UINT_42, UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) new UnsignedIntValue(42L << 42L)));
    }

    /**
     * Asserts that {@link BinaryOperator#apply(Value, Value, EvaluationContext)} on {@link BinaryOperator#BIT_SHIFT_RIGHT} performs
     * a bit shift right operation.
     */
    @Test
    public void bitShiftRight() {
        final Value result = BinaryOperator.BIT_SHIFT_RIGHT.apply(UINT_42, UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) new UnsignedIntValue(42L >> 42L)));
    }

    /**
     * Asserts that {@link BinaryOperator#apply(Value, Value, EvaluationContext)} on {@link BinaryOperator#BITWISE_AND} performs a
     * bitwise AND operation.
     */
    @Test
    public void bitwiseAnd() {
        final Value result = BinaryOperator.BITWISE_AND.apply(UINT_42, UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) new UnsignedIntValue(42 & 42)));
    }

    /**
     * Asserts that {@link BinaryOperator#apply(Value, Value, EvaluationContext)} on {@link BinaryOperator#BITWISE_OR} performs a
     * bitwise OR operation.
     */
    @Test
    public void bitwiseOr() {
        final Value result = BinaryOperator.BITWISE_OR.apply(UINT_42, UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) new UnsignedIntValue(42 | 42)));
    }

    /**
     * Asserts that {@link BinaryOperator#apply(Value, Value, EvaluationContext)} on {@link BinaryOperator#BITWISE_XOR} performs a
     * bitwise XOR operation.
     */
    @Test
    public void bitwiseXor() {
        final Value result = BinaryOperator.BITWISE_XOR.apply(UINT_42, UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) new UnsignedIntValue(42 ^ 42)));
    }

    /**
     * Asserts that {@link BinaryOperator#apply(Value, Value, EvaluationContext)} on {@link BinaryOperator#DIFFERENT_FROM} performs
     * a "different from" comparison.
     */
    @Test
    public void differentFrom() {
        final Value result = BinaryOperator.DIFFERENT_FROM.apply(UINT_42, UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) new UnsignedIntValue(0)));
    }

    /**
     * Asserts that {@link BinaryOperator#apply(Value, Value, EvaluationContext)} on {@link BinaryOperator#DIVISION} performs a
     * division.
     */
    @Test
    public void division() {
        final Value result = BinaryOperator.DIVISION.apply(UINT_42, UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) new UnsignedIntValue(42 / 42)));
    }

    /**
     * Asserts that {@link BinaryOperator#apply(Value, Value, EvaluationContext)} on {@link BinaryOperator#EQUAL_TO} performs an
     * "equal to" comparison.
     */
    @Test
    public void equalTo() {
        final Value result = BinaryOperator.EQUAL_TO.apply(UINT_42, UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) new UnsignedIntValue(1)));
    }

    /**
     * Asserts that {@link BinaryOperator#apply(Value, Value, EvaluationContext)} on {@link BinaryOperator#GREATER_THAN} performs a
     * "greater than" comparison.
     */
    @Test
    public void greaterThan() {
        final Value result = BinaryOperator.GREATER_THAN.apply(UINT_42, UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) new UnsignedIntValue(0)));
    }

    /**
     * Asserts that {@link BinaryOperator#apply(Value, Value, EvaluationContext)} on {@link BinaryOperator#GREATER_THAN_OR_EQUAL_TO}
     * performs a "greater than or equal to" comparison.
     */
    @Test
    public void greaterThanOrEqualTo() {
        final Value result = BinaryOperator.GREATER_THAN_OR_EQUAL_TO.apply(UINT_42, UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) new UnsignedIntValue(1)));
    }

    /**
     * Asserts that {@link BinaryOperator#apply(Value, Value, EvaluationContext)} on {@link BinaryOperator#LESS_THAN} performs a
     * "less than" comparison.
     */
    @Test
    public void lessThan() {
        final Value result = BinaryOperator.LESS_THAN.apply(UINT_42, UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) new UnsignedIntValue(0)));
    }

    /**
     * Asserts that {@link BinaryOperator#apply(Value, Value, EvaluationContext)} on {@link BinaryOperator#LESS_THAN_OR_EQUAL_TO}
     * performs a "less than or equal to" comparison.
     */
    @Test
    public void lessThanOrEqualTo() {
        final Value result = BinaryOperator.LESS_THAN_OR_EQUAL_TO.apply(UINT_42, UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) new UnsignedIntValue(1)));
    }

    /**
     * Asserts that {@link BinaryOperator#apply(Value, Value, EvaluationContext)} on {@link BinaryOperator#LOGICAL_AND} performs a
     * logical AND operation.
     */
    @Test
    public void logicalAnd() {
        final Value result = BinaryOperator.LOGICAL_AND.apply(UINT_42, UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) UINT_42));
    }

    /**
     * Asserts that {@link BinaryOperator#apply(Value, Value, EvaluationContext)} on {@link BinaryOperator#LOGICAL_OR} performs a
     * logical OR operation.
     */
    @Test
    public void logicalOr() {
        final Value result = BinaryOperator.LOGICAL_OR.apply(UINT_42, UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) UINT_42));
    }

    /**
     * Asserts that {@link BinaryOperator#apply(Value, Value, EvaluationContext)} on {@link BinaryOperator#MODULUS} performs a
     * modulus operation.
     */
    @Test
    public void modulus() {
        final Value result = BinaryOperator.MODULUS.apply(UINT_42, UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) new UnsignedIntValue(42 % 42)));
    }

    /**
     * Asserts that {@link BinaryOperator#apply(Value, Value, EvaluationContext)} on {@link BinaryOperator#MULTIPLICATION} performs
     * a multiplication.
     */
    @Test
    public void multiplication() {
        final Value result = BinaryOperator.MULTIPLICATION.apply(UINT_42, UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) new UnsignedIntValue(42 * 42)));
    }

    /**
     * Asserts that {@link BinaryOperator#apply(Value, Value, EvaluationContext)} on {@link BinaryOperator#STRICTLY_DIFFERENT_FROM}
     * performs a "strictly different from" comparison.
     */
    @Test
    public void strictlyDifferentFrom() {
        final Value result = BinaryOperator.STRICTLY_DIFFERENT_FROM.apply(UINT_42, UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) new UnsignedIntValue(0)));
    }

    /**
     * Asserts that {@link BinaryOperator#apply(Value, Value, EvaluationContext)} on {@link BinaryOperator#STRICTLY_EQUAL_TO}
     * performs a "strictly equal to" comparison.
     */
    @Test
    public void strictlyEqualTo() {
        final Value result = BinaryOperator.STRICTLY_EQUAL_TO.apply(UINT_42, UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) new UnsignedIntValue(1)));
    }

    /**
     * Asserts that {@link BinaryOperator#apply(Value, Value, EvaluationContext)} on {@link BinaryOperator#SUBTRACTION} performs a
     * subtraction.
     */
    @Test
    public void subtraction() {
        final Value result = BinaryOperator.SUBTRACTION.apply(UINT_42, UINT_42, EMPTY_EVALUATION_CONTEXT);
        assertThat(result, is((Value) new UnsignedIntValue(42 - 42)));
    }

}
