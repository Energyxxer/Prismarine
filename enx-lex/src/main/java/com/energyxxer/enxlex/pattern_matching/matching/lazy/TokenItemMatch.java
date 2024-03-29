package com.energyxxer.enxlex.pattern_matching.matching.lazy;

import com.energyxxer.enxlex.lexical_analysis.Lexer;
import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.lexical_analysis.token.TokenType;
import com.energyxxer.enxlex.pattern_matching.TokenMatchResponse;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.pattern_matching.structures.TokenItem;
import com.energyxxer.enxlex.suggestions.ComplexSuggestion;
import com.energyxxer.enxlex.suggestions.LiteralSuggestion;

public class TokenItemMatch extends TokenPatternMatch {
    private final TokenType type;
    private String stringMatch = null;

    private boolean caseSensitive = true;

    public TokenItemMatch(TokenType type) {
        this.type = type;
        this.optional = false;
    }

    public TokenItemMatch(TokenType type, String stringMatch) {
        this.type = type;
        this.stringMatch = stringMatch;
        this.optional = false;
    }

    public TokenItemMatch(TokenType type, boolean optional) {
        this.type = type;
        this.optional = optional;
    }

    public TokenItemMatch(TokenType type, String stringMatch, boolean optional) {
        this.type = type;
        this.stringMatch = stringMatch;
        this.optional = optional;
    }

    @Override
    public TokenItemMatch setName(String name) {
        super.setName(name);
        return this;
    }

    @Override
    public TokenMatchResponse match(int index, Lexer lexer) {
        lexer.setCurrentIndex(index);

        int popSuggestionStatus = handleSuggestionTags(lexer, index);

        boolean matched;
        Token faultyToken = null;

        if(lexer.getSuggestionModule() != null && lexer.getSuggestionModule().shouldSuggest() && lexer.getSuggestionModule().isAtSuggestionIndex(index)) {
            if(this.stringMatch != null) {
                LiteralSuggestion suggestion = new LiteralSuggestion(this.stringMatch);
                if(this.tags != null) {
                    for(String tag : this.tags) {
                        if(tag.startsWith("cst:") || tag.startsWith("mst:")) {
                            suggestion.addTag(tag);
                        }
                    }
                }
                suggestion.setCaseSensitive(caseSensitive);
                lexer.getSuggestionModule().addSuggestion(suggestion);
            } else {
                lexer.getSuggestionModule().addSuggestion(new ComplexSuggestion(this.type.toString()));
            }
        }

        Token retrieved = lexer.retrieveTokenOfType(this.type);
        if(retrieved == null) {
            invokeFailProcessors(null, lexer);
            return TokenMatchResponse.failure(lexer.retrieveAnyToken(), 0, index, this, null);
        }

        matched = stringMatch == null || (caseSensitive ? retrieved.value.equals(stringMatch) : retrieved.value.equalsIgnoreCase(stringMatch));

        if (!matched) {
            faultyToken = retrieved;
        }

        int length = (matched) ? retrieved.value.length() : 0;

        TokenItem item = new TokenItem(retrieved, this).setName(this.name).addTags(this.tags);

        while(--popSuggestionStatus >= 0) {
            lexer.getSuggestionModule().popStatus();
        }
        if(matched) invokeProcessors(item, lexer);
        else invokeFailProcessors(null, lexer);
        if(matched) {
            return TokenMatchResponse.success(length, retrieved.endIndex(), item);
        } else {
            return TokenMatchResponse.failure(faultyToken, length, retrieved.endIndex(), this, item);
        }
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public TokenItemMatch setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }

    public TokenType getType() {
        return type;
    }

    @Override
    public String toString() {
        String s = "";
        if (optional) {
            s += "[";
        } else {
            s += "<";
        }
        s += type;
        if (stringMatch != null) {
            s += ":" + stringMatch;
        }
        if (optional) {
            s += "]";
        } else {
            s += ">";
        }
        return s;
    }

    @Override
    public String deepToString(int levels) {
        return toString();
    }

    @Override
    public String toTrimmedString() {
        return (stringMatch != null) ? "'" + stringMatch + "'" : type.getHumanReadableName();
    }
}
