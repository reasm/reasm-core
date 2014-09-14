package org.reasm.source;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Architecture;

import ca.fragag.text.Document;

/**
 * A source file. The main source file and each included source file are represented by an instance of the SourceFile class.
 *
 * @author Francis Gagné
 */
@Immutable
public final class SourceFile extends AbstractSourceFile<SourceFile> {

    @CheckForNull
    private final String fileName;

    /**
     * Initializes a new source file.
     *
     * @param text
     *            the text of the file
     * @param fileName
     *            the name of the file, if available
     */
    public SourceFile(@Nonnull Document text, @CheckForNull String fileName) {
        super(text);
        this.fileName = fileName;
    }

    /**
     * Initializes a new source file.
     *
     * @param text
     *            the text of the file
     * @param fileName
     *            the name of the file, if available
     */
    public SourceFile(@Nonnull String text, @CheckForNull String fileName) {
        this(new Document(Objects.requireNonNull(text, "text")), fileName);
    }

    private SourceFile(@Nonnull Document text, @CheckForNull String fileName, @Nonnull LineLengthList lineLengths,
            @Nonnull Map<Architecture, SourceNode> parsedMap) {
        super(text, lineLengths, parsedMap);
        this.fileName = fileName;
    }

    @CheckForNull
    @Override
    public final String getFileName() {
        return this.fileName;
    }

    @Nonnull
    @Override
    final SourceFile createDerivedFile(Document text) {
        return new SourceFile(text, this.fileName);
    }

    @Nonnull
    @Override
    final SourceFile createDerivedFile(Document text, LineLengthList lineLengths, HashMap<Architecture, SourceNode> parsedMap) {
        return new SourceFile(text, this.fileName, lineLengths, parsedMap);
    }

    @Nonnull
    @Override
    final SourceFile self() {
        return this;
    }

}
