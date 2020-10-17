package com.energyxxer.prismarine.summaries;

import com.energyxxer.enxlex.lexical_analysis.summary.SummaryModule;
import com.energyxxer.enxlex.lexical_analysis.summary.Todo;
import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.util.SortedList;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

public class PrismarineSummaryModule extends SummaryModule {
    protected PrismarineProjectSummary parentSummary;
    protected Path fileLocation = null;
    protected SummaryBlock fileBlock = new SummaryBlock(this);
    protected ArrayList<Path> requires = new ArrayList<>();
    protected ArrayList<Todo> todos = new ArrayList<>();

    private boolean searchingSymbols = false;

    private Stack<SummaryBlock> contextStack = new Stack<>();

    private Stack<SummarySymbol> subSymbolStack = new Stack<>();

    private SortedList<SymbolUsage> symbolUsages = new SortedList<>(u -> u.index);

    public PrismarineSummaryModule() {
        this(null);
    }

    public PrismarineSummaryModule(PrismarineProjectSummary parentSummary) {
        this.parentSummary = parentSummary;
        contextStack.push(fileBlock);
    }

    public void addElement(SummaryElement element) {
        contextStack.peek().putElement(element);
    }

    public void push(SummaryBlock block) {
        contextStack.push(block);
    }

    public SummaryBlock pop() {
        return contextStack.pop();
    }

    public SummaryBlock peek() {
        return contextStack.peek();
    }

    public void addRequires(Path loc) {
        requires.add(loc);
    }

    public List<Path> getRequires() {
        return requires;
    }

    public ArrayList<Todo> getTodos() {
        return todos;
    }

    public SummarySymbol getSymbolForName(String name, int index) {
        Collection<SummarySymbol> rootSymbols = getSymbolsVisibleAt(index);
        for(SummarySymbol sym : rootSymbols) {
            if(sym.getName().equals(name)) {
                return sym;
            }
        }
        return null;
    }

    public Collection<SummarySymbol> getSymbolsVisibleAt(int index) {
        ArrayList<SummarySymbol> list = new ArrayList<>();
        if(parentSummary != null) {
            for(SummarySymbol globalSymbol : parentSummary.getGlobalSymbols()) {
                if(!Objects.equals(((PrismarineSummaryModule) globalSymbol.getParentFileSummary()).getFileLocation(), this.getFileLocation())) {
                    list.add(globalSymbol);
                }
            }
        }
        collectSymbolsVisibleAt(index, list, fileLocation);
        return list;
    }

    public void collectSymbolsVisibleAt(int index, ArrayList<SummarySymbol> list, Path fromPath) {
        if(searchingSymbols) return;
        searchingSymbols = true;
        if(parentSummary != null) {
            for(Path required : requires) {
                PrismarineSummaryModule superFile = parentSummary.getSummaryForLocation(required);
                if(superFile != null) {
                    superFile.collectSymbolsVisibleAt(-1, list, fromPath);
                }
            }
        }
        fileBlock.collectSymbolsVisibleAt(index, list, fromPath);
        searchingSymbols = false;
    }

    public Collection<SummarySymbol> getGlobalSymbols() {
        ArrayList<SummarySymbol> list = new ArrayList<>();
        collectGlobalSymbols(list);
        return list;
    }

    public void collectGlobalSymbols(ArrayList<SummarySymbol> list) {
        fileBlock.collectGlobalSymbols(list);
    }

    public void updateIndices(Function<Integer, Integer> h) {
        fileBlock.updateIndices(h);
    }

    public void setFileLocation(Path location) {
        this.fileLocation = location;
    }

    public Path getFileLocation() {
        return fileLocation;
    }

    public PrismarineProjectSummary getParentSummary() {
        return parentSummary;
    }

    public void setParentSummary(PrismarineProjectSummary parentSummary) {
        this.parentSummary = parentSummary;
    }

    public boolean isSubSymbolStackEmpty() {
        return subSymbolStack.isEmpty();
    }

    public SummarySymbol peekSubSymbol() {
        return subSymbolStack.peek();
    }

    public SummarySymbol popSubSymbol() {
        return subSymbolStack.pop();
    }

    public void pushSubSymbol(SummarySymbol sym) {
        subSymbolStack.push(sym);
    }

    public void addSymbolUsage(TokenPattern<?> pattern) {
        addSymbolUsage(new SymbolUsage(pattern, pattern.flatten(false)));
    }

    public void addSymbolUsage(SymbolUsage usage) {
        SymbolUsage existingUsage = symbolUsages.getByKey(usage.index);
        if(existingUsage != null && existingUsage.symbolName.equals(usage.symbolName)) {
            existingUsage.pattern = usage.pattern; //replace old usage, with new info
            return;
        }
        symbolUsages.add(usage);
    }

    public List<SymbolUsage> getSymbolUsages() {
        return symbolUsages;
    }

    @Override
    public void onEnd() {
        super.onEnd();
        fileBlock.clearEmptyBlocks();
    }

    @Override
    public String toString() {
        return "File Summary for " + fileLocation + ": \n" +
                "    Requires: " + requires + "\n" +
                "    Scopes: " + fileBlock.toString() + "\n";
    }

    public void addTodo(Token token, String text) {
        todos.add(new Todo(token, text));
    }

    public static class SymbolUsage {
        public final String symbolName;
        public TokenPattern<?> pattern;
        public int index;

        public SymbolUsage(TokenPattern<?> pattern, String symbolName) {
            this.symbolName = symbolName;
            this.pattern = pattern;
            index = pattern.getStringLocation().index;
        }
    }
}
