package com.energyxxer.enxlex.pattern_matching.matching.lazy;

import com.energyxxer.enxlex.lexical_analysis.Lexer;
import com.energyxxer.enxlex.pattern_matching.TokenMatchResponse;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.pattern_matching.structures.TokenItem;
import com.energyxxer.enxlex.pattern_matching.structures.TokenSwitch;
import com.energyxxer.enxlex.suggestions.LiteralSuggestion;
import com.energyxxer.enxlex.suggestions.Suggestion;

import java.util.HashMap;

public class TokenSwitchMatch extends TokenPatternMatch {
    private final HashMap<String, TokenPatternMatch> entries = new HashMap<>();
    private final TokenItemMatch switchMatch;

    public TokenSwitchMatch(String name, TokenItemMatch switchMatch) {
        this.name = name;
        this.switchMatch = switchMatch;
        optional = false;
    }

    public TokenSwitchMatch(String name, TokenItemMatch switchMatch, boolean optional) {
        this.name = name;
        this.switchMatch = switchMatch;
        this.optional = optional;
    }

    @Override
    public TokenSwitchMatch setName(String name) {
        super.setName(name);
        return this;
    }

    public TokenSwitchMatch add(String key, TokenPatternMatch g) {
        entries.put(key, g);
        return this;
    }

    @Override
    public TokenMatchResponse match(int index, Lexer lexer) {
        lexer.setCurrentIndex(index);

        int popSuggestionStatus = handleSuggestionTags(lexer, index);

        TokenMatchResponse branchResponse = null;

        if(!entries.isEmpty()) {
            if(lexer.getSuggestionModule() != null && lexer.getSuggestionModule().shouldSuggest() && lexer.getSuggestionModule().isAtSuggestionIndex(index)) {
                for(String key : entries.keySet()) {
                    Suggestion suggestion = new LiteralSuggestion(key);
                    if(this.tags != null) {
                        for(String tag : this.tags) {
                            if(tag.startsWith("cst:") || tag.startsWith("mst:")) {
                                suggestion.addTag(tag);
                            }
                        }
                    }
                    lexer.getSuggestionModule().addSuggestion(suggestion);
                }
            }

            TokenMatchResponse headResponse = switchMatch.match(index, lexer);
            if(headResponse.matched) {
                String key = ((TokenItem) headResponse.pattern).getContents().value;
                headResponse.discard();

                TokenPatternMatch chosenBranch = entries.get(key);

                if(chosenBranch != null) {
                    branchResponse = chosenBranch.match(index, lexer);
                }
            } else {
                headResponse.discard();
            }
        }

        while(--popSuggestionStatus >= 0) {
            lexer.getSuggestionModule().popStatus();
        }

        if(branchResponse != null && branchResponse.matched) {
            TokenSwitch result = new TokenSwitch(this.name, branchResponse.pattern, this).addTags(this.tags);
            invokeProcessors(result, lexer);
            return TokenMatchResponse.success(branchResponse.length, branchResponse.endIndex, result);
        } else if(branchResponse != null && !switchMatch.isRecessive()) {
            invokeFailProcessors(branchResponse.pattern, lexer);
            return TokenMatchResponse.failure(branchResponse.faultyToken, branchResponse.length, branchResponse.endIndex, branchResponse.expected, null);
        } else {
            invokeFailProcessors(null, lexer);
            return TokenMatchResponse.failure(null, 0, index, this, null);
        }
    }

    @Override
    public String toString() {
        return ((optional) ? "[" : "<") + "-" + name + "-" + ((optional) ? "]" : ">");
    }

    public String deepToString(int levels) {
        if(levels <= 0) return toString();
        StringBuilder s = new StringBuilder(((optional) ? "[" : "<") + "-");
        for (int i = 0; i < entries.size(); i++) {
            s.append(entries.get(i).deepToString(levels - 1));
            if (i < entries.size() - 1) {
                s.append("\n OR ");
            }
        }
        return s + "-" + ((optional) ? "]" : ">");
    }

    @Override
    public String toTrimmedString() {
        String humanReadableName = name.toLowerCase().replace('_',' ');
        humanReadableName = humanReadableName.substring(0,1).toUpperCase() + humanReadableName.substring(1);
        return humanReadableName;
    }

    public void remove(String key) {
        entries.remove(key);
    }
}
