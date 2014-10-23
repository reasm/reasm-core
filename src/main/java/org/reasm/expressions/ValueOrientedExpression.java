package org.reasm.expressions;

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

    @Override
    public final IdentifierExpression toIdentifier(EvaluationContext evaluationContext, ValueVisitor<String> valueVisitor) {
        final String identifier = Value.accept(this.evaluate(evaluationContext), valueVisitor);
        if (identifier == null) {
            return null;
        }

        return new IdentifierExpression(identifier, null);
    }

}
