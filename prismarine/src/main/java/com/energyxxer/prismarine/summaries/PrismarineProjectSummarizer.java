package com.energyxxer.prismarine.summaries;

import com.energyxxer.commodore.util.io.DirectoryCompoundInput;
import com.energyxxer.enxlex.lexical_analysis.summary.ProjectSummarizer;
import com.energyxxer.enxlex.lexical_analysis.token.ProjectSourceFile;
import com.energyxxer.prismarine.PrismarineSuiteConfiguration;
import com.energyxxer.prismarine.in.ProjectReader;
import com.energyxxer.prismarine.libraries.PrismarineLibrary;
import com.energyxxer.prismarine.walker.DefaultWalkerStops;
import com.energyxxer.prismarine.walker.FileWalker;
import com.energyxxer.prismarine.worker.PrismarineProjectWorker;
import com.energyxxer.prismarine.worker.PrismarineProjectWorkerTask;
import com.energyxxer.util.logger.Debug;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public final class PrismarineProjectSummarizer<T extends PrismarineProjectSummary> implements ProjectSummarizer {

    private final PrismarineSuiteConfiguration suiteConfig;
    public static boolean logTimes = false;

    private PrismarineProjectSummarizer<T> parentSummarizer = null;
    private final File rootFile;
    private Thread thread;

    private final T summary;

    private final ArrayList<Runnable> completionListeners = new ArrayList<>();

    private final PrismarineProjectWorker worker;
    FileWalker<PrismarineProjectSummary> walker;

    private ProjectReader cachedReader;

    private long startTime;
    private long previousStageTime;


    private boolean successful = false;

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
        thread.setUncaughtExceptionHandler((t, ex) -> {
            logException((Exception) ex);
        });
        //report = new Report();
        thread.start();
    }

    private void logTime(String description) {
        long now = System.currentTimeMillis();
        if(logTimes) Debug.log("Prismarine-Summarizer[" + rootFile.getName() + "] | " + description + " | This Segment Time: " + (now - previousStageTime) + " ms | Total Elapsed Time: " + (now - startTime) + " ms");
        previousStageTime = now;
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
        startTime = System.currentTimeMillis();
        previousStageTime = startTime;

        suiteConfig.setupWorkerForSummary(worker);

        logTime("Worker Setup");

        try {
            worker.work();
        } catch(Exception x) {
            logException(x);
            return;
        }

        logTime("Workers");

        for(PrismarineProjectWorker subWorker : worker.output.getDependencies()) {
            PrismarineProjectSummarizer subSummarizer = suiteConfig.createSummarizer(subWorker);
            subSummarizer.setParentSummarizer(this);
            subSummarizer.setCachedReader(cachedReader);
            try {
                subSummarizer.runSummary();
            } catch(Exception ex) {
                logException(ex);
                return;
            }
            this.setCachedReader(subSummarizer.getProjectReader());
            this.summary.join(subSummarizer.summary);
        }

        logTime("Sub-Summaries");

        PrismarineLibrary standardLibrary = suiteConfig.getStandardLibrary();
        if(standardLibrary != null) {
            standardLibrary.populateSummary(summary);
        }

        logTime("Populating with Standard Library");

        suiteConfig.runSummaryPreFileTree(this);

        logTime("Suite Pre-Walkers");
        final Path finalRootPath = rootFile.toPath();
        walker = new FileWalker<>(
                new DirectoryCompoundInput(rootFile),
                p -> new ProjectSourceFile(finalRootPath, p),
                worker,
                summary
        );
        walker.addStops(DefaultWalkerStops.createSummaryWalkerStops(suiteConfig));
        suiteConfig.setupWalkerForSummary(walker);

        logTime("Walker Setup");

        if(cachedReader != null) {
            walker.getReader().populateWithCachedReader(cachedReader);
            cachedReader = null; // THIS IS A LOAD-BEARING STATEMENT HOLY SH*T
            //Remove this, and every project summary ever run becomes a huge linked list of stuff that won't be GC'd.
        }
        try {
            walker.walk();
        } catch (IOException x) {
            logException(x);
            return;
        }

        logTime("Walkers");

        suiteConfig.runSummaryPostFileTree(this);

        logTime("Suite Post-Walkers");

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

        logTime("File-Aware Processors");
        successful = true;

        for(java.lang.Runnable r : completionListeners) {
            r.run();
        }

        logTime("Completion Listeners");
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

    public FileWalker<PrismarineProjectSummary> getWalker() {
        return walker;
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
    public boolean isSuccessful() {
        return successful;
    }
}
