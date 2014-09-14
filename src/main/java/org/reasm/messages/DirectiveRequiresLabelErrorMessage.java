package org.reasm.messages;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a directive that requires a label is used without a label.
 *
 * @author Francis Gagné
 */
public class DirectiveRequiresLabelErrorMessage extends AssemblyErrorMessage {

    @Nonnull
    private final String directiveName;

    /**
     * Initializes a new DirectiveRequiresLabelErrorMessage.
     *
     * @param directiveName
     *            the name of the directive that caused the error
     */
    public DirectiveRequiresLabelErrorMessage(@Nonnull String directiveName) {
        super("Directive " + Objects.requireNonNull(directiveName, "directiveName") + " requires a label");
        this.directiveName = directiveName;
    }

    /**
     * Gets the name of the directive that caused this error.
     *
     * @return the name of the directive
     */
    @Nonnull
    public final String getDirectiveName() {
        return this.directiveName;
    }

}
