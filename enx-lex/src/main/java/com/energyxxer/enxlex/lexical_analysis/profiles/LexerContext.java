package com.energyxxer.enxlex.lexical_analysis.profiles;

import com.energyxxer.enxlex.lexical_analysis.token.TokenType;

/**
 * Defines sub-routines to analyze special-case tokens.
 */
public interface LexerContext {
    /**
     * Analyzes the given substring, starting at the
     * current position of the EagerLexer, and returns information about the analysis.
     *
     * @param str The substring to analyze.
     *
     * @param startIndex
     * @param profile
     * @return A semanticContext response object containing information about the analysis.
     * */
    ScannerContextResponse analyze(String str, int startIndex, LexerProfile profile);

    default ScannerContextResponse analyzeExpectingType(String str, int startIndex, TokenType type, LexerProfile profile) {
        return analyze(str, startIndex, profile);
    }

    default ContextCondition getCondition() {
        return ContextCondition.NONE;
    }

    default boolean ignoreLeadingWhitespace() { return true; }

    boolean handlesType(TokenType type);

    enum ContextCondition {
        NONE, LINE_START, LEADING_WHITESPACE
    }
}
