package org.reasm;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A namespace. An architecture's syntax may provide directives to define a namespace.
 *
 * @author Francis Gagné
 */
public final class Namespace {

    @Nonnull
    private final String name;
    @CheckForNull
    private final Namespace parent;
    @Nonnull
    private final TreeMap<String, Namespace> innerNamespaces = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @Nonnull
    private final Map<String, Namespace> unmodifiableInnerNamespaces = Collections.unmodifiableMap(this.innerNamespaces);

    /**
     * Initializes a new Namespace.
     *
     * @param name
     *            the name of the namespace
     * @param parent
     *            the parent namespace, or <code>null</code> if there is no parent namespace
     */
    Namespace(@Nonnull String name, @CheckForNull Namespace parent) {
        this.name = name;
        this.parent = parent;
    }

    /**
     * Gets a map of the inner namespaces in this namespace.
     *
     * @return an unmodifiable {@link Map} of the inner namespaces
     */
    public final Map<String, Namespace> getInnerNamespaces() {
        return this.unmodifiableInnerNamespaces;
    }

    /**
     * Gets the name of this namespace.
     *
     * @return the name of this namespace
     */
    @Nonnull
    public final String getName() {
        return this.name;
    }

    /**
     * Gets the parent namespace of this namespace. This may be <code>null</code>.
     *
     * @return the parent namespace of this namespace
     */
    @CheckForNull
    public final Namespace getParent() {
        return this.parent;
    }

    /**
     * Gets a map of the inner namespaces in this namespace.
     *
     * @return the inner namespaces
     */
    @Nonnull
    final Map<String, Namespace> internalGetInnerNamespaces() {
        return this.innerNamespaces;
    }

}
