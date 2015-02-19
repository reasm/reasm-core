package org.reasm.source;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.reasm.Architecture;
import org.reasm.SubstringBounds;
import org.reasm.testhelpers.NullArchitecture;

/**
 * Test class for {@link SourceNodeRangeReader}.
 *
 * @author Francis Gagné
 */
public class SourceNodeRangeReaderTest {

    /**
     * An implementation of {@link SourceNodeRangeReader.SkipHandler} that skips text between parentheses.
     *
     * @author Francis Gagné
     */
    private static final class ParenthesesSkipHandler extends SourceNodeRangeReader.SkipHandler {

        private boolean inParentheses;

        public ParenthesesSkipHandler() {
        }

        @Override
        protected boolean skipCurrentCodePoint() {
            final SourceNodeRangeReader reader = this.getReader();
            assert reader != null;

            if (this.inParentheses) {
                if (reader.getCurrentCodePoint() == ')') {
                    this.inParentheses = false;
                }

                return true;
            }

            if (reader.getCurrentCodePoint() == '(') {
                this.inParentheses = true;
                return true;
            }

            return false;
        }

    }

    private static final String TEXT = "0123\nabc(def)g\uD83C\uDF41hij";
    private static final SourceFile SOURCE_FILE = new SourceFile(TEXT, null);
    private static final Architecture NULL_ARCHITECTURE = NullArchitecture.DEFAULT;
    private static final CompositeSourceNode ROOT_NODE = (CompositeSourceNode) NULL_ARCHITECTURE.parse(SOURCE_FILE.getText());
    private static final SourceNode SOURCE_NODE = ROOT_NODE.getChildNodes().get(1);
    private static final SourceLocation SOURCE_LOCATION = new SourceLocation(SOURCE_FILE, NULL_ARCHITECTURE, SOURCE_NODE, 5, 2, 1);

    /**
     * Asserts that {@link SourceNodeRangeReader#getCurrentCodePoint()} returns the code point at the reader's current position,
     * {@link SourceNodeRangeReader#getCurrentPosition()} returns the reader's current position (where the initial position is 0),
     * {@link SourceNodeRangeReader#getCurrentPositionInSourceNode()} returns the reader's position within the source node of the
     * source location the reader was constructed with, {@link SourceNodeRangeReader#atEnd()} returns <code>true</code> when the end
     * of the reader has been reached and <code>false</code> otherwise, and {@link SourceNodeRangeReader#advance()} advances the
     * reader to the next code point.
     */
    @Test
    public void advance() {
        final SourceNodeRangeReader reader = new SourceNodeRangeReader(SOURCE_LOCATION, new SubstringBounds(2, 12));

        int codePoint;
        for (int i = 5 + 2; i < 5 + 12; i += Character.charCount(codePoint)) {
            codePoint = TEXT.codePointAt(i);
            assertThat(reader.getCurrentCodePoint(), is(codePoint));
            assertThat(reader.getCurrentPosition(), is(i - (5 + 2)));
            assertThat(reader.getCurrentPositionInSourceNode(), is(i - 5));
            assertThat(reader.atEnd(), is(false));
            reader.advance();
        }

        assertThat(reader.atEnd(), is(true));
    }

    /**
     * Asserts that {@link SourceNodeRangeReader#getCurrentCodePoint()}, {@link SourceNodeRangeReader#getCurrentPosition()},
     * {@link SourceNodeRangeReader#getCurrentPositionInSourceNode()}, {@link SourceNodeRangeReader#atEnd()} and
     * {@link SourceNodeRangeReader#advance()} work correctly when the {@link SourceNodeRangeReader} has been constructed with a
     * {@link SourceNodeRangeReader.SkipHandler}.
     */
    @Test
    public void advanceSkipHandler() {
        final SourceNodeRangeReader reader = new SourceNodeRangeReader(SOURCE_LOCATION, new SubstringBounds(2, 12),
                new ParenthesesSkipHandler());

        assertThat(reader.getCurrentPosition(), is(0));
        assertThat(reader.getCurrentPositionInSourceNode(), is(2));
        reader.advance();

        int codePoint;
        for (int i = 5 + 8; i < 5 + 12; i += Character.charCount(codePoint)) {
            codePoint = TEXT.codePointAt(i);
            assertThat(reader.getCurrentCodePoint(), is(codePoint));
            assertThat(reader.getCurrentPosition(), is(i - 7));
            assertThat(reader.getCurrentPositionInSourceNode(), is(i - 5));
            assertThat(reader.atEnd(), is(false));
            reader.advance();
        }

        assertThat(reader.atEnd(), is(true));
    }

