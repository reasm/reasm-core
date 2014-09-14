package org.reasm.testhelpers;

import java.io.IOException;
import java.util.ArrayList;
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
 * Implements an architecture that provides 256 instructions that, when assembled, generate a single byte whose value is the
 * hexadecimal value of the instruction name. For example, the instruction {@code 00} produces a single {@code 00} byte.
 *
 * @author Francis Gagné
 */
@Immutable
public class HexArchitecture extends Architecture {

    @Immutable
    private static class HexSourceNode extends SourceNode {

        public HexSourceNode(int length, @CheckForNull ParseError parseError) {
            super(length, parseError);
        }

        @Override
        protected void assembleCore(@Nonnull AssemblyBuilder builder) throws IOException {
            final String octet = builder.getStep().getLocation().getSourceLocation().getTextReader().readToString().trim();
            if (octet.length() == 2) {
                try {
                    final int value = Integer.parseInt(octet, 16);
                    builder.appendAssembledData((byte) value);
                    return;
                } catch (NumberFormatException e) {
                }
            }

            builder.addMessage(new UnknownMnemonicErrorMessage());
        }

    }

    /**
     * The unique instance of the <code>HexArchitecture</code> class.
     */
    public static final HexArchitecture INSTANCE = new HexArchitecture();

    private static boolean isHexDigit(char ch) {
        return ch >= '0' && ch <= '9' || ch >= 'A' && ch <= 'F' || ch >= 'a' && ch <= 'f';
    }

    @Nonnull
    private static SourceNode parse(@Nonnull DocumentReader reader) {
        final int start = reader.getCurrentPosition();

        // Skip initial whitespace.
        if (skipWhitespace(reader)) {
            // Read an octet (2 hex characters).
            final int octetStart = reader.getCurrentPosition();
            for (; reader.getCurrentPosition() - octetStart < 2; reader.advance()) {
                if (reader.atEnd() && reader.getCurrentPosition() - octetStart > 0) {
                    return new HexSourceNode(reader.getCurrentPosition() - start, new ParseError("Incomplete octet") {
                    });
                }

                char ch = reader.getCurrentChar();
                if (!isHexDigit(ch)) {
                    return new HexSourceNode(reader.getCurrentPosition() - start, new ParseError("Unexpected character: '" + ch
                            + "'") {
                    });
                }
            }

            // Skip trailing whitespace.
            skipWhitespace(reader);
        }

        return new HexSourceNode(reader.getCurrentPosition() - start, null);
    }

    private static final boolean skipWhitespace(@Nonnull DocumentReader reader) {
        for (;; reader.advance()) {
            if (reader.atEnd()) {
                return false;
            }

            final int codePoint = reader.getCurrentCodePoint();
            if (!Character.isWhitespace(codePoint)) {
                return true;
            }
        }
    }

    /**
     * Initializes the unique HexArchitecture.
     */
    private HexArchitecture() {
        super(null);
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
