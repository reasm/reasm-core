package org.reasm.source;

import java.util.HashMap;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Architecture;

import ca.fragag.text.Document;

/**
 * The instantiated text of a macro, with operands substituted.
 * <p>
 * To use this class, first instantiate it by calling {@link #MacroInstantiation(SourceLocation)}, passing a {@link SourceLocation}
 * that contains the macro's body. Then, call {@link #replaceText(int, int, String)} to perform text substitutions.
 *
 * @author Francis Gagné
 */
@Immutable
public final class MacroInstantiation extends AbstractSourceFile<MacroInstantiation> {

    @Nonnull
    private final SourceLocation macroBody;

    /**
     * Initializes a new MacroInstantiation.
     *
     * @param macroBody
     *            a {@link SourceLocation} that contains the macro's body. Line numbers for {@link SourceLocation} objects created
     *            from this MacroInstantiation will start from the line number of that {@link SourceLocation}.
     */
    public MacroInstantiation(@Nonnull SourceLocation macroBody) {
        super(new Document(Objects.requireNonNull(macroBody, "macroBody").getText()));
        this.macroBody = macroBody;
    }

    private MacroInstantiation(@Nonnull Document text, @Nonnull SourceLocation macroBody) {
        super(text);
        this.macroBody = macroBody;
    }

    private MacroInstantiation(@Nonnull Document text, @Nonnull SourceLocation macroBody, @Nonnull LineLengthList lineLengths,
            @Nonnull HashMap<Architecture, SourceNode> parsedMap) {
        super(text, lineLengths, parsedMap);
        this.macroBody = macroBody;
    }

    @CheckForNull
    @Override
    public final String getFileName() {
        return this.macroBody.getFile().getFileName();
    }

    @Nonnull
    @Override
    final MacroInstantiation createDerivedFile(@Nonnull Document text) {
        return new MacroInstantiation(text, this.macroBody);
    }

    @Nonnull
    @Override
    final MacroInstantiation createDerivedFile(@Nonnull Document text, @Nonnull LineLengthList lineLengths,
            @Nonnull HashMap<Architecture, SourceNode> parsedMap) {
        return new MacroInstantiation(text, this.macroBody, lineLengths, parsedMap);
    }

    @Nonnull
    @Override
    final MacroInstantiation self() {
        return this;
    }

    @Nonnull
    @Override
    void textLocationOfTextPosition(int textPosition, @Nonnull LineLengthList.TextLocation textLocation) {
        super.textLocationOfTextPosition(textPosition, textLocation);
        textLocation.lineIndex += this.macroBody.getLineNumber() - 1;
    }

}
