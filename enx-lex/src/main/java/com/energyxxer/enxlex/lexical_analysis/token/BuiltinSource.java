package com.energyxxer.enxlex.lexical_analysis.token;

import java.io.File;

public class BuiltinSource implements TokenSource {
    private String name;

    public BuiltinSource() {
        this(null);
    }

    public BuiltinSource(String name) {
        this.name = name;

        if(name == null) {
            this.name = "<built-in>";
        } else {
            this.name = "<built-in: " + name + ">";
        }
    }

    @Override
    public String getFileName() {
        return name;
    }

    @Override
    public String getFullPath() {
        return name;
    }

    @Override
    public String getPrettyName() {
        return name;
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
