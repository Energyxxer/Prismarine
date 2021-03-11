package com.energyxxer.prismarine.plugins;

import com.energyxxer.commodore.util.io.CompoundInput;
import com.energyxxer.enxlex.lexical_analysis.EagerLexer;
import com.energyxxer.enxlex.lexical_analysis.token.TokenStream;
import com.energyxxer.prismarine.PrismarineSuiteConfiguration;
import com.energyxxer.prismarine.walker.DefaultWalkerStops;
import com.energyxxer.prismarine.walker.FileWalker;
import com.energyxxer.prismarine.worker.PrismarineProjectWorker;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class PrismarinePlugin {
    private final PrismarineSuiteConfiguration suiteConfig;
    private final String name;
    private final PrismarineProjectWorker pluginWorker;
    @NotNull
    private final FileWalker<PrismarinePlugin> walker;
    private final Path sourcePath;
    private boolean loaded;

    private final ArrayList<PrismarinePluginUnit> units = new ArrayList<>();

    public PrismarinePlugin(String name, @NotNull CompoundInput source, File sourceFile, PrismarineSuiteConfiguration suiteConfig) {
        this.suiteConfig = suiteConfig;
        this.name = name;
        this.loaded = false;
        this.pluginWorker = new PrismarineProjectWorker(suiteConfig, sourceFile);
        this.walker = new FileWalker<>(
                source,
                p -> new PluginSource(sourceFile, p),
                pluginWorker,
                this
        );
        this.sourcePath = sourceFile.toPath();
    }

    private EagerLexer eagerLexer;

    public synchronized void load() throws IOException {
        if(this.loaded) return;

        this.eagerLexer = new EagerLexer(new TokenStream(false));

        suiteConfig.setupWorkerForPlugin(pluginWorker);

        try {
            pluginWorker.work();
        } catch(Exception x) {
            throw new IOException(x.getMessage()); //TODO ?
        }

        walker.addStops(DefaultWalkerStops.createPluginWalkerStops(suiteConfig));
        suiteConfig.setupWalkerForPlugin(walker);

        walker.walk();

        loaded = true;
    }

    public synchronized void attachToProjectWorker(PrismarineProjectWorker projectWorker) throws IOException {
        load();

        this.walker.getReader().setWorker(projectWorker);
        this.walker.getReader().refreshPatterns();

        for(PrismarinePluginUnit unit : units) {
            unit.update(projectWorker);
        }
    }

    public void addUnit(PrismarinePluginUnit unit) {
        units.add(unit);
    }

    public String getName() {
        return name;
    }

    @NotNull
    public FileWalker<PrismarinePlugin> getWalker() {
        return walker;
    }

    public PrismarineProjectWorker getPluginWorker() {
        return pluginWorker;
    }

    public EagerLexer getEagerLexer() {
        return eagerLexer;
    }

    public Path getSourcePath() {
        return sourcePath;
    }
}
