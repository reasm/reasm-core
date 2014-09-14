package org.reasm.messages;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.reasm.AssemblyErrorMessage;
import org.reasm.source.ParseError;

/**
 * An error message that is generated during an assembly when a parse error has occurred.
 *
 * @author Francis Gagné
 */
public class ParseErrorMessage extends AssemblyErrorMessage {

    @Nonnull
    private final ParseError parseError;

    /**
     * Initializes a new ParseErrorMessage.
     *
     * @param parseError
     *            the parse error
     */
    public ParseErrorMessage(ParseError parseError) {
        super("Parse error: " + Objects.requireNonNull(parseError, "parseError").getText());
        this.parseError = parseError;
    }

    /**
     * Gets the parse error that generated this assembly error.
     *
     * @return the parse error
     */
    @Nonnull
    public final ParseError getParseError() {
        return this.parseError;
    }

}
