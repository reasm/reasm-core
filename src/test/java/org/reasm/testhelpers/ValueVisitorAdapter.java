package org.reasm.testhelpers;

import static org.junit.Assert.fail;

import org.reasm.Function;
import org.reasm.ValueVisitor;

/**
 * A visitor that fails when any of its methods is called.
 *
 * @author Francis Gagné
 */
public class ValueVisitorAdapter implements ValueVisitor<Void> {

    @Override
    public Void visitFloat(double value) {
        fail("accept() shouldn't call visitFloat()");
        return null; // unreachable
    }

    @Override
    public Void visitFunction(Function value) {
        fail("accept() shouldn't call visitFunction()");
        return null; // unreachable
    }

    @Override
    public Void visitSignedInt(long value) {
        fail("accept() shouldn't call visitSignedInt()");
        return null; // unreachable
    }

    @Override
    public Void visitString(String value) {
        fail("accept() shouldn't call visitString()");
        return null; // unreachable
    }

    @Override
    public Void visitUndetermined() {
        fail("accept() shouldn't call visitUndetermined()");
        return null; // unreachable
    }

    @Override
    public Void visitUnsignedInt(long value) {
        fail("accept() shouldn't call visitUnsignedInt()");
        return null; // unreachable
    }

}
