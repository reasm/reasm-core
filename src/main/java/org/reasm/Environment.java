package org.reasm;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * An environment. Environments contain a set of architectures that are made available to the assembler.
 * <p>
 * This class is immutable.
 *
 * @author Francis Gagné
 */
@Immutable
public final class Environment {

    interface ObjectWithNames {
        Set<String> getNames();
    }

    /**
     * An environment with the following attributes:
     * <ul>
     * <li>no architectures</li>
     * <li>the default output memory size</li>
     * <li>no output transformation factories</li>
     * </ul>
     */
    public static final Environment DEFAULT = new Environment(Collections.unmodifiableSet(Collections.<Architecture> emptySet()),
            0, Collections.unmodifiableSet(Collections.<OutputTransformationFactory> emptySet()));

    private static <T extends ObjectWithNames> T findObjectByName(Set<T> objects, String objectName) {
        for (final T object : objects) {
            for (String name : object.getNames()) {
                if (name != null && name.equalsIgnoreCase(objectName)) {
                    return object;
                }
            }
        }

        return null;
    }

    @Nonnull
    private final Set<Architecture> architectures;
    private final int outputMemorySize;
    @Nonnull
    private final Set<OutputTransformationFactory> outputTransformationFactories;

    /**
     * Initializes a new Environment with the specified architectures and the specified output memory size.
     *
     * @param architectures
     *            a set of architectures
     * @param outputMemorySize
     *            the size to allocate for memory to store the output of an assembly
     * @param outputTransformationFactories
     *            a set of output transformation factories
     */
    private Environment(@Nonnull Set<Architecture> architectures, int outputMemorySize,
            @Nonnull Set<OutputTransformationFactory> outputTransformationFactories) {
        this.architectures = architectures;
        this.outputMemorySize = outputMemorySize;
        this.outputTransformationFactories = outputTransformationFactories;
    }

    /**
     * Creates a new environment from this environment with the specified architecture added to it.
     *
     * @param architecture
     *            the architecture to add to this environment to produce the new environment
     * @return the new environment
     */
    public final Environment addArchitecture(@Nonnull Architecture architecture) {
        if (architecture == null) {
            throw new NullPointerException("architecture");
        }

        final HashSet<Architecture> newArchitectures = new HashSet<>(this.architectures);
        if (newArchitectures.add(architecture)) {
            return new Environment(Collections.unmodifiableSet(newArchitectures), this.outputMemorySize,
                    this.outputTransformationFactories);
        }

        return this;
    }

    /**
     * Creates a new environment from this environment with the specified architectures added to it.
     *
     * @param architectures
     *            the architectures to add to this environment to produce the new environment
     * @return the new environment
     */
    public final Environment addArchitectures(@Nonnull Collection<? extends Architecture> architectures) {
        if (architectures == null) {
            throw new NullPointerException("architectures");
        }

        final HashSet<Architecture> newArchitectures = new HashSet<>(this.architectures);
        if (newArchitectures.addAll(architectures)) {
            return new Environment(Collections.unmodifiableSet(newArchitectures), this.outputMemorySize,
                    this.outputTransformationFactories);
        }

        return this;
    }

    /**
     * Creates a new environment from this environment with the specified output transformation factories added to it.
     *
     * @param outputTransformationFactories
     *            the output transformation factories to add to this environment to produce the new environment
     * @return the new environment
     */
    public final Environment addOutputTransformationFactories(
            @Nonnull Collection<? extends OutputTransformationFactory> outputTransformationFactories) {
        if (outputTransformationFactories == null) {
            throw new NullPointerException("outputTransformationFactories");
        }

        final HashSet<OutputTransformationFactory> newOutputTransformationFactories = new HashSet<>(
                this.outputTransformationFactories);
        if (newOutputTransformationFactories.addAll(outputTransformationFactories)) {
            return new Environment(this.architectures, this.outputMemorySize,
                    Collections.unmodifiableSet(newOutputTransformationFactories));
        }

        return this;
    }

    /**
     * Creates a new environment from this environment with the specified output transformation factory added to it.
     *
     * @param outputTransformationFactory
     *            the output transformation factory to add to this environment to produce the new environment
     * @return the new environment
     */
    public final Environment addOutputTransformationFactory(@Nonnull OutputTransformationFactory outputTransformationFactory) {
        if (outputTransformationFactory == null) {
            throw new NullPointerException("outputTransformationFactory");
        }

        final HashSet<OutputTransformationFactory> newOutputTransformationFactories = new HashSet<>(
                this.outputTransformationFactories);
        if (newOutputTransformationFactories.add(outputTransformationFactory)) {
            return new Environment(this.architectures, this.outputMemorySize,
                    Collections.unmodifiableSet(newOutputTransformationFactories));
        }

        return this;
    }

    /**
     * Finds an architecture registered in this environment by name.
     *
     * @param architectureName
     *            the name of the architecture to find
     * @return an {@link Architecture} registered in this environment, or <code>null</code> if no architecture with the specified
     *         name was registered
     */
    public final Architecture findArchitectureByName(String architectureName) {
        return findObjectByName(this.architectures, architectureName);
    }

