package org.reasm.source;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.messages.ParseErrorMessage;

/**
 * A syntax error that occurs while parsing a source node. If a source file containing a parse error is assembled, a
 * {@link ParseErrorMessage} will be added to the assembly (unless a fatal error occurs before the parse error).
 *
 * @author Francis Gagné
 */
@Immutable
public abstract class ParseError {

    @Nonnull
    private final String text;

    /**
     * Initializes a new ParseError.
     *
     * @param text
     *            the text of the parse error
     */
    protected ParseError(@Nonnull String text) {
        if (text == null) {
            throw new NullPointerException("text");
        }

        this.text = text;
    }

    /**
     * Gets the text of the parse error.
     *
     * @return the text of the parse error.
     */
    @Nonnull
    public final String getText() {
        return this.text;
    }

}
