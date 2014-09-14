package org.reasm.messages;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.reasm.AssemblyFatalErrorMessage;

/**
 * A fatal error message that is generated during an assembly when the assembler encounters an internal error that it cannot recover
 * from.
 *
 * @author Francis Gagné
 */
public final class InternalAssemblerErrorMessage extends AssemblyFatalErrorMessage {

    @Nonnull
    private final Throwable throwable;

    /**
     * Initializes a new InternalAssemblerErrorMessage.
     *
     * @param throwable
     *            the throwable that is generating this message
     */
    public InternalAssemblerErrorMessage(@Nonnull Throwable throwable) {
        super("Internal assembler error");
        this.throwable = Objects.requireNonNull(throwable, "throwable");
    }

    /**
     * Gets the {@link Throwable} that generated this message.
     *
     * @return the throwable
     */
    @Nonnull
    public final Throwable getThrowable() {
        return this.throwable;
    }

}
