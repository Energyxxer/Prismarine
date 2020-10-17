package com.energyxxer.prismarine.plugins;

import com.energyxxer.prismarine.plugins.syntax.PrismarineSyntaxFile;
import com.energyxxer.prismarine.worker.PrismarineProjectWorker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

public class PrismarinePluginUnit {
    private final PrismarinePlugin definingPlugin;
    private final PrismarinePluginUnitConfiguration config;

    private HashMap<PluginDataIdentifier<?>, Object> data = new HashMap<>();

    public PrismarinePluginUnit(PrismarinePlugin definingPlugin, PrismarinePluginUnitConfiguration config) {
        this.definingPlugin = definingPlugin;
        this.config = config;
    }

    public void update(PrismarineProjectWorker projectWorker) throws IOException {
        config.updateUnitForProjectWorker(this, projectWorker);
    }

    public PrismarineSyntaxFile createSyntaxFile(Path... paths) throws IOException {
        if(paths.length == 0) throw new NullPointerException();
        IOException lastFNFException = null;
        for(Path path : paths) {
            try {
                String strContent = definingPlugin.getWalker().getReader().startQuery(path).needsString().perform().getString();
                return new PrismarineSyntaxFile(path, this, strContent);
            } catch (FileNotFoundException x) {
                lastFNFException = x;
            }
        }
        throw lastFNFException;
    }

    public PrismarinePluginFile createFile(Path relativePath) {
        return new PrismarinePluginFile(relativePath, this);
    }

    public PrismarinePlugin getDefiningPlugin() {
        return definingPlugin;
    }

    public PrismarinePluginUnitConfiguration getConfig() {
        return config;
    }

    public <T> T get(PluginDataIdentifier<T> identifier) {
        return (T) data.get(identifier);
    }

    public <T> void set(PluginDataIdentifier<T> identifier, T data) {
        this.data.put(identifier, data);
    }
}
