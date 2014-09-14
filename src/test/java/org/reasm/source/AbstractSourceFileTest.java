package org.reasm.source;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.reasm.testhelpers.IsSourceNodeWithLength.hasLength;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reasm.Architecture;
import org.reasm.ContractViolationException;
import org.reasm.testhelpers.DummySourceNode;
import org.reasm.testhelpers.HexArchitecture;
import org.reasm.testhelpers.IsMapWithSize;
import org.reasm.testhelpers.NullArchitecture;

import ca.fragag.text.Document;

import com.google.common.collect.ImmutableList;

/**
 * Test class for {@link AbstractSourceFile}.
 *
 * @author Francis Gagné
 */
public class AbstractSourceFileTest {

    /**
     * Parameterized test class for {@link AbstractSourceFile#adjustLineLengths(int, int, String)}.
     *
     * @author Francis Gagné
     */
    @RunWith(Parameterized.class)
    public static class AdjustLineLengthsTest {

        /**
         * Stores the subject, parameters and expected result for a test.
         *
         * @author Francis Gagné
         */
        public static class DataItem {

            @Nonnull
            final DummySourceFile sourceFile;
            final int offset;
            final int lengthToRemove;
            @Nonnull
            final String textToInsert;
            @Nonnull
            final Integer[] expectedAdjustedLineLengths;

            /**
             * Initializes a new DataItem.
             *
             * @param documentText
             *            the text of the AbstractSourceFile on which {@link AbstractSourceFile#adjustLineLengths(int, int, String)}
             *            will be called
             * @param offset
             *            the offset to pass to {@link AbstractSourceFile#adjustLineLengths(int, int, String)}
             * @param lengthToRemove
             *            the length to pass to {@link AbstractSourceFile#adjustLineLengths(int, int, String)}
             * @param textToInsert
             *            the text to pass to {@link AbstractSourceFile#adjustLineLengths(int, int, String)}
             * @param expectedAdjustedLineLengths
             *            the expected result from {@link AbstractSourceFile#adjustLineLengths(int, int, String)}
             */
            public DataItem(@Nonnull String documentText, int offset, int lengthToRemove, @Nonnull String textToInsert,
                    @Nonnull Integer... expectedAdjustedLineLengths) {
                this.sourceFile = new DummySourceFile(new Document(documentText));
                this.offset = offset;
                this.lengthToRemove = lengthToRemove;
                this.textToInsert = textToInsert;
                this.expectedAdjustedLineLengths = expectedAdjustedLineLengths;
            }

        }

        private static final List<Object[]> TEST_DATA = Arrays.asList(new Object[][] {
                // A single line to which characters are added
                { new DataItem("aaaaa", 2, 0, "bbb", 8) },

                // A single line to which a line feed is added
                { new DataItem("aaaaa", 2, 0, "\n", 3, 3) },

                // A single line to which characters are replaced, without changing the length
                { new DataItem("aaaaa", 1, 3, "bbb", 5) },

                // 3 lines, add characters to the second line
                { new DataItem("aaaaa\nbbbbb\nccccc", 9, 0, "dddd", 6, 10, 5) },

                // 3 lines, remove first line feed
                { new DataItem("aaaaa\nbbbbb\nccccc", 5, 1, "", 11, 5) },

                // 3 lines, remove second line feed
                { new DataItem("aaaaa\nbbbbb\nccccc", 11, 1, "", 6, 10) },

                // 3 lines, remove both line feeds
                { new DataItem("aaaaa\nbbbbb\nccccc", 5, 7, "", 10) },

                // 3 lines, replace some text including the first line feed with other text including 2 line feeds
                { new DataItem("aaaaa\nbbbbb\nccccc", 3, 6, "ddd\neee\nfff", 7, 4, 6, 5) } });

        /**
         * Gets the parameters to use to test {@link AbstractSourceFile#adjustLineLengths(int, int, String)}.
         *
         * @return the parameters
         */
        @Nonnull
        @Parameters
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        @Nonnull
        private final DataItem data;

