package org.reasm;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.meta.When;

/**
 * An implementation of {@link ValueVisitor} that converts a value to a {@link Boolean}.
 *
 * @author Francis Gagné
 */
@Immutable
public final class ValueToBooleanVisitor implements ValueVisitor<Boolean> {

    /** The single instance of the ValueToBooleanVisitor class. */
    public static final ValueToBooleanVisitor INSTANCE = new ValueToBooleanVisitor();

    private ValueToBooleanVisitor() {
    }

    @Nonnull
    @Override
    public Boolean visitFloat(double value) {
        return value != 0.0;
    }

    @Nonnull
    @Override
    public Boolean visitFunction(@Nonnull Function value) {
        return Boolean.TRUE;
    }

    @Nonnull
    @Override
    public Boolean visitSignedInt(long value) {
        return value != 0;
    }

    @Nonnull
    @Override
    public Boolean visitString(@Nonnull String value) {
        return value.length() != 0;
    }

    @Nonnull(when = When.NEVER)
    @Override
    public Boolean visitUndetermined() {
        return null;
    }

    @Nonnull
    @Override
    public Boolean visitUnsignedInt(long value) {
        return value != 0;
    }

}
