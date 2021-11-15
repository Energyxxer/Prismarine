package com.energyxxer.enxlex.lexical_analysis.token;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public class ProjectSourceFile implements TokenSource {

    private final Path rootPath;
    private final Path relativePath;
    private final File file;

    public ProjectSourceFile(Path rootPath, Path relativePath) {
        this.rootPath = rootPath;
        this.relativePath = relativePath;
        this.file = rootPath.resolve(relativePath).toFile();
    }

    @Override
    public String getFileName() {
        return file.getName();
    }

    @Override
    public String getFullPath() {
        return file.getAbsolutePath();
    }

    @Override
    public String getPrettyName() {
        return rootPath.getFileName() + " > " + relativePath;
    }

    @Override
    public File getRelatedFile() {
        return file;
    }

    @Override
    public File getExactFile() {
        return file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectSourceFile that = (ProjectSourceFile) o;
        return rootPath.equals(that.rootPath) &&
                relativePath.equals(that.relativePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootPath, relativePath);
    }
}