        /**
         * Initializes a new {@link AdjustLineLengthsTest}.
         *
         * @param data
         *            the data to test with
         */
        public AdjustLineLengthsTest(@Nonnull DataItem data) {
            this.data = data;
        }

        /**
         * Asserts that {@link AbstractSourceFile#adjustLineLengths(int, int, String)} returns the correct list of adjusted line
         * lengths.
         */
        @Test
        public void adjustLineLengths() {
            final LineLengthList adjustedLineLengths = this.data.sourceFile.adjustLineLengths(this.data.offset,
                    this.data.lengthToRemove, this.data.textToInsert);
            assertThat(adjustedLineLengths, contains(this.data.expectedAdjustedLineLengths));
        }

    }

    /**
     * Parameterized test class for {@link AbstractSourceFile#computeLineLengths(CharSequence)}.
     *
     * @author Francis Gagné
     */
    @RunWith(Parameterized.class)
    public static class ComputeLineLengthsTest {

        /**
         * Stores the parameters and expected result for a test.
         *
         * @author Francis Gagné
         */
        public static class DataItem {

            @Nonnull
            final CharSequence text;
            @Nonnull
            final Integer[] expectedLineLengths;

            /**
             * Initializes a new DataItem.
             *
             * @param text
             *            the text to pass to {@link AbstractSourceFile#computeLineLengths(CharSequence)}
             * @param expectedLineLengths
             *            the expected result from {@link AbstractSourceFile#computeLineLengths(CharSequence)}
             */
            public DataItem(@Nonnull CharSequence text, @Nonnull Integer... expectedLineLengths) {
                this.text = text;
                this.expectedLineLengths = expectedLineLengths;
            }

        }

        private static final List<Object[]> TEST_DATA = Arrays.asList(new Object[][] {
                // Empty text: one empty line
                { new DataItem("", 0) },

                // A single character: a single line of length 1
                { new DataItem("a", 1) },

                // A line feed in the string: 2 lines, the first of which includes the line feed in its length
                { new DataItem("aaa\nbbb", 4, 3) },

                // Same, but with a carriage return
                { new DataItem("aaa\rbbb", 4, 3) },

                // Same, but with a carriage return followed by a line feed (treated like a single line separator)
                { new DataItem("aaa\r\nbbb", 5, 3) },

                // A line feed followed by a carriage return: 3 lines
                { new DataItem("aaa\n\rbbb", 4, 1, 3) },

                // A line feed at the end of the text
                { new DataItem("aaa\n", 4, 0) },

                // A carriage return at the end of the text
                { new DataItem("aaa\r", 4, 0) },

                // A carriage return + line feed at the end of the text
                { new DataItem("aaa\r\n", 5, 0) } });

        /**
         * Gets the parameters to use to test {@link AbstractSourceFile#computeLineLengths(CharSequence)}.
         *
         * @return the parameters
         */
        @Nonnull
        @Parameters
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        @Nonnull
        private final DataItem data;

        /**
         * Initializes a new {@link ComputeLineLengthsTest}.
         *
         * @param data
         *            the data to test with
         */
        public ComputeLineLengthsTest(@Nonnull DataItem data) {
            this.data = data;
        }

        /**
         * Asserts that {@link AbstractSourceFile#computeLineLengths(CharSequence)} computes the correct line lengths in a
         * {@link String}.
         */
        @Test
        public void computeLineLengths() {
            final ArrayList<Integer> lineLengths = AbstractSourceFile.computeLineLengths(this.data.text);
            assertThat(lineLengths, contains(this.data.expectedLineLengths));
        }

        /**
         * Asserts that {@link AbstractSourceFile#computeLineLengths(CharSequence)} computes the correct line lengths in a
         * {@link Document}.
         */
        @Test
        public void computeLineLengthsDocument() {
            final ArrayList<Integer> lineLengths = AbstractSourceFile.computeLineLengths(new Document(this.data.text));
            assertThat(lineLengths, contains(this.data.expectedLineLengths));
        }

    }

