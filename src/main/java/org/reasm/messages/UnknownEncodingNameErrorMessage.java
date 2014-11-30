package org.reasm.messages;

import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a supplied encoding name is not recognized.
 *
 * @author Francis Gagné
 */
public class UnknownEncodingNameErrorMessage extends AssemblyErrorMessage {

    @Nonnull
    private final String encodingName;
    @CheckForNull
    private final RuntimeException exception;

    /**
     * Initializes a new UnknownEncodingNameErrorMessage.
     *
     * @param encodingName
     *            the encoding name
     * @param exception
     *            a {@link RuntimeException} that was thrown while trying to use the encoding name
     */
    public UnknownEncodingNameErrorMessage(@Nonnull String encodingName, @CheckForNull RuntimeException exception) {
        super("Illegal encoding name: " + Objects.requireNonNull(encodingName));
        this.encodingName = encodingName;
        this.exception = exception;
    }

    /**
     * The name of the unknown encoding that caused this error.
     *
     * @return the name of the unknown encoding
     */
    @Nonnull
    public final String getEncodingName() {
        return this.encodingName;
    }

    /**
     * The {@link RuntimeException} that was thrown while trying to use the encoding name.
     *
     * @return the exception
     */
    @CheckForNull
    public final RuntimeException getException() {
        return this.exception;
    }

}
