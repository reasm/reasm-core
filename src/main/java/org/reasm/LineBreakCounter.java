package org.reasm;

import javax.annotation.Nonnull;

/**
 * An object that counts line breaks in a sequence of characters/code points. It handles the CR+LF sequence as a single line break.
 * <p>
 * There are two ways to use this class. The first way is to create an instance, call {@link #consumeChararacter(char)} or,
 * preferably, {@link #consumeCodePoint(int)} for each character or code point that is processed, then call {@link #getCount()} to
 * retrieve the number of line breaks in the processed characters or code points. The second way is to call
 * {@link #count(CharSequence)} to retrieve the number of line breaks in a {@link CharSequence}. The first way should be used if you
 * need to loop through the characters or code points yourself to do some other processing; the second way should be used if you
 * don't need to perform any processing on the characters or code points.
 *
 * @author Francis Gagné
 */
public final class LineBreakCounter {

    /**
     * Counts the number of line breaks in the specified {@link CharSequence}.
     *
     * @param text
     *            the text to count the number of line breaks in
     * @return the number of line breaks
     */
    public static int count(@Nonnull CharSequence text) {
        if (text == null) {
            throw new NullPointerException("text");
        }

        int lineBreaks = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\r') {
                // If the CR is followed by a LF, treat the sequence as a single line break.
                if (i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                }

                lineBreaks++;
            } else if (text.charAt(i) == '\n') {
                lineBreaks++;
            }
        }

        return lineBreaks;
    }

    private int count;
    private boolean lastWasCR;

    /**
     * Consumes the specified character.
     *
     * @param ch
     *            the character
     */
    public final void consumeChararacter(char ch) {
        this.consumeCodePoint(ch);
    }

    /**
     * Consumes the specified code point.
     *
     * @param codePoint
     *            the code point
     */
    public final void consumeCodePoint(int codePoint) {
        if (codePoint == '\r') {
            this.count++;
            this.lastWasCR = true;
        } else {
            if (codePoint == '\n') {
                if (!this.lastWasCR) {
                    this.count++;
                }
            }

            this.lastWasCR = false;
        }
    }

    /**
     * Gets the number of line breaks in the consumed characters/code points.
     *
     * @return the number of line breaks
     */
    public final int getCount() {
        return this.count;
    }

}