    private static final class ArchitectureWithBrokenParser1 extends Architecture {

        public static final ArchitectureWithBrokenParser1 INSTANCE = new ArchitectureWithBrokenParser1();

        private ArchitectureWithBrokenParser1() {
            super(null);
        }

        @Nonnull
        @Override
        public SourceNode parse(@Nonnull Document text) {
            return getNull();
        }

    }

    private static final class ArchitectureWithBrokenParser2 extends Architecture {

        public static final ArchitectureWithBrokenParser2 INSTANCE = new ArchitectureWithBrokenParser2();

        private ArchitectureWithBrokenParser2() {
            super(null);
        }

        @Nonnull
        @Override
        public SourceNode parse(@Nonnull Document text) {
            return new DummySourceNode(text.length() / 2, null);
        }

    }

    private static final class ArchitectureWithBrokenReparser extends Architecture {

        public static final ArchitectureWithBrokenReparser INSTANCE = new ArchitectureWithBrokenReparser();

        private ArchitectureWithBrokenReparser() {
            super(null);
        }

        @Nonnull
        @Override
        public SourceNode parse(@Nonnull Document text) {
            return new DummySourceNode(text.length(), null);
        }

        @Nonnull
        @Override
        public SourceNode reparse(@Nonnull Document text, @Nonnull AbstractSourceFile<?> oldSourceFile, int replaceOffset,
                int lengthToRemove, int lengthToInsert) {
            return getNull();
        }

    }

    private static final class DummySourceFile extends AbstractSourceFile<DummySourceFile> {

        DummySourceFile(@Nonnull Document text) {
            super(text);
        }

        DummySourceFile(@Nonnull Document text, @Nonnull LineLengthList lineLengths,
                @Nonnull Map<Architecture, SourceNode> parsedMap) {
            super(text, lineLengths, parsedMap);
        }

        @CheckForNull
        @Override
        public final String getFileName() {
            return null;
        }

        @Nonnull
        @Override
        final DummySourceFile createDerivedFile(@Nonnull Document text) {
            return new DummySourceFile(text);
        }

        @Nonnull
        @Override
        final DummySourceFile createDerivedFile(@Nonnull Document text, @Nonnull LineLengthList lineLengths,
                @Nonnull HashMap<Architecture, SourceNode> parsedMap) {
            return new DummySourceFile(text, lineLengths, parsedMap);
        }

        @Nonnull
        @Override
        final DummySourceFile self() {
            return this;
        }

    }

    private static final Document SOURCE_FILE_CONTENTS_A = new Document("aaaaaa");
    private static final String SOURCE_FILE_CONTENTS_B = "bbbbbbbb";
    private static final DummySourceFile SOURCE_FILE = new DummySourceFile(SOURCE_FILE_CONTENTS_A);
    private static final NullArchitecture NULL_ARCHITECTURE = NullArchitecture.DEFAULT;

    static <T> T getNull() { // no @Nonnull to cheat FindBugs
        return null;
    }

    /**
     * Asserts that {@link AbstractSourceFile#AbstractSourceFile(Document)} throws a {@link NullPointerException} when the
     * <code>document</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void abstractSourceFileDocumentNullDocument() {
        new DummySourceFile(null);
    }

    /**
     * Asserts that {@link AbstractSourceFile#getParsed(Architecture)} returns a nonnull {@link SourceNode} and caches its result.
     */
    @Test
    public void getParsed() {
        final DummySourceFile file = new DummySourceFile(new Document("12345678"));
        final SourceNode parsed = file.getParsed(HexArchitecture.INSTANCE);
        assertThat(parsed, is(notNullValue()));
        final SourceNode parsedAgain = file.getParsed(HexArchitecture.INSTANCE);
        assertThat(parsedAgain, is(sameInstance(parsed)));
    }

