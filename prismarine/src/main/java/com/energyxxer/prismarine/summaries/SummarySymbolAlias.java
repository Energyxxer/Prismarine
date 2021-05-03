package com.energyxxer.prismarine.summaries;

import com.energyxxer.prismarine.symbols.SymbolVisibility;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.Function;

public class SummarySymbolAlias extends SummarySymbol {
    private SummarySymbol real;

    public SummarySymbolAlias(String name, SymbolVisibility visibility, int declarationIndex, SummarySymbol real) {
        super(real != null ? real.getParentFileSummary() : null, name, visibility, declarationIndex);
        this.real = real;
    }

    public SummarySymbolAlias(String name, int declarationIndex, SummarySymbol real) {
        super(real != null ? real.getParentFileSummary() : null, name, declarationIndex);
        this.real = real;
    }

    public SummarySymbol getReal() {
        return real;
    }

    public void setReal(SummarySymbol real) {
        this.real = real;
    }

    @Override
    public SummarySymbol getType(PrismarineSummaryModule summary) {
        return real != null ? real.getType(summary) : null;
    }

    @Override
    public void setType(SummarySymbol type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setType(SymbolReference type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SummarySymbol getReturnType(PrismarineSummaryModule summary) {
        return real != null ? real.getReturnType(summary) : null;
    }

    @Override
    public SummarySymbol setReturnType(SummarySymbol returnType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SummarySymbol setReturnType(SymbolReference returnType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putElement(SummaryElement element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateIndices(Function<Integer, Integer> h) {
        if(real != null) real.updateIndices(h);
    }

    @Override
    public ArrayList<SummarySymbol> collectSymbolsVisibleAt(int index, ArrayList<SummarySymbol> list, Path fromPath, PrismarineSummaryModule summary) {
        super.collectSymbolsVisibleAt(index, list, fromPath, summary);
        if(real != null) real.collectSymbolsVisibleAt(index, list, fromPath, summary);
        return list;
    }

    @Override
    public ArrayList<SummarySymbol> collectGlobalSymbols(ArrayList<SummarySymbol> list) {
        super.collectGlobalSymbols(list);
        if(real != null) real.collectGlobalSymbols(list);
        return list;
    }

    @Override
    public boolean hasSubBlock() {
        return real != null && real.hasSubBlock();
    }

    @Override
    public void setSubBlock(SummaryBlock subBlock) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SummaryBlock getSubBlock() {
        return real != null ? real.getSubBlock() : null;
    }

    @Override
    public ArrayList<SummarySymbol> collectSubSymbols(Path fromFile, int inFileIndex, ArrayList<SummarySymbol> list, PrismarineSummaryModule summary) {
        if(real != null) real.collectSubSymbols(fromFile, inFileIndex, list, summary);
        return list;
    }

    @Override
    public ArrayList<SummarySymbol> collectSubSymbolsByName(String name, Path fromFile, int inFileIndex, ArrayList<SummarySymbol> list, PrismarineSummaryModule summary) {
        if(real != null) real.collectSubSymbolsByName(name, fromFile, inFileIndex, list, summary);
        return list;
    }
}
