package org.reasm.source;

import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.SubstringBounds;

import ca.fragag.text.DocumentReader;
import ca.fragag.text.RangedCharSequenceReader;

/**
 * Provides a means to read the characters and/or code points, one at a time, of a range of text in a source node.
 * <p>
 * SourceNodeRangeReader objects may use a {@link SkipHandler} to skip some code points so as to, for example, skip continuation
 * characters to expose a "logical line" as if it was written on a single source line.
 * <p>
 * Instances of this class can be copied through the copy constructor (
 * {@link SourceNodeRangeReader#SourceNodeRangeReader(SourceNodeRangeReader)}) to implement backtracking.
 *
 * @author Francis Gagné
 */
public final class SourceNodeRangeReader {

    /**
     * Determines what characters are to be skipped when reading characters from a source node.
     * <p>
     * Subclasses should override {@link #skipCurrentCodePoint()} to specify whether a character should be returned by the reader or
     * skipped. Subclasses should also override {@link #clone()} to return an instance of the subclass.
     *
     * @author Francis Gagné
     */
    public static class SkipHandler implements Cloneable {

        @CheckForNull
        private SourceNodeRangeReader reader;

        /**
         * Initializes a new SkipHandler.
         */
        public SkipHandler() {
        }

        /**
         * Clones this SkipHandler.
         *
         * @return a new SkipHandler with the same state as this SkipHandler.
         */
        @Override
        public SkipHandler clone() {
            try {
                return (SkipHandler) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new Error("CloneNotSupportedException thrown on a Cloneable type", e);
            }
        }

        /**
         * Gets the {@link SourceNodeRangeReader} associated with this skip handler.
         *
         * @return the reader associated with this skip handler.
         */
        @CheckForNull
        public final SourceNodeRangeReader getReader() {
            return this.reader;
        }

        /**
         * Determines whether the current code point should be skipped by the reader.
         * <p>
         * Note to implementers: You should avoid calling {@link #advance()} or {@link #readToString()} from this method; this
         * causes recursion. Instead, return <code>true</code> and maintain some state in your class, if necessary.
         *
         * @return <code>true</code> if the code point at the current position should be skipped; <code>false</code> otherwise. The
         *         default implementation always returns <code>false</code>.
         */
        protected boolean skipCurrentCodePoint() {
            return false;
        }

        /**
         * Sets the {@link SourceNodeRangeReader} associated with this skip handler.
         *
         * @param reader
         *            the reader to set
         */
        final void setReader(@Nonnull SourceNodeRangeReader reader) {
            this.reader = reader;
        }

    }

    private static int restrictEndBound(int start, int end, @Nonnull SourceLocation sourceLocation) {
        if (end < start) {
            return start;
        }

        if (end > sourceLocation.getSourceNode().getLength()) {
            return sourceLocation.getSourceNode().getLength();
        }

        return end;
    }

    private static int restrictStartBound(int start, @Nonnull SourceLocation sourceLocation) {
        if (start < 0) {
            return 0;
        }

        if (start > sourceLocation.getSourceNode().getLength()) {
            return sourceLocation.getSourceNode().getLength();
        }

        return start;
    }

    @Nonnull
    private final SourceLocation sourceLocation;
    @Nonnull
    private final DocumentReader reader;
    @Nonnull
    private final RangedCharSequenceReader rangedReader;
    @Nonnull
    private final SkipHandler skipHandler;

    /**
     * Initializes a new SourceNodeRangeReader.
     *
     * @param sourceLocation
     *            the source location
     * @param start
     *            the start (inclusive) of the range to read
     * @param end
     *            the end (exclusive) of the range to read
     */
    public SourceNodeRangeReader(@Nonnull SourceLocation sourceLocation, int start, int end) {
        this(sourceLocation, start, end, null);
    }

    /**
     * Initializes a new SourceNodeRangeReader.
     *
     * @param sourceLocation
     *            the source location
     * @param start
     *            the start (inclusive) of the range to read
     * @param end
     *            the end (exclusive) of the range to read
     * @param skipHandler
     *            an object that will determine characters to skip while reading a source node. Specify <code>null</code> to skip no
     *            characters.
     */
    public SourceNodeRangeReader(@Nonnull SourceLocation sourceLocation, int start, int end, @CheckForNull SkipHandler skipHandler) {
        if (sourceLocation == null) {
            throw new NullPointerException("sourceLocation");
        }

        this.sourceLocation = sourceLocation;
        start = restrictStartBound(start, sourceLocation);
        end = restrictEndBound(start, end, sourceLocation);
        final int sourceLocationTextPosition = sourceLocation.getTextPosition();
        this.reader = new DocumentReader(sourceLocation.getFile().getText());
        this.rangedReader = new RangedCharSequenceReader(this.reader, start + sourceLocationTextPosition, end
                + sourceLocationTextPosition);
        if (skipHandler == null) {
            skipHandler = new SkipHandler();
        }

        this.skipHandler = skipHandler;
        skipHandler.setReader(this);
        this.checkForCodePointsToSkip();
    }

