package org.reasm.expressions;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Value;

/**
 * An expression consisting of a {@linkplain BinaryOperator binary operator} and its two operands.
 *
 * @author Francis Gagné
 */
@Immutable
public final class BinaryOperatorExpression extends ValueOrientedExpression {

    @Nonnull
    private final BinaryOperator operator;
    @Nonnull
    private final Expression operand1, operand2;

    /**
     * Initializes a new BinaryOperatorExpression.
     *
     * @param operator
     *            the operator
     * @param operand1
     *            the first operand
     * @param operand2
     *            the second operand
     */
    public BinaryOperatorExpression(@Nonnull BinaryOperator operator, @Nonnull Expression operand1, @Nonnull Expression operand2) {
        if (operator == null) {
            throw new NullPointerException("operator");
        }

        if (operand1 == null) {
            throw new NullPointerException("operand1");
        }

        if (operand2 == null) {
            throw new NullPointerException("operand2");
        }

        this.operator = operator;
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    @Override
    public final boolean equals(@CheckForNull Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        BinaryOperatorExpression other = (BinaryOperatorExpression) obj;
        if (this.operator != other.operator) {
            return false;
        }

        if (!this.operand1.equals(other.operand1)) {
            return false;
        }

        if (!this.operand2.equals(other.operand2)) {
            return false;
        }

        return true;
    }

    @Override
    public final Value evaluate(EvaluationContext evaluationContext) {
        return this.operator.apply(this.operand1.evaluate(evaluationContext), this.operand2.evaluate(evaluationContext),
                evaluationContext);
    }

    /**
     * Gets the first operand to the binary operator in this expression.
     *
     * @return the first operand
     */
    @Nonnull
    public final Expression getOperand1() {
        return this.operand1;
    }

    /**
     * Gets the second operand to the binary operator in this expression.
     *
     * @return the second operand
     */
    @Nonnull
    public final Expression getOperand2() {
        return this.operand2;
    }

    /**
     * Gets the operator in this expression.
     *
     * @return the operator
     */
    @Nonnull
    public final BinaryOperator getOperator() {
        return this.operator;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.operator.hashCode();
        result = prime * result + this.operand1.hashCode();
        result = prime * result + this.operand2.hashCode();
        return result;
    }

    @Nonnull
    @Override
    public final String toString() {
        return "BinaryOperatorExpression [operator=" + this.operator + ", operand1=" + this.operand1 + ", operand2="
                + this.operand2 + "]";
    }

}
