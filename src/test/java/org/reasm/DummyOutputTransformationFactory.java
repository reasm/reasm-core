package org.reasm;

import java.util.Collection;

import ca.fragag.Consumer;

/**
 * An implementation of {@link OutputTransformationFactory} whose {@link #create(String[], Consumer)} method always returns
 * <code>null</code>.
 *
 * @author Francis Gagné
 */
final class DummyOutputTransformationFactory extends OutputTransformationFactory {

    DummyOutputTransformationFactory(Collection<String> names) {
        super(names);
    }

    @Override
    public OutputTransformation create(String[] arguments, Consumer<AssemblyMessage> assemblyMessageConsumer) {
        return null;
    }

}
