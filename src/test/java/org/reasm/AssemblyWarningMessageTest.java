package org.reasm;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.reasm.AssemblyMessageTestsCommon.CUSTOM_ASSEMBLY_MESSAGE;
import static org.reasm.AssemblyMessageTestsCommon.TEXT;

import org.junit.Test;

/**
 * Test class for {@link AssemblyWarningMessage}.
 *
 * @author Francis Gagné
 */
public class AssemblyWarningMessageTest {

    /**
     * Asserts that {@link AssemblyWarningMessage#AssemblyWarningMessage(String)} correctly initializes an
     * {@link AssemblyWarningMessage}.
     */
    @Test
    public void assemblyWarningMessageString() {
        final AssemblyWarningMessage message = new AssemblyWarningMessage(TEXT) {
        };

        assertThat(message.getGravity(), is(MessageGravity.WARNING));
        assertThat(message.getText(), is(TEXT));
        assertThat(message.getParent(), is(nullValue()));
        assertThat(message.getStep(), is(nullValue()));
    }

    /**
     * Asserts that {@link AssemblyWarningMessage#AssemblyWarningMessage(String, AssemblyMessage)} correctly initializes an
     * {@link AssemblyWarningMessage}.
     */
    @Test
    public void assemblyWarningMessageStringAssemblyMessage() {
        final AssemblyMessage parentMessage = CUSTOM_ASSEMBLY_MESSAGE;
        final AssemblyWarningMessage message = new AssemblyWarningMessage(TEXT, parentMessage) {
        };

        assertThat(message.getGravity(), is(MessageGravity.WARNING));
        assertThat(message.getText(), is(TEXT));
        assertThat(message.getParent(), is(parentMessage));
        assertThat(message.getStep(), is(nullValue()));
    }

}