    /**
     * Initializes a new SourceNodeRangeReader.
     *
     * @param sourceLocation
     *            the source location
     * @param bounds
     *            the start (inclusive) and end (exclusive) of the range to read
     */
    public SourceNodeRangeReader(@Nonnull SourceLocation sourceLocation, @Nonnull SubstringBounds bounds) {
        this(sourceLocation, Objects.requireNonNull(bounds, "bounds").getStart(), bounds.getEnd(), null);
    }

    /**
     * Initializes a new SourceNodeRangeReader.
     *
     * @param sourceLocation
     *            the source location
     * @param bounds
     *            the start (inclusive) and end (exclusive) of the range to read
     * @param skipHandler
     *            an object that will determine characters to skip while reading a source node. Specify <code>null</code> to skip no
     *            characters.
     */
    public SourceNodeRangeReader(@Nonnull SourceLocation sourceLocation, @Nonnull SubstringBounds bounds,
            @CheckForNull SkipHandler skipHandler) {
        this(sourceLocation, Objects.requireNonNull(bounds, "bounds").getStart(), bounds.getEnd(), skipHandler);
    }

    /**
     * Initializes a new SourceNodeRangeReader from an existing SourceNodeRangeReader. The objects are independent; advancing one
     * does not advance the other. This can be used to implement backtracking.
     *
     * @param other
     *            the SourceNodeRangeReader to copy
     */
    public SourceNodeRangeReader(@Nonnull SourceNodeRangeReader other) {
        if (other == null) {
            throw new NullPointerException("other");
        }

        this.sourceLocation = other.sourceLocation;
        this.reader = new DocumentReader(other.reader.getDocument());
        this.rangedReader = new RangedCharSequenceReader(this.reader, other.rangedReader.getStart(), other.rangedReader.getEnd());
        this.reader.setCurrentPosition(other.reader.getCurrentPosition());
        this.skipHandler = other.skipHandler.clone();
    }

    /**
     * Advances the reader to the next Unicode code point, potentially skipping code points depending on the implementation of
     * {@link SkipHandler#skipCurrentCodePoint()}.
     */
    public final void advance() {
        this.rangedReader.advance();
        this.checkForCodePointsToSkip();
    }

    /**
     * Gets a value indicating whether the end of the range has been reached.
     *
     * @return <code>true</code> if the end of the range has been reached; otherwise, <code>false</code>
     */
    public final boolean atEnd() {
        return this.rangedReader.atEnd();
    }

    /**
     * Gets the current code point, or -1 if the end of the range has been reached.
     *
     * @return the current code point, or -1 if the end of the range has been reached
     */
    public final int getCurrentCodePoint() {
        return this.rangedReader.getCurrentCodePoint();
    }

    /**
     * Gets the current position of this reader within the range.
     *
     * @return the current position
     */
    public final int getCurrentPosition() {
        return this.rangedReader.getCurrentPosition();
    }

    /**
     * Gets the current position of this reader within the {@link SourceNode}.
     *
     * @return the current position
     */
    public final int getCurrentPositionInSourceNode() {
        return this.reader.getCurrentPosition() - this.sourceLocation.getTextPosition();
    }

    /**
     * Gets the source location that was used to construct this {@link SourceNodeRangeReader}.
     *
     * @return the source location
     */
    @Nonnull
    public final SourceLocation getSourceLocation() {
        return this.sourceLocation;
    }

    /**
     * Reads all characters from the current position to the end of this reader into a string.
     *
     * @return a {@link String} containing the characters from the current position to the end of the reader
     */
    @Nonnull
    public final String readToString() {
        StringBuilder sb = new StringBuilder();
        while (this.getCurrentCodePoint() != -1) {
            sb.appendCodePoint(this.getCurrentCodePoint());
            this.advance();
        }

        return sb.toString();
    }

    /**
     * Checks if code points need to be skipped, and skips them until a code point that must not be skipped is found.
     */
    private final void checkForCodePointsToSkip() {
        for (;;) {
            // If we have reached the end of the specified range or of the source node, return.
            if (this.rangedReader.atEnd()) {
                return;
            }

            // Check if the character at the current position should be skipped or not.
            if (this.skipHandler.skipCurrentCodePoint()) {
                // Skip this character and repeat.
                this.rangedReader.advance();
                continue;
            }

            break;
        }
    }

}
