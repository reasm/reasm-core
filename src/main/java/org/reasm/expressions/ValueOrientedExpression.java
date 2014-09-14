package org.reasm.expressions;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Value;
import org.reasm.ValueVisitor;

/**
 * An expression that produces a new value when it is evaluated.
 *
 * @author Francis Gagné
 */
@Immutable
public abstract class ValueOrientedExpression extends Expression {

    @CheckForNull
    @Override
    public final String toIdentifier(@Nonnull EvaluationContext evaluationContext, @Nonnull ValueVisitor<String> valueVisitor) {
        return Value.accept(this.evaluate(evaluationContext), valueVisitor);
    }

}
