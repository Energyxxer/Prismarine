package com.energyxxer.enxlex.suggestions;

public class PairSuggestion extends Suggestion {
    private final int startIndex;
    private final int endIndex;
    private final String openSymbol;
    private final String closeSymbol;

    public PairSuggestion(int startIndex, int endIndex, String openSymbol, String closeSymbol) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.openSymbol = openSymbol;
        this.closeSymbol = closeSymbol;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public String getOpenSymbol() {
        return openSymbol;
    }

    public String getCloseSymbol() {
        return closeSymbol;
    }
}
