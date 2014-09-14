package org.reasm.source;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.AssemblyBuilder;

/**
 * A composite source node that assembles all its child source nodes in order.
 *
 * @author Francis Gagné
 */
@Immutable
public final class SimpleCompositeSourceNode extends CompositeSourceNode {

    /**
     * Initializes a new SimpleCompositeSourceNode.
     *
     * @param childNodes
     *            the child nodes
     */
    public SimpleCompositeSourceNode(@Nonnull Iterable<? extends SourceNode> childNodes) {
        super(childNodes, null);
    }

    @Override
    protected void assembleCore(@Nonnull AssemblyBuilder builder) {
        builder.enterComposite(true);
    }

}
