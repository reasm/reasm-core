package org.reasm.testhelpers;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.reasm.AssemblyBuilder;
import org.reasm.source.SourceNode;

/**
 * An implementation of {@link SourceNode} for unit tests.
 *
 * @author Francis Gagné
 */
public abstract class TestSourceNode extends SourceNode {

    private int actualAssembleCount;

    /**
     * Initializes a new TestSourceNode.
     */
    public TestSourceNode() {
        super(0, null);
    }

    /**
     * Asserts that {@link #assembleCore(AssemblyBuilder)} has been called the expected number of times.
     *
     * @param expectedAssembleCount
     *            the number of times that {@link #assembleCore(AssemblyBuilder)} is expected to be called
     */
    public final void assertAssembleCount(int expectedAssembleCount) {
        final int actualAssembleCount = this.actualAssembleCount;
        this.actualAssembleCount = 0;
        assertThat(actualAssembleCount, is(expectedAssembleCount));
    }

    @Override
    protected final void assembleCore(@Nonnull AssemblyBuilder builder) throws IOException {
        this.actualAssembleCount++;
        this.assembleCore2(builder);
    }

    /**
     * Assembles this source node.
     *
     * @param builder
     *            an assembly builder
     * @throws IOException
     *             an I/O exception occurred
     */
    protected abstract void assembleCore2(@Nonnull AssemblyBuilder builder) throws IOException;

}
