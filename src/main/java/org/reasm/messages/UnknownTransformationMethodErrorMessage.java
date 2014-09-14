package org.reasm.messages;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.reasm.AssemblyErrorMessage;
import org.reasm.Environment;
import org.reasm.OutputTransformationFactory;

/**
 * An error message that is generated during an assembly when an attempt to look up an {@link OutputTransformationFactory} in an
 * {@link Environment} fails.
 *
 * @author Francis Gagné
 */
public class UnknownTransformationMethodErrorMessage extends AssemblyErrorMessage {

    @Nonnull
    private final String transformationName;

    /**
     * Initializes a new UnknownTransformationMethodErrorMessage.
     *
     * @param transformationName
     *            the name of the missing transformation factory
     */
    public UnknownTransformationMethodErrorMessage(@Nonnull String transformationName) {
        super("Unknown transformation method: " + Objects.requireNonNull(transformationName, "transformationName"));
        this.transformationName = transformationName;
    }

    /**
     * Gets the name of the missing transformation factory.
     *
     * @return the name
     */
    @Nonnull
    public final String getTransformationName() {
        return this.transformationName;
    }

}
