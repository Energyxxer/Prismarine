package com.energyxxer.prismarine.summaries;

import com.energyxxer.enxlex.suggestions.Suggestion;

public class SymbolSuggestion extends Suggestion {
    private final SummarySymbol symbol;

    public SymbolSuggestion(SummarySymbol symbol) {
        this.symbol = symbol;
    }

    public SummarySymbol getSymbol() {
        return symbol;
    }
}
