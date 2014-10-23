package org.reasm.expressions;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.junit.Test;
import org.reasm.StaticSymbol;
import org.reasm.StringValue;
import org.reasm.Symbol;
import org.reasm.UnsignedIntValue;
import org.reasm.Value;
import org.reasm.ValueVisitor;
import org.reasm.testhelpers.DummySymbolLookup;

/**
 * Test class for {@link IndexerExpression}.
 *
 * @author Francis Gagné
 */
public class IndexerExpressionTest {

    private static final SymbolLookup SYMBOL_LOOKUP = new SymbolLookup() {

        private final Symbol foo1Symbol = new StaticSymbol(new UnsignedIntValue(2));
        private final Symbol barSymbol = new StaticSymbol(new UnsignedIntValue(1));

        @CheckForNull
        @Override
        public Symbol getSymbol(@Nonnull String name) {
            if ("foo[1]".equalsIgnoreCase(name)) {
                return this.foo1Symbol;
            }

            if ("bar".equalsIgnoreCase(name)) {
                return this.barSymbol;
            }

            fail("Unexpected symbol lookup attempted on name \"" + name + "\"");
            return null;
        }

    };

    private static final Expression SUBJECT_EXPRESSION = new IdentifierExpression("foo", DummySymbolLookup.DEFAULT);
    private static final Expression VALUE_INDEX_EXPRESSION = new ValueExpression(new UnsignedIntValue(0));
    private static final Expression SYMBOLIC_INDEX_EXPRESSION = new IdentifierExpression("bar", SYMBOL_LOOKUP);
    private static final IndexerExpression INDEXER_EXPRESSION_WITH_VALUE_INDEX = new IndexerExpression(SUBJECT_EXPRESSION,
            VALUE_INDEX_EXPRESSION, DummySymbolLookup.DEFAULT);

    /**
     * Asserts that {@link IndexerExpression#evaluate(EvaluationContext)} evaluates to the value of the symbol identified by the
     * indexer expression.
     */
    @Test
    public void evaluate() {
        final Expression subjectExpression = new IdentifierExpression("foo", SYMBOL_LOOKUP);
        final Expression indexExpression = SYMBOLIC_INDEX_EXPRESSION;
        final IndexerExpression indexerExpression = new IndexerExpression(subjectExpression, indexExpression,
                DummySymbolLookup.DEFAULT);
        assertThat(indexerExpression.evaluate(EvaluationContext.DUMMY), is((Value) new UnsignedIntValue(2)));
    }

    /**
     * Asserts that {@link IndexerExpression#IndexerExpression(Expression, Expression, SymbolLookup)} correctly initializes an
     * {@link IndexerExpression}.
     */
    @Test
    public void indexerExpression() {
        assertThat(INDEXER_EXPRESSION_WITH_VALUE_INDEX.getSubjectExpression(), is(sameInstance(SUBJECT_EXPRESSION)));
        assertThat(INDEXER_EXPRESSION_WITH_VALUE_INDEX.getIndexExpression(), is(sameInstance(VALUE_INDEX_EXPRESSION)));
        assertThat(INDEXER_EXPRESSION_WITH_VALUE_INDEX.getFallbackSymbolLookup(),
                is(sameInstance((SymbolLookup) DummySymbolLookup.DEFAULT)));
    }

    /**
     * Asserts that {@link IndexerExpression#IndexerExpression(Expression, Expression, SymbolLookup)} throws a
     * {@link NullPointerException} when the <code>indexExpression</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void indexerExpressionNullIndexExpression() {
        new IndexerExpression(SUBJECT_EXPRESSION, null, DummySymbolLookup.DEFAULT);
    }

    /**
     * Asserts that {@link IndexerExpression#IndexerExpression(Expression, Expression, SymbolLookup)} throws a
     * {@link NullPointerException} when the <code>subjectExpression</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void indexerExpressionNullSubjectExpression() {
        new IndexerExpression(null, VALUE_INDEX_EXPRESSION, DummySymbolLookup.DEFAULT);
    }

    /**
     * Asserts that {@link IndexerExpression#simplify(EvaluationContext)} simplifies to an {@link IdentifierExpression} with the
     * correct identifier when the index expression is an {@link IdentifierExpression}.
     */
    @Test
    public void simplifySymbolicIndexDefined() {
        final Expression subjectExpression = SUBJECT_EXPRESSION;
        final Expression indexExpression = SYMBOLIC_INDEX_EXPRESSION;
        final IndexerExpression indexerExpression = new IndexerExpression(subjectExpression, indexExpression,
                DummySymbolLookup.DEFAULT);
        final Expression expected = new IdentifierExpression("foo[1]", DummySymbolLookup.DEFAULT);
        assertThat(indexerExpression.simplify(EvaluationContext.DUMMY), is(expected));
    }

