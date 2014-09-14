package org.reasm.source.parseerrors;

import javax.annotation.concurrent.Immutable;

import org.reasm.source.ParseError;

/**
 * A parse error where a closing parenthesis that has no matching opening parenthesis is found.
 *
 * @author Francis Gagné
 */
@Immutable
public class MismatchedParenthesisParseError extends ParseError {

    private final int logicalPosition;

    /**
     * Initializes a new MismatchedParenthesisParseError.
     *
     * @param logicalPosition
     *            the position in the source node where the mismatched parenthesis was found
     */
    public MismatchedParenthesisParseError(int logicalPosition) {
        super("Mismatched parenthesis");
        this.logicalPosition = logicalPosition;
    }

    /**
     * Gets the position in the source node where the mismatched parenthesis was found.
     *
     * @return the position in the source node where the mismatched parenthesis was found
     */
    public final int getLogicalPosition() {
        return this.logicalPosition;
    }

}
