package com.energyxxer.enxlex.pattern_matching.matching.lazy;

import com.energyxxer.enxlex.lexical_analysis.Lexer;
import com.energyxxer.enxlex.pattern_matching.TokenMatchResponse;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;

public class TokenFailMatch extends TokenPatternMatch {
    private final TokenPatternMatch sub;

    public TokenFailMatch(String name, TokenPatternMatch sub) {
        this.name = name;
        this.sub = sub;
    }

    @Override
    public TokenFailMatch setName(String name) {
        super.setName(name);
        return this;
    }

    @Override
    public TokenMatchResponse match(int index, Lexer lexer) {
        lexer.setCurrentIndex(index);

        int popSuggestionStatus = handleSuggestionTags(lexer, index);

        TokenMatchResponse itemMatch = sub.match(index, lexer);
        //ignore result and just fail

        while(--popSuggestionStatus >= 0) {
            lexer.getSuggestionModule().popStatus();
        }

        invokeFailProcessors(itemMatch.pattern, lexer);
        return new TokenMatchResponse(false, null, 0, this, null);
    }

    @Override
    public String toString() {
        return "";
    }

    public String deepToString(int levels) {
        return "";
    }

    @Override
    public String toTrimmedString() {
        String humanReadableName = name.toLowerCase().replace('_',' ');
        humanReadableName = humanReadableName.substring(0,1).toUpperCase() + humanReadableName.substring(1);
        return humanReadableName;
    }
}
