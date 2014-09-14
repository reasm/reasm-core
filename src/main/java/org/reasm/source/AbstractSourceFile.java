package org.reasm.source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Architecture;
import org.reasm.ContractViolationException;
import org.reasm.LineBreakCounter;

import ca.fragag.text.CharSequenceReader;
import ca.fragag.text.Document;
import ca.fragag.text.DocumentReader;
import ca.fragag.text.GenericCharSequenceReader;

/**
 * The base class for {@link SourceFile} and {@link MacroInstantiation}.
 *
 * @param <TSelf>
 *            the type of the derived class
 *
 * @author Francis Gagné
 */
@Immutable
public abstract class AbstractSourceFile<TSelf extends AbstractSourceFile<TSelf>> {

    /**
     * Computes the lengths of lines in the specified text.
     *
     * @param text
     *            the text
     * @return a {@link Collection} of the line lengths in the text
     */
    @Nonnull
    static ArrayList<Integer> computeLineLengths(@Nonnull CharSequence text) {
        final ArrayList<Integer> lineLengths = new ArrayList<>();
        final CharSequenceReader<?> reader;
        if (text instanceof Document) {
            reader = new DocumentReader((Document) text);
        } else {
            reader = new GenericCharSequenceReader(text);
        }

        int currentLineStart = 0;
        for (; !reader.atEnd();) {
            final int codePoint = reader.getCurrentCodePoint();
            if (codePoint == '\r') {
                reader.advance();
                if (!reader.atEnd() && reader.getCurrentCodePoint() == '\n') {
                    reader.advance();
                }

                lineLengths.add(reader.getCurrentPosition() - currentLineStart);
                currentLineStart = reader.getCurrentPosition();
            } else if (codePoint == '\n') {
                reader.advance();
                lineLengths.add(reader.getCurrentPosition() - currentLineStart);
                currentLineStart = reader.getCurrentPosition();
            } else {
                reader.advance();
            }
        }

        lineLengths.add(reader.getCurrentPosition() - currentLineStart);
        return lineLengths;
    }

    /**
     * Validates the result of {@link Architecture#parse(Document)} or
     * {@link Architecture#reparse(Document, SourceFile, int, int, int)}.
     *
     * @param result
     *            the result
     * @param text
     *            the text that was to be parsed
     * @param method
     *            the name of the method called (<code>"parse"</code> or <code>"reparse"</code>)
     */
    private static void validateParseResult(@Nonnull SourceNode result, @Nonnull Document text, @Nonnull String method) {
        if (result == null) {
            throw new ContractViolationException("Contract violation: Architecture." + method + "() must not return null.");
        }

        if (result.getLength() != text.length()) {
            throw new ContractViolationException("Contract violation: Architecture." + method
                    + "() must return a SourceNode with the same length as the text it was given (expected " + text.length()
                    + ", got " + result.getLength() + ").");
        }
    }

    @Nonnull
    private final Document text;
    @Nonnull
    private final LineLengthList lineLengths;
    @Nonnull
    private final Map<Architecture, SourceNode> parsedMap;

    /**
     * Initializes a new AbstractSourceFile.
     *
     * @param text
     *            the text of the file
     */
    AbstractSourceFile(@Nonnull Document text) {
        if (text == null) {
            throw new NullPointerException("text");
        }

        this.text = text;
        this.lineLengths = LineLengthList.Factory.INSTANCE.create(computeLineLengths(text));
        this.parsedMap = Collections.synchronizedMap(new HashMap<Architecture, SourceNode>());
    }

    AbstractSourceFile(@Nonnull Document text, @Nonnull LineLengthList lineLengths, @Nonnull Map<Architecture, SourceNode> parsedMap) {
        this.text = text;
        this.lineLengths = lineLengths;
        this.parsedMap = Collections.synchronizedMap(parsedMap);
    }

    /**
     * Gets the name of this source file. May be <code>null</code>.
     *
     * @return the name of the file
     */
    @CheckForNull
    public abstract String getFileName();

