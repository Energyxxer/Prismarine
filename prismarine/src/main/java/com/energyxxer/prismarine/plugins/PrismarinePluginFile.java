package com.energyxxer.prismarine.plugins;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.PrismarineLanguageUnitConfiguration;
import com.energyxxer.prismarine.in.ProjectReader;

import java.io.IOException;
import java.nio.file.Path;

public class PrismarinePluginFile<T> {
    protected final Path relativePath;
    protected final PrismarinePluginUnit unit;

    protected String string;
    protected TokenPattern<?> pattern;

    protected T output;

    public PrismarinePluginFile(Path relativePath, PrismarinePluginUnit unit) {
        this.relativePath = relativePath;
        this.unit = unit;
    }

    public void update(PrismarineLanguageUnitConfiguration langUnitConfig) throws IOException {
        ProjectReader reader = unit.getDefiningPlugin().getWalker().getReader();
        ProjectReader.Query query = reader.startQuery(relativePath);
        query.needsString();
        if(langUnitConfig != null) {
            query.needsPattern(langUnitConfig);
        }
        ProjectReader.Result result = query.perform();

        this.string = result.getString();
        this.pattern = result.getPattern();

        updateOutput();
    }

    public void updateOutput() {

    }

    public T getOutput() {
        return output;
    }

    public String getString() {
        return string;
    }

    public TokenPattern<?> getPattern() {
        return pattern;
    }
}
