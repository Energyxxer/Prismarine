package com.energyxxer.prismarine.symbols;

import com.energyxxer.prismarine.summaries.SummarySymbol;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;

import java.nio.file.Path;

public abstract class SymbolVisibility {

    //Special. Visible from any context.
    public static final SymbolVisibility GLOBAL = new SymbolVisibility(999) {
        @Override
        public boolean isVisibleFromContext(Symbol symbol, ISymbolContext containingContext, ISymbolContext accessingContext) {
            return true;
        }

        @Override
        public boolean isVisibleFromSummaryBlock(SummarySymbol symbol, Path fromPath, int inFileIndex) {
            return true;
        }

        @Override
        public String toString() {
            return "GLOBAL";
        }
    };

    //Accessible from anywhere, so long as it's accessed from the parent
    public static final SymbolVisibility PUBLIC = new SymbolVisibility(998) {
        @Override
        public boolean isVisibleFromContext(Symbol symbol, ISymbolContext containingContext, ISymbolContext accessingContext) {
            return true;
        }

        @Override
        public boolean isVisibleFromSummaryBlock(SummarySymbol symbol, Path fromPath, int inFileIndex) {
            return true;
        }

        @Override
        public String toString() {
            return "PUBLIC";
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

    public boolean isVisibleMemberFromSummaryBlock(SummarySymbol symbol, Path fromPath, int inFileIndex) {
        return isVisibleFromSummaryBlock(symbol, fromPath, inFileIndex);
    }
}
