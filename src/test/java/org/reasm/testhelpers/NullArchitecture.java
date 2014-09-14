package org.reasm.testhelpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Architecture;
import org.reasm.AssemblyBuilder;
import org.reasm.messages.UnknownMnemonicErrorMessage;
import org.reasm.source.AbstractSourceFile;
import org.reasm.source.CompositeSourceNode;
import org.reasm.source.ParseError;
import org.reasm.source.SimpleCompositeSourceNode;
import org.reasm.source.SourceNode;

import ca.fragag.text.Document;
import ca.fragag.text.DocumentReader;

/**
 * Implements an architecture that parses a source file by splitting it on line breaks, always adds an
 * <code>UnknownMnemonicErrorMessage</code> when its <code>assemble</code> method is called and doesn't evaluate expressions.
 *
 * @author Francis Gagné
 */
@Immutable
public class NullArchitecture extends Architecture {

    @Immutable
    private static class NullSourceNode extends SourceNode {

        public NullSourceNode(int length, @CheckForNull ParseError parseError) {
            super(length, parseError);
        }

        @Override
        protected void assembleCore(@Nonnull AssemblyBuilder builder) {
            builder.addMessage(new UnknownMnemonicErrorMessage());
        }

    }

    /** A default instance of NullArchitecture with no names. */
    public static final NullArchitecture DEFAULT = new NullArchitecture();

    @Nonnull
    static SourceNode parse(@Nonnull DocumentReader reader) {
        final int start = reader.getCurrentPosition();

        while (!reader.atEnd()) {
            int codePoint = reader.getCurrentCodePoint();
            if (codePoint == '\r') {
                reader.advance();
                codePoint = reader.getCurrentCodePoint();
                if (codePoint == '\n') {
                    reader.advance();
                }

                break;
            }

            if (codePoint == '\n') {
                reader.advance();
                break;
            }

            reader.advance();
        }

        return new NullSourceNode(reader.getCurrentPosition() - start, null);
    }

    /**
     * Initializes a new NullArchitecture.
     */
    public NullArchitecture() {
        super(null);
    }

    /**
     * Initializes a new NullArchitecture with the specified names.
     *
     * @param names
     *            the names of the architecture
     */
    public NullArchitecture(@Nonnull String... names) {
        super(Arrays.asList(names));
    }

    @Nonnull
    @Override
    public SourceNode parse(@Nonnull Document text) {
        ArrayList<SourceNode> nodes = new ArrayList<>();
        DocumentReader reader = new DocumentReader(text);
        while (!reader.atEnd()) {
            nodes.add(parse(reader));
        }

        return new SimpleCompositeSourceNode(nodes);
    }

    @Nonnull
    @Override
    public SourceNode reparse(@Nonnull Document text, @Nonnull AbstractSourceFile<?> oldSourceFile, int replaceOffset,
            int lengthToRemove, int lengthToInsert) {
        final int endOfReplacedRange = replaceOffset - lengthToRemove + lengthToInsert;
        final SourceNode oldParsed = oldSourceFile.getParsed(this);
        if (oldParsed.getClass() == SimpleCompositeSourceNode.class) {
            List<SourceNode> oldNodes = ((CompositeSourceNode) oldParsed).getChildNodes();
            Iterator<SourceNode> oldNodesIterator = oldNodes.iterator();
            SourceNode oldNode = null;

            ArrayList<SourceNode> newNodes = new ArrayList<>();
            int newTextPosition = 0;

            // Reuse nodes from the old source file that are before replaceOffset.
            for (; oldNodesIterator.hasNext();) {
                oldNode = oldNodesIterator.next();
                if (newTextPosition >= replaceOffset) {
                    break;
                }

                if (newTextPosition <= replaceOffset + oldNode.getLength()) {
                    newNodes.add(oldNode);
                    newTextPosition += oldNode.getLength();
                }
            }

            int catchupPosition = newTextPosition - lengthToRemove + lengthToInsert;
            DocumentReader reader = new DocumentReader(text, newTextPosition);
            while (!reader.atEnd()) {
                final SourceNode newNode = parse(reader);
                newNodes.add(newNode);
                newTextPosition += newNode.getLength();

                // If we haven't reached the end of the replaced range, continue parsing.
                if (newTextPosition < endOfReplacedRange) {
                    continue;
                }

                while (oldNodesIterator.hasNext() && catchupPosition < newTextPosition) {
                    oldNode = oldNodesIterator.next();
                    catchupPosition += oldNode.getLength();
                }

                // If we managed to catch up on matching nodes after the replaced range, stop parsing and reuse them.
                if (oldNode != null && catchupPosition == newTextPosition) {
                    newNodes.add(oldNode);

                    while (oldNodesIterator.hasNext()) {
                        newNodes.add(oldNodesIterator.next());
                    }

                    break;
                }
            }

            return new SimpleCompositeSourceNode(newNodes);
        }

        // Fall back to the default implementation.
        return super.reparse(text, oldSourceFile, replaceOffset, lengthToRemove, lengthToInsert);
    }

}