    /**
     * Asserts that {@link IndexerExpression#simplify(EvaluationContext)} simplifies to {@link ValueExpression#UNDETERMINED} when
     * the index expression is undetermined.
     */
    @Test
    public void simplifySymbolicIndexUndetermined() {
        final Expression subjectExpression = new IdentifierExpression("foo", null);
        final Expression indexExpression = new IdentifierExpression("bar", null);
        final IndexerExpression indexerExpression = new IndexerExpression(subjectExpression, indexExpression, null);
        final Expression expected = ValueExpression.UNDETERMINED;
        assertThat(indexerExpression.simplify(EvaluationContext.DUMMY), is(expected));
    }

    /**
     * Asserts that {@link IndexerExpression#simplify(EvaluationContext)} simplifies to {@link ValueExpression#UNDETERMINED} when
     * the subject expression is undetermined.
     */
    @Test
    public void simplifySymbolicSubjectUndetermined() {
        final Expression subjectExpression = ValueExpression.UNDETERMINED;
        final Expression indexExpression = SYMBOLIC_INDEX_EXPRESSION;
        final IndexerExpression indexerExpression = new IndexerExpression(subjectExpression, indexExpression,
                DummySymbolLookup.DEFAULT);
        final Expression expected = ValueExpression.UNDETERMINED;
        assertThat(indexerExpression.simplify(EvaluationContext.DUMMY), is(expected));
    }

    /**
     * Asserts that {@link IndexerExpression#simplify(EvaluationContext)} simplifies to an {@link IdentifierExpression} with the
     * correct identifier when the index expression is a {@link ValueOrientedExpression}.
     */
    @Test
    public void simplifyValueIndex() {
        final Expression subjectExpression = SUBJECT_EXPRESSION;
        final Expression indexExpression = new ValueExpression(new UnsignedIntValue(0));
        final IndexerExpression indexerExpression = new IndexerExpression(subjectExpression, indexExpression,
                DummySymbolLookup.DEFAULT);
        final Expression expected = new IdentifierExpression("foo[0]", DummySymbolLookup.DEFAULT);
        assertThat(indexerExpression.simplify(EvaluationContext.DUMMY), is(expected));
    }

    /**
     * Asserts that {@link IndexerExpression#simplify(EvaluationContext)} simplifies to an {@link IdentifierExpression} with the
     * correct identifier when the subject expression is a {@link ValueOrientedExpression} by using the {@link SymbolLookup} from
     * the {@link IndexerExpression}.
     */
    @Test
    public void simplifyValueSubject() {
        final Expression subjectExpression = new ValueExpression(new StringValue("foo"));
        final Expression indexExpression = new ValueExpression(new UnsignedIntValue(0));
        final IndexerExpression indexerExpression = new IndexerExpression(subjectExpression, indexExpression, SYMBOL_LOOKUP);
        final Expression expected = new IdentifierExpression("foo[0]", SYMBOL_LOOKUP);
        assertThat(indexerExpression.simplify(EvaluationContext.DUMMY), is(expected));
    }

    /**
     * Asserts that {@link IndexerExpression#toString()} returns a string representation of the expression.
     */
    @Test
    public void testToString() {
        assertThat(INDEXER_EXPRESSION_WITH_VALUE_INDEX.toString(),
                is("IndexerExpression [subjectExpression=" + SUBJECT_EXPRESSION.toString() + ", " + "indexExpression="
                        + VALUE_INDEX_EXPRESSION.toString() + ", " + "fallbackSymbolLookup=" + DummySymbolLookup.DEFAULT.toString()
                        + "]"));
    }

    /**
     * Asserts that {@link IndexerExpression#toIdentifier(EvaluationContext, ValueVisitor)} returns an identifier composed of the
     * subject expression followed by the index expression, evaluated, wrapped in brackets.
     */
    @Test
    public void toIdentifierWithSymbolicIndex() {
        final Expression subjectExpression = SUBJECT_EXPRESSION;
        final Expression indexExpression = SYMBOLIC_INDEX_EXPRESSION;
        final IndexerExpression indexerExpression = new IndexerExpression(subjectExpression, indexExpression,
                DummySymbolLookup.DEFAULT);
        assertThat(
                indexerExpression.toIdentifier(EvaluationContext.DUMMY, new ValueToStringVisitor(EvaluationContext.DUMMY, "???")),
                is(new IdentifierExpression("foo[1]", DummySymbolLookup.DEFAULT)));
    }

    /**
     * Asserts that {@link IndexerExpression#toIdentifier(EvaluationContext, ValueVisitor)} returns an identifier composed of the
     * subject expression followed by the index expression, evaluated, wrapped in brackets.
     */
    @Test
    public void toIdentifierWithValueIndex() {
        final Expression subjectExpression = SUBJECT_EXPRESSION;
        final Expression indexExpression = new ValueExpression(new UnsignedIntValue(0));
        final IndexerExpression indexerExpression = new IndexerExpression(subjectExpression, indexExpression, SYMBOL_LOOKUP);
        assertThat(
                indexerExpression.toIdentifier(EvaluationContext.DUMMY, new ValueToStringVisitor(EvaluationContext.DUMMY, "???")),
                is(new IdentifierExpression("foo[0]", DummySymbolLookup.DEFAULT)));
    }

}
