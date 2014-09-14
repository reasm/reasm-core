package org.reasm;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * A value. An expression produces a value when it is evaluated. Values are immutable.
 *
 * @author Francis Gagné
 */
@Immutable
public abstract class Value {

    /**
     * Calls the method in the specified visitor corresponding to the actual type of the specified value, or
     * {@link ValueVisitor#visitUndetermined()} if the value is <code>null</code>.
     *
     * @param value
     *            the value to visit
     * @param visitor
     *            the visitor
     * @return the visitor's result
     */
    public static <T> T accept(@CheckForNull Value value, @Nonnull ValueVisitor<T> visitor) {
        if (value == null) {
            return visitor.visitUndetermined();
        }

        return value.accept(visitor);
    }

    /**
     * Initializes a new Value.
     */
    // This constructor is default-visible to prevent external packages from subclassing this class directly.
    Value() {
    }

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    @Nonnull
    @Override
    public abstract String toString();

    abstract <T> T accept(@Nonnull ValueVisitor<T> visitor);

}
