package org.reasm.expressions;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;

import org.junit.Test;
import org.reasm.AssemblyMessage;
import org.reasm.Function;
import org.reasm.messages.FunctionOperandNotApplicableErrorMessage;
import org.reasm.testhelpers.AssemblyMessageCollector;
import org.reasm.testhelpers.DummyFunction;
import org.reasm.testhelpers.EquivalentAssemblyMessage;

/**
 * Test class for {@link ValueToStringVisitor}.
 *
 * @author Francis Gagné
 */
public class ValueToStringVisitorTest {

    private static final ValueToStringVisitor VALUE_TO_STRING_VISITOR = new ValueToStringVisitor(EvaluationContext.DUMMY, "???");
    private static final Function DUMMY_FUNCTION = new DummyFunction();

    /**
     * Asserts that {@link ValueToStringVisitor#visitFloat(double)} returns the string representation of a floating-point number.
     */
    @Test
    public void visitFloat() {
        assertThat(VALUE_TO_STRING_VISITOR.visitFloat(3.25), is("3.25"));
    }

    /**
     * Asserts that {@link ValueToStringVisitor#visitFunction(Function)} returns <code>null</code> and emits a
     * {@link FunctionOperandNotApplicableErrorMessage}.
     */
    @Test
    public void visitFunction() {
        final ArrayList<AssemblyMessage> messages = new ArrayList<>();
        final ValueToStringVisitor valueToStringVisitor = new ValueToStringVisitor(new EvaluationContext(null, 0,
                new AssemblyMessageCollector(messages)), "???");
        assertThat(valueToStringVisitor.visitFunction(DUMMY_FUNCTION), is(nullValue()));
        assertThat(messages, contains(new EquivalentAssemblyMessage(new FunctionOperandNotApplicableErrorMessage("???"))));
    }

    /**
     * Asserts that {@link ValueToStringVisitor#visitSignedInt(long)} returns the string representation of a signed integer.
     */
    @Test
    public void visitSignedInt() {
        assertThat(VALUE_TO_STRING_VISITOR.visitSignedInt(42), is("42"));
    }

    /**
     * Asserts that {@link ValueToStringVisitor#visitSignedInt(long)} returns the string representation of a negative signed
     * integer.
     */
    @Test
    public void visitSignedIntNegative() {
        assertThat(VALUE_TO_STRING_VISITOR.visitSignedInt(-1), is("-1"));
    }

    /**
     * Asserts that {@link ValueToStringVisitor#visitString(String)} returns the string as is.
     */
    @Test
    public void visitString() {
        assertThat(VALUE_TO_STRING_VISITOR.visitString("foo"), is("foo"));
    }

    /**
     * Asserts that {@link ValueToStringVisitor#visitUndetermined()} returns <code>null</code>.
     */
    @Test
    public void visitUndetermined() {
        assertThat(VALUE_TO_STRING_VISITOR.visitUndetermined(), is(nullValue()));
    }

    /**
     * Asserts that {@link ValueToStringVisitor#visitUnsignedInt(long)} returns the string representation of an unsigned integer.
     */
    @Test
    public void visitUnsignedInt() {
        assertThat(VALUE_TO_STRING_VISITOR.visitUnsignedInt(42), is("42"));
    }

    /**
     * Asserts that {@link ValueToStringVisitor#visitUnsignedInt(long)} returns the string representation of an unsigned integer
     * greater than {@link Long#MAX_VALUE}.
     */
    @Test
    public void visitUnsignedIntLarge() {
        assertThat(VALUE_TO_STRING_VISITOR.visitUnsignedInt(-1), is("18446744073709551615"));
    }

}
