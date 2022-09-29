package com.energyxxer.enxlex.suggestions;

import com.energyxxer.util.PrimitiveIntList;
import com.energyxxer.util.logger.Debug;

public class PairCompletionRanges {
    private final char openingSymbol;
    private final char closingSymbol;
    private final PrimitiveIntList indices = new PrimitiveIntList(); //pairs of indices: start and end
    public static int memorySavings = 0;

    public PairCompletionRanges(char openingSymbol, char closingSymbol) {
        this.openingSymbol = openingSymbol;
        this.closingSymbol = closingSymbol;
    }

    private int findListIndex(int start) {
        if(indices.isEmpty()) return 0;
        if(start > indices.get(indices.size()-2)) return indices.size();
        if(start <= indices.get(0)) return 0;
        for(int i = indices.size()-2; i >= 0; i -= 2) {
            int currentValue = indices.get(i);
            if(currentValue == start) return i;
            else if(currentValue < start) return i + 2;
        }
        return 0;
    }

    public void put(int start, int end) {
        memorySavings++;
        int insertIndex = findListIndex(start);
        if(insertIndex >= indices.size()) {
            if(insertIndex > 0 && start < indices.get(insertIndex-2)) {
                Debug.log("uh oh bad");
            }
            indices.add(start);
            indices.add(end);
            return;
        }
        if(indices.get(insertIndex) == start) {
            if(indices.get(insertIndex+1) < end) {
                indices.set(insertIndex+1, end);
            }
        } else {
            if(insertIndex > 0 && start < indices.get(insertIndex-2)) {
                Debug.log("uh oh bad 2");
            }
            else if(start > indices.get(insertIndex)) {
                Debug.log("uh oh bad 2");
            }
            indices.add(insertIndex, end);
            indices.add(insertIndex, start);
        }
    }

    public boolean isInRange(int index) {
        int listIndex = findListIndex(index);
        if(listIndex >= indices.size()) return false;
        return indices.get(listIndex) <= index && index <= indices.get(listIndex+1);
    }

    public char getOpeningSymbol() {
        return openingSymbol;
    }

    public char getClosingSymbol() {
        return closingSymbol;
    }
}
