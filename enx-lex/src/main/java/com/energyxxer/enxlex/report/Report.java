package com.energyxxer.enxlex.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Report {
    private ArrayList<Notice> errors;
    private ArrayList<Notice> warnings;
    private ArrayList<Notice> info;
    private ArrayList<Notice> debug;

    public Report() {
    }

    public void addNotices(Collection<? extends Notice> notices) {
        notices.forEach(this::addNotice);
    }

    public void addNotice(Notice n) {
        switch(n.getType()) {
            case ERROR: {
                if(errors == null) errors = new ArrayList<>();
                errors.add(n);
                break;
            }
            case WARNING: {
                if(warnings == null) warnings = new ArrayList<>();
                warnings.add(n);
                break;
            }
            case INFO: {
                if(info == null) info = new ArrayList<>();
                info.add(n);
                break;
            }
            case DEBUG: {
                if(debug == null) debug = new ArrayList<>();
                debug.add(n);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupported notice type " + n);
            }
        }
    }

    public List<Notice> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return errors != null;
    }

    public List<Notice> getWarnings() {
        return warnings;
    }

    public boolean hasWarnings() {
        return warnings != null;
    }

    public List<Notice> getInfo() {
        return info;
    }

    public boolean hasInfo() {
        return info != null;
    }

    public List<Notice> getDebug() {
        return debug;
    }

    public boolean hasDebug() {
        return debug != null;
    }

    public HashMap<String, ArrayList<Notice>> group() {
        HashMap<String, ArrayList<Notice>> map = new HashMap<>();

        if(hasWarnings()) {
            for(Notice n : getWarnings()) {
                if(!map.containsKey(n.getGroup())) map.put(n.getGroup(), new ArrayList<>());
                map.get(n.getGroup()).add(n);
            }
        }
        if(hasErrors()) {
            for(Notice n : getErrors()) {
                if(!map.containsKey(n.getGroup())) map.put(n.getGroup(), new ArrayList<>());
                map.get(n.getGroup()).add(n);
            }
        }
        if(hasInfo()) {
            for(Notice n : getInfo()) {
                if(!map.containsKey(n.getGroup())) map.put(n.getGroup(), new ArrayList<>());
                map.get(n.getGroup()).add(n);
            }
        }
        if(hasDebug()) {
            for(Notice n : getDebug()) {
                if(!map.containsKey(n.getGroup())) map.put(n.getGroup(), new ArrayList<>());
                map.get(n.getGroup()).add(n);
            }
        }

        return map;
    }

    public String getTotalsString() {
        int errorCount = hasErrors() ? errors.size() : 0;
        int warningsCount = hasWarnings() ? warnings.size() : 0;

        return "" + ((errorCount == 0) ? "no" : errorCount) + " error" + ((errorCount == 1) ? "" : "s") + " and " + ((warningsCount == 0) ? "no" : warningsCount) + " warning" + ((warningsCount == 1) ? "" : "s");
    }

    public int getTotal() {
        return (info != null ? info.size() : 0) +
                (warnings != null ? warnings.size() : 0) +
                (errors != null ? errors.size() : 0) +
                (debug != null ? debug.size() : 0);
    }

    public List<Notice> getAllNotices() {
        ArrayList<Notice> list = new ArrayList<>();
        if(errors != null) list.addAll(errors);
        if(warnings != null) list.addAll(warnings);
        if(info != null) list.addAll(info);
        if(debug != null) list.addAll(debug);
        return list;
    }

    public void clearNotices() {
        if(errors != null) {
            errors.clear();
            errors = null;
        }
        if(warnings != null) {
            warnings.clear();
            warnings = null;
        }
        if(info != null) {
            info.clear();
            info = null;
        }
        if(debug != null) {
            debug.clear();
            debug = null;
        }
    }
}
