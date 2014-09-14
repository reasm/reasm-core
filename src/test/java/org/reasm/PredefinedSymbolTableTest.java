package org.reasm;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;

import org.hamcrest.Matcher;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * Test class for {@link PredefinedSymbolTable}.
 *
 * @author Francis Gagné
 */
public class PredefinedSymbolTableTest {

    /**
     * Asserts that {@link PredefinedSymbolTable#PredefinedSymbolTable()} initializes an empty {@link PredefinedSymbolTable}.
     */
    @Test
    public void predefinedSymbolTable() {
        final PredefinedSymbolTable table = new PredefinedSymbolTable();
        assertThat(table.symbols(), is(empty()));
    }

    /**
     * Asserts that {@link PredefinedSymbolTable#PredefinedSymbolTable(Iterable)} throws an {@link IllegalArgumentException} when
     * the collection contains {@link PredefinedSymbol PredefinedSymbols} with the same name.
     */
    @Test
    public void predefinedSymbolTableIterableDuplicateName() {
        final PredefinedSymbol predefinedSymbol1 = new PredefinedSymbol(SymbolContext.VALUE, "foo", SymbolType.CONSTANT,
                new UnsignedIntValue(42L));
        final PredefinedSymbol predefinedSymbol2 = new PredefinedSymbol(SymbolContext.VALUE, "bar", SymbolType.CONSTANT,
                new SignedIntValue(-78L));
        final PredefinedSymbol predefinedSymbol3 = new PredefinedSymbol(SymbolContext.VALUE, "foo", SymbolType.VARIABLE,
                new FloatValue(3.25));

        try {
            new PredefinedSymbolTable(Arrays.asList(predefinedSymbol1, predefinedSymbol2, predefinedSymbol3));
        } catch (IllegalArgumentException e) {
            return;
        }

        fail("new PredefinedSymbolTable(Iterable) should have thrown IllegalArgumentException");
    }

    /**
     * Asserts that {@link PredefinedSymbolTable#PredefinedSymbolTable(Iterable)} throws an {@link IllegalArgumentException} when
     * the collection contains {@link PredefinedSymbol PredefinedSymbols} with the same name, ignoring the case.
     */
    @Test
    public void predefinedSymbolTableIterableDuplicateNameDifferentCase() {
        final PredefinedSymbol predefinedSymbol1 = new PredefinedSymbol(SymbolContext.VALUE, "foo", SymbolType.CONSTANT,
                new UnsignedIntValue(42L));
        final PredefinedSymbol predefinedSymbol2 = new PredefinedSymbol(SymbolContext.VALUE, "bar", SymbolType.CONSTANT,
                new SignedIntValue(-78L));
        final PredefinedSymbol predefinedSymbol3 = new PredefinedSymbol(SymbolContext.VALUE, "FOO", SymbolType.VARIABLE,
                new FloatValue(3.25));

        try {
            new PredefinedSymbolTable(Arrays.asList(predefinedSymbol1, predefinedSymbol2, predefinedSymbol3));
        } catch (IllegalArgumentException e) {
            return;
        }

        fail("new PredefinedSymbolTable(Iterable) should have thrown IllegalArgumentException");
    }

    /**
     * Asserts that {@link PredefinedSymbolTable#PredefinedSymbolTable(Iterable)} initializes an empty {@link PredefinedSymbolTable}
     * when the <code>symbols</code> argument is an empty collection.
     */
    @Test
    public void predefinedSymbolTableIterableEmpty() {
        final PredefinedSymbolTable table = new PredefinedSymbolTable(Collections.<PredefinedSymbol> emptySet());
        assertThat(table.symbols(), is(empty()));
    }

    /**
     * Asserts that {@link PredefinedSymbolTable#PredefinedSymbolTable(Iterable)} throws a {@link NullPointerException} when the
     * <code>symbols</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void predefinedSymbolTableIterableNull() {
        new PredefinedSymbolTable(null);
    }

    /**
     * Asserts that {@link PredefinedSymbolTable#PredefinedSymbolTable(Iterable)} initializes a {@link PredefinedSymbolTable} with
     * the specified symbol.
     */
    @Test
    public void predefinedSymbolTableIterableOneSymbol() {
        final PredefinedSymbol predefinedSymbol = new PredefinedSymbol(SymbolContext.VALUE, "foo", SymbolType.CONSTANT,
                new UnsignedIntValue(42L));
        final PredefinedSymbolTable table = new PredefinedSymbolTable(Collections.singleton(predefinedSymbol));
        assertThat(table.symbols(), contains(sameInstance(predefinedSymbol)));
    }

    /**
     * Asserts that {@link PredefinedSymbolTable#PredefinedSymbolTable(Iterable)} initializes a {@link PredefinedSymbolTable} with
     * the specified symbols.
     */
    @Test
    public void predefinedSymbolTableIterableThreeSymbols() {
        final PredefinedSymbol predefinedSymbol1 = new PredefinedSymbol(SymbolContext.VALUE, "foo", SymbolType.CONSTANT,
                new UnsignedIntValue(42L));
        final PredefinedSymbol predefinedSymbol2 = new PredefinedSymbol(SymbolContext.VALUE, "bar", SymbolType.CONSTANT,
                new SignedIntValue(-78L));
        final PredefinedSymbol predefinedSymbol3 = new PredefinedSymbol(SymbolContext.VALUE, "baz", SymbolType.VARIABLE,
                new FloatValue(3.25));
        final PredefinedSymbolTable table = new PredefinedSymbolTable(Arrays.asList(predefinedSymbol1, predefinedSymbol2,
                predefinedSymbol3));
        assertThat(table.symbols(), contains(ImmutableList.<Matcher<? super PredefinedSymbol>> of(sameInstance(predefinedSymbol1),
                sameInstance(predefinedSymbol2), sameInstance(predefinedSymbol3))));
    }

}
