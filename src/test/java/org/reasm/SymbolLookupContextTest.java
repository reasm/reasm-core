package org.reasm;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.reasm.AssemblyTestsCommon.NODE_THAT_SHOULD_NOT_BE_REACHED;
import static org.reasm.AssemblyTestsCommon.createAssembly;

import org.junit.Test;

/**
 * Test class for {@link SymbolLookupContext}.
 *
 * @author Francis Gagné
 */
public class SymbolLookupContextTest {

    /**
     * Asserts that {@link SymbolLookupContext#expandAnonymousSymbol(String)} returns the empty string when given the empty string.
     */
    @Test
    public void expandAnonymousSymbolEmpty() {
        final Assembly assembly = createAssembly(NODE_THAT_SHOULD_NOT_BE_REACHED);
        final SymbolLookupContext lookupContext = new SymbolLookupContext(assembly, null, null, 0, 0);
        assertThat(lookupContext.expandAnonymousSymbol(""), is(""));
    }

    /**
     * Asserts that {@link SymbolLookupContext#expandAnonymousSymbol(String)} returns the given string when it starts with '+' but
     * not all characters are '+'.
     */
    @Test
    public void expandAnonymousSymbolMisleading() {
        final Assembly assembly = createAssembly(NODE_THAT_SHOULD_NOT_BE_REACHED);
        final SymbolLookupContext lookupContext = new SymbolLookupContext(assembly, null, null, 0, 0);
        assertThat(lookupContext.expandAnonymousSymbol("+a"), is("+a"));
    }

}
