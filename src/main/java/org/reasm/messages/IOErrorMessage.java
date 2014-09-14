package org.reasm.messages;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.reasm.AssemblyFatalErrorMessage;

/**
 * A fatal error message that is generated during an assembly when an I/O exception occurs.
 *
 * @author Francis Gagné
 */
public final class IOErrorMessage extends AssemblyFatalErrorMessage {

    private static String getFriendlyDescription(IOException exception) {
        if (exception instanceof FileNotFoundException || exception instanceof NoSuchFileException) {
            return "File not found";
        }

        return "I/O exception";
    }

    @Nonnull
    private final IOException exception;

    /**
     * Initializes a new IOErrorMessage.
     *
     * @param exception
     *            the {@link IOException} that is causing this fatal error message
     */
    public IOErrorMessage(@Nonnull IOException exception) {
        super(getFriendlyDescription(exception) + ": " + Objects.requireNonNull(exception, "exception").getLocalizedMessage());
        this.exception = exception;
    }

    /**
     * Gets the {@link IOException} that caused this fatal error message.
     *
     * @return the {@link IOException}
     */
    @Nonnull
    public final IOException getException() {
        return this.exception;
    }

}
