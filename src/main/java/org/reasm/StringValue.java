package org.reasm;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * A string value.
 *
 * @author Francis Gagné
 */
@Immutable
public final class StringValue extends Value {

    @Nonnull
    private final String value;

    /**
     * Initializes a new string value.
     *
     * @param value
     *            the value
     * @throws NullPointerException
     *             value is null
     */
    public StringValue(@Nonnull String value) {
        if (value == null) {
            throw new NullPointerException("value");
        }

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

        if (!this.value.equals(((StringValue) obj).value)) {
            return false;
        }

        return true;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.value.hashCode();
        return result;
    }

    @Nonnull
    @Override
    public final String toString() {
        return "StringValue [value=" + this.value + "]";
    }

    @Override
    final <T> T accept(@Nonnull ValueVisitor<T> visitor) {
        return visitor.visitString(this.value);
    }

}
