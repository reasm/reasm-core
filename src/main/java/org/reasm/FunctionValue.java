package org.reasm;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * A value that references a function.
 *
 * @author Francis Gagné
 */
@Immutable
public final class FunctionValue extends Value {

    @Nonnull
    private final Function function;

    /**
     * Initializes a new FunctionValue.
     *
     * @param function
     *            the function
     */
    public FunctionValue(@Nonnull Function function) {
        if (function == null) {
            throw new NullPointerException("function");
        }

        this.function = function;
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

        if (!this.function.equals(((FunctionValue) obj).function)) {
            return false;
        }

        return true;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + System.identityHashCode(this.function);
        return result;
    }

    @Nonnull
    @Override
    public final String toString() {
        return "FunctionValue [function=" + this.function + "]";
    }

    @Override
    final <T> T accept(@Nonnull ValueVisitor<T> visitor) {
        return visitor.visitFunction(this.function);
    }

}
