package com.energyxxer.prismarine.walker;

import com.energyxxer.prismarine.PrismarineCompiler;
import com.energyxxer.prismarine.PrismarineLanguageUnitConfiguration;
import com.energyxxer.prismarine.PrismarineSuiteConfiguration;
import com.energyxxer.prismarine.in.ProjectReader;
import com.energyxxer.prismarine.plugins.PrismarinePlugin;
import com.energyxxer.prismarine.plugins.PrismarinePluginUnit;
import com.energyxxer.prismarine.plugins.PrismarinePluginUnitConfiguration;
import com.energyxxer.prismarine.summaries.PrismarineProjectSummary;
import com.energyxxer.prismarine.util.PathMatcher;
import com.energyxxer.prismarine.worker.PrismarineProjectWorker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

public class DefaultWalkerStops {
    private DefaultWalkerStops() {}

    public static Collection<FileWalkerStop<PrismarineCompiler>> createCompilerWalkerStops(PrismarineSuiteConfiguration suiteConfig) {
        ArrayList<FileWalkerStop<PrismarineCompiler>> stops = new ArrayList<>();

        for(PrismarineLanguageUnitConfiguration unitConfig : suiteConfig.getLanguageUnitConfigurations().values()) {
            FileWalkerStop<PrismarineCompiler> stop = new FileWalkerStop<PrismarineCompiler>(unitConfig.getStopPath()) {
                @Override
                public boolean accept(File file, Path relativePath, PathMatcher.Result pathMatchResult, PrismarineProjectWorker worker, FileWalker<PrismarineCompiler> walker) throws IOException {

                    ProjectReader.Result result = walker.getReader()
                            .startQuery(relativePath)
                            .needsPattern(unitConfig)
                            .perform();

                    walker.getSubject().putUnitReadResults(unitConfig, result);

                    return unitConfig.consumeCompilerWalkerStop();
                }
            };

            stops.add(stop);
        }

        return stops;
    }

    public static Collection<FileWalkerStop<PrismarineProjectSummary>> createSummaryWalkerStops(PrismarineSuiteConfiguration suiteConfig) {
        ArrayList<FileWalkerStop<PrismarineProjectSummary>> stops = new ArrayList<>();

        for(PrismarineLanguageUnitConfiguration unitConfig : suiteConfig.getLanguageUnitConfigurations().values()) {
            FileWalkerStop<PrismarineProjectSummary> stop = new FileWalkerStop<PrismarineProjectSummary>(unitConfig.getStopPath()) {
                @Override
                public boolean accept(File file, Path relativePath, PathMatcher.Result pathMatchResult, PrismarineProjectWorker worker, FileWalker<PrismarineProjectSummary> walker) throws IOException {

                    ProjectReader.Result result = walker.getReader()
                            .startQuery(relativePath)
                            .needsSummary(unitConfig, walker.getSubject(), true)
                            .perform();

                    if(result.getSummary() != null) {
                        walker.getSubject().store(file, result.getSummary());
                    }

                    return unitConfig.consumeSummaryWalkerStop();
                }
            };

            stops.add(stop);
        }

        return stops;
    }

    public static Collection<FileWalkerStop<PrismarinePlugin>> createPluginWalkerStops(PrismarineSuiteConfiguration suiteConfig) {
        ArrayList<FileWalkerStop<PrismarinePlugin>> stops = new ArrayList<>();

        for(PrismarinePluginUnitConfiguration unitConfig : suiteConfig.getPluginUnitConfigurations().values()) {
            FileWalkerStop<PrismarinePlugin> stop = new FileWalkerStop<PrismarinePlugin>(unitConfig.getStopPath()) {
                @Override
                public boolean accept(File file, Path relativePath, PathMatcher.Result pathMatchResult, PrismarineProjectWorker worker, FileWalker<PrismarinePlugin> walker) throws IOException {

                    PrismarinePluginUnit unit = new PrismarinePluginUnit(
                            walker.getSubject(),
                            unitConfig,
                            relativePath
                    );

                    unitConfig.onStaticWalkerStop(unit, file, relativePath, pathMatchResult, worker, walker);
                    walker.getSubject().addUnit(unit);

                    return unitConfig.consumePluginWalkerStop();
                }
            };
            stops.add(stop);
        }

        return stops;
    }
}
