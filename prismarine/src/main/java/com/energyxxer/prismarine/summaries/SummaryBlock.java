package com.energyxxer.prismarine.summaries;

import com.energyxxer.enxlex.lexical_analysis.summary.SummaryModule;
import com.energyxxer.prismarine.symbols.SymbolVisibility;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.Function;

public class SummaryBlock implements SummaryElement {
    private boolean fixed = false;
    private SummarySymbol associatedSymbol = null;
    private PrismarineSummaryModule parentSummary;
    private int startIndex;
    private int endIndex;
    private ArrayList<SummaryElement> subElements = new ArrayList<>();

    public SummaryBlock(PrismarineSummaryModule parentSummary) {
        this(parentSummary, 0, Integer.MAX_VALUE);
        this.fixed = true;
    }

    public SummaryBlock(PrismarineSummaryModule parentSummary, int startIndex, int endIndex) {
        this(parentSummary, startIndex, endIndex, null);
    }

    public SummaryBlock(PrismarineSummaryModule parentSummary, int startIndex, int endIndex, SummarySymbol associatedSymbol) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.parentSummary = parentSummary;
        this.associatedSymbol = associatedSymbol;
        if(associatedSymbol != null) associatedSymbol.setSubBlock(this);
    }

    @Override
    public String getName() {
        return associatedSymbol != null ? associatedSymbol.getName() : null;
    }

    @Override
    public void putElement(SummaryElement element) {
        clearEmptyBlocks();
        int i = subElements.size();
        while(i > 0) {
            if(element.getStartIndex() >= subElements.get(i-1).getStartIndex()) break;
            i--;
        }
        subElements.add(i, element);
    }

    @Override
    public String toString() {
        return (associatedSymbol != null ? getName() + ": " : "") + subElements.toString();
    }

    @Override
    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public int getEndIndex() {
        return endIndex;
    }

    void clearEmptyBlocks() {
        if(!subElements.isEmpty()) {
            SummaryElement last = subElements.get(subElements.size()-1);
            if(last instanceof SummaryBlock && ((SummaryBlock) last).isEmpty() && ((SummaryBlock) last).associatedSymbol == null) {
                subElements.remove(last);
            }
        }
    }

    public void surroundBlock(int start, int end) {
        surroundBlock(start, end, null);
    }

    public void surroundBlock(int start, int end, SummarySymbol associatedSymbol) {
        clearEmptyBlocks();
        SummaryBlock sub = null;
        int i = 0;
        for(; i < subElements.size(); i++) {
            SummaryElement elem = subElements.get(i);
            if(elem.getStartIndex() < start) continue;
            if(elem.getStartIndex() >= end) break;
            if(elem instanceof SummarySymbol && ((SummarySymbol) elem).isField()) {
                ((SummarySymbol) elem).setFieldScope(start, end);
            }
            if(sub == null) sub = new SummaryBlock(parentSummary, start, end, associatedSymbol);
            sub.putElement(elem);
            subElements.remove(i);
            i--;
        }
        if(sub == null) sub = new SummaryBlock(parentSummary, start, end, associatedSymbol);
        subElements.add(i, sub);
    }

    public void putLateElement(SummaryElement elem) {
        boolean inserted = false;
        for(SummaryElement sub : subElements) {
            if (sub instanceof SummaryBlock && sub.getStartIndex() <= elem.getStartIndex() && elem.getEndIndex() <= sub.getEndIndex()) {
                sub.putElement(elem);
                inserted = true;
                break;
            }
        }
        if(inserted) subElements.remove(elem);
    }

    @Override
    public void collectSymbolsVisibleAt(int index, ArrayList<SummarySymbol> list, Path fromPath) {
        if(associatedSymbol != null) associatedSymbol.collectSymbolsVisibleAt(index, list, fromPath);
        if(subElements.isEmpty()) return;

        for(SummaryElement elem : subElements) {
            if((index < 0 && fixed) || (startIndex <= index && index <= endIndex) || (elem instanceof SummarySymbol && ((SummarySymbol) elem).getDeclarationPattern().getStringLocation().index == index)) {
                elem.collectSymbolsVisibleAt(index, list, fromPath);
            }
        }
    }

    @Override
    public void collectGlobalSymbols(ArrayList<SummarySymbol> list) {
        if(associatedSymbol != null) associatedSymbol.collectGlobalSymbols(list);
        for(SummaryElement elem : subElements) {
            elem.collectGlobalSymbols(list);
        }
    }

    @Override
    public void updateIndices(Function<Integer, Integer> h) {
        if(!fixed) {
            startIndex = h.apply(startIndex);
            endIndex = h.apply(endIndex);
        }

        for(SummaryElement elem : subElements) {
            elem.updateIndices(h);
        }
    }

    public boolean isEmpty() {
        return subElements.isEmpty();
    }

    @Override
    public SummaryModule getParentFileSummary() {
        return parentSummary;
    }

    @Override
    public SymbolVisibility getVisibility() {
        return associatedSymbol != null ? associatedSymbol.getVisibility() : SymbolVisibility.GLOBAL;
    }

    void collectStaticSubSymbols(String name, Path fromFile, int inFileIndex, ArrayList<SummarySymbol> list) {
        for(SummaryElement element : subElements) {
            if(element instanceof SummarySymbol) {
                if(((SummarySymbol) element).isMemberOrStaticFieldAndVisible(name, fromFile, inFileIndex)) {
                    list.add((SummarySymbol) element);
                }
            }
        }
    }

    void collectInstanceSubSymbols(String name, Path fromFile, int inFileIndex, ArrayList<SummarySymbol> list) {
        for(SummaryElement element : subElements) {
            if(element instanceof SummarySymbol) {
                if(((SummarySymbol) element).isInstanceFieldAndVisible(name, fromFile, inFileIndex)) {
                    list.add((SummarySymbol) element);
                }
            }
        }
    }
}
