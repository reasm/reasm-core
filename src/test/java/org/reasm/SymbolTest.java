package org.reasm;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Test class for {@link Symbol}.
 *
 * @author Francis Gagné
 */
public class SymbolTest {

    /**
     * Asserts that {@link Symbol#Symbol(String, SymbolType)} initializes a {@link Symbol} correctly.
     */
    @Test
    public void symbol() {
        final Symbol symbol = new Symbol("foo", SymbolType.CONSTANT) {
            @Override
            public Object getValue() {
                fail();
                return null;
            }
        };

        assertThat(symbol.getName(), is("foo"));
        assertThat(symbol.getType(), is(SymbolType.CONSTANT));
    }

    /**
     * Asserts that {@link Symbol#Symbol(String, SymbolType)} throws a {@link NullPointerException} if the <code>type</code>
     * argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void symbolNullType() {
        new Symbol("foo", null) {
            @Override
            public Object getValue() {
                fail();
                return null;
            }
        };
    }

}
