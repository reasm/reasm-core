package org.reasm.expressions;

import java.util.Objects;

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
    @CheckForNull
    private final SymbolLookup fallbackSymbolLookup;

    /**
     * Initializes a new PeriodExpression.
     *
     * @param leftExpression
     *            the expression on the left of the period operator
     * @param rightExpression
     *            the expression on the right of the period operator
     * @param fallbackSymbolLookup
     *            an object that looks up symbols by name, which will be used to look up the symbol for the constructed
     *            {@link IdentifierExpression} when the identifier is {@linkplain #simplify(EvaluationContext) simplified} if the
     *            <code>leftExpression</code> does not {@linkplain #simplify(EvaluationContext) simplify} to an
     *            {@link IdentifierExpression}, or <code>null</code> to consider the symbol undefined
     */
    public PeriodExpression(@Nonnull Expression leftExpression, @Nonnull Expression rightExpression,
            @CheckForNull SymbolLookup fallbackSymbolLookup) {
        if (leftExpression == null) {
            throw new NullPointerException("leftExpression");
        }

        if (rightExpression == null) {
            throw new NullPointerException("rightExpression");
        }

        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
        this.fallbackSymbolLookup = fallbackSymbolLookup;
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

        final PeriodExpression other = (PeriodExpression) obj;
        if (!this.leftExpression.equals(other.leftExpression)) {
            return false;
        }

        if (!this.rightExpression.equals(other.rightExpression)) {
            return false;
        }

        if (!Objects.equals(this.fallbackSymbolLookup, other.fallbackSymbolLookup)) {
            return false;
        }

        return true;
    }

    /**
     * Gets the {@link SymbolLookup} that will look up the symbol for the constructed {@link IdentifierExpression} when the
     * identifier is {@linkplain #simplify(EvaluationContext) simplified} if the <code>leftExpression</code> does not
     * {@linkplain #simplify(EvaluationContext) simplify} to an {@link IdentifierExpression}
     *
     * @return the {@link SymbolLookup}
     */
    @CheckForNull
    public final SymbolLookup getFallbackSymbolLookup() {
        return this.fallbackSymbolLookup;
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
        result = prime * result + Objects.hashCode(this.fallbackSymbolLookup);
        return result;
    }

    @Nonnull
    @Override
    public final String toString() {
        return "PeriodExpression [leftExpression=" + this.leftExpression + ", rightExpression=" + this.rightExpression
                + ", fallbackSymbolLookup=" + this.fallbackSymbolLookup + "]";
    }

    @Nonnull
    @Override
    protected final Expression simplify(@Nonnull EvaluationContext evaluationContext) {
        final ValueVisitor<String> valueVisitor = new ValueToStringVisitor(evaluationContext, ".");

        final IdentifierExpression left = this.leftExpression.toIdentifier(evaluationContext, valueVisitor);
        final IdentifierExpression right = this.rightExpression.toIdentifier(evaluationContext, valueVisitor);

        if (left == null || right == null) {
            return ValueExpression.UNDETERMINED;
        }

        final String identifier = left.getIdentifier() + "." + right.getIdentifier();
        SymbolLookup symbolLookup = left.getSymbolLookup();
        if (symbolLookup == null) {
            symbolLookup = this.fallbackSymbolLookup;
        }

        return new IdentifierExpression(identifier, symbolLookup);
    }

}
