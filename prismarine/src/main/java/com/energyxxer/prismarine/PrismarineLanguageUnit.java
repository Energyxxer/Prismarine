package com.energyxxer.prismarine;

import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import com.energyxxer.prismarine.symbols.contexts.SymbolContext;

import java.io.File;
import java.nio.file.Path;

public abstract class PrismarineLanguageUnit extends SymbolContext {

    protected final Path pathFromRoot;

    public PrismarineLanguageUnit(PrismarineCompiler compiler, Path pathFromRoot) {
        super(compiler);
        this.pathFromRoot = pathFromRoot;
    }

    public PrismarineLanguageUnit(ISymbolContext parentScope, Path pathFromRoot) {
        super(parentScope);
        this.pathFromRoot = pathFromRoot;
    }

    public Path getPathFromRoot() {
        return pathFromRoot;
    }

    public String getPrettyName() {
        return getPathFromRoot().toString().replace(File.separatorChar, '/');
    }
}
