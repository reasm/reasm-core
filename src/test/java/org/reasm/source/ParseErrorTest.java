package org.reasm.source;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Test class for {@link ParseError}.
 *
 * @author Francis Gagné
 */
public class ParseErrorTest {

    /**
     * Asserts that {@link ParseError#ParseError(String)} correctly initializes a {@link ParseError}.
     */
    @Test
    public void parseError() {
        final String text = "test";
        final ParseError parseError = new ParseError(text) {
        };
        assertThat(parseError.getText(), is(text));
    }

    /**
     * Asserts that {@link ParseError#ParseError(String)} throws a {@link NullPointerException} when the <code>text</code> argument
     * is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void parseErrorNull() {
        new ParseError(null) {
        };
    }

}
