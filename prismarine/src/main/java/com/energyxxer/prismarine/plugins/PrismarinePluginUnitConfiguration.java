package com.energyxxer.prismarine.plugins;

import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.prismarine.PrismarineProductions;
import com.energyxxer.prismarine.util.PathMatcher;
import com.energyxxer.prismarine.walker.FileWalker;
import com.energyxxer.prismarine.worker.PrismarineProjectWorker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public abstract class PrismarinePluginUnitConfiguration {

    public abstract String getStopPath();

    public abstract TokenPatternMatch getStructureByName(String name, PrismarineProductions productions);

    public abstract void onStaticWalkerStop(PrismarinePluginUnit unit, File file, Path relativePath, PathMatcher.Result pathMatchResult, PrismarineProjectWorker worker, FileWalker<PrismarinePlugin> walker) throws IOException;

    public abstract void updateUnitForProjectWorker(PrismarinePluginUnit unit, PrismarineProjectWorker projectWorker) throws IOException;
}
