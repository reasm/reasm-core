package org.reasm;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.reasm.testhelpers.ValueVisitorAdapter;

import ca.fragag.testhelpers.ObjectHashCodeEqualsContract;

/**
 * Test class for {@link SignedIntValue}.
 *
 * @author Francis Gagné
 */
public class SignedIntValueTest extends ObjectHashCodeEqualsContract {

    /**
     * A visitor that asserts that the {@link ValueVisitor#visitSignedInt(long)} method is called with the correct value.
     *
     * @author Francis Gagné
     */
    private static final class SignedIntValueVisitor extends ValueVisitorAdapter<Void> {

        boolean visited;

        SignedIntValueVisitor() {
        }

        @Override
        public Void visitSignedInt(long value) {
            assertThat(value, is(VALUE));
            this.visited = true;
            return null;
        }

    }

    private static final long VALUE = 1234;

    /**
     * Initializes a new SignedIntValueTest.
     */
    public SignedIntValueTest() {
        super(new SignedIntValue(VALUE), new SignedIntValue(VALUE), new SignedIntValue(VALUE), new SignedIntValue(VALUE + 1),
                new Object());
    }

    /**
     * Asserts that {@link SignedIntValue#accept(ValueVisitor)} calls {@link ValueVisitor#visitSignedInt(long)}.
     */
    @Test
    public void accept() {
        final SignedIntValueVisitor visitor = new SignedIntValueVisitor();
        new SignedIntValue(VALUE).accept(visitor);
        assertTrue("accept() didn't call any method in visitor", visitor.visited);
    }

}
