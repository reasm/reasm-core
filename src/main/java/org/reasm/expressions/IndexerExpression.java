package org.reasm.expressions;

import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Value;
import org.reasm.ValueVisitor;

/**
 * An expression consisting of a subject operand and an index operand.
 *
 * @author Francis Gagné
 */
@Immutable
public final class IndexerExpression extends ExpressionOrientedExpression {

    @Nonnull
    private final Expression subjectExpression;
    @Nonnull
    private final Expression indexExpression;
    @CheckForNull
    private final SymbolLookup fallbackSymbolLookup;

    /**
     * Initializes a new IndexerExpression.
     *
     * @param subjectExpression
     *            the subject to index
     * @param indexExpression
     *            the index to apply to the subject
     * @param fallbackSymbolLookup
     *            an object that looks up symbols by name, which will be used to look up the symbol for the constructed
     *            {@link IdentifierExpression} when the identifier is {@linkplain #simplify(EvaluationContext) simplified} if the
     *            <code>subjectExpression</code> does not {@linkplain #simplify(EvaluationContext) simplify} to an
     *            {@link IdentifierExpression}, or <code>null</code> to consider the symbol undefined
     */
    public IndexerExpression(@Nonnull Expression subjectExpression, @Nonnull Expression indexExpression,
            @CheckForNull SymbolLookup fallbackSymbolLookup) {
        if (subjectExpression == null) {
            throw new NullPointerException("subjectExpression");
        }

        if (indexExpression == null) {
            throw new NullPointerException("indexExpression");
        }

        this.subjectExpression = subjectExpression;
        this.indexExpression = indexExpression;
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

        final IndexerExpression other = (IndexerExpression) obj;
        if (!this.indexExpression.equals(other.indexExpression)) {
            return false;
        }

        if (!this.subjectExpression.equals(other.subjectExpression)) {
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
     * Gets the index of this indexer expression.
     *
     * @return the index
     */
    @Nonnull
    public final Expression getIndexExpression() {
        return this.indexExpression;
    }

    /**
     * Gets the subject of this indexer expression.
     *
     * @return the subject
     */
    @Nonnull
    public final Expression getSubjectExpression() {
        return this.subjectExpression;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.indexExpression.hashCode();
        result = prime * result + this.subjectExpression.hashCode();
        result = prime * result + Objects.hashCode(this.fallbackSymbolLookup);
        return result;
    }

    @Nonnull
    @Override
    public final String toString() {
        return "IndexerExpression [subjectExpression=" + this.subjectExpression + ", indexExpression=" + this.indexExpression
                + ", fallbackSymbolLookup=" + this.fallbackSymbolLookup + "]";
    }

    @Nonnull
    @Override
    protected final Expression simplify(@Nonnull EvaluationContext evaluationContext) {
        final ValueVisitor<String> valueVisitor = new ValueToStringVisitor(evaluationContext, "[]");

        final IdentifierExpression subject = this.subjectExpression.toIdentifier(evaluationContext, valueVisitor);
        final String index = Value.accept(this.indexExpression.evaluate(evaluationContext), valueVisitor);

        if (subject == null || index == null) {
            return ValueExpression.UNDETERMINED;
        }

        final String identifier = subject.getIdentifier() + "[" + index + "]";
        SymbolLookup symbolLookup = subject.getSymbolLookup();
        if (symbolLookup == null) {
            symbolLookup = this.fallbackSymbolLookup;
        }

        return new IdentifierExpression(identifier, symbolLookup);
    }

}
