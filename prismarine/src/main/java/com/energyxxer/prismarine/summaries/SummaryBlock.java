package com.energyxxer.prismarine.summaries;

import com.energyxxer.prismarine.symbols.SymbolVisibility;
import com.energyxxer.util.SimpleReadArrayList;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Predicate;

public class SummaryBlock implements SummaryElement {
    private boolean fixed = false;
    private SummarySymbol associatedSymbol = null;
    private final PrismarineSummaryModule parentSummary;
    private int startIndex;
    private int endIndex;
    private final ArrayList<SummaryElement> subElements = new SimpleReadArrayList<>();
    private ArrayList<SummarySymbol> fallbackSymbols = null;
    @NotNull
    private RepeatPolicy repeatPolicy = RepeatPolicy.DUPLICATE;

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
        if(repeatPolicy != RepeatPolicy.DUPLICATE) {
            SummaryElement existing = getElementByName(element.getName());
            if(existing != null) {
                if (repeatPolicy == RepeatPolicy.REPLACE) {
                    subElements.remove(existing);
                } else {
                    return; //keep
                }
            }
        }

        int i = subElements.size();
        while(i > 0) {
            if(element.getStartIndex() >= subElements.get(i-1).getStartIndex()) break;
            i--;
        }
        subElements.add(i, element);
    }

    public void addFallbackSymbol(SummarySymbol symbol) {
        if(fallbackSymbols == null) fallbackSymbols = new ArrayList<>();
        fallbackSymbols.add(symbol);
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
        SummaryBlock sub = associatedSymbol != null ? associatedSymbol.getSubBlock() : null;
        if(sub != null) {
            sub.startIndex = start;
            sub.endIndex = end;
        }
        int i = 0;
        for(; i < subElements.size(); i++) {
            SummaryElement elem = subElements.get(i);
            if(elem.getStartIndex() < start) continue;
            if(elem.getStartIndex() >= end) break;
            if(elem instanceof SummarySymbol) {
                ((SummarySymbol) elem).setScope(start, end);
            }
            if(sub == null) sub = new SummaryBlock(parentSummary, start, end, associatedSymbol);
            sub.putElement(elem);
            subElements.remove(i);
            i--;
        }
        if(sub == null) sub = new SummaryBlock(parentSummary, start, end, associatedSymbol);
        if(!subElements.contains(sub)) subElements.add(i, sub);
    }

    public void rescopeElements() {
        for(SummaryElement element : subElements) {
            if(element instanceof SummarySymbol) {
                ((SummarySymbol) element).setScope(startIndex, endIndex);
            }
        }
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

        for(SummaryElement elem : subElements) {
            if((index < 0 && fixed) || (startIndex <= index && index <= endIndex) || (elem instanceof SummarySymbol && ((SummarySymbol) elem).getDeclarationPattern().getStringLocation().index == index)) {
                elem.collectSymbolsVisibleAt(index, list, fromPath);
            }
        }

        if(fallbackSymbols != null && ((index < 0 && fixed) || (startIndex <= index && index <= endIndex)) && !checkingFallbackSymbol) {
            this.checkingFallbackSymbol = true;
            try {
                for (SummarySymbol fallbackSymbol : fallbackSymbols) {
                    if (fallbackSymbol.hasSubBlock()) {
                        fallbackSymbol.getSubBlock().collectSymbolsFromOutside(index, list, fromPath);
                    }
                }
            } finally {
                this.checkingFallbackSymbol = false;
            }
        }
    }
    private boolean checkingFallbackSymbol = false;

    private void collectSymbolsFromOutside(int index, ArrayList<SummarySymbol> list, Path fromPath) {
        for(SummaryElement elem : subElements) {
            elem.collectSymbolsVisibleAt(index, list, fromPath);
        }

        if(fallbackSymbols != null && !checkingFallbackSymbol) {
            this.checkingFallbackSymbol = true;
            try {
                for (SummarySymbol fallbackSymbol : fallbackSymbols) {
                    if (fallbackSymbol.hasSubBlock()) {
                        fallbackSymbol.getSubBlock().collectSymbolsFromOutside(index, list, fromPath);
                    }
                }
            } finally {
                this.checkingFallbackSymbol = false;
            }
        }
    }

    public SummaryElement getElementByName(String name) {
        for(SummaryElement elem : subElements) {
            if(elem.getName().equals(name)) return elem;
        }
        return null;
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
    public PrismarineSummaryModule getParentFileSummary() {
        return parentSummary;
    }

    @Override
    public SymbolVisibility getVisibility() {
        return associatedSymbol != null ? associatedSymbol.getVisibility() : SymbolVisibility.PUBLIC;
    }

    public void collectStaticSubSymbols(String name, Path fromFile, int inFileIndex, ArrayList<SummarySymbol> list) {
        for(SummaryElement element : subElements) {
            if(element instanceof SummarySymbol) {
                if(
                        (name == null || name.equals(element.getName()))
                                && !((SummarySymbol) element).isInstanceField()
                                && ((SummarySymbol) element).isVisibleMember(fromFile, inFileIndex)
                ) {
                    list.add((SummarySymbol) element);
                }
            }
        }

//        if(fallbackSymbols != null) { TODO
//            for(SummarySymbol fallbackSymbol : fallbackSymbols) {
//                fallbackSymbol.collect(name, fromFile, inFileIndex, list);
//            }
//        }
    }

    public void collectInstanceSubSymbols(String name, Path fromFile, int inFileIndex, ArrayList<SummarySymbol> list) {
        for(SummaryElement element : subElements) {
            if(element instanceof SummarySymbol) {
                if(
                        (name == null || name.equals(element.getName()))
                                && ((SummarySymbol) element).isInstanceField()
                                && ((SummarySymbol) element).isVisibleMember(fromFile, inFileIndex)
                ) {
                    list.add((SummarySymbol) element);
                }
            }
        }

        if(fallbackSymbols != null && !checkingFallbackSymbol) {
            this.checkingFallbackSymbol = true;
            try {
                for(SummarySymbol fallbackSymbol : fallbackSymbols) {
                    fallbackSymbol.collectInstanceSubSymbols(name, fromFile, inFileIndex, list);
                }
            } finally {
                this.checkingFallbackSymbol = false;
            }
        }
    }

    @NotNull
    public RepeatPolicy getRepeatPolicy() {
        return repeatPolicy;
    }

    public void setRepeatPolicy(@NotNull RepeatPolicy repeatPolicy) {
        this.repeatPolicy = repeatPolicy;
    }

    public void removeIf(Predicate<SummaryElement> filter) {
        subElements.removeIf(filter);
    }

    public SummarySymbol getAssociatedSymbol() {
        return associatedSymbol;
    }

    public enum RepeatPolicy {
        KEEP, DUPLICATE, REPLACE
    }
}
