package com.energyxxer.prismarine.summaries;

import com.energyxxer.enxlex.lexical_analysis.summary.ProjectSummary;
import com.energyxxer.enxlex.lexical_analysis.summary.Todo;
import com.energyxxer.util.SimpleReadArrayList;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class PrismarineProjectSummary implements ProjectSummary {
    protected ArrayList<PrismarineSummaryModule> fileSummaries = new SimpleReadArrayList<>();
    private ArrayList<PrismarineProjectSummary> subSummaries = new ArrayList<>(); //here solely to keep references to dependencies' summaries alive
    protected HashMap<File, PrismarineSummaryModule> fileSummaryMap = new HashMap<>();
    protected Set<SummarySymbol> globalSymbols = new LinkedHashSet<>();
    protected Set<Todo> todos = new HashSet<>();

    protected int generation = 0;

    public void store(File file, PrismarineSummaryModule summaryModule) {
        incrementGeneration();
        summaryModule.setParentSummary(this);
        if(file != null) {
            PrismarineSummaryModule existingModule = fileSummaryMap.get(file);
            fileSummaryMap.put(file, summaryModule);
            if(existingModule != null) {
                fileSummaries.remove(existingModule);
            }
        }
        fileSummaries.add(summaryModule);
        this.todos.addAll(summaryModule.getTodos());
        globalSymbols.addAll(summaryModule.getGlobalSymbols());
    }

    public void storeTemporarily(File file, PrismarineSummaryModule summaryModule) {
        incrementGeneration();
        summaryModule.setParentSummary(this);
        if(file != null) {
            PrismarineSummaryModule existingModule = fileSummaryMap.get(file);
            fileSummaryMap.put(file, summaryModule);
            if(existingModule != null) {
                fileSummaries.remove(existingModule);
            }
        }
        fileSummaries.add(summaryModule);

        //No global symbol or to-do merging; meant for incorporating editor changes without saving.
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

    public void addSubSummary(PrismarineProjectSummary other) {
        this.fileSummaries.addAll(other.fileSummaries);
        this.globalSymbols.addAll(other.globalSymbols);
        this.subSummaries.add(other);
    }

    public Collection<PrismarineSummaryModule> getAllSummaries() {
        return fileSummaries;
    }

    public int getGeneration() {
        return generation;
    }

    public void incrementGeneration() {
        generation++;
    }
}
