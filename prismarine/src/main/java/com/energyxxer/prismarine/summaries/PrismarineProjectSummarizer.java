package com.energyxxer.prismarine.summaries;

import com.energyxxer.commodore.util.io.DirectoryCompoundInput;
import com.energyxxer.enxlex.lexical_analysis.summary.ProjectSummarizer;
import com.energyxxer.enxlex.lexical_analysis.token.SourceFile;
import com.energyxxer.enxlex.pattern_matching.ParsingSignature;
import com.energyxxer.prismarine.PrismarineSuiteConfiguration;
import com.energyxxer.prismarine.in.ProjectReader;
import com.energyxxer.prismarine.libraries.PrismarineLibrary;
import com.energyxxer.prismarine.walker.DefaultWalkerStops;
import com.energyxxer.prismarine.walker.FileWalker;
import com.energyxxer.prismarine.worker.PrismarineProjectWorker;
import com.energyxxer.prismarine.worker.PrismarineProjectWorkerTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public final class PrismarineProjectSummarizer<T extends PrismarineProjectSummary> implements ProjectSummarizer {

    private final PrismarineSuiteConfiguration suiteConfig;

    private PrismarineProjectSummarizer<T> parentSummarizer = null;
    private File rootFile;
    private Thread thread;

    private T summary;

    private ArrayList<Runnable> completionListeners = new ArrayList<>();

    private PrismarineProjectWorker worker;
    FileWalker<PrismarineProjectSummary> walker;

    private ProjectReader cachedReader;

    public PrismarineProjectSummarizer(PrismarineSuiteConfiguration suiteConfig, File rootFile) {
        this.suiteConfig = suiteConfig;
        this.rootFile = rootFile;

        this.worker = new PrismarineProjectWorker(suiteConfig, rootFile);

        this.summary = (T) suiteConfig.createSummary();
    }

    public PrismarineProjectSummarizer(PrismarineProjectWorker worker) {
        this.suiteConfig = worker.suiteConfig;
        this.rootFile = worker.rootDir;

        this.worker = worker;

        this.summary = (T) suiteConfig.createSummary();
    }

    public void start() {
        this.thread = new Thread(this::runSummary,"Prismarine-Summarizer[" + rootFile.getName() + "]");
        //report = new Report();
        thread.start();
    }

    private void runSummary() {

        if(parentSummarizer != null) {
            PrismarineProjectSummarizer<T> next = parentSummarizer;
            while(next != null) {
                if(next.rootFile.equals(this.rootFile)) {
                    return;
                }
                next = next.parentSummarizer;
            }
        }

        suiteConfig.setupWorkerForSummary(worker);

        try {
            worker.work();
        } catch(Exception x) {
            logException(x);
            return;
        }

        for(PrismarineProjectWorker subWorker : worker.output.getDependencies()) {
            PrismarineProjectSummarizer subSummarizer = suiteConfig.createSummarizer(subWorker);
            subSummarizer.setParentSummarizer(this);
            subSummarizer.setSourceCache(this.getSourceCache());
            try {
                subSummarizer.runSummary();
            } catch(Exception ex) {
                logException(ex);
                return;
            }
            this.setSourceCache(subSummarizer.getSourceCache());
            this.summary.join(subSummarizer.summary);
        }

        PrismarineLibrary standardLibrary = suiteConfig.getStandardLibrary();
        if(standardLibrary != null) {
            standardLibrary.populateSummary(summary);
        }

        suiteConfig.runSummaryPreFileTree(this);

        walker = new FileWalker<>(
                new DirectoryCompoundInput(rootFile),
                p -> new SourceFile(rootFile.toPath().resolve(p).toFile()),
                worker,
                summary
        );
        walker.addStops(DefaultWalkerStops.createSummaryWalkerStops(suiteConfig));
        suiteConfig.setupWalkerForSummary(walker);

        if(cachedReader != null) walker.getReader().populateWithCachedReader(cachedReader);
        try {
            walker.walk();
        } catch (IOException x) {
            logException(x);
            return;
        }

        suiteConfig.runSummaryPostFileTree(this);

        if(parentSummarizer == null) {
            int pass = 0;
            while(true) {
                boolean anyRan = false;
                for(PrismarineSummaryModule fileSummary : summary.fileSummaries) {
                    if(fileSummary.runFileAwareProcessors(pass)) {
                        anyRan = true;
                    }
                }
                pass++;
                if(!anyRan) break;
            }
        }

        for(java.lang.Runnable r : completionListeners) {
            r.run();
        }
    }

    public PrismarineProjectSummarizer<T> getParentSummarizer() {
        return parentSummarizer;
    }

    public void setParentSummarizer(PrismarineProjectSummarizer<T> parentSummarizer) {
        this.parentSummarizer = parentSummarizer;
    }

    public void addCompletionListener(java.lang.Runnable r) {
        completionListeners.add(r);
    }

    public void removeCompletionListener(java.lang.Runnable r) {
        completionListeners.remove(r);
    }

    private void logException(Exception x) {
        x.printStackTrace();
        for(java.lang.Runnable r : completionListeners) {
            r.run();
        }
    }

    public T getSummary() {
        return summary;
    }

    public PrismarineProjectWorker getWorker() {
        return worker;
    }

    public <C> C get(PrismarineProjectWorkerTask<C> task) {
        return worker.output.get(task);
    }

    public ProjectReader getProjectReader() {
        return walker.getReader();
    }

    public void setCachedReader(ProjectReader cachedReader) {
        this.cachedReader = cachedReader;
    }


    @Override
    public void setSourceCache(HashMap<String, ParsingSignature> hashMap) {

    }

    private static final HashMap<String, ParsingSignature> TEMP_SOURCE_CACHE = new HashMap<>();

    @Override
    public HashMap<String, ParsingSignature> getSourceCache() {
        return TEMP_SOURCE_CACHE;
    }
}
