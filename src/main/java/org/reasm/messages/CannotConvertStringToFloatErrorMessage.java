package org.reasm.messages;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.reasm.AssemblyErrorMessage;
import org.reasm.expressions.Expression;

/**
 * An error message that is generated during an assembly when a string value is implicitly or explicitly converted to a
 * floating-point value, but the string cannot be parsed as a floating-point value.
 *
 * @author Francis Gagné
 */
public class CannotConvertStringToFloatErrorMessage extends AssemblyErrorMessage {

    private final String string;

    /**
     * Initializes a new CannotConvertStringToFloatErrorMessage.
     *
     * @param string
     *            the string that failed conversion
     */
    public CannotConvertStringToFloatErrorMessage(@Nonnull String string) {
        super("Cannot convert string " + Expression.quoteString(Objects.requireNonNull(string, "string")) + " to float");
        this.string = string;
    }

    /**
     * Gets the string that failed conversion.
     *
     * @return the string
     */
    public final String getString() {
        return this.string;
    }

}
