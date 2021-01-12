package com.energyxxer.enxlex.lexical_analysis.summary;

public interface ProjectSummarizer {
    void addCompletionListener(java.lang.Runnable r);
    ProjectSummary getSummary();
    void start();
}
