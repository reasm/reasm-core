package org.reasm.expressions;

import javax.annotation.Nonnull;

import org.reasm.Function;
import org.reasm.ValueVisitor;
import org.reasm.messages.FunctionOperandNotApplicableErrorMessage;

import com.google.common.primitives.UnsignedLongs;

final class ValueToStringVisitor implements ValueVisitor<String> {

    @Nonnull
    private final EvaluationContext evaluationContext;
    @Nonnull
    private final String operator;

    ValueToStringVisitor(@Nonnull EvaluationContext evaluationContext, @Nonnull String operator) {
        this.evaluationContext = evaluationContext;
        this.operator = operator;
    }

    @Override
    public String visitFloat(double value) {
        return Double.toString(value);
    }

    @Override
    public String visitFunction(@Nonnull Function value) {
        this.evaluationContext.getAssemblyMessageConsumer().accept(new FunctionOperandNotApplicableErrorMessage(this.operator));
        return null;
    }

    @Override
    public String visitSignedInt(long value) {
        return Long.toString(value);
    }

    @Override
    public String visitString(@Nonnull String value) {
        return value;
    }

    @Override
    public String visitUndetermined() {
        return null;
    }

    @Override
    public String visitUnsignedInt(long value) {
        return UnsignedLongs.toString(value);
    }

}