    /**
     * Asserts that {@link SourceNodeRangeReader#readToString()} returns the rest of the reader's contents as a {@link String}.
     */
    @Test
    public void readToString() {
        final SourceNodeRangeReader reader = new SourceNodeRangeReader(SOURCE_LOCATION, new SubstringBounds(2, 12));
        assertThat(reader.readToString(), is("c(def)g\uD83C\uDF41h"));
        assertThat(reader.atEnd(), is(true));
    }

    /**
     * Asserts that {@link SourceNodeRangeReader#readToString()} returns the rest of the reader's contents as a {@link String}.
     */
    @Test
    public void readToStringAfterAdvance() {
        final SourceNodeRangeReader reader = new SourceNodeRangeReader(SOURCE_LOCATION, new SubstringBounds(1, 12));
        reader.advance();
        assertThat(reader.readToString(), is("c(def)g\uD83C\uDF41h"));
        assertThat(reader.atEnd(), is(true));
    }

    /**
     * Asserts that {@link SourceNodeRangeReader#readToString()} returns the rest of the reader's contents, excluding the text to
     * skip as specified by the reader's {@link SourceNodeRangeReader.SkipHandler}, as a {@link String}.
     */
    @Test
    public void readToStringSkipHandler() {
        final SourceNodeRangeReader reader = new SourceNodeRangeReader(SOURCE_LOCATION, new SubstringBounds(2, 12),
                new ParenthesesSkipHandler());
        assertThat(reader.readToString(), is("cg\uD83C\uDF41h"));
        assertThat(reader.atEnd(), is(true));
    }

    /**
     * Asserts that {@link SourceNodeRangeReader#SourceNodeRangeReader(SourceLocation, int, int)} correctly initializes a
     * {@link SourceNodeRangeReader}.
     */
    @Test
    public void sourceNodeRangeReaderSourceLocationIntInt() {
        final SourceNodeRangeReader reader = new SourceNodeRangeReader(SOURCE_LOCATION, 2, 12);
        assertThat(reader.getSourceLocation(), is(sameInstance(SOURCE_LOCATION)));
    }

    /**
     * Asserts that {@link SourceNodeRangeReader#SourceNodeRangeReader(SourceLocation, int, int, SourceNodeRangeReader.SkipHandler)}
     * correctly initializes a {@link SourceNodeRangeReader}
     */
    @Test
    public void sourceNodeRangeReaderSourceLocationIntIntSkipHandler() {
        final SourceNodeRangeReader reader = new SourceNodeRangeReader(SOURCE_LOCATION, 2, 12, new ParenthesesSkipHandler());
        assertThat(reader.getSourceLocation(), is(sameInstance(SOURCE_LOCATION)));
    }

    /**
     * Asserts that {@link SourceNodeRangeReader#SourceNodeRangeReader(SourceLocation, int, int, SourceNodeRangeReader.SkipHandler)}
     * correct the <code>end</code> argument to the source location's source node's length when the <code>end</code> argument is
     * greater than the source location's source node's length.
     */
    @Test
    public void sourceNodeRangeReaderSourceLocationIntIntSkipHandlerEndTooLarge() {
        final SourceNodeRangeReader reader = new SourceNodeRangeReader(SOURCE_LOCATION, 2, 15, new ParenthesesSkipHandler());
        assertThat(reader.getSourceLocation(), is(sameInstance(SOURCE_LOCATION)));
        assertThat(reader.readToString(), is("cg\uD83C\uDF41hij"));
        assertThat(reader.atEnd(), is(true));
    }

    /**
     * Asserts that {@link SourceNodeRangeReader#SourceNodeRangeReader(SourceLocation, int, int, SourceNodeRangeReader.SkipHandler)}
     * corrects the <code>end</code> argument to the value of the <code>start</code> argument when the <code>end</code> argument is
     * less than the <code>start</code> argument.
     */
    @Test
    public void sourceNodeRangeReaderSourceLocationIntIntSkipHandlerEndTooSmall() {
        final SourceNodeRangeReader reader = new SourceNodeRangeReader(SOURCE_LOCATION, 2, 1, new ParenthesesSkipHandler());
        assertThat(reader.getSourceLocation(), is(sameInstance(SOURCE_LOCATION)));
        assertThat(reader.atEnd(), is(true));
    }

    /**
     * Asserts that {@link SourceNodeRangeReader#SourceNodeRangeReader(SourceLocation, int, int, SourceNodeRangeReader.SkipHandler)}
     * throws a {@link NullPointerException} when the <code>sourceLocation</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void sourceNodeRangeReaderSourceLocationIntIntSkipHandlerNullSourceLocation() {
        new SourceNodeRangeReader(null, 2, 12, new ParenthesesSkipHandler());
    }

    /**
     * Asserts that {@link SourceNodeRangeReader#SourceNodeRangeReader(SourceLocation, int, int, SourceNodeRangeReader.SkipHandler)}
     * corrects the <code>start</code> argument to the source location's source node's length. when the <code>start</code> argument
     * is greater than the source location's source node's length.
     */
    @Test
    public void sourceNodeRangeReaderSourceLocationIntIntSkipHandlerStartTooLarge() {
        final SourceNodeRangeReader reader = new SourceNodeRangeReader(SOURCE_LOCATION, 15, 14, new ParenthesesSkipHandler());
        assertThat(reader.getSourceLocation(), is(sameInstance(SOURCE_LOCATION)));
        assertThat(reader.atEnd(), is(true));
    }

