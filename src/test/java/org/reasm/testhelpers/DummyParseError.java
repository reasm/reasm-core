package org.reasm.testhelpers;

import org.reasm.source.ParseError;

/**
 * A dummy implementation of {@link ParseError}.
 *
 * @author Francis Gagné
 */
public final class DummyParseError extends ParseError {

    /**
     * Initializes a new DummyParseError.
     */
    public DummyParseError() {
        super("Dummy parse error");
    }

}
