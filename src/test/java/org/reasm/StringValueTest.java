package org.reasm;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import javax.annotation.Nonnull;

import org.junit.Test;
import org.reasm.testhelpers.ValueVisitorAdapter;

import ca.fragag.testhelpers.ObjectHashCodeEqualsContract;

/**
 * Test class for {@link StringValue}.
 *
 * @author Francis Gagné
 */
public class StringValueTest extends ObjectHashCodeEqualsContract {

    /**
     * A visitor that asserts that the {@link ValueVisitor#visitString(String)} method is called with the correct value.
     *
     * @author Francis Gagné
     */
    private static final class StringValueVisitor extends ValueVisitorAdapter {

        boolean visited;

        StringValueVisitor() {
        }

        @Override
        public Void visitString(@Nonnull String value) {
            assertThat(value, is(VALUE));
            this.visited = true;
            return null;
        }

    }

    private static final String VALUE = "value";

    /**
     * Initializes a new StringValueTest.
     */
    public StringValueTest() {
        super(new StringValue(VALUE), new StringValue(VALUE), new StringValue(VALUE), new StringValue(VALUE + "2"), new Object());
    }

    /**
     * Asserts that {@link StringValue#accept(ValueVisitor)} calls {@link ValueVisitor#visitString(String)}.
     */
    @Test
    public void accept() {
        final StringValueVisitor visitor = new StringValueVisitor();
        new StringValue(VALUE).accept(visitor);
        assertTrue("accept() didn't call any method in visitor", visitor.visited);
    }

    /**
     * Asserts that {@link StringValue#StringValue(String)} throws a {@link NullPointerException} when the <code>value</code>
     * argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void stringValueNull() {
        new StringValue(null);
    }

}
