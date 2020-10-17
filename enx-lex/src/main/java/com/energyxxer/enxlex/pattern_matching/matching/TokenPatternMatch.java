package com.energyxxer.enxlex.pattern_matching.matching;

import com.energyxxer.enxlex.lexical_analysis.Lexer;
import com.energyxxer.enxlex.pattern_matching.PatternEvaluator;
import com.energyxxer.enxlex.pattern_matching.TokenMatchResponse;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.enxlex.suggestions.ComplexSuggestion;
import com.energyxxer.enxlex.suggestions.SuggestionModule;
import com.energyxxer.enxlex.suggestions.SuggestionTags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class TokenPatternMatch {
    public String name = "";
    public boolean optional;
    public List<String> tags = new ArrayList<>();
    protected List<BiConsumer<TokenPattern<?>, Lexer>> processors = new ArrayList<>();
    protected List<BiConsumer<TokenPattern<?>, Lexer>> failProcessors = new ArrayList<>();

    private Consumer<TokenPattern.SimplificationDomain> simplificationFunction;
    protected PatternEvaluator evaluator;

    public TokenPatternMatch addTags(String... newTags) {
        tags.addAll(Arrays.asList(newTags));
        return this;
    }

    public TokenPatternMatch setName(String name) {
        this.name = name;
        return this;
    }

    public TokenPatternMatch addProcessor(BiConsumer<TokenPattern<?>, Lexer> processor) {
        processors.add(processor);
        return this;
    }

    public TokenPatternMatch addFailProcessor(BiConsumer<TokenPattern<?>, Lexer> failProcessor) {
        failProcessors.add(failProcessor);
        return this;
    }

    public abstract String deepToString(int levels);

    public abstract String toTrimmedString();

    protected void invokeProcessors(TokenPattern<?> pattern, Lexer lexer) {
        processors.forEach(p -> p.accept(pattern, lexer));
    }

    protected void invokeFailProcessors(TokenPattern<?> incompletePattern, Lexer lexer) {
        failProcessors.forEach(p -> p.accept(incompletePattern, lexer));
    }






    public abstract TokenMatchResponse match(int index, Lexer lexer);

    public TokenPatternMatch setOptional() {
        return setOptional(true);
    }

    public TokenPatternMatch setOptional(boolean optional) {
        this.optional = optional;
        return this;
    }

    protected int handleSuggestionTags(Lexer lexer, int index) {
        int popSuggestionStatus = 0;
        if(lexer.getSuggestionModule() != null) {

            if(tags.contains(SuggestionTags.ENABLED)) {
                lexer.getSuggestionModule().pushStatus(SuggestionModule.SuggestionStatus.ENABLED);
                popSuggestionStatus++;
            } else if(tags.contains(SuggestionTags.DISABLED)) {
                lexer.getSuggestionModule().pushStatus(SuggestionModule.SuggestionStatus.DISABLED);
                popSuggestionStatus++;
            }

            if(lexer.getSuggestionModule().isAtSuggestionIndex(index) && lexer.getSuggestionModule().getCaretIndex() == lexer.getSuggestionModule().getSuggestionIndex()) {
                if(tags.contains(SuggestionTags.ENABLED_INDEX)) {
                    lexer.getSuggestionModule().pushStatus(SuggestionModule.SuggestionStatus.ENABLED);
                    popSuggestionStatus++;
                } else if(tags.contains(SuggestionTags.DISABLED_INDEX)) {
                    lexer.getSuggestionModule().pushStatus(SuggestionModule.SuggestionStatus.DISABLED);
                    popSuggestionStatus++;
                }
            }

            if(lexer.getSuggestionModule().isAtSuggestionIndex(index)) {
                ComplexSuggestion complexSuggestion = null;
                for(String tag : tags) {
                    if((tag.startsWith("csk:") && lexer.getSuggestionModule().shouldSuggest()) || tag.startsWith("ctx:") || tag.startsWith("cspn:")) {
                        lexer.getSuggestionModule().addSuggestion(complexSuggestion = new ComplexSuggestion(tag));
                    } else if((tag.startsWith("cst:") || tag.startsWith("mst:")) && complexSuggestion != null && lexer.getSuggestionModule().shouldSuggest()) {
                        complexSuggestion.addTag(tag);
                    }
                }
            }
        }
        return popSuggestionStatus;
    }

    public PatternEvaluator getEvaluator() {
        return evaluator;
    }

    public TokenPatternMatch setEvaluator(PatternEvaluator evaluator) {
        this.evaluator = evaluator;
        return this;
    }

    public Consumer<TokenPattern.SimplificationDomain> getSimplificationFunction() {
        return simplificationFunction;
    }

    public TokenPatternMatch setSimplificationFunction(Consumer<TokenPattern.SimplificationDomain> simplificationFunction) {
        this.simplificationFunction = simplificationFunction;
        return this;
    }

    public TokenPatternMatch setSimplificationFunctionFind(String path) {
        return setSimplificationFunction((d) -> d.pattern = d.pattern.find(path));
    }
}
