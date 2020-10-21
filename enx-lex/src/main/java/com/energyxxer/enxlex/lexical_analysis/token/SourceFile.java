package com.energyxxer.enxlex.lexical_analysis.token;

import java.io.File;

public class SourceFile implements TokenSource {

    private final File file;

    public SourceFile(File file) {
        this.file = file;
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
        return file.getAbsolutePath();
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
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof SourceFile && ((SourceFile) obj).file.equals(this.file));
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }
}
