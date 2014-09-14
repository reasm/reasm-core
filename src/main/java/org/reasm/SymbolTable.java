package org.reasm;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A symbol table. {@link Assembly} objects automatically create a symbol table.
 *
 * @author Francis Gagné
 */
final class SymbolTable implements Iterable<UserSymbol> {

    static Iterator<UserSymbol> nextSymbolIterator(@Nonnull Iterator<? extends Map<String, UserSymbol>> contextIterator) {
        if (contextIterator.hasNext()) {
            return contextIterator.next().values().iterator();
        }

        return null;
    }

    @Nonnull
    final IdentityHashMap<SymbolContext<?>, TreeMap<String, UserSymbol>> symbols = new IdentityHashMap<>();

    @Nonnull
    @Override
    public Iterator<UserSymbol> iterator() {
        return new Iterator<UserSymbol>() {

            private final Iterator<? extends Map<String, UserSymbol>> contextIterator = SymbolTable.this.symbols.values()
                    .iterator();
            private Iterator<UserSymbol> symbolIterator = nextSymbolIterator(this.contextIterator);

            @Override
            public boolean hasNext() {
                for (;;) {
                    // When symbolIterator is null, the iteration is over.
                    if (this.symbolIterator == null) {
                        return false;
                    }

                    if (this.symbolIterator.hasNext()) {
                        return true;
                    }

                    this.symbolIterator = nextSymbolIterator(this.contextIterator);
                }
            }

            @Override
            public UserSymbol next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                }

                return this.symbolIterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    /**
     * Adds the specified symbol to the symbol table.
     *
     * @param symbol
     *            The symbol to add to the symbol table. Cannot be <code>null</code>.
     * @throws IllegalArgumentException
     *             A symbol with the same name is already present in the symbol table.
     */
    final void addSymbol(@Nonnull UserSymbol symbol) {
        final SymbolContext<?> context = symbol.getContext();
        TreeMap<String, UserSymbol> contextSymbols = this.symbols.get(context);
        if (contextSymbols == null) {
            contextSymbols = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            this.symbols.put(context, contextSymbols);
        }

        final String name = symbol.getName();
        assert !contextSymbols.containsKey(name) : "Symbol " + name + " is already present in the symbol table.";
        contextSymbols.put(name, symbol);
    }

    /**
     * Gets the symbol in the specified context and with the specified name in the symbol table.
     *
     * @param context
     *            the context in which the symbol is defined
     * @param name
     *            the name of the symbol
     * @return the symbol in the specified context and with the specified name in the symbol table, or <code>null</code> if the
     *         symbol table does not contain a symbol in that context and with that name
     */
    @CheckForNull
    final UserSymbol getSymbol(@Nonnull SymbolContext<?> context, @Nonnull String name) {
        final TreeMap<String, UserSymbol> contextSymbols = this.symbols.get(context);
        if (contextSymbols != null) {
            return contextSymbols.get(name);
        }

        return null;
    }

}
