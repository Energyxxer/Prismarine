package com.energyxxer.prismarine.symbols.contexts;

import com.energyxxer.prismarine.PrismarineCompiler;
import com.energyxxer.prismarine.PrismarineLanguageUnit;
import com.energyxxer.prismarine.symbols.Symbol;
import com.energyxxer.prismarine.typesystem.functions.ActualParameterList;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class SymbolContext implements ISymbolContext {
    protected final PrismarineCompiler compiler;
    protected ISymbolContext parentScope;
    protected HashMap<String, Symbol> table = new HashMap<>();

    public SymbolContext(PrismarineCompiler compiler) {
        this.compiler = compiler;
    }

    public SymbolContext(ISymbolContext parentScope) {
        this.compiler = parentScope.getCompiler();
        this.parentScope = parentScope;
    }

    public Symbol search(@NotNull String name, ISymbolContext from, ActualParameterList params) {
        Symbol inMap = table.get(name);
        if(inMap != null && inMap.getVisibility().isVisibleFromContext(inMap, this, from)) {
            return table.get(name);
        }
        else if(parentScope != null) return parentScope.search(name, from, params);
        else return getGlobalContext().search(name, from, params);
    }

    public @NotNull PrismarineCompiler getCompiler() {
        return compiler.getRootCompiler();
    }

    public PrismarineLanguageUnit getStaticParentUnit() {
        if(this instanceof PrismarineLanguageUnit) return (PrismarineLanguageUnit) this;
        else if(parentScope != null) return parentScope.getStaticParentUnit();
        else throw new IllegalStateException();
    }

    public void put(Symbol symbol) {
        table.put(symbol.getName(), symbol);
    }

    @Override
    public ISymbolContext getParent() {
        return parentScope;
    }

    @Override
    public HashMap<String, Symbol> collectVisibleSymbols(HashMap<String, Symbol> list, ISymbolContext from) {
        for(Symbol inMap : table.values()) {
            if(inMap.getVisibility().isVisibleFromContext(inMap, this, from)) {
                list.putIfAbsent(inMap.getName(), inMap);
            }
        }
        if(parentScope != null) parentScope.collectVisibleSymbols(list, from);
        getGlobalContext().collectVisibleSymbols(list, from);
        return list;
    }
}
