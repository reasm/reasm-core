package org.reasm;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test class for {@link LineBreakCounter}.
 *
 * @author Francis Gagné
 */
public class LineBreakCounterTest {

    /**
     * Test class for {@link LineBreakCounter#count(CharSequence)}.
     */
    @RunWith(Parameterized.class)
    public static class CountTest {

        private static final List<Object[]> TEST_DATA = Arrays.asList(new Object[][] {

                // Empty string
                { "", 0 },

                // String without line breaks
                { "abc", 0 },

                // String with LF
                { "abc\ndef", 1 },

                // String with CR
                { "abc\rdef", 1 },

                // String ending with CR
                { "abc\r", 1 },

                // String with CRLF
                { "abc\r\ndef", 1 },

                // String with LF and CR
                { "abc\n\rdef", 2 },

                // String with 3 LFs
                { "abc\ndef\nghi\njkl", 3 },

        });

        /**
         * Gets the test data for this parameterized test.
         *
         * @return the test data
         */
        @Nonnull
        @Parameters
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        @Nonnull
        private final String text;
        private final int expectedResult;

        /**
         * Initializes a new CountTest.
         *
         * @param text
         *            the text in which to count line breaks
         * @param expectedResult
         *            the expected number of line breaks
         */
        public CountTest(@Nonnull String text, int expectedResult) {
            this.text = text;
            this.expectedResult = expectedResult;
        }

        /**
         * Asserts that creating a {@link LineBreakCounter}, calling {@link LineBreakCounter#consumeChararacter(char)} on it for
         * each character of a {@link CharSequence} and calling {@link LineBreakCounter#getCount()} at the end returns the number of
         * line breaks in the {@link CharSequence}.
         */
        @Test
        public void consumeCharacter() {
            final LineBreakCounter counter = new LineBreakCounter();
            for (int i = 0; i < this.text.length(); i++) {
                counter.consumeChararacter(this.text.charAt(i));
            }

            assertThat(counter.getCount(), is(this.expectedResult));
        }

        /**
         * Asserts that creating a {@link LineBreakCounter}, calling {@link LineBreakCounter#consumeCodePoint(int)} on it for each
         * code point of a {@link CharSequence} and calling {@link LineBreakCounter#getCount()} at the end returns the number of
         * line breaks in the {@link CharSequence}.
         */
        @Test
        public void consumeCodePoint() {
            final LineBreakCounter counter = new LineBreakCounter();
            int codePoint;
            for (int i = 0; i < this.text.length(); i += Character.charCount(codePoint)) {
                codePoint = this.text.codePointAt(i);
                counter.consumeCodePoint(codePoint);
            }

            assertThat(counter.getCount(), is(this.expectedResult));
        }

        /**
         * Asserts that {@link LineBreakCounter#count(CharSequence)} returns the number of line breaks in the specified text.
         */
        @Test
        public void count() {
            int result = LineBreakCounter.count(this.text);
            assertThat(result, is(this.expectedResult));
        }

    }

    /**
     * Asserts that {@link LineBreakCounter#count(CharSequence)} throws a {@link NullPointerException} when the <code>text</code>
     * argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void countNull() {
        LineBreakCounter.count(null);
    }

}