    /**
     * Gets the root source node of this source file for the specified architecture.
     *
     * @param architecture
     *            the architecture
     * @return the file's root {@link SourceNode}
     */
    @Nonnull
    public final SourceNode getParsed(@Nonnull Architecture architecture) {
        if (architecture == null) {
            throw new NullPointerException("architecture");
        }

        synchronized (this.parsedMap) {
            SourceNode result = this.parsedMap.get(architecture);
            if (result == null) {
                result = architecture.parse(this.text);
                validateParseResult(result, this.text, "parse");
                this.parsedMap.put(architecture, result);
            }

            return result;
        }
    }

    /**
     * Gets a list of {@link SourceLocation SourceLocations} for the root source node of this source file for the specified
     * architecture. The list contains only one source location.
     *
     * @param architecture
     *            the architecture
     * @return the list of {@link SourceLocation SourceLocations} for the root source node
     */
    @Nonnull
    public final List<SourceLocation> getSourceLocations(@Nonnull Architecture architecture) {
        if (architecture == null) {
            throw new NullPointerException("architecture");
        }

        return new SourceLocationList(this, architecture, Collections.singletonList(this.getParsed(architecture)), 0);
    }

    /**
     * Gets the contents of this source file.
     *
     * @return a {@link Document} that exposes the contents of the file
     */
    @Nonnull
    public final Document getText() {
        return this.text;
    }

    /**
     * Creates a new source file with the contents of this source file with the replacement specified by the arguments and the file
     * name of this source file. The new source file is immediately re-parsed unless the whole contents are replaced.
     *
     * @param offset
     *            the offset at which the replacement occurs
     * @param length
     *            the length of text to remove at the specified offset
     * @param text
     *            the new text to insert at the specified offset
     * @return the new SourceFile
     */
    @Nonnull
    public final TSelf replaceText(int offset, int length, @Nonnull String text) {
        if (text == null) {
            throw new NullPointerException("text");
        }

        // If the whole contents are being replaced, don't parse the contents immediately.
        if (offset == 0 && length == this.text.length()) {
            return this.setText(text);
        }

        final Document newTextDocument = this.text.replace(offset, length, text);

        // If Document.replace() returned the original Document instance, the text didn't change. Return the current instance.
        if (newTextDocument == this.text) {
            return this.self();
        }

        // Adjust the line lengths.
        final LineLengthList lineLengths = this.adjustLineLengths(offset, length, text);

        // Re-parse the edited source file for each architecture for which this source file had been parsed.
        final HashMap<Architecture, SourceNode> parsedMap = this.reparse(offset, length, text, newTextDocument);

        return this.createDerivedFile(newTextDocument, lineLengths, parsedMap);
    }

    /**
     * Creates a new source file with the specified contents and the file name of this source file. The new source file is not
     * parsed initially.
     *
     * @param text
     *            the contents of the file
     * @return the new SourceFile
     */
    @Nonnull
    public final TSelf setText(@Nonnull Document text) {
        if (text == null) {
            throw new NullPointerException("text");
        }

        return this.createDerivedFile(text);
    }

    /**
     * Creates a new source file with the specified contents and the file name of this source file. The new source file is not
     * parsed initially.
     *
     * @param text
     *            the contents of the file
     * @return the new SourceFile
     */
    @Nonnull
    public final TSelf setText(@Nonnull String text) {
        if (text == null) {
            throw new NullPointerException("text");
        }

        return this.createDerivedFile(new Document(text));
    }

