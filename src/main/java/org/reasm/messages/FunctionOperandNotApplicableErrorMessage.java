package org.reasm.messages;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an operand that evaluates to a function is applied to an operator that
 * does not support function values.
 *
 * @author Francis Gagné
 */
public class FunctionOperandNotApplicableErrorMessage extends AssemblyErrorMessage {

    @Nonnull
    private final String operator;

    /**
     * Initializes a new FunctionOperandNotApplicableErrorMessage.
     *
     * @param operator
     *            the operator on which the function value was applied
     */
    public FunctionOperandNotApplicableErrorMessage(@Nonnull String operator) {
        super("Operand of function type cannot be applied to '" + Objects.requireNonNull(operator, "operator") + "' operator");
        this.operator = operator;
    }

    /**
     * Gets the operator on which the function value was applied.
     *
     * @return the operator
     */
    @Nonnull
    public final String getOperator() {
        return this.operator;
    }

}
