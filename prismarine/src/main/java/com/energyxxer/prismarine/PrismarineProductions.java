package com.energyxxer.prismarine;

import com.energyxxer.enxlex.lexical_analysis.Lexer;
import com.energyxxer.enxlex.lexical_analysis.token.TokenType;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.pattern_matching.matching.lazy.*;
import com.energyxxer.enxlex.pattern_matching.structures.TokenGroup;
import com.energyxxer.enxlex.pattern_matching.structures.TokenList;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.enxlex.report.Notice;
import com.energyxxer.enxlex.report.NoticeType;
import com.energyxxer.enxlex.suggestions.SuggestionTags;
import com.energyxxer.prismarine.plugins.PrismarinePluginUnit;
import com.energyxxer.prismarine.providers.PatternProviderSet;
import com.energyxxer.prismarine.summaries.PrismarineSummaryModule;
import com.energyxxer.prismarine.summaries.SummarySymbol;
import com.energyxxer.prismarine.worker.PrismarineProjectWorker;
import com.energyxxer.util.StringBounds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Function;

@SuppressWarnings("FieldCanBeLocal")
public class PrismarineProductions {
    private final HashMap<Class, PatternProviderSet> installedProviderSets = new HashMap<>();

    private final HashMap<String, TokenStructureMatch> providedStructures = new HashMap<>();
    private final HashMap<String, TokenPatternMatch> providedSingleMatches = new HashMap<>();

    public final TokenStructureMatch FILE;

    public final PrismarineLanguageUnitConfiguration unitConfig;
    public final PrismarineProjectWorker worker;

    public PrismarineProductions(PrismarineProjectWorker worker, PrismarineLanguageUnitConfiguration unitConfig) {
        this.worker = worker;
        this.unitConfig = unitConfig;

        FILE = getOrCreateStructure("FILE");
    }

    public static TokenItemMatch matchItem(TokenType type, String text) {
        return new TokenItemMatch(type, text).setName("ITEM_MATCH");
    }

    public static TokenItemMatch literal(String text) {
        return (TokenItemMatch) new TokenItemMatch(TokenType.UNKNOWN, text).setName("LITERAL_" + text.toUpperCase()).addTags(SuggestionTags.ENABLED);
    }

    public static TokenItemMatch ofType(TokenType type) {
        return new TokenItemMatch(type);
    }

    public static TokenStructureMatch struct(String name) {
        return new TokenStructureMatch(name);
    }

    public static TokenSwitchMatch tokenSwitch(String name, TokenType switchType) {
        return new TokenSwitchMatch(name, ofType(switchType));
    }

    public static TokenStructureMatch choice(TokenPatternMatch... options) {
        if(options.length == 0) throw new IllegalArgumentException("Need one or more options for choice");
        TokenStructureMatch s = struct("CHOICE");
        for(TokenPatternMatch option : options) {
            if(option != null) s.add(option);
        }
        return s;
    }

    public static TokenStructureMatch choice(String... options) {
        if(options.length == 0) throw new IllegalArgumentException("Need one or more options for choice");
        TokenStructureMatch s = struct("CHOICE");
        for(String option : options) {
            s.add(literal(option));
        }
        return s;
    }

    public static TokenGroupMatch optional() {
        return new TokenGroupMatch(true);
    }

    public static TokenGroupMatch wrapper(TokenPatternMatch inner, PostValidationPatternEvaluator evaluator) {
        return (TokenGroupMatch) group(inner).setEvaluator((p, d) -> {
            Object result = ((TokenGroup) p).getContents()[0].evaluate(d);
            return evaluator.apply(result, p, d);
        });
    }

    public static TokenGroupMatch wrapper(TokenPatternMatch inner, Function<Object[], Object[]> dataTransformer, PostValidationPatternEvaluator evaluator) {
        return (TokenGroupMatch) group(inner).setEvaluator((p, d) -> {
            Object result = ((TokenGroup) p).getContents()[0].evaluate(dataTransformer.apply(d));
            return evaluator.apply(result, p, d);
        });
    }

    public static TokenGroupMatch wrapper(TokenPatternMatch inner) {
        return (TokenGroupMatch) group(inner).setSimplificationFunctionContentIndex(0);
    }

    public static TokenGroupMatch wrapperOptional(TokenPatternMatch inner) {
        return (TokenGroupMatch) group(inner).setSimplificationFunctionContentIndex(0).setOptional();
    }

    public static TokenGroupMatch group(TokenPatternMatch... items) {
        TokenGroupMatch g = new TokenGroupMatch();
        for(TokenPatternMatch item : items) {
            if(item != null) g.append(item);
        }
        return g;
    }

    public static TokenListMatch list(TokenPatternMatch pattern) {
        return list(pattern, null);
    }

    public static TokenListMatch list(TokenPatternMatch pattern, TokenPatternMatch separator) {
        return new TokenListMatch(pattern, separator);
    }

    public static TokenGroupMatch optional(TokenPatternMatch... items) {
        TokenGroupMatch g = group(items);
        g.setOptional();
        return g;
    }

