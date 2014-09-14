package org.reasm;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.reasm.AssemblyMessageTestsCommon.CUSTOM_ASSEMBLY_MESSAGE;
import static org.reasm.AssemblyMessageTestsCommon.TEXT;

import org.junit.Test;

/**
 * Test class for {@link AssemblyFatalErrorMessage}.
 *
 * @author Francis Gagné
 */
public class AssemblyFatalErrorMessageTest {

    /**
     * Asserts that {@link AssemblyFatalErrorMessage#AssemblyFatalErrorMessage(String)} correctly initializes an
     * {@link AssemblyFatalErrorMessage}.
     */
    @Test
    public void assemblyFatalErrorMessageString() {
        final AssemblyFatalErrorMessage message = new AssemblyFatalErrorMessage(TEXT) {
        };

        assertThat(message.getGravity(), is(MessageGravity.FATAL_ERROR));
        assertThat(message.getText(), is(TEXT));
        assertThat(message.getParent(), is(nullValue()));
        assertThat(message.getStep(), is(nullValue()));
    }

    /**
     * Asserts that {@link AssemblyFatalErrorMessage#AssemblyFatalErrorMessage(String, AssemblyMessage)} correctly initializes an
     * {@link AssemblyFatalErrorMessage}.
     */
    @Test
    public void assemblyFatalErrorMessageStringAssemblyMessage() {
        final AssemblyMessage parentMessage = CUSTOM_ASSEMBLY_MESSAGE;
        final AssemblyFatalErrorMessage message = new AssemblyFatalErrorMessage(TEXT, parentMessage) {
        };

        assertThat(message.getGravity(), is(MessageGravity.FATAL_ERROR));
        assertThat(message.getText(), is(TEXT));
        assertThat(message.getParent(), is(parentMessage));
        assertThat(message.getStep(), is(nullValue()));
    }

}