    /**
     * Asserts that {@link AbstractSourceFile#getParsed(Architecture)} throws a {@link NullPointerException} when the
     * <code>architecture</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void getParsedNullArchitecture() {
        new DummySourceFile(new Document("12345678")).getParsed(null);
    }

    /**
     * Asserts that {@link AbstractSourceFile#getParsed(Architecture)} throws a {@link ContractViolationException} when the
     * architecture's {@linkplain Architecture#parse(Document) parse method} returns <code>null</code>.
     */
    @Test
    public void getParsedNullNode() {
        final DummySourceFile file = new DummySourceFile(new Document("12345678"));

        try {
            file.getParsed(ArchitectureWithBrokenParser1.INSTANCE);
            fail("Expected a ContractViolationException to be thrown");
        } catch (ContractViolationException e) {
            assertThat(e.getMessage(), is("Contract violation: Architecture.parse() must not return null."));
        }
    }

    /**
     * Asserts that {@link AbstractSourceFile#getParsed(Architecture)} throws a {@link ContractViolationException} when the
     * architecture's {@linkplain Architecture#parse(Document) parse method} returns a {@link SourceNode} with a length that is
     * different from the source file's length.
     */
    @Test
    public void getParsedWrongLength() {
        final DummySourceFile file = new DummySourceFile(new Document("12345678"));

        try {
            file.getParsed(ArchitectureWithBrokenParser2.INSTANCE);
            fail("Expected a ContractViolationException to be thrown");
        } catch (ContractViolationException e) {
            assertThat(
                    e.getMessage(),
                    is("Contract violation: Architecture.parse() must return a SourceNode with the same length as the text it was given (expected 8, got 4)."));
        }
    }

    /**
     * Asserts that {@link AbstractSourceFile#getSourceLocations(Architecture)} returns a list of one {@link SourceLocation} and
     * that this SourceLocation is correctly initialized.
     */
    @Test
    public void getSourceLocations() {
        final DummySourceFile sourceFile = new DummySourceFile(new Document("00\n0102 03"));
        final List<SourceLocation> sourceLocationList = sourceFile.getSourceLocations(HexArchitecture.INSTANCE);
        assertThat(sourceLocationList, is(notNullValue()));
        assertThat(sourceLocationList, hasSize(1));

        final SourceLocation sourceLocation = sourceLocationList.get(0);
        assertThat(sourceLocation.getFile(), is(sameInstance((Object) sourceFile)));
        assertThat(sourceLocation.getArchitecture(), is(sameInstance((Architecture) HexArchitecture.INSTANCE)));
        assertThat(sourceLocation.getSourceNode(), hasLength(10));
        assertThat(sourceLocation.getTextPosition(), is(0));
        assertThat(sourceLocation.getLineNumber(), is(1));
        assertThat(sourceLocation.getLinePosition(), is(1));
    }

    /**
     * Asserts that {@link AbstractSourceFile#getSourceLocations(Architecture)} throws a {@link NullPointerException} when the
     * <code>architecture</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void getSourceLocationsNullArchitecture() {
        new DummySourceFile(new Document("00\n0102 03")).getSourceLocations(null);
    }

    /**
     * Asserts that {@link AbstractSourceFile#getText()} returns the {@link Document} the file was initialized with.
     */
    @Test
    public void getText() {
        assertThat(SOURCE_FILE.getText(), is(sameInstance(SOURCE_FILE_CONTENTS_A)));
    }

