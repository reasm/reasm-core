package org.reasm.expressions;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.ValueVisitor;

/**
 * An expression consisting of the two operands of the period operator.
 *
 * @author Francis Gagné
 */
@Immutable
public final class PeriodExpression extends ExpressionOrientedExpression {

    @Nonnull
    private final Expression leftExpression;
    @Nonnull
    private final Expression rightExpression;

    /**
     * Initializes a new PeriodExpression.
     *
     * @param leftExpression
     *            the expression on the left of the period operator
     * @param rightExpression
     *            the expression on the right of the period operator
     */
    public PeriodExpression(@Nonnull Expression leftExpression, @Nonnull Expression rightExpression) {
        if (leftExpression == null) {
            throw new NullPointerException("leftExpression");
        }

        if (rightExpression == null) {
            throw new NullPointerException("rightExpression");
        }

        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
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

        PeriodExpression other = (PeriodExpression) obj;
        if (!this.leftExpression.equals(other.leftExpression)) {
            return false;
        }

        if (!this.rightExpression.equals(other.rightExpression)) {
            return false;
        }

        return true;
    }

    /**
     * Gets the expression on the left of the period operator in this expression.
     *
     * @return the left expression
     */
    @Nonnull
    public final Expression getLeftExpression() {
        return this.leftExpression;
    }

    /**
     * Gets the expression on the right of the period operator in this expression.
     *
     * @return the right expression
     */
    @Nonnull
    public final Expression getRightExpression() {
        return this.rightExpression;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.leftExpression.hashCode();
        result = prime * result + this.rightExpression.hashCode();
        return result;
    }

    @Nonnull
    @Override
    public final String toString() {
        return "PeriodExpression [leftExpression=" + this.leftExpression + ", rightExpression=" + this.rightExpression + "]";
    }

    @Nonnull
    @Override
    protected final Expression simplify(@Nonnull EvaluationContext evaluationContext) {
        final ValueVisitor<String> valueVisitor = new ValueToStringVisitor(evaluationContext, ".");

        final String left = this.leftExpression.toIdentifier(evaluationContext, valueVisitor);
        final String right = this.rightExpression.toIdentifier(evaluationContext, valueVisitor);

        if (left == null || right == null) {
            return ValueExpression.UNDETERMINED;
        }

        return new IdentifierExpression(left + "." + right);
    }

}
