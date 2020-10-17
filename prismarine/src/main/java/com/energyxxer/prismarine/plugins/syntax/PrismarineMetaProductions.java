package com.energyxxer.prismarine.plugins.syntax;

import com.energyxxer.enxlex.lexical_analysis.token.TokenType;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.pattern_matching.matching.lazy.TokenGroupMatch;
import com.energyxxer.enxlex.pattern_matching.matching.lazy.TokenItemMatch;
import com.energyxxer.enxlex.pattern_matching.matching.lazy.TokenListMatch;
import com.energyxxer.enxlex.pattern_matching.matching.lazy.TokenStructureMatch;

import static com.energyxxer.prismarine.plugins.syntax.PrismarineMetaLexerProfile.*;

public class PrismarineMetaProductions {
    public static final TokenStructureMatch FILE;

    static {
        FILE = new TokenStructureMatch("FILE");


    }

    private static TokenGroupMatch group(TokenPatternMatch... patterns) {
        TokenGroupMatch g = new TokenGroupMatch();
        for(TokenPatternMatch p : patterns) {
            g.append(p);
        }
        return g;
    }

    private static TokenListMatch list(TokenPatternMatch main) {
        return list(main, null);
    }

    private static TokenListMatch list(TokenPatternMatch main, TokenPatternMatch separator) {
        return new TokenListMatch(main, separator);
    }

    private static TokenGroupMatch optional(TokenPatternMatch p) {
        return new TokenGroupMatch(true).append(p);
    }

    private static TokenItemMatch ofType(TokenType type) {
        return new TokenItemMatch(type);
    }

    public static TokenItemMatch stringMatch(TokenType type, String value) {
        return new TokenItemMatch(type, value);
    }
}
