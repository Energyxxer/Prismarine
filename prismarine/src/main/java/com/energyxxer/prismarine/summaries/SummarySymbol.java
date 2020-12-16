package com.energyxxer.prismarine.summaries;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.symbols.SymbolVisibility;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

public class SummarySymbol implements SummaryElement {
    private PrismarineSummaryModule parentSummary;
    private String name;
    private int declarationIndex;
    private SymbolVisibility visibility;
    private HashSet<String> suggestionTags = new HashSet<>();
    private SummaryBlock subBlock = null;
    private boolean isInstanceField = false;
    private TokenPattern<?> declarationPattern;

    private SummarySymbol type;
    private SummarySymbol returnType;

    public SummarySymbol(PrismarineSummaryModule parentSummary, String name, int declarationIndex) {
        this(parentSummary, name, SymbolVisibility.PUBLIC, declarationIndex);
    }

    public SummarySymbol(PrismarineSummaryModule parentSummary, String name, SymbolVisibility visibility, int declarationIndex) {
        this.parentSummary = parentSummary;
        this.name = name;
        this.visibility = visibility;
        this.declarationIndex = declarationIndex;
    }

    public SummarySymbol getType() {
        return type;
    }

    public void setType(SummarySymbol type) {
        this.type = type;
    }

    public SummarySymbol getReturnType() {
        return returnType;
    }

    public SummarySymbol setReturnType(SummarySymbol returnType) {
        this.returnType = returnType;
        return this;
    }

    public SymbolVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(SymbolVisibility visibility) {
        this.visibility = visibility;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void putElement(SummaryElement element) {
        if(subBlock != null) subBlock.putElement(element);
        else throw new UnsupportedOperationException();
    }

    @Override
    public int getStartIndex() {
        return declarationIndex;
    }

    @Override
    public int getEndIndex() {
        return declarationIndex;
    }

    @Override
    public String toString() {
        return "(" + visibility.toString().toLowerCase() + ") " + name + "@" + declarationIndex;
    }

    @Override
    public void updateIndices(Function<Integer, Integer> h) {
        declarationIndex = h.apply(declarationIndex);
        scopeStart = h.apply(scopeStart);
        scopeEnd = h.apply(scopeEnd);
    }

    @Override
    public void collectSymbolsVisibleAt(int index, ArrayList<SummarySymbol> list, Path fromPath) {
        if(index < 0 || visibility.isVisibleFromSummaryBlock(this, fromPath, index)) {
            list.removeIf(e -> e.getName().equals(name));
            list.add(this);
        }
    }

    @Override
    public void collectGlobalSymbols(ArrayList<SummarySymbol> list) {
        if(getVisibility() == SymbolVisibility.GLOBAL) {
            list.removeIf(e -> e.getName().equals(this.getName()));
            list.add(this);
        }
    }

    public SummarySymbol addTag(String tag) {
        suggestionTags.add(tag);
        return this;
    }

    public HashSet<String> getSuggestionTags() {
        return suggestionTags;
    }

    public boolean hasSubBlock() {
        return subBlock != null;
    }

    public void setSubBlock(SummaryBlock subBlock) {
        this.subBlock = subBlock;
    }

    public SummaryBlock getSubBlock() {
        return subBlock;
    }

    public void setInstanceField(boolean instanceField) {
        isInstanceField = instanceField;
    }

    @Override
    public PrismarineSummaryModule getParentFileSummary() {
        return parentSummary;
    }

    private int scopeStart, scopeEnd;

    public void setScope(int start, int end) {
        this.scopeStart = start;
        this.scopeEnd = end;
    }

    public void setDeclarationPattern(TokenPattern<?> declarationPattern) {
        this.declarationPattern = declarationPattern;
    }


    public TokenPattern<?> getDeclarationPattern() {
        return declarationPattern;
    }

    public List<SummarySymbol> getSubSymbols(Path fromFile, int inFileIndex) {
        ArrayList<SummarySymbol> list = new ArrayList<>();
        if(subBlock != null) {
            subBlock.collectStaticSubSymbols(null, fromFile, inFileIndex, list);
        }
        if(type != null && type.subBlock != null) {
            type.subBlock.collectInstanceSubSymbols(null, fromFile, inFileIndex, list);
        }
        return list;
    }

    public List<SummarySymbol> getSubSymbolsByName(String name, Path fromFile, int inFileIndex) {
        ArrayList<SummarySymbol> list = new ArrayList<>();
        if(subBlock != null) {
            subBlock.collectStaticSubSymbols(name, fromFile, inFileIndex, list);
        }
        if(type != null && type.subBlock != null) {
            type.subBlock.collectInstanceSubSymbols(name, fromFile, inFileIndex, list);
        }
        return list;
    }

    public boolean isVisibleMember(Path fromFile, int inFileIndex) {
        return visibility.isVisibleMemberFromSummaryBlock(this, fromFile, inFileIndex);
    }

    public boolean isInstanceField() {
        return isInstanceField;
    }

    public int getScopeStart() {
        return scopeStart;
    }

    public int getScopeEnd() {
        return scopeEnd;
    }
}
