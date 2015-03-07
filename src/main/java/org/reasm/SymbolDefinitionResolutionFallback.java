package org.reasm;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

@Immutable
final class SymbolDefinitionResolutionFallback implements SymbolResolutionFallback {

    private static final SymbolDefinitionResolutionFallback CONSTANT = new SymbolDefinitionResolutionFallback(SymbolType.CONSTANT);
    private static final SymbolDefinitionResolutionFallback VARIABLE = new SymbolDefinitionResolutionFallback(SymbolType.VARIABLE);

    @Nonnull
    static SymbolDefinitionResolutionFallback getInstance(@Nonnull SymbolType symbolType) {
        switch (symbolType) {
        case CONSTANT:
            return CONSTANT;

        case VARIABLE:
            return VARIABLE;
        }

        throw new AssertionError(); // unreachable
    }

    @Nonnull
    private final SymbolType symbolType;

    private SymbolDefinitionResolutionFallback(@Nonnull SymbolType symbolType) {
        this.symbolType = symbolType;
    }

    @Nonnull
    @Override
    public UserSymbol resolve(@Nonnull SymbolReference symbolReference) {
        final UserSymbol newSymbol = new UserSymbol(symbolReference.getContexts().get(0), symbolReference.getDefinedName(),
                this.symbolType);
        symbolReference.getAssembly().addSymbol(newSymbol, symbolReference.getScope());
        return newSymbol;
    }

}
