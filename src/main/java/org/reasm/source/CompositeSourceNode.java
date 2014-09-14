package org.reasm.source;

import java.util.List;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import ca.fragag.collections.ImmutableTreeList;
import ca.fragag.collections.ImmutableTreeListFactory;

/**
 * A source node that is composed of other source nodes.
 *
 * @author Francis Gagné
 */
@Immutable
public abstract class CompositeSourceNode extends SourceNode {

    /**
     * Gets the sum of the lengths of the specified sequence of source nodes. The result will be the length of a composite source
     * node having this sequence of source nodes as children.
     *
     * @param childNodes
     *            the child nodes of a composite source node being created
     * @return the sum of the lengths of the child nodes
     */
    private static int totalLength(@Nonnull Iterable<? extends SourceNode> childNodes) {
        int result = 0;
        for (SourceNode node : childNodes) {
            result += node.getLength();
        }

        return result;
    }

    @Nonnull
    private final ImmutableTreeList<SourceNode> childNodes;

    /**
     * Initializes a new composite source node.
     *
     * @param childNodes
     *            the child nodes
     * @param parseError
     *            the parse error on the source node, or <code>null</code> if no parse error occurred
     */
    public CompositeSourceNode(@Nonnull Iterable<? extends SourceNode> childNodes, @CheckForNull ParseError parseError) {
        this(ImmutableTreeListFactory.<SourceNode> getInstance().create(Objects.requireNonNull(childNodes, "childNodes")),
                parseError);
    }

    private CompositeSourceNode(@Nonnull ImmutableTreeList<SourceNode> childNodes, @CheckForNull ParseError parseError) {
        super(totalLength(childNodes), parseError);
        this.childNodes = childNodes;
    }

    /**
     * Gets the child nodes of this composite source node.
     *
     * @return an immutable {@link List} of the child nodes
     */
    @Nonnull
    public List<SourceNode> getChildNodes() {
        return this.childNodes;
    }

}
