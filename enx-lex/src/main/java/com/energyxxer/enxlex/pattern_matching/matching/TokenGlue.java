package com.energyxxer.enxlex.pattern_matching.matching;

import com.energyxxer.enxlex.lexical_analysis.Lexer;
import com.energyxxer.enxlex.pattern_matching.TokenMatchResponse;

import java.util.ArrayList;
import java.util.Arrays;

public class TokenGlue extends TokenPatternMatch {

    private final ArrayList<TokenPatternMatch> required = new ArrayList<>();
    private final ArrayList<TokenPatternMatch> ignored = new ArrayList<>();

    public TokenGlue(boolean required, TokenPatternMatch... patterns) {
        if(required)
            this.required.addAll(Arrays.asList(patterns));
        else
            this.ignored.addAll(Arrays.asList(patterns));
    }

    @Override
    public TokenMatchResponse match(int index, Lexer lexer) {
        lexer.setCurrentIndex(index);
        if(lexer.getLookingIndexTrimmed() == index) {
            boolean matched = true;
            for(TokenPatternMatch ignored : this.ignored) {
                TokenMatchResponse match = ignored.match(index, lexer);
                if(match.matched) {
                    matched = false;
                    match.discard();
                    break;
                }
                match.discard();
            }
            if(matched && !this.required.isEmpty()) {
                boolean valid = false;
                for(TokenPatternMatch required : this.required) {
                    TokenMatchResponse match = required.match(index, lexer);
                    if(match.matched) {
                        valid = true;
                        match.discard();
                        break;
                    }
                    match.discard();
                }
                matched = valid;
            }
            if(matched) {
                return TokenMatchResponse.success(0, index, null);
            } else {
                return TokenMatchResponse.failure(null, 0, index, this, null);
            }
        }
        return TokenMatchResponse.failure(lexer.retrieveAnyToken(), 0, index, this, null);
    }

    @Override
    public String deepToString(int levels) {
        return null;
    }

    @Override
    public String toTrimmedString() {
        return "Anything but whitespace";
    }
}
