package org.reasm.expressions;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Value;

/**
 * An unary operator, used in {@link UnaryOperatorExpression} objects.
 *
 * @author Francis Gagné
 */
@Immutable
public abstract class UnaryOperator {

    /** The unary plus operator (prefix +). */
    public static final UnaryOperator UNARY_PLUS = new UnaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateUnaryPlus(operand, evaluationContext);
        }

        @Nonnull
        @Override
        public String toString() {
            return "UNARY_PLUS";
        };
    };

    /** The negation operator (prefix -). */
    public static final UnaryOperator NEGATION = new UnaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateNegation(operand, evaluationContext);
        }

        @Nonnull
        @Override
        public String toString() {
            return "NEGATION";
        };
    };

    /** The bitwise not operator (prefix ~). */
    public static final UnaryOperator BITWISE_NOT = new UnaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateBitwiseNot(operand, evaluationContext);
        }

        @Nonnull
        @Override
        public String toString() {
            return "BITWISE_NOT";
        };
    };

    /** The logical not operator (prefix !). */
    public static final UnaryOperator LOGICAL_NOT = new UnaryOperator() {
        @CheckForNull
        @Override
        public Value apply(@CheckForNull Value operand, @Nonnull EvaluationContext evaluationContext) {
            return ExpressionEvaluation.evaluateLogicalNot(operand);
        }

        @Nonnull
        @Override
        public String toString() {
            return "LOGICAL_NOT";
        };
    };

    /**
     * Initializes a new UnaryOperator.
     */
    protected UnaryOperator() {
    }

    /**
     * Applies the specified operand to this operator.
     *
     * @param operand
     *            the only operand
     * @param evaluationContext
     *            the context in which the expression is evaluated
     * @return the result {@link Value}
     */
    @CheckForNull
    public abstract Value apply(Value operand, EvaluationContext evaluationContext);

}
