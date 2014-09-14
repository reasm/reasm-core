package org.reasm.source;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Architecture;
import org.reasm.SubstringBounds;

/**
 * A source location. A source location references a source node in its context (source file and architecture). The text position,
 * line number and line position are available.
 * <p>
 * To obtain SourceLocations, call {@link AbstractSourceFile#getSourceLocations(Architecture)} or
 * {@link SourceLocation#getChildSourceLocations()}.
 * <p>
 * This class is immutable.
 *
 * @author Francis Gagné
 */
@Immutable
public final class SourceLocation {

    @Nonnull
    private final AbstractSourceFile<?> file;
    @Nonnull
    private final Architecture architecture;
    @Nonnull
    private final SourceNode sourceNode;
    private final int textPosition;
    private final int lineNumber;
    private final int linePosition;

    /**
     * Initializes a new source location.
     *
     * @param file
     *            the source file
     * @param architecture
     *            the architecture under which the source file was parsed
     * @param sourceNode
     *            the source node
     * @param textPosition
     *            the position of the start of the text for this source node in the source file
     * @param lineNumber
     *            the line number, starting from 1
     * @param linePosition
     *            the position on the line, starting from 1
     */
    SourceLocation(@Nonnull AbstractSourceFile<?> file, @Nonnull Architecture architecture, @Nonnull SourceNode sourceNode,
            int textPosition, int lineNumber, int linePosition) {
        this.file = file;
        this.architecture = architecture;
        this.sourceNode = sourceNode;
        this.textPosition = textPosition;
        this.lineNumber = lineNumber;
        this.linePosition = linePosition;
    }

    @Override
    public final boolean equals(@CheckForNull Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        final SourceLocation other = (SourceLocation) obj;

        if (!this.file.equals(other.file)) {
            return false;
        }

        if (!this.architecture.equals(other.architecture)) {
            return false;
        }

        if (!this.sourceNode.equals(other.sourceNode)) {
            return false;
        }

        if (this.textPosition != other.textPosition) {
            return false;
        }

        if (this.lineNumber != other.lineNumber) {
            return false;
        }

        if (this.linePosition != other.linePosition) {
            return false;
        }

        return true;
    }

    /**
     * Gets the architecture under which the source file was parsed and is being assembled.
     *
     * @return the architecture
     */
    @Nonnull
    public final Architecture getArchitecture() {
        return this.architecture;
    }

    /**
     * Gets a list of {@link SourceLocation SourceLocations} for the child nodes of {@linkplain #getSourceNode() the source node
     * referenced by this source location}. The source node must be a {@link CompositeSourceNode}.
     *
     * @return the list of {@link SourceLocation SourceLocations} for the child nodes
     */
    @Nonnull
    public final List<SourceLocation> getChildSourceLocations() {
        return new SourceLocationList(this.file, this.architecture, ((CompositeSourceNode) this.sourceNode).getChildNodes(),
                this.textPosition);
    }

    /**
     * Gets the source file of this source location.
     *
     * @return the source file
     */
    @Nonnull
    public final AbstractSourceFile<?> getFile() {
        return this.file;
    }

    /**
     * Gets the line number of this source location.
     *
     * @return the line number
     */
    public final int getLineNumber() {
        return this.lineNumber;
    }

    /**
     * Gets the position on the line of this source location.
     *
     * @return the line position
     */
    public final int getLinePosition() {
        return this.linePosition;
    }

    /**
     * Gets the source node of this source location.
     *
     * @return the source node
     */
    @Nonnull
    public final SourceNode getSourceNode() {
        return this.sourceNode;
    }

    /**
     * Gets the text on the source node at this source location.
     *
     * @return the text of the source node
     */
    @Nonnull
    public final CharSequence getText() {
        return this.file.getText().subSequence(this.textPosition, this.textPosition + this.sourceNode.getLength());
    }

    /**
     * Gets the position of the start of the text for the source node in the source file.
     *
     * @return the position of the start of the text
     */
    public final int getTextPosition() {
        return this.textPosition;
    }

    /**
     * Gets the text on the source node at this source location using a {@link SourceNodeRangeReader}.
     *
     * @return a {@link SourceNodeRangeReader} that reads all the characters on the source node
     */
    @Nonnull
    public final SourceNodeRangeReader getTextReader() {
        return new SourceNodeRangeReader(this, 0, Integer.MAX_VALUE, null);
    }

    /**
     * Gets the text of a substring of this source node within the specified bounds.
     *
     * @param start
     *            the starting bound (inclusive) of the substring
     * @param end
     *            the ending bound (exclusive) of the substring
     * @return the substring
     */
    @Nonnull
    public final String getTextSubstring(int start, int end) {
        return new SourceNodeRangeReader(this, start, end, null).readToString();
    }

    /**
     * Gets the text of a substring of this source node within the specified bounds.
     *
     * @param bounds
     *            the bounds of the substring
     * @return the substring
     */
    @Nonnull
    public final String getTextSubstring(SubstringBounds bounds) {
        return new SourceNodeRangeReader(this, bounds, null).readToString();
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.file.hashCode();
        result = prime * result + this.architecture.hashCode();
        result = prime * result + this.sourceNode.hashCode();
        result = prime * result + this.textPosition;
        result = prime * result + this.lineNumber;
        result = prime * result + this.linePosition;
        return result;
    }

}
