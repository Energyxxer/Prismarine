package com.energyxxer.prismarine.walker;

import com.energyxxer.prismarine.util.PathMatcher;
import com.energyxxer.prismarine.worker.PrismarineProjectWorker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public abstract class FileWalkerStop<T> {
    protected PathMatcher pathMatcher;

    public FileWalkerStop(String regex) {
        this.pathMatcher = PathMatcher.createMatcher(regex);
    }

    public abstract boolean accept(File file, Path relativePath, PathMatcher.Result pathMatchResult, PrismarineProjectWorker worker, FileWalker<T> walker) throws IOException;
}
