package org.reasm.messages;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when some directive that references an architecture name that is not
 * registered is encountered.
 *
 * @author Francis Gagné
 */
public final class ArchitectureNotRegisteredErrorMessage extends AssemblyErrorMessage {

    @Nonnull
    private final String architectureName;

    /**
     * Initializes a new ArchitectureNotRegisteredErrorMessage.
     *
     * @param architectureName
     *            the name of the missing architecture
     */
    public ArchitectureNotRegisteredErrorMessage(@Nonnull String architectureName) {
        super("Architecture not registered: " + Objects.requireNonNull(architectureName, "architectureName"));
        this.architectureName = architectureName;
    }

    /**
     * Gets the name of the missing architecture that caused this fatal error message.
     *
     * @return the architecture name
     */
    @Nonnull
    public final String getArchitectureName() {
        return this.architectureName;
    }

}
