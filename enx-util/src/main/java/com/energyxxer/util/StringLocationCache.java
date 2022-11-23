package com.energyxxer.util;

public class StringLocationCache {
    //index: line number (0-indexed)
    //value: index within text of first character in the line (0-indexed)
    private final PrimitiveIntList lineLocations = new PrimitiveIntList();
    //0-indexed
    private int lastKnownLine = 0;

    public String text;

    public StringLocationCache() {
        clear();
    }

    public StringLocationCache(String text) {
        setText(text);
    }

    public void textChanged(String newText, int changeIndex) {
        //changeIndex: 0-indexed
        this.text = newText;
        if(changeIndex > lineLocations.get(lastKnownLine)) return;
        int line = getLocationForOffset(changeIndex).line-1;
        if(lineLocations.size() >= line) {
            lineLocations.setSize(line);
        }
        if(lineLocations.isEmpty()) {
            lineLocations.add(0);
        }
        lastKnownLine = lineLocations.size()-1;
    }

    public void setText(String text) {
        this.text = text;
        clear();
    }

    public void clear() {
        lineLocations.setSize(0);
        lineLocations.add(0);
        lastKnownLine = 0;
    }

    public StringLocation getLocationForOffset(int index) {
        return getLocationForOffset(index, new StringLocation(0, 0, 0));
    }

    public StringLocation getLocationForOffset(int index, StringLocation location) {
        int line = 0;
        int lineStart = 0;

        int firstLine = 0; //inclusive
        int lastLine = lineLocations.size()-1; //inclusive

        while(lastLine >= firstLine) {
            if(lastLine <= firstLine) {
                int loc = lineLocations.get(lastLine);
                line = lastLine;
                lineStart = loc;
                break;
            } else {
                int pivot = (lastLine + firstLine) / 2;
                int loc = lineLocations.get(pivot);
                if(loc == index) {
                    return location.setLocation(index, pivot+1, 1);
                } else if(index > loc) {
                    firstLine = pivot;
                    if(lastLine - firstLine <= 1) {
                        lastLine = firstLine;
                    }
                } else {
                    lastLine = pivot-1;
                }
            }
        }

        if(text == null) return location.setLocation(0, 1, 1);

        int column = 0;

        for(int i = lineStart; i < text.length(); i++) {
            if(i == index) {
                return location.setLocation(index, line + 1, column + 1);
            }
            if(text.charAt(i) == '\n') {
                line++;
                column = 0;
                if(line == lineLocations.size()) {
                    lineLocations.add(i + 1);
                }
                lastKnownLine = Math.max(lastKnownLine, line);
            } else {
                column++;
            }
        }
        return location.setLocation(index, line + 1, column + 1);
    }

    public void prepopulate() {
        getLocationForOffset(text.length());
    }
}
