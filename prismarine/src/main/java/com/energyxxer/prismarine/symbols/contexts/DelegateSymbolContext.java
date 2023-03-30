package com.energyxxer.prismarine.symbols.contexts;

import com.energyxxer.prismarine.PrismarineCompiler;
import com.energyxxer.prismarine.PrismarineLanguageUnit;
import com.energyxxer.prismarine.symbols.Symbol;
import com.energyxxer.prismarine.typesystem.functions.ActualParameterList;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class DelegateSymbolContext implements ISymbolContext {
    private final ISymbolContext parent;
    private final ISymbolContext delegate;

    public DelegateSymbolContext(@NotNull ISymbolContext delegate, @NotNull ISymbolContext parent) {
        this.parent = parent;
        this.delegate = delegate;
    }

    @Override
    public ISymbolContext getParent() {
        return parent;
    }

    public ISymbolContext getDelegate() {
        return delegate;
    }

    @Override
    public Symbol search(@NotNull String name, ISymbolContext from, ActualParameterList params) {
        Symbol fromDelegate = delegate.search(name, from, params);
        if(fromDelegate != null) return fromDelegate;
        return parent.search(name, from, params);
    }

    @Override
    public PrismarineCompiler getCompiler() {
        return parent.getCompiler();
    }

    @Override
    public PrismarineLanguageUnit getStaticParentUnit() {
        return parent.getStaticParentUnit();
    }

    @Override
    public void put(Symbol symbol) {
        delegate.put(symbol);
    }

    @Override
    public HashMap<String, Symbol> collectVisibleSymbols(HashMap<String, Symbol> list, ISymbolContext from) {
        delegate.collectVisibleSymbols(list, from);
        parent.collectVisibleSymbols(list, from);
        return list;
    }

    @Override
    public boolean isAncestor(ISymbolContext ancestor) {
        return delegate.isAncestor(ancestor) || ISymbolContext.super.isAncestor(ancestor);
    }
}
