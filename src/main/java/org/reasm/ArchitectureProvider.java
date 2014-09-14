package org.reasm;

import java.util.ServiceLoader;

/**
 * This interface is a service type that can be used with {@link ServiceLoader}. An implementation of this interface must implement
 * {@link Iterable#iterator()} to return an iterator of architectures provided by the provider.
 *
 * @author Francis Gagné
 */
public interface ArchitectureProvider extends Iterable<Architecture> {
}
