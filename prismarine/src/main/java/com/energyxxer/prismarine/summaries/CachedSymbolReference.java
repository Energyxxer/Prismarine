package com.energyxxer.prismarine.summaries;

import java.lang.ref.WeakReference;

public class CachedSymbolReference implements SymbolReference {
    private SymbolReference getter;
    private PrismarineProjectSummary lastProjectSummary;

    private WeakReference<SummarySymbol> cachedSymbol;
    private boolean symbolIsNull = false;
    private int generation = 0;

    public CachedSymbolReference(SymbolReference getter) {
        this.getter = getter;
    }

    @Override
    public SummarySymbol getSymbol(PrismarineSummaryModule summary) {
        if(cachedSymbol == null || (!symbolIsNull && cachedSymbol.get() == null) || summary.getParentSummary() == null || summary.getParentSummary() != lastProjectSummary || summary.getParentSummary().generation != this.generation) {
            SummarySymbol symbol = getter.getSymbol(summary);
            cachedSymbol = new WeakReference<>(symbol);
            symbolIsNull = symbol == null;
            lastProjectSummary = summary.parentSummary;
            generation = lastProjectSummary.getGeneration();
        }
        return cachedSymbol.get();
    }
}
