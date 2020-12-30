package com.energyxxer.enxlex.lexical_analysis.inspections;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.util.StringBounds;

import java.util.ArrayList;

public class Inspection {
    private String description;

    private int startIndex;
    private int endIndex;

    private InspectionSeverity severity;

    private ArrayList<CodeAction> actions = new ArrayList<>();

    public Inspection(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }


    public Inspection setDescription(String description) {
        this.description = description;
        return this;
    }

    public Inspection setStartIndex(int startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    public Inspection setEndIndex(int endIndex) {
        this.endIndex = endIndex;
        return this;
    }

    public Inspection setBounds(int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        return this;
    }

    public Inspection setBounds(TokenPattern<?> pattern) {
        StringBounds bounds = pattern.getStringBounds();
        return setBounds(bounds.start.index, bounds.end.index);
    }

    public ArrayList<CodeAction> getActions() {
        return actions;
    }

    public Inspection addAction(CodeAction action) {
        actions.add(action);
        return this;
    }

    public InspectionSeverity getSeverity() {
        return severity;
    }

    public Inspection setSeverity(InspectionSeverity severity) {
        this.severity = severity;
        return this;
    }
}
