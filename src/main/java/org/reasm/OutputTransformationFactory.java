package org.reasm;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import ca.fragag.Consumer;

/**
 * A factory of {@link OutputTransformation} objects.
 * <p>
 * Subclasses should be immutable.
 *
 * @author Francis Gagné
 */
@Immutable
public abstract class OutputTransformationFactory implements Environment.ObjectWithNames {

    @Nonnull
    private final Set<String> names;

    /**
     * Initializes a new OutputTransformationFactory.
     *
     * @param names
     *            the names of this factory. If your factory has only one name, you can use {@link Collections#singleton(Object)}.
     *            If your factory has no name, you may pass either {@code null} or an empty collection.
     */
    protected OutputTransformationFactory(@CheckForNull Collection<String> names) {
        this.names = names == null ? Collections.<String> emptySet() : Collections.unmodifiableSet(new HashSet<>(names));
    }

    /**
     * Creates an {@link OutputTransformation} for the specified arguments.
     *
     * @param arguments
     *            the arguments that customize an {@link OutputTransformation}
     * @param assemblyMessageConsumer
     *            a {@link Consumer} that will receive {@link AssemblyMessage} emitted while processing the arguments
     * @return an {@link OutputTransformation}, or <code>null</code> if the arguments do not allow the factory to determine how the
     *         {@link OutputTransformation} should be constructed
     */
    @CheckForNull
    public abstract OutputTransformation create(@Nonnull String[] arguments,
            @CheckForNull Consumer<AssemblyMessage> assemblyMessageConsumer);

    /**
     * Gets this factory's names. The returned set is {@linkplain Collections#unmodifiableSet(Set) unmodifiable}.
     *
     * @return the factory's names
     */
    @Override
    public final Set<String> getNames() {
        return this.names;
    }

}
