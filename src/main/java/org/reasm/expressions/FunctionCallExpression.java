package org.reasm.expressions;

import java.util.Arrays;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Function;
import org.reasm.Value;
import org.reasm.ValueVisitor;
import org.reasm.messages.SubjectOfFunctionCallIsNotFunctionErrorMessage;

/**
 * A function call expression is an expression consisting of a call to a function defined in source code with its arguments.
 *
 * @author Francis Gagné
 */
@Immutable
public final class FunctionCallExpression extends ExpressionOrientedExpression {

    @Nonnull
    private final Expression function;
    @Nonnull
    private final Expression[] arguments;

    /**
     * Initializes a new function call expression.
     *
     * @param function
     *            an expression that evaluates to a function value
     * @param arguments
     *            the arguments. The array is copied.
     */
    public FunctionCallExpression(@Nonnull Expression function, @Nonnull Expression... arguments) {
        if (function == null) {
            throw new NullPointerException("function");
        }

        if (arguments == null) {
            throw new NullPointerException("arguments");
        }

        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] == null) {
                throw new NullPointerException("arguments[" + i + "]");
            }
        }

        this.function = function;
        this.arguments = arguments.length == 0 ? arguments : arguments.clone();
    }

    /**
     * Initializes a new function call expression.
     *
     * @param function
     *            an expression that evaluates to a function value
     * @param arguments
     *            the arguments. The list is copied to an array.
     */
    public FunctionCallExpression(@Nonnull Expression function, @Nonnull List<Expression> arguments) {
        this(function, arguments.toArray(new Expression[arguments.size()]));
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

        FunctionCallExpression other = (FunctionCallExpression) obj;
        if (!this.function.equals(other.function)) {
            return false;
        }

        if (!Arrays.equals(this.arguments, other.arguments)) {
            return false;
        }

        return true;
    }

    /**
     * Gets the arguments of this function call expression.
     *
     * @return the arguments. The returned value is a copy of the private array.
     */
    @Nonnull
    public final Expression[] getArguments() {
        return this.arguments.clone();
    }

    /**
     * Gets the expression of the function used in this function call expression.
     *
     * @return the function expression
     */
    @Nonnull
    public final Expression getFunction() {
        return this.function;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.function.hashCode();
        result = prime * result + Arrays.hashCode(this.arguments);
        return result;
    }

    @Override
    public final String toString() {
        return "FunctionCallExpression [function=" + this.function + ", arguments=" + Arrays.toString(this.arguments) + "]";
    }

    @Nonnull
    @Override
    protected final Expression simplify(@Nonnull final EvaluationContext evaluationContext) {
        final Value function = this.function.evaluate(evaluationContext);

        return Value.accept(function, new ValueVisitor<Expression>() {

            @Override
            public Expression visitFloat(double value) {
                return this.visitDefault();
            }

            @Override
            public Expression visitFunction(@Nonnull Function value) {
                // Call getArguments() to get a copy of the array, because Function.call() could modify the array it receives.
                return value.call(FunctionCallExpression.this.getArguments(), evaluationContext);
            }

            @Override
            public Expression visitSignedInt(long value) {
                return this.visitDefault();
            }

            @Override
            public Expression visitString(@Nonnull String value) {
                return this.visitDefault();
            }

            @Override
            public Expression visitUndetermined() {
                return ValueExpression.UNDETERMINED;
            }

            @Override
            public Expression visitUnsignedInt(long value) {
                return this.visitDefault();
            }

            private Expression visitDefault() {
                evaluationContext.getAssemblyMessageConsumer().accept(new SubjectOfFunctionCallIsNotFunctionErrorMessage(function));
                return ValueExpression.UNDETERMINED;
            }

        });
    }

}
