package org.reasm.expressions;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Value;

/**
 * An expression consisting of a condition operand and two operands that are candidates for the result.
 *
 * @author Francis Gagné
 */
@Immutable
public final class ConditionalExpression extends ExpressionOrientedExpression {

    @Nonnull
    private final Expression condition, truePart, falsePart;

    /**
     * Initializes a new ConditionalExpression.
     *
     * @param condition
     *            the condition expression
     * @param truePart
     *            the expression to return when the condition is true
     * @param falsePart
     *            the expression to return when the condition is false
     */
    public ConditionalExpression(@Nonnull Expression condition, @Nonnull Expression truePart, @Nonnull Expression falsePart) {
        if (condition == null) {
            throw new NullPointerException("condition");
        }

        if (truePart == null) {
            throw new NullPointerException("truePart");
        }

        if (falsePart == null) {
            throw new NullPointerException("falsePart");
        }

        this.condition = condition;
        this.truePart = truePart;
        this.falsePart = falsePart;
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

        ConditionalExpression other = (ConditionalExpression) obj;
        if (!this.condition.equals(other.condition)) {
            return false;
        }

        if (!this.truePart.equals(other.truePart)) {
            return false;
        }

        if (!this.falsePart.equals(other.falsePart)) {
            return false;
        }

        return true;
    }

    /**
     * Gets the condition expression in this conditional expression.
     *
     * @return the condition expression
     */
    @Nonnull
    public final Expression getCondition() {
        return this.condition;
    }

    /**
     * Gets the expression to return when the condition is false in this conditional expression.
     *
     * @return the false part
     */
    @Nonnull
    public final Expression getFalsePart() {
        return this.falsePart;
    }

    /**
     * Gets the expression to return when the condition is true in this conditional expression.
     *
     * @return the true part
     */
    @Nonnull
    public final Expression getTruePart() {
        return this.truePart;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.condition.hashCode();
        result = prime * result + this.truePart.hashCode();
        result = prime * result + this.falsePart.hashCode();
        return result;
    }

    @Nonnull
    @Override
    public final String toString() {
        return "ConditionalExpression [condition=" + this.condition + ", truePart=" + this.truePart + ", falsePart="
                + this.falsePart + "]";
    }

    @Override
    @Nonnull
    protected final Expression simplify(@Nonnull EvaluationContext evaluationContext) {
        final Value condition = this.condition.evaluate(evaluationContext);

        final Boolean conditionValue = ExpressionEvaluation.valueToBoolean(condition);

        if (conditionValue == null) {
            return ValueExpression.UNDETERMINED;
        }

        return conditionValue.booleanValue() ? this.truePart : this.falsePart;
    }

}
