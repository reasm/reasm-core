package org.reasm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * A table of predefined symbols for a {@link Configuration}. Predefined symbols are symbols that are automatically added to an
 * assembly.
 * <p>
 * This class is immutable.
 *
 * @author Francis Gagné
 */
@Immutable
public final class PredefinedSymbolTable {

    /** A {@link PredefinedSymbolTable} with no predefined symbols. */
    @Nonnull
    public static final PredefinedSymbolTable EMPTY = new PredefinedSymbolTable();

    @Nonnull
    private final ArrayList<PredefinedSymbol> symbols = new ArrayList<>();

    /**
     * Initializes a new PredefinedSymbolTable with no predefined symbols.
     *
     * @see #EMPTY
     */
    public PredefinedSymbolTable() {
    }

    /**
     * Initializes a new PredefinedSymbolTable with the specified collection of predefined symbols.
     *
     * @param symbols
     *            An {@link Iterable} of symbols. This collection must not contain two or more symbols with the same name.
     */
    public PredefinedSymbolTable(@Nonnull Iterable<PredefinedSymbol> symbols) {
        if (symbols == null) {
            throw new NullPointerException("symbols");
        }

        // This set will store the name of the symbols as they are added to the list. Before adding a symbol to the list, we check
        // if the name exists in this set; if it does, an exception is thrown.
        TreeSet<String> symbolNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        for (PredefinedSymbol symbol : symbols) {
            if (!symbolNames.add(symbol.getName())) {
                throw new IllegalArgumentException("Symbol " + symbol.getName()
                        + " is present more than once in the provided collection.");
            }

            this.symbols.add(symbol);
        }
    }

    /**
     * Gets a collection of the predefined symbols in this predefined symbol table.
     *
     * @return a {@link Collection} of the predefined symbols
     */
    @Nonnull
    public final Collection<PredefinedSymbol> symbols() {
        return Collections.unmodifiableCollection(this.symbols);
    }

}
