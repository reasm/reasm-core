package org.reasm;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.reasm.testhelpers.ValueVisitorAdapter;

/**
 * Test class for {@link Value}.
 *
 * @author Francis Gagné
 */
public class ValueTest {

    /**
     * A visitor that asserts that the visitUndetermined() method is called.
     *
     * @author Francis Gagné
     */
    private static final class UndeterminedValueVisitor extends ValueVisitorAdapter<Void> {

        boolean visited;

        UndeterminedValueVisitor() {
        }

        @Override
        public Void visitUndetermined() {
            this.visited = true;
            return null;
        }

    }

    /**
     * Asserts that {@link Value#accept(Value, ValueVisitor)} calls {@link ValueVisitor#visitUndetermined()} when the
     * <code>value</code> argument is <code>null</code>.
     */
    @Test
    public void acceptValueValueVisitorNullValue() {
        final UndeterminedValueVisitor visitor = new UndeterminedValueVisitor();
        Value.accept(null, visitor);
        assertTrue("accept() didn't call any method in visitor", visitor.visited);
    }

}
