package com.energyxxer.prismarine.summaries;

import com.energyxxer.enxlex.lexical_analysis.summary.SummaryModule;
import com.energyxxer.prismarine.symbols.SymbolVisibility;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.Function;

public interface SummaryElement {
    String getName();
    int getStartIndex();
    int getEndIndex();

    void putElement(SummaryElement element);

    SummaryModule getParentFileSummary();

    void collectGlobalSymbols(ArrayList<SummarySymbol> list);

    void updateIndices(Function<Integer, Integer> h);

    default void collectSymbolsVisibleAt(int index, ArrayList<SummarySymbol> list, Path fromPath) {

    }

    SymbolVisibility getVisibility();
}
