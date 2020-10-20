package com.energyxxer.enxlex.suggestions;

public class ParameterNameSuggestion extends Suggestion {
    private String parameterName;

    public ParameterNameSuggestion(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterName() {
        return parameterName;
    }
}
