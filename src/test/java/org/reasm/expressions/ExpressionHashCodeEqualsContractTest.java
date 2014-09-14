package org.reasm.expressions;

import java.util.Arrays;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reasm.UnsignedIntValue;

import ca.fragag.testhelpers.ObjectHashCodeEqualsContract;

/**
 * Test class for {@link Expression} using {@link ObjectHashCodeEqualsContract}.
 *
 * @author Francis Gagné
 */
@RunWith(Parameterized.class)
public class ExpressionHashCodeEqualsContractTest extends ObjectHashCodeEqualsContract {

    private static final Object[][] TEST_DATA_VALUES = new Object[][] {

            // ValueExpression
            { new ValueExpression(new UnsignedIntValue(1)), new ValueExpression(new UnsignedIntValue(1)),
                    new ValueExpression(new UnsignedIntValue(1)),
                    new Object[] { new ValueExpression(new UnsignedIntValue(2)), new Object() } },

            // IdentifierExpression
            { new IdentifierExpression("foo"), new IdentifierExpression("foo"), new IdentifierExpression("foo"),
                    new Object[] { new IdentifierExpression("bar"), new Object() } },

            // ProgramCounterExpression
            { ProgramCounterExpression.INSTANCE, ProgramCounterExpression.INSTANCE, ProgramCounterExpression.INSTANCE,
                    new Object[] { new Object() } },

            // UnaryOperatorExpression
            {
                    new UnaryOperatorExpression(UnaryOperator.NEGATION, new ValueExpression(new UnsignedIntValue(1))),
                    new UnaryOperatorExpression(UnaryOperator.NEGATION, new ValueExpression(new UnsignedIntValue(1))),
                    new UnaryOperatorExpression(UnaryOperator.NEGATION, new ValueExpression(new UnsignedIntValue(1))),
                    new Object[] {
                            new UnaryOperatorExpression(UnaryOperator.NEGATION, new ValueExpression(new UnsignedIntValue(2))),
                            new UnaryOperatorExpression(UnaryOperator.BITWISE_NOT, new ValueExpression(new UnsignedIntValue(1))),
                            new Object() } },

            // BinaryOperatorExpression
            {
                    new BinaryOperatorExpression(BinaryOperator.ADDITION, new ValueExpression(new UnsignedIntValue(1)),
                            new ValueExpression(new UnsignedIntValue(2))),
                    new BinaryOperatorExpression(BinaryOperator.ADDITION, new ValueExpression(new UnsignedIntValue(1)),
                            new ValueExpression(new UnsignedIntValue(2))),
                    new BinaryOperatorExpression(BinaryOperator.ADDITION, new ValueExpression(new UnsignedIntValue(1)),
                            new ValueExpression(new UnsignedIntValue(2))),
                    new Object[] {
                            new BinaryOperatorExpression(BinaryOperator.ADDITION, new ValueExpression(new UnsignedIntValue(1)),
                                    new ValueExpression(new UnsignedIntValue(3))),
                            new BinaryOperatorExpression(BinaryOperator.ADDITION, new ValueExpression(new UnsignedIntValue(3)),
                                    new ValueExpression(new UnsignedIntValue(2))),
                            new BinaryOperatorExpression(BinaryOperator.SUBTRACTION, new ValueExpression(new UnsignedIntValue(1)),
                                    new ValueExpression(new UnsignedIntValue(2))), new Object() } },

            // GroupingExpression
            { new GroupingExpression(new ValueExpression(new UnsignedIntValue(1))),
                    new GroupingExpression(new ValueExpression(new UnsignedIntValue(1))),
                    new GroupingExpression(new ValueExpression(new UnsignedIntValue(1))),
                    new Object[] { new GroupingExpression(new ValueExpression(new UnsignedIntValue(2))), new Object() } },

            // PeriodExpression
            {
                    new PeriodExpression(new IdentifierExpression("foo"), new IdentifierExpression("bar")),
                    new PeriodExpression(new IdentifierExpression("foo"), new IdentifierExpression("bar")),
                    new PeriodExpression(new IdentifierExpression("foo"), new IdentifierExpression("bar")),
                    new Object[] { new PeriodExpression(new IdentifierExpression("boo"), new IdentifierExpression("bar")),
                            new PeriodExpression(new IdentifierExpression("foo"), new IdentifierExpression("far")), new Object() } },

            // IndexerExpression
            {
                    new IndexerExpression(new IdentifierExpression("foo"), new IdentifierExpression("bar")),
                    new IndexerExpression(new IdentifierExpression("foo"), new IdentifierExpression("bar")),
                    new IndexerExpression(new IdentifierExpression("foo"), new IdentifierExpression("bar")),
                    new Object[] { new IndexerExpression(new IdentifierExpression("boo"), new IdentifierExpression("bar")),
                            new IndexerExpression(new IdentifierExpression("foo"), new IdentifierExpression("far")), new Object() } },

            // ConditionalExpression
            {
                    new ConditionalExpression(new IdentifierExpression("foo"), new IdentifierExpression("bar"),
                            new IdentifierExpression("quux")),
                    new ConditionalExpression(new IdentifierExpression("foo"), new IdentifierExpression("bar"),
                            new IdentifierExpression("quux")),
                    new ConditionalExpression(new IdentifierExpression("foo"), new IdentifierExpression("bar"),
                            new IdentifierExpression("quux")),
                    new Object[] {
                            new ConditionalExpression(new IdentifierExpression("boo"), new IdentifierExpression("bar"),
                                    new IdentifierExpression("quux")),
                            new ConditionalExpression(new IdentifierExpression("foo"), new IdentifierExpression("far"),
                                    new IdentifierExpression("quux")),
                            new ConditionalExpression(new IdentifierExpression("foo"), new IdentifierExpression("bar"),
                                    new IdentifierExpression("zuux")), new Object() } },

            // FunctionCallExpression
            {
                    new FunctionCallExpression(new IdentifierExpression("foo"), new IdentifierExpression("bar")),
                    new FunctionCallExpression(new IdentifierExpression("foo"), new IdentifierExpression("bar")),
                    new FunctionCallExpression(new IdentifierExpression("foo"), new IdentifierExpression("bar")),
                    new Object[] { new FunctionCallExpression(new IdentifierExpression("boo"), new IdentifierExpression("bar")),
                            new FunctionCallExpression(new IdentifierExpression("foo"), new IdentifierExpression("far")),
                            new Object() } },

    };

    private static final List<Object[]> TEST_DATA = Arrays.asList(TEST_DATA_VALUES);

    /**
     * The test data for the {@link Parameterized} test.
     *
     * @return the test data
     */
    @Nonnull
    @Parameters
    public static List<Object[]> data() {
        return TEST_DATA;
    }

    /**
     * Initializes a new ExpressionHashCodeEqualsContractTest.
     *
     * @param mainObject
     *            the main object to run tests on
     * @param otherEqualObject
     *            an object that is equal (by {@link Object#equals(Object)} to the main object
     * @param anotherEqualObject
     *            another object that is equal (by {@link Object#equals(Object)} to the main object and to the other equal object
     * @param differentObjects
     *            objects that are not equal (by {@link Object#equals(Object)} to the other objects
     */
    public ExpressionHashCodeEqualsContractTest(@Nonnull Object mainObject, @Nonnull Object otherEqualObject,
            @Nonnull Object anotherEqualObject, @CheckForNull Object... differentObjects) {
        super(mainObject, otherEqualObject, anotherEqualObject, differentObjects);
    }

}
