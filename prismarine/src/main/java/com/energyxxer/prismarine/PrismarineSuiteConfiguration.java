package com.energyxxer.prismarine;

import com.energyxxer.prismarine.libraries.PrismarineLibrary;
import com.energyxxer.prismarine.plugins.PrismarinePlugin;
import com.energyxxer.prismarine.plugins.PrismarinePluginUnitConfiguration;
import com.energyxxer.prismarine.summaries.PrismarineProjectSummarizer;
import com.energyxxer.prismarine.summaries.PrismarineProjectSummary;
import com.energyxxer.prismarine.symbols.contexts.GlobalSymbolContext;
import com.energyxxer.prismarine.typesystem.PrismarineTypeSystem;
import com.energyxxer.prismarine.walker.FileWalker;
import com.energyxxer.prismarine.worker.PrismarineProjectWorker;

import java.io.File;
import java.util.LinkedHashMap;

public abstract class PrismarineSuiteConfiguration {
    private final LinkedHashMap<Class, PrismarineLanguageUnitConfiguration> languageUnitConfigurations = new LinkedHashMap<>();
    private final LinkedHashMap<Class, PrismarinePluginUnitConfiguration> pluginUnitConfigurations = new LinkedHashMap<>();

    public UnitPassStrategy getUnitPassStrategy() {
        return UnitPassStrategy.GROUP_BY_PASS;
    }

    public abstract PrismarineProjectSummary createSummary();

    public abstract PrismarineTypeSystem createTypeSystem(PrismarineCompiler compiler, GlobalSymbolContext globalCtx);

    public abstract void populateGlobalContext(PrismarineCompiler compiler, GlobalSymbolContext global);
    public LinkedHashMap<Class, PrismarineLanguageUnitConfiguration> getLanguageUnitConfigurations() {
        return languageUnitConfigurations;
    }

    public LinkedHashMap<Class, PrismarinePluginUnitConfiguration> getPluginUnitConfigurations() {
        return pluginUnitConfigurations;
    }

    public abstract void setupWorkerForCompilation(PrismarineProjectWorker worker);
    public abstract void setupWorkerForSummary(PrismarineProjectWorker worker);
    public abstract void setupWorkerForLibrary(PrismarineProjectWorker worker);
    public abstract void setupWorkerForPlugin(PrismarineProjectWorker worker);

    public abstract void setupWalkerForCompilation(FileWalker<PrismarineCompiler> walker);
    public abstract void setupWalkerForSummary(FileWalker<PrismarineProjectSummary> walker);
    public abstract void setupWalkerForPlugin(FileWalker<PrismarinePlugin> walker);
    /**
     * Runs when the compilation process starts
     * */
    public void onCompilationStarted(PrismarineCompiler compiler) {}

    /**
     * Runs after ALL of this project's dependencies have been resolved after compilation starts.
     * */
    public void onDependenciesResolved(PrismarineCompiler compiler) {}
    /**
     * Runs before performing a pass on all the units.
     *
     * Only applicable when this suite's Unit Pass Strategy is GROUP_BY_PASS.
     * Refer to {@link PrismarineLanguageUnitConfiguration#onPassStart(PrismarineCompiler, int)} for the GROUP_BY_UNIT_TYPE strategy
     * */
    public void onPassStart(PrismarineCompiler compiler, int passNumber) {}

    /**
     * Runs after performing a pass on all the units.
     *
     * Only applicable when this suite's Unit Pass Strategy is GROUP_BY_PASS
     * Refer to {@link PrismarineLanguageUnitConfiguration#onPassEnd(PrismarineCompiler, int)} for the GROUP_BY_UNIT_TYPE strategy
     * */
    public void onPassEnd(PrismarineCompiler compiler, int passNumber) {}

    public abstract void incorporateDependency(PrismarineCompiler thisCompiler, PrismarineCompiler subCompiler);
    public void onAllCompilationWorkerTasksFinished(PrismarineProjectWorker worker, PrismarineCompiler compiler) {}

    public void onAllPassesFinished(PrismarineCompiler compiler) {}

    public abstract void generateOutput(PrismarineCompiler compiler);
    public abstract PrismarineCompiler createCompiler(PrismarineProjectWorker worker);

    public abstract PrismarineProjectSummarizer createSummarizer(PrismarineProjectWorker worker);

    protected void putLanguageUnitConfiguration(PrismarineLanguageUnitConfiguration unitConfig) {
        languageUnitConfigurations.put(unitConfig.getUnitClass(), unitConfig);
    }

    protected void putPluginUnitConfiguration(PrismarinePluginUnitConfiguration unitConfig) {
        pluginUnitConfigurations.put(unitConfig.getClass(), unitConfig);
    }

    public PrismarineProjectWorker createWorker(File rootDirectory) {
        return new PrismarineProjectWorker(this, rootDirectory);
    }

    public abstract PrismarineLibrary getStandardLibrary();

    public abstract void runSummaryPreFileTree(PrismarineProjectSummarizer<?> summarizer);
    public abstract void runSummaryPostFileTree(PrismarineProjectSummarizer<?> summarizer);

    public enum UnitPassStrategy {
        /**
         * Tells the Prismarine Compiler that all the units of all types should have their passes finished before continuing onto the next pass.
         * Example with 3 passes and unit types A and B:
         *
         * 1. Pass 1 for type A
         * 2. Pass 1 for type B
         *
         * 3. Pass 2 for type A
         * 4. Pass 2 for type B
         *
         * 5. Pass 3 for type A
         * 6. Pass 3 for type B
         * */
        GROUP_BY_PASS,
        /**
         * Tells the Prismarine Compiler that all the passes for units of a type should be complete before continuing onto the next unit type.
         * Example with 3 passes and unit types A and B:
         *
         * 1. Pass 1 for type A
         * 3. Pass 2 for type A
         * 3. Pass 3 for type A
         *
         * 4. Pass 1 for type B
         * 5. Pass 2 for type B
         * 6. Pass 3 for type B
         * */
        GROUP_BY_UNIT_TYPE
    }
}
