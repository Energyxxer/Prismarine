package com.energyxxer.prismarine;

import com.energyxxer.commodore.util.io.DirectoryCompoundInput;
import com.energyxxer.enxlex.report.Notice;
import com.energyxxer.enxlex.report.NoticeType;
import com.energyxxer.enxlex.report.Report;
import com.energyxxer.enxlex.report.Reported;
import com.energyxxer.prismarine.in.ProjectReader;
import com.energyxxer.prismarine.libraries.PrismarineLibrary;
import com.energyxxer.prismarine.state.CallStack;
import com.energyxxer.prismarine.state.TryStack;
import com.energyxxer.prismarine.symbols.contexts.GlobalSymbolContext;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import com.energyxxer.prismarine.typesystem.PrismarineTypeSystem;
import com.energyxxer.prismarine.walker.DefaultWalkerStops;
import com.energyxxer.prismarine.walker.FileWalker;
import com.energyxxer.prismarine.worker.PrismarineProjectWorker;
import com.energyxxer.prismarine.worker.PrismarineProjectWorkerTask;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.util.processes.AbstractProcess;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public final class PrismarineCompiler extends AbstractProcess implements Reported {

    private Path rootPath;

    private PrismarineSuiteConfiguration suiteConfig;

    private PrismarineProjectWorker worker;
    private FileWalker<PrismarineCompiler> walker;
    private ProjectReader cachedReader;

    //Caller Feedback
    private Report report = null;
    private Dependency.Mode dependencyMode;

    //File Structure Tracking
    private HashMap<PrismarineLanguageUnitConfiguration, ArrayList<PrismarineLanguageUnit>> unitsList = new HashMap<>();
    private HashMap<Path, PrismarineLanguageUnit> pathToUnitMap = new HashMap<>();

    //Stacks
    private GlobalSymbolContext global = new GlobalSymbolContext(this);
    private CallStack callStack = new CallStack();
    private TryStack tryStack = new TryStack();

    //useful stuff
    private PrismarineTypeSystem typeSystem; //set from prismarine suite config

    public PrismarineCompiler(PrismarineSuiteConfiguration suiteConfig, File rootDir) {
        super("Prismarine-Compiler[" + rootDir.getName() + "]");
        this.suiteConfig = suiteConfig;
        this.rootPath = rootDir.toPath();
        initializeThread(this::runCompilation);
        this.thread.setUncaughtExceptionHandler((th, ex) -> {
            logException(ex);
        });
        report = new Report();

        worker = new PrismarineProjectWorker(suiteConfig, rootDir);
    }

    public PrismarineCompiler(PrismarineProjectWorker worker) {
        super("Prismarine-Compiler[" + worker.rootDir.getName() + "]");
        this.suiteConfig = worker.suiteConfig;
        this.rootPath = worker.rootDir.toPath();
        initializeThread(this::runCompilation);
        this.thread.setUncaughtExceptionHandler((th, ex) -> {
            logException(ex);
        });
        report = new Report();

        this.worker = worker;
    }

    public PrismarineProjectWorker getWorker() {
        return worker;
    }

    public PrismarineTypeSystem getTypeSystem() {
        return typeSystem;
    }

    private void runCompilation() {
        if(parentCompiler != null) {
            PrismarineCompiler next = parentCompiler;
            while(next != null) {
                if(next.rootPath.equals(this.rootPath)) {
                    Debug.log("Stopping circular dependencies");
                    finalizeProcess(false);
                    return;
                }
                next = next.parentCompiler;
            }
        }

        invokeStart();

        this.setProgress("Initializing type system");

        suiteConfig.onCompilationStarted(this);
        typeSystem = suiteConfig.createTypeSystem(this, global);

        suiteConfig.setupWorkerForCompilation(worker);

        try {
            worker.setup.useReport = true;
            worker.work();
        } catch(Exception x) {
            //this shouldn't occur since useReport is set to true
            logException(x, "Error while doing a bunch of stuff: ");
            return;
        }

        suiteConfig.onAllCompilationWorkerTasksFinished(worker, this);

        report.addNotices(worker.report.getAllNotices());
        if(report.hasErrors()) {
            finalizeProcess(false);
            return;
        }

        this.setProgress("Adding native methods");
        suiteConfig.populateGlobalContext(this, global);

        //pass -1 (parsing)
        this.setProgress("Parsing files");
        walker = new FileWalker<>(new DirectoryCompoundInput(rootPath.toFile()), worker, this);
        if(cachedReader != null) {
            walker.setReader(cachedReader);
            cachedReader.setWorker(worker);
        }

        suiteConfig.setupWalkerForCompilation(walker);
        walker.addStops(DefaultWalkerStops.createCompilerWalkerStops(suiteConfig));

        try {
            walker.walk();
        } catch (IOException x) {
            logException(x);
            return;
        }

        walker.getReader().dumpNotices(report);
        if(report.hasErrors()) {
            finalizeProcess(false);
            return;
        }

        this.setProgress("Resolving dependencies");

        //not a pass
        for(PrismarineProjectWorker dependencyWorker : worker.output.getDependencies()) {
            PrismarineCompiler subCompiler = dependencyWorker.createCompiler();
            subCompiler.setParentCompiler(this);
            subCompiler.addProgressListener((process) -> this.updateStatus(process.getStatus()));
            try {
                subCompiler.runCompilation();
            } catch(Exception ex) {
                logException(ex);
                return;
            }
            report.addNotices(subCompiler.getReport().getAllNotices());
            if(!subCompiler.isSuccessful()) {
                finalizeProcess(false);
            } else {
                suiteConfig.incorporateDependency(this, subCompiler);

                for(Map.Entry<PrismarineLanguageUnitConfiguration, ArrayList<PrismarineLanguageUnit>> dependencyEntry : subCompiler.unitsList.entrySet()) {
                    if(!this.unitsList.containsKey(dependencyEntry.getKey())) {
                        this.unitsList.put(dependencyEntry.getKey(), new ArrayList<>());
                    }
                    this.unitsList.get(dependencyEntry.getKey()).addAll(dependencyEntry.getValue());
                }

                global.join(subCompiler.global);

                subCompiler.setRerouteRoot(true);
            }
        }

        suiteConfig.onDependenciesResolved(this);

        PrismarineLibrary standardLibrary = suiteConfig.getStandardLibrary();
        if(standardLibrary != null) {
            standardLibrary.populateCompiler(this);
        }

        //pass 0 (instantiation)
        for(Map.Entry<PrismarineLanguageUnitConfiguration, ArrayList<ProjectReader.Result>> entry : unitReadResults.entrySet()) {
            //TODO check that dependencies in combine mode have their unit read results merged

            PrismarineLanguageUnitConfiguration unitConfig = entry.getKey();
            ArrayList<PrismarineLanguageUnit> unitsForType = new ArrayList<>();
            unitsList.put(unitConfig, unitsForType);
            for(ProjectReader.Result readResult : entry.getValue()) {
                try {
                    PrismarineLanguageUnit unit = unitConfig.createUnit(this, readResult);
                    unitsForType.add(unit);
                    pathToUnitMap.put(readResult.getRelativePath(), unit);
                } catch(Exception ex) {
                    report.addNotice(new Notice(NoticeType.ERROR, ex.toString(), readResult.getPattern()));
                    break;
                }
            }
        }

        if(parentCompiler != null && dependencyMode == Dependency.Mode.COMBINE) {
            finalizeProcess(true);
            return;
        }

        if(!performPasses()) {
            finalizeProcess(false);
            return;
        }
        updateProgress(-1);

        suiteConfig.onAllPassesFinished(this);

        if(report.hasErrors()) {
            finalizeProcess(false);
            return;
        }

        if(parentCompiler != null && dependencyMode == Dependency.Mode.PRECOMPILE) {
            finalizeProcess(true);
            return;
        }

        suiteConfig.generateOutput(this);

        if(report.hasErrors()) {
            finalizeProcess(false);
            return;
        }

        finalizeProcess(true);
    }

    private boolean performPasses() {
        switch(suiteConfig.getUnitPassStrategy()) {
            case GROUP_BY_PASS: {
                return performPassesGroupedByPass();
            }
            case GROUP_BY_UNIT_TYPE: {
                return performPassesGroupedByUnitType();
            }
            default: {
                return false;
            }
        }
    }

    private boolean performPassesGroupedByPass() {
        int pass = 0;
        boolean anyPasses = true;

        while(anyPasses) {
            if(pass > 0) {
                suiteConfig.onPassEnd(this, pass);

                if(report.hasErrors()) {
                    return false;
                }
            }

            pass++;
            anyPasses = false;

            for(Map.Entry<PrismarineLanguageUnitConfiguration, ArrayList<PrismarineLanguageUnit>> entry : unitsList.entrySet()) {
                PrismarineLanguageUnitConfiguration unitConfig = entry.getKey();

                if(pass <= unitConfig.getNumberOfPasses()) {
                    updateProgress(0);
                    float delta = 1f / entry.getValue().size();
                    float progress = 0;

                    if(!anyPasses) {
                        suiteConfig.onPassStart(this, pass);
                    }
                    anyPasses = true;

                    for(PrismarineLanguageUnit unit : entry.getValue()) {
                        unitConfig.performPass(unit, this, pass);

                        progress += delta;
                        updateProgress(progress);
                    }
                }
            }
        }

        return true;
    }

    private boolean performPassesGroupedByUnitType() {
        for(Map.Entry<PrismarineLanguageUnitConfiguration, ArrayList<PrismarineLanguageUnit>> entry : unitsList.entrySet()) {
            PrismarineLanguageUnitConfiguration unitConfig = entry.getKey();

            int maxPasses = unitConfig.getNumberOfPasses();
            for(int pass = 1; pass <= maxPasses; pass++) { //1 indexed because pass 0 is instantiation. sorry.
                unitConfig.onPassStart(this, pass);

                updateProgress(0);
                float delta = 1f / entry.getValue().size();
                float progress = 0;

                for(PrismarineLanguageUnit unit : entry.getValue()) {
                    unitConfig.performPass(unit, this, pass);

                    progress += delta;
                    updateProgress(progress);
                }
                unitConfig.onPassEnd(this, pass);

                if(report.hasErrors()) {
                    return false;
                }
            }
        }
        return true;
    }

    public <T extends PrismarineLanguageUnit> void sortUnits(PrismarineLanguageUnitConfiguration<T> unitConfig, Comparator<T> comparator) {
        unitsList.get(unitConfig).sort((a, b) -> comparator.compare((T)a, (T)b));
    }

    private boolean rerouteRoot = false;

    private PrismarineCompiler parentCompiler = null;

    public PrismarineCompiler getRootCompiler() {
        return parentCompiler != null && rerouteRoot ? parentCompiler.getRootCompiler() : this;
    }
    public void setParentCompiler(PrismarineCompiler parentCompiler) {
        this.parentCompiler = parentCompiler;
    }

    private void setRerouteRoot(boolean rerouteRoot) {
        this.rerouteRoot = rerouteRoot;
    }

    @Override
    public void updateProgress(float progress) {
        super.updateProgress(progress);
    }

    private void logException(Throwable x) {
        logException(x, "");
    }

    public void logException(Throwable x, String prefix) {
        this.report.addNotice(new Notice(NoticeType.ERROR, prefix+x.toString() + " ; See console for details"));
        x.printStackTrace();
        finalizeProcess(false);
    }

    protected void finalizeProcess(boolean success) {
        this.setProgress("Compilation " + (success ? "completed" : "interrupted") + " with " + report.getTotalsString(), false);
        super.finalizeProcess(success);
    }

    public void setProgress(String message) {
        setProgress(message, true);
    }

    private void setProgress(String message, boolean includeProjectName) {
        updateStatus(message + (includeProjectName ? ("... [" + getRootDir().getName() + "]") : ""));
    }

    public <T> T get(PrismarineProjectWorkerTask<T> task) {
        return worker.output.get(task);
    }

    @Override
    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public CallStack getCallStack() {
        return callStack;
    }

    public ProjectReader getFileReader() {
        return walker.getReader();
    }

    public void setCachedReader(ProjectReader cachedReader) {
        this.cachedReader = cachedReader;
    }

    public TryStack getTryStack() {
        return tryStack;
    }

    public File getRootDir() {
        return rootPath.toFile();
    }

    public Path getRootPath() {
        return rootPath;
    }

    public ISymbolContext getGlobalContext() {
        return global;
    }

    public void setDependencyMode(Dependency.Mode dependencyMode) {
        this.dependencyMode = dependencyMode;
    }

    public Dependency.Mode getDependencyMode() {
        return dependencyMode;
    }

    private HashMap<PrismarineLanguageUnitConfiguration, ArrayList<ProjectReader.Result>> unitReadResults = new HashMap<>();

    public void putUnitReadResults(PrismarineLanguageUnitConfiguration unitConfig, ProjectReader.Result result) {
        if(!unitReadResults.containsKey(unitConfig)) {
            unitReadResults.put(unitConfig, new ArrayList<>());
        }
        unitReadResults.get(unitConfig).add(result);
    }



    public <T extends PrismarineLanguageUnit> T getUnit(PrismarineLanguageUnitConfiguration<T> unitConfig, Path path) {
        return getUnit(unitConfig.getUnitClass(), path);
    }
    public <T extends PrismarineLanguageUnit> T getUnit(Class<T> unitClass, Path path) {
        PrismarineLanguageUnit retrievedUnit = pathToUnitMap.get(path);
        if(unitClass.isInstance(retrievedUnit))
            return (T) retrievedUnit;
        return null;
    }

    public <T extends PrismarineLanguageUnit> Collection<T> getAllUnits(PrismarineLanguageUnitConfiguration<T> unitConfig) {
        return (Collection<T>) unitsList.get(unitConfig);
    }

    public <T extends PrismarineLanguageUnit> Collection<T> getAllUnits(Class<T> unitClass) {
        return (Collection<T>) unitsList.get(suiteConfig.getLanguageUnitConfigurations().get(unitClass));
    }

    public static File newFileObject(String path, File rootDir) {
        path = Paths.get(path.replace("$PROJECT_DIR$", rootDir.getAbsolutePath())).normalize().toString();
        return new File(path);
    }

    public static class Dependency {

        public enum Mode {
            PRECOMPILE, COMBINE;
        }

        PrismarineCompiler compiler;
        boolean doExport = false;
        Mode mode = Mode.PRECOMPILE;

        public Dependency(PrismarineCompiler compiler) {
            this.compiler = compiler;
        }
    }
}
