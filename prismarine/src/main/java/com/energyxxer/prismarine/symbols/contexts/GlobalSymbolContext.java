package com.energyxxer.prismarine.symbols.contexts;

import com.energyxxer.prismarine.PrismarineCompiler;
import com.energyxxer.prismarine.PrismarineLanguageUnit;
import com.energyxxer.prismarine.symbols.Symbol;
import com.energyxxer.prismarine.typesystem.functions.ActualParameterList;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class GlobalSymbolContext implements ISymbolContext {
    private final PrismarineCompiler compiler;
    private HashMap<String, Symbol> map = new HashMap<>();

    public GlobalSymbolContext(PrismarineCompiler compiler) {
        this.compiler = compiler;
    }

    @Override
    public ISymbolContext getParent() {
        return null;
    }

    @Override
    public Symbol search(@NotNull String name, ISymbolContext from, ActualParameterList params) {
        return map.get(name);
    }

    @Override
    public @NotNull PrismarineCompiler getCompiler() {
        return compiler.getRootCompiler();
    }

    @Override
    public PrismarineLanguageUnit getStaticParentUnit() {
        return null;
    }

    @Override
    public void put(Symbol symbol) {
        map.put(symbol.getName(), symbol);
    }

    public void join(GlobalSymbolContext other) {
        map.putAll(other.map);
    }

    @Override
    public HashMap<String, Symbol> collectVisibleSymbols(HashMap<String, Symbol> list, ISymbolContext from) {
        for(Map.Entry<String, Symbol> entry : map.entrySet()) {
            list.putIfAbsent(entry.getKey(), entry.getValue());
        }
        return map;
    }
}
