package org.reasm;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Test class for {@link SymbolContext}.
 *
 * @author Francis Gagné
 */
public class SymbolContextTest {

    /**
     * Asserts that {@link SymbolContext#SymbolContext(Class)} initializes a {@link SymbolContext} object correctly.
     */
    @Test
    public void symbolContext() {
        assertThat(new SymbolContext<>(Integer.class).getValueType(), is(equalTo(Integer.class)));
    }

    /**
     * Asserts that {@link SymbolContext#SymbolContext(Class)} throws a {@link NullPointerException} when the <code>valueType</code>
     * argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void symbolContextNullClass() {
        new SymbolContext<>(null);
    }

}