    /**
     * Asserts that {@link AbstractSourceFile#reparse(int, int, String, Document)} reparses the new file under the same
     * architectures as the original file had been parsed and that the new file is parsed correctly.
     */
    @Test
    public void reparse() {
        // Create a source file.
        final DummySourceFile sourceFile = new DummySourceFile(new Document("00\n1122\n334455\n66778899\naabbccddee\nff"));

        // Parse it under 2 different architectures.
        sourceFile.getParsed(NULL_ARCHITECTURE);
        sourceFile.getParsed(HexArchitecture.INSTANCE);

        // Call reparse.
        final HashMap<Architecture, SourceNode> parsedMap = sourceFile.reparse(15, 0, "0123456789abcdef\n", new Document(
                "00\n1122\n334455\n0123456789abcdef\n66778899\naabbccddee\nff"));

        // Check that both architectures are in the result map.
        assertThat(parsedMap, IsMapWithSize.hasSize(2));

        SourceNode sourceNode;
        CompositeSourceNode compositeSourceNode;
        List<SourceNode> childNodes;
        final Matcher<SourceNode> hasLength2 = hasLength(2);
        final Matcher<SourceNode> hasLength3 = hasLength(3);

        // Check that the source tree for NULL_ARCHITECTURE is correct.
        sourceNode = parsedMap.get(NULL_ARCHITECTURE);
        assertThat(sourceNode, hasLength(54));
        assertThat(sourceNode, is(instanceOf(CompositeSourceNode.class)));

        compositeSourceNode = (CompositeSourceNode) sourceNode;
        childNodes = compositeSourceNode.getChildNodes();
        assertThat(childNodes, contains(ImmutableList.<Matcher<? super SourceNode>> of(hasLength3, hasLength(5), hasLength(7),
                hasLength(17), hasLength(9), hasLength(11), hasLength2)));

        // Check that the source tree for HexArchitecture.INSTANCE is correct.
        sourceNode = parsedMap.get(HexArchitecture.INSTANCE);
        assertThat(sourceNode, hasLength(54));
        assertThat(sourceNode, is(instanceOf(CompositeSourceNode.class)));

        compositeSourceNode = (CompositeSourceNode) sourceNode;
        childNodes = compositeSourceNode.getChildNodes();
        ArrayList<Matcher<? super SourceNode>> nodesWithLengths = new ArrayList<>();
        nodesWithLengths.add(hasLength3);
        nodesWithLengths.add(hasLength2);
        nodesWithLengths.add(hasLength3);
        nodesWithLengths.add(hasLength2);
        nodesWithLengths.add(hasLength2);
        nodesWithLengths.add(hasLength3);
        nodesWithLengths.add(hasLength2);
        nodesWithLengths.add(hasLength2);
        nodesWithLengths.add(hasLength2);
        nodesWithLengths.add(hasLength2);
        nodesWithLengths.add(hasLength2);
        nodesWithLengths.add(hasLength2);
        nodesWithLengths.add(hasLength2);
        nodesWithLengths.add(hasLength3);
        nodesWithLengths.add(hasLength2);
        nodesWithLengths.add(hasLength2);
        nodesWithLengths.add(hasLength2);
        nodesWithLengths.add(hasLength3);
        nodesWithLengths.add(hasLength2);
        nodesWithLengths.add(hasLength2);
        nodesWithLengths.add(hasLength2);
        nodesWithLengths.add(hasLength2);
        nodesWithLengths.add(hasLength3);
        nodesWithLengths.add(hasLength2);
        assertThat(childNodes, contains(nodesWithLengths));
    }

    /**
     * Asserts that {@link AbstractSourceFile#replaceText(int, int, String)} throws a {@link ContractViolationException} when the
     * architecture's {@linkplain Architecture#reparse(Document, AbstractSourceFile, int, int, int) reparse method} returns
     * <code>null</code>.
     */
    @Test
    public void replaceTextBrokenReparser() {
        DummySourceFile sourceFile = new DummySourceFile(new Document("00"));
        sourceFile.getParsed(ArchitectureWithBrokenReparser.INSTANCE);

        try {
            sourceFile = sourceFile.replaceText(1, 1, "1");
            fail("Expected a ContractViolationException to be thrown");
        } catch (ContractViolationException e) {
            assertThat(e.getMessage(), is("Contract violation: Architecture.reparse() must not return null."));
        }
    }

    /**
     * Asserts that {@link AbstractSourceFile#replaceText(int, int, String)} returns the original {@link AbstractSourceFile} when
     * the text to insert is identical to the text to remove.
     */
    @Test
    public void replaceTextIdempotent() {
        final DummySourceFile sourceFile = new DummySourceFile(new Document("00"));

        // Call replaceText.
        final DummySourceFile sourceFile2 = sourceFile.replaceText(1, 1, "0");

        assertThat(sourceFile2, is(sameInstance(sourceFile)));
    }

