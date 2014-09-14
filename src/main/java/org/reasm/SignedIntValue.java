package org.reasm;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * A signed integer value.
 *
 * @author Francis Gagné
 */
@Immutable
public final class SignedIntValue extends Value {

    private final long value;

    /**
     * Initializes a new unsigned integer value.
     *
     * @param value
     *            the value
     */
    public SignedIntValue(long value) {
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

        if (this.value != ((SignedIntValue) obj).value) {
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
        return "SignedIntValue [value=" + this.value + "]";
    }

    @Override
    final <T> T accept(@Nonnull ValueVisitor<T> visitor) {
        return visitor.visitSignedInt(this.value);
    }

}
