package com.energyxxer.enxlex.lexical_analysis.inspections;

import com.energyxxer.util.SimpleReadArrayList;
import com.energyxxer.util.SortedList;

import java.util.ArrayList;
import java.util.List;

public class InspectionModule {
    private final SortedList<Inspection> inspections = new SortedList<>(Inspection::getStartIndex);
    private List<Inspection> readOnlyInspections;

    public void addInspection(Inspection inspection) {
        inspections.add(inspection);
        readOnlyInspections = null;
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

    public List<Inspection> getInspections() {
        if(readOnlyInspections == null) {
            readOnlyInspections = new SimpleReadArrayList<>(inspections);
        }
        return readOnlyInspections;
    }
}
