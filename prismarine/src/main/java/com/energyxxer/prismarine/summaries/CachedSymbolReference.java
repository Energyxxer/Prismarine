package com.energyxxer.prismarine.summaries;

import com.energyxxer.util.logger.Debug;

import java.lang.ref.WeakReference;

public class CachedSymbolReference implements SymbolReference {
    private SymbolReference getter;
    private int generation = 0;

    private WeakReference<PrismarineProjectSummary> lastProjectSummary;
    private boolean summaryIsNull = false;

    private WeakReference<SummarySymbol> cachedSymbol;
    private boolean symbolIsNull = false;

    public CachedSymbolReference(SymbolReference getter) {
        this.getter = getter;
    }

    @Override
    public SummarySymbol getSymbol(PrismarineSummaryModule summary) {
        if(
                cachedSymbol == null || (!symbolIsNull && cachedSymbol.get() == null) ||
                summary.getParentSummary() == null ||
                (summaryIsNull && lastProjectSummary.get() == null) ||
                summary.getParentSummary() != lastProjectSummary.get() ||
                summary.getParentSummary().generation != this.generation
        ) {
            if(cachedSymbol != null && !symbolIsNull && cachedSymbol.get() == null) {
                Debug.log("REFRESHING SYMBOL BECAUSE IT WAS GARBAGE COLLECTED, HURRAY!");
            }
            SummarySymbol symbol = getter.getSymbol(summary);
            if(cachedSymbol != null) cachedSymbol.clear();
            cachedSymbol = new WeakReference<>(symbol);
            symbolIsNull = symbol == null;
            if(lastProjectSummary != null) lastProjectSummary.clear();
            lastProjectSummary = new WeakReference<>(summary.parentSummary);
            generation = summary.parentSummary.getGeneration();
        }
        return cachedSymbol.get();
    }
}
