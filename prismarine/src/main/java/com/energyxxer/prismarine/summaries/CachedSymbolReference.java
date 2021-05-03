package com.energyxxer.prismarine.summaries;

import java.lang.ref.WeakReference;

public class CachedSymbolReference implements SymbolReference {
    private SymbolReference getter;
    private PrismarineProjectSummary lastProjectSummary;

    private WeakReference<SummarySymbol> cachedSymbol;

    public CachedSymbolReference(SymbolReference getter) {
        this.getter = getter;
    }

    @Override
    public SummarySymbol getSymbol(PrismarineSummaryModule summary) {
        if(cachedSymbol == null || !cachedSymbol.isEnqueued() || summary.getParentSummary() == null || summary.getParentSummary() != lastProjectSummary) {
            cachedSymbol = new WeakReference<>(getter.getSymbol(summary));
            lastProjectSummary = summary.parentSummary;
        }
        return cachedSymbol.get();
    }
}
