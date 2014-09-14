package org.reasm;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.reasm.AssemblyTestsCommon.checkOutput;
import static org.reasm.AssemblyTestsCommon.createAssembly;
import static org.reasm.AssemblyTestsCommon.createNodeThatEmitsData;
import static org.reasm.AssemblyTestsCommon.createNodeThatEntersATransformationBlock;
import static org.reasm.AssemblyTestsCommon.createNodeThatExitsATransformationBlock;
import static org.reasm.AssemblyTestsCommon.step;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.reasm.source.SimpleCompositeSourceNode;
import org.reasm.source.SourceNode;
import org.reasm.testhelpers.TestSourceNode;

/**
 * Test class for {@link IdentityTransformation}.
 *
 * @author Francis Gagné
 */
public class IdentityTransformationTest {

    private static void transform(final byte[] data) throws IOException {
        final TestSourceNode nodeThatEntersATransformationBlock = createNodeThatEntersATransformationBlock(IdentityTransformation.INSTANCE);
        final TestSourceNode nodeThatEmitsData = createNodeThatEmitsData(data);
        final TestSourceNode nodeThatExitsATransformationBlock = createNodeThatExitsATransformationBlock();
        final SourceNode rootNode = new SimpleCompositeSourceNode(Arrays.asList(nodeThatEntersATransformationBlock,
                nodeThatEmitsData, nodeThatExitsATransformationBlock));

        final Assembly assembly = createAssembly(rootNode);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.PENDING);
        step(assembly, AssemblyCompletionStatus.COMPLETE);
        assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        checkOutput(assembly, data);
    }

    /**
     * Asserts that {@link IdentityTransformation#transform(Output, AssemblyBuilder)} emits the specified output unaltered.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void transform() throws IOException {
        final byte[] zeroOneTwoThree = new byte[] { 0, 1, 2, 3 };
        transform(zeroOneTwoThree);
    }

    /**
     * Asserts that {@link IdentityTransformation#transform(Output, AssemblyBuilder)} emits the specified output unaltered.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void transformLarge() throws IOException {
        final int length = 0x12345;
        final byte[] largeData = new byte[length];
        byte b = 0;
        for (int i = 0; i < length; i++) {
            b += i;
            largeData[i] = b;
        }

        transform(largeData);
    }

}
