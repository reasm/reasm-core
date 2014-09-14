package org.reasm.source.parseerrors;

import javax.annotation.concurrent.Immutable;

import org.reasm.source.ParseError;

/**
 * A parse error where a string has no end delimiter.
 *
 * @author Francis Gagné
 */
@Immutable
public class UnterminatedStringParseError extends ParseError {

    private final int startOfString;

    /**
     * Initializes a new UnterminatedStringParseError.
     *
     * @param startOfString
     *            the position in the source node where the string starts
     */
    public UnterminatedStringParseError(int startOfString) {
        super("Unterminated string");
        this.startOfString = startOfString;
    }

    /**
     * Gets the position in the source node where the string starts.
     *
     * @return the position in the source node where the string starts
     */
    public final int getStartOfString() {
        return this.startOfString;
    }

}
