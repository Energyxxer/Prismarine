package com.energyxxer.enxlex.lexical_analysis.inspections;

public class SuggestionInspection implements Inspection {
    private String description;

    private int startIndex;
    private int endIndex;

    private String replacementText;

    private int replacementStartIndex;
    private int replacementEndIndex;

    public SuggestionInspection(String description) {
        this.description = description;
    }


    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public int getEndIndex() {
        return endIndex;
    }

    public String getReplacementText() {
        return replacementText;
    }

    public int getReplacementStartIndex() {
        return replacementStartIndex;
    }

    public int getReplacementEndIndex() {
        return replacementEndIndex;
    }


    public SuggestionInspection setDescription(String description) {
        this.description = description;
        return this;
    }

    public SuggestionInspection setStartIndex(int startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    public SuggestionInspection setEndIndex(int endIndex) {
        this.endIndex = endIndex;
        return this;
    }

    public SuggestionInspection setReplacementText(String replacementText) {
        this.replacementText = replacementText;
        return this;
    }

    public SuggestionInspection setReplacementStartIndex(int replacementStartIndex) {
        this.replacementStartIndex = replacementStartIndex;
        return this;
    }

    public SuggestionInspection setReplacementEndIndex(int replacementEndIndex) {
        this.replacementEndIndex = replacementEndIndex;
        return this;
    }
}
