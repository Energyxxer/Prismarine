package com.energyxxer.prismarine.summaries;

import com.energyxxer.prismarine.symbols.SymbolVisibility;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.Function;

public interface SummaryElement {
    String getName();
    int getStartIndex();
    int getEndIndex();

    void putElement(SummaryElement element);

    PrismarineSummaryModule getParentFileSummary();

    ArrayList<SummarySymbol> collectGlobalSymbols(ArrayList<SummarySymbol> list);

    void updateIndices(Function<Integer, Integer> h);

    default ArrayList<SummarySymbol> collectSymbolsVisibleAt(int index, ArrayList<SummarySymbol> list, Path fromPath, PrismarineSummaryModule summary) {
        return list;
    }

    SymbolVisibility getVisibility();
}
