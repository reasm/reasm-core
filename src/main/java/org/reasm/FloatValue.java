package org.reasm;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * A floating-point value.
 *
 * @author Francis Gagné
 */
@Immutable
public final class FloatValue extends Value {

    private final double value;

    /**
     * Initializes a new floating-point value.
     *
     * @param value
     *            the value
     */
    public FloatValue(double value) {
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

        if (Double.doubleToLongBits(this.value) != Double.doubleToLongBits(((FloatValue) obj).value)) {
            return false;
        }

        return true;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        long doubleBits;
        doubleBits = Double.doubleToLongBits(this.value);
        result = prime * result + (int) (doubleBits ^ doubleBits >>> 32);
        return result;
    }

    @Nonnull
    @Override
    public final String toString() {
        return "FloatValue [value=" + this.value + "]";
    }

    @Override
    final <T> T accept(@Nonnull ValueVisitor<T> visitor) {
        return visitor.visitFloat(this.value);
    }

}
