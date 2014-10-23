package org.reasm.testhelpers;

import javax.annotation.concurrent.Immutable;

import org.reasm.Symbol;
import org.reasm.expressions.SymbolLookup;

/**
 * An implementation of {@link SymbolLookup} that fails when its {@link #getSymbol(String)} method is called.
 *
 * @author Francis Gagné
 */
@Immutable
public final class DummySymbolLookup implements SymbolLookup {

    /** A default instance of the {@link DummySymbolLookup} class. */
    public static final DummySymbolLookup DEFAULT = new DummySymbolLookup();

    @Override
    public final Symbol getSymbol(String name) {
        throw new AssertionError("Symbol lookup shouldn't be invoked in this test");
    }

}
