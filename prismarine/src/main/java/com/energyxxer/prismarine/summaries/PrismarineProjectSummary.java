package com.energyxxer.prismarine.summaries;

import com.energyxxer.enxlex.lexical_analysis.summary.ProjectSummary;
import com.energyxxer.enxlex.lexical_analysis.summary.Todo;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class PrismarineProjectSummary implements ProjectSummary {
    protected ArrayList<PrismarineSummaryModule> fileSummaries = new ArrayList<>();
    protected HashMap<File, PrismarineSummaryModule> fileSummaryMap = new HashMap<>();
    protected Set<SummarySymbol> globalSymbols = new LinkedHashSet<>();
    protected Set<Todo> todos = new HashSet<>();

    public void store(File file, PrismarineSummaryModule summaryModule) {
        summaryModule.setParentSummary(this);
        if(file != null) fileSummaryMap.put(file, summaryModule);
        fileSummaries.add(summaryModule);
        this.todos.addAll(summaryModule.getTodos());
        globalSymbols.addAll(summaryModule.getGlobalSymbols());
    }

    public PrismarineSummaryModule getSummaryForLocation(Path loc) {
        for(PrismarineSummaryModule summaryModule : fileSummaries) {
            if(summaryModule.getFileLocation() != null && summaryModule.getFileLocation().equals(loc)) return summaryModule;
        }
        return null;
    }

    public Collection<SummarySymbol> getGlobalSymbols() {
        return globalSymbols;
    }

    @Override
    public String toString() {
        return "Project Summary:\nFiles: " + fileSummaryMap + " files";
    }

    @Override
    public Collection<Todo> getTodos() {
        return todos;
    }

    public PrismarineSummaryModule getSummaryForFile(File file) {
        return fileSummaryMap.get(file);
    }

    public Path getLocationForFile(File file) {
        PrismarineSummaryModule summary = getSummaryForFile(file);
        return summary != null ? summary.getFileLocation() : null;
    }

    public void join(PrismarineProjectSummary other) {
        this.fileSummaries.addAll(other.fileSummaries);
        this.globalSymbols.addAll(other.globalSymbols);
    }

    public Collection<PrismarineSummaryModule> getAllSummaries() {
        return fileSummaries;
    }
}
