package org.reasm.expressions;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Value;
import org.reasm.ValueVisitor;

/**
 * An expression that simplifies to a different expression.
 *
 * @author Francis Gagné
 */
@Immutable
public abstract class ExpressionOrientedExpression extends Expression {

    @Override
    public final Value evaluate(EvaluationContext evaluationContext) {
        return this.simplify(evaluationContext).evaluate(evaluationContext);
    }

    @Override
    public final IdentifierExpression toIdentifier(EvaluationContext evaluationContext, ValueVisitor<String> valueVisitor) {
        return this.simplify(evaluationContext).toIdentifier(evaluationContext, valueVisitor);
    }

    /**
     * Simplifies this expression to an expression that does not contain subexpressions or to another expression that can be further
     * simplified.
     *
     * @param evaluationContext
     *            the {@link EvaluationContext} in which the expression is evaluated
     * @return the simplified expression (must <strong>not</strong> be <code>this</code>!)
     */
    @Nonnull
    protected abstract Expression simplify(@Nonnull EvaluationContext evaluationContext);

}
