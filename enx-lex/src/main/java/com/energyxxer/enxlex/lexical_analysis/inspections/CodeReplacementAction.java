package com.energyxxer.enxlex.lexical_analysis.inspections;

public class CodeReplacementAction implements CodeAction {
    private String description;

    private String replacementText;
    private int replacementStartIndex;
    private int replacementEndIndex;

    public CodeReplacementAction() {

    }

    public CodeReplacementAction(String description) {
        this.description = description;
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

    public CodeReplacementAction setReplacementText(String replacementText) {
        this.replacementText = replacementText;
        return this;
    }

    public CodeReplacementAction setReplacementStartIndex(int replacementStartIndex) {
        this.replacementStartIndex = replacementStartIndex;
        return this;
    }

    public CodeReplacementAction setReplacementEndIndex(int replacementEndIndex) {
        this.replacementEndIndex = replacementEndIndex;
        return this;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
