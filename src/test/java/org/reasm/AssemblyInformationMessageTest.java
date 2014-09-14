package org.reasm;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.reasm.AssemblyMessageTestsCommon.CUSTOM_ASSEMBLY_MESSAGE;
import static org.reasm.AssemblyMessageTestsCommon.TEXT;

import org.junit.Test;

/**
 * Test class for {@link AssemblyInformationMessage}.
 *
 * @author Francis Gagné
 */
public class AssemblyInformationMessageTest {

    /**
     * Asserts that {@link AssemblyInformationMessage#AssemblyInformationMessage(String)} correctly initializes an
     * {@link AssemblyInformationMessage}.
     */
    @Test
    public void assemblyInformationMessageString() {
        final AssemblyInformationMessage message = new AssemblyInformationMessage(TEXT) {
        };

        assertThat(message.getGravity(), is(MessageGravity.INFORMATION));
        assertThat(message.getText(), is(TEXT));
        assertThat(message.getParent(), is(nullValue()));
        assertThat(message.getStep(), is(nullValue()));
    }

    /**
     * Asserts that {@link AssemblyInformationMessage#AssemblyInformationMessage(String, AssemblyMessage)} correctly initializes an
     * {@link AssemblyInformationMessage}.
     */
    @Test
    public void assemblyInformationMessageStringAssemblyMessage() {
        final AssemblyMessage parentMessage = CUSTOM_ASSEMBLY_MESSAGE;
        final AssemblyInformationMessage message = new AssemblyInformationMessage(TEXT, parentMessage) {
        };

        assertThat(message.getGravity(), is(MessageGravity.INFORMATION));
        assertThat(message.getText(), is(TEXT));
        assertThat(message.getParent(), is(parentMessage));
        assertThat(message.getStep(), is(nullValue()));
    }

}