    /**
     * Finds an output transformation factory registered in this environment by name.
     *
     * @param outputTransformationFactoryName
     *            the name of the output transformation factory to find
     * @return an {@link OutputTransformationFactory} registered in this environment, or <code>null</code> if no output
     *         transformation factory with the specified name was registered
     */
    public final OutputTransformationFactory findOutputTransformationFactoryByName(String outputTransformationFactoryName) {
        return findObjectByName(this.outputTransformationFactories, outputTransformationFactoryName);
    }

    /**
     * Gets the set of architectures registered in this environment. This set is unmodifiable.
     *
     * @return the set of architectures registered in this environment
     */
    public final Set<Architecture> getArchitectures() {
        return this.architectures;
    }

    /**
     * Gets the size to allocate for memory to store the output of an assembly.
     *
     * @return the output memory size, or 0 to use the default size
     */
    public final int getOutputMemorySize() {
        return this.outputMemorySize;
    }

    /**
     * Gets the set of output transformation factories registered in this environment. This set is unmodifiable.
     *
     * @return the set of output transformation factories registered in this environment
     */
    public final Set<OutputTransformationFactory> getOutputTransformationFactories() {
        return this.outputTransformationFactories;
    }

    /**
     * Creates a new environment from this environment with the specified architecture removed from it.
     *
     * @param architecture
     *            the architecture to remove from this environment to produce the new environment
     * @return the new environment
     */
    public final Environment removeArchitecture(@Nonnull Architecture architecture) {
        if (architecture == null) {
            throw new NullPointerException("architecture");
        }

        final HashSet<Architecture> newArchitectures = new HashSet<>(this.architectures);
        if (newArchitectures.remove(architecture)) {
            return new Environment(Collections.unmodifiableSet(newArchitectures), this.outputMemorySize,
                    this.outputTransformationFactories);
        }

        return this;
    }

    /**
     * Creates a new environment from this environment with the specified architectures removed from it.
     *
     * @param architectures
     *            the architectures to remove from this environment to produce the new environment
     * @return the new environment
     */
    public final Environment removeArchitectures(@Nonnull Collection<? extends Architecture> architectures) {
        if (architectures == null) {
            throw new NullPointerException("architectures");
        }

        final HashSet<Architecture> newArchitectures = new HashSet<>(this.architectures);
        if (newArchitectures.removeAll(architectures)) {
            return new Environment(Collections.unmodifiableSet(newArchitectures), this.outputMemorySize,
                    this.outputTransformationFactories);
        }

        return this;
    }

    /**
     * Creates a new environment from this environment with the specified output transformation factories removed from it.
     *
     * @param outputTransformationFactories
     *            the output transformation factories to remove from this environment to produce the new environment
     * @return the new environment
     */
    public final Environment removeOutputTransformationFactories(
            @Nonnull Collection<? extends OutputTransformationFactory> outputTransformationFactories) {
        if (outputTransformationFactories == null) {
            throw new NullPointerException("outputTransformationFactories");
        }

        final HashSet<OutputTransformationFactory> newOutputTransformationFactories = new HashSet<>(
                this.outputTransformationFactories);
        if (newOutputTransformationFactories.removeAll(outputTransformationFactories)) {
            return new Environment(this.architectures, this.outputMemorySize,
                    Collections.unmodifiableSet(newOutputTransformationFactories));
        }

        return this;
    }

    /**
     * Creates a new environment from this environment with the specified output transformation factory removed from it.
     *
     * @param outputTransformationFactory
     *            the output transformation factory to remove from this environment to produce the new environment
     * @return the new environment
     */
    public final Environment removeOutputTransformationFactory(@Nonnull OutputTransformationFactory outputTransformationFactory) {
        if (outputTransformationFactory == null) {
            throw new NullPointerException("outputTransformationFactory");
        }

        final HashSet<OutputTransformationFactory> newOutputTransformationFactories = new HashSet<>(
                this.outputTransformationFactories);
        if (newOutputTransformationFactories.remove(outputTransformationFactory)) {
            return new Environment(this.architectures, this.outputMemorySize,
                    Collections.unmodifiableSet(newOutputTransformationFactories));
        }

        return this;
    }

    /**
     * Creates a new environment from this environment with the specified size to allocate for memory to store the output of an
     * assembly. When the output size of an assembly exceeds this value, the output will be written to a temporary file.
     *
     * @param outputMemorySize
     *            the output memory size, or 0 to use the default size
     * @return the new environment
     * @throws IllegalArgumentException
     *             <code>outputMemorySize</code> is less than 0
     */
    public final Environment setOutputMemorySize(int outputMemorySize) {
        if (outputMemorySize < 0) {
            throw new IllegalArgumentException("outputMemorySize must be >= 0");
        }

        if (this.outputMemorySize != outputMemorySize) {
            return new Environment(this.architectures, outputMemorySize, this.outputTransformationFactories);
        }

        return this;
    }

}
