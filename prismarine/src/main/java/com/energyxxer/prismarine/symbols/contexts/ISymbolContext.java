package com.energyxxer.prismarine.symbols.contexts;

import com.energyxxer.prismarine.PrismarineCompiler;
import com.energyxxer.prismarine.PrismarineLanguageUnit;
import com.energyxxer.prismarine.symbols.Symbol;
import com.energyxxer.prismarine.symbols.SymbolVisibility;
import com.energyxxer.prismarine.typesystem.PrismarineTypeSystem;
import com.energyxxer.prismarine.typesystem.functions.ActualParameterList;
import com.energyxxer.prismarine.worker.PrismarineProjectWorkerTask;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.HashMap;

public interface ISymbolContext {

    ISymbolContext getParent();

    Symbol search(@NotNull String name, ISymbolContext from, ActualParameterList params);

    PrismarineCompiler getCompiler();

    PrismarineLanguageUnit getStaticParentUnit();

    void put(Symbol symbol);

    default ISymbolContext getGlobalContext() {
        return getCompiler().getGlobalContext();
    }

    default ISymbolContext getContextForVisibility(SymbolVisibility visibility) {
        if(visibility == SymbolVisibility.GLOBAL) return getGlobalContext();
        return this;
    }

    default void putInContextForVisibility(SymbolVisibility visibility, Symbol symbol) {
        this.put(symbol);
        if(visibility == SymbolVisibility.GLOBAL) getGlobalContext().put(symbol);
    }

    default Path getPathFromRoot() {
        return getStaticParentUnit().getPathFromRoot();
    }

    HashMap<String, Symbol> collectVisibleSymbols(HashMap<String, Symbol> list, ISymbolContext from);

    default boolean isAncestor(ISymbolContext ancestor) {
        if(this == ancestor) return true;
        ISymbolContext parent = getParent();
        if(parent == null) return false;
        return parent.isAncestor(ancestor);
    }

    default <T> T get(PrismarineProjectWorkerTask<T> task) {
        return getCompiler().get(task);
    }

    default PrismarineTypeSystem getTypeSystem() {
        return getCompiler().getTypeSystem();
    }
}
