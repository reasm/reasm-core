package org.reasm.expressions;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Value;

/**
 * An expression consisting of an {@linkplain UnaryOperator unary operator} and its only operand.
 *
 * @author Francis Gagné
 */
@Immutable
public final class UnaryOperatorExpression extends ValueOrientedExpression {

    @Nonnull
    private final UnaryOperator operator;
    @Nonnull
    private final Expression operand;

    /**
     * Initializes a new UnaryOperatorExpression.
     *
     * @param operator
     *            the operator
     * @param operand
     *            the operand
     */
    public UnaryOperatorExpression(@Nonnull UnaryOperator operator, @Nonnull Expression operand) {
        if (operator == null) {
            throw new NullPointerException("operator");
        }

        if (operand == null) {
            throw new NullPointerException("operand");
        }

        this.operator = operator;
        this.operand = operand;
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

        UnaryOperatorExpression other = (UnaryOperatorExpression) obj;
        if (this.operator != other.operator) {
            return false;
        }

        if (!this.operand.equals(other.operand)) {
            return false;
        }

        return true;
    }

    @Override
    public final Value evaluate(EvaluationContext evaluationContext) {
        return this.operator.apply(this.operand.evaluate(evaluationContext), evaluationContext);
    }

    /**
     * Gets the operand to the unary operator in this expression.
     *
     * @return the operand
     */
    @Nonnull
    public final Expression getOperand() {
        return this.operand;
    }

    /**
     * Gets the operator in this expression.
     *
     * @return the operator
     */
    @Nonnull
    public final UnaryOperator getOperator() {
        return this.operator;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.operator.hashCode();
        result = prime * result + this.operand.hashCode();
        return result;
    }

    @Nonnull
    @Override
    public final String toString() {
        return "UnaryOperatorExpression [operator=" + this.operator + ", operand=" + this.operand + "]";
    }

}
