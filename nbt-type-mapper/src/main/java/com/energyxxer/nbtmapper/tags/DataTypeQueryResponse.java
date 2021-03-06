package com.energyxxer.nbtmapper.tags;

import com.energyxxer.enxlex.report.Notice;

import java.util.ArrayList;
import java.util.Collection;

public class DataTypeQueryResponse {
    private final ArrayList<DataType> possibleTypes = new ArrayList<>();
    private final ArrayList<Notice> notices = new ArrayList<>();

    public void addLikelyType(DataType type) {
        possibleTypes.remove(type);
        possibleTypes.add(0, type); //add it to the start, even if it already exists
        //if it was previously unlikely, this will make it likely
    }

    public void addUnlikelyType(DataType type) {
        if(!possibleTypes.contains(type)) possibleTypes.add(type); //add it to the end, if it doesn't already exist
        //if it was previously likely, this will not make it unlikely
    }

    public Collection<DataType> getPossibleTypes() {
        return possibleTypes;
    }

    public boolean isEmpty() {
        return possibleTypes.isEmpty();
    }

    public void addNotice(Notice notice) {
        notices.add(notice);
    }

    public ArrayList<Notice> getAllNotices() {
        return notices;
    }
}
