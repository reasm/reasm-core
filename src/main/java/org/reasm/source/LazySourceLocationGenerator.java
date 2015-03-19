package org.reasm.source;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;

import org.reasm.Architecture;

/**
 * A generator of {@link SourceLocation SourceLocations} in an {@link AbstractSourceFile}.
 *
 * @author Francis Gagné
 */
final class LazySourceLocationGenerator implements Iterator<SourceLocation> {

    @Nonnull
    private final AbstractSourceFile<?> file;
    @Nonnull
    private final Architecture architecture;
    @Nonnull
    private final Iterator<SourceNode> sourceNodeIterator;
    private int textPosition;
    @Nonnull
    private final LineLengthList.TextLocation textLocation = new LineLengthList.TextLocation();

    /**
     * Initializes a new LazySourceLocationGenerator.
     *
     * @param file
     *            the source file that contains the source nodes
     * @param architecture
     *            the architecture under which the source file was parsed and is being assembled
     * @param sourceNodes
     *            an {@link Iterable} of source nodes that will be used to generate the {@link SourceLocation}s
     * @param initialTextPosition
     *            the position of the start of the text of the first {@link SourceLocation} that will be generated
     */
    LazySourceLocationGenerator(@Nonnull AbstractSourceFile<?> file, @Nonnull Architecture architecture,
            @Nonnull Iterable<SourceNode> sourceNodes, int initialTextPosition) {
        this.file = file;
        this.architecture = architecture;
        this.sourceNodeIterator = sourceNodes.iterator();
        this.textPosition = initialTextPosition;
    }

    @Override
    public boolean hasNext() {
        return this.sourceNodeIterator.hasNext();
    }

    @Nonnull
    @Override
    public SourceLocation next() {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }

        // Get the next source node.
        SourceNode sourceNode = this.sourceNodeIterator.next();

        // Create the source location.
        this.file.textLocationOfTextPosition(this.textPosition, this.textLocation);
        SourceLocation sourceLocation = new SourceLocation(this.file, this.architecture, sourceNode, this.textPosition,
                this.textLocation.lineIndex + 1, this.textLocation.linePosition + 1);

        // Determine the value of textPosition for the following source location.
        this.textPosition += sourceNode.getLength();

        return sourceLocation;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
