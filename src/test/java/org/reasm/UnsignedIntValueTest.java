package org.reasm;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.reasm.testhelpers.ValueVisitorAdapter;

import ca.fragag.testhelpers.ObjectHashCodeEqualsContract;

/**
 * Test class for {@link UnsignedIntValue}.
 *
 * @author Francis Gagné
 */
public class UnsignedIntValueTest extends ObjectHashCodeEqualsContract {

    /**
     * A visitor that asserts that the {@link ValueVisitor#visitUnsignedInt(long)} method is called with the correct value.
     *
     * @author Francis Gagné
     */
    private static final class UnsignedIntValueVisitor extends ValueVisitorAdapter<Void> {

        boolean visited;

        UnsignedIntValueVisitor() {
        }

        @Override
        public Void visitUnsignedInt(long value) {
            assertThat(value, is(VALUE));
            this.visited = true;
            return null;
        }

    }

    private static final long VALUE = 1234;

    /**
     * Initializes a new UnsignedIntValueTest.
     */
    public UnsignedIntValueTest() {
        super(new UnsignedIntValue(VALUE), new UnsignedIntValue(VALUE), new UnsignedIntValue(VALUE),
                new UnsignedIntValue(VALUE + 1), new Object());
    }

    /**
     * Asserts that {@link UnsignedIntValue#accept(ValueVisitor)} calls {@link ValueVisitor#visitUnsignedInt(long)}.
     */
    @Test
    public void accept() {
        final UnsignedIntValueVisitor visitor = new UnsignedIntValueVisitor();
        new UnsignedIntValue(VALUE).accept(visitor);
        assertTrue("accept() didn't call any method in visitor", visitor.visited);
    }

}
