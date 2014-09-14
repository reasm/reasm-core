package org.reasm.source;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.HashMap;

import org.junit.Test;
import org.reasm.Architecture;
import org.reasm.testhelpers.NullArchitecture;

import ca.fragag.text.Document;

/**
 * Test class for {@link MacroInstantiation}.
 *
 * @author Francis Gagné
 */
public class MacroInstantiationTest {

    private static final String SOURCE_FILE_NAME = "file.asm";
    private static final MacroInstantiation MACRO_INSTANTIATION = new MacroInstantiation(new SourceFile(
            "one\ntwo\nthree\nfour\nfive", SOURCE_FILE_NAME).getSourceLocations(NullArchitecture.DEFAULT).get(0)
            .getChildSourceLocations().get(2));

    /**
     * Asserts that {@link MacroInstantiation#createDerivedFile(Document)} creates a new {@link MacroInstantiation} with the
     * contents of the specified {@link Document} and the original {@link MacroInstantiation}'s file name.
     */
    @Test
    public void createDerivedFileDocument() {
        final MacroInstantiation derived = MACRO_INSTANTIATION.createDerivedFile(new Document("foobar"));
        assertThat(derived.getText().toString(), is("foobar"));
        assertThat(derived.getFileName(), is(SOURCE_FILE_NAME));
    }

    /**
     * Asserts that {@link MacroInstantiation#createDerivedFile(Document, LineLengthList, HashMap)} creates a new
     * {@link MacroInstantiation} with the contents of the specified {@link Document} and the original {@link MacroInstantiation}'s
     * file name.
     */
    @Test
    public void createDerivedFileDocumentLineLengthListHashMap() {
        MacroInstantiation macroInstantiation = MACRO_INSTANTIATION;

        final int offset = 2;
        final int length = 1;
        final String text = "RRRRR";

        final Document newTextDocument = macroInstantiation.getText().replace(offset, length, text);
        final LineLengthList lineLengths = macroInstantiation.adjustLineLengths(offset, length, text);
        final HashMap<Architecture, SourceNode> parsedMap = macroInstantiation.reparse(offset, length, text, newTextDocument);

        macroInstantiation = macroInstantiation.createDerivedFile(newTextDocument, lineLengths, parsedMap);

        assertThat(macroInstantiation.getText().toString(), is("thRRRRRee\n"));
        assertThat(macroInstantiation.getFileName(), is(SOURCE_FILE_NAME));
    }

    /**
     * Asserts that {@link MacroInstantiation#getFileName()} returns the file name of the file from which the macro body originated.
     */
    @Test
    public void getFileName() {
        assertThat(MACRO_INSTANTIATION.getFileName(), is(SOURCE_FILE_NAME));
    }

    /**
     * Asserts that {@link MacroInstantiation#MacroInstantiation(SourceLocation)} throws a {@link NullPointerException} when the
     * <code>macroBody</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void macroInstantiationNull() {
        new MacroInstantiation(null);
    }

    /**
     * Asserts that {@link MacroInstantiation#self()} returns <code>this</code>.
     */
    @Test
    public void self() {
        final MacroInstantiation newMacroInstantiation = MACRO_INSTANTIATION.replaceText(2, 1, "r");
        assertThat(newMacroInstantiation, is(sameInstance(MACRO_INSTANTIATION)));
    }

    /**
     * Asserts that {@link MacroInstantiation#textLocationOfTextPosition(int, LineLengthList.TextLocation)} sets the attributes of
     * the specified {@link LineLengthList.TextLocation} to the correct values (and particularly that the location is based on the
     * macro body's location).
     */
    @Test
    public void textLocationOfTextPosition() {
        final LineLengthList.TextLocation textLocation = new LineLengthList.TextLocation();
        MACRO_INSTANTIATION.textLocationOfTextPosition(0, textLocation);
        assertThat(textLocation.lineIndex, is(2));
        assertThat(textLocation.linePosition, is(0));
    }

}
