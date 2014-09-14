package org.reasm.expressions;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.UnsignedIntValue;
import org.reasm.Value;

/**
 * An expression that evaluates to the program counter.
 *
 * @author Francis Gagné
 */
@Immutable
public final class ProgramCounterExpression extends ValueOrientedExpression {

    /**
     * The unique instance of the ProgramCounterExpression class.
     */
    public static final ProgramCounterExpression INSTANCE = new ProgramCounterExpression();

    private ProgramCounterExpression() {
    }

    @Override
    public final boolean equals(@CheckForNull Object obj) {
        return obj != null && this.getClass() == obj.getClass();
    }

    @Nonnull
    @Override
    public final Value evaluate(@Nonnull EvaluationContext evaluationContext) {
        return new UnsignedIntValue(evaluationContext.getProgramCounter());
    }

    @Override
    public final int hashCode() {
        return 1;
    }

    @Nonnull
    @Override
    public final String toString() {
        return "ProgramCounterExpression []";
    }

}