    /**
     * Asserts that {@link SourceNodeRangeReader#SourceNodeRangeReader(SourceLocation, int, int, SourceNodeRangeReader.SkipHandler)}
     * corrects the <code>start</code> argument to 0 when it is negative.
     */
    @Test
    public void sourceNodeRangeReaderSourceLocationIntIntSkipHandlerStartTooSmall() {
        final SourceNodeRangeReader reader = new SourceNodeRangeReader(SOURCE_LOCATION, -1, 12, new ParenthesesSkipHandler());
        assertThat(reader.getSourceLocation(), is(sameInstance(SOURCE_LOCATION)));
        assertThat(reader.getCurrentCodePoint(), is(0x61));
    }

    /**
     * Asserts that {@link SourceNodeRangeReader#SourceNodeRangeReader(SourceLocation, SubstringBounds)} correctly initializes a
     * {@link SourceNodeRangeReader}.
     */
    @Test
    public void sourceNodeRangeReaderSourceLocationSubstringBounds() {
        final SourceNodeRangeReader reader = new SourceNodeRangeReader(SOURCE_LOCATION, new SubstringBounds(2, 12));
        assertThat(reader.getSourceLocation(), is(sameInstance(SOURCE_LOCATION)));
    }

    /**
     * Asserts that {@link SourceNodeRangeReader#SourceNodeRangeReader(SourceLocation, SubstringBounds)} throws a
     * {@link NullPointerException} when the <code>bounds</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void sourceNodeRangeReaderSourceLocationSubstringBoundsNullBounds() {
        new SourceNodeRangeReader(SOURCE_LOCATION, null);
    }

    /**
     * Asserts that
     * {@link SourceNodeRangeReader#SourceNodeRangeReader(SourceLocation, SubstringBounds, SourceNodeRangeReader.SkipHandler)}
     * correctly initializes a {@link SourceNodeRangeReader}.
     */
    @Test
    public void sourceNodeRangeReaderSourceLocationSubstringBoundsSkipHandler() {
        final SourceNodeRangeReader reader = new SourceNodeRangeReader(SOURCE_LOCATION, new SubstringBounds(2, 12),
                new ParenthesesSkipHandler());
        assertThat(reader.getSourceLocation(), is(sameInstance(SOURCE_LOCATION)));
    }

    /**
     * Asserts that
     * {@link SourceNodeRangeReader#SourceNodeRangeReader(SourceLocation, SubstringBounds, SourceNodeRangeReader.SkipHandler)}
     * throws a {@link NullPointerException} when the <code>bounds</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void sourceNodeRangeReaderSourceLocationSubstringBoundsSkipHandlerNullBounds() {
        new SourceNodeRangeReader(SOURCE_LOCATION, null, new ParenthesesSkipHandler());
    }

    /**
     * Asserts that {@link SourceNodeRangeReader#SourceNodeRangeReader(SourceNodeRangeReader)} correctly initializes a
     * {@link SourceNodeRangeReader} from another {@link SourceNodeRangeReader}.
     */
    @Test
    public void sourceNodeRangeReaderSourceNodeRangeReader() {
        final SourceNodeRangeReader reader = new SourceNodeRangeReader(SOURCE_LOCATION, new SubstringBounds(2, 12),
                new ParenthesesSkipHandler());
        assertThat(reader.getCurrentCodePoint(), is(0x63));
        reader.advance();
        assertThat(reader.getCurrentCodePoint(), is(0x67));
        final SourceNodeRangeReader reader2 = new SourceNodeRangeReader(reader);
        assertThat(reader2.getSourceLocation(), is(sameInstance(SOURCE_LOCATION)));
        assertThat(reader2.getCurrentCodePoint(), is(0x67));
        reader2.advance();
        assertThat(reader2.getCurrentCodePoint(), is(0x1F341));
        reader2.advance();
        assertThat(reader2.getCurrentCodePoint(), is(0x68));
        assertThat(reader.getCurrentCodePoint(), is(0x67));
    }

    /**
     * Asserts that {@link SourceNodeRangeReader#SourceNodeRangeReader(SourceNodeRangeReader)} throws a {@link NullPointerException}
     * when the <code>other</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void sourceNodeRangeReaderSourceNodeRangeReaderNull() {
        new SourceNodeRangeReader(null);
    }

}
