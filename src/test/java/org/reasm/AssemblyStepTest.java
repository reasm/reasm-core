package org.reasm;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.reasm.AssemblyTestsCommon.checkOutput;
import static org.reasm.AssemblyTestsCommon.createAssembly;
import static org.reasm.AssemblyTestsCommon.step;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.annotation.Nonnull;

import org.junit.Test;
import org.reasm.testhelpers.TestSourceNode;

/**
 * Test class for {@link AssemblyStep}.
 *
 * @author Francis Gagné
 */
public class AssemblyStepTest {

    private static void assembleNode(@Nonnull final TestSourceNode node) {
        final Assembly assembly = createAssembly(node);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        node.assertAssembleCount(1);
    }

    /**
     * Asserts that {@link AssemblyStep#appendAssembledData(byte)} appends 1 byte of data to the assembly and that the length of the
     * assembled data on the step increments by 1.
     */
    @Test
    public void appendAssembledDataByte() {
        final TestSourceNode nodeThatAppendsAByteOfAssembledData = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                final byte[] oneOne = new byte[] { 1 };

                builder.appendAssembledData((byte) 1);
                assertThat(builder.getStep().getAssembledDataLength(), is(1L));
                checkOutput(builder.getAssembly(), oneOne);
                checkOutput(builder.getStep(), oneOne);
            }
        };

        assembleNode(nodeThatAppendsAByteOfAssembledData);
    }

    /**
     * Asserts that {@link AssemblyStep#appendAssembledData(byte[])} appends all the bytes of an array of data to the assembly and
     * that the length of the assembled data on the step increments by the size of that array.
     */
    @Test
    public void appendAssembledDataByteArray() {
        final TestSourceNode nodeThatAppendsManyBytesOfAssembledData = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                final byte[] arbitraryData = new byte[] { 1, 2, 3, 4 };

                builder.appendAssembledData(arbitraryData);
                assertThat(builder.getStep().getAssembledDataLength(), is((long) arbitraryData.length));
                checkOutput(builder.getAssembly(), arbitraryData);
                checkOutput(builder.getStep(), arbitraryData);
            }
        };

        assembleNode(nodeThatAppendsManyBytesOfAssembledData);
    }

    /**
     * Asserts that {@link AssemblyStep#appendAssembledData(byte[], int, int)} appends a subset of the bytes of an array of data to
     * the assembly and that the length of the assembled data on the step increments by the length of the specified subset.
     */
    @Test
    public void appendAssembledDataByteArrayIntInt() {
        final TestSourceNode nodeThatAppendsManyBytesOfAssembledData = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                final byte[] arbitraryData = new byte[] { 1, 2, 3, 4 };
                final byte[] writtenData = new byte[] { 2, 3 };

                builder.appendAssembledData(arbitraryData, 1, 2);
                assertThat(builder.getStep().getAssembledDataLength(), is(2L));
                checkOutput(builder.getAssembly(), writtenData);
                checkOutput(builder.getStep(), writtenData);
            }
        };

        assembleNode(nodeThatAppendsManyBytesOfAssembledData);
    }

    /**
     * Asserts that {@link AssemblyStep#appendAssembledData(ByteBuffer)} appends the contents of a {@link ByteBuffer} to the
     * assembly and that the length of the assembled data on the step increments by the size of the contents that was appended.
     */
    @Test
    public void appendAssembledDataByteBuffer() {
        final TestSourceNode nodeThatAppendsManyBytesOfAssembledData = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                final byte[] arbitraryData = new byte[] { 1, 2, 3, 4 };
                final byte[] writtenData = new byte[] { 2, 3 };
                final ByteBuffer bb = ByteBuffer.wrap(arbitraryData, 1, 2);

                builder.appendAssembledData(bb);
                assertThat(builder.getStep().getAssembledDataLength(), is((long) writtenData.length));
                checkOutput(builder.getAssembly(), writtenData);
                checkOutput(builder.getStep(), writtenData);
            }
        };

        assembleNode(nodeThatAppendsManyBytesOfAssembledData);
    }

    /**
     * Asserts that {@link AssemblyStep#setHasSideEffects()} sets a flag on the step.
     */
    @Test
    public void setHasSideEffects() {
        final TestSourceNode nodeThatHasMysteriousSideEffects = new TestSourceNode() {
            @Override
            protected void assembleCore2(AssemblyBuilder builder) throws IOException {
                builder.setCurrentStepHasSideEffects();
                assertThat(builder.getStep().hasSideEffects(), is(true));
            }
        };

        assembleNode(nodeThatHasMysteriousSideEffects);
    }

}
