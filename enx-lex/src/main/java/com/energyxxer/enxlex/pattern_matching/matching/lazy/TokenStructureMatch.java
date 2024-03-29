package com.energyxxer.enxlex.pattern_matching.matching.lazy;

import com.energyxxer.enxlex.lexical_analysis.Lexer;
import com.energyxxer.enxlex.pattern_matching.TokenMatchResponse;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.pattern_matching.structures.TokenStructure;

import java.util.ArrayList;

public class TokenStructureMatch extends TokenPatternMatch {
    private final ArrayList<TokenPatternMatch> entries = new ArrayList<>();
    private ThreadLocal<ArrayList<TokenPatternMatch>> dynamicEntries = null;
    /**
     * When greedy: false
     * The structure will always try to return a positive match, even if there are longer negative matches.
     *
     * When greedy: true
     * The structure will always return the longest match, regardless of whether it's positive or not.
     *
     * Use greedy structures whenever the first few tokens of the entries overlap, so error messages point to the
     * point where the match failed, rather than some point far before it failed.
     * */
    private boolean greedy = false;

    public TokenStructureMatch(String name) {
        this.name = name;
        optional = false;
    }

    public TokenStructureMatch(String name, boolean optional) {
        this.name = name;
        this.optional = optional;
    }

    @Override
    public TokenStructureMatch setName(String name) {
        super.setName(name);
        return this;
    }

    public TokenStructureMatch add(TokenPatternMatch g) {
        if(dynamicEntries != null && dynamicEntries.get() != entries) {
            if(!dynamicEntries.get().contains(g)) dynamicEntries.get().add(g);
        }
        if(!entries.contains(g)) entries.add(g);
        return this;
    }

    private void initializeDynamicEntries() {
        if(dynamicEntries == null) {
            dynamicEntries = ThreadLocal.withInitial(() -> entries);
        }
        if(dynamicEntries.get() == entries) {
            dynamicEntries.set(new ArrayList<>(entries));
        }
    }

    public TokenStructureMatch addDynamic(TokenPatternMatch g) {
        initializeDynamicEntries();
        if(!dynamicEntries.get().contains(g)) dynamicEntries.get().add(g);
        return this;
    }

    @Override
    public TokenMatchResponse match(int index, Lexer lexer) {
        lexer.setCurrentIndex(index);

        int popSuggestionStatus = handleSuggestionTags(lexer, index);

        TokenMatchResponse longestMatch = null;

        ArrayList<TokenPatternMatch> entries = dynamicEntries != null ? dynamicEntries.get() : this.entries;

        if(entries.isEmpty()) {
            //throw new IllegalStateException("Cannot attempt match; TokenStructureMatch '" + this.name + "' is empty.");
            invokeFailProcessors(null, lexer);
            return TokenMatchResponse.failure(null, 0, index, this, null);
        }
        for (TokenPatternMatch entry : entries) {
            lexer.setCurrentIndex(index);

            TokenMatchResponse itemMatch = entry.match(index, lexer);

            if (longestMatch == null) {
                longestMatch = itemMatch;
            } else if(itemMatch.length >= longestMatch.length) {
                if (!longestMatch.matched || itemMatch.matched || (greedy && itemMatch.length > longestMatch.length)) {
                    longestMatch.discard();
                    longestMatch = itemMatch;
                }
            } else {
                itemMatch.discard();
            }
        }

        while(--popSuggestionStatus >= 0) {
            lexer.getSuggestionModule().popStatus();
        }

        if (longestMatch == null || longestMatch.matched) {
            if(longestMatch != null) {
                TokenStructure struct = new TokenStructure(this.name, longestMatch.pattern, this).addTags(this.tags);
                invokeProcessors(struct, lexer);
                return TokenMatchResponse.success(longestMatch.length, longestMatch.endIndex, struct);
            } else {
                return TokenMatchResponse.success(0, index, null);
            }
        } else {
            invokeFailProcessors(longestMatch.pattern, lexer);
            if (longestMatch.length <= 0 && entries.size() > 1) {
                return TokenMatchResponse.failure(longestMatch.faultyToken, longestMatch.length, longestMatch.endIndex, this, null/*new TokenStructure(this.name, longestMatch.pattern).addTags(this.tags)*/);
            } else {
                return TokenMatchResponse.failure(longestMatch.faultyToken, longestMatch.length, longestMatch.endIndex, longestMatch.expected, null/*new TokenStructure(this.name, longestMatch.pattern).addTags(this.tags)*/);
            }
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

    public TokenStructureMatch exclude(TokenPatternMatch entryToExclude) {
        TokenStructureMatch newStruct = new TokenStructureMatch(name, optional);
        for(TokenPatternMatch entry : entries) {
            if(entry != entryToExclude) {
                newStruct.add(entry);
            }
        }
        return newStruct;
    }

    public TokenStructureMatch setGreedy(boolean greedy) {
        this.greedy = greedy;
        return this;
    }

    public boolean getGreedy() {
        return greedy;
    }

    public void remove(TokenPatternMatch pattern) {
        entries.remove(pattern);
    }

    public void removeDynamic(TokenPatternMatch pattern) {
        initializeDynamicEntries();
        dynamicEntries.get().remove(pattern);
    }
}
