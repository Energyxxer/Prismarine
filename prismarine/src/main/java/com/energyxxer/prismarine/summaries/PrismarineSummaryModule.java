package com.energyxxer.prismarine.summaries;

import com.energyxxer.enxlex.lexical_analysis.Lexer;
import com.energyxxer.enxlex.lexical_analysis.summary.SummaryModule;
import com.energyxxer.enxlex.lexical_analysis.summary.Todo;
import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.lexical_analysis.token.TokenSource;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.util.SortedList;
import com.energyxxer.util.StringBounds;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class PrismarineSummaryModule extends SummaryModule {
    protected WeakReference<PrismarineProjectSummary> parentSummary;
    protected Path fileLocation = null;
    protected SummaryBlock fileBlock = new SummaryBlock(this);
    protected ArrayList<Path> requires = new ArrayList<>();
    protected SortedList<Todo> todos = new SortedList<>((todo -> todo.getToken().index));
    protected StringBuilder documentation = new StringBuilder();
    protected int documentationEndIndex;
    protected HashMap<String, Object> tempMap;

    public SortedList<FileAwareProcessor> fileAwareProcessors = new SortedList<>(p -> p.priority);

    private boolean searchingSymbols = false;

    private final Stack<SummaryBlock> contextStack = new Stack<>();

    private final Stack<SummarySymbol> subSymbolStack = new Stack<>();

    private final SortedList<SymbolUsage> symbolUsages = new SortedList<>(u -> u.index);

    public PrismarineSummaryModule(PrismarineProjectSummary parentSummary) {
        this.parentSummary = new WeakReference<>(parentSummary);
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
        //First search symbol stack
        for(int i = subSymbolStack.size()-1; i >= 0; i--) {
            SummarySymbol element = subSymbolStack.get(i);
            if(element != null && element.getName().equals(name)) {
                return element;
            }
        }
        //If not found, search the rest.
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
        if(parentSummary.get() != null) {
            for(SummarySymbol globalSymbol : parentSummary.get().getGlobalSymbols()) {
                if(globalSymbol.getParentFileSummary() == null || !Objects.equals(globalSymbol.getParentFileSummary().getFileLocation(), this.getFileLocation())) {
                    list.add(globalSymbol);
                }
            }
        }
        collectSymbolsVisibleAt(index, list, fileLocation);
        return list;
    }

    private ArrayList<SummarySymbol> searchingSymbolsResult = new ArrayList<>();

    public ArrayList<SummarySymbol> collectSymbolsVisibleAt(int index, ArrayList<SummarySymbol> list, Path fromPath) {
        if(searchingSymbols) {
            list.addAll(searchingSymbolsResult);
            return list;
        }
        searchingSymbols = true;
        try {
            if(parentSummary.get() != null) {
                for(Path required : requires) {
                    PrismarineSummaryModule superFile = parentSummary.get().getSummaryForLocation(required);
                    if(superFile != null) {
                        superFile.collectSymbolsVisibleAt(-1, searchingSymbolsResult, fromPath);
                    }
                }
            }
            collectExternalSymbolsVisibleToFile(searchingSymbolsResult, fromPath);
            fileBlock.collectSymbolsVisibleAt(index, searchingSymbolsResult, fromPath, this);
        } finally {
            searchingSymbols = false;
            list.addAll(searchingSymbolsResult);
            searchingSymbolsResult.clear();
        }
        return list;
    }

    public ArrayList<SummarySymbol> collectExternalSymbolsVisibleToFile(ArrayList<SummarySymbol> list, Path fromPath) {
        return list;
    }

    public Collection<SummarySymbol> getGlobalSymbols() {
        ArrayList<SummarySymbol> list = new ArrayList<>();
        collectGlobalSymbols(list);
        return list;
    }

    public ArrayList<SummarySymbol> collectGlobalSymbols(ArrayList<SummarySymbol> list) {
        fileBlock.collectGlobalSymbols(list);
        return list;
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
        return parentSummary.get();
    }

    public void setParentSummary(PrismarineProjectSummary parentSummary) {
        this.parentSummary = new WeakReference<>(parentSummary);
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

    public Stack<SummarySymbol> getSubSymbolStack() {
        return subSymbolStack;
    }

    public void addSymbolUsage(TokenPattern<?> pattern) {
        addSymbolUsage(new SymbolUsage(pattern, pattern.flatten(false)));
    }

    public void addSymbolUsage(SymbolUsage usage) {
        SymbolUsage existingUsage = symbolUsages.getByKey(usage.index);
        if(existingUsage != null && existingUsage.symbolName.equals(usage.symbolName)) {
            existingUsage.tokenSource = usage.tokenSource;
            existingUsage.bounds = usage.bounds; //replace old usage with new info
            return;
        }
        symbolUsages.add(usage);
    }

    public SymbolUsage getSymbolUsageAtIndex(int index) {
        int foundIndex = symbolUsages.findIndexForKey(index);
        if(foundIndex < 0) return null;

        while(foundIndex >= 0 && foundIndex < symbolUsages.size() && symbolUsages.get(foundIndex).index >= index) {
            foundIndex--;
        }
        if(foundIndex < symbolUsages.size()-1) {
            foundIndex++;
            return symbolUsages.get(foundIndex);
        }
        return null;
    }

    public List<SymbolUsage> getSymbolUsages() {
        return symbolUsages;
    }

    @Override
    public void onEnd(Lexer lexer) {
        super.onEnd(lexer);
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

    public boolean isDocumentationAvailable() {
        return documentation.length() > 0;
    }

    public int getDocumentationEndIndex() {
        return documentationEndIndex;
    }

    public void setDocumentationEndIndex(int documentationEndIndex) {
        this.documentationEndIndex = documentationEndIndex;
    }

    public void appendDocumentation(String line) {
        if(documentation.length() > 0) {
            documentation.append('\n');
        }
        documentation.append(line);
    }

    public void clearDocumentation() {
        documentation.setLength(0);
    }

    public String getDocumentation() {
        return documentation.toString();
    }

    public SummaryBlock getFileBlock() {
        return fileBlock;
    }

    public void setIndex(String key, int index) {
        if(tempMap == null) tempMap = new HashMap<>();
        tempMap.put(key, index);
    }

    public int getIndex(String key) {
        return (int) tempMap.get(key);
    }

    public Integer tryGetIndex(String key) {
        if(tempMap == null) return null;
        return (Integer) tempMap.get(key);
    }

    public int tryGetIndex(String key, int defaultValue) {
        if(tempMap == null) return defaultValue;
        Object existing = tempMap.get(key);
        if(existing instanceof Integer) return (int)(Integer)existing;
        return defaultValue;
    }

    public void set(String key, Object value) {
        if(tempMap == null) tempMap = new HashMap<>();
        tempMap.put(key, value);
    }

    public Object get(String key) {
        if(tempMap == null) return null;
        return tempMap.get(key);
    }

    public static class SymbolUsage {
        public static final BiFunction<PrismarineSummaryModule, SymbolUsage, SummarySymbol> ROOT_SYMBOL_GETTER = (fs, usage) -> fs.getSymbolForName(usage.symbolName, usage.index);

        public final String symbolName;
        public TokenSource tokenSource;
        public StringBounds bounds;
        public int index;
        public BiFunction<PrismarineSummaryModule, SymbolUsage, SummarySymbol> symbolGetter = ROOT_SYMBOL_GETTER;

        public SymbolUsage(TokenPattern<?> pattern, String symbolName) {
            this.symbolName = symbolName;
            this.tokenSource = pattern.getSource();
            this.bounds = pattern.getStringBounds();
            index = pattern.getStringLocation().index;
        }

        public SummarySymbol fetchSymbol(PrismarineSummaryModule fileSummary) {
            return symbolGetter.apply(fileSummary, this);
        }
    }

    public void addFileAwareProcessor(int priority, Consumer<PrismarineSummaryModule> r) {
        fileAwareProcessors.add(new FileAwareProcessor(priority, r));
    }

    public boolean runFileAwareProcessors(int pass) {
        boolean any = !fileAwareProcessors.isEmpty();
        for(int i = 0; i < fileAwareProcessors.size();) {
            FileAwareProcessor processor = fileAwareProcessors.get(i);

            if(processor.priority > pass) break;
            processor.getConsumer().accept(this);
            fileAwareProcessors.remove(0);
        }
        return any;
    }

    public static class FileAwareProcessor {
        private final int priority;
        private final Consumer<PrismarineSummaryModule> consumer;

        public FileAwareProcessor(int priority, Consumer<PrismarineSummaryModule> consumer) {
            this.priority = priority;
            this.consumer = consumer;
        }

        public int getPriority() {
            return priority;
        }

        public Consumer<PrismarineSummaryModule> getConsumer() {
            return consumer;
        }
    }
}
