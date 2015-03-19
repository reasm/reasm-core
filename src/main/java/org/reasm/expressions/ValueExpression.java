package org.reasm.expressions;

import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Value;

/**
 * An expression that consists of a simple {@linkplain Value value}.
 *
 * @author Francis Gagné
 */
@Immutable
public final class ValueExpression extends ValueOrientedExpression {

    /** A {@link ValueExpression} whose {@linkplain #getValue() value} is undetermined (<code>null</code>). */
    public static final ValueExpression UNDETERMINED = new ValueExpression(null);

    @CheckForNull
    private final Value value;

    /**
     * Initializes a new ValueExpression.
     *
     * @param value
     *            the value of the expression
     */
    public ValueExpression(@CheckForNull Value value) {
        this.value = value;
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

        ValueExpression other = (ValueExpression) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }

        return true;
    }

    @Override
    public final Value evaluate(EvaluationContext evaluationContext) {
        return this.value;
    }

    /**
     * Gets the value of this expression.
     *
     * @return the value
     */
    @CheckForNull
    public final Value getValue() {
        return this.value;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(this.value);
        return result;
    }

    @Nonnull
    @Override
    public final String toString() {
        return "ValueExpression [value=" + this.value + "]";
    }

}
