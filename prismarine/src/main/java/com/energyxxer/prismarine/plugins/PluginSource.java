package com.energyxxer.prismarine.plugins;

import com.energyxxer.enxlex.lexical_analysis.token.TokenSource;

import java.io.File;
import java.nio.file.Path;

public class PluginSource implements TokenSource {
    private final File pluginRoot;
    private final Path relativePath;

    public PluginSource(File pluginRoot, Path relativePath) {
        this.pluginRoot = pluginRoot;
        this.relativePath = relativePath;
    }

    @Override
    public String getFileName() {
        return relativePath.getFileName().toString();
    }

    @Override
    public String getFullPath() {
        File exactFile = getExactFile();
        return exactFile != null ? exactFile.getAbsolutePath() : relativePath.toString();
    }

    @Override
    public String getPrettyName() {
        return "Plugin: " + pluginRoot.getName() + " > " + getFullPath();
    }

    @Override
    public File getRelatedFile() {
        File exactFile = getExactFile();
        return exactFile != null ? exactFile : pluginRoot;
    }

    @Override
    public File getExactFile() {
        File potentialExactFile = pluginRoot.toPath().resolve(relativePath).toFile();
        if(potentialExactFile.exists()) return potentialExactFile;
        return null;
    }
}
