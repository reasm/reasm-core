package org.reasm.messages;

import javax.annotation.Nonnull;

import org.reasm.AssemblyErrorMessage;
import org.reasm.OutputTransformationFactory;

import ca.fragag.Consumer;

/**
 * An error message that is generated during an assembly when an implementation of
 * {@link OutputTransformationFactory#create(String[], Consumer)} returns <code>null</code>.
 *
 * @author Francis Gagné
 */
public class InvalidTransformationArgumentsErrorMessage extends AssemblyErrorMessage {

    @Nonnull
    private final String transformationName;
    @Nonnull
    private final String[] transformationArguments;

    /**
     * Initializes a new InvalidTransformationArgumentsErrorMessage.
     *
     * @param transformationName
     *            the name of the transformation factory that rejected the arguments
     * @param transformationArguments
     *            the arguments that were passed to the transformation factory
     */
    public InvalidTransformationArgumentsErrorMessage(String transformationName, String[] transformationArguments) {
        super("Invalid arguments for transformation: " + transformationName);
        this.transformationName = transformationName;
        this.transformationArguments = transformationArguments.clone();
    }

    /**
     * Gets the arguments that were passed to the transformation factory.
     *
     * @return the arguments
     */
    public final String[] getTransformationArguments() {
        return this.transformationArguments.clone();
    }

    /**
     * Gets the name of the transformation factory that rejected the arguments.
     *
     * @return the name
     */
    public final String getTransformationName() {
        return this.transformationName;
    }

}
