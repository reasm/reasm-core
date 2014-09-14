package org.reasm.messages;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

import org.junit.Test;

/**
 * Test class for {@link IOErrorMessage}.
 *
 * @author Francis Gagné
 */
public class IOErrorMessageTest {

    /**
     * Asserts that {@link IOErrorMessage#IOErrorMessage(IOException)} correctly initializes an {@link IOErrorMessage}.
     */
    @Test
    public void ioErrorMessage() {
        final IOException exception = new IOException("message");
        final IOErrorMessage message = new IOErrorMessage(exception);
        assertThat(message.getText(), is("I/O exception" + ": " + exception.getLocalizedMessage()));
        assertThat(message.getException(), is(sameInstance(exception)));
    }

    /**
     * Asserts that {@link IOErrorMessage#IOErrorMessage(IOException)} initializes an {@link IOErrorMessage} with a more specific
     * message text when the exception is a {@link FileNotFoundException}.
     */
    @Test
    public void ioErrorMessageFileNotFoundException() {
        final IOException exception = new FileNotFoundException("message");
        final IOErrorMessage message = new IOErrorMessage(exception);
        assertThat(message.getText(), is("File not found" + ": " + exception.getLocalizedMessage()));
        assertThat(message.getException(), is(sameInstance(exception)));
    }

    /**
     * Asserts that {@link IOErrorMessage#IOErrorMessage(IOException)} initializes an {@link IOErrorMessage} with a more specific
     * message text when the exception is a {@link NoSuchFileException}.
     */
    @Test
    public void ioErrorMessageNoSuchFileException() {
        final IOException exception = new NoSuchFileException("message");
        final IOErrorMessage message = new IOErrorMessage(exception);
        assertThat(message.getText(), is("File not found" + ": " + exception.getLocalizedMessage()));
        assertThat(message.getException(), is(sameInstance(exception)));
    }

}
