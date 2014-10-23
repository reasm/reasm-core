package org.reasm.expressions;

import java.util.Objects;

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
    @CheckForNull
    private final SymbolLookup symbolLookup;

    /**
     * Initializes a new IdentifierExpression.
     *
     * @param identifier
     *            the identifier
     * @param symbolLookup
     *            an object that looks up symbols by name, which will be used to look up the symbol for this identifier when the
     *            identifier is {@linkplain #evaluate(EvaluationContext) evaluated}, or <code>null</code> to consider the symbol
     *            undefined
     */
    public IdentifierExpression(@Nonnull String identifier, @CheckForNull SymbolLookup symbolLookup) {
        if (identifier == null) {
            throw new NullPointerException("identifier");
        }

        this.identifier = identifier;
        this.symbolLookup = symbolLookup;
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

        final IdentifierExpression other = (IdentifierExpression) obj;
        if (!this.identifier.equals(other.identifier)) {
            return false;
        }

        if (!Objects.equals(this.symbolLookup, other.symbolLookup)) {
            return false;
        }

        return true;
    }

    @Override
    public final Value evaluate(EvaluationContext evaluationContext) {
        if (this.symbolLookup != null) {
            final Symbol symbol = this.symbolLookup.getSymbol(this.identifier);
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

    /**
     * Gets the {@link SymbolLookup} that will look up the symbol for this identifier when the expression is
     * {@linkplain #evaluate(EvaluationContext) evaluated}.
     *
     * @return the {@link SymbolLookup}
     */
    @CheckForNull
    public final SymbolLookup getSymbolLookup() {
        return this.symbolLookup;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.identifier.hashCode();
        result = prime * result + Objects.hashCode(this.symbolLookup);
        return result;
    }

    @Nonnull
    @Override
    public final IdentifierExpression toIdentifier(EvaluationContext evaluationContext, ValueVisitor<String> valueVisitor) {
        return this;
    }

    @Nonnull
    @Override
    public final String toString() {
        return "IdentifierExpression [identifier=" + this.identifier + ", symbolLookup=" + this.symbolLookup + "]";
    }

}
