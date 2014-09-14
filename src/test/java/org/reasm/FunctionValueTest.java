package org.reasm;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import javax.annotation.Nonnull;

import org.junit.Test;
import org.reasm.expressions.EvaluationContext;
import org.reasm.expressions.Expression;
import org.reasm.expressions.ValueExpression;
import org.reasm.testhelpers.ValueVisitorAdapter;

import ca.fragag.testhelpers.ObjectHashCodeEqualsContract;

/**
 * Test class for {@link FunctionValue}.
 *
 * @author Francis Gagné
 */
public class FunctionValueTest extends ObjectHashCodeEqualsContract {

    /**
     * A dummy implementation of {@link Function}.
     *
     * @author Francis Gagné
     */
    private static final class DummyFunction implements Function {

        DummyFunction() {
        }

        @Nonnull
        @Override
        public Expression call(@Nonnull Expression[] arguments, @Nonnull EvaluationContext evaluationContext) {
            return ValueExpression.UNDETERMINED;
        }

    }

    /**
     * A visitor that asserts that the {@link ValueVisitor#visitFunction(Function)} method is called with the correct value.
     *
     * @author Francis Gagné
     */
    private static final class FunctionValueVisitor extends ValueVisitorAdapter {

        boolean visited;

        FunctionValueVisitor() {
        }

        @Override
        public Void visitFunction(@Nonnull Function value) {
            assertThat(value, is(sameInstance(FUNCTION1)));
            this.visited = true;
            return null;
        }

    }

    static final Function FUNCTION1 = new DummyFunction();
    private static final Function FUNCTION2 = new DummyFunction();

    /**
     * Initializes a new FunctionValueTest.
     */
    public FunctionValueTest() {
        super(new FunctionValue(FUNCTION1), new FunctionValue(FUNCTION1), new FunctionValue(FUNCTION1),
                new FunctionValue(FUNCTION2), new Object());
    }

    /**
     * Asserts that {@link FunctionValue#accept(ValueVisitor)} calls {@link ValueVisitor#visitFunction(Function)}.
     */
    @Test
    public void accept() {
        final FunctionValueVisitor visitor = new FunctionValueVisitor();
        new FunctionValue(FUNCTION1).accept(visitor);
        assertTrue("accept() didn't call any method in visitor", visitor.visited);
    }

    /**
     * Asserts that {@link FunctionValue#FunctionValue(Function)} throws a {@link NullPointerException} when the
     * <code>function</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void functionValueNull() {
        new FunctionValue(null);
    }

}