    /**
     * Creates a new {@link LineLengthList} corresponding to the line lengths of this file's text with the specified replacement.
     *
     * @param offset
     *            the offset at which the replacement occurs
     * @param length
     *            the length of text to remove at the specified offset
     * @param text
     *            the new text to insert at the specified offset
     * @return the new {@link LineLengthList}
     */
    @Nonnull
    final LineLengthList adjustLineLengths(int offset, int length, @Nonnull String text) {
        LineLengthList lineLengths = this.lineLengths;
        final ArrayList<Integer> newTextLineLengths = computeLineLengths(text);
        final int numberOfLinesInOldText = LineBreakCounter.count(this.text.subSequence(offset, offset + length)) + 1;
        final int numberOfLinesInNewText = newTextLineLengths.size();

        assert numberOfLinesInOldText >= 1;
        assert numberOfLinesInNewText >= 1;

        final LineLengthList.TextLocation textLocation = new LineLengthList.TextLocation();

        lineLengths.textLocationOfTextPosition(offset, textLocation);
        int lineIndex = textLocation.lineIndex;
        final int numberOfCharsOnFirstLineBeforeReplacement = textLocation.linePosition;

        if (length > 0) {
            lineLengths.textLocationOfTextPosition(offset + length, textLocation);
        }

        final int numberOfCharsOnLastLineAfterReplacement = lineLengths.get(textLocation.lineIndex) - textLocation.linePosition;

        for (int i = 0;; i++) {
            if (i < numberOfLinesInNewText) {
                final int lineLength = (i == 0 ? numberOfCharsOnFirstLineBeforeReplacement : 0)
                        + (i == numberOfLinesInNewText - 1 ? numberOfCharsOnLastLineAfterReplacement : 0)
                        + newTextLineLengths.get(i);
                if (i < numberOfLinesInOldText) {
                    // Update the line length.
                    if (lineLength != lineLengths.get(lineIndex)) {
                        lineLengths = LineLengthList.Factory.INSTANCE.set(lineLengths, lineIndex, lineLength);
                    }
                } else {
                    // Add a line.
                    lineLengths = LineLengthList.Factory.INSTANCE.add(lineLengths, lineIndex, lineLength);
                }

                lineIndex++;
            } else {
                if (i < numberOfLinesInOldText) {
                    // Remove the line.
                    lineLengths = LineLengthList.Factory.INSTANCE.remove(lineLengths, lineIndex);
                } else {
                    break;
                }
            }
        }

        return lineLengths;
    }

    @Nonnull
    abstract TSelf createDerivedFile(@Nonnull Document text);

    @Nonnull
    abstract TSelf createDerivedFile(@Nonnull Document text, @Nonnull LineLengthList lineLengths,
            @Nonnull HashMap<Architecture, SourceNode> parsedMap);

    /**
     * Parses an edited source file for each architecture for which this source file had been parsed using existing parsed
     * information from this source file.
     *
     * @param offset
     *            the offset at which the replacement occurs
     * @param length
     *            the length of text to remove at the specified offset
     * @param text
     *            the new text to insert at the specified offset
     * @param newTextDocument
     *            the contents of the edited source file
     * @return the re-parsed information
     */
    @Nonnull
    final HashMap<Architecture, SourceNode> reparse(int offset, int length, @Nonnull String text, @Nonnull Document newTextDocument) {
        final HashMap<Architecture, SourceNode> parsedMap = new HashMap<>();

        synchronized (this.parsedMap) {
            for (final Map.Entry<Architecture, SourceNode> entry : this.parsedMap.entrySet()) {
                final Architecture architecture = entry.getKey();
                final SourceNode result = architecture.reparse(newTextDocument, this, offset, length, text.length());
                validateParseResult(result, newTextDocument, "reparse");
                parsedMap.put(architecture, result);
            }
        }

        return parsedMap;
    }

    @Nonnull
    abstract TSelf self();

    /**
     * Writes the location of the specified position in <code>textLocation</code>.
     *
     * @param textPosition
     *            the text position
     * @param textLocation
     *            the text location
     */
    void textLocationOfTextPosition(int textPosition, @Nonnull LineLengthList.TextLocation textLocation) {
        this.lineLengths.textLocationOfTextPosition(textPosition, textLocation);
    }

}
