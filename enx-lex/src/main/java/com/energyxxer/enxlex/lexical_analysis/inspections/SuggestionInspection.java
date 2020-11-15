package com.energyxxer.enxlex.lexical_analysis.inspections;

import java.util.ArrayList;

public class SuggestionInspection implements Inspection {
    private String description;

    private int startIndex;
    private int endIndex;

    private ArrayList<InspectionAction> actions = new ArrayList<>();

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

    public ArrayList<InspectionAction> getActions() {
        return actions;
    }

    public SuggestionInspection addAction(InspectionAction action) {
        actions.add(action);
        return this;
    }
}
