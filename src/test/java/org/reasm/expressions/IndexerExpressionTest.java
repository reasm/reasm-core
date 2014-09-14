package org.reasm.expressions;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.junit.Test;
import org.reasm.StaticSymbol;
import org.reasm.Symbol;
import org.reasm.UnsignedIntValue;
import org.reasm.Value;
import org.reasm.ValueVisitor;

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

    private static final EvaluationContext EVALUATION_CONTEXT_WITH_SYMBOL_LOOKUP = new EvaluationContext(null, 0, SYMBOL_LOOKUP,
            null);

    private static final Expression SUBJECT_EXPRESSION = new IdentifierExpression("foo");
    private static final Expression VALUE_INDEX_EXPRESSION = new ValueExpression(new UnsignedIntValue(0));
    private static final Expression SYMBOLIC_INDEX_EXPRESSION = new IdentifierExpression("bar");
    private static final IndexerExpression INDEXER_EXPRESSION_WITH_VALUE_INDEX = new IndexerExpression(SUBJECT_EXPRESSION,
            VALUE_INDEX_EXPRESSION);
    private static final IndexerExpression INDEXER_EXPRESSION_WITH_SYMBOLIC_INDEX = new IndexerExpression(SUBJECT_EXPRESSION,
            SYMBOLIC_INDEX_EXPRESSION);

    /**
     * Asserts that {@link IndexerExpression#evaluate(EvaluationContext)} evaluates to the symbol identified by the indexer
     * expression.
     */
    @Test
    public void evaluate() {
        assertThat(INDEXER_EXPRESSION_WITH_SYMBOLIC_INDEX.evaluate(EVALUATION_CONTEXT_WITH_SYMBOL_LOOKUP),
                is((Value) new UnsignedIntValue(2)));
    }

    /**
     * Asserts that {@link IndexerExpression#IndexerExpression(Expression, Expression)} correctly initializes an
     * {@link IndexerExpression}.
     */
    @Test
    public void indexerExpression() {
        assertThat(INDEXER_EXPRESSION_WITH_VALUE_INDEX.getSubjectExpression(), is(sameInstance(SUBJECT_EXPRESSION)));
        assertThat(INDEXER_EXPRESSION_WITH_VALUE_INDEX.getIndexExpression(), is(sameInstance(VALUE_INDEX_EXPRESSION)));
    }

    /**
     * Asserts that {@link IndexerExpression#IndexerExpression(Expression, Expression)} throws a {@link NullPointerException} when
     * the <code>indexExpression</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void indexerExpressionNullIndexExpression() {
        new IndexerExpression(SUBJECT_EXPRESSION, null);
    }

    /**
     * Asserts that {@link IndexerExpression#IndexerExpression(Expression, Expression)} throws a {@link NullPointerException} when
     * the <code>subjectExpression</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void indexerExpressionNullSubjectExpression() {
        new IndexerExpression(null, VALUE_INDEX_EXPRESSION);
    }

    /**
     * Asserts that {@link IndexerExpression#simplify(EvaluationContext)} simplifies to an {@link IdentifierExpression} with the
     * correct identifier when the index expression is an {@link IdentifierExpression}.
     */
    @Test
    public void simplifySymbolicIndexDefined() {
        assertThat(INDEXER_EXPRESSION_WITH_SYMBOLIC_INDEX.simplify(EVALUATION_CONTEXT_WITH_SYMBOL_LOOKUP),
                is((Expression) new IdentifierExpression("foo[1]")));
    }

    /**
     * Asserts that {@link IndexerExpression#simplify(EvaluationContext)} simplifies to {@link ValueExpression#UNDETERMINED} when
     * the index expression is undetermined.
     */
    @Test
    public void simplifySymbolicIndexUndetermined() {
        assertThat(INDEXER_EXPRESSION_WITH_SYMBOLIC_INDEX.simplify(new EvaluationContext(null, 0, null, null)),
                is((Expression) ValueExpression.UNDETERMINED));
    }

    /**
     * Asserts that {@link IndexerExpression#simplify(EvaluationContext)} simplifies to {@link ValueExpression#UNDETERMINED} when
     * the subject expression is undetermined.
     */
    @Test
    public void simplifySymbolicSubjectUndetermined() {
        final IndexerExpression indexerExpression = new IndexerExpression(ValueExpression.UNDETERMINED, SYMBOLIC_INDEX_EXPRESSION);
        assertThat(indexerExpression.simplify(new EvaluationContext(null, 0, null, null)),
                is((Expression) ValueExpression.UNDETERMINED));
    }

    /**
     * Asserts that {@link IndexerExpression#simplify(EvaluationContext)} simplifies to an {@link IdentifierExpression} with the
     * correct identifier when the index expression is a {@link ValueOrientedExpression}.
     */
    @Test
    public void simplifyValueIndex() {
        assertThat(INDEXER_EXPRESSION_WITH_VALUE_INDEX.simplify(new EvaluationContext(null, 0, null, null)),
                is((Expression) new IdentifierExpression("foo[0]")));
    }

    /**
     * Asserts that {@link IndexerExpression#toString()} returns a string representation of the expression.
     */
    @Test
    public void testToString() {
        assertThat(INDEXER_EXPRESSION_WITH_VALUE_INDEX.toString(),
                is("IndexerExpression [subjectExpression=IdentifierExpression [identifier=foo], "
                        + "indexExpression=ValueExpression [value=UnsignedIntValue [value=0]]]"));
    }

    /**
     * Asserts that {@link IndexerExpression#toIdentifier(EvaluationContext, ValueVisitor)} returns an identifier composed of the
     * subject expression followed by the index expression, evaluated, wrapped in brackets.
     */
    @Test
    public void toIdentifierWithSymbolicIndex() {
        assertThat(INDEXER_EXPRESSION_WITH_SYMBOLIC_INDEX.toIdentifier(EVALUATION_CONTEXT_WITH_SYMBOL_LOOKUP,
                new ValueToStringVisitor(EVALUATION_CONTEXT_WITH_SYMBOL_LOOKUP, "???")), is("foo[1]"));
    }

    /**
     * Asserts that {@link IndexerExpression#toIdentifier(EvaluationContext, ValueVisitor)} returns an identifier composed of the
     * subject expression followed by the index expression, evaluated, wrapped in brackets.
     */
    @Test
    public void toIdentifierWithValueIndex() {
        assertThat(INDEXER_EXPRESSION_WITH_VALUE_INDEX.toIdentifier(EvaluationContext.DUMMY, new ValueToStringVisitor(
                EvaluationContext.DUMMY, "???")), is("foo[0]"));
    }

}
