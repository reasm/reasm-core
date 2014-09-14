package org.reasm;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.reasm.testhelpers.ValueVisitorAdapter;

import ca.fragag.testhelpers.ObjectHashCodeEqualsContract;

/**
 * Test class for {@link FloatValue}.
 *
 * @author Francis Gagné
 */
public class FloatValueTest extends ObjectHashCodeEqualsContract {

    /**
     * A visitor that asserts that the {@link ValueVisitor#visitFloat(double)} method is called with the correct value.
     *
     * @author Francis Gagné
     */
    private static final class FloatValueVisitor extends ValueVisitorAdapter {

        boolean visited;

        FloatValueVisitor() {
        }

        @Override
        public Void visitFloat(double value) {
            assertThat(value, is(VALUE));
            this.visited = true;
            return null;
        }

    }

    private static final double VALUE = 1234.5678;

    /**
     * Initializes a new FloatValueTest.
     */
    public FloatValueTest() {
        super(new FloatValue(VALUE), new FloatValue(VALUE), new FloatValue(VALUE), new FloatValue(VALUE + 1), new Object());
    }

    /**
     * Asserts that {@link FloatValue#accept(ValueVisitor)} calls {@link ValueVisitor#visitFloat(double)}.
     */
    @Test
    public void accept() {
        final FloatValueVisitor visitor = new FloatValueVisitor();
        new FloatValue(VALUE).accept(visitor);
        assertTrue("accept() didn't call any method in visitor", visitor.visited);
    }

}
