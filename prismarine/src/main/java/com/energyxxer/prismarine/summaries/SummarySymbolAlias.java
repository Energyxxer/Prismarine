package com.energyxxer.prismarine.summaries;

import com.energyxxer.prismarine.symbols.SymbolVisibility;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SummarySymbolAlias extends SummarySymbol {
    private final SummarySymbol real;

    public SummarySymbolAlias(String name, SymbolVisibility visibility, int declarationIndex, SummarySymbol real) {
        super(real.getParentFileSummary(), name, visibility, declarationIndex);
        this.real = real;
    }

    public SummarySymbolAlias(String name, int declarationIndex, SummarySymbol real) {
        super(real.getParentFileSummary(), name, declarationIndex);
        this.real = real;
    }

    @Override
    public SummarySymbol getType() {
        return real.getType();
    }

    @Override
    public void setType(SummarySymbol type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SummarySymbol getReturnType() {
        return real.getReturnType();
    }

    @Override
    public SummarySymbol setReturnType(SummarySymbol returnType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putElement(SummaryElement element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateIndices(Function<Integer, Integer> h) {
        real.updateIndices(h);
    }

    @Override
    public void collectSymbolsVisibleAt(int index, ArrayList<SummarySymbol> list, Path fromPath) {
        super.collectSymbolsVisibleAt(index, list, fromPath);
        real.collectSymbolsVisibleAt(index, list, fromPath);
    }

    @Override
    public void collectGlobalSymbols(ArrayList<SummarySymbol> list) {
        super.collectGlobalSymbols(list);
        real.collectGlobalSymbols(list);
    }

    @Override
    public boolean hasSubBlock() {
        return real.hasSubBlock();
    }

    @Override
    public void setSubBlock(SummaryBlock subBlock) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SummaryBlock getSubBlock() {
        return real.getSubBlock();
    }

    @Override
    public List<SummarySymbol> getSubSymbols(Path fromFile, int inFileIndex) {
        return real.getSubSymbols(fromFile, inFileIndex);
    }

    @Override
    public List<SummarySymbol> getSubSymbolsByName(String name, Path fromFile, int inFileIndex) {
        return real.getSubSymbolsByName(name, fromFile, inFileIndex);
    }
}
