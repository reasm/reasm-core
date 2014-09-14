package org.reasm.expressions;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * An expression consisting of a child expression.
 *
 * @author Francis Gagné
 */
@Immutable
public final class GroupingExpression extends ExpressionOrientedExpression {

    @Nonnull
    private final Expression childExpression;

    /**
     * Initializes a new GroupingExpression.
     *
     * @param childExpression
     *            the expression between the grouping parentheses
     */
    public GroupingExpression(@Nonnull Expression childExpression) {
        if (childExpression == null) {
            throw new NullPointerException("childExpression");
        }

        this.childExpression = childExpression;
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

        GroupingExpression other = (GroupingExpression) obj;
        if (!this.childExpression.equals(other.childExpression)) {
            return false;
        }

        return true;
    }

    /**
     * Gets the expression between the grouping parentheses in this expression.
     *
     * @return the child expression
     */
    @Nonnull
    public final Expression getChildExpression() {
        return this.childExpression;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.childExpression.hashCode();
        return result;
    }

    @Nonnull
    @Override
    public final String toString() {
        return "GroupingExpression [childExpression=" + this.childExpression + "]";
    }

    @Nonnull
    @Override
    protected final Expression simplify(@Nonnull EvaluationContext evaluationContext) {
        return this.childExpression;
    }

}
