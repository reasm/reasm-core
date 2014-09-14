package org.reasm.expressions;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Symbol;
import org.reasm.Value;
import org.reasm.ValueVisitor;

/**
 * An expression consisting of an identifier.
 *
 * @author Francis Gagné
 */
@Immutable
public final class IdentifierExpression extends Expression {

    @Nonnull
    private final String identifier;

    /**
     * Initializes a new IdentifierExpression.
     *
     * @param identifier
     *            the identifier
     */
    public IdentifierExpression(@Nonnull String identifier) {
        if (identifier == null) {
            throw new NullPointerException("identifier");
        }

        this.identifier = identifier;
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

        IdentifierExpression other = (IdentifierExpression) obj;
        if (!this.identifier.equals(other.identifier)) {
            return false;
        }

        return true;
    }

    @CheckForNull
    @Override
    public final Value evaluate(@Nonnull EvaluationContext evaluationContext) {
        final SymbolLookup symbolLookup = evaluationContext.getSymbolLookup();
        if (symbolLookup != null) {
            final Symbol symbol = symbolLookup.getSymbol(this.identifier);
            if (symbol != null) {
                final Object value = symbol.getValue();
                if (value instanceof Value) {
                    return (Value) value;
                }
            }
        }

        return null;
    }

    /**
     * Gets the identifier of this expression.
     *
     * @return the identifier
     */
    @Nonnull
    public final String getIdentifier() {
        return this.identifier;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.identifier.hashCode();
        return result;
    }

    @Nonnull
    @Override
    public final String toIdentifier(@Nonnull EvaluationContext evaluationContext, @Nonnull ValueVisitor<String> valueVisitor) {
        return this.identifier;
    }

    @Nonnull
    @Override
    public final String toString() {
        return "IdentifierExpression [identifier=" + this.identifier + "]";
    }

}
