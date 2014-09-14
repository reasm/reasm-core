package org.reasm.messages;

import javax.annotation.CheckForNull;

import org.reasm.AssemblyErrorMessage;
import org.reasm.FunctionValue;
import org.reasm.Value;
import org.reasm.expressions.Expression;

/**
 * An error message that is generated during an assembly when the subject of a function call (i.e. the expression that appears
 * before the parentheses surrounding the arguments) does not evaluate to a {@link FunctionValue}.
 *
 * @author Francis Gagné
 */
public class SubjectOfFunctionCallIsNotFunctionErrorMessage extends AssemblyErrorMessage {

    @CheckForNull
    private final Value value;

    /**
     * Initializes a new SubjectOfFunctionCallIsNotFunctionErrorMessage.
     *
     * @param value
     *            the value that was used as the subject of a function call
     */
    public SubjectOfFunctionCallIsNotFunctionErrorMessage(@CheckForNull Value value) {
        super("The subject of a function call expression must be a function; got " + Expression.valueToString(value));
        this.value = value;
    }

    /**
     * Gets the value that was used as the subject of a function call.
     *
     * @return the value
     */
    @CheckForNull
    public final Value getValue() {
        return this.value;
    }

}
