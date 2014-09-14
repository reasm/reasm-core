package org.reasm;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.reasm.AssemblyMessageTestsCommon.CUSTOM_ASSEMBLY_MESSAGE;
import static org.reasm.AssemblyMessageTestsCommon.TEXT;

import org.junit.Test;

/**
 * Test class for {@link AssemblyErrorMessage}.
 *
 * @author Francis Gagné
 */
public class AssemblyErrorMessageTest {

    /**
     * Asserts that {@link AssemblyErrorMessage#AssemblyErrorMessage(String)} correctly initializes an {@link AssemblyErrorMessage}.
     */
    @Test
    public void assemblyErrorMessageString() {
        final AssemblyErrorMessage message = new AssemblyErrorMessage(TEXT) {
        };

        assertThat(message.getGravity(), is(MessageGravity.ERROR));
        assertThat(message.getText(), is(TEXT));
        assertThat(message.getParent(), is(nullValue()));
        assertThat(message.getStep(), is(nullValue()));
    }

    /**
     * Asserts that {@link AssemblyErrorMessage#AssemblyErrorMessage(String, AssemblyMessage)} correctly initializes an
     * {@link AssemblyErrorMessage}.
     */
    @Test
    public void assemblyErrorMessageStringAssemblyMessage() {
        final AssemblyMessage parentMessage = CUSTOM_ASSEMBLY_MESSAGE;
        final AssemblyErrorMessage message = new AssemblyErrorMessage(TEXT, parentMessage) {
        };

        assertThat(message.getGravity(), is(MessageGravity.ERROR));
        assertThat(message.getText(), is(TEXT));
        assertThat(message.getParent(), is(parentMessage));
        assertThat(message.getStep(), is(nullValue()));
    }

}
