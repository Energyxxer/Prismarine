package com.energyxxer.prismarine.worker;

import com.energyxxer.enxlex.report.Notice;
import com.energyxxer.enxlex.report.NoticeType;
import com.energyxxer.enxlex.report.Report;
import com.energyxxer.prismarine.PrismarineCompiler;
import com.energyxxer.prismarine.PrismarineSuiteConfiguration;
import com.energyxxer.prismarine.summaries.PrismarineProjectSummarizer;

import java.io.File;
import java.util.*;

public class PrismarineProjectWorker {
    public final File rootDir;

    public final PrismarineSuiteConfiguration suiteConfig;
    public final Setup setup;
    public final Output output;

    public Report report;

    private DependencyInfo dependencyInfo;

    private boolean hasWorked = false;

    public PrismarineProjectWorker(PrismarineSuiteConfiguration suiteConfig, File rootDir) {
        this.suiteConfig = suiteConfig;
        this.rootDir = rootDir;
        this.setup = new Setup();
        this.output = new Output();
    }

    public void work() throws Exception {
        if(hasWorked) return;

        setup.resolveImplications(this);

        if(setup.useReport) {
            report = new Report();
        }

        for(PrismarineProjectWorkerTask task : setup.tasks) {
            //compiler.setProgress(task.getProgressMessage());
            try {
                if(!output.hasWorked(task)) {
                    Object taskResult = task.perform(this);
                    output.put(task, taskResult);
                }
            } catch(Exception x) {
                logException(x, "Error while " + task.getProgressMessage() + ": ");
            }
        }

        hasWorked = true;
    }

    private File newFileObject(String path) {
        return PrismarineCompiler.newFileObject(path, rootDir);
    }

    private void logException(Exception x, String prefix) throws Exception {
        if (setup.useReport) {
            this.report.addNotice(new Notice(NoticeType.ERROR, prefix + x.toString() + " ; See console for details"));
            x.printStackTrace();
        } else {
            throw x;
        }
    }

    public PrismarineCompiler createCompiler() {
        PrismarineCompiler compiler = new PrismarineCompiler(this);
        if(dependencyInfo != null) {
            compiler.setDependencyMode(dependencyInfo.mode);
        }
        return compiler;
    }

    public PrismarineProjectSummarizer createSummarizer() {
        return suiteConfig.createSummarizer(this);
    }

    public void reset() {
        hasWorked = false;
    }

    public class Setup {
        ArrayList<PrismarineProjectWorkerTask> tasks = new ArrayList<>();
        public boolean useReport;

        void resolveImplications(PrismarineProjectWorker worker) {
            boolean fullyResolved = false;
            while(!fullyResolved) {
                fullyResolved = true;
                for(int i = tasks.size()-1; i >= 0; i--) {
                    PrismarineProjectWorkerTask task = tasks.get(i);
                    PrismarineProjectWorkerTask[] implications = task.getImplications();
                    if(implications != null && implications.length > 0) {
                        for(PrismarineProjectWorkerTask implication : implications) {
                            if(worker.output.hasWorked(implication)) continue; //output already provided
                            int alreadyPresentIndex = tasks.indexOf(implication);
                            if(alreadyPresentIndex == -1 || alreadyPresentIndex > i) {
                                tasks.remove(implication);
                                tasks.add(i, implication);
                                fullyResolved = false;
                            }
                        }
                    }
                }
            }
        }

        public void copyFrom(Setup setup) {
            this.tasks.clear();
            this.tasks.addAll(setup.tasks);
            this.useReport = setup.useReport;
        }

        public void addTasks(PrismarineProjectWorkerTask... tasks) {
            Collections.addAll(this.tasks, tasks);
        }

        public void addTasks(Collection<PrismarineProjectWorkerTask> tasks) {
            this.tasks.addAll(tasks);
        }
    }

    public class Output {
        private HashMap<PrismarineProjectWorkerTask, Object> taskOutput = new HashMap<>();
        private final List<PrismarineProjectWorker> dependencies = new ArrayList<>();

        public void put(PrismarineProjectWorkerTask task, Object result) {
            taskOutput.put(task, result);
        }

        public <T> T get(PrismarineProjectWorkerTask<T> task) {
            return (T) taskOutput.get(task);
        }

        public boolean hasWorked(PrismarineProjectWorkerTask task) {
            return taskOutput.containsKey(task);
        }

        public void addDependency(PrismarineProjectWorker subWorker) {
            dependencies.add(subWorker);
        }

        public List<PrismarineProjectWorker> getDependencies() {
            return dependencies;
        }
    }

    public void setDependencyInfo(DependencyInfo dependencyInfo) {
        this.dependencyInfo = dependencyInfo;
    }

    public DependencyInfo getDependencyInfo() {
        return dependencyInfo;
    }

    public static class DependencyInfo {
        public boolean doExport;
        public PrismarineCompiler.Dependency.Mode mode;
        public PrismarineProjectWorker parentWorker;
    }
}
