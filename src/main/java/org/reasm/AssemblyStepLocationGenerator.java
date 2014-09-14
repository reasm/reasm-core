package org.reasm;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.source.SourceLocation;

/**
 * A generator of {@link AssemblyStepLocation} objects.
 *
 * @author Francis Gagné
 */
final class AssemblyStepLocationGenerator implements Iterator<AssemblyStepLocation> {

    @Nonnull
    private final Iterable<SourceLocation> sourceLocationIterable;
    @CheckForNull
    private final AssemblyStepIterationController iterationController;
    @CheckForNull
    private final AssemblyStepLocation parentAssemblyStepLocation;
    private final boolean transparentParent;

    @CheckForNull
    private Iterator<SourceLocation> sourceLocationIterator;
    private long iterationNumber;

    /**
     * Initializes a new AssemblyStepLocationGenerator.
     *
     * @param sourceLocationIterable
     *            an {@link Iterable} of {@link SourceLocation} objects
     * @param iterationController
     *            a {@link AssemblyStepIterationController} indicating whether to perform a new iteration when the end of
     *            <code>sourceLocationIterable</code> is reached
     * @param parentAssemblyStepLocation
     *            the parent assembly step location of the {@link AssemblyStepLocation} objects produced by this iterator
     */
    AssemblyStepLocationGenerator(@Nonnull Iterable<SourceLocation> sourceLocationIterable,
            @CheckForNull AssemblyStepIterationController iterationController,
            @CheckForNull AssemblyStepLocation parentAssemblyStepLocation, boolean transparentParent) {
        this.sourceLocationIterable = sourceLocationIterable;
        this.iterationController = iterationController;
        this.parentAssemblyStepLocation = parentAssemblyStepLocation;
        this.transparentParent = transparentParent;

        if (iterationController == null) {
            this.sourceLocationIterator = this.sourceLocationIterable.iterator();
        }
    }

    @Override
    public final boolean hasNext() {
        for (;;) {
            if (this.sourceLocationIterator != null && this.sourceLocationIterator.hasNext()) {
                return true;
            }

            this.sourceLocationIterator = null;
            if (this.iterationController == null || !this.iterationController.hasNextIteration()) {
                return false;
            }

            this.iterationNumber++;
            this.sourceLocationIterator = this.sourceLocationIterable.iterator();
        }
    }

    @Override
    public final AssemblyStepLocation next() {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }

        final Iterator<SourceLocation> sourceLocationIterator = this.sourceLocationIterator;
        assert sourceLocationIterator != null;
        return new AssemblyStepLocation(sourceLocationIterator.next(), this.iterationNumber, this.parentAssemblyStepLocation,
                this.transparentParent);
    }

    @Override
    public final void remove() {
        throw new UnsupportedOperationException();
    }

}
