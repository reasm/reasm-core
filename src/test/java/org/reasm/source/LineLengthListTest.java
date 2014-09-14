package org.reasm.source;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;

/**
 * Test class for {@link LineLengthList}.
 *
 * @author Francis Gagné
 */
public class LineLengthListTest {

    private static final LineLengthList EMPTY_LINE_LENGTH_LIST = LineLengthList.Factory.INSTANCE.create();
    private static final LineLengthList LINE_LENGTH_LIST = LineLengthList.Factory.INSTANCE.create(Arrays.asList(6, 9, 7, 4));

    /**
     * Asserts that {@link LineLengthList#lineIndexOfTextPosition(int)} returns the correct line index.
     */
    @Test
    public void lineIndexOfTextPosition() {
        assertThat(LINE_LENGTH_LIST.lineIndexOfTextPosition(0), is(0));
        assertThat(LINE_LENGTH_LIST.lineIndexOfTextPosition(10), is(1));
        assertThat(LINE_LENGTH_LIST.lineIndexOfTextPosition(20), is(2));
    }

    /**
     * Asserts that {@link LineLengthList#lineIndexOfTextPosition(int)} returns 0 when the {@link LineLengthList} is empty and the
     * <code>textPosition</code> argument is 0.
     */
    @Test
    public void lineIndexOfTextPositionEmpty() {
        assertThat(EMPTY_LINE_LENGTH_LIST.lineIndexOfTextPosition(0), is(0));
    }

    /**
     * Asserts that {@link LineLengthList#lineIndexOfTextPosition(int)} throws an {@link IndexOutOfBoundsException} when the
     * {@link LineLengthList} is empty and the <code>textPosition</code> argument is not 0.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void lineIndexOfTextPositionEmptyNonZeroTextPosition() {
        EMPTY_LINE_LENGTH_LIST.lineIndexOfTextPosition(1);
    }

    /**
     * Asserts that {@link LineLengthList#lineIndexOfTextPosition(int)} returns the line index of the last line when the
     * <code>textPosition</code> argument is equal to the sum of the line lengths.
     */
    @Test
    public void lineIndexOfTextPositionTextPositionAtEnd() {
        assertThat(LINE_LENGTH_LIST.lineIndexOfTextPosition(26), is(3));
    }

    /**
     * Asserts that {@link LineLengthList#lineIndexOfTextPosition(int)} throws an {@link IndexOutOfBoundsException} when the
     * <code>textPosition</code> is greater than the sum of the line lengths.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void lineIndexOfTextPositionTextPositionTooLarge() {
        LINE_LENGTH_LIST.lineIndexOfTextPosition(27);
    }

    /**
     * Asserts that {@link LineLengthList#lineIndexOfTextPosition(int)} throws an {@link IndexOutOfBoundsException} when the
     * <code>textPosition</code> is negative.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void lineIndexOfTextPositionTextPositionTooSmall() {
        LINE_LENGTH_LIST.lineIndexOfTextPosition(-1);
    }

    /**
     * Asserts that {@link LineLengthList#textLocationOfTextPosition(int, LineLengthList.TextLocation)} correctly sets the specified
     * {@link LineLengthList.TextLocation}'s attributes.
     */
    @Test
    public void textLocationOfTextPosition() {
        final LineLengthList.TextLocation textLocation = new LineLengthList.TextLocation();

        LINE_LENGTH_LIST.textLocationOfTextPosition(0, textLocation);
        assertThat(textLocation.lineIndex, is(0));
        assertThat(textLocation.linePosition, is(0));

        LINE_LENGTH_LIST.textLocationOfTextPosition(10, textLocation);
        assertThat(textLocation.lineIndex, is(1));
        assertThat(textLocation.linePosition, is(4));

        LINE_LENGTH_LIST.textLocationOfTextPosition(20, textLocation);
        assertThat(textLocation.lineIndex, is(2));
        assertThat(textLocation.linePosition, is(5));
    }

    /**
     * Asserts that {@link LineLengthList#textLocationOfTextPosition(int, LineLengthList.TextLocation)} sets the line index and line
     * position on the {@link LineLengthList.TextLocation} to 0 when the {@link LineLengthList} is empty and the
     * <code>textPosition</code> argument is 0.
     */
    @Test
    public void textLocationOfTextPositionEmpty() {
        final LineLengthList.TextLocation textLocation = new LineLengthList.TextLocation();

        EMPTY_LINE_LENGTH_LIST.textLocationOfTextPosition(0, textLocation);
        assertThat(textLocation.lineIndex, is(0));
        assertThat(textLocation.linePosition, is(0));
    }

