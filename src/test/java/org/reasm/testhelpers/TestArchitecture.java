package org.reasm.testhelpers;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Architecture;
import org.reasm.source.SourceNode;

import ca.fragag.text.Document;

/**
 * An implementation of {@link Architecture} that always returns the same {@link SourceNode} in {@link #parse(Document)}.
 *
 * @author Francis Gagné
 */
@Immutable
public final class TestArchitecture extends Architecture {

    @Nonnull
    private final SourceNode sourceNode;

    /**
     * Initializes a new TestArchitecture.
     *
     * @param sourceNode
     *            the node to return in {@link #parse(Document)}
     */
    public TestArchitecture(@Nonnull SourceNode sourceNode) {
        super(null);
        this.sourceNode = sourceNode;
    }

    @Nonnull
    @Override
    public SourceNode parse(@Nonnull Document text) {
        return this.sourceNode;
    }

}
