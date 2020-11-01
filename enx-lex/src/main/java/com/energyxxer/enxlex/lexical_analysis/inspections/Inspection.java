package com.energyxxer.enxlex.lexical_analysis.inspections;

public interface Inspection {
    String getDescription();
    int getStartIndex();
    int getEndIndex();
}
