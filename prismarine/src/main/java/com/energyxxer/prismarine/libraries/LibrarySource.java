package com.energyxxer.prismarine.libraries;

import com.energyxxer.enxlex.lexical_analysis.token.TokenSource;

import java.io.File;
import java.nio.file.Path;

public class LibrarySource implements TokenSource {
    private final String libraryName;
    private final Path path;

    public LibrarySource(String libraryName, Path path) {
        this.libraryName = libraryName;
        this.path = path;
    }

    @Override
    public String getFileName() {
        return path.getFileName().toString();
    }

    @Override
    public String getFullPath() {
        return path.toString();
    }

    @Override
    public String getPrettyName() {
        return libraryName + " > " + path;
    }

    @Override
    public File getRelatedFile() {
        return null;
    }

    @Override
    public File getExactFile() {
        return null;
    }
}
