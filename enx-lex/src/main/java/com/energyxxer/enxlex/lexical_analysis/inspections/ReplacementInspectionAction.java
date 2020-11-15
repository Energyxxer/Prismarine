package com.energyxxer.enxlex.lexical_analysis.inspections;

public class ReplacementInspectionAction implements InspectionAction {
    private String replacementText;

    private int replacementStartIndex;
    private int replacementEndIndex;

    public String getReplacementText() {
        return replacementText;
    }

    public int getReplacementStartIndex() {
        return replacementStartIndex;
    }

    public int getReplacementEndIndex() {
        return replacementEndIndex;
    }

    public ReplacementInspectionAction setReplacementText(String replacementText) {
        this.replacementText = replacementText;
        return this;
    }

    public ReplacementInspectionAction setReplacementStartIndex(int replacementStartIndex) {
        this.replacementStartIndex = replacementStartIndex;
        return this;
    }

    public ReplacementInspectionAction setReplacementEndIndex(int replacementEndIndex) {
        this.replacementEndIndex = replacementEndIndex;
        return this;
    }
}
