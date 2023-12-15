package com.energyxxer.enxlex.lexical_analysis.summary;

import com.energyxxer.enxlex.lexical_analysis.Lexer;

import java.util.function.Function;

public abstract class SummaryModule {
    public void onStart(Lexer lexer) {}
    public void onEnd(Lexer lexer) {}
    public abstract void updateIndices(Function<Integer, Integer> h);
    public abstract ProjectSummary getParentSummary();
}