    public static <T extends Enum> TokenStructureMatch enumChoice(Class<T> enumClass) {
        TokenStructureMatch choice = struct("CHOICE");
        for(Object enumConstant : enumClass.getEnumConstants()) {
            String lit = ((T) enumConstant).name().toLowerCase(Locale.ENGLISH);
            if(lit.startsWith("_")) lit = lit.substring(1);
            choice.add(literal(lit).setEvaluator((p, d) -> enumConstant));
        }
        return choice;
    }

    public static <T extends Enum> TokenStructureMatch enumChoice(T... enumConstants) {
        TokenStructureMatch choice = struct("CHOICE");
        for(T enumConstant : enumConstants) {
            String lit = enumConstant.name().toLowerCase(Locale.ENGLISH);
            if(lit.startsWith("_")) lit = lit.substring(1);
            choice.add(literal(lit).setEvaluator((p, d) -> enumConstant));
        }
        return choice;
    }

    public static void checkDuplicates(TokenList list, String message, Lexer lx) {
        if(lx == null) return;
        duplicateCheck.clear();
        for(TokenPattern<?> entry : list.getContents()) {
            if(entry.getName().equals("COMMA")) continue;
            if(!duplicateCheck.add(entry.flatten(false))) {
                lx.getNotices().add(new Notice(NoticeType.ERROR, message + " '" + entry.flatten(false) + "'", entry));
            }
        }
        duplicateCheck.clear();
    }

    public void installProviderSet(PatternProviderSet providerSet) {
        providerSet.install(this);
        installedProviderSets.put(providerSet.getClass(), providerSet);
    }

    public PrismarineProjectWorker getWorker() {
        return worker;
    }

    public <T extends PatternProviderSet> T getProviderSet(Class<T> cls) {
        return ((T) installedProviderSets.get(cls));
    }

    private ArrayList<PrismarinePluginUnit> pluginUnits;

    public void registerPluginUnit(PrismarinePluginUnit unit) {
        if(pluginUnits == null) pluginUnits = new ArrayList<>();
        pluginUnits.add(unit);
    }

    public ArrayList<PrismarinePluginUnit> getPluginUnits() {
        return pluginUnits;
    }

    public TokenStructureMatch getOrCreateStructure(String name) {
        if(!providedStructures.containsKey(name)) {
            providedStructures.put(name, struct(name));
        }
        return providedStructures.get(name);
    }

    public TokenPatternMatch putPatternMatch(String name, TokenPatternMatch match) {
        providedSingleMatches.put(name, match);
        return match;
    }

    public TokenPatternMatch getPatternMatch(String name) {
        return providedSingleMatches.get(name);
    }

    public void addFileGroup(TokenGroupMatch group) {
        group.append(ofType(TokenType.END_OF_FILE));
        FILE.add(group);
    }

    public interface PostValidationPatternEvaluator {
        Object apply(Object result, TokenPattern<?> pattern, Object... data);
    }


    private static final HashSet<String> duplicateCheck = new HashSet<>();

    public static final BiConsumer<TokenPattern<?>, Lexer> startComplexValue = (p, l) -> {
        if(l.getSummaryModule() != null) {
            PrismarineSummaryModule summaryModule = ((PrismarineSummaryModule) l.getSummaryModule());
            SummarySymbol topSym = null;
            if(!summaryModule.isSubSymbolStackEmpty()) {
                topSym = summaryModule.peekSubSymbol();
            }
            summaryModule.pushSubSymbol(topSym);
        }
    };

    public static final BiConsumer<TokenPattern<?>, Lexer> startClosure = (p, l) -> {
        if(l.getSummaryModule() != null) {
            PrismarineSummaryModule summaryModule = ((PrismarineSummaryModule) l.getSummaryModule());
            summaryModule.pushSubSymbol(null);
        }
    };

    public static final BiConsumer<TokenPattern<?>, Lexer> endComplexValue = (p, l) -> {
        if(l.getSummaryModule() != null) {
            PrismarineSummaryModule summaryModule = ((PrismarineSummaryModule) l.getSummaryModule());
            summaryModule.popSubSymbol();
        }
    };

    public static final BiConsumer<TokenPattern<?>, Lexer> claimTopSymbol = (p, l) -> {
        if(l.getSummaryModule() != null) {
            PrismarineSummaryModule summaryModule = ((PrismarineSummaryModule) l.getSummaryModule());
            SummarySymbol topSym = null;
            if(!summaryModule.isSubSymbolStackEmpty()) {
                topSym = summaryModule.peekSubSymbol();
            }
            if(topSym != null) {
                summaryModule.peek().surroundBlock(p.getStringBounds().start.index, p.getStringBounds().end.index, topSym);
            }
        }
    };

    public static final BiConsumer<TokenPattern<?>, Lexer> surroundBlock = (p, lx) -> {
        if(lx.getSummaryModule() != null) {
            StringBounds bounds = p.getStringBounds();
            if(bounds != null) {
                ((PrismarineSummaryModule) lx.getSummaryModule()).peek().surroundBlock(bounds.start.index, bounds.end.index);
            }
        }
    };
}
