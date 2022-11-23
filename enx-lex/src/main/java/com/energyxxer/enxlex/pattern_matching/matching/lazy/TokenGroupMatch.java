package com.energyxxer.enxlex.pattern_matching.matching.lazy;

import com.energyxxer.enxlex.lexical_analysis.Lexer;
import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.pattern_matching.TokenMatchResponse;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.pattern_matching.structures.TokenGroup;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;

import java.util.ArrayList;

import static com.energyxxer.enxlex.pattern_matching.TokenMatchResponse.*;

public class TokenGroupMatch extends TokenPatternMatch {
    public ArrayList<TokenPatternMatch> items;
    private boolean greedy = false;

    public TokenGroupMatch() {
        this.optional = false;
        items = new ArrayList<>();
    }

    public TokenGroupMatch(boolean optional) {
        this.optional = optional;
        items = new ArrayList<>();
    }

    @Override
    public TokenGroupMatch setName(String name) {
        super.setName(name);
        return this;
    }

    public TokenGroupMatch append(TokenPatternMatch i) {
        items.add(i);
        return this;
    }

    @Override
    public TokenMatchResponse match(int index, Lexer lexer) {
        lexer.setCurrentIndex(index);
        if(items.size() == 0) return TokenMatchResponse.success(0, index, new TokenGroup(this, new TokenPattern<?>[0]));

        int popSuggestionStatus = handleSuggestionTags(lexer, index);

        ArrayList<TokenPattern<?>> contents = TokenPattern.PATTERN_LIST_POOL.get().claim();
        try {

            int currentIndex = index;
            boolean hasMatched = true;
            Token faultyToken = null;
            int length = 0;
            int endIndex = index;
            TokenPatternMatch expected = null;

            int longestFailedMatchLength = -1;
            TokenMatchResponse longestFailedMatch = null;

            boolean[] itemsMatched = new boolean[items.size()];
            boolean anyItemsMatched = false;

            itemLoop: for (int i = 0; i < items.size(); i++) {

                if (currentIndex > lexer.getFileLength() && !items.get(i).optional) {
                    hasMatched = false;
                    expected = items.get(i);
                    break;
                }

                TokenMatchResponse itemMatch = items.get(i).match(currentIndex, lexer);
                switch(itemMatch.getMatchType()) {
                    case NO_MATCH: {
                        if(!items.get(i).optional) {
                            hasMatched = false;
                            faultyToken = itemMatch.faultyToken;
                            expected = itemMatch.expected;
                            itemMatch.discard();
                            break itemLoop;
                        }
                        break;
                    }
                    case PARTIAL_MATCH: {
                        int totalLengthUpToNow = length + itemMatch.length;
                        if(totalLengthUpToNow > longestFailedMatchLength) {
                            longestFailedMatch = itemMatch;
                            longestFailedMatchLength = totalLengthUpToNow;
                        }
                        if(!(items.get(i).optional && i+1 < items.size() && items.get(i+1).match(currentIndex, lexer).matchedThenDiscard())) {
                            hasMatched = false;
                            length += itemMatch.length;
                            endIndex = Math.max(endIndex, itemMatch.endIndex);
                            faultyToken = itemMatch.faultyToken;
                            expected = itemMatch.expected;
                            itemMatch.discard();
                            break itemLoop;
                        } else {
                            break;
                        }
                    }
                    case COMPLETE_MATCH: {
                        if(itemMatch.pattern != null) contents.add(itemMatch.pattern);
                        currentIndex = itemMatch.endIndex;
                        length += itemMatch.length;
                        endIndex = Math.max(endIndex, itemMatch.endIndex);
                        itemsMatched[i] = true;
                        anyItemsMatched = true;
                    }
                }
                itemMatch.discard();
            }

            while(--popSuggestionStatus >= 0) {
                lexer.getSuggestionModule().popStatus();
            }
            if(greedy && !hasMatched && longestFailedMatch != null) {
                faultyToken = longestFailedMatch.faultyToken;
                length = longestFailedMatchLength;
                expected = longestFailedMatch.expected;
            }

            if(!hasMatched && length > 0 && anyItemsMatched) {
                //check for recessive matches
                boolean allRecessive = true;
                for(int i = 0; i < items.size(); i++) {
                    if(itemsMatched[i] && !items.get(i).isRecessive()) {
                        allRecessive = false;
                        break;
                    }
                }
                if(allRecessive) {
                    length = 0;
                }
            }

            TokenGroup group = new TokenGroup(this, contents.toArray(new TokenPattern<?>[0])).setName(this.name).addTags(this.tags);

            TokenMatchResponse response;
            if(hasMatched) {
                response = TokenMatchResponse.success(length, endIndex, group);
                invokeProcessors(group, lexer);
            } else {
                response = TokenMatchResponse.failure(faultyToken, length, endIndex, expected, group);
                invokeFailProcessors(group, lexer);
            }
            return response;
        } finally {
            TokenPattern.PATTERN_LIST_POOL.get().free(contents);
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        if (this.optional) {
            s.append("[");
        } else {
            s.append("<");
        }
        for (int i = 0; i < items.size(); i++) {
            s.append(items.get(i));
            if (i < items.size() - 1) {
                s.append(" ");
            }
        }
        if (this.optional) {
            s.append("]");
        } else {
            s.append(">");
        }
        return s.toString();
    }

    @Override
    public String deepToString(int levels) {
        if(levels <= 0) return toString();
        StringBuilder s = new StringBuilder();
        if (this.optional) {
            s.append("[");
        } else {
            s.append("<");
        }
        for (int i = 0; i < items.size(); i++) {
            s.append(items.get(i).deepToString(levels - 1));
            if (i < items.size() - 1) {
                s.append(" ");
            }
        }
        if (this.optional) {
            s.append("]");
        } else {
            s.append(">");
        }
        return s.toString();
    }

    @Override
    public String toTrimmedString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            sb.append(items.get(i).toTrimmedString());
            if (i < items.size() - 1) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    public TokenGroupMatch setGreedy(boolean greedy) {
        this.greedy = greedy;
        return this;
    }

    public TokenPatternMatch setSimplificationFunctionContentIndex(int contentIndex) {
        return setSimplificationFunction((d) -> d.pattern = ((TokenGroup) d.pattern).getContents()[contentIndex]);
    }
}
