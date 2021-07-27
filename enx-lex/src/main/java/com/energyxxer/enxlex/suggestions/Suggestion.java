package com.energyxxer.enxlex.suggestions;

import java.util.ArrayList;
import java.util.List;

public abstract class Suggestion {
    protected ArrayList<String> tags = null;

    public List<String> getTags() {
        return tags;
    }

    public boolean hasTag(String tag) {
        return tags != null && tags.contains(tag);
    }

    public Suggestion addTag(String newTag) {
        if(newTag != null) {
            if(tags == null) tags = new ArrayList<>(2);
            tags.add(newTag);
        }
        return this;
    }

    public Suggestion addTags(List<String> newTags) {
        if(newTags != null) {
            if(tags == null) tags = new ArrayList<>(2);
            tags.addAll(newTags);
        }
        return this;
    }
}
