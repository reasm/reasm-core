package org.reasm.testhelpers;

import static org.junit.Assert.fail;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.AssemblyBuilder;
import org.reasm.source.ParseError;
import org.reasm.source.SourceNode;

/**
 * An implementation of {@link SourceNode} that raises an {@link AssertionError} when its {@link #assembleCore(AssemblyBuilder)}
 * method is called.
 *
 * @author Francis Gagné
 */
@Immutable
public class DummySourceNode extends SourceNode {

    /**
     * Initializes a new DummySourceNode.
     *
     * @param length
     *            the number of characters in the source node
     * @param parseError
     *            the parse error on the source node, or <code>null</code> if no parse error occurred
     */
    public DummySourceNode(int length, @CheckForNull ParseError parseError) {
        super(length, parseError);
    }

    @Override
    protected void assembleCore(@Nonnull AssemblyBuilder builder) throws IOException {
        fail();
    }

}