    /**
     * Asserts that {@link AbstractSourceFile#replaceText(int, int, String)} throws a {@link NullPointerException} when the
     * <code>text</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void replaceTextNullText() {
        new DummySourceFile(new Document("00")).replaceText(0, 2, null);
    }

    /**
     * Asserts that {@link AbstractSourceFile#replaceText(int, int, String)} replaces the specified range of the file with the
     * specified text.
     */
    @Test
    public void replaceTextPartial() {
        DummySourceFile sourceFile = new DummySourceFile(new Document("00"));

        // Call replaceText.
        sourceFile = sourceFile.replaceText(1, 1, "1");

        assertThat(sourceFile.getText().toString(), is("01"));
    }

    /**
     * Asserts that {@link AbstractSourceFile#replaceText(int, int, String)} replaces the specified range of the file with the
     * specified text.
     */
    @Test
    public void replaceTextPartialStart() {
        DummySourceFile sourceFile = new DummySourceFile(new Document("00"));

        // Call replaceText.
        sourceFile = sourceFile.replaceText(0, 1, "1");

        assertThat(sourceFile.getText().toString(), is("10"));
    }

    /**
     * Asserts that {@link AbstractSourceFile#replaceText(int, int, String)} returns a new {@link AbstractSourceFile} with the whole
     * text replaced when the text to remove is the whole file.
     */
    @Test
    public void replaceTextWhole() {
        DummySourceFile sourceFile = new DummySourceFile(new Document("00"));

        // Parse the source file; the new source file should *not* be parsed, but we can't observe it...
        sourceFile.getParsed(NULL_ARCHITECTURE);

        // Call replaceText.
        sourceFile = sourceFile.replaceText(0, 2, "11");

        assertThat(sourceFile.getText().toString(), is("11"));
    }

    /**
     * Asserts that {@link AbstractSourceFile#setText(Document)} returns a new {@link AbstractSourceFile} with the contents of the
     * specified {@link Document}.
     */
    @Test
    public void setTextDocument() {
        final DummySourceFile newSourceFile = SOURCE_FILE.setText(new Document(SOURCE_FILE_CONTENTS_B));
        assertThat(newSourceFile.getText().toString(), is(SOURCE_FILE_CONTENTS_B));
    }

    /**
     * Asserts that {@link AbstractSourceFile#setText(Document)} throws a {@link NullPointerException} when the <code>text</code>
     * argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void setTextDocumentNull() {
        SOURCE_FILE.setText((Document) null);
    }

    /**
     * Asserts that {@link AbstractSourceFile#setText(String)} returns a new {@link AbstractSourceFile} with the contents of the
     * specified {@link String}.
     */
    @Test
    public void setTextString() {
        final DummySourceFile newSourceFile = SOURCE_FILE.setText(SOURCE_FILE_CONTENTS_B);
        assertThat(newSourceFile.getText().toString(), is(SOURCE_FILE_CONTENTS_B));
    }

    /**
     * Asserts that {@link AbstractSourceFile#setText(String)} throws a {@link NullPointerException} when the <code>text</code>
     * argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void setTextStringNull() {
        SOURCE_FILE.setText((String) null);
    }

    /**
     * Asserts that {@link AbstractSourceFile#textLocationOfTextPosition(int, LineLengthList.TextLocation)} calculates the correct
     * line index and line position of the specified text position.
     */
    @Test
    public void textLocationOfTextPosition() {
        final LineLengthList.TextLocation textLocation = new LineLengthList.TextLocation();
        new DummySourceFile(new Document("01\n23")).textLocationOfTextPosition(3, textLocation);
        assertThat(textLocation.lineIndex, is(1));
        assertThat(textLocation.linePosition, is(0));
    }

}
