package org.reasm;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.reasm.AssemblyMessageTestsCommon.TEXT;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

import org.junit.Test;
import org.reasm.source.SourceFile;
import org.reasm.testhelpers.NullArchitecture;

/**
 * Test class for {@link AssemblyMessage}.
 *
 * @author Francis Gagné
 */
public class AssemblyMessageTest {

    private static final String TEXT2 = "text2";

    @Nonnull
    private static AssemblyStep createAssemblyStep() {
        final Assembly assembly = new Assembly(new Configuration(Environment.DEFAULT, new SourceFile("", null),
                NullArchitecture.DEFAULT));
        assembly.step();

        final List<AssemblyStep> steps = assembly.getSteps();
        assertThat(steps, hasSize(1));
        return steps.get(0);
    }

    /**
     * Asserts that {@link AssemblyMessage#addToAssembly(AssemblyStep)} sets the message's step.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void addToAssembly() throws IOException {
        final AssemblyMessage message = AssemblyMessageTestsCommon.createCustomAssemblyMessage();
        final AssemblyStep step = createAssemblyStep();

        message.addToAssembly(step);

        assertThat(message.getStep(), is(step));
    }

    /**
     * Asserts that {@link AssemblyMessage#addToAssembly(AssemblyStep)} throws an {@link IllegalStateException} when it's called a
     * second time on the same message.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void addToAssemblyTwice() throws IOException {
        final AssemblyMessage message = AssemblyMessageTestsCommon.createCustomAssemblyMessage();
        final AssemblyStep step = createAssemblyStep();

        message.addToAssembly(step);

        try {
            message.addToAssembly(step);
        } catch (IllegalStateException e) {
            return;
        }

        fail("addToAssembly() should have thrown IllegalStateException");
    }

    /**
     * Asserts that {@link AssemblyMessage#AssemblyMessage(MessageGravity, String, AssemblyMessage)} correctly initializes an
     * {@link AssemblyMessage}.
     */
    @Test
    public void assemblyMessage() {
        final AssemblyMessage parentMessage = AssemblyMessageTestsCommon.CUSTOM_ASSEMBLY_MESSAGE;
        final AssemblyMessage message = new AssemblyMessage(MessageGravity.ERROR, TEXT2, parentMessage) {
        };

        assertThat(parentMessage.getGravity(), is(MessageGravity.INFORMATION));
        assertThat(parentMessage.getText(), is(TEXT));
        assertThat(parentMessage.getParent(), is(nullValue()));
        assertThat(parentMessage.getStep(), is(nullValue()));

        assertThat(message.getGravity(), is(MessageGravity.ERROR));
        assertThat(message.getText(), is(TEXT2));
        assertThat(message.getParent(), is(parentMessage));
        assertThat(message.getStep(), is(nullValue()));
    }

    /**
     * Asserts that {@link AssemblyMessage#AssemblyMessage(MessageGravity, String, AssemblyMessage)} throws a
     * {@link NullPointerException} when the <code>text</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void assemblyMessageNullText() {
        new AssemblyMessage(MessageGravity.ERROR, null, null) {
        };
    }

}
