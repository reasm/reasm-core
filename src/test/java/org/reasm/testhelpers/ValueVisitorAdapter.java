package org.reasm.testhelpers;

import org.reasm.Function;
import org.reasm.ValueVisitor;

/**
 * A visitor that fails when any of its methods is called.
 *
 * @param <T>
 *            the visitor's result type
 *
 * @author Francis Gagné
 */
public class ValueVisitorAdapter<T> implements ValueVisitor<T> {

    @Override
    public T visitFloat(double value) {
        throw new AssertionError("accept() shouldn't call visitFloat()");
    }

    @Override
    public T visitFunction(Function value) {
        throw new AssertionError("accept() shouldn't call visitFunction()");
    }

    @Override
    public T visitSignedInt(long value) {
        throw new AssertionError("accept() shouldn't call visitSignedInt()");
    }

    @Override
    public T visitString(String value) {
        throw new AssertionError("accept() shouldn't call visitString()");
    }

    @Override
    public T visitUndetermined() {
        throw new AssertionError("accept() shouldn't call visitUndetermined()");
    }

    @Override
    public T visitUnsignedInt(long value) {
        throw new AssertionError("accept() shouldn't call visitUnsignedInt()");
    }

}
