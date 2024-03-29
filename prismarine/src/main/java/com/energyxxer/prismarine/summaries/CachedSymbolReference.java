package com.energyxxer.prismarine.summaries;

import java.lang.ref.WeakReference;
import java.nio.file.Path;

public class CachedSymbolReference implements SymbolReference {
    private SymbolReference getter;
    private int generation = 0;

    private WeakReference<PrismarineProjectSummary> lastProjectSummary;
    private boolean summaryIsNull = false;

    private WeakReference<SummarySymbol> cachedSymbol;
    private boolean symbolIsNull = false;

    private Path originalModulePath;

    public CachedSymbolReference(SymbolReference getter) {
        this.getter = getter;
    }

    public CachedSymbolReference(PrismarineSummaryModule originalModule, SymbolReference getter) {
        if(originalModule != null) this.originalModulePath = originalModule.getFileLocation();
        this.getter = getter;
    }

    @Override
    public SummarySymbol getSymbol(PrismarineSummaryModule summary) {
        if(originalModulePath != null && !originalModulePath.equals(summary.getFileLocation()) && summary.getParentSummary() != null) {
            PrismarineSummaryModule correctedSummary = summary.getParentSummary().getSummaryForLocation(originalModulePath);
            if(correctedSummary != null) summary = correctedSummary;
        }
        if(
                cachedSymbol == null || (!symbolIsNull && cachedSymbol.get() == null) ||
                summary.getParentSummary() == null ||
                (summaryIsNull && lastProjectSummary.get() == null) ||
                summary.getParentSummary() != lastProjectSummary.get() ||
                summary.getParentSummary().generation != this.generation
        ) {
//            if(cachedSymbol != null && !symbolIsNull && cachedSymbol.get() == null) {
//                Debug.log("REFRESHING SYMBOL BECAUSE IT WAS GARBAGE COLLECTED, HURRAY!");
//            }
            SummarySymbol symbol = getter.getSymbol(summary);
            if(cachedSymbol != null) cachedSymbol.clear();
            cachedSymbol = new WeakReference<>(symbol);
            symbolIsNull = symbol == null;
            if(lastProjectSummary != null) lastProjectSummary.clear();
            lastProjectSummary = new WeakReference<>(summary.getParentSummary());
            summaryIsNull = summary.getParentSummary() == null;
            generation = summaryIsNull ? 0 : summary.getParentSummary().getGeneration();
        }
        return cachedSymbol.get();
    }
}
