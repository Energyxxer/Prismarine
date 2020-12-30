package com.energyxxer.enxlex.lexical_analysis.inspections;

import java.util.ArrayList;

public class CodeChainAction implements CodeAction {
    private String description;
    private ArrayList<CodeAction> actions = new ArrayList<>();

    public CodeChainAction(CodeAction... actions) {
        this(null, actions);
    }

    public CodeChainAction(String description, CodeAction... actions) {
        this.description = description;
        for(CodeAction action : actions) {
            //noinspection UseBulkOperation
            this.actions.add(action);
        }
    }

    public CodeChainAction() {
        this((String) null);
    }

    public CodeChainAction(String description) {
        this.description = description;
    }

    public CodeChainAction addAction(CodeAction action) {
        actions.add(action);
        return this;
    }

    @Override
    public String getDescription() {
        if(description != null) return description;
        for(CodeAction action : actions) {
            String actionDescription = action.getDescription();
            if(actionDescription != null) {
                return actionDescription;
            }
        }
        return null;
    }

    public ArrayList<CodeAction> getActions() {
        return actions;
    }
}
