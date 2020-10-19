package com.energyxxer.prismarine.libraries;

import com.energyxxer.prismarine.Prismarine;
import com.energyxxer.prismarine.PrismarineCompiler;
import com.energyxxer.prismarine.PrismarineLanguageUnitConfiguration;
import com.energyxxer.prismarine.PrismarineSuiteConfiguration;
import com.energyxxer.prismarine.in.ProjectReader;
import com.energyxxer.prismarine.summaries.PrismarineProjectSummary;
import com.energyxxer.prismarine.worker.PrismarineProjectWorker;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.util.out.Console;

import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;

public class PrismarineLibrary {
    private final PrismarineSuiteConfiguration suiteConfig;
    private final ArrayList<PrismarineLibraryUnit> libraryUnits = new ArrayList<>();
    private boolean unitsLocked = false;

    public PrismarineLibrary(PrismarineSuiteConfiguration suiteConfig) {
        this.suiteConfig = suiteConfig;
    }

    public PrismarineLibrary registerUnit(Path path, PrismarineLanguageUnitConfiguration unitConfig, String content) {
        return registerUnit(path, unitConfig, content, PrismarineLibraryUnit.Availability.BOTH);
    }

    public PrismarineLibrary registerUnit(Path path, PrismarineLanguageUnitConfiguration unitConfig, String content, PrismarineLibraryUnit.Availability availability) {
        if(unitsLocked) {
            throw new IllegalStateException("Units for this library are locked!");
        }
        PrismarineLibraryUnit unit = new PrismarineLibraryUnit(path, unitConfig, content, availability);
        libraryUnits.add(unit);
        return this;
    }

    public PrismarineLibrary start() throws Exception {
        if(!unitsLocked) {
            unitsLocked = true;

            PrismarineProjectWorker worker = new PrismarineProjectWorker(suiteConfig, null);
            suiteConfig.setupWorkerForLibrary(worker);

            worker.work();

            ProjectReader reader = new ProjectReader(null, worker);
            //input is null because we avoid calling performQuery(), and instead pass the string contents ourselves.

            for(PrismarineLibraryUnit unit : libraryUnits) {
                ProjectReader.Query query = reader.startQuery(unit.getRelativePath()).needsPattern(unit.getUnitConfig()).needsSummary(unit.getUnitConfig(), true);
                ProjectReader.Result result = reader.populateParseResult(query, unit.getRelativePath().toFile(), reader.createResultFromString(unit.getContent()));

                if(!result.matchResponse.matched) {
                    throw new RuntimeException("FATAL: Syntax error in library for " + unit.getUnitConfig().getClass().getSimpleName() + ": " + unit.getRelativePath());
                }

                unit.setParseResult(result);
            }
        }
        return this;
    }

    public void populateCompiler(PrismarineCompiler compiler) {
        for(PrismarineLibraryUnit unit : libraryUnits) {
            if(!unit.getAvailability().compiler) continue;
            compiler.putUnitReadResults(unit.getUnitConfig(), unit.getParseResult());
        }
    }

    public void populateSummary(PrismarineProjectSummary summary) {
        for(PrismarineLibraryUnit unit : libraryUnits) {
            if(!unit.getAvailability().summary) continue;
            summary.store(null, unit.getParseResult().getSummary());
        }
    }
}
