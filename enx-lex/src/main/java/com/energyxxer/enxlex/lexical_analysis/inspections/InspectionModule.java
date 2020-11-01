package com.energyxxer.enxlex.lexical_analysis.inspections;

import com.energyxxer.util.SortedList;

import java.util.ArrayList;
import java.util.List;

public class InspectionModule {
    private SortedList<Inspection> inspections = new SortedList<>(Inspection::getStartIndex);

    public void addInspection(Inspection inspection) {
        inspections.add(inspection);
    }

    public List<Inspection> collectInspectionsForIndex(int index) {
        ArrayList<Inspection> collected = new ArrayList<>();
        for(Inspection inspection : inspections) {
            if(inspection.getStartIndex() > index) break;
            if(inspection.getStartIndex() <= index && index <= inspection.getEndIndex()) {
                collected.add(inspection);
            }
        }
        return collected;
    }
}
