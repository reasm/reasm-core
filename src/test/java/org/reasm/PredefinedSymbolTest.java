package org.reasm;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Test class for {@link PredefinedSymbol}.
 *
 * @author Francis Gagné
 */
public class PredefinedSymbolTest {

    /**
     * Asserts that {@link PredefinedSymbol#PredefinedSymbol(SymbolContext, String, SymbolType, Object)} initializes a
     * {@link PredefinedSymbol} correctly.
     */
    @Test
    public void predefinedSymbol() {
        final SymbolContext<Integer> context = new SymbolContext<>(Integer.class);
        final Integer value = new Integer(42);
        final PredefinedSymbol predefinedSymbol = new PredefinedSymbol(context, "foo", SymbolType.CONSTANT, value);
        assertThat(predefinedSymbol.getContext(), is(sameInstance((Object) context)));
        assertThat(predefinedSymbol.getName(), is("foo"));
        assertThat(predefinedSymbol.getType(), is(SymbolType.CONSTANT));
        assertThat(predefinedSymbol.getValue(), is(sameInstance((Object) value)));
    }

    /**
     * Asserts that {@link PredefinedSymbol#PredefinedSymbol(SymbolContext, String, SymbolType, Object)} throws a
     * {@link NullPointerException} when the <code>context</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void predefinedSymbolNullContext() {
        new PredefinedSymbol(null, "foo", SymbolType.CONSTANT, new Object());
    }

    /**
     * Asserts that {@link PredefinedSymbol#PredefinedSymbol(SymbolContext, String, SymbolType, Object)} throws a
     * {@link NullPointerException} when the <code>context</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void predefinedSymbolNullName() {
        new PredefinedSymbol(SymbolContext.VALUE, null, SymbolType.CONSTANT, new UnsignedIntValue(42));
    }

    /**
     * Asserts that {@link PredefinedSymbol#PredefinedSymbol(SymbolContext, String, SymbolType, Object)} throws a
     * {@link NullPointerException} when the <code>context</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void predefinedSymbolNullType() {
        new PredefinedSymbol(SymbolContext.VALUE, "foo", null, new UnsignedIntValue(42));
    }

}
