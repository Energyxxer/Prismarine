package com.energyxxer.prismarine.symbols;

import com.energyxxer.prismarine.summaries.SummarySymbol;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;

import java.nio.file.Path;

public abstract class SymbolVisibility {

    public static final SymbolVisibility GLOBAL = new SymbolVisibility(999) {
        @Override
        public boolean isVisibleFromContext(Symbol symbol, ISymbolContext containingContext, ISymbolContext accessingContext) {
            return true;
        }

        @Override
        public boolean isVisibleFromSummaryBlock(SummarySymbol symbol, Path fromPath, int inFileIndex) {
            return true;
        }
    };

    private final int visibilityIndex;

    public SymbolVisibility(int visibilityIndex) {
        this.visibilityIndex = visibilityIndex;
    }

    public static SymbolVisibility min(SymbolVisibility a, SymbolVisibility b) {
        if(a == null) return b;
        if(b == null) return a;
        return a.getVisibilityIndex() < b.getVisibilityIndex() ? a : b;
    }

    public static SymbolVisibility max(SymbolVisibility a, SymbolVisibility b) {
        if(a == null) return b;
        if(b == null) return a;
        return a.getVisibilityIndex() > b.getVisibilityIndex() ? a : b;
    }

    public int getVisibilityIndex() {
        return visibilityIndex;
    }

    public abstract boolean isVisibleFromContext(Symbol symbol, ISymbolContext containingContext, ISymbolContext accessingContext);

    public abstract boolean isVisibleFromSummaryBlock(SummarySymbol symbol, Path fromPath, int inFileIndex);
}