    /**
     * Asserts that {@link LineLengthList#textLocationOfTextPosition(int, LineLengthList.TextLocation)} throws an
     * {@link IndexOutOfBoundsException} when the {@link LineLengthList} is empty and the <code>textPosition</code> argument is not
     * 0.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void textLocationOfTextPositionEmptyNonZeroTextPosition() {
        final LineLengthList.TextLocation textLocation = new LineLengthList.TextLocation();
        EMPTY_LINE_LENGTH_LIST.textLocationOfTextPosition(1, textLocation);
    }

    /**
     * Asserts that {@link LineLengthList#textLocationOfTextPosition(int, LineLengthList.TextLocation)} sets the line index of the
     * last line and the last position on that line on the {@link LineLengthList.TextLocation} when the <code>textPosition</code>
     * argument is equal to the sum of the line lengths.
     */
    @Test
    public void textLocationOfTextPositionTextPositionAtEnd() {
        final LineLengthList.TextLocation textLocation = new LineLengthList.TextLocation();

        LINE_LENGTH_LIST.textLocationOfTextPosition(26, textLocation);
        assertThat(textLocation.lineIndex, is(3));
        assertThat(textLocation.linePosition, is(4));
    }

    /**
     * Asserts that {@link LineLengthList#textLocationOfTextPosition(int, LineLengthList.TextLocation)} throws an
     * {@link IndexOutOfBoundsException} when the <code>textPosition</code> argument is greater than the sum of the line lengths.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void textLocationOfTextPositionTextPositionTooLarge() {
        final LineLengthList.TextLocation textLocation = new LineLengthList.TextLocation();
        LINE_LENGTH_LIST.textLocationOfTextPosition(27, textLocation);
    }

    /**
     * Asserts that {@link LineLengthList#textLocationOfTextPosition(int, LineLengthList.TextLocation)} throws an
     * {@link IndexOutOfBoundsException} when the <code>textPosition</code> argument is negative.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void textLocationOfTextPositionTextPositionTooSmall() {
        final LineLengthList.TextLocation textLocation = new LineLengthList.TextLocation();
        LINE_LENGTH_LIST.textLocationOfTextPosition(-1, textLocation);
    }

    /**
     * Asserts that {@link LineLengthList#textPositionOfLineIndex(int)} returns the correct text position.
     */
    @Test
    public void textPositionOfLineIndex() {
        assertThat(LINE_LENGTH_LIST.textPositionOfLineIndex(0), is(0));
        assertThat(LINE_LENGTH_LIST.textPositionOfLineIndex(1), is(6));
        assertThat(LINE_LENGTH_LIST.textPositionOfLineIndex(2), is(15));
        assertThat(LINE_LENGTH_LIST.textPositionOfLineIndex(3), is(22));
    }

    /**
     * Asserts that {@link LineLengthList#textPositionOfLineIndex(int)} returns 0 when the {@link LineLengthList} is empty and the
     * <code>lineIndex</code> argument is 0.
     */
    @Test
    public void textPositionOfLineIndexEmpty() {
        assertThat(EMPTY_LINE_LENGTH_LIST.textPositionOfLineIndex(0), is(0));
    }

    /**
     * Asserts that {@link LineLengthList#lineIndexOfTextPosition(int)} throws an {@link IndexOutOfBoundsException} when the
     * {@link LineLengthList} is empty and the <code>lineIndex</code> argument is not 0.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void textPositionOfLineIndexEmptyNonZeroIndex() {
        EMPTY_LINE_LENGTH_LIST.textPositionOfLineIndex(1);
    }

    /**
     * Asserts that {@link LineLengthList#textPositionOfLineIndex(int)} throws an {@link IndexOutOfBoundsException} when the
     * <code>lineIndex</code> argument is greater than or equal to the number of lines in the {@link LineLengthList}.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void textPositionOfLineIndexIndexTooLarge() {
        LINE_LENGTH_LIST.textPositionOfLineIndex(4);
    }

    /**
     * Asserts that {@link LineLengthList#textPositionOfLineIndex(int)} throws an {@link IndexOutOfBoundsException} when the
     * <code>lineIndex</code> argument is negative.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void textPositionOfLineIndexIndexTooSmall() {
        LINE_LENGTH_LIST.textPositionOfLineIndex(-1);
    }

}
