package org.reasm.expressions;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Value;

/**
 * A binary operator, used in {@link BinaryOperatorExpression} objects.
 *
 * @author Francis Gagné
 */
@Immutable
public abstract class BinaryOperator {

    /** The multiplication operator. */
    public static final BinaryOperator MULTIPLICATION = new BinaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateArithmetic(operand1, operand2, evaluationContext,
                    ExpressionEvaluation.Arithmetic.MULTIPLICATION);
        }

        @Nonnull
        @Override
        public String toString() {
            return "MULTIPLICATION";
        };
    };

    /** The division operator. */
    public static final BinaryOperator DIVISION = new BinaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateArithmetic(operand1, operand2, evaluationContext,
                    ExpressionEvaluation.Arithmetic.DIVISION);
        }

        @Nonnull
        @Override
        public String toString() {
            return "DIVISION";
        };
    };

    /** The modulus operator. */
    public static final BinaryOperator MODULUS = new BinaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateArithmetic(operand1, operand2, evaluationContext,
                    ExpressionEvaluation.Arithmetic.MODULUS);
        }

        @Nonnull
        @Override
        public String toString() {
            return "MODULUS";
        };
    };

    /** The addition operator. */
    public static final BinaryOperator ADDITION = new BinaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateAddition(operand1, operand2, evaluationContext);
        }

        @Nonnull
        @Override
        public String toString() {
            return "ADDITION";
        };
    };

    /** The subtraction operator. */
    public static final BinaryOperator SUBTRACTION = new BinaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateArithmetic(operand1, operand2, evaluationContext,
                    ExpressionEvaluation.Arithmetic.SUBTRACTION);
        }

        @Nonnull
        @Override
        public String toString() {
            return "SUBTRACTION";
        };
    };

    /** The bit shift left operator. */
    public static final BinaryOperator BIT_SHIFT_LEFT = new BinaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateBitShift(operand1, operand2, evaluationContext,
                    ExpressionEvaluation.BitShift.BIT_SHIFT_LEFT);
        }

        @Nonnull
        @Override
        public String toString() {
            return "BIT_SHIFT_LEFT";
        };
    };

    /** The bit shift right operator. */
    public static final BinaryOperator BIT_SHIFT_RIGHT = new BinaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateBitShift(operand1, operand2, evaluationContext,
                    ExpressionEvaluation.BitShift.BIT_SHIFT_RIGHT);
        }

        @Nonnull
        @Override
        public String toString() {
            return "BIT_SHIFT_RIGHT";
        };
    };

    /** The less than operator. */
    public static final BinaryOperator LESS_THAN = new BinaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateComparison(operand1, operand2, evaluationContext,
                    ExpressionEvaluation.Comparison.LESS_THAN);
        }

        @Nonnull
        @Override
        public String toString() {
            return "LESS_THAN";
        };
    };

    /** The less than or equal to operator. */
    public static final BinaryOperator LESS_THAN_OR_EQUAL_TO = new BinaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateComparison(operand1, operand2, evaluationContext,
                    ExpressionEvaluation.Comparison.LESS_THAN_OR_EQUAL_TO);
        }

        @Nonnull
        @Override
        public String toString() {
            return "LESS_THAN_OR_EQUAL_TO";
        };
    };

    /** The greater than operator. */
    public static final BinaryOperator GREATER_THAN = new BinaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateComparison(operand1, operand2, evaluationContext,
                    ExpressionEvaluation.Comparison.GREATER_THAN);
        }

        @Nonnull
        @Override
        public String toString() {
            return "GREATER_THAN";
        };
    };

    /** The greater than or equal to operator. */
    public static final BinaryOperator GREATER_THAN_OR_EQUAL_TO = new BinaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateComparison(operand1, operand2, evaluationContext,
                    ExpressionEvaluation.Comparison.GREATER_THAN_OR_EQUAL_TO);
        }

        @Nonnull
        @Override
        public String toString() {
            return "GREATER_THAN_OR_EQUAL_TO";
        };
    };

    /** The equal to operator. */
    public static final BinaryOperator EQUAL_TO = new BinaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateComparison(operand1, operand2, evaluationContext,
                    ExpressionEvaluation.Comparison.EQUAL_TO);
        }

        @Nonnull
        @Override
        public String toString() {
            return "EQUAL_TO";
        };
    };

    /** The different from operator. */
    public static final BinaryOperator DIFFERENT_FROM = new BinaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateComparison(operand1, operand2, evaluationContext,
                    ExpressionEvaluation.Comparison.DIFFERENT_FROM);
        }

        @Nonnull
        @Override
        public String toString() {
            return "DIFFERENT_FROM";
        };
    };

    /** The strictly equal to operator. */
    public static final BinaryOperator STRICTLY_EQUAL_TO = new BinaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateStrictEquality(operand1, operand2,
                    ExpressionEvaluation.StrictEquality.STRICTLY_EQUAL_TO);
        }

        @Nonnull
        @Override
        public String toString() {
            return "STRICTLY_EQUAL_TO";
        };
    };

    /** The strictly different from operator. */
    public static final BinaryOperator STRICTLY_DIFFERENT_FROM = new BinaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateStrictEquality(operand1, operand2,
                    ExpressionEvaluation.StrictEquality.STRICTLY_DIFFERENT_FROM);
        }

        @Nonnull
        @Override
        public String toString() {
            return "STRICTLY_DIFFERENT_FROM";
        };
    };

    /** The bitwise AND operator. */
    public static final BinaryOperator BITWISE_AND = new BinaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateBinaryBitwise(operand1, operand2, evaluationContext,
                    ExpressionEvaluation.BinaryBitwise.BITWISE_AND);
        }

        @Nonnull
        @Override
        public String toString() {
            return "BITWISE_AND";
        };
    };

    /** The bitwise XOR operator. */
    public static final BinaryOperator BITWISE_XOR = new BinaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateBinaryBitwise(operand1, operand2, evaluationContext,
                    ExpressionEvaluation.BinaryBitwise.BITWISE_XOR);
        }

        @Nonnull
        @Override
        public String toString() {
            return "BITWISE_XOR";
        };
    };

    /** The bitwise OR operator. */
    public static final BinaryOperator BITWISE_OR = new BinaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateBinaryBitwise(operand1, operand2, evaluationContext,
                    ExpressionEvaluation.BinaryBitwise.BITWISE_OR);
        }

        @Nonnull
        @Override
        public String toString() {
            return "BITWISE_OR";
        };
    };

    /** The logical AND operator. */
    public static final BinaryOperator LOGICAL_AND = new BinaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateBinaryLogical(operand1, operand2, ExpressionEvaluation.BinaryLogical.LOGICAL_AND);
        }

        @Nonnull
        @Override
        public String toString() {
            return "LOGICAL_AND";
        };
    };

    /** The logical OR operator. */
    public static final BinaryOperator LOGICAL_OR = new BinaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand1, @CheckForNull Value operand2, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateBinaryLogical(operand1, operand2, ExpressionEvaluation.BinaryLogical.LOGICAL_OR);
        }

        @Nonnull
        @Override
        public String toString() {
            return "LOGICAL_OR";
        };
    };

    /**
     * Initializes a new BinaryOperator.
     */
    protected BinaryOperator() {
    }

    /**
     * Applies the specifies operands to this operator.
     *
     * @param operand1
     *            the first operand
     * @param operand2
     *            the second operand
     * @param evaluationContext
     *            the context in which the expression is evaluated
     * @return the result {@link Value}
     */
    @CheckForNull
    public abstract Value apply(@CheckForNull Value operand1, @CheckForNull Value operand2,
            @Nonnull EvaluationContext evaluationContext);

}
