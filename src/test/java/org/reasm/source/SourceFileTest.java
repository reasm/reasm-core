package org.reasm.source;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.HashMap;

import org.junit.Test;
import org.reasm.Architecture;

import ca.fragag.text.Document;

/**
 * Test class for {@link SourceFile}.
 *
 * @author Francis Gagné
 */
public class SourceFileTest {

    private static final Document SOURCE_FILE_CONTENTS_A = new Document("aaaaaa");
    private static final String SOURCE_FILE_CONTENTS_B = "bbbbbbbb";
    private static final String SOURCE_FILE_NAME = "file.asm";
    private static final SourceFile SOURCE_FILE = new SourceFile(SOURCE_FILE_CONTENTS_A, SOURCE_FILE_NAME);

    /**
     * Asserts that {@link SourceFile#createDerivedFile(Document)} creates a new {@link SourceFile} with the specified contents and
     * the original {@link SourceFile}'s file name.
     */
    @Test
    public void createDerivedFileDocument() {
        final SourceFile newSourceFile = SOURCE_FILE.createDerivedFile(new Document(SOURCE_FILE_CONTENTS_B));
        assertThat(newSourceFile.getText().toString(), is(SOURCE_FILE_CONTENTS_B));
        assertThat(newSourceFile.getFileName(), is(SOURCE_FILE_NAME));
    }

    /**
     * Asserts that {@link SourceFile#createDerivedFile(Document, LineLengthList, HashMap)} creates a new {@link SourceFile} with
     * the specified contents and the original {@link SourceFile}'s file name.
     */
    @Test
    public void createDerivedFileDocumentLineLengthListHashMap() {
        SourceFile sourceFile = new SourceFile("00", SOURCE_FILE_NAME);

        final int offset = 1;
        final int length = 1;
        final String text = "1";

        final Document newTextDocument = sourceFile.getText().replace(offset, length, text);
        final LineLengthList lineLengths = sourceFile.adjustLineLengths(offset, length, text);
        final HashMap<Architecture, SourceNode> parsedMap = sourceFile.reparse(offset, length, text, newTextDocument);

        sourceFile = sourceFile.createDerivedFile(newTextDocument, lineLengths, parsedMap);

        assertThat(sourceFile.getText().toString(), is("01"));
        assertThat(sourceFile.getFileName(), is(SOURCE_FILE_NAME));
    }

    /**
     * Asserts that {@link SourceFile#getFileName()} returns the file name passed to the constructor.
     */
    @Test
    public void getFileName() {
        assertThat(SOURCE_FILE.getFileName(), is(SOURCE_FILE_NAME));
    }

    /**
     * Asserts that {@link SourceFile#self()} returns <code>this</code>.
     */
    @Test
    public void self() {
        final SourceFile sourceFile = new SourceFile("00", SOURCE_FILE_NAME);
        final SourceFile sourceFile2 = sourceFile.replaceText(1, 1, "0");
        assertThat(sourceFile2, is(sameInstance(sourceFile)));
    }

    /**
     * Asserts that {@link SourceFile#SourceFile(Document, String)} throws a {@link NullPointerException} when the <code>text</code>
     * argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void sourceFileDocumentStringNullText() {
        new SourceFile((Document) null, SOURCE_FILE_NAME);
    }

    /**
     * Asserts that {@link SourceFile#SourceFile(String, String)} throws a {@link NullPointerException} when the <code>text</code>
     * argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void sourceFileStringStringNullText() {
        new SourceFile((String) null, SOURCE_FILE_NAME);
    }

}
