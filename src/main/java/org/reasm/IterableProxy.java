package org.reasm;

import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * Wraps an {@link Iterable} object to prevent unwanted casts to the concrete type of the object.
 *
 * @author Francis Gagné
 */
final class IterableProxy<T> implements Iterable<T> {

    @Nonnull
    private final Iterable<T> iterable;

    /**
     * Initializes a new {@link IterableProxy}.
     *
     * @param iterable
     *            the {@link Iterable} object to wrap.
     */
    IterableProxy(@Nonnull Iterable<T> iterable) {
        this.iterable = iterable;
    }

    @Override
    public Iterator<T> iterator() {
        return this.iterable.iterator();
    }

}
