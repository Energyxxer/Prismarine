package com.energyxxer.enxlex.lexical_analysis.token;

import java.io.File;
import java.nio.file.Path;

public class ZipSource implements TokenSource {
    private File root;
    private Path relativePath;

    public ZipSource(File root, Path relativePath) {
        this.root = root;
        this.relativePath = relativePath;
    }

    @Override
    public String getFileName() {
        return relativePath.getFileName().toString();
    }

    @Override
    public String getFullPath() {
        return relativePath.toString();
    }

    @Override
    public String getPrettyName() {
        return root.getName() + " > " + relativePath;
    }

    @Override
    public File getRelatedFile() {
        return root;
    }

    @Override
    public File getExactFile() {
        return null;
    }
}
