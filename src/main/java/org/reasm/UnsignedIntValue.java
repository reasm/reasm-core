package org.reasm;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * An unsigned integer value.
 *
 * @author Francis Gagné
 */
@Immutable
public final class UnsignedIntValue extends Value {

    private final long value;

    /**
     * Initializes a new unsigned integer value.
     *
     * @param value
     *            the value
     */
    public UnsignedIntValue(long value) {
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

        if (this.value != ((UnsignedIntValue) obj).value) {
            return false;
        }

        return true;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.value ^ this.value >>> 32);
        return result;
    }

    @Nonnull
    @Override
    public final String toString() {
        return "UnsignedIntValue [value=" + this.value + "]";
    }

    @Override
    final <T> T accept(@Nonnull ValueVisitor<T> visitor) {
        return visitor.visitUnsignedInt(this.value);
    }

}
